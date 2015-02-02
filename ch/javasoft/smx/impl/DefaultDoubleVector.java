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
import ch.javasoft.smx.iface.DoubleVector;

import ch.javasoft.math.BigFraction;

public class DefaultDoubleVector extends AbstractDoubleMatrix implements DoubleVector {
	
	private final boolean	mIsColumnVector;
	private final double[]	mValues;
	
	public DefaultDoubleVector(int size, boolean columnVector) {
		this(new double[size], columnVector);
	}
	/**
	 * Constructor without cloning the values
	 */
	public DefaultDoubleVector(double[] values, boolean columnVector) {
		mValues			= values;
		mIsColumnVector	= columnVector;
	}
	/**
	 * Constructor, values are cloned
	 */
	public DefaultDoubleVector(Double[] values, boolean columnVector) {
		this(values.length, columnVector);
		for (int i = 0; i < values.length; i++) {
			mValues[i] = values[i].doubleValue();
		}
	}
	
	public boolean isColumnVector() {
		return mIsColumnVector;
	}
	public boolean isRowVector() {
		return !mIsColumnVector;
	}
	
	@Override
	public DefaultDoubleVector clone() {
		return new DefaultDoubleVector(mValues.clone(), mIsColumnVector);
	}
	public DefaultDoubleVector toReadableVector(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}
	public DefaultDoubleVector toWritableVector(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}
	@Override
	public DefaultDoubleVector toReadableMatrix(boolean enforceNewInstance) {
		return toReadableVector(enforceNewInstance);
	}
	@Override
	public DefaultDoubleVector toWritableMatrix(boolean enforceNewInstance) {
		return toWritableVector(enforceNewInstance);
	}
	
    public DoubleMatrix newInstance(int rows, int cols) {
    	if (rows == 0 || cols == 0) {
    		return new DefaultDoubleVector(0, mIsColumnVector);
    	}
    	else if (cols == 1) {
        	return new DefaultDoubleVector(rows, true /*column vector*/);
    	}
    	else if (rows == 1) {
        	return new DefaultDoubleVector(cols, false /*column vector*/);
    	}
    	return new DefaultDoubleMatrix(rows, cols);
    }
    
    public DoubleMatrix newInstance(Double[][] data, boolean rowsInDim1) {
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
    	if (rows == 0 || cols == 0) {
    		return new DefaultDoubleVector(0, mIsColumnVector);
    	}
    	else if (cols == 1) {
        	return new DefaultDoubleVector(values, true /*column vector*/);
    	}
    	else if (rows == 1) {
        	return new DefaultDoubleVector(values, false /*column vector*/);
    	}
    	return new DefaultDoubleMatrix(values, rows, cols);
    }
    
    @Override
	public DefaultDoubleVector transposeR() {
		return new DefaultDoubleVector(mValues.clone(), !mIsColumnVector);
	}

	public DefaultDoubleVector transposeW() {
		return new DefaultDoubleVector(mValues.clone(), !mIsColumnVector);
	}

	public DefaultDoubleVector transpose() {
		return new DefaultDoubleVector(mValues.clone(), !mIsColumnVector);
	}

	public double getDoubleValueAt(int index) {
		return mValues[index];
	}
	
	public int getSize() {
		return mValues.length;
	}
	
	protected int getIndexForMatrixAccess(int row, int col) {
		if (isRowVector()) {
			if (row != 0) {
				throw new IndexOutOfBoundsException("row vector, row index must be 0, but was " + row);
			}
			return col;
		}
		if (col != 0) {
			throw new IndexOutOfBoundsException("column vector, column index must be 0, but was " + col);			
		}
		return row;
	}

	public double getDoubleValueAt(int row, int col) {
		return getDoubleValueAt(getIndexForMatrixAccess(row, col));
	}
	
	public Double getNumberValueAt(int index) {
		return Double.valueOf(getDoubleValueAt(index));
	}
	
	public void setValueAt(int index, Double value) {
		setValueAt(index, value.doubleValue());
	}

	public int getColumnCount() {
		return isRowVector() ? mValues.length : 1;
	}

	public int getRowCount() {
		return isColumnVector() ? mValues.length : 1;
	}
	
	public void setValueAt(int index, double value) {
		mValues[index] = value;
	}

	public void setValueAt(int row, int col, double value) {
		setValueAt(getIndexForMatrixAccess(row, col), value);
	}

	public void swapValues(int indexA, int indexB) {
		double tmp = mValues[indexA];
		mValues[indexA] = mValues[indexB];
		mValues[indexB] = tmp;
	}

	public void swapRows(int rowA, int rowB) {
		if (isRowVector()) {
			if (rowA != 0) throw new IndexOutOfBoundsException("row vector, row index must be 0, but was " + rowA);
			if (rowB != 0) throw new IndexOutOfBoundsException("row vector, row index must be 0, but was " + rowB);
			return;//nothing to swap
		}
		else {
			swapValues(rowA, rowB);
		}
	}
	@Override
	public void swapColumns(int colA, int colB) {
		if (isColumnVector()) {
			if (colA != 0) throw new IndexOutOfBoundsException("column vector, column index must be 0, but was " + colA);
			if (colB != 0) throw new IndexOutOfBoundsException("column vector, column index must be 0, but was " + colB);
			return;//nothing to swap
		}
		else {
			swapValues(colA, colB);
		}
	}

}
