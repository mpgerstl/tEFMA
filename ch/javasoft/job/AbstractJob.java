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
 * <code>AbstractJob</code> is an abstract base implementation for most jobs 
 * implementing both {@link Job} and {@link Executable}.
 *
 * @param <R> The return value type of the result
 */
abstract public class AbstractJob<R> implements Job<R>, Executable<R> {

	/**
	 * Executes the job in a new thread. The call immediately returns without
	 * waiting for job completion.
	 * 
	 * @return	the job status, which also allows access to the result
	 * 			when execution completed
	 */
	public JobMonitor<R> exec() {
		return new NewThreadJobProcessor<R>().exec(this);
	}
	
	/**
	 * Executes the job in a new thread. The call immediately returns without
	 * waiting for job completion.
	 * 
	 * @param 	terminationHandler 	the handler for normal and exceptional 
	 * 								termination of this job
	 */
	public JobMonitor<R> exec(JobTerminationHandler<R> terminationHandler) {
		final NewThreadJobProcessor<R> proc = new NewThreadJobProcessor<R>();
		proc.addJobTerminatedHandler(terminationHandler);
		return proc.exec(this);
	}
	
	/** 
	 * Executes the job in a new thread and waits for completion. The current
	 * thread is blocked until the job thread terminates.
	 * 
	 * @return	the job result, giving access to the return value on success,
	 * 			or to the exception if execution caused any.
	 */ 
	public JobResult<R> execAndWait() throws InterruptedException {
		return new NewThreadJobProcessor<R>().execAndWait(this);		
	}
	
	/** 
	 * Executes the job in a new thread and waits for completion. The current
	 * thread is blocked until the job thread terminates. If the job terminates
	 * normally, the result is returned. Otherwise, the job exception is thrown.
	 * 
	 * @return	the result if the job terminates normally 
	 */ 
	public R execAndWaitThrowException() throws InterruptedException, Throwable {
		return new NewThreadJobProcessor<R>().execAndWaitThrowException(this);
	}
	
}
