/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.core.header.reader;

import java.text.ParseException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
/* package */ class HttpHeaderListAdapter extends HttpHeaderReader {
    private HttpHeaderReader reader;

    boolean isTerminated;

    public HttpHeaderListAdapter(HttpHeaderReader reader) {
        this.reader = reader;
    }

    public void reset() {
        isTerminated = false;
    }


    @Override
    public boolean hasNext() {
        if (isTerminated)
            return false;

        if (reader.hasNext()) {
            if (reader.hasNextSeparator(',', true)) {
                isTerminated = true;
                return false;
            } else
                return true;
        }

        return false;
    }

    @Override
    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (isTerminated)
            return false;

        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            return false;
        } else
            return reader.hasNextSeparator(separator, skipWhiteSpace);
    }

    @Override
    public Event next() throws ParseException {
        return next(true);
    }

    @Override
    public HttpHeaderReader.Event next(boolean skipWhiteSpace) throws ParseException {
        return next(skipWhiteSpace, false);
    }

    @Override
    public HttpHeaderReader.Event next(boolean skipWhiteSpace, boolean preserveBackslash) throws ParseException {
        if (isTerminated)
            throw new ParseException("End of header", getIndex());

        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            throw new ParseException("End of header", getIndex());
        }

        return reader.next(skipWhiteSpace, preserveBackslash);
    }

    @Override
    public String nextSeparatedString(char startSeparator, char endSeparator) throws ParseException {
        if (isTerminated)
            throw new ParseException("End of header", getIndex());

        if (reader.hasNextSeparator(',', true)) {
            isTerminated = true;
            throw new ParseException("End of header", getIndex());
        }

        return reader.nextSeparatedString(startSeparator, endSeparator);
    }

    @Override
    public HttpHeaderReader.Event getEvent() {
        return reader.getEvent();
    }

    @Override
    public String getEventValue() {
        return reader.getEventValue();
    }

    @Override
    public String getRemainder() {
        return reader.getRemainder();
    }

    @Override
    public int getIndex() {
        return reader.getIndex();
    }
}
