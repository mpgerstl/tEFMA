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
package ch.javasoft.metabolic.efm.model;

import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <code>AdjEnumModel</code> contains all state of the algorithm which is
 * associated with a single iteration of the double description method. 
 * Typically, this involves the intermediary modes or columns, separated into
 * positive, zero and negative parts. 
 * <p>
 * Positive and zero modes correspond to feasible modes not contained 
 * in/contained in the separating hyperplane. Negative modes correspond to 
 * infeasible modes.   
 */
public class AdjEnumModel<Col extends Column> extends DefaultIterationStepModel {
	
	private final SortableMemory<Col>	pos, zero, neg;
	private final AppendableMemory<Col>	out;
	
	/**
	 * Default constructor used by most implementations. Iteration state models
	 * for current and next iteration step are derived from the efm model.
	 * 
	 * @param efmModel			efm model, from which current and next iteration
	 * 							state is derived
	 * @param iterationIndex	The current iteration index, where 0 is before 
	 * 							the first iteration. Thus, this index is 1 
	 * 							based. The highest possible index is determined 
	 * 							by {@link NetworkEfmModel#getIterationCount()}.
	 * @param pos	memory with positive intermediary modes or columns, not
	 * 				thread safe
	 * @param zero	memory with zero intermediary modes or columns, not thread
	 * 				safe
	 * @param neg	memory with negative intermediary modes or columns, not
	 * 				thread safe
	 * @param out	appendable memory to add new modes or columns derived from 
	 * 				adjacent positive/negative pairs, must be thread-safe
	 */
	public AdjEnumModel(NetworkEfmModel efmModel, int iterationIndex, SortableMemory<Col> pos, SortableMemory<Col> zero, SortableMemory<Col> neg, AppendableMemory<Col> out) {
		super(efmModel, iterationIndex);
		this.pos	= pos;
		this.zero	= zero;
		this.neg	= neg;
		this.out	= out;
	}
	
	/**
	 * Constructor for used by distributed computation.
	 * 
	 * @param stepModel	contains information about the current iteration step
	 * @param pos	memory with positive intermediary modes or columns, not
	 * 				thread safe
	 * @param zero	memory with zero intermediary modes or columns, not thread
	 * 				safe
	 * @param neg	memory with negative intermediary modes or columns, not
	 * 				thread safe
	 * @param out	appendable memory to add new modes or columns derived from 
	 * 				adjacent positive/negative pairs, must be thread-safe
	 */
	public AdjEnumModel(IterationStepModel stepModel, SortableMemory<Col> pos, SortableMemory<Col> zero, SortableMemory<Col> neg, AppendableMemory<Col> out) {
		super(stepModel);
		this.pos	= pos;
		this.zero	= zero;
		this.neg	= neg;
		this.out	= out;
	}

	/** 
	 * A sortable/readable memory with positive, negative or zero intermediary 
	 * modes or columns, depending on the part argument. Serves as accessor for 
	 * source columns on the any side of, or within, the separating hyperplane. 
	 * The memory is NOT thread-safe.
	 * <p>
	 * Calls
	 * <ul>
	 * 	<li>{@link #getMemoryPos()}	if <tt>part</tt> is {@link PartId#POS POS}
	 * 		</li>
	 * 	<li>{@link #getMemoryNeg()}	if <tt>part</tt> is {@link PartId#NEG NEG}
	 * 		</li>
	 * 	<li>{@link #getMemoryZero()} if <tt>part</tt> is {@link PartId#ZER ZER}
	 * 		</li>
	 * </ul>
	 * @throws IllegalArgumentException if <tt>part</tt> is none of the mentioned
	 * 									parts
	 */
	public SortableMemory<Col> getMemory(PartId part) {
		switch (part) {
			case POS:
				return getMemoryPos();
			case NEG:
				return getMemoryNeg();
			case ZER:
				return getMemoryZero();
			default:
				throw new IllegalArgumentException("unsupported memory part: " + part);
		}
	}

	/** 
	 * A sortable/readable memory with positive intermediary modes or columns.
	 * Serves as accessor for source columns on the positive side of the 
	 * separating hyperplane. The memory is NOT thread-safe.
	 * <p>
	 * The sortable property of the memory can be used to construct tree 
	 * structures upon the column/mode list (e.g. bit pattern trees). 
	 */
	public SortableMemory<Col> getMemoryPos() {
		return pos;
	}
	/** 
	 * A sortable/readable memory with zero intermediary modes or columns
	 * Serves as access for source columns lying in the separating hyperplane. 
	 * The memory is NOT thread-safe.
	 * <p>
	 * The sortable property of the memory can be used to construct tree 
	 * structures upon the column/mode list (e.g. bit pattern trees). 
	 */
	public SortableMemory<Col> getMemoryZero() {
		return zero;
	}
	/** 
	 * A sortable/readable memory with negative intermediary modes or columns
	 * Serves as access for source columns on the negative side of the 
	 * separating hyperplane. The memory is NOT thread-safe.
	 * <p>
	 * The sortable property of the memory can be used to construct tree 
	 * structures upon the column/mode list (e.g. bit pattern trees). 
	 */
	public SortableMemory<Col> getMemoryNeg() {
		return neg;
	}
	/**
	 * An appendable memory to add new modes or columns derived from adjacent
	 * positive/negative pairs. The memory is thread-safe.
	 * Serves as sink for next generation columns. Only columns from adjacent 
	 * columns (no kept columns) are added here. 
	 */
	public AppendableMemory<Col> getMemoryForNewFromAdj() {
		return out;
	}
	
	/**
	 * Closes all tables for the current thread
	 * 
     * @throws IOException	if an i/o exception occurs, for instance caused by
     * 						file based memory access 
	 */
	@Override
	public void closeForThread() throws IOException {
		pos.close(false /*erase*/);
		if (zero != null) zero.close(false /*erase*/);
		neg.close(false /*erase*/);
	}
	
}
