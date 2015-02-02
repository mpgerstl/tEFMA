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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.adj.incore.tree.AbstractRoot;
import ch.javasoft.metabolic.efm.adj.incore.tree.Node;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrixFactory;
import ch.javasoft.metabolic.efm.rankup.RankUpRoot;

/**
 * The <tt>RankUpdateRoot</tt> uses rank updating to test adjacency.
 */
public class RankUpdateRoot extends AbstractRoot<RankUpdateToken> implements RankUpRoot, PreprocessedMatrixFactory  {

	private final int mStoichRank;

	public RankUpdateRoot(Config config, EfmModel model, RankUpdateJobScheduleTreeFactory treeFactory, int stoichRank, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols) {
		super(config, model, treeFactory, posCols, zeroCols, negCols);
		mStoichRank	= stoichRank;
	}

	@Override
	public boolean enterIfCandidates(RankUpdateToken token, Node<RankUpdateToken> nodeA, Node<RankUpdateToken> nodeB) {
		final IBitSet unionCut = nodeA.unionPattern.getAnd(nodeB.unionPattern);
		if (hasRequiredZeroBitCount(token, unionCut)) {
			token.addChildRankMatrix(unionCut);
			//intersectionSet.cardinality() >= getRequiredRank() - getStoichRank();
//			if (getRequiredRank() - getStoichRank() - unionCut.cardinality() >= 0) {
//				if (token.getRankMatrix().hasRequiredRank(token, this, unionCut)) {
//					return true;
//				}
//				else {
//					token.removeRankMatrix();
//					return false;
//				}
//			}
			return true;
		}
		return false;
	}
	
	@Override
	public void leave(RankUpdateToken token, Node<RankUpdateToken> nodeA, Node<RankUpdateToken> nodeB) {
		token.removeChildRankMatrix();
	}
	
	private static final int commonTestThreshold = 3;//(incl) 3 seems to be best
	
	public void filterAdjacentPairs(RankUpdateToken token, Node<RankUpdateToken> nodeA, Node<RankUpdateToken> nodeB, IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjCandidates) throws IOException {		
		int ind = 0;
		int len = adjCandidates.size();
		if (ind < len) {
			final IBitSet nodePattern = nodeA.unionPattern.getAnd(nodeB.unionPattern);
			if (len - ind >= commonTestThreshold) {
				IBitSet filterUnionPattern = adjCandidates.getIntersection(0);
				for (int i = 1; i < len; i++) {
					filterUnionPattern.or(adjCandidates.getIntersection(i));
				}
				if (!hasRequiredRank(token, nodePattern, filterUnionPattern)) {
//					cntRankCommonNo++;
					adjCandidates.removeAll();
					return;
				}
//				cntRankCommonYes++;
			}
			while (ind < len) {
				if (hasRequiredRank(token, nodePattern, adjCandidates.getIntersection(ind))) {
//					cntRankIndividualYes++;
					ind++;
				}
				else {
//					cntRankIndividualNo++;
					len--;
					if (ind != len) {
						adjCandidates.swap(ind, len);
					}
					adjCandidates.removeLast();
				}
			}
		}
	}
	
	public RankUpdateJobScheduleTreeFactory getRankUpdateTreeFactory() {
		return (RankUpdateJobScheduleTreeFactory)mTreeFactory;
	}
	
	public int[] getColMapping() {
		return mModel.getReactionSorting();
	}

	/**
	 * Returns the number of reactions. At the same time, this is also the bit
	 * set length for all bit sets. It is also equal to the number of columns in
	 * the stoichiometrix matrix.
	 * 
	 * @return q, the number of reactions
	 */
	public int getReactionCount() {
		return getStoichRational().getColumnCount();
	}
	
	/**
	 * Returns the required rank, that is, number of reactions minus 2
	 * @return returns q - 2
	 */
	public int getRequiredRank() {
		return getReactionCount() - 2;
	}
	
	public int getRequiredCardinality() {
		return getRequiredRank() - getStoichRank();
	}
	
	/**
	 * Returns true if the number of zeros (true bits) in the intersection set
	 * is at least q - 2 - stoich.
	 * @return true if cardinality of set >= q - 2 - rank(N)
	 */
	public boolean hasRequiredZeroBitCount(RankUpdateToken token, IBitSet intersectionSet) {
		return intersectionSet.cardinality() >= getRequiredCardinality();
	}
	/**
	 * Returns true if <code>count</code> is at least <code>q - 2 - stoich</code>
	 * @return true if cardinality of set >= q - 2 - rank(N)
	 */
	public boolean isRequiredZeroBitCount(RankUpdateToken token, int count) {
		return count >= getRequiredCardinality();
	}
	
	public int getStoichRank() {
		return mStoichRank;
	}
	
	protected boolean hasRequiredRank(RankUpdateToken token, IBitSet nodePattern, IBitSet intersectionSet) {
		final PreprocessableMatrix mx = token.getRankMatrix();
		return mx.hasRequiredRank(token, this, this, intersectionSet);
	}
	
	public PreprocessedMatrix createInitialPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token) {
		return getRankUpdateTreeFactory().createInitialPreprocessedMatrix(owner, token, this);
	}
	public PreprocessedMatrix createChildPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token, PreprocessedMatrix parentPreprocessedMatrix) {
		return getRankUpdateTreeFactory().createChildPreprocessedMatrix(owner, token, this, parentPreprocessedMatrix);
	}
	
}
