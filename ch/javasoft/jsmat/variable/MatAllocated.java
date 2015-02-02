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

/**
 * <tt>MatAllocated</tt> is a variable which allocates space and data. Such 
 * variables are the most simple to use, e.g. to allocate 
 * {@link MatMatrix matrix variables} by passing the data they represent. 
 * <p>
 * For large matrices, {@link MatReserved} variables might be preferable since
 * they only reserve space, but do not allocate it.
 */
abstract public class MatAllocated extends MatVariable {
	
	/**
	 * Constructor for subclasses with matrix class and dimensions
	 * @param matClass	the matrix class constant
	 * @param dims		the matrix dimensions
	 */
	public MatAllocated(MatClass matClass, int[] dims) {
		super(matClass, dims);
	}
	
    /**
     * Write the raw data to the given output, excluding type, size, name etc.
     * This method is implemented by the type specific subclasses. 
     * 
     * @param out			the data output to write to
     * @throws IOException	if any io exception occurs
     */
    protected abstract void writeRawData(DataOutput out) throws IOException;

    /**
     * Writes the data to the given data output. At the end of this method,
     * {@link #writeRawData(DataOutput)} is called to write the type specific 
     * data.
     */
	public void write(String name, DataOutput out) throws IOException {
		writeStart(name, out);
		//write raw data
		writeRawData(out);
	}
    
}
