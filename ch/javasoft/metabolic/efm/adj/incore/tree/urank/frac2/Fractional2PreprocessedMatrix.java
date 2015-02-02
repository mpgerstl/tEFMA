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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank.frac2;

import java.math.BigInteger;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.RankUpdateRoot;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;
import ch.javasoft.metabolic.efm.rankup.RankUpRoot;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.IntArray;

/**
 * 
 */
public class Fractional2PreprocessedMatrix implements PreprocessedMatrix {
	
	private final BigFraction[][]		matrix;
	private final int						rank;
	private final IBitSet			unusedBits;
	
	private BigFraction[][]			matrixResidue;//the rows/cols > rank, or null

	/**
	 * Constructor for cloneing
	 */
	public Fractional2PreprocessedMatrix(Fractional2PreprocessedMatrix copyOf) {
		matrix			= deepClone(copyOf.matrix);
		matrixResidue	= copyOf.matrixResidue == null ? null : deepClone(copyOf.matrixResidue.clone());
		rank			= copyOf.rank;
		unusedBits		= copyOf.unusedBits.clone();		
	}
	/**
	 * Constructor for root matrix (has no parent)
	 */
	public Fractional2PreprocessedMatrix(PreprocessableMatrix owner, RankUpdateRoot root) {
		final int stoichRank 	= root.getStoichRank();
		final int[] colmap		= root.getColMapping();
		final IBitSet bits = BitSetUtil.factory().create(colmap.length);
		for (int i = 0; i < colmap.length; i++) bits.set(i);

		matrix			= toArray(root.getStoichRational());
		rank			= computeRank(matrix, stoichRank, stoichRank, 0, colmap, bits, bits, owner.nodeCutSet);
		unusedBits		= bits;
		matrixResidue	= initResidue(matrix, rank, unusedBits, colmap);
	}
	/**
	 * Constructor for child matrix (has parent)
	 */
	public Fractional2PreprocessedMatrix(PreprocessableMatrix owner, RankUpdateRoot root, Fractional2PreprocessedMatrix parentProcessed) {		
		matrix 			= parentProcessed.matrix;
		unusedBits 		= parentProcessed.unusedBits.clone();

		final int stoichRank 	= root.getStoichRank();
		final int[] colmap		= root.getColMapping();
		restoreResidue(matrix, parentProcessed.matrixResidue, parentProcessed.rank, unusedBits, colmap);
		rank = computeRank(matrix, stoichRank, stoichRank, parentProcessed.rank, colmap, unusedBits, unusedBits, owner.nodeCutSet);
		matrixResidue = initResidue(matrix, rank, unusedBits, colmap);
	}
	
	public boolean hasRequiredRank(RankUpRoot root, IBitSet intersectionSet) {
		final int stoichRank 	= root.getStoichRank();
		final int reqRank 		= root.getRequiredRank() - intersectionSet.cardinality();
		final int[] colmap		= root.getColMapping();
		
		restoreResidue(matrix, matrixResidue, rank, unusedBits, colmap);
		return
			reqRank <= rank ||
			reqRank <= computeRank(matrix, stoichRank, reqRank, rank, colmap, unusedBits, null, intersectionSet);		
	}
		
	@Override
	public String toString() {
		return "preprocessed(" + rank + ")=" + matrix;
	}

