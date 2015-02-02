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
import java.util.HashMap;
import java.util.Map;

import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.MetabolicNetworkVisitor;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericArray;
import ch.javasoft.util.genarr.GenericFixSizeArray;

public class FractionNumberStoichMetabolicNetwork extends AbstractMetabolicNetwork {

	private final String[] 					mMetaboliteNames;
	private final String[] 					mReactionNames;
	private final boolean[] 				mReversible;
	private final BigIntegerRationalMatrix	mStoich;
	
	//some lookup stuff, lazily initialized
	private Map<String, Integer> 		tMetaIndexByName	= null;
	private Map<String, Integer> 		tReacIndexByName	= null;
	private GenericArray<Metabolite>	tMetabolies			= null;
	private GenericArray<Reaction>		tReactions			= null;
	
	public FractionNumberStoichMetabolicNetwork(BigIntegerRationalMatrix stoich, boolean[] reversible) {
		this(
			DefaultMetabolicNetwork.metaboliteNames(stoich.getRowCount()),
			DefaultMetabolicNetwork.reactionNames(stoich.getColumnCount()),
			stoich, reversible
		);
	}
	public FractionNumberStoichMetabolicNetwork(String[] metaboliteNames, String[] reactionNames, BigIntegerRationalMatrix stoich, boolean[] reversible) {
		mMetaboliteNames	= metaboliteNames;
		mReactionNames		= reactionNames;
		mReversible			= reversible;
		mStoich				= stoich;		
	}
	
	protected Map<String, Integer> getMetaIndicesByName() {
		if (tMetaIndexByName == null) {
			tMetaIndexByName = new HashMap<String, Integer>();
			for (int i = 0; i < mMetaboliteNames.length; i++) {
				tMetaIndexByName.put(mMetaboliteNames[i], Integer.valueOf(i));
			}
		}
		return tMetaIndexByName;
	}
	protected Map<String, Integer> getReacIndicesByName() {
		if (tReacIndexByName == null) {
			tReacIndexByName = new HashMap<String, Integer>();
			for (int i = 0; i < mReactionNames.length; i++) {
				tReacIndexByName.put(mReactionNames[i], Integer.valueOf(i));
			}
		}
		return tReacIndexByName;
	}
	@Override
	public Metabolite getMetabolite(String name) {
		int index = getMetaboliteIndex(name);
		if (index < 0) {
			throw new IllegalArgumentException("no such metabolite: " + name);
		}
		return getMetabolites().get(index);
	}
	@Override
	public int getMetaboliteIndex(String name) {
		Integer index = getMetaIndicesByName().get(name);
		return index == null ? -1 : index.intValue();
	}
	public ArrayIterable<? extends Metabolite> getMetabolites() {
		if (tMetabolies == null) {
			tMetabolies = new GenericFixSizeArray<Metabolite>(mMetaboliteNames.length);
			for (int i = 0; i < mMetaboliteNames.length; i++) {
				tMetabolies.set(i, new DefaultMetabolite(mMetaboliteNames[i]));
			}
		}
		return tMetabolies;
	}
	@Override
	public Reaction getReaction(String name) {
		int index = getReactionIndex(name);
		if (index < 0) {
			throw new IllegalArgumentException("no such reaction: " + name);
		}
		return getReactions().get(index);
	}
	@Override
	public int getReactionIndex(String name) {
		Integer index = getReacIndicesByName().get(name);
		return index == null ? -1 : index.intValue();
	}
	public ArrayIterable<? extends Reaction> getReactions() {
		if (tReactions == null) {
			tReactions = new GenericFixSizeArray<Reaction>(mReactionNames.length);
			for (int i = 0; i < mReactionNames.length; i++) {
				tReactions.set(i, new BigIntegerReaction(i));
			}
		}
		return tReactions;
	}

	public ReadableBigIntegerRationalMatrix<?> getStoichiometricMatrix() {
		return mStoich;
	}
	
