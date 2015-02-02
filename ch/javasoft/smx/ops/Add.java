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
 * The <code>Add</code> class contains static methods to add one matrix to
 * another matrix, supporting different matrix types.
 */
public class Add {
    
    public static DoubleMatrix add(ReadableDoubleMatrix srcA, ReadableDoubleMatrix srcB) {
        DefaultDoubleMatrix dst = new DefaultDoubleMatrix(srcA.getRowCount(), srcA.getColumnCount());
        add(srcA, srcB, dst);
        return dst;
    }

    public static void add(ReadableDoubleMatrix srcA, ReadableDoubleMatrix srcB, WritableDoubleMatrix dst) {
        DimensionCheck.checkEqualDimensions(srcA, srcB);
        DimensionCheck.checkEqualDimensions(srcB, dst);
        int rows = srcA.getRowCount();
        int cols = srcA.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final double sum = srcA.getDoubleValueAt(row, col) + srcB.getDoubleValueAt(row, col);
                dst.setValueAt(row, col, sum);
            }
        }
    }

    public static IntMatrix add(ReadableIntMatrix srcA, ReadableIntMatrix srcB) {
        DefaultIntMatrix dst = new DefaultIntMatrix(srcA.getRowCount(), srcA.getColumnCount());
        add(srcA, srcB, dst);
        return dst;
    }
    
    public static void add(ReadableIntMatrix srcA, ReadableIntMatrix srcB, WritableIntMatrix dst) {
        DimensionCheck.checkEqualDimensions(srcA, srcB);
        DimensionCheck.checkEqualDimensions(srcB, dst);
        int rows = srcA.getRowCount();
        int cols = srcA.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final long sum = srcA.getIntValueAt(row, col) + srcB.getIntValueAt(row, col);
                dst.setValueAt(row, col, SmxIntegerUtil.checkIntegerRange(sum));
            }
        }
    }
    public static LongMatrix add(ReadableLongMatrix srcA, ReadableLongMatrix srcB) {
        DefaultLongMatrix dst = new DefaultLongMatrix(srcA.getRowCount(), srcA.getColumnCount());
        add(srcA, srcB, dst);
        return dst;
    }
    
    public static void add(ReadableLongMatrix srcA, ReadableLongMatrix srcB, WritableLongMatrix dst) {
        DimensionCheck.checkEqualDimensions(srcA, srcB);
        DimensionCheck.checkEqualDimensions(srcB, dst);
        int rows = srcA.getRowCount();
        int cols = srcA.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final long sum = srcA.getLongValueAt(row, col) + srcB.getLongValueAt(row, col);
//                SmxIntegerUtil.checkLongRange(sum);
                dst.setValueAt(row, col, sum);
            }
        }
    }
    
    public static IntRationalMatrix add(ReadableIntRationalMatrix srcA, ReadableIntRationalMatrix srcB) {
        DefaultIntRationalMatrix dst = new DefaultIntRationalMatrix(srcA.getRowCount(), srcA.getColumnCount());
        add(srcA, srcB, dst);
        return dst;
    }
    
    public static void add(ReadableIntRationalMatrix srcA, ReadableIntRationalMatrix srcB, WritableIntRationalMatrix dst) {
        DimensionCheck.checkEqualDimensions(srcA, srcB);
        DimensionCheck.checkEqualDimensions(srcB, dst);
        int rows = srcA.getRowCount();
        int cols = srcA.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                SmxIntegerUtil.add(
                        srcA.getIntNumeratorAt(row, col), srcA.getIntDenominatorAt(row, col),
                        srcB.getIntNumeratorAt(row, col), srcB.getIntDenominatorAt(row, col),
                        dst, row, col
                        );
            }
        }        
    }
    
    public static <N extends Number> ReadableMatrix<N> addGeneric(ReadableMatrix<N> srcA, ReadableMatrix<N> srcB) {
        DimensionCheck.checkEqualDimensions(srcA, srcB);
        final NumberOperations<N> nops = srcA.getNumberOperations();
    	final int rows = srcA.getRowCount();
    	final int cols = srcA.getColumnCount();
    	final WritableMatrix<N> res = srcA.newInstance(rows, cols);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final N sum = nops.add(srcA.getNumberValueAt(row, col), srcB.getNumberValueAt(row, col));
				res.setValueAt(row, col, nops.reduce(sum));
			}
		}
    	return res.toReadableMatrix(false /*enforceNewInstance*/);
    }

    private Add() {
        //no instances
    }
}
