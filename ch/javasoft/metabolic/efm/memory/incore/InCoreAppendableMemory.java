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
package ch.javasoft.metabolic.efm.memory.incore;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <code>InCoreAppendableMemory</code> is a thread-safe memory for 
 * appending only. Data storage is backed by a {@link ConcurrentLinkedQueue},
 * thus this class is thread safe and lock free.
 */
public class InCoreAppendableMemory<Col extends Column> implements AppendableMemory<Col> {
	
	private final ConcurrentLinkedQueue<Col> queue = new ConcurrentLinkedQueue<Col>();

	public String fileId() throws IOException {
		throw new IOException("in core memory has no associated file id");
	}
	public void appendColumn(Col column) throws IOException {
		queue.add(column);
	}

	public void appendColumns(Iterable<? extends Col> columns) throws IOException {
		for (final Col col : columns) queue.add(col);
	}

	public void appendFrom(IndexableMemory<? extends Col> memory) throws IOException {
		for (final Col col : memory) queue.add(col);
	}
	
	/**
	 * Note that this is not efficient, it uses O(n)
	 */
	public int getColumnCount() throws IOException {
		return queue.size();
	}
	
	public Iterator<Col> iterator() {
		return Collections.unmodifiableCollection(queue).iterator();
	}
	
	public void clear() throws IOException {
		queue.clear();
	}
	public SortableMemory<Col> toSortableMemory() throws IOException {
		final InCoreMemory<Col> mem = new InCoreMemory<Col>();
		mem.appendColumns(this);
		return mem;
	}
	public void close(boolean erase) throws IOException {
		//nothing to do
	}

	public void flush() throws IOException {
		//nothing to do
	}
}
