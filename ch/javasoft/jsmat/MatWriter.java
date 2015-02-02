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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import ch.javasoft.jsmat.variable.MatAllocated;
import ch.javasoft.jsmat.variable.MatReservedComplex;
import ch.javasoft.jsmat.variable.MatReservedMatrix;

/**
 * <tt>MatWriter</tt> is an abstract superclass for different writers producing
 * <tt>.mat</tt> formatted output.
 * <p>
 * Common subclasses are<ul>
 * <li>{@link MatFileWriter} preferrable if files are written</li>
 * <li>{@link MatOutputStreamWriter} to write to output streams</li>
 * </ul>
 * 
 * @param <D>	output type
 */
abstract public class MatWriter<D extends DataOutput> {
	
	protected ReservedWriter mReservedWriter = null;
	
	public MatWriter(D out) throws IOException {
		initDataOutput(out);
		writeHeader(out);
	}
	
	/**
	 * StackTrace called in the constructor before anything is written to the
	 * data output. For instance, the file could be opened/cleared here.
	 * 
	 * @param dataOutput the data output object to initialize
	 */
	abstract protected void initDataOutput(D dataOutput) throws IOException;
	
	/**
	 * Creates a writer for variable output. Variables are written compressed,
	 * which needs some initialization executed when 
	 * {@link VariableWriter#open()} is called. The same structures must be
	 * released again after writing the variable data, which is done in 
	 * {@link VariableWriter#close()}.
	 */
	abstract protected VariableWriter createVariableWriter() throws IOException;

	/**
	 * Write the given matrix
	 * 
	 * @param name			name of the matrix
	 * @param variable		the variable containing the data
	 * 
	 * @throws IOException	if any io exception occurs
	 */
	public void write(String name, MatAllocated variable) throws IOException {
		checkNoReservedWriter();
		VariableWriter writer = createVariableWriter();
		variable.write(name, writer.open());
		writer.close();
	}
	
	/**
	 * Throws an io exception if an open reserved writer exists
	 */
	protected void checkNoReservedWriter() throws IOException {
		if (mReservedWriter != null) {
			throw new IOException("unclosed reserved writer found, close it first");
		}
	}

	/**
	 * Creates a writer for the given reserved variable. Reserved variables are
	 * placeholders for data. The size of the data is already known, but to
	 * reduce memory demands, the values are not allocated yet. The reserved 
	 * writer allows appending data piecewise. It must be closed to complete
	 * the data output process for the given reserved variable.
	 * 
	 * @param name			The matlab name of the variable
	 * @param reserved		The variable reserving space for data
	 * @return				Writer to stream out data piecewise. Has to be 
	 * 						closed to complete the data output process.
	 * @throws IOException	If another unclosed reserved writer exists, or if 
	 * 						any other io exception occurs
	 */
	public ReservedComplexWriter createReservedWriter(String name, MatReservedComplex reserved) throws IOException {
		checkNoReservedWriter();
		ReservedComplexWriter writer = new ReservedComplexWriter(this, reserved);
		mReservedWriter = writer;
		writer.open(name);
		return writer;
	}
	/**
	 * Creates a writer for the given reserved variable. Reserved variables are
	 * placeholders for data. The size of the data is already known, but to
	 * reduce memory demands, the values are not allocated yet. The reserved 
	 * writer allows appending data piecewise. It must be closed to complete
	 * the data output process for the given reserved variable.
	 * 
	 * @param <A>			The java data type, defined by the reserved variable 
	 * @param name			The matlab name of the variable
	 * @param reserved		The variable reserving space for data
	 * @return				Writer to stream out data piecewise. Has to be 
	 * 						closed to complete the data output process.
	 * @throws IOException	If another unclosed reserved writer exists, or if 
	 * 						any other io exception occurs
	 */
	public <A> ReservedMatrixWriter<A> createReservedWriter(String name, MatReservedMatrix<A> reserved) throws IOException {
		checkNoReservedWriter();
		ReservedMatrixWriter<A> writer = new ReservedMatrixWriter<A>(this, reserved);
		mReservedWriter = writer;
		writer.open(name);
		return writer;
	}

	/**
	 * Write the given matrices. It is recommended to use {@link LinkedHashMap}
	 * in order to preserve the order of the variables, or {@link TreeMap} to
	 * sort the variables lexicographically.
	 * <p>
	 * The method {@link #write(String, MatAllocated)} is invoked with every
	 * name/value pair in <tt>matrices</tt>.
	 * 
	 * @param variables		a map containing name/variable pairs to be written
	 * 
	 * @throws IOException	if any io exception occurs
	 */
	public void write(Map<String, ? extends MatAllocated> variables) throws IOException {
		for (String name : variables.keySet()) {
			MatAllocated variable = variables.get(name);
			write(name, variable);
		}
	}
	
	/**
	 * Close the target. For instance, the underlying file or output stream
	 * could be closed here.
	 * 
	 * @throws IOException	if any io exception occurs
	 */
	abstract public void close() throws IOException;
	
    /**
     * Writes MAT-file header. Invoked in the constructor, after calling
     * {@link #initDataOutput(DataOutput)}
     */
    private void writeHeader(DataOutput out) throws IOException
    {    	
        //write descriptive text
        ch.javasoft.jsmat.MatFileHeader header = MatFileHeader.createHeader();
        char[] dest = new char[116];
        char[] src = header.getDescription().toCharArray();
        System.arraycopy(src, 0, dest, 0, src.length);
        for ( int i = 0; i < dest.length; i++ )
        {
        	out.writeByte( dest[i] );
        }
        //write subsyst data offset
        out.write( new byte[8] );
        
        //write version
        int version = header.getVersion();
        out.writeByte(version >> 8);
        out.writeByte(version);
        
        out.write( header.getEndianIndicator() );
    }
}
