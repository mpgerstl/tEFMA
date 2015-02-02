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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.concurrent.ThreadFinalizer;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyFilter;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.InterNode;
import ch.javasoft.metabolic.efm.tree.LeafNode;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;
import ch.javasoft.metabolic.efm.tree.impl.DefaultTreePairTraverser;

/**
 * Concurrent implementation for a {@link TreePairTraverser}. At every recursion
 * level, the current thread tries to acquire permits to start new threads. If
 * it permits are available, one or multiple new threads are started, and the
 * next recursions are executed in parallel.
 * <p>
 * Currently, parallelization might occur in both recursion methods, that is, 
 * in {@link #traverseII(ColumnHome, AdjEnumModel, ConcurrentToken, int, int, boolean, InterNode, InterNode) traversII(..)}
 * and
 * {@link #traverseIL(ColumnHome, AdjEnumModel, ConcurrentToken, int, int, boolean, InterNode, LeafNode) traverseIL(..)}.
 * The parallelization is handled by {@link JobQueue}, which collects recursions
 * to a certain depth, and creates a job for each recursive call. The jobs are
 * added to the queue and processed concurrently.
 * <p>
 * Adjacency precondition testing and adjacent pair filtering is delegated to an 
 * {@link AdjacencyPrecondition} instance and an {@link AdjacencyFilter} 
 * instance.
 */
public class ConcurrentTreePairTraverser<T extends ConcurrentToken> extends DefaultTreePairTraverser<T> {
	
	private final ThreadFinalizer threadFinalizer;
	
	public ConcurrentTreePairTraverser(EfmModel efmModel, AdjacencyPrecondition<T> precondition, AdjacencyFilter<T> filter, final AdjEnumModel itModel, final BitPatternTree posTree, final BitPatternTree negTree) {
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
	public <Col extends Column, N extends Number> void traverseII(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, InterNode other) throws IOException {
		if (!JobQueue.execThreaded(efmModel.getConfig(), columnHome, token, myDepth, otherDepth, this, itModel, threadFinalizer, meIsPos, me, other)) {	
			traverseNN(columnHome, itModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.left(), other.left());
			traverseNN(columnHome, itModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.left(), other.right());
			traverseNN(columnHome, itModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.right(), other.left());
			traverseNN(columnHome, itModel, token, myDepth + 1, otherDepth + 1, meIsPos, me.right(), other.right());
			checkAndNotifyProgress(token, myDepth + otherDepth, myDepth + otherDepth + 2);
		}
	}
	@Override
	public <Col extends Column, N extends Number> void traverseIL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, LeafNode other) throws IOException {
		if (!JobQueue.execThreaded(efmModel.getConfig(), columnHome, token, myDepth, otherDepth, this, itModel, threadFinalizer, meIsPos, me, other)) {		
			traverseLN(columnHome, itModel, token, otherDepth, myDepth + 1, !meIsPos, other, me.left());
			traverseLN(columnHome, itModel, token, otherDepth, myDepth + 1, !meIsPos, other, me.right());
			checkAndNotifyProgress(token, myDepth + otherDepth, myDepth + otherDepth + 1);
		}
	}
	
	private void checkAndNotifyProgress(T token, int curProgress, int childProgress) throws IOException {
		if (!token.isProgressIncrementNotifiable(childProgress)) {
			token.notifyProgressIncrement(curProgress);
		}
	}
	
}
