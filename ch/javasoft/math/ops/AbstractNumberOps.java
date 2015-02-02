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
package ch.javasoft.math.ops;

import ch.javasoft.math.NumberOperations;

abstract public class AbstractNumberOps<N extends Number> implements NumberOperations<N> {

	public N abs(N number) {
		return isNegative(number) ? negate(number) : number;
	}

	public N invert(N number) {
		return divide(one(), number);
	}

	public boolean isNegative(N number) {
		return signum(number) < 0;
	}

	public boolean isNonZero(N number) {
		return signum(number) != 0;
	}

	public boolean isOne(N number) {
		return 0 == compare(one(), number);
	}

	public boolean isPositive(N number) {
		return signum(number) >= 0;
	}

	public boolean isZero(N number) {
		return signum(number) == 0;
	}
	
	public boolean isNonNegative(N number) {
		return signum(number) >= 0;
	}
	public boolean isNonPositive(N number) {
		return signum(number) <= 0;
	}


	public N max(N valA, N valB) {
		final int cmp = compare(valA, valB);
		return cmp == -1 ? valB : valA;
	}

	public N max(N... vals) {
		N max = null;
		for (N val : vals) {
			max = max == null ? val : max(max, val);
		}
		return max;
	}

	public N min(N valA, N valB) {
		final int cmp = compare(valA, valB);
		return cmp == 1 ? valB : valA;
	}

	public N min(N... vals) {
		N min = null;
		for (N val : vals) {
			min = min == null ? val : min(min, val);
		}
		return min;
	}

}
