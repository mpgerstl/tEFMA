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
package ch.javasoft.metabolic.efm.dist.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.MemoryAccessor;

/**
 * Client server memory fetches the requested columns from the server. Note that
 * only {@link #getColumn(int)}, {@link #getColumnCount()} and 
 * {@link #close(boolean)} are supported, all other exception throw an 
 * {@link IOException} or an {@link UnsupportedOperationException}.
 */
public class ClientServerMemory<Col extends Column> implements SortableMemory<Col> {

	private static final int CACHE_SIZE = 4048;
	private static class Entry<C extends Column> {
		public Entry(int index, C column) {
			this.index	= index;
			this.column	= column;
		}
		private final int index;
		private final C   column;
	}
	private final AtomicReferenceArray<Entry<Col>> cache = new AtomicReferenceArray<Entry<Col>>(CACHE_SIZE);
	
	private final MemoryAccessor<Col> memoryAccessor;
	private final PartId part;

	public ClientServerMemory(MemoryAccessor<Col> memoryAccessor, PartId part) {
		this.memoryAccessor = memoryAccessor;
		this.part			= part;
	}
	/**
	 * Unsupported method, always throws an exception
	 * @throws IOException always, since this is an unsupported method
	 */
	public void flush() throws IOException {
		throw new IOException("unsupported method");
	}

	/**
	 * Unsupported method, always throws an exception
	 * @throws IOException always, since this is an unsupported method
	 */
	public void swapColumns(int indexA, int indexB) throws IOException {
		throw new IOException("unsupported method");
	}

	/**
	 * Fetches the specified column from the server and returns it.
	 */
	public Col getColumn(int index) throws IOException {
		final int pos = hash(index) % CACHE_SIZE;
		final Entry<Col> e = cache.get(pos);
		if (e != null && e.index == index) {
			return e.column;
		}
		final Entry<Col> n = new Entry<Col>(index, memoryAccessor.getColumn(part, index));
		cache.set(pos, n);
		return n.column;
	}

	/**
	 * Does actually nothing, that is, the call is silently ignored
	 */
	public void close(boolean erase) throws IOException {
		//ignore
	}

	/**
	 * Unsupported method, always throws an exception
	 * @throws IOException always, since this is an unsupported method
	 */
	public String fileId() throws IOException {
		throw new IOException("unsupported method");
	}

	/**
	 * Fetches the column count from the server and returns it.
	 */
	public int getColumnCount() throws IOException {
		return memoryAccessor.getColumnCount(part);
	}

	/**
	 * Unsupported method, always throws an exception
	 * @throws UnsupportedOperationException always, since this is an unsupported method
	 */
	public Iterator<Col> iterator() {
		throw new UnsupportedOperationException("unsupported method");
	}

	//copied from HashMap
    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index 0.
     */
    private static int hash(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
}
