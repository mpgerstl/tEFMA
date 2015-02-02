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
 * The <code>ConvertingUnaryOperator</code> performs an operation on a single 
 * operand and returns and output of a different type
 * 
 * @type I	input type of the operation
 * @type R	result or output type of the operation
 * @type AI	array type of input
 * @type AR	array type of result or output
 */
public interface ConvertingUnaryOperator<I, AI, R, AR> {
	/**
	 * Performs the unary operation &oplus; on an operand and returns the 
	 * result:
	 * <pre>
	 *   return = &oplus; ( operand )
	 * </pre>
	 * 
	 * @param operand	the operand to operate on
	 * @return the result of the unary operation
	 */
	R operate(I operand);

	/**
	 * Performs the unary operation &oplus; on a vector element, also writing 
	 * the result back into a vector:
	 * <pre>
	 *   dst[dstIndex] = &oplus; ( src[srcIndex] )
	 * </pre>
	 * 
	 * @param src		the operand array
	 * @param srcIndex	the index in the operand's array
	 * @param dst		the result array
	 * @param dstIndex	the index in the result array
	 */
	void operate(AI src, int srcIndex, AR dst, int dstIndex);
}
