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

import java.util.concurrent.Callable;

/**
 * The <code>Executables</code> class contains methods to create 
 * {@link Executable executables} from {@link Job jobs}, 
 * {@link Runnable runnables} or {@link Callable callables}. Invoking the
 * executables {@link Executable#exec() exec()} or 
 * {@link Executable#execAndWait() execAndWait()} method causes execution in a
 * new thread. 
 */
public class Executables<R> {
	
	/**
	 * Creates a new executable from the given job. The 
	 * {@link Executable#exec() exec()} and 
	 * {@link Executable#execAndWait() execAndWait()} methods of the returned
	 * executable are processed in a new thread.
	 * 
	 * @param <R>	the return type of the job
	 * @param job	the job to convert into an executable
	 * @return the executable for execution in a new thread
	 */
	public static <R> Executable<R> create(final Job<R> job) {
		return new AbstractJob<R>() {
			public R run() throws Throwable {
				return job.run();
			}
		};		
	}
	/**
	 * Creates a new executable from the given callable. The 
	 * {@link Executable#exec() exec()} and 
	 * {@link Executable#execAndWait() execAndWait()} methods of the returned
	 * executable are processed in a new thread.
	 * 
	 * @param <R>		the return type of the callable
	 * @param callable	the callable to convert into an executable
	 * @return the executable for execution in a new thread
	 */
	public static <R> Executable<R> create(final Callable<R> callable) {
		return createAbstractJob(callable);
	}
	/**
	 * Creates a new executable from the given runnable. The 
	 * {@link Executable#exec() exec()} and 
	 * {@link Executable#execAndWait() execAndWait()} methods of the returned
	 * executable are processed in a new thread.
	 * 
	 * @param runnable	the runnable to convert into an executable
	 * @return the executable for execution in a new thread
	 */
	public static Executable<Void> create(final Runnable runnable) {
		return createAbstractJob(runnable);
	}
	
	/**
	 * Internal methods used by {@link #create(Callable)} and 
	 * {@link Jobs#create(Callable)}
	 */
	static <R> AbstractJob<R> createAbstractJob(final Callable<R> callable) {
		return new AbstractJob<R>() {
			public R run() throws Throwable {
				return callable.call();
			}
		};
	}

	/**
	 * Internal methods used by {@link #create(Runnable)} and 
	 * {@link Jobs#create(Runnable)}
	 */
	static AbstractJob<Void> createAbstractJob(final Runnable runnable) {
		return new AbstractJob<Void>() {
			public Void run() throws Throwable {
				runnable.run();
				return null;
			}
		};		
	}
	
	//no instances
	private Executables() {}

}
