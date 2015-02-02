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

import ch.javasoft.smx.iface.ReadableDoubleMatrix;

/**
 * The <code>DefaultDoubleMatrix</code> is the default implementation of the DoubleMatrix.
 * It contains a 1-dimensional double array with all the matrix values.
 */
public class DefaultDoubleMatrix extends AbstractDoubleMatrix {
    
    private final int		mColumnCount;
    private final double[]	mValues;
    
    public DefaultDoubleMatrix(int rowCount, int colCount) {
        this(new double[rowCount * colCount], rowCount, colCount);
    }
    
    /**
     * Initializes the matrix from a 2-dim double array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the second dimension otherwise. 
     * If the second dimension has different lengths, the maximum value
     * is taken, missing values are initialized with 0.0d.
     * 
     * @param values		The 2-dim double array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultDoubleMatrix(double[][] values, boolean rowsInDim1) {
    	int rowCnt, colCnt;
    	if (rowsInDim1) {
    		rowCnt = values.length;
    		colCnt = 0;
    		for (int row = 0; row < values.length; row++) {
				colCnt = Math.max(colCnt, values[row].length);
			}
    	}
    	else {
    		colCnt = values.length;
    		rowCnt = 0;
    		for (int col = 0; col < values.length; col++) {
				rowCnt = Math.max(rowCnt, values[col].length);
			}
    	}
    	final double[] vals = new double[rowCnt * colCnt];
    	for (int row = 0; row < rowCnt; row++) {
			for (int col = 0; col < colCnt; col++) {
				int index1 = rowsInDim1 ? row : col;
				int index2 = rowsInDim1 ? col : row;
				vals[row * colCnt + col] = 
					index2 < values[index1].length ? values[index1][index2] : 0.0d;
			}
		}
    	mValues			= vals;
    	mColumnCount	= colCnt;
    }
    /**
     * Initializes the matrix from a 2-dim double array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the sencond dimension otherwise. 
     * If the second dimension has differend lengths, the maximum value
     * is taken, missing values are initialized with 0.0d.
     * 
     * @param values		The 2-dim double array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultDoubleMatrix(Double[][] values, boolean rowsInDim1) {
    	this((Number[][])values, rowsInDim1);
    }
    public DefaultDoubleMatrix(Number[][] values, boolean rowsInDim1) {
    	int rowCnt, colCnt;
    	if (rowsInDim1) {
    		rowCnt = values.length;
    		colCnt = 0;
    		for (int row = 0; row < values.length; row++) {
				colCnt = Math.max(colCnt, values[row].length);
			}
    	}
    	else {
    		colCnt = values.length;
    		rowCnt = 0;
    		for (int col = 0; col < values.length; col++) {
				rowCnt = Math.max(rowCnt, values[col].length);
			}
    	}
    	double[] vals = new double[rowCnt * colCnt];
    	for (int row = 0; row < rowCnt; row++) {
			for (int col = 0; col < colCnt; col++) {
				int index1 = rowsInDim1 ? row : col;
				int index2 = rowsInDim1 ? col : row;
				vals[row * colCnt + col] = 
					index2 < values[index1].length ? values[index1][index2].doubleValue() : 0.0d;
			}
		}
    	mValues			= vals;
    	mColumnCount	= colCnt;
    }
    
    public DefaultDoubleMatrix(double[] values, int rowCount, int columnCount) {
        if (rowCount == -1) rowCount = values.length / columnCount;
        if (columnCount == -1) columnCount = values.length / rowCount;
        if (rowCount < 0 || columnCount < 0 || rowCount * columnCount != values.length) {
            throw new IllegalArgumentException(
                    "rowCount (" + rowCount + ") * columnCount (" + columnCount + 
                    ") != number of values (" + values.length + ")"
                    );
        }
        mValues			= values;
        mColumnCount	= columnCount;
    }
    
    public DefaultDoubleMatrix(ReadableDoubleMatrix mx) {
    	mValues			= mx.toDoubleArray();
    	mColumnCount	= mx.getColumnCount();
    }
    
    public static DefaultDoubleMatrix diag(double[] values) {
        int len = values.length;
        DefaultDoubleMatrix mx = new DefaultDoubleMatrix(new double[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, values[ii]);
        }
        return mx;
    }
    
    public static DefaultDoubleMatrix diag(double value, int len) {
        DefaultDoubleMatrix mx = new DefaultDoubleMatrix(new double[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, value);
        }
        return mx;        
    }
    
    public static DefaultDoubleMatrix identity(int len) {
        return diag(1.0d, len);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#getDoubleValueAt(int, int)
     */
    public double getDoubleValueAt(int row, int col) {
        return mValues[row * mColumnCount + col];
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#getRowCount()
     */
    public int getRowCount() {
        return mValues.length / (mColumnCount == 0 ? 1 : mColumnCount);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#getColumnCount()
     */
    public int getColumnCount() {
        return mColumnCount;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableDoubleMatrix#setValueAt(int, int, double)
     */
    public void setValueAt(int row, int col, double value) {
        mValues[row * mColumnCount + col] = value;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
     */
    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        double[] tmpRow = new double[mColumnCount];
        System.arraycopy(mValues, rowA * mColumnCount, tmpRow, 0, mColumnCount);
        System.arraycopy(mValues, rowB * mColumnCount, mValues, rowA * mColumnCount, mColumnCount);
        System.arraycopy(tmpRow, 0, mValues, rowB * mColumnCount, mColumnCount);        
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#toDoubleArray()
     */
    @Override
	public double[] toDoubleArray() {
        return mValues.clone();
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#toArray(double[])
     */
    @Override
	public void toArray(double[] array) {
        if (array.length != mValues.length) {
            throw new IllegalArgumentException("expected array length " + mValues.length + " but found " + array.length);
        }
        System.arraycopy(mValues, 0, array, 0, array.length);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#clone()
     */
    @Override
	public DefaultDoubleMatrix clone() {
        return new DefaultDoubleMatrix(mValues.clone(), getRowCount(), mColumnCount);
    }
    
    public DefaultDoubleMatrix newInstance(int rows, int cols) {
    	return new DefaultDoubleMatrix(rows, cols);
    }
    
    public DefaultDoubleMatrix newInstance(Double[][] data, boolean rowsInDim1) {
    	final int rows, cols;
    	if (rowsInDim1) {
        	rows = data.length;
        	cols = rows == 0 ? 0 : data[0].length;
    	}
    	else {
        	cols = data.length;
        	rows = cols == 0 ? 0 : data[0].length;
    	}
    	final double[] values = new double[rows * cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final Double val = rowsInDim1 ? data[row][col] : data[col][row];
				values[row * cols + col] = val.doubleValue();
			}
		}
    	return new DefaultDoubleMatrix(values, rows, cols);
    }
    
    @Override
    public DefaultDoubleMatrix transposeR() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultDoubleMatrix tr = new DefaultDoubleMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getDoubleValueAt(row, col));
			}
		}
    	return tr;
    }

    public DefaultDoubleMatrix transposeW() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultDoubleMatrix tr = new DefaultDoubleMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getDoubleValueAt(row, col));
			}
		}
    	return tr;
    }

    public DefaultDoubleMatrix transpose() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultDoubleMatrix tr = new DefaultDoubleMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getDoubleValueAt(row, col));
			}
		}
    	return tr;
    }
}
