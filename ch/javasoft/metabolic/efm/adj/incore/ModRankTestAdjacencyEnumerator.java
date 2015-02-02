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
package ch.javasoft.metabolic.efm.adj.incore;

import java.io.IOException;
import java.util.Queue;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.lang.reflect.Array;
import ch.javasoft.math.Prime;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.metabolic.efm.util.MappingUtil;
import ch.javasoft.metabolic.efm.util.ModUtil;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.numeric.Zero;

public class ModRankTestAdjacencyEnumerator extends AbstractAdjacencyEnumerator implements RankAdjacencyEnumerator {

	public static final String NAME = "mod-rank";
	
	private static final int PRIME = Prime.getPrimeBelow((int)(Math.sqrt(Integer.MAX_VALUE / 2)));
	
	protected final boolean mIsMinCardinalityTested;

	private int[]				mColMapping;
	private int[][]				mStoichRed;	
	private IBitSet[] 	mRowsAnyInCol;
	private IBitSet[] 	mColsAnyInRow;

	public ModRankTestAdjacencyEnumerator() {
		this(false);
	}
	public ModRankTestAdjacencyEnumerator(boolean isMinCardinalityTested) {
		mIsMinCardinalityTested = isMinCardinalityTested;
	}
	public String name() {
		return NAME;
	}
	
