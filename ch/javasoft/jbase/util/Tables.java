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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import ch.javasoft.jbase.MemoryTable;
import ch.javasoft.jbase.Table;
import ch.javasoft.jbase.concurrent.ConcurrentTable;

/**
 * The <tt>Tables</tt> class contains static helper methods to deal with
 * {@link Table tables}. It is comparable to the {@link Collections} or
 * {@link Arrays} class.
 */
public class Tables {
	
	public static final Table<?> EMPTY_TABLE = unmodifyableTable(new MemoryTable<Object>());

	/**
	 * Returns an empty (immutable) table
	 */
	@SuppressWarnings("unchecked")
	public static <E> Table<E> emptyTable() {
		return (Table<E>)EMPTY_TABLE;
	}
	
	/**
	 * Returns an unmodifyable table, i.e. all write operations of the returned
	 * table throw an {@link UnsupportedOperationException}
	 */
	public static <E> Table<E> unmodifyableTable(final Table<E> table) {
		return new Table<E>() {
			public int add(E entity) throws UnsupportedOperationException {
				throw new UnsupportedOperationException("unmodifyable table");
			}
			public void remove(int index) throws UnsupportedOperationException {
				throw new UnsupportedOperationException("unmodifyable table");
			}
			public void set(int index, E entity) throws UnsupportedOperationException {
				throw new UnsupportedOperationException("unmodifyable table");
			}
			public void swap(int indexA, int indexB) throws IOException {
				throw new UnsupportedOperationException("unmodifyable table");
			}
			public void removeAll() throws UnsupportedOperationException {
				throw new UnsupportedOperationException("unmodifyable table");
			}
			public E get(int index) throws IOException {
				return table.get(index);
			}
			public int size() throws IOException {
				return table.size();
			}
			public void flush() throws IOException {
				//don't do anything, nothing to flush
			}
			public void close(boolean erase) throws IOException {
				if (erase) {
					throw new UnsupportedOperationException("unmodifyable table");
				}
				table.close(false);
			}			
		};
	}
	
	/**
	 * Returns an synchronized, thread-safe table (All methods of the table are
	 * synchronized).
	 * <p>
	 * Note: consider using an instance of {@link ConcurrentTable} 
	 * instead, which is oftentimes more efficient.
	 */
	public static <E> Table<E> synchronizedTable(final Table<E> table) {
		return new Table<E>() {
			public int add(E entity) throws IOException {
				synchronized(table) {
					return table.add(entity);
				}
			}
			public void remove(int index) throws IOException {
				synchronized(table) {
					table.remove(index);
				}
			}
			public void set(int index, E entity) throws IOException {
				synchronized(table) {
					table.set(index, entity);
				}
			}
			public void swap(int indexA, int indexB) throws IOException {
				synchronized(table) {
					table.swap(indexA, indexB);
				}
			}
			public void removeAll() throws IOException {
				synchronized(table) {
					table.removeAll();
				}
			}
			public E get(int index) throws IOException {
				synchronized(table) {
					return table.get(index);
				}
			}
			public int size() throws IOException {
				synchronized(table) {
					return table.size();
				}
			}
			public void flush() throws IOException {
				synchronized(table) {
					table.flush();
				}
			}
			public void close(boolean erase) throws IOException {
				synchronized(table) {
					table.close(erase);
				}
			}			
		};
	}

	public static <E> Table<E> autoflushTable(final Table<E> table) {
		return new Table<E>() {
			public int add(E entity) throws IOException {
				final int pos = table.add(entity);
				table.flush();
				return pos;
			}
			public void remove(int index) throws IOException {
				table.remove(index);
				table.flush();
			}
			public void set(int index, E entity) throws IOException {
				table.set(index, entity);
				table.flush();
			}
			public void swap(int indexA, int indexB) throws IOException {
				table.swap(indexA, indexB);
				table.flush();
			}
			public void removeAll() throws IOException {
				table.removeAll();
				table.flush();
			}
			public E get(int index) throws IOException {
				return table.get(index);
			}
			public int size() throws IOException {
				return table.size();
			}
			public void flush() throws IOException {
				table.flush();
			}
			public void close(boolean erase) throws IOException {
				table.close(erase);
			}			
		};
	}
	
	public static <E> List<E> asList(Table<E> table) {
		return new TableList<E>(table);
	}
	
	public static Object[] asArray(Table<?> table) {
		return asList(table).toArray();
	}
	public static <A, E extends A> A[] asArray(Table<E> table, A[] array) {
		return asList(table).toArray(array);
	}
	
	public static <E> Enumeration<E> enumeration(final Table<E> table) {
		return new Enumeration<E>() {
			int current = 0;
			public boolean hasMoreElements() {
				try {
					return current < table.size();					
				}
				catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
			public E nextElement() {
				try {
					if (current < table.size()) {
						final E e = table.get(current);
						current++;
						return e;
					}
					throw new NoSuchElementException();
				}
				catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}
	
	public static <E> void fill(Table<E> table, E value) throws IOException {
		final int size = table.size();
		table.removeAll();
		for (int i = 0; i < size; i++) {
			table.add(value);			
		}
	}
	
	public static <E> void addAll(Iterable<? extends E> src, Table<E> dst) throws IOException {
		for (final E e : src) dst.add(e);
	}
	public static <E> void addAll(Table<? extends E> src, Table<E> dst) throws IOException {
		for (int i = 0; i < src.size(); i++) {
			dst.add(src.get(i));
		}
	}
	
	// no instances
	private Tables() {}
}
