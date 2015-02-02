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
 * The <code>Neg</code> TODO type javadoc for Neg
 */
public class Neg {
    
    public static IntMatrix negate(ReadableIntMatrix src) {        
        IntMatrix dst = new DefaultIntMatrix(src.getRowCount(), src.getColumnCount());
        negate(src, dst);
        return dst;
    }
    
    public static void negate(ReadableIntMatrix src, WritableIntMatrix dst) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final long value = -((long)src.getIntValueAt(row, col));
                dst.setValueAt(row, col, SmxIntegerUtil.checkIntegerRange(value));
            }
        }
    }
    
    public static LongMatrix negate(ReadableLongMatrix src) {        
        LongMatrix dst = new DefaultLongMatrix(src.getRowCount(), src.getColumnCount());
        negate(src, dst);
        return dst;
    }
    
    public static void negate(ReadableLongMatrix src, WritableLongMatrix dst) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final long value = -src.getLongValueAt(row, col);
//                dst.setValueAt(row, col, SmxIntegerUtil.checkLongRange(value));
                dst.setValueAt(row, col, value);
            }
        }
    }
    
    public static IntRationalMatrix negate(ReadableIntRationalMatrix src) {
        DefaultIntRationalMatrix dst = new DefaultIntRationalMatrix(src.getRowCount(), src.getColumnCount());
        negate(src, dst);
        return dst;
    }
    
    public static void negate(ReadableIntRationalMatrix src, WritableIntRationalMatrix dst) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                long dividend = -((long)src.getIntNumeratorAt(row, col));
                SmxIntegerUtil.checkIntegerRange(dividend);
                dst.setValueAt(row, col, (int)dividend, src.getIntDenominatorAt(row, col));
            }
        }
    }
    
    public static DoubleMatrix negate(ReadableDoubleMatrix src) {        
        DoubleMatrix dst = new DefaultDoubleMatrix(src.getRowCount(), src.getColumnCount());
        negate(src, dst);
        return dst;
    }
    
    public static void negate(ReadableDoubleMatrix src, WritableDoubleMatrix dst) {
        DimensionCheck.checkEqualDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                dst.setValueAt(row, col, -src.getDoubleValueAt(row, col));
            }
        }
    }
    
    public static <N extends Number> ReadableMatrix<N> negateGeneric(ReadableMatrix<N> src) {
    	final int rows = src.getRowCount();
    	final int cols = src.getColumnCount();
    	final WritableMatrix<N> res = src.newInstance(rows, cols);
    	for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				res.negate(row, col);
			}
		}
    	return res.toReadableMatrix(false /*enforceNewInstance*/);
    }

    private Neg() {
        //no instances
    }
}
