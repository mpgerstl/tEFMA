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

import ch.javasoft.smx.exception.SingularMatrixException;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.IntRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableIntRationalMatrix;
import ch.javasoft.smx.iface.WritableDoubleMatrix;
import ch.javasoft.smx.iface.WritableIntRationalMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.impl.DefaultIntRationalMatrix;
import ch.javasoft.smx.util.DimensionCheck;

/**
 * The <code>Invert</code> TODO type javadoc for Invert
 */
public class Invert {
    
    public static IntRationalMatrix invert(ReadableIntRationalMatrix src) {
        DimensionCheck.checkSqareDimensions(src);
        int len = src.getRowCount();
        DefaultIntRationalMatrix dst = new DefaultIntRationalMatrix(len, len);
        for (int ii = 0; ii < len; ii++) {            
            dst.setValueAt(ii, ii, 1);
        }
        invertInternal(src.toIntRationalMatrix(true), dst);
        return dst;
    }
    
    public static void invert(ReadableIntRationalMatrix src, WritableIntRationalMatrix dst) {
        DimensionCheck.checkSqareDimensions(src);
        DimensionCheck.checkEqualDimensions(src, dst);
        if (src == dst) {
            src = (ReadableIntRationalMatrix)src.clone();//or we would overwrite the source
        }
        int len = src.getRowCount();
        for (int row = 0; row < len; row++) {            
            for (int col = 0; col < len; col++) {
                dst.setValueAt(row, col, row == col ? 1 : 0);
            }
        }
        invertInternal(src.toIntRationalMatrix(true), dst);
    }
    private static void invertInternal(IntRationalMatrix src, WritableIntRationalMatrix dst) {
        DimensionCheck.checkSqareDimensions(src);
        DimensionCheck.checkEqualDimensions(src, dst);
        int len = src.getRowCount();
        int[] rowMapping = new int[len];
        for (int ii = 0; ii < len; ii++) {
            rowMapping[ii] = ii;
        }
        for (int col = 0; col < len; col++) {
            int rowPivot	= col;
            int pivotDividend	= src.getIntNumeratorAt(rowPivot, col);
            int pivotDivisor;
            //make the pivot be a 1
            if (pivotDividend == 0) {
                //search another row
                boolean found = false;
                for (int row = rowPivot + 1; row < len && !found; row++) {
                    pivotDividend = src.getIntNumeratorAt(row, col);
                    if (pivotDividend != 0) {
                        src.swapRows(rowPivot, row);
                        dst.swapRows(rowPivot, row);
                        int tmp = rowMapping[rowPivot];
                        rowMapping[rowPivot] = rowMapping[row];
                        rowMapping[row] = tmp;
                        found = true;
                    }           
                }
                if (!found) {
                    throw new SingularMatrixException("pivot is 0, supposing matrix has not full range", rowMapping[rowPivot]);
                }
            }
            pivotDivisor = src.getIntDenominatorAt(rowPivot, col);            
            //make pivot a 1
            src.multiplyRow(rowPivot, pivotDivisor, pivotDividend);
            dst.multiplyRow(rowPivot, pivotDivisor, pivotDividend);
            for (int row = 0; row < len; row++) {            
                if (row != col) {
                    //make it a 0
                    int colPivotDividend = src.getIntNumeratorAt(row, col);
                    if (colPivotDividend != 0) {
                        int colPivotDivisor = src.getIntDenominatorAt(row, col);
//                        for (int iCol = col; iCol < len; iCol++) {
//                            src.multiply(row, iCol, colPivotDivisor, colPivotDividend);
//                            dst.multiply(row, iCol, colPivotDivisor, colPivotDividend);
//                        }                        
                        src.addRowToOtherRow(rowPivot, -1, 1, row, colPivotDivisor, colPivotDividend);
                        dst.addRowToOtherRow(rowPivot, -1, 1, row, colPivotDivisor, colPivotDividend);
                    }
                }
            }
        }
        for (int row = 0; row < len; row++) {
            int pivotDividend	= src.getIntNumeratorAt(row, row);
            int pivotDivisor	= src.getIntDenominatorAt(row, row);
            //if it is no more a 1, we have to scale our inversin matrix
            if (pivotDividend != pivotDivisor) {
                dst.multiplyRow(row, pivotDivisor, pivotDividend);
            }
        }
    }
    
