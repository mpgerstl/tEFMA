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

import java.math.BigInteger;
import java.util.Arrays;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.ops.BigFractionOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.util.FluxNormalizer;
import ch.javasoft.util.numeric.Zero;

/**
 * Implementation of {@link FluxDistribution} backing the flux values with
 * {@link BigFraction}s
 */
public class FractionNumberFluxDistribution extends AbstractFluxDistribution {
	
	private final BigFraction[] mRates;
	
	public FractionNumberFluxDistribution(MetabolicNetwork network, BigInteger[] rates) {
		super(network);
		final BigFraction[] frac = new BigFraction[rates.length];
		for (int i = 0; i < frac.length; i++) {
			frac[i] = BigFraction.valueOf(rates[i]);
		}
		mRates = frac;
	}
	public FractionNumberFluxDistribution(MetabolicNetwork network, BigFraction[] rates) {
		super(network);
		mRates = rates;
	}
	
	public int getSize() {
		return mRates.length;
	}
	
	public BigFraction getNumberRate(Reaction reaction) {
		final int index = getReactionIndex(reaction);
		if (index < 0) {
			throw new IllegalArgumentException("no such reaction in the network: " + reaction);
		}
		return getNumberRate(index);
	}
	public BigFraction getNumberRate(int index) {
		return mRates[index];
	}
	public int getRateSignum(int index) {
		return mRates[index].signum();
	}
	public Number getCombinedRate(int indexForward, int indexBackward) {
		final BigFraction result = mRates[indexForward].subtract(mRates[indexBackward]);
		result.reduce();
		return result;
	}
	
	public void setRate(int index, Number rate) {
		mRates[index] = BigFraction.valueOf(rate);
	}
	@Override
	public void setRate(Reaction reaction, Number rate) {
		final int index = getReactionIndex(reaction);
		if (index < 0) {
			throw new IllegalArgumentException("no such reaction in the network: " + reaction);
		}
		setRate(index, rate);		
	}
	public Class<BigFraction> getPreferredNumberClass() {
		return BigFraction.class;
	}
	
	public void norm(Norm norm, Zero zero) {
		switch (norm) {
			case max:
				FluxNormalizer.normalizeMax(mNetwork, mRates, BigFractionOperations.instance(), zero);
				break;
			case min:
				FluxNormalizer.normalizeMin(mNetwork, mRates, BigFractionOperations.instance(), zero);
				break;
			case norm2:
				FluxNormalizer.normalizeNorm2(mNetwork, mRates, BigFractionOperations.instance(), zero);
				break;
			case squared:
				FluxNormalizer.normalizeSquared(mNetwork, mRates, BigFractionOperations.instance(), zero);
				break;
			case none:
				//done
				break;

			default:
				throw new IllegalArgumentException("unsupported normalization: " + norm);
		}
	}	

	@Override
	public FractionNumberFluxDistribution clone() {
		return new FractionNumberFluxDistribution(mNetwork, mRates.clone());
	}
	
	public FractionNumberFluxDistribution create(MetabolicNetwork net) {
		final BigFraction[] rates = new BigFraction[net.getReactions().length()];
		Arrays.fill(rates, BigFraction.ZERO);
		return new FractionNumberFluxDistribution(net, rates);
	}

}
