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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

public class ThreadPoolToken implements PoolToken {

	private final int					threadCount;
	private final Queue<Runnable>		jobs		= new ConcurrentLinkedQueue<Runnable>();
	private final CyclicBarrier			barrier;

	private int jobCount = 0;
	
	public ThreadPoolToken(EfmModel efmModel) {
		this.threadCount = efmModel.getAdjEnumThreads();
		barrier = new CyclicBarrier(threadCount);
		for (int i = 1; i < threadCount; i++) {
			new Thread() {
				@Override
				public void run() {
					enterThreadLoop();
				}
			}.start();
		}
	}
	
	private void enterThreadLoop() {
		while (true) {
			final Runnable job = jobs.poll();
			if (job == null) {
				try {
					barrier.await();
					//if we get here, all jobs are complete, we're done
					return;
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				catch (BrokenBarrierException e) {
					//reset on barrier since new jobs exist
					//ignore exception & continue with loop
				}
			}
			else {
				jobCount--;
				job.run();
			}
		}		
	}
	
	public <T extends PoolToken> boolean scheduleAsJob(final T token, final Root<T> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<T> nodeA, final Node<T> nodeB, final boolean nodeAIsPos, final Queue<ColumnPair> adjacentPairs) {
		int jobsToAdd = 64*threadCount - jobCount;
		if (jobsToAdd > 0) {
			jobCount += addToJobQueue(0, token, root, posCols, zeroCols, negCols, nodeA, nodeB, nodeAIsPos, adjacentPairs);
			if (barrier.getNumberWaiting() > 0) {
				barrier.reset();
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * How many recursion levels should be converted to jobs?
	 * Maximum number of jobs is 4^levels, since every level consists of 4
	 * recursive calls. 
	 */
	private static final int MAX_LEVEL_DEPTH = 6; 

	/**
	 * Returns true if the recursive invocation has been added to the job queue, and false if the 
	 * caller should execute the recursion
	 */
	private <T extends PoolToken> int addToJobQueue(int level, final T token, final Root<T> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<T> nodeA, final Node<T> nodeB, final boolean nodeAIsPos, final Queue<ColumnPair> adjacentPairs) {
		if (root.enterIfCandidates(token, nodeA, nodeB)) {
			int jobs = 0;
			if (level < MAX_LEVEL_DEPTH) {
				level++;
				if (nodeA instanceof InterNode && nodeB instanceof InterNode) {
					final InterNode<T> interA = (InterNode<T>)nodeA;
					final InterNode<T> interB = (InterNode<T>)nodeB;
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, interA.child0, interB.child0, nodeAIsPos, adjacentPairs);
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, interA.child1, interB.child0, nodeAIsPos, adjacentPairs);
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, interA.child0, interB.child1, nodeAIsPos, adjacentPairs);
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, interA.child1, interB.child1, nodeAIsPos, adjacentPairs);
				}
				else if (nodeA instanceof InterNode) {
					final InterNode<T> interA = (InterNode<T>)nodeA;
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, interA.child0, nodeB, nodeAIsPos, adjacentPairs);
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, interA.child1, nodeB, nodeAIsPos, adjacentPairs);
				}
				else if (nodeB instanceof InterNode) {
					final InterNode<T> interB = (InterNode<T>)nodeB;
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, nodeA, interB.child0, nodeAIsPos, adjacentPairs);
					jobs += addToJobQueue(level, token, root, posCols, zeroCols, negCols, nodeA, interB.child1, nodeAIsPos, adjacentPairs);				
				}
				else {
					addJobToQueue(token, root, posCols, zeroCols, negCols, nodeA, nodeB, nodeAIsPos, adjacentPairs);
					jobs = 1;
				}
			}
			else {
				addJobToQueue(token, root, posCols, zeroCols, negCols, nodeA, nodeB, nodeAIsPos, adjacentPairs);
				jobs = 1;
			}
			root.leave(token, nodeA, nodeB);
			return jobs;
		}
		return 0;
	}
	private <T extends PoolToken> void addJobToQueue(final T token, final Root<T> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<T> nodeA, final Node<T> nodeB, final boolean nodeAIsPos, final Queue<ColumnPair> adjacentPairs) {
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
	
	public void execMainThread() {
		enterThreadLoop();
	}
	
}
