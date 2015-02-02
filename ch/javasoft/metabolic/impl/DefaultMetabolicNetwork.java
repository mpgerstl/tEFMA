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

import java.util.LinkedHashSet;
import java.util.Set;

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

public class DefaultMetabolicNetwork extends AbstractMetabolicNetwork {
	
	private GenericDynamicArray<? extends Metabolite> mMetabolites;
	private GenericDynamicArray<? extends Reaction>	mReactions;
	
	public DefaultMetabolicNetwork(double[][] stoichMatrix, boolean[] reversible) {
		this(metabolites(metaboliteNames(stoichMatrix)), reactionNames(stoichMatrix), stoichMatrix, reversible);
	}	
	public DefaultMetabolicNetwork(String[] metaboliteNames, String[] reactionNames, double[][] stoichMatrix, boolean[] reversible) {
		this(metabolites(metaboliteNames), reactionNames, stoichMatrix, reversible);
	}
	public DefaultMetabolicNetwork(String[] metaboliteNames, String[] reactionNames, double[][] stoichMatrix, ReactionConstraints[] constraints) {
		this(metabolites(metaboliteNames), reactionNames, stoichMatrix, constraints);
	}
	
	public DefaultMetabolicNetwork(Metabolite[] metabolites, String[] reactionNames, double[][] stoichMatrix, boolean[] reversible) {
		this(metabolites, reactions(metabolites, reactionNames, stoichMatrix, reversible));
	}	
	public DefaultMetabolicNetwork(Metabolite[] metabolites, String[] reactionNames, double[][] stoichMatrix, ReactionConstraints[] constraints) {
		this(metabolites, reactions(metabolites, reactionNames, stoichMatrix, constraints));
	}	
	public DefaultMetabolicNetwork(Reaction[] reactions) {
		this(metabolites(reactions), reactions);
	}
	public DefaultMetabolicNetwork(Iterable<? extends Metabolite> metabolites, Iterable<? extends Reaction> reactions) {		
		this(new GenericDynamicArray<Metabolite>(metabolites), new GenericDynamicArray<Reaction>(reactions));
	}
	public DefaultMetabolicNetwork(Metabolite[] metabolites, Reaction[] reactions) {
		this(new GenericDynamicArray<Metabolite>(metabolites), new GenericDynamicArray<Reaction>(reactions));
	}

	public DefaultMetabolicNetwork(GenericDynamicArray<? extends Metabolite> metabolites, GenericDynamicArray<? extends Reaction> reactions) {
		mMetabolites	= metabolites;
		mReactions		= reactions;
	}

	public ArrayIterable<? extends Metabolite> getMetabolites() {
		return mMetabolites;
	}

	public ArrayIterable<? extends Reaction> getReactions() {
		return mReactions;
	}

	public DoubleMatrix getStoichiometricMatrix() {
		return new DefaultDoubleMatrix(StoichiometricMatrices.createStoichiometricMatrix(this), true /*rowsInFirstDim*/);
	}
	
	/////////////////////////////// static helpers
	
	public static String[] metaboliteNames(double[][] stoichMatrix) {
		return metaboliteNames(stoichMatrix.length);
	}
	public static String[] metaboliteNames(int length) {
		return DefaultMetabolite.names(length, metabolitePrefix());
	}
	public static String[] reactionNames(double[][] stoichMatrix) {
		return reactionNames(stoichMatrix.length == 0 ? 0 : stoichMatrix[0].length);
	}
	public static String[] reactionNames(int length) {
		return names(length, reactionPrefix());
	}
	
	private static String[] names(int count, String prefix) {
		String[] names = new String[count];
		int len = String.valueOf(names.length - 1).length();
		StringBuffer zeros = new StringBuffer(len);
		for (int ii = 0; ii < len; ii++) {
			zeros.append('0');
		}
		for (int ii = 0; ii < names.length; ii++) {
			String sII = String.valueOf(ii);
			names[ii] = prefix + zeros.substring(0, zeros.length() - sII.length()) + sII;			
		}
		return names;		
	}
	
	public static Metabolite[] metabolites(Reaction[] reactions) {
		Set<Metabolite> metas = new LinkedHashSet<Metabolite>();
		for (Reaction reac : reactions) {
			for (MetaboliteRatio ratio : reac.getMetaboliteRatios()) {
				if (!metas.contains(ratio.getMetabolite())) {
					metas.add(ratio.getMetabolite());
				}
			}
		}
		Metabolite[] res = new Metabolite[metas.size()];
		metas.toArray(res);
		return res;
	}
	public static Metabolite[] metabolites(String[] metaboliteNames) {
		Metabolite[] metabolites = new Metabolite[metaboliteNames.length];
		for (int ii = 0; ii < metabolites.length; ii++) {
			metabolites[ii] = new DefaultMetabolite(metaboliteNames[ii]);
		}
		return metabolites;
	}
	
	private static Reaction[] reactions(Metabolite[] metabolites, String[] reactionNames, double[][] stoichMatrix, boolean[] reversible) {
		ReactionConstraints[] constr = new ReactionConstraints[reactionNames.length];
		for (int ii = 0; ii < constr.length; ii++) {
			constr[ii] = ii < reversible.length && reversible[ii] ? /*irreversible is default*/ 
					DefaultReactionConstraints.DEFAULT_REVERSIBLE : 
					DefaultReactionConstraints.DEFAULT_IRREVERSIBLE;
		}
		return reactions(metabolites, reactionNames, stoichMatrix, constr);
	}
	private static Reaction[] reactions(final Metabolite[] metabolites, String[] reactionNames, final double[][] stoichMatrix, final ReactionConstraints[] constr) {
		Reaction[] reactions = new Reaction[reactionNames.length];
		for (int ii = 0; ii < reactions.length; ii++) {
			final int reactionIndex = ii;
			final int[] metaIndices = getMetaboliteIndices(stoichMatrix, reactionIndex);
			reactions[ii] = new AbstractNamedReaction(reactionNames[reactionIndex]) {
				@Override
				public ReactionConstraints getConstraints() {
					return constr[reactionIndex];
				}
				@Override
				public ArrayIterable<MetaboliteRatio> getMetaboliteRatios() {
					return new AbstractArrayIterable<MetaboliteRatio>() {
						@Override
						public int length() {
							return metaIndices.length;
						}
						@Override
						public MetaboliteRatio get(int index) throws IndexOutOfBoundsException {
							return new DefaultMetaboliteRatio(
								metabolites[metaIndices[index]],
								stoichMatrix[metaIndices[index]][reactionIndex]
							); 
						}						
					};
				}
			};
		}
		return reactions;
	}
	
	private static int[] getMetaboliteIndices(double[][] stoichMatrix, int reactionIndex) {
		int[] tmp = new int[stoichMatrix.length];
		int cnt = 0;
		for (int ii = 0; ii < stoichMatrix.length; ii++) {
			if (stoichMatrix[ii][reactionIndex] != 0.0d) {
				tmp[cnt] = ii;
				cnt++;
			}
		}
		int[] result = new int[cnt];
		System.arraycopy(tmp, 0, result, 0, cnt);
		return result;
	}
	
	private static String metabolitePrefix() {
//		return "M";
		return "";
	}
	
	private static String reactionPrefix() {
		return "R";
	}

}
