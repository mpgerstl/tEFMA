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
package ch.javasoft.math.operator;

/**
 * The <code>AggregatingBinaryOperator</code> performs an operation on two
 * operand arrays and returns a single output value of the same type. A typical 
 * operation is the inner product of two vectors.
 * 
 * @type T	input and output type of the operation
 * @type A	array type of operand and result
 */
public interface AggregatingBinaryOperator<T, A> {
	/**
	 * Performs the aggregating binary operation &oplus; on elements of two 
	 * vectors and returns the result:
	 * <pre>
	 *   return = src1[off1 : off1+len-1] &oplus; src2[off2 : off2+len-1]
	 *     off1 : src1IndexFrom 	
	 *     off2 : src2IndexFrom 	
	 *     len  : length
	 * </pre>
	 * 
	 * @param src1			the first operand vector
	 * @param src1IndexFrom	the start index in the vector, inclusive
	 * @param src2			the second operand vector
	 * @param src2IndexFrom	the start index in the vector, inclusive
	 * @param length		the number of vector elements to consider
	 */
	T operate(A src1, int src1IndexFrom, A src2, int src2IndexFrom, int length);

	/**
	 * Performs the aggregating binary operation &oplus; on elements of a matrix 
	 * and a vector and returns the result:
	 * <pre>
	 *   return = src1[row1 : row1+len-1][col1] &oplus; src2[off2 : off2+len-1]
	 *     row1 : src1RowFrom
	 *     col1 : src1Col
	 *     off2 : src2IndexFrom 	
	 *     len  : length
	 * </pre>
	 * 
	 * @param src1			the first operand matrix
	 * @param src1RowFrom	the start row index in the operand matrix, inclusive
	 * @param src1Col		the column index in the matrix
	 * @param src2			the second operand vector
	 * @param src2IndexFrom	the start index in the vector, inclusive
	 * @param length		the number of vector elements to consider
	 */
	T operate(A[] src1, int src1RowFrom, int src1Col, A src2, int src2IndexFrom, int length);
	
	/**
	 * Performs the aggregating binary operation &oplus; on elements of two 
	 * matrices and returns the result:
	 * <pre>
	 *   return = src1[row1 : row1+len-1][col1] &oplus; src2[row2 : row2+len-1][col2]
	 *     row1 : src1RowFrom
	 *     col1 : src1Col
	 *     row2 : src2RowFrom
	 *     col2 : src2Col
	 *     len  : length
	 * </pre>
	 * 
	 * @param src1			the first operand matrix
	 * @param src1RowFrom	the start row index in the operand matrix, inclusive
	 * @param src1Col		the column index in the matrix
	 * @param src2			the second operand array
	 * @param src2RowFrom	the start row index in the operand matrix, inclusive
	 * @param src2Col		the column index in the matrix
	 * @param length		the number of vector elements to consider
	 */
	T operate(A[] src1, int src1RowFrom, int src1Col, A[] src2, int src2RowFrom, int src2Col, int length);

	/**
	 * Performs the aggregating binary operation &oplus; on elements of two 
	 * vectors, writing the result back into an array:
	 * <pre>
	 * dst[ind] = src1[off1 : off1+len-1] &oplus; src2[off2 : off2+len-1]
	 *     ind  : dstIndex 	
	 *     off1 : src1IndexFrom 	
	 *     off2 : src2IndexFrom 	
	 *     len  : length
	 * </pre>&oplus;
	 * 
	 * @param src1			the first operand vector
	 * @param src1IndexFrom	the start index in the vector, inclusive
	 * @param src2			the second operand vector
	 * @param src2IndexFrom	the start index in the vector, inclusive
	 * @param dst			the result array
	 * @param dstIndex		the index in the result array
	 * @param length		the number of vector elements to consider
	 */
	void operate(A src1, int src1IndexFrom, A src2, int src2IndexFrom, A dst, int dstIndex, int length);

	/**
	 * Performs the aggregating binary operation &oplus; on elements of a matrix 
	 * and a vector, writing the result back into an array:
	 * <pre>
	 * dst[ind] = src1[row1 : row1+len-1][col1] &oplus; src2[off2 : off2+len-1]
	 *     ind  : dstIndex 	
	 *     row1 : src1RowFrom
	 *     col1 : src1Col
	 *     off2 : src2IndexFrom 	
	 *     len  : length
	 * </pre>
	 * 
	 * @param src1			the first operand vector
	 * @param src1RowFrom	the start row index in the operand matrix, inclusive
	 * @param src1Col		the column index in the matrix
	 * @param src2			the second operand vector
	 * @param src2IndexFrom	the start index in the vector, inclusive
	 * @param dst			the result array
	 * @param dstIndex		the index in the result array
	 * @param length		the number of vector elements to consider
	 */
	void operate(A[] src1, int src1RowFrom, int src1Col, A src2, int src2IndexFrom, A dst, int dstIndex, int length);
	
	/**
	 * Performs the aggregating binary operation &oplus; on elements of two 
	 * matrices, writing the result back into an array:
	 * <pre>
	 * dst[ind] = src1[row1 : row1+len-1][col1] &oplus; src2[row2 : row2+len-1][col2]
	 *     ind  : dstIndex 	
	 *     row1 : src1RowFrom
	 *     col1 : src1Col
	 *     row2 : src2RowFrom
	 *     col2 : src2Col
	 *     len  : length
	 * </pre>
	 * 
	 * @param src1			the first operand matrix
	 * @param src1RowFrom	the start row index in the operand matrix, inclusive
	 * @param src1Col		the column index in the matrix
	 * @param src2			the second operand array
	 * @param src2RowFrom	the start row index in the operand matrix, inclusive
	 * @param src2Col		the column index in the matrix
	 * @param dst			the result array
	 * @param dstIndex		the index in the result array
	 * @param length		the number of vector elements to consider
	 */
	void operate(A[] src1, int src1RowFrom, int src1Col, A[] src2, int src2RowFrom, int src2Col, A dst, int dstIndex, int length);

	/**
	 * Constants identifying standard operators of this kind
	 */
	static enum Id {
	    /**
	     * Inner product operator, returning the sum of the element by element 
	     * products of the two operand vectors, that is, 
	     * {@code sum(x[i] * y[i])} for operand vectors {@code x[*]} and 
	     * {@code y[*]}
	     */
		innerProduct;
	}
	
}
