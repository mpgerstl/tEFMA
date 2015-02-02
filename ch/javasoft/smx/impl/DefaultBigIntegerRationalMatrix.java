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
import ch.javasoft.math.ops.BigFractionOperations;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.matrix.BigIntegerRationalMatrixOperations;
import ch.javasoft.util.StringUtil;

public class DefaultBigIntegerRationalMatrix implements BigIntegerRationalMatrix {
	
	private static final String NL = StringUtil.LINE_SEPARATOR;
	
    private final int			mRowCount;
    private final int			mColumnCount;
	private final BigInteger[] 	mNumerators;
	private final BigInteger[] 	mDenominators;
	
	public DefaultBigIntegerRationalMatrix(int rowCount, int colCount) {
		if (rowCount < 0) throw new IllegalArgumentException("negative row count: " + rowCount);
		if (colCount < 0) throw new IllegalArgumentException("negative column count: " + colCount);
		final int vals  = rowCount * colCount;
		mRowCount		= rowCount;
		mColumnCount	= colCount;
		mNumerators		= new BigInteger[vals];
		mDenominators	= new BigInteger[vals];
		Arrays.fill(mNumerators, BigInteger.ZERO);
		Arrays.fill(mDenominators, BigInteger.ONE);
	}

	public DefaultBigIntegerRationalMatrix(ReadableBigIntegerRationalMatrix mx) {
		this(mx.getRowCount(), mx.getColumnCount());
		for (int row = 0; row < mx.getRowCount(); row++) {
			for (int col = 0; col < mx.getColumnCount(); col++) {
				setValueAt(row, col, mx.getBigIntegerNumeratorAt(row, col), mx.getBigIntegerDenominatorAt(row, col));
			}
		}
	}
	public DefaultBigIntegerRationalMatrix(ReadableMatrix<BigFraction> mx) {
		this(mx.getRowCount(), mx.getColumnCount());
		for (int row = 0; row < mx.getRowCount(); row++) {
			for (int col = 0; col < mx.getColumnCount(); col++) {
				setValueAt(row, col, mx.getNumberValueAt(row, col));
			}
		}
	}
	protected DefaultBigIntegerRationalMatrix(DefaultBigIntegerRationalMatrix toClone) {
		mRowCount		= toClone.mRowCount;
		mColumnCount	= toClone.mColumnCount;
		mNumerators		= toClone.mNumerators.clone();
		mDenominators	= toClone.mDenominators.clone();
	}
	
	public DefaultBigIntegerRationalMatrix(String[] values, int rowCount, int colCount) {
		this(toBigIntegerFractionNumbers(values), rowCount, colCount);
	}
	public DefaultBigIntegerRationalMatrix(double[][] values, boolean rowIsFirstDim, boolean adjustDoubleValues) {
		this(toBigIntegerFractionNumbers(values, rowIsFirstDim, adjustDoubleValues), values.length, values.length == 0 ? 0 : values[0].length);
	}
	public DefaultBigIntegerRationalMatrix(double[] values, int rowCount, int colCount, boolean adjustDoubleValues) {
		this(toBigIntegerFractionNumbers(values, adjustDoubleValues, 0d), rowCount, colCount);
	}
	public DefaultBigIntegerRationalMatrix(double[] values, int rowCount, int colCount, double adjustTolerance) {
		this(toBigIntegerFractionNumbers(values, true, adjustTolerance), rowCount, colCount);
	}
	public DefaultBigIntegerRationalMatrix(String[] numerators, String[] denominators, int rowCount, int colCount) {
		this(toBigIntegers(numerators), toBigIntegers(denominators), rowCount, colCount);
	}
	public DefaultBigIntegerRationalMatrix(long[] values, int rowCount, int colCount) {
		this(toBigIntegers(values), ones(values.length), rowCount, colCount);
	}
	public DefaultBigIntegerRationalMatrix(long[] numerators, long[] denominators, int rowCount, int colCount) {
		this(toBigIntegers(numerators), toBigIntegers(denominators), rowCount, colCount);
	}

	public DefaultBigIntegerRationalMatrix(BigFraction[][] values, boolean rowIsFirstDim) {
		this(toBigIntegerFractionNumbers(values, rowIsFirstDim), values.length, values.length == 0 ? 0 : values[0].length);		
	}

