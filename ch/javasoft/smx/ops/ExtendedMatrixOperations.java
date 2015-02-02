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

import ch.javasoft.smx.iface.ReadableMatrix;

/**
 * Extended matrix operations adds more complex operations for matrices, such
 * as inversion or nullspace computation, where the result matrix is not
 * necessarily of the same number type as the input matrix. Other operations 
 * are related to such operations, and for some number types, one might have to
 * define a precision (tolerance) to perform the operations.
 * <p>
 * Usually, the result type involves inversion of numbers. For instance, for an
 * interger matrix, its inverse may be a fraction number matrix.
 *
 * @param <N>	the input number type
 * @param <I>	the output number type for inverted numbers
 */
public interface ExtendedMatrixOperations<N extends Number, I extends Number> {
	
	/**
	 * Returns the rank of the given matrix.
	 * <p>
	 * Usually, Gaussian elimination is used to compute the rank
	 */
	int rank(ReadableMatrix<N> mx);
	
	/**
	 * Returns the nullity of the given matrix, that is, the dimension of the
	 * nullspace of <tt>mx</tt>. Note that by the <i>rank-nullity</i> theorem,
	 * <pre>
	 *   rank(mx) + nullity(mx) = n
	 * </pre>
	 * where <tt>n</tt> is the number of columns of <tt>mx</tt>.
	 * <p>
	 * Usually, Gaussian elimination is used to compute the nullity.
	 */
	int nullity(ReadableMatrix<N> mx);
	/**
	 * Returns the matrix inverse of a square matrix <tt>mx</tt>, that is, a 
	 * matrix <tt>res = null(mx)</tt>, such that
	 * <pre>
	 *   mx * res = I
	 * </pre>
	 * where <tt>I</tt> is the identity matrix.
	 * <p>
	 * Usually, Gaussian elimination is used to compute the inverse matrix.
	 */
	ReadableMatrix<I> invert(ReadableMatrix<N> mx);
	/**
	 * Returns a basis for the kernel (or nullspace) of <tt>mx</tt>, that is, a
	 * matrix <tt>res</tt>, such that it spans the nullspace:
	 * <pre>
	 *   span(res) = null(mx) = { x : mx * x = 0}
	 * </pre>
	 * <p>
	 * Usually, Gaussian elimination is used to compute the kernel matrix.
	 */
	ReadableMatrix<I> nullspace(ReadableMatrix<N> mx);
}
