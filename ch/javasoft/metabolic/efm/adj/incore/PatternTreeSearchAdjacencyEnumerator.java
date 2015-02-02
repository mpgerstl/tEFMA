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
package ch.javasoft.metabolic.efm.adj.incore;

import java.io.IOException;
import java.util.Queue;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.adj.incore.tree.AbstractRoot;
import ch.javasoft.metabolic.efm.adj.incore.tree.DefaultTreeFactory;
import ch.javasoft.metabolic.efm.adj.incore.tree.Node;
import ch.javasoft.metabolic.efm.adj.incore.tree.TreeFactory;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <code>PatternTreeSearchAdjacencyEnumerator</code> performs the
 * combinatorial adjacency test by scanning for a superset of the intersection
 * set. The search is performed with a pattern tree.
 */
public class PatternTreeSearchAdjacencyEnumerator extends AbstractSearchAdjacencyEnumerator {

	public static final String NAME = "pattern-tree-search";
	
	public String name() {
		return NAME;
	}
	
	private Node<Void> nodeZer;	
	private Node<Void> nodePos;	
	private Node<Void> nodeNeg;	

	@SuppressWarnings("unchecked")
	@Override
	public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException {
		final TreeFactory<Void> fac = new DefaultTreeFactory(mModel);
		nodePos = fac.createNode(posCols, AbstractRoot.calculateXorBitOrder(posCols), -1, 0, posCols.getColumnCount());
		nodeNeg = fac.createNode(negCols, AbstractRoot.calculateXorBitOrder(negCols), -1, 0, negCols.getColumnCount());
		nodeZer = fac.createNode(zerCols, AbstractRoot.calculateXorBitOrder(zerCols), -1, 0, zerCols.getColumnCount());
		super.adjacentPairs(adjacentPairs, zerCols, posCols, negCols);
		nodePos = null;
		nodeNeg = null;
		nodeZer = null;
	}
	@Override
	protected void filterAdjacentPairs(SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols, AdjCandidates<Column> candidates) throws IOException {
		IBitSet filter = candidates.getIntersection(0);
		filter = nodeZer.filterAdjacentPairs(filter, zerCols, candidates);
		if (filter != null) {
			filter = nodePos.filterAdjacentPairs(filter, posCols, candidates);			
		}
		if (filter != null) {
			filter = nodeNeg.filterAdjacentPairs(filter, negCols, candidates);			
		}
	}

}
