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
package ch.javasoft.metabolic.efm.output;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.Writer;

import ch.javasoft.io.WriterOutputStream;
import ch.javasoft.metabolic.MetabolicNetwork;

/**
 * The <tt>ByteEncodedOutputCallback</tt> as an extension of
 * {@link AbstractFormattedOutputCallback} is a convenience class. It adds some 
 * constructors and determines the writer template of the superclass with
 * {@link DataOutput}.
 */
public class ByteEncodedOutputCallback extends AbstractFormattedOutputCallback<DataOutput> {

	/**
	 * Constructor with given output mode, writing to the given output stream 
	 * using a {@link ByteEncodedOutputFormatter} to format the output.
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param out	the output stream to write to
	 */
	public ByteEncodedOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, OutputStream out) {
		this(originalNetwork, mode, new ByteEncodedOutputFormatter(), out);
	}

	/**
	 * Constructor with given output mode, writing to the given output stream 
	 * using the specified formatter to format the output.
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param out	the output stream to write to
	 * @param formatter	the formatter defining the concrete output format
	 */
	public ByteEncodedOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, EfmOutputFormatter<DataOutput> formatter, OutputStream out) {
		super(mode, new UnmappingEfmOutputFormatter<DataOutput>(formatter, originalNetwork), new DataOutputStream(out));	
	}

	/**
	 * Constructor with given output mode, using the specified formatter to
	 * format the output and write it with the given writer.
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode		the output mode, i.e. uncompressed/compressed, binary/double
	 * @param writer	the writer to write the formatted output to
	 * @param formatter	the formatter defining the concrete output format
	 */
	public ByteEncodedOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, EfmOutputFormatter<DataOutput> formatter, Writer writer) {
		super(mode, new UnmappingEfmOutputFormatter<DataOutput>(formatter, originalNetwork), new DataOutputStream(new WriterOutputStream(writer)));	
	}
	
	public boolean allowLoggingDuringOutput() {
		return true;
	}

}
