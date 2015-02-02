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
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableIntRationalMatrix;
import ch.javasoft.smx.iface.WritableDoubleMatrix;
import ch.javasoft.smx.iface.WritableIntRationalMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.impl.DefaultIntMatrix;
import ch.javasoft.smx.impl.DefaultIntRationalMatrix;
import ch.javasoft.util.numeric.IntegerUtil;

/**
 * The <code>NullspaceRank</code> class contains static methods to calculate the
 * rank and the nullspace for a given matrix.
 */
public class NullspaceRank {
    
    public static int rank(ReadableIntRationalMatrix src) {
        int rank = nullspaceRankInternal(src.toIntRationalMatrix(true), DefaultIntRationalMatrix.identity(src.getRowCount()));
        return rank;
    }

    public static int nullity(ReadableIntRationalMatrix src) {
        int rank = nullspaceRankInternal(src.toIntRationalMatrix(true), DefaultIntRationalMatrix.identity(src.getRowCount()));
        return src.getColumnCount() - rank;
    }
    
    public static IntMatrix nullspace(ReadableIntRationalMatrix src) {
        IntRationalMatrix srcT  = Transpose.transpose(src);
        IntRationalMatrix dst   = DefaultIntRationalMatrix.identity(srcT.getRowCount());
        int rank = nullspaceRankInternal(srcT, dst);
        int len = srcT.getRowCount();
        DefaultIntMatrix nullspace = new DefaultIntMatrix(len - rank, dst.getColumnCount());
        for (int row = rank; row < len; row++) {
            int sgn = 0;
            int scp = 1;
            for (int col = 0; col < len; col++) {
                int dividend = dst.getIntNumeratorAt(row, col);
                if (dividend != 0) {
                    int divisor = dst.getIntDenominatorAt(row, col);
                    scp /= IntegerUtil.gcd(scp, divisor);
                    scp *= divisor;
                    sgn += Integer.signum(dividend);
                }
            }
            if (Integer.signum(scp) != Integer.signum(sgn)) scp = -scp;
            for (int col = 0; col < len; col++) {
                int dividend = dst.getIntNumeratorAt(row, col);
                int value;
                if (dividend == 0) {
                    value = 0;
                }
                else {
                    int divisor = dst.getIntDenominatorAt(row, col);
                    value = dividend * (scp / divisor);
                }
                nullspace.setValueAt(row - rank, col, value);
            }
        }
        return nullspace;
    }
    
    //precond: dst is square matrix of size src.rowCount
    private static int nullspaceRankInternal(IntRationalMatrix src, WritableIntRationalMatrix dst) {
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        int[] rowMapping = new int[rows];
        for (int ii = 0; ii < rows; ii++) {
            rowMapping[ii] = ii;
        }
        for (int col = 0; col < cols; col++) {
            int rowPivot	= col;
            if (rowPivot >= rows) return rows;
            int pivotDividend	= src.getIntNumeratorAt(rowPivot, col);
            int pivotDivisor;
            //if pivotDividend == 0, try to find another non-dependent row
            for (int row = rowPivot + 1; row < rows && pivotDividend == 0; row++) {
                pivotDividend = src.getIntNumeratorAt(row, col);
                if (pivotDividend != 0) {
                    src.swapRows(rowPivot, row);
                    dst.swapRows(rowPivot, row);
                    int tmp = rowMapping[rowPivot];
                    rowMapping[rowPivot] = rowMapping[row];
                    rowMapping[row] = tmp;
                }     
            }
            if (pivotDividend == 0) {
                //done, col is rank
                return col;
            }
            pivotDivisor = src.getIntDenominatorAt(rowPivot, col);            
            //make pivot a 1
            src.multiplyRow(rowPivot, pivotDivisor, pivotDividend);
            dst.multiplyRow(rowPivot, pivotDivisor, pivotDividend);
            for (int row = 0; row < rows; row++) {            
                if (row != col) {
                    //make it a 0
                    int colPivotDividend = src.getIntNumeratorAt(row, col);
                    if (colPivotDividend != 0) {
                        int colPivotDivisor = src.getIntDenominatorAt(row, col);
                        src.addRowToOtherRow(rowPivot, -1, 1, row, colPivotDivisor, colPivotDividend);
                        dst.addRowToOtherRow(rowPivot, -1, 1, row, colPivotDivisor, colPivotDividend);
                    }
                }
            }                
        }
        return cols;
    }
    
