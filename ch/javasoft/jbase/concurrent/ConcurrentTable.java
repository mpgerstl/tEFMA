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
package ch.javasoft.jbase.concurrent;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.javasoft.jbase.Table;

/**
 * The <code>ConcurrentTable</code> creates a thread-safe table for concurrent 
 * use. Concurrent tables allow multiple concurrent readers, writing requires 
 * exclusive table access. The access control is handled with a
 * {@link ReentrantReadWriteLock read/write lock}.
 * <p>
 * A read copy is created for every thread when the table is accessed for 
 * reading the first time. If it is accessed for writing, exclusive access to 
 * the main table is required. If the table is closed, only the read copy will 
 * be closed, unless the owner of the table (mostly the thread which created 
 * this instance) closes it. If the owner closes the table, every subsequent 
 * table access will cause an exception, also for read access to read copy 
 * tables. Multiple close calls do not cause an exception.  
 */
public class ConcurrentTable<E> implements Table<E> {

	private final ReadWriteLock    rwLock;
	private final Table<E> 	       table;
	
	private final Thread owner;
	
	private final ThreadLocal<Table<E>> readTable = new ThreadLocal<Table<E>>();
	
	/**
	 * Creates a table owned by the current thread. The owner is the thread 
	 * which definitely closes this table. All other threads close only their
	 * read copies of the table. After closing the table by the owner, all other
	 * tables are also closed and subsequent accesses will fail.
	 */
	public <T extends Table<E> & Stateful> ConcurrentTable(T table) throws IOException {
		this(table, Thread.currentThread());
	}
	/**
	 * Creates a table owned by the specified thread. The owner is the thread 
	 * which definitely closes this table. All other threads close only their
	 * read copies of the table. After closing the table by the owner, all other
	 * tables are also closed and subsequent accesses will fail.
	 */
	public <T extends Table<E> & Stateful> ConcurrentTable(T table, Thread owner) throws IOException {
		this.rwLock	= new ReentrantReadWriteLock(true /*fair*/); 
		this.table 	= table;
		this.owner 	= owner;
	}
	
	/**
	 * Returns the read/write lock which controls access to read and write
	 * methods of this table
	 */
	protected ReadWriteLock getReadWriteLock() {
		return rwLock;
	}
	
	@SuppressWarnings("unchecked")
    private Table<E> createReadCopyTable() throws IOException {
	    return (Table<E>) ((Stateful)table).createReadCopy(rwLock);	    
	}
	//do not call this method with held locks!
	protected Table<E> getReadTable(boolean forceCreate) throws IOException {
		Table<E> readTbl = readTable.get();
		if (readTbl == null && forceCreate) {
			final Lock lock = rwLock.writeLock();
			lock.lock();
			try {
				table.flush();
				readTbl = createReadCopyTable();
				readTable.set(readTbl);
			} 
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			finally {
				lock.unlock();
			}
		}
		return readTbl;
	}

	protected boolean isOwner() {
		return owner == Thread.currentThread();
	}
	public int add(E entity) throws IOException {
		final Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			return table.add(entity);
		}
		finally {
			lock.unlock();
		}
	}

	public void close(boolean erase) throws IOException {
		if (erase && !isOwner()) {
			throw new IOException("erase is only possible for owners of this table");
		}
		final Table<E> readTable = getReadTable(false /*forceCreate*/);
		if (readTable != null) {
			final Lock lock = rwLock.readLock();
			lock.lock();
			try {
				readTable.close(false);
			}
			finally {
				lock.unlock();
			}			
		}
		if (isOwner()) {
			final Lock lock = rwLock.writeLock();
			lock.lock();
			try {
				table.close(erase);
			}
			finally {
				lock.unlock();
			}
		}
	}

	public void flush() throws IOException {
		final Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			table.flush();
		}
		finally {
			lock.unlock();
		}
	}

	public E get(int index) throws IOException {
		final Table<E> readTable = getReadTable(true /*forceCreate*/);
		final Lock lock = rwLock.readLock();
		lock.lock();
		try {
			return readTable.get(index);
		}
		finally {
			lock.unlock();
		}
	}

	public void removeAll() throws IOException {
		final Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			table.removeAll();
		}
		finally {
			lock.unlock();
		}
	}

	public void remove(int index) throws IOException {
		final Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			table.remove(index);
		}
		finally {
			lock.unlock();
		}
	}

	public void set(int index, E entity) throws IOException {
		final Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			table.set(index, entity);
		}
		finally {
			lock.unlock();
		}
	}

	public void swap(int indexA, int indexB) throws IOException {
		final Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			table.swap(indexA, indexB);
		}
		finally {
			lock.unlock();
		}
	}
	
	public int size() throws IOException {
		final Table<E> readTable = getReadTable(true /*forceCreate*/);
		final Lock lock = rwLock.readLock();
		lock.lock();
		try {
			return readTable.size();
		}
		finally {
			lock.unlock();
		}
	}

}
