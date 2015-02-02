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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DeflaterOutputStream;

import ch.javasoft.io.DataOutputOutputStream;

import com.jmatio.common.MatDataTypes;

/**
 * The <tt>MatFileWriter</tt> writes a <tt>.mat</tt> file. 
 * <p>
 * This class is a memory efficient version of {@link MatOutputStreamWriter} 
 * optimized for file targets. A {@link RandomAccessFile} is used to write the
 * file.
 */
public class MatFileWriter extends MatWriter<RandomAccessFile> {

	private final RandomAccessFile mFile;
	
	/**
	 * Constructor to write to the given file
	 * 
	 * @param file			the file to create
	 * @throws IOException	if any io exception occurs
	 */
	public MatFileWriter(File file) throws IOException {
		this(new RandomAccessFile(file, "rw"));
	}

	/**
	 * Constructor to write to the given ras file
	 * 
	 * @param file			the ras file to create
	 * @throws IOException	if any io exception occurs
	 */
	public MatFileWriter(RandomAccessFile file) throws IOException {
		super(file);
		mFile = file;
	}
	
	/**
	 * The file is cleared, that is, {@link RandomAccessFile#setLength(long)}
	 * is called.
	 */
	@Override
	protected void initDataOutput(RandomAccessFile ras) throws IOException {
		ras.setLength(0L);
	}
	
	//inherit javadoc comments
	@Override
	protected VariableWriter createVariableWriter() throws IOException {
		return new VariableWriter() {
			private long posSize, posStart;
			private DeflaterOutputStream deflater;
			private DataOutputStream dataDefOut;
			public DataOutput open() throws IOException {
				// write COMPRESSED tag
				mFile.writeInt( MatDataTypes.miCOMPRESSED );
				// write size, but only as placeholder since we don't know it yet
				posSize = mFile.getFilePointer();
				mFile.writeInt(0);
				posStart = mFile.getFilePointer();

		        deflater	= new DeflaterOutputStream(new DataOutputOutputStream(mFile));
				dataDefOut	= new DataOutputStream(deflater);
				
				return dataDefOut;
			}
			public void close() throws IOException {				
				dataDefOut.flush();
				deflater.finish();

				// now, write the real compressed size
				long posEnd = mFile.getFilePointer();
				mFile.seek(posSize);
				mFile.writeInt((int)(posEnd - posStart));
				mFile.seek(posEnd);
				
				dataDefOut	= null;
				deflater	= null;
			}
		};
	}

	/**
	 * Closes the underlying file
	 */
	@Override
	public void close() throws IOException {
		checkNoReservedWriter();
		mFile.close();
	}
    
}
