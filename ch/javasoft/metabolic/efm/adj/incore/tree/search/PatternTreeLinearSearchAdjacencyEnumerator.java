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
import java.util.Queue;

import ch.javasoft.metabolic.efm.adj.incore.AbstractAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.adj.incore.tree.DefaultTreeFactory;
import ch.javasoft.metabolic.efm.adj.incore.tree.JobScheduleMultiThreadTreeFactory;
import ch.javasoft.metabolic.efm.adj.incore.tree.Root;
import ch.javasoft.metabolic.efm.adj.incore.tree.TreeFactory;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.concurrent.SemaphoreConcurrentToken;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

/**
 * The <code>PatternTreeLinearSearchAdjacencyEnumerator</code> uses candidate
 * narrowing to enumerate adjacent pairs, but performs a linear superset search 
 * for the combinatorial adjacency test.
 */
public class PatternTreeLinearSearchAdjacencyEnumerator extends AbstractAdjacencyEnumerator {

	public static final String NAME = "pattern-tree-linear";

	private int mRequiredZeroCount;
	
	public PatternTreeLinearSearchAdjacencyEnumerator() {
		super();
	}

	public String name() {
		return NAME;
	}

	@Override
	public <Col extends Column, N extends Number> void initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel model) {
		super.initialize(columnHome, config, model);
		mRequiredZeroCount = model.getRequiredCardinality();
	}
	@Override
	public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException {
		if (mModel.getAdjEnumThreads() > 1) {
//	    	TreeFactory<SemaphoreConcurrentToken> fac = new SemIncMultiThreadTreeFactory(threads);
	    	TreeFactory<SemaphoreConcurrentToken> fac = new JobScheduleMultiThreadTreeFactory(mModel);//faster, mainly if cpu-cores > 2
	    	Root<SemaphoreConcurrentToken> root = new LinearSearchRoot<SemaphoreConcurrentToken>(mConfig, mModel, fac, mRequiredZeroCount, posCols, zerCols, negCols);
//	    	TreeFactory<PoolToken> fac = new PoolTreeFactory(threads);
//	    	Root<PoolToken> root = new SearchRoot<PoolToken>(fac, mRequiredZeroCount, posCols, zerCols, negCols);
	    	fac.createTraverser().traverseTree(root, posCols, zerCols, negCols, adjacentPairs);
		}
		else {
    		// normal, 1 thread
    		TreeFactory<Void> fac = new DefaultTreeFactory(mModel);
	    	Root<Void> root = new LinearSearchRoot<Void>(mConfig, mModel, fac, mRequiredZeroCount, posCols, zerCols, negCols);
	    	fac.createTraverser().traverseTree(root, posCols, zerCols, negCols, adjacentPairs);    			
		}
	}
	
}
