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

import java.util.Collection;

abstract public class AbstractLongList extends AbstractLongCollection implements LongList, Cloneable {
	
	public AbstractLongList() {
		super();
	}

	public AbstractLongList(long[] values) {
		super(values);
	}

	public AbstractLongList(LongCollection set) {
		super(set);
	}

	public boolean addLong(long value) {
		return addLong(size(), value);
	}
	public void add(int index, Long element) {
		addLong(index, element.longValue());
	}
	
	public boolean addAll(int index, Collection<? extends Long> c) {
		boolean any = true;
		for (Long val : c) any |= addLong(index, val.longValue());
		return any;
	}

	public Long get(int index) {
		return Long.valueOf(getLong(index));
	}

	public int indexOfLong(long value) {
		for (int i = 0; i < size(); i++) {
			if (value == getLong(i)) return i;
		}
		return -1;
	}
	public int indexOf(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return indexOfLong(((Number)o).longValue());
		}
		return -1;
	}

	public int lastIndexOfLong(long value) {
		for (int i = size() - 1; i >= 0; i--) {
			if (value == getLong(i)) return i;
		}
		return -1;
	}
	public int lastIndexOf(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return lastIndexOfLong(((Number)o).intValue());
		}
		return -1;
	}
	
	@Override
	public boolean containsLong(long value) {
		return indexOfLong(value) != -1;
	}
	
	public Long remove(int index) {
		return Long.valueOf(removeLongAt(index));
	}
	public boolean removeLong(long value) {
		final int index = indexOfLong(value);
		if (index != -1) {
			removeLongAt(index);
			return true;
		}
		return false;
	}

	public LongListIterator listIterator() {
		return listIterator(0, size());
	}
	public LongListIterator listIterator(int index) {
		return listIterator(index, size());
	}
	
	public LongIterator iterator() {
		return listIterator();
	}
	
	public Long set(int index, Long element) {
		return Long.valueOf(setLong(index, element.longValue()));
	}

}
