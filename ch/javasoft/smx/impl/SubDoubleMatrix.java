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
package ch.javasoft.smx.impl;

import ch.javasoft.smx.iface.DoubleMatrix;

/**
 * The <code>SubDoubleMatrix</code> TODO type javadoc for SubDoubleMatrix
 */
public class SubDoubleMatrix extends AbstractDoubleMatrix {
    
    private static final int[] EMPTY_MAPPING = new int[] {};
    
    private DoubleMatrix	mBase;
    private int[]		mRowMapping;
    private int[]		mColMapping;

    public SubDoubleMatrix(DoubleMatrix base, boolean selectAllRows, boolean selectAllColumns) {
        mBase = base;
        if (selectAllRows) {
            selectAllRows();
        }
        else {
            mRowMapping = EMPTY_MAPPING;
        }
        if (selectAllColumns) {
            selectAllColumns();
        }
        else {
            mColMapping = EMPTY_MAPPING;
        }
    }
    
    /**
     * Selects a given row from the underlying base matrix and appends it to the end of the
     * rowset of this <code>SubDoubleMatrix</code>
     * 
     * @param 	rowInBaseMatrix The row index in the underlying base matrix
     * @return	The index of the added row in this <code>SubDoubleMatrix</code>
     */
    public int selectRow(int rowInBaseMatrix) {
        mRowMapping = addToArray(mRowMapping, rowInBaseMatrix);
        return mRowMapping.length - 1;
    }
    
    /**
     * Selects a range of rows from the underlying base matrix and appends them to the end of the
     * rowset of this <code>SubDoubleMatrix</code>
     * 
     * @param 	fromRowInBaseMatrix The start row index in the underlying base matrix (inclusive)
     * @param 	toRowInBaseMatrix   The end row index in the underlying base matrix (exclusive)
     * @return	The index in this <code>SubDoubleMatrix</code> of the first row added. The next
     * 			rows are appended, the last row is at position <code>getColumnCount() - 1</code>.
     */
    public int selectRows(int fromRowInBaseMatrix /*incl*/, int toRowInBaseMatrix /*excl*/) {
        int ret = -1;
        for (int row = fromRowInBaseMatrix; row < toRowInBaseMatrix; row++) {
            int index = selectRow(row);
            if (row == fromRowInBaseMatrix) {
                ret = index;
            }
        }
        return ret;
    }
    
    public void selectAllRows() {
        mRowMapping = new int[mBase.getRowCount()];
        for (int ii = 0; ii < mRowMapping.length; ii++) {
            mRowMapping[ii] = ii;
        }
    }
    
    public void switchSelectedRow(int row, int rowInBaseMatrix) {
        mRowMapping[row] = rowInBaseMatrix;
    }
    
    public int getMappedRow(int row) {
        return mRowMapping[row];
    }
    
    public int getUnmappedRow(int rowInBaseMatrix) {
        for (int ii = 0; ii < mRowMapping.length; ii++) {
            if (mRowMapping[ii] == rowInBaseMatrix) return ii;
        }
        return -1;
    }
    
    public int[] getRowMapping() {
        return mRowMapping.clone();
    }

    /**
     * Selects a given column from the underlying base matrix and appends it to the end of the
     * columnset of this <code>SubDoubleMatrix</code>
     * 
     * @param 	colInBaseMatrix The column index in the underlying base matrix
     * @return	The index of the added column in this <code>SubDoubleMatrix</code>
     */
    public int selectColumn(int colInBaseMatrix) {
        mColMapping = addToArray(mColMapping, colInBaseMatrix);
        return mColMapping.length - 1;
    }

    public int selectColumn(int fromColInBaseMatrix /*incl*/, int toColInBaseMatrix /*excl*/) {
        int ret = -1;
        for (int col = fromColInBaseMatrix; col < toColInBaseMatrix; col++) {
            int index = selectColumn(col);
            if (col == fromColInBaseMatrix) {
                ret = index;
            }
        }
        return ret;
    }
    
    public void selectAllColumns() {
        mColMapping = new int[mBase.getColumnCount()];                        
        for (int ii = 0; ii < mColMapping.length; ii++) {
            mColMapping[ii] = ii;
        }
    }
    
