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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class AbstractGenericArray<T> extends AbstractArrayIterable<T> implements GenericArray<T>, List<T> {
	
	protected static final Object[] EMPTY = new Object[] {};

	public AbstractGenericArray() {
		super();
	}
	
	public AbstractGenericArray(T[] array) {
		addAll(array);
	}
	
	public AbstractGenericArray(GenericArray<? extends T> genericArray) {
		addAll(genericArray);
	}
	
	public AbstractGenericArray(Iterable<T> iterable) {
		addAll(iterable);
	}
	
	@Override
	public ListIterator<T> iterator(int start, int end) {
		return new ArrayListIterator(start, end) {
			protected int mLast = -1;
			@Override
			public T next() {
				mLast = nextIndex();
				return super.next();
			}
			@Override
			public T previous() {
				mLast = previousIndex();
				return super.previous();
			}
			@Override
			public void set(T obj) {
				if (mLast >= mStart && mLast < mEnd) {
					AbstractGenericArray.this.set(mLast, obj);
					mLast = -1;
					return;
				}
				throw new NoSuchElementException();
			}
			@Override
			public void remove() {
				if (mLast >= mStart && mLast < mEnd) {
					AbstractGenericArray.this.remove(mLast);
					mLast = -1;
					return;
				}
				throw new NoSuchElementException();
			}
			@Override
			public void add(T obj) {
				AbstractGenericArray.this.set(mIndex, obj);//TODO right?
			}
		};
	}
	
	public void set(int offset, T[] arr) {
		set(offset, arr, 0, arr.length);
	}
	public void addAll(Iterable<? extends T> it) {
		set(length(), it);
	}
	
	public void addAll(T[] arr) {
		set(length(), arr);
	}
	
	public T first() throws IndexOutOfBoundsException {
		if (length() > 0) {
			return get(0);
		}
		throw new IndexOutOfBoundsException(
			"empty array"
		); 
	}
	
	public T last() throws IndexOutOfBoundsException {
		int len = length();
		if (len > 0) {
			return get(len - 1);
		}
		throw new IndexOutOfBoundsException(
			"empty array"
		); 
	}

	public void swap(int indexA, int indexB) {
		int len = length();
		if (Math.min(indexA, indexB) < 0 || Math.max(indexA, indexB) >= len) {
			throw new IndexOutOfBoundsException(
				"index " + indexA + " or " + indexB + 
				" out of bounds [0, " + (len - 1) + "]"				
			);
		}
		T tmp = ((GenericArray<T>)this).set(indexA, get(indexB));
		((GenericArray<T>)this).set(indexB, tmp);
	}
	
	public void add(int index, T element) {
		((GenericArray<T>)this).set(index, element);
	}
	
	public boolean add(T obj) {
		((GenericArray<T>)this).set(length(), obj);
		return true;
	}
	
	public boolean addAll(Collection<? extends T> coll) {
		addAll((Iterable<? extends T>)coll);
		return !coll.isEmpty();
	}
	
	public boolean addAll(int index, Collection<? extends T> coll) {
		set(index, coll);
		return !coll.isEmpty();
	}
	
	public boolean contains(Object obj) {
		return indexOf(obj) != -1;
	}
	
	public boolean containsAll(Collection<?> coll) {
		for (Object obj : coll) {
			if (!contains(obj)) return false;
		}
		return true;
	}
	
	public boolean remove(Object obj) {
		int index = indexOf(obj);
		if (index == -1) return false;
		remove(index);
		return true;
	}
	
	public T remove(int index) {
		throw new RuntimeException("not implemented yet");
	}
	
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("not implemented yet");
	}
	
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("not implemented yet");
	}
	
	public ListIterator<T> listIterator() {
		return iterator();
	}
	
	public ListIterator<T> listIterator(int index) {
		return iterator(index, length());
	}
	
	public int size() {
		return length();
	}

	@Override
	public String toString() {
		final int len = length();
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int ii = 0; ii < len; ii++) {
			if (ii > 0) sb.append(", ");
			sb.append(get(ii));
		}
		sb.append(']');
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	protected T[] newArray(int size) {
		return (T[])new Object[size];
	}
	
}
