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
package ch.javasoft.jbase.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The <tt>AbstractDataInput</tt> class implements most methods of
 * {@link DataInput}, leaving only the following methods for subclasses: 
 * <ul>
 * <li>{@link #read()}</li>
 * <li>{@link #peek()}</li>
 * <li>{@link #read(byte[], int, int)}</li>
 * <li>{@link #skipBytes(int)}</li>
 * </ul>
 * <p>
 * This class is thread safe.
 */
abstract public class AbstractDataInput implements DataInput {

    /**
     * Reads a byte of data from this file. The byte is returned as an 
     * integer in the range 0 to 255 (<code>0x00-0x0ff</code>). This 
     * method should block if no input is yet available. 
     * <p>
     * Although <code>AbstractDataInput</code> is not a subclass of 
     * <code>InputStream</code>, this method behaves in exactly the same 
     * way as the {@link InputStream#read()} method of 
     * <code>InputStream</code>.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             input has been reached.
     * @exception  IOException  if an I/O error occurs. Not thrown if  
     *                          end-of-file has been reached.
     */
	abstract protected int read() throws IOException;

	/**
	 * Like {@link #read()}, but the value is read again, that is, the position
	 * is not moved forward. This method is used by {@link #readLine()}, if
	 * subclasses cannot support this method, an i/o exception should be thrown,
	 * causing that {@link #readLine()} will also throw an i/o exception.
	 * 
	 * @throws IOException	if peek operations are not supported
	 */
	abstract protected int peek() throws IOException;
	
    /**
     * Reads a sub array as a sequence of bytes. 
     * @param b the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the number of bytes to read.
     * @exception IOException If an I/O error has occurred.
     */
    abstract protected int read(byte b[], int off, int len) throws IOException;

	abstract public int skipBytes(int n) throws IOException;

	public boolean readBoolean() throws IOException {
		int ch = read();
		if (ch < 0)
		    throw new EOFException();
		return (ch != 0);
	}

	public byte readByte() throws IOException {
		int ch = this.read();
		if (ch < 0)
		    throw new EOFException();
		return (byte)(ch);
	}

	public char readChar() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
		    throw new EOFException();
		return (char)((ch1 << 8) + (ch2 << 0));
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	public void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
    	do {
    	    int count = read(b, off + n, len - n);
    	    if (count < 0)
    		throw new EOFException(count + " < 0, n=" + n + ", len=" + len);
    	    n += count;
    	} while (n < len);
	}

	public int readInt() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		int ch3 = this.read();
		int ch4 = this.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
		    throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	final ThreadLocal<char[]> lineBuffer = new ThreadLocal<char[]>();
	/**
	 * Note that this method throws an i/o exception if the {@link #peek()} 
	 * method is not supported.
	 * 
	 * @see #peek()
	 * @see DataInput#readLine()
	 */
	public String readLine() throws IOException {
		//check support of readLine by calling peek;
		peek();
		
		char buf[] = lineBuffer.get();

		if (buf == null) {
		    lineBuffer.set(buf = new char[128]);
		}

		int room = buf.length;
		int offset = 0;
		int c;

	loop:	while (true) {
		    switch (c = read()) {
		      case -1:
		      case '\n':
			break loop;

		      case '\r':
			int c2 = peek();
			if ((c2 == '\n')) {
				c2 = read();//consume it
			}
			break loop;

		      default:
			if (--room < 0) {
			    buf = new char[offset + 128];
			    room = buf.length - offset - 1;
			    System.arraycopy(lineBuffer.get(), 0, buf, 0, offset);
			    lineBuffer.set(buf);
			}
			buf[offset++] = (char) c;
			break;
		    }
		}
		if ((c == -1) && (offset == 0)) {
		    return null;
		}
		return String.copyValueOf(buf, 0, offset);
	}

	public long readLong() throws IOException {
		return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}

	public short readShort() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
		    throw new EOFException();
		return (short)((ch1 << 8) + (ch2 << 0));
	}

	public String readUTF() throws IOException {
		return DataInputStream.readUTF(this);
	}

	public int readUnsignedByte() throws IOException {
		int ch = this.read();
		if (ch < 0)
		    throw new EOFException();
		return ch;
	}

	public int readUnsignedShort() throws IOException {
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
		    throw new EOFException();
		return (ch1 << 8) + (ch2 << 0);
	}

}
