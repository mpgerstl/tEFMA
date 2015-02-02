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
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import ch.javasoft.util.IntArray;

/**
 * <code>DefaultIntIntMap</code> is an mutable mapping from int to int. It is 
 * implemented with two lists for keys and values, and the key list is sorted.
 */
public class DefaultIntIntMap extends AbstractIntIntMap implements Serializable, Cloneable {
	
	private static final long serialVersionUID = -2304981943154788232L;
	
	private final IntArray keys;
	private final IntArray vals;
	
	/**
	 * Constructor for a new empty map
	 */
	public DefaultIntIntMap() {
		this(7);
	}
	/**
	 * Constructor for a new empty map of the given initial capacity
	 */
	public DefaultIntIntMap(int capacity) {
		keys = new IntArray(capacity);
		vals = new IntArray(capacity);
	}
	
	public DefaultIntIntMap(DefaultIntIntMap copy) {
		keys = copy.keys.clone();
		vals = copy.vals.clone();
	}

	public DefaultIntIntMap(Map<Integer, Integer> map) {
		if (map instanceof DefaultIntIntMap) {
			keys = ((DefaultIntIntMap)map).keys.clone();
			vals = ((DefaultIntIntMap)map).vals.clone();
		}
		else {
			final int[][] keyvals = toSortedKeyValArray(map);
			keys = new IntArray(keyvals.length);
			vals = new IntArray(keyvals.length);
			for (int i = 0; i < keyvals.length; i++) {
				keys.add(keyvals[i][0]);
				vals.add(keyvals[i][1]);
			}
		}
	}
	/**
	 * Returns an array <code>arr[map.size()][2]</code>, where each double entry
	 * belongs to a map entry. For an entry at position i, the value
	 * <code>arr[i][0]</code> is the key and <code>arr[i][1]</code> the value.
	 * The entries in the returned double array are sorted by key. 
	 */
	private static int[][] toSortedKeyValArray(Map<Integer, Integer> map) {
		final int[][] keyvals = new int[map.size()][2];
		final Iterator<Integer> it = map.keySet().iterator();
		int index = 0;
		while (it.hasNext()) {
			final int key = it instanceof IntIterator ? 
					((IntIterator)it).nextInt() : it.next().intValue();
			final int val = map instanceof IntIntMap ?
					((IntIntMap)map).getInt(key) : map instanceof IntMap ?
							((IntMap<Integer>)map).get(key).intValue() :
								map.get(Integer.valueOf(key)).intValue();
			keyvals[index][0] = key;
			keyvals[index][1] = val;
			index++;
		}
		Arrays.sort(keyvals, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				return o1[0] - o2[0];//compare the keys
			}
		});
		return keyvals;
	}
	public int getInt(int key) {
		final int index = keys.binarySearch(key);
		if (index >= 0) {
			return vals.get(index);
		}
		throw new NoSuchElementException("no such key: " + key);
	}
	
	@Override
	public boolean containsKey(int key) {
		return keys.binarySearch(key) >= 0;
	}
	
	public boolean containsValue(int value) {
		return vals.indexOf(value) >= 0;
	}
	
	@Override
	public boolean isEmpty() {
		return keys.isEmpty();
	}
	
	@Override
	public int size() {
		return keys.length();
	}
	
	@Override
	public IntSet keySet() {
		return new DefaultIntSet(keys.toArray()) {
			private static final long serialVersionUID = -1944925467921640704L;
			@Override
			public boolean removeInt(int value) {
				final int index = keys.binarySearch(value);
				if (index < 0) return false;
				super.removeInt(value);
				vals.remove(index);
				return true;
			}
			@Override
			public void clear() {
				super.clear();
				vals.clear();
			}
			@Override
			public boolean addInt(int value) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean add(Integer o) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean addAll(Collection<? extends Integer> c) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean addAll(int... values) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean addAll(IntCollection coll) {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntMap#intEntrySet()
	 */
	public Set<IntIntMap.IntIntEntry> intIntEntrySet() {
		return new AbstractSet<IntIntMap.IntIntEntry>() {
			@Override
			public Iterator<IntIntMap.IntIntEntry> iterator() {
				return new Iterator<IntIntMap.IntIntEntry>() {
					int indexToRemove = -1;
					int index = 0;					
					public boolean hasNext() {
						return index < size();
					}
					public IntIntMap.IntIntEntry next() {
						if (index >= size()) throw new NoSuchElementException();
						final int cur = index;
						index++;
						indexToRemove = cur;
						return new SimpleIntIntEntry(keys.get(cur), vals.get(cur));
					}
					public void remove() {
						if (indexToRemove < 0) {
							throw new IllegalStateException("value already removed, or next() not called yet");
						}
						keys.remove(indexToRemove);
						vals.remove(indexToRemove);						
						index--;
						indexToRemove = -1;
					}
				};
			}
			@Override
			public boolean remove(Object o) {
				if (o instanceof Map.Entry) {
					final Map.Entry<?, ?> entry = (Map.Entry<?, ?>)o;
					final Object key = entry.getKey();
					final Object val = entry.getValue();
					if (key instanceof Integer && val instanceof Integer) {
						final int ikey = ((Integer)key).intValue(); 
						if (getInt(ikey) == ((Integer)val).intValue()) {
							DefaultIntIntMap.this.remove(ikey);
							return true;
						}
					}
				}
				return false;
			}
			@Override
			public void clear() {
				DefaultIntIntMap.this.clear();
			}
			@Override
			public int size() {
				return keys.length();
			}
			@Override
			public boolean isEmpty() {
				return DefaultIntIntMap.this.isEmpty();
			}
			@Override
			public boolean contains(Object o) {
				if (o instanceof Map.Entry) {
					final Map.Entry<?, ?> entry = (Map.Entry<?, ?>)o;
					final Object key = entry.getKey();
					final Object val = entry.getValue();
					if (key instanceof Integer && val instanceof Integer) {
						final int ikey = ((Integer)key).intValue(); 
						return getInt(ikey) == ((Integer)val).intValue();
					}
				}
				return false;
			}						
		};
	}
	
	@Override
	public IntCollection values() {
		return new AbstractIntCollection() {
			@Override
			public IntIterator iterator() {
				return new AbstractIntIterator() {
					int indexToRemove = -1;
					int index = 0;					
					public boolean hasNext() {
						return index < size();
					}
					public int nextInt() {
						if (index >= size()) throw new NoSuchElementException();
						final int cur = index;
						index++;
						indexToRemove = cur;
						return vals.get(cur);
					}
					@Override
					public void remove() {
						if (indexToRemove < 0) {
							throw new IllegalStateException("value already removed, or next() not called yet");
						}
						keys.remove(indexToRemove);
						vals.remove(indexToRemove);
						index--;
						indexToRemove = -1;
					}
				};
			}
			/* (non-Javadoc)
			 * @see ch.javasoft.util.intcoll.AbstractIntCollection#removeInt(int)
			 */
			@Override
			public boolean removeInt(int value) {
				final int index = vals.binarySearch(value);
				if (index >= 0) {
					keys.remove(index);
					vals.remove(index);
					return true;
				}
				return false;
			}
			@Override
			public void clear() {
				DefaultIntIntMap.this.clear();
			}
			@Override
			public int size() {
				return keys.length();
			}
			@Override
			public boolean isEmpty() {
				return DefaultIntIntMap.this.isEmpty();
			}
			@Override
			public boolean containsInt(int value) {
				return vals.indexOf(value) >= 0;
			}
			@Override
			public boolean addInt(int value) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean add(Integer o) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean addAll(Collection<? extends Integer> c) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean addAll(int... values) {
				throw new UnsupportedOperationException();
			}
			@Override
			public boolean addAll(IntCollection coll) {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/**
	 * Returns the keys as a sorted int array. The key at position i corresponds
	 * to the value at position i as returned by {@link #toValueArray()}
	 * 
	 * @return	a copy of the sorted key array
	 */
	public int[] toKeyArray() {
		return keys.toArray();
	}
	/**
	 * Returns the values as an int array. The value at position i corresponds
	 * to the key at position i as returned by {@link #toKeyArray()}
	 * 
	 * @return	a copy of the value array
	 */
	public int[] toValueArray() {
		return vals.toArray();
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DefaultIntIntMap) {
			final DefaultIntIntMap other = (DefaultIntIntMap)o;
			return keys.equals(other.keys) && vals.equals(other.vals);
		}
		return super.equals(o);
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntIntMap#put(int, int)
	 */
	public Integer put(int key, int value) {
		int index = keys.binarySearch(key);
		if (index >= 0) {
			return Integer.valueOf(vals.set(index, value));
		}
		index = -(index + 1);
		keys.add(index, key);
		vals.add(index, value);
		return null;
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.AbstractIntIntMap#putAll(ch.javasoft.util.intcoll.IntIntMap)
	 */
	@Override
	public void putAll(IntIntMap map) {
		mergeWith(toSortedKeyValArray(map));
	}
	
	private void mergeWith(final int[][] sortedKeyVals) {
		final int curlen = size();
		final int addlen = sortedKeyVals.length;
		if (addlen <= 2) {			
			for (int i = 0; i < addlen; i++) {
				final int index = keys.addToSorted(sortedKeyVals[i][0]);
				vals.addToSorted(sortedKeyVals[index][0]);
			}
			return;
		}
		final int[] newkey = new int[curlen + addlen];
		final int[] newval = new int[curlen + addlen];
		int curind = 0;
		int addind = 0;
		int newind = 0;
		while (curind < curlen && addind < addlen) {
			final int curval = keys.get(curind);
			final int addval = sortedKeyVals[addind][0];
			if (curval <= addval) {
				newkey[newind] = curval;
				newval[newind] = vals.get(curind);
				newind++;
				curind++;
				if (curval == addval) {
					addind++;
				}
			}
			else {
				newkey[newind] = addval;
				newval[newind] = sortedKeyVals[addind][1];
				newind++;
				addind++;
			}
		}
		while (curind < curlen) {
			newkey[newind] = keys.get(curind);
			newval[newind] = vals.get(curind);
			newind++;
			curind++;
		}
		while (addind < addlen) {
			newkey[newind] = sortedKeyVals[addind][0];
			newval[newind] = sortedKeyVals[addind][1];
			newind++;
			addind++;
		}
		keys.clear();
		vals.clear();
		for (int i = 0; i < newind; i++) {
			keys.add(newkey[i]);
			vals.add(newval[i]);
		}
	}
	@Override
	public void clear() {
		keys.clear();
		vals.clear();
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntMap#remove(int)
	 */
	public Integer remove(int key) {
		final int index = keys.binarySearch(key);
		if (index < 0) return null;
		keys.remove(index);
		return Integer.valueOf(vals.remove(index));
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#clone()
	 */
	@Override
	public DefaultIntIntMap clone() {
		return new DefaultIntIntMap(this);
	}
}
