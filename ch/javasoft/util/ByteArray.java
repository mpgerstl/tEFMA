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
package ch.javasoft.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * The <code>ByteArray</code> is an dynamic size array for bytes. Values can be 
 * added causing the array to grow dynamically.
 */
public class ByteArray implements Iterable<Byte>, Cloneable, Serializable {

	private static final long serialVersionUID = 6523901146842278626L;

	/**
	 * Constant for a byte array of length 0
	 */
	public static final byte[] EMPTY_ARRAY = new byte[0];

	private static final int DEFAULT_CAPACITY	= 7;

	private int		mLength = 0;
	private byte[] 	mArray;

	/**
	 * Constructor for <code>ByteArray</code> with default capacity
	 */
	public ByteArray() {
		this(DEFAULT_CAPACITY);
	}
	/**
	 * Constructor for <code>ByteArray</code> with default capacity
	 */
	public ByteArray(int capacity) {
		mArray = new byte[capacity];
		initialize(mArray, 0, capacity, false);
	}
	/**
	 * Constructor for <code>ByteArray</code> with specified values. The given
	 * array is not cloned, thus, changes to the array are also reflected in
	 * the state this <code>ByteArray</code>.
	 */
	public ByteArray(byte[] initialValues) {
		mArray	= initialValues;
		mLength	= initialValues.length;
	}
	/**
	 * Constructor for <code>ByteArray</code> with specified values. The given
	 * array is cloned.
	 * <p>
     * Copies the specified range of the specified array into this new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>0d</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the this array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
	 */
	public ByteArray(byte[] original, int from, int to) {
		mArray  = ch.javasoft.util.Arrays.copyOfRange(original, from, to);
		mLength	= to - from;
	}
	/**
	 * Returns the current length of this array, that is, the number of elements
	 * in the array.
	 */
	public int length() {
		return mLength;
	}
	/**
	 * Returns true if the array {@link #length() length} is zero
	 */
	public boolean isEmpty() {
		return mLength == 0;
	}
	/**
	 * Returns the value at the specified position
	 */
	public byte get(int index) throws IndexOutOfBoundsException {
		if (index >= mLength) {
			throw new IndexOutOfBoundsException(
					"index not in [0, " + (mLength - 1) + "]: " + index
			);
		}
		return mArray[index];
	}
	/**
	 * Returns the first index containing a value equal to the specified 
	 * <code>value</code>. If no such value is found, <code>-1</code> is 
	 * returned.
	 * 
	 * @param 	value	the value to search for
	 * @return	the index of the first occurrence of <code>value</code>, or 
	 * 			<code>-1</code> if no such value is found
	 */
	public int indexOf(byte value) {
		return indexOf(0, value);
	}
	/**
	 * Returns the first index containing a value equal to the specified 
	 * <code>value</code> starting at position <code>fromIndex</code>. If no 
	 * such value is found, <code>-1</code> is returned.
	 * 
	 * @param fromIndex	the index (inclusive) at which the search is started
	 * @param value		the value to search for
	 * @return	the index of the first occurrence of <code>value</code>, or 
	 * 			<code>-1</code> if no such value is found
	 */
	public int indexOf(int fromIndex, byte value) {
		for (int i = fromIndex; i < mLength; i++) {
			if (value == mArray[i]) return i;
		}
		return -1;
	}
	/**
	 * Sets the value at the specified position. If <code>index</code> is 
	 * smaller than the current {@link #length()}, the value at the specified 
	 * position is replaced by <code>value</code>. If <code>index</code> equal
	 * to the <code>length</code> of this array, the value is appended. If it
	 * is larger than <code>length</code>, it is also appended after filling the
	 * gap positions with {@link #initialValue() initial values}.
	 * 
	 * @param index	the index at which <code>value</code> will be stored
	 * @param value the new value to be set
	 */
	public byte set(int index, byte value) throws IndexOutOfBoundsException {
		byte old;
		if (index >= mLength) {
			ensureCapacity(index + 1);
			old = initialValue();
		}
		else {
			old = mArray[index];			
		}
		mArray[index]	= value;
		mLength			= Math.max(mLength, index + 1);
		return old;
	}
	/**
	 * Adds the specified value to the end of this byte array
	 */
	public void add(byte value) {
		set(mLength, value);
	}
	/**
	 * Adds the values from <code>array</code> to the end of this byte array
	 */
	public void addAll(ByteArray array) {
		addAll(0, mLength, array.mArray);
	}
	/**
	 * Adds the values from <code>array</code> to the end of this int array
	 */
	public void addAll(int from, int to, ByteArray array) {
		if (to > array.mLength) throw new IndexOutOfBoundsException("to index after length: " + to + " > " + array.mLength);
		addAll(from, to, array.mArray);
	}
	/**
	 * Adds the specified values to the end of this byte array
	 */
	public void addAll(byte... values) {
		addAll(0, values.length, values);
	}
	/**
	 * Adds the specified values to the end of this byte array
	 */
	public void addAll(int from, int to, byte... values) {
		if (from < 0) throw new IndexOutOfBoundsException("from must be non-negative: " + from);
		if (to < from) throw new IndexOutOfBoundsException("to must not be smaller than from: " + to + " < " + from);
		ensureCapacity(mLength + to - from);
		System.arraycopy(values, from, mArray, mLength, to - from);
		mLength += to - from;
	}
	/**
	 * Adds the specified value at the given position of the array. If the
	 * <code>index</code> is negative or larger than the current 
	 * {@link #length() length} of this array, an 
	 * {@link IndexOutOfBoundsException} is thrown. 
	 */
	public boolean add(int index, byte value) {
		if (index < 0 || index > mLength) {
			throw new IndexOutOfBoundsException("index must be >=0 and <= length, but was " + index);
		}
		ensureCapacity(mLength + 1);
		System.arraycopy(mArray, index, mArray, index + 1, mLength - index);
		mArray[index] = value;
		mLength++;
		return true;
	}
	/**
	 * Removes the last value, or throws an {@link IndexOutOfBoundsException} if
	 * the array is empty.  
	 */
	public byte removeLast() {
		return remove(mLength - 1);
	}
	/**
	 * Removes the specified value and moves all values on the right one 
	 * position leftwards. 
	 * 
	 * @throws  IndexOutOfBoundsException	if <code>index</code> is negative or
	 * 										larger or equal to 
	 * 										{@link #length() length}
	 */
	public byte remove(int index) {
		if (index >= mLength) {
			throw new IndexOutOfBoundsException(
					"index not in [0, " + (mLength - 1) + "]: " + index
			);
		}
		byte old = mArray[index];
		mLength--;
		System.arraycopy(mArray, index + 1, mArray, index, mLength - index);
		return old;
	}
	/**
	 * Removes all elements, but does not shrink the underlying array (that is,
	 * the capacity is kept as it is).
	 */
	public void clear() {
		initialize(mArray, 0, mLength, true);
		mLength = 0;
	}
	/**
	 * Returns the first element, or throws an {@link IndexOutOfBoundsException}
	 * if the array is empty
	 */
	public byte first() {
		return get(0);
	}
	/**
	 * Returns the last element, or throws an {@link IndexOutOfBoundsException}
	 * if the array is empty
	 */
	public byte last() {
		return get(mLength - 1);
	}
	/**
	 * Interchange the values at the specified positions
	 */
	public void swap(int indexA, int indexB) {
		if (indexA < 0 || indexA >= mLength) {
			throw new IndexOutOfBoundsException(
				"index " + indexA + " not in [0, " + (mLength - 1) + "]"
			);
		}
		if (indexB < 0 || indexB >= mLength) {
			throw new IndexOutOfBoundsException(
				"index " + indexB + " not in [0, " + (mLength - 1) + "]"
			);
		}
		byte tmp = mArray[indexA];
		mArray[indexA] = mArray[indexB];
		mArray[indexB] = tmp;
	}
	
