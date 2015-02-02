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
import ch.javasoft.math.ops.DoubleOperations;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.matrix.DoubleMatrixOperations;
import ch.javasoft.util.StringUtil;

/**
 * The <code>AbstractDoubleMatrix</code> contains methods which are typically
 * common for {@link DoubleMatrix} implementations, or default implementations
 * for some methods.
 */
abstract public class AbstractDoubleMatrix implements DoubleMatrix {

	private static final String NL = StringUtil.LINE_SEPARATOR;
	
    public AbstractDoubleMatrix() {
        super();
    }
    
    public DoubleOperations getNumberOperations() {
    	return DoubleOperations.instance();
    }
    public MatrixOperations<Double> getMatrixOperations() {
    	return DoubleMatrixOperations.instance();
    }
    
    public void setValueAt(int row, int col, long value) {
        setValueAt(row, col, (double)value);
    }
    
    public void setValueAt(int row, int col, Double value) {
    	setValueAt(row, col, value.doubleValue());
    }
    
    public void setValueAt(int row, int col, int value) {
        setValueAt(row, col, (double)value);
    }
    
    public void setValueAt(int row, int col, BigInteger value) {
        setValueAt(row, col, value.doubleValue());
    }
    // cj: b
    public void setValueAt_BigInteger(int row, int col, BigInteger value) {
        setValueAt(row, col, value.doubleValue());
    }
    // cj: e

    public void setValueAt(int row, int col, BigInteger dividend, BigInteger divisor) {
        setValueAt(row, col, BigFraction.toDouble(dividend, divisor));
    }
    
    public void setValueAt(int row, int col, BigFraction value) {
    	setValueAt(row, col, value.getNumerator(), value.getDenominator());
    }
    
    // cj: b
    public void setValueAt_BigFraction(int row, int col, BigFraction value) {
    	setValueAt(row, col, value.getNumerator(), value.getDenominator());
    }
    // cj: e
    
    public void setValueAt(int row, int col, long dividend, long divisor) {
        setValueAt(row, col, ((double)dividend) / ((double)divisor));
    }
    
    public void setValueAt(int row, int col, int dividend, int divisor) {
        setValueAt(row, col, ((double)dividend) / ((double)divisor));
    }

    public Double getNumberValueAt(int row, int col) {
        return Double.valueOf(getDoubleValueAt(row, col));
    }
    
    public int getSignumAt(int row, int col) {    	
    	final double val = getDoubleValueAt(row, col);
    	return val == 0d ? 0 : val < 0d ? -1 : 1;
    }
    
    public double[] toDoubleArray() {
        double[] array = new double[getRowCount() * getColumnCount()];
        toArray(this, array);
        return array;
    }

