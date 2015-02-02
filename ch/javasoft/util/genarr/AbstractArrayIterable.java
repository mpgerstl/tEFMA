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

import java.util.ListIterator;
import java.util.NoSuchElementException;

import ch.javasoft.util.Null;

abstract public class AbstractArrayIterable<T> implements ArrayIterable<T> {

	abstract public int length();

	abstract public T get(int index) throws IndexOutOfBoundsException;

	public T set(int index, T obj) throws IndexOutOfBoundsException, UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public boolean isEmpty() {
		return length() == 0;
	}

	public ListIterator<T> iterator() {
		return iterator(0, length());
	}

	public ListIterator<T> iterator(final int start, final int end) {
		return new ArrayListIterator(start, end);
	}

	public Iterable<T> iterable(final int start, final int end) {
		if (start < 0) {
			throw new IndexOutOfBoundsException("negative start index: " + start);
		}
		if (end < start) {
			throw new IndexOutOfBoundsException("end index before start: " + start + ">" + end);
		}
		if (end > length()) {
			throw new IndexOutOfBoundsException("end index after length: " + end + ">" + length());
		}
		final ArrayIterable<T> nested = this;
		return new AbstractArrayIterable<T>() {
			@Override
			public T get(int index) throws IndexOutOfBoundsException {
				return nested.get(index - start);
			}
			@Override
			public int length() {
				return end - start;
			}
		};
	}
	
	public Object[] toArray() {
		return toArray(Object.class);
	}
	@SuppressWarnings("unchecked")
	public <TT> TT[] toArray(TT[] arr) {
		int size = length();
        if (arr.length < size) {
            arr = (TT[])java.lang.reflect.Array.newInstance(arr.getClass().getComponentType(), size);        	
        }
        for (int ii = 0; ii < size; ii++) {
			arr[ii] = (TT)get(ii);
		}
        if (arr.length > size) {
            arr[size] = null;        	
        }
        return arr;
	}
	@SuppressWarnings("unchecked")
	public <TT> TT[] toArray(Class<TT> componentType) {
		int size = length();
        TT[] arr = (TT[])java.lang.reflect.Array.newInstance(componentType, size);        	
        for (int ii = 0; ii < size; ii++) {
			arr[ii] = (TT)get(ii);
		}
        return arr;
	}
	
	public class ArrayListIterator implements ListIterator<T> {
		
		protected int mIndex;
		protected final int mStart;
		protected final int mEnd;
		
		public ArrayListIterator(int start, int end) {
			if (start < 0 || end > length() || start > end) {
				throw new IndexOutOfBoundsException(
					"[" + start + ", " + end + "] out of bounds [0, " +
					length() + "] or fromIndex > toIndex"
				);
			}
			mStart	= start;
			mIndex	= start;
			mEnd	= end;
		}
		public boolean hasPrevious() {return mIndex > mStart;}
		public boolean hasNext() {return mIndex < mEnd;}
		public T next() {
			if (mIndex < mEnd) return get(mIndex++);
			throw new NoSuchElementException();
		}
		public T previous() {
			if (mIndex > mStart) return get(--mIndex);
			throw new NoSuchElementException();
		}
		public int nextIndex() {
			if (mIndex < mEnd) return mIndex;
			return AbstractArrayIterable.this.length();
		}
		public int previousIndex() {
			if (mIndex > mStart) return mIndex - 1;
			return -1;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		public void add(T o) {
			throw new UnsupportedOperationException();
		}
		public void set(T o) {
			throw new UnsupportedOperationException();				
		}		
	}
	
	public int indexOf(Object obj) {
		if (obj == null) obj = Null.INSTANCE;
		int len = length();
		for (int ii = 0; ii < len; ii++) {
			if (obj.equals(get(ii))) return ii;
		}
		return -1;
	}
	public int lastIndexOf(Object obj) {
		if (obj == null) obj = Null.INSTANCE;
		int len = length();
		for (int ii = len - 1; ii >= 0; ii--) {
			if (obj.equals(get(ii))) return ii;			
		}
		return -1;
	}
	
	
	@SuppressWarnings("unchecked")
	public GenericArray<T> toGenericArray(boolean forceNewInstance) {
		if (!forceNewInstance && this instanceof GenericArray) {
			return (GenericArray<T>)this;
		}
		return new GenericFixSizeArray<T>(this);
	}
	
	@Override
	public int hashCode() {
		int code = 0;
		for (int ii = 0; ii < length(); ii++) {
			code ^= get(ii).hashCode();
		}
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == getClass()) {
			ArrayIterable<?> other = (ArrayIterable)obj;
			if (length() != other.length()) return false;
			for (int ii = 0; ii < length(); ii++) {
				Object mine		= get(ii);
				Object others	= other.get(ii);
				if (mine == null) {
					if (others != null) return false;
				}
				if (!mine.equals(others)) return false;
			}
			return true;
		}
		return false;
	}
	
}
