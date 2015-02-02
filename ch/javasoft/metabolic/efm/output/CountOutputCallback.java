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
package ch.javasoft.metabolic.efm.output;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.io.Print;
import ch.javasoft.util.logging.LogPrintWriter;

/**
 * The <tt>CountOutputCallback</tt> outputs the number of elementary modes,
 * compressed or uncompressed to any stream or writer.
 * <p>
 * In opposition to the {@link NullOutputCallback}, it receives an event for
 * every mode and updates the mode counter (which can be fetched after 
 * completion of the output process).
 * 
 * <p>Sample output of the <tt>CountOutputCallback<tt>:
 * <pre>
 * 27100 elementary modes.
 * </pre>
 */
public class CountOutputCallback implements EfmOutputCallback {

	protected final PrintWriter	mPw;
	protected final boolean		mUncompress;
	
	protected final AtomicLong cnt = new AtomicLong();

	/**
	 * Logs the efm count (compressed or uncompressed efms) with the package 
	 * logger and log level {@link Level#INFO}
	 *  
	 * @param uncompress Whether or not to uncompress the efms before counting
	 */
	public CountOutputCallback(boolean uncompress) {
		this(LogPkg.LOGGER, Level.INFO, uncompress);
	}

	/**
	 * Logs the efm count (compressed or uncompressed efms) with 
	 * the given logger and log level 
	 *  
	 * @param logger		The logger to log with
	 * @param level			The desired log level
	 * @param uncompress	Whether or not to uncompress the efms before counting
	 */
	public CountOutputCallback(Logger logger, Level level, boolean uncompress) {
		this(new LogPrintWriter(logger, level), uncompress);
	}

	/**
	 * Prints the efm count (compressed or uncompressed efms) to the given
	 * output stream
	 *  
	 * @param uncompress Whether or not to uncompress the efms before counting
	 */
	public CountOutputCallback(OutputStream out, boolean uncompress) {
		mPw			= Print.createWriter(out);
		mUncompress	= uncompress;
	}

	/**
	 * Prints the efm count (compressed or uncompressed efms) to the given
	 * writer
	 *  
	 * @param uncompress Whether or not to uncompress the efms before counting
	 */
	public CountOutputCallback(Writer writer, boolean uncompress) {
		mPw 		= Print.createWriter(writer);
		mUncompress	= uncompress;
	}
	
	public boolean isUncompress() {
		return mUncompress;
	}
	
	public void callback(EfmOutputEvent evt) {			
		switch(evt.getKind()) {
		case PRE:
			cnt.set(0);
			break;
		case EFM_OUT:
			cnt.incrementAndGet();
			break;
		case POST:
			mPw.println(cnt + " elementary modes");
			mPw.flush();
			break;
		default:						
			//no such kind
			throw new RuntimeException("unknown kind " + evt.getKind());					
		}
	}
	
	/**
	 * Returns the actual number of elementary modes. It is safe to call this
	 * method after the POST event. At any other time, the return value is
	 * undefined.
	 */
	public long getEfmCount() {
		return cnt.get();
	}
	
	public CallbackGranularity getGranularity() {
		return mUncompress ? CallbackGranularity.CountUncompressed : CallbackGranularity.CountCompressed;
	}
	
	public boolean allowLoggingDuringOutput() {
		return true;
	}
	
	/**
	 * Returns {@code true}
	 * @see EfmOutputCallback#isThreadSafe()
	 */
	public boolean isThreadSafe() {
		return true;
		// return false;
	}
	
}
