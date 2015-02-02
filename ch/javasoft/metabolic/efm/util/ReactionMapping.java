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
package ch.javasoft.metabolic.efm.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compress.CompressedMetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.util.ints.BitSetIntSet;
import ch.javasoft.util.ints.DefaultIntList;
import ch.javasoft.util.ints.IntIterator;
import ch.javasoft.util.ints.IntList;
import ch.javasoft.util.ints.IntSet;
import ch.javasoft.util.ints.KeyRangeIntIntMap;
import ch.javasoft.util.map.DefaultIntIntMultiValueMap;
import ch.javasoft.util.map.IntIntMultiValueMap;
import ch.javasoft.util.map.JoinedMultiValueMap;
import ch.javasoft.util.map.MultiValueMap;
import ch.javasoft.util.map.SingleValueMap;

/**
 * The <code>ReactionMapping</code> handles forward and backward mapping of
 * reactions in the whole flux mode computation process.
 * <p> 
 * The reactions exist on four layers:<ul>
 * <li><b>(O) Original Network:   </b>The metabolic network as submitted to the  
 * 				     				  efm calculation routine.</li>
 * <li><b>(C) Compressed Network: </b>The metabolic network after compression. 
 * 									  Deadend reactions as well as suppressed 
 * 									  ones are removed, others are merged due to 
 * 									  the compression routine.</li>
 * <li><b>(E) Expanded Network:   </b>Reversible reactions are split, if 
 * 									  reaction splitting is not suppressed.</li>
 * <li><b>(S) Sorted Network:     </b>Reactions are sorted, e.g. to compute the 
 * 									  nullspace matrix or to optimize running 
 * 									  time of the iteration phase</li>
 * </ul>
 * <p>
 * The reaction mapping relations between the different layers are as follows:
 * <br/>
 * <tt>(O) -[1..m]--[0..n]- (C) -[1]--[1..2]- (E) -[1]--[1]- (S)</tt>
 */
public class ReactionMapping {
	public static enum Layer {
		Original, Compressed, Expanded, Sorted;
		
		private Layer min(Layer other) {
			return ordinal() > other.ordinal() ? other : this;
		}
		private Layer max(Layer other) {
			return ordinal() < other.ordinal() ? other : this;
		}
		private Layer next() {
			return values()[ordinal() + 1];
		}
	}
	
	private static enum SingleForwardMap {
		OC, CE, ES;	
		@SuppressWarnings("unused")
		public Layer sourceLayer() {
			return Layer.values()[ordinal()];
		}
		@SuppressWarnings("unused")
		public Layer destinationLayer() {
			return Layer.values()[ordinal() + 1];
		}
		public MultiValueMap<Integer, Integer> map(MultiValueMap<Integer, Integer>[] maps) {
			return maps[ordinal()];
		}
	}
	
