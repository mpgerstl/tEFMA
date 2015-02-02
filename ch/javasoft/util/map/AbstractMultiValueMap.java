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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Abstract implementation for {@link MultiValueMap} for most common methods.
 * For subclasses implementing mutable maps, subclassing 
 * {@link AbstractMutableMultiValueMap} might be advantageous.
 */
abstract public class AbstractMultiValueMap<K, V> implements MultiValueMap<K, V> {
	
	/**
	 * Constructor for an empty mapping
	 */
	public AbstractMultiValueMap() {
		super();
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean add(K key, V value) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean addAll(K key, Collection<? extends V> values) {
		throw new UnsupportedOperationException();		
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean addAll(K key, V... values) {
		throw new UnsupportedOperationException();		
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean addAll(Map<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean addAll(MultiValueMap<? extends K, ? extends V> mapping) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean addAllNested(Map<? extends K, ? extends Collection<? extends V>> map) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean clear() {
		throw new UnsupportedOperationException();
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean remove(Object key) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 */
	public boolean remove(Object key, V value) {
		throw new UnsupportedOperationException();		
	}

	public V getFirst(Object key) {
		final Collection<V> coll = get(key);
		return coll.isEmpty() ? null : coll.iterator().next();
	}
	public boolean contains(Object key) {
		return count(key) == 0;
	}
	
	/**
	 * Implementation returns a {@link LinkedHashMap linked hash map} containing 
	 * {@link ArrayList} instances. 
	 * <p>
	 * Changing the returned map does not affect this multi value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#asCollectionMap()
	 */
	public Map<K, ? extends Collection<V>> asCollectionMap() {
		final LinkedHashMap<K, Collection<V>> map = new LinkedHashMap<K, Collection<V>>();
		for (final K key : keySet()) {
			map.put(key, new ArrayList<V>(get(key)));
		}
		return map;
	}
	/**
	 * Implementation returns a {@link LinkedHashMap linked hash map} containing 
	 * the single values.
	 * <p>
	 * Changing the returned map does not affect this multi value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#asSingleValueMap()
	 */
	public Map<K, V> asSingleValueMap() {
		final LinkedHashMap<K, V> map = new LinkedHashMap<K, V>();
		for (final K key : keySet()) {
			map.put(key, get(key).iterator().next());
		}
		return map;
	}
	/**
	 * Implementation returns a new {@link DefaultMultiValueMap} instance.
	 * <p>
	 * Changing the returned map does not affect this multi value map.
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#invert()
	 */
	public MultiValueMap<V, K> invert() {
		final DefaultMultiValueMap<V, K> inv = new DefaultMultiValueMap<V, K>();
		for (final K key : keySet()) {
			for (final V val : get(key)) {
				inv.add(val, key);
			}
		}
		return inv;
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.MultiValueMap#keySize()
	 */
	public int keySize() {
		return keySet().size();
	}
	
	public int valueSize() {
		int size = 0;
		for (final K key : keySet()) {
			size += get(key).size();
		}
		return size;
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#isEmpty()
	 */
	public boolean isEmpty() {
		return keySize() == 0;
	}
	
	/**
	 * Returns an unmodifiable iterator for all values
	 * 
	 * @see MultiValueMap#values()
	 */
	public Iterable<V> values() {
		return new Iterable<V>() {
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					private final Iterator<? extends Iterable<V>> mItIt	= asCollectionMap().values().iterator();
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() == getClass()) {
			final MultiValueMap other = (MultiValueMap)obj;
			if (keySet().equals(other.keySet())) {
				for (final K key : keySet()) {
					if (!get(key).equals(other.get(key))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		int hash = 0;
		for (final K key : keySet()) {
			hash ^= key.hashCode();
			hash ^= get(key).hashCode();
		}
		return hash;
	}
	@Override
	public String toString() {
		return asCollectionMap().toString();
	}
	
}
