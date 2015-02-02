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

import ch.javasoft.math.array.ArrayOperations;
/**
 * Default implementation for {@link ArrayOperations} for normal arrays, where
 * the single components are Objects (not primitive types).
 * 
 * @type C	the component type, a single array element
 */
public class DefaultArrayOperations<C> extends AbstractArrayOperations<C[]> {
	
	public DefaultArrayOperations(Class<C[]> arrayClass) {
		super(arrayClass);
	}
	
	public int getLength(C[] array) {
		return array.length;
	}
	
	@Override
	public int getColumnCount(C[][] matrix) {
		return matrix.length == 0 ? 0 : matrix[0].length;		
	}
	
	public String getAsString(C[] vector, int index) {
		return String.valueOf(vector[index]);
	}
	
	public void swapVectorElements(C[] v, int index1, int index2) {
		final C tmp = v[index1];
		v[index1] = v[index2];
		v[index2] = tmp;
	}
	
	public void swapMatrixElements(C[][] matrix, int row1, int col1, int row2, int col2) {
		final C tmp = matrix[row1][col1];
		matrix[row1][col1] = matrix[row2][col2];
		matrix[row2][col2] = tmp;
	}
	
	public void swapMatrixRows(C[][] matrix, int row1, int row2) {
		final int cols = getColumnCount(matrix);
		for (int c = 0; c < cols; c++) {
			final C tmp = matrix[row1][c];
			matrix[row1][c] = matrix[row2][c];
			matrix[row2][c] = tmp;
		}
	}
	
	public void swapMatrixColumns(C[][] matrix, int col1, int col2) {
		final int rows = getRowCount(matrix);
		for (int r = 0; r < rows; r++) {
			final C tmp = matrix[r][col1];
			matrix[r][col1] = matrix[r][col2];
			matrix[r][col2] = tmp;
		}
	}
	
	public void swapVectorVectorElements(C[] v1, int index1, C[] v2, int index2) {
		final C tmp = v1[index1];
		v1[index1] = v2[index2];
		v2[index2] = tmp;
	}
	
	public void swapVectorMatrixElements(C[] v, int index, C[][] m, int row, int col) {
		final C tmp = v[index];
		v[index] = m[row][col];
		m[row][col] = tmp;
	}
	
	public void swapMatrixMatrixElements(C[][] m1, int row1, int col1, C[][] m2, int row2, int col2) {
		final C tmp = m1[row1][col1];
		m1[row1][col1] = m2[row2][col2];
		m2[row2][col2] = tmp;
	}
	
