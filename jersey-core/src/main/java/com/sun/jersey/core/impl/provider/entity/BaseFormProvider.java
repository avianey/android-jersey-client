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

package com.sun.jersey.core.impl.provider.entity;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.jersey.core.util.ReaderWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class BaseFormProvider<T extends MultivaluedMap<String, String>> extends AbstractMessageReaderWriterProvider<T> {
    
    public T readFrom(T map,
            MediaType mediaType,
            InputStream entityStream) throws IOException {
        final String encoded = readFromAsString(entityStream, mediaType);

        final String charsetName = ReaderWriter.getCharset(mediaType).name();

        final StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
        String token;
        try {
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                int idx = token.indexOf('=');
                if (idx < 0) {
                    map.add(URLDecoder.decode(token, charsetName), null);
                } else if (idx > 0) {
                    map.add(URLDecoder.decode(token.substring(0, idx), charsetName),
                            URLDecoder.decode(token.substring(idx+1), charsetName));
                }
            }
            return map;
        } catch (IllegalArgumentException ex) {
            throw new WebApplicationException(ex, Status.BAD_REQUEST);
        }
    }

    public void writeTo(
            T t,
            MediaType mediaType, 
            OutputStream entityStream) throws IOException {
        final String charsetName = ReaderWriter.getCharset(mediaType).name();
        
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : t.entrySet()) {
            for (String value : e.getValue()) {
                if (sb.length() > 0)
                    sb.append('&');
                sb.append(URLEncoder.encode(e.getKey(), charsetName));
                if (value != null) {
                    sb.append('=');
                    sb.append(URLEncoder.encode(value, charsetName));
                }
            }
        }
                
        writeToAsString(sb.toString(), entityStream, mediaType);
    }    
}