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
import java.util.ConcurrentModificationException;
import java.util.List;

import ch.javasoft.util.IntArray;
import ch.javasoft.util.Iterables;

/**
 * The <tt>DefaultIntList</tt> is a list of integers backed by an 
 * {@link IntArray}. Integers can be added after construction. This class 
 * implements the collection interfaces and is therefore a list of integers.
 * 
 * @see #addInt(int)
 * @see #trimToLength()
 * @see List
 * @see IntList
 */
public class DefaultIntList extends AbstractIntList implements IntList, Serializable, Cloneable {
	
	private static final long serialVersionUID = -4806359511537837653L;

	private final IntArray array;
	
	public DefaultIntList() {
		this(new IntArray());
	}
	public DefaultIntList(int initialCapacity) {
		this(new IntArray(initialCapacity));
	}
	public DefaultIntList(int[] initialValues) {
		this(new IntArray(initialValues));
	}
	public DefaultIntList(IntArray array) {
		this.array = array;
	}
	public DefaultIntList(Iterable<Integer> values) {
		this(Iterables.iterableSize(values));
		for (final Integer value : values) {
			add(value);
		}
	}
	public DefaultIntList(DefaultIntList copy) {
		this(copy.array.clone());
	}
	public DefaultIntList(IntCollection coll) {
		this(coll.size());
		addAll(coll);
	}
	@Override
	public DefaultIntList clone() {
		return new DefaultIntList(this);
	}
	@Override
	public int size() {
		return array.length();
	}
	@Override
	public boolean isEmpty() {
		return array.isEmpty();
	}
	public int getInt(int index) throws IndexOutOfBoundsException {
		return array.get(index);
	}
	public int setInt(int index, int value) throws IndexOutOfBoundsException {
		final int old = array.set(index, value);
		mod++;
		return old;
	}
	
	/**
	 * Sets the value at the given position, if the index is in the range or
	 * this int array. If the index is at the end (i.e. 
	 * <code>index == length()</code>), the value is added to this array. If the 
	 * index is after the end (i.e. <code>index > length()</code>), the value is
	 * added at the specified position, the gap between the added value and the
	 * current length of the array is filled with 
	 * {@link IntArray#initialValue() initial values}. 
	 * 
	 * @param index	the index where the new value should be placed, either added
	 * 				or replacing an old one
	 * @param value	the value to set or add
	 */
	public void setOrAddInt(int index, int value) {
		array.set(index, value);
		mod++;
	}
	
	/**
	 * Adds the given value at the position where the value fits according
	 * to {@link #binarySearchInt(int)}. If this int array is not sorted 
	 * ascending, the result is undefined.
	 * 
	 * @param value the value to insert
	 * @return	the position where the value has been inserted
	 */
	public int addIntToSorted(int value) {
		int pos = binarySearchInt(value);
		if (pos < 0) pos = -(pos + 1);
		addInt(pos, value);
		return pos;
	}
	/**
	 * Merges two sorted int lists. If any of the lists is not sorted 
	 * ascending, the result is undefined.
	 * 
	 * @param sorted the other sorted int list to merge into this int array
	 */
	public void mergeSorted(IntList sorted) {
		if (sorted.size() <= 2) {
			for (int i = 0; i < sorted.size(); i++) {
				array.addToSorted(sorted.getInt(i));
			}
			mod++;
			return;
		}

		final int curlen = array.length();
		final int addlen = sorted.size();	
		final int[] newarr  = new int[curlen + addlen];
		int curind = 0;
		int addind = 0;
		int newind = 0;
		while (curind < curlen && addind < addlen) {
			final int curval = array.get(curind);
			final int addval = sorted.getInt(addind);
			if (curval <= addval) {
				newarr[newind++] = curval;
				curind++;
			}
			else {
				newarr[newind++] = addval;
				addind++;
			}
		}
		while (curind < curlen) {
			newarr[newind++] = array.get(curind);
			curind++;
		}
		while (addind < addlen) {
			newarr[newind++] = sorted.getInt(addind);
			addind++;
		}
		
		array.clear();
		array.addAll(newarr);
		mod++;
	}
	@Override
	public boolean addInt(int value) {
		array.add(value);
		mod++;
		return true;
	}
	
	public boolean addInt(int index, int value) {
		array.add(index, value);
		mod++;
		return true;
	}
	@Override
	public boolean addAll(IntCollection coll) {
		if (coll instanceof DefaultIntList) {
			array.addAll(((DefaultIntList)coll).array);
			mod++;
			return true;
		}		
		return super.addAll(coll);
	}
	@Override
	public boolean addAll(int... values) {
		array.addAll(values);
		mod++;
		return true;
	}
	public int removeLastInt() {
		final int old = array.removeLast();
		mod++;
		return old;
	}
	
