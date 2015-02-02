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

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract implementation of {@link JobProcessor}, mainly implementing 
 * {@link JobTerminationHandler} support.
 */
abstract public class AbstractJobProcessor<JR, R, J extends Job<? extends JR>> implements JobProcessor<JR, R, J> {
	
	private final List<JobTerminationHandler<R>> mHandlers = new ArrayList<JobTerminationHandler<R>>();

	//inherit javadoc comments
	public void addJobTerminatedHandler(JobTerminationHandler<R> handler) {
		mHandlers.add(handler);
	}
	//inherit javadoc comments
	public boolean removeJobTerminatedHandler(JobTerminationHandler<R> handler) {
		return mHandlers.remove(handler);
	}

	protected void invokeTerminationHandlers(J job, R result) {		
		List<JobTerminationHandler<R>> handlers = new ArrayList<JobTerminationHandler<R>>(mHandlers);
		for (JobTerminationHandler<R> hdl : handlers) {
			try {
				hdl.terminated(job, result);
			}
			catch (Throwable unc) {
				throw new UncaughtJobTerminationHandlerException(
					"uncaught exception in job termination handler [job=" + job + ", hdl=" + hdl + "], e=" + unc, unc
				);
			}
		}			
	}
	protected void invokeTerminationExceptionHandlers(J job, Throwable throwable) {
		List<JobTerminationHandler<R>> handlers = new ArrayList<JobTerminationHandler<R>>(mHandlers);
		for (JobTerminationHandler<R> hdl : handlers) {
			try {
				hdl.terminatedByException(job, throwable);
			}
			catch (ClassCastException cce) {
				throw new UncaughtJobTerminationHandlerException(
					"uncaught exception in job termination handler [job=" + job + ", hdl=" + hdl + "], e=" + cce, 
					
					cce
				);				
			}
			catch (Throwable unc) {
				throw new UncaughtJobTerminationHandlerException(
					"uncaught exception in job termination handler [job=" + job + ", hdl=" + hdl + "], e=" + unc, unc
				);
			}
		}
	}
	
	public JobResult<R> execAndWait(J job) throws InterruptedException {
		return exec(job).waitForResult();
	}
	
	public R execAndWaitThrowException(J job) throws InterruptedException, Throwable {
		final JobResult<R> result = execAndWait(job);
		if (result.isException()) throw result.getException();
		return result.getResult();
	}
}