	private static class Join {
		private final Layer from, to; 
		Join(Layer from, Layer to) {
			this.from = from.min(to);
			this.to   = from.max(to);
		}
		public boolean isSingleLayer() {
			return from.equals(to);
		}
		@Override
		public int hashCode() {
			return from.hashCode() ^ to.hashCode();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Join) {
				final Join other = (Join)obj;
				return from.equals(other.from) && to.equals(other.to);
			}
			return false;
		}
	}
	
	public enum Category {
		Suppress {
			@Override
			public boolean isMember(Config config, MetabolicNetwork origNet, String name) {
				return config.getReactionsToSuppress().contains(name);
			}
		}, 
		NoSplit {
			@Override
			public boolean isMember(Config config, MetabolicNetwork origNet, String name) {
				final Reaction reac = origNet.getReaction(name);
				return 
					config.getReactionsNoSplit().contains(name) || 
					reac.getConstraints().isReversible() && !config.getGenerator().splitReaction(reac);
			}
 		},
		Enforce {
			@Override
			public boolean isMember(Config config, MetabolicNetwork metaNet, String name) {
				return config.getReactionsToEnforce().contains(name);
			}
		}, 
 		Else {
			@Override
			public boolean isMember(Config config, MetabolicNetwork metaNet, String name) {
				return true;
			} 			
 		};
 		public boolean isSpecial() {
 			return this != Else;
 		}
 		public Category getStronger(Category other) {
 			return other.ordinal() < ordinal() ? other : this;
 		}
		public abstract boolean isMember(Config config, MetabolicNetwork origNet, String name);
		public static Category find(Config config, MetabolicNetwork origNet, String name) {
			for (Category o : values()) {
				if (o.isMember(config, origNet, name)) return o;
			}
			throw new RuntimeException("internal error, Else should always accept");
		}
	}
	
	private final Config			config;
	private final MetabolicNetwork 	cmpNet;
	private final int[] 			sortMapping;
	private final MultiValueMap<Integer, Integer>[] maps;
	private final Map<Join, MultiValueMap<Integer, Integer>> cachedJoins = new HashMap<Join, MultiValueMap<Integer,Integer>>();
	
	@SuppressWarnings("unchecked")
	public ReactionMapping(Config config, MetabolicNetwork cmpNet, int[] sortMapping) {
		this.config			= config;
		this.cmpNet 		= cmpNet;
		this.sortMapping	= sortMapping;
		maps = new MultiValueMap[Layer.values().length - 1];
		final IntIntMultiValueMap oc	 	= getOC(cmpNet);
		maps[SingleForwardMap.OC.ordinal()] = oc;
		maps[SingleForwardMap.CE.ordinal()] = getCE(config, cmpNet, oc);
		maps[SingleForwardMap.ES.ordinal()] = getES(sortMapping);
	}
	
	private static IntIntMultiValueMap getOC(MetabolicNetwork cmpNet) {
		if (cmpNet instanceof CompressedMetabolicNetwork) {
			return ((CompressedMetabolicNetwork)cmpNet).getReactionMapping();
		}
		else {
			//return an identity mapping
			return DefaultIntIntMultiValueMap.createFromSingleValueMap(new KeyRangeIntIntMap(cmpNet.getReactions().length()));
		}
	}
	private static IntIntMultiValueMap getCE(Config config, MetabolicNetwork cmpNet, IntIntMultiValueMap oc) {
		final IntIntMultiValueMap co = oc.invert();
		final DefaultIntIntMultiValueMap map = new DefaultIntIntMultiValueMap();
		int iOrg = 0;
		int iMap = 0;
		for (final Reaction reac : cmpNet.getReactions()) {
			map.add(iOrg, iMap);
			iMap++;
			if (isSplitReaction(config, cmpNet, co, reac, iOrg)) {
				map.add(iOrg, iMap);
				iMap++;
			}
			iOrg++;
		}
		return map;
	}
	private static MultiValueMap<Integer, Integer> getES(int[] sortMapping) {
		return new SingleValueMap<Integer, Integer>(new KeyRangeIntIntMap(MappingUtil.getInvertedMapping(sortMapping)));		
	}
	
	public void refreshSortMapping() {
		cachedJoins.clear();
		maps[Layer.Expanded.ordinal()] = getES(sortMapping);	
	}
	
	public int getFirst(Layer srcLayer, int srcIndex, Layer dstLayer) {
		final MultiValueMap<Integer, Integer> join = getJoin(srcLayer, dstLayer);
		final Integer mapped = join.getFirst(Integer.valueOf(srcIndex));
		return mapped == null ? -1 : mapped.intValue();
	}
	
	public IntList get(Layer srcLayer, int srcIndex, Layer dstLayer) {
		final MultiValueMap<Integer, Integer> join = getJoin(srcLayer, dstLayer);
		return new DefaultIntList(join.get(Integer.valueOf(srcIndex)));		
	}
	
	private MultiValueMap<Integer, Integer> getJoin(Layer srcLayer, Layer dstLayer) {
		final Join join = new Join(srcLayer, dstLayer);
		
		//try from cache
		MultiValueMap<Integer, Integer> res = cachedJoins.get(join);
		if (res != null) {
			return res;
		}
		
		res = getMap(join.from);
		if (join.isSingleLayer()) {
			res = new SingleValueMap<Integer, Integer>(new KeyRangeIntIntMap(res.keySize()));						
		}
		else {
			Layer cur = join.from.next();
			while (!cur.equals(join.to)) {
				res = JoinedMultiValueMap.join(res, getMap(cur));
				cur = cur.next();
			}
			if (dstLayer.equals(join.from)) {
				res = res.invert();
			}
		}
		if (res instanceof JoinedMultiValueMap) {
			res = ((JoinedMultiValueMap<Integer, Integer>)res).flatten();
		}
		cachedJoins.put(join, res);
		return res;
	}
	
	private MultiValueMap<Integer, Integer> getMap(Layer layer) {
		return maps[layer.ordinal()];
	}
	
	/**
	 * Returns true if this reaction is split in the expanded stoichiometric 
	 * matrix. A reaction is split if it is reversible and it is not a
	 * no-split reaction (for Efm). For Ep, a reaction is only split if it is 
	 * not an exchange reaction. 
	 * 
	 * @param config	the config to access no-split and suppressed reactions
	 * @param cmpNet	the compressed metabolic network
	 * @param reaction	the reaction to check
	 * 
	 * @return	true if this reaction is split, i.e. reflected by two columns
	 * 			in the expanded stoichiometric matrix
	 */
	public static boolean isSplitReaction(Config config, MetabolicNetwork cmpNet, Reaction reaction) {
		return isSplitReaction(config, cmpNet, getOC(cmpNet).invert(), reaction, cmpNet.getReactionIndex(reaction.getName()));
	}
	/**
	 * Returns true if this reaction is split in the expanded stoichiometric 
	 * matrix. A reaction is split if it is reversible and it is not a
	 * no-split reaction (for Efm). For Ep, a reaction is only split if it is 
	 * not an exchange reaction. 
	 * 
	 * @param config	the config to access no-split and suppressed reactions
	 * @param cmpNet	the compressed metabolic network
	 * @param co		the reverted OC mapping
	 * @param reaction	the reaction to check
	 * @param reactionIndex the reaction index in the compressed network
	 * 
	 * @return	true if this reaction is split, i.e. reflected by two columns
	 * 			in the expanded stoichiometric matrix
	 */
	private static boolean isSplitReaction(Config config, MetabolicNetwork cmpNet, IntIntMultiValueMap co, Reaction reaction, int reactionIndex) {
		final MetabolicNetwork orgNet = getRootNetwork(cmpNet);
		if (reaction.getConstraints().isReversible()) {
			final IntIterator it = co.get(reactionIndex).iterator();
			while (it.hasNext()) {
				final int origIndex = it.nextInt();
				String origName = orgNet.getReactions().get(origIndex).getName();
				if (Category.NoSplit.isMember(config, orgNet, origName)) return false;
			}
			return true;
		}
		return false;
	}
	
	public int getOriginalReactionIndexByName(String reactionName) {
		return getRootNetwork(cmpNet).getReactionIndex(reactionName);
	}
	
	public IntList getByOriginalReactionName(String reactionName, Layer dstLayer) {
		final int index = getOriginalReactionIndexByName(reactionName);
		return get(Layer.Original, index, dstLayer);
	}
	
	private static MetabolicNetwork getRootNetwork(MetabolicNetwork cmpNet) {
		return cmpNet instanceof CompressedMetabolicNetwork ? ((CompressedMetabolicNetwork)cmpNet).getRootNetwork() : cmpNet;
	}
	
	/**
	 * Returns the reaction category by the sorted index in the kernel matrix
	 */
	public Category getReactionCategoryBySortedIndex(int sortedIndex) {
		return getReactionCategoryByExpandedIndex(sortMapping[sortedIndex]);
	}
	/**
	 * Returns the reaction category by the expanded index, without sorting
	 */
	public Category getReactionCategoryByExpandedIndex(int expandedIndex) {
		final MetabolicNetwork origNet = getRootNetwork(cmpNet);
		final IntList origReacs = get(Layer.Expanded, expandedIndex, Layer.Original);
		Category cat = Category.Else;
		for (int i = 0; i < origReacs.size(); i++) {
			final int origIndex = origReacs.getInt(i);			
			final String origName = origNet.getReactions().get(origIndex).getName(); 
			cat = cat.getStronger(Category.find(config, origNet, origName));
		}
		return cat;
	}
	
	public boolean isReactionReversibleBySortedIndex(int sortedIndex) {		
		final int cmpIndex = getFirst(Layer.Sorted, sortedIndex, Layer.Compressed);
		return cmpNet.getReactions().get(cmpIndex).getConstraints().isReversible();
	}
	
	/**
	 * If one sorted index of a split reversible reaction is known, this method
	 * returns the other (twin) index
	 * 
	 * @throws IllegalArgumentException if the specified index is not a 
	 * 									reversible split reaction
	 */
	public int getSortedReactionIndexOfTwinPart(int sortedIndex) {
		final int compressed = getFirst(Layer.Sorted, sortedIndex, Layer.Compressed);
		final IntList sorted = get(Layer.Compressed, compressed, Layer.Sorted);
		if (sorted.size() != 2) {
			throw new IllegalArgumentException("not a split reversible reaction: " + sortedIndex);
		}
		final int other = sorted.getInt(0);
		if (other == sortedIndex) {
			return sorted.getInt(1);
		}
		return other;
	}
	
	/**
	 * Returns the unexpanded flux values from expanded ones, i.e. unmaps flux
	 * values using the inverted CE map
	 */
	public <N extends Number, Col extends Column> N[] getUnexpandedFluxValues(ColumnHome<N, Col> columnHome, N[] expandedFluxValues) {
		final IntIntMultiValueMap map = (IntIntMultiValueMap)SingleForwardMap.CE.map(maps);
		final NumberOperations<N> ops = columnHome.getNumberOperations(); 
		final N[] unexpanded = ops.newArray(cmpNet.getReactions().length());
		for (int i = 0; i < unexpanded.length; i++) {
			if (map.count(i) > 1) {
				final IntIterator ixIt = map.get(i).iterator();
				final int ixA = ixIt.nextInt();
				final int ixB = ixIt.nextInt();
				if (ops.isZero(expandedFluxValues[ixB])) {
					unexpanded[i] = expandedFluxValues[ixA];
				}
				else {
					if (ops.isNonZero(expandedFluxValues[ixA])) {
						throw new IllegalArgumentException("non-zero values for forward/backward reversible reaction [" + i + "]-->[" + ixA + ", " + ixB + "]: " + 
								Arrays.toString(expandedFluxValues));
					}
					unexpanded[i] = ops.negate(expandedFluxValues[ixB]);
				}
			}
			else {
				unexpanded[i] = expandedFluxValues[map.getFirst(i)];
			}
		}
		return unexpanded;
	}
	
	/**
	 * Returns the number reactions which are not processed within the iteration
	 * loop, e.g. because a flux value is enforced or because they are not
	 * split
	 */
	public int getExpandedReactionCountOutOfIterationLoop() {
		final Set<String> reacs = new HashSet<String>();
		reacs.addAll(config.getReactionsNoSplit());
		reacs.addAll(config.getReactionsToEnforce());
		for (final Reaction reac : getRootNetwork(cmpNet).getReactions()) {
			if (!reacs.contains(reac.getName()) && reac.getConstraints().isReversible()) {
				if (!config.getGenerator().splitReaction(reac)) {
					reacs.add(reac.getName());					
				}
			}
		}
		//NOT necessary: reacs.addAll(config.getReactionsToSuppress());
		final IntSet inds = new BitSetIntSet();
		for (final String reac : reacs) {
			final int rind = getRootNetwork(cmpNet).getReactionIndex(reac);
			inds.addAll(get(Layer.Original, rind, Layer.Expanded));
		}
		return inds.size();
	}
	
	public static <N extends Number> ReadableMatrix<N> unsortExpandedStoichMatrixCols(ReadableMatrix<N> stoich, int[] sortMapping) {
		return unsortMatrix(stoich, sortMapping, false);
	}
	public static <N extends Number> ReadableMatrix<N> unsortKernelMatrixRows(ReadableMatrix<N> kn, int[] sortMapping) {
		return unsortMatrix(kn, sortMapping, true);
	}
	private static <N extends Number> ReadableMatrix<N> unsortMatrix(ReadableMatrix<N> kn, int[] sortMapping, boolean unsortRows) {
		final WritableMatrix<N> mapped = kn.toWritableMatrix(true /*enforceNewInstance*/);
		for (int row = 0; row < sortMapping.length; row++) {
			final int dstRow = unsortRows ? sortMapping[row] : row;
			for (int col = 0; col < kn.getColumnCount(); col++) {
				final int dstCol = unsortRows ? col : sortMapping[col];
				final N value = kn.getNumberValueAt(row, col);
				mapped.setValueAt(dstRow, dstCol, value);				
			}
		}
		return mapped.toReadableMatrix(false /*new instance*/);
	}
}
