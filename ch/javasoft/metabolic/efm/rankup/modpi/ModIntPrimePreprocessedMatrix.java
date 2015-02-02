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
package ch.javasoft.metabolic.efm.rankup.modpi;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.math.Prime;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;
import ch.javasoft.metabolic.efm.rankup.RankUpRoot;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.metabolic.efm.util.ModUtil;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.IntArray;

/**
 * 
 */
public class ModIntPrimePreprocessedMatrix implements PreprocessedMatrix {
	
	private static final int[] PRIMES = new int[] {
		//largest prime, which is squared still a positive integer.
		//this allows for multiplication within our bounds.
		//since we subtract, the result could be double, thus we take
		//the sqrt of half(max int).
		Prime.getPrimeBelow((int)(Math.sqrt(Integer.MAX_VALUE / 2)))
		//works for colinature(All): 		17, 23, 29, 31, 37, 41, 43, 47, 53
		//									(3, 19), (7, 11), (7, 13), (7, 19),
		//									(11, 13), (11, 19), (13, 19)
		//									
		//not works for colinature(All): 	3, 5, 7, 11, 13, 19,
		//									(3, 5), (3, 7), (5, 7), (3, 11), 
		//									(3, 13), (5, 11), (5, 13), (5, 19), 		 
		//									(3, 5, 7), (3, 5, 11), (3, 5, 13)
		
		//works for colinature(Super):		59, 61, 79, 89, 97, 107
		//not works for colinature(Super):	23, 29, 31, 37, 41, 43, 47, 53, 67, 
		//									71, 73, 83, 101, 103
	};
	
    private static class Data {
        public Data(int[][] matrix, int rank, IBitSet unusedBits) {
            this.matrix     = matrix;
            this.rank       = rank;
            this.unusedBits = unusedBits;
        }
        final int[][]			matrix;
        final int				rank;
        final IBitSet	unusedBits;
        @Override
		public Data clone() {
            return new Data(IntArray.clone(matrix), rank, unusedBits.clone());
        }
    }
    
    private final Data[] data;
	
	/**
	 * Constructor for cloning
	 */
	public ModIntPrimePreprocessedMatrix(ModIntPrimePreprocessedMatrix copyOf) {
        data = new Data[copyOf.data.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = copyOf.data[i].clone();
        }
	}
	/**
	 * Constructor for root matrix (has no parent)
	 */
	public ModIntPrimePreprocessedMatrix(PreprocessableMatrix owner, RankUpRoot root) {
		//final int stoichRank 	= root.getStoichRank();
		final int[] colmap		= root.getColMapping();
		final IBitSet bits = BitSetUtil.factory().create(colmap.length);
		for (int i = 0; i < colmap.length; i++) bits.set(i);

        data = new Data[PRIMES.length];
        for (int i = 0; i < data.length; i++) {
            final IBitSet unused = bits.clone();
            final int[][] matrix    = ModUtil.toIntArrayNoInversion(root.getStoichRational(), PRIMES[i]);
            final int rank          = computeRank(matrix, i, 0, colmap, unused, unused, owner.nodeCutSet);
            data[i] = new Data(matrix, rank, unused);
        }
	}
	/**
	 * Constructor for child matrix (has parent)
	 */
	public ModIntPrimePreprocessedMatrix(PreprocessableMatrix owner, RankUpRoot root, ModIntPrimePreprocessedMatrix parentProcessed) {		
        data = new Data[PRIMES.length];
        for (int i = 0; i < data.length; i++) {
            final IBitSet unused = parentProcessed.data[i].unusedBits.clone();
            final int[][] matrix    = parentProcessed.data[i].matrix;
            //final int stoichRank    = root.getStoichRank();
            final int[] colmap      = root.getColMapping();
            final int rank = computeRank(matrix, i, parentProcessed.data[i].rank, colmap, unused, unused, owner.nodeCutSet);
            data[i] = new Data(matrix, rank, unused);
        }
	}
	
