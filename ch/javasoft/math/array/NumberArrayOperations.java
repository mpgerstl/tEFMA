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

import ch.javasoft.math.operator.AggregatingBinaryOperator;
import ch.javasoft.math.operator.AggregatingUnaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.operator.IntUnaryOperator;
import ch.javasoft.math.operator.UnaryOperator;

/**
 * <code>NumberArrayOperations</code> defines utility methods to apply numeric
 * operators to arrays representing vectors or matrices. An instance of this 
 * class defines the (boxed) number type and the array type, such as
 * {@link Double} and {@code double[]}. 
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public interface NumberArrayOperations<N extends Number, A> {

	/**
	 * Returns the number type class for a single value
	 */
	Class<N> numberClass();

	/**
	 * Returns the number type class of an array of values
	 */
	Class<A> arrayClass();
	
	/**
	 * Returns the number operators for the number type of this operations
	 */
	NumberOperators<N, A> getNumberOperators();
	
	/**
	 * Returns the array operations, used to create, copy and handle vectors and
	 * matrices
	 */
	ArrayOperations<A> getArrayOperations();
	
	/**
	 * Returns a converter that is able to convert matrices, vectors and numbers 
	 * of input type {@code IN} to the current type {@code N}.
	 * <pre>
	 *   converter(IN x) = convert x to N
	 * </pre>
	 */
	<IN extends Number, IA> Converter<IN, IA, N, A> getConverterFrom(final NumberArrayOperations<IN, IA> fromOps);
	
	/**
	 * Returns a converter that is able to convert matrices, vectors and numbers 
	 * of the current type {@code N} to the result type {@code RN}.
	 * <pre>
	 *   converter(N x) = convert x to RN
	 * </pre>
	 */
	<RN extends Number, RA> Converter<N, A, RN, RA> getConverterTo(final NumberArrayOperations<RN, RA> toOps);
	
	/**
	 * Returns the expression composer, used to compose arithmetic expressions
	 */
	ExpressionComposer<N, A> getExpressionComposer();
	
	/**
	 * Converts any number value to this operations' number type.
	 */
	N convertNumber(Number value);
	
	/**
	 * Creates a new vector of length {@code length}. All vector elements are 
	 * initialized with {@code initialValue}.
	 * 
	 * @param length 		the length of the vector
	 * @param initialValue	the value used to initialize all vector elements
	 * @return	a vector of the specified length
	 */
	A newVector(int length, N initialValue);

	/**
	 * Creates a new 2D matrix of the specified dimensions. The first dimension
	 * of the returned array contains the rows. All matrix elements are 
	 * initialized with {@code initialValue}.
	 * 
	 * @param rows		the number of rows in the new matrix, length of dim 1
	 * @param columns	the number of columns in the new matrix, length of dim 2
	 * @param initialValue	the value used to initialize all matrix elements
	 */
	A[] newMatrix(int rows, int columns, N initialValue);

	/**
	 * Creates a new vector of length {@code length}. All vector elements are 
	 * initialized with zero.
	 * 
	 * @param length the length of the vector
	 * @return	a vector of the specified length
	 */
	A newZeroVector(int length);

	/**
	 * Creates a new 2D matrix of the specified dimensions. The first dimension
	 * of the returned array contains the rows. All matrix elements are 
	 * initialized with zero.
	 * 
	 * @param rows		the number of rows in the new matrix, length of dim 1
	 * @param columns	the number of columns in the new matrix, length of dim 2
	 */
	A[] newZeroMatrix(int rows, int columns);

	/**
	 * Creates a new vector of length {@code length}. All vector elements are 
	 * initialized with one.
	 * 
	 * @param length the length of the vector
	 * @return	a vector of the specified length
	 */
	A newOneVector(int length);

	/**
	 * Creates a new 2D matrix of the specified dimensions. The first dimension
	 * of the returned array contains the rows. All matrix elements are 
	 * initialize with one.
	 * 
	 * @param rows		the number of rows in the new matrix, length of dim 1
	 * @param columns	the number of columns in the new matrix, length of dim 2
	 */
	A[] newOneMatrix(int rows, int columns);

	/**
	 * Creates a new square 2D matrix of size {@code size x size}. The first 
	 * dimension of the returned array contains the rows. All diagonal elements
	 * are initialized with one, all other matrix elements with zero.
	 * 
	 * @param size	the number of rows and columns in the returned square matrix
	 */
	A[] newIdentityMatrix(int size);

	/**
	 * Creates a new square 2D matrix of size {@code size x size}. The first 
	 * dimension of the returned array contains the rows. All diagonal elements
	 * are initialized with {@code diagonalValue}, all other matrix elements 
	 * with zero.
	 * 
	 * @param size	the number of rows and columns in the returned square matrix
	 * @param diagonalValue	the value to put to the diagonal of the matrix
	 */
	A[] newDiagonalMatrix(int size, N diagonalValue);

	/**
	 * Creates a new square 2D matrix of size {@code n x n}, where {@code n} 
	 * denotes the number of elements in the {@code diagonalValues} vector. The 
	 * first dimension of the returned array contains the rows. The diagonal 
	 * elements of the matrix are equal to those in the {@code diagonalValues} 
	 * vector, all other matrix elements are initialized with zero.
	 * 
	 * @param diagonalValues	the values to put to the diagonal of the matrix
	 */
	A[] newDiagonalMatrix(A diagonalValues);

	/**
	 * Returns a single element of the given vector
	 * 
	 * @param vector	the vector from which to return an element
	 * @param index		the index in the vector, zero based
	 * @return the value {@code vector[index}
	 */
	N get(A vector, int index);
	
	/**
	 * Returns a single element of the given matrix
	 * 
	 * @param matrix	the matrix from which to return an element
	 * @param row		the row index in the matrix, zero based
	 * @param col		the column index in the matrix, zero based
	 */
	N get(A[] matrix, int row, int col);

    /**
     * Returns the signum function of the specified value.  The return value is 
     * -1 if the {@code vector[index]} is negative; 0 if it is zero; and 1 if 
     * the specified value is positive.
     *
	 * @param vector	the vector from which to return an element's signum
	 * @param index		the index in the vector, zero based
     * @return the signum function of {@code vector[index]}
	 */
	int getSignum(A vector, int index);

    /**
     * Returns the signum function of the specified value.  The return value is 
     * -1 if the {@code matrix[row][col]} is negative; 0 if it is zero; and 1 if 
     * the specified value is positive.
     *
	 * @param matrix	the matrix from which to return an element's signum
	 * @param row		the row index in the matrix, zero based
	 * @param col		the column index in the matrix, zero based
     * @return the signum function of {@code matrix[row][col]}
	 */
	int getSignum(A[] matrix, int row, int col);

	/**
	 * Returns the support of a vector as bits stored in long values. The bit
	 * {@code i} in the return value is set if the i<sup>th</sup> value in the
	 * vector {@code A} is non-zero.
	 * <p>
	 * Let {@code ret} be the return value. Then
	 * <pre>
	 *   ret[0] & 0x1 = (vector[0] &ne; 0) 
	 *   ret[0] & 0x2 = (vector[1] &ne; 0)
	 *   ... 
	 *   ret[0] & 0x8000000000000000L = (vector[63] &ne; 0)
	 *   ret[1] & 0x1                 = (vector[64] &ne; 0)
	 *   ... 
	 * </pre> 
	 * 
	 * @param vector the input vector
	 * @return the support of the given vector as bits in the returned longs
	 */	
	long[] getVectorSupportAsLongBits(A vector);
	
	/**
	 * Returns the support of a matrix as bits stored in long values. The bit
	 * {@code col} in the return value {@code ret[row]} is set if the matrix
	 * value {@code A[row][col]} is non-zero.
	 * <p>
	 * Let {@code ret} be the return value. Then
	 * <pre>
	 *   ret[row][0] & 0x1 = (matrix[row][0] &ne; 0) 
	 *   ret[row][0] & 0x2 = (matrix[row][1] &ne; 0)
	 *   ... 
	 *   ret[row][0] & 0x8000000000000000L = (matrix[row][63] &ne; 0)
	 *   ret[row][1] & 0x1                 = (matrix[row][64] &ne; 0)
	 *   ... 
	 * </pre> 
	 * 
	 * @param matrix the input matrix
	 * @return the support of the given matrix as bits in the returned longs
	 */	
	long[][] getMatrixSupportAsLongBits(A[] matrix);

	/**
	 * Returns the support of a matrix row as bits stored in long values. The 
	 * bit {@code i} in the return value is set if the matrix value
	 * {@code A[row][i]} is non-zero.
	 * <p>
	 * Let {@code ret} be the return value. Then
	 * <pre>
	 *   ret[0] & 0x1 = (matrix[row][0] &ne; 0) 
	 *   ret[0] & 0x2 = (matrix[row][1] &ne; 0)
	 *   ... 
	 *   ret[0] & 0x8000000000000000L = (matrix[row][63] &ne; 0)
	 *   ret[1] & 0x1                 = (matrix[row][64] &ne; 0)
	 *   ... 
	 * </pre> 
	 * 
	 * @param matrix	the input matrix
	 * @param row		the matrix row
	 * @return 	the support of the specified matrix row as bits in the returned 
	 * 			longs
	 */	
	long[] getMatrixRowSupportAsLongBits(A[] matrix, int row);

	/**
	 * Returns the support of a matrix column as bits stored in long values. The 
	 * bit {@code i} in the return value is set if the matrix value
	 * {@code A[i][col]} is non-zero.
	 * <p>
	 * Let {@code ret} be the return value. Then
	 * <pre>
	 *   ret[0] & 0x1 = (matrix[0][col] &ne; 0) 
	 *   ret[0] & 0x2 = (matrix[1][col] &ne; 0)
	 *   ... 
	 *   ret[0] & 0x8000000000000000L = (matrix[63][col] &ne; 0)
	 *   ret[1] & 0x1                 = (matrix[64][col] &ne; 0)
	 *   ... 
	 * </pre> 
	 * 
	 * @param matrix	the input matrix
	 * @param col		the matrix column
	 * @return 	the support of the specified matrix column as bits in the 
	 * 			returned longs
	 */	
	long[] getMatrixColumnSupportAsLongBits(A[] matrix, int col);

	/**
	 * Sets a single element of the given vector
	 * 
	 * @param vector	the vector to which the given value is written
	 * @param index		the index in the vector, zero based
	 * @param value		the value to be set
	 */
	void set(A vector, int index, N value);

	/**
	 * Sets a single element of the given matrix
	 * 
	 * @param matrix	the matrix to which the given value is written
	 * @param row		the row index in the matrix, zero based
	 * @param col		the column index in the matrix, zero based
	 * @param value		the value to be set
	 */
	void set(A[] matrix, int row, int col, N value);

	/**
	 * Sets all elements of {@code vector} to {@code value} 
	 * 
	 * @param vector	the vector to which the given value is written
	 * @param value		the value to be set
	 */
	void setAll(A vector, N value);

	/**
	 * Sets all elements in the {@code matrix} to {@code value} 
	 * 
	 * @param matrix	the matrix to which the given value is written
	 * @param value		the value to be set
	 */
	void setAll(A[] matrix, N value);

	/**
	 * Applies the given operator to each element of {@code v} and returns the 
	 * result vector. The source vector is not modified, a new vector instance 
	 * of equal length is returned.
	 * 
	 * @param v			the source vector to which the given operator is applied 
	 * @param operator	the operator
	 * @return	the result vector with same length as {@code v}
	 */
	A applyToEachElement(A v, UnaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to each element of the {@code src} vector and 
	 * writes the result to the {@code dst} vector. The source vector is not 
	 * modified, unless source and destination vector are the same instance. The 
	 * destination vector must have the same length as the source vector, 
	 * otherwise, an exception is thrown.
	 * 
	 * @param src		the source vector to which the given operator is applied 
	 * @param dst		the destination vector for the results 
	 * @param operator	the operator
	 * @throws IllegalArgumentException if {@code src} and {@code dst}
	 * 									have not equal length
	 */
	void applyToEachElement(A src, A dst, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element in the specified range of the
	 * {@code src} vector and writes the result to the range in the {@code dst}
	 * vector. More precisely, the operator is applied to
	 * {@code src[srcFrom : srcFrom+length-1]} of vector {@code src} and written
	 * to {@code dst[dstFrom : dstFrom+length-1]}. The source vector is not 
	 * modified, unless source and destination vector are the same instance. 
	 * 
	 * @param src		the source vector to which the given operator is applied 
	 * @param srcStart	the start index in {@code src}
	 * @param dst		the destination vector for the results 
	 * @param dstStart	the start index in {@code dst}
	 * @param length	the number of elements to consider
	 * @param operator	the operator
	 */
	void applyToEachElement(A src, int srcStart, A dst, int dstStart, int length, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element of {@code matrix} and returns 
	 * the result matrix. The source matrix is not modified, a new matrix 
	 * instance of equal size is returned.
	 * 
	 * @param matrix	the source matrix to which the given operator is applied 
	 * @param operator	the operator
	 * @return	the result matrix with same size as {@code matrix}
	 */
	A[] applyToEachElement(A[] matrix, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element of the {@code src} matrix and 
	 * writes the result to the {@code dst} matrix. The source matrix is not 
	 * modified, unless source and destination matrices are the same instance. 
	 * The destination matrix must have the same number of rows and columns as
	 * the source matrix, otherwise, an exception is thrown.
	 * 
	 * @param src		the source vector to which the given operator is applied 
	 * @param dst		the destination vector for the results 
	 * @param operator	the operator
	 * @throws IllegalArgumentException if {@code src} and {@code dst}
	 * 									have not equal size
	 */
	void applyToEachElement(A[] src, A[] dst, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element in the specified row of the 
	 * {@code src} matrix and writes the result to the row in the {@code dst} 
	 * matrix. More precisely, the operator is applied to
	 * {@code src[srcRow][*]} and written to {@code dst[dstRow][*]}. The source 
	 * matrix is not modified, unless source and destination matrix are the same 
	 * instance. 
	 * 
	 * @param src			the source matrix to which the given operator is applied 
	 * @param srcRow		the row index in {@code src}
	 * @param dst			the destination matrix for the results 
	 * @param dstRow		the row index in {@code dst}
	 * @param operator		the operator
	 */
	void applyToEachRowElement(A[] src, int srcRow, A[] dst, int dstRow, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element in the specified row range of 
	 * the {@code src} matrix and writes the result to the row range in the 
	 * {@code dst} matrix. More precisely, the operator is applied to
	 * {@code src[srcRow][srcColFrom : srcColFrom+length-1]} of and written
	 * to {@code dst[dstRow][dstColFrom : dstColFrom+length-1]}. The source 
	 * matrix is not modified, unless source and destination matrix are the same 
	 * instance. 
	 * 
	 * @param src			the source matrix to which the given operator is applied 
	 * @param srcRow		the row index in {@code src}
	 * @param srcColStart	the first column index in {@code src}
	 * @param dst			the destination matrix for the results 
	 * @param dstRow		the row index in {@code dst}
	 * @param dstColStart	the first column index in {@code dst}
	 * @param length		the number of elements to consider
	 * @param operator		the operator
	 */
	void applyToEachRowElement(A[] src, int srcRow, int srcColStart, A[] dst, int dstRow, int dstColStart, int length, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element in the specified column of the 
	 * {@code src} matrix and writes the result to the column in the {@code dst} 
	 * matrix. More precisely, the operator is applied to {@code src[*][srcCol]} 
	 * and written to {@code dst[*][dstCol]}. The source matrix is not modified, 
	 * unless source and destination matrix are the same instance. 
	 * 
	 * @param src			the source matrix to which the given operator is applied 
	 * @param srcCol		the column index in {@code src}
	 * @param dst			the destination matrix for the results 
	 * @param dstCol		the column index in {@code dst}
	 * @param operator		the operator
	 */
	void applyToEachColumnElement(A[] src, int srcCol, A[] dst, int dstCol, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element in the specified column range 
	 * of the {@code src} matrix and writes the result to the column range in 
	 * the {@code dst} matrix. More precisely, the operator is applied to
	 * {@code src[srcRowFrom : srcRowFrom+length-1][srcCol]} and written
	 * to {@code dst[dstRowFrom : dstRowFrom+length-1][dstCol]}. The source 
	 * matrix is not modified, unless source and destination matrix are the same 
	 * instance. 
	 * 
	 * @param src			the source matrix to which the given operator is applied 
	 * @param srcRowStart	the first row index in {@code src}
	 * @param srcCol		the column index in {@code src}
	 * @param dst			the destination matrix for the results 
	 * @param dstRowStart	the first row index in {@code dst}
	 * @param dstCol		the column index in {@code dst}
	 * @param length		the number of elements to consider
	 * @param operator		the operator
	 */
	void applyToEachColumnElement(A[] src, int srcRowStart, int srcCol, A[] dst, int dstRowStart, int dstCol, int length, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element in the diagonal of the 
	 * {@code src} matrix and writes the result to the diagonal in the 
	 * {@code dst} matrix. More precisely, the operator is applied to 
	 * {@code src[i][i]} and written to {@code dst[i][i]} for 
	 * {@code 0 <= i < len}, with 
	 * {@code len = min(#srcRows, #srcColumns) = min(#dstRows, #dstColumns)}, 
	 * throwing an exception if minimal dimension of {@code src} is not equal to
	 * that of {@code dst}. The source matrix is not modified, unless source and 
	 * destination matrix are the same instance. 
	 * 
	 * @param src			the source matrix to which the given operator is applied 
	 * @param dst			the destination matrix for the results 
	 * @param operator		the operator
	 * @throws 	IllegalArgumentException	if the minimum dimension of the 
	 * 			{@code src} matrix is not equal to the minimum dimension of the 
	 * 			{@code dst} matrix
	 */
	void applyToEachDiagonalElement(A[] src, A[] dst, UnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element pair {@code (v[i], u[i])} of 
	 * the vectors {@code u} and {@code v} and returns the result vector. The 
	 * source vectors are not modified, a new vector instance of equal length is 
	 * returned. The source vectors must have equal length, otherwise, an 
	 * exception is thrown.
	 * 
	 * @param v			the first source vector
	 * @param u			the second source vector
	 * @param operator	the operator
	 * @return	the result vector with same length as {@code v} and {@code u}
	 * @throws IllegalArgumentException if {@code u} and {@code v}
	 * 									have not equal length
	 */
	A applyToElementByElement(A v, A u, BinaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to each element pair 
	 * {@code (src1[i], src2[i])} of the vectors {@code src1} and {@code src2},
	 * and writes the result to the {@code dst} vector. The source vectors are
	 * not modified, unless source and destination vector are the same instance. 
	 * Source and destination vectors must have the same length, otherwise, an 
	 * exception is thrown.
	 * 
	 * @param src1		the first source vector 
	 * @param src2		the second source vector 
	 * @param dst		the destination vector for the results 
	 * @param operator	the operator
	 * @throws IllegalArgumentException if {@code src1}, {@code src2} and 
	 * 									{@code dst} have not equal length
	 */
	void applyToElementByElement(A src1, A src2, A dst, BinaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element pair 
	 * {@code (src1[i][j], src2[i][j])} of the matrices {@code src1} and 
	 * {@code src2} and returns the result matrix. The source matrices are not 
	 * modified, a new matrix instance of equal size is returned. 
	 * The source matrices must have the same number of rows and columns, 
	 * otherwise, an exception is thrown.
	 * 
	 * 
	 * @param src1		the first source matrix
	 * @param src2		the second source matrix
	 * @param operator	the operator
	 * @return the result matrix with same size as {@code src1} and {@code src2}
	 * @throws IllegalArgumentException if {@code src1} and {@code src2}
	 * 									have not equal size
	 */
	A[] applyToElementByElement(A[] src1, A[] src2, BinaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element pair 
	 * {@code (src1[i][j], src2[i][j])} of the matrices {@code src1} and 
	 * {@code src2}, and writes the result to the {@code dst} matrix. The source 
	 * matrices are not modified, unless source and destination matrix are the 
	 * same instance. Source and destination matrices must have the same number 
	 * of rows and columns, otherwise, an exception is thrown.
	 * 
	 * @param src1		the first source matrix
	 * @param src2		the second source matrix
	 * @param dst		the destination matrix for the results 
	 * @param operator	the operator
	 * @throws IllegalArgumentException if {@code src1}, {@code src2} and 
	 * 									{@code dst} have not equal size
	 */
	void applyToElementByElement(A[] src1, A[] src2, A[] dst, BinaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to the vector {@code v} and returns the 
	 * resulting aggregated value.
	 * 
	 * @param v			the source vector to which the given operator is applied 
	 * @param operator	the operator
	 * @return	the aggregated result value
	 */
	N applyTo(A v, AggregatingUnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to row {@code matrix[row][*]} and returns the 
	 * resulting aggregated value.
	 * 
	 * @param matrix	the source matrix
	 * @param row		the row index in the matrix 
	 * @param operator	the operator
	 * @return	the aggregated result value
	 */
	N applyToRow(A[] matrix, int row, AggregatingUnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each row {@code matrix[row][*]} and returns 
	 * a (column) vector containing the resulting aggregated values.
	 * 
	 * @param matrix	the source matrix
	 * @param operator	the operator
	 * @return	a (column) vector containing the aggregated result values
	 */
	A applyToEachRow(A[] matrix, AggregatingUnaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to each row {@code matrix[row][*]} and writes 
	 * the resulting aggregated values to the (column) vector {@code dst}. 
	 * 
	 * @param matrix	the source matrix
	 * @param dst		the destination (column) vector for the resulting 
	 * 					aggregated values 
	 * @param operator	the operator
	 */
	void applyToEachRow(A[] matrix, A dst, AggregatingUnaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to column {@code matrix[*][col]} and returns 
	 * the resulting aggregated value.
	 * 
	 * @param matrix	the source matrix
	 * @param col		the column index in the matrix 
	 * @param operator	the operator
	 * @return	the aggregated result value
	 */
	N applyToColumn(A[] matrix, int col, AggregatingUnaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to each column {@code matrix[*][col]} and 
	 * returns a (row) vector containing the resulting aggregated values.
	 * 
	 * @param matrix	the source matrix
	 * @param operator	the operator
	 * @return	a (row) vector containing the aggregated result values
	 */
	A applyToEachColumn(A[] matrix, AggregatingUnaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to each column {@code matrix[*][col]} and 
	 * writes the resulting aggregated values to the (row) vector {@code dst}.  
	 * 
	 * @param matrix	the source matrix
	 * @param dst		the destination (row) vector for the resulting 
	 * 					aggregated values 
	 * @param operator	the operator
	 */
	void applyToEachColumn(A[] matrix, A dst, AggregatingUnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to each element pair {@code (v[i], u[i])} of 
	 * the vectors {@code u} and {@code v} and returns the resulting aggregated 
	 * value. The source vectors must have equal length, otherwise, an exception 
	 * is thrown.
	 * 
	 * @param v			the first source vector
	 * @param u			the second source vector
	 * @param operator	the operator
	 * @return	the aggregated result value
	 * @throws IllegalArgumentException if {@code u} and {@code v}
	 * 									have not equal length
	 */
	N applyTo(A v, A u, AggregatingBinaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to vector element {@code v[index]} and returns 
	 * the resulting int value.
	 * 
	 * @param v			the source vector
	 * @param index		the index in {@code v} identifying the element to which
	 * 					the given operator is applied
	 * @param operator	the operator
	 * @return	the int result value
	 */
	int evalInt(A v, int index, IntUnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to element {@code matrix[row][col]} and 
	 * returns the resulting int value.
	 * 
	 * @param matrix	the source matrix
	 * @param row		the row index in {@code matrix}
	 * @param col		the column index in {@code matrix}
	 * @param operator	the operator
	 * @return	the int result value
	 */
	int evalInt(A[] matrix, int row, int col, IntUnaryOperator<N, A> operator);

	/**
	 * Applies the given operator to vector element {@code v[index]} and returns 
	 * the resulting boolean value.
	 * 
	 * @param v			the source vector
	 * @param index		the index in {@code v} identifying the element to which
	 * 					the given operator is applied
	 * @param operator	the operator
	 * @return	the boolean result value
	 */
	boolean evalBoolean(A v, int index, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Applies the given operator to element {@code matrix[row][col]} and 
	 * returns the resulting boolean value.
	 * 
	 * @param matrix	the source matrix
	 * @param row		the row index in {@code matrix}
	 * @param col		the column index in {@code matrix}
	 * @param operator	the operator
	 * @return	the boolean result value
	 */
	boolean evalBoolean(A[] matrix, int row, int col, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any element of vector {@code v}, and {@code false} otherwise. An
	 * empty element range evaluates to {@code false}.
	 * 
	 * @param v			the source vector
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code v[*]}
	 */
	boolean anyBoolean(A v, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any of the specified elements of vector {@code v}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code false}.
	 * 
	 * @param v			the source vector
	 * @param indices	the indices in {@code v} identifying elements to which 
	 * 					the operator is applied
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code v[indices[*]]}
	 */
	boolean anyBoolean(A v, int[] indices, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any element {@code v[from : to-1]} of vector {@code v}, and 
	 * {@code false} otherwise. Start index {@code from} is inclusive, end index 
	 * {@code to} exclusive. An empty element range evaluates to {@code false}.
	 * 
	 * @param v			the source vector
	 * @param from		the start index in {@code v}, inclusive
	 * @param to		the end index in {@code v}, exclusive
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code v[from : to-1]}
	 */
	boolean anyBooleanInRange(A v, int from, int to, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any element of {@code matrix}, and {@code false} otherwise. An empty 
	 * element range evaluates to {@code false}.
	 * 
	 * @param matrix	the source matrix
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code matrix[*][*]}
	 */
	boolean anyBoolean(A[] matrix, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any of the specified elements of {@code matrix}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code false}.
	 * 
	 * @param matrix		the source matrix
	 * @param rowIndices	the row indices in {@code matrix}
	 * @param colIndices	the column indices in {@code matrix}
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code matrix[rowIndices[*]][colIndices[*]]}
	 */
	boolean anyBoolean(A[] matrix, int[] rowIndices, int[] colIndices, BooleanUnaryOperator<N, A> operator);

	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any row element {@code matrix[row][*]}, and {@code false} otherwise.
	 * An empty element range evaluates to {@code false}.
	 * 
	 * @param matrix	the source matrix
	 * @param row		the row in {@code matrix}
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code matrix[row][*]}
	 */
	boolean anyBooleanInRow(A[] matrix, int row, BooleanUnaryOperator<N, A> operator);

	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any column element {@code matrix[*][col]}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code false}.
	 * 
	 * @param matrix	the source matrix
	 * @param col		the column in {@code matrix}
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code matrix[*][col]}
	 */
	boolean anyBooleanInColumn(A[] matrix, int col, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any element in the row/column range 
	 * {@code matrix[rowFrom : rowTo-1][colFrom : colTo-1]}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code false}.
	 * 
	 * @param matrix	the source matrix
	 * @param rowFrom	the start row in {@code matrix}, inclusive
	 * @param rowTo		the end row in {@code matrix}, exclusive
	 * @param colFrom	the start column in {@code matrix}, inclusive
	 * @param colTo		the end column in {@code matrix}, exclusive
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element {@code matrix[rowFrom : rowTo-1][colFrom : colTo-1]}
	 */
	boolean anyBooleanInRange(A[] matrix, int rowFrom, int rowTo, int colFrom, int colTo, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for any element in the diagonal {@code matrix[i][i]} for 
	 * {@code 0 <= i < min(#rows, #columns)}, and {@code false} otherwise. An 
	 * empty element range evaluates to {@code false}.
	 * 
	 * @param matrix	the source matrix
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for any
	 * 			element in the diagonal {@code matrix[i][i]}
	 */
	boolean anyBooleanInDiagonal(A[] matrix, BooleanUnaryOperator<N, A> operator);

	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all elements of vector {@code v}, and {@code false} otherwise. An 
	 * empty element range evaluates to {@code true}.
	 * 
	 * @param v			the source vector
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code v[*]}
	 */
	boolean allBoolean(A v, BooleanUnaryOperator<N, A> operator);

	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all of the specified elements of vector {@code v}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code true}.
	 * 
	 * @param v			the source vector
	 * @param indices	the indices in {@code v} identifying elements to which 
	 * 					the operator is applied
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code v[indices[*]]}
	 */
	boolean allBoolean(A v, int[] indices, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all elements {@code v[from : to-1]} of vector {@code v}, and 
	 * {@code false} otherwise. Start index {@code from} is inclusive, end index 
	 * {@code to} exclusive. An empty element range evaluates to {@code true}.
	 * 
	 * @param v			the source vector
	 * @param from		the start index in {@code v}, inclusive
	 * @param to		the end index in {@code v}, exclusive
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code v[from : to-1]}
	 */	
	boolean allBooleanInRange(A v, int from, int to, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all elements of {@code matrix}, and {@code false} otherwise. An empty 
	 * element range evaluates to {@code true}.
	 * 
	 * @param matrix	the source matrix
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code matrix[*][*]}
	 */	
	boolean allBoolean(A[] matrix, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all of the specified elements of {@code matrix}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code true}.
	 * 
	 * @param matrix		the source matrix
	 * @param rowIndices	the row indices in {@code matrix}
	 * @param colIndices	the column indices in {@code matrix}
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code matrix[rowIndices[*]][colIndices[*]]}
	 */	
	boolean allBoolean(A[] matrix, int[] rowIndices, int[] colIndices, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all row elements {@code matrix[row][*]}, and {@code false} otherwise.
	 * An empty element range evaluates to {@code true}.
	 * 
	 * @param matrix	the source matrix
	 * @param row		the row in {@code matrix}
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code matrix[row][*]}
	 */	
	boolean allBooleanInRow(A[] matrix, int row, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all column elements {@code matrix[*][col]}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code true}.
	 * 
	 * @param matrix	the source matrix
	 * @param col		the column in {@code matrix}
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code matrix[*][col]}
	 */	
	boolean allBooleanInColumn(A[] matrix, int col, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all elements in the row/column range 
	 * {@code matrix[rowFrom : rowTo-1][colFrom : colTo-1]}, and {@code false} 
	 * otherwise. An empty element range evaluates to {@code true}.
	 * 
	 * @param matrix	the source matrix
	 * @param rowFrom	the start row in {@code matrix}, inclusive
	 * @param rowTo		the end row in {@code matrix}, exclusive
	 * @param colFrom	the start column in {@code matrix}, inclusive
	 * @param colTo		the end column in {@code matrix}, exclusive
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements {@code matrix[rowFrom : rowTo-1][colFrom : colTo-1]}
	 */	
	boolean allBooleanInRange(A[] matrix, int rowFrom, int rowTo, int colFrom, int colTo, BooleanUnaryOperator<N, A> operator);
	
	/**
	 * Returns {@code true} if the the given operator evaluates to {@code true} 
	 * for all elements in the diagonal {@code matrix[i][i]} for 
	 * {@code 0 <= i < min(#rows, #columns)}, and {@code false} otherwise. An 
	 * empty element range evaluates to {@code true}.
	 * 
	 * @param matrix	the source matrix
	 * @param operator	the operator
	 * @return	{@code true} if the operator evaluates to {@code true} for all
	 * 			elements in the diagonal {@code matrix[i][i]}
	 */
	boolean allBooleanInDiagonal(A[] matrix, BooleanUnaryOperator<N, A> operator);
}
