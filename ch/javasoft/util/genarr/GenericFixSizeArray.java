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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenericFixSizeArray<T> extends AbstractGenericArray<T> implements CloneableTyped<GenericFixSizeArray<T>> {
	
	@SuppressWarnings("unchecked")
	public static <T> GenericFixSizeArray empty() {
		return new GenericFixSizeArray<T>((T[])EMPTY);
	}
	
	private final T[] mArray;

	public GenericFixSizeArray() {
		this(0);
	}
	
	@SuppressWarnings("unchecked")
	public GenericFixSizeArray(int length) {
		mArray = length == 0 ? (T[])EMPTY : newArray(length);
	}
	
	public GenericFixSizeArray(T... arr) {
		mArray = arr.clone();
	}
	public GenericFixSizeArray(GenericFixSizeArray<? extends T> fixSizeArray) {
		mArray = fixSizeArray.mArray.clone();	
	}
	public GenericFixSizeArray(GenericArray<? extends T> genericArray) {
		mArray = newArray(genericArray.length());
		genericArray.toArray(mArray);
	}
	public GenericFixSizeArray(Iterable<? extends T> iterable) {
		int size;
		if (iterable instanceof List) {
			size = ((List<? extends T>)iterable).size();
		}
		else {
			size = 0;
			for (@SuppressWarnings("unused") T el : iterable) {
				size++;
			}
		}
		mArray = newArray(size);
		int index = 0;
		for (T el : iterable) {
			mArray[index++] = el;
		}
	}
	
	@Override
	public int length() {
		return mArray.length;
	}
	@Override
	public boolean isEmpty() {
		return mArray.length == 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <TT> TT[] toArray(TT[] a) {
		int size = length();
        if (a.length < size) {
            a = (TT[])java.lang.reflect.Array.newInstance(
            		a.getClass().getComponentType(), size
            	);
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
			copyFrom((GenericFixSizeArray<T>)src, srcPos, dstPos, length);
		}
		else {
			src.copyTo(this, srcPos, dstPos, length);
		}
	}
	@SuppressWarnings("unchecked")
	public void copyTo(GenericArray<? super T> dst, int srcPos, int dstPos, int length) {
		if (dst instanceof GenericDynamicArray) {
			copyTo((GenericFixSizeArray<T>)dst, srcPos, dstPos, length);
		}
		else {
			dst.set(dstPos, mArray, srcPos, length);
		}
	}	

	public void copyFrom(GenericFixSizeArray<? extends T> src, int srcPos, int dstPos, int length) {
		set(dstPos, src.mArray, srcPos, length);
	}
	public void copyTo(GenericFixSizeArray<? super T> dst, int srcPos, int dstPos, int length) {
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
		if ((offset + length) > mArray.length) {
			throw new IndexOutOfBoundsException(
					"not enough destination space: " + 
					offset + " + " + length + " > " + mArray.length
			);
		}
		System.arraycopy(arr, srcOffset, mArray, offset, length);
	}

	@SuppressWarnings("unchecked")
	public void set(int offset, Iterable<? extends T> iterable) throws IndexOutOfBoundsException {
		if (iterable instanceof GenericArray) {
			copyFrom((GenericArray<T>)iterable, 0, offset, ((GenericArray<T>)iterable).length());
		}
		else {
			List<T> list;
			if (iterable instanceof List) {
				list = (List<T>)iterable;
			}
			else {
				list = new ArrayList<T>();
				for (T el : iterable) {
					list.add(el);
				}
			}
			if (mArray.length < offset + list.size()) {
				throw new IndexOutOfBoundsException(
						"not enough target space, " + (offset + list.size()) + " > " + mArray.length
				);
			}
			Iterator<T> it = list.iterator();
			for (int ii = 0; ii < mArray.length; ii++) {
				mArray[ii] = it.next();
			}			
		}
	}

	@Override
	public T get(int index) throws IndexOutOfBoundsException {
		return mArray[index];
	}

	@Override
	public T set(int index, T element) {
		T old = mArray[index];
		mArray[index] = element;
		return old;
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
	
	public void clear() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public GenericFixSizeArray<T> clone() {
		GenericFixSizeArray<T> clone = new GenericFixSizeArray<T>(mArray.length);
		clone.copyFrom(this, 0, 0, mArray.length);
		return clone;
	}

}
