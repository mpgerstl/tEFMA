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
package ch.javasoft.metabolic.efm.output.mat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.javasoft.jsmat.MatOutputStreamWriter;
import ch.javasoft.jsmat.MatWriter;
import ch.javasoft.jsmat.ReservedComplexWriter;
import ch.javasoft.jsmat.ReservedMatrixWriter;
import ch.javasoft.jsmat.variable.MatCell;
import ch.javasoft.jsmat.variable.MatCharMatrix;
import ch.javasoft.jsmat.variable.MatDoubleMatrix;
import ch.javasoft.jsmat.variable.MatMatrix;
import ch.javasoft.jsmat.variable.MatReservedMatrix;
import ch.javasoft.jsmat.variable.MatReservedStructure;
import ch.javasoft.jsmat.variable.MatVariable;

public class MatFileWriter  {
	
	private final MatWriter	mMatOut;
	private final String	mStructName;
	
	private ReservedComplexWriter	mStructWriter;
	
	private final Map<String, MatVariable> 	mFields	= new LinkedHashMap<String, MatVariable>();
	
	public MatFileWriter(File file, String structName) throws IOException {
		mMatOut 	= new ch.javasoft.jsmat.MatFileWriter(file);
		mStructName	= structName;
	}	
	
	public MatFileWriter(OutputStream out, String structName) throws IOException {
		mMatOut		= new MatOutputStreamWriter(out);
		mStructName	= structName;
	}

	public void write(String name, String value) {
		mFields.put(name, new MatCharMatrix(value));
	}
	public void write(String name, String[] values) {
		mFields.put(name, new MatCell(values));
	}

	public void write(String name, double[] values) {
		mFields.put(name, new MatDoubleMatrix(values, true /*row vector*/));
	}

	public void write(String name, double[][] values) {
		mFields.put(name, new MatDoubleMatrix(values));
	}
	public void write(String name, List<double[]> values, boolean rowsInList) {
		MatDoubleMatrix mx = rowsInList ? 
			MatDoubleMatrix.createMatrixFromRows(values) :
			MatDoubleMatrix.createMatrixFromColumns(values);
		mFields.put(name, mx);
	}
	public void write(String name, int value) {
		write(name, new int[] {value});
	}
	public void write(String name, int[] values) {
		MatMatrix<int[]> mxInt = MatMatrix.createIntMatrix(values, 1, values.length);
		mFields.put(name, mxInt);
	}
	
	public <A> ReservedMatrixWriter<A> createReservedWriter(String fieldName, MatReservedMatrix<A> mx) throws IOException {
		mFields.put(fieldName, mx);
		MatReservedStructure struc = new MatReservedStructure(mFields);
		mStructWriter = mMatOut.createReservedWriter(mStructName, struc);		
		return struc.createReservedWriter(mStructWriter, mx);
	}

	/**
	 * Write, flush and close output
     * @throws IOException	if an i/o exception occurs, for instance cause by 
     * 						file access
	 */
	public void close() throws IOException {
		mStructWriter.close();
		mFields.clear();
		mMatOut.close();
	}

}
