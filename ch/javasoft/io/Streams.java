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
package ch.javasoft.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class with static methods related to input/output streams
 */
public class Streams {

	/**
	 * Reads from <code>src</code> and writes to <code>dst</code>, using a 
	 * byte buffer of size 1024.
	 * 
	 * @param src			source input stream to read from
	 * @param dst			destination output stream to write to
	 * 
	 * @throws IOException	if reading from the input stream or writing to
	 * 						the output stream throws such an exception
	 */
	public static void transfer(InputStream src, OutputStream dst) throws IOException {
		byte[] buffer = new byte[1024];
		transfer(src, dst, buffer);
	}

	/**
	 * Reads from <code>src</code> and writes to <code>dst</code>, using  
	 * the given byte buffer.
	 * 
	 * @param src			source input stream to read from
	 * @param dst			destination output stream to write to
	 * @param buffer		the byte buffer to use for the byte transfer
	 * 
	 * @throws IOException	if reading from the input stream or writing to
	 * 						the output stream throws such an exception
	 */
	public static void transfer(InputStream src, OutputStream dst, byte[] buffer) throws IOException {
		int size;
		while ((size = src.read(buffer)) != -1) {
			dst.write(buffer, 0, size);
		}
		dst.flush();
	}
	
	//no instances
	private Streams() {
		super();
	}

}
