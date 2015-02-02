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
import java.util.BitSet;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * Implementation of IntSet, also implementing SortedSet, but with 
 * support limited to positive integers.
 */
public class BitSetIntSet extends AbstractSortedIntSet implements Cloneable, Serializable {

	private static final long serialVersionUID = 1418206509682222060L;

	private BitSet set;
	
	public BitSetIntSet() {
		set = new BitSet();
	}
	public BitSetIntSet(IntSet set) {
		this();
		addAll(set);
	}
	public BitSetIntSet(BitSet bitSet) {
		set = (BitSet)bitSet.clone();
	}
	public BitSetIntSet(int[] values) {
		this();
		for (int val : values) addInt(values[val]);
	}

	public BitSet toBitSet() {
		return (BitSet)set.clone();
	}

	@Override
	public int size() {
		return set.cardinality();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean containsInt(int o) {
		return set.get(o);
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
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
			arr[index++] = (T)Integer.valueOf(i); 
		}
		return arr;
	}

	@Override
	public void clear() {
		set.clear();
	}

	@Override
	public boolean addInt(int value) {
		if (set.get(value)) return true;
		set.set(value);
		mod++;
		return false;
	}
	
	@Override
	public boolean removeInt(int value) {
		if (set.get(value)) {
			set.clear(value);
			mod++;
			return true;
		}
		return false;
	}

	@Override
	public IntIterator iterator() {
		return new AbstractIntIterator() {
			int ref		= mod;
			int next	= set.nextSetBit(0);
			int rem		= -1;
			public boolean hasNext() {
				return next >= 0;
			}
			public int nextInt() {
				if (next < 0) throw new NoSuchElementException();
				checkMod();
				int cur	= next;
				next	= set.nextSetBit(cur + 1);
				rem		= cur;
				return cur;
			}
			@Override
			public void remove() {
				if (rem == -1) {
					throw new IllegalStateException(
						"next has not yet been called, or remove has already been performed."
					);
				}
				BitSetIntSet.this.removeInt(rem);
				ref = mod;
				rem = -1;
			}
			private void checkMod() {
				if (mod != ref) throw new ConcurrentModificationException();
			}
		};
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
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
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
		int val = set.nextSetBit(0);
		if (val < 0) throw new NoSuchElementException();
		return val;
	}
	@Override
	public int lastInt() {
		int val = set.length() - 1;
		if (val < 0) throw new NoSuchElementException();
		return val;
	}
	public BitSetIntSet subSet(int fromElement, int toElement) {
		if (fromElement > toElement) {
			throw new IllegalArgumentException("fromElement > toElement: " + fromElement + " > " + toElement);
		}
		return new BitSetIntSet(set.get(fromElement, toElement));
	}
	
	@Override
	public BitSetIntSet clone() {
		final BitSetIntSet clone = (BitSetIntSet)super.clone();
		clone.set = (BitSet)set.clone();
		clone.mod = 0;
		return clone;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BitSetIntSet) {
			return set.equals(((BitSetIntSet)obj).set);
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(set.toString());
		sb.setCharAt(0, '[');
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

}
