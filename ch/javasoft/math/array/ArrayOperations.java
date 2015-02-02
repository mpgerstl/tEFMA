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
package ch.javasoft.math.array;

import java.io.PrintWriter;

/**
 * Array operations is a collection of array operations, particularly focusing
 * on arrays used as vectors or 2D matrices. For instance, elements can be 
 * copied from a matrix to another matrix, or from vectors to matrices and so 
 * on.
 * 
 * @type A	the type of the array, for instance double[] or String[]
 */
public interface ArrayOperations<A> {
	
	/**
	 * Returns the number type class of an array of values
	 */
	Class<A> arrayClass();

	/**
	 * Creates a new vector of length {@code length}. If elements are objects,
	 * all entries are {@code null}; primitive type elements are initialize with 
	 * {@code 0}.
	 * 
	 * @param length the length of the vector
	 * @return	a vector of the specified length
	 */
	A newVector(int length);

	/**
	 * Creates a new 2D matrix of the specified dimensions. The first dimension
	 * of the returned array contains the rows. If elements are objects, all 
	 * entries are {@code null}; primitive type elements are initialize with 
	 * {@code 0}.
	 * 
	 * @param rows		the number of rows in the new matrix, length of dim 1
	 * @param columns	the number of columns in the new matrix, length of dim 2
	 */
	A[] newMatrix(int rows, int columns);
	
	/**
	 * Creates a new diagonal matrix containing the elements given in the 
	 * {@code diagonal} vector. 
	 * 
	 * @param diagonal	the vector containing elements put to the diagonal in 
	 * 					the resulting matrix
	 * @return a matrix containing the given elements in the diagonal
	 */
	A[] newDiagonalMatrix(A diagonal);

	/**
	 * Returns the length of the given array
	 * 
	 * @param array	the array for which to return the length
	 */
	int getLength(A array);

	/**
	 * Returns the first dimension of the given 2D array, associated with matrix
	 * rows
	 * 
	 * @param matrix	the matrix for which to return the number of rows
	 */
	int getRowCount(A[] matrix);

	/**
	 * Returns the second dimension of the given 2D array, associated with 
	 * matrix columns
	 * 
	 * @param matrix	the matrix for which to return the number of columns
	 */
	int getColumnCount(A[] matrix);
	
	/**
	 * Returns a single vector element as string
	 * 
	 * @param vector	the vector
	 * @param index		the index in the vector
	 * @return	the vector element as string
	 */
	String getAsString(A vector, int index);
	
	/**
	 * Returns a single matrix element as string
	 * 
	 * @param matrix	the matrix
	 * @param row		the row index in the matrix
	 * @param column	the column index in the matrix
	 * @return	the matrix element as string
	 */
	String getAsString(A[] matrix, int row, int column);
	
	/**
	 * Returns the vector signature string as follows (assuming that 
	 * {@code name="Vec"}):
	 * <pre> 
	 * Vec:1x4  if rowvec = true
	 * Vec:4x1  otherwise
	 * </pre> 
	 */
	String getVectorSignatureString(String name, A vector, boolean rowvec);
	/**
	 * Returns the matrix signature string looks as follows (assuming that 
	 * {@code name="Mat"}):
	 * <pre> 
	 * Mat:5x2
	 * </pre> 
	 */
	String getMatrixSignatureString(String name, A[] matrix);
	
	/**
	 * Swaps the two elements {@code v[index1]} and {@code v[index2]} in vector
	 * {@code v}
	 * 
	 * @param v			the vector
	 * @param index1	the index identifying the first element in {@code v}
	 * @param index2	the index identifying the second element in {@code v}
	 */
	void swapVectorElements(A v, int index1, int index2);
	
	/**
	 * Swaps the two elements {@code matrix[row1][col1]} and 
	 * {@code matrix[row2][col2]} in {@code matrix}
	 * 
	 * @param matrix	the matrix
	 * @param row1		the row index to identify the first element in 
	 * 					{@code matrix}
	 * @param col1		the column index to identify the first element in 
	 * 					{@code matrix}
	 * @param row2		the row index to identify the second element in 
	 * 					{@code matrix}
	 * @param col2		the column index to identify the second element in 
	 * 					{@code matrix}
	 */
	void swapMatrixElements(A[] matrix, int row1, int col1, int row2, int col2);
	
