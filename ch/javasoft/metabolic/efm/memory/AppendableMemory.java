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
package ch.javasoft.metabolic.efm.memory;

import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;

/**
 * The <code>AppendableMemory</code> adds methods to append modes to the memory.
 * Subclasses should implement append functionality in a thread-safe manner.
 */
public interface AppendableMemory<Col extends Column> extends IterableMemory<Col> {
	/**
	 * Appends the given column to this memory
	 * 
	 * @param column		the column to append
	 * @throws IOException	if any i/o exception occurs
	 */
    void appendColumn(Col column) throws IOException;
	/**
	 * Appends the given columns to this memory
	 * 
	 * @param columns		the columns to append
	 * @throws IOException	if any i/o exception occurs
	 */
    void appendColumns(Iterable<? extends Col> columns) throws IOException;
	/**
	 * Appends the columns from the specified memory to this memory
	 * 
	 * @param memory		the memory containing columns to append
	 * @throws IOException	if any i/o exception occurs
	 */
    void appendFrom(IndexableMemory<? extends Col> memory) throws IOException;
	/**
	 * Flushes the changed data to the underlying stream or file
	 * 
	 * @throws IOException	if any i/o exception occurs
	 */
    void flush() throws IOException;
    /**
     * Returns a sortable memory instance containing all columns currently
     * contained in this appendable memory. For certain memory implementations,
     * this is an efficient operation since this instance also implements the
     * read/write memory, and the same instance can be returned. For other 
     * implementations, a new memory instance might be created an all columns are
     * added to the new instance.
     * <p>
     * Note that the thread-safeness of an implementation might be lost after
     * conversion to the new sortable instance.
     */
    SortableMemory<Col> toSortableMemory() throws IOException;
}