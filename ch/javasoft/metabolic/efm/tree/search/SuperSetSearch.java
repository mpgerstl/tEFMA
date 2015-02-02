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
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.InterNode;
import ch.javasoft.metabolic.efm.tree.LeafNode;
import ch.javasoft.metabolic.efm.tree.impl.AbstractTreeTraverser;

/**
 * The <code>SuperSetSearch</code> performs the combinatorial test on a bit
 * pattern tree by searching for a super set of the intersection set of the 
 * adjacency candidate zero sets.
 */
public class SuperSetSearch<T> extends AbstractTreeTraverser<T> {
	
	private final IBitSet intersectionSet;
	private final int ignoreColumn;
	
	private volatile PartId 	memoryPartId;
	private volatile boolean	superSetFound;
	
	/**
	 * Constructor for <code>SuperSetSearch</code> with intersection set of 
	 * adjacency candidate zero sets. This constructor should only be used for
	 * the {@link BitPatternTree.Kind#Zero Zero}-Tree, since it does cannot 
	 * contain any of the ancestor zero sets.
	 *  
	 * @param intersectionSet	the intersection set of the adjacency candidate
	 * 							zero sets
	 */
	public SuperSetSearch(IBitSet intersectionSet) {		
		this(intersectionSet, -1);
	}
	/**
	 * Constructor for <code>SuperSetSearch</code> with intersection set of 
	 * adjacency candidate zero sets and column index to ignore when searching.
	 * This constructor should be used for the 
	 * {@link BitPatternTree.Kind#Pos Pos} and the 
	 * {@link BitPatternTree.Kind#Neg Neg} tree. The index {@code ignoreColumn}
	 * specifies the column index to ignore, since it is one of the the ancestor 
	 * zero sets.
	 * 
	 * @param intersectionSet	the intersection set of the adjacency candidate
	 * 							zero sets
	 * @param ignoreColumn		the column index to ignore, the index of the
	 * 							ancestor zero set
	 */
	public SuperSetSearch(IBitSet intersectionSet, int ignoreColumn) {		
		this.intersectionSet	= intersectionSet;
		this.ignoreColumn		= ignoreColumn;
	}

	/**
	 * Returns {@code true} if a superset has been found, and {@code false} 
	 * otherwise. If no tree has been traversed, {@code false} is returned.
	 */
	public boolean hasSuperSetFound() {
                // System.out.println("in SuperSetSearch<T>.hasSuperSetFound");
		return superSetFound;
	}
	@Override
	public <Col extends Column, N extends Number> void traverse(ColumnHome<N,Col> columnHome, AdjEnumModel<Col> iterationModel, T token, BitPatternTree tree) throws IOException {
		memoryPartId = tree.kind().toPartId();
		super.traverse(columnHome, iterationModel, token, tree);
	}
	@Override
	public <Col extends Column, N extends Number> boolean traverseI(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, InterNode node) throws IOException {
		if (intersectionSet.isSubSetOf(node.unionPattern())) {
			return super.traverseI(columnHome, iterationModel, token, node);
		}
		return true;
	}
	public <Col extends Column, N extends Number> boolean traverseL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, LeafNode node) throws IOException {
		if (intersectionSet.isSubSetOf(node.unionPattern())) {
			final int start = node.getLeafColumnStart();
			final int end = node.getLeafColumnEnd();
			final SortableMemory<Col> mem = iterationModel.getMemory(memoryPartId);
			for (int i = start; i < end; i++) {
				if (i != ignoreColumn) {
					final IBitSet superCandidate = mem.getColumn(i).bitValues();
					if (intersectionSet.isSubSetOf(superCandidate)) {
						superSetFound = true;
						return false;
					}
				}
			}
		}
		return true;
	}

}
