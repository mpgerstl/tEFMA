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
package ch.javasoft.smx.util;

import ch.javasoft.smx.iface.MatrixBase;

/**
 * The <code>DimensionCheck</code> TODO type javadoc for DimensionCheck
 */
public class DimensionCheck {

    public static void checkEqualDimensions(MatrixBase mxA, MatrixBase mxB) throws IllegalArgumentException {
        checkEqualRowCount(mxA, mxB);
        checkEqualColumnCount(mxA, mxB);
    }

    public static void checkEqualRowCount(MatrixBase mxA, MatrixBase mxB) throws IllegalArgumentException {
        if (mxA.getRowCount() != mxB.getRowCount()) {
            throw new IllegalArgumentException(
                    "number of rows are not equal for matrices: " +
                    mxA.getRowCount() + " != " + mxB.getRowCount()
                    );
        }
    }

    public static void checkEqualColumnCount(MatrixBase mxA, MatrixBase mxB) throws IllegalArgumentException {
        if (mxA.getColumnCount() != mxB.getColumnCount()) {
            throw new IllegalArgumentException(
                    "number of columns are not equal for matrices: " +
                    mxA.getColumnCount() + " != " + mxB.getColumnCount()
                    );
        }
    }

    public static void checkTransposeDimensions(MatrixBase src, MatrixBase dst) throws IllegalArgumentException {
        if (src.getColumnCount() != dst.getRowCount()) {
            throw new IllegalArgumentException(
                    "dimension missmatch for matrix transposition: src.columnCount (" +
                    src.getColumnCount() + ") != dst.rowCount (" + dst.getRowCount() + ")"
                    );
        }
        if (dst.getColumnCount() != src.getRowCount()) {
            throw new IllegalArgumentException(
                    "dimension missmatch for matrix transposition: dst.columnCount (" +
                    dst.getColumnCount() + ") != src.rowCount (" + src.getRowCount() + ")"
                    );
        }
    }
    public static void checkMulDimensions(MatrixBase srcA, MatrixBase srcB, MatrixBase dst) throws IllegalArgumentException {
        if (srcA.getColumnCount() != srcB.getRowCount()) {
            throw new IllegalArgumentException(
                    "dimension missmatch for matrix multiplication: srcA.columnCount (" +
                    srcA.getColumnCount() + ") != srcB.rowCount (" + srcB.getRowCount() + ")"
                    );
        }
        if (srcA.getRowCount() != dst.getRowCount()) {
            throw new IllegalArgumentException(
                    "dimension missmatch for matrix multiplication: srcA.rowCount (" +
                    srcA.getRowCount() + ") != dst.rowCount (" + dst.getRowCount() + ")"
                    );
        }
        if (srcB.getColumnCount() != dst.getColumnCount()) {
            throw new IllegalArgumentException(
                    "dimension missmatch for matrix multiplication: srcB.columnCount (" +
                    srcB.getColumnCount() + ") != dst.columnCount (" + dst.getColumnCount() + ")"
                    );
        }
    }

    public static void checkSqareDimensions(MatrixBase mx) throws IllegalArgumentException {
        if (mx.getRowCount() != mx.getColumnCount()) {
            throw new IllegalArgumentException(
                    "not a square matrix: " + mx.getRowCount() + " != " + mx.getColumnCount()
                    );
        }
    }
    
}
