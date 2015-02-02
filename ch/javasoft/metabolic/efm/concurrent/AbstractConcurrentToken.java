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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ch.javasoft.metabolic.efm.progress.ProgressAggregator;

abstract public class AbstractConcurrentToken implements ConcurrentToken {
	
	private static final Object DUMMY = new Object();

	private final ProgressAggregator progressAggregator;
	
	private final AtomicReference<Exception> exception = new AtomicReference<Exception>();
	private final Map<Thread, Object> childThreads = new ConcurrentHashMap<Thread, Object>();
	
	protected AbstractConcurrentToken() {
		progressAggregator = null;
	}
	protected AbstractConcurrentToken(ProgressAggregator progressAggregator) throws IOException {
		this.progressAggregator = progressAggregator;
	}
	
	public Thread createChildThread(final Callable<Void> callable, final ThreadFinalizer finalizer) {
//		if (finalizer == null) throw new NullPointerException();
		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					callable.call();
					childThreads.remove(this);
					if (finalizer != null) {
						finalizer.finalizeCurrentThread();
					}
				} 
				catch (Exception e) {
					childThreads.remove(this);
					if (exception.compareAndSet(null, e)) {
						handleException(this, e);
					}
				}
			}
		};
		childThreads.put(thread, DUMMY);
		return thread;
	}

	private void handleException(Thread thread, Exception e) {
		Set<Thread> children = childThreads.keySet();
		while (!children.isEmpty()) {
			for (final Thread child : children) {
				child.interrupt();
			}
			//be sure, modifications to concurrent map during iteration could
			//have been missed
			children = childThreads.keySet();
		}
	}

	public void waitForChildThreads() throws InterruptedException {
		Set<Thread> children = childThreads.keySet();
		while (!children.isEmpty()) {
			for (final Thread child : children) {
				child.join();
			}
			//be sure, modifications to concurrent map during iteration could
			//have been missed
			children = childThreads.keySet();
		}
		if (progressAggregator != null) {
			try {
				progressAggregator.close();
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		if (exception.get() != null) {
			throw new RuntimeException(exception.get());
		}
	}
	
	public boolean isProgressIncrementNotifiable(int e) {
		return progressAggregator == null ? false : e <= progressAggregator.getSmallestIncrement();
	}
	
	public void notifyProgressIncrement(int e) throws IOException {
		if (isProgressIncrementNotifiable(e)) {
			progressAggregator.updateProgress(e);
		}
	}

}
