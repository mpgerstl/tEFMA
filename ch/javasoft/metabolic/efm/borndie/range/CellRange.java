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
package ch.javasoft.metabolic.efm.borndie.range;

import java.io.IOException;

import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.matrix.CellStage;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * A <code>CellRange</code> is a cell range referring to a single cell.
 */
public interface CellRange extends RectangularRange {
	
	/**
	 * Returns the die row index of this cell. The die row is 
	 * {@code it - 1} for modes dying at iteration {@code it}, where {@code it}
	 * specifies the 1-based iteration index. For final (never dying) modes in
	 * the last matrix row, the returned index is {@code it}.
	 */
	int getDieRow();
	/**
	 * Returns the born column index of this cell. The born column is 0 for
	 * initial columns, and the 1-based iteration index otherwise.
	 */
	int getBornColumn();

	/**
	 * Returns the memory for appending modes to the born-die matrix cell
	 * identified by this single cell.
	 * 
	 * @param matrix	the born-die matrix to access the cell's modes
	 * @return 	the memory for appending
	 * 
	 * @throws IllegalStateException	if this cell is not in the
	 * 								 	{@link CellStage#Accumulating Accumulating} 
	 * 									stage
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	<Col extends Column> AppendableMemory<Col> getForAppending(BornDieMatrix<Col> matrix) throws IllegalStateException, IOException;
	/**
	 * Returns the memory for the generation of new modes from adjacent modes 
	 * resulting from the pairing of two generating memories, one for negative
	 * and one for positive modes.
	 * 
	 * @param matrix	the born-die matrix to access the cell's modes
	 * @return the memory for reading or sorting
	 * 
	 * @throws IllegalStateException if this cell is not in an
	 * 								 {@link CellStage#isActive() active} stage
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	<Col extends Column> SortableMemory<Col> getNegForGenerating(BornDieMatrix<Col> matrix) throws IllegalStateException, IOException;
	/**
	 * Returns the memory for the generation of new modes from adjacent modes 
	 * resulting from the pairing of two generating memories, one for negative
	 * and one for positive modes.
	 * 
	 * @param matrix	the born-die matrix to access the cell's modes
	 * @param dieRow	the die row of the dying partner cells
	 * @return the memory for reading or sorting
	 * 
	 * @throws IllegalStateException if this cell is not in an
	 * 								 {@link CellStage#isActive() active} stage
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	<Col extends Column> SortableMemory<Col> getPosForGenerating(BornDieMatrix<Col> matrix, int dieRow) throws IllegalStateException, IOException;
}
