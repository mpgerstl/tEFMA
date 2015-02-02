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
import ch.javasoft.metabolic.Reaction;

abstract public class AbstractFluxDistribution implements FluxDistribution {
	
	protected final MetabolicNetwork mNetwork;
	
	public AbstractFluxDistribution(MetabolicNetwork network) {
		mNetwork = network;
	}

	public MetabolicNetwork getNetwork() {
		return mNetwork;		
	}

	public int getReactionIndex(Reaction reaction) {
		try {
			final int index = mNetwork.getReactionIndex(reaction.getName());
			if (reaction.equals(mNetwork.getReactions().get(index))) {
				return index;
			}
			return -1;
		}
		catch (IllegalArgumentException ex) {
			return -1;
		}
	}
	public double getRate(Reaction reaction) {
		return getNumberRate(reaction).doubleValue();
	}
	
	public void setRate(Reaction reaction, Number rate) {
		final int index = getReactionIndex(reaction);
		if (index < 0) {
			throw new IllegalArgumentException("no such reaction in the network: " + reaction);
		}
		setRate(index, rate);		
	}
	public double[] getDoubleRates() {
		final double[] rates = new double[getSize()];
		for (int i = 0; i < rates.length; i++) {
			rates[i] = getNumberRate(i).doubleValue();
		}
		return rates;
	}
	
	@Override
	abstract public AbstractFluxDistribution clone();
		
	@Override
	public int hashCode() {
		final int len = getSize();
		int code = mNetwork.hashCode();
		for (int ii = 0; ii < len; ii++) {
			code ^= getNumberRate(ii).hashCode();
		}
		return code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof FluxDistribution) {
			FluxDistribution dist = (FluxDistribution)obj;
			if (mNetwork.equals(dist.getNetwork())) {
				final int len = getSize();
				for (int ii = 0; ii < len; ii++) {
					if (!getNumberRate(ii).equals(dist.getNumberRate(ii))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		final int len = getSize();
		final StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int ii = 0; ii < len; ii++) {
			if (ii > 0) sb.append(", ");
			sb.append(getNumberRate(ii));
		}
		sb.append(']');
		return sb.toString();
	}

}
