/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2008-2009, Marco Terzer, Zurich, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Swiss Federal Institute of Technology Zurich 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */
package ch.javasoft.util.map;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The <code>SingleValueMap</code> is pretty much the same as a {@link Map}, but
 * it implements the {@link MultiValueMap} interface, e.g. to use normal maps in 
 * a join (see {@link JoinedMultiValueMap}). 
 * <p>
 * This class does not copy the values of the original java map, but
 * encapsulates the java map. 
 */
public class SingleValueMap<K, V> extends AbstractMultiValueMap<K, V> implements Serializable {
	
	private static final long serialVersionUID = 3091457636828431277L;
	
	private final Map<K, V> map;
	
	public SingleValueMap(Map<K, V> map) {
		this.map = map;
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object key) {
		return map.containsKey(key);
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.MultiValueMap#count(java.lang.Object)
	 */
	public int count(Object key) {
		return map.containsKey(key) ? 1 : 0;
	}
	public boolean contains(Object key, V value) {
		final Object myval = map.get(key);
		return myval != null && myval.equals(value);
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.MultiValueMap#get(java.lang.Object)
	 */
	public Collection<V> get(Object key) {
		return Collections.singleton(map.get(key));
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#getFirst(java.lang.Object)
	 */
	@Override
	public V getFirst(Object key) {
		return map.get(key);
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.MultiValueMap#keySet()
	 */
	public Set<K> keySet() {
		return map.keySet();
	}
	/**
	 * Implementation returns the underlying map.
	 * <p>
	 * Changing the returned map also affects this single value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#asSingleValueMap()
	 */
	@Override
	public Map<K, V> asSingleValueMap() {
		return map;
	}
	/**
	 * Implementation returns a {@link SingleValueMap} instance if the mapping 
	 * from values to keys is unambiguous, and a {@link DefaultMultiValueMap} if
	 * this is not the case.
	 * <p>
	 * Changing the returned map does not affect this single value map.
	 * 
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#invert()
	 */
	@Override
	public MultiValueMap<V, K> invert() {
		final LinkedHashMap<V, K> hinv = new LinkedHashMap<V, K>();
		MultiValueMap<V, K> minv = null;
		for (final K key : keySet()) {
			final V value = map.get(key);
			if (minv == null) {
				final K pkey = hinv.put(value, key);
				if (pkey != null) {
					hinv.put(value, pkey);					
					minv = DefaultMultiValueMap.createFromSingleValueMap(hinv);
					minv.add(value, pkey);
				}
				else {
					hinv.put(value, key);
				}
			}
			else {
				minv.add(value, key);				
			}
		}
		if (minv == null) {
			minv = new SingleValueMap<V, K>(hinv);
		}
		return minv;
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#keySize()
	 */
	@Override
	public int keySize() {
		return map.size();
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#valueSize()
	 */
	@Override
	public int valueSize() {
		return map.size();
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#values()
	 */
	@Override
	public Iterable<V> values() {
		return map.values();
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() == getClass()) {
			return map.equals(((SingleValueMap)obj).map);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#hashCode()
	 */
	@Override
	public int hashCode() {
		return map.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.AbstractMultiValueMap#toString()
	 */
	@Override
	public String toString() {
		return map.toString();
	}
	
}