	public DefaultBigIntegerRationalMatrix(BigFraction[] values, int rowCount, int colCount) {
		int expLen = rowCount * colCount;
		if (expLen != values.length) {
			throw new IllegalArgumentException("expected " + expLen + " numerators, but found " + values.length);
		}
		mRowCount		= rowCount;
		mColumnCount	= colCount;
		mNumerators		= new BigInteger[expLen];
		mDenominators	= new BigInteger[expLen];
		for (int i = 0; i < values.length; i++) {
			mNumerators[i] 		= values[i].getNumerator();
			mDenominators[i]	= values[i].getDenominator();
		}
	}
	public DefaultBigIntegerRationalMatrix(BigInteger[] numerators, BigInteger[] denominators, int rowCount, int colCount) {
		int expLen = rowCount * colCount;
		if (expLen != numerators.length) {
			throw new IllegalArgumentException("expected " + expLen + " numerators, but found " + numerators.length);
		}
		if (expLen != denominators.length) {
			throw new IllegalArgumentException("expected " + expLen + " denominators, but found " + denominators.length);
		}
		mNumerators		= numerators;
		mDenominators	= denominators;
		mRowCount		= rowCount;
		mColumnCount	= colCount;
	}
	
	public BigFractionOperations getNumberOperations() {
		return BigFractionOperations.instance();
	}
	public MatrixOperations<BigFraction> getMatrixOperations() {
		return BigIntegerRationalMatrixOperations.instance();
	}
	
	public BigFraction getBigFractionValueAt(int row, int col) {
		return new BigFraction(
			getBigIntegerNumeratorAt(row, col), 
			getBigIntegerDenominatorAt(row, col)
		);
	}

	public BigInteger getBigIntegerNumeratorAt(int row, int col) {		
		return mNumerators[row * mColumnCount + col];
	}

	public BigInteger getBigIntegerDenominatorAt(int row, int col) {
		return mDenominators[row * mColumnCount + col];
	}

    public int getSignumAt(int row, int col) {    	
    	final int num = getBigIntegerNumeratorAt(row, col).signum();
    	final int den = getBigIntegerDenominatorAt(row, col).signum();
    	return num * den;
    }

