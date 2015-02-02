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

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
 * The filtered network nests an original network, but some reactions and/or
 * metabolites can be removed (filtered).
 */
public class FilteredMetabolicNetwork extends AbstractMetabolicNetwork {
	
	private final MetabolicNetwork	mNetwork;
	private final Set<Metabolite>	mExcludedMetas	= new HashSet<Metabolite>();
	private final Set<Reaction>		mExcludedReacs	= new HashSet<Reaction>();
	
	private transient GenericDynamicArray<Metabolite>	tMetabolites	= null;
	private transient GenericDynamicArray<Reaction>		tReactions		= null;
	
	public FilteredMetabolicNetwork(MetabolicNetwork network) {
		mNetwork = network;
	}
	
	/**
	 * Constructor for <code>FilteredMetabolicNetwork</code> with specified
	 * reactions to suppress
	 * 
	 * @param network				the network
	 * @param suppressedReactions	the reactions to remove
	 */
	public FilteredMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) {
		this(network);
		if (suppressedReactions != null) {
			excludeReactions(suppressedReactions.toArray(new String[suppressedReactions.size()]));
		}
	}

	public void excludeMetabolite(String metaboliteName, boolean removeConcernedReactions) {
		excludeMetabolite(new DefaultMetabolite(metaboliteName), removeConcernedReactions);
	}
	public void excludeMetabolites(String[] metaboliteNames, boolean removeConcernedReactions) {
		for (int ii = 0; ii < metaboliteNames.length; ii++) {
			excludeMetabolite(new DefaultMetabolite(metaboliteNames[ii]), removeConcernedReactions);			
		}
	}
	public void excludeMetabolite(Metabolite metabolite, boolean removeConcernedReactions) {
		if (mExcludedMetas.add(metabolite)) {
			if (removeConcernedReactions) {
				for (Reaction reac : mNetwork.getReactions()) {
					if (!mExcludedReacs.contains(reac) && reac.isMetaboliteParticipating(metabolite)) {
						excludeReaction(reac);
					}
				}				
			}
			tMetabolites	= null;
			tReactions		= null;
		}
	}
	
	public void excludeReactions(String... reactionNames) {
		for (int ii = 0; ii < reactionNames.length; ii++) {
			excludeReaction(reactionNames[ii]);
		}
	}
	public void excludeReaction(String reactionName) {
		for (Reaction reac : getReactions()) {
			if (reac.getName().equals(reactionName)) {
				excludeReaction(reac);
				return;
			}
		}
		throw new IllegalArgumentException("no such reaction: " + reactionName);
	}
	public void excludeReaction(Reaction reaction) {
		if (mExcludedReacs.add(reaction)) {
			for (MetaboliteRatio metaRatio : reaction.getMetaboliteRatios()) {
				Metabolite meta = metaRatio.getMetabolite();
				if (!mExcludedMetas.contains(meta)) {//not yet removed
					int cnt = 0;
					Iterator<? extends Reaction> it = mNetwork.getReactions(meta).iterator();
					while (it.hasNext() && cnt == 0) {
						Reaction reac = it.next();
						if (!mExcludedReacs.contains(reac)) cnt++;						
					}
					if (cnt == 0) {
						mExcludedMetas.add(meta);
					}
				}
			}
			tMetabolites	= null;
			tReactions		= null;
		}
	}

	public ArrayIterable<Metabolite> getMetabolites() {
		if (tMetabolites == null) {
			tMetabolites = new GenericDynamicArray<Metabolite>();
			for (Metabolite meta : mNetwork.getMetabolites()) {
				if (!mExcludedMetas.contains(meta)) {
					tMetabolites.add(meta);
				}
			}
		}
		return tMetabolites;
	}

	public ArrayIterable<Reaction> getReactions() {
		if (tReactions == null) {
			Map<Metabolite, Integer> metaCounter = new HashMap<Metabolite, Integer>();
			tReactions = new GenericDynamicArray<Reaction>();
			for (Reaction reac : mNetwork.getReactions()) {
				if (!mExcludedReacs.contains(reac)) {
					tReactions.add(mapReaction(reac, metaCounter));
				}
			}
		}
		return tReactions;
	}
	
	private Reaction mapReaction(final Reaction reaction, Map<Metabolite, Integer> metaboliteCounter) {
		boolean mustWrap = false;
		for (MetaboliteRatio metaRatio : reaction.getMetaboliteRatios()) {
			Metabolite meta = metaRatio.getMetabolite();
			if (mExcludedMetas.contains(meta)) {
				mustWrap = true;
				break;
			}
		}
		if (mustWrap) {
			final GenericDynamicArray<MetaboliteRatio> wrapped = new GenericDynamicArray<MetaboliteRatio>();
			for (MetaboliteRatio metaRatio : reaction.getMetaboliteRatios()) {
				Metabolite meta = metaRatio.getMetabolite();
				if (mExcludedMetas.contains(meta)) {
					Integer iIndex = metaboliteCounter.get(meta);
					int index = iIndex == null ? 0 : iIndex.intValue();
					metaboliteCounter.put(meta, Integer.valueOf(index + 1));
					wrapped.add(
						new DefaultMetaboliteRatio(
							new DefaultMetabolite(meta.getName() + "-" + index),
							metaRatio.getRatio()
						)	
					);
				}
				else {
					wrapped.add(metaRatio);
				}
			}
			return new AbstractNamedReaction(reaction.getName()) {
				@Override
				public ArrayIterable<MetaboliteRatio> getMetaboliteRatios() {
					return wrapped;
				}
				@Override
				public ReactionConstraints getConstraints() {
					return reaction.getConstraints();
				}				
			};
		}
		else {
			return reaction;
		}
	}
	
	@SuppressWarnings("unchecked")
	public ReadableMatrix<?> getStoichiometricMatrix() {
		final ReadableMatrix<Number> orig = (ReadableMatrix<Number>)mNetwork.getStoichiometricMatrix();
		final int origMetas = orig.getRowCount();
		final int origReacs = orig.getColumnCount();
		final int metas 	= getMetabolites().length();
		final int reacs		= getReactions().length();
		final Number[][] stoich = (Number[][])Array.newInstance(orig.getNumberValueAt(0, 0).getClass(), new int[] {metas, reacs});
		int metaInd = 0;
		for (int m = 0; m < origMetas; m++) {
			final Metabolite meta = mNetwork.getMetabolites().get(m);
			if (!mExcludedMetas.contains(meta)) {
				int reacInd = 0;
				for (int r = 0; r < origReacs; r++) {
					final Reaction reac = mNetwork.getReactions().get(r);
					if (!mExcludedReacs.contains(reac)) {
						stoich[metaInd][reacInd] = orig.getNumberValueAt(m, r);
						reacInd++;
					}
				}
				metaInd++;
			}
		}
		WritableMatrix<?> stoichMx = orig.newInstance(stoich, true /*rowsInDim1*/);
		return stoichMx.toReadableMatrix(false /*enforceNewInstance*/);
	}

}