	/**
	 * Internal method, called to ensure capacity <code>size</code>
	 * @param size	the desired capacity
	 */
	protected void ensureCapacity(int size) {
		if (size >= mArray.length) {
			int newSize = Math.max(size, mArray.length * 2);
			byte[] newArr = new byte[newSize];
			System.arraycopy(mArray, 0, newArr, 0, mLength);
			initialize(newArr, mLength, newSize, false);
			mArray = newArr;
		}
	}
	
	/**
	 * This method can be overriden to define an alternate initial value. The
	 * initial value is used if {@link #set(int, byte)} is used with an index
	 * larger than the length of the array.
	 * <p>
	 * Default initial value is 0
	 */
	protected byte initialValue() {
		return 0;
	}
	
	private void initialize(byte[] array, int from, int to, boolean force) {
		final byte initialValue = initialValue();
		if ((force || initialValue != 0) && from < to) {
			Arrays.fill(array, from, to, initialValue);
		}
	}
	
	/**
	 * Clones the underlying array (trimmed to length) and returns it
	 * 
	 * @return	a copy of the underlying array, trimmed to the length of this \
	 * 			byte array
	 */
	public byte[] toArray() {
		byte[] arr = new byte[mLength];
		System.arraycopy(mArray, 0, arr, 0, mLength);
		return arr;		
	}
	/**
	 * Returns the internal array after trimming it to the current length and
	 * returns it. This <code>ByteArray</code> will be empty after this 
	 * operation.
	 */
	public byte[] yieldArray() {
		trimToLength();
		final byte[] arr = mArray;
		mArray 	= new byte[DEFAULT_CAPACITY];
		initialize(mArray, 0, DEFAULT_CAPACITY, false);
		mLength	= 0;
		return arr;
	}
	
