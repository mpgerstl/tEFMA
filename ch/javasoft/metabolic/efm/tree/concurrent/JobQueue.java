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
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.concurrent.ReleasePolicy;
import ch.javasoft.metabolic.efm.concurrent.ThreadFinalizer;
import ch.javasoft.metabolic.efm.concurrent.TimeoutWaitingReleasePolicy;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.InterNode;
import ch.javasoft.metabolic.efm.tree.LeafNode;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;
import ch.javasoft.util.ExceptionUtil;

/**
 * Tree recursions are split up into jobs, each job is an instance of
 * {@link Runnable}. Multiple threads can then invoke the jobs.
 */
public class JobQueue<T extends ConcurrentToken> {
		
	private final TreePairTraverser<T> 					traverser;
	private final T 									token;
	private final ConcurrentLinkedQueue<Callable<Void>> queue;
	
	public JobQueue(TreePairTraverser<T> traverser, T token) {
		this.traverser	= traverser;
		this.token		= token;
		this.queue 		= new ConcurrentLinkedQueue<Callable<Void>>();
	}
	
	/**
	 * Returns true if the given nodes are traversed by this queue, and false
	 * if not. If the latter is the case, the caller has to traverse the nodes.
	 */
	public static <Tok extends ConcurrentToken, Col extends Column, N extends Number> boolean execThreaded(final Config config, ColumnHome<N, Col> columnHome, Tok token, int myDepth, int otherDepth, TreePairTraverser<Tok> traverser, AdjEnumModel<Col> itModel, ThreadFinalizer threadFinalizer, boolean meIsPos, Node me, Node other) throws IOException {
		final int threadCount = token.drainPermits();
		if (threadCount > 0) {
//			System.out.print(threadCount);
			final JobQueue<Tok> queue = new JobQueue<Tok>(traverser, token);
			queue.addJobsToQueue(columnHome, itModel, token, myDepth, otherDepth, meIsPos, me, other);
			try {
				queue.exec(config, threadCount, threadFinalizer);
			}
			catch (Exception e) {
				throw ExceptionUtil.toRuntimeExceptionOr(IOException.class, e);
			}
			return true;
		}
		else return false;
	}
	
	private <I> void exec(final Config config, final int threadCount, ThreadFinalizer threadFinalizer) throws Exception {
		startChildThreads(config, threadCount, threadFinalizer);
		execParentThread();
	}
	private <I> void startChildThreads(final Config config, final int threadCount, ThreadFinalizer threadFinalizer) {
		final ReleasePolicy releasePolicy;
//		releasePolicy	= new ImmediateReleasePolicy();
//		releasePolicy	= new WaitForHalfReleasePolicy();
		releasePolicy	= new TimeoutWaitingReleasePolicy();
		releasePolicy.initialize(config, 4 << new Level().maxlevelDepth, threadCount);
		for (int i = 0; i < threadCount; i++) {
			token.createChildThread(new Callable<Void>() {
				public Void call() throws Exception {
					execQueuedJobsByCurrentThread();
					releasePolicy.releasePermit(token);
					return null;//void
				}
			}, threadFinalizer).start();
		}
	}
	
	private void execQueuedJobsByCurrentThread() throws Exception {
		Callable<Void> job = queue.poll();
		while (job != null) {
			job.call();
			job = queue.poll();
		}
	}
	
	private void execParentThread() throws Exception {
		execQueuedJobsByCurrentThread();
	}
	
