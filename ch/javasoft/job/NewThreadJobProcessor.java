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
import java.util.Collections;
import java.util.List;

/**
 * The <tt>NewThreadJobProcessor</tt> executes jobs asynchroneously in a new 
 * thread.
 * 
 * @param <R>	The final result returned by the job after successful 
 * 				termination.
 */
public class NewThreadJobProcessor<R> extends AbstractJobProcessor<R, R, Job<? extends R>> {
	
	//inherit javadoc comments
	public JobMonitor<R> exec(final Job<? extends R> job) {
		final List<JobResult<R>> res = Collections.synchronizedList(new ArrayList<JobResult<R>>(1));
		final Thread thread = new Thread() {
			@Override
			public void run() {
				JobResult<R> result;
				try {
					final R rawResult = job.run();
					result = JobResultFactory.createJobResult(rawResult);
				}
				catch (Throwable th) {
					result = JobResultFactory.createJobResultForException(th);
				}
				res.add(result);
				if (result.isException()) {
					invokeTerminationExceptionHandlers(job, result.getException());					
				}
				else {
					invokeTerminationHandlers(job, result.getResult());
				}
			}
		};
		thread.start();
		return new JobMonitor<R>() {
			public JobResult<R> getJobResult() {
				return res.isEmpty() ? null : res.get(0);
			}
			public boolean isRunning() {
				return res.isEmpty();
			}
			public JobResult<R> waitForResult() throws InterruptedException {
				int time	= 8;
				int end		= 1024;
				while (isRunning()) {
					Thread.sleep(time);
					if (time < end) time*=2;
				}
				return getJobResult();
			}
			public void interrupt() {
				thread.interrupt();
			}
		};
	}

}
