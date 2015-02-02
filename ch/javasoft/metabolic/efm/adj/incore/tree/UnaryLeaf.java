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
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

public class UnaryLeaf<T /*traversing token*/> extends Node<T> {

	public static final int MAX_LEAF_SIZE = 1;//4

        private static AtomicLong atomic_addAdjacentPairs_invocations;
        private static AtomicLong atomic_adjacency_tests;
        private static AtomicLong atomic_found_adjacents;
	
	protected final int columnIndex; 
	protected UnaryLeaf(TreeFactory<T> treeFactory, SortableMemory<Column> cols, int columnIndex) throws IOException {
//		super(Node.calculateUnionPattern(cols, columnIndex, columnIndex + 1));
		super(cols.getColumn(columnIndex).bitValues());
		this.columnIndex = columnIndex;
		atomic_addAdjacentPairs_invocations = new AtomicLong();
		atomic_adjacency_tests              = new AtomicLong();
		atomic_found_adjacents              = new AtomicLong();
	}
	@Override
	public void addAdjacentPairs(T token, Root<T> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Node<T> partner, boolean thisIsPos, Queue<ColumnPair> adjacentPairs) throws IOException {

		atomic_addAdjacentPairs_invocations.incrementAndGet();
                // System.out.println("void UnaryLeaf.addAdjacentPairs(). addAdjacentPairs_invocations: " + addAdjacentPairs_invocations);
		if (root.enterIfCandidates(token, this, partner)) {
                	// System.out.println("it is a candidate");
			if (partner instanceof UnaryLeaf) {
				// int cur_adjPairSize = adjacentPairs.size();

                                atomic_adjacency_tests.incrementAndGet();
                            	// System.out.println("leaf-leaf hit. adjacency_tests: " + adjacency_tests);
				final UnaryLeaf<T> leafPartner = ((UnaryLeaf<T>)partner);    		
				final AdjCandidates<Column> adjCandidates = new AdjCandidates<Column>(posCols, negCols, 1);
	    			final IBitSet cutPat = addAdjacentPairs(token, root, thisIsPos ? posCols : negCols, thisIsPos ? negCols : posCols, leafPartner, adjCandidates, adjacentPairs);
	    			if (cutPat != null) {
	    				root.filterAdjacentPairs(token, this, partner, cutPat, posCols, zeroCols, negCols, adjCandidates);
    					adjCandidates.appendPairsTo(adjacentPairs);

					atomic_found_adjacents.addAndGet(adjCandidates.size());
                                        // System.out.println("cutPat equal != null -> found_adjacents: " + found_adjacents);
	    			}
                                else {
                                        // found_adjacents++;
                                        // System.out.println("cutPat equal = null -> found_adjacents: " + found_adjacents);
                                }
                        	// System.out.println("stop leaf-leaf procedure");
		       		// System.out.println("Size of adjacentPairs: " + adjacentPairs.size());
				// adjacency_tests += adjacentPairs.size() - cur_adjPairSize;
			}
			else {
                            	// System.out.println("partner is a InterNode");
//                ((InterNode<T>)partner).addAdjacentPairs(token, root, posCols, zeroCols, negCols, this, !thisIsPos, adjacentPairs);
				partner.addAdjacentPairs(token, root, posCols, zeroCols, negCols, this, !thisIsPos, adjacentPairs);
			}
			root.leave(token, this, partner);
		}
                else {
                	// System.out.println("not a candidate");
                }
	}

	private IBitSet addAdjacentPairs(T token, Root<T> root, SortableMemory<Column> thisCols, SortableMemory<Column> partnerCols, final UnaryLeaf partner, final AdjCandidates<Column> adjCandidates, Queue<ColumnPair> adjacentPairs) throws IOException {
                // System.out.println("IBitSet UnaryLeaf.addAdjacentPairs()");
		IBitSet pat = null;
		final int index = adjCandidates.size(); 
		adjCandidates.add(thisCols, columnIndex, partnerCols, partner.columnIndex);
		final int interCard = adjCandidates.getIntersectionCardinality(index);
		if (root.isRequiredZeroBitCount(token, interCard)) {
//					if (root.keepByColumnPairFilter(pair)) {

				//adjacent if |Z(r1)\Z(r2)| = 1 or |Z(r2)\Z(r1)| = 1
				if (adjCandidates.getColumnPos(index).bitValues().cardinality() - interCard == 1 ||
					adjCandidates.getColumnNeg(index).bitValues().cardinality() - interCard == 1) {
					
					atomic_found_adjacents.incrementAndGet();
                                        // System.out.println("Found adjacent rays!: found_adjacents: " + found_adjacents);
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
		return pat;
	}
	@Override
	public IBitSet filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> cols, AdjCandidates<Column> adjCandidates) throws IOException {
		if (filterCutPattern != null && filterCutPattern.isSubSetOf(unionPattern)) {
			IBitSet cutPat = null;
			int len = adjCandidates.size();
			int index = 0;
			while (index < len) {
				boolean remove = false;
				if (adjCandidates.hasSuperSet(index, cols, columnIndex, columnIndex + 1)) {
					remove = true;
				}
				if (remove) {
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

        // atomic implementations
	public long get_atomic_addAdjacentPairs_invocations() {
		return atomic_addAdjacentPairs_invocations.get();
        }

	public long get_atomic_adjacency_tests() {
		return atomic_adjacency_tests.get();
	}

	public long get_atomic_found_adjacents() {
		return atomic_found_adjacents.get();
	}

	public long reset_atomic_addAdjacentPairs_invocations() {
                // System.out.println("entered UnaryLeav.reset_atomic_addAdjacentPairs_invocations()");
		long ret = atomic_addAdjacentPairs_invocations.get();
		atomic_addAdjacentPairs_invocations.set(0);
		return ret;
        }

	public long reset_atomic_adjacency_tests() {
                // System.out.println("entered UnaryLeav.reset_atomic_adjacency_tests()");
		long ret = atomic_adjacency_tests.get();
		atomic_adjacency_tests.set(0);
		return ret;
	}

	public long reset_atomic_found_adjacents() {
                // System.out.println("entered UnaryLeav.reset_atomic_found_adjacents()");
		long ret = atomic_found_adjacents.get();
		atomic_found_adjacents.set(0);
		return ret;
	}
}
