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
package ch.javasoft.metabolic.efm.adj.incore.tree.search;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.adj.incore.tree.AbstractRoot;
import ch.javasoft.metabolic.efm.adj.incore.tree.Node;
import ch.javasoft.metabolic.efm.adj.incore.tree.TreeFactory;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

/**
 * The <tt>SearchRoot</tt> performs the combinatorial adjacency test based on
 * three bit pattern trees.
 */
public class SearchRoot<T /*traversing token*/> extends AbstractRoot<T> {
	
	private final 	Node<T> mZero;
	private final 	int		mRequiredZeroCount;
	
	private final TestMethod mTestMethod;

	//TODO do this nicer (memory!)
	public SearchRoot(Config config, EfmModel model, TreeFactory<T> treeFactory, final int requiredZeroCount, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols) throws IOException {
		super(config, model, treeFactory, posCols, zeroCols, negCols);
		mZero 				= treeFactory.createNode(zeroCols, mSelectiveBits, -1 /*prevSelBitIndex*/, 0, zeroCols.getColumnCount());
		mRequiredZeroCount	= requiredZeroCount;
		mTestMethod 		= SearchRoot.createTestMethod(this, posCols.getColumnCount(), negCols.getColumnCount(), zeroCols.getColumnCount());
	}

	public boolean isRequiredZeroBitCount(T token, int count) {
		return count >= mRequiredZeroCount;
	}
	
	/**
	 * We have 3 trees pos, neg, zero. Each tree is used for filtering the
	 * adjacency candidates. It is better to use the largest tree first since
	 * it might filter out more candidates with little more effort (log n).
	 * This method creates the best filter method regarding the order of the
	 * trees when filtering.
	 */
	private static TestMethod createTestMethod(final SearchRoot<?> root, int posCnt, int negCnt, int zerCnt) {
		if (posCnt > negCnt) {
			if (posCnt > zerCnt) {
				if (negCnt > zerCnt) {
					return new TestMethod() {
						public void filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException {
							if (filterCutPattern != null) filterCutPattern = root.mPos.filterAdjacentPairs(filterCutPattern, posCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mNeg.filterAdjacentPairs(filterCutPattern, negCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mZero.filterAdjacentPairs(filterCutPattern, zeroCols, adjacentPairs);
						}
					};
				}
				else {
					return new TestMethod() {
						public void filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException {
							if (filterCutPattern != null) filterCutPattern = root.mPos.filterAdjacentPairs(filterCutPattern, posCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mZero.filterAdjacentPairs(filterCutPattern, zeroCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mNeg.filterAdjacentPairs(filterCutPattern, negCols, adjacentPairs);
						}
					};
				}
			}
			else {
				return new TestMethod() {
					public void filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException {
						if (filterCutPattern != null) filterCutPattern = root.mZero.filterAdjacentPairs(filterCutPattern, zeroCols, adjacentPairs);
						if (filterCutPattern != null) filterCutPattern = root.mPos.filterAdjacentPairs(filterCutPattern, posCols, adjacentPairs);
						if (filterCutPattern != null) filterCutPattern = root.mNeg.filterAdjacentPairs(filterCutPattern, negCols, adjacentPairs);
					}
				};
			}
		}
		else {
			if (negCnt > zerCnt) {
				if (posCnt > zerCnt) {
					return new TestMethod() {
						public void filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException {
							if (filterCutPattern != null) filterCutPattern = root.mNeg.filterAdjacentPairs(filterCutPattern, negCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mPos.filterAdjacentPairs(filterCutPattern, posCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mZero.filterAdjacentPairs(filterCutPattern, zeroCols, adjacentPairs);
						}
					};
				}
				else {
					return new TestMethod() {
						public void filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException {
							if (filterCutPattern != null) filterCutPattern = root.mNeg.filterAdjacentPairs(filterCutPattern, negCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mZero.filterAdjacentPairs(filterCutPattern, zeroCols, adjacentPairs);
							if (filterCutPattern != null) filterCutPattern = root.mPos.filterAdjacentPairs(filterCutPattern, posCols, adjacentPairs);
						}
					};
				}				
			}
			else {
				return new TestMethod() {
					public void filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException {
						if (filterCutPattern != null) filterCutPattern = root.mZero.filterAdjacentPairs(filterCutPattern, zeroCols, adjacentPairs);
						if (filterCutPattern != null) filterCutPattern = root.mNeg.filterAdjacentPairs(filterCutPattern, negCols, adjacentPairs);
						if (filterCutPattern != null) filterCutPattern = root.mPos.filterAdjacentPairs(filterCutPattern, posCols, adjacentPairs);
					}
				};
			}			
		}
		
	}
	
	public void filterAdjacentPairs(T token, Node<T> nodeA, Node<T> nodeB, IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> candidates) throws IOException {
		mTestMethod.filterAdjacentPairs(filterCutPattern, posCols, zeroCols, negCols, candidates);
	}

}