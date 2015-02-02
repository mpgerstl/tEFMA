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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Arrays;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.ops.IntegerOperations;
import ch.javasoft.smx.iface.BigIntegerMatrix;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.IntMatrix;
import ch.javasoft.smx.iface.IntRationalMatrix;
import ch.javasoft.smx.iface.LongMatrix;
import ch.javasoft.smx.iface.LongRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableIntMatrix;
import ch.javasoft.smx.iface.ReadableLongMatrix;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.matrix.IntMatrixOperations;

/**
 * The <code>DefaultIntMatrix</code> is the default implementation of IntMatrix. It
 * contains a 1-dimensional array with all the matrix values.
 */
public class DefaultIntMatrix implements IntMatrix {
    
    private static final String NL = System.getProperty("line.separator");

    private int		mColumnCount;
    private int[]	mValues;
    
    public DefaultIntMatrix(int rowCount, int colCount) {
        this(new int[rowCount * colCount], rowCount, colCount);
    }
    
    public DefaultIntMatrix(int[] values, int rowCount, int columnCount) {
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
    
    /**
     * Initializes the matrix from a 2-dim int array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the second dimension otherwise. 
     * If the second dimension has different lengths, the maximum value
     * is taken, missing values are initialized with 0.
     * 
     * @param values		The 2-dim int array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultIntMatrix(int[][] values, boolean rowsInDim1) {
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
    	final int[] vals = new int[rowCnt * colCnt];
    	for (int row = 0; row < rowCnt; row++) {
			for (int col = 0; col < colCnt; col++) {
				int index1 = rowsInDim1 ? row : col;
				int index2 = rowsInDim1 ? col : row;
				vals[row * colCnt + col] = 
					index2 < values[index1].length ? values[index1][index2] : 0;
			}
		}
    	mValues			= vals;
    	mColumnCount	= colCnt;
    }
    /**
     * Initializes the matrix from a 2-dim int array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the second dimension otherwise. 
     * If the second dimension has different lengths, the maximum value
     * is taken, missing values are initialized with 0.
     * 
     * @param values		The 2-dim int array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultIntMatrix(Integer[][] values, boolean rowsInDim1) {
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
    	final int[] vals = new int[rowCnt * colCnt];
    	for (int row = 0; row < rowCnt; row++) {
			for (int col = 0; col < colCnt; col++) {
				int index1 = rowsInDim1 ? row : col;
				int index2 = rowsInDim1 ? col : row;
				vals[row * colCnt + col] = 
					index2 < values[index1].length ? values[index1][index2].intValue() : 0;
			}
		}
    	mValues			= vals;
    	mColumnCount	= colCnt;
    }
    public DefaultIntMatrix(ReadableIntMatrix mx) {
    	mValues			= mx.toIntArray();
    	mColumnCount	= mx.getColumnCount();
    }
    
    public static DefaultIntMatrix diag(int[] values) {
        int len = values.length;
        DefaultIntMatrix mx = new DefaultIntMatrix(new int[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, values[ii]);
        }
        return mx;
    }
    
    public static DefaultIntMatrix diag(int value, int len) {
        DefaultIntMatrix mx = new DefaultIntMatrix(new int[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, value);
        }
        return mx;        
    }
    
    public static DefaultIntMatrix identity(int len) {
        return diag(1, len);
    }
    
    public IntegerOperations getNumberOperations() {
    	return IntegerOperations.instance();
    }
    
    public MatrixOperations<Integer> getMatrixOperations() {
    	return IntMatrixOperations.instance();
    }

    public int getIntValueAt(int row, int col) {
        return mValues[row * mColumnCount + col];
    }
    public long getLongValueAt(int row, int col) {
    	return getIntValueAt(row, col);
    }
    public int getSignumAt(int row, int col) {    	
    	final int val = getIntValueAt(row, col);
    	return val == 0 ? 0 : val < 0d ? -1 : 1;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableIntRationalMatrix#getIntDividendAt(int, int)
     */
    public int getIntNumeratorAt(int row, int col) {
        return getIntValueAt(row, col);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableIntRationalMatrix#getIntDivisorAt(int, int)
     */
    public int getIntDenominatorAt(int row, int col) {
        return 1;
    }
    
    /* (non-Javadoc)
     * @see ReadableLongRationalMatrix#getLongDividendAt(int, int)
     */
    public long getLongNumeratorAt(int row, int col) {
    	return getIntNumeratorAt(row, col);
    }
    
    /* (non-Javadoc)
     * @see ReadableLongRationalMatrix#getLongDivisorAt(int, int)
     */
    public long getLongDenominatorAt(int row, int col) {
    	return 1L;
    }
    
    /* (non-Javadoc)
     * @see ReadableBigIntegerRationalMatrix#getBigIntegerDividendAt(int, int)
     */
    public BigInteger getBigIntegerNumeratorAt(int row, int col) {
    	return BigInteger.valueOf(getIntNumeratorAt(row, col));
    }
    
    /* (non-Javadoc)
     * @see ReadableBigIntegerRationalMatrix#getBigIntegerDivisorAt(int, int)
     */
    public BigInteger getBigIntegerDenominatorAt(int row, int col) {
    	return BigInteger.ONE;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#getDoubleValueAt(int, int)
     */
    public double getDoubleValueAt(int row, int col) {
        return getIntValueAt(row, col);
    }
    
    /* (non-Javadoc)
     * @see ReadableBigIntegerRationalMatrix#getBigIntegerFractionNumberValueAt(int, int)
     */
    public BigFraction getBigFractionValueAt(int row, int col) {
    	return new BigFraction(
    		getBigIntegerNumeratorAt(row, col), getBigIntegerDenominatorAt(row, col)
    	);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#getRowCount()
     */
    public int getRowCount() {
        return mColumnCount > 0 ? mValues.length / mColumnCount : 0;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#getColumnCount()
     */
    public int getColumnCount() {
        return mColumnCount;
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#setValueAt(int, int, int)
     */
    public void setValueAt(int row, int col, int value) {
        mValues[row * mColumnCount + col] = value;
    }
    public void setValueAt(int row, int col, Integer value) {
    	setValueAt(row, col, value.intValue());
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
     */
    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        int[] tmpRow = new int[mColumnCount];
        System.arraycopy(mValues, rowA * mColumnCount, tmpRow, 0, mColumnCount);
        System.arraycopy(mValues, rowB * mColumnCount, mValues, rowA * mColumnCount, mColumnCount);
        System.arraycopy(tmpRow, 0, mValues, rowB * mColumnCount, mColumnCount);        
    }

    public void swapColumns(int colA, int colB) {
    	if (colA != colB) {
    		int rows = getRowCount();
    		for (int row = 0; row < rows; row++) {
				int tmp = getIntValueAt(row, colA);
				setValueAt(row, colA, getIntValueAt(row, colB));
				setValueAt(row, colB, tmp);
			}
    	}
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableMatrix#getNumberValueAt(int, int)
     */
    public Integer getNumberValueAt(int row, int col) {
        return Integer.valueOf(getIntValueAt(row, col));
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableIntMatrix#toIntArray()
     */
    public int[] toIntArray() {
        return mValues.clone();
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableIntMatrix#toArray(int[])
     */
    public void toArray(int[] array) {
        if (array.length != mValues.length) {
            throw new IllegalArgumentException("expected array length " + mValues.length + " but found " + array.length);
        }
        System.arraycopy(mValues, 0, array, 0, array.length);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#toDoubleArray()
     */
    public double[] toDoubleArray() {
        double[] array = new double[mValues.length];
        toArray(array);
        return array;
    }
    
    public long[] toLongArray() {
        long[] array = new long[mValues.length];
        toArray(array);
        return array;
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#toArray(double[])
     */
    public void toArray(double[] array) {
        AbstractDoubleMatrix.toArray(this, array);
    }
    
    public void toArray(long[] array) {
    	DefaultLongMatrix.toArray(this, array);
    }
    
    public double[] getDoubleColumn(int col) {
        return AbstractDoubleMatrix.getDoubleColumn(this, col);
    }
    
    public double[][] getDoubleColumns() {
        return AbstractDoubleMatrix.getDoubleColumns(this);
    }
    
    public double[] getDoubleRow(int row) {
        return AbstractDoubleMatrix.getDoubleRow(this, row);
    }
    
    public double[][] getDoubleRows() {
        return AbstractDoubleMatrix.getDoubleRows(this);
    }

    public int[] getIntColumn(int col) {
        return getIntColumn(this, col);
    }
    public static int[] getIntColumn(ReadableIntMatrix mx, int col) {
        int rows = mx.getRowCount();
        int[] res = new int[rows];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getIntValueAt(row, col);            
        }
        return res;
    }
    public long[] getLongColumn(int col) {
    	return DefaultLongMatrix.getLongColumn(this, col);
    }
    
    public int[] getIntRow(int row) {
        return getIntRow(this, row);
    }
    public static int[] getIntRow(ReadableIntMatrix mx, int row) {
        int cols = mx.getColumnCount();
        int[] res = new int[cols];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getIntValueAt(row, col);
        }
        return res;
    }
    public long[] getLongRow(int row) {
    	return DefaultLongMatrix.getLongRow(this, row);
    }
    
    public int[][] getIntColumns() {
        return getIntColumns(this);
    }
    public static int[][] getIntColumns(ReadableIntMatrix mx) {
        int cols = mx.getColumnCount();
        int[][] res = new int[cols][];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getIntColumn(col);
        }
        return res;
    }
    public long[][] getLongColumns() {
    	return DefaultLongMatrix.getLongColumns(this);
    }
    
    public Integer[][] getNumberRows() {
    	return getNumberRows(this);
    }
    public static Integer[][] getNumberRows(ReadableIntMatrix<Integer> mx) {
    	final int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
    	final Integer[][] data = new Integer[rows][cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				data[row][col] = Integer.valueOf(mx.getIntValueAt(row, col));
			}
		}
    	return data;
    }
    
    public int[][] getIntRows() {
        return getIntRows(this);
    }
    public static int[][] getIntRows(ReadableIntMatrix mx) {
        int rows = mx.getRowCount();
        int[][] res = new int[rows][];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getIntRow(row);
        }
        return res;
    }
    public long[][] getLongRows() {
    	return DefaultLongMatrix.getLongRows(this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return toString(this);
    }
    public void writeTo(Writer writer) {
        writeTo(writer, this);
    }
    public void writeTo(OutputStream out) {
        writeTo(out, this);
    }
    public static String toString(ReadableIntMatrix mx) {
        return toString(mx, "{", " }", " [", "]", "", "", "", ", ");
    }
    public static void writeTo(Writer writer, ReadableIntMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }
    public static void writeTo(OutputStream out, ReadableIntMatrix mx) {
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }    
    
    public String toMultilineString() {
        return toMultilineString(this);
    }
    public void writeToMultiline(Writer writer) {
        writeToMultiline(writer, this);
    }
    public void writeToMultiline(OutputStream out) {
        writeToMultiline(out, this);
    }
    public static String toMultilineString(ReadableIntMatrix mx) {
        return toString(mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");
    }
    public static void writeToMultiline(Writer writer, ReadableIntMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }
    public static void writeToMultiline(OutputStream out, ReadableIntMatrix mx) {
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }    
    
    protected static String toString(ReadableIntMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        StringWriter sw = new StringWriter();
        writeTo(new PrintWriter(sw), mx, prefix, postfix, rowPrefix, rowPostfix, rowSeparator, colPrefix, colPostfix, colSeparator);
        return sw.toString();
    }
    protected static void writeTo(PrintWriter writer, ReadableIntMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        int rows = mx.getRowCount();
        int cols = mx.getColumnCount();
        writer.print(prefix);
        for (int row = 0; row < rows; row++) {
            if (row > 0) writer.print(rowSeparator);
            writer.print(rowPrefix);            
            for (int col = 0; col < cols; col++) {
                if (col > 0) writer.print(colSeparator);
                writer.print(colPrefix);
                writer.print(mx.getIntValueAt(row, col));
                writer.print(colPostfix);
            }
            writer.print(rowPostfix);
        }
        writer.print(postfix);
        writer.flush();
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.MatrixBase#clone()
     */
    @Override
	public DefaultIntMatrix clone() {
        return new DefaultIntMatrix(mValues.clone(), -1, mColumnCount);
    }
    public DefaultIntMatrix newInstance(int rows, int cols) {
    	return new DefaultIntMatrix(rows, cols);
    }
    
    public DefaultIntMatrix newInstance(Integer[][] data, boolean rowsInDim1) {
    	final int rows, cols;
    	if (rowsInDim1) {
        	rows = data.length;
        	cols = rows == 0 ? 0 : data[0].length;
    	}
    	else {
        	cols = data.length;
        	rows = cols == 0 ? 0 : data[0].length;
    	}
    	final int[] values = new int[rows * cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final Integer val = rowsInDim1 ? data[row][col] : data[col][row];
				values[row * cols + col] = val.intValue();
			}
		}
    	return new DefaultIntMatrix(values, rows, cols);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableIntMatrix#toIntMatrix(boolean)
     */
    public IntMatrix toIntMatrix(boolean enforceNewInstance) {
        return enforceNewInstance ? (IntMatrix)clone() : this;
    }
    
    public IntMatrix toWritableMatrix(boolean enforceNewInstance) {
    	return toIntMatrix(enforceNewInstance);
    }
    public IntMatrix toReadableMatrix(boolean enforceNewInstance) {
    	return toIntMatrix(enforceNewInstance);
    }
    
    public LongMatrix toLongMatrix(boolean enforceNewInstance) {
    	return new DefaultLongMatrix(this);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableIntRationalMatrix#toIntFractionMatrix(boolean)
     */
    public IntRationalMatrix toIntRationalMatrix(boolean enforceNewInstance) {
        int rows = getRowCount();
        int cols = getColumnCount();
        int[] ones = new int[rows * cols];
        Arrays.fill(ones, 1);
        return new DefaultIntRationalMatrix(mValues.clone(), ones, rows, cols);
    }
    
    /* (non-Javadoc)
     * @see ReadableLongRationalMatrix#toLongRationalMatrix(boolean)
     */
    public LongRationalMatrix toLongRationalMatrix(boolean enforceNewInstance) {
    	// TODO impl
    	throw new RuntimeException("not implemented yet");
    }

    /* (non-Javadoc)
     * @see ReadableBigIntegerRationalMatrix#toBigIntegerRationalMatrix(boolean)
     */
    public BigIntegerRationalMatrix toBigIntegerRationalMatrix(boolean enforceNewInstance) {
    	return new DefaultBigIntegerRationalMatrix(toLongArray(), getRowCount(), getColumnCount()); 
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.ReadableDoubleMatrix#toDoubleMatrix(boolean)
     */
    public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
        return new DefaultDoubleMatrix(toDoubleArray(), getRowCount(), getColumnCount());
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#addRowToOtherRow(int, int, int, int)
     */
    public void addRowToOtherRow(int srcRow, int srcFactor, int dstRow, int dstFactor) {        
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            setValueAt(dstRow, col, dstFactor * getIntValueAt(dstRow, col) + srcFactor * getIntValueAt(srcRow, col));
        }
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#add(int, int, int)
     */
    public void add(int row, int col, int value) {
        setValueAt(row, col, getIntValueAt(row, col) + value);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#multiply(int, int, int)
     */
    public void multiply(int row, int col, int factor) {
        setValueAt(row, col, factor * getIntValueAt(row, col));
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#multiplyRow(int, int)
     */
    public void multiplyRow(int row, int factor) {
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            multiply(row, col, factor);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        int value = mColumnCount;
        for (int ii = 0; ii < mValues.length; ii++) {
            value ^= mValues[ii];
        }
        return value;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof IntMatrix) {
            IntMatrix other = (IntMatrix)obj;
            int rows = getRowCount();
            if (rows != other.getRowCount()) return false;
            int cols = getColumnCount();
            if (cols != other.getColumnCount()) return false;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (getIntValueAt(row, col) != other.getIntValueAt(row, col)) return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public IntMatrix subIntMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	DefaultIntMatrix res = new DefaultIntMatrix(rowEnd - rowStart, colEnd - colStart);
    	for (int row = 0; row < res.getRowCount(); row++) {
			for (int col = 0; col < res.getColumnCount(); col++) {
				res.setValueAt(row, col, getIntValueAt(rowStart + row, colStart + col));
			}
		}
    	return res;
    }
    public ReadableLongMatrix subLongMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntMatrix(rowStart, rowEnd, colStart, colEnd).toLongMatrix(false /*cloned anyway*/);
    }
    public IntRationalMatrix subIntRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntMatrix(rowStart, rowEnd, colStart, colEnd).toIntRationalMatrix(false /*cloned anyway*/);
    }
    public LongRationalMatrix subLongRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntMatrix(rowStart, rowEnd, colStart, colEnd).toLongRationalMatrix(false /*cloned anyway*/);
    }
    public ReadableBigIntegerMatrix subBigIntegerMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntMatrix(rowStart, rowEnd, colStart, colEnd).toBigIntegerMatrix(false /*clonded anyway*/);
    }
    public BigIntegerRationalMatrix subBigIntegerRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntMatrix(rowStart, rowEnd, colStart, colEnd).toBigIntegerRationalMatrix(false /*cloned anyway*/);
    }
    public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntMatrix(rowStart, rowEnd, colStart, colEnd).toDoubleMatrix(false /*clonded anyway*/);
    }
    public DefaultIntMatrix transposeR() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultIntMatrix tr = new DefaultIntMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getIntValueAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultIntMatrix transposeW() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultIntMatrix tr = new DefaultIntMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getIntValueAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultIntMatrix transpose() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultIntMatrix tr = new DefaultIntMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getIntValueAt(row, col));
			}
		}
    	return tr;
    }
    
	public BigInteger getBigIntegerValueAt(int row, int col) {
		return BigInteger.valueOf(getIntValueAt(row, col));
	}

	public void toArray(BigInteger[] array) {
        if (array.length != mValues.length) {
            throw new IllegalArgumentException("expected array length " + mValues.length + " but found " + array.length);
        }
		for (int i = 0; i < array.length; i++) {
			array[i] = BigInteger.valueOf(mValues[i]);
		}
	}

	public BigInteger[] toBigIntegerArray() {
        final BigInteger[] array = new BigInteger[mValues.length];
        toArray(array);
        return array;
	}

	public BigIntegerMatrix toBigIntegerMatrix(boolean enforceNewInstance) {
		return new DefaultBigIntegerMatrix(this);
	}
    
	public BigInteger[] getBigIntegerColumn(int col) {
    	final BigInteger[] theCol = new BigInteger[getRowCount()];
    	for (int row = 0; row < theCol.length; row++) {
			theCol[row] = getBigIntegerValueAt(row, col);
		}
    	return theCol;
	}

	public BigInteger[][] getBigIntegerColumns() {
    	final BigInteger[][] cols = new BigInteger[getColumnCount()][];
    	for (int col = 0; col < cols.length; col++) {
			cols[col] = getBigIntegerColumn(col);
		}
    	return cols;
	}

	public BigInteger[] getBigIntegerRow(int row) {
    	final BigInteger[] theRow = new BigInteger[getColumnCount()];
    	for (int col = 0; col < theRow.length; col++) {
    		theRow[col] = getBigIntegerValueAt(row, col);
		}
    	return theRow;
	}

	public BigInteger[][] getBigIntegerRows() {
    	final BigInteger[][] rows = new BigInteger[getRowCount()][];
    	for (int row = 0; row < rows.length; row++) {
    		rows[row] = getBigIntegerRow(row);
		}
    	return rows;
	}
	public void negate(int row, int col) {
		setValueAt(row, col, -getIntValueAt(row, col));
	}
}
