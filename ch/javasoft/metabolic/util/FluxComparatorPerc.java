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
package ch.javasoft.metabolic.util;

import java.util.BitSet;
import java.util.Comparator;

import ch.javasoft.metabolic.FluxDistribution;

/**
 * Compare two flux distributions with respect a percentage tolerance
 */
public class FluxComparatorPerc implements Comparator<FluxDistribution> {
	private final float 	mPerc;
	private final BitSet	mIgnoreFluxes;
	public FluxComparatorPerc(float perc) {
		this(perc, new BitSet());
	}
	public FluxComparatorPerc(float perc, BitSet ignoreFluxes) {
		mPerc			= Math.abs(perc);
		mIgnoreFluxes	= ignoreFluxes;
	}
	public int compare(FluxDistribution f1, FluxDistribution f2) {
		double[] d1 = f1.getDoubleRates();
		double[] d2 = f2.getDoubleRates();
		for (int ii = 0; ii < d1.length; ii++) {
			if (!mIgnoreFluxes.get(ii)) {
				double sum = Math.abs(d1[ii]) + Math.abs(d2[ii]);
				if (sum > 0d) {
					double rDiff = (d1[ii] - d2[ii]) / sum;
					if (Math.abs(rDiff) > mPerc) {
						if (d1[ii] < d2[ii]) return -1;
						if (d1[ii] > d2[ii]) return 1;
						throw new RuntimeException("internal error: should not get here");
					}				
				}				
			}
		}			
		return 0;
	}
}
