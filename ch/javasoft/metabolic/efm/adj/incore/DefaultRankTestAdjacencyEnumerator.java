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
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.numeric.Zero;

public class DefaultRankTestAdjacencyEnumerator extends AbstractAdjacencyEnumerator implements RankAdjacencyEnumerator {

	public static final String NAME = "rank";
	
	protected final boolean mIsMinCardinalityTested;

	protected int[]				mColMapping;
	protected double[][]		mStoichRed;	
	
	public DefaultRankTestAdjacencyEnumerator() {
		this(false);
	}
	public DefaultRankTestAdjacencyEnumerator(boolean isMinCardinalityTested) {
		mIsMinCardinalityTested = isMinCardinalityTested;
	}
	
	public String name() {
		return NAME;
	}

	@Override
	public <Col extends Column, N extends Number> void initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel model) {
		super.initialize(columnHome, config, model);
    	int[] colMapping	= getInitialColMapping(model.getStoichRational().getColumnCount());
		mStoichRed			= getReducedStoichMatrix(config.zero(), model.getStoichRational(), colMapping);
		int[] newMapping	= new int[colMapping.length];
		for (int ii = 0; ii < newMapping.length; ii++) {
			newMapping[ii] = colMapping[model.getReactionSorting()[ii]];
		}
		mColMapping = newMapping;
		mModel		= model;
	}
	
	@Override
	public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException {
		for (final Column colP : posCols) {
			for (final Column colN : negCols) {
        		final ColumnPair pair = new ColumnPair<Column>(colP, colN);
        		final IBitSet and = pair.intersectBitValues();
        		if (hasRequiredRank(null, and)) {
        			adjacentPairs.add(pair);        			
        		}
        	}
        }
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
		if (mIsMinCardinalityTested) {
			int[] rankPtr = new int[1];
			double[][] mx = getRemainingMatrix(zeroColumns, rankPtr);
	    	int rankSoFar = rankPtr[0];
			boolean hasRank = isRankGaussFullPivoting(mx, rankSoFar, reqRank);
			return hasRank;
		}
		else {
			if (zeroColumns.cardinality() >= getRequiredZeroBitCount()) {
				int[] rankPtr = new int[1];
				double[][] mx = getRemainingMatrix(zeroColumns, rankPtr);
		    	int rankSoFar = rankPtr[0];
				boolean hasRank = isRankGaussFullPivoting(mx, rankSoFar, reqRank);
				return hasRank;
			}						
			return false;
		}
	}
	public boolean hasRequiredRank(final long[] timers, IBitSet zeroColumns) {
		int reqRank = getRequiredRank();
		if (reqRank > 0) {
			long t1 = 0L;
			long t2 = 0L;
			if (timers != null) t1 = System.currentTimeMillis();
			if (timers != null) t2 = System.currentTimeMillis();
			if (timers != null) timers[0] += t2 - t1;
			if (zeroColumns.cardinality() >= getRequiredZeroBitCount()) {
				int[] rankPtr = new int[1];
				double[][] mx = getRemainingMatrix(zeroColumns, rankPtr);
		    	int rankSoFar = rankPtr[0];
				if (timers != null) t1 = System.currentTimeMillis();
				if (timers != null) timers[1] += t1 - t2;
				boolean hasRank = isRankGaussFullPivoting(mx, rankSoFar, reqRank);
				if (timers != null) t2 = System.currentTimeMillis();
				if (timers != null) timers[2] += t2 - t1;
				return hasRank;
			}
			return false;
		}
		return true;
	}
    
    /**
     * Reduces the stoichiometric matrix to the basis of it, i.e. to a matrix with full rank.
     * @see #reduce(Zero, DoubleMatrix, int[])
     */
    private double[][] getReducedStoichMatrix(Zero zero, ReadableMatrix<? extends Number> stoich, int[] colMapping) {
    	int rows = stoich.getRowCount();
    	int cols = stoich.getColumnCount();
    	DoubleMatrix mx = new DefaultDoubleMatrix(rows, cols);
    	for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
				double val = stoich.getNumberValueAt(row, col).doubleValue();
				mx.setValueAt(row, col, val);
			}
		}
    	return reduce(zero, mx, colMapping);
    }
    
    private static double[][] subMatrixToArray(DoubleMatrix mx, int rows) {
    	int cols = mx.getColumnCount();
    	double[][] vals = new double[rows][cols];
    	/*
    	This is the end of the main loop over columns of the reduction. It only remains to unscramble
    	the solution in view of the column interchanges. We do this by interchanging pairs of
    	columns in the reverse order that the permutation was built up.
    	*/
//    	mxOut("reduced ", mx, new Zero());
    	for (int ii = 0; ii < rows; ii++) {
			for (int jj = 0; jj < cols; jj++) {
				vals[ii][jj] = mx.getDoubleValueAt(ii, jj);
			}
		}
    	return vals;
    }
    
    private static int[] getInitialColMapping(int cols) {
    	final int[] res = new int[cols];
    	for (int col = 0; col < cols; col++) res[col] = col;
    	return res;
    }
    private static void invertColMapping(int[] colMapping) {
    	int[] orig = colMapping.clone();
    	for (int ii = 0; ii < orig.length; ii++) {
			colMapping[orig[ii]] = ii;
		}
    }
    private static double[][] reduce(Zero zero, DoubleMatrix mx, int[] colMapping) {
    	int rows		= mx.getRowCount();
    	int cols		= mx.getColumnCount();
    	int fullRank	= Math.min(rows, cols);
    	    	
    	for (int pivot = 0; pivot < fullRank; pivot++) {
    		double max = 0.0d;
    		int pcol = -1, prow = -1;
			for (int row = pivot; row < rows; row++) {
				for (int col = pivot; col < cols; col++) {
					double val = mx.getDoubleValueAt(row, col);
					if (Math.abs(val) > max) {
						max = Math.abs(val);
						pcol = col;
						prow = row;
					}
				}
			}
			if (zero.isZero(max)) {
		    	invertColMapping(colMapping);
				return subMatrixToArray(mx, pivot);
			}
			if (prow != pivot) {
				mx.swapRows(prow, pivot);
			}
			if (pcol != pivot) {
				mx.swapColumns(pcol, pivot);
				Arrays.swap(colMapping, pcol, pivot);
			}
			
			//first, norm the pivot row
			double pivotInv		= 1.0d / mx.getDoubleValueAt(pivot, pivot);
			mx.setValueAt(pivot, pivot, 1.0d);				
			for (int col = pivot + 1; col < cols; col++) {
				mx.multiply(pivot, col, pivotInv);
			}
			//now, reduce the other metas
			for (int row = 0; row < rows; row++) {
				if (row != pivot) {
					double pivColVal = mx.getDoubleValueAt(row, pivot); 
					mx.setValueAt(row, pivot, 0.0d);
					for (int col = pivot + 1; col < cols; col++) {
						double subtract = pivColVal * mx.getDoubleValueAt(pivot, col);
						mx.add(row, col, -subtract);
					}					
				}
			}
		}
    	invertColMapping(colMapping);
		return subMatrixToArray(mx, fullRank);
   	}
    protected double[][] getRemainingMatrix(IBitSet zeroColumns, int[] rankSoFarPtr) {
    	final Zero zero	= mConfig.zero();
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
    	double[][] arr = new double[keptRowCnt][keptColCnt];
    	int zeroCols	= 0;
    	int newCol		= 0;
    	for (int iCol = 0; iCol < keptColCnt; iCol++) {
    		int col		= keptCols[iCol];
			boolean any = false;
			for (int iRow = 0; iRow < keptRowCnt; iRow++) {
				int row = keptRows[iRow];
				double val = mStoichRed[row][col];
				arr[iRow][newCol] = val;
				any |= zero.isNonZero(val);
			}
			if (any) newCol++;
			else zeroCols++;
		}    	
   		rankSoFarPtr[0] = stoichRows + zeroCnt - keptRowCnt;
    	if (zeroCols > 0) {
    		double[][] zarr = new double[keptRowCnt][keptColCnt - zeroCols];
    		for (int i = 0; i < zarr.length; i++) {
				System.arraycopy(arr[i], 0, zarr[i], 0, zarr[i].length);
			}
    		return zarr;
    	}
    	return arr;
    }
    protected boolean isRankGaussFullPivoting(double[][] arr, int rankSoFar, int rank) {		
    	final Zero zero		= mConfig.zero();
    	final int rows		= arr.length;
    	final int cols		= rows == 0 ? 0 : arr[0].length;
    	
    	final int fullRank	= Math.min(rows, cols);
    	final int stopRank	= rank - rankSoFar;
    	
    	if (stopRank > fullRank) return false;

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
    	for (pivot = 0; pivot < stopRank; pivot++) {
			if (zero.isZero(max)) {
				return pivot + rankSoFar >= rank;
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
		return pivot + rankSoFar >= rank;					
   	}    	
    
}
