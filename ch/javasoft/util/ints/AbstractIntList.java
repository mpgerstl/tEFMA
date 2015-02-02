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

import java.util.Collection;

abstract public class AbstractIntList extends AbstractIntCollection implements IntList, Cloneable {
	
	public AbstractIntList() {
		super();
	}

	public AbstractIntList(int[] values) {
		super(values);
	}

	public AbstractIntList(IntCollection set) {
		super(set);
	}

	@Override
	public boolean addInt(int value) {
		return addInt(size(), value);
	}
	public void add(int index, Integer element) {
		addInt(index, element.intValue());
	}
	
	public boolean addAll(int index, Collection<? extends Integer> c) {
		boolean any = true;
		for (Integer val : c) any |= addInt(index, val.intValue());
		return any;
	}

	public Integer get(int index) {
		return Integer.valueOf(getInt(index));
	}

	public int indexOfInt(int value) {
		for (int i = 0; i < size(); i++) {
			if (value == getInt(i)) return i;
		}
		return -1;
	}
	public int indexOf(Object o) {
		if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return indexOfInt(((Number)o).intValue());
		}
		return -1;
	}

	public int lastIndexOfInt(int value) {
		for (int i = size() - 1; i >= 0; i--) {
			if (value == getInt(i)) return i;
		}
		return -1;
	}
	public int lastIndexOf(Object o) {
		if (o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return lastIndexOfInt(((Number)o).intValue());
		}
		return -1;
	}
	
	@Override
	public boolean containsInt(int value) {
		return indexOfInt(value) != -1;
	}
	
	public Integer remove(int index) {
		return Integer.valueOf(removeIntAt(index));
	}
	@Override
	public boolean removeInt(int value) {
		int index = indexOfInt(value);
		if (index != -1) {
			removeIntAt(index);
			return true;
		}
		return false;
	}

	public IntListIterator listIterator() {
		return listIterator(0);
	}
	
	@Override
	public IntIterator iterator() {
		return listIterator();
	}
	
	public Integer set(int index, Integer element) {
		return Integer.valueOf(setInt(index, element.intValue()));
	}

}
