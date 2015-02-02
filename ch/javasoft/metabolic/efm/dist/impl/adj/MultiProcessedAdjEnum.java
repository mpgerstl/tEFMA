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

import java.util.logging.Level;

import ch.javasoft.io.WriterOutputStream;
import ch.javasoft.job.ExecJob;
import ch.javasoft.job.ExecJobMonitor;
import ch.javasoft.job.JobTerminationHandler;
import ch.javasoft.metabolic.efm.dist.impl.DistJobController;
import ch.javasoft.metabolic.efm.dist.impl.RunningJob;
import ch.javasoft.util.logging.LogWriter;

/**
 * The <tt>MultiProcessedAdjEnum</tt> is a distributed implementation of the 
 * adjacency enumeration process, using separate processes to execute the client
 * jobs. The processes are usually run on different hosts, for instance on the
 * nodes of a cluster computer.
 */
public class MultiProcessedAdjEnum extends AbstractDistributedAdjEnum {
	
	public static final String NAME = "multi-processed";
	
	/**
	 * Constructor for <code>MultiProcessedAdjEnum</code>
	 */
	public MultiProcessedAdjEnum() {
		super(NAME);
	}
	
	/**
	 * Returns the name to use for configuration, where this adj enum is used 
	 * with a {@link MultiProcessedAdjEnum} instance.
	 * 
	 * @param delegate	the name of the {@link MultiProcessedAdjEnum} instance
	 * @return the name to use for configuration
	 */
	public static String name(String delegate) {
		return NAME + ":" + delegate;
	}

	@Override
	protected RunningJob execJob(DistJobController controller, final JobTerminationHandler<Void> terminationHandler, int nodeIndex) {
		final ExecJob job = controller.createExecJob(nodeIndex); 
		LogPkg.LOGGER.fine("EXEC: " + job);

		final ExecJobMonitor mtr = job.exec(terminationHandler);
		mtr.pipeErr(new WriterOutputStream(new LogWriter(LogPkg.LOGGER, Level.WARNING)), false);
		mtr.pipeOut(new WriterOutputStream(new LogWriter(LogPkg.LOGGER, Level.INFO)), false);
		
		return new RunningJob(job, mtr);
	}
}
