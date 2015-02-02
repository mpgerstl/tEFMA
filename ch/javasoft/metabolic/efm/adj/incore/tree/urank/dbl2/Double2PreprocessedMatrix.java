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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank.dbl2;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.RankUpdateRoot;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;
import ch.javasoft.metabolic.efm.rankup.RankUpRoot;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.numeric.Zero;

/**
 * 
 */
public class Double2PreprocessedMatrix implements PreprocessedMatrix {
	
	private final double[][]		matrix;
	private final int				rank;
	private final IBitSet	unusedBits;
	
	private double[][] matrixResidue;//the rows/cols > rank, or null

	/**
	 * Constructor for cloneing
	 */
	private Double2PreprocessedMatrix(Double2PreprocessedMatrix copyOf) {
		matrix			= deepClone(copyOf.matrix);
		matrixResidue	= copyOf.matrixResidue == null ? null : deepClone(copyOf.matrixResidue);
		rank			= copyOf.rank;
		unusedBits		= copyOf.unusedBits.clone();
	}
	/**
	 * Constructor for root matrix (has no parent)
	 */
	public Double2PreprocessedMatrix(PreprocessableMatrix owner, RankUpdateRoot root, DoubleMatrix stoich) {
		final int stopRank	= Math.min(stoich.getRowCount(), stoich.getColumnCount());
		final int[] colmap	= root.getColMapping();
		final IBitSet bits = BitSetUtil.factory().create(colmap.length);
		for (int i = 0; i < colmap.length; i++) bits.set(i);

		matrix			= toArray(stoich);
		matrixResidue	= null;
		rank			= computeRank(root.zero(), matrix, 0, stopRank, 0, colmap, bits, bits, owner.nodeCutSet);
		unusedBits		= bits;
		matrixResidue 	= initResidue(matrix, rank, unusedBits, colmap);
	}
	/**
	 * Constructor for child matrix (has parent)
	 */
	public Double2PreprocessedMatrix(PreprocessableMatrix owner, RankUpdateRoot root, Double2PreprocessedMatrix parentProcessed) {		
//		matrix 			= deepClone(parentProcessed.matrix);//more numerical problems if we do not clone here
		matrix 			= parentProcessed.matrix;
		unusedBits 		= parentProcessed.unusedBits.clone();

		final int stopRank 	= root.getStoichRank();
		final int[] colmap	= root.getColMapping();
		restoreResidue(matrix, parentProcessed.matrixResidue, parentProcessed.rank, unusedBits, colmap);
		rank = computeRank(root.zero(), matrix, 0, stopRank, parentProcessed.rank, colmap, unusedBits, unusedBits, owner.nodeCutSet);
		matrixResidue = initResidue(matrix, rank, unusedBits, colmap);
	}
	
	public boolean hasRequiredRank(RankUpRoot root, IBitSet intersectionSet) {
		final int reqRank 	= root.getRequiredRank() - intersectionSet.cardinality();
		final int[] colmap	= root.getColMapping();

		restoreResidue(matrix, matrixResidue, rank, unusedBits, colmap);
		return
			reqRank <= rank ||
//			reqRank <= rank + getRank(root.zero(), getRemainingMatrix(matrix, root.getStoichRank(), rank, colmap, unusedBits, intersectionSet), reqRank - rank);		
			reqRank <= computeRank(root.zero(), matrix, reqRank, reqRank, rank, colmap, unusedBits, null, intersectionSet);		
	}
	
	@Override
	public Double2PreprocessedMatrix clone() {
		return new Double2PreprocessedMatrix(this);
	}
	@Override
	public String toString() {
		return "preprocessed(" + rank + ")=" + matrix;
	}
	
