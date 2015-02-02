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
 * The <code>Executable</code> is like a {@link Job}, but the execution is
 * not necessarily performed synchronous.
 * <br/>
 * The {@link #exec()} method does not wait until the job execution has 
 * completed, and thus an execution monitor object is returned.
 * <br/>
 * The {@link #execAndWait()} method blocks until completion and returns the
 * result, which might also be an exception, thus a result object instead of the
 * return value is returned.
 *
 * @param <R> The return value type of the result
 */
public interface Executable<R> {
	
	/** 
	 * Executes and returns immediately, without waiting for completion.
	 * 
	 * @return	the job monitor, which also allows access to the result
	 * 			when execution completed
	 */ 
	JobMonitor<R> exec();

	/** 
	 * Executes and returns immediately, without waiting for completion. The 
	 * submitted termination handler is notified upon normal or exceptional
	 * termination.
	 * 
	 * @param 	terminationHandler
	 * 			the handler for normal and exceptional termination of this job
	 * @return	the job monitor
	 */ 
	JobMonitor<R> exec(JobTerminationHandler<R> terminationHandler);
	
	/** 
	 * Executes and waits for completion.
	 * 
	 * @return	the job result, giving access to the return value on success,
	 * 			or to the exception if execution caused any.
	 */ 
	JobResult<R> execAndWait() throws InterruptedException;
	
	/** 
	 * Executes and waits for completion. The result is returned on normal
	 * termination, and an exception is thrown on abnormal (exceptional)
	 * termination. 
	 * 
	 * @return	the result if termination is normal
	 */ 
	R execAndWaitThrowException() throws InterruptedException, Throwable;
}
