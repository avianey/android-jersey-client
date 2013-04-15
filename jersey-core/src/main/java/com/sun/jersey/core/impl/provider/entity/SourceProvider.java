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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.jersey.spi.inject.Injectable;

/**
 *
 * @author Paul Sandoz (paul.sandoz at oracle.com)
 */
public final class SourceProvider {

    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class StreamSourceReader implements
            MessageBodyReader<StreamSource> {
        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return StreamSource.class == t;
        }

        public StreamSource readFrom(
                Class<StreamSource> t,
                Type gt,
                Annotation[] as,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            return new StreamSource(entityStream);
        }
    }

    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class SAXSourceReader implements
            MessageBodyReader<SAXSource> {
        // Delay construction of factory
        private final Injectable<SAXParserFactory> spf;

        public SAXSourceReader(@Context Injectable<SAXParserFactory> spf) {
            this.spf = spf;
        }

        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return SAXSource.class == t;
        }

        public SAXSource readFrom(
                Class<SAXSource> t,
                Type gt,
                Annotation[] as,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            try {
                return new SAXSource(spf.getValue().newSAXParser().getXMLReader(),
                        new InputSource(entityStream));
            } catch (SAXParseException ex) {
                throw new WebApplicationException(ex, Status.BAD_REQUEST);
            } catch (SAXException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            } catch (ParserConfigurationException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class DOMSourceReader implements
            MessageBodyReader<DOMSource> {
        // Delay construction of factory
        private final Injectable<DocumentBuilderFactory> dbf;

        public DOMSourceReader(@Context Injectable<DocumentBuilderFactory> dbf) {
            this.dbf = dbf;
        }

        public boolean isReadable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return DOMSource.class == t;
        }

        public DOMSource readFrom(
                Class<DOMSource> t,
                Type gt,
                Annotation[] as,
                MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            try {
                Document d = dbf.getValue().newDocumentBuilder().parse(entityStream);
                return new DOMSource(d);
            } catch (SAXParseException ex) {
                throw new WebApplicationException(ex, Status.BAD_REQUEST);
            } catch (SAXException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            } catch (ParserConfigurationException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @Produces({"application/xml", "text/xml", "*/*"})
    @Consumes({"application/xml", "text/xml", "*/*"})
    public static final class SourceWriter implements
            MessageBodyWriter<Source> {

        // Delay construction of factory
        private final Injectable<SAXParserFactory> spf;

        // Delay construction of factory
        private final Injectable<TransformerFactory> tf;

        public SourceWriter(@Context Injectable<SAXParserFactory> spf,
                @Context Injectable<TransformerFactory> tf) {
            this.spf = spf;
            this.tf = tf;
        }

        public boolean isWriteable(Class<?> t, Type gt, Annotation[] as, MediaType mediaType) {
            return Source.class.isAssignableFrom(t);
        }

        public long getSize(Source o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(Source o, Class<?> t, Type gt, Annotation[] as,
                MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {

            try {
                if (o instanceof StreamSource) {
                    StreamSource s = (StreamSource)o;
                    InputSource is = new InputSource(s.getInputStream());
                    o = new SAXSource(spf.getValue().newSAXParser().getXMLReader(), is);
                }
                StreamResult sr = new StreamResult(entityStream);
                tf.getValue().newTransformer().transform(o, sr);
            } catch (SAXException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            } catch (ParserConfigurationException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            } catch (TransformerException ex) {
                throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
