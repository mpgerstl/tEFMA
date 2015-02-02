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
 * Package visible helper class with static methods for {@link JobResult} and 
 * constant {@link JobMonitor} instantiation.
 */
class JobResultFactory {
	
	/**
	 * @return job result with given result value 
	 */
	public static <R> JobResult<R> createJobResult(final R result) {
		return new JobResult<R>() {
			public Throwable getException() {
				return null;
			}
			public R getResult() {
				return result;
			}
			public boolean isException() {
				return false;
			}
		};
	}
	
	/**
	 * @return exceptional job result with given cause for abortion  
	 */
	public static <R> JobResult<R> createJobResultForException(final Throwable exc) {
		return new JobResult<R>() {
			public Throwable getException() {
				return exc;
			}
			public R getResult() {
				return null;
			}
			public boolean isException() {
				return true;
			}
		};
	}
	
	/**
	 * @return a constant job status with the given exceptional result 
	 */
	public static <R> JobMonitor<R> createJobStatusForException(final Throwable exc) {
		final JobResult<R> result = createJobResultForException(exc);
		return new JobMonitor<R>() {
			public JobResult<R> getJobResult() {
				return result;
			}
			public void interrupt() {
				//nothing to do
			}
			public boolean isRunning() {
				return false;
			}
			public JobResult<R> waitForResult() throws InterruptedException {
				return result;
			}			
		};
	}
	
	// no instances
	private JobResultFactory() {
		super();
	}

}
