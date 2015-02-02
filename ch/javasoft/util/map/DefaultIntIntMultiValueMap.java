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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import ch.javasoft.util.ints.AbstractIntIterator;
import ch.javasoft.util.ints.DefaultIntList;
import ch.javasoft.util.ints.IntCollection;
import ch.javasoft.util.ints.IntCollections;
import ch.javasoft.util.ints.IntHashMap;
import ch.javasoft.util.ints.IntIterable;
import ch.javasoft.util.ints.IntIterator;
import ch.javasoft.util.ints.IntList;
import ch.javasoft.util.ints.IntMap;
import ch.javasoft.util.ints.IntSet;

/**
 * The <code>IntIntMultiValueMap</code> is a mutable multi value map for int
 * keys and values. 
 */
public class DefaultIntIntMultiValueMap extends AbstractMutableMultiValueMap<Integer, Integer> 
	implements IntIntMultiValueMap, Cloneable, Serializable {
		
	private static final long serialVersionUID = -93905570980186647L;
	
	private final IntMap<IntCollection> map;
	
	/**
	 * Constructor for an empty multi value map
	 */
	public DefaultIntIntMultiValueMap() {
		map = createMap();
	}
	/**
	 * Constructor for a multi value map based on an existing one
	 */
	public DefaultIntIntMultiValueMap(MultiValueMap<? extends Integer, ? extends Integer> copy) {
		map = createMap();
		for (final Integer key : copy.keySet()) {
			final IntCollection coll = createCollection(key.intValue());
			coll.addAll(copy.get(key));
			map.put(key, coll);
		}
	}
	/**
	 * Constructor for a multi value map based on an existing one
	 */
	public DefaultIntIntMultiValueMap(DefaultIntIntMultiValueMap copy) {
		map = createMap();
		final IntIterator keyIt = keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			final IntCollection coll = createCollection(key);
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
	public DefaultIntIntMultiValueMap(IntMap<IntCollection> collectionMap) {
		map = collectionMap;
	}

	/**
	 * Creates a multi value map from an existing (single value) map
	 * 
	 * @param singleMap	the map containing key/value pairs to add to the new map	
	 * @return	a new multi value map containing the entries of the submitted
	 * 			single value map
	 */
	public static DefaultIntIntMultiValueMap createFromSingleValueMap(Map<? extends Integer, ? extends Integer> singleMap) {
		final DefaultIntIntMultiValueMap map = new DefaultIntIntMultiValueMap();
		for (final Integer key : singleMap.keySet()) {
			map.add(key, singleMap.get(key));
		}
		return map;
	}
	/**
	 * Creates a multi value map from an existing collection map. 
	 * 
	 * @param collectionMap	the map containing key/list-of-value entries to add 
	 * 						to the new map	
	 * @return	a new multi value map containing the entries of the submitted
	 * 			collection map
	 */
	public static DefaultIntIntMultiValueMap createFromCollectionMap(Map<? extends Integer, ? extends Collection<? extends Integer>> collectionMap) {
		final DefaultIntIntMultiValueMap map = new DefaultIntIntMultiValueMap();
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
	public boolean add(Integer key, Integer value) {
		return add(key.intValue(), value.intValue());
	}
	public boolean add(int key, int value) {
		final IntCollection coll = getOrCreateCollection(key);
		return coll.addInt(value);
	}
	public boolean addAll(int key, IntCollection values) {
		if (values.isEmpty()) return false;
		final IntCollection coll = getOrCreateCollection(key);
		return coll.addAll(values);		
	}
	@Override
	public boolean addAll(Integer key, Collection<? extends Integer> values) {
		if (values.isEmpty()) return false;
		final IntCollection coll = getOrCreateCollection(key.intValue());
		return coll.addAll(values);		
	}

	public boolean addAll(int key, int... values) {
		boolean any = false;
		for (final int value : values) {
			any |= add(key, value);
		}
		return any;
	}
	public boolean addAll(IntMap<? extends Integer> map) {
		boolean any = false;
		final IntIterator keyIt = map.keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			any |= add(key, map.get(key).intValue());
		}
		return any;
	}
	public boolean addAll(IntIntMultiValueMap mapping) {
		boolean any = false;
		final IntIterator keyIt = mapping.keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			any |= addAll(key, mapping.get(key));
		}
		return any;
	}
	public boolean addAllNested(IntMap<? extends IntCollection> map) {
		boolean any = false;
		final IntIterator keyIt = map.keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			any |= addAll(key, map.get(key));
		}
		return any;
	}

	/**
	 * Returns an unmodifiable value collection.
	 * 
	 * @see MultiValueMap#get(Object)
	 */
	public IntCollection get(Object key) {
		final IntCollection coll = map.get(key);
		if (coll == null) return IntCollections.EMPTY_LIST;
		return IntCollections.unmodifiableCollection(coll);
	}
	/**
	 * Returns an unmodifiable value collection.
	 * 
	 * @see MultiValueMap#get(Object)
	 */
	public IntCollection get(int key) {
		final IntCollection coll = map.get(key);
		if (coll == null) return IntCollections.EMPTY_LIST;
		return IntCollections.unmodifiableCollection(coll);
	}
	
	@Override
	public Integer getFirst(Object key) {
		final IntCollection coll = map.get(key);
		return coll == null ? null : coll.iterator().next();
	}
	/**
	 * Returns the first value associated with this key, or throws an exception
	 * if no such value exists
	 * 
	 * @param key	the key to search for
	 * @return the first value associated with this key
	 * @throws NoSuchElementException if no value exists for the given key
	 */
	public int getFirst(int key) {
		final IntCollection coll = map.get(key);
		if (coll == null) throw new NoSuchElementException();
		return coll instanceof IntList ? ((IntList)coll).getInt(0) : coll.iterator().nextInt();
	}
	public int count(Object key) {
		final IntCollection coll = map.get(key);
		return coll == null ? 0 : coll.size();
	}
	public int count(int key) {
		final IntCollection coll = map.get(key);
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
	public boolean contains(int key) {
		return map.containsKey(key);
	}
	public boolean contains(Object key, Integer value) {
		final IntCollection coll = map.get(key);
		return coll == null ? false : coll.contains(value);
	}
	public boolean contains(int key, int value) {
		final IntCollection coll = map.get(key);
		return coll == null ? false : coll.containsInt(value);
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
	public boolean remove(int key) {
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
	public boolean remove(Object key, Integer value) {
		final IntCollection coll = map.get(key);
		if (coll == null) return false;
		if (coll.remove(value)) {
			if (coll.isEmpty()) {
				map.remove(key);
			}
			return true;
		}
		return false;
	}
	public boolean remove(int key, int value) {
		final IntCollection coll = map.get(key);
		if (coll == null) return false;
		if (coll.removeInt(value)) {
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
	public IntSet keySet() {
		return map.keySet();
	}
	
	/**
	 * Returns an unmodifiable iterator for all values
	 * 
	 * @see MultiValueMap#values()
	 */
	@Override
	public IntIterable values() {	
		return new IntIterable() {
			public IntIterator iterator() {
				return new AbstractIntIterator() {
					private final Iterator<? extends IntIterable> mItIt = map.values().iterator();
					private IntIterator	mIt	= nextIterator();
					private IntIterator nextIterator() {
						IntIterable itbl = mItIt.hasNext() ? mItIt.next() : null;
						if (itbl != null) {
							IntIterator it = itbl.iterator();
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
					public int nextInt() {
						if (hasNext()) {
							return mIt.nextInt();
						}
						throw new NoSuchElementException();
					}
					@Override
					public void remove() {
						if (mIt == null) throw new NoSuchElementException();
						mIt.remove();
					}
				};
			}
		};
	}
	
	/**
	 * Implementation returns an {@link IntHashMap} containing 
	 * {@link DefaultIntList} instances. 
	 * <p>
	 * Changing the returned map does not affect this multi value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#asCollectionMap()
	 */
	@Override
	public IntMap<? extends IntCollection> asCollectionMap() {
		final IntHashMap<IntCollection> map = new IntHashMap<IntCollection>();
		final IntIterator keyIt = keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			map.put(key, new DefaultIntList(get(key)));
		}
		return map;
	}
	/**
	 * Implementation returns an {@link IntHashMap} containing 
	 * the single values.
	 * <p>
	 * Changing the returned map does not affect this multi value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#asSingleValueMap()
	 */
	@Override
	public IntMap<Integer> asSingleValueMap() {
		final IntHashMap<Integer> map = new IntHashMap<Integer>();
		final IntIterator keyIt = keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			map.put(key, get(key).iterator().next());
		}
		return map;
	}
	/**
	 * Implementation returns a new {@link DefaultIntIntMultiValueMap} instance.
	 * <p>
	 * Changing the returned map does not affect this multi value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#invert()
	 */
	@Override
	public IntIntMultiValueMap invert() {
		final IntIntMultiValueMap inv = new DefaultIntIntMultiValueMap();
		final IntIterator keyIt = keySet().iterator();
		while (keyIt.hasNext()) {
			final int key = keyIt.nextInt();
			final IntIterator valIt = get(key).iterator();
			while (valIt.hasNext()) {
				final int val = valIt.nextInt();
				inv.add(val, key);
			}
		}
		return inv;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DefaultIntIntMultiValueMap clone() {
		try {
			final Method meth = map.getClass().getMethod("clone");
			final IntMap<IntCollection> clonedMap = (IntMap<IntCollection>)meth.invoke(map);
			return new DefaultIntIntMultiValueMap(clonedMap);
		} 
		catch (Exception e) {
			//map might not be cloneable, lets just create a new map now
			return new DefaultIntIntMultiValueMap(this);
		}
	}
	
	/**
	 * Returns the value collection for the specified key. If no such
	 * collection exists, a new collection is 
	 * {@link #createCollection(int) created} and added to the backing
	 * map.
	 * 
	 * @param key		the key identifying the value collection
	 * @return	the value collection
	 */
	protected IntCollection getOrCreateCollection(int key) {
		IntCollection coll = map.get(key);
		if (coll == null) {
			map.put(key, coll = createCollection(key));
		}
		return coll;
	}
	/**
	 * Returns a new int list instance. By default, an {@link ArrayList} of
	 * size one is created. The method shall never return null.
	 * 
	 * @param key	the key for which a value collection is to be created
	 * @return		the new array list instance
	 */
	protected IntCollection createCollection(int key) {
		return new DefaultIntList(1);
	}
	/**
	 * Returns a new map instance. By default, a {@link IntHashMap} is returned.
	 * The method shall never return null.
	 * 
	 * @return		the new int hash map instance
	 */
	protected IntMap<IntCollection> createMap() {
		return new IntHashMap<IntCollection>();
	}
}