	/**
	 * Swaps all corresponding row elements {@code matrix[row1][*]} and 
	 * {@code matrix[row2][*]} in {@code matrix}
	 * 
	 * @param matrix	the matrix
	 * @param row1		the index identifying the first row in {@code matrix}
	 * @param row2		the index identifying the second row in {@code matrix}
	 */
	void swapMatrixRows(A[] matrix, int row1, int row2);
	
	/**
	 * Swaps all corresponding column elements {@code matrix[*][col1]} and 
	 * {@code matrix[*][col2]} in {@code matrix}
	 * 
	 * @param matrix	the matrix
	 * @param col1		the index identifying the first column in {@code matrix}
	 * @param col2		the index identifying the second column in 
	 * 					{@code matrix}
	 */
	void swapMatrixColumns(A[] matrix, int col1, int col2);

	/**
	 * Swaps the two elements {@code v1[index1]} and {@code v2[index2]} in the 
	 * two vectors {@code v1} and {@code v2}
	 * 
	 * @param v1		the first vector
	 * @param index1	the index identifying the first element in {@code v1}
	 * @param v2		the second vector
	 * @param index2	the index identifying the second element in {@code v2}
	 */
	void swapVectorVectorElements(A v1, int index1, A v2, int index2);

	/**
	 * Swaps the two elements {@code v[index1]} and {@code v[row][col]} in 
	 * vector {@code v} and matrix {@code m}
	 * 
	 * @param v		the vector containing the first element
	 * @param index	the index identifying the first element in {@code v}
	 * @param m		the matrix containing the second element
	 * @param row	the row index to identify the second element in {@code m}
	 * @param col	the column index to identify the second element in {@code m}
	 */
	void swapVectorMatrixElements(A v, int index, A[] m, int row, int col);

	/**
	 * Swaps the two elements {@code m1[row1][col1]} and {@code m2[row2][col2]} 
	 * in the two matrices {@code m1} and {@code m2}
	 * 
	 * @param m1	the first matrix
	 * @param row1	the row index to identify the first element in {@code m1}
	 * @param col1	the column index to identify the first element in {@code m1}
	 * @param m2	the second matrix
	 * @param row2	the row index to identify the second element in {@code m2}
	 * @param col2	the column index to identify the second element in {@code m2}
	 */
	void swapMatrixMatrixElements(A[] m1, int row1, int col1, A[] m2, int row2, int col2);

	/**
	 * Copies all elements of the {@code src} vector to the {@code dst} vector,
	 * that is
	 * <p>
	 * {@code dst[0 : length-1] = src[0 : length-1]}
	 * <p>
	 * The {@code dst} vector must have at least the length of the {@code src}
	 * vector, otherwise, an exception is thrown.
	 * 
	 * @param src			the source array
	 * @param dst			the destination array
	 * 
	 * @throws ArrayIndexOutOfBoundsException if the length of {@code dst} is 
	 *			smaller than the length of {@code src}
	 */
	void copyVector(A src, A dst);

	/**
	 * Copies the specified element from {@code src} to {@code dst}, that is,
	 * <p>
	 * {@code dst[dstIndex] = src[srcIndex]}
	 * 
	 * @param src		the source array
	 * @param srcIndex	the index in {@code src}
	 * @param dst		the destination array
	 * @param dstIndex	the index in {@code dst}
	 */
	void copyVectorElement(A src, int srcIndex, A dst, int dstIndex);
	
	/**
	 * Copies the specified elements from {@code src} to {@code dst}, that is,
	 * <p>
	 * {@code dst[dstStart : dstStart+length-1] = src[srcStart : srcStart+length-1]}
	 * 
	 * @param src		the source array
	 * @param srcStart	the first index in {@code src}
	 * @param dst		the destination array
	 * @param dstStart	the first index in {@code dst}
	 * @param length	the number of elements to copy
	 */
	void copyVectorElements(A src, int srcStart, A dst, int dstStart, int length);
	
