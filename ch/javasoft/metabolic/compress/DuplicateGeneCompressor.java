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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.impl.AbstractReaction;
import ch.javasoft.metabolic.impl.DefaultMetabolite;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.ints.BitSetIntSet;
import ch.javasoft.util.ints.DefaultIntList;
import ch.javasoft.util.ints.IntIterable;
import ch.javasoft.util.ints.IntIterator;
import ch.javasoft.util.logging.Loggers;

/**
 * <tt>DuplicateGeneCompressor</tt> compresses a given metabolic network by 
 * removing duplicate gene reactions, that is, reactions with equal 
 * stoichiometry. 
 * <p>
 * The {@link #compress(ReadableBigIntegerRationalMatrix, boolean[], String[], String[], boolean) compress}
 * method uses mainly the stoichiometric matrix and reaction reversibilities as
 * input, and returns 2 matrices <tt>dupelim</tt> and <tt>dupfree</tt>, 
 * such that <tt>stoich * dupelim == dupfree</tt>.
 */
public class DuplicateGeneCompressor {
	
	private static final Logger LOG = LogPkg.LOGGER;
	
	/* no instances */
	private DuplicateGeneCompressor() {
		super();
	}

	/**
	 * Returns the compression record, containing 2 matrices 
	 * <tt>dupelim, dupfree</tt> such that
	 * <tt>stoich * dupelim == dupfree</tt>.
	 *  
	 * @param stoich		the original stoichiometric matrix
	 * @param reversible	the reversibilities of the original reactions
	 * @param metaNames		the metabolite names
	 * @param reacNames		the reaction names
	 * @param extended		if false, all duplicate gene reactions must have the 
	 * 						same directionality
	 */
	public static CompressionRecord compress(ReadableBigIntegerRationalMatrix stoich, boolean[] reversible, String[] metaNames, String[] reacNames, boolean extended) {
		final WorkRecord workRecord = new WorkRecord(stoich, reversible, metaNames, reacNames);
		
		//start compression
		int itCount = workRecord.stats.incCompressionIteration();
		LOG.fine("compression iteration " + (itCount + 1) + " (duplicate genes)");
		compressDuplicateGenes(workRecord, extended);
		
		//log compression statistics
		workRecord.stats.writeToLog();
		//return matrices
		return workRecord.getTruncated();
	}
	
