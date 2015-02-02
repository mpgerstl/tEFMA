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

import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.ops.BigIntegerOperations;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.iface.WritableVector;
import ch.javasoft.smx.impl.DefaultBigIntegerMatrix;
import ch.javasoft.smx.ops.Add;
import ch.javasoft.smx.ops.ExtendedMatrixOperations;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.Mul;
import ch.javasoft.smx.ops.Neg;
import ch.javasoft.smx.ops.ScalarOps;
import ch.javasoft.smx.ops.Sub;

/**
 * The <code>BigIntegerMatrixOperations</code> 
 */
public class BigIntegerMatrixOperations implements MatrixOperations<BigInteger>, ExtendedMatrixOperations<BigInteger, BigFraction> {

	private static BigIntegerMatrixOperations sInstance;
	
	public static BigIntegerMatrixOperations instance() {
		if (sInstance == null) {
			sInstance = new BigIntegerMatrixOperations();
		}
		return sInstance;
	}
	
	public ReadableMatrix<BigInteger> createReadableMatrix(BigInteger[][] values, boolean rowsInFirstDim) {
		return new DefaultBigIntegerMatrix(values, rowsInFirstDim);
	}

	public ReadableMatrix<BigInteger> createReadableMatrix(int rows, int cols) {
		return new DefaultBigIntegerMatrix(rows, cols);
	}

	public WritableMatrix<BigInteger> createWritableMatrix(BigInteger[][] values, boolean rowsInFirstDim) {
		return new DefaultBigIntegerMatrix(values, rowsInFirstDim);
	}

	public WritableMatrix<BigInteger> createWritableMatrix(int rows, int cols) {
		return new DefaultBigIntegerMatrix(rows, cols);
	}
	public ReadableVector<BigInteger> createReadableVector(BigInteger[] values, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public WritableVector<BigInteger> createWritableVector(BigInteger[] values, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public ReadableVector<BigInteger> createReadableVector(int size, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public WritableVector<BigInteger> createWritableVector(int size, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}

	public NumberOperations<BigInteger> getNumberOperations() {
		return BigIntegerOperations.instance();
	}

	public ReadableMatrix<BigInteger> add(ReadableMatrix<BigInteger> mxA, BigInteger value) {
		return ScalarOps.addGeneric(mxA, value);
	}

	public ReadableMatrix<BigInteger> add(ReadableMatrix<BigInteger> mxA, ReadableMatrix<BigInteger> mxB) {
        return Add.addGeneric(mxA, mxB);
	}
	public ReadableMatrix<BigInteger> multiply(ReadableMatrix<BigInteger> mxA, BigInteger value) {
		return ScalarOps.scaleGeneric(mxA, value);
	}

	public ReadableMatrix<BigInteger> multiply(ReadableMatrix<BigInteger> mxA, ReadableMatrix<BigInteger> mxB) {
		return Mul.multiplyGeneric(mxA, mxB);
	}

	public ReadableMatrix<BigInteger> negate(ReadableMatrix<BigInteger> matrix) {
		return Neg.negateGeneric(matrix);
	}

	public ReadableMatrix<BigInteger> subtract(ReadableMatrix<BigInteger> mxA, BigInteger value) {
		return ScalarOps.subtractGeneric(mxA, value);
	}

	public ReadableMatrix<BigInteger> subtract(ReadableMatrix<BigInteger> mxA, ReadableMatrix<BigInteger> mxB) {
        return Sub.subtractGeneric(mxA, mxB);
	}

	public ReadableMatrix<BigInteger> transposeR(ReadableMatrix<BigInteger> matrix) {
		return matrix.transposeR();
	}
	
	public ReadableMatrix<BigInteger> transpose(ReadableMatrix<BigInteger> matrix) {
		return matrix.transposeR();
	}
	
	public int rank(ReadableMatrix<BigInteger> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().rank((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public int nullity(ReadableMatrix<BigInteger> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().nullity((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public ReadableMatrix<BigFraction> invert(ReadableMatrix<BigInteger> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().invert((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public ReadableMatrix<BigFraction> nullspace(ReadableMatrix<BigInteger> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().nullspace((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}

}
