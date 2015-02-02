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
package ch.javasoft.math.linalg;

import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.array.Converter;
import ch.javasoft.math.array.ExpressionComposer;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;

/**
 * <code>LinAlgOperations</code> is a collection of arithmetic operations on 
 * vectors and matrices as known from linear algebra. This interface only 
 * defines basic functions such as addition, matrix multiplication or inner and
 * outer product for vectors. For more complex functions, such as matrix 
 * inversion or nullspace and rank computations, the extension 
 * {@link LinAlgOperations} should be used instead.   
 * <p>
 * An instance of this class defines the (boxed) number type and the array type, 
 * such as {@link Double} and {@code double[]}. 
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public interface BasicLinAlgOperations<N extends Number, A> {

	/**
	 * Returns the number type class for a single value
	 */
	Class<N> numberClass();

	/**
	 * Returns the number type class of an array of values
	 */
	Class<A> arrayClass();
	
	/**
	 * Returns the number array operations, used to apply numeric operators to 
	 * vectors or matrices
	 */
	NumberArrayOperations<N, A> getNumberArrayOperations();
	
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
	 * Returns a single element of the given vector
	 * 
	 * @param vector	the vector from which to return an element
	 * @param index		the index in the matrix, zero based
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
	 * Returns the sign of a single element of the given vector
	 * 
	 * @param vector	the vector from which to return the sign of an element
	 * @param index		the index in the matrix, zero based
	 * @return -1, 0, 1 for a positive, zero, negative value
	 */
	int signum(A vector, int index);

	/**
	 * Returns the sign of a single element of the given matrix
	 * 
	 * @param matrix	the matrix from which to return the sign of an element
	 * @param row		the row index in the matrix, zero based
	 * @param col		the column index in the matrix, zero based
	 * @return -1, 0, 1 for a positive, zero, negative value
	 */
	int signum(A[] matrix, int row, int col);

	/**
	 * Returns a new vector where all elements are negated compared to the 
	 * original vector
	 * 
	 * @param vector	the vector to negate
	 * @return {@code -vector}
	 */
	A negate(A vector);

	/**
	 * Returns a new matrix where all elements are negated compared to the 
	 * original matrix
	 * 
	 * @param matrix	the matrix to negate
	 * @return {@code -matrix}
	 */
	A[] negate(A[] matrix);

	/**
	 * Returns a new vector where all elements are non-negative, negative values
	 * from the original vector have changed sign
	 * 
	 * @param vector	the vector
	 * @return {@code abs(vector)}
	 */
	A abs(A vector);

	/**
	 * Returns a new matrix where all elements are non-negative, negative values
	 * from the original matrix have changed sign
	 * 
	 * @param matrix	the matrix
	 * @return {@code abs(matrix)}
	 */
	A[] abs(A[] matrix);

	/**
	 * Returns a new vector where each element is the sum of the corresponding
	 * source vector elements. If the vectors have not the same length, an
	 * exception is thrown.
	 * 
	 * @param v	the first addend vector
	 * @param u	the second addend vector
	 * 
	 * @return {@code v + u}
	 * @throws IllegalArgumentException if the vectors have not the same length
	 */
	A add(A v, A u);

	/**
	 * Returns a new matrix where each element is the sum of the corresponding
	 * source matrix elements. If the matrices have not equal dimensions, an
	 * exception is thrown.
	 * 
	 * @param m1	the first addend matrix
	 * @param m2	the second addend matrix
	 * 
	 * @return {@code m1 + m2}
	 * @throws IllegalArgumentException if the matrices have unequal dimensions
	 */
	A[] add(A[] m1, A[] m2);
	
	/**
	 * Returns a new vector where each element is the difference of the 
	 * corresponding source vector elements. If the vectors have not the same 
	 * length, an exception is thrown.
	 * 
	 * @param v	the vector to subtract from
	 * @param u	the vector to subtract
	 * 
	 * @return {@code v - u}
	 * @throws IllegalArgumentException if the vectors have not the same length
	 */
	A subtract(A v, A u);

	/**
	 * Returns a new matrix where each element is the difference of the 
	 * corresponding source matrix elements. If the matrices have not equal 
	 * dimensions, an exception is thrown.
	 * 
	 * @param m1	the matrix to subtract from
	 * @param m2	the matrix to subtract
	 * 
	 * @return {@code m1 - m2}
	 * @throws IllegalArgumentException if the matrices have unequal dimensions
	 */
	A[] subtract(A[] m1, A[] m2);

	/**
	 * Returns a scalar representing the inner product of the two vectors. This
	 * product is also known as dot or scalar product. If the vector have not 
	 * equal lengths, an exception is thrown.
     *
	 * @param v	the first vector
	 * @param u	the second vector
	 * 
	 * @return 	<code>&lt;v,u&gt; = u<sup>T</sup>v = 
	 * 			&sum;(v[0]*u[0] + v[1]*u[1] + ... + v[n-1]*u[n-1])</code>
	 * @throws IllegalArgumentException if the vectors have not the same length
	 */
	N multiplyInner(A v, A u);

	/**
	 * Returns a new vector where each element is the product of the 
	 * corresponding source vector elements. If the vector have not equal  
	 * lengths, an exception is thrown.
     *
	 * @param v	the first vector
	 * @param u	the second vector
	 * 
	 * @return {@code v .* u}
	 * @throws IllegalArgumentException if the vectors have not the same length
	 */
	A multiplyElementByElement(A v, A u);

	/**
	 * Returns a matrix representing the outer product of the two vectors. This
	 * product sometimes called tensor product of the vectors. If the vector 
	 * have not equal lengths, an exception is thrown.
     *
	 * @param v	the first vector
	 * @param u	the second vector
	 * 
	 * @return 	<pre>v &otimes; u = u v<sup>T</sup> = 
	 * [u1 v1 , u1 v2 , u1 v3 , ...]
	 * [u2 v1 , u2 v2 , u2 v3 , ...]
	 * [u3 v1 , u3 v2 , u3 v3 , ...]
	 * [             ...           ]
	 * </pre> 
	 * @throws IllegalArgumentException if the vectors have not the same length
	 */
	A[] multiplyOuter(A v, A u);

	/**
	 * Returns a new matrix where each element is the product of the 
	 * corresponding source matrix elements. If the matrices have not equal 
	 * dimensions, an exception is thrown.
     *
	 * @param m1	the first matrix
	 * @param m2	the second matrix
	 * 
	 * @return {@code m1 .* m2}
	 * @throws IllegalArgumentException if the matrices have unequal dimensions
	 */
	A[] multiplyElementByElement(A[] m1, A[] m2);

	/**
	 * Returns the product of the two matrices using matrix multiplication. If 
	 * the number of columns of the first matrix is not equal to the number of 
	 * rows of the second matrix, an exception is thrown.
     *
	 * @param m1	the first matrix
	 * @param m2	the second matrix
	 * 
	 * @return {@code m1 * m2}
	 * @throws IllegalArgumentException if the dimensions of the matrices are 
	 * 									not compatible for matrix multiplication
	 */
	A[] multiply(A[] m1, A[] m2);
	
	/**
	 * Returns the product of the matrix with the column vector using matrix 
	 * multiplication. Input and output vector are interpreted as column 
	 * vectors. If the number of columns of the matrix is not equal to the 
	 * length of the vector, an exception is thrown.
     *
	 * @param m	the matrix
	 * @param v	the vector
	 * 
	 * @return {@code m * v}
	 * @throws IllegalArgumentException if the number of columns in {@code m} is 
	 * 									not equal to the length of {@code v}
	 */
	A multiply(A[] m, A v);

	/**
	 * Returns the product of the row vector with the matrix using matrix 
	 * multiplication. Input and output vector are interpreted as row 
	 * vectors. If the number of rows of the matrix is not equal to the 
	 * length of the vector, an exception is thrown.
     *
	 * @param v	the vector
	 * @param m	the matrix
	 * 
	 * @return {@code v * m}
	 * @throws IllegalArgumentException if the number of rows in {@code m} is 
	 * 									not equal to the length of {@code v}
	 */
	A multiply(A v, A[] m);

	/**
	 * Returns a new vector where each element is the quotient of the 
	 * corresponding source vector elements. If the vector have not equal  
	 * lengths, an exception is thrown.
     *
	 * @param v	the dividend vector
	 * @param u	the divisor vector
	 * 
	 * @return {@code v ./ u}
	 * @throws IllegalArgumentException if the matrices have unequal dimensions
	 */
	A divideElementByElement(A v, A u);

	/**
	 * Returns a new matrix where each element is the quotient of the 
	 * corresponding source matrix elements. If the matrices have not equal 
	 * dimensions, an exception is thrown.
     *
	 * @param m1	the dividend matrix
	 * @param m2	the divisor matrix
	 * 
	 * @return {@code m1 ./ m2}
	 * @throws IllegalArgumentException if the matrices have unequal dimensions
	 */
	A[] divideElementByElement(A[] m1, A[] m2);
}
