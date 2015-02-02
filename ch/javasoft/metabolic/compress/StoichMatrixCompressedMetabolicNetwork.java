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
package ch.javasoft.metabolic.compress;

import java.util.ArrayList;
import java.util.List;

import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.impl.AbstractReaction;
import ch.javasoft.metabolic.impl.DefaultReactionConstraints;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericArray;
import ch.javasoft.util.genarr.GenericFixSizeArray;
import ch.javasoft.util.map.DefaultIntIntMultiValueMap;
import ch.javasoft.util.map.IntIntMultiValueMap;

/**
 * <tt>StoichMatrixCompressedMetabolicNetwork</tt> extends the fraction number
 * stoichiometric matrix network by uncompression methods. For information about
 * the compression, see {@link StoichMatrixCompressor}.
 */
public class StoichMatrixCompressedMetabolicNetwork extends FractionNumberStoichMetabolicNetwork implements CompressedMetabolicNetwork {

	private final MetabolicNetwork mParentNetwork;
	// private final BigIntegerRationalMatrix mPre;
	private final BigIntegerRationalMatrix mPost;
	private final BigIntegerRationalMatrix mStoichCompressed;

	// create the sparse version of the post matrix
	private int[][] tSparsePostIndices;
	private BigFraction[][] tSparsePostFrns;
	private double[][] tSparsePostDbls;

	public StoichMatrixCompressedMetabolicNetwork(BigIntegerRationalMatrix stoichOrig, boolean[] origReversible, BigIntegerRationalMatrix pre, BigIntegerRationalMatrix post,
			BigIntegerRationalMatrix compressedStoich) {
		this(new FractionNumberStoichMetabolicNetwork(stoichOrig, origReversible), pre, post, compressedStoich);
	}

	public StoichMatrixCompressedMetabolicNetwork(String[] origMetaboliteNames, String[] origReactionNames, boolean[] origReversible, BigIntegerRationalMatrix pre,
			BigIntegerRationalMatrix origStoich, BigIntegerRationalMatrix post, BigIntegerRationalMatrix compressedStoich) {
		this(new FractionNumberStoichMetabolicNetwork(origMetaboliteNames, origReactionNames, origStoich, origReversible), pre, post, compressedStoich);
	}

	public StoichMatrixCompressedMetabolicNetwork(MetabolicNetwork original, BigIntegerRationalMatrix pre, BigIntegerRationalMatrix post, BigIntegerRationalMatrix compressedStoich) {
		super(getMetaboliteNames(original, pre, post, compressedStoich), getReactionNames(original, pre, post, compressedStoich), compressedStoich,
				getReversible(original, pre, post, compressedStoich));
		mParentNetwork = original;
		// mPre = pre;
		mPost = post;
		mStoichCompressed = compressedStoich;
	}

	public MetabolicNetwork getParentNetwork() {
		return mParentNetwork;
	}

	public MetabolicNetwork getRootNetwork() {
		return (mParentNetwork instanceof CompressedMetabolicNetwork) ? ((CompressedMetabolicNetwork) mParentNetwork).getRootNetwork() : mParentNetwork;
	}

	// ========================================================
	// added by matthias
	// needed for infeasible pattern output in
	// at.acib.thermodynamic.PatternConverter.java
	public BigIntegerRationalMatrix getPostMatrix() {
		return mPost;
	}

	// matthias: end of implementation
	// ========================================================

	public List<Metabolite> getMappedMetabolites(List<Metabolite> original) {
		final List<Metabolite> res = new ArrayList<Metabolite>(original.size());
		for (int i = 0; i < original.size(); i++) {
			Metabolite orig = original.get(i);
			int index = getMetaboliteIndex(orig.getName());
			res.add(index < 0 ? null : orig);
		}
		return res;
	}

	public List<Reaction> getMappedReactions(List<Reaction> original) {
		final List<Reaction> res = new ArrayList<Reaction>(original.size());
		for (int i = 0; i < original.size(); i++) {
			final Reaction orig = original.get(i);
			final int origIndex = mParentNetwork.getReactionIndex(orig.getName());
			List<Reaction> mappings = new ArrayList<Reaction>(1);
			for (int newIndex = 0; newIndex < mStoichCompressed.getColumnCount(); newIndex++) {
				if (mPost.getSignumAt(origIndex, newIndex) != 0) {
					mappings.add(getReactions().get(newIndex));
				}
			}
			if (mappings.size() == 0) {
				res.add(null);
			} else if (mappings.size() == 1) {
				res.add(mappings.get(0));
			} else {
				res.add(new MultiplexedReaction());// TODO this is not so nice
			}
		}
		return res;
	}

