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
package ch.javasoft.math.operator.compose;

import ch.javasoft.math.operator.AbstractBinaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.UnaryOperator;

/**
 * The <code>ConcatBinaryOperator</code> applies a {@link UnaryOperator} &oplus; 
 * to the result a nested {@link BinaryOperator} instance. It is thus itself a
 * binary operator:
 * <pre>
 *   this.operate(x1, x2) = &oplus; ( operand.operate(x1, x2) )
 * </pre>
 * 
 * @type T	result type of the operation
 * @type A	array type of result
 */
public class ConcatBinaryOperator<T extends Number, A> extends AbstractBinaryOperator<T, A> {

	private final UnaryOperator<T, A>	operator;
	private final BinaryOperator<T, A>	operand;
	
	public ConcatBinaryOperator(UnaryOperator<T, A> operator, BinaryOperator<T, A> operand) {
		this.operator	= operator;
		this.operand	= operand;
	}

	public T operate(T operand1, T operand2) {
		return operator.operate(this.operand.operate(operand1, operand2));
	}
	
	public void operate(A operand1, int index1, A operand2, int index2, A dst, int dstIndex) {
		operand.operate(operand1, index1, operand2, index2, dst, dstIndex);
		operator.operate(dst, dstIndex, dst, dstIndex);
	}

}
