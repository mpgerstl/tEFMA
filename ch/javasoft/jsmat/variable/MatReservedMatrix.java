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
import ch.javasoft.jsmat.common.MatType;
import ch.javasoft.jsmat.primitive.MatPrimitive;

abstract public class MatReservedMatrix<A /*array data type*/> extends MatReserved {
	
	private final MatType mMatType;
	
	public MatReservedMatrix(MatClass matClass, MatType matType, int[] dims) {
		super(matClass, dims);
		mMatType = matType;
		//check size overflow
		checkRawDataSizeOverflow(matType.size);
	}
	
	@Override
	public int getRawDataSize() {
		return mMatType.size * getDimLength();
	}

	/**
	 * Only the start of the data block is written here 
	 * (see {@link MatPrimitive#writeStart(MatType, DataOutput, int)}).
	 * The real data is appended when {@link #appendData(DataOutput, Object)}
	 * is called. The write process is terminated with 
	 * {@link #writeDataBlockEnd(DataOutput)}.
	 */
	@Override
	public void writeDataBlockStart(String name, DataOutput out) throws IOException {
		writeStart(name, out);
		MatPrimitive.writeStart(mMatType, out, getRawDataSize());
	}

	public abstract int appendData(DataOutput out, A data) throws IOException;
	
	@Override
	public void writeDataBlockEnd(DataOutput out) throws IOException {
		MatPrimitive.writeEnd(mMatType, out, getRawDataSize());				
	}
	
	public static MatReservedMatrix<double[]> createDoubleMatrix(int rows, int cols) {
		return createDoubleMatrix(new int[] {rows, cols});
	}
	public static MatReservedMatrix<double[]> createDoubleMatrix(int[] dims) {
		return new MatReservedMatrix<double[]>(MatClass.DOUBLE, MatType.DOUBLE, dims) {
			@Override
			public int appendData(DataOutput out, double[] data) throws IOException {
				for (int i = 0; i < data.length; i++) {
					out.writeDouble(data[i]);
				}
				return data.length;
			}
		};
	}
	
	
	public static MatReservedMatrix<int[]> createInt32Matrix(int rows, int cols) {
		return createInt32Matrix(new int[] {rows, cols});
	}
	public static MatReservedMatrix<int[]> createInt32Matrix(int[] dims) {
		return new MatReservedMatrix<int[]>(MatClass.INT32, MatType.INT32, dims) {
			@Override
			public int appendData(DataOutput out, int[] data) throws IOException {
				for (int i = 0; i < data.length; i++) {
					out.writeInt(data[i]);
				}
				return data.length;
			}
		};
	}

	public static MatReservedMatrix<int[]> createUInt32Matrix(int rows, int cols) {
		return createUInt32Matrix(new int[] {rows, cols});
	}
	public static MatReservedMatrix<int[]> createUInt32Matrix(int[] dims) {
		return new MatReservedMatrix<int[]>(MatClass.UINT32, MatType.UINT32, dims) {
			@Override
			public int appendData(DataOutput out, int[] data) throws IOException {
				for (int i = 0; i < data.length; i++) {
					out.writeInt(data[i]);
				}
				return data.length;
			}
		};
	}
	public static MatReservedMatrix<int[]> createInt8Matrix(int rows, int cols) {
		return createInt8Matrix(new int[] {rows, cols});
	}
	public static MatReservedMatrix<int[]> createInt8Matrix(int[] dims) {
		return new MatReservedMatrix<int[]>(MatClass.INT8, MatType.INT8, dims) {
			@Override
			public int appendData(DataOutput out, int[] data) throws IOException {
				for (int i = 0; i < data.length; i++) {
					out.writeByte((byte)data[i]);
				}
				return data.length;
			}
		};
	}
	public static MatReservedMatrix<int[]> createUInt8Matrix(int rows, int cols) {
		return createUInt8Matrix(new int[] {rows, cols});
	}
	public static MatReservedMatrix<int[]> createUInt8Matrix(int[] dims) {
		return new MatReservedMatrix<int[]>(MatClass.UINT8, MatType.UINT8, dims) {
			@Override
			public int appendData(DataOutput out, int[] data) throws IOException {
				for (int i = 0; i < data.length; i++) {
					out.writeByte(data[i]);
				}
				return data.length;
			}
		};
	}
	
}
