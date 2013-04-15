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
package com.sun.jersey.core.spi.scanning;

import java.io.IOException;
import java.io.InputStream;

/**
 * A listener for receiving events on resources from a {@link Scanner}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface ScannerListener {

    /**
     * Accept a scanned resource.
     * <p>
     * This method will be invoked by a {@link Scanner} to ascertain if the
     * listener accepts the resource for processing. If acceptable then
     * the {@link Scanner} will then invoke the
     * {@link #onProcess(java.lang.String, java.io.InputStream) } method.
     *
     * @param name the resource name.
     * @return true if the resource is accepted for processing, otherwise false.
     */
    boolean onAccept(String name);

    /**
     * Process a scanned resource.
     * <p>
     * This method will be invoked after the listener has accepted the
     * resource.
     *
     * @param name the resource name.
     * @param in the input stream of the resource
     * @throws IOException if an error occurs when processing the resource.
     */
    void onProcess(String name, InputStream in) throws IOException;
}