	public void copyVectorElement(C[] src, int srcIndex, C[] dst, int dstIndex) {
		dst[dstIndex] = src[srcIndex];
	}
	public void copyVectorElements(C[] src, int[] srcIndices, C[] dst, int[] dstIndices) {
		if (srcIndices.length != dstIndices.length) {
			throw new IllegalArgumentException("size of source and destination indices must match: " + srcIndices.length + " != " + dstIndices.length);
		}
		for (int i = 0; i < srcIndices.length; i++) {
			src[srcIndices[i]] = dst[dstIndices[i]];
		}
	}
	public void copyVectorElementsToMatrixColumn(C[] src, int srcStart, C[][] dst, int dstRowStart, int dstCol, int length) {
		for (int i = 0; i < length; i++) {
			dst[dstRowStart + i][dstCol] = src[srcStart + i];
		}
	}
	public void copyMatrixElement(C[][] src, int srcRow, int srcCol, C[][] dst, int dstRow, int dstCol) {
		dst[dstRow][dstCol] = src[srcRow][srcCol];
	}
	public void copyMatrixRowElementsToColumn(C[][] src, int srcRow, int srcColStart, C[][] dst, int dstRowStart, int dstCol, int length) {
		for (int i = 0; i < length; i++) {
			dst[dstRowStart + i][dstCol] = src[srcRow][srcColStart + i];
		}
	}
	public void copyMatrixColumnElements(C[][] src, int srcRowStart, int srcCol, C[][] dst, int dstRowStart, int dstCol, int length) {
		for (int i = 0; i < length; i++) {
			dst[dstRowStart + i][dstCol] = src[srcRowStart + i][srcCol];
		}
	}
	public void copyMatrixColumnElementsToVector(C[][] src, int srcRowStart, int srcCol, C[] dst, int dstStart, int length) {
		for (int i = 0; i < length; i++) {
			dst[dstStart + i] = src[srcRowStart + i][srcCol];
		}
	}
	public void copyMatrixColumnElementsToRow(C[][] src, int srcRowStart, int srcCol, C[][] dst, int dstRow, int dstColStart, int length) {
		for (int i = 0; i < length; i++) {
			dst[dstRow][dstColStart + i] = src[srcRowStart + i][srcCol];
		}
	}
	public void copyVectorElementsToMatrixDiagonal(C[] src, C[][] dst) {
		final int len = getLength(src);
		final int rows = getRowCount(dst);
		final int cols = getColumnCount(dst);
		if (len != Math.min(rows, cols)) {
			throw new IllegalArgumentException("vector length must be equal to minimum dimension of matrix: " + len + " != min(" + rows + ", " + cols + ")");
		}
		for (int i = 0; i < len; i++) {
			dst[i][i] = src[i];
		}
	}
	public void copyMatrixDiagonalElementsToVector(C[][] src, C[] dst) {
		final int len = getLength(dst);
		final int rows = getRowCount(src);
		final int cols = getColumnCount(src);
		if (len != Math.min(rows, cols)) {
			throw new IllegalArgumentException("vector length must be equal to minimum dimension of matrix: " + len + " != min(" + rows + ", " + cols + ")");
		}
		for (int i = 0; i < len; i++) {
			dst[i] = src[i][i];
		}
	}
	public C[] copyOfMatrixDiagonal(C[][] matrix) {
		final int rows = getRowCount(matrix);
		final int cols = getColumnCount(matrix);
		final int len  = Math.min(rows, cols);
		final C[] diag = newVector(len);
		for (int i = 0; i < len; i++) {
			diag[i] = matrix[i][i];
		}
		return diag;
	}
	public C[][] newDiagonalMatrix(C[] diagonal) {
		final int len = diagonal.length;
		final C[][] matrix = newMatrix(len, len);
		for (int i = 0; i < len; i++) {
			matrix[i][i] = diagonal[i];
		}
		return matrix;
	}
	public C[] copyOfMatrixColumn(C[][] matrix, int column) {
		final int rows = getRowCount(matrix);
		final C[] col = newVector(rows);
		for (int r = 0; r < rows; r++) {
			col[r] = matrix[r][column];
		}
		return col;
	}
	public C[] copyOfMatrixColumnRange(C[][] matrix, int rowFrom, int rowTo, int column) {
		final int rows = rowTo - rowFrom;
		final C[] col = newVector(rows);
		for (int r = 0; r < rows; r++) {
			col[r] = matrix[rowFrom + r][column];
		}
		return col;
	}
	public C[][] copyOfColumnSubMatrix(C[][] matrix, int... columnIndices) {
		if (columnIndices == null) {
			return copyOfMatrix(matrix);
		}
		final int rows = getRowCount(matrix);
		final int cols = columnIndices.length;
		final C[][] copy = newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			for (int i = 0; i < cols; i++) {
				copy[r][i] = matrix[r][columnIndices[i]];
			}
		}
		return copy;
	}
	public C[][] copyOfSubMatrix(C[][] matrix, int rowIndexFrom, int rowIndexTo, int colIndexFrom, int colIndexTo) {
		final int rows = rowIndexTo - rowIndexFrom;
		final int cols = colIndexTo - colIndexFrom;
		final C[][] copy = newMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				copy[i][j] = matrix[rowIndexFrom + i][colIndexFrom + j];
			}
		}
		return copy;
	}
	public C[][] copyOfSubMatrix(C[][] matrix, int[] rowIndices, int[] columnIndices) {
		if (rowIndices == null) {
			return copyOfColumnSubMatrix(matrix, columnIndices);
		}
		else if (columnIndices == null) {
			return copyOfRowSubMatrix(matrix, rowIndices);
		}
		final int rows = rowIndices.length;
		final int cols = columnIndices.length;
		final C[][] copy = newMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				copy[i][j] = matrix[rowIndices[i]][columnIndices[j]];
			}
		}
		return copy;
	}
	
	public C[][] transpose(C[][] matrix) {
		final int rows = getRowCount(matrix);
		final int cols = getColumnCount(matrix);
		final C[][] trans = newMatrix(cols, rows);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				trans[c][r] = matrix[r][c];
			}
		}
		return trans;
	}
	
}
