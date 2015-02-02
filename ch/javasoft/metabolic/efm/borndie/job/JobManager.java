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
package ch.javasoft.metabolic.efm.borndie.job;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.column.Column;

/**
 * The <code>JobManager</code> allows the queuing of pairing jobs and manages 
 * their multi-threaded execution.
 */
public class JobManager<Col extends Column> {

	private final BornDieController<Col> 	controller;
	private final ThreadPoolExecutor 		service;
	
	/**
	 * Constructor for <code>JobManager</code> with controller and job queue of
	 * unlimited capacity
	 * 
	 * 
	 * @param controller	the born/die controller
	 */
	public JobManager(BornDieController<Col> controller) {
		this(controller, 0);
	}
	/**
	 * Constructor for <code>JobManager</code> with controller and job queue of
	 * the specified capacity
	 * 
	 * @param controller	the born/die controller
	 * @param queueCapacity	the queue size to queue jobs, 0 for unlimited queue
	 */
	public JobManager(BornDieController<Col> controller, int queueCapacity) {
		this.controller	= controller;
		this.service	= createExecutorService(controller.getConfig().getMaxThreads(), queueCapacity);
	}
	
	private static ThreadPoolExecutor createExecutorService(int threadCount, int queueCapacity) {
//		return new ThreadPoolExecutor(threadCount, threadCount,
//                0L, TimeUnit.MILLISECONDS,
//                new PriorityBlockingQueue<Runnable>(1024, JOB_PRIORITIZER));		
		return queueCapacity == 0 ?
				new ThreadPoolExecutor(threadCount, threadCount,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>())		
				:
				new ThreadPoolExecutor(threadCount, threadCount,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<Runnable>(queueCapacity));				
	}
	
	/**
	 * The job prioritizer executes large jobs first, that is, the job with more
	 * adjacency candidates.
	 */
	@SuppressWarnings("unused")
	private static Comparator<Runnable> JOB_PRIORITIZER = new Comparator<Runnable>() {
		public int compare(Runnable o1, Runnable o2) {
			final PairingJob<?> job1 = (PairingJob<?>)o1;
			final PairingJob<?> job2 = (PairingJob<?>)o2;
			long dCnt = 0;
			try {
				final long cnt1 = ((long)job1.getMemoryNeg().getColumnCount()) * job1.getMemoryPos().getColumnCount();
				final long cnt2 = ((long)job2.getMemoryNeg().getColumnCount()) * job2.getMemoryPos().getColumnCount();
				dCnt = cnt1 - cnt2;
			}
			catch (Exception e) {
				//ignore
			}
			if (dCnt != 0) return dCnt < 0 ? 1 : -1;
			
//			final int drN = job1.getCellRangeNeg().getDieRow() - job2.getCellRangeNeg().getDieRow();
//			if (drN != 0) return -drN;
//			final int bcN = job1.getCellRangeNeg().getBornColumn() - job2.getCellRangeNeg().getBornColumn();
//			if (bcN != 0) return -bcN;
//			final int drP = job1.getCellRangePos().getDieRow() - job2.getCellRangePos().getDieRow();
//			if (drP != 0) return -drP;
//			final int bcP = job1.getCellRangePos().getBornColumn() - job2.getCellRangePos().getBornColumn();
//			if (bcP != 0) return -bcP;
			return 0;
		}
	};
	
	/**
	 * Adds the pairing job to the job queue
	 * 
	 * @param pairingJob	the job to add to the queue
	 */
	public void addJob(PairingJob<Col> pairingJob) throws InterruptedException {
		schedule(pairingJob);
		if (controller.getDebugger().doDebug()) {
			controller.getDebugger().notifyPairingQueued(pairingJob);
		}
	}
	
	private void schedule(final PairingJob<Col> pairingJob) {
		schedule((Runnable)pairingJob);
//		schedule(new Runnable() {
//			public void run() {
//				try {
//					//init adjacency enum
//					final SortableMemory<Col> memPos = pairingJob.getMemoryPos();
//					if (memPos.getColumnCount() > 0) {
//						final Config config = controller.getConfig();
//						final NetworkEfmModel model = controller.getModel();
//				        final AdjEnum adjEnum = config.getAdjMethodFactory().createAdjEnumFromConfig();
//				        adjEnum.initialize(controller.getColumnHome(), config, model);
//						final AdjEnumModel<Col> adjModel = new AdjEnumModel<Col>(
//								model, pairingJob.getIteration(), 
//								memPos, null /*job.getMemoryZero()*/, 
//								pairingJob.getMemoryNeg(), 
//								pairingJob.getMemoryForAppending());
//						adjEnum.adjacentPairs(controller.getColumnHome(), adjModel);
//					}
//					pairingJob.getOwner().notifyPairingJobCompleted(pairingJob);
//					if (controller.getDebugger().doDebug()) {
//						controller.getDebugger().notifyPairingComplete(pairingJob);
//					}
//				} 
//				catch (Exception e) {
//					controller.handleJobException(pairingJob, e);
//				}
//			}
//		});		
	}
	/**
	 * Schedule the given command for execution in another thread. The command 
	 * is expected to do its own exception handling.
	 */
	public void schedule(Runnable command) {
		try {
			service.execute(command);
		}
		catch (RejectedExecutionException ex) {
			if (controller.getException() != null) {
				throw new RejectedExecutionException(controller.getException());
			}
			throw ex;
		}
	}
	
	/**
	 * Schedule the given command for execution in another thread. Possible 
	 * exceptions are handled by the job manager.
	 */
	public void schedule(final Callable<Void> command) {
		schedule(new Runnable() {
			public void run() {
				try {
					command.call();
				}
				catch (Exception e) {
					controller.handleCommandException(e);
				}
			}
		});
	}
	
	/**
	 * Start scheduling jobs for processing.
	 */
	public void terminate() throws Exception {
		if (controller.getException() != null) {
			throw controller.getException();
		}
		service.awaitTermination(0, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public String toString() {
		return "JobManager{done=" + service.getCompletedTaskCount() + 
			", active=" + service.getActiveCount() + ", total=" +
			service.getTaskCount() + "}";
	}
}
