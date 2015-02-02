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

import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.linalg.GaussPivoting;
import ch.javasoft.math.linalg.GaussPivotingFactory;

/**
 * This class contains constants for factories returning instances of inner 
 * classes implementing {@link GaussPivoting pivoting strategies} for Gaussian 
 * elimination with {@link Double} numbers.
 */
public class DoubleGaussPivoting {

	/**
	 * Returns a {@link AbsG} strategy instance.
	 * <p>
	 * Pivot, if <code>abs(value) > previous</code>
	 */
	public static GaussPivotingFactory<Double, double[]> ABS_G = new GaussPivotingFactory<Double, double[]>() {
		public GaussPivoting<Double, double[]> getGaussPivoting(NumberArrayOperations<Double, double[]> numberOps, int pivot) {
			return new AbsG(pivot);
		}
	};
	/**
	 * Returns a {@link AbsGE} strategy instance.
	 * <p>
	 * Pivot, if <code>abs(value) >= previous</code>
	 */
	public static GaussPivotingFactory<Double, double[]> ABS_GE = new GaussPivotingFactory<Double, double[]>() {
		public GaussPivoting<Double, double[]> getGaussPivoting(NumberArrayOperations<Double, double[]> numberOps, int pivot) {
			return new AbsGE(pivot);
		}
	};
	
	/**
	 * This pivoting strategy uses the largest absolute value as pivot. A new
	 * value is selected as pivot only if it is (G)reater then a previous 
	 * candidate.
	 * <p>
	 * Pivot, if <code>abs(value) > previous</code>
	 */
	public static class AbsG implements GaussPivoting<Double, double[]> {
		private int pivCol;
		private int pivRow;
		private double pivValAbs = 0;
		public AbsG(int pivot) {
			this.pivRow 	= pivot;
			this.pivCol 	= pivot;
		}
		public int checkCandidateRow(double[][] matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(double[][] matrix, int pivot, int row, int col, int rowResult) {
			final double abs = Math.abs(matrix[row][col]); 
			if (abs > pivValAbs) {
				pivCol 		= col;
				pivRow 		= row;
				pivValAbs	= abs;
			}
			//always continue searching
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
	 * This pivoting strategy uses the largest absolute value as pivot. A new
	 * value is selected as pivot only if it is (G)reater than or (E)qual to a 
	 * previous candidate.
	 * <p>
	 * Pivot, if <code>abs(value) >= previous</code>
	 */
	public static class AbsGE implements GaussPivoting<Double, double[]> {
		private int pivCol;
		private int pivRow;
		private double pivValAbs = 0;
		public AbsGE(int pivot) {
			this.pivRow 	= pivot;
			this.pivCol 	= pivot;
		}
		public int checkCandidateRow(double[][] matrix, int pivot, int row) {
			//nothing to do
			return 0;
		}
		public boolean checkCandidateCol(double[][] matrix, int pivot, int row, int col, int rowResult) {
			final double abs = Math.abs(matrix[row][col]); 
			if (abs >= pivValAbs) {
				pivCol 		= col;
				pivRow 		= row;
				pivValAbs	= abs;
			}
			//always continue searching
			return true;
		}
		public int getPivotCol() {
			return pivCol;
		}
		public int getPivotRow() {
			return pivRow;
		}
	}
	
	//no instances
	private DoubleGaussPivoting() {}
}
