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
package ch.javasoft.math.array.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;

import ch.javasoft.math.array.ArrayOperations;
/**
 * Abstract superclass for implementations of {@link ArrayOperations}. All 
 * methods are implemented which do not depend on the single element type. 
 * Methods depending on the type of a single array element are implemented by
 * subclasses, since some implementations use primitive types, which cannot be
 * handled generically.
 * 
 * @type A	the type of the array, for instance double[] or String[]
 */
abstract public class AbstractArrayOperations<A> implements ArrayOperations<A> {
	
	private final Class<A> arrayClass;
	
	public AbstractArrayOperations(Class<A> arrayClass) {
		this.arrayClass = arrayClass;
	}
	
	public Class<A> arrayClass() {
		return arrayClass;
	}

	@SuppressWarnings("unchecked")
	public A newVector(int size) {
		return (A)Array.newInstance(arrayClass.getComponentType(), size);
	}
	
	@SuppressWarnings("unchecked")
	public A[] newMatrix(int rows, int columns) {
		return (A[])Array.newInstance(arrayClass.getComponentType(), new int[] {rows, columns});
	}
	
	public int getRowCount(A[] matrix) {
		return matrix.length;
	}
	public int getColumnCount(A[] matrix) {
		return matrix.length == 0 ? 0 : getLength(matrix[0]);		
	}
	
	public String getAsString(A[] matrix, int row, int column) {
		return getAsString(matrix[row], column);
	}
	public String getVectorSignatureString(String name, A vector, boolean rowvec) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		printVectorSignature(pw, name, vector, rowvec, false /*newLine*/);
		pw.flush();
		return sw.toString();
	}
	public String getMatrixSignatureString(String name, A[] matrix) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		printMatrixSignature(pw, name, matrix, false /*newLine*/);
		pw.flush();
		return sw.toString();
	}
	
	public void copyVector(A src, A dst) {
		final int len = getLength(src);
		System.arraycopy(src, 0, dst, 0, len);
	}
	public void copyVectorElements(A src, int srcStart, A dst, int dstStart, int length) {
		System.arraycopy(src, srcStart, dst, dstStart, length);
	}
	public void copyVectorElementsToMatrixRow(A src, int srcStart, A[] dst, int dstRow, int dstColStart, int length) {
		copyVectorElements(src, srcStart, dst[dstRow], dstColStart, length);
	}
	public void copyMatrix(A[] src, A[] dst) {
		final int rows = getRowCount(src);
		final int cols = getColumnCount(src);
		for (int r = 0; r < rows; r++) {
			System.arraycopy(src[r], 0, dst[r], 0, cols);
		}
	}
	public void copyMatrixElements(A[] src, int srcRowStart, int srcColStart, A[] dst, int dstRowStart, int dstColStart, int rows, int cols) {		
		for (int i = 0; i < rows; i++) {
			copyMatrixRowElements(src, srcRowStart + i, srcColStart, dst, dstRowStart + i, dstColStart, cols);
		}
	}
	public void copyMatrixRowElements(A[] src, int srcRow, int srcColStart, A[] dst, int dstRow, int dstColStart, int length) {
		copyVectorElements(src[srcRow], srcColStart, dst[dstRow], dstColStart, length);
	}
	public void copyMatrixRowElementsToVector(A[] src, int srcRow, int srcColStart, A dst, int dstStart, int length) {
		copyVectorElements(src[srcRow], srcColStart, dst, dstStart, length);
	}
	public A copyOfVector(A vector) {
		return copyOfVectorRange(vector, 0, getLength(vector));
	}
	public A copyOfVector(A vector, int newLength) {
		return copyOfVectorRange(vector, 0, newLength);
	}
	public A copyOfVectorRange(A vector, int from, int to) {
		final int len = to - from;
		final A copy = newVector(len);
		System.arraycopy(vector, from, copy, 0, len);
		return copy;
	}
	public A[] copyOfMatrix(A[] matrix) {
		final int rows = getRowCount(matrix);
		final int cols = getColumnCount(matrix);
		final A[] copy = newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			System.arraycopy(matrix[r], 0, copy[r], 0, cols);
		}
		return copy;
	}
	public A copyOfMatrixRow(A[] matrix, int row) {
		return copyOfVector(matrix[row]);
	}
	public A copyOfMatrixRowRange(A[] matrix, int row, int colFrom, int colTo) {
		return copyOfVectorRange(matrix[row], colFrom, colTo);
	}
	public A[] copyOfRowSubMatrix(A[] matrix, int... rowIndices) {
		if (rowIndices == null) {
			return copyOfMatrix(matrix);
		}
		final int rows = rowIndices.length;
		final int cols = getColumnCount(matrix);
		final A[] copy = newMatrix(rows, cols);
		for (int i = 0; i < rowIndices.length; i++) {
			System.arraycopy(matrix[rowIndices[i]], 0, copy[i], 0, cols);
		}
		return copy;
	}
	
	public void printVectorSignature(PrintWriter writer, String name, A vector, boolean rowvec, boolean newLine) {
		final int len = getLength(vector);
		writer.print(name);
		writer.print(':');
		writer.print(rowvec ? 1 : len);
		writer.print('x');
		writer.print(rowvec ? len : 1);		
		if (newLine) {
			writer.println();
		}
	}
	public void printMatrixSignature(PrintWriter writer, String name, A[] matrix, boolean newLine) {
		final int rows = getRowCount(matrix);
		final int cols = getColumnCount(matrix);
		writer.print(name);
		writer.print(':');
		writer.print(rows);
		writer.print('x');
		writer.print(cols);		
		if (newLine) {
			writer.println();
		}
	}
	public void printVector(PrintWriter writer, String name, A vector, boolean newLine) {
		final int len = getLength(vector);
		writer.print(name);
		writer.print(" = [");
		for (int i = 0; i < len; i++) {
			writer.write(i == 0 ? " " : " , ");
			writer.write(getAsString(vector, i));
		}
		writer.print(" ]");
		if (newLine) {
			writer.println();
			writer.flush();
		}
	}
	public void printMatrix(PrintWriter writer, String name, A[] matrix) {
		final int rows = getRowCount(matrix);
		final int cols = getColumnCount(matrix);
		printMatrixSignature(writer, name, matrix, false /*newLine*/);
		writer.println(" {");
		for (int r = 0; r < rows; r++) {
			writer.print("\t[");
			for (int c = 0; c < cols; c++) {
				writer.write(c == 0 ? " " : " , ");
				writer.write(getAsString(matrix, r, c));				
			}
			writer.println(" ]");
		}
		writer.println("}");
		writer.flush();
	}
	
}
