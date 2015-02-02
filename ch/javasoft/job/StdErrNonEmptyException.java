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

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * An exception which is thrown by {@link ExecJob#execWaitForStdOutString()} or
 * {@link ExecJobMonitor#waitForStdOutString()} respectively if the process
 * was writing to the error stream.<br/>
 * Both standard out and standard error output are available, either as
 * String or with {@link BufferedReader}s allowing linewise accessing the
 * output.
 */
public class StdErrNonEmptyException extends ExecException {
	
	private static final long serialVersionUID = 1L;
	private final String mStdOut, mStdErr;
	
	public StdErrNonEmptyException(String stdOut, String stdErr) {
		super(stdErr);
		mStdOut = stdOut;
		mStdErr = stdErr;
	}
	
	/**
	 * @return	the standard error as a string
	 */
	public String getStdErr() {
		return mStdErr;
	}
	
	/**
	 * @return	The standard error as buffered reader. Each call to this 
	 * 			method creates a new reader instance. 
	 */
	public BufferedReader getStdErrReader() {
		return new BufferedReader(new StringReader(mStdErr));
	}
	
	/**
	 * @return	the standard output as a string
	 */
	public String getStdOut() {
		return mStdOut;
	}
	
	/**
	 * @return	The standard error as buffered reader. Each call to this 
	 * 			method creates a new reader instance. 
	 */
	public BufferedReader getStdOutReader() {
		return new BufferedReader(new StringReader(mStdOut));
	}
}
