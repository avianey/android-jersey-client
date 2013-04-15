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
package com.sun.jersey.core.spi.factory;

import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.InjectableProviderContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderServices.ProviderListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A factory for managing {@link InjectableProvider} instances.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectableProviderFactory implements InjectableProviderContext {
    
    private static final class MetaInjectableProvider {
        final InjectableProvider ip;
        final Class<? extends Annotation> ac;
        final Class<?> cc;
        
        MetaInjectableProvider(
                InjectableProvider ip,
                Class<? extends Annotation> ac, 
                Class<?> cc) {
            this.ip = ip;
            this.ac = ac;
            this.cc = cc;
        }
    }
    
    private final Map<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>> ipm =
            new HashMap<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>>();

    public final void update(InjectableProviderFactory ipf) {
        for (Map.Entry<Class<? extends Annotation>, LinkedList<MetaInjectableProvider>> e : ipf.ipm.entrySet()) {
            getList(e.getKey()).addAll(e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public final void add(InjectableProvider ip) {
        Type[] args = getMetaArguments(ip.getClass());
        if (args != null) {
            MetaInjectableProvider mip = new MetaInjectableProvider(ip, 
                    (Class)args[0], (Class)args[1]);
            
            // TODO change to add first
            getList(mip.ac).add(mip);
        } else {
            // TODO throw exception or log error            
        }
    }

    public final void configure(ProviderServices providerServices) {
        providerServices.getProvidersAndServices(InjectableProvider.class,
                new ProviderListener<InjectableProvider>() {
                    public void onAdd(InjectableProvider ip) {
                        add(ip);
                    }
        });
    }
    
    public final void configureProviders(ProviderServices providerServices) {
        providerServices.getProviders(InjectableProvider.class,
                new ProviderListener<InjectableProvider>() {
                    public void onAdd(InjectableProvider ip) {
                        add(ip);
                    }
        });
    }

    private LinkedList<MetaInjectableProvider> getList(Class<? extends Annotation> c) {
        LinkedList<MetaInjectableProvider> l = ipm.get(c);
        if (l == null) {
            l = new LinkedList<MetaInjectableProvider>();
            ipm.put(c, l);
        }
        return l;
    }
    
    private Type[] getMetaArguments(Class<? extends InjectableProvider> c) {
        Class _c = c;
        while (_c != Object.class) {
            Type[] ts = _c.getGenericInterfaces();
            for (Type t : ts) {
                if (t instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)t;
                    if (pt.getRawType() == InjectableProvider.class) {
                        Type[] args = pt.getActualTypeArguments();
                        for (int i = 0; i < args.length; i++)
                            args[i] = getResolvedType(args[i], c, _c);
                        if (args[0] instanceof Class &&
                                args[1] instanceof Class)
                            return args;
                    }
                }
            }
            
            _c = _c.getSuperclass();
        }
        
        return null;        
    }
    
    private Type getResolvedType(Type t, Class c, Class dc) {
        if (t instanceof Class)
            return t;
        else if (t instanceof TypeVariable) {
            ReflectionHelper.ClassTypePair ct = ReflectionHelper.
                    resolveTypeVariable(c, dc, (TypeVariable)t);
            if (ct != null)
                return ct.c;
            else 
                return t;
        } else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            return pt.getRawType();
        } else
            return t;
    }
    
    private List<MetaInjectableProvider> findInjectableProviders(
            Class<? extends Annotation> ac, 
            Class<?> cc,
            ComponentScope s) {
        List<MetaInjectableProvider> subips = new ArrayList<MetaInjectableProvider>();        
        for (MetaInjectableProvider i : getList(ac)) {
            if (s == i.ip.getScope()) {
                if (i.cc.isAssignableFrom(cc)) {
                    subips.add(i);                        
                }
            }
        }
        
        return subips;    
    }
    
    // InjectableProviderContext

    public boolean isAnnotationRegistered(Class<? extends Annotation> ac,
            Class<?> cc) {
        for (MetaInjectableProvider i : getList(ac)) {
            if (i.cc.isAssignableFrom(cc)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInjectableProviderRegistered(Class<? extends Annotation> ac,
            Class<?> cc,
            ComponentScope s) {
        return !findInjectableProviders(ac, cc, s).isEmpty();
    }
    
    public final <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            ComponentContext ic,
            A a,
            C c,
            ComponentScope s) {
        for (MetaInjectableProvider mip : findInjectableProviders(ac, c.getClass(), s)) {
            Injectable i = mip.ip.getInjectable(ic, a, c);
            if (i != null)
                return i;
        }
        return null;
    }
    
    public final <A extends Annotation, C> Injectable getInjectable(
            Class<? extends Annotation> ac,             
            ComponentContext ic,
            A a,
            C c,
            List<ComponentScope> ls) {
        for (ComponentScope s : ls) {
            Injectable i = getInjectable(ac, ic, a, c, s);
            if (i != null)
                return i;
            else {
            }
        }
        
        return null;
    }

    public <A extends Annotation, C> InjectableScopePair getInjectableWithScope(
            Class<? extends Annotation> ac,
            ComponentContext ic,
            A a,
            C c,
            List<ComponentScope> ls) {
        for (ComponentScope s : ls) {
            Injectable i = getInjectable(ac, ic, a, c, s);
            if (i != null)
                return new InjectableScopePair(i, s);
        }
        return null;
    }
}