	/**
	 * See
	 * {@link StoichMatrixCompressor#compress(ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix, boolean[], String[], String[], java.util.Set)}
	 * for details on uncompression.
	 */
	public FluxDistribution uncompressFluxDistribution(FluxDistribution fluxDistribution) {
		final FluxDistribution uncompressed;
		if (fluxDistribution.getPreferredNumberClass() == Double.class) {
			uncompressed = uncompressFluxDistributionsDouble(fluxDistribution);
		} else {
			uncompressed = uncompressFluxDistributionFactional(fluxDistribution);
		}
		if (mParentNetwork instanceof CompressedMetabolicNetwork) {
			return ((CompressedMetabolicNetwork) mParentNetwork).uncompressFluxDistribution(uncompressed);
		}
		return uncompressed;
	}

	private synchronized void initSparsePostMatrix() {
		if (tSparsePostIndices == null) {
			final int compReacs = getReactions().length();
			final int origReacs = mParentNetwork.getReactions().length();
			// create the sparse version of the post matrix
			final BigFraction[] template = new BigFraction[] {};
			tSparsePostIndices = new int[origReacs][];
			tSparsePostFrns = new BigFraction[origReacs][];
			for (int origReac = 0; origReac < origReacs; origReac++) {
				final IntArray indices = new IntArray();
				final List<BigFraction> values = new ArrayList<BigFraction>();
				for (int compReac = 0; compReac < compReacs; compReac++) {
					if (mPost.getSignumAt(origReac, compReac) != 0) {
						indices.add(compReac);
						values.add(mPost.getBigFractionValueAt(origReac, compReac));
					}
				}
				tSparsePostIndices[origReac] = indices.toArray();
				tSparsePostFrns[origReac] = values.toArray(template);
			}
		}
	}

	private synchronized void initSparsePostMatrixDbl() {
		if (tSparsePostDbls == null) {
			initSparsePostMatrix();
			tSparsePostDbls = new double[tSparsePostFrns.length][];
			for (int i = 0; i < tSparsePostDbls.length; i++) {
				tSparsePostDbls[i] = new double[tSparsePostFrns[i].length];
				for (int j = 0; j < tSparsePostDbls[i].length; j++) {
					tSparsePostDbls[i][j] = tSparsePostFrns[i][j].doubleValue();
				}
			}
		}
	}

	/**
	 * Fraction number version of the flux distribution uncompression. This is
	 * the default method. If the resulting flux values are doubles,
	 * {@link #uncompressFluxDistributionsDouble(FluxDistribution)} is used
	 * instead for faster computation.
	 */
	public FluxDistribution uncompressFluxDistributionFactional(final FluxDistribution compFluxDist) {
		initSparsePostMatrix();
		final int origReacs = mParentNetwork.getReactions().length();
		final FluxDistribution origFluxDist = compFluxDist.create(mParentNetwork);
		for (int origReac = 0; origReac < origReacs; origReac++) {
			BigFraction origFlux = BigFraction.ZERO;
			for (int i = 0; i < tSparsePostIndices[origReac].length; i++) {
				final int compReac = tSparsePostIndices[origReac][i];
				BigFraction cmpFlux = BigFraction.valueOf(compFluxDist.getNumberRate(compReac));
				if (!cmpFlux.isZero()) {
					BigFraction mul = tSparsePostFrns[origReac][i];
					BigFraction add = cmpFlux.multiply(mul);
					origFlux = origFlux.add(add).reduce();
				}
			}
			origFluxDist.setRate(origReac, origFlux);
		}
		return origFluxDist;
	}

