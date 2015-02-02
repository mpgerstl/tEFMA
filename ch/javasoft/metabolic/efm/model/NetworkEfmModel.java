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

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.filter.ColumnFilter;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.util.ReactionMapping;

/**
 * The <code>NetworkEfmModel</code> is a common extension of the efm model with
 * access to the metabolic network. 
 * <p>
 * Usually, the {@link NetworkEfmModel} is used in the algorithm implementation 
 * classes, and adjacent ray generators operate on the simpler {@link EfmModel}.
 */
public interface NetworkEfmModel extends EfmModel {
	
	/**
	 * Returns the (possibly compressed) metatbolic network
	 */
	MetabolicNetwork getMetabolicNetwork();
	
	/**
	 * Returns access to the reaction mapping. The reaction mapping class 
	 * contains the whole chain of mapping reactions, e.g. splitting reactions
	 * into two, resorting reactions etc. Note that this class is related to
	 * {@link #getReactionSorting() reaction sorting}, but offers enhanced
	 * functionality.
	 */
	ReactionMapping getReactionMapping();
	
	/**
	 * Returns the column filter, sorting out columns before completion of the
	 * algorithm. Column filtering might for instance include removal of futile 
	 * cycle modes resulting from split reversible reactions.
	 */
	ColumnFilter getColumnFilter();
	
	/**
	 * Returns the number of iterations
	 */
	int getIterationCount();
	
	/**
	 * Returns the index of the hyperplane for the specified iteration. At the
	 * specified iteration step, the returned hyperplane is used to separate
	 * kept from removed intermediary modes.
	 * <p>
	 * <b>Notes: </b><br>
	 * <i>Nullspace approach</i><br>
	 * For the nullspace approach, a hyperplane is associated with a reaction,
	 * i.e. with a column in the stoichiometric matrix or a row in
	 * the kernel matrix or within a {@link Column}. The index returned here is
	 * directly applicable to the kernel or column, i.e. it reflects the index
	 * of the sorted reaction according to {@link #getReactionSorting() reaction sorting}. 
	 * The index must be unmapped to use it to access appropriate stoich matrix 
	 * columns, e.g. by using the {@link #getReactionMapping() reaction mapping}.
	 * <br>
	 * <i>Canonical approach</i><br>
	 * For the canonical approach, a hyperplane is associated with a stoich
	 * matrix row. Since the stoich matrix itself is not resorted, hyperplane
	 * sorting is reflected by {@link #getMetaboliteSorting() metabolite sorting}.
	 * The returned index performs this mapping, i.e. the index can be used to
	 * address the appropriate row in the stoich matrix.
	 * 
	 * @param iteration 	0 for initial modes, and 1..{@link #getIterationCount() n} 
	 * 						for normal iterations.
	 * 
	 * @return 	the index of the hyperplane as described above
	 */
	int getHyperplaneIndex(int iteration);
	
	/**
	 * Returns the columns' boolean size at the given iteration
	 * 
	 * @param iteration	0 for initial, 1 to n for the normal iterations
	 */
	int getBooleanSize(int iteration);
	/**
	 * Returns the columns' numeric size at the given iteration
	 * 
	 * @param iteration	0 for initial, 1 to n for the normal iterations
	 */
	int getNumericSize(int iteration);
	/**
	 * Returns the binary size after post processing. This is typically the 
	 * number of reactions (or rows in the kernel matrix) for the nullspace 
	 * approach. For the canonical approach, this value is constant and equals
	 * the number of reactions, or more precise, the number of columns in the 
	 * expanded stoichiometric matrix. 
	 */
	int getFinalBooleanSize();
	/**
	 * Returns the numeric size after post processing. This is typically zero 
	 * for the nullspace approach. For the canonical approach, this value is 
	 * constant and equals the number of reactions, or more precise, the number 
	 * of columns in the expanded stoichiometric matrix. 
	 */
	int getFinalNumericSize();
	
	/**
	 * Returns the number reactions which are not processed within the iteration
	 * loop, e.g. because a flux value is enforced or because they are not
	 * split. This value is usually obtained from the
	 * {@link ReactionMapping#getExpandedReactionCountOutOfIterationLoop() reaction mapping}.
	 */
	int getOutOfIterationLoopCount();
	
	/**
	 * Returns the converter to create flux distribution vectors from columns.
	 * Depending on the model, this might include conversion from binary to
	 * numeric. For other implementations, numeric values are contained within
	 * the columns.
	 * 
	 * @param <N>			the number type
	 * @param <Col>			the column type
	 * @param columnHome	the column home specifying number and column type
	 * @return the converter instance
	 */
	<N extends Number, Col extends Column> ColumnToFluxDistributionConverter<N, Col> getColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome);
	/**
	 * Create and return the initial memory, containing the initial columns,
	 * that is, the returned memory contains the initial generating matrix.
	 * Depending on the model, the initial matrix might be the inverse of the 
	 * system matrix, some special kernel matrix etc.
	 * 
	 * @param <N>			the number type
	 * @param <Col>			the column type
	 * @param columnHome	the column home specifying number and column type
	 * @param memoryFactory	the memory factory to create the appropriate memory
	 * @throws IOException	if writing to the memory causes an i/o exception
	 */
	<N extends Number, Col extends Column> AppendableMemory<Col> createInitialMemory(ColumnHome<N, Col> columnHome, MemoryFactory memoryFactory) throws IOException;
	
	/**
	 * Columns are partitioned into the 3 groups zero, positive and negative.
	 * Columns in the zero group lie within the separating hyperplane, columns
	 * in the positive group are on the strict positive side, columns in the 
	 * negative group are on the strict negative side.
	 */
	enum Partition {Zero, Positive, Negative};
	
	/**
	 * Returns true if the corresponding columns are cut off, which are usually
	 * the negative columns. However, certain strategies (e.g. the canonical
	 * approach) might process two constraints at the time, thus, also positive
	 * columns might be cut off and only columns within the hyperplane are kept. 
	 * 
	 * @param partition	the partition to question
	 * @return true if the columns within the specified partition should be cut
	 * 				off
	 */
	boolean cutOff(Partition partition);
	
//	/**
//	 * Partitions the columns with respect to the criteria reflected by this 
//	 * efm model, if the specified partition is not 
//	 * {@link #cutOff(Partition) cut off}. If keep is true, the appropriate keep 
//	 * method will be invoked for every column before moving it to the new 
//	 * partition.
//	 * <p>
//	 * Note that the source memory is closed after partitioning.
//	 * 
//	 * @param <Col>			column type
//	 * @param <N>			number type
//	 * @param columnHome	column home defining column and number type 
//	 * @param src			source memory containing columns to partition
//	 * @param pos			destination memory for columns on strictly positive 
//	 * 						side of the separating hyperplane
//	 * @param zer			destination memory for columns lying within the
//	 * 						separating hyperplane			
//	 * @param neg			destination memory for columns on strictly negative
//	 * 						side of the separating hyperplane
//	 * @param iteration 	the iteration index, from 1 to n, can be useful to
//	 * 						identify the hyperplane
//	 * @param keep			if true, the appropriate keep method is invoked for
//	 * 						the column before moving it. The keep methods 
//	 * 						usually convert the current position from numeric
//	 * 						to binary
//	 * @throws IOException	if an i/o exception occurs, e.g. for file-based 
//	 * 						memory implementations
//	 */
//	<Col extends Column, N extends Number> void partition(Column.Home<N, Col> columnHome, IterableMemory<Col> src, AppendableMemory<Col> pos, AppendableMemory<Col> zer, AppendableMemory<Col> neg, int iteration, boolean keep) throws IOException;
}
