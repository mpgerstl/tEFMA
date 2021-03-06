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
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.iface.WritableVector;

/**
 * The <code>MatrixOperations</code> is similar to {@link NumberOperations}, but
 * for matrices.
 */
public interface MatrixOperations<N extends Number> {
	ReadableMatrix<N> createReadableMatrix(N[][] values, boolean rowsInFirstDim);
	WritableMatrix<N> createWritableMatrix(N[][] values, boolean rowsInFirstDim);
	ReadableMatrix<N> createReadableMatrix(int rows, int cols);
	WritableMatrix<N> createWritableMatrix(int rows, int cols);
	ReadableVector<N> createReadableVector(N[] values, boolean columnVector);
	WritableVector<N> createWritableVector(N[] values, boolean columnVector);
	ReadableVector<N> createReadableVector(int size, boolean columnVector);
	WritableVector<N> createWritableVector(int size, boolean columnVector);
	ReadableMatrix<N> transpose(ReadableMatrix<N> matrix);
	ReadableMatrix<N> negate(ReadableMatrix<N> matrix);
	ReadableMatrix<N> add(ReadableMatrix<N> mxA, N value);
	ReadableMatrix<N> add(ReadableMatrix<N> mxA, ReadableMatrix<N> mxB);
	ReadableMatrix<N> subtract(ReadableMatrix<N> mxA, N value);
	ReadableMatrix<N> subtract(ReadableMatrix<N> mxA, ReadableMatrix<N> mxB);
	ReadableMatrix<N> multiply(ReadableMatrix<N> mxA, N value);
	ReadableMatrix<N> multiply(ReadableMatrix<N> mxA, ReadableMatrix<N> mxB);
	NumberOperations<N> getNumberOperations();
}
