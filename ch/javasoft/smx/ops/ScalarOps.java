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
package ch.javasoft.smx.ops;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.IntMatrix;
import ch.javasoft.smx.iface.IntRationalMatrix;
import ch.javasoft.smx.iface.LongMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableIntMatrix;
import ch.javasoft.smx.iface.ReadableIntRationalMatrix;
import ch.javasoft.smx.iface.ReadableLongMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableDoubleMatrix;
import ch.javasoft.smx.iface.WritableIntMatrix;
import ch.javasoft.smx.iface.WritableIntRationalMatrix;
import ch.javasoft.smx.iface.WritableLongMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.impl.DefaultIntMatrix;
import ch.javasoft.smx.impl.DefaultIntRationalMatrix;
import ch.javasoft.smx.impl.DefaultLongMatrix;
import ch.javasoft.smx.util.DimensionCheck;
import ch.javasoft.smx.util.SmxIntegerUtil;

/**
 * The <code>ScalarOps</code> class contains static methods performing 
 * operations with a matrix and a scalar. Such operations are for instance 
 * scaling all matrix elements with the same factor, or adding a value to each
 * matrix element.
 */
public class ScalarOps {
    
    public static <N extends Number> DoubleMatrix add(ReadableDoubleMatrix src, double value) {
    	final int rows = src.getRowCount();
    	final DoubleMatrix res = src.toDoubleMatrix(true /*enforceNewInstance*/);
    	for (int row = 0; row < rows; row++) {
			res.multiplyRow(rows, value);
		}
    	return res;
    }
    public static <N extends Number> IntMatrix add(ReadableIntMatrix src, int value) {
    	final int rows = src.getRowCount();
    	final IntMatrix res = src.toIntMatrix(true /*enforceNewInstance*/);
    	for (int row = 0; row < rows; row++) {
			res.multiplyRow(rows, value);
		}
    	return res;
    }
    public static <N extends Number> LongMatrix add(ReadableLongMatrix src, long value) {
    	final int rows = src.getRowCount();
    	final LongMatrix res = src.toLongMatrix(true /*enforceNewInstance*/);
    	for (int row = 0; row < rows; row++) {
			res.multiplyRow(rows, value);
		}
    	return res;
    }
    public static <N extends Number> ReadableMatrix<N> subtractGeneric(ReadableMatrix<N> src, N value) {
    	return addGeneric(src, src.getNumberOperations().negate(value));
    }
    public static <N extends Number> ReadableMatrix<N> addGeneric(ReadableMatrix<N> src, N value) {
        final NumberOperations<N> nops = src.getNumberOperations();
    	final int rows = src.getRowCount();
    	final int cols = src.getColumnCount();
    	final WritableMatrix<N> res = src.newInstance(rows, cols);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final N sum = nops.add(src.getNumberValueAt(row, col), value);
				res.setValueAt(row, col, nops.reduce(sum));
			}
		}
    	return res.toReadableMatrix(false /*enforceNewInstance*/);
    }

    public static IntMatrix scale(ReadableIntMatrix src, int factor) {
        DefaultIntMatrix dst = new DefaultIntMatrix(src.getRowCount(), src.getColumnCount());
        scale(src, dst, factor);
        return dst;
    }
    
    public static void scale(ReadableIntMatrix src, WritableIntMatrix dst, int factor) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        for (int row = 0; row < rows; row++) {
            scaleRowInternal(src, dst, row, factor);
        }
    }
    public static LongMatrix scale(ReadableLongMatrix src, long factor) {
        DefaultLongMatrix dst = new DefaultLongMatrix(src.getRowCount(), src.getColumnCount());
        scale(src, dst, factor);
        return dst;
    }
    
    public static void scale(ReadableLongMatrix src, WritableLongMatrix dst, long factor) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        for (int row = 0; row < rows; row++) {
            scaleRowInternal(src, dst, row, factor);
        }
    }
    
    public static <N extends Number> ReadableMatrix<N> scaleGeneric(ReadableMatrix<N> src, N factor) {
    	final NumberOperations<N> nops = src.getNumberOperations();
    	final int rows = src.getRowCount();
    	final int cols = src.getColumnCount();
		final WritableMatrix<N> dst = src.newInstance(rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final N val = nops.multiply(factor, src.getNumberValueAt(row, col));
				dst.setValueAt(row, col, nops.reduce(val));
			}
		}
		return dst.toReadableMatrix(false /*enforceNewInstance*/);
    }
    
    public static IntMatrix scaleRow(ReadableIntMatrix src, int row, int factor) {
        DefaultIntMatrix dst = new DefaultIntMatrix(src.getRowCount(), src.getColumnCount());
        scaleRowInternal(src, dst, row, factor);
        return dst;
    }

    public static void scaleRow(ReadableIntMatrix src, WritableIntMatrix dst, int row, int factor) {
        DimensionCheck.checkEqualRowCount(src, dst);
        scaleRowInternal(src, dst, row, factor);
    }

    private static void scaleRowInternal(ReadableIntMatrix src, WritableIntMatrix dst, int row, int factor) {
        int cols = src.getColumnCount();        
        for (int col = 0; col < cols; col++) {
            final long scaled = src.getIntValueAt(row, col) * factor;
            dst.setValueAt(row, col, SmxIntegerUtil.checkIntegerRange(scaled));
        }
    }
    private static void scaleRowInternal(ReadableLongMatrix src, WritableLongMatrix dst, int row, long factor) {
        int cols = src.getColumnCount();        
        for (int col = 0; col < cols; col++) {
            final long scaled = src.getLongValueAt(row, col) * factor;
//            SmxIntegerUtil.checkLongRange(scaled);
            dst.setValueAt(row, col, scaled);
        }
    }
    
    public static IntRationalMatrix scale(ReadableIntRationalMatrix src, int mulFactor, int divFactor) {
        DefaultIntRationalMatrix dst = new DefaultIntRationalMatrix(src.getRowCount(), src.getColumnCount());
        scale(src, dst, mulFactor, divFactor);
        return dst;
    }
    
    public static void scale(ReadableIntRationalMatrix src, WritableIntRationalMatrix dst, int mulFactor, int divFactor) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        for (int row = 0; row < rows; row++) {
            scaleRowInternal(src, dst, row, mulFactor, divFactor);
        }
    }
    
    public static IntRationalMatrix scaleRow(ReadableIntRationalMatrix src, int row, int mulFactor, int divFactor) {
        DefaultIntRationalMatrix dst = new DefaultIntRationalMatrix(src.getRowCount(), src.getColumnCount());
        scaleRowInternal(src, dst, row, mulFactor, divFactor);
        return dst;
    }

    public static void scaleRow(ReadableIntRationalMatrix src, WritableIntRationalMatrix dst, int row, int mulFactor, int divFactor) {
        DimensionCheck.checkEqualRowCount(src, dst);
        scaleRowInternal(src, dst, row, mulFactor, divFactor);
    }

    private static void scaleRowInternal(ReadableIntRationalMatrix src, WritableIntRationalMatrix dst, int row, int mulFactor, int divFactor) {
        int cols = src.getColumnCount();        
        for (int col = 0; col < cols; col++) {            
            SmxIntegerUtil.multiply(src.getIntNumeratorAt(row, col), src.getIntDenominatorAt(row, col), mulFactor, divFactor, dst, row, col);
        }
    }
    
    public static DoubleMatrix scale(ReadableDoubleMatrix src, double factor) {
        DoubleMatrix dst = new DefaultDoubleMatrix(src.getRowCount(), src.getColumnCount());
        scale(src, dst, factor);
        return dst;
    }
    
    public static void scale(ReadableDoubleMatrix src, WritableDoubleMatrix dst, double factor) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        for (int row = 0; row < rows; row++) {
            scaleRowInternal(src, dst, row, factor);
        }
    }
    
    public static DoubleMatrix scaleRow(ReadableDoubleMatrix src, int row, double factor) {
        DoubleMatrix dst = new DefaultDoubleMatrix(src.getRowCount(), src.getColumnCount());
        scaleRowInternal(src, dst, row, factor);
        return dst;
    }

    public static void scaleRow(ReadableDoubleMatrix src, WritableDoubleMatrix dst, int row, double factor) {
        DimensionCheck.checkEqualRowCount(src, dst);
        scaleRowInternal(src, dst, row, factor);
    }

    private static void scaleRowInternal(ReadableDoubleMatrix src, WritableDoubleMatrix dst, int row, double factor) {
        int cols = src.getColumnCount();        
        for (int col = 0; col < cols; col++) {
            dst.setValueAt(row, col, factor * src.getDoubleValueAt(row, col));
        }
    }
    
    private ScalarOps() {
        //no instances
    }
}
