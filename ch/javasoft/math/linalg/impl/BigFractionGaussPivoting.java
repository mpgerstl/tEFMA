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
package ch.javasoft.math.linalg.impl;

import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.linalg.GaussPivoting;
import ch.javasoft.math.linalg.GaussPivotingFactory;
import ch.javasoft.math.operator.BooleanUnaryOperator;

/**
 * This class contains constants for factories returning instances of inner 
 * classes implementing {@link GaussPivoting pivoting strategies} for Gaussian 
 * elimination with {@link BigFraction} numbers.
 */
public class BigFractionGaussPivoting {

	/**
	 * Returns a {@link LenProductL} strategy instance.
	 * <p>
	 * Pivot, if <code>bitlen(numerator)*bitlen(denominator) < previous</code>
	 */
	public static GaussPivotingFactory<BigFraction, BigFraction[]> LEN_PRODUCT_L = new GaussPivotingFactory<BigFraction, BigFraction[]>() {
		public GaussPivoting<BigFraction,BigFraction[]> getGaussPivoting(NumberArrayOperations<BigFraction,BigFraction[]> numberOps, int pivot) {
			return new LenProductL(pivot);
		}
	};
	/**
	 * Returns a {@link LenProductLE} strategy instance.
	 * <p>
	 * Pivot, if <code>bitlen(numerator)*bitlen(denominator) <= previous</code>
	 */
	public static GaussPivotingFactory<BigFraction, BigFraction[]> LEN_PRODUCT_LE = new GaussPivotingFactory<BigFraction, BigFraction[]>() {
		public GaussPivoting<BigFraction,BigFraction[]> getGaussPivoting(NumberArrayOperations<BigFraction,BigFraction[]> numberOps, int pivot) {
			return new LenProductLE(pivot);
		}
	};
	/**
	 * Returns a {@link LenProductLorEandMoreRowZeros} strategy instance.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   bitlen(numerator)*bitlen(denominator)  < previous OR
	 *   bitlen(numerator)*bitlen(denominator) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static GaussPivotingFactory<BigFraction, BigFraction[]> LEN_PRODUCT_L_OR_E_AND_MORE_ROW_ZEROS = new GaussPivotingFactory<BigFraction, BigFraction[]>() {
		public GaussPivoting<BigFraction,BigFraction[]> getGaussPivoting(NumberArrayOperations<BigFraction,BigFraction[]> numberOps, int pivot) {
			return new LenProductLorEandMoreRowZeros(numberOps, pivot);
		}
	};
	/**
	 * Returns a {@link LenSumLorEandMoreRowZeros} strategy instance.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   bitlen(numerator)+bitlen(denominator)  < previous OR
	 *   bitlen(numerator)+bitlen(denominator) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static GaussPivotingFactory<BigFraction, BigFraction[]> LEN_SUM_L_OR_E_AND_MORE_ROW_ZEROS = new GaussPivotingFactory<BigFraction, BigFraction[]>() {
		public GaussPivoting<BigFraction,BigFraction[]> getGaussPivoting(NumberArrayOperations<BigFraction,BigFraction[]> numberOps, int pivot) {
			return new LenSumLorEandMoreRowZeros(numberOps, pivot);
		}
	};
	/**
	 * Returns a {@link MaxLenLorEandMoreRowZeros} strategy instance.
	 * <p>
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   max(bitlen(numerator), bitlen(denominator))  < previous OR
	 *   max(bitlen(numerator), bitlen(denominator)) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static GaussPivotingFactory<BigFraction, BigFraction[]> MAX_LEN_L_OR_E_AND_MORE_ROW_ZEROS = new GaussPivotingFactory<BigFraction, BigFraction[]>() {
		public GaussPivoting<BigFraction,BigFraction[]> getGaussPivoting(NumberArrayOperations<BigFraction,BigFraction[]> numberOps, int pivot) {
			return new MaxLenLorEandMoreRowZeros(numberOps, pivot);
		}
	};
	
	/**
	 * This pivoting strategy uses the product of the bit lengths of 
	 * numerator and denominator. Note that the length is an estimate for the 
	 * logarithm of the big integer numbers. A pivot element is chosen if the 
	 * product is (L)ess than the value of a previous candidate.
	 * <p>
	 * Pivot, if <code>bitlen(numerator)*bitlen(denominator) < previous</code>
	 */
	public static class LenProductL implements GaussPivoting<BigFraction, BigFraction[]> {
		private int pivCol;
		private int pivRow;
		private int pivPro = Integer.MAX_VALUE;
		public LenProductL(int pivot) {
			this.pivRow 	= pivot;
			this.pivCol 	= pivot;
		}
		public int checkCandidateRow(BigFraction[][] matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(BigFraction[][] matrix, int pivot, int row, int col, int rowResult) {
			final BigInteger num = matrix[row][col].getNumerator();
			final BigInteger den = matrix[row][col].getDenominator();
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
	 * This pivoting strategy for which uses the product of the bit lengths of 
	 * numerator and denominator. Note that the length is an estimate for the 
	 * logarithm of the big integer numbers. A pivot element is chosen if the 
	 * product is (L)ess than or (E)qual to the value of a previous candidate.
	 * <p>
	 * Pivot, if <code>bitlen(numerator)*bitlen(denominator) <= previous</code>
	 */
	public static class LenProductLE implements GaussPivoting<BigFraction, BigFraction[]> {
		private int pivCol;
		private int pivRow;
		private int pivPro = Integer.MAX_VALUE;
		public LenProductLE(int pivot) {
			pivRow = pivot;
			pivCol = pivot;
		}
		public int checkCandidateRow(BigFraction[][] matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(BigFraction[][] matrix, int pivot, int row, int col, int rowResult) {
			final BigInteger num = matrix[row][col].getNumerator();
			final BigInteger den = matrix[row][col].getDenominator();
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
	 * This pivoting strategy uses the product of the bit lengths of 
	 * numerator and denominator. Note that the length is an estimate for the 
	 * logarithm of the big integer numbers. A pivot element is chosen if the 
	 * product is (L)ess than the value of a previous candidate. If it is 
	 * (E)qual, the number of zeros of the row determines the choice of the 
	 * pivot element.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   bitlen(numerator)*bitlen(denominator)  < previous OR
	 *   bitlen(numerator)*bitlen(denominator) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class LenProductLorEandMoreRowZeros implements GaussPivoting<BigFraction, BigFraction[]> {
		private final NumberArrayOperations<BigFraction, BigFraction[]> numberOps;
		private int pivCol;
		private int pivRow;
		private int pivPro = Integer.MAX_VALUE;
		private int pivCnt = 0;
		public LenProductLorEandMoreRowZeros(NumberArrayOperations<BigFraction, BigFraction[]> numberOps, int pivot) {
			this.numberOps = numberOps;
			this.pivRow = pivot;
			this.pivCol = pivot;
		}
		public int checkCandidateRow(BigFraction[][] matrix, int pivot, int row) {
			final int cols = matrix[row].length;
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (isZero(numberOps, matrix[row][col])) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(BigFraction[][] matrix, int pivot, int row, int col, int zerCnt) {
			final BigInteger num = matrix[row][col].getNumerator();
			final BigInteger den = matrix[row][col].getDenominator();
			final int pro = num.abs().bitLength() * den.abs().bitLength();
			if (pro < pivPro || pro == pivPro && zerCnt > pivCnt) {
				pivCol = col;
				pivRow = row;
				pivPro = pro;
				pivCnt = zerCnt;
			}
			//if we have a 1 or a -1, we can stop if we have the maximum 
			//possible number of zeros
			final int cols = matrix[row].length;
			return pivPro != 1 || pivCnt < cols - 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy for which uses the sum of the bit lengths of 
	 * numerator and denominator. Note that the length is an estimate for the 
	 * logarithm of the big integer numbers. A pivot element is chosen if the 
	 * product is (L)ess than the value of a previous candidate. If it is 
	 * (E)qual, the number of zeros of the row determines the choice of the 
	 * pivot element.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   bitlen(numerator)+bitlen(denominator)  < previous OR
	 *   bitlen(numerator)+bitlen(denominator) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class LenSumLorEandMoreRowZeros implements GaussPivoting<BigFraction, BigFraction[]> {
		private final NumberArrayOperations<BigFraction, BigFraction[]> numberOps;
		private int pivCol;
		private int pivRow;
		private int pivSum = Integer.MAX_VALUE;
		private int pivCnt = 0;
		public LenSumLorEandMoreRowZeros(NumberArrayOperations<BigFraction, BigFraction[]> numberOps, int pivot) {
			this.numberOps = numberOps;
			this.pivRow = pivot;
			this.pivCol = pivot;
		}
		public int checkCandidateRow(BigFraction[][] matrix, int pivot, int row) {
			final int cols = matrix[row].length;
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (isZero(numberOps, matrix[row][col])) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(BigFraction[][] matrix, int pivot, int row, int col, int zerCnt) {
			final BigInteger num = matrix[row][col].getNumerator();
			final BigInteger den = matrix[row][col].getDenominator();
			final int sum = num.abs().bitLength() + den.abs().bitLength();
			if (sum < pivSum || sum == pivSum && zerCnt > pivCnt) {
				pivCol = col;
				pivRow = row;
				pivSum = sum;
				pivCnt = zerCnt;
			}
			//if we have a 1 or a -1, we can stop if we have the maximum 
			//possible number of zeros
			final int cols = matrix[row].length;
			return pivSum != 2 || pivCnt < cols - 1;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	/**
	 * This pivoting strategy for which uses the maximum of the bit lengths of 
	 * numerator and denominator. Note that the length is an estimate for the 
	 * logarithm of the big integer numbers. A pivot element is chosen if the 
	 * product is (L)ess than the value of a previous candidate. If it is equal, 
	 * the number of zeros of the row determines the choice of the pivot 
	 * element.
	 * <p>
	 * Pivot, if 
	 * <code>
	 *   max(bitlen(numerator), bitlen(denominator))  < previous OR
	 *   max(bitlen(numerator), bitlen(denominator)) == previous AND #rowzeros > previous
	 * </code>
	 */
	public static class MaxLenLorEandMoreRowZeros implements GaussPivoting<BigFraction, BigFraction[]> {
		private final NumberArrayOperations<BigFraction, BigFraction[]> numberOps;
		private int pivCol;
		private int pivRow;
		private int pivLen = Integer.MAX_VALUE;
		private int pivCnt = 0;
		public MaxLenLorEandMoreRowZeros(NumberArrayOperations<BigFraction, BigFraction[]> numberOps, int pivot) {
			this.numberOps = numberOps;
			this.pivRow = pivot;
			this.pivCol = pivot;
		}
		public int checkCandidateRow(BigFraction[][] matrix, int pivot, int row) {
			final int cols = matrix[row].length;
			int zerCnt = 0;
			for (int col = pivot; col < cols; col++) {
				if (isZero(numberOps, matrix[row][col])) zerCnt++;
			}		
			return zerCnt;
		}
		public boolean checkCandidateCol(BigFraction[][] matrix, int pivot, int row, int col, int zerCnt) {
			final BigInteger num = matrix[row][col].getNumerator();
			final BigInteger den = matrix[row][col].getDenominator();
			final int len = Math.max(num.abs().bitLength(), den.abs().bitLength());
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
	
	private static boolean isZero(NumberArrayOperations<BigFraction, BigFraction[]> nops, BigFraction value) {
		return nops.getNumberOperators().booleanUnary(BooleanUnaryOperator.Id.isZero).booleanOperate(value);
	}
	
	//no instances
	private BigFractionGaussPivoting() {}
}
