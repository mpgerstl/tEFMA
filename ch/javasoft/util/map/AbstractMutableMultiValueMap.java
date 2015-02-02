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

import java.util.Collection;
import java.util.Map;

/**
 * Abstract implementation for {@link MultiValueMap} for most common 
 * methods for subclasses
 */
abstract public class AbstractMutableMultiValueMap<K, V> extends AbstractMultiValueMap<K, V> {
	
	/**
	 * Constructor for an empty mapping
	 */
	public AbstractMutableMultiValueMap() {
		super();
	}

	/**
	 * Constructor for a mapping based on an existing mapping to copy
	 */
	public AbstractMutableMultiValueMap(MultiValueMap<? extends K, ? extends V> copy) {
		this();
		addAll(copy);
	}
	
	/**
	 * Adds the given values to the key collection
	 * 
	 * @param key		the key which identifies the value collection
	 * @param values	the values to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	@Override
	public boolean addAll(K key, V... values) {
		boolean any = false;
		for (final V value : values) {
			any |= add(key, value);
		}
		return any;
	}
	/**
	 * Adds the given values to the key collection
	 * 
	 * @param key		the key which identifies the value collection
	 * @param values	the values to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	@Override
	public boolean addAll(K key, Collection<? extends V> values) {
		boolean any = false;
		for (final V value : values) {
			any |= add(key, value);
		}
		return any;
	}
	/**
	 * Adds the key/value map entries of the given map to this key collection
	 * 
	 * @param map	the map containing key/value-collection entries to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	@Override
	public boolean addAll(Map<? extends K, ? extends V> map) {
		boolean any = false;
		for (final K key : map.keySet()) {
			any |= add(key, map.get(key));
		}
		return any;
	}
	/**
	 * Adds the key/value-collection entries of the given map to this key 
	 * collection
	 * 
	 * @param map	the map containing key/value-collection entries to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	@Override
	public boolean addAllNested(Map<? extends K, ? extends Collection<? extends V>> map) {
		boolean any = false;
		for (final K key : map.keySet()) {
			any |= addAll(key, map.get(key));
		}
		return any;
	}
	/**
	 * Adds the key/value-collection entries of the given mapping to this key 
	 * collection
	 * 
	 * @param mapping the mapping containing key/value-collection entries to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	@Override
	public boolean addAll(MultiValueMap<? extends K, ? extends V> mapping) {
		boolean any = false;
		for (final K key : mapping.keySet()) {
			any |= addAll(key, mapping.get(key));
		}
		return any;
	}
}
