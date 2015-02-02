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

import java.util.LinkedHashMap;
import java.util.Map;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;

/**
 * A network based on an original network, which adds additional constraints to
 * some reversible reactions. The constraint reactions are made only forward or
 * backward reactions.
 */
public class ConstrainedReversibilitiesMetabolicNetwork extends AbstractMetabolicNetwork {
	
	private final MetabolicNetwork			mNetwork;
	private final Map<Reaction, Reaction>	mConstrained = new LinkedHashMap<Reaction, Reaction>();	
	
	private transient GenericDynamicArray<Reaction>	tReactions 	= null;
	private transient ReadableMatrix<?> 			tStoich		= null;
	
	public ConstrainedReversibilitiesMetabolicNetwork(MetabolicNetwork network) {
		mNetwork = network;
	}
	
	public ArrayIterable<Reaction> getReactions() {
		if (tReactions == null) {
			tReactions = new GenericDynamicArray<Reaction>();
			for (Reaction reac : mNetwork.getReactions()) {
				final Reaction constrained = mConstrained.get(reac);
				tReactions.add(constrained == null ? reac: constrained);
			}
		}
		return tReactions;
	}
	
	public void constrainReaction(String name, boolean forward) {
		final Reaction reac = mNetwork.getReaction(name);
		if (!reac.getConstraints().isReversible()) {
			throw new IllegalArgumentException("reaction must be reversible: " + reac);
		}
		if (mConstrained.containsKey(reac)) {
			throw new IllegalArgumentException("reaction is already constrained: " + mConstrained.get(reac));
		}
		
		final GenericDynamicArray<MetaboliteRatio> ratios = new GenericDynamicArray<MetaboliteRatio>(reac.getMetaboliteRatios());
		if (!forward) {
			for (int i = 0; i < ratios.length(); i++) {
				ratios.set(i, ratios.get(i).invert());
			}
		}
		mConstrained.put(reac, new AbstractNestedReaction(reac) {
			@Override
			public ReactionConstraints getConstraints() {
				return DefaultReactionConstraints.DEFAULT_IRREVERSIBLE;
			}
			@Override
			public ArrayIterable<? extends MetaboliteRatio> getMetaboliteRatios() {
				return ratios;
			}
		});
	}

	public ArrayIterable<? extends Metabolite> getMetabolites() {
		return mNetwork.getMetabolites();
	}

	public ReadableMatrix<?> getStoichiometricMatrix() {
		if (tStoich == null) {
			final WritableMatrix<?> mx = mNetwork.getStoichiometricMatrix().toWritableMatrix(true /*new instance*/);
			for (final Reaction reac : mConstrained.keySet()) {
				final Reaction cons = mConstrained.get(reac);
				if (!reac.getMetaboliteRatios().equals(cons.getMetaboliteRatios())) {
					final int rindex = mNetwork.getReactionIndex(reac.getName());
					for (final MetaboliteRatio ratio : reac.getMetaboliteRatios()) {
						final int mindex = mNetwork.getMetaboliteIndex(ratio.getMetabolite().getName());
						mx.negate(mindex, rindex);
					}
				}
			}
			tStoich = mx.toReadableMatrix(false /*new instance*/);
		}
		return tStoich;
	}
	
}
