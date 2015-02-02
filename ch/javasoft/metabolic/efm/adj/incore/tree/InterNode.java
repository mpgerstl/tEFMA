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
package ch.javasoft.metabolic.efm.adj.incore.tree;

import java.io.IOException;
import java.util.Queue;

import java.util.concurrent.atomic.AtomicLong;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.AbstractColumn;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

public class InterNode<T /*traversing token*/> extends Node<T> {
        private static AtomicLong atomic_addAdjacentPairs_invocations;
	public final Node<T> child0;
	public final Node<T> child1;
	protected InterNode(TreeFactory<T> treeFactory, SortableMemory<Column> cols, int[] selectiveBits, int prevSelBitIndex, int iStart, int iEnd) throws IOException {
		super(Node.calculateUnionPattern(cols, iStart, iEnd));
		atomic_addAdjacentPairs_invocations = new AtomicLong();
		
		//this loop does path-shortening:
		//- intermediary nodes with only one child are shortened
		//- this saves memory and enhances performance significantly
		int median;
		do {
			median = AbstractColumn.partition(cols, iStart, iEnd, selectiveBits[++prevSelBitIndex]);
		}
		while (median == iStart || median == iEnd);
		child0 = treeFactory.createNode(cols, selectiveBits, prevSelBitIndex, iStart, median);
		child1 = treeFactory.createNode(cols, selectiveBits, prevSelBitIndex, median, iEnd);
	}
	@Override
	public void addAdjacentPairs(T token, Root<T> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Node<T> partner, boolean thisIsPos, Queue<ColumnPair> adjacentPairs) throws IOException {

		atomic_addAdjacentPairs_invocations.incrementAndGet();
                // System.out.println("void InterNode.addAdjacentPairs(). addAdjacentPairs_invocations: " + addAdjacentPairs_invocations);

		if (root.enterIfCandidates(token, this, partner)) {
    		if (partner instanceof InterNode) {
    			InterNode<T> interPartner = (InterNode<T>)partner;
			child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
			child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);
			child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
			child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);    					

				//better first the one-side? (seems: NO)
//				child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);    					
//				child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
//				child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);
//				child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);

				//better cache use? (seems: NO)
//				child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
//				child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);
//				child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);    					
//				child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
    		}
    		else {
			child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, partner, thisIsPos, adjacentPairs);
			child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, partner, thisIsPos, adjacentPairs);
    		}
			root.leave(token, this, partner);
		}
	}
	@Override
	public IBitSet filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> cols, AdjCandidates<Column> adjCandidates) throws IOException {
		if (filterCutPattern != null && filterCutPattern.isSubSetOf(child0.unionPattern)) {
			filterCutPattern = child0.filterAdjacentPairs(filterCutPattern, cols, adjCandidates);
		}
		if (filterCutPattern != null && filterCutPattern.isSubSetOf(child1.unionPattern)) {
			filterCutPattern = child1.filterAdjacentPairs(filterCutPattern, cols, adjCandidates);
		}
		return filterCutPattern;
	}
 
	// atomic
	public long get_atomic_addAdjacentPairs_invocations()
        {
		return atomic_addAdjacentPairs_invocations.get();
        }
 
	public long reset_atomic_addAdjacentPairs_invocations()
        {
		// System.out.println("entered InterNode.reset_addAdjacentPairs_invocations()");
                long ret = atomic_addAdjacentPairs_invocations.get();
                atomic_addAdjacentPairs_invocations.set(0);
		return ret;
        }
}
