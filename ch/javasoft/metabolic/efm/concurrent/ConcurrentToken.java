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
package ch.javasoft.metabolic.efm.concurrent;

import java.io.IOException;
import java.util.concurrent.Callable;

import ch.javasoft.metabolic.efm.progress.ProgressAggregator;

/**
 * Interface which is implemented by multi threaded tokens used by 
 * traversers of bit set trees. A token is used to store thread related
 * state. For this purpose, new threads are registered here, and terminated 
 * threads are unregistered. Implementations might offer additional methods to
 * store specific state associated with threads.
 */
public interface ConcurrentToken {
	
	/**
	 * Return as many permits as are available
	 * @return	the number of permits to start new child threads
	 */
	int drainPermits();
	/**
	 * Returns true if a permit was acquired to start a new child thread
	 */
	boolean tryAcquirePermit();
	/**
	 * Release a single permit
	 */
	void releasePermit();
	/**
	 * Release the given number of permits
	 * 
	 * @param permits	the number of permits to release
	 */
	void releasePermits(int permits);
	/**
	 * Creates a new child thread. All initial resources are prepared, and the
	 * new thread is registered to be {@link #waitForChildThreads() awaitable}.
	 * The new thread is not started yet.
	 * 
	 * @param callable	the code which called within the 
	 * 					{@link Thread#run() run()} method of the returned 
	 * 					thread, should not be null
	 * @param finalizer	before {@link Thread#run() run()} method of the returned
	 * 					thread terminates (after invoking the callable), 
	 * 					{@link ThreadFinalizer#finalizeCurrentThread() finalizer.finalizeCurrentThread()}
	 * 					is called, if <code>finalizeCurrentThread</code> is not
	 * 					null  
	 */
	Thread createChildThread(Callable<Void> callable, ThreadFinalizer finalizer);
	
	/**
	 * Called by main thread at end of computation to wait for completion of all
	 * child threads. Child threads are created using {@link #createChildThread(Callable, ThreadFinalizer)} 
	 * @throws InterruptedException 
	 */
	void waitForChildThreads() throws InterruptedException;
	
	/**
	 * Returns true if the progress increment value 2^-e is large enough to be
	 * added to the overall progress, and false otherwise. If the progress 
	 * increment value is too small, the parents (with summarized increment
	 * value) should call the notify method.
	 * 
	 * @param e	the increment value to check
	 * @return	true if it can safely be passed to {@link #notifyProgressIncrement(int)}
	 */
	boolean isProgressIncrementNotifiable(int e);

	/**
	 * Any worker, e.g. a thread when it terminates or at any stage, can notify
	 * its working progress. Progress is partitioned into steps of size 2^-e, 
	 * it is divided by 2 or 4 when recursing a tree, i.e. e is incremented by
	 * 1 or 2. Thus, the overall progress is updated by adding 2^-e to the 
	 * overall counter. The progress counter starts with 0 and ends with 1.
	 * 
	 * @param e A non-negative number indicating a progress increment 
	 * 			of <code>2<sup>-e</sup></code>
	 * 						
	 * @throws IOException	if progress monitor outputting causes an 
	 * 						i/o exception
	 * 
	 * @see ProgressAggregator#updateProgress(int)
	 */
	void notifyProgressIncrement(int e) throws IOException;

}
