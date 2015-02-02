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

import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.NAryOperator;

/**
 * The <code>NAryNAryOperator</code> applies a {@link BinaryOperator} 
 * &oplus; to the result of two nested {@link NAryOperator} instances. 
 * It is thus itself again a n-ary operator:
 * <pre>
 *   this.operate(x*, y*) = ( operand1.operate(x*) &oplus; operand2.operate(y*) )
 * </pre>
 * 
 * @type T	result type of the operation
 * @type A	array type of result
 */
public class NAryNAryOperator<T extends Number, A> implements NAryOperator<T, A> {

	private final BinaryOperator<T, A>	operator;
	private final NAryOperator<T, A> 	operand1;
	private final NAryOperator<T, A> 	operand2;
	private final TempArray<A>			temp;
	
	public NAryNAryOperator(ArrayOperations<A> ops, BinaryOperator<T, A> operator, NAryOperator<T, A> operand1, NAryOperator<T, A> operand2) {
		this.operator	= operator;
		this.operand1	= operand1;
		this.operand2	= operand2;
		this.temp 		= new TempArray<A>(ops, 2);
	}
	public int getOperandCount() {
		return operand1.getOperandCount() + operand2.getOperandCount();
	}
	public T operate(int offset, T... operands) {
		final T operated1 = operand1.operate(offset, operands);
		final T operated2 = operand2.operate(offset + operand1.getOperandCount(), operands);
		return operator.operate(operated1, operated2);
	}
	public void operate(int offset, A[] operands, int[] indices, A dst, int dstIndex) {
		final A tmp = temp.get();
		operand1.operate(offset, operands, indices, tmp, 0);
		operand2.operate(offset + operand1.getOperandCount(), operands, indices, tmp, 1);
		this.operator.operate(tmp, 0, tmp, 1, dst, dstIndex);
	}
}