    public static int rank(ReadableDoubleMatrix src, double tolerance) {
        int rank = nullspaceRankInternal(src.toDoubleMatrix(true), DefaultDoubleMatrix.identity(src.getRowCount()), tolerance);
        return rank;
    }

    public static int nullity(ReadableDoubleMatrix src, double tolerance) {
        int rank = nullspaceRankInternal(src.toDoubleMatrix(true), DefaultDoubleMatrix.identity(src.getRowCount()), tolerance);
        return src.getColumnCount() - rank;
    }
    
    public static DoubleMatrix nullspace(ReadableDoubleMatrix src, double tolerance) {
        DoubleMatrix srcT  = Transpose.transpose(src);
        DoubleMatrix dst   = DefaultDoubleMatrix.identity(srcT.getRowCount());
        int rank = nullspaceRankInternal(srcT, dst, tolerance);
        int len = srcT.getRowCount();
        DefaultDoubleMatrix nullspace = new DefaultDoubleMatrix(len - rank, dst.getColumnCount());
        for (int row = rank; row < len; row++) {
            double minValue = Double.MAX_VALUE;
            int sgn = 0;
            for (int col = 0; col < len; col++) {
                double value = dst.getDoubleValueAt(row, col);
                if (value != 0.0d) {
                    if (value < 0.0d) {
                        sgn--;
                        if (minValue > -value) minValue = -value;
                    }
                    else {
                        sgn++;
                        if (minValue > value) minValue = value;
                    }
                }
            }
            double fac = (sgn < 0 ? -1.0d : 1.0d) / minValue;
            for (int col = 0; col < len; col++) {
                double value = fac * dst.getDoubleValueAt(row, col);
                nullspace.setValueAt(row - rank, col, value);
            }
        }
        return nullspace;
    }
    
    //precond: dst is square matrix of size src.rowCount
    private static int nullspaceRankInternal(DoubleMatrix src, WritableDoubleMatrix dst, double tolerance) {
        tolerance = Math.abs(tolerance);
        double negTolerance = -tolerance;
        int rows = src.getRowCount();
        int cols = src.getColumnCount();
        int[] rowMapping = new int[rows];
        for (int ii = 0; ii < rows; ii++) {
            rowMapping[ii] = ii;
        }
        for (int col = 0; col < cols; col++) {
            int rowPivot    = col;
            if (rowPivot >= rows) return rows;
            double pivotValue = src.getDoubleValueAt(rowPivot, col);
            if (negTolerance < pivotValue && pivotValue < tolerance) pivotValue = 0.0d;
            //if pivotDividend == 0, try to find another non-dependent row
            for (int row = rowPivot + 1; row < rows && pivotValue == 0.0d; row++) {
                pivotValue = src.getDoubleValueAt(rowPivot, col);
                if (negTolerance < pivotValue && pivotValue < tolerance) pivotValue = 0.0d;
                if (pivotValue == 0.0d) {
                    src.swapRows(rowPivot, row);
                    dst.swapRows(rowPivot, row);
                    int tmp = rowMapping[rowPivot];
                    rowMapping[rowPivot] = rowMapping[row];
                    rowMapping[row] = tmp;
                }     
            }
            if (pivotValue == 0.0d) {
                //done, col is rank
                return col;
            }
            //make pivot a 1
            double pivotMultiplyer = 1.0d / pivotValue;
            src.multiplyRow(rowPivot, pivotMultiplyer);
            dst.multiplyRow(rowPivot, pivotMultiplyer);
            for (int row = 0; row < rows; row++) {            
                if (row != col) {
                    //make it a 0
                    double colPivotValue = src.getDoubleValueAt(row, col);
                    if (negTolerance < colPivotValue && colPivotValue < tolerance) {
                        //is already zero
                        src.setValueAt(row, col, 0.0d);
                    }
                    else {
                        double fac = -1.0d / colPivotValue;
                        src.addRowToOtherRow(rowPivot, 1.0d, row, fac);
                        dst.addRowToOtherRow(rowPivot, 1.0d, row, fac);
                    }
                }
            }                
        }
        return cols;
    }

    private NullspaceRank() {
        //no instances
    }
}
