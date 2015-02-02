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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.lang.reflect.Array;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.numeric.Zero;

public class FastRankTestAdjacencyEnumerator extends DefaultRankTestAdjacencyEnumerator {

    @SuppressWarnings("hiding")
	public static final String NAME = "fast-rank";
	
	private IBitSet[] mRowsAnyInCol;
	private IBitSet[] mColsAnyInRow;
	
	public FastRankTestAdjacencyEnumerator() {
		this(false);
	}
	public FastRankTestAdjacencyEnumerator(boolean isMinCardinalityTested) {
		super(isMinCardinalityTested);
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public <Col extends Column, N extends Number> void initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel model) {
		super.initialize(columnHome, config, model);
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

	/*
	 * The reduced stoichiometric matrix mStoichRed has the form
	 * [I Rest] and thus full rank 
	 * (stoichRows x stoichCols, stoichRows <= #metas, stoichCols == #reacs).
	 * 
	 * The matrix to calculate the rank for consists of the stoich-red and
	 * of a submatrix of I (#reacs), i.e.
	 * [ I Rest    ]
	 * [ I_zeroset ]
	 * 
	 * We iterate through all columns (==reactions) and do the following:
	 *   a) if the zero set contains the reaction, it increments our rank
	 *      by 1. we clear out that column anyway (i.e. keptColCnt is not
	 *      incremented).
	 *      
	 *      if the reaction index ...
	 *      a1) ... lies in the identity part of the stoich-red matrix, the 
	 *      	rest of that row (without the identity-part-1 which has been 
	 *      	cleared out) has to be kept for subsequent rest-rank 
	 *      	computation, i.e. keptRowCnt++.
	 *      a2) ... does not lie in the identity part of stoich-red, the
	 *          stoich-red has still full rank. neither row of stoich-red
	 *          nor column are kept.
	 *   
	 *   b) it the zero set does not contain the reaction, it means that
	 *      the corresponding column in the matrix cannot be cleared out
	 *      and must be considered, i.e. keptColCnt++.
	 *      
	 * For the returned matrix, the rank has to be computed. All missing metas
	 * in this matrix (compared to [I Rest ; I_zeroset]) are to be seen as
	 * rank-decisive. If mx is the returned matrix, this means that our rank
	 * of interest is calculated by
	 * 
	 * #metas(stoich-red) + #metas(I_zeroset) - #metas(mx) + rank(mx)
	 */
    @Override
	protected double[][] getRemainingMatrix(IBitSet zeroColumns, int[] rankSoFarPtr) {
    	final Zero zero	= mConfig.zero();
		int stoichRows	= mStoichRed.length;
		int stoichCols	= stoichRows == 0 ? 0 : mStoichRed[0].length;
		
		int		zeroSetCnt	= 0;
		int		zeroColCnt	= 0;
		int		zeroRowCnt	= 0;
    	int[]	keptCols	= new int[stoichCols];
		int		keptColCnt	= 0;
    	int[]	keptRows	= new int[stoichRows];
		int		keptRowCnt	= 0;
		
		IBitSet rowsAny = BitSetUtil.factory().create();		
		IBitSet colsAny = BitSetUtil.factory().create();
    	for (int ii = 0; ii < stoichCols; ii++) {
			int col = mColMapping[ii];
			if (zeroColumns.get(ii)) {
				if (col < stoichRows) {
					keptRows[keptRowCnt++] = col;
					colsAny.or(mRowsAnyInCol[col]);
				}
				zeroSetCnt++;
			}
			else {
				keptCols[keptColCnt++] = col;
				rowsAny.or(mColsAnyInRow[col]);
			}
		}    	

    	//kick out zero metas/reacs
    	for (int i = 0; i < keptRowCnt; ) {
			int row = keptRows[i];
			if (!rowsAny.get(row)) {
				keptRowCnt--;
				Arrays.swap(keptRows, i, keptRowCnt);
				zeroRowCnt++;
			}
			else i++;
		}
    	for (int i = 0; i < keptColCnt; ) {
			int col = keptCols[i];
			if (!colsAny.get(col)) {
				keptColCnt--;
				Arrays.swap(keptCols, i, keptColCnt);
				zeroColCnt++;
			}
			else i++;
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
    	int zeroCols		= 0;
    	int oneValueCols	= 0;
    	int newCol			= 0;
    	for (int iCol = 0; iCol < keptColCnt; iCol++) {
    		int col 		= keptCols[iCol];
			int cntNonZeros = 0;
			int rowNonZero	= -1;
			for (int iRow = 0; iRow < keptRowCnt - oneValueCols; iRow++) {
				int row = keptRows[iRow];
				double val = mStoichRed[row][col];
				arr[iRow][newCol] = val;
				if (zero.isNonZero(val)) {
					rowNonZero = iRow;
					cntNonZeros++;
				}
			}
			if (cntNonZeros == 1) {
				oneValueCols++;
				Arrays.swapRow(arr, rowNonZero, keptRowCnt - oneValueCols);
	    		Arrays.swap(keptRows, rowNonZero, keptRowCnt - oneValueCols);
			}
			else if (cntNonZeros > 1) newCol++;
			else zeroCols++;    			
		}
   		rankSoFarPtr[0] = stoichRows + zeroSetCnt - zeroRowCnt - (keptRowCnt - oneValueCols);
    	if (zeroCols + oneValueCols > 0) {
    		double[][] zarr = new double[keptRowCnt - oneValueCols][keptColCnt - zeroCols - oneValueCols];
    		for (int i = 0; i < zarr.length; i++) {
				System.arraycopy(arr[i], 0, zarr[i], 0, zarr[i].length);
			}
     		return zarr;
    	}
    	return arr;
    }
    
}
