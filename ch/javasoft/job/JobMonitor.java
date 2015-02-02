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
 * The <code>JobMonitor</code> allows monitoring of a {@link Job} currently 
 * being executed. Once the job has terminated, the result can be accessed by
 * {@link #getJobResult()}. Extensions of <code>JobMonitor</code> might offer
 * further methods being specific for certain job types 
 * (e.g. {@link ExecJobMonitor} for {@link ExecJob}s).
 *
 * @param <R> The return value type of the result
 */
public interface JobMonitor<R> {
	
	/**
	 * @return	true if still processing, false if terminated (with or without
	 * 			exception)
	 */
	boolean isRunning();
	
	/**
	 * @return	the result, or <code>null</code> if {@link #isRunning()} returns 
	 * 			<code>true</code>
	 */
	JobResult<R> getJobResult();

	/**
	 * Blocks the current thread until the result is available.
	 * @return	the result
	 * @throws	InterruptedException	if execution has been interrupted, e.g.
	 * 									by calling {@link #interrupt()}
	 */
	JobResult<R> waitForResult() throws InterruptedException;
	
	/**
	 * Interrupts the process or thread, or does nothing if 
	 * {@link #isRunning()} returns <code>false</code>. The 
	 * {@link #waitForResult()} method receives an {@link InterruptedException}
	 * if the job status is affected by this call.
	 */
	void interrupt();
}
