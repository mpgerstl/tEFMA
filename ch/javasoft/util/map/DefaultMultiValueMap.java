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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Default implementation for a mutable {@link MultiValueMap}. 
 * <p>
 * The standard implementation is backed by a {@link LinkedHashMap} containing
 * an {@link ArrayList} per key. The backing map and collection can be changed
 * by overriding {@link #createMap()} and {@link #createCollection(Object)}, 
 * respectively.
 */
public class DefaultMultiValueMap<K, V> extends AbstractMutableMultiValueMap<K, V> 
	implements Cloneable, Serializable {
		
	private static final long serialVersionUID = 8019175851546861383L;
	
	private final Map<K, Collection<V>> map;
	
	/**
	 * Constructor for an empty multi value map
	 */
	public DefaultMultiValueMap() {
		map = createMap();
	}
	/**
	 * Constructor for a multi value map based on an existing one
	 */
	public DefaultMultiValueMap(MultiValueMap<? extends K, ? extends V> copy) {
		map = createMap();
		for (final K key : copy.keySet()) {
			final Collection<V> coll = createCollection(key);
			coll.addAll(copy.get(key));
			map.put(key, coll);
		}
	}
	
	/**
	 * Constructor for a multi value map based on the given collection map. The
	 * submitted map and its contents are not copied, thus, changes to it will
	 * also affect the newly created multi value map instance. To copy the
	 * submitted map, {@link #createFromCollectionMap(Map)} can be used.
	 * 
	 * @param	collectionMap the map to use for this multi value map
	 */
	public DefaultMultiValueMap(Map<K, Collection<V>> collectionMap) {
		map = collectionMap;
	}

	/**
	 * Creates a multi value map from an existing (single value) map
	 * 
	 * @param <K>		the key type
	 * @param <V>		the value type
	 * @param singleMap	the map containing key/value pairs to add to the new map	
	 * @return	a new multi value map containing the entries of the submitted
	 * 			single value map
	 */
	public static <K, V> DefaultMultiValueMap<K, V> createFromSingleValueMap(Map<? extends K, ? extends V> singleMap) {
		final DefaultMultiValueMap<K, V> map = new DefaultMultiValueMap<K, V>();
		for (final K key : singleMap.keySet()) {
			map.add(key, singleMap.get(key));
		}
		return map;
	}
	/**
	 * Creates a multi value map from an existing collection map. 
	 * 
	 * @param <K>			the key type
	 * @param <V>			the value type
	 * @param collectionMap	the map containing key/list-of-value entries to add 
	 * 						to the new map	
	 * @return	a new multi value map containing the entries of the submitted
	 * 			collection map
	 */
	public static <K, V> DefaultMultiValueMap<K, V> createFromCollectionMap(Map<? extends K, ? extends Collection<? extends V>> collectionMap) {
		final DefaultMultiValueMap<K, V> map = new DefaultMultiValueMap<K, V>();
		map.addAllNested(collectionMap);
		return map;
	}
	
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
	@Override
	public boolean add(K key, V value) {
		final Collection<V> coll = getOrCreateCollection(key);
		return coll.add(value);
	}
	@Override
	public boolean addAll(K key, Collection<? extends V> values) {
		if (values.isEmpty()) return false;
		final Collection<V> coll = getOrCreateCollection(key);
		return coll.addAll(values);
	}

	/**
	 * Returns an unmodifiable value collection.
	 * 
	 * @see MultiValueMap#get(Object)
	 */
	public Collection<V> get(Object key) {
		final Collection<V> coll = map.get(key);
		if (coll == null) return Collections.emptyList();
		return Collections.unmodifiableCollection(coll);
	}
	@Override
	public V getFirst(Object key) {
		final Collection<V> coll = map.get(key);
		return coll == null ? null : coll.iterator().next();
	}
	public int count(Object key) {
		final Collection<V> coll = map.get(key);
		return coll == null ? 0 : coll.size();
	}
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	@Override
	public boolean contains(Object key) {
		return map.containsKey(key);
	}
	public boolean contains(Object key, V value) {
		final Collection<V> coll = map.get(key);
		return coll == null ? false : coll.contains(value);
	}
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
	@Override
	public boolean remove(Object key) {
		return map.remove(key) != null;
	}
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
	@Override
	public boolean remove(Object key, V value) {
		final Collection<V> coll = map.get(key);
		if (coll == null) return false;
		if (coll.remove(value)) {
			if (coll.isEmpty()) {
				map.remove(key);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Removes all entries from this mapping
	 * 
	 * @return 	true if this mapping has changed as a consequence of this 
	 * 			operation
	 * @throws UnsupportedOperationException if this is an immutable map
	 */
	@Override
	public boolean clear() {
		if (!map.isEmpty()) {
			map.clear();
			return true;
		}
		return false;
	}
	
	/**
	 * Returns an modifiable key set, i.e. removal of keys is also reflected in 
	 * this multi value map
	 * 
	 * @see MultiValueMap#get(Object)
	 */
	public Set<K> keySet() {
		return map.keySet();
	}
	
	/**
	 * Returns an unmodifiable iterator for all values
	 * 
	 * @see MultiValueMap#values()
	 */
	@Override
	public Iterable<V> values() {	
		return new Iterable<V>() {
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					private final Iterator<? extends Iterable<V>> mItIt	= map.values().iterator();
					private Iterator<V>	mIt	= nextIterator();
					private Iterator<V> nextIterator() {
						Iterable<V> itbl = mItIt.hasNext() ? mItIt.next() : null;
						if (itbl != null) {
							Iterator<V> it = itbl.iterator();
							return it.hasNext() ? it : nextIterator();
						}
						return null;
					}
					public boolean hasNext() {
						if (mIt == null) return false;
						if (mIt.hasNext()) return true;
						mIt = nextIterator();
						return hasNext();
					}
					public V next() {
						if (hasNext()) {
							return mIt.next();
						}
						throw new NoSuchElementException();
					}
					public void remove() {
						if (mIt == null) throw new NoSuchElementException();
						mIt.remove();
					}
				};
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DefaultMultiValueMap<K, V> clone() {
		try {
			final Method meth = map.getClass().getMethod("clone");
			final Map<K, Collection<V>> clonedMap = (Map<K, Collection<V>>)meth.invoke(map);
			return new DefaultMultiValueMap<K, V>(clonedMap);
		} 
		catch (Exception e) {
			//map might not be cloneable, lets just create a new map now
			return new DefaultMultiValueMap<K, V>(this);
		}
	}
	
	/**
	 * Returns the value collection for the specified key. If no such
	 * collection exists, a new collection is 
	 * {@link #createCollection(Object) created} and added to the backing
	 * map.
	 * 
	 * @param key		the key identifying the value collection
	 * @return	the value collection
	 */
	protected Collection<V> getOrCreateCollection(K key) {
		Collection<V> coll = map.get(key);
		if (coll == null) {
			map.put(key, coll = createCollection(key));
		}
		return coll;
	}
	/**
	 * Returns a new collection instance. By default, an {@link ArrayList} of
	 * size one is created. The method shall never return null.
	 * 
	 * @param key	the key for which a value collection is to be created
	 * @return		the new array list instance
	 */
	protected Collection<V> createCollection(K key) {
		return new ArrayList<V>(1);
	}
	/**
	 * Returns a new map instance. By default, a {@link LinkedHashMap} is 
	 * created. The method shall never return null.
	 * 
	 * @return		the new linked hash map instance
	 */
	protected Map<K, Collection<V>> createMap() {
		return new LinkedHashMap<K, Collection<V>>();
	}
}
