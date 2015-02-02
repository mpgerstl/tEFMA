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
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;

/**
 * The <code>MemoryFactory</code> class instantiates the appropriate memory.
 * The factory is usually configured and instantiated with a default constructor
 * with no arguments.
 */
public interface MemoryFactory {
	/**
	 * Creates an empty, appendable memory instance for concurrent use by 
	 * multiple threads. Note that thread-safety is the relevant advantage of
	 * using this method instead of {@link #createReadWriteMemory(ColumnHome, NetworkEfmModel, int, MemoryPart) createReadWriteMemory()},
	 * since a {@link ReadWriteMemory read/write memory} implements all 
	 * functionality of the {@link AppendableMemory appendable memory}, but is
	 * not necessarily thread-safe. 
	 * 
	 * @param <N>			number type
	 * @param <Col>			column type
	 * @param columnHome	column home defining number and column type
	 * @param efmModel		the model with access to config and other model
	 * 						specific information, such as binary and numeric
	 * 						size of columns for any iteration step
	 * @param iteration		the current iteration, <code>0</code> for initial, 
	 * 						<code>1..n</code> for iterations 1 to n, and 
	 * 						<code>n+1</code> for the final memory. The iteration 
	 * 						index must be that index which defines the state of 
	 * 						the columns added to the memory. For instance, if a 
	 * 						memory is initialized in iteration 0 with columns to 
	 * 						be used in iteration one, the iteration index should 
	 * 						be 1.
	 * @param part			memory part for partitioned memories, or null if a
	 * 						non-partitioned memory instance is desired
	 * @return				the appendable, thread-safe empty memory instance
	 * @throws IOException	if an i/o exception occurrs, e.g. if the memory is
	 * 						stored on disk
	 */
	<N extends Number, Col extends Column> AppendableMemory<Col> createConcurrentAppendableMemory(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, int iteration, MemoryPart part) throws IOException;
	/**
	 * Creates an empty, read/write memory instance. Note that this memory type
	 * is not necessarily thread-safety, so you might consider using 
	 * {@link #createConcurrentAppendableMemory(ColumnHome, NetworkEfmModel, int, MemoryPart) createConcurrentAppendableMemory()} 
	 * instead, returning a thread-safe memory instance for appending.
	 * 
	 * @param <N>			number type
	 * @param <Col>			column type
	 * @param columnHome	column home defining number and column type
	 * @param efmModel		the model with access to config and other model
	 * 						specific information, such as binary and numeric
	 * 						size of columns for any iteration step
	 * @param iteration		the current iteration, <code>0</code> for initial, 
	 * 						<code>1..n</code> for iterations 1 to n, and 
	 * 						<code>n+1</code> for the final memory. The iteration 
	 * 						index must be that index which defines the state of 
	 * 						the columns added to the memory. For instance, if a 
	 * 						memory is initialized in iteration 0 with columns to 
	 * 						be used in iteration one, the iteration index should 
	 * 						be 1.
	 * @param part			memory part for partitioned memories, or null if a
	 * 						non-partitioned memory instance is desired
	 * @return				the empty read/write memory instance (not thread-safe)
	 * @throws IOException	if an i/o exception occurrs, e.g. if the memory is
	 * 						stored on disk
	 */
	<N extends Number, Col extends Column> ReadWriteMemory<Col> createReadWriteMemory(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, int iteration, MemoryPart part) throws IOException;
}
