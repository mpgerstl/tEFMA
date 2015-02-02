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

/**
 * A <tt>MatMatrix</tt> is a matrix with data. The data must be allocated when
 * instantiating the matrix, that is, it is kept in memory. They are simple to
 * use, but {@link MatReservedMatrix} might be an option for large matrices
 * which allow writing the data piecewise without preallocating it in memory.
 *
 * @param <A>	the matrix type, typically an array of a java primitive type
 */
abstract public class MatMatrix<A> extends MatAllocated {
	
	protected final MatType mMatType;
	
	public MatMatrix(MatClass matClass, MatType matType, int[] dims) {
		super(matClass, dims);
		mMatType = matType;
		//check size overflow
		checkRawDataSizeOverflow(matType.size);
		//call in subclasses:
		//checkValueLength(getMatrixSize());
	}
	
	@Override
	protected int getRawDataSize() {
		return mMatType.size * getMatrixSize();
	}
	
	/**
	 * Returns the matrix size, typically the overall array length of the 
	 * underlying data
	 */
	protected abstract int getMatrixSize();
    
    /**
     * Write the raw data to the given output, excluding type, size, name etc.
     * This method is implemented by the type specific subclasses. 
     * 
     * @param out			the data output to write to
     * @throws IOException	if any io exception occurs
     */
    @Override
	abstract public void writeRawData(DataOutput out) throws IOException;
    
    
    /** Generic class for internal use in static create... methods*/
    protected abstract static class MatGenericMatrix<A> extends MatMatrix<A> {
    	public MatGenericMatrix(MatClass matClass, MatType matType, int[] dims) {
    		super(matClass, matType, dims);
    	}    	
    	@Override
    	public void write(String name, DataOutput out) throws IOException {
    		final int rawDataSize = getRawDataSize();
    		writeStart(name, out);
    		MatPrimitive.writeStart(mMatType, out, rawDataSize);
    		writeRawData(out);
    		MatPrimitive.writeEnd(mMatType, out, rawDataSize);    		
    	}
    }
    
    /**
     * Creates a double matrix
     * 
     * @param values	the data values, column packed (FORTRAN like)
     * @param rows		number of rows
     * @param cols		number of columns
     * @return the double matrix
     */
	public static MatMatrix<double[]> createDoubleMatrix(final double[] values, int rows, int cols) {
		return createDoubleMatrix(values, new int[] {rows, cols});
	}
    /**
     * Creates a double matrix
     * 
     * @param values	the data values, column packed (FORTRAN like)
     * @param dims		the dimensions
     * @return the double matrix
     */
	public static MatMatrix<double[]> createDoubleMatrix(final double[] values, int[] dims) {
		return new MatGenericMatrix<double[]>(MatClass.DOUBLE, MatType.DOUBLE, dims) {
			@Override
			protected void writeStart(String name, DataOutput out) throws IOException {
				super.writeStart(name, out);
				
			}
			@Override
			protected int getMatrixSize() {
				return values.length;
			}
			@Override
			public void writeRawData(DataOutput out) throws IOException {
				for (int i = 0; i < values.length; i++) {
					out.writeDouble(values[i]);
				}
			}
		};
	}
	
    /**
     * Creates a integer matrix
     * 
     * @param values	the data values, column packed (FORTRAN like)
     * @param rows		number of rows
     * @param cols		number of columns
     * @return the integer matrix
     */
	public static MatMatrix<int[]> createIntMatrix(final int[] values, int rows, int cols) {
		return createIntMatrix(values, new int[] {rows, cols});
	}
    /**
     * Creates a integer matrix
     * 
     * @param values	the data values, column packed (FORTRAN like)
     * @param dims		the dimensions
     * @return the integer matrix
     */
	public static MatMatrix<int[]> createIntMatrix(final int[] values, int[] dims) {
		return new MatGenericMatrix<int[]>(MatClass.INT32, MatType.INT32, dims) {
			@Override
			protected int getMatrixSize() {
				return values.length;
			}
			@Override
			public void writeRawData(DataOutput out) throws IOException {
				for (int i = 0; i < values.length; i++) {
					out.writeInt(values[i]);
				}
			}
		};
	}
	
    /**
     * Creates a utf8 encoded character matrix with 1 row
     * 
     * @param values	the data values, column packed (FORTRAN like)
     * @return the utf8 encoded character matrix
     */
	public MatMatrix<char[]> createUtf8Matrix(final byte[] values) {
		return createUtf8Matrix(values, new int[] {1, values.length});
	}
    /**
     * Creates a utf8 encoded character matrix
     * 
     * @param values	the data values, column packed (FORTRAN like)
     * @param dims		the dimensions
     * @return the utf8 encoded character matrix
     */
	public MatMatrix<char[]> createUtf8Matrix(final byte[] values, int[] dims) {
		return new MatGenericMatrix<char[]>(MatClass.CHAR, MatType.UTF8, dims) {
			@Override
			protected int getMatrixSize() {
				return values.length;
			}
			@Override
			public void writeRawData(DataOutput out) throws IOException {
				for (int i = 0; i < values.length; i++) {
					out.write(values[i]);
				}
			}
		};
	}

}