	private static boolean compressDuplicateGenes(WorkRecord workRecord, boolean extended) {
		
		//some aliasing
		final BigIntegerRationalMatrix stoich	= workRecord.dupfree;
		final boolean[] reversible				= workRecord.reversible;
		final Size size 						= workRecord.size;

		//start
		final int metas	= size.metas;
		final int reacs	= size.reacs;
		
		List<IntArray> groups = new ArrayList<IntArray>();
		BigFraction[] ratios = new BigFraction[reacs];//ration by reaction index, null if uncoupled and for master reaction, reacA/reacB otherwise		
		for (int reacA = 0; reacA < reacs; reacA++) {
			IntArray group = null;
			for (int reacB = reacA + 1; reacB < reacs; reacB++) {
				BigFraction ratio = null; // reacA / reacB (zero if not coupled, null if all zeros)
				for (int meta = 0; meta < metas; meta++) {
					boolean isZeroA = isZero(stoich.getBigIntegerNumeratorAt(meta, reacA));
					boolean isZeroB = isZero(stoich.getBigIntegerNumeratorAt(meta, reacB));
					if (isZeroA != isZeroB) {
						ratio = BigFraction.ZERO;
						break;
					}
					else if (!isZeroA) {
						BigFraction valA = stoich.getBigFractionValueAt(meta, reacA);
						BigFraction valB = stoich.getBigFractionValueAt(meta, reacB);
						BigFraction curRatio = valA.divide(valB).reduce();
						if (ratio == null) {
							ratio = curRatio;
						}
						else if (ratio.compareTo(curRatio) != 0) {
							ratio = BigFraction.ZERO;
							break;							
						}
					}
				}
				if (ratio == null) {
					LOG.warning("zero stoichiometries found: " + workRecord.getReactionNames(reacA, reacB));
					workRecord.logReactionDetails(Level.WARNING, "  ", reacA);
					workRecord.logReactionDetails(Level.WARNING, "  ", reacB);
					throw new RuntimeException("no zero stoichiometries expected");					
				}
				else if (!isZero(ratio.getNumerator())) {
					//found coupled reactions
					ratios[reacB] = ratio;
					if (group == null) {
						group = new IntArray();
						group.add(reacA);
					}
					group.add(reacB);
				}						
			}
			if (group != null) {
				groups.add(group);
			}
		}
		BitSet toRemove = new BitSet();
		for (int iGrp = 0; iGrp < groups.size(); iGrp++) {
			final IntArray grp = groups.get(iGrp);
			//find out about the reversibility of our duplicate gene reactions
			boolean	keptRev = reversible[grp.first()];//is the kept reaction goint to be reversible
			Boolean sameRev = Boolean.valueOf(keptRev);//true if all reversible, false if all irreversible, same direction, null otherwise
			boolean scaled	= false;//true if any ratio is unequal to 1
			for (int i = 1; i < grp.length(); i++) {
				int reac = grp.get(i);
				keptRev |= reversible[reac] || (ratios[reac].signum() < 0);				
				scaled	|= !ratios[reac].isOne();
				if (!
					(sameRev.booleanValue() && reversible[i]) ||
					(!sameRev.booleanValue() && !reversible[i] && ratios[reac].signum() > 0)
				) {
					sameRev = null;
					break;
				}
			}
			
			if (sameRev != null || extended) {
				//remove the non-kept duplicate gene reactions now 
				//(put to end of matrix)
				if (scaled) {
					LOG.info("found and removed duplicate gene reactions (some ratios unequal to one): " + workRecord.getReactionNames(grp));
				}
				else if (logFine()) {
					LOG.fine("found and removed duplicate gene reactions: " + workRecord.getReactionNames(grp));
				}
				final int keptReac = grp.first();
				if (logFiner()) {
					String prefix = "   [+] r=1: ";
					workRecord.logReactionDetails(Level.FINER, prefix, keptReac);
				}
				for (int i = 1; i < grp.length(); i++) {
					int reac = grp.get(i);
					if (logFiner()) {
						String prefix = "   [-] r=" + ratios[reac] + ": ";
						workRecord.logReactionDetails(Level.FINER, prefix, reac);
					}
					toRemove.set(reac);
				}
				reversible[keptReac] = keptRev;			
				workRecord.groups.add(grp);
				workRecord.stats.incDuplicateGeneReactions(grp.length());
			}
			else {
				LOG.finer("ignoring weak duplicate gene reactions (not all have same directionality): " + workRecord.getReactionNames(grp));
			}
		}
		//now, really remove the reactions
		workRecord.removeReactions(toRemove);
		return !toRemove.isEmpty();
	}

	/*==========================================================================
	 * Private classes 
	 */
	
