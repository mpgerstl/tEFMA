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
package ch.javasoft.math.varint.array;

import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.linalg.GaussPivoting;
import ch.javasoft.math.linalg.GaussPivotingFactory;
import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.varint.VarIntNumber;

/**
 * This class contains constants for factories returning instances of inner 
 * classes implementing {@link GaussPivoting pivoting strategies} for Gaussian 
 * elimination with {@link VarIntNumber} numbers.
 */
public class VarIntGaussPivoting {

	/**
	 * Returns a {@link LenL} strategy instance.
	 * <p>
	 * Pivot, if <code>bitlen(value) < previous</code>
	 */
	public static GaussPivotingFactory<VarIntNumber, VarIntNumber[]> LEN_L = new GaussPivotingFactory<VarIntNumber, VarIntNumber[]>() {
		public GaussPivoting<VarIntNumber,VarIntNumber[]> getGaussPivoting(NumberArrayOperations<VarIntNumber,VarIntNumber[]> numberOps, int pivot) {
			return new LenL(pivot);
		}
	};
	/**
	 * Returns a {@link LenLE} strategy instance.
	 * <p>
	 * Pivot, if <code>bitlen(value) <= previous</code>
	 */
	public static GaussPivotingFactory<VarIntNumber, VarIntNumber[]> LEN_LE = new GaussPivotingFactory<VarIntNumber, VarIntNumber[]>() {
		public GaussPivoting<VarIntNumber,VarIntNumber[]> getGaussPivoting(NumberArrayOperations<VarIntNumber,VarIntNumber[]> numberOps, int pivot) {
			return new LenLE(pivot);
		}
	};
	/**
	 * Returns a {@link LenLorEandMoreRowZeros} strategy instance.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   bitlen(value)  < previous OR
	 *   bitlen(value) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static GaussPivotingFactory<VarIntNumber, VarIntNumber[]> LEN_L_OR_E_AND_MORE_ROW_ZEROS = new GaussPivotingFactory<VarIntNumber, VarIntNumber[]>() {
		public GaussPivoting<VarIntNumber,VarIntNumber[]> getGaussPivoting(NumberArrayOperations<VarIntNumber,VarIntNumber[]> numberOps, int pivot) {
			return new LenLorEandMoreRowZeros(numberOps, pivot);
		}
	};
	
	/**
	 * This pivoting strategy uses bit length of the value. Note that the length 
	 * is an estimate for the logarithm of the big integer number. A pivot 
	 * element is chosen if the length is (L)ess than the value of a previous 
	 * candidate.
	 * <p>
	 * Pivot, if <code>bitlen(value) < previous</code>
	 */
	public static class LenL implements GaussPivoting<VarIntNumber, VarIntNumber[]> {
		private int pivCol;
		private int pivRow;
		private int pivLen = Integer.MAX_VALUE;
		public LenL(int pivot) {
			this.pivRow 	= pivot;
			this.pivCol 	= pivot;
		}
		public int checkCandidateRow(VarIntNumber[][] matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(VarIntNumber[][] matrix, int pivot, int row, int col, int rowResult) {
			final VarIntNumber val = matrix[row][col];
			final int len = val.abs().bitLength();
			if (len < pivLen) {
				pivCol = col;
				pivRow = row;
				pivLen = len;
			}
			//if we have a 1 or a -1, we can stop
			return pivLen != 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy uses bit length of the value. Note that the length 
	 * is an estimate for the logarithm of the big integer number. A pivot 
	 * element is chosen if the length is (L)ess than or (E)qual to the value of 
	 * a previous candidate.
	 * <p>
	 * Pivot, if <code>bitlen(value) <= previous</code>
	 */
	public static class LenLE implements GaussPivoting<VarIntNumber, VarIntNumber[]> {
		private int pivCol;
		private int pivRow;
		private int pivLen = Integer.MAX_VALUE;
		public LenLE(int pivot) {
			this.pivRow 	= pivot;
			this.pivCol 	= pivot;
		}
		public int checkCandidateRow(VarIntNumber[][] matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(VarIntNumber[][] matrix, int pivot, int row, int col, int rowResult) {
			final VarIntNumber val = matrix[row][col];
			final int len = val.abs().bitLength();
			if (len <= pivLen) {
				pivCol = col;
				pivRow = row;
				pivLen = len;
			}
			//if we have a 1 or a -1, we can stop
			return pivLen != 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}

	/**
	 * This pivoting strategy uses bit length of the value. Note that the length 
	 * is an estimate for the logarithm of the big integer number. A pivot 
	 * element is chosen if the product is (L)ess than the value of a previous 
	 * candidate. If it is (E)qual, the number of zeros of the row determines 
	 * the choice of the pivot element.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   bitlen(value)  < previous OR
	 *   bitlen(value) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class LenLorEandMoreRowZeros implements GaussPivoting<VarIntNumber, VarIntNumber[]> {
		private final NumberArrayOperations<VarIntNumber, VarIntNumber[]> numberOps;
		private int pivCol;
		private int pivRow;
		private int pivLen = Integer.MAX_VALUE;
		private int pivCnt = 0;
		public LenLorEandMoreRowZeros(NumberArrayOperations<VarIntNumber, VarIntNumber[]> numberOps, int pivot) {
			this.numberOps = numberOps;
			this.pivRow = pivot;
			this.pivCol = pivot;
		}
		public int checkCandidateRow(VarIntNumber[][] matrix, int pivot, int row) {
			final int cols = matrix[row].length;
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (isZero(numberOps, matrix[row][col])) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(VarIntNumber[][] matrix, int pivot, int row, int col, int zerCnt) {
			final VarIntNumber val = matrix[row][col];
			final int len = val.abs().bitLength();
			if (len < pivLen || len == pivLen && zerCnt > pivCnt) {
				pivCol = col;
				pivRow = row;
				pivLen = len;
				pivCnt = zerCnt;
			}
			//if we have a 1 or a -1, we can stop if we have the maximum 
			//possible number of zeros
			final int cols = matrix[row].length;
			return pivLen != 1 || pivCnt < cols - 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	private static boolean isZero(NumberArrayOperations<VarIntNumber, VarIntNumber[]> nops, VarIntNumber value) {
		return nops.getNumberOperators().booleanUnary(BooleanUnaryOperator.Id.isZero).booleanOperate(value);
	}
	
	//no instances
	private VarIntGaussPivoting() {}
}
