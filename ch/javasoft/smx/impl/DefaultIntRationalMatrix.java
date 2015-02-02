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
import ch.javasoft.math.NumberOperations;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.IntRationalMatrix;
import ch.javasoft.smx.iface.LongRationalMatrix;
import ch.javasoft.smx.iface.ReadableIntRationalMatrix;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.util.SmxIntegerUtil;
import ch.javasoft.util.numeric.IntegerUtil;

/**
 * The <code>DefaultIntRationalMatrix</code> is the default implementation of
 * IntRationalMatrix. It contains 2 int-arrays which contain the enumerator and 
 * denominator values for the matrix fractional integers.
 */
public class DefaultIntRationalMatrix implements IntRationalMatrix {
    
    private static final String NL = System.getProperty("line.separator");

    private int		mColumnCount;
    private int[]	mNumerators;
    private int[]	mDenominators;
    
    public DefaultIntRationalMatrix(int rowCount, int columnCount) {
        this(new int[rowCount * columnCount], null, rowCount, columnCount);
    }
    
    public DefaultIntRationalMatrix(int[] numerators, int[] denominators, int rowCount, int columnCount) {
        this(numerators, denominators, rowCount, columnCount, true);
    }
    private DefaultIntRationalMatrix(int[] numerators, int[] denominators, int rowCount, int columnCount, boolean checkDenominators) {
        if (denominators == null) {
            denominators = getOnes(numerators.length);
        }
        else {
            if (checkDenominators) {
                for (int ii = 0; ii < denominators.length; ii++) {
                    if (denominators[ii] <= 0) {
                        throw new IllegalArgumentException(
                                "denominator values must be > 0, but value " + ii + " is " + denominators[ii]
                        );
                    }
                }                            
            }
        }
        if (numerators.length != denominators.length) {
            throw new IllegalArgumentException(
                    "number of numerators not equal to number of denominators: " +
                    numerators.length + " != " + denominators.length
                    );
        }
        if (rowCount == -1) rowCount = numerators.length / columnCount;
        if (columnCount == -1) columnCount = numerators.length / rowCount;
        if (rowCount < 0 || columnCount < 0 || rowCount * columnCount != numerators.length) {
            throw new IllegalArgumentException(
                    "rowCount (" + rowCount + ") * columnCount (" + columnCount + 
                    ") != number of values (" + numerators.length + ")"
                    );
        }
        mNumerators	= numerators;
        mDenominators	= denominators;
        mColumnCount	= columnCount;
    }
    