    public BigIntegerRationalMatrix subBigIntegerRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
		int rows = getRowCount();
		int cols = getColumnCount();
		if (rowEnd < rowStart) throw new IllegalArgumentException("rowEnd < rowStart");
		if (colEnd < colStart) throw new IllegalArgumentException("colEnd < colStart");
		if (rowStart < 0) throw new IllegalArgumentException("rowStart < 0");
		if (colStart < 0) throw new IllegalArgumentException("colStart < 0");
		if (rowEnd > rows) throw new IllegalArgumentException("rowEnd > getRowCount()");
		if (colEnd > cols) throw new IllegalArgumentException("colEnd > getColumnCount()");
		int newRows = rowEnd - rowStart;
		int newCols = colEnd - colStart;
		int newVals = newRows * newCols;
		BigInteger[] numerators = new BigInteger[newVals]; 
		BigInteger[] denominators = new BigInteger[newVals];
		for (int row = 0; row < newRows; row++) {
			System.arraycopy(mNumerators, (rowStart + row) * cols + colStart, numerators, row * newCols, newCols);
			System.arraycopy(mDenominators, (rowStart + row) * cols + colStart, denominators, row * newCols, newCols);
		}
		return new DefaultBigIntegerRationalMatrix(numerators, denominators, newRows, newCols);
	}

	public BigIntegerRationalMatrix toBigIntegerRationalMatrix(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}
	
	public BigIntegerRationalMatrix toWritableMatrix(boolean enforceNewInstance) {
		return toBigIntegerRationalMatrix(enforceNewInstance);
	}
	public BigIntegerRationalMatrix toReadableMatrix(boolean enforceNewInstance) {
		return toBigIntegerRationalMatrix(enforceNewInstance);
	}

	public double[] getDoubleColumn(int col) {
		double[] colData = new double[getRowCount()];
		for (int row = 0; row < colData.length; row++) {
			colData[row] = getDoubleValueAt(row, col);
		}
		return colData;
	}

	public double[][] getDoubleColumns() {
		double[][] cols = new double[getColumnCount()][];
		for (int col = 0; col < cols.length; col++) {
			cols[col] = getDoubleColumn(col);
		}
		return cols;
	}

	public double[] getDoubleRow(int row) {
		double[] rowData = new double[getColumnCount()];
		for (int col = 0; col < rowData.length; col++) {
			rowData[col] = getDoubleValueAt(row, col);
		}
		return rowData;
	}

	public double[][] getDoubleRows() {
		double[][] rows = new double[getRowCount()][];
		for (int row = 0; row < rows.length; row++) {
			rows[row] = getDoubleRow(row);
		}
		return rows;
	}

	public double getDoubleValueAt(int row, int col) {
		return BigFraction.toDouble(
			getBigIntegerNumeratorAt(row, col), 
			getBigIntegerDenominatorAt(row, col)
		);
	}

	public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
		// TODO can be done more efficient
		return subBigIntegerRationalMatrix(rowStart, rowEnd, colStart, colEnd).toDoubleMatrix(false /*anyway cloned*/);
	}

	public void toArray(double[] array) {
        if (array.length != mNumerators.length) {
            throw new IllegalArgumentException("expected array length " + mNumerators.length + " but found " + array.length);
        }
        for (int ii = 0; ii < array.length; ii++) {
            array[ii] = BigFraction.toDouble(mNumerators[ii], mDenominators[ii]);
        }
	}

	public double[] toDoubleArray() {
		double[] vals = new double[mNumerators.length];
		toArray(vals);
		return vals;
	}

	public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
		return new DefaultDoubleMatrix(toDoubleArray(), getRowCount(), getColumnCount());
	}

	public BigFraction getNumberValueAt(int row, int col) {
		return getBigFractionValueAt(row, col);
	}

	public int getColumnCount() {
		return mColumnCount;
	}

	public int getRowCount() {
		return mRowCount;
	}

    public void add(int row, int col, BigInteger numerator, BigInteger denominator) {
		BigFraction cur = getBigFractionValueAt(row, col);
		BigFraction add = new BigFraction(numerator, denominator);
		BigFraction sum = cur.add(add).reduce();
		setValueAt(row, col, sum.getNumerator(), sum.getDenominator());
	}

	public void addRowToOtherRow(int srcRow, BigInteger srcNumerator, BigInteger srcDenominator, int dstRow, BigInteger dstNumerator, BigInteger dstDenominator) {
		final int cols = getColumnCount();
		for (int col = 0; col < cols; col++) {
			BigFraction src = getBigFractionValueAt(srcRow, col);
			BigFraction dst = getBigFractionValueAt(dstRow, col);
			src = src.multiply(new BigFraction(srcNumerator, srcDenominator));
			dst = src.multiply(new BigFraction(dstNumerator, dstDenominator));
			BigFraction res = dst.add(src).reduce();
			setValueAt(dstRow, col, res.getNumerator(), res.getDenominator());
		}
	}

	public void multiply(int row, int col, BigInteger numerator, BigInteger denominator) {
		BigFraction cur = getBigFractionValueAt(row, col);
		BigFraction mul = new BigFraction(numerator, denominator);
		BigFraction pro = cur.multiply(mul).reduce();
		setValueAt(row, col, pro.getNumerator(), pro.getDenominator());
	}

	public void multiplyRow(int row, BigInteger numerator, BigInteger denominator) {
		final int cols = getColumnCount();
		final BigFraction mul = new BigFraction(numerator, denominator);
		for (int col = 0; col < cols; col++) {
			BigFraction cur = getBigFractionValueAt(row, col);
			cur = cur.multiply(mul).reduce();
			setValueAt(row, col, cur.getNumerator(), cur.getDenominator());
		}
	}

	public void setValueAt(int row, int col, BigInteger numerator, BigInteger denominator) {
		final int index = row * mColumnCount + col; 
		mNumerators[index]		= numerator;
		mDenominators[index]	= denominator;
	}

    public void setValueAt(int row, int col, BigFraction value) {
    	setValueAt(row, col, value.getNumerator(), value.getDenominator());
    }
	
    // cj: b
    public void setValueAt_BigFraction(int row, int col, BigFraction value) {
    	setValueAt(row, col, value.getNumerator(), value.getDenominator());
    }
    // cj: e
	
	public boolean reduce() {
		final int rows = getRowCount();
		final int cols = getColumnCount();
		boolean any = false;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				any |= reduceValueAt(row, col);
			}
		}
		return any;
	}
	
	public boolean reduceRow(int row) {
		final int cols = getColumnCount();
		boolean any = false;
		for (int col = 0; col < cols; col++) {
			any |= reduceValueAt(row, col);
		}
		return any;
	}
	
	public boolean reduceValueAt(int row, int col) {
		BigInteger num = getBigIntegerNumeratorAt(row, col);
		BigInteger den = getBigIntegerDenominatorAt(row, col);		
		if (num.signum() == 0) {
			if (num != BigInteger.ZERO || den != BigInteger.ONE) {
				setValueAt(row, col, BigInteger.ZERO, BigInteger.ONE);
				return true;
			}
			return false;
		}
		BigInteger gcd = num.gcd(den);
		if (0 != BigInteger.ONE.compareTo(gcd)) {
			if (den.signum() < 0) {
				gcd = gcd.negate();	
			}
			num = num.divide(gcd);
			den = den.divide(gcd);
			if (0 == BigInteger.ONE.compareTo(num)) num = BigInteger.ONE;
			if (0 == BigInteger.ONE.compareTo(den)) den = BigInteger.ONE;
			setValueAt(row, col, num, den);
			return true;
		}
		return false;
	}

	public void add(int row, int col, long numerator, long denominator) {
		add(row, col, BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}

	public void addRowToOtherRow(int srcRow, long srcNumerator, long srcDenominator, int dstRow, long dstNumerator, long dstDenominator) {
		addRowToOtherRow(
			srcRow, BigInteger.valueOf(srcNumerator), BigInteger.valueOf(srcDenominator), 
			dstRow, BigInteger.valueOf(dstNumerator), BigInteger.valueOf(dstDenominator)
		);
	}

	public void multiply(int row, int col, long numerator, long denominator) {
		multiply(row, col, BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}

	public void multiplyRow(int row, long numerator, long denominator) {
		multiplyRow(row, BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}

	public void setValueAt(int row, int col, long numerator, long denominator) {
		setValueAt(row, col, BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}

	public void add(int row, int col, long value) {
		add(row, col, BigInteger.valueOf(value), BigInteger.ONE);
	}

	public void addRowToOtherRow(int srcRow, long srcFactor, int dstRow, long dstFactor) {
		addRowToOtherRow(srcRow, BigInteger.valueOf(srcFactor), BigInteger.ONE, dstRow, BigInteger.valueOf(dstFactor), BigInteger.ONE);
	}

	public void multiply(int row, int col, long factor) {
		multiply(row, col, BigInteger.valueOf(factor), BigInteger.ONE);
	}

	public void multiplyRow(int row, long factor) {
		multiplyRow(row, BigInteger.valueOf(factor), BigInteger.ONE);
	}

	public void setValueAt(int row, int col, long value) {
		setValueAt(row, col, BigInteger.valueOf(value), BigInteger.ONE);
	}

	public void add(int row, int col, int value) {
		add(row, col, (long)value);
	}

	public void addRowToOtherRow(int srcRow, int srcFactor, int dstRow, int dstFactor) {
		addRowToOtherRow(srcRow, (long)srcFactor, dstRow, (long)dstFactor);
	}

	public void multiply(int row, int col, int factor) {
		multiply(row, col, (long)factor);
	}

	public void multiplyRow(int row, int factor) {
		multiplyRow(row, (long)factor);
	}

	public void setValueAt(int row, int col, int value) {
		setValueAt(row, col, (long)value);
	}

	public void swapColumns(int colA, int colB) {
    	if (colA != colB) {
    		int rows = getRowCount();
    		for (int row = 0; row < rows; row++) {
    			BigInteger numA = getBigIntegerNumeratorAt(row, colA);
    			BigInteger denA = getBigIntegerDenominatorAt(row, colA);
    			BigInteger numB = getBigIntegerNumeratorAt(row, colB);
    			BigInteger denB = getBigIntegerDenominatorAt(row, colB);
				setValueAt(row, colA, numB, denB);
				setValueAt(row, colB, numA, denA);
			}
    	}
	}

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableMatrix#swapRows(int, int)
     */
    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        final int cols = getColumnCount();
        BigInteger[] tmp = new BigInteger[cols];
        System.arraycopy(mNumerators, rowA * cols, tmp, 0, cols);
        System.arraycopy(mNumerators, rowB * cols, mNumerators, rowA * cols, cols);
        System.arraycopy(tmp, 0, mNumerators, rowB * cols, cols);        
        System.arraycopy(mDenominators, rowA * cols, tmp, 0, cols);
        System.arraycopy(mDenominators, rowB * cols, mDenominators, rowA * cols, cols);
        System.arraycopy(tmp, 0, mDenominators, rowB * cols, cols);        
    }

	public void add(int row, int col, int numerator, int denominator) {
		add(row, col, (long)numerator, (long)denominator);
	}

	public void addRowToOtherRow(int srcRow, int srcNumerator, int srcDenominator, int dstRow, int dstNumerator, int dstDenominator) {
		addRowToOtherRow(srcRow, (long)srcNumerator, (long)srcDenominator, dstRow, (long)dstNumerator, (long)dstDenominator);
	}

	public void multiply(int row, int col, int numerator, int denominator) {
		multiply(row, col, (long)numerator, (long)denominator);
	}

	public void multiplyRow(int row, int numerator, int denominator) {
		multiplyRow(row, (long)numerator, (long)denominator);
	}

	public void setValueAt(int row, int col, int numerator, int denominator) {
		setValueAt(row, col, (long)numerator, (long)denominator);
	}
	
	@Override
	public DefaultBigIntegerRationalMatrix clone() {
		return new DefaultBigIntegerRationalMatrix(this);
	}
	
    public DefaultBigIntegerRationalMatrix newInstance(int rows, int cols) {
    	return new DefaultBigIntegerRationalMatrix(rows, cols);
    }
    
    public DefaultBigIntegerRationalMatrix newInstance(BigFraction[][] data, boolean rowsInDim1) {
    	return new DefaultBigIntegerRationalMatrix(data, rowsInDim1);
    }

////////////////////////////////////////////////////////// static
	
	private static BigInteger[] toBigIntegers(String[] values) {
		BigInteger[] vals = new BigInteger[values.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = new BigInteger(values[i]);
		}
		return vals;
	}
	private static BigInteger[] toBigIntegers(long[] values) {
		BigInteger[] vals = new BigInteger[values.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = BigInteger.valueOf(values[i]);
		}
		return vals;
	}
	private static BigInteger[] ones(int len) {
		BigInteger[] ones = new BigInteger[len];
		for (int i = 0; i < ones.length; i++) {
			ones[i] = BigInteger.ONE;
		}
		return ones;
	}
	private static BigFraction[] toBigIntegerFractionNumbers(String[] values) {
		BigFraction[] vals = new BigFraction[values.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = BigFraction.valueOf(values[i]);
		}
		return vals;
	}
	private static BigFraction[] toBigIntegerFractionNumbers(double[] values, boolean adjustDoubleValues, double tolerance) {
		BigFraction[] vals = new BigFraction[values.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = adjustDoubleValues ?
				BigFraction.valueOfAdjusted(values[i], tolerance) :
				BigFraction.valueOf(values[i]);
		}
		return vals;
	}
	private static BigFraction[] toBigIntegerFractionNumbers(double[][] values, boolean rowIsFirstDim, boolean adjustDoubleValues) {
		final int rows, cols;
		if (rowIsFirstDim) {
			rows = values.length;
			cols = rows == 0 ? 0 : values[0].length;
		}
		else {
			cols = values.length;
			rows = cols == 0 ? 0 : values[0].length;
		}
		BigFraction[] vals = new BigFraction[rows * cols];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final double val = rowIsFirstDim ? values[row][col] : values[col][row];
				vals[row * cols + col] = adjustDoubleValues ?
					BigFraction.valueOfAdjusted(val) :
					BigFraction.valueOf(val);
			}
		}
		return vals;
	}
	private static BigFraction[] toBigIntegerFractionNumbers(BigFraction[][] values, boolean rowIsFirstDim) {
		final int rows, cols;
		if (rowIsFirstDim) {
			rows = values.length;
			cols = rows == 0 ? 0 : values[0].length;
		}
		else {
			cols = values.length;
			rows = cols == 0 ? 0 : values[0].length;
		}
		BigFraction[] vals = new BigFraction[rows * cols];
		for (int row = 0; row < rows; row++) {
			System.arraycopy(values[row], 0, vals, row * cols, values[row].length);
		}
		return vals;
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
    public static String toString(ReadableBigIntegerRationalMatrix mx) {
        return toString(mx, "{", " }", " [", "]", "", "", "", ", ");
    }
    public static void writeTo(Writer writer, ReadableBigIntegerRationalMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }
    public static void writeTo(OutputStream out, ReadableBigIntegerRationalMatrix mx) {
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
    public static String toMultilineString(ReadableBigIntegerRationalMatrix mx) {
        return toString(mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");
    }
    public static void writeToMultiline(Writer writer, ReadableBigIntegerRationalMatrix mx) {
        String sizeStr = mx.getRowCount() + "x" + mx.getColumnCount();
    	writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, sizeStr + " {" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }
    public static void writeToMultiline(OutputStream out, ReadableBigIntegerRationalMatrix mx) {
        String sizeStr = mx.getRowCount() + "x" + mx.getColumnCount();
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, sizeStr + " {" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }    
    
    protected static String toString(ReadableBigIntegerRationalMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        StringWriter sw = new StringWriter();
        writeTo(new PrintWriter(sw), mx, prefix, postfix, rowPrefix, rowPostfix, rowSeparator, colPrefix, colPostfix, colSeparator);
        return sw.toString();
    }
    protected static void writeTo(PrintWriter writer, ReadableBigIntegerRationalMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        int rows = mx.getRowCount();
        int cols = mx.getColumnCount();
        writer.print(prefix);
        for (int row = 0; row < rows; row++) {
            if (row > 0) writer.print(rowSeparator);
            writer.print(rowPrefix);            
            for (int col = 0; col < cols; col++) {
                if (col > 0) writer.print(colSeparator);
                writer.print(colPrefix);
                writer.print(mx.getBigFractionValueAt(row, col));
                writer.print(colPostfix);
            }
            writer.print(rowPostfix);
        }
        writer.print(postfix);
        writer.flush();
    }

    public DefaultBigIntegerRationalMatrix transposeW() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultBigIntegerRationalMatrix tr = new DefaultBigIntegerRationalMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, 
					getBigIntegerNumeratorAt(row, col),
					getBigIntegerDenominatorAt(row, col));
			}
		}
    	return tr;
    }

    public DefaultBigIntegerRationalMatrix transposeR() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultBigIntegerRationalMatrix tr = new DefaultBigIntegerRationalMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, 
					getBigIntegerNumeratorAt(row, col),
					getBigIntegerDenominatorAt(row, col));
			}
		}
    	return tr;
    }

    public DefaultBigIntegerRationalMatrix transpose() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultBigIntegerRationalMatrix tr = new DefaultBigIntegerRationalMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, 
					getBigIntegerNumeratorAt(row, col),
					getBigIntegerDenominatorAt(row, col));
			}
		}
    	return tr;
    }
    public BigFraction[][] getNumberRows() {
    	return getNumberRows(this);
    }
    public static BigFraction[][] getNumberRows(ReadableBigIntegerRationalMatrix<BigFraction> mx) {
    	final int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
    	final BigFraction[][] data = new BigFraction[rows][cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				data[row][col] = mx.getBigFractionValueAt(row, col);
			}
		}
    	return data;
    }

	public void add(int row, int col, BigInteger value) {
		add(row, col, value, BigInteger.ONE);
	}

	public void addRowToOtherRow(int srcRow, BigInteger srcFactor, int dstRow, BigInteger dstFactor) {
		addRowToOtherRow(srcRow, srcFactor, BigInteger.ONE, dstRow, dstFactor, BigInteger.ONE);
	}

	public void multiply(int row, int col, BigInteger factor) {
		multiply(row, col, factor, BigInteger.ONE);
	}

	public void multiplyRow(int row, BigInteger factor) {
		multiplyRow(row, factor, BigInteger.ONE);
	}

	public void setValueAt(int row, int col, BigInteger value) {
		setValueAt(row, col, value, BigInteger.ONE);
	}
       // cj: b
	public void setValueAt_BigInteger(int row, int col, BigInteger value) {
		setValueAt(row, col, value, BigInteger.ONE);
       }
       // cj: e
	
	public void negate(int row, int col) {
		final BigFraction val = getBigFractionValueAt(row, col);
		setValueAt(row, col, val.signum() == 0 ? BigFraction.ZERO : val.negate());
	}
	
}
