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
package ch.javasoft.metabolic.impl;

import ch.javasoft.metabolic.MetabolicNetworkVisitor;
import ch.javasoft.metabolic.ReactionConstraints;

public class DefaultReactionConstraints implements ReactionConstraints {
	
	private final double mLower, mUpper;
	
	public DefaultReactionConstraints(double lower, double upper) {
		if (lower > upper) {
			throw new IllegalArgumentException("lower bound > upper bound: " + lower + " > " + upper);
		}
		if ((lower == Double.NEGATIVE_INFINITY || lower < 0d) && upper == 0d) {
			throw new IllegalArgumentException("reverse irreversible reactions not supported: [" + lower + ", " + upper + "]");
		}
		mLower = lower;
		mUpper = upper;
	}	
	
	/** Reaction constraint constant with lower/upper bounds -Inf/+Inf respectively*/
	public static final ReactionConstraints DEFAULT_REVERSIBLE		= new DefaultReactionConstraints(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	/** Reaction constraint constant with lower/upper bounds 0/+Inf respectively*/
	public static final ReactionConstraints DEFAULT_IRREVERSIBLE	= new DefaultReactionConstraints(0d, Double.POSITIVE_INFINITY);

	public boolean isReversible() {
		return (mLower == Double.NEGATIVE_INFINITY || mLower < 0d) && (mUpper == Double.POSITIVE_INFINITY || mUpper > 0d);
	}

	public double getLowerBound() {
		return mLower;
	}
	
	public double getUpperBound() {
		return mUpper;
	}

	@Override
	public int hashCode() {
		//from Double.hashCode()
		int code;
		long bits;
		bits = Double.doubleToLongBits(mLower);
		code = (int)(bits ^ (bits >>> 32));
		bits = Double.doubleToLongBits(mUpper);
		code ^= (int)(bits ^ (bits >>> 32));
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof ReactionConstraints) {
			ReactionConstraints cons = (ReactionConstraints)obj;
			return mLower == cons.getLowerBound() && mUpper == cons.getUpperBound();
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + mLower + " .. " + mUpper + "]";
	}
	
	public void accept(MetabolicNetworkVisitor visitor) {
		visitor.visitReactionConstraints(this);
	}

}
