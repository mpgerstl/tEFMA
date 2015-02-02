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
package ch.javasoft.util.genarr;

import java.util.List;

public class GenericDynamicArray<T> extends AbstractGenericArray<T> implements CloneableTyped<GenericDynamicArray<T>> {
	
	@SuppressWarnings("unchecked")
	public static <T> GenericDynamicArray empty() {
		return new GenericDynamicArray<T>((T[])EMPTY);
	}
	
	private static final int DEFAULT_CAPACITY = 7;
	
	private int	mLength = 0;
	private T[]	mArray;

	public GenericDynamicArray() {
		this(DEFAULT_CAPACITY);
	}
	
	@SuppressWarnings("unchecked")
	public GenericDynamicArray(int initialCapacity) {
		mArray = initialCapacity == 0 ? (T[])EMPTY : newArray(initialCapacity);
		//mLength = 0
	}
	
	public GenericDynamicArray(T... array) {
		mArray	= array;
		mLength	= array.length;
	}
	
	public GenericDynamicArray(GenericDynamicArray<? extends T> dynamicArray) {
		mArray	= dynamicArray.mArray.clone();
		mLength	= dynamicArray.mLength;
	}
	public GenericDynamicArray(GenericArray<? extends T> genericArray) {
		mArray	= newArray(genericArray.length());
		genericArray.toArray(mArray);
		mLength	= genericArray.length();
	}
	
	public GenericDynamicArray(Iterable<? extends T> iterable) {
		this();
		addAll(iterable);
	}
	
	@Override
	public int length() {
		return mLength;
	}
	public int capacity() {
		return mArray.length;
	}
	
	public void trimToLength() {
		trim(false);
	}
	public void trimToNonNull() {
		trim(true);
	}

