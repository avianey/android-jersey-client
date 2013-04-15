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

package com.sun.jersey.core.provider.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

/**
 * An abstract provider for {@link JAXBElement}.
 * <p>
 * Implementing classes may extend this class to provide specific marshalling
 * and unmarshalling behaviour.
 * <p>
 * When unmarshalling a {@link UnmarshalException} will result in a
 * {@link WebApplicationException} being thrown with a status of 400
 * (Client error), and a {@link JAXBException} will result in a
 * {@link WebApplicationException} being thrown with a status of 500
 * (Internal Server error).
 * <p>
 * When marshalling a {@link JAXBException} will result in a
 * {@link WebApplicationException} being thrown with a status of 500
 * (Internal Server error).
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractJAXBElementProvider extends AbstractJAXBProvider<JAXBElement<?>> {    
    public AbstractJAXBElementProvider(Providers ps) {
        super(ps);
    }
    
    public AbstractJAXBElementProvider(Providers ps, MediaType mt) {
        super(ps, mt);        
    }
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return type == JAXBElement.class && genericType instanceof ParameterizedType && isSupported(mediaType);
    }
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return JAXBElement.class.isAssignableFrom(type) && isSupported(mediaType);
    }
            
    @Override
    public final JAXBElement<?> readFrom(
            Class<JAXBElement<?>> type, 
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        final ParameterizedType pt = (ParameterizedType)genericType;
        final Class ta = (Class)pt.getActualTypeArguments()[0];
        
        try {
            return readFrom(ta, mediaType, getUnmarshaller(ta, mediaType), entityStream);
        } catch (UnmarshalException ex) {
            throw new WebApplicationException(ex, Status.BAD_REQUEST);
        } catch (JAXBException ex) {
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }    
    }

    protected abstract JAXBElement<?> readFrom(Class<?> type, MediaType mediaType,
            Unmarshaller u, InputStream entityStream)
            throws JAXBException;
    
    
    @Override
    public final void writeTo(
            JAXBElement<?> t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            final Marshaller m = getMarshaller(t.getDeclaredType(), mediaType);
            final Charset c = getCharset(mediaType);
            if (c != UTF8) {
                m.setProperty(Marshaller.JAXB_ENCODING, c.name());
            }
            setHeader(m, annotations);
            writeTo(t, mediaType, c, m, entityStream);
        } catch (JAXBException ex) {
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected abstract void writeTo(JAXBElement<?> t, MediaType mediaType, Charset c,
            Marshaller m, OutputStream entityStream)
            throws JAXBException;
}