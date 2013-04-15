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
package com.sun.jersey.api.client;

import com.sun.jersey.spi.MessageBodyWorkers;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A request writer for writing header values and a request entity.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class RequestWriter {
    private static final Logger LOGGER = Logger.getLogger(RequestWriter.class.getName());

    protected static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    private MessageBodyWorkers workers;

    public RequestWriter() {}

    public RequestWriter(MessageBodyWorkers workers) {
        this.workers = workers;
    }
    
    @Context
    public void setMessageBodyWorkers(MessageBodyWorkers workers) {
        this.workers = workers;
    }

    public MessageBodyWorkers getMessageBodyWorkers() {
        return workers;
    }
    
    /**
     * A lister for listening to events when writing a request entity.
     * <p>
     * The listener is registered when invoking the 
     * {@link TerminatingClientHandler#writeRequestEntity(com.sun.jersey.api.client.ClientRequest, com.sun.jersey.api.client.TerminatingClientHandler.RequestEntityWriterListener) }
     * method.
     *
     * @author Paul.Sandoz@Sun.Com
     */
    protected interface RequestEntityWriterListener {

        /**
         * Called when the size of the request entity is obtained.
         * <p>
         * Enables the appropriate setting of HTTP headers
         * for the size of the request entity and/or configure an appropriate
         * transport encoding.
         *
         * @param size the size, in bytes, of the request entity, otherwise -1
         *        if the size cannot be determined before serialization.
         * @throws java.io.IOException
         */
        void onRequestEntitySize(long size) throws IOException;

        /**
         * Called when the output stream is required to write the request
         * entity.
         *
         * @return the output stream to write the request entity.
         * @throws java.io.IOException
         */
        OutputStream onGetOutputStream() throws IOException;
    }

    /**
     * A writer for writing a request entity.
     * <p>
     * An instance of a <code>RequestEntityWriter</code> is obtained by
     * invoking the {@link TerminatingClientHandler#getRequestEntityWriter(com.sun.jersey.api.client.ClientRequest) }
     * method.
     * 
     */
    protected interface RequestEntityWriter {
        /**
         * 
         * @return size the size, in bytes, of the request entity, otherwise -1
         *         if the size cannot be determined before serialization.
         */
        long getSize();

        /**
         *
         * @return the media type of the request entity.
         */
        MediaType getMediaType();

        /**
         * Write the request entity.
         * 
         * @param out the output stream to write the request entity.
         * @throws java.io.IOException
         */
        void writeRequestEntity(OutputStream out) throws IOException;
    }

    /**
     * 
     */
    private final class RequestEntityWriterImpl implements RequestEntityWriter {
        private final ClientRequest cr;
        private final Object entity;
        private final Type entityType;
        private MediaType mediaType;
        private final long size;
        private final MessageBodyWriter bw;

        /**
         * 
         * @param cr
         */
        public RequestEntityWriterImpl(ClientRequest cr) {
            this.cr = cr;

            final Object e = cr.getEntity();
            if (e == null)
                throw new IllegalArgumentException("The entity of the client request is null");

            if (e instanceof GenericEntity) {
                final GenericEntity ge = (GenericEntity)e;
                this.entity = ge.getEntity();
                this.entityType = ge.getType();
            } else {
                this.entity = e;
                this.entityType = entity.getClass();
            }
            final Class entityClass = entity.getClass();

            final MultivaluedMap<String, Object> headers = cr.getHeaders();
            this.mediaType = RequestWriter.this.
                    getMediaType(entityClass, entityType, headers);

            this.bw = workers.getMessageBodyWriter(
                    entityClass, entityType,
                    EMPTY_ANNOTATIONS, mediaType);
            if (bw == null) {
                String message = "A message body writer for Java class " +
                        entity.getClass().getName() +
                        ", and Java type " + entityType +
                        ", and MIME media type " + mediaType + " was not found";
                LOGGER.severe(message);
                Map<MediaType, List<MessageBodyWriter>> m = workers.getWriters(mediaType);
                LOGGER.severe("The registered message body writers compatible with the MIME media type are:\n" +
                        workers.writersToString(m));

                throw new ClientHandlerException(message);
            }

            this.size = headers.containsKey(HttpHeaders.CONTENT_ENCODING)
                    ? -1
                    : bw.getSize(entity, entityClass, entityType, EMPTY_ANNOTATIONS, mediaType);
        }

        /**
         *
         * @return
         */
        public long getSize() {
            return size;
        }

        /**
         * 
         * @return
         */
        public MediaType getMediaType() {
            return mediaType;
        }

        /**
         * 
         * @param out
         * @throws java.io.IOException
         */
        public void writeRequestEntity(OutputStream out) throws IOException {
            out = cr.getAdapter().adapt(cr, out);
            try {
                bw.writeTo(entity, entity.getClass(), entityType,
                        EMPTY_ANNOTATIONS, mediaType, cr.getMetadata(),
                        out);
                out.flush();
            } finally {
                out.close();
            }
        }
    }

    /**
     * Get a request entity writer capable of writing the request entity.
     * 
     * @param ro the client request.
     * @return the request entity writer.
     */
    protected RequestEntityWriter getRequestEntityWriter(final ClientRequest ro) {
        return new RequestEntityWriterImpl(ro);
    }

    /**
     * Write a request entity using an appropriate message body writer.
     * <p>
     * The method {@link RequestEntityWriterListener#onRequestEntitySize(long) } will be invoked
     * with the size of the request entity to be serialized.
     * The method {@link RequestEntityWriterListener#onGetOutputStream() } will be invoked
     * when the output stream is required to write the request entity.
     * 
     * @param ro the client request containing the request entity. If the
     *        request entity is null then the method will not write any entity.
     * @param listener the request entity listener.
     * @throws java.io.IOException
     */
    protected void writeRequestEntity(ClientRequest ro,
            RequestEntityWriterListener listener) throws IOException {
        Object entity = ro.getEntity();
        if (entity == null)
            return;

        Type entityType = null;
        if (entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)entity;
            entityType = ge.getType();
            entity = ge.getEntity();
        } else {
            entityType = entity.getClass();
        }
        final Class entityClass = entity.getClass();


        final MultivaluedMap<String, Object> headers = ro.getHeaders();
        final MediaType mediaType = getMediaType(entityClass, entityType, headers);

        final MessageBodyWriter bw = workers.getMessageBodyWriter(
                entityClass, entityType,
                EMPTY_ANNOTATIONS, mediaType);
        if (bw == null) {
            throw new ClientHandlerException(
                    "A message body writer for Java type, " + entity.getClass() +
                    ", and MIME media type, " + mediaType + ", was not found");
        }

        final long size = headers.containsKey(HttpHeaders.CONTENT_ENCODING) 
                ? -1 
                : bw.getSize(entity, entityClass, entityType, EMPTY_ANNOTATIONS, mediaType);
        listener.onRequestEntitySize(size);

        final OutputStream out = ro.getAdapter().adapt(ro, listener.onGetOutputStream());
        try {
            bw.writeTo(entity, entityClass, entityType,
                    EMPTY_ANNOTATIONS, mediaType, headers, out);
            out.flush();
        } catch (IOException ex) {
            try { out.close(); } catch (Exception e) { }
            throw ex;
        } catch (RuntimeException ex) {
            try { out.close(); } catch (Exception e) { }
            throw ex;
        }

        out.close();
    }


    private MediaType getMediaType(Class entityClass, Type entityType,
            MultivaluedMap<String, Object> headers) {
        final Object mediaTypeHeader = headers.getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            return (MediaType)mediaTypeHeader;
        } else if (mediaTypeHeader != null) {
            return MediaType.valueOf(mediaTypeHeader.toString());
        } else {
            // Content-Type is not present choose a default type
            final List<MediaType> mediaTypes = workers.getMessageBodyWriterMediaTypes(
                    entityClass, entityType, EMPTY_ANNOTATIONS);
            final MediaType mediaType = getMediaType(mediaTypes);
            headers.putSingle("Content-Type", mediaType);
            return mediaType;
        }
    }

    private MediaType getMediaType(List<MediaType> mediaTypes) {
        if (mediaTypes.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        } else {
            MediaType mediaType = mediaTypes.get(0);
            if (mediaType.isWildcardType() || mediaType.isWildcardSubtype())
                mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            return mediaType;
        }
    }

}