	private void trim(boolean nonNull) {
		int len = length();
		if (nonNull) {
			while (len - 1 >= 0 && mArray[len - 1] == null) len--;
		}
		if (len != mArray.length) {
			T[] newArr = newArray(len);
			System.arraycopy(mArray, 0, newArr, 0, len);
			mArray = newArr;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <TT> TT[] toArray(TT[] a) {
		int size = length();
        if (a.length < size) {
            a = (TT[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);        	
        }
        System.arraycopy(mArray, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;        	
        }
        return a;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <TT> TT[] toArray(Class<TT> componentType) {
		int size = length();
        TT[] arr = (TT[])java.lang.reflect.Array.newInstance(componentType, size);        	
        System.arraycopy(mArray, 0, arr, 0, size);
        return arr;
	}
	
	@SuppressWarnings("unchecked")
	public void copyFrom(GenericArray<? extends T> src, int srcPos, int dstPos, int length) {
		if (src instanceof GenericDynamicArray) {
			copyFrom((GenericDynamicArray<T>)src, srcPos, dstPos, length);
		}
		else {
			src.copyTo(this, srcPos, dstPos, length);
		}
	}
	@SuppressWarnings("unchecked")
	public void copyTo(GenericArray<? super T> dst, int srcPos, int dstPos, int length) {
		if (dst instanceof GenericDynamicArray) {
			copyTo((GenericDynamicArray<T>)dst, srcPos, dstPos, length);
		}
		else {
			dst.set(dstPos, mArray, srcPos, length);
		}
	}	

	public void copyFrom(GenericDynamicArray<? extends T> src, int srcPos, int dstPos, int length) {
		set(dstPos, src.mArray, srcPos, length);
	}
	public void copyTo(GenericDynamicArray<? super T> dst, int srcPos, int dstPos, int length) {
		dst.set(dstPos, mArray, srcPos, length);
	}
	
	public void set(int offset, T[] arr, int srcOffset, int length) throws IndexOutOfBoundsException {
		if (offset < 0) throw new IllegalArgumentException("illegal negative offset: " + offset);
		if (srcOffset < 0) throw new IllegalArgumentException("illegal negative src offset: " + srcOffset);
		if (length < 0) throw new IllegalArgumentException("illegal negative length: " + length);
		if ((srcOffset + length) > arr.length) {
			throw new IndexOutOfBoundsException(
					"not enough source entries: " + 
					srcOffset + " + " + length + " > " + arr.length
			);
		}
		ensureCapacity(offset + length);
		System.arraycopy(arr, srcOffset, mArray, offset, length);
		mLength = Math.max(offset + length, mLength);
	}

	public void removeLast() {
		removeLast(1);
	}
	public void removeLast(int len) {
		if (mLength - len < 0) {
			throw new IndexOutOfBoundsException(
					"cannot remove last " + len + 
					" entries, there are only " + mLength					
			);
		}
		mLength -= len;
		for (int ii = 0; ii < len; ii++) {
			mArray[mLength + ii] = null;
		}
	}
	@Override
	public T remove(int index) {
		if (index == length() - 1) {
			T last = last();
			removeLast();
			return last;
		}
		return super.remove(index);
	}

	public void clear() {
		mArray = newArray(Math.max(mArray.length / 2, DEFAULT_CAPACITY));
		mLength = 0;
	}

	@Override
	public T get(int index) throws IndexOutOfBoundsException {
		if (/* index < 0 ||*/ index >= mLength) {
			//index < 0 is thrown anyway by array access below
			throw new IndexOutOfBoundsException(
				"index " + index + " not in[0, " + (mLength - 1) + "]"
			); 
		}
		return mArray[index];
	}
	
	@Override
	public T first() throws IndexOutOfBoundsException {
		if (mLength > 0) {
			return mArray[0];
		}
		throw new IndexOutOfBoundsException(
			"empty array"
		); 
	}
	
	@Override
	public T last() throws IndexOutOfBoundsException {
		if (mLength > 0) {
			return mArray[mLength - 1];
		}
		throw new IndexOutOfBoundsException(
			"empty array"
		); 
	}

	@Override
	public T set(int index, T element) {
		T old;
		if (index < mArray.length) {
			old = mArray[index];
		}
		else {
			if (element == null) return null;
			old = null;			
			ensureCapacity(index + 1);
		}
		mArray[index] = element;
		if (index >= mLength) {
			mLength = index + 1;			
		}
		return old;
	}
	@SuppressWarnings("unchecked")
	public void set(int offset, Iterable<? extends T> it) {
		if (it instanceof GenericArray) {
			copyFrom((GenericArray<T>)it, 0, offset, ((GenericArray<T>)it).length());
		}
		else {
			for (T el : it) {
				set(offset++, el);
			}
            mLength = Math.max(offset, mLength);
		}
	}
	
	protected void ensureCapacity(int capacity) {
		if (capacity >= mArray.length) {
			int newSize = Math.max(mArray.length * 2, capacity);
			T[] newArr = newArray(newSize);
			System.arraycopy(mArray, 0, newArr, 0, mLength);
			mArray = newArr;
		}		
	}
	
	@Override
	public void swap(int indexA, int indexB) {
		if (Math.min(indexA, indexB) < 0 || Math.max(indexA, indexB) >= mLength) {
			throw new IndexOutOfBoundsException(
				"index " + indexA + " or " + indexB + 
				" out of bounds [0, " + (mLength - 1) + "]"				
			);
		}
		T tmp = mArray[indexA];
		mArray[indexA] = mArray[indexB];
		mArray[indexB] = tmp;
	}
	
	public List<T> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex > length() || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException(
				"[" + fromIndex + ", " + toIndex + "] out of bounds [0, " +
				length() + "] or fromIndex > toIndex"
			);
		}
		T[] arr = newArray(toIndex - fromIndex);
		System.arraycopy(mArray, fromIndex, arr, 0, toIndex - fromIndex);
		return new GenericFixSizeArray<T>(arr);
	}
	
	@Override
	public GenericDynamicArray<T> clone() {
		GenericDynamicArray<T> clone = new GenericDynamicArray<T>(0);
		clone.mArray	= mArray.clone();
		clone.mLength	= mLength;
		return clone;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int ii = 0; ii < mLength; ii++) {
			if (ii > 0) sb.append(", ");
			sb.append(mArray[ii]);
		}
		sb.append(']');
		return sb.toString();
	}
	
}
