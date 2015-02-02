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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract pre-implementation of IntCollection.
 */
abstract public class AbstractIntCollection implements IntCollection {
	
	protected transient int mod;

	public AbstractIntCollection() {
		super();
	}
	public AbstractIntCollection(IntCollection set) {
		addAll(set);
	}
	public AbstractIntCollection(int[] values) {
		for (int val : values) addInt(val);
	}

	abstract public int size();
	abstract public void clear();
	abstract public boolean addInt(int value);
	abstract public boolean removeInt(int value);
	abstract public IntIterator iterator();

	
	public boolean isEmpty() {
		return size() == 0;
	}
	public boolean containsInt(int value) {
		IntIterator it = iterator();
		while (it.hasNext()) {
			if (value == it.nextInt()) return true;
		}
		return false;
	}
	public boolean contains(Object o) {
		if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return containsInt(((Number)o).intValue());
		}
		return false;
	}

	public Integer[] toArray() {
		return toArrayInternal(new Integer[size()], false);
	}

	public <T> T[] toArray(T[] arr) {
		return toArrayInternal(arr, true);
	}
	@SuppressWarnings("unchecked")
	protected <T> T[] toArrayInternal(T[] arr, boolean checkSize) {
		if (checkSize) {
			int size = size();
			if (arr.length < size) {
				arr = (T[])Array.newInstance(arr.getClass().getComponentType(), size);
			}			
		}
		int index = 0;
		IntIterator it = iterator();
		while (it.hasNext()) {
			arr[index++] = (T)Integer.valueOf(it.nextInt()); 
		}
		return arr;
	}

	public boolean add(Integer o) {
		return addInt(o.intValue());
	}

	public boolean remove(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return removeInt(((Number)o).intValue());
		}
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		for (Object obj : c) {
			if (!contains((obj))) return false;
		}
		return true;
	}

	public boolean addAll(Collection<? extends Integer> c) {
		if (c instanceof IntCollection) {
			return addAll((IntCollection)c);
		}
		boolean any = false;
		for (Integer i : c) {
			any |= add(i);
		}
		return any;
	}

	public boolean retainAll(Collection<?> c) {
		boolean any = false;
		Iterator it = iterator();
		while (it.hasNext()) {
			if (!c.contains(it.next())) {
				it.remove();
				any |= true;
			}
		}
		return any;
	}

	public boolean removeAll(Collection<?> c) {
		boolean any = false;
		for (Object obj : c) {
			any |= remove(obj);
		}
		return any;
	}

	public boolean addAll(IntCollection coll) {
		boolean any = false;
		final IntIterator it = coll.iterator();
		while (it.hasNext()) {
			any |= addInt(it.nextInt());
		}
		return any;
	}
	
	public boolean addAll(int... values) {
		if (values.length == 0) return false;
		boolean any = false;
		for (int i = 0; i < values.length; i++) {
			any |= addInt(values[i]);
		}
		return any;
	}

	public int[] toIntArray() {
		return toIntArrayInternal(new int[size()], false);
	}

	public int[] toIntArray(int[] arr) {
		return toIntArrayInternal(arr, true);
	}
	protected int[] toIntArrayInternal(int[] arr, boolean checkSize) {
		if (checkSize) {
			int size = size();
			if (arr.length < size) {
				arr = new int[size];
			}			
		}
		int index = 0;
		IntIterator it = iterator();
		while (it.hasNext()) {
			arr[index++] = it.nextInt(); 
		}
		return arr;
	}

	public int firstInt() {
		return iterator().nextInt();
	}
	public Integer first() {
		return Integer.valueOf(firstInt());
	}
	public int lastInt() {
		IntIterator it = iterator();
		if (!it.hasNext()) throw new NoSuchElementException();
		int val = it.nextInt();
		while (it.hasNext()) val = it.nextInt();
		return val;
	}
	public Integer last() {
		return Integer.valueOf(lastInt());
	}
	@Override
	public AbstractIntCollection clone() {
		final AbstractIntCollection clone;
		try {
			clone = (AbstractIntCollection)super.clone();
		}
		catch (CloneNotSupportedException ex) {
			//should not happen since we are cloneable
        	throw new RuntimeException(ex);
		}
		clone.mod = 0;
		return clone;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj instanceof Collection) {
			final Collection col = (Collection)obj;
			if (size() != col.size()) return false;			
			final IntIterator thisIt = iterator();
			final Iterator otherIt = col.iterator();
			while (thisIt.hasNext()) {
				final int val = thisIt.nextInt();
				if (otherIt instanceof IntIterator) {
					if (val != ((IntIterator)otherIt).nextInt()) return false;
				}
				else {
					final Object oval = otherIt.next();
					if (oval == null) return false;
					if (!(oval instanceof Integer)) return false;
					if (((Integer)oval).intValue() != val) return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		IntIterator it = iterator();
		while (it.hasNext()) hash ^= it.nextInt();
		return hash;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		IntIterator it = iterator();
		while (it.hasNext()) {
			sb.append(sb.length() > 0 ? ", " : "[");
			sb.append(it.nextInt());
		}
		return sb.append("]").toString();
	}

}
