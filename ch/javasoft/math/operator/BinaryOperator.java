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
 * The <code>BinaryOperator</code> performs an operation on two operands and
 * returns an output of the same type. A typical operator is addition, 
 * subtraction, multiplication or division of two numbers.
 * 
 * @type T	input and output type of the operation
 * @type A	array type of input operands and result
 */
public interface BinaryOperator<T, A> extends NAryOperator<T, A> {
	/**
	 * Performs the binary operation &oplus; on two operands and returns the 
	 * result:
	 * <pre>
	 *   return = operand1 &oplus; operand2
	 * </pre>
	 * 
	 * @param operand1	the first operand to operate on
	 * @param operand2	the second operand to operate on
	 * @return the result of the binary operation
	 */
	T operate(T operand1, T operand2);

	/**
	 * Performs the binary operation &oplus; on two vector elements, also 
	 * writing the result back into a vector:
	 * <pre>
	 *   dst[dstIndex] = operand1[index1] &oplus; operand2[index2]
	 * </pre>
	 * 
	 * @param operand1	the first operand array
	 * @param index1	the index in the first operand's array
	 * @param operand2	the second operand array
	 * @param index2	the index in the second operand's array
	 * @param dst		the result array
	 * @param dstIndex	the index in the result array
	 */
	void operate(A operand1, int index1, A operand2, int index2, A dst, int dstIndex);

	/**
	 * Constants identifying standard operators of this kind
	 */
	static enum Id {
	    /**
	     * Addition operator, returning {@code x + y} for two operands {@code x} 
	     * and {@code y}
		 */
		add, 
	    /**
	     * Subtraction operator, returning {@code x - y} for two operands 
	     * {@code x} and {@code y}
		 */
		subtract, 
	    /**
	     * Multiplication operator, returning {@code x * y} for two operands 
	     * {@code x} and {@code y}
		 */
		multiply, 
	    /**
	     * Division operator, returning {@code x / y} for two operands {@code x} 
	     * and {@code y}
		 */
		divide, 
	    /**
	     * Min operator, returning the smaller of the two operands, that is
	     * {@code min(x, y)} for two operands {@code x} and {@code y}
	     */
		min, 
	    /**
	     * Max operator, returning the larger of the two operands, that is
	     * {@code max(x, y)} for two operands {@code x} and {@code y}
	     */
		max;
	}
}
