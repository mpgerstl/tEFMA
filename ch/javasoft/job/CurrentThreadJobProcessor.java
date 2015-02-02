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
 * The <tt>CurrentThreadJobProcessor</tt> executes jobs synchroneously in the 
 * current thread, i.e. both methods {@link #exec(Job)} and 
 * {@link #execAndWait(Job)} behave alike and block until completion.
 * 
 * @param <R>	The final result returned by the job after successful 
 * 				termination.
 */
public class CurrentThreadJobProcessor<R> extends AbstractJobProcessor<R, R, Job<R>> {
	
	/**
	 * Since executed in the current thread, this method blocks until completion
	 * just like {@link #execAndWait(Job)}.
	 */
	public JobMonitor<R> exec(Job<R> job) {
		final JobResult<R> res = execAndWait(job);
		return new JobMonitor<R>() {
			public JobResult<R> getJobResult() {
				return res;
			}
			public boolean isRunning() {
				return false;
			}
			public JobResult<R> waitForResult() {
				return res;
			}
			public void interrupt() {
				//nothing to do
			}
		};
	}

	//inherit javadoc comments
	@Override
	public JobResult<R> execAndWait(Job<R> job) {
		try {
			R result = job.run();
			invokeTerminationHandlers(job, result);
			return JobResultFactory.createJobResult(result);
		}
		catch (Throwable th) {
			try {
				invokeTerminationExceptionHandlers(job, th);
				return JobResultFactory.createJobResultForException(th);
			}
			catch (UncaughtJobTerminationHandlerException ex) {
				throw ex;
			}
		}
	}

}