	private static class Level {
		/**
		 * How many recursion levels should be converted to jobs?
		 * Maximum number of jobs is 4^levels, since every level consists of up to 
		 * four recursive calls. 
		 */
		final int maxlevelDepth = 6;
		//current level
		int level;		
	}
	private class JobCollector implements TreePairTraverser<Level> {
		public <Col extends Column, N extends Number> void traverse(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, Level token, BitPatternTree posTree, BitPatternTree negTree) throws IOException {
			posTree.root().traverseN(this, columnHome, iterationModel, token, 0, 0, true, negTree.root());
		}
		public <C extends Column, N extends Number> void traverseNN(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, Level levelToken, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) throws IOException {
			me.traverseN(this, columnHome, itModel, levelToken, myDepth, otherDepth, meIsPos, other);
		}
		public <C extends Column, N extends Number> void traverseIN(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, Level levelToken, int myDepth, int otherDepth, boolean meIsPos, InterNode me, Node other) throws IOException {
			other.traverseI(this, columnHome, itModel, levelToken, myDepth, otherDepth, !meIsPos, me);
		}
		public <C extends Column, N extends Number> void traverseLN(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, Level levelToken, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, Node other) throws IOException {
			other.traverseL(this, columnHome, itModel, levelToken, myDepth, otherDepth, !meIsPos, me);
		}
		public <C extends Column, N extends Number> void traverseII(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, Level levelToken, int myDepth, int otherDepth, boolean meIsPos, InterNode me, InterNode other) throws IOException {
			if (levelToken.level < levelToken.maxlevelDepth) {
				levelToken.level++;
				traverseNN(columnHome, itModel, levelToken, myDepth + 1, otherDepth + 1, meIsPos, me.left(), other.left());
				traverseNN(columnHome, itModel, levelToken, myDepth + 1, otherDepth + 1, meIsPos, me.left(), other.right());
				traverseNN(columnHome, itModel, levelToken, myDepth + 1, otherDepth + 1, meIsPos, me.right(), other.left());
				traverseNN(columnHome, itModel, levelToken, myDepth + 1, otherDepth + 1, meIsPos, me.right(), other.right());
				checkAndNotifyProgress(token, myDepth + otherDepth, myDepth + otherDepth + 2);
				levelToken.level--;
			}
			else {
				addJobToQueue(columnHome, traverser, itModel, token, myDepth, otherDepth, meIsPos, me, other);
			}
		}
		public <C extends Column, N extends Number> void traverseIL(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, Level levelToken, int myDepth, int otherDepth, boolean meIsPos, InterNode me, LeafNode other) throws IOException {
			if (levelToken.level < levelToken.maxlevelDepth) {
				levelToken.level++;
				traverseLN(columnHome, itModel, levelToken, otherDepth, myDepth + 1, !meIsPos, other, me.left());
				traverseLN(columnHome, itModel, levelToken, otherDepth, myDepth + 1, !meIsPos, other, me.right());
				checkAndNotifyProgress(token, myDepth + otherDepth, myDepth + otherDepth + 1);
				levelToken.level--;
			}
			else {
				addJobToQueue(columnHome, traverser, itModel, token, myDepth, otherDepth, meIsPos, me, other);
			}
		}
		public <C extends Column, N extends Number> void traverseLL(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, Level levelToken, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, LeafNode other) throws IOException {
			addJobToQueue(columnHome, traverser, itModel, token, myDepth, otherDepth, meIsPos, me, other);
		}		
	}
		
	protected <C extends Column, N extends Number> void addJobsToQueue(ColumnHome<N, C> columnHome, AdjEnumModel<C> itModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) throws IOException {
		new JobCollector().traverseNN(columnHome, itModel, new Level(), myDepth, otherDepth, meIsPos, me, other);
	}
	private <C extends Column, N extends Number> void addJobToQueue(final ColumnHome<N, C> columnHome, final TreePairTraverser<T> traverser, final AdjEnumModel<C> itModel, final T token, final int myDepth, final int otherDepth, final boolean meIsPos, final Node me, final Node other) {
		queue.add(new Callable<Void>() {
			public Void call() throws IOException {
				me.traverseN(traverser, columnHome, itModel, token, myDepth, otherDepth, meIsPos, other);
				return null;//void
			}
		});
	}
	private void checkAndNotifyProgress(final T token, final int curProgress, int childProgress) {
		if (!token.isProgressIncrementNotifiable(childProgress)) {
			queue.add(new Callable<Void>() {
				public Void call() throws Exception {
					token.notifyProgressIncrement(curProgress);
					return null;//void
				}
			});
		}
	}
	

}
