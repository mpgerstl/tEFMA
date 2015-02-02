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
package ch.javasoft.jsmat.primitive;

import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.jsmat.common.MatType;

/**
 * <tt>MatPrimitive</tt> is the abstract superclass for all primitive matlab 
 * types. These classes are used to read and write raw data. Typically, data
 * consists of a type flag (4 bytes), the size of the block (4 bytes), the raw
 * data block and some padding bytes if needed.
 */
abstract public class MatPrimitive {

	private final MatType mMatType;
	
	/**
	 * Constructor with type constant
	 * 
	 * @param type	the type constant
	 */
	public MatPrimitive(MatType type) {
		mMatType = type;
	}
	
    /**
     * @return	size of raw data (bytes) excluding padding, type(4) and size(4),
     * 			that is {@link #getUnitSize()} times {@link #getArrayLength()}
     */
    public int getRawDataSize() {
    	return getUnitSize() * getArrayLength();
    }
    
    /**
     * @return	the logical data length, that is {@link #getRawDataSize()} 
     * 			divided by {@link #getUnitSize()}. this is usually the length
     * 			of the array storing the data in java
     */
    abstract public int getArrayLength();
    
    public static void writeStart(MatType mType, DataOutput out, int rawDataSize) throws IOException {
    	out.writeInt(mType.type);
    	out.writeInt(rawDataSize);
    }
    
   /**
     * Writes the raw data block, without padding, type and size information
     * 
     * @param out			the data output to write to
     * @throws IOException	if any io exception occurs
     */
    abstract protected void writeBody(DataOutput out) throws IOException;
     
    public static void writeEnd(MatType mType, DataOutput out, int rawDataSize) throws IOException {
    	int padding = getPaddingSize(rawDataSize, mType.size, false /*compressed*/);
    	out.write(new byte[padding]);
    }
    
    /**
     * @return	the total size of this data block, including type, size and
     * 			padding (if any)
     */
    public int getSize() {
    	int rawSize = getRawDataSize();
    	int padding = getPaddingSize(rawSize, false /*compressed*/);
    	return 4 /*type*/ + 4 /*size*/ + rawSize + padding;
    }
    
    /**
     * Writes the data block: type, raw size, raw data, padding (this order).
     * To write the raw data, which is type specific (implemented in 
     * subclasses), {@link #writeBody(DataOutput)} is called.
     * <p>
     * Equivalent to calling
     * <pre>
     *  writeStart(out);
     *	writeBody(out);
     *	writeEnd(out);
     * </pre>
     * 
     * @param out			the data output to write to
     * @throws IOException	if any io exception occurs
     */
    public void write(DataOutput out) throws IOException {
    	final int rawDataSize = getRawDataSize();
    	writeStart(mMatType, out, rawDataSize);
    	writeBody(out);
    	writeEnd(mMatType, out, rawDataSize);
    }
    
    /**
     * @return	padding size for a raw block of given size
     */
    private int getPaddingSize(int size, boolean compressed) {
    	return getPaddingSize(size, getUnitSize(), compressed);
    }
    /**
     * @return	padding size for a raw block of given size
     */
    private static int getPaddingSize(int size, int unitSize, boolean compressed) {
        int padding;
        //data not packed in the tag
        if ( !compressed )
        {    
            int b;
            padding = ( b = ( ((size/unitSize)%(8/unitSize))*unitSize ) ) !=0   ? 8-b : 0;
        }
        else //data _packed_ in the tag (compressed)
        {
            int b;
            padding = ( b = ( ((size/unitSize)%(4/unitSize))*unitSize ) ) !=0   ? 4-b : 0;
        }
        return padding;
    }
    
    /**
     * @return 	the unit size, that is, the number of bytes used to store one
     * 			value of this type. see {@link MatType#size}
     */
    protected int getUnitSize() {
    	return mMatType.size;
    }
    
}
