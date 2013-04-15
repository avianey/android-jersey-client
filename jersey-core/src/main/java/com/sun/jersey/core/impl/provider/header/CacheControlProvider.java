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

package com.sun.jersey.core.impl.provider.header;

import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.jersey.spi.HeaderDelegateProvider;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.CacheControl;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 * @author hubick@java.net
 */
public final class CacheControlProvider implements HeaderDelegateProvider<CacheControl> {
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    
    private static final Pattern COMMA_SEPARATED_LIST = Pattern.compile("[\\s]*,[\\s]*");
    
    public boolean supports(Class<?> type) {
        return type == CacheControl.class;
    }

    public String toString(CacheControl header) {
        StringBuffer b = new StringBuffer();
        if (header.isPrivate())
            appendQuotedWithSeparator(b, "private", buildListValue(header.getPrivateFields()));
        if (header.isNoCache())
            appendQuotedWithSeparator(b, "no-cache", buildListValue(header.getNoCacheFields()));
        if (header.isNoStore())
            appendWithSeparator(b, "no-store");
        if (header.isNoTransform())
            appendWithSeparator(b, "no-transform");
        if (header.isMustRevalidate())
            appendWithSeparator(b, "must-revalidate");
        if (header.isProxyRevalidate())
            appendWithSeparator(b, "proxy-revalidate");
        if (header.getMaxAge() != -1)
            appendWithSeparator(b, "max-age", header.getMaxAge());
        if (header.getSMaxAge() != -1)
            appendWithSeparator(b, "s-maxage", header.getSMaxAge());
        
        for (Map.Entry<String, String> e : header.getCacheExtension().entrySet()) {
            appendWithSeparator(b, e.getKey(), quoteIfWhitespace(e.getValue()));
        }
                    
        return b.toString();        
    }

    private void readFieldNames(List<String> fieldNames, HttpHeaderReader reader, String directiveName)
            throws ParseException {
        if (!reader.hasNextSeparator('=', false))
            return;
        reader.nextSeparator('=');
        fieldNames.addAll(Arrays.asList(COMMA_SEPARATED_LIST.split(reader.nextQuotedString())));
        return;
    }

    private int readIntValue(HttpHeaderReader reader, String directiveName)
            throws ParseException {
        reader.nextSeparator('=');
        int index = reader.getIndex();
        try {
            return Integer.parseInt(reader.nextToken());
        } catch (NumberFormatException nfe) {
            ParseException pe = new ParseException(
                    "Error parsing integer value for " + directiveName + " directive", index);
            pe.initCause(nfe);
            throw pe;
        }
    }

    private void readDirective(CacheControl cacheControl,
            HttpHeaderReader reader) throws ParseException {
        String directiveName = reader.nextToken();
        if (directiveName.equalsIgnoreCase("private")) {
            cacheControl.setPrivate(true);
            readFieldNames(cacheControl.getPrivateFields(), reader, directiveName);
        } else if (directiveName.equalsIgnoreCase("public")) {
            // CacheControl API doesn't support 'public' for some reason.
            cacheControl.getCacheExtension().put(directiveName, null);
        } else if (directiveName.equalsIgnoreCase("no-cache")) {
            cacheControl.setNoCache(true);
            readFieldNames(cacheControl.getNoCacheFields(), reader, directiveName);
        } else if (directiveName.equalsIgnoreCase("no-store")) {
            cacheControl.setNoStore(true);
        } else if (directiveName.equalsIgnoreCase("no-transform")) {
            cacheControl.setNoTransform(true);
        } else if (directiveName.equalsIgnoreCase("must-revalidate")) {
            cacheControl.setMustRevalidate(true);
        } else if (directiveName.equalsIgnoreCase("proxy-revalidate")) {
            cacheControl.setProxyRevalidate(true);
        } else if (directiveName.equalsIgnoreCase("max-age")) {
            cacheControl.setMaxAge(readIntValue(reader, directiveName));
        } else if (directiveName.equalsIgnoreCase("s-maxage")) {
            cacheControl.setSMaxAge(readIntValue(reader, directiveName));
        } else {
            String value = null;
            if (reader.hasNextSeparator('=', false)) {
                reader.nextSeparator('=');
                value = reader.nextTokenOrQuotedString();
            }
            cacheControl.getCacheExtension().put(directiveName, value);
        }
        return;
    }

    public CacheControl fromString(String header) {
        if (header == null)
            throw new IllegalArgumentException("Cache control is null");
        try {
            HttpHeaderReader reader = HttpHeaderReader.newInstance(header);
            CacheControl cacheControl = new CacheControl();
            cacheControl.setNoTransform(false); // defaults to true
            while (reader.hasNext()) {
                readDirective(cacheControl, reader);
                if (reader.hasNextSeparator(',', true))
                    reader.nextSeparator(',');
            }
            return cacheControl;
        } catch (ParseException pe) {
            throw new IllegalArgumentException(
                    "Error parsing cache control '" + header + "'", pe);
        }
    }
    
    private void appendWithSeparator(StringBuffer b, String field) {
        if (b.length()>0)
            b.append(", ");
        b.append(field);
    }
    
    private void appendQuotedWithSeparator(StringBuffer b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && value.length() > 0) {
            b.append("=\"");
            b.append(value);
            b.append("\"");
        }
    }

    private void appendWithSeparator(StringBuffer b, String field, String value) {
        appendWithSeparator(b, field);
        if (value != null && value.length() > 0) {
        b.append("=");
            b.append(value);
        }
    }

    private void appendWithSeparator(StringBuffer b, String field, int value) {
        appendWithSeparator(b, field);
        b.append("=");
        b.append(value);
    }

    private String buildListValue(List<String> values) {
        StringBuffer b = new StringBuffer();
        for (String value: values)
            appendWithSeparator(b, value);
        return b.toString();
    }
    
    private String quoteIfWhitespace(String value) {
        if (value==null)
            return null;
        Matcher m = WHITESPACE.matcher(value);
        if (m.find()) {
            return "\""+value+"\"";
        }
        return value;
    }    
}
