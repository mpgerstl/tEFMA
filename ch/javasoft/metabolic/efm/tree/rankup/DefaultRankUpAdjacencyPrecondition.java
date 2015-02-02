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
package ch.javasoft.metabolic.efm.tree.rankup;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.impl.MinCardinalityAdjacencyPrecondition;

/**
 * Default implementation for {@link AdjacencyPrecondition} for rank updating. 
 * The precondition is tested by counting for the minimum cardinality in the
 * zero set, actually equal to {@link MinCardinalityAdjacencyPrecondition}. But
 * this class additionally stores matrices on {@link #enterIfMet(ColumnHome, AdjEnumModel, RankUpdateToken, int, int, boolean, Node, Node) entering}
 * and removes the matrices on {@link #leave(ColumnHome, AdjEnumModel, RankUpdateToken, int, int, boolean, Node, Node) leaving}.
 */
public class DefaultRankUpAdjacencyPrecondition<T extends RankUpdateToken> implements AdjacencyPrecondition<T> {
	
	//private final EfmModel efmModel;
	private final MinCardinalityAdjacencyPrecondition precondition;
	
	public <Col extends Column, N extends Number> DefaultRankUpAdjacencyPrecondition(ColumnHome<N, Col> columnHome, EfmModel efmModel) {
//		/this.efmModel		= efmModel;	
		this.precondition	= new MinCardinalityAdjacencyPrecondition(efmModel);
	}

	public <Col extends Column, N extends Number> boolean enterIfMet(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) {
		final IBitSet unionCut = precondition.getUnionCutIfMet(columnHome, iterationModel, token, meIsPos, me, other);
		if (unionCut != null) {
			token.addChildRankMatrix(unionCut);
			return true;
		}
		return false;
	}

	public <Col extends Column, N extends Number> void leave(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) {
		token.removeChildRankMatrix();
	}

	public <Col extends Column, N extends Number> boolean isMet(ColumnHome<N,Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int posColIndex, int negColIndex) throws IOException {
		return precondition.isMet(columnHome, iterationModel, token, posColIndex, negColIndex);		
	};
}
