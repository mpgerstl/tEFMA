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

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.ops.IntegerOperations;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableIntMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.iface.WritableVector;
import ch.javasoft.smx.impl.DefaultIntMatrix;
import ch.javasoft.smx.ops.Add;
import ch.javasoft.smx.ops.ExtendedMatrixOperations;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.Mul;
import ch.javasoft.smx.ops.Neg;
import ch.javasoft.smx.ops.ScalarOps;
import ch.javasoft.smx.ops.Sub;

/**
 * The <code>IntegerMatrixOperations</code> ... TODO javadoc-IntegerMatrixOperations-type
 * 
 */
public class IntMatrixOperations implements MatrixOperations<Integer>, ExtendedMatrixOperations<Integer, BigFraction> {

	private static IntMatrixOperations sInstance;
	
	public static IntMatrixOperations instance() {
		if (sInstance == null) {
			sInstance = new IntMatrixOperations();
		}
		return sInstance;
	}
	
	public ReadableMatrix<Integer> createReadableMatrix(Integer[][] values, boolean rowsInFirstDim) {
		return new DefaultIntMatrix(values, rowsInFirstDim);
	}

	public ReadableMatrix<Integer> createReadableMatrix(int rows, int cols) {
		return new DefaultIntMatrix(rows, cols);
	}

	public WritableMatrix<Integer> createWritableMatrix(Integer[][] values, boolean rowsInFirstDim) {
		return new DefaultIntMatrix(values, rowsInFirstDim);
	}

	public WritableMatrix<Integer> createWritableMatrix(int rows, int cols) {
		return new DefaultIntMatrix(rows, cols);
	}
	public ReadableVector<Integer> createReadableVector(Integer[] values, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public WritableVector<Integer> createWritableVector(Integer[] values, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public ReadableVector<Integer> createReadableVector(int size, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public WritableVector<Integer> createWritableVector(int size, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}

	public NumberOperations<Integer> getNumberOperations() {
		return IntegerOperations.instance();
	}

	public ReadableMatrix<Integer> add(ReadableMatrix<Integer> mxA, Integer value) {
		if (mxA instanceof ReadableIntMatrix) {
			return ScalarOps.add((ReadableIntMatrix)mxA, value.intValue());
		}
		return ScalarOps.addGeneric(mxA, value);
	}

	public ReadableMatrix<Integer> add(ReadableMatrix<Integer> mxA, ReadableMatrix<Integer> mxB) {
		if (mxA instanceof ReadableIntMatrix && mxB instanceof ReadableIntMatrix) {
			return Add.add((ReadableIntMatrix<Integer>)mxA, (ReadableIntMatrix<Integer>)mxB);
		}
		return Add.addGeneric(mxA, mxB);
	}
	public ReadableMatrix<Integer> multiply(ReadableMatrix<Integer> mxA, Integer value) {
		if (mxA instanceof ReadableIntMatrix) {
			return ScalarOps.scale((ReadableIntMatrix)mxA, value.intValue());
		}
		return ScalarOps.scaleGeneric(mxA, value);
	}

	public ReadableMatrix<Integer> multiply(ReadableMatrix<Integer> mxA, ReadableMatrix<Integer> mxB) {
		if (mxA instanceof ReadableIntMatrix && mxB instanceof ReadableIntMatrix) {
			return Mul.multiply((ReadableIntMatrix<Integer>)mxA, (ReadableIntMatrix<Integer>)mxB);
		}
		return Mul.multiplyGeneric(mxA, mxB);
	}

	public ReadableMatrix<Integer> negate(ReadableMatrix<Integer> matrix) {
		if (matrix instanceof ReadableIntMatrix) {
			return Neg.negate((ReadableIntMatrix<Integer>)matrix);
		}
		return Neg.negateGeneric(matrix);
	}

	public ReadableMatrix<Integer> subtract(ReadableMatrix<Integer> mxA, Integer value) {
		return add(mxA, Integer.valueOf(-value.intValue()));
	}

	public ReadableMatrix<Integer> subtract(ReadableMatrix<Integer> mxA, ReadableMatrix<Integer> mxB) {
		if (mxA instanceof ReadableIntMatrix && mxB instanceof ReadableIntMatrix) {
			return Sub.subtract((ReadableIntMatrix<Integer>)mxA, (ReadableIntMatrix<Integer>)mxB);
		}
		return Sub.subtractGeneric(mxA, mxB);
	}

	public ReadableMatrix<Integer> transposeR(ReadableMatrix<Integer> matrix) {
		return matrix.transposeR();
	}
	
	public ReadableMatrix<Integer> transpose(ReadableMatrix<Integer> matrix) {
		return matrix.transposeR();
	}
	
	public int rank(ReadableMatrix<Integer> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().rank((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public int nullity(ReadableMatrix<Integer> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().nullity((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public ReadableMatrix<BigFraction> invert(ReadableMatrix<Integer> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().invert((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public ReadableMatrix<BigFraction> nullspace(ReadableMatrix<Integer> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().nullspace((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}

}
