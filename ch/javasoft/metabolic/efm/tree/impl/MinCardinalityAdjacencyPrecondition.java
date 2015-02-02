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
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.Node;

/**
 * The <code>MinCardinalityAdjacencyPrecondition</code> uses the minimum
 * requirement of true bits in the zero sets as precondition.
 */
public class MinCardinalityAdjacencyPrecondition implements AdjacencyPrecondition<ConcurrentToken> {
	
	public final int requiredCardinality;

	public MinCardinalityAdjacencyPrecondition(EfmModel model) {
		this.requiredCardinality = model.getRequiredCardinality();
	}

	public <Col extends Column, N extends Number> boolean enterIfMet(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, ConcurrentToken token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) {
		return me.unionPattern().getAndCardinality(other.unionPattern()) >= requiredCardinality;
	}
	
	public <Col extends Column, N extends Number> void leave(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, ConcurrentToken token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) {
		// no resources to free
	}
	
	/**
	 * Returns the intersection of the union patterns, if the precondition is
	 * met, and null otherwise;
	 */
	public <Col extends Column, N extends Number> IBitSet getUnionCutIfMet(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, ConcurrentToken token, boolean meIsPos, Node me, Node other) {
		if (me.unionPattern().getAndCardinality(other.unionPattern()) >= requiredCardinality) {
			return me.unionPattern().getAnd(other.unionPattern());
		}
		return null;
	}

	public <Col extends Column, N extends Number> boolean isMet(ColumnHome<N,Col> columnHome, AdjEnumModel<Col> iterationModel, ConcurrentToken token, int posColIndex, int negColIndex) throws IOException {
		final Col posCol = iterationModel.getMemoryPos().getColumn(posColIndex);
		final Col negCol = iterationModel.getMemoryNeg().getColumn(negColIndex);
		return posCol.bitValues().getAndCardinality(negCol.bitValues()) >= requiredCardinality;		
	};
}
