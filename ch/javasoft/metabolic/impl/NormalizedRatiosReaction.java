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

import java.util.Collections;
import java.util.Comparator;

import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericFixSizeArray;
import ch.javasoft.util.numeric.Zero;

public class NormalizedRatiosReaction extends AbstractNestedReaction {

	public final double 					mNormalizationFactor;
	private ArrayIterable<MetaboliteRatio>	mNormalizedRatios;
	
	public NormalizedRatiosReaction(Reaction reaction) {
		super(reaction);
		mNormalizationFactor = normalizeRatios(reaction);
	}
	
	private double normalizeRatios(Reaction reaction) {
		ArrayIterable<? extends MetaboliteRatio> ratios = reaction.getMetaboliteRatios();
		GenericFixSizeArray<MetaboliteRatio> nRatios = new GenericFixSizeArray<MetaboliteRatio>(ratios.length());

		//find max ratio
		double max = -1.0d;
		for (int ii = 0; ii < ratios.length(); ii++) {
			max = Math.max(max, Math.abs(ratios.get(ii).getRatio()));
		}
		
		//normalize, max gets 1
		for (int ii = 0; ii < ratios.length(); ii++) {
			MetaboliteRatio ratio = ratios.get(ii);
			nRatios.set(ii, new DefaultMetaboliteRatio(ratio.getMetabolite(), ratio.getRatio() / max));
		}
		//sort by metabolite
		Collections.sort(nRatios, new Comparator<MetaboliteRatio>() {
			public int compare(MetaboliteRatio o1, MetaboliteRatio o2) {
				return o1.getMetabolite().getName().compareTo(o2.getMetabolite().getName());
			}
		});
		
		//set the normalized metabolites
		mNormalizedRatios = nRatios;
		
		//return the normalization factor
		return 1.0d / max;
	}
	
	/**
	 * @return 	<ul><li>{@code 1}	this and with are correlated, same direction</li>
	 * 				<li>{@code -1}	this and with are correlated, opposite direction</li>
	 * 				<li>{@code 0}	no correlation between this and with</li></ul>
	 */
	public int getCorrelation(NormalizedRatiosReaction with, Zero zero) {		
		ArrayIterable<MetaboliteRatio> ratiosMine	= mNormalizedRatios; 
		ArrayIterable<MetaboliteRatio> ratiosOther	= with.mNormalizedRatios;
		
		if (mNormalizedRatios.length() != ratiosOther.length()) return 0;
		
		//precond: the ratios are sorted by metabolite name (which is true for NormalizedRatiosReaction)
		int sign = 0;
		for (int ii = 0; ii < ratiosMine.length(); ii++) {
			if (!ratiosMine.get(ii).getMetabolite().equals(ratiosOther.get(ii).getMetabolite())) return 0;
			double valA = ratiosMine.get(ii).getRatio();
			double valB = ratiosOther.get(ii).getRatio();
			//the ratios must be all equal or all equal to the negative value
			if (sign == 0) sign = (int)Math.signum(valA * valB);
			if (sign > 0 && zero.isNonZero(valA - valB)) return 0;
			if (sign < 0 && zero.isNonZero(valA + valB)) return 0;
		}
		return sign;
	}

	@Override
	public ArrayIterable<MetaboliteRatio> getMetaboliteRatios() {
		return mNormalizedRatios;
	}

	@Override
	public ReactionConstraints getConstraints() {
		return mNestedReaction.getConstraints();
	}

}
