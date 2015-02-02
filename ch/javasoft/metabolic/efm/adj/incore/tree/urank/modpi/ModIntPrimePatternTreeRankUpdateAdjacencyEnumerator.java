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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank.modpi;

import java.io.IOException;
import java.util.Queue;

import ch.javasoft.metabolic.efm.adj.incore.AbstractStoichMappingAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.RankUpdateRoot;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <tt>ModuloPatternTreeRankUpdateAdjacencyEnumerator</tt> uses pattern trees for
 * candidate narrowing, and rank computation for adjacency testing modulo an int prime.
 * The prime is chosen such that doubling the product of any two numbers (being &lt; prime)
 * still fits into an int, i.e. the <tt>prime < Math.sqrt(Integer.MAX_VALUE / 2)</tt>.
 * <p>
 * The ranks are not always computed from scratch, but updated. For every node 
 * pair, the rank matrix is processed when it is used the first time. Child node 
 * pairs can then base their rank computation on parent pairs, i.e. the gauss 
 * algorithm starts where the parent rank computation stopped (it stopped 
 * because no other non-zero pivot was found). Note that child node-pairs have 
 * fewer zeros in the zero sets, and thus the sub matrix of the stoichiometric 
 * matrix contains more columns, but at least those columns from the parent node 
 * pair.
 */
public class ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator extends AbstractStoichMappingAdjacencyEnumerator {

	public static final String NAME = "pattern-tree-rank-update-modpi";

	public ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator() {
		super();
	}

	public String name() {
		return NAME;
	}

	@Override
	public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException {
		final ModIntPrimeRankUpdateTreeFactory fac = new ModIntPrimeRankUpdateTreeFactory(mModel);
    	final RankUpdateRoot root = new RankUpdateRoot(mConfig, mModel, fac, mModel.getStoichRank(), posCols, zerCols, negCols);
//    	final SearchAndRankUpdateRoot root = new SearchAndRankUpdateRoot(mConfig, mModel, fac, mModel.getStoichRank(), posCols, zerCols, negCols);
    	fac.createTraverser().traverseTree(root, posCols, zerCols, negCols, adjacentPairs);
	}

//	private int getRank(ReadableMatrix<? extends Number> matrix) {
////		final BigIntegerRationalMatrix mx = MatrixUtil.convertToBigIntegerRationalMatrix(matrix, mConfig.zero(), true /*enforce new instance*/);
//		final BigIntegerRationalMatrix mx = MatrixUtil.convertToBigIntegerRationalMatrix(matrix, mConfig.zero(), false /*enforce new instance*/);
//		return new Gauss(0d).rank(mx);
//	}
}
