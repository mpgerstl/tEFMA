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
package ch.javasoft.smx.ops;

import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;

/**
 * The <code>GaussPivoting</code> interface defines a pivoting strategy for
 * the gauss algorithm. Information to find the pivot, and the current pivot
 * row and column index are stored and can be retrieved after checking the 
 * matrix.
 * <p>
 * Implementing member classes are available with standard pivoting techniques.
 * 
 * @param <M> The matrix/number type supported by this pivoting strategy
 */
public interface GaussPivoting<M extends ReadableMatrix<?>> {
	
	/**
	 * Check a whole row, e.g. to count the number of zeros for the row, since
	 * {@link #checkCandidateCol(ReadableMatrix, int, int, int, int) checkCandidateCol(..)} 
	 * is only called for non-zero values. The returned value is then passed to
	 * {@link #checkCandidateCol(ReadableMatrix, int, int, int, int) checkCandidateCol(..)}.
	 * 
	 * @param matrix	the matrix to check
	 * @param pivot		the pivot index, 0 for the first pivot
	 * @param row		the row to check
	 * @return a	value which is then passed to subsequent calls to
	 * 				{@link #checkCandidateCol(ReadableMatrix, int, int, int, int) checkCandidateCol(..)}
	 */
	int checkCandidateRow(M matrix, int pivot, int row);
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
	 * 					{@link #checkCandidateRow(ReadableMatrix, int, int) checkCandidateRow(..)}
	 * @return true if the pivot search should be continued, and false if the
	 * 				pivot is already identified and searching can be aborted  
	 */
	boolean checkCandidateCol(M matrix, int pivot, int row, int col, int rowResult);
	/** Returns the row index of the pivot row*/
	int getPivotRow();
	/** Returns the column index of the pivot row*/
	int getPivotCol();
	
	/**
	 * This pivoting strategy for (B)ig (I)nteger rational matrices uses the 
	 * product of the bit lengths of numerator and denominator. Note that the 
	 * length is an estimate for the logarithm of the big integer numbers. A
	 * pivot element is chosen if the product is (L)ess than the value of a 
	 * previous candidate.
	 * <p>
	 * pivot, if <code>bitlen(numerator)*bitlen(denominator) < previous</code>
	 */
	public static class BiLenProductL implements GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> {
		int pivCol;
		int pivRow;
		int pivPro = Integer.MAX_VALUE;
		public BiLenProductL(int pivot) {
			pivRow = pivot;
			pivCol = pivot;
		}
		public int checkCandidateRow(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row, int col, int rowResult) {
			final BigInteger num = matrix.getBigIntegerNumeratorAt(row, col);
			final BigInteger den = matrix.getBigIntegerDenominatorAt(row, col);
			final int pro = num.abs().bitLength() * den.abs().bitLength();
			if (pro < pivPro) {
				pivCol = col;
				pivRow = row;
				pivPro = pro;
			}
			//if we have a 1 or a -1, we can stop
			return pivPro != 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy for (B)ig (I)nteger rational matrices uses the 
	 * product of the bit lengths of numerator and denominator. Note that the 
	 * length is an estimate for the logarithm of the big integer numbers. A
	 * pivot element is chosen if the product is (L)ess than or (Equal) to the 
	 * value of a previous candidate.
	 * <p>
	 * pivot, if <code>bitlen(numerator)*bitlen(denominator) <= previous</code>
	 */
	public static class BiLenProductLE implements GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> {
		int pivCol;
		int pivRow;
		int pivPro = Integer.MAX_VALUE;
		public BiLenProductLE(int pivot) {
			pivRow = pivot;
			pivCol = pivot;
		}
		public int checkCandidateRow(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row, int col, int rowResult) {
			final BigInteger num = matrix.getBigIntegerNumeratorAt(row, col);
			final BigInteger den = matrix.getBigIntegerDenominatorAt(row, col);
			final int pro = num.abs().bitLength() * den.abs().bitLength();
			if (pro <= pivPro) {
				pivCol = col;
				pivRow = row;
				pivPro = pro;
			}
			//we must always continue, since subsequent 1's are preferred
			return true;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy for (B)ig (I)nteger rational matrices uses the 
	 * product of the bit lengths of numerator and denominator. Note that the 
	 * length is an estimate for the logarithm of the big integer numbers. A
	 * pivot element is chosen if the product is (L)ess than the value of a 
	 * previous candidate. If it is equal, the number of zeros of the row
	 * determines the choice of the pivot element.
	 * <p>
	 * pivot, if 
	 * <code>
	 *   bitlen(numerator)*bitlen(denominator)  < previous OR
	 *   bitlen(numerator)*bitlen(denominator) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class BiLenProductLorEandMoreRowZeros implements GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> {
		int pivCol;
		int pivRow;
		int pivPro = Integer.MAX_VALUE;
		int pivCnt = 0;
		public BiLenProductLorEandMoreRowZeros(int pivot) {
			pivRow = pivot;
			pivCol = pivot;
		}
		public int checkCandidateRow(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row) {
			final int cols = matrix.getColumnCount();
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (matrix.getSignumAt(row, col) == 0) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row, int col, int zerCnt) {
			final BigInteger num = matrix.getBigIntegerNumeratorAt(row, col);
			final BigInteger den = matrix.getBigIntegerDenominatorAt(row, col);
			final int pro = num.abs().bitLength() * den.abs().bitLength();
			if (pro < pivPro || pro == pivPro && zerCnt > pivCnt) {
				pivCol = col;
				pivRow = row;
				pivPro = pro;
				pivCnt = zerCnt;
			}
			//if we have a 1 or a -1, we can stop if we have the maximum 
			//possible number of zeros
			return pivPro != 1 || pivCnt < matrix.getColumnCount() - 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy for (B)ig (I)nteger rational matrices uses the 
	 * sum of the bit lengths of numerator and denominator. Note that the 
	 * length is an estimate for the logarithm of the big integer numbers. A
	 * pivot element is chosen if the product is (L)ess than the value of a 
	 * previous candidate. If it is equal, the number of zeros of the row
	 * determines the choice of the pivot element.
	 * <p>
	 * pivot, if 
	 * <code>
	 *   bitlen(numerator)+bitlen(denominator)  < previous OR
	 *   bitlen(numerator)+bitlen(denominator) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class BiLenSumLorEandMoreRowZeros implements GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> {
		int pivCol;
		int pivRow;
		int pivSum = Integer.MAX_VALUE;
		int pivCnt = 0;
		public BiLenSumLorEandMoreRowZeros(int pivot) {
			pivRow = pivot;
			pivCol = pivot;
		}
		public int checkCandidateRow(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row) {
			final int cols = matrix.getColumnCount();
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (matrix.getSignumAt(row, col) == 0) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row, int col, int zerCnt) {
			final BigInteger num = matrix.getBigIntegerNumeratorAt(row, col);
			final BigInteger den = matrix.getBigIntegerDenominatorAt(row, col);
			final int sum = num.abs().bitLength() + den.abs().bitLength();
			if (sum < pivSum || sum == pivSum && zerCnt > pivCnt) {
				pivCol = col;
				pivRow = row;
				pivSum = sum;
				pivCnt = zerCnt;
			}
			//if we have a 1 or a -1, we can stop if we have the maximum 
			//possible number of zeros
			return pivSum != 2 || pivCnt < matrix.getColumnCount() - 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy for (B)ig (I)nteger rational matrices uses the 
	 * maximum of the bit lengths of numerator and denominator. Note that the 
	 * length is an estimate for the logarithm of the big integer numbers. A
	 * pivot element is chosen if the product is (L)ess than the value of a 
	 * previous candidate. If it is equal, the number of zeros of the row
	 * determines the choice of the pivot element.
	 * <p>
	 * pivot, if 
	 * <code>
	 *   max(bitlen(numerator), bitlen(denominator))  < previous OR
	 *   max(bitlen(numerator), bitlen(denominator)) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class BiMaxLenLorEandMoreRowZeros implements GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> {
		int pivCol;
		int pivRow;
		int pivLen = Integer.MAX_VALUE;
		int pivCnt = 0;
		public BiMaxLenLorEandMoreRowZeros(int pivot) {
			pivRow = pivot;
			pivCol = pivot;
		}
		public int checkCandidateRow(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row) {
			final int cols = matrix.getColumnCount();
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (matrix.getSignumAt(row, col) == 0) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(ReadableBigIntegerRationalMatrix<BigFraction> matrix, int pivot, int row, int col, int zerCnt) {
			final BigInteger num = matrix.getBigIntegerNumeratorAt(row, col);
			final BigInteger den = matrix.getBigIntegerDenominatorAt(row, col);
			final int len = Math.max(num.abs().bitLength(), den.abs().bitLength());
			if (len < pivLen || len == pivLen && zerCnt > pivCnt) {
				pivCol = col;
				pivRow = row;
				pivLen = len;
				pivCnt = zerCnt;
			}
			//if we have a 1 or a -1, we can stop if we have the maximum 
			//possible number of zeros
			return pivLen != 1 || pivCnt < matrix.getColumnCount() - 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
}
