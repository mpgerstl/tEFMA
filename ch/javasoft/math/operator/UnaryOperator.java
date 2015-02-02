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
 * The <code>UnaryOperator</code> performs an operation on a single operand and
 * returns an output of the same type. A typical operator is negation or 
 * absolute value of a number, but also identity is important for instance to
 * convert an array element to the boxed object type.
 * 
 * @type T	input and output type of the operation
 * @type A	array type of operand and result
 */
public interface UnaryOperator<T, A> extends NAryOperator<T, A> {
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
	T operate(T operand);
	
	/**
	 * Performs the unary operation &oplus; on a vector element and returns the 
	 * result:
	 * <pre>
	 *   return = &oplus; ( src[srcIndex] )
	 * </pre>
	 * 
	 * @param src		the operand array
	 * @param srcIndex	the index in the operand's array
	 */
	T operate(A src, int srcIndex);

	/**
	 * Performs the unary operation &oplus; on a vector element, also writing 
	 * the result back to a vector: 
	 * result:
	 * <pre>
	 *   dst[dstIndex] = &oplus; ( src[srcIndex] )
	 * </pre>
	 * 
	 * @param src		the operand array
	 * @param srcIndex	the index in the operand's array
	 * @param dst		the result array
	 * @param dstIndex	the index in the result array
	 */
	void operate(A src, int srcIndex, A dst, int dstIndex);

	/**
	 * Constants identifying standard operators of this kind
	 */
	static enum Id {
		/**
		 * Identity operator, returns the operand itself
		 */
		identity, 
		/**
		 * Normalization operator, for instance, a fraction number is reduced,
		 * or a value is rounded
		 */
		normalize,
	    /**
	     * Absolute value operator, returns the absolute value of the operand,
	     * that is, {@code |x|} for an operand {@code x}. For example, see also
	     * {@link Math#abs(double)}.
		 */
		abs, 
	    /**
	     * Negation operator, returning the negated value of the operand, that 
	     * is, {@code -x} for an operand {@code x}.
		 */
		negate, 
	    /**
	     * Inversion operator, returning the inverted value of the operand, that 
	     * is, {@code 1/x} for an operand {@code x}.
		 */
		invert, 
	    /**
	     * Square operator, returning the squared value of the operand, that 
	     * is, <code>x<sup>2</sup></code> for an operand {@code x}.
		 */
		square;
	}
	
}
