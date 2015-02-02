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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import ch.javasoft.io.Print;
import ch.javasoft.io.WriterOutputStream;

/**
 * The <code>ExecJobMonitor</code> adds some functions specific for 
 * {@link ExecJob}s to the normal {@link JobMonitor} functions. For instance, 
 * input and output stream handling methods are available, such as piping the
 * output of the job to some output stream, or piping the output of one job to
 * the input of another job.
 */
public class ExecJobMonitor implements JobMonitor<Void> {
	
	private final Process mProcess;
	private final List<InputStream> mCloseInOnTerminate = new ArrayList<InputStream>();
	private final List<OutputStream> mCloseOutOnTerminate = new ArrayList<OutputStream>();
	private final List<JobMonitor> mPipeJobs = new ArrayList<JobMonitor>();
	
	private JobResult<Void> mResult;
	
	protected ExecJobMonitor(Process process) {
		mProcess = process;
	}
	
	protected static ExecJobMonitor createForException(Throwable th) {
		ExecJobMonitor hdl = new ExecJobMonitor(null) {
			@Override
			public boolean isRunning() {
				return false;
			}
		};
		JobResult<Void> result = JobResultFactory.createJobResultForException(th);
		hdl.setResult(result);
		return hdl;
	}
	protected Process getProcess() {
		return mProcess;
	}
	
	protected synchronized void setResult(JobResult<Void> result) {
		mResult = result;
		closeStreams();
	}

	// inherit javadoc
	public void interrupt() {
		if (mProcess != null) mProcess.destroy();
	}
	// inherit javadoc
	public boolean isRunning() {
		try {
			mProcess.exitValue();
			return false;
		}
		catch (IllegalThreadStateException ex) {
			return true;
		}
	}
	// inherit javadoc
	public synchronized JobResult<Void> getJobResult() {
		return mResult;
	}
	// inherit javadoc
	public JobResult<Void> waitForResult() throws InterruptedException {
		mProcess.waitFor();
		//wait for pipe jobs to complete
		for (JobMonitor pipeJob : mPipeJobs) {
			pipeJob.waitForResult();
		}
		synchronized (this) {
			mPipeJobs.clear();
		}
		//wait for result to be set
		while (null == getJobResult()) {
			//process has terminated, so result should quickly be available
			Thread.yield();
		}
		return getJobResult();
	}
	
