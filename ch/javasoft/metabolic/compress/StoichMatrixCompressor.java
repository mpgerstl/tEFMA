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
import java.util.Set;
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
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.ints.BitSetIntSet;
import ch.javasoft.util.ints.DefaultIntList;
import ch.javasoft.util.ints.IntIterable;
import ch.javasoft.util.ints.IntIterator;
import ch.javasoft.util.logging.LogPrintWriter;
import ch.javasoft.util.logging.Loggers;

/**
 * <tt>StoichMatrixCompressor</tt> compresses a given metabolic network. 
 * Compression methods are specified at instantiation time. 
 * <p>
 * The {@link #compress(ReadableBigIntegerRationalMatrix, boolean[], String[], String[], Set)}
 * method uses mainly the stoichiometric matrix and reaction reversibilities as
 * input, and returns 3 matrices <tt>pre</tt>, <tt>post</tt> and <tt>cmp</tt>, 
 * such that <tt>pre * stoich * post == cmp</tt>. Fba and flux analysis methods
 * can be performed on the compressed matrix. Resulting flux vectors such as
 * elementary modes can easily be mapped back to the original network. 
 */
public class StoichMatrixCompressor {
	
	private static final Logger LOG = LogPkg.LOGGER;
	
	private final CompressionMethod[] mCompressionMethods;

	/**
	 * Constructor using default compression methods 
	 * {@link CompressionMethod#STANDARD}
	 */
	public StoichMatrixCompressor() {
		this(CompressionMethod.STANDARD);
	}

	/**
	 * Constructor using the given compression methods 
	 */
	public StoichMatrixCompressor(CompressionMethod ... compressionMethods) {
		mCompressionMethods = compressionMethods;
		CompressionMethod.logUnsupported(Level.WARNING, compressionMethods, 
			CompressionMethod.CoupledZero, CompressionMethod.CoupledCombine, 
			CompressionMethod.CoupledContradicting, 
			CompressionMethod.UniqueFlows, CompressionMethod.DeadEnd,
			CompressionMethod.Recursive);
	}	
	