	/**
	 * Same as {@link #uncompressFluxDistributionFactional(FluxDistribution)},
	 * but double values are used instead of fraction numbers for faster
	 * computation. This method is only used if the resulting flux values are
	 * doubles anyway.
	 */
	public FluxDistribution uncompressFluxDistributionsDouble(final FluxDistribution compFluxDist) {
		initSparsePostMatrixDbl();
		final int origReacs = mParentNetwork.getReactions().length();
		final FluxDistribution origFluxDist = compFluxDist.create(mParentNetwork);
		for (int origReac = 0; origReac < origReacs; origReac++) {
			double origFlux = 0d;
			for (int i = 0; i < tSparsePostIndices[origReac].length; i++) {
				final int compReac = tSparsePostIndices[origReac][i];
				double cmpFlux = compFluxDist.getDoubleRates()[compReac];
				if (cmpFlux != 0d) {
					origFlux += cmpFlux * tSparsePostDbls[origReac][i];
				}
			}
			origFluxDist.getDoubleRates()[origReac] = origFlux;
		}
		return origFluxDist;
	}

	/**
	 * A reaction that has been mapped to multiple compressed reactions. Happens
	 * with uniquely consumed/produced metabolites. This is a dummy class which
	 * is only used in
	 * {@link StoichMatrixCompressedMetabolicNetwork#getMappedReactions(List)}
	 */
	private static class MultiplexedReaction extends AbstractReaction {
		public MultiplexedReaction() {
		}

		@Override
		public ReactionConstraints getConstraints() {
			return new DefaultReactionConstraints(Double.NaN, Double.NaN);
		}

		@Override
		public ArrayIterable<? extends MetaboliteRatio> getMetaboliteRatios() {
			GenericArray<MetaboliteRatio> ratios = new GenericFixSizeArray<MetaboliteRatio>(1);
			return ratios;
		}

		@Override
		public String getName() {
			return "MultiplexedReaction[@" + objHashCode() + "]";
		}

		@Override
		public int hashCode() {
			return objHashCode();
		}
	}

	// //////////////////// helpers
	private static boolean[] getReversible(MetabolicNetwork original, BigIntegerRationalMatrix pre, BigIntegerRationalMatrix post, BigIntegerRationalMatrix stoichCompressed) {
		final boolean[] revs = new boolean[stoichCompressed.getColumnCount()];
		for (int newReac = 0; newReac < revs.length; newReac++) {
			boolean reversible = true;
			for (int oldReac = 0; oldReac < original.getReactions().length() && reversible; oldReac++) {
				if (post.getSignumAt(oldReac, newReac) != 0) {
					reversible &= original.getReactions().get(oldReac).getConstraints().isReversible();
				}
			}
			revs[newReac] = reversible;
		}
		return revs;
	}

	private static String[] getMetaboliteNames(MetabolicNetwork original, BigIntegerRationalMatrix pre, BigIntegerRationalMatrix post, BigIntegerRationalMatrix stoichCompressed) {
		final String[] newNames = new String[stoichCompressed.getRowCount()];
		for (int newMeta = 0; newMeta < newNames.length; newMeta++) {
			final StringBuilder sb = new StringBuilder();
			for (int oldMeta = 0; oldMeta < original.getMetabolites().length(); oldMeta++) {
				if (pre.getSignumAt(newMeta, oldMeta) != 0) {
					if (sb.length() > 0)
						sb.append("::");
					sb.append(original.getMetabolites().get(oldMeta).getName());
				}
			}
			newNames[newMeta] = sb.toString();
		}
		return newNames;
	}

	private static String[] getReactionNames(MetabolicNetwork original, BigIntegerRationalMatrix pre, BigIntegerRationalMatrix post, BigIntegerRationalMatrix stoichCompressed) {
		final String[] newNames = new String[stoichCompressed.getColumnCount()];
		for (int newReac = 0; newReac < newNames.length; newReac++) {
			final StringBuilder sb = new StringBuilder();
			for (int oldReac = 0; oldReac < original.getReactions().length(); oldReac++) {
				if (post.getSignumAt(oldReac, newReac) != 0) {
					if (sb.length() > 0)
						sb.append("::");
					sb.append(original.getReactions().get(oldReac).getName());
				}
			}
			newNames[newReac] = sb.toString();
		}
		return newNames;
	}

	public IntIntMultiValueMap getReactionMapping() {
		initSparsePostMatrix();
		final IntIntMultiValueMap map = new DefaultIntIntMultiValueMap();
		final int origReacs = mParentNetwork.getReactions().length();
		for (int origReac = 0; origReac < origReacs; origReac++) {
			map.addAll(origReac, tSparsePostIndices[origReac]);
		}
		return map;
	}

}
