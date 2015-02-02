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
package ch.javasoft.metabolic.efm.tree.concurrent;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.concurrent.ThreadFinalizer;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyFilter;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;
import ch.javasoft.metabolic.efm.tree.impl.DefaultTreePairTraverser;
import ch.javasoft.metabolic.efm.tree.impl.SubtreePairTraverser;
import ch.javasoft.util.ExceptionUtil;

/**
 * Concurrent implementation for a {@link TreePairTraverser}. The overall 
 * recursion process is split into several jobs, and all subtree traversal jobs
 * are then executed in parallel by all available threads. Note that this method
 * might not balance the execution to all threads as well as 
 * {@link ConcurrentTreePairTraverser}, but the overhead is lower.
 * <p>
 * Adjacency precondition testing and adjacent pair filtering is delegated to an 
 * {@link AdjacencyPrecondition} instance and an {@link AdjacencyFilter} 
 * instance.
 */
public class ConcurrentSubtreePairTraverser<T extends ConcurrentToken> extends DefaultTreePairTraverser<T> {
	
	/**
	 * How many recursion levels should be executed in parallel?
	 * Maximum number of jobs is 4^levels, since every level consists of up to 
	 * four recursive calls. 
	 */
	private static final int MAX_LEVEL_DEPTH = 6;

	private final ThreadFinalizer threadFinalizer;
	
	public ConcurrentSubtreePairTraverser(EfmModel efmModel, AdjacencyPrecondition<T> precondition, AdjacencyFilter<T> filter, final AdjEnumModel itModel, final BitPatternTree posTree, final BitPatternTree negTree) {
		super(efmModel, precondition, filter);
		threadFinalizer = new ThreadFinalizer() {
			public void finalizeCurrentThread() throws Exception {
				itModel.closeForThread();
				posTree.closeForCurrentThread();
				negTree.closeForCurrentThread();
			}
		};
	}
	
	@Override
	public <Col extends Column, N extends Number> void traverse(final ColumnHome<N,Col> columnHome, final AdjEnumModel<Col> iterationModel, final T token, final BitPatternTree posTree, final BitPatternTree negTree) throws IOException {
		final AtomicInteger jobs = new AtomicInteger(1 << (MAX_LEVEL_DEPTH << 1));
		final int threads = token.drainPermits();
		for (int i = 0; i < threads; i++) {
			final Callable<Void> callable = createCallable(columnHome, iterationModel, token, posTree, negTree, MAX_LEVEL_DEPTH, jobs);
			final Thread thread = token.createChildThread(callable, threadFinalizer);
			thread.start();
		}

		//current thread also participates in execution of jobs
		final Callable<Void> callable = createCallable(columnHome, iterationModel, token, posTree, negTree, MAX_LEVEL_DEPTH, jobs);
		try {
			callable.call();
			token.waitForChildThreads();
		}
		catch (Exception e) {
			throw ExceptionUtil.toRuntimeExceptionOr(IOException.class, e); 
		}
	}
	
	private <Col extends Column, N extends Number> Callable<Void> createCallable(final ColumnHome<N,Col> columnHome, final AdjEnumModel<Col> iterationModel, final T token, final BitPatternTree posTree, final BitPatternTree negTree, final int maxLevelDepth, final AtomicInteger jobs) throws IOException {
		return new Callable<Void>() {
			public Void call() throws Exception {
				int job = jobs.decrementAndGet();
				while (job >= 0) {
					final SubtreePairTraverser<T> st = new SubtreePairTraverser<T>(maxLevelDepth, job, ConcurrentSubtreePairTraverser.this);
					st.traverse(columnHome, iterationModel, token, posTree, negTree);
					job = jobs.decrementAndGet();
				}
				return null;
			}
		};
	}
	
}
