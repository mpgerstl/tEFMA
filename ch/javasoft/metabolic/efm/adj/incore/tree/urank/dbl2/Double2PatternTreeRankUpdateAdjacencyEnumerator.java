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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank.dbl2;

import java.io.IOException;
import java.util.Queue;

import ch.javasoft.metabolic.efm.adj.incore.AbstractStoichMappingAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.RankUpdateRoot;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.dbl.DoublePatternTreeRankUpdateAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <tt>Double2PatternTreeRankUpdateAdjacencyEnumerator</tt> uses pattern trees for
 * candidate narrowing, and rank computation for adjacency testing. The ranks 
 * are not always computed from scratch, but updated. For every node pair, the
 * rank matrix is processed when it is used the first time. Child node pairs can
 * then base their rank computation on parent pairs, i.e. the gauss algorithm 
 * starts where the parent rank computation stopped (it stopped because no other
 * non-zero pivot was found). Note that child node-pairs have fewer zeros in the
 * zero sets, and thus the sub matrix of the stoichiometric matrix contains more
 * columns, but at least those columns from the parent node pair.
 * <p>
 * For numerical reasons, the matrices must be stored and restored for multiple
 * rank computations. This version, compared to
 * {@link DoublePatternTreeRankUpdateAdjacencyEnumerator}, only stores and 
 * restores matrix residues for leaf matrices.
 */
public class Double2PatternTreeRankUpdateAdjacencyEnumerator extends AbstractStoichMappingAdjacencyEnumerator {

	public static final String NAME = "pattern-tree-rank-update-dbl2";

	public Double2PatternTreeRankUpdateAdjacencyEnumerator() {
		super();
	}

	public String name() {
		return NAME;
	}

	@Override
	public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException {		
		Double2RankUpdateTreeFactory fac = new Double2RankUpdateTreeFactory(mModel);
    	RankUpdateRoot root = new RankUpdateRoot(mConfig, mModel, fac, mModel.getStoichRank(), posCols, zerCols, negCols);
    	fac.createTraverser().traverseTree(root, posCols, zerCols, negCols, adjacentPairs);
	}
	
}
