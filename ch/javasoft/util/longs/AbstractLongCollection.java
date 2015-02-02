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
package ch.javasoft.util.longs;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.javasoft.util.ints.IntCollection;
import ch.javasoft.util.ints.IntIterator;

/**
 * Abstract pre-implementation of IntCollection.
 */
abstract public class AbstractLongCollection implements LongCollection {
	
	protected transient int mod;

	public AbstractLongCollection() {
		super();
	}
	public AbstractLongCollection(IntCollection set) {
		addAll(set);
	}
	public AbstractLongCollection(LongCollection set) {
		addAll(set);
	}
	public AbstractLongCollection(int[] values) {
		for (int val : values) addLong(val);
	}
	public AbstractLongCollection(long[] values) {
		for (long val : values) addLong(val);
	}

	public boolean isEmpty() {
		return size() == 0;
	}
	public boolean containsLong(long value) {
		final LongIterator it = iterator();
		while (it.hasNext()) {
			if (value == it.nextLong()) return true;
		}
		return false;
	}
	public boolean contains(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return containsLong(((Number)o).longValue());
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
		final LongIterator it = iterator();
		while (it.hasNext()) {
			arr[index++] = (T)Long.valueOf(it.nextLong()); 
		}
		return arr;
	}

	public boolean add(Long o) {
		return addLong(o.longValue());
	}

	public boolean remove(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return removeLong(((Number)o).longValue());
		}
		return false;
	}
	
	public boolean containsAll(Collection<?> c) {
		for (Object obj : c) {
			if (!contains((obj))) return false;
		}
		return true;
	}

	public boolean addAll(Collection<? extends Long> c) {
		if (c instanceof LongCollection) {
			return addAll((LongCollection)c);
		}
		boolean any = false;
		for (final Long i : c) {
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
				any = true;
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
			any |= addLong(it.nextInt());
		}
		return any;
	}
	public boolean addAll(LongCollection coll) {
		boolean any = false;
		final LongIterator it = coll.iterator();
		while (it.hasNext()) {
			any |= addLong(it.nextLong());
		}
		return any;
	}
	
	public boolean addAll(long... values) {
		if (values.length == 0) return false;
		boolean any = false;
		for (int i = 0; i < values.length; i++) {
			any |= addLong(values[i]);
		}
		return any;
	}

	public long[] toLongArray() {
		return toLongArrayInternal(new long[size()], false);
	}

	public long[] toLongArray(long[] arr) {
		return toLongArrayInternal(arr, true);
	}
	protected long[] toLongArrayInternal(long[] arr, boolean checkSize) {
		if (checkSize) {
			int size = size();
			if (arr.length < size) {
				arr = new long[size];
			}			
		}
		int index = 0;
		final LongIterator it = iterator();
		while (it.hasNext()) {
			arr[index++] = it.nextLong(); 
		}
		return arr;
	}

	public long firstLong() {
		return iterator().nextLong();
	}
	public Long first() {
		return Long.valueOf(firstLong());
	}
	public long lastLong() {
		final LongIterator it = iterator();
		if (!it.hasNext()) throw new NoSuchElementException();
		long val = it.nextLong();
		while (it.hasNext()) val = it.nextLong();
		return val;
	}
	public Long last() {
		return Long.valueOf(lastLong());
	}
	@Override
	public AbstractLongCollection clone() {
		final AbstractLongCollection clone;
		try {
			clone = (AbstractLongCollection)super.clone();
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
			final LongIterator thisIt = iterator();
			final Iterator otherIt = col.iterator();
			while (thisIt.hasNext()) {
				final long val = thisIt.nextLong();
				if (otherIt instanceof LongIterator) {
					if (val != ((LongIterator)otherIt).nextLong()) return false;
				}
				else {
					final Object oval = otherIt.next();
					if (oval == null) return false;
					if (!(oval instanceof Long)) return false;
					if (((Long)oval).longValue() != val) return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
		final LongIterator it = iterator();
		while (it.hasNext()) {
			final long val = it.nextLong();
			hash ^= (int)val;
			hash ^= (int)(val >>> 32);
		}
		return hash;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		final LongIterator it = iterator();
		while (it.hasNext()) {
			sb.append(sb.length() > 0 ? ", " : "[");
			sb.append(it.nextLong());
		}
		return sb.append("]").toString();
	}

}
