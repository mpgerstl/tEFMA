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
package ch.javasoft.util.ints;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * The <code>IntCollections</code> contains, like the {@link Collections} class,
 * static helper methods for int collections.
 */
public class IntCollections {
	
	public static final IntList EMPTY_LIST 	= unmodifiableList(new DefaultIntList(0));
	public static final IntSet  EMPTY_SET	= new RangeIntSet(0, 0);
	public static final IntMap  EMPTY_MAP	= unmodifiableMap(new IntHashMap<Object>(0));
	
	@SuppressWarnings("unchecked")
	public static <V> IntMap<V> emptyMap() {
		return EMPTY_MAP;
	}
	
	public static IntCollection unmodifiableCollection(IntCollection coll) {
		return new UnmodifiableCollection(coll);
	}
	public static IntList unmodifiableList(IntList list) {
		return new UnmodifiableList(list);
	}
	public static IntSet unmodifiableSet(IntSet set) {
		return new UnmodifiableSet(set);
	}
	public static <V> IntMap<V> unmodifiableMap(IntMap<V> map) {
		return new UnmodifiableMap<V>(map);
	}

	private static class UnmodifiableCollection implements IntCollection, Serializable {
		private static final long serialVersionUID = 3053283048803504941L;
		public final IntCollection delegate;
		public UnmodifiableCollection(IntCollection delegate) {
			this.delegate = delegate;
		}
		public boolean addAll(Collection<? extends Integer> c) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean addInt(int value) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean addAll(int... values) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public void clear() {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public IntIterator iterator() {
			return delegate.iterator();
		}
		public boolean removeInt(int value) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public int size() {
			return delegate.size();
		}
		public boolean addAll(IntCollection coll) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean containsInt(int value) {
			return delegate.containsInt(value);
		}
		public int[] toIntArray() {
			return delegate.toIntArray();
		}
		public int[] toIntArray(int[] arr) {
			return delegate.toIntArray(arr);
		}
		public boolean add(Integer e) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean contains(Object o) {
			return delegate.contains(o);
		}
		public boolean containsAll(Collection<?> c) {
			return delegate.containsAll(c);
		}
		public boolean isEmpty() {
			return delegate.isEmpty();
		}
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public Object[] toArray() {
			return delegate.toArray();
		}
		public <T> T[] toArray(T[] a) {
			return delegate.toArray(a);
		}
		@Override
		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}
		@Override
		public int hashCode() {
			return delegate.hashCode();
		}
		@Override
		public String toString() {
			return delegate.toString();
		}
	}
	
	private static class UnmodifiableList extends UnmodifiableCollection implements IntList {
		private static final long serialVersionUID = -4863903826971083099L;
		public UnmodifiableList(IntList delegate) {
			super(delegate);
		}
		private IntList delegate() {
			return (IntList)delegate;
		}
		public boolean addInt(int index, int value) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public int getInt(int index) {
			return delegate().getInt(index);
		}
		public IntListIterator listIterator(int index) {
			return delegate().listIterator(index);
		}
		public int removeIntAt(int index) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public int setInt(int index, int value) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public IntList subList(int fromIndex, int toIndex) {
			return delegate().subList(fromIndex, toIndex);
		}
		public void add(int index, Integer element) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean addAll(int index, Collection<? extends Integer> c) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public Integer get(int index) {
			return delegate().get(index);
		}
		public int indexOf(Object o) {
			return delegate().indexOf(o);
		}
		public int lastIndexOf(Object o) {
			return delegate().lastIndexOf(o);
		}
		public ListIterator<Integer> listIterator() {
			return delegate().listIterator();
		}
		public Integer remove(int index) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public Integer set(int index, Integer element) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		
	}

	private static class UnmodifiableSet extends UnmodifiableCollection implements IntSet {
		private static final long serialVersionUID = 7012485294936787378L;
		public UnmodifiableSet(IntSet delegate) {
			super(delegate);
		}
	}
	
	private static class UnmodifiableMap<V> implements IntMap<V>, Serializable {
		private static final long serialVersionUID = 2364133582095025174L;
		public final IntMap<V> delegate;
		public UnmodifiableMap(IntMap<V> delegate) {
			this.delegate = delegate;
		}
		public boolean containsKey(int key) {
			return delegate.containsKey(key);
		}
		public Set<Entry<Integer, V>> entrySet() {
			return delegate.entrySet();
		}
		public V get(int key) {
			return delegate.get(key);
		}
		public Set<IntEntry<V>> intEntrySet() {
			return delegate.intEntrySet();
		}
		public IntSet keySet() {
			return delegate.keySet();
		}
		public V put(int key, V value) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public void putAll(IntMap map) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public V remove(int key) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public void clear() {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public boolean containsKey(Object key) {
			return delegate.containsKey(key);
		}
		public boolean containsValue(Object value) {
			return delegate.containsValue(value);
		}
		public V get(Object key) {
			return delegate.get(key);
		}
		public boolean isEmpty() {
			return delegate.isEmpty();
		}
		public V put(Integer key, V value) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public void putAll(Map<? extends Integer, ? extends V> m) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public V remove(Object key) {
			throw new UnsupportedOperationException("unmodifiable collection");
		}
		public int size() {
			return delegate.size();
		}
		public Collection<V> values() {
			return delegate.values();
		}
		@Override
		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}
		@Override
		public int hashCode() {
			return delegate.hashCode();
		}
		@Override
		public String toString() {
			return delegate.toString();
		}
	}

	
	//no instances
	private IntCollections() {
		super();
	}
}