	@Override
	public Fractional2PreprocessedMatrix clone() {
		return new Fractional2PreprocessedMatrix(this);
	}
	/**
	 * 
	 * @param mx
	 * @param stopRank
	 * @param rank
	 * @param setToCompute	the columns to use are the unset bits
	 */
	private static int computeRank(BigFraction[][] mx, int stoichRank, int stopRank, int rank, int[] fixcolmap, IBitSet unusedBits, IBitSet unusedBitsOut, IBitSet setToCompute) {
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
//		if (rank != colsPre) throw new RuntimeException("internal error, rank should be same as used: " + rank + "!=" + colsPre);
		if (colsPas != colsAct) throw new RuntimeException("internal error, colsPas should be same as colsAct: " + colsPas + "!=" + colsAct);
		final int colsTot = bits;
		
		//now, compute the new rank stuff (copied from Gauss() in common-util)
		final int rows = mx.length;
		final int pivs = Math.min(stopRank, Math.min(rows, colsAct));		
		
		for (int ipiv = rank; ipiv < pivs; ipiv++) {
			//find pivot row/column
			int prow	= ipiv;
			int picol 	= ipiv;
			int plen 	= Integer.MAX_VALUE;
			int zlen 	= 0;
			for (int row = ipiv; row < rows; row++) {
				int zerCnt = 0;
				for (int icol = ipiv; icol < colsAct; icol++) {
					if (mx[row][colmap[icol]].signum() == 0) zerCnt++;
				}				
				//Pivot: smallest sum of bit lengths of numerator/denominator
				//       or, if equal, the one with more zeros in the row
				//Aim:   we want pivots around 1, which do not blow up the 
				//       fraction numbers on multiplying/dividing by them
				for (int icol = ipiv; icol < colsAct; icol++) {
					final int col = colmap[icol];
					if (0 != mx[row][col].signum()) {
//						if (ipiv == 0) mx.cancelValueAt(row, col);
						BigInteger num = mx[row][col].getNumerator();
						BigInteger den = mx[row][col].getDenominator();
						final int len = num.abs().bitLength() + den.abs().bitLength();
						if (len < plen || (len == plen && zerCnt > zlen)) {
							prow 	= row;
							picol 	= icol;
							plen 	= len;
							zlen 	= zerCnt;
						}
					}
				}

			}
						
			if (mx[prow][colmap[picol]].signum() == 0) {
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
			BigFraction pval = mx[pivrow][pivcol];		
			for (int icol = ipiv + 1; icol < colsTot; icol++) {
				final int col = colmap[icol]; 
				if (mx[pivrow][col].isNonZero()) {
					mx[pivrow][col] = mx[pivrow][col].divide(pval).reduce();
					pivCols.add(col);
				}
			}
			mx[pivrow][pivcol] = BigFraction.ONE;
			
			//subtract pivot row from other rows
			//find next pivot at the same time
			for (int row = ipiv + 1; row < rows; row++) {
				BigFraction rpiv = mx[row][pivcol];
				mx[row][pivcol] = BigFraction.ZERO;
				for (int icol = 0; icol < pivCols.length(); icol++) {
					final int col = pivCols.get(icol);
					BigFraction val = mx[row][col];
					BigFraction sub = mx[pivrow][col];
					val = val.subtract(sub.multiply(rpiv));
					mx[row][col] = val.reduce();
				}
//				for (int icol = ipiv + 1; icol < colsTot; icol++) {
//					final int col = colmap[icol];
//					BigFraction val = mx[row][col];
//					BigFraction sub = mx[pivrow][col];
//					val = val.subtract(sub.multiply(rpiv));
//					mx[row][col] = val.cancel();
//				}
			}
		}
//		trace("rank=" + pivs + ":unused-new", bits, unusedBits);
		return pivs;
	}
	
	private static BigFraction[][] initResidue(BigFraction[][] matrix, int rank, IBitSet unusedBits, int[] fixcolmap) {
		final int rows = matrix.length;
		final int cols = rows == 0 ? 0 : matrix[0].length;
		final BigFraction[][] residue = new BigFraction[rows - rank][cols - rank];
		for (int i = 0; i < residue.length; i++) {
			int j = 0;
			for (int bit = unusedBits.nextSetBit(0); bit >= 0; bit = unusedBits.nextSetBit(bit + 1)) {
				residue[i][j] = matrix[rank + i][fixcolmap[bit]];
				j++;
			}
		}
		return residue;
	}
	
	private static void restoreResidue(BigFraction[][] matrix, BigFraction[][] residue, int rank, IBitSet unusedBits, int[] fixcolmap) {
		for (int i = 0; i < residue.length; i++) {
			int j = 0;
			for (int bit = unusedBits.nextSetBit(0); bit >= 0; bit = unusedBits.nextSetBit(bit + 1)) {
				matrix[rank + i][fixcolmap[bit]] = residue[i][j];
				j++;
			}
		}
	}

	private static BigFraction[][] toArray(ReadableMatrix<BigFraction> stoich) {
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		final BigFraction[][] arr = new BigFraction[rows][cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				arr[row][col] = stoich.getNumberValueAt(row, col);
			}
		}
		return arr;
	}
	private static BigFraction[][] deepClone(BigFraction[][] mx) {
		final BigFraction[][] clone = mx.clone();
		for (int i = 0; i < clone.length; i++) {
			clone[i] = clone[i].clone();
		}
		return clone;
	}
	private static void trace(String what, int len, IBitSet... sets) {
		//if (sets.length == 1) System.out.println(what.substring(0, what.indexOf(':')));	
		if (true) return;
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
