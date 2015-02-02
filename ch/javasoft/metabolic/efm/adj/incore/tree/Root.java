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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

public interface Root<T /*traversing token*/> {
	/**
	 * Returns the root for the tree containing the negative columns that are
	 * discarded after this iteration step.
	 */
	Node<T> neg();
	/**
	 * Returns the root for the tree containing the positive columns that are
	 * retained after this iteration step.
	 */
	Node<T> pos();
	
	/**
	 * Filters the adjacency candidates contained in <code>adjacentPairs</code>
	 * and keeps only those that pass the adjacency test.
	 * 
	 * @param token				the traversing token
	 * @param nodeA				the first leaf node, source of columns used to 
	 * 							create the adjacency candidates
	 * @param nodeB				the second leaf node, source of columns used to 
	 * 							create the adjacency candidates
	 * @param filterCutPattern	the pattern representing the common bits of all
	 * 							adjacency candidates
	 * @param posCols			positive columns that are retained after this
	 * 							iteration step, but not lying in the hyperplane
	 * @param zeroCols			zero columns that are retained after this
	 * 							iteration step, lying in the hyperplane
	 * @param negCols			negative columns that are discarded after this
	 * 							iteration step, not lying in the hyperplane
	 * @param adjacentPairs		the candidate pairs, possibly adjacent
	 * @throws IOException	if an i/o exception occurs, for instance since
	 * 						columns are stored in files
	 */
	void filterAdjacentPairs(T token, Node<T> nodeA, Node<T> nodeB, IBitSet filterCutPattern, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, AdjCandidates<Column> adjacentPairs) throws IOException;
	
	/**
	 * Returns true if this recursion level should be entered, and false if the
	 * subtrees specified by <code>nodeA</code> and <code>nodeB</code> cannot
	 * bear adjacent rays. After the recursion, 
	 * {@link #leave(Object, Node, Node) leave(..)} must be called to free 
	 * resources that might have been allocated. If this method returns false,
	 * <code>leave(..)</code> must not be called. 
	 * 
	 * @param token		the traversing token
	 * @param nodeA		node of the first tree to traverse
	 * @param nodeB		node of the second tree to traverse
	 * @return true if traversing should be continued
	 */
	boolean enterIfCandidates(T token, Node<T> nodeA, Node<T> nodeB);
	/**
	 * Leave this traversing step, counterpart to 
	 * {@link #enterIfCandidates(Object, Node, Node)}. Frees resources that 
	 * might have been allocated for this recursion level.
	 *  
	 * @param token		the traversing token
	 * @param nodeA		node of the first tree to traverse
	 * @param nodeB		node of the second tree to traverse
	 */
	void leave(T token, Node<T> nodeA, Node<T> nodeB);
	
	/**
	 * Returns true if the given <code>count</code> is at least the minimum 
	 * required zero bit count needed for adjacency
	 * 
	 * @param token		the traversing token
	 * @param count		the count to test
	 * @return true if <code>count</code> is sufficiently large
	 */
	boolean isRequiredZeroBitCount(T token, int count);
}
