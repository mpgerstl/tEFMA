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
import ch.javasoft.math.ops.LongOperations;
import ch.javasoft.smx.iface.BigIntegerMatrix;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.LongMatrix;
import ch.javasoft.smx.iface.LongRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableLongMatrix;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.matrix.LongMatrixOperations;

/**
 * The <code>DefaultLongMatrix</code> is the default implementation of LongMatrix. It
 * contains a 1-dimensional array with all the matrix values.
 */
public class DefaultLongMatrix implements LongMatrix {
    
    private static final String NL = System.getProperty("line.separator");

    private int		mColumnCount;
    private long[]	mValues;
    
    public DefaultLongMatrix(int rowCount, int colCount) {
        this(new long[rowCount * colCount], rowCount, colCount);
    }
    
    public DefaultLongMatrix(long[] values, int rowCount, int columnCount) {
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
     * Initializes the matrix from a 2-dim long array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the second dimension otherwise. 
     * If the second dimension has different lengths, the maximum value
     * is taken, missing values are initialized with 0.
     * 
     * @param values		The 2-dim long array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultLongMatrix(long[][] values, boolean rowsInDim1) {
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
    	final long[] vals = new long[rowCnt * colCnt];
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
     * Initializes the matrix from a 2-dim long array. The resulting
     * rows relate to the first dimension if <code>rowsInDim1</code> is
     * true, or to the second dimension otherwise. 
     * If the second dimension has different lengths, the maximum value
     * is taken, missing values are initialized with 0.
     * 
     * @param values		The 2-dim long array with the source values
     * @param rowsInDim1	If true, the 1<sup>st</sup> dimension will become
     * 						rows in the resulting <code>DefaultDoubleMatrix</code>,
     * 						the columns relate to the 2<sup>nd</sup> dimension.
     * 						If false, it is vice versa.
     */
    public DefaultLongMatrix(Long[][] values, boolean rowsInDim1) {
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
    	final long[] vals = new long[rowCnt * colCnt];
    	for (int row = 0; row < rowCnt; row++) {
			for (int col = 0; col < colCnt; col++) {
				int index1 = rowsInDim1 ? row : col;
				int index2 = rowsInDim1 ? col : row;
				vals[row * colCnt + col] = 
					index2 < values[index1].length ? values[index1][index2].longValue() : 0;
			}
		}
    	mValues			= vals;
    	mColumnCount	= colCnt;
    }
    public DefaultLongMatrix(ReadableLongMatrix mx) {
    	mValues			= mx.toLongArray();
    	mColumnCount	= mx.getColumnCount();
    }
    
    public static DefaultLongMatrix diag(long[] values) {
    	final int len = values.length;
        DefaultLongMatrix mx = new DefaultLongMatrix(new long[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, values[ii]);
        }
        return mx;
    }
    
    public static DefaultLongMatrix diag(long value, int len) {
    	final DefaultLongMatrix mx = new DefaultLongMatrix(new long[len * len], len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, value);
        }
        return mx;        
    }
    
    public static DefaultLongMatrix identity(int len) {
        return diag(1, len);
    }
    
    public LongOperations getNumberOperations() {
    	return LongOperations.instance();
    }
    
    public MatrixOperations<Long> getMatrixOperations() {
    	return LongMatrixOperations.instance();
    }

    public long getLongValueAt(int row, int col) {
        return mValues[row * mColumnCount + col];
    }

    public int getSignumAt(int row, int col) {    	
    	final long val = getLongValueAt(row, col);
    	return val == 0 ? 0 : val < 0d ? -1 : 1;
    }

    public long getLongNumeratorAt(int row, int col) {
    	return getLongValueAt(row, col);
    }
    
    public long getLongDenominatorAt(int row, int col) {
    	return 1L;
    }
    
    public BigInteger getBigIntegerNumeratorAt(int row, int col) {
    	return BigInteger.valueOf(getLongNumeratorAt(row, col));
    }
    
    public BigInteger getBigIntegerDenominatorAt(int row, int col) {
    	return BigInteger.ONE;
    }

    public double getDoubleValueAt(int row, int col) {
        return getLongValueAt(row, col);
    }
    
    public BigFraction getBigFractionValueAt(int row, int col) {
    	return new BigFraction(
    		getBigIntegerNumeratorAt(row, col), getBigIntegerDenominatorAt(row, col)
    	);
    }

    public int getRowCount() {
        return mColumnCount > 0 ? mValues.length / mColumnCount : 0;
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public void setValueAt(int row, int col, int value) {
        mValues[row * mColumnCount + col] = value;
    }
    public void setValueAt(int row, int col, long value) {
        mValues[row * mColumnCount + col] = value;
    }
    public void setValueAt(int row, int col, Integer value) {
    	setValueAt(row, col, value.intValue());
    }
    public void setValueAt(int row, int col, Long value) {
    	setValueAt(row, col, value.longValue());
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
     */
    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        final long[] tmpRow = new long[mColumnCount];
        System.arraycopy(mValues, rowA * mColumnCount, tmpRow, 0, mColumnCount);
        System.arraycopy(mValues, rowB * mColumnCount, mValues, rowA * mColumnCount, mColumnCount);
        System.arraycopy(tmpRow, 0, mValues, rowB * mColumnCount, mColumnCount);        
    }

    public void swapColumns(int colA, int colB) {
    	if (colA != colB) {
    		int rows = getRowCount();
    		for (int row = 0; row < rows; row++) {
				long tmp = getLongValueAt(row, colA);
				setValueAt(row, colA, getLongValueAt(row, colB));
				setValueAt(row, colB, tmp);
			}
    	}
    }

    public Long getNumberValueAt(int row, int col) {
        return Long.valueOf(getLongValueAt(row, col));
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

    public long[] getLongColumn(int col) {
        return getLongColumn(this, col);
    }
    public static long[] getLongColumn(ReadableLongMatrix mx, int col) {
    	final int rows = mx.getRowCount();
        final long[] res = new long[rows];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getLongValueAt(row, col);            
        }
        return res;
    }
    public long[] getLongRow(int row) {
        return getLongRow(this, row);
    }
    public static long[] getLongRow(ReadableLongMatrix mx, int row) {
    	final int cols = mx.getColumnCount();
        final long[] res = new long[cols];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getLongValueAt(row, col);
        }
        return res;
    }
    
    public long[][] getLongColumns() {
        return getLongColumns(this);
    }
    public static long[][] getLongColumns(ReadableLongMatrix mx) {
    	final int cols = mx.getColumnCount();
        final long[][] res = new long[cols][];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getLongColumn(col);
        }
        return res;
    }
    
