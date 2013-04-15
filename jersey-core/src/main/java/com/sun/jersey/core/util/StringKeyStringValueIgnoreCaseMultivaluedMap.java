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

package com.sun.jersey.core.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

/**
 * An implementation of {@link MultivaluedMap} where keys are instances of
 * String and are compared ignoring case and values are instances of String.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class StringKeyStringValueIgnoreCaseMultivaluedMap
        extends StringKeyIgnoreCaseMultivaluedMap<String> {
        
    public StringKeyStringValueIgnoreCaseMultivaluedMap() {
        super();
    }

    public StringKeyStringValueIgnoreCaseMultivaluedMap(StringKeyStringValueIgnoreCaseMultivaluedMap that) {
        super(that);
    }

    public void putSingleObject(String key, Object value) {
        List<String> l = getList(key);

        l.clear();
        if (value != null)
            l.add(value.toString());
        else
            l.add("");
    }

    public void addObject(String key, Object value) {
        List<String> l = getList(key);

        if (value != null)
            l.add(value.toString());
        else
            l.add("");
    }

    public <A> List<A> get(String key, Class<A> type) {
        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName()+" has no String constructor", ex);
        }

        ArrayList<A> l = null;
        List<String> values = get(key);
        if (values != null) {
            l = new ArrayList<A>();
            for (String value: values) {
                try {
                    l.add(c.newInstance(value));
                } catch (Exception ex) {
                    l.add(null);
                }
            }
        }
        return l;
    }

    public <A> A getFirst(String key, Class<A> type) {
        String value = getFirst(key);
        if (value == null)
            return null;
        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName()+" has no String constructor", ex);
        }
        A retVal = null;
        try {
            retVal = c.newInstance(value);
        } catch (Exception ex) {
        }
        return retVal;
    }

    @SuppressWarnings("unchecked")
    public <A> A getFirst(String key, A defaultValue) {
        String value = getFirst(key);
        if (value == null)
            return defaultValue;

        Class<A> type = (Class<A>)defaultValue.getClass();

        Constructor<A> c = null;
        try {
            c = type.getConstructor(String.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException(type.getName()+" has no String constructor", ex);
        }
        A retVal = defaultValue;
        try {
            retVal = c.newInstance(value);
        } catch (Exception ex) {
        }
        return retVal;
    }
}