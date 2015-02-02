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
package ch.javasoft.metabolic.efm.adj;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.ModelPersister;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;
import ch.javasoft.metabolic.efm.rankup.modpi.ModIntPrimeMatrixFactory;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.TreeMemAdjEnum;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;
import ch.javasoft.metabolic.efm.tree.concurrent.ConcurrentTreePairTraverser;
import ch.javasoft.metabolic.efm.tree.rankup.DefaultRankUpAdjacencyFilter;
import ch.javasoft.metabolic.efm.tree.rankup.DefaultRankUpAdjacencyPrecondition;
import ch.javasoft.metabolic.efm.util.PreconditionUtil;

/**
 * The <code>AbstractModIntPrimeAdjEnum</code> uses rank updating with 
 * integer primes, such that each rank computation operation fits into a 32 bit 
 * integer register.
 * <p>
 * Subclasses determine whether the bit pattern trees are stored in-core or
 * out-of-core.
 */
abstract public class AbstractModIntPrimeAdjEnum extends TreeMemAdjEnum<RankUpdateToken> {

	protected AbstractModIntPrimeAdjEnum(String name) {
		super(name);
	}
	
	public ModelPersister getModelPersister() {
		throw new RuntimeException("subclass must override to support distributed computation");
//		return new FileBasedModelPersister();
//		return new ClientServerModelPersister();
	}
	
	@Override
	protected <Col extends Column, N extends Number> RankUpdateToken createToken(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree posTree, BitPatternTree negTree, ProgressAggregator progress) throws IOException {
		final RankUpdateToken token = progress == null ?
				new RankUpdateToken(getEfmModel()) : new RankUpdateToken(getEfmModel(), progress);
		if (token.tryAcquirePermit()) {
			final IBitSet unionCutPattern = posTree.root().unionPattern().getAnd(negTree.root().unionPattern());
			token.addRootRankMatrix(unionCutPattern);
			return token;
		}
		throw new RuntimeException("internal error: could not acquire permit to start main thread");
	}
	@Override
	protected <Col extends Column, N extends Number> void releaseToken(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree posTree, BitPatternTree negTree, RankUpdateToken token) {
		token.removeRootRankMatrix();
		token.releasePermit();
		try {
			token.waitForChildThreads();
		} 
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected <Col extends Column, N extends Number> TreePairTraverser<RankUpdateToken> createTreeTraverser(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, BitPatternTree posTree, BitPatternTree negTree, AdjacencyPrecondition<RankUpdateToken> precondition) {
		AdjacencyPrecondition<RankUpdateToken> precond = new DefaultRankUpAdjacencyPrecondition<RankUpdateToken>(columnHome, getEfmModel());
		if (precondition != null) {
			precond = PreconditionUtil.and(precondition, precond);
		}
//		return new ConcurrentSubtreePairTraverser<RankUpdateToken>(
//			getEfmModel(), precond,
//			new DefaultRankUpAdjacencyFilter<RankUpdateToken>(columnHome, getEfmModel(), new ModIntPrimeMatrixFactory(getEfmModel())),
//			iterationModel, posTree, negTree
//		);
		return new ConcurrentTreePairTraverser<RankUpdateToken>(
			getEfmModel(), precond,
			new DefaultRankUpAdjacencyFilter<RankUpdateToken>(columnHome, getEfmModel(), new ModIntPrimeMatrixFactory(getEfmModel())),
			iterationModel, posTree, negTree
		);
//		return new DefaultTreeTraverser<RankUpdateToken>(
//			getEfmModel(), 
//			new DefaultRankUpAdjacencyPrecondition<RankUpdateToken>(columnHome, getEfmModel()),
//			new DefaultRankUpAdjacencyFilter<RankUpdateToken>(columnHome, getEfmModel(), new ModIntPrimeMatrixFactory(getEfmModel()))
//		);
	}

}
