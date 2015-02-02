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
import java.io.Reader;
import java.io.UnsupportedEncodingException;

/**
 * Similar to the {@link java.io.InputStreamReader} as a {@link java.io.Reader}
 * reading from an {@link java.io.InputStream}, this <code>ReaderInputStream</code>
 * is an <code>InputStream</code> reading from underlying <code>Reader</code>.
 */
public class ReaderInputStream extends InputStream {
	
	private static final byte[] EMPTY_BYTE_BUF = new byte[] {};

	private final Reader mReader;
	private final String mCharsetName;
	private final char[] mCharBuf	= allocateBuffer();
	private byte[] mByteBuf			= EMPTY_BYTE_BUF;
	private int mByteBufIndex		= 0;
	
	public ReaderInputStream(Reader reader) {
		mReader			= reader;
		mCharsetName	= null;
	}
	public ReaderInputStream(Reader reader, String charsetName) throws UnsupportedEncodingException {
		new String(EMPTY_BYTE_BUF, charsetName);//force exception if charsetName is not supported
		mReader			= reader;
		mCharsetName	= charsetName;
	}
	
	/**
	 * @return the name of the character set, or null if not specified
	 */
	public String getCharsetName() {
		return mCharsetName;
	}
	
	/**
	 * Returns a default buffer of size 1024.
	 */
	protected char[] allocateBuffer() {
		return new char[1024];
	}

	@Override
	public int read() throws IOException {
		if (mByteBufIndex == -1) {
			return -1;
		}
		if (mByteBufIndex >= mByteBuf.length) {
			mByteBuf = fillBuffer();
			if (mByteBuf.length == 0) {
				mByteBufIndex = -1;
				return -1;
			}
			mByteBufIndex = 0;
		}
		return mByteBuf[mByteBufIndex++];
	}
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		// from super class START
		if (b == null) {
		    throw new NullPointerException();
		} 
		else if ((off < 0) || (off > b.length) || (len < 0) ||
			   ((off + len) > b.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} 
		else if (len == 0) {
		    return 0;
		}
		// from super class END
		if (mByteBufIndex == -1) {
			return -1;
		}
		int got = 0;
		while (got < len) {
			if (mByteBufIndex >= mByteBuf.length) {
				mByteBuf = fillBuffer();
				if (mByteBuf.length == 0) {
					mByteBufIndex = -1;
					return got == 0 ? -1 : got;
				}
				mByteBufIndex = 0;
			}
			int now = Math.min(len - got, mByteBuf.length - mByteBufIndex);
			System.arraycopy(mByteBuf, mByteBufIndex, b, off + got, now);
			got += now;
			mByteBufIndex += now;
		}
		return got;
	}
	
	private byte[] fillBuffer() throws IOException {
		int chars = mReader.read(mCharBuf);
		if (chars == -1) return EMPTY_BYTE_BUF;
		String str = new String(mCharBuf, 0, chars);
		return mCharsetName == null ? str.getBytes() : str.getBytes(mCharsetName);
	}
	
	@Override
	public void close() throws IOException {
		mReader.close();
	}

}
