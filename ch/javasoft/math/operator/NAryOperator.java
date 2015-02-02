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
 * The <code>NAryOperator</code> performs an operation on {@code n} many 
 * operands, where {@code n} is a fixed number. N-ary operators typically arise 
 * from concatenation of several {@link BinaryOperator binary} and 
 * {@link UnaryOperator unary} operators.
 * 
 * @type T	input and output type of the operation
 * @type A	array type of input operands and result
 */
public interface NAryOperator<T, A> {
	
	/**
	 * The number of operands, that is, the {@code "n"} for this n-ary operation
	 */
	int getOperandCount();
	/**
	 * Performs the binary operation &oplus; on two operands and returns the 
	 * result:
	 * <pre>
	 *   return = &oplus; ( operands )
	 * </pre>
	 * 
	 * @param offset	the offset for the first operand in {@code operands}
	 * @param operands	the {@link #getOperandCount() n} operands
	 * @return the result of the n-ary operation
	 */
	T operate(int offset, T... operands);

	/**
	 * Performs the binary operation &oplus; on two vector elements, also 
	 * writing the result back into a vector:
	 * <pre>
	 *   dst[dstIndex] = &oplus; ( operands[indices] )
	 * </pre>
	 * 
	 * @param offset	the offset for the first operand in {@code operands} and 
	 * 					{@code indices}
	 * @param operands	the {@link #getOperandCount() n} operand arrays
	 * @param indices	the indices in the operand arrays
	 * @param dst		the result array
	 * @param dstIndex	the index in the result array
	 */
	void operate(int offset, A[] operands, int[] indices, A dst, int dstIndex);
}
