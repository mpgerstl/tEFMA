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
package ch.javasoft.metabolic.efm.tree.search;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyFilter;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;

/**
 * Default implementation for {@link AdjacencyFilter} for superset updating. The
 * candidates are filtered by searching for a superset of the candidate zero
 * set in all three trees.
 */
public class DefaultSearchAdjacencyFilter<T extends ConcurrentToken> implements AdjacencyFilter<T> {
	
	private final BitPatternTree treePos, treeNeg, treeZer;
	
	public <Col extends Column, N extends Number> DefaultSearchAdjacencyFilter(BitPatternTree treePos, BitPatternTree treeNeg, BitPatternTree treeZer) {
		this.treePos	= treePos;
		this.treeNeg	= treeNeg;
		this.treeZer	= treeZer;
	}

	public <Col extends Column, N extends Number> boolean filter(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, AdjCandidates<Col> candidates, IBitSet commonPattern) throws IOException {
		int ind = 0;
		int len = candidates.size();
		if (ind < len) {
			while (ind < len) {
				SuperSetSearch<T> search;
				boolean remove;

				final IBitSet inter = candidates.getIntersection(ind);
				search = new SuperSetSearch<T>(inter);
				search.traverse(columnHome, iterationModel, token, treeZer);
				remove = search.hasSuperSetFound();
				if (!remove) {
					search = new SuperSetSearch<T>(inter, candidates.getColumnIndexPos(ind));
					search.traverse(columnHome, iterationModel, token, treePos);
					remove = search.hasSuperSetFound();
					if (!remove) {
						search = new SuperSetSearch<T>(inter, candidates.getColumnIndexNeg(ind));
						search.traverse(columnHome, iterationModel, token, treeNeg);
						remove = search.hasSuperSetFound();
					}
				}
				if (remove) {
					len--;
					if (ind != len) {
						candidates.swap(ind, len);
					}
					candidates.removeLast();
				}
				else {
					ind++;
				}
			}
		}
		return candidates.size() != 0;
	}


}
