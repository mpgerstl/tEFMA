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
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrixFactory;
import ch.javasoft.metabolic.efm.tree.AdjacencyFilter;

/**
 * Default implementation for {@link AdjacencyFilter} for rank updating. The
 * candidates are filtered by computing matrix ranks using rank updating. If
 * more than a certain {@link #COMMON_TEST_THRESHOLD threshold} pairs are to
 * be filtered, one common extra rank test is performed to possibly exclude all
 * candidates at once.
 */
public class DefaultRankUpAdjacencyFilter<T extends RankUpdateToken> implements AdjacencyFilter<T> {
	
	private final EfmModel efmModel;
	private final PreprocessedMatrixFactory factory;
	
	public <Col extends Column, N extends Number> DefaultRankUpAdjacencyFilter(ColumnHome<N, Col> columnHome, EfmModel efmModel, PreprocessedMatrixFactory factory) {
		this.efmModel	= efmModel;	
		this.factory	= factory;
	}

	public static final int COMMON_TEST_THRESHOLD = 3;//(incl) 3 seems to be best

	public <Col extends Column, N extends Number> boolean filter(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, AdjCandidates<Col> candidates, IBitSet commonPattern) throws IOException {
		int ind = 0;
		int len = candidates.size();
		if (ind < len) {
			if (len - ind >= COMMON_TEST_THRESHOLD) {
				IBitSet filterUnionPattern = candidates.getIntersection(0);
				for (int i = 1; i < len; i++) {
					filterUnionPattern.or(candidates.getIntersection(i));
				}
				if (!hasRequiredRank(token, commonPattern, filterUnionPattern)) {
					candidates.removeAll();
					return false;
				}
//				cntRankCommonYes++;
			}
			while (ind < len) {
				if (hasRequiredRank(token, commonPattern, candidates.getIntersection(ind))) {
//					cntRankIndividualYes++;
					ind++;
				}
				else {
//					cntRankIndividualNo++;
					len--;
					if (ind != len) {
						candidates.swap(ind, len);
					}
					candidates.removeLast();
				}
			}
		}
		return candidates.size() != 0;
	}

	protected boolean hasRequiredRank(T token, IBitSet nodePattern, IBitSet intersectionSet) {
		final PreprocessableMatrix mx = token.getRankMatrix();
		return mx.hasRequiredRank(token, efmModel, factory, intersectionSet);
	}

}