	/**
	 * Copies the specified elements from {@code src} to {@code dst}, that is,
	 * <p>
	 * {@code dst[dstIndices[..]] = src[srcIndices[..]]}
	 * 
	 * @param src			the source array
	 * @param srcIndices	the indices in {@code src}
	 * @param dst			the destination array
	 * @param dstIndices	the indices in {@code dst}
	 */
	void copyVectorElements(A src, int[] srcIndices, A dst, int[] dstIndices);

	/**
	 * Copies the specified elements from the {@code src} vector to a row of the 
	 * {@code dst} matrix, that is,
	 * <p>
	 * {@code dst[dstRow][dstColStart : dstColStart+length-1] = src[srcStart : srcStart+length-1]}
	 * 
	 * @param src			the source array
	 * @param srcStart		the first index in {@code src}
	 * @param dst			the destination array
	 * @param dstRow		the row index in {@code dst}
	 * @param dstColStart	the first column index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyVectorElementsToMatrixRow(A src, int srcStart, A[] dst, int dstRow, int dstColStart, int length);

	/**
	 * Copies the specified elements from the {@code src} vector to a row of the 
	 * {@code dst} matrix, that is,
	 * <p>
	 * {@code dst[dstRowStart : dstRowStart+length-1][dstCol] = src[srcStart : srcStart+length-1]}
	 * 
	 * @param src			the source array
	 * @param srcStart		the first index in {@code src}
	 * @param dst			the destination array
	 * @param dstRowStart	the first row index in {@code dst}
	 * @param dstCol		the column index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyVectorElementsToMatrixColumn(A src, int srcStart, A[] dst, int dstRowStart, int dstCol, int length);

	/**
	 * Copies all elements of the {@code src} matrix to the {@code dst} matrix,
	 * that is
	 * <p>
	 * {@code dst[0 : rows-1][0 : cols-1] = src[0 : rows-1][0 : cols-1]}
	 * <p>
	 * The {@code dst} matrix must have at least as many rows and columns as the
	 * {@code src} matrix
	 * 
	 * @param src			the source array
	 * @param dst			the destination array
	 * 
	 * @throws ArrayIndexOutOfBoundsException if there are fewer rows or columns
	 * 		in {@code dst} than in {@code src}
	 */
	void copyMatrix(A[] src, A[] dst);

	/**
	 * Copies the specified element from {@code src} to {@code dst}, that is,
	 * <p>
	 * {@code dst[dstRow][dstCol] = src[srcRow][srcCol]}
	 * 
	 * @param src		the source array
	 * @param srcRow	the row index in {@code src}
	 * @param srcCol	the column index in {@code src}
	 * @param dst		the destination array
	 * @param dstRow	the row index in {@code dst}
	 * @param dstCol	the column index in {@code dst}
	 */
	void copyMatrixElement(A[] src, int srcRow, int srcCol, A[] dst, int dstRow, int dstCol);

	/**
	 * Copies the specified matrix elements from {@code src} to {@code dst}, that 
	 * is,
	 * <p>
	 * {@code dst[dstRowStart : dstRowStart+rows-1][dstColStart : dstColStart+cols-1] = src[srcRowStart : srcRowStart+rows-1][srcColStart : srcColStart+cols-1]}
	 * 
	 * @param src			the source array
	 * @param srcRowStart	the first row index in {@code src}
	 * @param srcColStart	the first column index in {@code src}
	 * @param dst			the destination array
	 * @param dstRowStart	the first row index in {@code dst}
	 * @param dstColStart	the first column index in {@code dst}
	 * @param rows			the number of rows to copy
	 * @param cols			the number of columns to copy
	 */
	void copyMatrixElements(A[] src, int srcRowStart, int srcColStart, A[] dst, int dstRowStart, int dstColStart, int rows, int cols);

