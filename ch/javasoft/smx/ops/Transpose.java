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

import java.math.BigInteger;

import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.IntMatrix;
import ch.javasoft.smx.iface.IntRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableIntMatrix;
import ch.javasoft.smx.iface.ReadableIntRationalMatrix;
import ch.javasoft.smx.iface.WritableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.WritableDoubleMatrix;
import ch.javasoft.smx.iface.WritableIntMatrix;
import ch.javasoft.smx.iface.WritableIntRationalMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.impl.DefaultIntMatrix;
import ch.javasoft.smx.impl.DefaultIntRationalMatrix;
import ch.javasoft.smx.util.DimensionCheck;

/**
 * The <code>Transpose</code> TODO type javadoc for Transpose
 */
public class Transpose {
    
    public static IntMatrix transpose(ReadableIntMatrix src) {
        IntMatrix dst = new DefaultIntMatrix(src.getColumnCount(), src.getRowCount());
        transpose(src, dst);
        return dst;
    }
    public static void transpose(ReadableIntMatrix src, WritableIntMatrix dst) {
        DimensionCheck.checkTransposeDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        if (src == dst) {/*implies: row == cols, a square matrix*/
            //faster, and not overwriting values if src==dst
            for (int row = 0; row < rows; row++) {
                for (int col = row + 1; col < cols; col++) {
                    int valA = src.getIntValueAt(row, col);
                    int valB = src.getIntValueAt(col, row);
                    dst.setValueAt(col, row, valA);
                    dst.setValueAt(row, col, valB);
                }
            }            
        }
        else {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    dst.setValueAt(col, row, src.getIntValueAt(row, col));
                }
            }            
        }
    }

    public static IntRationalMatrix transpose(ReadableIntRationalMatrix src) {
    	IntRationalMatrix dst = new DefaultIntRationalMatrix(src.getColumnCount(), src.getRowCount());
        transpose(src, dst);
        return dst;
    }
    public static void transpose(ReadableIntRationalMatrix src, WritableIntRationalMatrix dst) {
        DimensionCheck.checkTransposeDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        if (src == dst) {/*implies: row == cols, a square matrix*/
            //faster, and not overwriting values if src==dst
            for (int row = 0; row < rows; row++) {
                for (int col = row + 1; col < cols; col++) {
                    int numeratorA	= src.getIntNumeratorAt(row, col);
                    int denominatorA	= src.getIntDenominatorAt(row, col);
                    int numeratorB	= src.getIntNumeratorAt(col, row);
                    int denominatorB	= src.getIntDenominatorAt(col, row);
                    dst.setValueAt(col, row, numeratorA, denominatorA);
                    dst.setValueAt(row, col, numeratorB, denominatorB);
                }
            }            
        }
        else {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    dst.setValueAt(col, row, src.getIntNumeratorAt(row, col), src.getIntDenominatorAt(row, col));
                }
            }            
        }
    }
    
    public static BigIntegerRationalMatrix transpose(ReadableBigIntegerRationalMatrix src) {
    	BigIntegerRationalMatrix dst = new DefaultBigIntegerRationalMatrix(src.getColumnCount(), src.getRowCount());
        transpose(src, dst);
        return dst;
    }
    public static void transpose(ReadableBigIntegerRationalMatrix src, WritableBigIntegerRationalMatrix dst) {
        DimensionCheck.checkTransposeDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        if (src == dst) {/*implies: row == cols, a square matrix*/
            //faster, and not overwriting values if src==dst
            for (int row = 0; row < rows; row++) {
                for (int col = row + 1; col < cols; col++) {
                	BigInteger numeratorA	= src.getBigIntegerNumeratorAt(row, col);
                	BigInteger denominatorA	= src.getBigIntegerDenominatorAt(row, col);
                	BigInteger numeratorB	= src.getBigIntegerNumeratorAt(col, row);
                	BigInteger denominatorB	= src.getBigIntegerDenominatorAt(col, row);
                    dst.setValueAt(col, row, numeratorA, denominatorA);
                    dst.setValueAt(row, col, numeratorB, denominatorB);
                }
            }            
        }
        else {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    dst.setValueAt(col, row, src.getBigIntegerNumeratorAt(row, col), src.getBigIntegerDenominatorAt(row, col));
                }
            }            
        }
    }

    public static DoubleMatrix transpose(ReadableDoubleMatrix src) {
    	if (src.getRowCount() == 0 || src.getColumnCount() == 0) return src.toDoubleMatrix(true);//FIXME this is not the proper way
        DoubleMatrix dst = new DefaultDoubleMatrix(src.getColumnCount(), src.getRowCount());
        transpose(src, dst);
        return dst;
    }

    public static void transpose(ReadableDoubleMatrix src, WritableDoubleMatrix dst) {
        DimensionCheck.checkTransposeDimensions(src, dst);
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        if (src == dst) {/*implies: row == cols, a square matrix*/
            //faster, and not overwriting values if src==dst
            for (int row = 0; row < rows; row++) {
                for (int col = row + 1; col < cols; col++) {
                    double valueA	= src.getDoubleValueAt(row, col);
                    double valueB = src.getDoubleValueAt(col, row);
                    dst.setValueAt(col, row, valueA);
                    dst.setValueAt(row, col, valueB);
                }
            }            
        }
        else {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    dst.setValueAt(col, row, src.getDoubleValueAt(row, col));
                }
            }            
        }
    }
    
    private Transpose() {
        //no instances
    }
}
