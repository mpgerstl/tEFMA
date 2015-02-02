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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.javasoft.jbase.Table;
import ch.javasoft.jbase.util.UnsupportedOperationException;

/**
 * The <code>MultiplexedAppendTable</code> uses a queue with limited or 
 * unlimited capacity to cache appended entities. A separate thread copies the
 * added entities from the queue to the underlying base table. Multiple threads
 * can concurrently add entities to this table. 
 * <p>
 * Note that all method except for {@link #add(Object)}, {@link #flush()}
 * and {@link #close(boolean)} throw an {@link UnsupportedOperationException}.
 */
public class MultiplexedAppendTable<E> implements Table<E> {
	
	private final Table<E>			baseTable;
	private final BlockingQueue<E>	queue;
	private final CopyThread		copyThread;
	
	private volatile boolean closed = false;
	
	/**
	 * Constructor with cache capacity of {@link Integer#MAX_VALUE}.
	 * 
	 * @param baseTable	the base table to which the entries are written from
	 * 					the cache
	 */
	public MultiplexedAppendTable(Table<E> baseTable) {
		this(baseTable, -1);
	}
	
	/**
	 * Constructor with specified cache capacity
	 * 
	 * @param baseTable	the base table to which the entries are written from
	 * 					the cache
	 */
	public MultiplexedAppendTable(Table<E> baseTable, int cacheSize) {
		this.baseTable	= baseTable;
		this.queue		= cacheSize <= 0 ? new LinkedBlockingQueue<E>() : new LinkedBlockingQueue<E>(cacheSize);
		this.copyThread = new CopyThread();
		copyThread.start();
	}
	
	/**
	 * The copy thread which is responsible for taking entries from the cache
	 * and adding them to the underlying base table.
	 */
	private final class CopyThread extends Thread {
		private volatile boolean flush = false;
		@Override
		public void run() {
			try {
				while (!closed) {
					final E next = queue.take();
					baseTable.add(next);
					if (flush) {
						E entity = queue.poll();
						while (entity != null) {
							baseTable.add(entity);
							entity = queue.poll();
						}
						baseTable.flush();
						notifyAll();
						flush = false;
					}
				}
				E entity = queue.poll();
				while (entity != null) {
					baseTable.add(entity);
					entity = queue.poll();
				}
				baseTable.flush();
			}
			catch (Exception e) {
				closed = true;
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * Adds an entry, initially to the cache. This method might block if the
	 * cache size is limited until the copy thread takes some entries from
	 * the cache and writes them to the underlying base table.
	 * <p>
	 * Note that this method always returns -1, since we do not know where the
	 * new entry has been put. If the table is already closed, an exception
	 * is thrown.
	 * 
	 * @param 	entity	the entity to add
	 * @return	always <tt>-1</tt> since the final position of the entry is not
	 * 			known
	 * @throws IOException	if an i/o exception occurs, or if the table has 
	 * 						already been closed
	 */
	public int add(E entity) throws IOException {
		if (closed) throw new IOException("table closed");
		try {
			queue.put(entity);
			return -1;
		} 
		catch (InterruptedException e) {
//			throw IOException(e);//only jdk 1.6++
			final IOException ex = new IOException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}
	/**
	 * Flushes the table. Blocks until at least one flush call has been made to
	 * the underlying table. Note, however, that other threads might still be
	 * adding entries concurrently, causing the caller to wait for an undefined
	 * time.
	 * <p>
	 * This method guarantees that the queue was empty once between the time 
	 * this method was called, and the call returns. This also guarantees that
	 * entries which have been added by the calling thread are flushed to the
	 * underlying base table. However, it is important to know that if 
	 * concurrent threads are constantly adding entries, this method might not
	 * return and leave the caller in a blocked state.
	 */
	public void flush() throws IOException {
		if (closed) return;
		baseTable.flush();
	}
	/**
	 * Closes the table. Subsequent add calls will cause an exception. The 
	 * method waits for the copying thread, that is, until all cached entries
	 * are flushed to the underlying base table. The call returns when the 
	 * cached entries have been written to the base table, and the base table is 
	 * has also been closed.
	 */
	public void close(boolean erase) throws IOException {
		if (closed) return;
		closed = true;
		try {
			copyThread.join();
		}
		catch (InterruptedException e) {
//			throw new IOException(e);//only jdk 1.6++
			final IOException ex = new IOException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
		baseTable.close(erase);
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 */
	public E get(int index) throws IOException {
		throw new UnsupportedOperationException("unsupported by multiplexed append table");    							
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 */
	public void removeAll() throws IOException {
		throw new UnsupportedOperationException("unsupported by multiplexed append table");    							
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 */
	public void remove(int index) throws IOException {
		throw new UnsupportedOperationException("unsupported by multiplexed append table");    							
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 */
	public void set(int index, E entity) throws IOException {
		throw new UnsupportedOperationException("unsupported by multiplexed append table");    							
	}
	
	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 */
	public void swap(int indexA, int indexB) throws IOException {
		throw new UnsupportedOperationException("unsupported by multiplexed append table");    							
	}

	/**
	 * Always throws an {@link UnsupportedOperationException}
	 * @throws UnsupportedOperationException always
	 */
	public int size() throws IOException {
		throw new UnsupportedOperationException("unsupported by multiplexed append table");    							
	}

}
