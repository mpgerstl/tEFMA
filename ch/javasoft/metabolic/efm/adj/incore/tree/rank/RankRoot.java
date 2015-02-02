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
package ch.javasoft.metabolic.efm.adj.incore.tree.rank;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.adj.incore.RankAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.adj.incore.tree.AbstractRoot;
import ch.javasoft.metabolic.efm.adj.incore.tree.Node;
import ch.javasoft.metabolic.efm.adj.incore.tree.TreeFactory;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

/**
 * The <tt>RankRoot</tt> uses the rank adjacency test.
 */
public class RankRoot<T /*traversing token*/> extends AbstractRoot<T> {

	private final RankAdjacencyEnumerator mRankEnum;

	public RankRoot(Config config, EfmModel model, TreeFactory<T> treeFactory, RankAdjacencyEnumerator rankEnum, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols) {
		super(config, model, treeFactory, posCols, zeroCols, negCols);
		mRankEnum = rankEnum;
	}
	
	private final int commonTestThreshold = 3;//(incl) 3 seems to be best

	
	public void filterAdjacentPairs(T token, Node<T> nodeA, Node<T> nodeB, IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjCandidates) throws IOException {
		int ind = 0;
		int len = adjCandidates.size();
		if (len - ind >= commonTestThreshold) {
			IBitSet filterUnionPattern = adjCandidates.getIntersection(0);
			for (int i = 1; i < len; i++) {
				filterUnionPattern.or(adjCandidates.getIntersection(i));
			}			
			if (!mRankEnum.hasRequiredRank(filterUnionPattern)) {
				adjCandidates.removeAll();
				return;
			}
		}
		while (ind < len) {
			if (mRankEnum.hasRequiredRank(adjCandidates.getIntersection(ind))) {
				ind++;
			}
			else {
				len--;
				if (ind != len) {
					adjCandidates.swap(ind, len);
				}
				adjCandidates.removeLast();
			}
		}
	}
	
	public boolean isRequiredZeroBitCount(T token, int count) {
		return mRankEnum.isRequiredZeroBitCount(count);
	}
	public boolean hasRequiredZeroBitCount(T token, IBitSet cutPattern) {
		return mRankEnum.hasRequiredZeroBitCount(cutPattern);
	}
	
//	private final int fullTestThreshold = 1;
//	@Override
//	public boolean hasRequiredZeroBitCount(SimpleLongBitSet cutPattern) {
//		final int card = cutPattern.cardinality();
//		final int diff = card - mRequiredZeroCount;
//		if (diff < 0) return false;
//		return diff >= fullTestThreshold ? true : mRankEnum.hasRequiredRank(cutPattern);
//	}

}
