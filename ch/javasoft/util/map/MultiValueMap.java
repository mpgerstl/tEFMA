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
import java.util.Set;

/**
 * <code>MultiValueMap</code> is a mapping of one key to multiple values. 
 * Multiple here means zero to many values. This interface is implemented by 
 * mutable and immutable mappings. Immutable mappings throw an 
 * {@link UnsupportedOperationException} at mutating methods.
 * <p>
 * This class is similar to {@link Map}, but manages multiple values for the
 * same key.
 */
public interface MultiValueMap<K, V> {
	/**
	 * Adds the given value to the multi value map
	 * 
	 * @param key	the key which identifies the value collection
	 * @param value	the value to add
	 * 
	 * @return	true if this map has changed, which is usually the case. If sets
	 * 			are used as key collections, however, the method could also 
	 * 			return false if the given value already existed.
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean add(K key, V value);
	/**
	 * Adds the given values to the multi value map
	 * 
	 * @param key		the key which identifies the value collection
	 * @param values	the values to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean addAll(K key, V... values);
	/**
	 * Adds the key/value map entries of the given map to this multi value map
	 * 
	 * @param map	the map containing key/value-collection entries to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean addAll(Map<? extends K, ? extends V> map);
	/**
	 * Adds the given values to the multi value map
	 * 
	 * @param key		the key which identifies the value collection
	 * @param values	the values to add
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean addAll(K key, Collection<? extends V> values);
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
	boolean addAll(MultiValueMap<? extends K, ? extends V> mapping);
	
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
	boolean addAllNested(Map<? extends K, ? extends Collection<? extends V>> map);	
	/**
	 * Returns all values belonging to this key, i.e. the value collection is
	 * cleared
	 * 
	 * @param key	the key which identifies the value collection
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean remove(Object key);
	/**
	 * Returns a single value of the value collection, if it exists
	 * 
	 * @param key	the key which identifies the value collection
	 * @param value	the value to remove
	 * 
	 * @return	true if this map has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean remove(Object key, V value);

	/**
	 * Removes all entries from this mapping
	 * 
	 * @return 	true if this mapping has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	boolean clear();

	/**
	 * Returns a collection for all values belonging to the specified key, or
	 * an empty collection if no such key collection exists yet. For existing
	 * keys, the returned collection is non-empty.
	 * 
	 * @param key	the key which identifies the value collection
	 * 
	 * @return	the value collection for the given key, never null, and 
	 * 			non-empty for existing keys 
	 */
	Collection<V> get(Object key);
	/**
	 * Returns the first value of the value collection, or null if no such value
	 * exists
	 * 
	 * @param key	the key which identifies the value collection
	 * 
	 * @return	the first value of the value collection, or null if no such 
	 * 			value exists 
	 */
	V getFirst(Object key);
	/**
	 * Returns the number of values in the value collection
	 * 
	 * @param key	the key which identifies the value collection
	 * 
	 * @return	the number of values in the value collection
	 */
	int count(Object key);
	/**
	 * Returns true if the value collection is non-empty
	 * 
	 * @param key	the key which identifies the value collection
	 * 
	 * @return	true if at least one value exists in the value collection
	 */
	boolean contains(Object key);
	/**
	 * Returns true if the value collection contains at least one occurrence of
	 * the given value
	 * 
	 * @param key	the key which identifies the value collection
	 * @param value	the value to look for
	 * 
	 * @return	true if at least one such value exists in the value collection
	 */
	boolean contains(Object key, V value);
	/**
	 * Returns a set containing all keys. Each value collection associated with
	 * the returned keys is non-empty
	 * 
	 * @return	the set of keys identifying non-empty value collections
	 */
	Set<K> keySet();
	/**
	 * Returns an iterable for all values in this mapping.
	 * 
	 * @return	an iterator for all values contained in this mapping
	 */
	Iterable<V> values();
	
	/**
	 * Returns a map with keys and the first value associated with that key.
	 * <p> 
	 * Changing the returned map might or might not affect this multi value map, 
	 * depending on the implementation.
	 * 
	 * @return	a map with key and first associated value
	 */
	Map<K, V> asSingleValueMap();

	/**
	 * Returns an inverted mapping, where the key can be asked knowing the
	 * value.
	 * <p> 
	 * Changing the returned map might or might not affect this multi value map, 
	 * depending on the implementation.
	 * 
	 * @return	the inverted value-key mapping
	 */
	MultiValueMap<V, K> invert();
	
	/**
	 * Returns a map with keys and collections of values. All collections in
	 * the map are non-empty.
	 * <p> 
	 * Changing the returned map might or might not affect this multi value map, 
	 * depending on the implementation.
	 * 
	 * @return	a map with key/collection-of-value entries
	 */
	Map<K, ? extends Collection<V>> asCollectionMap();

	/**
	 * Returns the number of keys contained in this mapping
	 */
	int keySize();
	/**
	 * Returns the number of values contained in this mapping
	 * @return the total number of values
	 */
	int valueSize();
	
	/**
	 * Returns true if this map is empty
	 * 
	 * @return	true if this map is empty
	 */
	boolean isEmpty();
	
}