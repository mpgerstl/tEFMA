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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

public class Leaf<T /*traversing token*/> extends Node<T> {

	public static final int MAX_LEAF_SIZE = 1;//4
	
	protected final int start, end; 
	protected Leaf(TreeFactory<T> treeFactory, SortableMemory<Column> cols, int iStart, int iEnd) {
		super(Node.calculateUnionPattern(cols, iStart, iEnd));
		start	= iStart;
		end		= iEnd;
	}
	@Override
	public void addAdjacentPairs(T token, Root<T> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Node<T> partner, boolean thisIsPos, Queue<ColumnPair> adjacentPairs) throws IOException {
                System.out.println("in void Leaf.addAdjacentPairs()");
		if (root.enterIfCandidates(token, this, partner)) {
			if (partner instanceof Leaf) {
				final Leaf<T> leafPartner = ((Leaf<T>)partner);    		
				final int cntCandidates = (end - start) * (leafPartner.end - leafPartner.start);
				final AdjCandidates<Column> adjCandidates = new AdjCandidates<Column>(posCols, negCols, cntCandidates);
	    		final IBitSet cutPat = addAdjacentPairs(token, root, thisIsPos ? posCols : negCols, thisIsPos ? negCols : posCols, leafPartner, adjCandidates, adjacentPairs);
	    		if (cutPat != null) {
	    			root.filterAdjacentPairs(token, this, partner, cutPat, posCols, zeroCols, negCols, adjCandidates);
				System.out.println("Leaf.addAdjacentPairs() before appendPairsTo()");
    				adjCandidates.appendPairsTo(adjacentPairs);
	    		}
			}
			else {
//                ((InterNode<T>)partner).addAdjacentPairs(token, root, posCols, zeroCols, negCols, this, !thisIsPos, adjacentPairs);
				partner.addAdjacentPairs(token, root, posCols, zeroCols, negCols, this, !thisIsPos, adjacentPairs);
			}
			root.leave(token, this, partner);
		}
	}
	private IBitSet addAdjacentPairs(T token, Root<T> root, SortableMemory<Column> thisCols, SortableMemory<Column> partnerCols, final Leaf partner, final AdjCandidates<Column> adjCandidates, Queue<ColumnPair> adjacentPairs) throws IOException {
                System.out.println("in IBitSet Leaf.addAdjacentPairs()");
		IBitSet pat = null;
		for (int ii = start; ii < end; ii++) {
			for (int jj = partner.start; jj < partner.end; jj++) {
				final int index = adjCandidates.size(); 
				adjCandidates.add(thisCols, ii, partnerCols, jj);
				final int interCard = adjCandidates.getIntersectionCardinality(index);
				if (root.isRequiredZeroBitCount(token, interCard)) {
//					if (root.keepByColumnPairFilter(pair)) {

						//adjacent if |Z(r1)\Z(r2)| = 1 or |Z(r2)\Z(r1)| = 1
						if (adjCandidates.getColumnPos(index).bitValues().cardinality() - interCard == 1 ||
							adjCandidates.getColumnNeg(index).bitValues().cardinality() - interCard == 1) {
							
							adjCandidates.appendPairTo(adjacentPairs, index);
							adjCandidates.removeLast();
						}
						else {
							final IBitSet inter = adjCandidates.getIntersection(index);
							if (pat == null) pat = inter;
							else pat.and(inter);
						}
//					}
				}
				else {
					adjCandidates.removeLast();
				}
			}
		}
		return pat;
	}
	@Override
	public IBitSet filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> cols, AdjCandidates<Column> adjCandidates) throws IOException {
		if (filterCutPattern != null && filterCutPattern.isSubSetOf(unionPattern)) {
			IBitSet cutPat = null;
			int len = adjCandidates.size();
			int index = 0;
			while (index < len) {
				if (adjCandidates.hasSuperSet(index, cols, start, end)) {
					len--;
					if (index != len) {
						adjCandidates.swap(index, len);
					}
					adjCandidates.removeLast();						
				}
				else {
					final IBitSet inter = adjCandidates.getIntersection(index);
					if (cutPat == null) cutPat = inter;
					else cutPat.and(inter);
					index++;
				}
			}
			return cutPat;
		}
		return filterCutPattern;
	}
}
