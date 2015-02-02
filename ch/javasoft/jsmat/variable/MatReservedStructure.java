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
import java.util.Map;

import ch.javasoft.jsmat.ReservedComplexWriter;
import ch.javasoft.jsmat.ReservedMatrixWriter;
import ch.javasoft.jsmat.ReservedWriter;
import ch.javasoft.jsmat.common.MatClass;

public class MatReservedStructure extends MatReservedComplex {
	
	private final Map<String, ? extends MatVariable>[] mFields;
	
	private int 	mNextFieldIndex = 0;
	private boolean mUnclosedWriter = false;
	
	@SuppressWarnings("unchecked")
	public MatReservedStructure(Map<String, ? extends MatVariable> fields) {
		this(new Map[] {fields}, new int[] {1, 1});
	}
	public MatReservedStructure(Map<String, ? extends MatVariable>[] fields, int[] dims) {
		super(MatClass.STRUCT, dims);
		mFields = fields;
	}
	
	protected void checkNoReservedWriter() throws IOException {
		if (mUnclosedWriter) {
			throw new IOException("unclosed reserved field writer found, close it first");
		}
	}
	@Override
	public void writeDataBlockStart(String name, DataOutput out) throws IOException {
		writeStart(name, out);
		MatStructure.writeStructStart(out, mFields);
		writeAllocatedFields(out);
	}
	@Override
	public void writeDataBlockEnd(DataOutput out) throws IOException {
		//check whether all fields have been written or not
		if (getFieldCount() != mNextFieldIndex) {
			throw new IOException(
				"not all fields have been written: " + mNextFieldIndex + " of " + getFieldCount() +
				". should open reserved writer for field " + getFieldName(getNextField())
			);
		}
		mNextFieldIndex = 0;
	}
	@Override
	public <A> ReservedMatrixWriter<A> createReservedWriter(final ReservedComplexWriter writer, final MatReservedMatrix<A> reservedMatrix) throws IOException {
		checkNoReservedWriter();
		checkNextReservedField(reservedMatrix);
		mUnclosedWriter = true;
		
		return new ReservedMatrixWriter<A>(writer, reservedMatrix) {
			{
				open(getFieldName(reservedMatrix));
			}
			@Override
			public void close() throws IOException {
				mNextFieldIndex++;
				writeAllocatedFields(mDataOutput);
				super.close();
				mUnclosedWriter = false;
			}			
		};
	}
	@Override
	public ReservedWriter createReservedWriter(final ReservedComplexWriter writer, final MatReserved reserved) throws IOException {
		checkNoReservedWriter();
		checkNextReservedField(reserved);
		mUnclosedWriter = true;
		
		return new ReservedWriter(writer, reserved) {
			{
				open(getFieldName(reserved));
			}
			@Override
			public void close() throws IOException {
				mNextFieldIndex++;
				mReserved.writeDataBlockEnd(mDataOutput);		
				writeAllocatedFields(mDataOutput);
				closeVariableWriter();
				mUnclosedWriter = false;
			}			
		};
	}
	
	private void writeAllocatedFields(DataOutput out) throws IOException {
		MatVariable var;
		while ((var = getNextField()) instanceof MatAllocated) {
			MatAllocated all = (MatAllocated)var;
			all.write(getFieldName(var), out);
			mNextFieldIndex++;
		}
	}
	
	private void checkNextReservedField(MatReserved reserved) throws IllegalArgumentException {
		MatVariable var = getNextField();
		if (var != reserved) {
			String curName = getFieldName(reserved);
			String expName = getFieldName(var);
			throw new IllegalArgumentException(
				"must open reserved writers for structure fields ordered, expected " + 
				(expName == null ? "closing the structure writer" : expName) + " but found " + 
				(curName == null ? "field " + reserved + " not belonging to this structure" : curName)
			);
		}
	}
	private MatVariable getNextField() {
		int index = 0;
		for (int i = 0; i < mFields.length; i++) {
			for (String name : mFields[i].keySet()) {
				if (index == mNextFieldIndex) {
					return mFields[i].get(name);
				}
				index++;
			}
		}
		return null;
	}
	private String getFieldName(MatVariable var) {
		for (int i = 0; i < mFields.length; i++) {
			for (String name : mFields[i].keySet()) {
				MatVariable cur = mFields[i].get(name);
				if (cur == var) {
					return name;
				}
			}
		}
		return null;
	}
	
	private int getFieldCount() {
		int count = 0;
		for (int i = 0; i < mFields.length; i++) {
			count += mFields[i].size();
		}
		return count;
	}

	@Override
	protected int getRawDataSize() {
		return MatStructure.getRawDataSize(mFields);
	}

}
