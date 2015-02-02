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

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The <code>Arrays</code> class contains static helper methods arround arrays,
 * similar to {@link java.util.Arrays}, but adding missing stuff.
 */
public class Arrays {
	
	public static Object[]	EMPTY_OBJECT	= new Object[0];
	public static long[] 	EMPTY_LONG 		= new long[0];
	public static int[] 	EMPTY_INT		= new int[0];
	public static double[] 	EMPTY_DOUBLE	= new double[0];
	public static float[] 	EMPTY_FLOAT 	= new float[0];
	
	/**
	 * Returns a modifiable {@link LinkedHashSet} containing the submitted 
	 * elements. If the list is empty or null, an empty linked hash set is 
	 * returned. In any case, the returned set is modifiable.
	 * 
	 * @param <T>	the element type
	 * @param a		the element(s)
	 * @return a linked hash set containing the elements
	 */
    public static <T> Set<T> asSet(T... a) {
    	if (a == null || a.length == 0) {
    		return new LinkedHashSet<T>(0);
    	}
    	return new LinkedHashSet<T>(java.util.Arrays.asList(a));
    }
	
	public static void swap(Object[] arr, int indexA, int indexB) {
		Object tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(long[] arr, int indexA, int indexB) {
		long tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(int[] arr, int indexA, int indexB) {
		int tmp		= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(short[] arr, int indexA, int indexB) {
		short tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(byte[] arr, int indexA, int indexB) {
		byte tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(char[] arr, int indexA, int indexB) {
		char tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(boolean[] arr, int indexA, int indexB) {
		boolean tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(double[] arr, int indexA, int indexB) {
		double tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swap(float[] arr, int indexA, int indexB) {
		float tmp	= arr[indexA];
		arr[indexA] = arr[indexB];
		arr[indexB] = tmp;
	}
	public static void swapRow(Object[][] arr, int rowA, int rowB) {
		Object[] tmp 	= arr[rowA];
		arr[rowA] 		= arr[rowB];
		arr[rowB] 		= tmp;
	}
	public static void swapRow(long[][] arr, int rowA, int rowB) {
		long[] tmp 	= arr[rowA];
		arr[rowA] 	= arr[rowB];
		arr[rowB] 	= tmp;
	}
	public static void swapRow(int[][] arr, int rowA, int rowB) {
		int[] tmp = arr[rowA];
		arr[rowA] = arr[rowB];
		arr[rowB] = tmp;
	}
	public static void swapRow(short[][] arr, int rowA, int rowB) {
		short[] tmp = arr[rowA];
		arr[rowA] 	= arr[rowB];
		arr[rowB] 	= tmp;
	}
	public static void swapRow(byte[][] arr, int rowA, int rowB) {
		byte[] tmp 	= arr[rowA];
		arr[rowA] 	= arr[rowB];
		arr[rowB] 	= tmp;
	}
	public static void swapRow(char[][] arr, int rowA, int rowB) {
		char[] tmp 	= arr[rowA];
		arr[rowA] 	= arr[rowB];
		arr[rowB] 	= tmp;
	}
	public static void swapRow(boolean[][] arr, int rowA, int rowB) {
		boolean[] tmp 	= arr[rowA];
		arr[rowA] 		= arr[rowB];
		arr[rowB] 		= tmp;
	}
	public static void swapRow(double[][] arr, int rowA, int rowB) {
		double[] tmp	= arr[rowA];
		arr[rowA] 		= arr[rowB];
		arr[rowB] 		= tmp;
	}
	public static void swapRow(float[][] arr, int rowA, int rowB) {
		float[] tmp		= arr[rowA];
		arr[rowA] 		= arr[rowB];
		arr[rowB] 		= tmp;
	}
	public static void swapCol(Object[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		Object tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(long[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		long tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(int[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		int tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(short[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		short tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(byte[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		byte tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(char[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		char tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(boolean[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		boolean tmp 	= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(double[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
			double tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	public static void swapCol(float[][] arr, int colA, int colB) {
    	for (int row = 0; row < arr.length; row++) {
    		float tmp 		= arr[row][colA];
			arr[row][colA] 	= arr[row][colB];
			arr[row][colB] 	= tmp;
		}
    }
	
	/**
	 * Rotates the elements in {@code arr} so that the index {@code i} holds the 
	 * value that used to be at the position {@code (i+k)%n}, where {@code n}
	 * denotes the length of the array. The rotation is performed in-place.
	 *  
	 * @param arr	the array
	 * @param k		the rotation number, negative for right and positive for
	 * 				left rotations
	 */
	public static void rotate(long[] arr, int k) {
		final int n = arr.length;
	    if (n <= 1) return;
	    if (k < 0 || k >= n) {
	        k %= n;
	        if (k < 0) k += n;
	    }
	    if (k == 0) return;

	    long tmp;
	    int v = 0;
	    int c = 0;
	    while (c < n) {
	        int t = v, tp = v + k;
	        tmp = arr[v];
	        c++;
	        while (tp != v) {
	            arr[t] = arr[tp];
	            t = tp;
	            tp += k;
	            if (tp >= n) tp -= n;
	            c++;
	        }
	        arr[t] = tmp;
	        v++;
	    }
	}
	/**
	 * Rotates the elements in {@code arr} so that the index {@code i} holds the 
	 * value that used to be at the position {@code (i+k)%n}, where {@code n}
	 * denotes the length of the array. The rotation is performed in-place.
	 *  
	 * @param arr	the array
	 * @param k		the rotation number, negative for right and positive for
	 * 				left rotations
     * @param from the initial index of the range to be rotated, inclusive
     * @param to the final index of the range to be rotated, exclusive.
     *     (This index may lie outside the array.)
	 */
	public static void rotate(long[] arr, int k, int from, int to) {
		final int n = to - from;
	    if (n <= 1) return;
	    if (k < 0 || k >= n) {
	        k %= n;
	        if (k < 0) k += n;
	    }
	    if (k == 0) return;

	    long tmp;
	    int v = from;
	    int c = from;
	    while (c < to) {
	        int t = v, tp = v + k;
	        tmp = arr[v];
	        c++;
	        while (tp != v) {
	            arr[t] = arr[tp];
	            t = tp;
	            tp += k;
	            if (tp >= to) tp -= n;
	            c++;
	        }
	        arr[t] = tmp;
	        v++;
	    }
	}
	
	/**
	 * Creates a new typed array using the type of an existing typed array to
	 * define the component type of the new array.
	 * 
	 * @param <T>		the	type of a component of the array 
	 * @param template	the template array used to define the component type of 
	 * 					the new array
	 * @param length	the length of the new array to return
	 * @return the new array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(T[] template, int length) {		
		return newArray((Class<T>)template.getClass().getComponentType(), length);
	}
	/**
	 * Creates a new typed array using the specified class to define the 
	 * component type of the new array.
	 * 
	 * @param <T>		the	type of a component of the array 
	 * @param clazz		the class specifying the component type of the new array
	 * @param length	the length of the new array to return
	 * @return the new array
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(Class<T> clazz, int length) {
		return (T[])Array.newInstance(clazz, length);
	}
	/**
	 * Creates a new typed class array using the specified class to define the 
	 * component type of the new array.
	 * 
	 * @param <T>		the	type of a component of the array 
	 * @param clazz		the class specifying the component type of the new array
	 * @param length	the length of the new array to return
	 * @return the new array
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T>[] newClassArray(Class<T> clazz, int length) {
		return new Class[length];
	}
	/**
	 * Creates a new typed subclass array using the specified class to define 
	 * the component type of the new array.
	 * 
	 * @param <T>		the	type of a component of the array 
	 * @param clazz		the class specifying the component type of the new array
	 * @param length	the length of the new array to return
	 * @return the new array
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T>[] newSubclassArray(Class<T> clazz, int length) {
		return new Class[length];
	}

	public static <T> T[] merge(T[] arr, T... others) {
		final T[] res = newArray(arr, arr.length + others.length);
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static long[] merge(long[] arr, long... others) {
		final long[] res = new long[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static int[] merge(int[] arr, int... others) {
		final int[] res = new int[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static short[] merge(short[] arr, short... others) {
		final short[] res = new short[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static byte[] merge(byte[] arr, byte... others) {
		final byte[] res = new byte[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static char[] merge(char[] arr, char... others) {
		final char[] res = new char[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static boolean[] merge(boolean[] arr, boolean... others) {
		final boolean[] res = new boolean[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static double[] merge(double[] arr, double... others) {
		final double[] res = new double[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	public static float[] merge(float[] arr, float... others) {
		final float[] res = new float[arr.length + others.length];
		System.arraycopy(arr, 0, res, 0, arr.length);
		System.arraycopy(others, 0, res, arr.length, others.length);
		return res;
	}
	
	public static void printArray(int[] arr) {
		printArray(System.out, arr);
	}
	public static void printArray(double[] arr) {
		printArray(System.out, arr);
	}
	public static void printArray(PrintStream out, int[] arr) {
		out.print('[');
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) out.print(", ");
			out.print(arr[i]);
		}
		out.println(']');
	}
	public static void printArray(PrintStream out, double[] arr) {
		out.print('[');
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) out.print(", ");
			out.print(arr[i]);
		}
		out.println(']');
	}
	public static void printArray(int[][] arr) {
		printArray(System.out, arr);
	}
	public static void printArray(double[][] arr) {
		printArray(System.out, arr);
	}
	public static void printArray(PrintStream out, int[][] arr) {
		int rows = arr.length;
		int cols = 0;
		for (int i = 0; i < arr.length; i++) {
			if (cols == 0) cols = arr[i].length;
			else if (cols != arr[i].length) cols = -1;
		}
		out.println(rows + "x" + (cols < 0 ? "?" : String.valueOf(cols)) + "{");
		for (int i = 0; i < arr.length; i++) {
			out.print('\t');
			printArray(out, arr[i]);
		}
		out.println("}");
	}
	public static void printArray(PrintStream out, double[][] arr) {
		int rows = arr.length;
		int cols = 0;
		for (int i = 0; i < arr.length; i++) {
			if (cols == 0) cols = arr[i].length;
			else if (cols != arr[i].length) cols = -1;
		}
		out.println(rows + "x" + (cols < 0 ? "?" : String.valueOf(cols)) + "{");
		for (int i = 0; i < arr.length; i++) {
			out.print('\t');
			printArray(out, arr[i]);
		}
		out.println("}");
	}
	
	////////////////////////////////////////////////////////////////////////////
	// backward compat stuff for jdk1.5
	
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>null</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     * <p>
     * The resulting array is of exactly the same class as the original array.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with nulls to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
	public static <T> T[] copyOfRange(T[] original, int from, int to) {
        return copyOfRange(original, from, to, (Class<T[]>) original.getClass());
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>null</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     * The resulting array is of the class <tt>newType</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @param newType the class of the copy to be returned
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with nulls to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @throws ArrayStoreException if an element copied from
     *     <tt>original</tt> is not of a runtime type that can be stored in
     *     an array of class <tt>newType</tt>.
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
	public static <T,U> T[] copyOfRange(U[] original, int from, int to, Class<? extends T[]> newType) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified range of the specified array into a new array.
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
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static double[] copyOfRange(double[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        double[] copy = new double[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified range of the specified array into a new array.
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
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if {@code from < 0}
     *     or {@code from > original.length}
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static long[] copyOfRange(long[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        long[] copy = new long[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if <tt>from &lt; 0</tt>
     *     or <tt>from &gt; original.length()</tt>
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static int[] copyOfRange(int[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        int[] copy = new int[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified range of the specified array into a new array.
     * The initial index of the range (<tt>from</tt>) must lie between zero
     * and <tt>original.length</tt>, inclusive.  The value at
     * <tt>original[from]</tt> is placed into the initial element of the copy
     * (unless <tt>from == original.length</tt> or <tt>from == to</tt>).
     * Values from subsequent elements in the original array are placed into
     * subsequent elements in the copy.  The final index of the range
     * (<tt>to</tt>), which must be greater than or equal to <tt>from</tt>,
     * may be greater than <tt>original.length</tt>, in which case
     * <tt>(byte)0</tt> is placed in all elements of the copy whose index is
     * greater than or equal to <tt>original.length - from</tt>.  The length
     * of the returned array will be <tt>to - from</tt>.
     *
     * @param original the array from which a range is to be copied
     * @param from the initial index of the range to be copied, inclusive
     * @param to the final index of the range to be copied, exclusive.
     *     (This index may lie outside the array.)
     * @return a new array containing the specified range from the original array,
     *     truncated or padded with zeros to obtain the required length
     * @throws ArrayIndexOutOfBoundsException if <tt>from &lt; 0</tt>
     *     or <tt>from &gt; original.length()</tt>
     * @throws IllegalArgumentException if <tt>from &gt; to</tt>
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of exactly the same class as the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
	public static <T> T[] copyOf(T[] original, int newLength) {
        return (T[]) copyOf(original, newLength, original.getClass());
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified array, truncating or padding with nulls (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>null</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     * The resulting array is of the class <tt>newType</tt>.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @param newType the class of the copy to be returned
     * @return a copy of the original array, truncated or padded with nulls
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @throws ArrayStoreException if an element copied from
     *     <tt>original</tt> is not of a runtime type that can be stored in
     *     an array of class <tt>newType</tt>
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
	public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>(byte)0</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>0</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static int[] copyOf(int[] original, int newLength) {
        int[] copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Copies the specified array, truncating or padding with zeros (if necessary)
     * so the copy has the specified length.  For all indices that are
     * valid in both the original array and the copy, the two arrays will
     * contain identical values.  For any indices that are valid in the
     * copy but not the original, the copy will contain <tt>0d</tt>.
     * Such indices will exist if and only if the specified length
     * is greater than that of the original array.
     *
     * @param original the array to be copied
     * @param newLength the length of the copy to be returned
     * @return a copy of the original array, truncated or padded with zeros
     *     to obtain the specified length
     * @throws NegativeArraySizeException if <tt>newLength</tt> is negative
     * @throws NullPointerException if <tt>original</tt> is null
     * @since 1.6
     */
    public static double[] copyOf(double[] original, int newLength) {
        double[] copy = new double[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Searches the specified array of bytes for the specified value using the
     * binary search algorithm.  The array must be sorted (as
     * by the {@link java.util.Arrays#sort(byte[])} method) prior to making this call.  If it
     * is not sorted, the results are undefined.  If the array contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
     *
     * @param a the array to be searched
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the array;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the array: the index of the first
     *	       element greater than the key, or <tt>a.length</tt> if all
     *	       elements in the array are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.
     */
    public static int binarySearch(byte[] a, byte key) {
	return binarySearch0(a, 0, a.length, key);
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Searches a range of
     * the specified array of bytes for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link java.util.Arrays#sort(byte[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
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
    public static int binarySearch(byte[] a, int fromIndex, int toIndex,
				   byte key) {
	rangeCheck(a.length, fromIndex, toIndex);
	return binarySearch0(a, fromIndex, toIndex, key);
    }
	//copied from java.util.Arrays, jdk1.6
    // Like public version, but without range checks.
    private static int binarySearch0(byte[] a, int fromIndex, int toIndex,
				     byte key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    byte midVal = a[mid];

	    if (midVal < key)
		low = mid + 1;
	    else if (midVal > key)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found.
    }
    
	//copied from java.util.Arrays, jdk1.6
    /**
     * Searches a range of
     * the specified array of ints for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link java.util.Arrays#sort(int[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
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
     * @throws IllegalArgumentException	if {@code fromIndex > toIndex}
     * @throws ArrayIndexOutOfBoundsException	if {@code fromIndex < 0 or 
     * 											toIndex > a.length}
     * @since 1.6
     */
    public static int binarySearch(int[] a, int fromIndex, int toIndex,
				   int key) {
	rangeCheck(a.length, fromIndex, toIndex);
	return binarySearch0(a, fromIndex, toIndex, key);
    }
	//copied from java.util.Arrays, jdk1.6
    /**
     * Check that fromIndex and toIndex are in range, and throw an
     * appropriate exception if they aren't.
     */
    /*package*/ static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                       ") > toIndex(" + toIndex+")");
        if (fromIndex < 0)
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLen)
            throw new ArrayIndexOutOfBoundsException(toIndex);
    }
	//copied from java.util.Arrays, jdk1.6
    // Like public version, but without range checks.
    private static int binarySearch0(int[] a, int fromIndex, int toIndex,
				     int key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    int midVal = a[mid];

	    if (midVal < key)
		low = mid + 1;
	    else if (midVal > key)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found.
    }
	
	//copied from java.util.Arrays, jdk1.6
    /**
     * Searches a range of
     * the specified array of longs for the specified value using the
     * binary search algorithm.
     * The range must be sorted (as
     * by the {@link java.util.Arrays#sort(long[], int, int)} method)
     * prior to making this call.  If it
     * is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.
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
    public static int binarySearch(long[] a, int fromIndex, int toIndex,
				   long key) {
	rangeCheck(a.length, fromIndex, toIndex);
	return binarySearch0(a, fromIndex, toIndex, key);
    }

	//copied from java.util.Arrays, jdk1.6
    // Like public version, but without range checks.
    private static int binarySearch0(long[] a, int fromIndex, int toIndex,
				     long key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    long midVal = a[mid];

	    if (midVal < key)
		low = mid + 1;
	    else if (midVal > key)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found.
    }

	//copied from java.util.Arrays, jdk1.6
    /**
     * Searches a range of
     * the specified array of doubles for the specified value using
     * the binary search algorithm.
     * The range must be sorted
     * (as by the {@link java.util.Arrays#sort(double[], int, int)} method)
     * prior to making this call.
     * If it is not sorted, the results are undefined.  If the range contains
     * multiple elements with the specified value, there is no guarantee which
     * one will be found.  This method considers all NaN values to be
     * equivalent and equal.
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
    public static int binarySearch(double[] a, int fromIndex, int toIndex,
				   double key) {
	rangeCheck(a.length, fromIndex, toIndex);
	return binarySearch0(a, fromIndex, toIndex, key);
    }
    
	//copied from java.util.Arrays, jdk1.6    
    // Like public version, but without range checks.
    private static int binarySearch0(double[] a, int fromIndex, int toIndex,
				     double key) {
	int low = fromIndex;
	int high = toIndex - 1;

	while (low <= high) {
	    int mid = (low + high) >>> 1;
	    double midVal = a[mid];

            int cmp;
            if (midVal < key) {
                cmp = -1;   // Neither val is NaN, thisVal is smaller
            } else if (midVal > key) {
                cmp = 1;    // Neither val is NaN, thisVal is larger
            } else {
                long midBits = Double.doubleToLongBits(midVal);
                long keyBits = Double.doubleToLongBits(key);
                cmp = (midBits == keyBits ?  0 : // Values are equal
                       (midBits < keyBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                        1));                     // (0.0, -0.0) or (NaN, !NaN)
            }

	    if (cmp < 0)
		low = mid + 1;
	    else if (cmp > 0)
		high = mid - 1;
	    else
		return mid; // key found
	}
	return -(low + 1);  // key not found.
    }

    ////////////////////////////////////////////////////////////////////////////
	//no instances
	private Arrays() {
		super();
	}
}
