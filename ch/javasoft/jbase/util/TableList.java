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
package ch.javasoft.jbase.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import ch.javasoft.jbase.Table;

/**
 * A <code>TableList</code> is a {@link List} wrapping a {@link Table}. It 
 * serves as simple adapter class from tables to Java's collections framework.
 * 
 * @param <E>	The entity (or entry) type of this list and the nested table
 */
public class TableList<E> extends AbstractList<E> implements RandomAccess {
	
	private final Table<E> table;
	
	/**
	 * ConstConstructor with table to be wrapped
	 */
	public TableList(Table<E> table) {
		this.table = table;
	}
	
	@Override
	public boolean add(E e) {
		try {
			table.add(e);
			return true;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	@Override
	public void add(int index, E element) {
		try {
			if (index < 0) {
				throw new IndexOutOfBoundsException("index " + index + 
						" < 0");
			}
			if (index > table.size()) {
				throw new IndexOutOfBoundsException("index " + index + 
						" > table size " + table.size());
			}
			if (index == table.size()) {
				add(element);
			}
			else {
				for (int i = table.size() - 1; i >= index; i--) {
					final E e = table.get(i);
					if (i == table.size()) table.add(e);
					else table.set(i + 1, e);
				}
				table.set(index, element);
			}					
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		for (final E e: c) add(e);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		try {
			if (index < 0) {
				throw new IndexOutOfBoundsException("index " + index + 
						" < 0");
			}
			if (index > table.size()) {
				throw new IndexOutOfBoundsException("index " + index + 
						" > table size " + table.size());
			}
			if (index == table.size()) {
				addAll(c);
			}
			else {
				final int tblSize = table.size();
				final int colSize = c.size();
				for (int i = 0; i < colSize; i++) {
					final E e = table.get(tblSize - colSize + i);
					table.add(e);
				}
				for (int i = tblSize - colSize; i >= index; i--) {
					final E e = table.get(i);
					table.set(i + colSize, e);
				}
				for (final E e : c) {
					table.set(index, e);
					index++;
				}
			}					
			return true;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void clear() {
		try {
			table.removeAll();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		final Collection<?> clone = new LinkedList<Object>(c);
		try {
			for (int i = 0; i < table.size(); i++) {
				final E e = table.get(i);
				final Iterator<?> it = clone.iterator();
				while (it.hasNext()) {
					if (e.equals(it.next())) {
						it.remove();
					}
				}
			}
			return clone.isEmpty();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public E get(int index) {
		try {
			if (index < 0) {
				throw new IndexOutOfBoundsException("index " + index + 
						" < 0");
			}
			if (index >= table.size()) {
				throw new IndexOutOfBoundsException("index " + index + 
						" >= table size " + table.size());
			}				
			return table.get(index);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public int indexOf(Object o) {
		try {
			for (int i = 0; i < table.size(); i++) {
				final E e = table.get(i);
				if (e.equals(o)) return i;
			}
			return -1;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		try {
			for (int i = table.size() - 1; i >= 0; i--) {
				final E e = table.get(i);
				if (e.equals(o)) return i;
			}
			return -1;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public boolean remove(Object o) {
		final int index = indexOf(o);
		if (index < 0) return false;
		remove(index);
		return true;
	}

	@Override
	public E remove(int index) {
		final E entity = get(index);
		remove(index);
		return entity;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean any = false;
		for (final Object e : c) {
			any |= remove(e);
		}
		return any;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		try {
			boolean modified = false;
			for (int i = 0; i < table.size(); i++) {
				final E e = table.get(i);
				boolean keep = false;
				for (final Object ec : c) {
					if (e.equals(ec)) {
						keep = true;
						break;
					}
				}
				if (!keep) {
					table.remove(i);
					modified = true;
				}
			}
			return modified;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public E set(int index, E element) {
		try {
			final E prev = table.get(index);
			table.set(index, element);
			return prev;
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public int size() {
		try {
			return table.size();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if (size() > a.length) {
			a = (T[])Array.newInstance(a.getClass().getComponentType(), size());
		}
		for (int i = 0; i < a.length; i++) {
			a[i] = (T)get(i);
		}
		return a;
	}			
}
