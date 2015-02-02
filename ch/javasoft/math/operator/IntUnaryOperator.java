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
 * The <code>IntUnaryOperator</code> performs an operation on a single operand 
 * and returns an integer output. A typical operator is the signum function.
 * 
 * @type T	input type of the operation
 * @type A	array type of operand
 */
public interface IntUnaryOperator<T, A> extends ConvertingUnaryOperator<T, A, Integer, int[]> {
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
	int intOperate(T operand);
	
	/**
	 * Performs the unary operation &oplus; on a vector element and returns the 
	 * result:
	 * <pre>
	 *   return = &oplus; ( src[srcIndex] )
	 * </pre>
	 * 
	 * @param operand	the operand array
	 * @param index		the index in the operand's array
	 */
	int intOperate(A operand, int index);

	/**
	 * Constants identifying standard operators of this kind
	 */
	static enum Id {
		/**
		 * The signum or sign function, returning -1/0/1 for negative, zero and
		 * positive values
		 */
		signum
	}
	
}
