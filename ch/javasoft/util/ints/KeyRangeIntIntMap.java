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
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <code>KeyRangeIntIntMap</code> is an immutable mapping from int to int. The 
 * lowest key is zero, the next larger key is one and so on, i.e. all k keys are
 * <code>key in [0..k-1]</code>. The values can be any integer value.
 */
public class KeyRangeIntIntMap extends AbstractIntIntMap implements Serializable {
	
	private static final long serialVersionUID = -9111876887151093531L;

	private final int[] mapping;
	
	/**
	 * Constructor for a new identity mapping, i.e. val[key] == key
	 */
	public KeyRangeIntIntMap(int size) {
		mapping = new int[size];
		for (int i = 0; i < size; i++) {
			mapping[i] = i;
		}
	}
	public KeyRangeIntIntMap(int[] mapping) {
		this.mapping = mapping;
	}

	public int getInt(int key) {
		if (containsKey(key)) {
			return mapping[key];
		}
		throw new NoSuchElementException("no such key: " + key);
	}
	
	@Override
	public boolean containsKey(int key) {
		return key >= 0 && key < mapping.length;
	}
	
	public boolean containsValue(int value) {
		for (int i = 0; i < mapping.length; i++) {
			if (mapping[i] == value) return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return mapping.length == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#size()
	 */
	@Override
	public int size() {
		return mapping.length;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#keySet()
	 */
	@Override
	public IntSet keySet() {
		return new RangeIntSet(0, mapping.length);
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntMap#intEntrySet()
	 */
	public Set<IntIntMap.IntIntEntry> intIntEntrySet() {
		return new AbstractSet<IntIntMap.IntIntEntry>() {
			@Override
			public Iterator<IntIntMap.IntIntEntry> iterator() {
				return new Iterator<IntIntMap.IntIntEntry>() {
					int index = 0;					
					public boolean hasNext() {
						return index < mapping.length;
					}
					public IntIntMap.IntIntEntry next() {
						if (index >= mapping.length) throw new NoSuchElementException();
						final int cur = index;
						index++;
						return new SimpleIntIntEntry(cur, mapping[cur]);
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
			@Override
			public int size() {
				return mapping.length;
			}
			
			/* (non-Javadoc)
			 * @see java.util.AbstractCollection#isEmpty()
			 */
			@Override
			public boolean isEmpty() {
				return KeyRangeIntIntMap.this.isEmpty();
			}
			@Override
			public boolean contains(Object o) {
				if (o instanceof Map.Entry) {
					final Map.Entry entry = (Map.Entry)o;
					final Integer value = get(entry.getKey());
					return value != null && value.equals(entry.getValue());
				}
				return false;
			}						
		};
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#values()
	 */
	@Override
	public IntCollection values() {
		return IntCollections.unmodifiableCollection(new DefaultIntList(mapping));
	}
	
	/**
	 * Returns a copy of the underlying mapping array. The value corresponding 
	 * to a key <tt>k</tt> is <tt>array[k]</tt>.
	 * 
	 * @return	a copy of the mapping array
	 */
	public int[] toArray() {
		return ch.javasoft.util.Arrays.copyOf(mapping, mapping.length);
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false;
		if (o.getClass() == getClass()) {
			return Arrays.equals(mapping, ((KeyRangeIntIntMap)o).mapping);
		}		
		return super.equals(o);
	}
	
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.IntMap#put(int, java.lang.Object)
	 */
	@Override
	public Integer put(int key, Integer value) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.IntIntMap#put(int, int)
	 */
	public Integer put(int key, int value) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.AbstractIntIntMap#put(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Integer put(Integer key, Integer value) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.AbstractIntIntMap#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends Integer, ? extends Integer> map) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.IntMap#putAll(ch.javasoft.util.ints.IntMap)
	 */
	@Override
	public void putAll(IntMap<? extends Integer> map) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.IntIntMap#putAll(ch.javasoft.util.ints.IntIntMap)
	 */
	@Override
	public void putAll(IntIntMap map) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.IntMap#remove(int)
	 */
	public Integer remove(int key) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see ch.javasoft.util.ints.AbstractIntIntMap#remove(java.lang.Object)
	 */
	@Override
	public Integer remove(Object key) {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 * @see java.util.AbstractMap#clear()
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("unmodifiable map");
	}
	
	
}
