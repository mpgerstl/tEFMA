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
package ch.javasoft.smx.ops.matrix;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.ops.DoubleOperations;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.iface.WritableVector;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.impl.DefaultDoubleVector;
import ch.javasoft.smx.ops.Add;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.Mul;
import ch.javasoft.smx.ops.Neg;
import ch.javasoft.smx.ops.ScalarOps;
import ch.javasoft.smx.ops.Sub;

/**
 * The <code>DoubleMatrixOperations</code> ... TODO javadoc-DoubleMatrixOperations-type
 * 
 */
public class DoubleMatrixOperations implements MatrixOperations<Double> {

	private static DoubleMatrixOperations sInstance;
	
	public static DoubleMatrixOperations instance() {
		if (sInstance == null) {
			sInstance = new DoubleMatrixOperations();
		}
		return sInstance;
	}
	
	public ReadableMatrix<Double> createReadableMatrix(Double[][] values, boolean rowsInFirstDim) {
		return new DefaultDoubleMatrix(values, rowsInFirstDim);
	}

	public ReadableMatrix<Double> createReadableMatrix(int rows, int cols) {
		return new DefaultDoubleMatrix(rows, cols);
	}

	public WritableMatrix<Double> createWritableMatrix(Double[][] values, boolean rowsInFirstDim) {
		return new DefaultDoubleMatrix(values, rowsInFirstDim);
	}

	public WritableMatrix<Double> createWritableMatrix(int rows, int cols) {
		return new DefaultDoubleMatrix(rows, cols);
	}
	public ReadableVector<Double> createReadableVector(Double[] values, boolean columnVector) {
		return new DefaultDoubleVector(values, columnVector);
	}
	public WritableVector<Double> createWritableVector(Double[] values, boolean columnVector) {
		return new DefaultDoubleVector(values, columnVector);
	}
	public ReadableVector<Double> createReadableVector(int size, boolean columnVector) {
		return new DefaultDoubleVector(size, columnVector);
	}
	public WritableVector<Double> createWritableVector(int size, boolean columnVector) {
		return new DefaultDoubleVector(size, columnVector);
	}

	public NumberOperations<Double> getNumberOperations() {
		return DoubleOperations.instance();
	}

	public ReadableMatrix<Double> add(ReadableMatrix<Double> mxA, Double value) {
		if (mxA instanceof ReadableDoubleMatrix) {
			return ScalarOps.add((ReadableDoubleMatrix)mxA, value.doubleValue());
		}
		return ScalarOps.addGeneric(mxA, value);
	}

	public ReadableMatrix<Double> add(ReadableMatrix<Double> mxA, ReadableMatrix<Double> mxB) {
		if (mxA instanceof ReadableDoubleMatrix && mxB instanceof ReadableDoubleMatrix) {
			return Add.add((ReadableDoubleMatrix<Double>)mxA, (ReadableDoubleMatrix<Double>)mxB);
		}
		return Add.addGeneric(mxA, mxB);
	}

	public ReadableMatrix<Double> multiply(ReadableMatrix<Double> mxA, Double value) {
		if (mxA instanceof ReadableDoubleMatrix) {
			return ScalarOps.scale((ReadableDoubleMatrix)mxA, value.doubleValue());
		}
		return ScalarOps.scaleGeneric(mxA, value);
	}

	public ReadableMatrix<Double> multiply(ReadableMatrix<Double> mxA, ReadableMatrix<Double> mxB) {
		if (mxA instanceof ReadableDoubleMatrix && mxB instanceof ReadableDoubleMatrix) {
			return Mul.multiply((ReadableDoubleMatrix<Double>)mxA, (ReadableDoubleMatrix<Double>)mxB);
		}
		return Mul.multiplyGeneric(mxA, mxB);
	}

	public ReadableMatrix<Double> negate(ReadableMatrix<Double> matrix) {
		if (matrix instanceof ReadableDoubleMatrix) {
			return Neg.negate((ReadableDoubleMatrix<Double>)matrix);
		}
		return Neg.negateGeneric(matrix);
	}

	public ReadableMatrix<Double> subtract(ReadableMatrix<Double> mxA, Double value) {
		return add(mxA, Double.valueOf(-value.doubleValue()));
	}

	public ReadableMatrix<Double> subtract(ReadableMatrix<Double> mxA, ReadableMatrix<Double> mxB) {
		if (mxA instanceof ReadableDoubleMatrix && mxB instanceof ReadableDoubleMatrix) {
			return Sub.subtract((ReadableDoubleMatrix<Double>)mxA, (ReadableDoubleMatrix<Double>)mxB);
		}
		return Sub.subtractGeneric(mxA, mxB);
	}

	public ReadableMatrix<Double> transpose(ReadableMatrix<Double> matrix) {
		return matrix.transposeR();
	}

}
