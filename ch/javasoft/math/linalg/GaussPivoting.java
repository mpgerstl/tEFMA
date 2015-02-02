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
package ch.javasoft.math.linalg;


/**
 * The <code>GaussPivoting</code> interface defines a pivoting strategy for
 * the gauss algorithm. Information to find the pivot, and the current pivot
 * row and column index are stored and can be retrieved after checking the 
 * matrix.
 * <p>
 * Implementing member classes are available with standard pivoting techniques.
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public interface GaussPivoting<N extends Number, A> {
	
	/**
	 * Check a whole row, e.g. to count the number of zeros for the row, since
	 * {@link #checkCandidateCol(A[], int, int, int, int) checkCandidateCol(..)} 
	 * is only called for non-zero values. The returned value is then passed to
	 * {@link #checkCandidateCol(A[], int, int, int, int) checkCandidateCol(..)}.
	 * 
	 * @param matrix	the matrix to check
	 * @param pivot		the pivot index, 0 for the first pivot
	 * @param row		the row to check
	 * @return a	value which is then passed to subsequent calls to
	 * 				{@link #checkCandidateCol(A[], int, int, int, int) checkCandidateCol(..)}
	 */
	int checkCandidateRow(A[] matrix, int pivot, int row);
	/**
	 * Check metrix value, e.g. to find smallest or largest pivot elements. Note
	 * that this method is only called for non-zero values. Returns true if the
	 * search should be continued, and false if the pivot is already found and
	 * searching can be aborted.
	 * 
	 * @param matrix	the matrix to check
	 * @param pivot		the pivot index, 0 for the first pivot
	 * @param row		the row index of the value to check
	 * @param col		the column index of the value to check
	 * @param rowResult the result which was previously returned by
	 * 					{@link #checkCandidateRow(A[], int, int) checkCandidateRow(..)}
	 * @return true if the pivot search should be continued, and false if the
	 * 				pivot is already identified and searching can be aborted  
	 */
	boolean checkCandidateCol(A[] matrix, int pivot, int row, int col, int rowResult);
	/** Returns the row index of the pivot row*/
	int getPivotRow();
	/** Returns the column index of the pivot row*/
	int getPivotCol();
	
}
