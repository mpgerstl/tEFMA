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
 * The <tt>SearchRoot</tt> performs the combinatorial adjacency test with a
 * linear search of the superset in all three lists.
 */
public class LinearSearchRoot<T /*traversing token*/> extends AbstractRoot<T> {
	
	private final int mRequiredZeroCount;
	
	//TODO do this nicer (memory!)
	public LinearSearchRoot(Config config, EfmModel model, TreeFactory<T> treeFactory, final int requiredZeroCount, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols) throws IOException {
		super(config, model, treeFactory, posCols, zeroCols, negCols);
		mRequiredZeroCount = requiredZeroCount;
	}

	public boolean isRequiredZeroBitCount(T token, int count) {
		return count >= mRequiredZeroCount;
	}
	
	public void filterAdjacentPairs(T token, Node<T> nodeA, Node<T> nodeB, IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> candidates) throws IOException {
		int len = candidates.size();
		int index = 0;
		while (index < len) {
			if (hasSuperSet(candidates, index, zeroCols) ||
				hasSuperSet(candidates, index, posCols) ||
				hasSuperSet(candidates, index, negCols)
			) {
				len--;
				if (index != len) {
					candidates.swap(index, len);
				}
				candidates.removeLast();						
			}
			else {
				index++;
			}
		}
	}
	
	private boolean hasSuperSet(AdjCandidates<Column> candidates, int pairIndex, SortableMemory<Column> cols) throws IOException {
                // System.out.println("in LinearSearchRoot.hasSuperSet()");
		return candidates.hasSuperSet(pairIndex, cols, 0, cols.getColumnCount());
	}

}