	@SuppressWarnings("unused")
	private static double[][] getRemainingMatrix(double[][] mx, int stoichRank, int rank, int[] fixcolmap, IBitSet unusedBits, IBitSet setToCompute) {
//		final int rows = stoichRank - rank;
		final int rows = mx.length - rank;
		final int cols = fixcolmap.length;
		final int[] colmap = new int[cols];
		
//		trace("all:unused-old:       set", bits, unusedBits, setToCompute);

		//collect the newly usable columns		
		int colsAct = 0;//active columns, those which are used as pivots
		for (int bit = unusedBits.nextSetBit(0); bit >= 0; bit = unusedBits.nextSetBit(bit + 1)) {
			if (!setToCompute.get(bit)) {
				colmap[colsAct] = fixcolmap[bit];
				colsAct++;
			}
		}
		
    	//create array
    	double[][] arr = new double[rows][colsAct];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < colsAct; col++) {
				arr[row][col] = mx[row + rank][colmap[col]];
			}
		}
    	return arr;
	}

	/**
	 * 
	 * @param mx
	 * @param stopRank
	 * @param rank
	 * @param fixcolmap		
	 * @param setToCompute	the columns to use are the unset bits
	 */
	private static int computeRank(Zero zero, double[][] mx, int requireRank, int stopRank, int rank, int[] fixcolmap, IBitSet unusedBits, IBitSet unusedBitsOut, IBitSet setToCompute) {
		final int bits = fixcolmap.length;
		final int[] colmap = new int[bits];
		final int[] bitmap = new int[bits];
		
//		trace("all:unused-old:       set", bits, unusedBits, setToCompute);

		//collect the newly usable columns		
//		int colsPre = 0;//already used for rank computation in previous step
		int colsAct = rank;//active columns, those which are used as pivots
		int colsPas = bits;//passive columns, those which are used not as pivots but for row operations
		for (int bit = unusedBits.nextSetBit(0); bit >= 0; bit = unusedBits.nextSetBit(bit + 1)) {
			if (!setToCompute.get(bit)) {
				bitmap[colsAct] = bit;
				colmap[colsAct] = fixcolmap[bit];
				colsAct++;
			}
			else {
				colsPas--;
				colmap[colsPas] = fixcolmap[bit];
			}
		}
		
		if (requireRank > colsAct) return rank;
		
//		if (rank != colsPre) throw new RuntimeException("internal error, rank should be same as used: " + rank + "!=" + colsPre);
		if (colsPas != colsAct) throw new RuntimeException("internal error, colsPas should be same as colsAct: " + colsPas + "!=" + colsAct);
		final int colsTot = unusedBits == null ? colsAct : bits;
		
		//now, compute the new rank stuff (copied from Gauss() in common-util)
		final int rows = mx.length;
		final int pivs = Math.min(stopRank, Math.min(rows, colsAct));		
		
		for (int ipiv = rank; ipiv < pivs; ipiv++) {
			//find pivot row/column
			int prow	= ipiv;
			int picol 	= ipiv;
			double piv	= 0d;
			for (int row = ipiv; row < rows; row++) {
				//Pivot: abs max pivot
				for (int icol = ipiv; icol < colsAct; icol++) {
					final int col = colmap[icol];
					final double abs = Math.abs(mx[row][col]);
					if (abs > piv) {
						prow 	= row;
						picol 	= icol;
						piv		= abs;
					}
				}
			}
						
			if (zero.isZero(piv)) {
				trace("rank=" + ipiv + ":unused-new", bits, unusedBits);
				return ipiv;
			}			
			
			//swap rows / columns
			if (prow != ipiv) {
				Arrays.swap(mx, prow, ipiv);
			}
			if (picol != ipiv) {
				IntArray.swap(colmap, picol, ipiv);
				IntArray.swap(bitmap, picol, ipiv);
			}
			if (unusedBitsOut != null) {
				unusedBitsOut.clear(bitmap[ipiv]);
			}
			
			final int pivrow = ipiv;
			final int pivcol = colmap[ipiv];
			
			//divide pivot row
			IntArray pivCols = new IntArray();
			final double pval = 1d / mx[pivrow][pivcol];			
			for (int icol = ipiv + 1; icol < colsTot; icol++) {
				final int col = colmap[icol];
				if (zero.isNonZero(mx[pivrow][col])) {
					mx[pivrow][col] = mx[pivrow][col] * pval;
					pivCols.add(col);
				}
			}
			mx[pivrow][pivcol] = 1d;

			//subtract pivot row from other rows
			//find next pivot at the same time
			for (int row = ipiv + 1; row < rows; row++) {
				final double rpiv = mx[row][pivcol];
				mx[row][pivcol] = 0d;
				for (int icol = 0; icol < pivCols.length(); icol++) {
					final int col = pivCols.get(icol);
					final double sub = mx[pivrow][col];
					mx[row][col] -= sub*rpiv;
				}
//				for (int icol = ipiv + 1; icol < colsTot; icol++) {
//					final int col = colmap[icol];
//					final double sub = mx[pivrow][col];
//					mx[row][col] -= sub*rpiv;
//				}
			}
		}
//		trace("rank=" + pivs + ":unused-new", bits, unusedBits);
		return pivs;
	}
	
   @SuppressWarnings("unused")
   private static int getRank(Zero zero, double[][] arr, int reqRank) {		
    	final int rows		= arr.length;
    	final int cols		= rows == 0 ? 0 : arr[0].length;
    	
    	final int fullRank	= Math.min(rows, cols);
    	
    	if (reqRank > fullRank) return 0;

    	// find the first pivot
		double max = 0.0d;
		int pcol = -1, prow = -1;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final double absVal = Math.abs(arr[row][col]);
				if (absVal > max) {
					max		= absVal;
					pcol 	= col;
					prow 	= row;
				}
			}
		}
		// iterate pivots
		int pivot;
    	for (pivot = 0; pivot < reqRank; pivot++) {
			if (zero.isZero(max)) {
				return pivot;
			}
			if (prow != pivot) Arrays.swapRow(arr, prow, pivot);
			if (pcol != pivot) Arrays.swapCol(arr, pcol, pivot);
			
			//first, norm the pivot row
			final double pivotInv	= 1.0d / arr[pivot][pivot];
			arr[pivot][pivot]		= 1.0d;				
			for (int col = pivot + 1; col < cols; col++) {
				arr[pivot][col] *= pivotInv;				
			}
			
			//now, reduce the other metas and find the next pivot
			max = 0.0d;
			pcol = prow = -1;			
			for (int row = pivot + 1; row < rows; row++) {
				final double pivColVal	= arr[row][pivot]; 
				arr[row][pivot]			= 0.0d;
				for (int col = pivot + 1; col < cols; col++) {
					arr[row][col] -= pivColVal * arr[pivot][col];
					//find the next pivot at the same time
					final double absVal = Math.abs(arr[row][col]);
					if (absVal > max) {
						max = absVal;
						pcol = col;
						prow = row;
					}
				}					
			}
		}
		return pivot;					
   	}    	


	private static double[][] initResidue(double[][] matrix, int rank, IBitSet unusedBits, int[] fixcolmap) {
		final int rows = matrix.length;
		final int cols = rows == 0 ? 0 : matrix[0].length;
		final double[][] residue = new double[rows - rank][cols - rank];
		for (int i = 0; i < residue.length; i++) {
			int j = 0;
			for (int bit = unusedBits.nextSetBit(0); bit >= 0; bit = unusedBits.nextSetBit(bit + 1)) {
				residue[i][j] = matrix[rank + i][fixcolmap[bit]];
				j++;
			}
		}
		return residue;
	}
	
	private static void restoreResidue(double[][] matrix, double[][] residue, int rank, IBitSet unusedBits, int[] fixcolmap) {
		for (int i = 0; i < residue.length; i++) {
			int j = 0;
			for (int bit = unusedBits.nextSetBit(0); bit >= 0; bit = unusedBits.nextSetBit(bit + 1)) {
				matrix[rank + i][fixcolmap[bit]] = residue[i][j];
				j++;
			}
		}
	}

	private static double[][] deepClone(double[][] mx) {
		final double[][] clone = new double[mx.length][];
		for (int i = 0; i < clone.length; i++) {
			clone[i] = new double[mx[i].length];
//			for (int j = 0; j < clone[i].length; j++) {
//				clone[i][j] = mx[i][j];
//			}
			System.arraycopy(mx[i], 0, clone[i], 0, clone[i].length);
		}
		return clone;
	}
	private static double[][] toArray(DoubleMatrix stoich) {
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		final double[][] arr = new double[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				arr[row][col] = stoich.getDoubleValueAt(row, col);
			}
		}
		return arr;
	}
	private static void trace(String what, int len, IBitSet... sets) {
		//if (sets.length == 1) System.out.println(what.substring(0, what.indexOf(':')));		
		return;
//		traceAlways(what, len, sets);
	}
	@SuppressWarnings("unused")
	private static void traceAlways(String what, int len, IBitSet... sets) {
		String[] whats = what.split(":");
		int w = 0;
		if (whats.length > sets.length) {
			System.out.println(whats[0]);
			w = 1;
		}
		for (IBitSet set : sets) {
			System.out.print(whats[w++] + "=");
			for (int i = 0; i < len; i++) {
				System.out.print(set.get(i) ? '1' : '0');
			}
			System.out.println();
		}		
	}

}