	/**
	 * Like {@link #waitForResult()}, but if the result was exceptional, an
	 * Exception is thrown. The exception type to be thrown is defined with the
	 * <code>exClass</code> parameter, every exception not being of that type
	 * will be transformed into a {@link RuntimeException} if necessary.
	 * 
	 * @param exClass	The desired exception type, e.g. use 
	 * 					{@link RuntimeException}<tt>.class</tt> that only runtime 
	 * 					exceptions are thrown, or {@link Throwable}<tt>.class</tt>
	 * 					to rethrow every occuring exception unchanged.
	 * @throws E		The exception of the specified type
	 * @throws RuntimeException	If an exception was thrown not being of type E
	 * 							and not being an Error. If it was already a 
	 * 							runtime exception, it is rethrown, if not, it
	 * 							is nested in a runtime exception which is then
	 * 							thrown.
	 * @throws Error	If an error was thrown not being of type E
	 */
	//KEEP IN SYNC WITH JAVADOC RESPECTIVE METHOD IN ExecJob
	@SuppressWarnings("unchecked")
	public <E extends Throwable> void  waitForResultThrowException(Class<E> exClass) throws E {
		try {
			JobResult res = waitForResult();
			if (res.isException()) throw res.getException();
		}
		catch (Throwable ex) {
			if (exClass.isAssignableFrom(ex.getClass())) throw (E)ex;
			if (ex instanceof RuntimeException) throw (RuntimeException)ex;
			if (ex instanceof Error) throw (Error)ex;
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Like {@link #waitForResult()}, but returning a string containing the
	 * stuff which was written to the standard output of the process. If the
	 * process also wrote to the standard error, a 
	 * {@link StdErrNonEmptyException} is thrown. If the process exits with an
	 * exit code unequal to 0, an {@link ExitValueException} is thrown. 
	 * 
	 * @return	the string containing what was written to the standard output of
	 * 			the process
	 * @throws ExitValueException		if the process terminated with an exit
	 * 									value unequal to 0
	 * @throws StdErrNonEmptyException	if the process wrote to the standard
	 * 									error
	 * @throws InterruptedException		if waiting for termination of the 
	 * 									process was interrupted, e.g.
	 * 									by calling {@link #interrupt()}
	 * @throws ExecException			if any other exception occurred during
	 * 									the process execution
	 */
	//KEEP IN SYNC WITH JAVADOC RESPECTIVE METHOD IN ExecJob
	public String waitForStdOutString() throws ExecException, ExitValueException, StdErrNonEmptyException, InterruptedException {
		StringWriter out = new StringWriter();
		StringWriter err = new StringWriter();
		pipeTo(new WriterOutputStream(out), new WriterOutputStream(err), true);
		JobResult<Void> res = waitForResult();
		if (res.isException()) {
			Throwable th = res.getException();
			if (th instanceof ExecException) throw (ExecException)th;
			throw new ExecException(th);
		}
		String errStr = err.toString();
		String outStr = out.toString();
		if (errStr.length() > 0) {
			throw new StdErrNonEmptyException(outStr, errStr);
		}
		return outStr;
	}
	
	/**
	 * @return the standard error of the process, as an input stream
	 */
	public InputStream getErrorStream() {
		if (mProcess == null) return null;
		return mProcess.getErrorStream();
	}
	
	/**
	 * @return the standard output of the process, as an input stream
	 */
	public InputStream getInputStream() {
		if (mProcess == null) return null;
		return mProcess.getInputStream();
	}
	
	/**
	 * @return the standard input of the process, as an output stream
	 */
	public OutputStream getOutputStream() {
		if (mProcess == null) return null;
		return mProcess.getOutputStream();
	}
	
	/**
	 * <p>
	 * Pushes the given lines to the process' output stream, i.e. to the 
	 * standard input. Each line is printed separately, followed by a system
	 * dependant new line separator. After pushing all lines, the standard input
	 * is closed to indicate end of input.
	 * 
	 * <p>Pushing the lines is performed in a separate thread.
	 * 
	 * @param inputLines	The lines to push to the standard input
	 */
	public void pushInput(final String ... inputLines) {
		if (mProcess == null) throw createIllegalThreadStateException();

		final PrintStream ps = Print.createStream(getOutputStream());
		AbstractJob<Void> job = new AbstractJob<Void>() {			
			public Void run() throws Throwable {
				for (String line : inputLines) ps.println(line);
				ps.close();
				return null;
			}
		};
		job.exec();
	}
	
	/**
	 * Pipe the output and error stream of <code>src</code> to the standard
	 * input of the current process.
	 * 
	 * <p>This is a convenience method, a call behaves exactly like calling
	 * {@link #pipeIn(InputStream, boolean)} with ouptut and error stream of
	 * <code>src</code>.
	 * 
	 * <p>Piping is performed in separate threads.
	 * 
	 * @param src				the source process to get the input from
	 * @param closeOnTerminate	if <code>true</code>, standard output and error
	 * 							of <code>src</code> will be closed when the
	 * 							current process terminates
	 */
	public void pipeFrom(ExecJobMonitor src, boolean closeOnTerminate) {
		pipeIn(src.getInputStream(), closeOnTerminate);
		pipeIn(src.getErrorStream(), closeOnTerminate);
	}

	/**
	 * Pipe the output and error stream of the current process to the standard
	 * input of <code>dst</code>.<br/>
	 * 
	 * <p>This is a convenience method, a call behaves exactly like calling
	 * {@link #pipeOut(OutputStream, boolean)} and 
	 * {@link #pipeErr(OutputStream, boolean)} with the standard input of
	 * <code>dst</code>.
	 * 
	 * <p>Piping is performed in separate threads.
	 * 
	 * @param dst				the target process to write the output to
	 * @param closeOnTerminate	if <code>true</code>, standard input of
	 * 							<code>dst</code> will be closed when the
	 * 							current process terminates
	 */
	public void pipeTo(ExecJobMonitor dst, boolean closeOnTerminate) {
		pipeOut(dst.getOutputStream(), closeOnTerminate);
		pipeErr(dst.getOutputStream(), closeOnTerminate);
	}
	
	/**
	 * Pipe the output and error stream of the current process to the given
	 * streams.<br/>
	 * 
	 * <p>This is a convenience method, a call behaves exactly like calling
	 * {@link #pipeOut(OutputStream, boolean)} and 
	 * {@link #pipeErr(OutputStream, boolean)} with the respective streams.
	 * 
	 * <p>Piping is performed in separate threads.
	 * 
	 * @param out				the target stream for standard output
	 * @param err				the target stream for standard error
	 * @param closeOnTerminate	if <code>true</code>, both target streams will 
	 * 							be closed when the current process terminates
	 */
	public void pipeTo(OutputStream out, OutputStream err, boolean closeOnTerminate) {
		pipeOut(out, closeOnTerminate);
		pipeErr(err, closeOnTerminate);
	}

	/**
	 * Read from the given input stream and pipe it to the standard input of the 
	 * current process.<br/>
	 * 
	 * <p>This is a convenience method, a call behaves exactly like calling
	 * {@link #pipeIn(InputStream, boolean)}.
	 * 
	 * <p>Piping is performed in a separate thread.
	 * 	
	 * @param in				the source stream to get the input from
	 * @param closeOnTerminate	if <code>true</code>, the input stream
	 * 							<code>in</code> will be closed when the
	 * 							current process terminates
	 */
	public void pipeFrom(InputStream in, boolean closeOnTerminate) {
		pipeIn(in, closeOnTerminate);
	}
	
	
	/**
	 * Pipe the output stream of the current process to the given stream
	 * 
	 * <p>Piping is performed in a separate thread.
	 * 
	 * @param out				the target stream for standard output
	 * @param closeOnTerminate	if <code>true</code>, the output stream 
	 * 							<code>out</code> will be closed when the current 
	 * 							process terminates
	 */
	public void pipeOut(OutputStream out, boolean closeOnTerminate) {
		if (mProcess == null) throw createIllegalThreadStateException();
		pipe(getInputStream(), out);
		if (closeOnTerminate) {
			synchronized (this) {
				mCloseOutOnTerminate.add(out);
			}
		}			
	}

	/**
	 * Pipe the error stream of the current process to the given stream
	 * 
	 * <p>Piping is performed in a separate thread.
	 * 
	 * @param err				the target stream for standard error
	 * @param closeOnTerminate	if <code>true</code>, the output stream 
	 * 							<code>err</code> will be closed when the current 
	 * 							process terminates
	 */
	public void pipeErr(OutputStream err, boolean closeOnTerminate) {
		if (mProcess == null) throw createIllegalThreadStateException();
		pipe(getErrorStream(), err);
		if (closeOnTerminate) {
			synchronized (this) {
				mCloseOutOnTerminate.add(err);
			}
		}			
	}

	/**
	 * Read from the given input stream and pipe it to the standard input of the 
	 * current process.
	 * 
	 * <p>Piping is performed in a separate thread.
	 * 
	 * @param in				the source stream to get the input from
	 * @param closeOnTerminate	if <code>true</code>, the input stream
	 * 							<code>in</code> will be closed when the
	 * 							current process terminates
	 */
	public void pipeIn(InputStream in, boolean closeOnTerminate) {
		if (mProcess == null) throw createIllegalThreadStateException();
		pipe(in, getOutputStream());
		if (closeOnTerminate) {
			synchronized (this) {
				mCloseInOnTerminate.add(in);
			}
		}			
	}
	
	private void pipe(InputStream in, OutputStream out) {
		JobMonitor<Void> pipeMonitor = new PipeJob(in, out).exec();
		synchronized (this) {
			mPipeJobs.add(pipeMonitor);			
		}
	}
	
	/**
	 * Close the registered streams on termination
	 */
	protected synchronized void closeStreams() {		
		for (InputStream in : mCloseInOnTerminate) {
			try {in.close();} catch (IOException ex) {/*ignore*/}
		}
		for (OutputStream out : mCloseOutOnTerminate) {
			try {out.close();} catch (IOException ex) {/*ignore*/}			
		}
	}

	private IllegalThreadStateException createIllegalThreadStateException() {
		return new IllegalThreadStateException(
			getJobResult().getException().toString()
		);
	}
}