    public void switchSelectedColumn(int col, int colInBaseMatrix) {
        mColMapping[col] = colInBaseMatrix;
    }
    
    public void unselectRow(int row) {
        mRowMapping = removeFromArray(mRowMapping, row);
    }
    
    public void unselectRows(int fromRow /*incl*/, int toRow /*excl*/) {
        for (int row = fromRow; row < toRow; row++) {
            unselectRow(row);
        }
    }
    
    public void unselectAllRows() {
        mRowMapping = EMPTY_MAPPING;
    }
    
    public void unselectColumn(int col) {
        mColMapping = removeFromArray(mColMapping, col);
    }
    
    public void unselectColumns(int fromCol /*incl*/, int toCol /*excl*/) {
        for (int col = fromCol; col < toCol; col++) {
            unselectColumn(col);
        }
    }
    
    public void unselectAllColumns() {
        mColMapping = EMPTY_MAPPING;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#getDoubleValueAt(int, int)
     */
    public double getDoubleValueAt(int row, int col) {
        return mBase.getDoubleValueAt(mRowMapping[row], mColMapping[col]);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableDoubleMatrix#setValueAt(int, int, double)
     */
    public void setValueAt(int row, int col, double value) {
        mBase.setValueAt(mRowMapping[row], mColMapping[col], value);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.impl.AbstractDoubleMatrix#addRowToOtherRow(int, double, int, double)
     */
    @Override
	public void addRowToOtherRow(int srcRow, double srcFactor, int dstRow, double dstFactor) {
        mBase.addRowToOtherRow(mRowMapping[srcRow], srcFactor, mRowMapping[dstRow], dstFactor);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.impl.AbstractDoubleMatrix#multiplyRow(int, double)
     */
    @Override
	public void multiplyRow(int row, double factor) {
        mBase.multiplyRow(mRowMapping[row], factor);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
	public SubDoubleMatrix clone() {
        SubDoubleMatrix clone = new SubDoubleMatrix(mBase.clone(), false, false);
        clone.mRowMapping = mRowMapping.clone();
        clone.mColMapping = mColMapping.clone();
        return clone;
    }

    public SubDoubleMatrix newInstance(int rows, int cols) {
    	return new SubDoubleMatrix(mBase.newInstance(rows, cols), true, true);
    }
    
    public SubDoubleMatrix newInstance(Double[][] data, boolean rowsInDim1) {
    	return new SubDoubleMatrix(mBase.newInstance(data, rowsInDim1), true, true);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#getRowCount()
     */
    public int getRowCount() {
        return mRowMapping.length;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#getColumnCount()
     */
    public int getColumnCount() {
        return mColMapping.length;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
     */
    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        int tmp = mRowMapping[rowA];
        mRowMapping[rowA] = mRowMapping[rowB];
        mRowMapping[rowB] = tmp;
    }
    
    private static int[] addToArray(int[] array, int value) {
        int[] newArray = new int[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[newArray.length - 1] = value;
        return newArray;
    }
    private static int[] removeFromArray(int[] array, int pos) {
        int[] newArray = new int[array.length - 1];
        System.arraycopy(array, 0, newArray, 0, pos);
        System.arraycopy(array, pos + 1, newArray, pos, newArray.length - pos);
        return newArray;
    }
    
    @Override
    public SubDoubleMatrix transpose() {
    	SubDoubleMatrix tr = new SubDoubleMatrix(mBase.transpose(), false, false);
    	tr.mColMapping = mRowMapping.clone();
    	tr.mRowMapping = mColMapping.clone();
    	return tr;
    }
    public SubDoubleMatrix transposeR() {
    	SubDoubleMatrix tr = new SubDoubleMatrix(mBase.transpose(), false, false);
    	tr.mColMapping = mRowMapping.clone();
    	tr.mRowMapping = mColMapping.clone();
    	return tr;
    }
    public SubDoubleMatrix transposeW() {
    	SubDoubleMatrix tr = new SubDoubleMatrix(mBase.transpose(), false, false);
    	tr.mColMapping = mRowMapping.clone();
    	tr.mRowMapping = mColMapping.clone();
    	return tr;
    }

}
