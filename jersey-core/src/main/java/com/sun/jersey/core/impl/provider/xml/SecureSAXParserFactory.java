/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Portions contributed by Joseph Walton (Atlassian)
 */

package com.sun.jersey.core.impl.provider.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import com.sun.jersey.core.util.SaxHelper;
import com.sun.jersey.impl.ImplMessages;

import org.xml.sax.EntityResolver;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Martin Matula (martin.matula at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
final class SecureSAXParserFactory extends SAXParserFactory {
    private static final Logger LOGGER = Logger.getLogger(SecureSAXParserFactory.class.getName());
    private static final EntityResolver EMPTY_ENTITY_RESOLVER = new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    };

    private final SAXParserFactory spf;

    SecureSAXParserFactory(SAXParserFactory spf) {
        this.spf = spf;

        if (SaxHelper.isXdkParserFactory(spf)) {
            LOGGER.log(Level.WARNING, ImplMessages.SAX_XDK_NO_SECURITY_FEATURES());
        } else {
            try {
                spf.setFeature("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
                spf.setFeature("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
            } catch (Exception ex) {
                throw new RuntimeException(ImplMessages.SAX_CANNOT_ENABLE_SECURITY_FEATURES(),  ex);
            }

            try {
                spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ImplMessages.SAX_CANNOT_ENABLE_SECURE_PROCESSING_FEATURE(), ex);
            }
        }
    }

    @Override
    public void setNamespaceAware(boolean b) {
        spf.setNamespaceAware(b);
    }

    @Override
    public void setValidating(boolean b) {
        spf.setValidating(b);
    }

    @Override
    public boolean isNamespaceAware() {
        return spf.isNamespaceAware();
    }

    @Override
    public boolean isValidating() {
        return spf.isValidating();
    }

    @Override
    public Schema getSchema() {
        return spf.getSchema();
    }

    @Override
    public void setSchema(Schema schema) {
        spf.setSchema(schema);
    }

    @Override
    public void setXIncludeAware(boolean b) {
        spf.setXIncludeAware(b);
    }

    @Override
    public boolean isXIncludeAware() {
        return spf.isXIncludeAware();
    }

    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        return new WrappingSAXParser(spf.newSAXParser());
    }

    @Override
    public void setFeature(String s, boolean b) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        spf.setFeature(s, b);
    }

    @Override
    public boolean getFeature(String s) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return spf.getFeature(s);
    }

    private static final class WrappingSAXParser extends SAXParser {
        private final SAXParser sp;

        protected WrappingSAXParser(SAXParser sp) {
            this.sp = sp;
        }

        @Override
        public void reset() {
            sp.reset();
        }

        @Override
        public void parse(InputStream inputStream, HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(inputStream, handlerBase);
        }

        @Override
        public void parse(InputStream inputStream, HandlerBase handlerBase, String s) throws SAXException, IOException {
            sp.parse(inputStream, handlerBase, s);
        }

        @Override
        public void parse(InputStream inputStream, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(inputStream, defaultHandler);
        }

        @Override
        public void parse(InputStream inputStream, DefaultHandler defaultHandler, String s) throws SAXException, IOException {
            sp.parse(inputStream, defaultHandler, s);
        }

        @Override
        public void parse(String s, HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(s, handlerBase);
        }

        @Override
        public void parse(String s, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(s, defaultHandler);
        }

        @Override
        public void parse(File file, HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(file, handlerBase);
        }

        @Override
        public void parse(File file, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(file, defaultHandler);
        }

        @Override
        public void parse(InputSource inputSource, HandlerBase handlerBase) throws SAXException, IOException {
            sp.parse(inputSource, handlerBase);
        }

        @Override
        public void parse(InputSource inputSource, DefaultHandler defaultHandler) throws SAXException, IOException {
            sp.parse(inputSource, defaultHandler);
        }

        @Override
        public Parser getParser() throws SAXException {
            return sp.getParser();
        }

        @Override
        public XMLReader getXMLReader() throws SAXException {
            XMLReader r = sp.getXMLReader();
            r.setEntityResolver(EMPTY_ENTITY_RESOLVER);
            return r;
        }

        @Override
        public boolean isNamespaceAware() {
            return sp.isNamespaceAware();
        }

        @Override
        public boolean isValidating() {
            return sp.isValidating();
        }

        @Override
        public void setProperty(String s, Object o) throws SAXNotRecognizedException, SAXNotSupportedException {
            sp.setProperty(s, o);
        }

        @Override
        public Object getProperty(String s) throws SAXNotRecognizedException, SAXNotSupportedException {
            return sp.getProperty(s);
        }

        @Override
        public Schema getSchema() {
            return sp.getSchema();
        }

        @Override
        public boolean isXIncludeAware() {
            return sp.isXIncludeAware();
        }
    }
}
