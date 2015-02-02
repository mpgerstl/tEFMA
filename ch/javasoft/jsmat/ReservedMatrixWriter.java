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
package ch.javasoft.jsmat;

import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.jsmat.variable.MatReservedMatrix;

/**
 * Writer to write simple reserved variables (matrices). 
 * Extends {@link ReservedWriter} mainly by adding the {@link #append(Object)}
 * method to write data to the variable.
 * 
 * @type <A>	the data type of the matrix
 */
public class ReservedMatrixWriter<A /*data type*/> extends ReservedWriter {
	
	private final MatReservedMatrix<A> mReservedMatrix;
	
	private int mWrittenLength = 0;//-1 if writer is closed
	
	/**
	 * Constructor to write to complex types, such as structs.
	 * 
	 * @param complexWriter		the writer for the complex type
	 * @param reservedMatrix	the reserved variable of the nested type, e.g. a 
	 * 							field of a struct
	 */
	public ReservedMatrixWriter(ReservedComplexWriter complexWriter, MatReservedMatrix<A> reservedMatrix) {
		super(complexWriter, reservedMatrix);
		mReservedMatrix = reservedMatrix;
	}

	/**
	 * Constructor to write to top-level variables.
	 *  
	 * @param writer			the mat writer, e.g. writing a matlab file
	 * @param reservedMatrix	the reserved variable
	 */
	public ReservedMatrixWriter(MatWriter<? extends DataOutput> writer, MatReservedMatrix<A> reservedMatrix) {
		super(writer, reservedMatrix);
		mReservedMatrix = reservedMatrix;
	}

	/**
	 * Append data to the reserved variable. Note that all together, the exact
	 * size as specified by the reserved variable has to be written, or an
	 * io exception will be thrown.
	 * 
	 * @param data			the data to append
	 * @throws IOException	if the writer has already been closed, or if the
	 * 						write operation appended more data to the variable
	 * 						then reserved, or if any other io exception occurrs
	 */
	public void append(A data) throws IOException {
		if (mWrittenLength < 0) {
			throw new IOException("reserved writer has already been closed");
		}
		mWrittenLength += mReservedMatrix.appendData(mDataOutput, data);
		if (mWrittenLength > mReservedMatrix.getDimLength()) {
			throw new IOException(
				"appended more data than reserved size permits: " + 
				mWrittenLength + " > " + mReservedMatrix.getDimLength()
			);
		}
	}
	
	/**
	 * Closes the writer and completes writing of the corresponding reserved 
	 * variable.
	 * <p>
	 * Before invoking the {@link ReservedWriter#close() super.close()}, the 
	 * written data length is checked and an exception is thrown if the length 
	 * differs from the reserved size.
	 *  
	 * @throws IOException	if the length of the written data is not as 
	 * 						expected, that is, not as predefined by the reserved
	 * 						variable, or if any other io exception occurrs
	 */
	@Override
	public void close() throws IOException {
		if (mWrittenLength != mReservedMatrix.getDimLength()) {
			throw new IOException(
				"appended data length differs from reserved size: " + 
				mWrittenLength + " != " + mReservedMatrix.getDimLength()
			);
		}
		mWrittenLength = -1;
		super.close();
	}

}
