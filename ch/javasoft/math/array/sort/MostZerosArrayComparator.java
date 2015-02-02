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
import ch.javasoft.math.operator.BooleanUnaryOperator;

/**
 * The <code>MostZerosArrayComparator</code> compares arrays according to the 
 * number of zeros, that is, an array is before another array if it containing 
 * more zero values.
 * 
 * @type T	number type
 * @type A	array type of number
 */
public class MostZerosArrayComparator<N extends Number, A> implements Comparator<A> {
	private final ArrayOperations<A>			aops;
	private final BooleanUnaryOperator<N, A> 	isZero;
	public MostZerosArrayComparator(NumberOperators<N, A> operators) {
		this.aops	= operators.getArrayOperations();
		this.isZero	= operators.booleanUnary(BooleanUnaryOperator.Id.isZero);
	}
	public int compare(A o1, A o2) {
		final int len1 = aops.getLength(o1);
		int cnt1 = 0;
		for (int i = 0; i < len1; i++) {
			if (isZero.booleanOperate(o1, i)) {
				cnt1++;
			}
		}
		final int len2 = aops.getLength(o2);
		int cnt2 = 0;
		for (int i = 0; i < len2; i++) {
			if (isZero.booleanOperate(o2, i)) {
				cnt2++;
			}
		}
		return cnt2 - cnt1;
	}
}
