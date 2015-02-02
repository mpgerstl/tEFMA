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

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.util.genarr.AbstractArrayIterable;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;

public class StoichMatrixMetabolicNetwork extends AbstractMetabolicNetwork {
	
	private final double[][]						mStoichMatrix;
	private final Metabolite[]						mMetabolites;
	private final String[]							mReactionNames;
	private final ReactionConstraints[]				mReactionConstraints;
	private final int[]								mMetaboliteIndices;
	private int										mMetaboliteCount;
	private final int[]								mReactionIndices;
	private int										mReactionCount;
	
	private transient GenericDynamicArray<Metabolite>	tMetabolites;
	private transient GenericDynamicArray<Reaction>		tReactions;

	public StoichMatrixMetabolicNetwork(MetabolicNetwork metaNet) {
		this(
			StoichiometricMatrices.createStoichiometricMatrix(metaNet), 
			metaNet.getMetabolites().toArray(new Metabolite[metaNet.getMetabolites().length()]),
			reactionNames(metaNet), reactionConstraints(metaNet)
		);
	}
	public StoichMatrixMetabolicNetwork(double[][] stoichMatrix, boolean[] reactionRevesibilities) {
		this(
			stoichMatrix, DefaultMetabolicNetwork.metaboliteNames(stoichMatrix), 
			DefaultMetabolicNetwork.reactionNames(stoichMatrix), reactionRevesibilities
		);
	}
	public StoichMatrixMetabolicNetwork(double[][] stoichMatrix, String[] metaboliteNames, String[] reactionNames, boolean[] reactionRevesibilities) {
		this(stoichMatrix, metaboliteNames, reactionNames, createReactionConstraints(reactionRevesibilities));
	}
	public StoichMatrixMetabolicNetwork(double[][] stoichMatrix, Metabolite[] metabolites, String[] reactionNames, boolean[] reactionRevesibilities) {
		this(stoichMatrix, metabolites, reactionNames, createReactionConstraints(reactionRevesibilities));
	}
	public StoichMatrixMetabolicNetwork(double[][] stoichMatrix, String[] metaboliteNames, String[] reactionNames, ReactionConstraints[] reactionConstraints) {
		this(stoichMatrix, DefaultMetabolicNetwork.metabolites(metaboliteNames), reactionNames, reactionConstraints);
	}
	public StoichMatrixMetabolicNetwork(double[][] stoichMatrix, Metabolite[] metabolites, String[] reactionNames, ReactionConstraints[] reactionConstraints) {
		mStoichMatrix			= stoichMatrix;
		mMetabolites			= metabolites;
		mReactionNames			= reactionNames;
		mReactionConstraints	= reactionConstraints;
		mMetaboliteIndices		= new int[metabolites.length];
		mReactionIndices		= new int[reactionNames.length];
		initIndicesAndCounts();
	}
	
	public void swapMetabolites(int metaboliteIndexA, int metaboliteIndexB) {
		swap(mMetaboliteIndices, metaboliteIndexA, metaboliteIndexB);
		tMetabolites	= null;
		tReactions		= null;
	}
	public void swapReactions(int reactionIndexA, int reactionIndexB) {
		swap(mReactionIndices, reactionIndexA, reactionIndexB);
		tMetabolites	= null;
		tReactions		= null;
	}
	public int getMetaboliteIndex(Metabolite meta) {
		for (int ii = 0; ii < mMetaboliteCount; ii++) {
			if (meta.equals(mMetabolites[mMetaboliteIndices[ii]])) {
				return ii;
			}
		}
		return -1;
	}
	public int hideMetabolite(Metabolite meta) {
		int metaIndex = getMetaboliteIndex(meta);
		if (metaIndex == -1) {
			throw new IllegalArgumentException("no such metabolite: " + meta);
		}
		return hideMetabolite(metaIndex);
	}
	public int hideMetabolite(int metaboliteIndex) {
		mMetaboliteCount--;
		swapMetabolites(metaboliteIndex, mMetaboliteCount);
		return mMetaboliteIndices[mMetaboliteCount];
	}
	public int getReactionIndex(Reaction react) {
		ArrayIterable<Reaction> reacts = getReactions();
		for (int ii = 0; ii < reacts.length(); ii++) {
			if (react.equals(reacts.get(ii))) {
				return ii;
			}
		}
		return -1;
	}
	public int hideReaction(Reaction react) {
		int reactionIndex = getReactionIndex(react);
		if (reactionIndex == -1) {
			throw new IllegalArgumentException("no such reaction: " + react);			
		}
		return hideReaction(reactionIndex);
	}
	public int hideReaction(int reactionIndex) {
		mReactionCount--;
		swapReactions(reactionIndex, mReactionCount);
		return mReactionIndices[mReactionCount];
	}
	