	/**
	 * Removes <code>n</code> elements from the tail of this array. If fewer 
	 * elements are left in this array, an {@link IndexOutOfBoundsException} is
	 * thrown.
	 * 
	 * @param n	the number of elements to remove from the tail of this array
	 * @return	true if this array changed as a consequence of this operation
	 */
	public boolean removeFromTail(int n) {
		if (n == 0) return false;
		if (n < 0) {
			throw new IllegalArgumentException("negative length argument");
		}
		if (array.length() < n) {
			throw new IndexOutOfBoundsException("cannot remove " + n + " elements, only " + array.length() + " left");
		}
		while (n > 0) {
			array.removeLast();
			n--;
		}
		mod++;
		return true;
	}
	
	/**
	 * Removes and returns the integer at the given position. Values at the 
	 * right of the concerned position are shifted to left.
	 * 
	 * @param index	the index of the value to remove
	 * @return	the removed value
	 */
	public int removeIntAt(int index) {
		final int old = array.remove(index);
		mod++;
		return old;
	}
	@Override
	public void clear() {
		array.clear();
		mod++;
	}
	
	@Override
	public int firstInt() {
		return array.first();
	}
	@Override
	public int lastInt() {
		return array.last();
	}
	
	public DefaultIntList subList(int fromIndex, int toIndex) {
		return new DefaultIntList(array.subRange(fromIndex, toIndex));
	}
	public IntListIterator listIterator(int index) {
		return new DefaultIntListIterator(index, this) {
			int ref = mod;
			@Override
			public void addInt(int value) {
				checkMod();
				super.addInt(value);
				ref = mod;
			}

			@Override
			public int nextInt() {
				checkMod();
				return super.nextInt();
			}

			@Override
			public int previousInt() {
				checkMod();
				return super.previousInt();
			}

			@Override
			public void setInt(int value) {
				checkMod();
				super.setInt(value);
				ref = mod;
			}

			@Override
			public void remove() {
				checkMod();
				super.remove();
				ref = mod;
			}
			private void checkMod() {
				if (mod != ref) throw new ConcurrentModificationException();
			}
		};
	}
	
	public void swap(int indexA, int indexB) {
		array.swap(indexA, indexB);
		mod++;
	}
	
	@Override
	public int[] toIntArray() {
		return array.toArray();		
	}
	/**
	 * Returns the internal array after trimming it to the current length and
	 * returns it. This <code>DefaultIntList</code> will be empty after this 
	 * operation.
	 */
	public int[] yieldIntArray() {
		return array.yieldArray();
	}
	
	public void trimToLength() {
		array.trimToLength();
		mod++;
	}
	
	/**
	 * Sort the array ascending or descending. If this array is sorted 
	 * ascending, the {@link #binarySearchInt(int)} and 
	 * {@link #addIntToSorted(int)} methods can be used
	 * 
	 * @param ascending	true for ascending sort order
	 */
	public void sort(boolean ascending) {
		array.sort(ascending);
		mod++;
	}
	/**
	 * Sort the specified array range ascending or descending. If this whole 
	 * array is sorted ascending, the {@link #binarySearchInt(int)} and 
	 * {@link #addIntToSorted(int)} methods can be used.
	 * <p>
	 * This method should only be used if one knows that only the defined
	 * interval needs sorting. Otherwise, {@link #sort(boolean)} should be used
	 * instead.
	 * 
	 * @param ascending	true for ascending sort order
	 * @param start		the index where the sorting starts (inclusive)
	 * @param end		the index where the sorting ends (exclusive)
	 */
	public void sort(boolean ascending, int start, int end) {
		array.sort(ascending, start, end);
		mod++;
	}
	
    /**
     * Searches this int array for the specified value using the
     * binary search algorithm.  The array <strong>must</strong> be sorted 
     * ascending (as by the {@link #sort(boolean)} method) prior to making this 
     * call.  If it is not sorted, the results are undefined.  If the array 
     * contains multiple elements with the specified value, there is no 
     * guarantee which one will be found.
     *
     * @param key the value to be searched for.
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     *
     * @see ch.javasoft.util.Arrays#binarySearch(int[], int, int, int)
     */
	public int binarySearchInt(int key) throws IllegalStateException {
		return array.binarySearch(key);
	}
    /**
     * Searches this int array for the specified value using the
     * binary search algorithm.  The array <strong>must</strong> be sorted 
     * ascending (as by the {@link #sort(boolean)} method) prior to making this 
     * call.  If it is not sorted, the results are undefined.  If the array 
     * contains multiple elements with the specified value, there is no 
     * guarantee which one will be found.
     *
     * @param key the value to be searched for.
     * @param fromIndex the index of the first element (inclusive) to be
     *		searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @return index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt>, if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     *
     * @see ch.javasoft.util.Arrays#binarySearch(int[], int, int, int)
     */
	public int binarySearchInt(int key, int fromIndex, int toIndex) throws IllegalStateException {
		return array.binarySearch(key, fromIndex, toIndex);
	}
	
    /**
     * Returns a string representation of the contents of this int array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(int)</tt>.  
     *
     * @return a string representation of <tt>a</tt>
     */
	@Override
	public String toString() {
		return array.toString();
	}
	
}
