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

import java.math.BigInteger;
import java.util.Comparator;

/**
 * The <code>Unsigned</code> class contains static methods and constants useful
 * if ints or longs should be treated as unsigned integers.
 */
public class Unsigned {
	
	/**
	 * Comparator for unsigned long comparison, delegating to 
	 * {@link Unsigned#compare(long, long)}.
	 */
	public static final Comparator<Long> LONG_COMPARATOR = new Comparator<Long>() {
		/**
		 * Performs an unsigned comparison, delegating to 
		 * {@link Unsigned#compare(long, long)}.
		 * 
		 * @param o1 the first long value to be compared.
		 * @param o2 the second long value to be compared.
	     * @return a negative integer, zero, or a positive integer as the
	     * 	       first argument is less than, equal to, or greater than the
	     *	       second. 
		 */
		public int compare(Long o1, Long o2) {
			return Unsigned.compare(o1.longValue(), o2.longValue());
		}
	};
	
	/**
	 * Returns the given {@code value} treated as unsigned value. More formally,
	 * the method returns {@code 256-value} if {@code value < 0}; otherwise,
	 * the {@code value} itself is returned. 
	 * 
	 * @param value	the value to treat as unsigned integer
	 * @return 	{@code value} if <code>value &ge; 0</code> and {@code 256-value} 
	 * 			if {@code value < 0} and 
	 */
	public static final short getUnsigned(byte value) {
		return (short)(0x000000ff & value);
	}
	/**
	 * Returns the given {@code value} treated as unsigned value. More formally,
	 * the method returns <code>2<sup>16</sup>-value</code> if 
	 * {@code value < 0}; otherwise, the {@code value} itself is returned. 
	 * 
	 * @param value	the value to treat as unsigned integer
	 * @return 	{@code value} if <code>value &ge; 0</code> and 
	 * 			<code>2<sup>16</sup>-value</code> if {@code value < 0} and 
	 */
	public static final int getUnsigned(short value) {
		return (0x0000ffff & value);
	}
	/**
	 * Returns the given {@code value} treated as unsigned value. More formally,
	 * the method returns <code>2<sup>32</sup>-value</code> if 
	 * {@code value < 0}; otherwise, the {@code value} itself is returned. 
	 * 
	 * @param value	the value to treat as unsigned integer
	 * @return 	{@code value} if <code>value &ge; 0</code> and 
	 * 			<code>2<sup>32</sup>-value</code> if {@code value < 0} and 
	 */
	public static final long getUnsigned(int value) {
		return (0xffffffffL & value);
	}
	/**
	 * Returns the given {@code value} treated as unsigned value. More formally,
	 * the method returns <code>2<sup>64</sup>-value</code> if 
	 * {@code value < 0}; otherwise, the {@code value} itself is returned. 
	 * 
	 * @param value	the value to treat as unsigned integer
	 * @return 	{@code value} if <code>value &ge; 0</code> and 
	 * 			<code>2<sup>64</sup>-value</code> if {@code value < 0} and 
	 */
	public static final BigInteger getUnsigned(long value) {
		return value >= 0 ? BigInteger.valueOf(value) : 
			new BigInteger(new byte[] {0, 
				(byte)(value >>> 56), (byte)(value >>> 48), 
				(byte)(value >>> 40), (byte)(value >>> 32), 
				(byte)(value >>> 24), (byte)(value >>> 16), 
				(byte)(value >>> 8), (byte)value});
	}

