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
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Implementation of IntSet, also implementing SortedSet, for an integer range.
 * All values in this range are members of this set, the set is unmodifiable.
 */
public class RangeIntSet extends AbstractSortedIntSet implements Serializable {

	private static final long serialVersionUID = 7872346283483103840L;
	
	private final int start;//inclusive
	private final int end;//exclusive
	
	/**
	 * Constructor for <code>RangeIntSet</code> start index (inclusive) and
	 * end index (exclusive).
	 * 
	 * @param start	the first index in the range (inclusive)
	 * @param end	the first index out of the range (exclusive)
	 */
	public RangeIntSet(int start, int end) {
		if (end < start) {
			throw new IllegalArgumentException("start must be greater or equal to end: " + end + " not >= " + start);
		}
		this.start	= start;
		this.end	= end;
	}

	@Override
	public int size() {
		return end - start;
	}

	@Override
	public boolean isEmpty() {
		return end == start;
	}

	@Override
	public boolean containsInt(int o) {
		return o >= start && o < end;
	}

	@Override
	public IntIterator iterator() {
		return new AbstractIntIterator() {
			int next = start;
			public boolean hasNext() {
				return next < end;
			}
			public int nextInt() {
				if (next >= end) throw new NoSuchElementException();
				final int cur = next;
				next++;
				return cur;
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException("unmodifiable set");
			}
		};
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
		int index = 0;
		for (int i = start; i < end; i++) {
			arr[index++] = (T)Integer.valueOf(i); 
		}
		return arr;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("unmodifiable set");
	}

	@Override
	public boolean addInt(int value) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	@Override
	public boolean removeInt(int value) {
		throw new UnsupportedOperationException("unmodifiable set");
	}

	@Override
	protected int[] toIntArrayInternal(int[] arr, boolean checkSize) {
		if (checkSize) {
			int size = size();
			if (arr.length < size) {
				arr = new int[size];
			}			
		}
		int index = 0;
		for (int i = start; i < end; i++) {
			arr[index++] = i; 
		}
		return arr;
	}

	/** @return always null*/
	public Comparator<? super Integer> comparator() {
		return null;
	}
	
	@Override
	public int firstInt() {
		if (start == end) {
			throw new NoSuchElementException();
		}
		return start;
	}
	@Override
	public int lastInt() {
		if (start == end) {
			throw new NoSuchElementException();
		}
		return end - 1;
	}
	public RangeIntSet subSet(int from, int to) {
		if (from > to) {
			throw new IllegalArgumentException("to must be greater or equal to from: " + to + " not >= " + from);
		}
		final int newStart 	= start + from;
		final int newEnd	= start + to;
		if (!containsInt(newStart)) {
			throw new IndexOutOfBoundsException("from index out of range: " + from);
		}
		if (!containsInt(newEnd)) {
			throw new IndexOutOfBoundsException("to index out of range: " + to);
		}
		return new RangeIntSet(newStart, newEnd);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RangeIntSet) {
			final RangeIntSet rs = (RangeIntSet)obj;
			return start == rs.start && end == rs.end;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return start ^ end;
	}
	
	@Override
	public String toString() {
		return "[" + start + ".." + (end - 1) + "]";
	}

}