    public DefaultIntRationalMatrix(ReadableIntRationalMatrix mx) {
    	int rows = mx.getRowCount();
    	int cols = mx.getColumnCount();
    	mNumerators	= new int[rows * cols];
    	mDenominators	= new int[rows * cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				mNumerators[row * cols + col]	= mx.getIntNumeratorAt(row, col);
				mDenominators[row * cols + col]		= mx.getIntDenominatorAt(row, col);
			}
		}
    	mColumnCount = cols;
    }

    public static DefaultIntRationalMatrix diag(int[] numerators, int[] denominators) {
        if (numerators.length != denominators.length) {
            throw new IllegalArgumentException("numerator and denominator length missmatch");
        }
        int len = numerators.length;
        DefaultIntRationalMatrix mx = new DefaultIntRationalMatrix(new int[len * len], null, len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, numerators[ii], denominators[ii]);
        }
        return mx;
    }
    
    public static DefaultIntRationalMatrix diag(int numerator, int denominator, int len) {
        DefaultIntRationalMatrix mx = new DefaultIntRationalMatrix(new int[len * len], null, len, len);
        for (int ii = 0; ii < len; ii++) {
            mx.setValueAt(ii, ii, numerator, denominator);
        }
        return mx;        
    }
    
    public static DefaultIntRationalMatrix identity(int len) {
        return diag(1, 1, len);
    }
    
    public NumberOperations getNumberOperations() {
    	throw new RuntimeException("not implemented");
    }
    public MatrixOperations getMatrixOperations() {
    	throw new RuntimeException("not implemented");
    }
    
    public int getIntNumeratorAt(int row, int col) {
        return mNumerators[row * mColumnCount + col];
    }

    public int getIntDenominatorAt(int row, int col) {
        return mDenominators[row * mColumnCount + col];
    }
    
    public int getSignumAt(int row, int col) {    	
    	final int num = getIntNumeratorAt(row, col);
    	final int den = getIntDenominatorAt(row, col);
    	return num == 0 ? 0 : (num < 0) == (den < 0) ? 1 : -1;
    }

    public long getLongNumeratorAt(int row, int col) {
    	return getIntNumeratorAt(row, col);
    }

    public long getLongDenominatorAt(int row, int col) {
    	return getIntDenominatorAt(row, col);
    }
    
    public BigInteger getBigIntegerNumeratorAt(int row, int col) {
    	return BigInteger.valueOf(getIntNumeratorAt(row, col));
    }

    public BigInteger getBigIntegerDenominatorAt(int row, int col) {
    	return BigInteger.valueOf(getIntDenominatorAt(row, col));
    }

    public double getDoubleValueAt(int row, int col) {
        return ((double)getIntNumeratorAt(row, col)) / ((double)getIntDenominatorAt(row, col));
    }

    public BigFraction getBigFractionValueAt(int row, int col) {
    	return new BigFraction(
    		getBigIntegerNumeratorAt(row, col), getBigIntegerDenominatorAt(row, col)
    	);
    }

    public int getRowCount() {
        return mNumerators.length / mColumnCount;
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public void setValueAt(int row, int col, int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("div by 0");
        }
        if (numerator == 0) denominator = 1;
        else if (denominator < 0) {
            numerator	= -numerator;
            denominator	= -denominator;
        }
        int index = row * mColumnCount + col;
        mNumerators[index]	= numerator;
        mDenominators[index]	= denominator;
    }

    public void setValueAt(int row, int col, int value) {
        setValueAt(row, col, value, 1);
    }

    public void swapRows(int rowA, int rowB) {
        if (rowA == rowB) return;
        int[] tmpRow = new int[mColumnCount];
        System.arraycopy(mNumerators, rowA * mColumnCount, tmpRow, 0, mColumnCount);
        System.arraycopy(mNumerators, rowB * mColumnCount, mNumerators, rowA * mColumnCount, mColumnCount);
        System.arraycopy(tmpRow, 0, mNumerators, rowB * mColumnCount, mColumnCount);        
        System.arraycopy(mDenominators, rowA * mColumnCount, tmpRow, 0, mColumnCount);
        System.arraycopy(mDenominators, rowB * mColumnCount, mDenominators, rowA * mColumnCount, mColumnCount);
        System.arraycopy(tmpRow, 0, mDenominators, rowB * mColumnCount, mColumnCount);        
    }
    
    public void swapColumns(int colA, int colB) {
    	if (colA != colB) {
    		int rows = getRowCount();
    		for (int row = 0; row < rows; row++) {
				int tmpNumerator = getIntNumeratorAt(row, colA);
				int tmpDenominator	= getIntDenominatorAt(row, colA);
				setValueAt(row, colA, getIntNumeratorAt(row, colB), getIntDenominatorAt(row, colB));
				setValueAt(row, colB, tmpNumerator, tmpDenominator);
			}
    	}
    }

    public Number getNumberValueAt(int row, int col) {
        return Double.valueOf(getDoubleValueAt(row, col));
    }
    public void setValueAt(int row, int col, Number value) {
    	throw new RuntimeException("not implemented");
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
    public static String toString(ReadableIntRationalMatrix mx) {
        return toString(mx, "{", " }", " [", "]", "", "", "", ", ");
    }
    public static void writeTo(Writer writer, ReadableIntRationalMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }
    public static void writeTo(OutputStream out, ReadableIntRationalMatrix mx) {
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
    public static String toMultilineString(ReadableIntRationalMatrix mx) {
        return toString(mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");
    }
    public static void writeToMultiline(Writer writer, ReadableIntRationalMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }
    public static void writeToMultiline(OutputStream out, ReadableIntRationalMatrix mx) {
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }    
    
    protected static String toString(ReadableIntRationalMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        StringWriter sw = new StringWriter();
        writeTo(new PrintWriter(sw), mx, prefix, postfix, rowPrefix, rowPostfix, rowSeparator, colPrefix, colPostfix, colSeparator);
        return sw.toString();
    }
    protected static void writeTo(PrintWriter writer, ReadableIntRationalMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        int rows = mx.getRowCount();
        int cols = mx.getColumnCount();
        writer.print(prefix);
        for (int row = 0; row < rows; row++) {
            if (row > 0) writer.print(rowSeparator);
            writer.print(rowPrefix);            
            for (int col = 0; col < cols; col++) {
                if (col > 0) writer.print(colSeparator);
                writer.print(colPrefix);
                writer.print(mx.getIntNumeratorAt(row, col));
                writer.print('/');
                writer.print(mx.getIntDenominatorAt(row, col));
                writer.print(colPostfix);
            }
            writer.print(rowPostfix);
        }
        writer.print(postfix);
        writer.flush();
    }

    @Override
	public DefaultIntRationalMatrix clone() {
        return new DefaultIntRationalMatrix(mNumerators.clone(), mDenominators.clone(), -1, mColumnCount, false);
    }
    
    public DefaultIntRationalMatrix newInstance(int rows, int cols) {
    	return new DefaultIntRationalMatrix(rows, cols);
    }
    public DefaultIntRationalMatrix newInstance(Number[][] data, boolean rowsInDim1) {
    	throw new RuntimeException("not implemented");
    }
    
    public IntRationalMatrix toIntRationalMatrix(boolean enforceNewInstance) {
        return enforceNewInstance ? (IntRationalMatrix)clone() : this;
    }
    
    public IntRationalMatrix toWritableMatrix(boolean enforceNewInstance) {
    	return toIntRationalMatrix(enforceNewInstance);
    }
    public IntRationalMatrix toReadableMatrix(boolean enforceNewInstance) {
    	return toIntRationalMatrix(enforceNewInstance);
    }
    
    public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
        return new DefaultDoubleMatrix(toDoubleArray(), getRowCount(), getColumnCount());
    }
    
    public LongRationalMatrix toLongRationalMatrix(boolean enforceNewInstance) {
    	//TODO impl
    	throw new RuntimeException("not implemented yet");
    }

    public BigIntegerRationalMatrix toBigIntegerRationalMatrix(boolean enforceNewInstance) {
    	final int len = mNumerators.length;
    	BigInteger[] numerators	= new BigInteger[len];
    	BigInteger[] denominators	= new BigInteger[len];
    	for (int i = 0; i < len; i++) {
			numerators[i] = BigInteger.valueOf(mNumerators[i]);
			denominators[i] = BigInteger.valueOf(mDenominators[i]);
		}
    	return new DefaultBigIntegerRationalMatrix(
    		numerators, denominators, getRowCount(), getColumnCount()
    	);
    }
    
    public double[] toDoubleArray() {
        double[] array = new double[mNumerators.length];
        toArray(array);
        return array;
    }
    
    public void toArray(double[] array) {
        if (array.length != mNumerators.length) {
            throw new IllegalArgumentException("expected array length " + mNumerators.length + " but found " + array.length);
        }
        for (int ii = 0; ii < array.length; ii++) {
            array[ii] = ((double)mNumerators[ii]) / ((double)mDenominators[ii]);
        }
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
    
    public Number[][] getNumberRows() {
    	throw new RuntimeException("not implemented");
    }
    
    private static int[] getOnes(int count) {
        int[] ones = new int[count];
        Arrays.fill(ones, 1);
        return ones;
    }
    
    public void addRowToOtherRow(int srcRow, int srcFactor, int dstRow, int dstFactor) {
        addRowToOtherRow(srcRow, srcFactor, 1, dstRow, dstFactor, 1);
    }
    
    public void addRowToOtherRow(int srcRow, int srcNumerator, int srcDenominator, int dstRow, int dstNumerator, int dstDenominator) {
        if (srcDenominator == 0 || dstDenominator == 0) {
            throw new ArithmeticException("div by 0");
        }
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            long srcNumeratorL	= getIntNumeratorAt(srcRow, col);
            long srcDenominatorL	= getIntDenominatorAt(srcRow, col);
            srcNumeratorL	*= srcNumerator;
            srcDenominatorL	*= srcDenominator;            
            long srcGcd	= IntegerUtil.gcd(srcNumeratorL, srcDenominatorL);
            srcNumeratorL	/= srcGcd;
            srcDenominatorL	/= srcGcd;
            SmxIntegerUtil.checkIntegerRange(srcNumeratorL);
            SmxIntegerUtil.checkIntegerRange(srcDenominatorL);
            
            //now, do the changes
            multiply(dstRow, col, dstNumerator, dstDenominator);
            add(dstRow, col, (int)srcNumeratorL, (int)srcDenominatorL);
        }
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#add(int, int, int)
     */
    public void add(int row, int col, int value) {       
        add(row, col, value, 1);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntRationalMatrix#add(int, int, int, int)
     */
    public void add(int row, int col, int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("div by 0");
        }
        if (numerator == 0) return;
        long rowNumerator	= getIntNumeratorAt(row, col);
        if (rowNumerator == 0) {
            setValueAt(row, col, numerator, denominator);
            return;
        }
        long rowDenominator	= getIntDenominatorAt(row, col);
        long newDenominator	= rowDenominator * denominator;
        long newNumerator	= denominator * rowNumerator + rowDenominator * numerator;//if this addition leads to a long overflow, the result is a very small (large negative) long, and the int check below will fail for this value
        long gcd			= IntegerUtil.gcd(newDenominator, newNumerator);
        newDenominator	/= gcd;
        newNumerator	/= gcd;
        SmxIntegerUtil.checkIntegerRange(newNumerator);
        SmxIntegerUtil.checkIntegerRange(newDenominator);
        setValueAt(row, col, (int)newNumerator, (int)newDenominator);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#multiply(int, int, int)
     */
    public void multiply(int row, int col, int factor) {
        multiply(row, col, factor, 1);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntRationalMatrix#multiply(int, int, int, int)
     */
    public void multiply(int row, int col, int numerator, int denominator) {
        if (denominator == 0) {
            throw new ArithmeticException("div by 0");
        }
        if (numerator == denominator) return;
        if (numerator == 0) {
            setValueAt(row, col, 0);
            return;
        }
        long rowNumerator	= getIntNumeratorAt(row, col);
        if (rowNumerator == 0) return;
        long rowDenominator	= getIntDenominatorAt(row, col);
        if (rowNumerator == rowDenominator) {
            int gcd = IntegerUtil.gcd(numerator, denominator);
            setValueAt(row, col, numerator / gcd, denominator / gcd);
            return;
        }
        long newDenominator	= rowDenominator * denominator;
        long newNumerator	= rowNumerator * numerator;
        long gcd			= IntegerUtil.gcd(newDenominator, newNumerator);
        newDenominator	/= gcd;
        newNumerator	/= gcd;
        SmxIntegerUtil.checkIntegerRange(newNumerator);
        SmxIntegerUtil.checkIntegerRange(newDenominator);
        setValueAt(row, col, (int)newNumerator, (int)newDenominator);
    }

    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntMatrix#multiplyRow(int, int)
     */
    public void multiplyRow(int row, int factor) {
        multiplyRow(row, factor, 1);
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableIntRationalMatrix#multiplyRow(int, int, int)
     */
    public void multiplyRow(int row, int numerator, int denominator) {
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            multiply(row, col, numerator, denominator);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        int value = mColumnCount;
        for (int ii = 0; ii < mNumerators.length; ii++) {
            value ^= mNumerators[ii] ^ mDenominators[ii];
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
        if (obj instanceof IntRationalMatrix) {
            IntRationalMatrix other = (IntRationalMatrix)obj;
            int rows = getRowCount();
            if (rows != other.getRowCount()) return false;
            int cols = getColumnCount();
            if (cols != other.getColumnCount()) return false;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (getIntNumeratorAt(row, col) != other.getIntNumeratorAt(row, col)) return false;
                    if (getIntDenominatorAt(row, col) != other.getIntDenominatorAt(row, col)) return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public IntRationalMatrix subIntRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	DefaultIntRationalMatrix res = new DefaultIntRationalMatrix(rowEnd - rowStart, colEnd - colStart);
    	for (int row = 0; row < res.getRowCount(); row++) {
			for (int col = 0; col < res.getColumnCount(); col++) {
				res.setValueAt(
					row, col, 
					getIntNumeratorAt(rowStart + row, colStart + col), 
					getIntDenominatorAt(rowStart + row, colStart + col)
				);
			}
		}
    	return res;
    }
    public LongRationalMatrix subLongRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntRationalMatrix(rowStart, rowEnd, colStart, colEnd).toLongRationalMatrix(false /*cloned anyway*/);
    }
    public BigIntegerRationalMatrix subBigIntegerRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntRationalMatrix(rowStart, rowEnd, colStart, colEnd).toBigIntegerRationalMatrix(false /*cloned anyway*/);
    }
    public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	return subIntRationalMatrix(rowStart, rowEnd, colStart, colEnd).toDoubleMatrix(false /*cloned anyway*/);
    }

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
		int num = getIntNumeratorAt(row, col);
		int den = getIntDenominatorAt(row, col);		
		if (num == 0) {
			if (den != 1) {
				setValueAt(row, col, 0, 1);
				return true;
			}
			return false;
		}
		int gcd = IntegerUtil.gcd(num, den);
		if (gcd != 1) {
			if (den < 0) {
				gcd = -gcd;				
			}
			setValueAt(row, col, num / gcd, den / gcd);
			return true;
		}
		return false;
	}
    public DefaultIntRationalMatrix transposeW() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultIntRationalMatrix tr = new DefaultIntRationalMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, 
					getIntNumeratorAt(row, col),
					getIntDenominatorAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultIntRationalMatrix transposeR() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultIntRationalMatrix tr = new DefaultIntRationalMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, 
					getIntNumeratorAt(row, col),
					getIntDenominatorAt(row, col));
			}
		}
    	return tr;
    }
    public DefaultIntRationalMatrix transpose() {
    	final int rows = getRowCount();
    	final int cols = getColumnCount();
    	DefaultIntRationalMatrix tr = new DefaultIntRationalMatrix(cols, rows);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				tr.setValueAt(col, row, 
					getIntNumeratorAt(row, col),
					getIntDenominatorAt(row, col));
			}
		}
    	return tr;
    }
	public void negate(int row, int col) {
		final int num = getIntNumeratorAt(row, col);
		final int den = getIntNumeratorAt(row, col);
		if (den > 0) {
			setValueAt(row, col, -num, den);
		}
		else {
			setValueAt(row, col, num, -den);
		}
	}

}