	/**
	 * Sorts the array using unsigned value comparison. This results in an array
	 * that is ascending sorted for nonnegative values; negative values are 
	 * always larger than nonnegative ones, meaning that they are placed after
	 * all positive values. Using unsigned comparison for two values with equal
	 * sign leads to the same result as signed comparison.
	 * 
	 * @param values the values to sort
	 * @return 	the index of the first negative value, or the length of the
	 * 			array if it contains no negative values  
	 */
	public static final int sort(long[] values) {
		final int len = values.length;
		java.util.Arrays.sort(values, 0, len);
		int nneg = java.util.Arrays.binarySearch(values, 0);
		if (nneg < 0) {
			nneg = -(nneg+1);
		}
		final int npos = len-nneg;
		//(A) [-7 -3 -1  0  2  4  5  6]
		//               ^
		//             nneg
		//     < nneg >  <    npos   >
		//(B) [-7 -6 -4 -3 -2 -1  3  5]
		//                        ^
		//                      nneg
		//     <     nneg      > <npos>
		
		//we perform three rotations and swaps as follows:
		// 0. original
		//(A) [-7 -3 -1  0  2  4  5  6]
		//(B) [-7 -6 -4 -3 -2 -1  3  5]
		// 1. rotate
		//(A) [-7 -3 -1  5  6  0  2  4]
		//(B) [-2 -1 -7 -6 -4 -3  3  5]
		// 2. interchange
		//(A) [ 0  2  4  5  6 -7 -3 -1]
		//(B) [ 3  5 -7 -6 -4 -3 -2 -1]		

		//1. rotate
		final int rots = nneg - npos;
		final int rfrom = rots >= 0 ? 0 : nneg;
		final int rto	= rots >= 0 ? nneg : len;
		Arrays.rotate(values, rots, rfrom, rto);
		//2. interchange
		final int swaps = rots >= 0 ? npos : nneg;
		for (int i = 0; i < swaps; i++) {
			Arrays.swap(values, i, len-swaps+i);
		}
		return npos;
	}
	
    /**
     * Searches a range of the specified array of longs for the specified value 
     * using the binary search algorithm and treating all values as unsigned 
     * longs. The array must be unsigned sorted (as by {@link #sort(long[])})
     * prior to making this call. If it is not sorted, the results are 
     * undefined. If the array contains multiple elements with the specified 
     * value, there is no guarantee which one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the array: the index of the first
     *	       element greater than the key, or <tt>a.length</tt> if all
     *	       elements are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @throws IllegalArgumentException
     *	       if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *	       if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(long[] a, long key) {
    	return binarySearch(a, 0, a.length, key);
    }
    /**
     * Searches a range of
     * the specified array of longs for the specified value using the
     * binary search algorithm and treating all values as unsigned longs.
     * The range must be unsigned sorted (as by {@link #sort(long[])})
     * prior to making this call. If it is not sorted, the results are 
     * undefined. If the range contains multiple elements with the specified 
     * value, there is no guarantee which one will be found.
     *
     * @param a the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be
     *		searched
     * @param toIndex the index of the last element (exclusive) to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array
     *	       within the specified range;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the array: the index of the first
     *	       element in the range greater than the key,
     *	       or <tt>toIndex</tt> if all
     *	       elements in the range are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     * @throws IllegalArgumentException
     *	       if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException
     *	       if {@code fromIndex < 0 or toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(long[] a, int fromIndex, int toIndex, long key) {
    	Arrays.rangeCheck(a.length, fromIndex, toIndex);
    	return binarySearch0(a, fromIndex, toIndex, key);
    }

	//copied from java.util.Arrays, jdk1.6, but using unsigned value comparison
    // Like public version, but without range checks.
    private static int binarySearch0(long[] a, int fromIndex, int toIndex, long key) {
		int low = fromIndex;
		int high = toIndex - 1;
	
		while (low <= high) {
		    final int mid = (low + high) >>> 1;
			final long midVal = a[mid];
	
		    final int cmp = compare(midVal, key);
		    if (cmp < 0)
		    	low = mid + 1;
		    else if (cmp > 0)
		    	high = mid - 1;
		    else
		    	return mid; // key found
		}
		return -(low + 1);  // key not found.
    }

	/**
     * Compares its two long arguments for order according to unsigned long
     * comparison. Returns a negative integer, zero, or a positive integer as 
     * the first argument is less than, equal to, or greater than the second.
     * <p>
     * Using unsigned comparison means that negative values are always larger 
     * than nonnegative values. Using unsigned comparison for two values with 
     * equal sign leads to the same result as signed comparison.
	 * 
	 * @param l1 the first long value to be compared.
	 * @param l2 the second long value to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
	 */
	public static final int compare(long l1, long l2) {
		if (l1 >= 0 && l2 >= 0 || l1 < 0 && l2 < 0) {
			return l1<l2 ? -1 : l1>l2 ? 1 : 0;
		}
		return l1 < 0 ? 1 : -1;//negative values are larger in unsigned notation
	}
	
	//no instances
	private Unsigned() {
		super();
	}
}
