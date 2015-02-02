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
package ch.javasoft.jsmat.variable;

import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.jsmat.common.MatClass;
import ch.javasoft.jsmat.common.MatFlag;
import ch.javasoft.jsmat.common.MatType;
import ch.javasoft.jsmat.primitive.MatInt32;
import ch.javasoft.jsmat.primitive.MatUtf8;

/**
 * <tt>MatVariable</tt> is the superclass of all (non primitive) matlab data 
 * types. 
 * <p>
 * Most subclasses are matrices of different primitive type, such as
 * {@link MatDoubleMatrix} or {@link MatCharMatrix}. However, some special cases
 * exist, such as {@link MatCell}, {@link MatStructure} etc.
 * <p>
 * Note that for matrices, the data is column packed (FORTRAN like), that is,
 * the first column is written, then the second etc (column by column, not row
 * by row as in java or c). For m dimensions, the first dimension is iterated
 * first (i.e. the rows), then the second (columns), third etc. (again, opposite
 * iteration order than java or c). 
 */
abstract public class MatVariable {
	
	protected static final String DEFAULT_NAME = "@";
	
	protected MatClass	mMatClass;
	protected int 		mDims[];
	private int 		mAttriubtes;
	
	/**
	 * Constructor with matrix class constant and dimensions
	 * 
	 * @param matClass	matrix class constant
	 * @param dims		dimensions
	 */
	public MatVariable(MatClass matClass, int[] dims) {
		mMatClass	= matClass;
		mDims		= dims;
		//check dim overflow
		long dim = mDims.length == 0 ? 0 : mDims[0];
		for (int i = 1; i < mDims.length; i++) {
			dim *= mDims[i];
		}
		if (dim > Integer.MAX_VALUE) {
			throw new ArithmeticException("integer overflow for matrix size: " + dim);
		}
	}
	
    /**
     * @return product of all dimensions
     */
	public int getDimLength() {
		int dim = mDims.length == 0 ? 0 : mDims[0];
		for (int i = 1; i < mDims.length; i++) {
			dim *= mDims[i];
		}
		return dim;
	}
	
	/**
	 * Check whether the given <tt>len</tt> is equal to the expected length,
	 * computed from the dimensions (see {@link #getDimLength()}). 
	 * <p>
	 * If the check fails, an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param len	the actual value count, to be compared with the reserved
	 * 				size computed from the dimensions
	 * @throws IllegalArgumentException if the check fails
	 */
	protected void checkValueLength(int len) throws IllegalArgumentException {
		if (len != getDimLength()) {
			throw new IllegalArgumentException("expected " + getDimLength() + " values, but found " + len);
		}
	}
	
    public int getFlags() { 
        int flags = mMatClass.type & MatFlag.MASK | mAttriubtes & 0xffffff00;
        
        return flags;
    }

    /**
     * @return size of raw data excluding type(4) and size(4) and name
     */
    abstract protected int getRawDataSize();

    /**
     * Throws an exception if the raw data size is larger than 
     * Integer.MAX_VALUE. The raw data size is computed from the given type size
     * times the overall dimension length.
     * 
     * @param typeSize				the size of a single value
     * @throws ArithmeticException	if the computed size is out of integer range
     */
    protected void checkRawDataSizeOverflow(int typeSize) throws ArithmeticException {
		long size = ((long)typeSize) * getDimLength();
		if (size > Integer.MAX_VALUE) {
			throw new ArithmeticException("integer overflow for raw data size: " + size);
		}
    	
    }

    /**
     * @return size of all data including flags, dimensions, names, etc.
     */
    public int getSize(String name) {
    	int flagSize	= new MatInt32(new int[2]).getSize();
    	int dimSize 	= new MatInt32(mDims).getSize();
    	int namSize		= new MatUtf8(name).getSize();
    	int rawSize		= getRawDataSize();
    	return flagSize + dimSize + namSize + rawSize;
    }
    
    /**
     * Writes the start of the data block to the given data output. 
     */
	protected void writeStart(String name, DataOutput out) throws IOException {
		out.writeInt(MatType.MATRIX.type);
		out.writeInt(getSize(name));
		//write flags		
		writeFlags(out);
		//write dims
		new MatInt32(mDims).write(out);
		//write name
		new MatUtf8(name).write(out);
	}

	/**
     * Writes MATRIX flags into <code>out</code>.
     */
    private void writeFlags(DataOutput out) throws IOException
    {
    	int maxNZ = 0;
//      if ( array.isSparse() )
//      {
//          maxNZ =  ((MLSparse)array).getMaxNZ();
//      }
    	
		new MatInt32(new int[] {getFlags(), maxNZ}).write(out);        
    }
    
}
