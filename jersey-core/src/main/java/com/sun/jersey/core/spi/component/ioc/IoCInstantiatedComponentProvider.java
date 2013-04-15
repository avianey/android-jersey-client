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
package com.sun.jersey.core.spi.component.ioc;

/**
 * An IoC-instantiated component provider.
 * <p>
 * The component is instantiated and injected by the underlying IoC framework,
 * but the life-cycle is managed by the runtime according to the life-cycle
 * declared by the runtime's semantics.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface IoCInstantiatedComponentProvider extends IoCComponentProvider {
    /**
     * The instance for the runtime to perform injection on.
     * <p>
     * Some IoC frameworks create proxies that proxy instances of the class to
     * enable, for example, Aspect Oriented Programming, to support cross
     * cutting functionality. 
     * If such proxies are returned then any injection of onto fields of the
     * proxy (if any) will not have any effect. The runtime requires access
     * to the proxied instance such that injection onto fields will take
     * effect.
     * 
     * @param o the component instance returned by {@link #getInstance()}.
     * @return the instance to inject on.
     */
    Object getInjectableInstance(Object o);
}