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
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableIntMatrix;
import ch.javasoft.smx.iface.ReadableIntRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableDoubleMatrix;
import ch.javasoft.smx.iface.WritableIntMatrix;
import ch.javasoft.smx.iface.WritableIntRationalMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.impl.DefaultIntMatrix;
import ch.javasoft.smx.impl.DefaultIntRationalMatrix;
import ch.javasoft.smx.util.DimensionCheck;
import ch.javasoft.smx.util.SmxIntegerUtil;
import ch.javasoft.util.numeric.IntegerUtil;

/**
 * The <code>Mul</code> class contains static methods to perform matrix 
 * multiplication, supporting different matrix number types.
 */
public class Mul {
    
    public static IntMatrix multiply(ReadableIntMatrix srcA, ReadableIntMatrix srcB) {
        DefaultIntMatrix dst = new DefaultIntMatrix(srcA.getRowCount(), srcB.getColumnCount());
        multiply(srcA, srcB, (WritableIntMatrix)dst);
        return dst;
    }
    
    public static void multiply(ReadableIntMatrix srcA, ReadableIntMatrix srcB, WritableIntMatrix dst) {
        DimensionCheck.checkMulDimensions(srcA, srcB, dst);
        boolean srcAIsSrcB = srcA == srcB;
        if (srcA == dst) srcA = (ReadableIntMatrix)srcA.clone();
        if (srcB == dst) srcB = (srcAIsSrcB ? srcA /*already cloned*/ : (ReadableIntMatrix)srcB.clone());
        int rows = dst.getRowCount();
        int cols = dst.getColumnCount();        
        int cnt	= srcA.getColumnCount();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                long val = 0;
                for (int ii = 0; ii < cnt; ii++) {
                    long summandA = ((long)srcA.getIntValueAt(row, ii)) * ((long)srcB.getIntValueAt(ii, col));
                    long summandB = val;
                    val = summandA + summandB;
                    SmxIntegerUtil.checkLongRangeAfterAddition(summandA, summandB, val);
                }
                SmxIntegerUtil.checkIntegerRange(val);
                dst.setValueAt(row, col, (int)val);
            }
        }
    }
    
    public static IntRationalMatrix multiply(ReadableIntRationalMatrix srcA, ReadableIntRationalMatrix srcB) {
        DefaultIntRationalMatrix dst = new DefaultIntRationalMatrix(srcA.getRowCount(), srcA.getColumnCount());
        multiply(srcA, srcB, (WritableIntRationalMatrix)dst);
        return dst;
    }
    
    public static void multiply(ReadableIntRationalMatrix srcA, ReadableIntRationalMatrix srcB, WritableIntRationalMatrix dst) {
        DimensionCheck.checkMulDimensions(srcA, srcB, dst);
        boolean srcAIsSrcB = srcA == srcB;
        if (srcA == dst) srcA = (ReadableIntRationalMatrix)srcA.clone();
        if (srcB == dst) srcB = (srcAIsSrcB ? srcA /*already cloned*/ : (ReadableIntRationalMatrix)srcB.clone());
        int rows = srcA.getRowCount();
        int cols = srcA.getColumnCount();        
        int cnt	= srcA.getColumnCount();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                long dividend	= 0;
                long divisor	= 1;
                for (int ii = 0; ii < cnt; ii++) {
                    long dividendA	= srcA.getIntNumeratorAt(row, ii);
                    long dividendB	= srcB.getIntNumeratorAt(ii, col);
                    long divisorA		= srcA.getIntDenominatorAt(row, ii);
                    long divisorB		= srcB.getIntDenominatorAt(ii, col);
                    
                    long dividendP	= dividendA * dividendB;
                    long divisorP		= divisorA * divisorB;
                    
                    long newDividendA	= dividendP * divisor;
                    long newDividendB	= dividend * divisorP;
                    SmxIntegerUtil.checkLongRangeAfterMultiplication(dividendP, divisor, newDividendA);
                    SmxIntegerUtil.checkLongRangeAfterMultiplication(dividend, divisorP, newDividendB);
                    long newDivisor	= divisor * divisorP;
                    SmxIntegerUtil.checkLongRangeAfterMultiplication(divisor, divisorP, newDivisor);
                    long newDividend	= newDividendA + newDividendB;
                    SmxIntegerUtil.checkLongRangeAfterAddition(newDividendA, newDividendB, newDividend);
                    
                    //shorten
                    long gcd		= IntegerUtil.gcd(newDividend, newDivisor);
                    dividend		= newDividend / gcd;
                    divisor		= newDivisor / gcd;
                }
                SmxIntegerUtil.checkIntegerRange(dividend);
                SmxIntegerUtil.checkIntegerRange(divisor);
                dst.setValueAt(row, col, (int)dividend, (int)divisor);
            }
        }
    }

    
    
    public static DoubleMatrix multiply(ReadableDoubleMatrix srcA, ReadableDoubleMatrix srcB) {
        DoubleMatrix dst = new DefaultDoubleMatrix(srcA.getRowCount(), srcB.getColumnCount());
        multiply(srcA, srcB, (WritableDoubleMatrix)dst);
        return dst;
    }    
    
    public static void multiply(ReadableDoubleMatrix srcA, ReadableDoubleMatrix srcB, WritableDoubleMatrix dst) {
        DimensionCheck.checkMulDimensions(srcA, srcB, dst);
        final boolean srcAIsSrcB = srcA == srcB;
        if (srcA == dst) srcA = (ReadableDoubleMatrix)srcA.clone();
        if (srcB == dst) srcB = (srcAIsSrcB ? srcA /*already cloned*/ : (ReadableDoubleMatrix)srcB.clone());
        final int rows = dst.getRowCount();
        final int cols = dst.getColumnCount();        
        final int cnt	= srcA.getColumnCount();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double val = 0.0d;
                for (int ii = 0; ii < cnt; ii++) {
                    val += srcA.getDoubleValueAt(row, ii) * srcB.getDoubleValueAt(ii, col);
                }
                dst.setValueAt(row, col, val);
            }
        }
    }

    public static <N extends Number> ReadableMatrix<N> multiplyGeneric(ReadableMatrix<N> srcA, ReadableMatrix<N> srcB) {
    	WritableMatrix<N> dst = srcA.newInstance(srcA.getRowCount(), srcB.getColumnCount());
    	multiplyGeneric(srcA, srcB, dst);
        return dst.toReadableMatrix(false /*enforceNewInstance*/);
    }    
    
    public static <N extends Number> void multiplyGeneric(ReadableMatrix<N> srcA, ReadableMatrix<N> srcB, WritableMatrix<N> dst) {
        DimensionCheck.checkMulDimensions(srcA, srcB, dst);
        final NumberOperations<N> numberOps = srcA.getNumberOperations();
        final boolean srcAIsSrcB = srcA == srcB;
        if (srcA == dst) srcA	= srcA.clone();
        if (srcB == dst) srcB	= (srcAIsSrcB ? srcA /*already cloned*/ : srcB.clone());
        final int rows	= dst.getRowCount();
        final int cols	= dst.getColumnCount();        
        final int cnt	= srcA.getColumnCount();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                N val = numberOps.zero();
                for (int ii = 0; ii < cnt; ii++) {
                	final N prod = numberOps.multiply(
                			srcA.getNumberValueAt(row, ii), 
                			srcB.getNumberValueAt(ii, col));
                	val = numberOps.add(val, prod);
                }
                val = numberOps.reduce(val);
                dst.setValueAt(row, col, val);
            }
        }
    }
        
    private Mul() {
        //no instances
    }
}
