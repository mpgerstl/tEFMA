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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Similar to the {@link java.io.OutputStreamWriter} as a {@link java.io.Writer}
 * printing to an {@link java.io.OutputStream}, this <code>WriterOutputStream</code>
 * is an <code>OutputStream</code> printing to an underlying <code>Writer</code>.
 */
public class WriterOutputStream extends OutputStream {

	private final Writer mWriter;
	private final String mCharsetName;
	
	public WriterOutputStream(Writer writer) {
		mWriter			= writer;
		mCharsetName	= null;
	}
	public WriterOutputStream(Writer writer, String charsetName) throws UnsupportedEncodingException {
		new String(new byte[] {}, charsetName);//force exception if charsetName is not supported
		mWriter			= writer;
		mCharsetName	= charsetName;
	}
	
	/**
	 * @return the name of the character set, or null if not specified
	 */
	public String getCharsetName() {
		return mCharsetName;
	}
	
	@Override
	public void write(int b) throws IOException {
		final String str = mCharsetName == null ? 
			new String(new byte[] {(byte)b}) :
			new String(new byte[] {(byte)b}, mCharsetName);				
		mWriter.write(str);
	}
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		final String str = mCharsetName == null ? 
			new String(b, off, len) :
			new String(b, off, len, mCharsetName);
		mWriter.write(str);
	}
	
	@Override
	public void flush() throws IOException {
		mWriter.flush();
	}
	
	@Override
	public void close() throws IOException {
		mWriter.close();
	}

}
