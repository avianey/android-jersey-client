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

import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.client.impl.ClientRequestImpl;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ViewResource extends Filterable implements 
        RequestBuilder<ViewResource.Builder>,
        ViewUniformInterface {
    private final Client client;

    private final URI u;
        
    /* package */ ViewResource(Client c, URI u) {
        super((ClientHandler)c);
        this.client = c;
        this.u = u;
    }

    private ViewResource(ViewResource that, UriBuilder ub) {
        super(that);
        this.client = that.client;
        this.u = ub.build();
    }
    
    /**
     * Get the URI to the resource.
     * 
     * @return the URI.
     */
    public URI getURI() {
        return u;
    }
    
    /**
     * Get the URI builder to the resource.
     * 
     * @return the URI builder.
     */
    public UriBuilder getUriBuilder() {
        return UriBuilder.fromUri(u);
    }

    /**
     * Get the ClientRequest builder.
     *
     * @return the ClientRequest builder.
     */
    public Builder getRequestBuilder() {
        return new Builder();
    }

    /**
     * @return the URI as a String instance
     */
    @Override
    public String toString() {
        return u.toString();
    }

    /**
     * Returns a hash code for this <code>WebResource</code>.
     * <p>
     * The hash code is the hash code of URI of this
     * <code>WebResource</code>.
     * 
     * @return a hash code for this <code>WebResource</code>.
     */
    @Override
    public int hashCode() {
        return u.hashCode();
    }

    /**
     * Compares this resource to the specified object.
     * <p>
     * The result is true if and only if the argument is not null and is a
     * <code>WebResource</code> object whose URI is equal to the URI of this
     * <code>WebResource</code>.
     *
     * @param obj the object to compare this <code>WebResource</code> against.
     * @return true if the <code>WebResource</code> are equal; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof ViewResource) {
            final ViewResource that = (ViewResource) obj;
            return that.u.equals(this.u);
        }
        return false;
    }

    // ViewUniformInterface
    
    public <T> T head(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "HEAD"));
    }

    public <T> T head(T t) {
        return handle(t, new ClientRequestImpl(getURI(), "HEAD"));
    }

        
    public <T> T options(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "OPTIONS"));
    }

    public <T> T options(T t) {
        return handle(t, new ClientRequestImpl(getURI(), "OPTIONS"));
    }


    public <T> T get(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "GET"));
    }

    public <T> T get(T t) {
        return handle(t, new ClientRequestImpl(getURI(), "GET"));
    }

    
    public <T> T put(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> T put(T t) {
        return handle(t, new ClientRequestImpl(getURI(), "PUT"));
    }

    public <T> T put(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }

    public <T> T put(T t, Object requestEntity) {
        return handle(t, new ClientRequestImpl(getURI(), "PUT", requestEntity));
    }
        
        
    public <T> T post(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> T post(T t) {
        return handle(t, new ClientRequestImpl(getURI(), "POST"));
    }

    public <T> T post(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }

    public <T> T post(T t, Object requestEntity) {
        return handle(t, new ClientRequestImpl(getURI(), "POST", requestEntity));
    }


    public <T> T delete(Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE"));
    }

    public <T> T delete(T t) {
        return handle(t, new ClientRequestImpl(getURI(), "DELETE"));
    }

    public <T> T delete(Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }

    public <T> T delete(T t, Object requestEntity) {
        return handle(t, new ClientRequestImpl(getURI(), "DELETE", requestEntity));
    }


    public <T> T method(String method, Class<T> c) {
        return handle(c, new ClientRequestImpl(getURI(), method));
    }

    public <T> T method(String method, T t) {
        return handle(t, new ClientRequestImpl(getURI(), method));
    }

    public <T> T method(String method, Class<T> c, Object requestEntity) {
        return handle(c, new ClientRequestImpl(getURI(), method, requestEntity));
    }

    public <T> T method(String method, T t, Object requestEntity) {
        return handle(t, new ClientRequestImpl(getURI(), method, requestEntity));
    }
    
    
    // RequestBuilder<WebResource.Builder>
    
    public Builder entity(Object entity) {
        return getRequestBuilder().entity(entity);
    }

    public Builder entity(Object entity, MediaType type) {
        return getRequestBuilder().entity(entity, type);
    }

    public Builder entity(Object entity, String type) {
        return getRequestBuilder().entity(entity, type);
    }

    public Builder type(MediaType type) {
        return getRequestBuilder().type(type);
    }
        
    public Builder type(String type) {
        return getRequestBuilder().type(type);
    }
    
    public Builder accept(MediaType... types) {
        return getRequestBuilder().accept(types);
    }

    public Builder accept(String... types) {
        return getRequestBuilder().accept(types);
    }

    public Builder acceptLanguage(Locale... locales) {
        return getRequestBuilder().acceptLanguage(locales);
    }

    public Builder acceptLanguage(String... locales) {
        return getRequestBuilder().acceptLanguage(locales);
    }

    public Builder cookie(Cookie cookie) {
        return getRequestBuilder().cookie(cookie);
    }
    
    public Builder header(String name, Object value) {
        return getRequestBuilder().header(name, value);
    }

    // URI specific building
    
    /**
     * Create a new WebResource from this web resource with an additional path
     * added to the URI of this web resource.
     * <p>
     * Any filters on this web resource are inherited. Removal of filters
     * may cause undefined behaviour.
     *
     * @param path the additional path.
     * 
     * @return the new web resource.
     */
    public ViewResource path(String path) {
        return new ViewResource(this, getUriBuilder().path(path));
    }

    /**
     * Create a new WebResource from this web resource.
     * <p>
     * If the URI contains a path component and the path starts with a '/' then
     * the path of this web resource URI is replaced. Otherwise the path is
     * appended.
     * <p>
     * If the URI contains query parameters then those query parameters will
     * replace the query parameters (if any) of this web resource.
     * <p>
     * Any filters on this web resource are inherited. Removal of filters
     * may cause undefined behaviour.
     * 
     * @param uri the URI.
     * @return the new web resource.
     */
    public ViewResource uri(URI uri) {
        UriBuilder b = getUriBuilder();
        String path = uri.getRawPath();
        if (path != null && path.length() > 0) {
            if (path.startsWith("/")) {
                b.replacePath(path);
            } else {
                b.path(path);
            }
        }
        String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            b.replaceQuery(query);        
        }
        return new ViewResource(this, b);
    }

    /**
     * Create a new WebResource from this web resource with an additional
     * query parameter added to the URI of this web resource.
     *
     * @param key the query parameter name
     * @param value the query parameter value
     * @return the new web resource.
     */
    public ViewResource queryParam(String key, String value) {
        UriBuilder b = getUriBuilder();
        b.queryParam(key, value);
        return new ViewResource(this, b);
    }

    /**
     * Create a new WebResource from this web resource with additional
     * query parameters added to the URI of this web resource.
     *
     * @param params the query parameters.
     * @return the new web resource.
     */
    public ViewResource queryParams(MultivaluedMap<String, String> params) {
        UriBuilder b = getUriBuilder();
        for (Map.Entry<String, List<String>> e : params.entrySet()) {
            for (String value : e.getValue())
                b.queryParam(e.getKey(), value);
        }
        return new ViewResource(this, b);
    }

    // Builder that builds client request and handles it
    
    /**
     * The builder for building a {@link ClientRequest} instance and 
     * handling the request using the {@link UniformInterface}. The methods
     * of the {@link UniformInterface} are the build methods of the builder.
     */
    public final class Builder extends PartialRequestBuilder<Builder> 
            implements ViewUniformInterface {


        private Builder() {
        }
        
        private ClientRequest build(String method) {
            ClientRequest ro = new ClientRequestImpl(u, method, entity, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
        
        private ClientRequest build(String method, Object e) {
            ClientRequest ro = new ClientRequestImpl(u, method, e, metadata);
            entity = null;
            metadata = null;
            return ro;
        }
        
        // ViewUniformInterface

        public <T> T head(Class<T> c) {
            return handle(c, build("HEAD"));
        }

        public <T> T head(T t) {
            return handle(t, build("HEAD"));
        }


        public <T> T options(Class<T> c) {
            return handle(c, build("OPTIONS"));
        }

        public <T> T options(T t) {
            return handle(t, build("OPTIONS"));
        }


        public <T> T get(Class<T> c) {
            return handle(c, build("GET"));
        }

        public <T> T get(T t) {
            return handle(t, build("GET"));
        }


        public <T> T put(Class<T> c) {
            return handle(c, build("PUT"));
        }

        public <T> T put(T t) {
            return handle(t, build("PUT"));
        }

        public <T> T put(Class<T> c, Object requestEntity) {
            return handle(c, build("PUT", requestEntity));
        }

        public <T> T put(T t, Object requestEntity) {
            return handle(t, build("PUT", requestEntity));
        }


        public <T> T post(Class<T> c) {
            return handle(c, build("POST"));
        }

        public <T> T post(T t) {
            return handle(t, build("POST"));
        }

        public <T> T post(Class<T> c, Object requestEntity) {
            return handle(c, build("POST", requestEntity));
        }

        public <T> T post(T t, Object requestEntity) {
            return handle(t, build("POST", requestEntity));
        }


        public <T> T delete(Class<T> c) {
            return handle(c, build("DELETE"));
        }

        public <T> T delete(T t) {
            return handle(t, build("DELETE"));
        }

        public <T> T delete(Class<T> c, Object requestEntity) {
            return handle(c, build("DELETE", requestEntity));
        }

        public <T> T delete(T t, Object requestEntity) {
            return handle(t, build("DELETE", requestEntity));
        }


        public <T> T method(String method, Class<T> c) {
            return handle(c, build(method));
        }

        public <T> T method(String method, T t) {
            return handle(t, build(method));
        }

        public <T> T method(String method, Class<T> c, Object requestEntity) {
            return handle(c, build(method, requestEntity));
        }

        public <T> T method(String method, T t, Object requestEntity) {
            return handle(t, build(method, requestEntity));
        }
    }


    private <T> T handle(Class<T> c, ClientRequest ro) {
        return client.getViewProxy(c).view(c, ro, getHeadHandler());
    }

    private <T> T handle(T t, ClientRequest ro) {
        return client.getViewProxy((Class<T>)t.getClass()).view(t, ro, getHeadHandler());
    }
}