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

import java.util.ArrayList;

import ch.javasoft.smx.iface.ReadableDoubleMatrix;

/**
 * The <code>DynamicDoubleMatrix</code> TODO type javadoc for DynamicDoubleMatrix
 */
abstract public class DynamicDoubleMatrix extends AbstractDoubleMatrix {
    
    private ArrayList<double[]> mVectors = new ArrayList<double[]>();

    /**
     * 
     */
    protected DynamicDoubleMatrix() {
        super();
    }
    
    protected double[] getVector(int index) {
        return mVectors.get(index);
    }
    
    protected int getVectorCount() {
        return mVectors.size();
    }
    
    protected int getVectorSize() {
        if (getVectorCount() == 0) return 0;
        return getVector(0).length;
    }
    
    protected void swapVectors(int indexA, int indexB) {
        if (indexA == indexB) return;
        double[] vA = mVectors.set(indexA, mVectors.get(indexB));
        mVectors.set(indexB, vA);
    }
    
    protected void setVector(int index, double[] vector) {
        if (index == mVectors.size()) mVectors.add(vector);
        else mVectors.set(index, vector);
    }
    
    protected double[] removeVector(int index) {
        return mVectors.remove(index);
    }
    
    
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.impl.AbstractDoubleMatrix#clone()
     */
    protected DynamicDoubleMatrix initClone(DynamicDoubleMatrix clone) {
        int size = mVectors.size();
        for (int ii = 0; ii < size; ii++) {
            clone.mVectors.add(mVectors.get(ii).clone());
        }
        return clone;
    }
    
    public static class DynamicRows extends DynamicDoubleMatrix {
        /**
         * 
         */
        public DynamicRows() {
            super();
        }
        
        public DynamicRows(ReadableDoubleMatrix src) {
            SubDoubleMatrix vRow = new SubDoubleMatrix(src.toDoubleMatrix(false), false, true);
            int rows = src.getRowCount();
            for (int row = 0; row < rows; row++) {
                vRow.selectRow(row);
                setVector(row, vRow.toDoubleArray());
                vRow.unselectRow(row);
            }
        }
        
        @Override
        public double[] getDoubleRow(int row) {
            return getRow(row).clone();
        }
        
        public double[] getRow(int row) {
            return getVector(row);
        }
        
        public double[] addRow() {
            double[] newRow = new double[getColumnCount()];
            addRow(newRow);
            return newRow;
        }
        public void addRow(double[] newRow) {
            int cols = getColumnCount();
            int rows = getRowCount();
            if (rows > 0 && newRow.length != cols) {
                throw new IllegalArgumentException(
                        "new row was expected to have length " + 
                        cols + " but has length " + newRow.length
                );
            }
            setVector(rows, newRow);
        }
        