    public static DoubleMatrix invert(ReadableDoubleMatrix src, double tolerance) {
        DimensionCheck.checkSqareDimensions(src);
        int len = src.getRowCount();
        DoubleMatrix dst = new DefaultDoubleMatrix(len, len);
        for (int ii = 0; ii < len; ii++) {            
            dst.setValueAt(ii, ii, 1);
        }
        invertInternal(src.toDoubleMatrix(true), dst, tolerance);
        return dst;
    }
    
    public static void invert(ReadableDoubleMatrix src, WritableDoubleMatrix dst, double tolerance) {
        DimensionCheck.checkSqareDimensions(src);
        DimensionCheck.checkEqualDimensions(src, dst);
        if (src == dst) {
            src = (ReadableDoubleMatrix)src.clone();//or we would overwrite the source
        }
        int len = src.getRowCount();
        for (int row = 0; row < len; row++) {            
            for (int col = 0; col < len; col++) {
                dst.setValueAt(row, col, row == col ? 1 : 0);
            }
        }
        invertInternal(src.toDoubleMatrix(true), dst, tolerance);
    }
    private static void invertInternal(DoubleMatrix src, WritableDoubleMatrix dst, double tolerance) {
        tolerance = Math.abs(tolerance);
        double negTolerance = -tolerance;
        DimensionCheck.checkSqareDimensions(src);
        DimensionCheck.checkEqualDimensions(src, dst);
        int len = src.getRowCount();
        int[] rowMapping = new int[len];
        for (int ii = 0; ii < len; ii++) {
            rowMapping[ii] = ii;
        }
        for (int col = 0; col < len; col++) {
            int rowPivot	= col;
            double pivotValue = src.getDoubleValueAt(rowPivot, col);
            //make the pivot be a 1
            if (pivotValue == 0.0d || (pivotValue < tolerance && pivotValue > negTolerance)) {
                if (pivotValue != 0.0d) {
                    src.setValueAt(rowPivot, col, 0.0d);
                    pivotValue = 0.0d;
                }
                //search another row
                boolean found = false;
                for (int row = rowPivot + 1; row < len && !found; row++) {
                    pivotValue = src.getDoubleValueAt(row, col);
                    if (pivotValue != 0.0d) {
                        if (pivotValue < tolerance && pivotValue > negTolerance) {
                            pivotValue = 0.0d;
                            src.setValueAt(row, col, 0.0d);
                        }
                    }
                    if (pivotValue != 0.0d) {
                        src.swapRows(rowPivot, row);
                        dst.swapRows(rowPivot, row);
                        int tmp = rowMapping[rowPivot];
                        rowMapping[rowPivot] = rowMapping[row];
                        rowMapping[row] = tmp;
                        found = true;
                    }           
                }
                if (!found) {
                    throw new SingularMatrixException("pivot is 0, supposing matrix has not full range", rowMapping[rowPivot]);
                }
            }
            //make pivot a 1
            double pivotMultiplier = -1.0d / pivotValue;
            for (int row = 0; row < len; row++) {            
                if (row != col) {
                    //make it a 0
                    double colPivotValue = src.getDoubleValueAt(row, col);
                    if (colPivotValue != 0.0d) {
                        if (colPivotValue < tolerance && colPivotValue > negTolerance) {
                            colPivotValue = 0.0d;
                            src.setValueAt(row, col, 0.0d);
                        }                        
                    }
                    if (colPivotValue != 0.0d) {
                        double colPivotMultiplier = 1.0d / colPivotValue;
                        src.addRowToOtherRow(rowPivot, pivotMultiplier, row, colPivotMultiplier);
                        dst.addRowToOtherRow(rowPivot, pivotMultiplier, row, colPivotMultiplier);
                    }
                }
            }
        }
        for (int row = 0; row < len; row++) {
            double pivotValue = src.getDoubleValueAt(row, row);
            //if it is no more a 1, we have to scale our inversion matrix
            if (pivotValue != 1.0d) {
                dst.multiplyRow(row, 1.0d / pivotValue);
            }
        }
    }
    
    private Invert() {
        //no instances
    }
}