	private void initIndicesAndCounts() {
		for (int ii = 0; ii < mMetaboliteIndices.length ; ii++) {
			mMetaboliteIndices[ii] = ii;
		}
		for (int ii = 0; ii < mReactionIndices.length; ii++) {
			mReactionIndices[ii] = ii;
		}
		mMetaboliteCount	= mMetaboliteIndices.length;
		mReactionCount		= mReactionIndices.length;
	}
	
	public ArrayIterable<Metabolite> getMetabolites() {
		if (tMetabolites == null) {
			tMetabolites = createMetabolites(this);			
		}
		return tMetabolites;
	}

	public ArrayIterable<Reaction> getReactions() {
		if (tReactions == null) {
			tReactions = createReactions(this);			
		}
		return tReactions;
	}
	
	protected double getStoichiometricValue(int metaIndex, int reactionIndex) {
		return mStoichMatrix[metaIndex][reactionIndex];
	}
	
	public DoubleMatrix getStoichiometricMatrix() {
		return new DefaultDoubleMatrix(mStoichMatrix, true /*rowsInFirstDim*/);
	}
	


	//############################# static helpers
	private static GenericDynamicArray<Metabolite> createMetabolites(StoichMatrixMetabolicNetwork net) {
		GenericDynamicArray<Metabolite> result = new GenericDynamicArray<Metabolite>(net.mMetaboliteCount);
		for (int ii = 0; ii < net.mMetaboliteCount; ii++) {
			result.add(net.mMetabolites[net.mMetaboliteIndices[ii]]);
		}
		return result;
	}
	
	private static GenericDynamicArray<Reaction> createReactions(final StoichMatrixMetabolicNetwork net) {
		GenericDynamicArray<Reaction> result = new GenericDynamicArray<Reaction>(net.mReactionCount);
		for (int ii = 0; ii < net.mReactionCount; ii++) {
			final int reactionIndex = net.mReactionIndices[ii];
			result.add(
				new AbstractNamedReaction(net.mReactionNames[reactionIndex]) {
					@Override
					public ArrayIterable<MetaboliteRatio> getMetaboliteRatios() {
						return new AbstractArrayIterable<MetaboliteRatio>() {
							private int[] mMetaboliteIndices = initMetaIndices();
							private int[] initMetaIndices() {
								int cnt = 0;
								int[] inds = new int[net.mMetaboliteCount];
								for (int ii = 0; ii < net.mMetaboliteCount; ii++) {
									int metaIndex = net.mMetaboliteIndices[ii];
									if (net.getStoichiometricValue(metaIndex, reactionIndex) != 0.0d) {
										inds[cnt] = metaIndex;
										cnt++;
									}
								}
								int[] result = new int[cnt];
								System.arraycopy(inds, 0, result, 0, cnt);
								return result;
							}
							@Override
							public MetaboliteRatio get(int index) throws IndexOutOfBoundsException {
								int metaIndex = mMetaboliteIndices[index];
								return new DefaultMetaboliteRatio(
									net.mMetabolites[metaIndex], net.getStoichiometricValue(metaIndex, reactionIndex)									
								);
							}
							@Override
							public int length() {
								return mMetaboliteIndices.length;
							}
						};
					}					
					@Override
					public ReactionConstraints getConstraints() {
						return net.mReactionConstraints[reactionIndex];
					}
					@Override
					public double getRatioValueForMetabolite(Metabolite metabolite) {
						int metaIndex = net.mMetaboliteIndices[net.getMetaboliteIndex(metabolite)];
						return net.getStoichiometricValue(metaIndex, reactionIndex);
					}
				}
			);
		}
		return result;
	}
	
	private static ReactionConstraints[] createReactionConstraints(boolean[] reactionReversibilities) {
		ReactionConstraints[] constraints = new ReactionConstraints[reactionReversibilities.length];
		for (int ii = 0; ii < constraints.length; ii++) {
			constraints[ii] = reactionReversibilities[ii] ? 
					DefaultReactionConstraints.DEFAULT_REVERSIBLE : DefaultReactionConstraints.DEFAULT_IRREVERSIBLE;
		}
		return constraints;
	}
	
	private static String[] reactionNames(MetabolicNetwork metaNet) {
		ArrayIterable<? extends Reaction> reacts = metaNet.getReactions();
		String[] names = new String[reacts.length()];
		for (int ii = 0; ii < names.length; ii++) {
			names[ii] = reacts.get(ii).getName();
		}
		return names;
	}
	private static ReactionConstraints[] reactionConstraints(MetabolicNetwork metaNet) {
		ArrayIterable<? extends Reaction> reacts = metaNet.getReactions();
		ReactionConstraints[] constraints = new ReactionConstraints[reacts.length()];
		for (int ii = 0; ii < constraints.length; ii++) {
			constraints[ii] = reacts.get(ii).getConstraints();
		}
		return constraints;
	}

	private static void swap(int[] indices, int indexA, int indexB) {
		if (indexA != indexB) {
			int tmp = indices[indexA];
			indices[indexA] = indices[indexB];
			indices[indexB] = tmp;			
		}
	}
	
	
}
