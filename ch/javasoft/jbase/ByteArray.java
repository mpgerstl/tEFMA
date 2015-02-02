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
package ch.javasoft.jbase;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A <tt>ByteArray</tt> is a byte array which can grow (but newer shrink). 
 * Normally, just bytes are copied to the array, but integers are also supported
 * since it is also used for variable tables. They store raw byte data (of unknown
 * type), but also int indices pointing to other tables. Fixed with tables also 
 * use the byte array to buffer reading/writing from and to the table. 
 * <p>
 * The class is not thread safe, that is, all threads use the same buffers.
 */
class ByteArray extends OutputStream {

    private byte[] bytes;
    private int start 		= 0;
    private int end			= 0;
    
    private ByteArrayInputStream	arrayIn;
    private DataInputStream			dataIn;
    private DataOutputStream		dataOut;
    
    public ByteArray() {
    	this(32);
    }
    public ByteArray(int size) {
    	bytes = new byte[size];
    }
    
    @Override
    public void write(int b) {
    	ensureCapacity(end + 1);
    	bytes[end++] = (byte)b;
    }
    @Override
    public void write(byte[] b) {
    	ensureCapacity(b.length + end);
        System.arraycopy(b, 0, bytes, end, b.length);
        end += b.length;
    }
    @Override
    public void write(byte[] b, int off, int len) {
    	if (off < 0 || len < 0) {
    		throw new IndexOutOfBoundsException("negative index of offset");
    	}
    	if (off + len > b.length) {
    		throw new IndexOutOfBoundsException("off + len > b.length: " + off + ", " + len + ", " + b.length);
    	}
    	ensureCapacity(len + end);
        System.arraycopy(b, off, bytes, end, len);
        end += len;
    }
    private void ensureCapacity(int capacity) {
        if (capacity > bytes.length) {
            final int len = Math.max(2 * bytes.length, capacity);
            final byte[] larger = new byte[len];
            System.arraycopy(bytes, 0, larger, 0, end);
            arrayIn = null;
            dataIn 	= null;
            bytes 	= larger;
        }
    }
    
    public int getStart() {
    	return start;
    }
    public int getEnd() {
    	return end;
    }
    public int getLength() {
    	return end - start;
    }
    public boolean isEmpty() {
    	return start == end;
    }
    
    /**
     * Resets start and end position of this byte array to zero.
     */
    public void reset() {
    	start = 0;
        end = 0;
    }
    
    /**
     * Copies the bytes of this byte array to dest. The number of bytes written
     * to dest is returned, it is dest.length if enough bytes are contained in
     * this byte array. The start position of this array is incremented by the
     * number of bytes copied
     * 
     * @param dest the buffer to copy to
     * @return	the number of copied bytes
     */
    public int copyTo(byte[] dest) {
    	final int len = Math.min(dest.length, end - start);
    	System.arraycopy(bytes, start, dest, 0, len);
    	start += len;
    	return len;
    }
    
    /**
     * Writes the bytes of this byte array to out. The number of bytes written
     * to out is returned, i.e. the length of this byte array. The start 
     * position of this array is incremented by the number of bytes written.
     * 
     * @param out the stream to write to
     * @return	the number of written bytes
     */
    public int writeTo(DataOutput out) throws IOException {
    	final int len = end - start;
    	out.write(bytes, start, len);
    	start += len;
    	return len;
    }
    
    /**
     * Reads len bytes from the data input and stores them in this byte
     * array.
     * 
     * @param in	the data input to read from
     * @param len	the number of bytes to read
     */
    public void readBytesFrom(DataInput in, int len) throws IOException {
    	ensureCapacity(end + len);
    	in.readFully(bytes, end, len);
    	end += len;
    }
    
    /**
     * Returns an input stream containing the bytes of this byte array. 
     * Start and end position of this byte array are reset to zero.
     */
    public ByteArrayInputStream getInputStream() {
		if (arrayIn == null) {
			arrayIn = new ByteArrayInputStream(bytes);    	
		}
		else {
			arrayIn.reset();
			arrayIn.skip(start);
		}
		final ByteArrayInputStream res = arrayIn;
        reset();
        return res;
    }
    
    /**
     * Wraps a {@link DataInputStream} around the array stream returned by
     * {@link #getInputStream()}. Note that old instances might be 
     * reused.
     */
    public DataInputStream getDataInputStream() throws IOException {
		if (dataIn == null) {
			dataIn = new DataInputStream(getInputStream());
		}
		else {
			dataIn.reset();
			dataIn.skip(start);
		}
		return dataIn;
    }

    /**
     * Wraps a {@link DataOutputStream} around this byte array, also being 
     * an output stream. Note that old instances might be reused.
     */
    public DataOutputStream getDataOutputStream() throws IOException {
		if (dataOut == null) {
			dataOut = new DataOutputStream(this);
		}
		return dataOut;
    }
}
