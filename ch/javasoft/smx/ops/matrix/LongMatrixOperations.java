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
import ch.javasoft.math.ops.LongOperations;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableLongMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.iface.WritableVector;
import ch.javasoft.smx.impl.DefaultLongMatrix;
import ch.javasoft.smx.ops.Add;
import ch.javasoft.smx.ops.ExtendedMatrixOperations;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.smx.ops.MatrixOperations;
import ch.javasoft.smx.ops.Mul;
import ch.javasoft.smx.ops.Neg;
import ch.javasoft.smx.ops.ScalarOps;
import ch.javasoft.smx.ops.Sub;

/**
 * The <code>LongMatrixOperations</code> ... TODO javadoc-LongMatrixOperations-type
 * 
 */
public class LongMatrixOperations implements MatrixOperations<Long>, ExtendedMatrixOperations<Long, BigFraction> {

	private static LongMatrixOperations sInstance;
	
	public static LongMatrixOperations instance() {
		if (sInstance == null) {
			sInstance = new LongMatrixOperations();
		}
		return sInstance;
	}
	
	public ReadableMatrix<Long> createReadableMatrix(Long[][] values, boolean rowsInFirstDim) {
		return new DefaultLongMatrix(values, rowsInFirstDim);
	}

	public ReadableMatrix<Long> createReadableMatrix(int rows, int cols) {
		return new DefaultLongMatrix(rows, cols);
	}

	public WritableMatrix<Long> createWritableMatrix(Long[][] values, boolean rowsInFirstDim) {
		return new DefaultLongMatrix(values, rowsInFirstDim);
	}

	public WritableMatrix<Long> createWritableMatrix(int rows, int cols) {
		return new DefaultLongMatrix(rows, cols);
	}
	public ReadableVector<Long> createReadableVector(Long[] values, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public WritableVector<Long> createWritableVector(Long[] values, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public ReadableVector<Long> createReadableVector(int size, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}
	public WritableVector<Long> createWritableVector(int size, boolean columnVector) {
		throw new RuntimeException("not implemented");
	}

	public NumberOperations<Long> getNumberOperations() {
		return LongOperations.instance();
	}

	public ReadableMatrix<Long> add(ReadableMatrix<Long> mxA, Long value) {
		if (mxA instanceof ReadableLongMatrix) {
			return ScalarOps.add((ReadableLongMatrix)mxA, value.longValue());
		}
		return ScalarOps.addGeneric(mxA, value);
	}

	public ReadableMatrix<Long> add(ReadableMatrix<Long> mxA, ReadableMatrix<Long> mxB) {
		if (mxA instanceof ReadableLongMatrix && mxB instanceof ReadableLongMatrix) {
			return Add.add((ReadableLongMatrix<Long>)mxA, (ReadableLongMatrix<Long>)mxB);
		}
		return Add.addGeneric(mxA, mxB);
	}
	public ReadableMatrix<Long> multiply(ReadableMatrix<Long> mxA, Long value) {
		if (mxA instanceof ReadableLongMatrix) {
			return ScalarOps.scale((ReadableLongMatrix)mxA, value.longValue());
		}
		return ScalarOps.scaleGeneric(mxA, value);
	}

	public ReadableMatrix<Long> multiply(ReadableMatrix<Long> mxA, ReadableMatrix<Long> mxB) {
		if (mxA instanceof ReadableLongMatrix && mxB instanceof ReadableLongMatrix) {
			return Add.add((ReadableLongMatrix<Long>)mxA, (ReadableLongMatrix<Long>)mxB);
		}
		return Mul.multiplyGeneric(mxA, mxB);
	}

	public ReadableMatrix<Long> negate(ReadableMatrix<Long> matrix) {
		if (matrix instanceof ReadableLongMatrix) {
			return Neg.negate((ReadableLongMatrix<Long>)matrix);
		}
		return Neg.negateGeneric(matrix);
	}

	public ReadableMatrix<Long> subtract(ReadableMatrix<Long> mxA, Long value) {
		if (mxA instanceof ReadableLongMatrix) {
			return ScalarOps.add((ReadableLongMatrix)mxA, -value.longValue());
		}
		return ScalarOps.addGeneric(mxA, value);
	}

	public ReadableMatrix<Long> subtract(ReadableMatrix<Long> mxA, ReadableMatrix<Long> mxB) {
		if (mxA instanceof ReadableLongMatrix && mxB instanceof ReadableLongMatrix) {
			return Sub.subtract((ReadableLongMatrix<Long>)mxA, (ReadableLongMatrix<Long>)mxB);
		}
		return Sub.subtractGeneric(mxA, mxB);
	}

	public ReadableMatrix<Long> transposeR(ReadableMatrix<Long> matrix) {
		return matrix.transposeR();
	}

	public ReadableMatrix<Long> transpose(ReadableMatrix<Long> matrix) {
		return matrix.transposeR();
	}
	
	public int rank(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().rank((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public int nullity(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().nullity((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public ReadableMatrix<BigFraction> invert(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().invert((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	public ReadableMatrix<BigFraction> nullspace(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix) {
			return Gauss.getRationalInstance().nullspace((ReadableBigIntegerRationalMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}

}
