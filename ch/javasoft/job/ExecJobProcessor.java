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
 * The <code>ExecJobProcessor</code> is specifically designed to execute
 * {@link ExecJob}s.
 */
public class ExecJobProcessor extends AbstractJobProcessor<ExecJobMonitor, Void, ExecJob> {

	/** Default instance */
	public static final ExecJobProcessor INSTANCE = new ExecJobProcessor();
	
	public ExecJobProcessor() {}
	
	public ExecJobMonitor exec(final ExecJob job) {		
		final ExecJobMonitor handle;
		try {
			handle = job.run();
		}
		catch (Throwable th) {
			return ExecJobMonitor.createForException(th);
		}
		new Thread() {
			@Override
			public void run() {
				waitFor(handle, job);
			}
		}.start();
		return handle;
	}
	
	@Override
	public JobResult<Void> execAndWait(ExecJob job) throws InterruptedException {
		final ExecJobMonitor hdl;
		try {
			hdl = job.run();
		}
		catch (Throwable th) {
			return JobResultFactory.createJobResultForException(th);
		}
		waitFor(hdl, job);
		return hdl.getJobResult();
	}
	
	private void waitFor(ExecJobMonitor handle, ExecJob job) {
		JobResult<Void> result;
		try {
			int exitValue = handle.getProcess().waitFor();
			if (exitValue == 0) {
				result = JobResultFactory.createJobResult(null /*void*/);
			}
			else {
				throw new ExitValueException(handle, exitValue);
			}
		}
		catch (Throwable th) {
			result = JobResultFactory.createJobResultForException(th);
		}
		handle.setResult(result);
		if (result.isException()) {
			invokeTerminationExceptionHandlers(job, result.getException());					
		}
		else {
			invokeTerminationHandlers(job, result.getResult());
		}		
	}

}
 