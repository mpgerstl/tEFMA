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
package ch.javasoft.lang.management;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The <code>JVMTimer</code> polls all java threads and updates timing 
 * information for all threads. This allows us to compute the total CPU time, 
 * user time and system time of the whole JVM.
 * <p>
 * A simple sample use case looks as follows:
 * <pre>
 * JVMTimer timer = new JVMTimer(100);//polling every 100ms
 * timer.start();
 * 
 * //do something
 * 
 * System.out.println("intermediate CPU time:    " + timer.getTotalCpuTime());
 * System.out.println("intermediate user time:   " + timer.getTotalUserTime());
 * System.out.println("intermediate system time: " + timer.getTotalSystemTime());
 * 
 * //do something
 * 
 * timer.stop();
 * System.out.println("total CPU time:    " + timer.getTotalCpuTime());
 * System.out.println("total user time:   " + timer.getTotalUserTime());
 * System.out.println("total system time: " + timer.getTotalSystemTime());
 * </pre>
 */
public class JVMTimer {
	
    private static class ThreadTime {	    	
        @SuppressWarnings("unused")
		public final long id;
        public final long startCpuTime;
        public final long startUserTime;
        public volatile long endCpuTime;
        public volatile long endUserTime;
        public ThreadTime(long id, long c, long u) {
            this.id = id;
            this.startCpuTime  = c;
            this.startUserTime = u;
            this.endCpuTime    = c;
            this.endUserTime   = u;	        	
        }	        
    }
 
    private final long interval;
    private final ConcurrentMap<Long,ThreadTime> history = new ConcurrentHashMap<Long,ThreadTime>();
    
    private volatile Thread runner = null;
 
    /** 
     * Create a polling thread to track times. The polling interval 
     * indicates after how many milliseconds the threads are polled again.
     */
    public JVMTimer(final long intervalMS) {
        this.interval = intervalMS;
    }
    
    /**
     * Start the timing.
     * 
     * @throws IllegalStateException if the timer is already running
     */
    public void start() {
    	if (runner != null) {
    		throw new IllegalStateException("already started");
    	}
    	runner = new Thread() {
    		@Override
    		public void run() {
    	        while (runner != null) {
    	            update();
    	            try { sleep(interval); }
    	            catch (InterruptedException e) { 
    	            	break; 
    	            }
    	        }
    		}
    	};
    	runner.setDaemon(true);
    	runner.start();
    }
    
    /**
     * Stop the timing.
     */
    public void stop() {
    	final Thread r = runner;
    	runner = null;
    	if (r != null) {
    		try {
    			r.join();
    		}
    		catch (InterruptedException e) {
    			//ignore
    		}
    	}
    }
 

    /** Update the hash table of thread times. */
    private void update() {
    	final long threadId = Thread.currentThread().getId();
        final ThreadMXBean bean =
            ManagementFactory.getThreadMXBean();
        final long[] ids = bean.getAllThreadIds();
        for (long id : ids) {
            if (id == threadId)
                continue;   // Exclude polling thread
            final long c = bean.getThreadCpuTime(id);
            final long u = bean.getThreadUserTime(id);
            if (c == -1 || u == -1)
                continue;   // Thread died
 
            ThreadTime times = history.get(Long.valueOf(id));
            if (times == null) {
                times = new ThreadTime(id, c, u);
                history.put(Long.valueOf(id), times);
            } else {
                times.endCpuTime  = c;
                times.endUserTime = u;
            }
        }
    }
    
    /**
     * Returns the CPU time of the java process in milliseconds, or 0 if this is 
     * not supported.
     */
    public static long getProcessCpuTimeMS() {
    	return getProcessCpuTimeNS() / 1000000;
    }
    /**
     * Returns the CPU time of the java process in nanoseconds, or 0 if this is 
     * not supported.
     */
    public static long getProcessCpuTimeNS() {
        final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean) {
        	return ((com.sun.management.OperatingSystemMXBean)bean).getProcessCpuTime();
        }
    	return 0;
    }
 
    /** 
     * Returns the total CPU time so far in milliseconds. 
     */
    public long getTotalCpuTimeMS() {
    	return getTotalCpuTimeNS() / 1000000;
    }
    /** 
     * Returns the total CPU time so far in nanoseconds. 
     */
    public long getTotalCpuTimeNS() {
        final Collection<ThreadTime> hist = history.values();
        long time = 0L;
        for (ThreadTime times : hist)
            time += times.endCpuTime - times.startCpuTime;
        return time;
    }
 
    /** 
     * Returns the total user time so far in milliseconds. 
     */
    public long getTotalUserTimeMS() {
    	return getTotalUserTimeNS() / 1000000;
    }
    /** 
     * Returns the total user time so far in nanoseconds. 
     */
    public long getTotalUserTimeNS() {
        final Collection<ThreadTime> hist = history.values();
        long time = 0L;
        for (ThreadTime times : hist)
            time += times.endUserTime - times.startUserTime;
        return time;
    }
 
    /** 
     * Returns the total system time so far in milliseconds. 
     */
    public long getTotalSystemTimeMS() {
    	return getTotalSystemTimeNS() / 1000000;
    }
    /** 
     * Returns the total system time so far in nanoseconds. 
     */
    public long getTotalSystemTimeNS() {
        final Collection<ThreadTime> hist = history.values();
        long time = 0L;
        for (ThreadTime times : hist)
	        //               TotalCpuTime                   -              TotalUserTime;
            time += (times.endCpuTime - times.startCpuTime) - (times.endUserTime - times.startUserTime);
        return time;
    }
    
    @Override
    public String toString() {
    	final long cpu  = getTotalCpuTimeMS();
    	final long user = getTotalUserTimeMS();
    	return getClass().getSimpleName() + "[CPU=" + cpu + "ms, user=" + user + "ms, system=" + (cpu - user) + "ms]";
    }
}