	public boolean hasRequiredRank(RankUpRoot root, IBitSet intersectionSet) {
		//final int stoichRank 	= root.getStoichRank();
		final int reqRank 		= root.getRequiredRank() - intersectionSet.cardinality();
		final int[] colmap		= root.getColMapping();
		return
			reqRank <= getMaxRank() ||
			reqRank <= computeRank(reqRank, colmap, intersectionSet);
	}
    
    public int getMaxRank() {
        int rank = 0;
        for (int i = 0; i < data.length; i++) {
            rank = Math.max(rank, data[i].rank);
        }
        return rank;
    }
    @Override
    public String toString() {
        return "preprocessed(" + getMaxRank() + ")";
    }
	
	@Override
	public ModIntPrimePreprocessedMatrix clone() {
		return new ModIntPrimePreprocessedMatrix(this);
	}
	/**
	 * @param setToCompute	the columns to use are the unset bits
	 */
	private int computeRank(int stopRank, int[] fixcolmap, IBitSet setToCompute) {
		int maxRank = 0;
		for (int i = 0; i < data.length; i++) {
			final int cur = computeRank(data[i].matrix, i, data[i].rank, fixcolmap, data[i].unusedBits, null, setToCompute);
			if (stopRank <= cur) return cur;
            maxRank = Math.max(maxRank, cur);
		}
		return maxRank;
	}
	/**
	 * @param setToCompute	the columns to use are the unset bits
	 */
	private static int computeRank(int[][] mx, int which, int rank, int[] fixcolmap, IBitSet unusedBits, IBitSet unusedBitsOut, IBitSet setToCompute) {
		final int iprime = PRIMES[which];
		
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
		final int pivs = Math.min(rows, colsAct);		
		
		for (int ipiv = rank; ipiv < pivs; ipiv++) {
			//find pivot row/column
			int prow	= ipiv;
			int picol 	= ipiv;
			int pval	= 0;
			//first, just try this column, then all columns
			for (int icol = ipiv; icol < colsAct && pval == 0; icol++) {
				for (int row = ipiv; row < rows && pval == 0; row++) {
					final int col = colmap[icol];
					prow   = row;
                    picol  = icol; 
					pval   = mx[row][col];
				}
			}
						
			if (pval == 0) {
				return ipiv;
			}			
			
			//swap rows / columns
			if (prow != ipiv) {
				Arrays.swap(mx, prow, ipiv);
			}
			if (picol != ipiv) {
//				colsAct--;
//				if (Math.min(pivs, colsAct) < reqRank) return ipiv;
//				IntArray.swap(colmap, picol, colsAct);
//				IntArray.swap(bitmap, picol, colsAct);				
//				IntArray.swap(colmap, colsAct, ipiv);
//				IntArray.swap(bitmap, colsAct, ipiv);
				IntArray.swap(colmap, picol, ipiv);
				IntArray.swap(bitmap, picol, ipiv);
			}
			if (unusedBitsOut != null) {
				unusedBitsOut.clear(bitmap[ipiv]);
			}
			
			final int pivrow = ipiv;
			final int pivcol = colmap[ipiv];
			
			//subtract pivot row from other rows
			final int[] prowvals = mx[pivrow];
			for (int row = ipiv + 1; row < rows; row++) {
				final int rpiv = mx[row][pivcol];
				if (rpiv != 0) {
					mx[row][pivcol] = 0;
	                for (int icol = ipiv + 1; icol < colsTot; icol++) {
	                    final int col = colmap[icol];
	
//	                    final int old = mx[row][col];
//						final int val = (old * pval - prowvals[col] * rpiv) % iprime;
//						mx[row][col] = val;
	                    mx[row][col] *= pval;
	                    mx[row][col] -= prowvals[col] * rpiv;
	                    mx[row][col] %= iprime;
	                    
	//                    final int sub  = mx[pivrow][col];
	//                    mx[row][col]   = (mx[row][col] * pval) % iprime;
	//                    mx[row][col]   = (mx[row][col] - ((sub * rpiv) % iprime)) % iprime;
					}
				}
			}
		}
//		trace("rank=" + pivs + ":unused-new", bits, unusedBits);
		return pivs;
	}

}