	/**
	 * Copies the specified row elements from {@code src} to {@code dst}, that 
	 * is,
	 * <p>
	 * {@code dst[dstRow][dstColStart : dstColStart+length-1] = src[srcRow][srcColStart : srcColStart+length-1]}
	 * 
	 * @param src			the source array
	 * @param srcRow		the row index in {@code src}
	 * @param srcColStart	the first column index in {@code src}
	 * @param dst			the destination array
	 * @param dstRow		the row index in {@code dst}
	 * @param dstColStart	the first column index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyMatrixRowElements(A[] src, int srcRow, int srcColStart, A[] dst, int dstRow, int dstColStart, int length);

	/**
	 * Copies the specified column elements from {@code src} to {@code dst}, 
	 * that is,
	 * <p>
	 * {@code dst[dstRowStart : dstRowStart+length-1][dstCol] = src[srcRowStart : srcRowStart+length-1][srcCol]}
	 * 
	 * @param src			the source array
	 * @param srcRowStart	the first row index in {@code src}
	 * @param srcCol		the column index in {@code src}
	 * @param dst			the destination array
	 * @param dstRowStart	the first row index in {@code dst}
	 * @param dstCol		the column index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyMatrixColumnElements(A[] src, int srcRowStart, int srcCol, A[] dst, int dstRowStart, int dstCol, int length);
	
	/**
	 * Copies the specified row elements from the {@code src} matrix to the  
	 * vector {@code dst}, that  is,
	 * <p>
	 * {@code dst[dstStart : dstStart+length-1] = src[srcRow][srcColStart : srcColStart+length-1]}
	 * 
	 * @param src			the source array
	 * @param srcRow		the row index in {@code src}
	 * @param srcColStart	the first column index in {@code src}
	 * @param dst			the destination array
	 * @param dstStart		the first index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyMatrixRowElementsToVector(A[] src, int srcRow, int srcColStart, A dst, int dstStart, int length);

	/**
	 * Copies the specified column elements from the {@code src} matrix to the  
	 * vector {@code dst}, that  is,
	 * <p>
	 * {@code dst[dstStart : dstStart+length-1] = src[srcRowStart : srcRowStart+length-1][srcCol]}
	 * 
	 * @param src			the source array
	 * @param srcRowStart	the first row index in {@code src}
	 * @param srcCol		the column index in {@code src}
	 * @param dst			the destination array
	 * @param dstStart		the first index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyMatrixColumnElementsToVector(A[] src, int srcRowStart, int srcCol, A dst, int dstStart, int length);
	
	/**
	 * Copies the specified row elements from the {@code src} matrix to the  
	 * column of the {@code dst} matrix, that  is,
	 * <p>
	 * {@code dst[dstRowStart : dstRowStart+length-1][dstCol] = src[srcRow][srcColStart : srcColStart+length-1]}
	 * 
	 * @param src			the source array
	 * @param srcRow		the row index in {@code src}
	 * @param srcColStart	the first column index in {@code src}
	 * @param dst			the destination array
	 * @param dstRowStart	the first row index in {@code dst}
	 * @param dstCol		the column index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyMatrixRowElementsToColumn(A[] src, int srcRow, int srcColStart, A[] dst, int dstRowStart, int dstCol, int length);

	/**
	 * Copies the specified column elements from the {@code src} matrix to the  
	 * row of the {@code dst} matrix, that  is,
	 * <p>
	 * {@code dst[dstRowStart : dstRowStart+length-1][dstCol] = src[srcRow][srcColStart : srcColStart+length-1]}
	 * 
	 * @param src			the source array
	 * @param srcRowStart	the first row index in {@code src}
	 * @param srcCol		the column index in {@code src}
	 * @param dst			the destination array
	 * @param dstRow		the row index in {@code dst}
	 * @param dstColStart	the first column index in {@code dst}
	 * @param length		the number of elements to copy
	 */
	void copyMatrixColumnElementsToRow(A[] src, int srcRowStart, int srcCol, A[] dst, int dstRow, int dstColStart, int length);