	/**
	 * Returns a clone of this byte array
	 */
	@Override
	public ByteArray clone() {
		return new ByteArray(mArray, 0, mLength);
	}

	/**
	 * Returns a new array instance containing a copy of a range of this array.
	 * The range starts from <code>from</code> (inclusive) to the end of this
	 * array.
	 * 
	 * @param from	the start index of the range, inclusive
	 * @return a new array containing a copy of the specified range
	 */
	public ByteArray subRange(int from) {
		return subRange(from, mLength);
	}
	/**
	 * Returns a new array instance containing a copy of a range of this array.
	 * The range starts from <code>from</code> (inclusive) to <code>to</code>
	 * (exclusive).
	 * 
	 * @param from	the start index of the range, inclusive
	 * @param to	the end index of the range, exclusive
	 * @return a new array containing a copy of the specified range
	 */
	public ByteArray subRange(int from, int to) {
		return new ByteArray(mArray, from, to);
	}

	/**
	 * Returns an immutable iterator with the byte values of this array
	 */
	public Iterator<Byte> iterator() {
		return new Iterator<Byte>() {
			int index = 0;
			public boolean hasNext() {
				return index < length();
			}
			public Byte next() {
				return Byte.valueOf(get(index++));
			}
			public void remove() {
				throw new UnsupportedOperationException("immutable iterator");
			}
		};
	}