	private class BigIntegerReaction extends AbstractReaction {
		private final int	mReactionIndex;
		private final int[] mMetaIndices;//metabolites with non-zero entry in stoich
		public BigIntegerReaction(int reactionIndex) {
			IntArray metaIndices = new IntArray();
			for (int meta = 0; meta < mMetaboliteNames.length; meta++) {
				if (mStoich.getSignumAt(meta, reactionIndex) != 0) {
					metaIndices.add(meta);
				}
			}
			mReactionIndex	= reactionIndex;
			mMetaIndices	= metaIndices.toArray();
		}
		@Override
		public ReactionConstraints getConstraints() {
			return mReversible[mReactionIndex] ?
				DefaultReactionConstraints.DEFAULT_REVERSIBLE :
				DefaultReactionConstraints.DEFAULT_IRREVERSIBLE;
		}
		@Override
		public ArrayIterable<? extends MetaboliteRatio> getMetaboliteRatios() {
			GenericArray<BigIntegerMetaboliteRatio> ratios = new GenericFixSizeArray<BigIntegerMetaboliteRatio>(mMetaIndices.length);
			for (int i = 0; i < mMetaIndices.length; i++) {
				final int meta = mMetaIndices[i];
				ratios.set(i, new BigIntegerMetaboliteRatio(mReactionIndex, meta));
			}
			return ratios;
		}
		@Override
		public String getName() {
			return mReactionNames[mReactionIndex];
		}				
	}
	public abstract static class AbstractBigIntegerMetaboliteRatio implements MetaboliteRatio {
		protected final int mReacIndex;
		protected final int mMetaIndex;
		public AbstractBigIntegerMetaboliteRatio(int reacIndex, int metaIndex) {
			mReacIndex = reacIndex;
			mMetaIndex = metaIndex;
		}
		abstract protected ReadableBigIntegerRationalMatrix getStoich();
		public void accept(MetabolicNetworkVisitor visitor) {
			visitor.visitMetaboliteRatio(this);
		}
		public double getRatio() {
			return getStoich().getDoubleValueAt(mMetaIndex, mReacIndex);
		}
		public BigFraction getNumberRatio() {
			return getStoich().getBigFractionValueAt(mMetaIndex, mReacIndex);
		}
		public boolean isEduct() {
			return getStoich().getSignumAt(mMetaIndex, mReacIndex) < 0;
		}
		public boolean isIntegerRatio() {
			return getNumberRatio().reduce().getDenominator().compareTo(BigInteger.ONE) == 0;
		}
		@Override
		public String toString() {
			return toString(getNumberRatio(), getMetabolite());
		}
		public String toStringAbs() {
			return toString(getNumberRatio().abs(), getMetabolite());
		}
		private String toString(BigFraction ratio, Metabolite meta) {
			return ratio.isOne() ? meta.toString() : ratio + " " + meta;
		}
		
		public MetaboliteRatio invert() {
			return new MetaboliteRatio() {
				final BigFraction ratio = AbstractBigIntegerMetaboliteRatio.this.getNumberRatio().negate();
				public Metabolite getMetabolite() {
					return AbstractBigIntegerMetaboliteRatio.this.getMetabolite();
				}
				public Number getNumberRatio() {
					return ratio;
				}
				public double getRatio() {
					return ratio.doubleValue();
				}
				public MetaboliteRatio invert() {
					return AbstractBigIntegerMetaboliteRatio.this;
				}
				public boolean isEduct() {
					return !AbstractBigIntegerMetaboliteRatio.this.isEduct();
				}
				public boolean isIntegerRatio() {
					return AbstractBigIntegerMetaboliteRatio.this.isIntegerRatio();
				}
				public String toStringAbs() {
					return AbstractBigIntegerMetaboliteRatio.this.toStringAbs();
				}
				public void accept(MetabolicNetworkVisitor visitor) {
					visitor.visitMetaboliteRatio(this);
				}
			};
		}
		
		@Override
		public int hashCode() {
			return getMetabolite().hashCode() ^ getNumberRatio().hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null) return false;
			if (obj.getClass() == getClass()) {
				AbstractBigIntegerMetaboliteRatio ratio = (AbstractBigIntegerMetaboliteRatio)obj;
				return 
					getMetabolite().equals(ratio.getMetabolite()) &&
					getNumberRatio().equalsNumerically(ratio.getNumberRatio());
			}
			return false;
		}
	}
	private class BigIntegerMetaboliteRatio extends AbstractBigIntegerMetaboliteRatio {
		public BigIntegerMetaboliteRatio(int reacIndex, int metaIndex) {
			super(reacIndex, metaIndex);
		}
		public Metabolite getMetabolite() {
			return FractionNumberStoichMetabolicNetwork.this.getMetabolites().get(mMetaIndex);
		}
		@Override
		protected ReadableBigIntegerRationalMatrix getStoich() {
			return FractionNumberStoichMetabolicNetwork.this.mStoich;
		}
	}
	
	/**
	 * Returns the stoichiometric matrix for any given metabolic network as a
	 * big integer rational matrix. If the given network is an instance of
	 * {@link FractionNumberStoichMetabolicNetwork}, the method 
	 * {@link #getStoichiometricMatrix()} is called. Otherwise, a (possible
	 * approximated) new matrix is created.
	 */
	public static ReadableBigIntegerRationalMatrix getStoich(MetabolicNetwork net) {
		if (net instanceof FractionNumberStoichMetabolicNetwork) {
			return ((FractionNumberStoichMetabolicNetwork)net).getStoichiometricMatrix();
		}
		return new DefaultBigIntegerRationalMatrix(
			StoichiometricMatrices.createStoichiometricMatrix(net), 
			true /*rowIsFirstDim*/, true /*adjustValues*/
		);
	}
	
}
