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
 * The <code>AggregatingUnaryOperator</code> performs an operation on an operand 
 * array and returns a single output value of the same type. A typical operation
 * is the sum over all elements of a vector.
 * 
 * @type T	input and output type of the operation
 * @type A	array type of operand and result
 */
public interface AggregatingUnaryOperator<T, A> {
	/**
	 * Performs the aggregating unary operation &oplus; on array elements and 
	 * returns the result:
	 * <pre>
	 *   return = &oplus; ( src1[off : off+len-1] )
	 *     off  : srcIndexFrom 	
	 *     len  : length
	 * </pre>
	 * 
	 * @param src			the operand array
	 * @param srcIndexFrom	the start index in the operand's array, inclusive
	 * @param length		the number of vector elements to consider
	 */
	T operate(A src, int srcIndexFrom, int length);

	/**
	 * Performs the aggregating unary operation &oplus; on matrix elements and 
	 * returns the result:
	 * <pre>
	 *   return = &oplus; ( src1[row : row+rows-1][col : col+cols-1] )
	 *     row  : srcRowFrom 	
	 *     col  : srcColFrom 	
	 * </pre>
	 * 
	 * @param src			the operand matrix
	 * @param srcRowFrom	the start row index in the operand matrix, inclusive
	 * @param srcColFrom	the start column index in the operand matrix, inclusive
	 * @param rows			the number of rows to consider
	 * @param cols			the number of columns to consider
	 */
	T operate(A[] src, int srcRowFrom, int srcColFrom, int rows, int cols);

	/**
	 * Performs the aggregating unary operation &oplus; on array elements, 
	 * writing the result back into an array:
	 * <pre>
	 * dst[ind] = &oplus; ( src1[off : off+len-1] )
	 *     ind  : dstIndex 	
	 *     off  : srcIndexFrom 	
	 *     len  : length
	 * </pre>
	 * 
	 * @param src			the operand array
	 * @param srcIndexFrom	the start index in the operand vector, inclusive
	 * @param length		the number of vector elements to consider
	 * @param dst			the result array
	 * @param dstIndex		the index in the result array
	 */
	void operate(A src, int srcIndexFrom, int length, A dst, int dstIndex);

	/**
	 * Performs the aggregating unary operation &oplus; on matrix elements, 
	 * writing the result back into an array:
	 * <pre>
	 * dst[ind] = &oplus; ( src1[row : row+rows-1][col : col+cols-1] )
	 *     ind  : dstIndex 	
	 *     row  : srcRowFrom 	
	 *     col  : srcColFrom 	
	 * </pre>
	 * 
	 * @param src			the operand matrix
	 * @param srcRowFrom	the start row index in the operand matrix, inclusive
	 * @param srcColFrom	the start column index in the operand matrix, inclusive
	 * @param rows			the number of rows to consider
	 * @param cols			the number of columns to consider
	 * @param dst			the result array
	 * @param dstIndex		the index in the result array
	 */
	void operate(A[] src, int srcRowFrom, int srcColFrom, int rows, int cols, A dst, int dstIndex);

	/**
	 * Constants identifying standard operators of this kind
	 */
	static enum Id {
	    /**
	     * Min operator, returning the smallest of all operands, that is
	     * {@code min(x[*])} for operands {@code x[*]}. For a zero length 
	     * vector, the result is null or NaN.
	     */
		min, 
	    /**
	     * Max operator, returning the largest of all operands, that is
	     * {@code max(x[*])} for operands {@code x[*]}. For a zero length 
	     * vector, the result is null or NaN.
	     */
		max, 
	    /**
	     * Sum operator, returning the sum of all operands, that is
	     * {@code sum(x[*])} for operands {@code x[*]}. For a zero length 
	     * vector, the result is zero.
	     */
		sum, 
	    /**
	     * Product operator, returning the product of all operands, that is
	     * {@code prod(x[*])} for operands {@code x[*]}. For a zero length 
	     * vector, the result is one.
	     */
		prod, 
	    /**
	     * Sum operator, returning the sum of all squared operands, that is
	     * <code>sum(x<sup>2</sup>[*])</code> for operands {@code x[*]}. For a 
	     * zero length vector, the result is zero.
	     */
		sumSquared,

		//NOTE: sync doc with ExpressionComposer
	    /**
	     * Divisor for vector normalization, that is, a value by which a vector 
	     * can be divided to derive a normalized vector. It is zero for a zero
	     * vector (including a vector of length zero), and positive for all 
	     * other vectors, that is, dividing the vector does not change the 
	     * direction of the vector.
	     * <p>
	     * For standard floating point numbers, the normalizer is the length of
	     * the vector, that is, the square root of {@link #sumSquared}. This
	     * implies that a normalized floating point vector has length one.
	     * <p>
	     * For big integers or fraction numbers, the normalizer is the greatest 
	     * common divisor (GCD). Dividing the vector changes its length such 
	     * that the element values become as small as possible (treating 
	     * numerator and denominator values individually for fraction numbers, 
	     * that is, {@code GCD(numerator)/GCD(denominator)}).
	     * <p>
	     * Returns zero if for zero vectors and for the zero length vector. If
	     * no meaningful vector normalizer can be defined for a certain number 
	     * type, one is returned.
	     */
		//NOTE: sync doc with ExpressionComposer
		normDivisor,

		//NOTE: sync doc with ExpressionComposer
		/**
	     * Divisor for vector squeezing, that is, a value by which a vector can 
	     * be divided to derive a squeezed vector. It is zero for a zero
	     * vector (including a vector of length zero), and positive for all 
	     * other vectors, that is, dividing the vector does not change the 
	     * direction of the vector. It changes the vector length such that 
	     * certain element values are squeezed out. 
	     * <p>
	     * For standard floating point numbers, the squeezer is the same as
	     * <code> 1/min(abs(x)), x &ne; 0</code>, that is, the inverse of the 
	     * smallest nonzero absolute value in the vector. This implies that the 
	     * smallest absolute value of nonzero elements in a squeezed floating 
	     * point vector is one. All values between {@code -1} and {@code 1} are
	     * squeezed out. 
	     * <p>
	     * For big integers or fraction numbers, the squeezer is defined as the
	     * quotient of greatest common divisor of numerators and least common 
	     * multiple of denominators, or {@code GCD(numerator)/LCM(denominator)}
	     * for short. For integer numbers, it is the GCD of all values, and 
	     * squeezing is the same as normalizing (see {@link #normDivisor}). For 
	     * fraction numbers, squeezing turns all vector elements into integer 
	     * values. The integer values are smallest possible, non-integer values 
	     * are squeezed out.
	     * <p>
	     * Returns zero if for zero vectors and for the zero length vector. If
	     * no meaningful vector normalizer can be defined for a certain number 
	     * type, one is returned.
	     */
		//NOTE: sync doc with ExpressionComposer
		squeezeDivisor;
	}
	
}