	/**
	 * Returns the compression record, containing 3 matrices 
	 * <tt>pre, cmp, post</tt> such that
	 * <tt>pre * stoich * post == cmp</tt>.
	 * <p>
	 * Remarks:
	 * <pre>
	 *   stoich<sup>m*r  </sup>         : original stoichiometric matrix,
	 *         <sup>     </sup>           m = #metabolites, r = #reactions
	 *   cmp<sup>mc*rc</sup>            : compressed stoichiometrix matrix
	 *      <sup>     </sup>              mc = #metabolites, rc = #reactions
	 *   pre<sup>mc*m </sup>            : metabolite mapping
	 *   post<sup>r*rc </sup>           : reaction mapping
	 *
	 *   efmc<sup>rc*1 </sup>           : a compressed elementary mode
	 *   efm<sup>r*1  </sup>            : an uncompressed elementary mode
	 *   
	 *   efm uncompression:
	 *       pre * stoich * post * efmc = 0
	 *             stoich * efm         = 0
	 *                              efm = post * efmc
	 * </pre>
	 *  
	 * @param stoich				the original stoichiometric matrix
	 * @param reversible			the reversibilities of the original reactions
	 * @param metaNames				the names of the metabolites
	 * @param reacNames				the names of the reactions
	 * @param suppressedReactions	reactions to suppress, i.e. they are removed
	 * 								in the compressed network (null is allowed)
	 */
	public CompressionRecord compress(ReadableBigIntegerRationalMatrix stoich, boolean[] reversible, String[] metaNames, String[] reacNames, Set<String> suppressedReactions) {
		final WorkRecord workRecord = new WorkRecord(stoich, reversible, metaNames, reacNames);
		
		//start compression
		boolean compressedAny;
		final boolean doRec = CompressionMethod.Recursive.containedIn(mCompressionMethods);
		final boolean doZer = CompressionMethod.CoupledZero.containedIn(mCompressionMethods);
		final boolean doCon = CompressionMethod.CoupledContradicting.containedIn(mCompressionMethods);
		final boolean doCom = CompressionMethod.CoupledCombine.containedIn(mCompressionMethods);
		final boolean doUnq = CompressionMethod.UniqueFlows.containedIn(mCompressionMethods);
		final boolean doDea = CompressionMethod.DeadEnd.containedIn(mCompressionMethods);
		final boolean doUnqInc = doDea;
		final boolean doUnqCom = doUnq;
		final boolean doNulInc = doZer || doCon;
		final boolean doNulCom = doCom;
		
		//first, only dead-ends and inconsistencies are considered
		
		compressedAny = workRecord.removeReactions(suppressedReactions);
		do {
			int itCount = workRecord.stats.incCompressionIteration();
			LOG.fine("compression iteration " + (itCount + 1) + " (dead-ends/inconsistencies)");
			compressedAny  = doUnqInc && workRecord.removeUnusedMetabolites();
			compressedAny |= doUnqInc && unique(workRecord, false);	//non-conditional or (right side always called)
			compressedAny |= doNulInc && workRecord.removeUnusedMetabolites();
			compressedAny |= doNulInc && nullspace(workRecord, false);//non-conditional or (right side always called)
		}
		while (compressedAny && doRec);

		//now, full compression
		if (doUnqCom) {
			do {
				int itCount = workRecord.stats.incCompressionIteration();
				LOG.fine("compression iteration " + (itCount + 1) + " (unique fluxes)");
				compressedAny  = workRecord.removeUnusedMetabolites();
				compressedAny |= unique(workRecord, true);	//non-conditional or (right side always called)
			}
			while (compressedAny && doRec);
			if (compressedAny && !doRec) {
				workRecord.removeUnusedMetabolites();
			}
		}
		if (doNulCom) {
			do {
				int itCount = workRecord.stats.incCompressionIteration();
				LOG.fine("compression iteration " + (itCount + 1) + " (nullspace)");
				compressedAny  = workRecord.removeUnusedMetabolites();
				compressedAny |= nullspace(workRecord, true);//non-conditional or (right side always called)
			}
			while (compressedAny && doRec);
			if (compressedAny && !doRec) {
				workRecord.removeUnusedMetabolites();
			}
		}
		
		//full compression, combined
		if (doRec && doUnqCom && doNulCom) {
			do {
				int itCount = workRecord.stats.incCompressionIteration();
				LOG.fine("compression iteration " + (itCount + 1) + " (unique/nullspace)");
				compressedAny  = workRecord.removeUnusedMetabolites();
				compressedAny |= unique(workRecord, true);	//non-conditional or (right side always called)
				compressedAny |= workRecord.removeUnusedMetabolites();
				compressedAny |= nullspace(workRecord, true);//non-conditional or (right side always called)
			}
			while (compressedAny && doRec);
		}

		//log compression statistics
		workRecord.stats.writeToLog();
		//return matrices
		return workRecord.getTruncated();
	}
	
