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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ch.javasoft.metabolic.efm.config.Config;

/**
 * The <code>TimeoutWaitingReleasePolicy</code> releases the permit if all or
 * some of the threads working with the common permit pool have released their 
 * permit, or when a certain timeout occurs, whatever happens fist. 
 */
public class TimeoutWaitingReleasePolicy implements ReleasePolicy {

	private volatile int	jobCount;
	private volatile int	threadCount;
	private volatile long	timeMS;
	private volatile long   maxWaitingTimeMS;
	private AtomicLong		curWaitingTimeMS = new AtomicLong();
	
	private volatile Semaphore permitsToRelease;

	/**
	 * Standard constructor
	 */
	public TimeoutWaitingReleasePolicy() {
		super();
	}

	//inherit javadoc
	public void initialize(Config config, int jobs, int threads) {
		jobCount		 = jobs;
		threadCount		 = threads;
		timeMS			 = -System.currentTimeMillis();
//		maxWaitingTimeMS = 1024;//wait at most 1s
		maxWaitingTimeMS = 64;//wait at most 64ms
//		maxWaitingTimeMS = 16;//wait at most 16ms
		curWaitingTimeMS.set(-1);
		permitsToRelease = new Semaphore(0);
	}
	
	/**
	 * Releases the permit if all or some of the threads working with the common 
	 * permit pool have released their permit, or when a certain timeout occurs, 
	 * whatever happens fist. 
	 */
	public void releasePermit(ConcurrentToken token) throws InterruptedException {
		//we wait until half of the threads are ready, then, 
		//we release all these permits at once
		permitsToRelease.release();

		//wait for approximately the time needed to execute one job in the queue
		//this thread has executed jobs: jobCount / threadCount
		//time per job: timeMS / jobs = timeMS / jobCount * threadCount
		if (timeMS < 0) {
			final long dtMS	= timeMS + System.currentTimeMillis();
			long curTimeMS	= Math.max(0, dtMS / jobCount * threadCount);
			curTimeMS		= Math.min(maxWaitingTimeMS, curTimeMS);
			if (curWaitingTimeMS.compareAndSet(-1, curTimeMS)) {
				timeMS = dtMS;
			}			
		}
		final long waitMS = curWaitingTimeMS.get();
		
		//collect permits
		int toCollect = 2;
		int toRelease = 0;
		while (toRelease == 0 && permitsToRelease.tryAcquire(toCollect, waitMS, TimeUnit.MILLISECONDS)) {
			toRelease = toCollect + permitsToRelease.drainPermits();
			if (toRelease < threadCount) {
				permitsToRelease.release(toRelease);
				toCollect = Math.min(toRelease << 1, threadCount);
				toRelease = 0;
			}
		}
		toRelease += permitsToRelease.drainPermits();
		if (toRelease > 0) {
			token.releasePermits(toRelease);
		}
	}

}
