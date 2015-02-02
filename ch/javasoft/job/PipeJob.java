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

import ch.javasoft.io.Streams;

/**
 * A <tt>PipeJob</tt> pipes an input stream to an output stream, i.e. it writes
 * to the output stream what it reads from the input stream, typically in its
 * own thread.
 */
public class PipeJob extends AbstractJob<Void> {

	private final InputStream	mSrc;
	private final OutputStream 	mDst;
	private final byte[]		mBuf;

	/**
	 * Constructs a new <tt>PipeJob</tt> with the given streams to pipe, using
	 * a buffer of default size (1024 bytes).
	 * 
	 * @param src	the input stream to read from
	 * @param dst	the output stream to write to
	 */
	public PipeJob(InputStream src, OutputStream dst) {
		this(src, dst, new byte[1024]);
	}
	/**
	 * Constructs a new <tt>PipeJob</tt> with the given streams to pipe, using
	 * the given buffer.
	 * 
	 * @param src		the input stream to read from
	 * @param dst		the output stream to write to
	 * @param buffer	the buffer to use when piping input to output
	 */
	public PipeJob(InputStream src, OutputStream dst, byte[] buffer) {
		mSrc = src;
		mDst = dst;
		mBuf = buffer;
	}
	
	//inherit javadoc comments
	public Void run() throws IOException {
		Streams.transfer(mSrc, mDst, mBuf);
		return null;
	}
	
	/**
	 * Pipe the specified input stream to the output stream. The data transfer
	 * is performed in a new thread.
	 * 
	 * @param src	the input stream from which data is read
	 * @param dst	the output stream to write the data to
	 * @return	a monitor for the pipe process
	 */
	public static JobMonitor<Void> pipe(final InputStream src, final OutputStream dst) {
		return pipe(src, dst, new byte[1024]);
	}
	/**
	 * Pipe the specified input stream to the output stream. The data transfer
	 * is performed in a new thread.
	 * 
	 * @param src		the input stream from which data is read
	 * @param dst		the output stream to write the data to
	 * @param buffer	the buffer to use when copying from in to out
	 * @return	a monitor for the pipe process
	 */
	public static JobMonitor<Void> pipe(final InputStream src, final OutputStream dst, final byte[] buffer) {
		return new PipeJob(src, dst, buffer).exec();
	}
	

}