	private boolean nullspace(WorkRecord workRecord, boolean inclCompression) {
		//compression options
		final boolean doZer = CompressionMethod.CoupledZero.containedIn(mCompressionMethods);
		final boolean doCon = CompressionMethod.CoupledContradicting.containedIn(mCompressionMethods);
		final boolean doCom = CompressionMethod.CoupledCombine.containedIn(mCompressionMethods);
		final boolean doCpl = doCon || doCom;

		//start
		NullspaceRecord nullspaceRecord = new NullspaceRecord(workRecord);		
		boolean compressedAny = false;
		if (doZer) compressedAny |= nullspaceZeroFluxReactions(nullspaceRecord);
		if (doCpl) compressedAny |= nullspaceCoupledReactions(nullspaceRecord, inclCompression);
		if (compressedAny) workRecord.removeUnusedMetabolites();
		return compressedAny;
	}
	private boolean nullspaceZeroFluxReactions(NullspaceRecord nullspaceRecord) {		
		//some aliasing
		final Size size 						= nullspaceRecord.size;
		final BigIntegerRationalMatrix kernel	= nullspaceRecord.kernel;

		boolean anyZeroFlux = false;
		final int cols = kernel.getColumnCount();
		int reac = 0;		
		while (reac < size.reacs) {
			boolean allZero = true;
			for (int col = 0; col < cols; col++) {
				if (!isZero(kernel.getBigIntegerNumeratorAt(reac, col))) {
					allZero = false;
					break;
				}
			}
			if (allZero) {
				LOG.fine("found and removed zero flux reaction: " + nullspaceRecord.reacNames[reac]);
				if (logFiner()) LOG.finer("    [-] " + nullspaceRecord.getReactionDetails(reac));
				anyZeroFlux = true;
				nullspaceRecord.removeReaction(reac);
				nullspaceRecord.stats.incZeroFluxReactions();
			}
			else {
				reac++;
			}
		}
		return anyZeroFlux;
	}
	private boolean nullspaceCoupledReactions(NullspaceRecord nullspaceRecord, boolean inclCompression) {
		//compression options
		final boolean doCon = CompressionMethod.CoupledContradicting.containedIn(mCompressionMethods);
		final boolean doCom = CompressionMethod.CoupledCombine.containedIn(mCompressionMethods);
		
		//some aliasing
		final BigIntegerRationalMatrix kernel	= nullspaceRecord.kernel;
		final BigIntegerRationalMatrix stoich	= nullspaceRecord.cmp;
		final BigIntegerRationalMatrix post		= nullspaceRecord.post;
		final boolean[] reversible				= nullspaceRecord.reversible;
		final Size size 						= nullspaceRecord.size;

		//start
		final int cols	= kernel.getColumnCount();
		final int reacs	= size.reacs;
		List<IntArray> groups 	= new ArrayList<IntArray>();//no single element groups contained, indices sorted ascending
		BigFraction[] ratios = new BigFraction[reacs];//ration by reaction index, null if uncoupled and for master reaction, reacA/reacB otherwise		
		for (int reacA = 0; reacA < reacs; reacA++) {
			if (ratios[reacA] == null) {
				IntArray group = null;
				for (int reacB = reacA + 1; reacB < reacs; reacB++) {
					BigFraction ratio = null; // reacA / reacB (zero if not coupled, null if all zeros)
					for (int col = 0; col < cols; col++) {
						boolean isZeroA = isZero(kernel.getBigIntegerNumeratorAt(reacA, col));
						boolean isZeroB = isZero(kernel.getBigIntegerNumeratorAt(reacB, col));
						if (isZeroA != isZeroB) {
							ratio = BigFraction.ZERO;
							break;
						}
						else if (!isZeroA) {
							BigFraction valA = kernel.getBigFractionValueAt(reacA, col);
							BigFraction valB = kernel.getBigFractionValueAt(reacB, col);
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
						throw new RuntimeException("no zero rows expected here");					
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
		}
		BitSet toRemove = new BitSet();
		for (IntArray grp : groups) {
			//first, check consistency of couplings, remove if inconsistent with
			//reversibilities
			boolean allOk;
			boolean forward = false;//swapped in next statement
			do {
				forward = !forward;
				allOk	= forward || reversible[grp.first()];
				for (int i = 1; i < grp.length() && allOk; i++) {
					int reac = grp.get(i);
					allOk &= (forward == ratios[reac].signum() > 0) || reversible[reac];
				}
			}
			while (forward && !allOk);
			if (!allOk) {
				if (doCon) {
					if (logFine()) {
						LOG.fine("found and removed inconsistently coupled reactions: " + nullspaceRecord.getReactionNames(grp));
					}
					for (int i = 0; i < grp.length(); i++) {
						int reac = grp.get(i);
						//inconsistent, remove all
						if (logFiner()) {
							String prefix = "   [-] r=" + (i == 0 ? BigFraction.ONE : ratios[reac]) + ": ";
							nullspaceRecord.logReactionDetails(Level.FINER, prefix, reac);
						}
						toRemove.set(reac);
						nullspaceRecord.stats.incContradictingReactions();
					}
				}
				else {
					LOG.finer("ignoring inconsistently coupled reactions due to compression settings");
				}
			}
			else {
				if (doCom && inclCompression) {
					//combine coupled reactions into one
					//the new reaction is placed at the position of the first
					//reaction, the others are removed (put to end of matrix)
					if (logFine()) {
						LOG.fine("found and combined coupled reactions: " + nullspaceRecord.getReactionNames(grp));
					}
					int masterReac = grp.first();
					if (logFiner()) {
						String prefix = "   [+] r=" + (forward ? 1 : -1) + ": ";
						nullspaceRecord.logReactionDetails(Level.FINER, prefix, masterReac);
					}
					if (!forward) {
						//negate the column
						negateColumn(stoich, masterReac);
						negateColumn(post, masterReac);
					}
					for (int i = 1; i < grp.length(); i++) {
						int reac = grp.get(i);
						BigFraction ratio = forward ? ratios[reac] : ratios[reac].negate();
						if (logFiner()) {
							String prefix = "   [-] r=" + ratios[reac] + ": ";
							nullspaceRecord.logReactionDetails(Level.FINER, prefix, reac);
						}
						addColumnMultipleTo(stoich, masterReac, reac, ratio);
						addColumnMultipleTo(post, masterReac, reac, ratio);
						reversible[masterReac] &= reversible[reac];
						toRemove.set(reac);
						nullspaceRecord.stats.incCoupledReactions();
					}
					if (logFiner()) LOG.finer("   [>] " + nullspaceRecord.getReactionDetails(masterReac));
					//check whether the newly created has all zeros
					//this can happen e.g. by merging     R1: #--> A / R2: A --> #
					//we don't handle this currently, so throw an excpetion for that case
					boolean allZero = false;
					for (int meta = 0; meta < size.metas && allZero; meta++) {
						allZero &= isZero(stoich.getBigIntegerNumeratorAt(meta, masterReac));
					}
					if (allZero) {
						throw new RuntimeException("all entries found 0 for a reaction after merging: " + masterReac);
					}
				}
				else {
					if (inclCompression && !doCom) {
						LOG.finer("ignoring coupled reactions due to compression settings");
					}
				}
			}
		}
		//now, really remove the reactions
		nullspaceRecord.removeReactions(toRemove);
		return !toRemove.isEmpty();
	}

	private boolean unique(WorkRecord workRecord, boolean inclCompression) {
		final Size size = workRecord.size;
		final Size orig = size.clone();
		int meta = 0;
		while (meta < size.metas) {
			int metas = size.metas;
			uniqueMeta(workRecord, meta, inclCompression);
			if (metas == size.metas) meta++;
			//else use same meta, since it represents another metabolite now
		}
		return !orig.equals(size);
	}
	private boolean uniqueMeta(WorkRecord workRecord, int meta, boolean inclCompression) {
		//some aliasing
		final BigIntegerRationalMatrix stoich	= workRecord.cmp;
		final BigIntegerRationalMatrix post		= workRecord.post;
		final boolean[] reversible				= workRecord.reversible;
		final Size size 						= workRecord.size;
		
		final BitSet eduReacs	= new BitSet();
		final BitSet proReacs	= new BitSet();
		final BitSet revReacs	= new BitSet();
		for (int reac = 0; reac < size.reacs; reac++) {
			int sgn = stoich.getSignumAt(meta, reac);
			if (sgn != 0) {
				if (reversible[reac]) {
					revReacs.set(reac);
				}
				else {
					if (sgn < 0) eduReacs.set(reac);
					else if (sgn > 0) proReacs.set(reac);
				}
			}
		}
		final int eduCnt = eduReacs.cardinality();
		final int proCnt = proReacs.cardinality();
		final int revCnt = revReacs.cardinality();
		
		if (/*UNUSED METABOLITE*/(eduCnt == 0 && proCnt == 0 && revCnt == 0)) {
			//such metabolites can be removed, they disappear because of
			//previous compression steps
			if (logFine()) {
				LOG.fine("found and removed unused metabolite: " + 
					workRecord.metaNames[meta]);
			}
			workRecord.removeMetabolite(meta, true /*setStoichToZero*/);
			workRecord.stats.incUnusedMetabolite();
			return true;
		}
		else if (/*DEAD END*/
			(eduCnt == 0 && proCnt == 0 && revCnt == 1) ||
			(eduCnt == 0 && revCnt == 0) ||
			(proCnt == 0 && revCnt == 0)
		) {	
			BitSet allReacs = (BitSet)eduReacs.clone();
			allReacs.or(proReacs);
			allReacs.or(revReacs);
			if (logFine()) {
				LOG.fine("found and removed dead-end metabolite/reaction(s): " + 
					workRecord.metaNames[meta] + " / " + workRecord.getReactionNames(allReacs));
				workRecord.logReactionDetails(Level.FINER, "   ", allReacs);
			}
			workRecord.removeMetabolite(meta, true /*setStoichToZero*/);
			workRecord.removeReactions(allReacs);
			workRecord.stats.incDeadEndMetaboliteReactions(allReacs.cardinality());
			return true;
		}
		else {
			if (inclCompression) {
				int reacToRemove	= -1;
				BitSet reacsToMerge = null;
				if /*NO REVERSIBLE INVOLVED*/ (revCnt == 0 && (eduCnt == 1 || proCnt == 1 /* 0 is handled in dead-end scope*/)) {
					//either uniquely consumed or produced to be compressable
					reacToRemove = eduCnt == 1 ? eduReacs.nextSetBit(0) : proReacs.nextSetBit(0);
					reacsToMerge = eduCnt == 1 ? proReacs : eduReacs;
					LOG.fine("found uniquely " + (eduCnt == 1 ? "consumed" : "produced") + " metabolite: " + workRecord.metaNames[meta]);
				}
				else if /*1 REVERSIBLE INVOLVED*/ (revCnt == 1 && (eduCnt == 0 || proCnt == 0)) {
					//1 reversible reaction, all other reactions of same type (direction)
					reacToRemove = revReacs.nextSetBit(0);
					reacsToMerge = eduCnt == 0 ? proReacs : eduReacs;
					LOG.fine("found uniquely (reversibly) " + (eduCnt == 0 ? "consumed" : "produced") + " metabolite: " + workRecord.metaNames[meta]);
				}
				else if /*ONLY 2 REVERSIBLE*/ (revCnt == 2 && eduCnt == 0 && proCnt == 0) {
					reacToRemove = revReacs.nextSetBit(0);
					reacsToMerge = new BitSet();
					reacsToMerge.set(revReacs.nextSetBit(reacToRemove + 1));
					LOG.fine("found and removed metabolite between 2 reversible reactions: " + workRecord.metaNames[meta] + " / " + workRecord.getReactionNames(revReacs));
				}
				if (reacsToMerge != null) {
					workRecord.logReactionDetails(Level.FINER, "   [-] ", reacToRemove);								
					BigFraction rmStoich	= stoich.getBigFractionValueAt(meta, reacToRemove);
					BigFraction kpMul		= rmStoich.abs();
					
					for (int reac = reacsToMerge.nextSetBit(0); reac >= 0; reac = reacsToMerge.nextSetBit(reac + 1)) {
						workRecord.logReactionDetails(Level.FINER, "   [+] ", reac);					
						BigFraction kpStoich	= stoich.getBigFractionValueAt(meta, reac);
						BigFraction rmMul		= rmStoich.signum() < 0 ? kpStoich : kpStoich.negate();
						addColumnMultipleTo(stoich, reac, kpMul, reacToRemove, rmMul);
						addColumnMultipleTo(post, reac, kpMul, reacToRemove, rmMul);
						
						reversible[reac] &= reversible[reacToRemove];
						workRecord.logReactionDetails(Level.FINER, "   [>] ", reac);
						
						//consistency check: stoich for the metaboilte must be zero now
						if (stoich.getSignumAt(meta, reac) != 0) {
							throw new RuntimeException(
								"internal error: stoichiometry for eliminated metabolite " + 
								workRecord.metaNames[meta] + " expected to be zero, but was " +
								stoich.getBigFractionValueAt(meta, reac)
							);
						}
					}
					workRecord.removeMetabolite(meta, true /*setStoichToZero*/);				
					workRecord.removeReaction(reacToRemove);
					workRecord.stats.incUniqueFlowReactions();
					return true;
				}
			}
			return false;
		}
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
	 * <tt>pre * stoich * post == cmp</tt>.
	 * <p>
	 * Remarks:
	 * <pre>
	 *   stoich<sup>m*r  </sup>         : original stoichiometric matrix,
	 *         <sup>     </sup>           m = #metabolites, r = #reactions
	 *   cmp<sup>mc*rc</sup>            : compressed stoichiometrix matrix
	 *      <sup>     </sup>              mc = #metabolites, rc = #reactions
	 *   pre<sup>mc*m </sup>            : metabolite mapping
	 *   post<sup>r*rc </sup>           : reaction mapping
	 *
	 *   efmc<sup>rc*1 </sup>           : a compressed elementary mode
	 *   efm<sup>r*1  </sup>            : an uncompressed elementary mode
	 *   
	 *   efm uncompression:
	 *       pre * stoich * post * efmc = 0
	 *             stoich * efm         = 0
	 *                              efm = post * efmc
	 * </pre>
	 */
	public static class CompressionRecord {
		public final BigIntegerRationalMatrix 	pre, cmp, post; 
		public final boolean[] 					reversible;
		public CompressionRecord(BigIntegerRationalMatrix pre, BigIntegerRationalMatrix stoich, BigIntegerRationalMatrix post, boolean[] reversible) {
			this.pre		= pre;
			this.cmp		= stoich;
			this.post		= post;
			this.reversible	= reversible;
		}
	}
	/**
	 * WorkRecord, mutable record containing all data for normal compression
	 */
	private static class WorkRecord extends CompressionRecord {
		final CompressionStatistics		stats;
		final String[] 					metaNames, reacNames;
		final Size 						size;
		/** Constructor for initial work record*/
		WorkRecord(ReadableBigIntegerRationalMatrix rdStoich, boolean[] reversible, String[] metaNames, String[] reacNames) {
			super(identity(rdStoich.getRowCount()), cancel(rdStoich), identity(rdStoich.getColumnCount()), reversible.clone());
			this.stats = new CompressionStatistics();
			//we have pre*rdStoich*post = stoich
			this.size = new Size(cmp.getRowCount(), cmp.getColumnCount());
			this.metaNames	= metaNames;
			this.reacNames	= reacNames;
		}
		/** Constructor cloning an existing work record, used by subclasses*/
		WorkRecord(WorkRecord workRecord) {
			super(workRecord.pre, workRecord.cmp, workRecord.post, workRecord.reversible);
			this.stats		= workRecord.stats;
			this.size		= workRecord.size;
			this.metaNames	= workRecord.metaNames;			
			this.reacNames	= workRecord.reacNames;
		}
		public CompressionRecord getTruncated() {
			final BigIntegerRationalMatrix cmpTrunc = createSubStoich(pre, cmp, post, reversible, size);
			final int m		= cmp.getRowCount();
			final int r		= cmp.getColumnCount();
			final int mc	= cmpTrunc.getRowCount();
			final int rc	= cmpTrunc.getColumnCount();
			final boolean[] revTrunc = new boolean[rc];
			System.arraycopy(reversible, 0, revTrunc, 0, rc);
			return new CompressionRecord(
				pre.subBigIntegerRationalMatrix(0, mc, 0, m),
				cmpTrunc,
				post.subBigIntegerRationalMatrix(0, r, 0, rc),
				revTrunc
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
				// cmp.setValueAt(meta, reac, BigFraction.ZERO);
				cmp.setValueAt_BigFraction(meta, reac, BigFraction.ZERO);
                                // cj: b
			}
			size.reacs--;
			if (reac != size.reacs) {
				post.swapColumns(reac, size.reacs);//the last column should contain 0's now, but we keep them, since size.reacts gives us this info, too
				cmp.swapColumns(reac, size.reacs);			
				ch.javasoft.util.Arrays.swap(reversible, reac, size.reacs);
				ch.javasoft.util.Arrays.swap(reacNames, reac, size.reacs);
			}
		}
		
		/**
		 * Removes the specified reactions. The concerned reactions are put to the end
		 * of stoich/post/reversible (objects are modified) and decrements 
		 * size.reacts.
		 */
		public boolean removeReactions(Set<String> suppressedReactions) {
			if (suppressedReactions == null || suppressedReactions.isEmpty()) {
				return false;
			}
			final BitSet indexSet = new BitSet();
			for (final String reac : suppressedReactions) {
				int index = -1;
				for (int i = 0; i < reacNames.length; i++) {
					if (reacNames[i].equals(reac)) {
						index = i;
						break;
					}
				}
				if (index < 0) {
					throw new IllegalArgumentException("no such reaction: " + reac);
				}
				indexSet.set(index);
			}
			removeReactions(indexSet);
			return true;
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
		
		/**
		 * Removes the specified metabolite. The concerned metabolite is put to the 
		 * end of pre/stoich (objects are modified) and decrements size.metas.
		 */
		public void removeMetabolite(int meta, boolean setStoichToZero) {
			if (setStoichToZero) {
				for (int reac = 0; reac < size.reacs; reac++) {
                                        // cj: b
					// cmp.setValueAt(meta, reac, BigFraction.ZERO);
					cmp.setValueAt_BigFraction(meta, reac, BigFraction.ZERO);
                                        // cj: e
				}
			}
			size.metas--;
			if (meta != size.metas) {
				pre.swapRows(meta, size.metas);//should contain 0's now, but we keep them, since size.metas gives us this info, too
				cmp.swapRows(meta, size.metas);
				ch.javasoft.util.Arrays.swap(metaNames, meta, size.metas);
			}
		}
		
		/**
		 * Moves unused metabolites to the end of the matrices. Modifies pre and
		 * stoich and reduces size.metas.
		 * 
		 * @return true if at least one metabolite has been removed
		 */
		public boolean removeUnusedMetabolites() {		
			final int origCnt = size.metas; 
			//find unused metas
			StringBuilder sb = logFine() ? new StringBuilder() : null;
			for (int meta = 0; meta < size.metas; ) {
				boolean any = false;
				for (int reac = 0; reac < size.reacs && !any; reac++) {
					any |= !isZero(cmp.getBigIntegerNumeratorAt(meta, reac));
				}
				if (!any) {
					if (sb != null) {
						if (sb.length() > 0) sb.append(" / ");
						sb.append(metaNames[meta]);
					}
					removeMetabolite( meta, false /*setStoichToZero not needed, already zero*/);
					stats.incUnusedMetabolite();
				}
				else {
					meta++;
				}
			}
			if (sb != null && sb.length() > 0) {
				LOG.fine("found and removed unused metabolites: " + sb);				
			}
			return origCnt != size.metas;
		}		
		/** Returns reaction names, separated by slash / */
		public String getReactionNames(IntArray reacs) {
			return getReactionNames(new DefaultIntList(reacs));
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
				BigFraction num = cmp.getBigFractionValueAt(meta, reac);
				if (num.signum() != 0) {
					final String metaName = metaNames[meta];
					ratios.add(new FractionNumberStoichMetabolicNetwork.AbstractBigIntegerMetaboliteRatio(reac, meta) {
						public Metabolite getMetabolite() {							
							return new DefaultMetabolite(metaName);
						}
						@Override
						protected ReadableBigIntegerRationalMatrix getStoich() {
							return cmp;
						}
					});
				}
			}
			return AbstractReaction.toString(ratios, reversible[reac]);
		}
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
	/*........................................................................*/
	/**
	 * NullspaceRecord, mutable record containing all data for compression using 
	 * nullspace analysis
	 */
	private static class NullspaceRecord extends WorkRecord {
		final BigIntegerRationalMatrix	reducedStoich;
		final BigIntegerRationalMatrix	kernel;
		public NullspaceRecord(WorkRecord workRecord) {
			super(workRecord);
			final BigIntegerRationalMatrix stoich = workRecord.cmp;
			final Size size = workRecord.size;
			reducedStoich = (stoich.getRowCount() == size.metas && stoich.getColumnCount() == size.reacs) ? 
				stoich : stoich.subBigIntegerRationalMatrix(0, size.metas, 0, size.reacs);  
			kernel = new Gauss(0d /*NOTE really?*/).nullspace(reducedStoich);
			if (Loggers.isLoggable(LOG, Level.FINEST)) {
				LogPrintWriter logWriter = new LogPrintWriter(LOG, Level.FINEST);
				LOG.finest("stoich matrix:");
				reducedStoich.writeToMultiline(logWriter);
				LOG.finest("kernel matrix:");
				kernel.writeToMultiline(logWriter);
			}
		}
		@Override
		public void removeReaction(int reac) {
			super.removeReaction(reac);//also decrements size.reacs
			kernel.swapRows(reac, size.reacs);			
		}
	}

	/*==========================================================================
	 * Static helper methods
	 */
	
//	private static int closeToOneLen(BigFraction value) {
//		value = value.abs();
//		return value.getNumerator().bitLength() + value.getDenominator().bitLength();		
//	}
	private static void negateColumn(BigIntegerRationalMatrix mx, int col) {
		final int rows = mx.getRowCount();
		for (int row = 0; row < rows; row++) {
			BigInteger num = mx.getBigIntegerNumeratorAt(row, col);
			BigInteger den = mx.getBigIntegerDenominatorAt(row, col);
			mx.setValueAt(row, col, num.negate(), den);
		}
	}
	private static void addColumnMultipleTo(BigIntegerRationalMatrix mx, int dstCol, BigFraction dstMul, int srcCol, BigFraction srcMul) {
		final int rows = mx.getRowCount();
		for (int row = 0; row < rows; row++) {
			if (mx.getSignumAt(row, srcCol) != 0 || mx.getSignumAt(row, dstCol) != 0) {
				BigFraction add = mx.getBigFractionValueAt(row, srcCol).multiply(srcMul);
				mx.multiply(row, dstCol, dstMul.getNumerator(), dstMul.getDenominator());
				mx.add(row, dstCol, add.getNumerator(), add.getDenominator());
				mx.reduceValueAt(row, dstCol);				
			}
		}
	}
	private static void addColumnMultipleTo(BigIntegerRationalMatrix mx, int dstCol, int srcCol, BigFraction dstToSrcRatio) {
		final int rows = mx.getRowCount();
		for (int row = 0; row < rows; row++) {
			if (mx.getSignumAt(row, srcCol) != 0) {
				BigFraction add = mx.getBigFractionValueAt(row, srcCol).divide(dstToSrcRatio);
				mx.add(row, dstCol, add.getNumerator(), add.getDenominator());
				mx.reduceValueAt(row, dstCol);
			}
		}
	}
	
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
	
	private static BigIntegerRationalMatrix createSubStoich(ReadableBigIntegerRationalMatrix pre, ReadableBigIntegerRationalMatrix stoich, ReadableBigIntegerRationalMatrix post, boolean[] reversible, Size size) {
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
