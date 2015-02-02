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
 * A <code>JobTerminationHandler</code> can be registered with a 
 * {@link JobProcessor} and is notified when jobs terminate, regularly or
 * exceptional.
 * <br/>
 * Note: the notification methods of a job termination handler should be
 * exception safe, i.e. all exceptions should be caught and handled. If an
 * exception is thrown during notification, it is wrapped within an
 * {@link UncaughtJobTerminationHandlerException} and rethrown, i.e. job
 * execution aborts.
 *
 * @param <R>	Result type of the job after successful termination
 */
public interface JobTerminationHandler<R> {
	
	/**
	 * Notification method for successful job execution termination.
	 * Note: this method should not throw exceptions, see class comments.
	 * 
	 * @param job		The concerned job
	 * @param result	The outcome of the job execution
	 */
	void terminated(Job job, R result);

	/**
	 * Notification method for exceptional job execution termination
	 * Note: this method should not throw exceptions, see class comments.
	 * 
	 * @param job		The concerned job
	 * @param exception	The exception which caused the abnormal termination
	 */
	void terminatedByException(Job job, Throwable exception);
}