    public long[][] getLongRows() {
        return getLongRows(this);
    }
    public static long[][] getLongRows(ReadableLongMatrix mx) {
    	final int rows = mx.getRowCount();
        final long[][] res = new long[rows][];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getLongRow(row);
        }
        return res;
    }
    
    public Long[][] getNumberRows() {
    	return getNumberRows(this);
    }
    public static Long[][] getNumberRows(ReadableLongMatrix<Long> mx) {
    	final int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
    	final Long[][] data = new Long[rows][cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				data[row][col] = Long.valueOf(mx.getLongValueAt(row, col));
			}
		}
    	return data;
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
    public static String toString(ReadableLongMatrix mx) {
        return toString(mx, "{", " }", " [", "]", "", "", "", ", ");
    }
    public static void writeTo(Writer writer, ReadableLongMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }
    public static void writeTo(OutputStream out, ReadableLongMatrix mx) {
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
    public static String toMultilineString(ReadableLongMatrix mx) {
        return toString(mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");
    }
    public static void writeToMultiline(Writer writer, ReadableLongMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }
    public static void writeToMultiline(OutputStream out, ReadableLongMatrix mx) {
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }    
    
    protected static String toString(ReadableLongMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        StringWriter sw = new StringWriter();
        writeTo(new PrintWriter(sw), mx, prefix, postfix, rowPrefix, rowPostfix, rowSeparator, colPrefix, colPostfix, colSeparator);
        return sw.toString();
    }
    protected static void writeTo(PrintWriter writer, ReadableLongMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
    	final int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
        writer.print(prefix);
        for (int row = 0; row < rows; row++) {
            if (row > 0) writer.print(rowSeparator);
            writer.print(rowPrefix);            
            for (int col = 0; col < cols; col++) {
                if (col > 0) writer.print(colSeparator);
                writer.print(colPrefix);
                writer.print(mx.getLongValueAt(row, col));
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
	public DefaultLongMatrix clone() {
        return new DefaultLongMatrix(mValues.clone(), -1, mColumnCount);
    }
    public DefaultLongMatrix newInstance(int rows, int cols) {
    	return new DefaultLongMatrix(rows, cols);
    }
    
    public DefaultLongMatrix newInstance(Long[][] data, boolean rowsInDim1) {
    	final int rows, cols;
    	if (rowsInDim1) {
        	rows = data.length;
        	cols = rows == 0 ? 0 : data[0].length;
    	}
    	else {
        	cols = data.length;
        	rows = cols == 0 ? 0 : data[0].length;
    	}
    	final long[] values = new long[rows * cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final Long val = rowsInDim1 ? data[row][col] : data[col][row];
				values[row * cols + col] = val.longValue();
			}
		}
    	return new DefaultLongMatrix(values, rows, cols);
    }
    
    public LongMatrix toLongMatrix(boolean enforceNewInstance) {
        return enforceNewInstance ? (LongMatrix)clone() : this;
    }
    
    public LongMatrix toWritableMatrix(boolean enforceNewInstance) {
    	return toLongMatrix(enforceNewInstance);
    }
    public LongMatrix toReadableMatrix(boolean enforceNewInstance) {
    	return toLongMatrix(enforceNewInstance);
    }
    
    public LongRationalMatrix toLongRationalMatrix(boolean enforceNewInstance) {
    	// TODO impl
    	throw new RuntimeException("not implemented yet");
    }

    public BigIntegerRationalMatrix toBigIntegerRationalMatrix(boolean enforceNewInstance) {
    	return new DefaultBigIntegerRationalMatrix(toLongArray(), getRowCount(), getColumnCount()); 
    }
    
    public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
        return new DefaultDoubleMatrix(toDoubleArray(), getRowCount(), getColumnCount());
    }
    
    public void addRowToOtherRow(int srcRow, int srcFactor, int dstRow, int dstFactor) {
    	addRowToOtherRow(srcRow, (long)srcFactor, dstRow, (long)dstFactor);
    }
    public void addRowToOtherRow(int srcRow, long srcFactor, int dstRow, long dstFactor) {        
        final int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            setValueAt(dstRow, col, dstFactor * getLongValueAt(dstRow, col) + srcFactor * getLongValueAt(srcRow, col));
        }
    }
    
    public void add(int row, int col, int value) {
    	add(row, col, (long)value);
    }
    public void add(int row, int col, long value) {
        setValueAt(row, col, getLongValueAt(row, col) + value);
    }
    
    public void multiply(int row, int col, int factor) {
    	multiply(row, col, (long)factor);
    }
    public void multiply(int row, int col, long factor) {
        setValueAt(row, col, factor * getLongValueAt(row, col));
    }
    
    public void multiplyRow(int row, int factor) {
    	multiplyRow(row, (long)factor);
    }
    public void multiplyRow(int row, long factor) {
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            multiply(row, col, factor);
        }
    }
    
    @Override
	public int hashCode() {
        int value = mColumnCount;
        for (int ii = 0; ii < mValues.length; ii++) {
        	value ^= (int)(mValues[ii] ^ (mValues[ii] >>> 32));
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
                    if (getLongValueAt(row, col) != other.getLongValueAt(row, col)) return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public LongMatrix subLongMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	final DefaultLongMatrix res = new DefaultLongMatrix(rowEnd - rowStart, colEnd - colStart);
    	for (int row = 0; row < res.getRowCount(); row++) {
			for (int col = 0; col < res.getColumnCount(); col++) {
				res.setValueAt(row, col, getLongValueAt(rowStart + row, colStart + col));
			}
		}
    	return res;
    }
    public LongRationalMatrix subLongRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subLongMatrix(rowStart, rowEnd, colStart, colEnd).toLongRationalMatrix(false /*cloned anyway*/);
    }
    public BigIntegerRationalMatrix subBigIntegerRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subLongMatrix(rowStart, rowEnd, colStart, colEnd).toBigIntegerRationalMatrix(false /*cloned anyway*/);
    }
    public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subLongMatrix(rowStart, rowEnd, colStart, colEnd).toDoubleMatrix(false /*clonded anyway*/);
    }
    public ReadableBigIntegerMatrix subBigIntegerMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subLongMatrix(rowStart, rowEnd, colStart, colEnd).toBigIntegerMatrix(false /*clonded anyway*/);
    }
    public DefaultLongMatrix transposeR() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	final DefaultLongMatrix tr = new DefaultLongMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getLongValueAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultLongMatrix transpose() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	final DefaultLongMatrix tr = new DefaultLongMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getLongValueAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultLongMatrix transposeW() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	final DefaultLongMatrix tr = new DefaultLongMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, getLongValueAt(row, col));
			}
		}
    	return tr;
    }

	public BigInteger getBigIntegerValueAt(int row, int col) {
		return BigInteger.valueOf(getLongValueAt(row, col));
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
		setValueAt(row, col, -getLongValueAt(row, col));
	}

}
