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
package ch.javasoft.math.array.sort;

import java.util.Comparator;

import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.operator.IntBinaryOperator;

/**
 * The <code>LexMinArrayComparator</code> sorts arrays according to the 
 * lexicographical ordering. This means that array elements are compared, and
 * the result is returned if it is unequal to zero. If equal to zero, the next
 * array element is compared. If all elements comparisons yield zero, the
 * shorter array is preferred, yielding zero if both arrays have equal length.  
 * 
 * @type T	number type
 * @type A	array type of number
 */
public class LexMinArrayComparator<N extends Number, A> implements Comparator<A> {
	private final ArrayOperations<A> 		aops;
	private final IntBinaryOperator<N, A>	comparator;
	public LexMinArrayComparator(NumberOperators<N, A> operators) {
		this.aops		= operators.getArrayOperations();
		this.comparator	= operators.intBinary(IntBinaryOperator.Id.compare);
	}
	public int compare(A o1, A o2) {
		final int len1 = aops.getLength(o1);
		final int len2 = aops.getLength(o1);
		final int len = Math.min(len1, len2);
		for (int i = 0; i < len; i++) {
			final int cmp = comparator.intOperate(o1, i, o2, i);
			if (cmp != 0) return cmp;
		}
		return len1 - len2;
	}
}
