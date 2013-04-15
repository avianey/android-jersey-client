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

package com.sun.jersey.core.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.ws.rs.core.MultivaluedMap;

/**
 * An unmodifiable view of a {@link MultivaluedMap}.
 *
 * @param <K> the key
 * @param <V> the value
 * @author Gili Tzabari
 */
public class UnmodifiableMultivaluedMap<K, V> implements MultivaluedMap<K, V>
{
	private final MultivaluedMap<K, V> delegate;

	/**
	 * Creates a new ImmutableMultivaluedMap.
	 *
	 * @param delegate the underlying MultivaluedMap
	 */
	public UnmodifiableMultivaluedMap(MultivaluedMap<K, V> delegate)
	{
		this.delegate = delegate;
	}

	public void putSingle(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	public void add(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	public V getFirst(K key)
	{
		return delegate.getFirst(key);
	}

	public int size()
	{
		return delegate.size();
	}

	public boolean isEmpty()
	{
		return delegate.isEmpty();
	}

	public boolean containsKey(Object key)
	{
		return delegate.containsKey(key);
	}

	public boolean containsValue(Object value)
	{
		return delegate.containsValue(value);
	}

	public List<V> get(Object key)
	{
		return delegate.get(key);
	}

	public List<V> put(K key, List<V> value)
	{
		throw new UnsupportedOperationException();
	}

	public List<V> remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends K, ? extends List<V>> m)
	{
		throw new UnsupportedOperationException();
	}

	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	public Set<K> keySet()
	{
		return Collections.unmodifiableSet(delegate.keySet());
	}

	public Collection<List<V>> values()
	{
		return Collections.unmodifiableCollection(delegate.values());
	}

	public Set<Entry<K, List<V>>> entrySet()
	{
		return Collections.unmodifiableSet(delegate.entrySet());
	}
}