        public double[] removeRow(int row) {
            return removeVector(row);
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.impl.AbstractDoubleMatrix#clone()
         */
        @Override
		public DynamicRows clone() {
            return (DynamicRows)initClone(new DynamicRows());
        }
        public DynamicRows newInstance(int rows, int cols) {
        	throw new RuntimeException("not implemented");
        }
        
        public DynamicRows newInstance(Double[][] data, boolean rowsInDim1) {
        	throw new RuntimeException("not implemented");
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.MatrixBase#getRowCount()
         */
        public int getRowCount() {
            return getVectorCount();
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.MatrixBase#getColumnCount()
         */
        public int getColumnCount() {
            return getVectorSize();
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#getDoubleValueAt(int, int)
         */
        public double getDoubleValueAt(int row, int col) {
            return getVector(row)[col];
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.WritableDoubleMatrix#setValueAt(int, int, double)
         */
        public void setValueAt(int row, int col, double value) {
            getVector(row)[col] = value;
        }
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
         */
        public void swapRows(int rowA, int rowB) {
            swapVectors(rowA, rowB);
        }
        @Override
        public DynamicRows transpose() {
        	final DynamicRows tr = new DynamicRows();
        	for (int i = 0; i < getVectorCount(); i++) {
        		double[] col = getVector(i);
				tr.addRow(col.clone());
			}
        	return tr;
        }
        public DynamicRows transposeR() {
        	final DynamicRows tr = new DynamicRows();
        	for (int i = 0; i < getVectorCount(); i++) {
        		double[] col = getVector(i);
				tr.addRow(col.clone());
			}
        	return tr;
        }
        public DynamicRows transposeW() {
        	final DynamicRows tr = new DynamicRows();
        	for (int i = 0; i < getVectorCount(); i++) {
        		double[] col = getVector(i);
				tr.addRow(col.clone());
			}
        	return tr;
        }
    }

    public static class DynamicColumns extends DynamicDoubleMatrix {
        /**
         * 
         */
        public DynamicColumns() {
            super();
        }
        
        public DynamicColumns(ReadableDoubleMatrix src) {
            SubDoubleMatrix vCol = new SubDoubleMatrix(src.toDoubleMatrix(false), true, false);
            int cols = src.getColumnCount();
            for (int col = 0; col < cols; col++) {
                if (col == 0) vCol.selectColumn(col);
                else vCol.switchSelectedColumn(0, col);
                setVector(col, vCol.toDoubleArray());
            }
        }
        
        @Override
        public double[] getDoubleColumn(int col) {
            return getColumn(col).clone();
        }
        
        public double[] getColumn(int col) {
            return getVector(col);
        }
        
        public double[] addColumn() {
            double[] newCol = new double[getRowCount()];
            addColumn(newCol);
            return newCol;
        }
        
        public void addColumn(double[] newCol) {
            int rows = getRowCount();
            int cols = getColumnCount();
            if (cols > 0 && newCol.length != rows) {
                throw new IllegalArgumentException(
                        "new column was expected to have length " + 
                        rows + " but has length " + newCol.length
                );
            }
            setVector(cols, newCol);
        }

        public double[] removeColumn(int col) {
            return removeVector(col);
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.impl.AbstractDoubleMatrix#clone()
         */
        @Override
		public DynamicColumns clone() {
            return (DynamicColumns)initClone(new DynamicColumns());
        }
        public DynamicColumns newInstance(int rows, int cols) {
        	throw new RuntimeException("not implemented");
        }
        
        public DynamicColumns newInstance(Double[][] data, boolean rowsInDim1) {
        	throw new RuntimeException("not implemented");
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.MatrixBase#getRowCount()
         */
        public int getRowCount() {
            return getVectorSize();
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.MatrixBase#getColumnCount()
         */
        public int getColumnCount() {
            return getVectorCount();
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#getDoubleValueAt(int, int)
         */
        public double getDoubleValueAt(int row, int col) {
            return getVector(col)[row];
        }
        
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.WritableDoubleMatrix#setValueAt(int, int, double)
         */
        public void setValueAt(int row, int col, double value) {
            getVector(col)[row] = value;
        }
        /* (non-Javadoc)
         * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
         */
        public void swapRows(int rowA, int rowB) {
            int cols = getColumnCount();
            for (int col = 0; col < cols; col++) {
                double tmp = getDoubleValueAt(rowA, col);
                setValueAt(rowA, col, getDoubleValueAt(rowB, col));
                setValueAt(rowB, col, tmp);
            }
        }
        @Override
        public DynamicColumns transposeW() {
        	final DynamicColumns tr = new DynamicColumns();
        	for (int i = 0; i < getVectorCount(); i++) {
        		double[] col = getVector(i);
				tr.addColumn(col.clone());
			}
        	return tr;
        }
        public DynamicColumns transposeR() {
        	final DynamicColumns tr = new DynamicColumns();
        	for (int i = 0; i < getVectorCount(); i++) {
        		double[] col = getVector(i);
				tr.addColumn(col.clone());
			}
        	return tr;
        }
        public DynamicColumns transpose() {
        	final DynamicColumns tr = new DynamicColumns();
        	for (int i = 0; i < getVectorCount(); i++) {
        		double[] col = getVector(i);
				tr.addColumn(col.clone());
			}
        	return tr;
        }
    }

}
