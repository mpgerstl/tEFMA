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
package ch.javasoft.job;

/**
 * The <code>JobProcessor</code> is a generic interface which is implemented
 * by different processors, which process jobs in different manners, e.g.
 * <ul> 
 *   <li>synchroneously in the caller's thread 
 *       (see {@link CurrentThreadJobProcessor})
 *   </li> 
 *   <li>asynchroneously in a new thread (see {@link NewThreadJobProcessor})<li/>
 *   <li>asynchroneously in its own process (for general commands, not 
 *       necessarily java jobs, see {@link ExecJob} and {@link ExecJobProcessor})
 *   </li>
 * </ul>
 * 
 * Not all job processors support all jobs, thus the implementation specifies
 * the supported job types with the given parameters: 
 * @param <JR>	Job result type, the (intermediate) result after starting job 
 * 				execution
 * @param <R>	The final result returned by the job after successful 
 * 				termination. Usually, <code>JR</code> and <code>R</code> are
 * 				identical
 * @param <J>	The job type supported by this job processor
 */
public interface JobProcessor<JR, R, J extends Job<? extends JR>> {
	/**
	 * Add a job termination handler, being notified after successful or
	 * exceptional job termination
	 * 
	 * @param handler	the termination handler to be registered
	 */
	void addJobTerminatedHandler(JobTerminationHandler<R> handler);
	/**
	 * Removes a job termination handler.
	 * 
	 * @param handler	the termination handler to be unregistered
	 * @return	<code>true</code> if such a handler existed and was removed
	 */
	boolean removeJobTerminatedHandler(JobTerminationHandler<R> handler);
	/** 
	 * Usually executes the given job and returns immediately, without waiting 
	 * for completion, corresponding to {@link Executable#exec()}. Note that the
	 * exact behaviour is up to the implementor and might diverge from this 
	 * general description.
	 * 
	 * @param job	the job to execute
	 * @return		the job status, which also allows access to the result
	 * 				when execution completed
	 */ 
	JobMonitor<R> exec(J job);
	/** 
	 * Usually executes the given job and waits for completion, corresponding to
	 * {@link Executable#execAndWait()}. Note that the exact behaviour is up to 
	 * the implementor and might diverge from this general description.
	 * 
	 * @param job	the job to execute
	 * @return	the job result, giving access to the return value on success,
	 * 			or to the exception if execution caused any.
	 */ 
	JobResult<R> execAndWait(J job) throws InterruptedException;
	
	R execAndWaitThrowException(J job) throws InterruptedException, Throwable; 
}
