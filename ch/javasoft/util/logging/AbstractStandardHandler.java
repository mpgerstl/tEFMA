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
package ch.javasoft.util.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;

/**
 * Standard err/out handlers, somewhat like {@link ConsoleHandler}, but for
 * both streams.
 */
public class AbstractStandardHandler extends AutoFlushStreamHandler {
    /**
     * Create a <tt>AbstractStandardHandler</tt> with the given output stream.
     */
	public AbstractStandardHandler(OutputStream stream) {
		this(stream instanceof StandardStream ? (StandardStream)stream : new StandardStream(stream));
	}
    /**
     * Create a <tt>AbstractStandardHandler</tt> with the given standard stream.
     */
	public AbstractStandardHandler(StandardStream stream) {
		super();
		setOutputStream(stream);
	}
    /**
     * Create a <tt>AbstractStandardHandler</tt> with the given output stream
     * and formatter
     */
	public AbstractStandardHandler(OutputStream stream, Formatter format) {
		this(stream instanceof StandardStream ? (StandardStream)stream : new StandardStream(stream), format);
	}
    /**
     * Create a <tt>AbstractStandardHandler</tt> with the given standard stream
     * and formatter
     */
	public AbstractStandardHandler(StandardStream stream, Formatter format) {
		super(stream, format);
	}
	/**
	 * A print stream which never closes the underlying stream
	 */
	public static class StandardStream extends PrintStream {
		public StandardStream(OutputStream out) {
			super(out);
		}
		/**
		 * Only perform a flush, but no close
		 */
		@Override
		public void close() {
			flush();
		}
	}
}
