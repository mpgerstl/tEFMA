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
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The <code>MultiJobExecutable</code> executes multiple jobs, each in its own
 * thread. The result is a {@link Queue queue} of the single job results, if all
 * jobs terminated normally. If any of the jobs terminated with an exception,
 * the result if this executable is also exceptional. The first occurring 
 * exception is returned by this executable, and all other jobs are interrupted
 * if an exception occurs.
 */
public class MultiJobExecutable<R> implements Executable<Queue<R>> {
	
	private final Job<? extends R>[] jobs;
	
	private MultiJobExecutable(Job<? extends R>... jobs) {
		this.jobs = jobs;
	}
	@SuppressWarnings("unchecked")
	public MultiJobExecutable(Job<? extends R> jobA, Job<? extends R> jobB) {
		this(new Job[] {jobA, jobB});
	}
	@SuppressWarnings("unchecked")
	public MultiJobExecutable(Job<? extends R> jobA, Job<? extends R> jobB, Job<? extends R> jobC) {
		this(new Job[] {jobA, jobB, jobC});
	}
	@SuppressWarnings("unchecked")
	public MultiJobExecutable(Job<? extends R> jobA, Job<? extends R> jobB, Job<? extends R> jobC, Job<? extends R> jobD) {
		this(new Job[] {jobA, jobB, jobC, jobD});
	}
	public MultiJobExecutable(Iterable<Job<? extends R>> jobs) {
		this(toArray(jobs));
	}
	
	@SuppressWarnings("unchecked")
	private static <R> Job<? extends R>[] toArray(Iterable<Job<? extends R>> jobs) {
		final Collection<Job<? extends R>> cjobs;
		if (jobs instanceof Collection) {
			cjobs = (Collection<Job<? extends R>>)jobs;
		}
		else {
			cjobs = new ArrayList<Job<? extends R>>();
			for (final Job<? extends R> job : jobs) {
				cjobs.add(job);
			}
		}
		return cjobs.toArray(new Job[cjobs.size()]);
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.job.Executable#exec()
	 */
	public JobMonitor<Queue<R>> exec() {
		final Map<Job, JobMonitor<R>> monitors = new ConcurrentHashMap<Job, JobMonitor<R>>();
		final Queue<R> results = new ConcurrentLinkedQueue<R>();
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
		final NewThreadJobProcessor<R> proc = new NewThreadJobProcessor<R>();
		final CountDownLatch counter = new CountDownLatch(jobs.length);
		proc.addJobTerminatedHandler(new JobTerminationHandler<R>() {
			public void terminated(Job job, R result) {
				results.add(result);
				monitors.remove(job);
				counter.countDown();
			}
			public void terminatedByException(Job job, Throwable t) {
				if (exception.compareAndSet(null, t)) {
					//we are the first exception
					while (counter.getCount() > 0) {
						counter.countDown();
					}
					for (final JobMonitor<R> mon : monitors.values()) {
						mon.interrupt();
					}
				}
				else {
					counter.countDown();					
				}
			}
		});
		for (int i = 0; i < jobs.length; i++) {
			monitors.put(jobs[i], proc.exec(jobs[i]));
		}
		return new JobMonitor<Queue<R>>() {
			private JobResult<Queue<R>> result;
			public JobResult<Queue<R>> getJobResult() {
				if (isRunning()) return null;
				if (result == null) {
					result = new JobResult<Queue<R>>() {
						public Throwable getException() {
							return exception.get();
						}
						public Queue<R> getResult() {
							return isException() ? null : results;
						}
						public boolean isException() {
							return exception.get() != null;
						}
					};
				}
				return result;
			}
			public void interrupt() {
				if (exception.compareAndSet(null, new InterruptedException())) {
					//we are the first exception
					while (counter.getCount() > 0) {
						counter.countDown();
					}
					for (final JobMonitor<R> mon : monitors.values()) {
						mon.interrupt();
					}
				}
			}
			public boolean isRunning() {
				return counter.getCount() > 0;
			}
			public JobResult<Queue<R>> waitForResult() throws InterruptedException {
				counter.await();
				return getJobResult();
			}
		};
	}
	
	public JobMonitor<Queue<R>> exec(JobTerminationHandler<Queue<R>> terminationHandler) {
		final NewThreadJobProcessor<Queue<R>> proc = new NewThreadJobProcessor<Queue<R>>();
		proc.addJobTerminatedHandler(terminationHandler);
		return proc.exec(new Job<Queue<R>>() {
			public Queue<R> run() throws Throwable {
				return MultiJobExecutable.this.execAndWaitThrowException();
			}
		});
	}

	public JobResult<Queue<R>> execAndWait() throws InterruptedException {
		return exec().waitForResult();
	}
	
	public Queue<R> execAndWaitThrowException() throws InterruptedException, Throwable {
		final JobResult<Queue<R>> result = execAndWait();
		if (result.isException()) throw result.getException();
		return result.getResult();
	}

}