	/**
	 * Converts the given array of <code>ByteArray</code> instances to a two
	 * dimensional byte array
	 * @param mx	the array of <code>ByteArray</code> instances to convert to 
	 * 				a 2-dim array
	 * @return the two dimensional array
	 */
	public static byte[][] toMatrix(ByteArray[] mx) {
		byte[][] res = new byte[mx.length][];
		for (int i = 0; i < mx.length; i++) {
			res[i] = mx[i].toArray();
		}
		return res;
	}
	/**
	 * Converts the given collection of <code>ByteArray</code> instances to a 
	 * two dimensional byte array
	 * 
	 * @param mx			the collection of arrays to convert to a 2-dim array
	 * @return the two dimensional array
	 */
	public static byte[][] toMatrix(Collection<ByteArray> mx) {
		byte[][] res = new byte[mx.size()][];
		int ri = 0;
		for (ByteArray row : mx) {
			res[ri++] = row.toArray();
		}
		return res;
	}
	/**
	 * Converts the given collection of byte arrays to a two dimensional byte 
	 * array. The source arrays are cloned upon request.
	 * 
	 * @param mx			the collection of arrays to convert to a 2-dim array
	 * @param cloneArrays	if true, the source arrays are cloned
	 * @return the two dimensional array
	 */
	public static byte[][] toMatrix(Collection<byte[]> mx, boolean cloneArrays) {
		byte[][] res = new byte[mx.size()][];
		int ri = 0;
		if (cloneArrays) {
			for (byte[] row : mx) {
				res[ri++] = row.clone();
			}
			
		}
		else {
			for (byte[] row : mx) {
				res[ri++] = row;
			}			
		}
		return res;
	}
	/**
	 * Interchange the values at the specified array positions
	 */
	public static void swap(byte[] arr, int indexA, int indexB) {
		byte tmp = arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	/**
	 * Returns a deep clone of the given array
	 */
	public static byte[][] clone(byte[][] arr) {
		final byte[][] clone = new byte[arr.length][];
		for (int i = 0; i < clone.length; i++) {
			final int len = arr[i].length;
			clone[i] = new byte[len];
			System.arraycopy(arr[i], 0, clone[i], 0, len);
		}
		return clone;
	}

	/**
	 * Fixes the capacity of the underlying array to the current 
	 * {@link #length() length} of this array. Note that this operation might
	 * be expensive due to array copying.
	 */
	public void trimToLength() {
		if (mLength < mArray.length) {
			byte[] arr = new byte[mLength];
			System.arraycopy(mArray, 0, arr, 0, mLength);
			mArray = arr;
		}
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
	public int binarySearch(byte key) throws IllegalStateException {
		return binarySearch(key, 0, mLength);
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
	public int binarySearch(byte key, int fromIndex, int toIndex) throws IllegalStateException {
		return ch.javasoft.util.Arrays.binarySearch(mArray, fromIndex, toIndex, key);
	}
	/**
	 * Adds the given value at the position where the value fits according
	 * to {@link #binarySearch(byte)}. If this int array is not sorted 
	 * ascending, the result is undefined.
	 * 
	 * @param value the value to insert
	 * @return	the position where the value has been inserted
	 */
	public int addToSorted(byte value) {
		int pos = binarySearch(value);
		if (pos < 0) pos = -(pos + 1);
		add(pos, value);
		return pos;
	}

	/**
	 * Sort the array ascending or descending. If this array is sorted 
	 * ascending, the {@link #binarySearch(byte)} and 
	 * {@link #addToSorted(byte)} methods can be used
	 * 
	 * @param ascending	true for ascending sort order
	 */
	public void sort(boolean ascending) {
		sort(ascending, 0, mLength);
	}
	/**
	 * Sort the specified array range ascending or descending. If this whole 
	 * array is sorted ascending, the {@link #binarySearch(byte)} and 
	 * {@link #addToSorted(byte)} methods can be used.
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
		Arrays.sort(mArray, start, end);
		if (!ascending) {
			for (int ii = start; ii < end / 2; ii++) {
				final byte tmp = mArray[ii];
				mArray[ii] = mArray[end - ii - 1];
				mArray[end - ii - 1] = tmp;
			}
		}
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		for (int i = 0; i < mLength; i++) {
			final byte val = mArray[i]; 
			hash ^= val;
			hash ^= (val >>> 32);
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof ByteArray) {
			final ByteArray other = (ByteArray)obj;
			if (mLength != other.mLength) return false;
			for (int i = 0; i < mLength; i++) {
				if (mArray[i] != other.mArray[i]) return false;
			}
			return true;
		}
		return false;
	}

	/**
     * Returns a string representation of the contents of this array.
     * The string representation consists of a list of the array's elements,
     * enclosed in square brackets (<tt>"[]"</tt>).  Adjacent elements are
     * separated by the characters <tt>", "</tt> (a comma followed by a
     * space).  Elements are converted to strings as by
     * <tt>String.valueOf(byte)</tt>.  
     *
     * @return a string representation of <tt>this</tt> array
	 * @see java.util.Arrays#toString(byte[])
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int ii = 0; ii < mLength; ii++) {
			if (ii > 0) sb.append(", ");
			sb.append(mArray[ii]);
		}
		sb.append(']');
		return sb.toString();
	}
	
}
