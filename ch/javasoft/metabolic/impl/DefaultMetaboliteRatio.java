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
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;

public class DefaultMetaboliteRatio implements MetaboliteRatio {
	
	private final Metabolite	mMetabolite;
	private final double		mRatio;
	
	public DefaultMetaboliteRatio(Metabolite metabolite, double ratio) {
		mMetabolite	= metabolite;
		mRatio		= ratio;		
	}

	public Metabolite getMetabolite() {
		return mMetabolite;
	}

	public double getRatio() {
		return mRatio;
	}
	public Double getNumberRatio() {
		return Double.valueOf(getRatio());
	}
	
	public boolean isEduct() {
		return mRatio < 0.0d;
	}
	
	@Override
	public String toString() {
		return toString(mRatio, mMetabolite);
	}
	
	public String toStringAbs() {
		return toString(Math.abs(mRatio), mMetabolite);
	}
	public static String toString(double value, Metabolite metabolite) {
		int intVal = (int)value;
		if (intVal == value) {
			if (intVal == 1) return metabolite.toString();
			return String.valueOf(intVal) + " " + metabolite;
		}
		return String.valueOf(value) + " " + metabolite;
	}
	
	public boolean isIntegerRatio() {
		return ((int)mRatio) == mRatio;
	}
	
	@Override
	public int hashCode() {
		//from Double.hashCode()
		long bits = Double.doubleToLongBits(mRatio);
		//Double: return (int)(bits ^ (bits >>> 32));		
		return mMetabolite.hashCode() ^ (int)(bits ^ (bits >>> 32));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() == getClass()) {
			MetaboliteRatio ratio = (MetaboliteRatio)obj;
			return 
				mMetabolite.equals(ratio.getMetabolite()) &&
				mRatio == ratio.getRatio();
		}
		return false;
	}
	
	public void accept(MetabolicNetworkVisitor visitor) {
		visitor.visitMetaboliteRatio(this);
	}
	
	public DefaultMetaboliteRatio invert() {
		return new DefaultMetaboliteRatio(mMetabolite, mRatio);
	}
	
}
