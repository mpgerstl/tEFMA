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
package ch.javasoft.metabolic.efm.sort;

import ch.javasoft.smx.iface.ReadableDoubleMatrix;

public abstract class RowColSorter implements MatrixSorter {
	private final boolean	mCompareRows;
	private final int		mStartRowCol, mStartColRow;
	private final int		mEndRowCol, mEndColRow;
	public RowColSorter(boolean compareRows, int startRowCol, int endRowCol, int startColRow, int endColRow) {
		mCompareRows	= compareRows;
		mStartRowCol	= startRowCol;
		mEndRowCol		= endRowCol;
		mStartColRow	= startColRow;
		mEndColRow		= endColRow;
	}
	public boolean compareRows() {
		return mCompareRows;
	}
	protected int count(ReadableDoubleMatrix mx) {
		return compareRows() ? mx.getColumnCount() : mx.getRowCount();
	}		
	protected int startRow(ReadableDoubleMatrix mx) {
		return compareRows() ? mStartRowCol : mStartColRow;
	}
	protected int endRow(ReadableDoubleMatrix mx) {
		return compareRows() ? mEndRowCol : mEndColRow;
	}
	protected int startCol(ReadableDoubleMatrix mx) {
		return compareRows() ? mStartColRow : mStartRowCol;
	}
	protected int endCol(ReadableDoubleMatrix mx) {
		return compareRows() ? mEndColRow : mEndRowCol;
	}
	protected int row(int rowOrCol, int colOrRow) {
		return compareRows() ? rowOrCol : colOrRow;			
	}
	protected int col(int rowOrCol, int colOrRow) {
		return compareRows() ? colOrRow : rowOrCol;			
	}
	protected double value(ReadableDoubleMatrix mx, int rowOrCol, int colOrRow) {
		int col = col(rowOrCol, colOrRow);
		int row = row(rowOrCol, colOrRow);
		return mx.getDoubleValueAt(row, col);			
	}
	public int start(ReadableDoubleMatrix mx) {
		return startRow(mx);
	}
	public int end(ReadableDoubleMatrix mx) {
		return endRow(mx);
	}
}