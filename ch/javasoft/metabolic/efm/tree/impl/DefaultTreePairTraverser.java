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
package ch.javasoft.metabolic.efm.tree.impl;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyFilter;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.InterNode;
import ch.javasoft.metabolic.efm.tree.LeafNode;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;

/**
 * Default implementation for a {@link TreePairTraverser}. The important stuff, such 
 * as adjacency precondition testing and adjacent pair filtering, is delegated 
 * to an {@link AdjacencyPrecondition} instance and an {@link AdjacencyFilter}
 * instance.
 */
public class DefaultTreePairTraverser<T extends ConcurrentToken> extends AbstractTreePairTraverser<T> {
	
	protected final EfmModel					efmModel;
	protected final AdjacencyPrecondition<T> 	precondition;
	protected final AdjacencyFilter<T>			filter;
	
	public DefaultTreePairTraverser(EfmModel efmModel, AdjacencyPrecondition<T> precondition, AdjacencyFilter<T> filter) {
		this.efmModel		= efmModel;
		this.precondition	= precondition;
		this.filter			= filter;
	}
	
	public <Col extends Column, N extends Number> void traverseIN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, Node other) throws IOException {
		if (precondition.enterIfMet(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other)) {
			other.traverseI(this, columnHome, iterationModel, token, otherDepth, myDepth, !meIsPos, me);
			precondition.leave(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
		}
		else {
			token.notifyProgressIncrement(myDepth + otherDepth);
		}
	}
	public <Col extends Column, N extends Number> void traverseLN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, Node other) throws IOException {
		if (precondition.enterIfMet(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other)) {
			other.traverseL(this, columnHome, iterationModel, token, otherDepth, myDepth, !meIsPos, me);
			precondition.leave(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
		}
		else {
			token.notifyProgressIncrement(myDepth + otherDepth);
		}
	}
	public <Col extends Column, N extends Number> void traverseII(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, InterNode other) throws IOException {
		traverseNN(columnHome, iterationModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.left(), other.left());
		traverseNN(columnHome, iterationModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.left(), other.right());
		traverseNN(columnHome, iterationModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.right(), other.left());
		traverseNN(columnHome, iterationModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.right(), other.right());
	}
	public <Col extends Column, N extends Number> void traverseIL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, LeafNode other) throws IOException {
		traverseLN(columnHome, iterationModel, token, otherDepth, myDepth + 1, !meIsPos, other, me.left());
		traverseLN(columnHome, iterationModel, token, otherDepth, myDepth + 1, !meIsPos, other, me.right());
	}
	public <Col extends Column, N extends Number> void traverseLL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, LeafNode other) throws IOException {
		final SortableMemory<Col> myCols, otherCols;
		if (meIsPos) {
			myCols 		= iterationModel.getMemoryPos();
			otherCols	= iterationModel.getMemoryNeg();
		}
		else {
			myCols 		= iterationModel.getMemoryNeg();
			otherCols	= iterationModel.getMemoryPos();
		}

		final int meS = me.getLeafColumnStart();
		final int meE = me.getLeafColumnEnd();
		final int otS = other.getLeafColumnStart();
		final int otE = other.getLeafColumnEnd();
		
		final AdjCandidates<Col> adjCandidates = new AdjCandidates<Col>(
			iterationModel.getMemoryPos(), iterationModel.getMemoryNeg(),
			(meE - meS) * (otE - otS));
		
		IBitSet commonPattern = null;
		for (int i = meS; i < meE; i++) {
			for (int j = otS; j < otE; j++) {
				if (precondition.isMet(columnHome, iterationModel, token, meIsPos ? i : j, meIsPos ? j : i)) {
					final int index = adjCandidates.size();
					adjCandidates.add(myCols, i, otherCols, j);
					final IBitSet inter = adjCandidates.getIntersection(index);

					//seems to be a bit faster with the following test
					//adjacent if |Z(r1)\Z(r2)| = 1 or |Z(r2)\Z(r1)| = 1
					final int icard = inter.cardinality();
					final int pcard = adjCandidates.getColumnPos(index).bitValues().cardinality();
					final int ncard = adjCandidates.getColumnNeg(index).bitValues().cardinality();
					if (pcard - icard == 1 || ncard - icard == 1) {
						adjCandidates.appendLastColumn(columnHome, efmModel, iterationModel);
					}
					else {
						if (commonPattern == null) commonPattern = inter;
						else commonPattern.and(inter);
					}
				}
			}
		}
		if (filter.filter(columnHome, iterationModel, token, adjCandidates, commonPattern)) {
			adjCandidates.appendNewColumns(columnHome, efmModel, iterationModel);
		}
		token.notifyProgressIncrement(myDepth + otherDepth);
	}
	
}
