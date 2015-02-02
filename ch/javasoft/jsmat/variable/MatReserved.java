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

import ch.javasoft.jsmat.MatWriter;
import ch.javasoft.jsmat.ReservedMatrixWriter;
import ch.javasoft.jsmat.ReservedWriter;
import ch.javasoft.jsmat.common.MatClass;
import ch.javasoft.jsmat.common.MatType;
import ch.javasoft.jsmat.primitive.MatPrimitive;

/**
 * <tt>MatReserved</tt> is a variable which reserves space for data, but does
 * not store (and allocate) the data itself. Such variables are somewhat more
 * complicated to use than {@link MatAllocated variables allocating data}, but
 * save memory since data is written piecewise.
 *  
 * @see MatWriter#createReservedWriter(String, MatReservedMatrix)
 * @see MatWriter#createReservedWriter(String, MatReservedComplex)
 * @see ReservedWriter
 * @see ReservedMatrixWriter
 */
abstract public class MatReserved extends MatVariable {
	
	public MatReserved(MatClass matClass, int[] dims) {
		super(matClass, dims);
	}
	
	/**
	 * Called when {@link ReservedWriter#open(String)} is called.
	 * Only the start of the data block is written here 
	 * (see {@link MatPrimitive#writeStart(MatType, DataOutput, int)}).
	 * The real data is appended by calling
	 * {@link ReservedMatrixWriter#append(Object)}
	 * The write process is terminated with {@link ReservedWriter#close()}, 
	 * where {@link #writeDataBlockEnd(DataOutput)} is called.
	 */
	abstract public void writeDataBlockStart(String name, DataOutput out) throws IOException;
	
	/**
	 * Called when the write process of the reserved variable is about to
	 * complete, that is, when {@link ReservedWriter#close()} is called.
	 * @see #writeDataBlockStart(String, DataOutput) 
	 */
	abstract public void writeDataBlockEnd(DataOutput out) throws IOException;
}
