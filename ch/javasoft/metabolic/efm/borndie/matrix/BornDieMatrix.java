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
package ch.javasoft.metabolic.efm.borndie.matrix;

import java.io.IOException;

import ch.javasoft.metabolic.efm.borndie.job.PairingJob;
import ch.javasoft.metabolic.efm.borndie.range.LowerTriangularMatrix;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <code>BornDieMatrix</code> is a lower triangular matrix containing cells
 * with intermediary rows. The row of the cell encodes for the die index, the
 * column for the birth index.
 * <p>
 * Note that the matrix contains n+1 columns and rows, where n is the number of
 * sequential iteration steps. Columns in the cell [i,j] are born during
 * iteration j, where the first column 0 contains modes from the initial 
 * matrix, and the last column n contains modes born during the last 
 * iteration. The first row 0 contains modes which become infeasible at 
 * iteration 1, the second last row n-1 contains modes dying during the last
 * iteration, and row n contains the final modes (the algorithm's result).
 * <p>
 * For instance, cell [4,1] contains modes born in the first iteration step,
 * becoming infeasible during the 3rd iteration step.
 * 
 * @type Col the column type of the columns (representing modes) stored in the 
 * 			matrix cells
 */
public interface BornDieMatrix<Col extends Column> {
	/**
	 * Returns the cell stage for the specified cell.
	 * 
	 * @param bornCol	the born index, from 0 to n, i.e. n+1 columns for n iterations
	 * @param dieRow	the die index, from 0 to n, i.e. n+1 rows for n iterations
	 * 
	 * @return the cell stage for the specified cell
	 * @throws IndexOutOfBoundsException 	if row or col is negative, or if row
	 * 										or col is larger than the number of
	 * 										iterations, or if col > row.
	 */
	CellStage getCellStage(int bornCol, int dieRow);

//	/**
//	 * The cell switches to the next stage. No stages are skipped, the caller
//	 * has to trigger another stage change to skip stages. Skipping stages might
//	 * for instance be desired if a cell leaves the accumulating stage and 
//	 * contains no modes.
//	 * <p>
//	 * <p>
//	 * To handle concurrent calls, the cell's current stage has to be specified.
//	 * The cell might already be in a subsequent stage, and no stage change is
//	 * necessary. If the specified stage equals the cell's current stage, the 
//	 * cell changes to the next stage. Otherwise, the cell was already in a 
//	 * subsequent stage. The method returns true if the cell stage was changed.
//	 * 
//	 * @param bornCol		the column index of this cell in the matrix
//	 * @param dieRow		the row index of this cell in the matrix
//	 * @param currentStage	the current cell stage
//	 * 
//	 * @return 	true if the cell stage was changed, and false if it was already
//	 * 			in a stage subsequent to the specified current stage
//	 * 
//	 * @throws IOException	if an i/o exception occurs, e.g. when a new 
//	 * 						file-based memory instantiation fails
//	 * @throws IndexOutOfBoundsException 	if row or col is negative, or if row
//	 * 										or col is larger than the number of
//	 * 										iterations, or if col > row.
//	 */
//	boolean switchCellStage(int bornCol, int dieRow, CellStage currentStage) throws IOException;
	
	/**
	 * Returns an object representing the lower triangular matrix of this 
	 * born-die matrix
	 */
	LowerTriangularMatrix getMatrixRange();
	
	/**
	 * Returns the number of iterations, defining the matrix size. For n 
	 * iterations, the lower-triangular matrix has n+1 rows and columns, i.e.
	 * valid row/col indices range from 0..n (both inclusive). 
	 */
	int getIterationCount();
	
	/**
	 * Creates and schedules all pairing jobs for the specified cell, with 
	 * partner cells in the specified active columns. If the cell is not in the 
	 * {@link CellStage#Bearing bearing} stage, or if the specified columns are 
	 * not containing partner cells (according to the {@link PairingRule}), an
	 * exception is thrown.
	 * 
	 * @param bornCol				the column index of the bearing cell
	 * @param dieRow				the row index of the bearing cell
	 * @param activeBornColumnFrom	the first column with partner cells, 
	 * 								inclusive
	 * @param activeBornColumnTo	the last column with partner cells, 
	 * 								exclusive 
	 * @throws IOException	if an i/o exception occurs, e.g. when a new 
	 * 						file-based memory instantiation fails
	 * @throws IllegalStateException if this cell is not in the 
	 * 			{@link CellStage#Bearing bearing} stage
	 * @throws IllegalArgumentException if the columns are not partner columns
	 * 			of this cell, according to the {@link PairingRule}
	 */
	void schedulePairingJobs(int bornCol, int dieRow, int activeBornColumnFrom, int activeBornColumnTo) throws IOException, IllegalStateException, IllegalArgumentException;	
	/**
	 * Notify that all modes have been added to the initial column, that is, the
	 * first column is ready to start pairing jobs now
	 * 
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	void notifyInitialColumnComplete() throws IOException;
//	/**
//	 * Notify that all pairing jobs of the specified {@code owner} cell are
//	 * empty, since the cell contains no modes
//	 * 
//	 * @param owner	owner cell, containing dying modes of the pairing jobs
//	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
//	 * 						memory
//	 */
//	void notifyAllJobsEmpty(CellRange owner) throws IOException;
	/**
	 * Notify about the completion of a {@link PairingJob pairing job}
	 * 
	 * @param job	the completed job
	 */
	void notifyPairingJobCompleted(PairingJob<Col> job) throws IOException;
	/**
	 * Returns the memory for appending modes to the cell in the born-die 
	 * matrix.
	 * 
	 * @param bornCol	the born index, from 0 to n, i.e. n+1 columns for n iterations
	 * @param dieRow	the die index, from 0 to n, i.e. n+1 rows for n iterations
	 * 
	 * @return the memory for appending
	 * 
	 * @throws IllegalStateException		if this cell is neither in the
	 * 								 		{@link CellStage#Accumulating Accumulating} stage
	 * @throws IndexOutOfBoundsException 	if row or col is negative, or if row
	 * 										or col is larger than the number of
	 * 										iterations, or if col > row.
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	AppendableMemory<Col> getForAppending(int bornCol, int dieRow) throws IllegalStateException, IOException;

	/**
	 * Returns the memory for the generation of new modes from adjacent modes 
	 * resulting from the pairing of two generating memories, one for negative
	 * and one for positive modes. Here, all dying modes are returned.
	 * 
	 * @param bornCol	the born index, from 0 to n, i.e. n+1 columns for n iterations
	 * @param dieRow	the die index, from 0 to n, i.e. n+1 rows for n iterations
	 * 
	 * @return the memory for reading or sorting
	 * 
	 * @throws IllegalStateException		if this cell is not in an
	 * 								 		{@link CellStage#isActive() active} 
	 * 										stage
	 * @throws IndexOutOfBoundsException 	if row or col is negative, or if row
	 * 										or col is larger than the number of
	 * 										iterations, or if col > row.
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	SortableMemory<Col> getNegForGenerating(int bornCol, int dieRow) throws IllegalStateException, IOException;
	/**
	 * Returns the memory for the generation of new modes from adjacent modes 
	 * resulting from the pairing of two generating memories, one for negative
	 * and one for positive modes. Here, surviving modes for partners in dying
	 * row {@code partnerDyingRow} are returned.
	 * 
	 * @param bornCol			the born index, from 0 to n, i.e. n+1 columns for 
	 * 							n iterations
	 * @param dieRow			the die index, from 0 to n, i.e. n+1 rows for n 
	 * 							iterations
	 * @param partnerDieRow		the die index, from dieRow + 1 to n
	 * 
	 * @return the memory for reading or sorting
	 * 
	 * @throws IllegalStateException		if this cell is not in an
	 * 								 		{@link CellStage#isActive() active} 
	 * 										stage
	 * @throws IndexOutOfBoundsException 	if row or col is negative, or if row
	 * 										or col is larger than the number of
	 * 										iterations, or if col > row
	 * @throws IllegalArgumentException 	if {@code partnerDyingRow} is not 
	 * 										greater than {@code dieRow}
	 * 										iterations, or if col > row
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	SortableMemory<Col> getPosForGenerating(int bornCol, int dieRow, int partnerDieRow) throws IllegalStateException, IOException;
	/**
	 * Returns the memory at the specified cell (always the last row) containing
	 * the final modes.
	 * 
	 * @param bornCol	the born index, from 0 to n, i.e. n+1 columns for n iterations
	 * 
	 * @return the memory for reading
	 * 
	 * @throws IllegalStateException		if this cell is not in the
	 * 								 		{@link CellStage#Done Done} stage
	 * @throws IndexOutOfBoundsException 	if col is negative or larger than 
	 * 										the number of iterations
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	IterableMemory<Col> getFinal(int bornCol) throws IllegalStateException, IOException;
	
	/**
	 * Returns the number of modes (aka columns) stored within the specified
	 * cell.
	 * 
	 * @param bornCol	the born index, from 0 to n, i.e. n+1 columns for n iterations
	 * @param dieRow	the die index, from 0 to n, i.e. n+1 rows for n iterations
	 * 
	 * @return the number of columns in the specified cell
	 * 
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	int getColumnCount(int bornCol, int dieRow) throws IOException;

	/**
	 * Return true if the specified cell contains no modes.
	 * 
	 * @param bornCol	the born index, from 0 to n, i.e. n+1 columns for n iterations
	 * @param dieRow	the die index, from 0 to n, i.e. n+1 rows for n iterations
	 * 
	 * @return true if cell contains no modes
	 * 
	 * @throws IndexOutOfBoundsException 	if row or col is negative, or if row
	 * 										or col is larger than the number of
	 * 										iterations, or if col > row.
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	boolean isEmpty(int bornCol, int dieRow) throws IOException;
}
