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

import ch.javasoft.jsmat.variable.MatReserved;
import ch.javasoft.jsmat.variable.MatReservedComplex;
import ch.javasoft.jsmat.variable.MatReservedStructure;

/**
 * A <tt>ReservedWriter</tt> is the abstract superclass for reserved writers. 
 * Reserved writers are used to write data piecewise to a 
 * {@link MatReserved} variable, which are placeholders for data.
 * <p>
 * For simple (matrix) data types, a {@link ReservedMatrixWriter} is used which
 * extends this class by an {@link ReservedMatrixWriter#append(Object) append}
 * method.It is usually created by invoking {@link MatWriter#createReservedWriter(String, ch.javasoft.jsmat.variable.MatReservedMatrix)
 * MatWriter.createReservedWriter}.
 * <p>
 * For {@link MatReservedComplex complex data types} such as structures (e.g. 
 * see {@link MatReservedStructure}), a {@link ReservedComplexWriter} for the 
 * complex type is created first by calling {@link MatWriter#createReservedWriter(String, MatReservedComplex) 
 * MatWriter.createReservedWriter}. With the returned complex writer,
 * {@link MatReservedComplex#createReservedWriter(ReservedComplexWriter, ch.javasoft.jsmat.variable.MatReservedMatrix)}
 * returns the simple writer for the nested reserved variable such as a 
 * structure field.
 * <p>
 * The following code writes int values into a variable:
 * <pre> 
		MatReservedMatrix<int[]> ri = MatReservedMatrix.createIntMatrix(1, 8);
		MatFileWriter wf = new MatFileWriter(new File("/tmp/tst.mat"));
		ReservedMatrixWriter<int[]> wi = rs.createReservedWriter(ws, ri);
		wi.append(new int[] {1, 2, 3, 4});
		wi.append(new int[] {5, 6, 7, 8});
		wi.close();
		wf.close();
 * </pre>  
 * <p>
 * The following code writes double values to a structure field:
 * <pre> 
		MatReservedStructure rs;
		MatReservedMatrix<double[]> rd;
		...
		MatFileWriter  wf = new MatFileWriter(new File("/tmp/tst.mat"));
		ReservedComplexWriter ws = wf.createReservedWriter("stru", rs);
		ReservedMatrixWriter<double[]> wd = rs.createReservedWriter(ws, rd);
		wd.append(new double[] {1, 2, 3, 4});
		wd.append(new double[] {5, 6, 7, 8});
		wd.close();
		ws.close();
		wf.close();
 * </pre>  
 */
abstract public class ReservedWriter {
	
	protected final MatWriter<? extends DataOutput>	mWriter;
	protected final MatReserved						mReserved;

	private VariableWriter	mVariableWriter;	//null if not yet open or closed
	protected DataOutput 	mDataOutput;		//null if not yet open or closed

	/**
	 * Constructor to write to complex types, such as structs.
	 * 
	 * @param complexWriter	the writer for the complex type
	 * @param reserved		the reserved variable of the nested type, e.g. a 
	 * 						field of a struct
	 */
	protected ReservedWriter(ReservedComplexWriter complexWriter, MatReserved reserved) {
		this(complexWriter.mWriter, reserved);
	}

	/**
	 * Constructor to write to top-level variables.
	 *  
	 * @param writer		the mat writer, e.g. writing a matlab file
	 * @param reserved		the reserved variable
	 */
	protected ReservedWriter(MatWriter<? extends DataOutput> writer, MatReserved reserved) {
		mWriter		= writer;
		mReserved	= reserved;
	}
	
	/**
	 * Opens the variable writer (if needed) and writes the start block of the
	 * reserved variable.
	 * 
	 * @param name			the variable name
	 * @throws IOException	if an io exception occurs
	 */
	protected void open(String name) throws IOException {
		if (mWriter.mReservedWriter == this) {
			//direct writer
			mVariableWriter	= mWriter.createVariableWriter();
			mDataOutput		= mVariableWriter.open();
		}
		else {
			//nested writer
			mVariableWriter = mWriter.mReservedWriter.mVariableWriter;
			if (mVariableWriter == null) {
				throw new IOException("outer reserved writer has already been closed");
			}
			mDataOutput = mWriter.mReservedWriter.mDataOutput;
		}
		mReserved.writeDataBlockStart(name, mDataOutput);		
	}
	
	/**
	 * Closes the reserved writer and completes writing of the corresponding 
	 * reserved variable.
	 * <p>
	 * Writes the data end block before closing (see 
	 * {@link MatReserved#writeDataBlockEnd(DataOutput)}). The underlying
	 * variable writer is also closed if needed.
	 *  
	 * @throws IOException	if any io exception occurrs
	 */
	public void close() throws IOException {
		mReserved.writeDataBlockEnd(mDataOutput);		
		closeVariableWriter();
	}
	protected void closeVariableWriter() throws IOException {
		if (mWriter.mReservedWriter == this) {
			//direct writer
			mVariableWriter.close();
			mWriter.mReservedWriter = null;
		}
		mVariableWriter	= null;
		mDataOutput		= null;
	}

}