	/**
	 * Copies the matrix diagonal elements from the {@code src} matrix to the  
	 * {@code dst} vector, that  is,
	 * <p>
	 * {@code dst[i] = src[i][i]} for {@code 0 <= i < len}, with
	 * {@code len = min(src#rows, src#cols) = dst#length}, throwing an exception 
	 * if minimal dimension of {@code src} is not equal to the length of 
	 * {@code dst}.
	 * 
	 * @param src			the source matrix
	 * @param dst			the destination vector
	 * @throws 	IllegalArgumentException	if the minimum dimension of the 
	 * 			{@code src} matrix is not equal to the length of the 
	 * 			{@code dst} vector
	 */
	void copyMatrixDiagonalElementsToVector(A[] src, A dst);

	/**
	 * Copies the elements of the {@code src} vector to the diagonal of the 
	 * {@code dst} matrix, that  is,
	 * <p>
	 * {@code dst[i][i] = src[i]} for {@code 0 <= i < len}, with
	 * {@code len = min(dst#rows, dst#cols) = src#length}, throwing an exception 
	 * if minimal dimension of {@code dst} is not equal to the length of 
	 * {@code src}.
	 * 
	 * @param src			the source vector
	 * @param dst			the destination matrix
	 * @throws 	IllegalArgumentException	if the minimum dimension of the 
	 * 			{@code dst} matrix is not equal to the length of the 
	 * 			{@code src} vector
	 */
	void copyVectorElementsToMatrixDiagonal(A src, A[] dst);
	
	/**
	 * Creates a copy of the diagonal elements from the {@code src} matrix and
	 * returns the resulting vector. 
	 * 
	 * @param matrix	the source matrix
	 * @return a vector containing the matrix diagonal elements 
	 * 			{@code matrix[i][i]} for {@code 0 <= i < min(#rows, #cols)}
	 */
	A copyOfMatrixDiagonal(A[] matrix);
	
	/**
	 * Returns a single row of the given matrix
	 * 
	 * @param matrix	the matrix from which to return a row
	 * @param row		the row index in the matrix, zero based
	 */
	A copyOfMatrixRow(A[] matrix, int row);

	/**
	 * Returns a copy of the range of a single matrix row, that is,
	 * {@code matrix[row][colFrom : colTo)} with inclusive from and exclusive to
	 * indices
	 * 
	 * @param matrix	the matrix from which to return a row
	 * @param row		the row index in the matrix, zero based
	 * @param colFrom	the first column index, inclusive
	 * @param colTo		the last column index, exclusive
	 */
	A copyOfMatrixRowRange(A[] matrix, int row, int colFrom, int colTo);

	/**
	 * Returns a single column of the given matrix
	 * 
	 * @param matrix	the matrix from which to return a column
	 * @param column	the column index in the matrix, zero based
	 */
	A copyOfMatrixColumn(A[] matrix, int column);

	/**
	 * Returns a copy of the range of a single matrix column, that is,
	 * {@code matrix[rowFrom : rowTo)[column]} with inclusive from and exclusive 
	 * to indices
	 * 
	 * @param matrix	the matrix from which to return a row
	 * @param rowFrom	the first row index, inclusive
	 * @param rowTo		the last row index, exclusive
	 * @param column	the column index in the matrix
	 */
	A copyOfMatrixColumnRange(A[] matrix, int rowFrom, int rowTo, int column);

	/**
	 * Returns a copy of the given vector
	 * 
	 * @param vector the vector to copy
	 */
	A copyOfVector(A vector);

	/**
	 * Returns a copy of the given vector, possibly shortened
	 * 
	 * @param vector 	the vector to copy
	 * @param newLength	the length of the new vector
	 */
	A copyOfVector(A vector, int newLength);

	/**
	 * Returns a copy of the vector range {@code from} (inclusive) to {@code to} 
	 * (exclusive)
	 * 
	 * @param vector	the vector to copy
	 * @param from		the start index, inclusive
	 * @param to		the end index, exclusive
	 */
	A copyOfVectorRange(A vector, int from, int to);

	/**
	 * Returns a copy of the given matrix
	 * 
	 * @param matrix the matrix to copy
	 */
	A[] copyOfMatrix(A[] matrix);
	