    public void toArray(double[] array) {
    	toArray(this, array);
    }
    public static void toArray(ReadableDoubleMatrix mx, double[] array) {
        int rows = mx.getRowCount();
        int cols = mx.getColumnCount();
        if (array.length != rows * cols) {
            throw new IllegalArgumentException("expected array length " + (rows * cols) + " but found " + array.length);
        }
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                array[row * cols + col] = mx.getDoubleValueAt(row, col);
            }
        }
    }
    
    public DoubleMatrix toWritableMatrix(boolean enforceNewInstance) {
    	return toDoubleMatrix(enforceNewInstance);
    }
    public DoubleMatrix toReadableMatrix(boolean enforceNewInstance) {
    	return toDoubleMatrix(enforceNewInstance);
    }
    
    public double[] getDoubleColumn(int col) {
        return getDoubleColumn(this, col);
    }
    public static double[] getDoubleColumn(ReadableDoubleMatrix mx, int col) {
        int rows = mx.getRowCount();
        double[] res = new double[rows];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getDoubleValueAt(row, col);            
        }
        return res;
    }
    
    public Double[][] getNumberRows() {
    	return getNumberRows(this);
    }
    public static Double[][] getNumberRows(ReadableDoubleMatrix mx) {
    	final int rows = mx.getRowCount();
    	final int cols = mx.getColumnCount();
    	final Double[][] data = new Double[rows][cols];
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				data[row][col] = Double.valueOf(mx.getDoubleValueAt(row, col));
			}
		}
    	return data;
    }
    public double[] getDoubleRow(int row) {
        return getDoubleRow(this, row);
    }
    public static double[] getDoubleRow(ReadableDoubleMatrix mx, int row) {
        int cols = mx.getColumnCount();
        double[] res = new double[cols];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getDoubleValueAt(row, col);
        }
        return res;
    }
    
    public double[][] getDoubleColumns() {
        return getDoubleColumns(this);
    }
    public static double[][] getDoubleColumns(ReadableDoubleMatrix mx) {
        int cols = mx.getColumnCount();
        double[][] res = new double[cols][];
        for (int col = 0; col < cols; col++) {
            res[col] = mx.getDoubleColumn(col);
        }
        return res;
    }
    
    public double[][] getDoubleRows() {
        return getDoubleRows(this);
    }
    public static double[][] getDoubleRows(ReadableDoubleMatrix mx) {
        int rows = mx.getRowCount();
        double[][] res = new double[rows][];
        for (int row = 0; row < rows; row++) {
            res[row] = mx.getDoubleRow(row);
        }
        return res;
    }
    
    public void swapColumns(int colA, int colB) {
    	if (colA != colB) {
    		int rows = getRowCount();
    		for (int row = 0; row < rows; row++) {
				double tmp = getDoubleValueAt(row, colA);
				setValueAt(row, colA, getDoubleValueAt(row, colB));
				setValueAt(row, colB, tmp);
			}
    	}
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
    public static String toString(ReadableDoubleMatrix mx) {
        return toString(mx, "{", " }", " [", "]", "", "", "", ", ");
    }
    public static void writeTo(Writer writer, ReadableDoubleMatrix mx) {
        writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, "{", " }", " [", "]", "", "", "", ", ");        
    }
    public static void writeTo(OutputStream out, ReadableDoubleMatrix mx) {
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
    public static String toMultilineString(ReadableDoubleMatrix mx) {
        return toString(mx, "{" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");
    }
    public static void writeToMultiline(Writer writer, ReadableDoubleMatrix mx) {
        String sizeStr = mx.getRowCount() + "x" + mx.getColumnCount();
    	writeTo(
                writer instanceof PrintWriter ? (PrintWriter)writer: new PrintWriter(writer), 
                mx, sizeStr + " {" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }
    public static void writeToMultiline(OutputStream out, ReadableDoubleMatrix mx) {
        String sizeStr = mx.getRowCount() + "x" + mx.getColumnCount();
        writeTo(
                new PrintWriter(new OutputStreamWriter(out)), 
                mx, sizeStr + " {" + NL, "}" + NL, " [", "]" + NL, "", " ", " ", ",");        
    }    
    
    protected static String toString(ReadableDoubleMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        StringWriter sw = new StringWriter();
        writeTo(new PrintWriter(sw), mx, prefix, postfix, rowPrefix, rowPostfix, rowSeparator, colPrefix, colPostfix, colSeparator);
        return sw.toString();
    }
    protected static void writeTo(PrintWriter writer, ReadableDoubleMatrix mx, String prefix, String postfix, String rowPrefix, String rowPostfix, String rowSeparator, String colPrefix, String colPostfix, String colSeparator) {
        int rows = mx.getRowCount();
        int cols = mx.getColumnCount();
        writer.print(prefix);
        for (int row = 0; row < rows; row++) {
            if (row > 0) writer.print(rowSeparator);
            writer.print(rowPrefix);            
            for (int col = 0; col < cols; col++) {
                if (col > 0) writer.print(colSeparator);
                writer.print(colPrefix);
                writer.print(mx.getDoubleValueAt(row, col));
                writer.print(colPostfix);
            }
            writer.print(rowPostfix);
        }
        writer.print(postfix);
        writer.flush();
    }
    
    @Override
	abstract public AbstractDoubleMatrix clone();
    
    public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
        return enforceNewInstance ? (AbstractDoubleMatrix)clone() : this;
    }

    public void addRowToOtherRow(int srcRow, BigInteger srcFactor, int dstRow, BigInteger dstFactor) {
    	addRowToOtherRow(srcRow, srcFactor.doubleValue(), dstRow, dstFactor.doubleValue());
    }
    public void addRowToOtherRow(int srcRow, double srcFactor, int dstRow, double dstFactor) {
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            setValueAt(dstRow, col, dstFactor * getDoubleValueAt(dstRow, col) + srcFactor * getDoubleValueAt(srcRow, col));
        }
    }
    
    public void addRowToOtherRow(int srcRow, long srcFactor, int dstRow, long dstFactor) {
        addRowToOtherRow(srcRow, (double)srcFactor, dstRow, (double)dstFactor);
    }
    
    public void addRowToOtherRow(int srcRow, int srcFactor, int dstRow, int dstFactor) {
        addRowToOtherRow(srcRow, (double)srcFactor, dstRow, (double)dstFactor);
    }
    
    public void addRowToOtherRow(int srcRow, BigInteger srcDividend, BigInteger srcDivisor, int dstRow, BigInteger dstDividend, BigInteger dstDivisor) {
    	double srcFactor = BigFraction.toDouble(srcDividend, srcDivisor);
    	double dstFactor = BigFraction.toDouble(dstDividend, dstDivisor);
    	addRowToOtherRow(srcRow, srcFactor, dstRow, dstFactor);
    }

    public void addRowToOtherRow(int srcRow, long srcDividend, long srcDivisor, int dstRow, long dstDividend, long dstDivisor) {
        addRowToOtherRow(srcRow, ((double)srcDividend) / ((double)srcDivisor), dstRow, ((double)dstDividend) / ((double)dstDivisor));
    }
    
    public void addRowToOtherRow(int srcRow, int srcDividend, int srcDivisor, int dstRow, int dstDividend, int dstDivisor) {
        addRowToOtherRow(srcRow, ((double)srcDividend) / ((double)srcDivisor), dstRow, ((double)dstDividend) / ((double)dstDivisor));
    }
    
    public void add(int row, int col, double value) {
        setValueAt(row, col, getDoubleValueAt(row, col) + value);
    }
    
    public void add(int row, int col, BigInteger value) {
    	add(row, col, value.doubleValue());
    }
    public void add(int row, int col, long value) {
    	add(row, col, (double)value);
    }
    
    public void add(int row, int col, int value) {
        add(row, col, (double)value);
    }
    
    public void add(int row, int col, int dividend, int divisor) {
        add(row, col, ((double)dividend) / ((double)divisor));
    }
    
    /* (non-Javadoc)
     * @see WritableLongRationalMatrix#add(int, int, long, long)
     */
    public void add(int row, int col, long dividend, long divisor) {
        add(row, col, ((double)dividend) / ((double)divisor));
    }
    
    /* (non-Javadoc)
     * @see WritableBigIntegerRationalMatrix#add(int, int, BigInteger, BigInteger)
     */
    public void add(int row, int col, BigInteger dividend, BigInteger divisor) {
        add(row, col, BigFraction.toDouble(dividend, divisor));
    }
    
    /* (non-Javadoc)
     * @see ch.javasoft.smx.iface.WritableDoubleMatrix#multiply(int, int, double)
     */
    public void multiply(int row, int col, double factor) {
        setValueAt(row, col, factor * getDoubleValueAt(row, col));
    }
    
    public void multiply(int row, int col, BigInteger factor) {
        multiply(row, col, factor.doubleValue());
    }
    public void multiply(int row, int col, long factor) {
        multiply(row, col, (double)factor);
    }
    
    public void multiply(int row, int col, int factor) {
        multiply(row, col, (double)factor);
    }

    public void multiply(int row, int col, BigInteger dividend, BigInteger divisor) {
    	multiply(row, col, BigFraction.toDouble(dividend, divisor));
    }
    
    public void multiply(int row, int col, long dividend, long divisor) {
        multiply(row, col, ((double)dividend) / ((double)divisor));
    }
    
    public void multiply(int row, int col, int dividend, int divisor) {
        multiply(row, col, ((double)dividend) / ((double)divisor));
    }
    
    public void multiplyRow(int row, BigInteger factor) {
        multiplyRow(row, factor.doubleValue());
    }
    public void multiplyRow(int row, long factor) {
        multiplyRow(row, (double)factor);
    }
    
    public void multiplyRow(int row, int factor) {
        multiplyRow(row, (double)factor);
    }

    public void multiplyRow(int row, BigInteger dividend, BigInteger divisor) {
    	multiplyRow(row, BigFraction.toDouble(dividend, divisor));
    }

    public void multiplyRow(int row, long dividend, long divisor) {
        multiplyRow(row, (double)dividend / (double)divisor);
    }
    
    public void multiplyRow(int row, int dividend, int divisor) {
        multiplyRow(row, (double)dividend / (double)divisor);
    }
    
    public void multiplyRow(int row, double factor) {
        int cols = getColumnCount();
        for (int col = 0; col < cols; col++) {
            multiply(row, col, factor);
        }
    }
    
    @Override
	public int hashCode() {
        int rows		= getRowCount();
        int cols		= getColumnCount();
        int value	= rows ^ cols;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                long bits = Double.doubleToRawLongBits(getDoubleValueAt(row, col));
                value ^= ((int)(bits >> 32)) ^ ((int)(bits & 0x00000000ffffffffL));
            }
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
        if (obj instanceof DoubleMatrix) {
            return equals(this, (DoubleMatrix)obj, 0.0d);
        }
        return false;
    }
    
    public boolean equals(DoubleMatrix other, double toleration) {
        return equals(this, other, toleration);
    }
    public static boolean equals(DoubleMatrix mxA, DoubleMatrix mxB, double toleration) {
        if (mxA == mxB) return true;
        if (mxA == null || mxB == null /*both null already returned true*/) return false;
        int rows = mxA.getRowCount();
        if (rows != mxB.getRowCount()) return false;
        int cols = mxA.getColumnCount();
        if (cols != mxB.getColumnCount()) return false;
        toleration = Math.abs(toleration);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double delta = mxA.getDoubleValueAt(row, col) - mxB.getDoubleValueAt(row, col);
                if (Math.abs(delta) > toleration) return false;
            }
        }
        return true;        
    }
    
    public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
    	DefaultDoubleMatrix res = new DefaultDoubleMatrix(rowEnd - rowStart, colEnd - colStart);
    	for (int row = 0; row < res.getRowCount(); row++) {
			for (int col = 0; col < res.getColumnCount(); col++) {
				res.setValueAt(row, col, getDoubleValueAt(rowStart + row, colStart + col));
			}
		}
    	return res;
    }
    public void negate(int row, int col) {
    	setValueAt(row, col, -getDoubleValueAt(row, col));
    }
    
    abstract public AbstractDoubleMatrix transpose();
    
}
