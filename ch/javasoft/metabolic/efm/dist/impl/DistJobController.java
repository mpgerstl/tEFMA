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
package ch.javasoft.metabolic.efm.dist.impl;

import ch.javasoft.job.ExecJob;
import ch.javasoft.job.Executable;


/**
 * The <code>DistJobController</code> creates jobs representing a single client 
 * task or a distributed computation job, executable in an own thread (in the 
 * current jvm) or an own process (in another jvm). Associated with a job is the 
 * node index, identifying the executing host or thread. The controller also 
 * offers {@link #awaitCompletion() waiting for completion} of all jobs, or 
 * {@link #abort() exceptional abortion}.
 */
public interface DistJobController {

	/**
	 * Returns an exec job which is run in a separate process (in a new jvm)
	 * 
	 * @param nodeIndex		the node or thread index of the job to create
	 */
	ExecJob createExecJob(int nodeIndex);
	
	/**
	 * Returns an executable which is run in a separate thread (in this jvm)
	 * 
	 * @param nodeIndex		the node or thread index of the job to create
	 */
	Executable<Void> createExecutable(int nodeIndex);
	
	/**
	 * Blocks the current thread until all jobs are completed. Before returning,
	 * all underlying resources should be released (closed). 
	 * 
	 * @throws InterruptedException	if waiting is interrupted
	 */
	void awaitCompletion() throws InterruptedException;
	
	/**
	 * Terminate completion immediately and release all resources. Typically
	 * called when an exception occurred. If jobs are executed in separate
	 * processes, all processes should be killed.
	 */
	void abort();

}