	/**
	 * Size, a mutable counter for metabolite/reaction size 
	 */
	private static class Size implements Cloneable {
		Size(int iMetas, int iReacs) {
			metas = iMetas;
			reacs = iReacs;
		}
		int metas;
		int reacs;
		@Override
		public String toString() {
			return "[metas=" + metas + ", reacs=" + reacs + "]";
		}
		@Override
		public Size clone() {
			return new Size(metas, reacs);
		}
		@Override
		public boolean equals(Object other) {
			if (other == this) return true;
			if (other == null) return false;
			if (other.getClass() == Size.class) {
				Size so = (Size)other;
				return metas == so.metas && reacs == so.reacs;
			}
			return false;
		}
	}	
	/*........................................................................*/
	/**
	 * CompressionRecord, containing all compression matrices
	 * <p> 
	 * <tt>stoich * dupelim == dupfree</tt>.
	 */
	public static class CompressionRecord {
		public final BigIntegerRationalMatrix 	dupelim, dupfree; 
		public final IntArray[]					dupgroups;//no single element groups contained, indices sorted ascending
		public final boolean[] 					reversible;
		public final String[] 					metaNames, reacNames;
		public CompressionRecord(BigIntegerRationalMatrix stoich, BigIntegerRationalMatrix dupelim, IntArray[] dupgroups, boolean[] reversible, String[] metaNames, String[] reacNames) {
			this.dupfree	= stoich;
			this.dupelim	= dupelim;
			this.dupgroups	= dupgroups;
			this.reversible	= reversible;
			this.metaNames	= metaNames;
			this.reacNames	= reacNames;
		}
	}
	/**
	 * WorkRecord, mutable record containing all data for normal compression
	 */
	private static class WorkRecord extends CompressionRecord {
		final CompressionStatistics		stats;
		final Size 						size;
		final List<IntArray>			groups;//no single element groups contained, indices sorted ascending
		/** Constructor for initial work record*/
		WorkRecord(ReadableBigIntegerRationalMatrix rdStoich, boolean[] reversible, String[] metaNames, String[] reacNames) {
			super(cancel(rdStoich), identity(rdStoich.getColumnCount()), null, reversible.clone(), metaNames.clone(), reacNames.clone());
			this.stats 	= new CompressionStatistics();
			//we have pre*rdStoich*post = stoich
			this.size 	= new Size(dupfree.getRowCount(), dupfree.getColumnCount());
			this.groups	= new ArrayList<IntArray>();
		}
		public CompressionRecord getTruncated() {
			final BigIntegerRationalMatrix cmpTrunc = createSubStoich(dupfree, dupelim, reversible, size);
//			final int m		= dupfree.getRowCount();
			final int r		= dupfree.getColumnCount();
			final int mc	= cmpTrunc.getRowCount();
			final int rc	= cmpTrunc.getColumnCount();
			final boolean[] revTrunc = new boolean[rc];
			final String[] metaTrunc = new String[mc];
			final String[] reacTrunc = new String[rc];
			System.arraycopy(reversible, 0, revTrunc, 0, rc);
			System.arraycopy(metaNames, 0, metaTrunc, 0, mc);
			System.arraycopy(reacNames, 0, reacTrunc, 0, rc);
			return new CompressionRecord(
				cmpTrunc,
				dupelim.subBigIntegerRationalMatrix(0, r, 0, rc),
				groups.toArray(new IntArray[groups.size()]),
				revTrunc, metaTrunc, reacTrunc
			);
		}
		/**
		 * Removes the specified reaction. The concerned reaction is put to the end
		 * of stoich/post/reversible (objects are modified) and decrements 
		 * size.reacts.
		 */
		public void removeReaction(int reac) {
			for (int meta = 0; meta < size.metas; meta++) {
                                // cj: b
				// dupfree.setValueAt(meta, reac, BigFraction.ZERO);
				dupfree.setValueAt_BigFraction(meta, reac, BigFraction.ZERO);
                                // cj: e
			}
			size.reacs--;
			if (reac != size.reacs) {
				dupelim.swapColumns(reac, size.reacs);//the last column should contain 0's now, but we keep them, since size.reacts gives us this info, too
				dupfree.swapColumns(reac, size.reacs);			
				ch.javasoft.util.Arrays.swap(reversible, reac, size.reacs);
				ch.javasoft.util.Arrays.swap(reacNames, reac, size.reacs);
			}
		}
		
		/**
		 * Removes the specified reactions. The concerned reactions are put to the end
		 * of stoich/post/reversible (objects are modified) and decrements 
		 * size.reacts.
		 */
		public void removeReactions(BitSet reactionsToRemove) {
			BitSet toRemove = (BitSet)reactionsToRemove.clone();
			//now, really remove the reactions
			for (int reac = toRemove.nextSetBit(0);reac >= 0; ) {
				removeReaction( reac);
				//reac has been swapped with size.reacs, so check that one now
				if (reac != size.reacs && toRemove.get(size.reacs)) {
					toRemove.clear(size.reacs);
					//continue with reac since size.reacs was also to remove, 
					//being at position reac now 
				}
				else {
					toRemove.clear(reac);
					//continue with next reaction to remove
					reac = toRemove.nextSetBit(reac + 1);
				}
			}
		}
		
