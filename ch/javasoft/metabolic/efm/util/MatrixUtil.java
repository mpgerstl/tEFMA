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
package ch.javasoft.metabolic.efm.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.numeric.Zero;

/**
 * The <code>MatrixUtil</code> contains static helper methods for matrices
 */
public class MatrixUtil {
	
	/**
	 * Returns the given readable matrix into a {@link BigIntegerRationalMatrix}.
	 * Supported input matrices are {@link ReadableBigIntegerRationalMatrix} and
	 * {@link ReadableDoubleMatrix}, if none of these interfaces is implemented
	 * by the specified matrix, an exception is thrown.
	 * 
	 * @param mx					the matrix to convert
	 * @param zero					the zero object for value adjustment
	 * @param enforceNewInstance	if true, a new instance will be returned in
	 * 								any case, otherwise, the returned matrix 
	 * 								might be the same instance as the input 
	 * 								matrix
	 * @return 	the converted matrix, definitely a new instance if enforced, 
	 * 			otherwise possibly the same instance as the input matrix
	 */
	public static BigIntegerRationalMatrix convertToBigIntegerRationalMatrix(ReadableMatrix mx, Zero zero, boolean enforceNewInstance) {
		if (mx instanceof ReadableBigIntegerRationalMatrix<?>) {
			return ((ReadableBigIntegerRationalMatrix<?>)mx).toBigIntegerRationalMatrix(enforceNewInstance);
		}
		else if (mx instanceof ReadableDoubleMatrix<?>) {
			final double[] values = ((ReadableDoubleMatrix<?>)mx).toDoubleArray();
//			return new DefaultBigIntegerRationalMatrix(values, mx.getRowCount(), mx.getColumnCount(), false /*adjust values*/);
			return new DefaultBigIntegerRationalMatrix(values, mx.getRowCount(), mx.getColumnCount(), zero.mZeroPos /*adjust values*/);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	
	/**
	 * Writes the given matrix using the given writer. The written data can be
	 * read and turned into a matrix using {@link #readMatrix(InputStream)}.
	 */
	@SuppressWarnings("unchecked")
	public static void writeMatrix(ReadableMatrix matrix, OutputStream out) throws IOException {
		writeMatrixInternal(matrix, out);
	}
	private static <N extends Number> void writeMatrixInternal(ReadableMatrix<N> matrix, OutputStream out) throws IOException {
		final DataOutputStream dout = new DataOutputStream(out);
		final int cols = matrix.getColumnCount();
		final int rows = matrix.getRowCount();
		final NumberOperations<N> ops = matrix.getNumberOperations();
		dout.writeUTF(matrix.getClass().getName());
		dout.writeInt(rows);
		dout.writeInt(cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				ops.writeTo(matrix.getNumberValueAt(row, col), dout);
			}
		}
		dout.flush();
	}
	
	/**
	 * Reads a matrix from the given reader and returns a matrix instance. The
	 * data has previously been written with {@link #writeMatrix(ReadableMatrix, OutputStream)}
	 */
	public static ReadableMatrix<?> readMatrix(InputStream in) throws IOException {
		final DataInputStream din = new DataInputStream(in);
		try {
			final String className = din.readUTF();
			final Integer zero = Integer.valueOf(0);
			final ReadableMatrix<?> template = (ReadableMatrix<?>)Class.forName(className).getConstructor(Integer.TYPE, Integer.TYPE).newInstance(zero, zero);
			final NumberOperations<?> ops	 = template.getNumberOperations();
			final int rows = din.readInt();
			final int cols = din.readInt();
			final Number[][] vals = ops.newArray(rows, cols);
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					vals[row][col] = ops.readFrom(din);
				}				
			}
			return (ReadableMatrix<?>)Class.forName(className).getConstructor(vals.getClass(), Boolean.TYPE).newInstance(vals, Boolean.TRUE /*rowsInDim1*/);
		}
		catch (Exception e) {
			throw ExceptionUtil.toRuntimeExceptionOr(IOException.class, e);
		}
	}
	
	//no instances
	private MatrixUtil() {}

}
