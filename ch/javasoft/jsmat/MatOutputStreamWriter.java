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

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;

import com.jmatio.common.MatDataTypes;

/**
 * The <tt>MatOutputStreamWriter</tt> writes a <tt>.mat</tt> file structure to
 * a given {@link OutputStream}.
 * <p>
 * Note: Writing a file is more memory efficient with the {@link MatFileWriter}.
 * This implementation uses byte array buffers to compute the block sizes before
 * writing to the real output stream.
 */
public class MatOutputStreamWriter extends MatWriter<DataOutputStream> {
	
	private final OutputStream mOut;
	
	/**
	 * Constructor, writing data to the given output stream
	 * 
	 * @param out			the output stream to write to
	 * @throws IOException	if any io exception occurs
	 */
	public MatOutputStreamWriter(OutputStream out) throws IOException {
		super(new DataOutputStream(out));
		mOut = out;
	}
	
	/**
	 * No operation is performed here (no initialization needed)
	 */
	@Override
	protected void initDataOutput(DataOutputStream dataOutput) throws IOException {
		//no initialization needed
	}
	
	//inherit javadoc comments
	@Override
	protected VariableWriter createVariableWriter() throws IOException {
		return new VariableWriter() {
			private ByteArrayOutputStream bout;
			private DeflaterOutputStream deflater;
			private DataOutputStream dataDefOut;
			public DataOutput open() throws IOException {
				bout		= new ByteArrayOutputStream();
		        deflater	= new DeflaterOutputStream(bout);
				dataDefOut	= new DataOutputStream(deflater);
				return dataDefOut;
			}
			public void close() throws IOException {
				dataDefOut.flush();
				deflater.finish();

				DataOutputStream dout = new DataOutputStream(mOut);
				// write COMPRESSED tag
				dout.writeInt( MatDataTypes.miCOMPRESSED );
				// write size
				dout.writeInt(bout.size());
				bout.writeTo(dout);
				bout.flush();
				dout.flush();
				
				bout		= null;
		        deflater	= null;
				dataDefOut	= null;
			}
		};		
	}
	
	/**
	 * Closes the underlying output stream
	 */
	@Override
	public void close() throws IOException {
		checkNoReservedWriter();
		mOut.close();
	}

}
