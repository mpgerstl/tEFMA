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

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * The <tt>AbstractDataOutput</tt> class implements most methods of
 * {@link DataOutput}, leaving only the following methods for subclasses: 
 * <ul>
 * <li>{@link #write(int)}</li>
 * <li>{@link #write(byte[], int, int)}</li>
 * </ul>
 * <p>
 * This class is thread safe.
 */
abstract public class AbstractDataOutput implements DataOutput {

	public void write(byte[] b) throws IOException {
		write(b, 0, b.length); 
	}

	public void writeBoolean(boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	public void writeByte(int v) throws IOException {
		write(v);
	}

	public void writeBytes(String s) throws IOException {
		write(s.getBytes());
	}

	public void writeChar(int v) throws IOException {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

	public void writeChars(String s) throws IOException {
		final int clen = s.length();
		final int blen = 2*clen;
		final byte[] b = new byte[blen];
		final char[] c = new char[clen];
		s.getChars(0, clen, c, 0);
		for (int i = 0, j = 0; i < clen; i++) {
		    b[j++] = (byte)(c[i] >>> 8);
		    b[j++] = (byte)(c[i] >>> 0);
		}
		write(b, 0, blen);
	}

	public void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	public void writeFloat(float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	public void writeInt(int v) throws IOException {
		write((v >>> 24) & 0xFF);
		write((v >>> 16) & 0xFF);
		write((v >>>  8) & 0xFF);
		write((v >>>  0) & 0xFF);
	}

	public void writeLong(long v) throws IOException {
		write((int)(v >>> 56) & 0xFF);
		write((int)(v >>> 48) & 0xFF);
		write((int)(v >>> 40) & 0xFF);
		write((int)(v >>> 32) & 0xFF);
		write((int)(v >>> 24) & 0xFF);
		write((int)(v >>> 16) & 0xFF);
		write((int)(v >>>  8) & 0xFF);
		write((int)(v >>>  0) & 0xFF);
	}

	public void writeShort(int v) throws IOException {
		write((v >>> 8) & 0xFF);
		write((v >>> 0) & 0xFF);
	}

    /**
     * bytearr is initialized on demand by writeUTF
     */
    private ThreadLocal<byte[]> bytearr = new ThreadLocal<byte[]>(); 
	public void writeUTF(String str) throws IOException {
        writeUTF(str, this);
	}
	
    /**
     * Copied from {@link DataOutputStream#writeUTF(String)}, but we use a 
     * thread safe array instead.
     * <p>
     * 
     * Writes a string to the specified DataOutput using
     * <a href="DataInput.html#modified-utf-8">modified UTF-8</a>
     * encoding in a machine-independent manner. 
     * <p>
     * First, two bytes are written to out as if by the <code>writeShort</code>
     * method giving the number of bytes to follow. This value is the number of
     * bytes actually written out, not the length of the string. Following the
     * length, each character of the string is output, in sequence, using the
     * modified UTF-8 encoding for the character. If no exception is thrown, the
     * counter <code>written</code> is incremented by the total number of 
     * bytes written to the output stream. This will be at least two 
     * plus the length of <code>str</code>, and at most two plus 
     * thrice the length of <code>str</code>.
     *
     * @param      str   a string to be written.
     * @param      out   destination to write to
     * @return     The number of bytes written out.
     * @exception  IOException  if an I/O error occurs.
     */
    protected static int writeUTF(String str, AbstractDataOutput out) throws IOException {
        int strlen = str.length();
    	int utflen = 0;
    	int c, count = 0;
     
            /* use charAt instead of copying String to char array */
    	for (int i = 0; i < strlen; i++) {
                c = str.charAt(i);
    	    if ((c >= 0x0001) && (c <= 0x007F)) {
    		utflen++;
    	    } else if (c > 0x07FF) {
    		utflen += 3;
    	    } else {
    		utflen += 2;
    	    }
    	}

    	if (utflen > 65535)
    	    throw new UTFDataFormatException(
                    "encoded string too long: " + utflen + " bytes");

        byte[] bytearr = out.bytearr.get();
        if(bytearr == null || (bytearr.length < (utflen+2))) {
        	out.bytearr.set(bytearr = new byte[(utflen*2) + 2]);
        }
     
    	bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
    	bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);  
        
        int i=0;
        for (i=0; i<strlen; i++) {
           c = str.charAt(i);
           if (!((c >= 0x0001) && (c <= 0x007F))) break;
           bytearr[count++] = (byte) c;
        }
    	
    	for (;i < strlen; i++){
                c = str.charAt(i);
    	    if ((c >= 0x0001) && (c <= 0x007F)) {
    	    	bytearr[count++] = (byte) c;                   
    	    } 
    	    else if (c > 0x07FF) {
	    		bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
	    		bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
	    		bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
    	    } 
    	    else {
	    		bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
	    		bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
    	    }
    	}
        out.write(bytearr, 0, utflen+2);
        return utflen + 2;
    }

}