		/** Returns reaction names, separated by slash / */
		public String getReactionNames(IntArray reacs) {
			return getReactionNames(new DefaultIntList(reacs));
		}
		/** Returns reaction names, separated by slash / */
		public String getReactionNames(int... reacs) {
			BitSet bs = new BitSet();
			for (int i = 0; i < reacs.length; i++) {
				bs.set(reacs[i]);
			}
			return getReactionNames(bs);
		}
		/** Returns reaction names, separated by slash / */
		public String getReactionNames(BitSet reacs) {
			return getReactionNames(new BitSetIntSet(reacs));
		}
		/** Returns reaction names, separated by slash / */
		public String getReactionNames(IntIterable reacs) {
			StringBuilder sb = new StringBuilder();
			IntIterator it = reacs.iterator();
			while (it.hasNext()) {
				if (sb.length() > 0) sb.append(" / ");
				sb.append(reacNames[it.nextInt()]);
			}
			return sb.toString();
		}
		/** Returns string representation of the given reaction */
		public String getReactionDetails(int reac) {
			List<MetaboliteRatio> ratios = new ArrayList<MetaboliteRatio>();
			for (int meta = 0; meta < size.metas; meta++) {
				BigFraction num = dupfree.getBigFractionValueAt(meta, reac);
				if (num.signum() != 0) {
					final String metaName = metaNames[meta];
					ratios.add(new FractionNumberStoichMetabolicNetwork.AbstractBigIntegerMetaboliteRatio(reac, meta) {
						public Metabolite getMetabolite() {							
							return new DefaultMetabolite(metaName);
						}
						@Override
						protected ReadableBigIntegerRationalMatrix getStoich() {
							return dupfree;
						}
					});
				}
			}
			return AbstractReaction.toString(ratios, reversible[reac]);
		}
		@SuppressWarnings("unused")
		public void logReactionDetails(Level logLevel, String prefix, BitSet reacs) {
			if (Loggers.isLoggable(LOG, logLevel)) {
				logReactionDetails(logLevel, prefix, new BitSetIntSet(reacs));
			}
		}
		public void logReactionDetails(Level logLevel, String prefix, IntIterable reacs) {
			if (Loggers.isLoggable(LOG, logLevel)) {
				IntIterator it = reacs.iterator();
				while (it.hasNext()) {
					int reac = it.nextInt();
					logReactionDetails(logLevel, prefix, reac);
				}
			}
		}
		public void logReactionDetails(Level logLevel, String prefix, int reac) {
			if (Loggers.isLoggable(LOG, logLevel)) {
				LOG.log(logLevel, prefix + reacNames[reac] + " := " + getReactionDetails(reac));
			}			
		}
	}
	/*==========================================================================
	 * Static helper methods
	 */
	
	private static BigIntegerRationalMatrix cancel(ReadableBigIntegerRationalMatrix matrix) {
		BigIntegerRationalMatrix mx = matrix.toBigIntegerRationalMatrix(true /*new instance*/);
		mx.reduce();
		return mx;
	}
	
	private static BigIntegerRationalMatrix identity(int size) {
		BigIntegerRationalMatrix id = new DefaultBigIntegerRationalMatrix(size, size);
		for (int piv = 0; piv < size; piv++) {
                        // cj: b
			// id.setValueAt(piv, piv, BigFraction.ONE);
			id.setValueAt_BigFraction(piv, piv, BigFraction.ONE);
                        // cj: e
		}
		return id;
	}
	
	private static BigIntegerRationalMatrix createSubStoich(ReadableBigIntegerRationalMatrix stoich, ReadableBigIntegerRationalMatrix post, boolean[] reversible, Size size) {
		return stoich.subBigIntegerRationalMatrix(0, size.metas, 0, size.reacs);
	}

	private static boolean isZero(BigInteger val) {
		return val.signum() == 0;
	}
	private static boolean logFine() {
		return Loggers.isLoggable(LOG, Level.FINE);
	}
	private static boolean logFiner() {
		return Loggers.isLoggable(LOG, Level.FINER);
	}
}
