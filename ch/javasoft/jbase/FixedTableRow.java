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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.ReadWriteLock;

import ch.javasoft.jbase.concurrent.Stateful;


/**
 * <tt>FixedTableRow</tt> is used by {@link VariableWidthTable}. It represents
 * an entity or row of the underlying {@link FixedWidthTable fixed with tables}.
 * A fixed table row carries data (raw bytes and int pointers), but at the same
 * time acts as entity marshaller for itself. This means that is takes the role
 * of a reused buffer and marshaller at the same time.
 * <p>
 * The class is not thread safe, that is, every thread uses the same buffers.
 */
class FixedTableRow implements FixedWidthMarshaller<FixedTableRow>, Stateful {
	
	final byte[]	bytes;
	final int[]		ints;
	
	private boolean indexOnlyMode = false;
	
	public FixedTableRow(final int byteArrayLength, final int intCount) {
		this.bytes 	= new byte[byteArrayLength];
		ints	= new int[intCount];
	}
	
	public static FixedTableRow getByTotalByteWidth(int width, int indexCount) {
		return new FixedTableRow(width - indexCount*4, indexCount);
	}
	public static FixedTableRow getByByteArrayLength(int byteArrayLength, int indexCount) {
		return new FixedTableRow(byteArrayLength, indexCount);
	}
	
	public int getByteArrayLength() {
		return bytes.length;
	}
	public int getByteWidth() {
		return bytes.length + ints.length * 4;
	}
	
	/**
	 * Index only mode means that the raw bytes are not read or written, only 
	 * the int indices. This is useful when indices have to be read or updated
	 * when removing elements from the tables.
	 */
	public boolean isIndexOnlyMode() {
		return indexOnlyMode;		
	}
	/**
	 * Index only mode means that the raw bytes are not read or written, only 
	 * the int indices. This is useful when indices have to be read or updated
	 * when removing elements from the tables.
	 * <p>
	 * Note: setting index only mode to true should always have an associated
	 * 		 resetting operation to false, since only one single instance of
	 *       <tt>FixedTableRow</tt> is used for each fixed with table.
	 */
	public void setIndexOnlyMode(boolean indexOnly) {
		this.indexOnlyMode = indexOnly;
	}
	public void writeTo(FixedTableRow entity, DataOutput out) throws IOException {
		for (int i = 0; i < ints.length; i++) {
			out.writeInt(ints[i]);
		}
		if (!indexOnlyMode) {
			out.write(bytes);
		}
	}
	public FixedTableRow readFrom(DataInput in) throws IOException {
		for (int i = 0; i < ints.length; i++) {
			ints[i] = in.readInt();
		}
		if (!indexOnlyMode) {
			in.readFully(bytes);
		}
		return this;
	}
	
	public void putBytesTo(ByteArray byteArray) {
		byteArray.write(bytes);
	}
	public int getBytesFrom(ByteArray byteArray) {
		return byteArray.copyTo(bytes);
	}
	public int getInt(int index) {
		return ints[index];
	}
	public void setInt(int index, int value) {
		ints[index] = value;
	}
    @Override
    public String toString() {
        return "{" + Arrays.toString(ints) + Arrays.toString(bytes) + "}";
    }
    
    /**
     * Returns a clone of this fixed table row
     */
    public FixedTableRow createReadCopy(ReadWriteLock lock) throws IOException {
        final FixedTableRow clone = new FixedTableRow(bytes.length, ints.length);
        System.arraycopy(bytes, 0, clone.bytes, 0, bytes.length);
        System.arraycopy(ints, 0, clone.ints, 0, ints.length);
        clone.indexOnlyMode = indexOnlyMode;
        return clone;
    }
	
}