	/**
	 * Returns the specified submatrix. An empty index array means no elements,
	 * null means all elements.
	 * 
	 * @param matrix		the matrix from which to take the submatrix
	 * @param rowIndices	the row indices to include in the submatrix, where
	 * 						an empty array means that the submatrix contains no 
	 * 						rows, and null means that all rows are included
	 */
	A[] copyOfRowSubMatrix(A[] matrix, int... rowIndices);

	/**
	 * Returns the specified submatrix. An empty index array means no elements,
	 * null means all elements.
	 * 
	 * @param matrix		the matrix from which to take the submatrix
	 * @param columnIndices	the column indices to include in the submatrix, 
	 * 						where an empty array means that the submatrix 
	 * 						contains no columns, and null means that all columns 
	 * 						are included
	 */
	A[] copyOfColumnSubMatrix(A[] matrix, int... columnIndices);

	/**
	 * Returns a copy of the sub matrix 
	 * {@code [rowIndexFrom : rowIndexTo)[colIndexFrom : colIndexTo)}, meaning
	 * that from indices are inclusive and to indices exclusive.
	 * 
	 * @param matrix		the matrix from which to take the submatrix
	 * @param rowIndexFrom	the first row index, inclusive
	 * @param rowIndexTo	the last row index, exclusive
	 * @param colIndexFrom	the first column index, inclusive
	 * @param colIndexTo	the last column index, exclusive
	 */
	A[] copyOfSubMatrix(A[] matrix, int rowIndexFrom, int rowIndexTo, int colIndexFrom, int colIndexTo);

	/**
	 * Returns the specified submatrix. An empty index array means no elements,
	 * null means all elements.
	 * 
	 * @param matrix		the matrix from which to take the submatrix
	 * @param rowIndices	the row indices to include in the submatrix, where
	 * 						an empty array means that the submatrix contains no 
	 * 						rows, and null means that all rows are included
	 * @param columnIndices	the column indices to include in the submatrix, 
	 * 						where an empty array means that the submatrix 
	 * 						contains no columns, and null means that all columns 
	 * 						are included
	 */
	A[] copyOfSubMatrix(A[] matrix, int[] rowIndices, int[] columnIndices);
	
	/**
	 * Returns the transpose of the input matrix.
	 * 
	 * @param matrix	the matrix to transpose
	 * @return <code>matrix<sup>T</sup></code>
	 */
	A[] transpose(A[] matrix);
	
	/**
	 * Prints the vector using the specified writer. The printed string looks as 
	 * follows (assuming that {@code name="Vec"}):
	 * <pre> 
	 * Vec = [ 1 , 2 , -3 , 5 ]
	 * </pre>
	 * The string is terminated with a newline character if 
	 * {@code newLine = true}.
	 */
	void printVector(PrintWriter writer, String name, A vector, boolean newLine);
	/**
	 * Prints the vector signature using the specified writer. The printed 
	 * string looks as follows (assuming that {@code name="Vec"}):
	 * <pre> 
	 * Vec:1x4  if rowvec = true
	 * Vec:4x1  otherwise
	 * </pre> 
	 * The string is terminated with a newline character if 
	 * {@code newLine = true}.
	 */
	void printVectorSignature(PrintWriter writer, String name, A vector, boolean rowvec, boolean newLine);
	/**
	 * Prints the matrix using the specified writer. The printed string spans
	 * multiple lines and looks as follows (assuming that {@code name="Mat"}):
	 * <pre> 
	 * Mat = 5x2 {
	 *   [ 0 , -3 ]
	 *   [ 1 , 1 ]
	 *   [ -1 , 1 ]
	 *   [ 1/2 , -1/2 ]
	 *   [ 1 , 1 ]
	 * }
	 * </pre> 
	 */
	void printMatrix(PrintWriter writer, String name, A[] matrix);
	/**
	 * Prints the matrix signature using the specified writer. The printed 
	 * string looks as follows (assuming that {@code name="Mat"}):
	 * <pre> 
	 * Mat:5x2
	 * </pre> 
	 * The string is terminated with a newline character if 
	 * {@code newLine = true}.
	 */
	void printMatrixSignature(PrintWriter writer, String name, A[] matrix, boolean newLine);
	
}
