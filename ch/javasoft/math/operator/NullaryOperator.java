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

import java.math.BigInteger;

/**
 * The <code>NullaryOperator</code> performs an operation without operands. 
 * Typical nullary operators are functions returning a constant output value. An
 * example of a nullary operator not returning a constant value is the random
 * function.
 * 
 * @type T	result type of the operation
 * @type A	array type of result
 */
public interface NullaryOperator<T, A> extends NAryOperator<T, A> {
	/**
	 * Performs the nullary operation <code>&oplus;()</code> and returns the 
	 * result:
	 * <pre>
	 *   return = &oplus;()
	 * </pre>
	 * 
	 * @return the operation result, for instance a constant
	 */
	T operate();
	
	/**
	 * Performs the nullary operation <code>&oplus;()</code> and writes the 
	 * result to a vector:
	 * <pre>
	 *   dst[dstIndex] = &oplus;()
	 * </pre>
	 * 
	 * 
	 * @param dst		the result array
	 * @param dstIndex	the index in the result array
	 */
	void operate(A dst, int dstIndex);

	/**
	 * Constants identifying standard operators of this kind
	 */
	static enum Id {
		/**
		 * Constant operator for the zero constant
		 */
		zero,
		/**
		 * Constant operator for the one constant
		 */
		one,
		/**
		 * Random operator, returns a pseudo random number, usually with 
		 * approximately uniform distribution in the number range. For number 
		 * formats supporting (nearly) infinite size, such as {@link BigInteger} 
		 * numbers, uniformity might be restricted to a subrange.
		 * <p>
		 * See also {@link Math#random()}
		 */
		random
	}
}