	@Override
	public <Col extends Column, N extends Number> void initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel model) {
		super.initialize(columnHome, config, model);
    	int[] colMapping	= MappingUtil.getInitialMapping(model.getStoichRational().getColumnCount());
		mStoichRed			= getReducedStoichMatrix(config.zero(), model.getStoichRational(), colMapping);
		int[] newMapping	= new int[colMapping.length];
		for (int ii = 0; ii < newMapping.length; ii++) {
			newMapping[ii] = colMapping[model.getReactionSorting()[ii]];
		}
		mColMapping = newMapping;		

		final int rows = mStoichRed.length;
		final int cols = mStoichRed.length == 0 ? 0 : mStoichRed[0].length;
		try {
			mRowsAnyInCol = Array.newInstanceInstantiate(BitSetUtil.factory().getBitSetClass(), rows);
			mColsAnyInRow = Array.newInstanceInstantiate(BitSetUtil.factory().getBitSetClass(), cols);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				if (mConfig.zero().isNonZero(mStoichRed[row][col])) {
					mRowsAnyInCol[row].set(col);
					mColsAnyInRow[col].set(row);
				}
			}
		}
	}
	
	@Override
	public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException {
		for (final Column colP : posCols) {
			for (final Column colN : negCols) {
        		final ColumnPair pair = new ColumnPair<Column>(colP, colN);
        		final IBitSet and = pair.intersectBitValues();
        		if (hasRequiredRank(and)) {
        			adjacentPairs.add(pair);        			
        		}
        	}
        }
	}

    /**
     * Reduces the stoichiometric matrix to the basis of it, i.e. to a matrix with full rank.
     * 
     * @see Gauss#rowEchelon(BigIntegerRationalMatrix, boolean, int[], int[])
     * @see ModUtil#toIntArrayNoInversion(BigIntegerRationalMatrix, int, int)
     */
    private int[][] getReducedStoichMatrix(Zero zero, ReadableMatrix<? extends Number> stoich, int[] colMapping) {
    	final BigIntegerRationalMatrix mx = convertToBigIntegerRationalMatrix(stoich);
    	final int fullRank = new Gauss(0d).rowEchelon(mx, true, null, colMapping);    	
    	MappingUtil.invertMapping(colMapping);
		return ModUtil.toIntArrayNoInversion(mx, PRIME, fullRank);
   	}
	private static BigIntegerRationalMatrix convertToBigIntegerRationalMatrix(ReadableMatrix<? extends Number> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix<?>) {
			return ((ReadableBigIntegerRationalMatrix<?>)mx).toBigIntegerRationalMatrix(true /*enforceNewInstance*/);
		}
		else if (mx instanceof ReadableDoubleMatrix<?>) {
			final double[] values = ((ReadableDoubleMatrix<?>)mx).toDoubleArray();
			return new DefaultBigIntegerRationalMatrix(values, mx.getRowCount(), mx.getColumnCount(), false /*adjust values*/);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
    
	protected int[][] getRemainingMatrix(IBitSet zeroColumns, int[] rankSoFarPtr) {
		int stoichRows	= mStoichRed.length;
		int stoichCols	= stoichRows == 0 ? 0 : mStoichRed[0].length;
		
//		int zeroColCnt				= 0;
//    	int zeroColInIdentPartCnt	= 0;
		int		zeroCnt		= 0;
    	int[]	keptCols	= new int[stoichCols];
		int		keptColCnt	= 0;
    	int[]	keptRows	= new int[stoichRows];
		int		keptRowCnt	= 0;
    	for (int ii = 0; ii < stoichCols; ii++) {
			int col = mColMapping[ii];
			if (zeroColumns.get(ii)) {
				if (col < stoichRows) keptRows[keptRowCnt++] = col;
				zeroCnt++;
			}
			else keptCols[keptColCnt++] = col;
		}
    	
    	// prepare:
    	// 		- new metas have a 1, the rest is 0
    	//		- if a new row has its 1 at the identity matrix part of the preprocessed matrix, 
    	//				we mark it as "to be processed". this can be seen as if we would replace
    	//				this column in the preprocessed matrix by the new row, and clear the 1 
    	//				in the identity-part of the row. the new row (having that 1) is independant
    	//				for sure.
    	//		- if a new row has its 1 in the part behind the identity matrix of the preprocessed
    	//				row, it is independant (after clearing the correspoinding 1 in all metas to
    	//				be processed).
    	final int[][] arr = new int[keptRowCnt][keptColCnt];
    	int zeroCols	= 0;
    	int newCol		= 0;
    	for (int iCol = 0; iCol < keptColCnt; iCol++) {
    		int col		= keptCols[iCol];
			boolean any = false;
			for (int iRow = 0; iRow < keptRowCnt; iRow++) {
				int row = keptRows[iRow];
				int val = mStoichRed[row][col];
				arr[iRow][newCol] = val;
				any |= (val != 0);
			}
			if (any) newCol++;
			else zeroCols++;
		}    	
   		rankSoFarPtr[0] = stoichRows + zeroCnt - keptRowCnt;
    	if (zeroCols > 0) {
    		final int[][] zarr = new int[keptRowCnt][keptColCnt - zeroCols];
    		for (int i = 0; i < zarr.length; i++) {
				System.arraycopy(arr[i], 0, zarr[i], 0, zarr[i].length);
			}
    		return zarr;
    	}
    	return arr;
    }
	/**
	 * Number of zeros required, this is required rank minus rank of stoich.
	 * @return q - 2 - rank(N)
	 */
	public int getRequiredZeroBitCount() {
		return getRequiredRank() - mStoichRed.length;
	}
	/**
	 * Required rank, this is number of reactions minus 2
	 * @return {@code q - 2}
	 */
	public int getRequiredRank() {
		int rows = mStoichRed.length;
		if (rows == 0) return 0;
		return mStoichRed[0].length - 2;
	}
	
	public boolean hasRequiredZeroBitCount(IBitSet zeroColumns) {
		return zeroColumns.cardinality() >= getRequiredZeroBitCount();		
	}
	public boolean isRequiredZeroBitCount(int count) {
		return count >= getRequiredZeroBitCount();		
	}
	public boolean hasRequiredRank(IBitSet zeroColumns) {
		int reqRank = getRequiredRank();
		if (mIsMinCardinalityTested || zeroColumns.cardinality() >= getRequiredZeroBitCount()) {
			final int[] rankPtr = new int[1];
			final int[][] mx = getRemainingMatrix(zeroColumns, rankPtr);
	    	final int rankSoFar = rankPtr[0];
			return isRankGaussFullPivoting(mx, rankSoFar, reqRank);
		}
		return false;
	}
	protected boolean isRankGaussFullPivoting(final int[][] arr, int rankSoFar, int rank) {
		final int rows 		= arr.length;
		final int cols 		= rows == 0 ? 0 : arr[0].length;
		final int maxRank	= Math.min(rows, cols);
		
		for (int pivot = 0; pivot < maxRank; pivot++) {
			//find pivot row/column
			int prow	= pivot;
			int pcol 	= pivot;
			int pval	= 0;
			//first, just try this column, then all columns
			for (int row = pivot; row < rows && pval == 0; row++) {
				for (int col = pivot; col < cols && pval == 0; col++) {
					prow	= row;
                    pcol	= col; 
					pval	= arr[row][col];
				}
			}
						
			if (pval == 0) {
				return pivot + rankSoFar >= rank;
			}			
			
			//swap rows / columns
			if (prow != pivot) Arrays.swapRow(arr, prow, pivot);
			if (pcol != pivot) Arrays.swapCol(arr, pcol, pivot);
			
			//subtract pivot row from other rows
			final int[] pivrow = arr[pivot];
			for (int row = pivot + 1; row < rows; row++) {
				final int rpiv = arr[row][pivot];
				arr[row][pivot] = 0;
                for (int col = pivot + 1; col < cols; col++) {
					arr[row][col] = (arr[row][col] * pval - pivrow[col] * rpiv) % PRIME;
				}
			}
		}
		return maxRank + rankSoFar >= rank;
	}
	
}
