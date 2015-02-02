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

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.ops.BigIntegerOperations;
import ch.javasoft.smx.iface.BigIntegerMatrix;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.LongMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableLongMatrix;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.matrix.BigIntegerMatrixOperations;

/**
 * The <code>DefaultBigIntegerMatrix</code> is the default implementation of 
 * {@link BigIntegerMatrix}. It contains a 1-dimensional array with all the 
 * matrix values.
 */
public class DefaultBigIntegerMatrix implements BigIntegerMatrix {
    
    private static final String NL = System.getProperty("line.separator");

    private int				mColumnCount;
    private BigInteger[]	mValues;
    
    public DefaultBigIntegerMatrix(int rowCount, int colCount) {
        this(new BigInteger[rowCount * colCount], rowCount, colCount);
    }
    
    public DefaultBigIntegerMatrix(BigInteger[] values, int rowCount, int columnCount) {
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
     * Initializes the matrix from a 2-dim BigInteger array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the second dimension otherwise. 
     * If the second dimension has different lengths, the maximum value
     * is taken, missing values are initialized with 0.
     * 
     * @param values		The 2-dim BigInteger array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultBigIntegerMatrix(BigInteger[][] values, boolean rowsInDim1) {
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
    	final BigInteger[] vals = new BigInteger[rowCnt * colCnt];
    	for (int row = 0; row < rowCnt; row++) {
			for (int col = 0; col < colCnt; col++) {
				int index1 = rowsInDim1 ? row : col;
				int index2 = rowsInDim1 ? col : row;
				vals[row * colCnt + col] = 
					index2 < values[index1].length ? values[index1][index2] : BigInteger.ZERO;
			}
		}
    	mValues			= vals;
    	mColumnCount	= colCnt;
    }
    public DefaultBigIntegerMatrix(ReadableBigIntegerMatrix mx) {
    	mValues			= mx.toBigIntegerArray();
    	mColumnCount	= mx.getColumnCount();
    }
    
    public static DefaultBigIntegerMatrix diag(BigInteger[] values) {
    	final int len = values.length;
        DefaultBigIntegerMatrix mx = new DefaultBigIntegerMatrix(new BigInteger[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, values[ii]);
        }
        return mx;
    }
    
    public static DefaultBigIntegerMatrix diag(BigInteger value, int len) {
    	final DefaultBigIntegerMatrix mx = new DefaultBigIntegerMatrix(new BigInteger[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, value);
        }
        return mx;        
    }
    
    public static DefaultBigIntegerMatrix identity(int len) {
        return diag(BigInteger.ONE, len);
    }
    
    public BigIntegerOperations getNumberOperations() {
    	return BigIntegerOperations.instance();
    }
    
    public MatrixOperations<BigInteger> getMatrixOperations() {
    	return BigIntegerMatrixOperations.instance();
    }

    public int getSignumAt(int row, int col) {    	
    	return getBigIntegerValueAt(row, col).signum();
    }

    public BigInteger getBigIntegerNumeratorAt(int row, int col) {
    	return getBigIntegerValueAt(row, col);
    }
    
    public BigInteger getBigIntegerDenominatorAt(int row, int col) {
    	return BigInteger.ONE;
    }

    public double getDoubleValueAt(int row, int col) {
        return getBigIntegerValueAt(row, col).doubleValue();
    }
    
    public BigFraction getBigFractionValueAt(int row, int col) {
    	return BigFraction.valueOf(getBigIntegerValueAt(row, col));
    }

    public int getRowCount() {
        return mColumnCount > 0 ? mValues.length / mColumnCount : 0;
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public void setValueAt(int row, int col, BigInteger value) {
        mValues[row * mColumnCount + col] = value;
    }
    // cj: b
    public void setValueAt_BigInteger(int row, int col, BigInteger value) {
        mValues[row * mColumnCount + col] = value;
    }
    // cj: e
    public void setValueAt(int row, int col, int value) {
        setValueAt(row, col, BigInteger.valueOf(value));
    }
    public void setValueAt(int row, int col, long value) {
        setValueAt(row, col, BigInteger.valueOf(value));
    }
    public void setValueAt(int row, int col, Integer value) {
        setValueAt(row, col, BigInteger.valueOf(value.intValue()));
    }
    public void setValueAt(int row, int col, Long value) {
    	setValueAt(row, col, value.longValue());
    }

    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        final BigInteger[] tmpRow = new BigInteger[mColumnCount];
        System.arraycopy(mValues, rowA * mColumnCount, tmpRow, 0, mColumnCount);
        System.arraycopy(mValues, rowB * mColumnCount, mValues, rowA * mColumnCount, mColumnCount);
        System.arraycopy(tmpRow, 0, mValues, rowB * mColumnCount, mColumnCount);        
    }

    public void swapColumns(int colA, int colB) {
    	if (colA != colB) {
    		int rows = getRowCount();
    		for (int row = 0; row < rows; row++) {
				final BigInteger tmp = getBigIntegerValueAt(row, colA);
				setValueAt(row, colA, getBigIntegerValueAt(row, colB));
				setValueAt(row, colB, tmp);
			}
    	}
    }

    public BigInteger getNumberValueAt(int row, int col) {
        return getBigIntegerValueAt(row, col);
    }
    
    public void toArray(long[] array) {
        if (array.length != mValues.length) {
            throw new IllegalArgumentException("expected array length " + mValues.length + " but found " + array.length);
        }
        System.arraycopy(mValues, 0, array, 0, array.length);
    }
    public static void toArray(ReadableLongMatrix mx, long[] array) {
    	final  int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
        if (array.length != rows * cols) {
            throw new IllegalArgumentException("expected array length " + (rows * cols) + " but found " + array.length);
        }
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                array[row * cols + col] = mx.getLongValueAt(row, col);
            }
        }
    }
    
    public double[] toDoubleArray() {
        final double[] array = new double[mValues.length];
        toArray(array);
        return array;
    }
    
    public long[] toLongArray() {
        final long[] array = new long[mValues.length];
        toArray(array);
        return array;
    }
    
    public void toArray(double[] array) {
        AbstractDoubleMatrix.toArray(this, array);
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

    public BigInteger[] getBigIntegerColumn(int col) {
        return getBigIntegerColumn(this, col);
    }
    public static BigInteger[] getBigIntegerColumn(ReadableBigIntegerMatrix mx, int col) {
    	final int rows = mx.getRowCount();
        final BigInteger[] res = new BigInteger[rows];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getBigIntegerValueAt(row, col);            
        }
        return res;
    }
    public BigInteger[] getBigIntegerRow(int row) {
        return getBigIntegerRow(this, row);
    }
    public static BigInteger[] getBigIntegerRow(ReadableBigIntegerMatrix mx, int row) {
    	final int cols = mx.getColumnCount();
        final BigInteger[] res = new BigInteger[cols];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getBigIntegerValueAt(row, col);
        }
        return res;
    }
    
    public BigInteger[][] getBigIntegerColumns() {
        return getBigIntegerColumns(this);
    }
    
    public BigInteger[][] getBigIntegerRows() {
        return getBigIntegerRows(this);
    }
    public static BigInteger[][] getBigIntegerRows(ReadableBigIntegerMatrix mx) {
    	final int rows = mx.getRowCount();
        final BigInteger[][] res = new BigInteger[rows][];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getBigIntegerRow(row);
        }
        return res;
    }
    public static BigInteger[][] getBigIntegerColumns(ReadableBigIntegerMatrix mx) {
    	final int cols = mx.getColumnCount();
    	final BigInteger[][] res = new BigInteger[cols][];
    	for (int col = 0; col < cols; col++) {
			res[col] = mx.getBigIntegerColumn(col);
		}
    	return res;
    }
    
    public BigInteger[][] getNumberRows() {
    	return getBigIntegerRows();
    }

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
    public static String toString(ReadableBigIntegerMatrix mx) {
        return toString(mx, "{", " }", " [", "]", "", "", "", ", ");
    }
    public static void writeTo(Writer writer, ReadableBigIntegerMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }
    public static void writeTo(OutputStream out, ReadableBigIntegerMatrix mx) {
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
    public static String toMultilineString(ReadableBigIntegerMatrix mx) {
        return toString(mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");
    }
    public static void writeToMultiline(Writer writer, ReadableBigIntegerMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }
    public static void writeToMultiline(OutputStream out, ReadableBigIntegerMatrix mx) {
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }    
    
    protected static String toString(ReadableBigIntegerMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        StringWriter sw = new StringWriter();
        writeTo(new PrintWriter(sw), mx, prefix, postfix, rowPrefix, rowPostfix, rowSeparator, colPrefix, colPostfix, colSeparator);
        return sw.toString();
    }
    protected static void writeTo(PrintWriter writer, ReadableBigIntegerMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
    	final int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
        writer.print(prefix);
        for (int row = 0; row < rows; row++) {
            if (row > 0) writer.print(rowSeparator);
            writer.print(rowPrefix);            
            for (int col = 0; col < cols; col++) {
                if (col > 0) writer.print(colSeparator);
                writer.print(colPrefix);
                writer.print(mx.getBigIntegerValueAt(row, col));
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
	public DefaultBigIntegerMatrix clone() {
        return new DefaultBigIntegerMatrix(mValues.clone(), -1, mColumnCount);
    }
    public DefaultBigIntegerMatrix newInstance(int rows, int cols) {
    	return new DefaultBigIntegerMatrix(rows, cols);
    }
    
    public DefaultBigIntegerMatrix newInstance(BigInteger[][] data, boolean rowsInDim1) {
    	final int rows, cols;
    	if (rowsInDim1) {
        	rows = data.length;
        	cols = rows == 0 ? 0 : data[0].length;
    	}
    	else {
        	cols = data.length;
        	rows = cols == 0 ? 0 : data[0].length;
    	}
    	final BigInteger[] values = new BigInteger[rows * cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final BigInteger val = rowsInDim1 ? data[row][col] : data[col][row];
				values[row * cols + col] = val;
			}
		}
    	return new DefaultBigIntegerMatrix(values, rows, cols);
    }
    
    public BigIntegerMatrix toBigIntegerMatrix(boolean enforceNewInstance) {
        return enforceNewInstance ? (BigIntegerMatrix)clone() : this;
    }
    
    public BigIntegerMatrix toWritableMatrix(boolean enforceNewInstance) {
    	return toBigIntegerMatrix(enforceNewInstance);
    }
    public BigIntegerMatrix toReadableMatrix(boolean enforceNewInstance) {
    	return toBigIntegerMatrix(enforceNewInstance);
    }
    
    public BigIntegerRationalMatrix toBigIntegerRationalMatrix(boolean enforceNewInstance) {
    	return new DefaultBigIntegerRationalMatrix(this); 
    }
    
    public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
        return new DefaultDoubleMatrix(this);
    }
    
	public void addRowToOtherRow(int srcRow, long srcFactor, int dstRow, long dstFactor) {
    	addRowToOtherRow(srcRow, BigInteger.valueOf(srcFactor), dstRow, BigInteger.valueOf(dstFactor));
	}
    public void addRowToOtherRow(int srcRow, int srcFactor, int dstRow, int dstFactor) {
    	addRowToOtherRow(srcRow, BigInteger.valueOf(srcFactor), dstRow, BigInteger.valueOf(dstFactor));
    }
    public void addRowToOtherRow(int srcRow, BigInteger srcFactor, int dstRow, BigInteger dstFactor) {        
        final int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
        	final BigInteger srcProd = srcFactor.multiply(getBigIntegerValueAt(srcRow, col));
        	final BigInteger dstProd = dstFactor.multiply(getBigIntegerValueAt(dstRow, col));
            setValueAt(dstRow, col, srcProd.add(dstProd));
        }
    }
    
	public void add(int row, int col, BigInteger value) {
		setValueAt(row, col, value.add(getBigIntegerValueAt(row, col)));
	}
    public void add(int row, int col, int value) {
    	add(row, col, BigInteger.valueOf(value));
    }
    public void add(int row, int col, long value) {
        setValueAt(row, col, BigInteger.valueOf(value));
    }
    
    public void multiply(int row, int col, int factor) {
    	multiply(row, col, BigInteger.valueOf(factor));
    }
    public void multiply(int row, int col, long factor) {
        setValueAt(row, col, BigInteger.valueOf(factor));
    }
    public void multiply(int row, int col, BigInteger factor) {
        setValueAt(row, col, factor.multiply(getBigIntegerValueAt(row, col)));
    }
    
    public void multiplyRow(int row, int factor) {
    	multiplyRow(row, BigInteger.valueOf(factor));
    }
    public void multiplyRow(int row, long factor) {
    	multiplyRow(row, BigInteger.valueOf(factor));
    }
    public void multiplyRow(int row, BigInteger factor) {
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            multiply(row, col, factor);
        }
    }
    
    @Override
	public int hashCode() {
        int value = mColumnCount;
        for (int ii = 0; ii < mValues.length; ii++) {
        	value ^= (int)(mValues[ii].hashCode() ^ (mValues[ii].hashCode() >>> 32));
        }
        return value;
    }
    
    @Override
	public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof LongMatrix) {
            final LongMatrix other = (LongMatrix)obj;
            final int rows = getRowCount();
            if (rows != other.getRowCount()) return false;
            final int cols = getColumnCount();
            if (cols != other.getColumnCount()) return false;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                	final BigInteger myVal = getBigIntegerValueAt(row, col); 
                	final BigInteger otVal = other.getBigIntegerValueAt(row, col); 
                    if (!myVal.equals(otVal)) return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public BigIntegerMatrix subBigIntegerMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	final DefaultBigIntegerMatrix res = new DefaultBigIntegerMatrix(rowEnd - rowStart, colEnd - colStart);
    	for (int row = 0; row < res.getRowCount(); row++) {
			for (int col = 0; col < res.getColumnCount(); col++) {
				res.setValueAt(row, col, getBigIntegerValueAt(rowStart + row, colStart + col));
			}
		}
    	return res;
    }
    public BigIntegerRationalMatrix subBigIntegerRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subBigIntegerMatrix(rowStart, rowEnd, colStart, colEnd).toBigIntegerRationalMatrix(false /*cloned anyway*/);
    }
    public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subBigIntegerMatrix(rowStart, rowEnd, colStart, colEnd).toDoubleMatrix(false /*clonded anyway*/);
    }
    public DefaultBigIntegerMatrix transposeR() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	final DefaultBigIntegerMatrix tr = new DefaultBigIntegerMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getBigIntegerValueAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultBigIntegerMatrix transpose() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	final DefaultBigIntegerMatrix tr = new DefaultBigIntegerMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getBigIntegerValueAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultBigIntegerMatrix transposeW() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	final DefaultBigIntegerMatrix tr = new DefaultBigIntegerMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getBigIntegerValueAt(row, col));
			}
		}
    	return tr;
    }

	public void toArray(BigInteger[] array) {
        if (array.length != mValues.length) {
            throw new IllegalArgumentException("expected array length " + mValues.length + " but found " + array.length);
        }
        if (mValues != array) {
        	System.arraycopy(mValues, 0, array, 0, mValues.length);
        }
	}

	public BigInteger[] toBigIntegerArray() {
        final BigInteger[] array = new BigInteger[mValues.length];
        toArray(array);
        return array;
	}

	public BigInteger getBigIntegerValueAt(int row, int col) {
		return mValues[row * mColumnCount + col];
	}
	
	public void negate(int row, int col) {
		final BigInteger val = getBigIntegerValueAt(row, col);
		setValueAt(row, col, val.signum() == 0 ? BigInteger.ZERO : val.negate());
	}

}
