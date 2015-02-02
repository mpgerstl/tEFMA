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

import ch.javasoft.job.Executable;
import ch.javasoft.job.JobTerminationHandler;
import ch.javasoft.metabolic.efm.dist.impl.DistJobController;
import ch.javasoft.metabolic.efm.dist.impl.RunningJob;


/**
 * The <tt>MultiThreadedAdjEnum</tt> is a distributed implementation of the 
 * adjacency enumeration process, using separate threads to execute the client
 * jobs. The client threads are started in the same virtual machine as the
 * server. 
 */
public class MultiThreadedAdjEnum extends AbstractDistributedAdjEnum {
	
	public static final String NAME = "multi-threaded";
	
	/**
	 * Constructor for <code>MultiProcessedAdjEnum</code>
	 */
	public MultiThreadedAdjEnum() {
		super(NAME);
	}
	
	/**
	 * Returns the name to use for configuration, where this adj enum is used 
	 * with a {@link MultiThreadedAdjEnum} instance.
	 * 
	 * @param delegate	the name of the {@link MultiThreadedAdjEnum} instance
	 * @return the name to use for configuration
	 */
	public static String name(String delegate) {
		return NAME + ":" + delegate;
	}

	@Override
	protected RunningJob execJob(DistJobController controller, JobTerminationHandler<Void> terminationHandler, int nodeIndex) {
		final Executable<Void> job = controller.createExecutable(nodeIndex);
		return new RunningJob(job, job.exec(terminationHandler));
	}
}
