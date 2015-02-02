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
package ch.javasoft.metabolic.efm.dist.impl.adj;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import ch.javasoft.job.Executable;
import ch.javasoft.job.Job;
import ch.javasoft.job.JobMonitor;
import ch.javasoft.job.JobTerminationHandler;
import ch.javasoft.metabolic.efm.adj.AbstractAdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.config.DistributedConfig;
import ch.javasoft.metabolic.efm.dist.DistributedAdjEnum;
import ch.javasoft.metabolic.efm.dist.impl.DistJobController;
import ch.javasoft.metabolic.efm.dist.impl.DistributableAdjEnum;
import ch.javasoft.metabolic.efm.dist.impl.RunningJob;
import ch.javasoft.metabolic.efm.dist.impl.file.FileBasedDistributableAdjEnum;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.logging.LogPrintStream;

/**
 * The <tt>AbstractDistributedAdjEnum</tt> uses a {@link DistributableAdjEnum} 
 * to perform the adjacent ray enumeration.
 * <p>
 * The computation consists of the following steps:
 * <ol>
 * 	<li>The distributed computation is initialized using 
 * 		{@link DistributableAdjEnum#initialize(ColumnHome, Config, EfmModel, AdjEnumModel, int) DistributableAdjEnum.initialize(..)}.</li>
 * 	<li>The distributed jobs are created using the {@link DistJobController}
 * 		returned by the initialize method of the previous step.</li>
 * </ol>
 */
abstract public class AbstractDistributedAdjEnum extends AbstractAdjEnum {
	
	/**
	 * Constructor with name for subclasses
	 */
	public AbstractDistributedAdjEnum(String name) {
		super(name);
	}
	
	public <Col extends Column, N extends Number> void adjacentPairs(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel) throws IOException {
		final long candidates = 
			((long)itModel.getMemoryPos().getColumnCount()) *
			((long)itModel.getMemoryNeg().getColumnCount());
		if (candidates == 0) {
			return;
		}
		final DistributedConfig distConfig = getConfig().getDistributedConfig();
		final DistributedAdjEnum delegate = getConfig().getAdjMethodFactory().createDistributedAdjEnumFromConfig();
		if (candidates < distConfig.getCandidateThreashold()) {
			LogPkg.LOGGER.fine("candidate count below threshold, not parallelizing: " +
					candidates + " < " + distConfig.getCandidateThreashold());
			PseudoDistributingAdjEnum adjEnum = new PseudoDistributingAdjEnum(delegate);
			adjEnum.initialize(columnHome, getConfig(), getEfmModel());
			adjEnum.adjacentPairs(columnHome, itModel);
			return;
		}
		final Map<Executable<Void>, JobMonitor<Void>> runningJobs = new ConcurrentHashMap<Executable<Void>, JobMonitor<Void>>();		
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
		final Thread mainThread = Thread.currentThread();
		final JobTerminationHandler<Void> terminationHandler = new JobTerminationHandler<Void>() {
			public void terminated(Job job, Void result) {
//				LogPkg.LOGGER.fine("DONE(ok): " + job);
				runningJobs.remove(job);
			}
			public void terminatedByException(Job job, Throwable t) {
//				LogPkg.LOGGER.fine("DONE(err): " + t);
				exception.compareAndSet(null, t);
				runningJobs.remove(job);
				mainThread.interrupt();
			}
		};
		
		final int nodeCnt = Math.min(distConfig.getNodeNames().size(), distConfig.getPartition());
		final DistributableAdjEnum denum = new FileBasedDistributableAdjEnum();//FIXME make configurable		
		final DistJobController controller = denum.initialize(columnHome, getConfig(), getEfmModel(), itModel, nodeCnt);
		final Thread jobKiller = new Thread() {
			@Override
			public void run() {
				while (!runningJobs.isEmpty()) {
					for (final Executable<Void> job : runningJobs.keySet()) {
						final JobMonitor<Void> mtr = runningJobs.remove(job);
						if (mtr != null) {//could be null due to concurrency
							mtr.interrupt();
						}
					}
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(jobKiller);
		try {
			//start jobs
			for (int nodeIndex = 0; nodeIndex < nodeCnt; nodeIndex++) {
				final RunningJob job = execJob(controller, terminationHandler, nodeIndex);
				runningJobs.put(job.getExecutable(), job.getMonitor());
				if (exception.get() != null) {
					throw exception.get();
				}
			}

			//wait for all jobs to complete
			controller.awaitCompletion();
		}
		catch (Throwable th) {
			LogPkg.LOGGER.warning("server caught exception, e=" + th);
			if (exception.get() != null) {
				th = exception.get();
				LogPkg.LOGGER.warning("server detected probable causing exception, e=" + th);
			}
			try {
				controller.abort();
			}
			catch (Exception ex) {
				LogPkg.LOGGER.warning("could not close server, e=" + ex);
				ex.printStackTrace(new LogPrintStream(LogPkg.LOGGER, Level.WARNING));
			}
			throw ExceptionUtil.toRuntimeExceptionOr(IOException.class, th);
		}
		finally {
			Runtime.getRuntime().removeShutdownHook(jobKiller);
		}
	}
	
	abstract protected RunningJob execJob(DistJobController jobController, JobTerminationHandler<Void> terminationHandler, int nodeIndex);

}
