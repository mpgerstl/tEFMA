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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation of IntSet, also implementing SortedSet, using a sorted int
 * array to store the values.
 */
public class DefaultIntSet extends AbstractSortedIntSet implements Cloneable, Serializable {

	private static final long serialVersionUID = -2690840135359345024L;
	
	private final DefaultIntList values;
	
	public DefaultIntSet() {
		this(7);
	}
	public DefaultIntSet(int initialCapacity) {
		values = new DefaultIntList(initialCapacity);
	}
	public DefaultIntSet(IntSet set) {
		this(set.size());
		addAll(set);
	}
	public DefaultIntSet(int[] values) {
		this(values.length);
		this.values.addAll(values);
		this.values.sort(true);
	}
	public static DefaultIntSet create(int... values) {
		return new DefaultIntSet(values);
	}
	//PRE: values are sorted
	protected DefaultIntSet(DefaultIntList values) {
		this.values = values;
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public boolean containsInt(int o) {
		return values.binarySearchInt(o) >= 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T> T[] toArrayInternal(T[] arr, boolean checkSize) {
		if (checkSize) {
			int size = size();
			if (arr.length < size) {
				arr = (T[])Array.newInstance(arr.getClass().getComponentType(), size);
			}			
		}
		for (int i = 0; i < values.size(); i++) {
			arr[i] = (T)Integer.valueOf(values.getInt(i));
		}
		return arr;
	}

	@Override
	public void clear() {
		values.clear();
		mod++;
	}

	@Override
	public boolean addInt(int value) {
		if (containsInt(value)) {
			return false;
		}
		values.addIntToSorted(value);
		mod++;
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends Integer> coll) {
		if (coll.isEmpty()) return false;
		if (coll instanceof IntCollection) {
			return addAll((IntCollection)coll);
		}
		final DefaultIntList sorted = new DefaultIntList(coll.size());
		final Iterator<? extends Integer> it = coll.iterator();
		while (it.hasNext()) {
			final int val = it.next().intValue();
			if (!containsInt(val)) sorted.addInt(val);
		}
		sorted.sort(true);
		values.mergeSorted(sorted);
		return true;
	}
	
	@Override
	public boolean addAll(IntCollection coll) {
		if (coll.isEmpty()) return false;
		final DefaultIntList sorted = new DefaultIntList(coll.size());
		final IntIterator intIt = coll.iterator();
		while (intIt.hasNext()) {
			final int val = intIt.nextInt();
			if (!containsInt(val)) sorted.addInt(val);
		}
		sorted.sort(true);
		values.mergeSorted(sorted);
		return true;
	}
	@Override
	public boolean addAll(int... values) {
		if (values.length == 0) return false;
		final DefaultIntList sorted = new DefaultIntList(values.length);
		for (int i = 0; i < values.length; i++) {			
			if (!containsInt(values[i])) sorted.addInt(values[i]);
		}
		sorted.sort(true);
		this.values.mergeSorted(sorted);
		return true;
	}	
	
	@Override
	public boolean removeInt(int value) {
		final int index = values.binarySearchInt(value);
		if (index >= 0) {
			values.remove(value);
			mod++;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		return retainOrRemove(c, true);
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.AbstractIntCollection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return retainOrRemove(c, false);
	}
	
	private boolean retainOrRemove(Collection<?> c, boolean remove) {
		if (isEmpty()) {
			return false;
		}
		if (c.isEmpty()) {
			if (remove) return false;
			clear();
			return true;
		}
		final DefaultIntList arr;
		if (c instanceof IntCollection) {
			arr = new DefaultIntList((IntCollection)c);
		}
		else {
			arr = new DefaultIntList();
			for (final Object o : c) {
				if (o instanceof Integer) {
					arr.addInt(((Integer)o).intValue());
				}
			}			
		}		
		arr.sort(true);
		final int len = values.size();
		int src = 0;
		int dst = 0;
		while (src < len) {
			final int val = values.getInt(src);
			if (remove == (arr.binarySearchInt(val) < 0)) {
				values.setInt(dst, val);
				dst++;
			}
			src++;
		}
		return values.removeFromTail(len - dst);
		
	}

	@Override
	public IntIterator iterator() {
		return values.iterator();
	}


	/** @return always null*/
	public Comparator<? super Integer> comparator() {
		return null;
	}
	
	@Override
	public int firstInt() {
		if (values.isEmpty()) {
			throw new NoSuchElementException();
		}
		return values.firstInt();
	}
	@Override
	public int lastInt() {
		if (values.isEmpty()) {
			throw new NoSuchElementException();
		}
		return values.lastInt();
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.SortedIntSet#subSet(int, int)
	 */
	public DefaultIntSet subSet(int fromElement, int toElement) {
		if (fromElement > toElement) {
			throw new IllegalArgumentException("fromElement > toElement: " + fromElement + " > " + toElement);
		}
		int fromIndex 	= values.binarySearchInt(fromElement);
		int toIndex		= values.binarySearchInt(toElement);
		if (fromIndex < 0) {
			fromIndex = -fromIndex;// -(fromIndex + 1) + 1;
		}
		if (toIndex < 0) {
			toIndex = (-toIndex + 1);
		}
		return new DefaultIntSet(values.subList(fromIndex, toIndex));
	}
	
	@Override
	public DefaultIntSet clone() {
		return new DefaultIntSet(values.clone());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DefaultIntSet) {
			return values.equals(((DefaultIntSet)obj).values);
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		return values.toString();
	}

}
