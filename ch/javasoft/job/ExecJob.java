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

import java.io.File;
import java.io.IOException;

import ch.javasoft.util.Env;

/**
 * An <code>ExecJob</code> represents a command line executable, such as those
 * being passed to {@link Runtime#exec(String)} and related methods.
 * 
 * <p>A sample usage of this class, piping 2 jobs and displaying the results
 * on the standard output/error could look like this:
 * <pre>
 * 	public void pipe2Jobs() throws Exception {
 *		ExecJob job1 = new ExecJob("cat /tmp/efms.txt");
 *		ExecJob job2 = new ExecJob("grep 191");
 *		ExecJobMonitor hdl1 = job1.exec();
 *		ExecJobMonitor hdl2 = job2.exec();
 *		hdl1.pipeTo(hdl2, true);
 *		hdl2.pipeTo(System.out, System.err, false);
 *		hdl2.waitForResult();
 *		System.out.println("DONE.");
 *	}
 * </pre>
 */
public class ExecJob implements Job<ExecJobMonitor>, Executable<Void> {
	
	private final String[]	mCmdArray;
	private final String[]	mEnvp;
	private final File		mDir;
	
    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command
     *
     * <p>This is a convenience method. An invocation of the form
     * <tt>ExecJob(command)</tt>
     * behaves in exactly the same way as the invocation
     * <tt>{@link #ExecJob(String, String[], File) exec}(command, null, null)</tt>.
     *
     * @param   command   a specified system command.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String)
     */
	public ExecJob(String command) {
		this(command, null, null);
	}

    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command and
     * environment.
     *
     * <p>This is a convenience method. An invocation of the form
     * <tt>ExecJob(command, envp)</tt>
     * behaves in exactly the same way as the invocation
     * <tt>{@link #ExecJob(String, String[], File) exec}(command, envp, null)</tt>.
     *
     * @param   command   a specified system command.
     * @param   envp      array of strings, each element of which
     *                    has environment variable settings in the format
     *                    <i>name</i>=<i>value</i>, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the environment of the current process.
     *
     * @throws  NullPointerException if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String, String[])
     * @see		Env
     */
	public ExecJob(String command, String[] envp) {
		this(command, envp, null);
	}
	
    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command and
     * working directory.
     *
     * <p>This is a convenience method. An invocation of the form
     * <tt>ExecJob(command, dir)</tt>
     * behaves in exactly the same way as the invocation
     * <tt>{@link #ExecJob(String, String[], File) exec}(command, null, dir)</tt>.
     *
     * @param   command   a specified system command.
     * @param   dir       the working directory of the subprocess, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the working directory of the current process.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String, String[], File)
     * @see		Env
     */
	public ExecJob(String command, File dir) {
		this(command, null, dir);
	}

    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command,
     * environment and working directory.
     *
     * @param   command   a specified system command.
     * @param   envp      array of strings, each element of which
     *                    has environment variable settings in the format
     *                    <i>name</i>=<i>value</i>, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the environment of the current process.
     * @param   dir       the working directory of the subprocess, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the working directory of the current process.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String, String[], File)
     * @see		Env
     */
	public ExecJob(String command, String[] envp, File dir) {
		this(command.split("\\s+"), envp, dir);
	}
    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command
     *
     * <p>This is a convenience method. An invocation of the form
     * <tt>ExecJob(command)</tt>
     * behaves in exactly the same way as the invocation
     * <tt>{@link #ExecJob(String, String[], File) exec}(command, null, null)</tt>.
     *
     * @param   cmdarray  array containing the command to call and
     *                    its arguments.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String)
     */
	public ExecJob(String[] cmdarray) {
		this(cmdarray, null, null);
	}

    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command and
     * environment.
     *
     * <p>This is a convenience method. An invocation of the form
     * <tt>ExecJob(command, envp)</tt>
     * behaves in exactly the same way as the invocation
     * <tt>{@link #ExecJob(String, String[], File) exec}(command, envp, null)</tt>.
     *
     * @param   cmdarray  array containing the command to call and
     *                    its arguments.
     * @param   envp      array of strings, each element of which
     *                    has environment variable settings in the format
     *                    <i>name</i>=<i>value</i>, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the environment of the current process.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String, String[])
     * @see		Env
     */
	public ExecJob(String[] cmdarray, String[] envp) {
		this(cmdarray, envp, null);
	}
	
    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command and
     * working directory.
     *
     * <p>This is a convenience method. An invocation of the form
     * <tt>ExecJob(command, dir)</tt>
     * behaves in exactly the same way as the invocation
     * <tt>{@link #ExecJob(String, String[], File) exec}(command, null, dir)</tt>.
     *
     * @param   cmdarray  array containing the command to call and
     *                    its arguments.
     * @param   dir       the working directory of the subprocess, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the working directory of the current process.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</tt>
     *
     * @see     Runtime#exec(String, String[], File)
     * @see		Env
     */
	public ExecJob(String[] cmdarray, File dir) {
		this(cmdarray, null, dir);
	}

    /**
     * Constructs an <tt>ExecJob</tt> with the specified string command,
     * environment and working directory.
     *
     * @param   cmdarray  array containing the command to call and
     *                    its arguments.
     * @param   envp      array of strings, each element of which
     *                    has environment variable settings in the format
     *                    <i>name</i>=<i>value</i>, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the environment of the current process.
     * @param   dir       the working directory of the subprocess, or
     *                    <tt>null</tt> if the subprocess should inherit
     *                    the working directory of the current process.
     *
     * @throws  NullPointerException	if <tt>command</tt> is <tt>null</code>
     *
     * @see     Runtime#exec(String, String[], File)
     * @see		Env
     */
	public ExecJob(String[] cmdarray, String[] envp, File dir) {
		if (cmdarray == null) throw new NullPointerException();
		mCmdArray	= cmdarray;
		mEnvp		= envp;
		mDir		= dir;
	}
	
	/** @return the object itself (<code>this</code>) */
	public Object id() {
		return this;
	}

	// inherit javadoc
	public ExecJobMonitor run() throws IOException {
		Runtime rt = Runtime.getRuntime();
		final Process proc;
		if (mEnvp == null && mDir == null) {
			proc = rt.exec(mCmdArray);
		}
		else if (mEnvp == null) {
			proc = rt.exec(mCmdArray, null, mDir);
		}
		else if (mDir == null) {
			proc = rt.exec(mCmdArray, mEnvp);
		}
		else {
			proc = rt.exec(mCmdArray, mEnvp, mDir);
		}
		return new ExecJobMonitor(proc);
	}
	
	// inherit javadoc
	public ExecJobMonitor exec() {
		return ExecJobProcessor.INSTANCE.exec(this);
	}
	// inherit javadoc
	public ExecJobMonitor exec(JobTerminationHandler<Void> terminationHandler) {
		final ExecJobProcessor proc = new ExecJobProcessor();
		proc.addJobTerminatedHandler(terminationHandler);
		return proc.exec(this);
	}
	// inherit javadoc
	public JobResult<Void> execAndWait() throws InterruptedException {
		return ExecJobProcessor.INSTANCE.execAndWait(this);
	}
	// inherit javadoc
	public Void execAndWaitThrowException() throws InterruptedException, Throwable {
		final JobResult<Void> result = execAndWait();
		if (result.isException()) throw result.getException();
		return result.getResult();
	}
	
	/**
	 * Like {@link #execAndWait()}, but if the result was exceptional, an
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
	//KEEP IN SYNC WITH JAVADOC OF CALLED METHOD
	public <E extends Throwable> void  execWaitForResultThrowException(Class<E> exClass) throws E {
		exec().waitForResultThrowException(exClass);
	}
	
	/**
	 * Like {@link #execAndWait()}, but returning a string containing the
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
	 * 									by calling {@link ExecJobMonitor#interrupt()}
	 * @throws ExecException			if any other exception occurred during
	 * 									the process execution
	 */
	//KEEP IN SYNC WITH JAVADOC OF CALLED METHOD
	public String execWaitForStdOutString() throws ExecException, ExitValueException, StdErrNonEmptyException, InterruptedException {
		return exec().waitForStdOutString();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mCmdArray.length; i++) {
			if (i > 0) sb.append(' ');
			sb.append(mCmdArray[i]);
		}
		return sb.toString();
	}


}
