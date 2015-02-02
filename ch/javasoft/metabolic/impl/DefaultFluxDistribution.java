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

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.util.FluxNormalizer;
import ch.javasoft.util.numeric.Zero;

/**
 * Implementation of {@link FluxDistribution} backing the flux values with
 * double values
 */
public class DefaultFluxDistribution extends AbstractFluxDistribution {
	
	private final double[] mRates;
	
	public DefaultFluxDistribution(MetabolicNetwork network, double[] rates) {
		super(network);
		mRates = rates;
	}

	public int getSize() {
		return mRates.length;
	}

	@Override
	public double[] getDoubleRates() {
		return mRates;
	}

	@Override
	public double getRate(Reaction reaction) {
		final int index = getReactionIndex(reaction);
		if (index < 0) {
			throw new IllegalArgumentException("no such reaction in the network: " + reaction);
		}
		return mRates[index];
	}
	
	
	public Double getNumberRate(Reaction reaction) {
		return Double.valueOf(getRate(reaction));
	}
	public Number getNumberRate(int index) {
		return Double.valueOf(mRates[index]);	
	}
	public int getRateSignum(int index) {
		return (int)Math.signum(mRates[index]);
	}
	public Number getCombinedRate(int indexForward, int indexBackward) {
		return Double.valueOf(mRates[indexForward] - mRates[indexBackward]);
	}
	
	public void setRate(int index, Number rate) {
		mRates[index] = rate.doubleValue();		
	}
	@Override
	public void setRate(Reaction reaction, Number rate) {
		final int index = getReactionIndex(reaction);
		if (index < 0) {
			throw new IllegalArgumentException("no such reaction in the network: " + reaction);
		}
		setRate(index, rate);		
	}
	public Class<Double> getPreferredNumberClass() {
		return Double.class;
	}
	
	public void norm(Norm norm, Zero zero) {
		switch (norm) {
			case max:
				FluxNormalizer.normalizeMax(mNetwork, mRates, zero);
				break;
			case min:
				FluxNormalizer.normalizeMin(mNetwork, mRates, zero);
				break;
			case norm2:
				FluxNormalizer.normalizeNorm2(mNetwork, mRates, zero);
				break;
			case squared:
				FluxNormalizer.normalizeSquared(mNetwork, mRates, zero);
				break;
			case none:
				//done
				break;

			default:
				throw new IllegalArgumentException("unsupported normalization: " + norm);
		}
	}	
	
	@Override
	public DefaultFluxDistribution clone() {
		return new DefaultFluxDistribution(mNetwork, mRates.clone());
	}
	public DefaultFluxDistribution create(MetabolicNetwork net) {
		return new DefaultFluxDistribution(net, new double[net.getReactions().length()]);
	}
	
	@Override
	public int hashCode() {
		int code = mNetwork.hashCode();
		for (int ii = 0; ii < mRates.length; ii++) {
			long bits = Double.doubleToLongBits(mRates[ii]);
			code ^= (int)(bits ^ (bits >>> 32));
		}
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof DefaultFluxDistribution) {
			DefaultFluxDistribution dist = (DefaultFluxDistribution)obj;
			if (mNetwork.equals(dist.getNetwork())) {
				double[] dRates = dist.mRates;
				for (int ii = 0; ii < mRates.length; ii++) {
					if (mRates[ii] != dRates[ii]) return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int ii = 0; ii < mRates.length; ii++) {
			if (ii > 0) sb.append(", ");
			sb.append(mRates[ii]);
		}
		sb.append(']');
		return sb.toString();
	}

}
