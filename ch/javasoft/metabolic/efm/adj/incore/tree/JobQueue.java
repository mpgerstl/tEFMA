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
package ch.javasoft.metabolic.efm.adj.incore.tree;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.concurrent.ReleasePolicy;
import ch.javasoft.metabolic.efm.concurrent.TimeoutWaitingReleasePolicy;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * Tree recursions are split up into jobs, each job is an instance of
 * {@link Runnable}. The different threads can then invoke jobs.
 */
public class JobQueue<T extends ConcurrentToken> {

//	private final int threadCount;
//	private final Semaphore semaphore;
	private final T token;
	private final Root<T> root;
	private final SortableMemory<Column> posCols;
	private final SortableMemory<Column> zeroCols;
	private final SortableMemory<Column> negCols;
//	private final Node<T> nodeA;
//	private final Node<T> nodeB;
	private final boolean nodeAIsPos;
	private final Queue<ColumnPair> adjacentPairs;	
	private final ConcurrentLinkedQueue<Runnable> queue;
	
	public JobQueue(final Config config, final int threadCount, final T token, final Root<T> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<T> nodeA, final Node<T> nodeB, final boolean nodeAIsPos, final Queue<ColumnPair> adjacentPairs) {
//		this.config			= config;
//		this.threadCount 	= threadCount;
//		this.semaphore		= semaphore;
		this.token			= token;
		this.root			= root;
		this.posCols		= posCols;
		this.zeroCols		= zeroCols;
		this.negCols		= negCols;
//		this.nodeA			= nodeA;
//		this.nodeB			= nodeB;
		this.nodeAIsPos		= nodeAIsPos;
		this.adjacentPairs	= adjacentPairs;
		this.queue 			= getJobQueue(nodeA, nodeB);
	}
	
	public static <T extends ConcurrentToken, I> JobQueue<T> createQueue(final Config config, final T token, final Root<T> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<T> nodeA, final Node<T> nodeB, final boolean nodeAIsPos, final Queue<ColumnPair> adjacentPairs) {
		final int threadCount = token.drainPermits();
		if (threadCount > 0) {
//			System.out.print(threadCount);
			final JobQueue<T> queue = new JobQueue<T>(config, threadCount, token, root, posCols, zeroCols, negCols, nodeA, nodeB, nodeAIsPos, adjacentPairs);
			queue.startChildThreads(config, threadCount, token, root, posCols, zeroCols, negCols, nodeA, nodeB, nodeAIsPos, adjacentPairs);
			return queue;
		}
		else return null;
	}
	
	/**
	 * How many recursion levels should be converted to jobs?
	 * Maximum number of jobs is 4^levels, since every level consists of up to 
	 * four recursive calls. 
	 */
	public static final int MAX_LEVEL_DEPTH = 6; 

	private <I> void startChildThreads(final Config config, final int threadCount, final T token, final Root<T> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<T> nodeA, final Node<T> nodeB, final boolean nodeAIsPos, final Queue<ColumnPair> adjacentPairs) {
		final ReleasePolicy releasePolicy;
//		releasePolicy	= new ImmediateReleasePolicy();
//		releasePolicy	= new WaitForHalfReleasePolicy();
		releasePolicy	= new TimeoutWaitingReleasePolicy();
		releasePolicy.initialize(config, 4 << MAX_LEVEL_DEPTH, threadCount);
		for (int i = 0; i < threadCount; i++) {
			token.createChildThread(new Callable<Void>() {
				public Void call() throws Exception {
					Runnable job = queue.poll();
					while (job != null) {
						job.run();
						job = queue.poll();
					}
					releasePolicy.releasePermit(token);
					return null;//void
				}
		}, null).start();
		}
	}
	
	public void execParentThread() {
		Runnable job = queue.poll();
		while (job != null) {
			job.run();
			job = queue.poll();
		}
	}
	
	/**
	 * Returns the job queue with recursive calls up to the given level depth
	 */
	private ConcurrentLinkedQueue<Runnable> getJobQueue(final Node<T> nodeA, final Node<T> nodeB) {
		final ConcurrentLinkedQueue<Runnable> jobs = new ConcurrentLinkedQueue<Runnable>();
		addToJobQueue(0, jobs, nodeA, nodeB);
		return jobs;
	}
	
	private void addToJobQueue(int level, final ConcurrentLinkedQueue<Runnable> jobs, final Node<T> nodeA, final Node<T> nodeB) {
		if (level < MAX_LEVEL_DEPTH) {
			level++;
			if (nodeA instanceof InterNode) {
				if (root.enterIfCandidates(token, nodeA, nodeB)) {
					final InterNode<T> interA = (InterNode<T>)nodeA;
					if (nodeB instanceof InterNode) {
						final InterNode<T> interB = (InterNode<T>)nodeB;
						addToJobQueue(level, jobs, interA.child0, interB.child0);
						addToJobQueue(level, jobs, interA.child1, interB.child0);
						addToJobQueue(level, jobs, interA.child0, interB.child1);
						addToJobQueue(level, jobs, interA.child1, interB.child1);							
					}
					else {
						addToJobQueue(level, jobs, interA.child0, nodeB);
						addToJobQueue(level, jobs, interA.child1, nodeB);							
					}
					root.leave(token, nodeA, nodeB);
				}
			}
			else {
				if (nodeB instanceof InterNode) {
					if (root.enterIfCandidates(token, nodeA, nodeB)) {
						final InterNode<T> interB = (InterNode<T>)nodeB;
						addToJobQueue(level, jobs, nodeA, interB.child0);
						addToJobQueue(level, jobs, nodeA, interB.child1);						
						root.leave(token, nodeA, nodeB);
					}
				}
				else {
					addJobToQueue(jobs, nodeA, nodeB);
				}
			}
		}
		else {
			addJobToQueue(jobs, nodeA, nodeB);
		}
	}
	private void addJobToQueue(final ConcurrentLinkedQueue<Runnable> jobs, final Node<T> nodeA, final Node<T> nodeB) {
		jobs.add(new Runnable() {
			public void run() {
				try {
					nodeA.addAdjacentPairs(token, root, posCols, zeroCols, negCols, nodeB, nodeAIsPos, adjacentPairs);
				} 
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}	

}
