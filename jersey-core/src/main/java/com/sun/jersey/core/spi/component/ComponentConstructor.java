/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.core.spi.component;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProviderContext;
import com.sun.jersey.spi.inject.Errors;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A constructor of a component.
 *
 * @param <T> the type to construct
 * @author Paul.Sandoz@Sun.Com
 */
public class ComponentConstructor<T> {
    /**
     * A tuple of a constructor and the list of injectables associated with
     * the parameters of the constructor.
     * 
     * @param <T> the type to construct.
     */
    private static class ConstructorInjectablePair<T> {
        /**
         * The constructor.
         */
        private final Constructor<T> con;

        /**
         * The list of injectables associated with the parameters of the
         * constructor;
         */
        private final List<Injectable> is;

        /**
         * Create a new tuple of a constructor and list of injectables.
         * 
         * @param con the constructor
         * @param is the list of injectables.
         */
        private ConstructorInjectablePair(Constructor<T> con, List<Injectable> is) {
            this.con = con;
            this.is = is;
        }
    }
    
    private static class ConstructorComparator<T> implements Comparator<ConstructorInjectablePair<T>> {
        @Override
        public int compare(ConstructorInjectablePair<T> o1, ConstructorInjectablePair<T> o2) {
            int p = Collections.frequency(o1.is, null) - Collections.frequency(o2.is, null);
            if (p != 0)
                return p;

            return o2.con.getParameterTypes().length - o1.con.getParameterTypes().length;
        }
    }
    
    private final InjectableProviderContext ipc;

    private final Class<T> c;

    private final List<Method> postConstructs;

    private final ComponentInjector<T> ci;
    
    public ComponentConstructor(InjectableProviderContext ipc, Class<T> c, ComponentInjector<T> ci) {
        this.ipc = ipc;
        this.c = c;
        this.ci = ci;
        this.postConstructs = getPostConstructMethods(c);
    }

    private static List<Method> getPostConstructMethods(Class c) {
        Class postConstructClass = ReflectionHelper.classForName("javax.annotation.PostConstruct");
        LinkedList<Method> list = new LinkedList<Method>();
        HashSet<String> names = new HashSet<String>();
        if (postConstructClass != null) {
            MethodList methodList = new MethodList(c, true);
            for (AnnotatedMethod m : methodList.
                    hasAnnotation(postConstructClass).
                    hasNumParams(0).
                    hasReturnType(void.class)) {
                Method method = m.getMethod();
                // only add method if not hidden/overridden
                if (names.add(method.getName())) {
                    ReflectionHelper.setAccessibleMethod(method);
                    // methods from the superclass should go first, so inserting at the beginning
                    list.addFirst(method);
                }
            }
        }
        return list;
    }

    /**
     * Get a new instance.
     *
     * @return a new instance.
     */
    public T getInstance()
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final int modifiers = c.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            Errors.nonPublicClass(c);
        }

        if (Modifier.isAbstract(modifiers)) {
            if (Modifier.isInterface(modifiers)) {
                Errors.interfaceClass(c);
            } else {
                Errors.abstractClass(c);
            }
        }

        if (c.getEnclosingClass() != null && !Modifier.isStatic(modifiers)) {
            Errors.innerClass(c);
        }

        if (Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers)) {
            if (c.getConstructors().length == 0) {
                Errors.nonPublicConstructor(c);
            }
        }
        
        final T t = _getInstance();
        ci.inject(t);
        for (Method postConstruct : postConstructs) {
            postConstruct.invoke(t);
        }
        return t;
    }

    private T _getInstance()
            throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        ConstructorInjectablePair<T> cip = getConstructor();
        if (cip == null || cip.is.isEmpty()) {
            return c.newInstance();
        } else {
            if (cip.is.contains(null)) {
                // Missing dependency
                for (int i = 0; i < cip.is.size(); i++) {
                    if (cip.is.get(i) == null) {
                        Errors.missingDependency(cip.con, i);
                    }
                }
            }

            Object[] params = new Object[cip.is.size()];
            int i = 0;
            for (Injectable injectable : cip.is) {
                if (injectable != null)
                    params[i++] = injectable.getValue();
            }
            return cip.con.newInstance(params);
        }
    }

    /**
     * Get the most suitable constructor. The constructor with the most
     * parameters and that has the most parameters associated with 
     * Injectable instances will be chosen.
     * 
     * @param <T> The type to construct.
     * @param c the class to instantiate.
     * @return a list of constructor and list of injectables for the constructor 
     *         parameters.
     */
    private ConstructorInjectablePair<T> getConstructor() {
        if (c.getConstructors().length == 0)
            return null;
        
        SortedSet<ConstructorInjectablePair<T>> cs = new TreeSet<ConstructorInjectablePair<T>>(
                new ConstructorComparator());
        
        AnnotatedContext aoc = new AnnotatedContext();
        for (Constructor con : c.getConstructors()) {
            List<Injectable> is = new ArrayList<Injectable>();
            int ps = con.getParameterTypes().length;
            aoc.setAccessibleObject(con);
            for (int p = 0; p < ps; p++) {
                Type pgtype = con.getGenericParameterTypes()[p];
                Annotation[] as = con.getParameterAnnotations()[p];
                aoc.setAnnotations(as);
                Injectable i = null;
                for (Annotation a : as) {
                    i = ipc.getInjectable(
                            a.annotationType(), aoc, a, pgtype, 
                            ComponentScope.UNDEFINED_SINGLETON);
                }
                is.add(i);
            }
            cs.add(new ConstructorInjectablePair<T>(con, is));
        }
                
        return cs.first();
    }
}