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

import ch.javasoft.math.array.NumberOperators;

/**
 * <code>LinAlgOperations</code> is a collection of arithmetic operations on 
 * vectors and matrices as known from linear algebra. This class extends
 * {@link BasicLinAlgOperations}, adding more complex functions like matrix
 * inversion, nullspace and rank computations and so on.
 * <p>
 * An instance of this class defines the (boxed) number type and the array type, 
 * such as {@link Double} and {@code double[]}. 
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public interface LinAlgOperations<N extends Number, A> extends BasicLinAlgOperations<N, A> {
	
	/**
	 * Computes and returns the rank of the given matrix
	 * 
	 * @param matrix	the matrix
	 * @return the rank of the matrix
	 */
	int rank(A[] matrix);
	
	/**
	 * Computes and returns the nullity of the given matrix, associated with
	 * rank deficiency. The nullity is the rank of the nullspace of the matrix,
	 * thus, nullity is the number of matrix columns minus rank.
	 * 
	 * @param matrix	the matrix
	 * @return the nullity of the matrix, the rank of the nullspace
	 */
	int nullity(A[] matrix);
	
	/**
	 * Transforms the {@code matrix} into row echelon form using Gaussian 
	 * elimination and returns the transformed matrix. The row echelon form
	 * has the form {@code [ I , M ; 0]} or {@code [ [I ; M] , 0]}, depending
	 * on dimensions and rank. 
	 * <p>
	 * If integer division is not supported
	 * (see {@link NumberOperators#getDivisionSupport()}), the diagonal of the
	 * identity part might contain positive values different from one.  
	 * <p>
	 * Note that rows and columns might be swapped due to pivoting. The row and 
	 * column permutations are returned in {@code colmap} and {@code colmap} if 
	 * not null. 
	 * <p>
	 * The rank	of -- or number of ones in -- the identity part of the returned 
	 * matrix is returned in {@code ptrRank[0]} if not null. Note that the rank 
	 * is not necessarily equal to the rank of the whole matrix, since the 
	 * lengths of {@code rowmap} and {@code colmap} might limit the allowed 
	 * pivot rows and columns.
	 *  
	 * @param matrix	the matrix to reduce
	 * @param reduced	if true, the reduced row echelon form is produced, 
	 * 					also resulting in zeros above the diagonal
	 * @param rowmap 	the row mapping (out parameter), if not null, the row
	 * 					indices are filled in which have been used to produce 
	 * 					the row echelon matrix form. The mapping is only used as 
	 * 					out parameter, but its length determines how many rows 
	 * 					to use as pivot rows. If null, all rows are pivot 
	 * 					candidates
	 * @param colmap	the column mapping (out parameter), if not null, the 
	 * 					column indices are filled in which have been used to 
	 * 					to produce the row echelon matrix form. The mapping is 
	 * 					only used as out parameter, but its length determines 
	 * 					how many columns to use as pivot columns. If null, all
	 * 					columns are pivot candidates
	 * @param ptrRank	if not null, {@code ptrRank[0]} will contain the rank
	 * 					of -- or number of ones in -- the identity part of the 
	 * 					returned matrix. Note that this is not necessarily equal
	 * 					to the rank of the whole matrix, since the lengths of 
	 * 					{@code rowmap} and {@code colmap} might limit the 
	 * 					allowed pivot rows and columns.
	 * @return the matrix in row echelon form
	 */
	A[] rowEchelon(A[] matrix, boolean reduced, int[] rowmap, int[] colmap, int[] ptrRank);
	
	/**
	 * Transforms the {@code src} matrix into row echelon form using Gaussian 
	 * elimination, writing the transformed matrix to {@code dst} and returning
	 * the rank of the matrix. The row echelon form
	 * has the form {@code [ I , M ; 0]} or {@code [ [I ; M] , 0]}, depending
	 * on dimensions and rank. 
	 * <p>
	 * If integer division is not supported
	 * (see {@link NumberOperators#getDivisionSupport()}), the diagonal of the
	 * identity part might contain positive values different from one.  
	 * <p>
	 * Note that rows and columns might be swapped due to pivoting. The row and 
	 * column permutations are returned in {@code rowmap} and {@code colmap} if 
	 * not null.
	 * <p>
	 * Note that the returned rank is not necessarily equal to the rank of the 
	 * whole matrix, since the lengths of {@code rowmap} and {@code colmap} 
	 * might limit the allowed pivot rows and columns. 
	 *  
	 * @param src		the matrix to reduce
	 * @param dst		the destination matrix for the results
	 * @param reduced	if true, the reduced row echelon form is produced, 
	 * 					also resulting in zeros above the diagonal
	 * @param rowmap 	the row mapping (out parameter), if not null, the row
	 * 					indices are filled in which have been used to produce 
	 * 					the row echelon matrix form. The mapping is only used as 
	 * 					out parameter, but its length determines how many rows 
	 * 					to use as pivot rows. If null, all rows are pivot 
	 * 					candidates
	 * @param colmap	the column mapping (out parameter), if not null, the 
	 * 					column indices are filled in which have been used to 
	 * 					to produce the row echelon matrix form. The mapping is 
	 * 					only used as out parameter, but its length determines 
	 * 					how many columns to use as pivot columns. If null, all
	 * 					columns are pivot candidates
	 * @return			the rank of the matrix
	 */
	int rowEchelon(A[] src, A[] dst, boolean reduced, int[] rowmap, int[] colmap);
	
	/**
	 * Computes a basis for the nullspace using by computing the reduced 
	 * row-echelon form of the input matrix. The reduced row echelon matrix has 
	 * structure [ I, M ; 0 ], thus the kernel matrix is simply [ -M ; I ].
	 * <p>
	 * Note that rows and columns might be swapped due to pivoting. The row and 
	 * column permutations are returned in {@code rowmap} and {@code colmap} if 
	 * not null.
	 * <p>
	 * The rank	of -- or number of columns in -- the returned matrix is returned 
	 * in {@code ptrNullity[0]} if not null. Note that the nullity is not 
	 * necessarily equal to the nullity of the whole matrix, since the lengths 
	 * of {@code rowmap} and {@code colmap} might limit the allowed pivot rows 
	 * and columns.
	 * 
	 * @param matrix	the input matrix
	 * @param rowmap 	the row mapping (out parameter), if not null, the row
	 * 					indices are filled in which have been used to produce 
	 * 					the row echelon matrix form. The mapping is only used as 
	 * 					out parameter, but its length determines how many rows 
	 * 					to use as pivot rows. If null, all rows are pivot 
	 * 					candidates
	 * @param colmap	the column mapping (out parameter), if not null, the 
	 * 					column indices are filled in which have been used to 
	 * 					to produce the row echelon matrix form. The mapping is 
	 * 					only used as out parameter, but its length determines 
	 * 					how many columns to use as pivot columns. If null, all
	 * 					columns are pivot candidates
	 * @param ptrNullity if not null, {@code ptrNullity[0]} will contain the 
	 * 					nullity, that is, the rank of the returned kernel 
	 * 					matrix. Note that this is not necessarily equal to the 
	 * 					nullity of the whole matrix, since the lengths of 
	 * 					{@code rowmap} and {@code colmap} might limit the 
	 * 					allowed pivot rows and columns.
	 * @return the kernel, a basis for the nullspace
	 */
	A[] kernel(A[] matrix, int[] rowmap, int[] colmap, int[] ptrNullity);

	/**
	 * Computes and returns the inverse of the given square {@code matrix}. The
	 * product of the returned matrix {@code inv(matrix)} with the matrix 
	 * results in an identity matrix, that is,
	 * {@code inv(matrix) * matrix = matrix * inv(matrix) = I}.
	 * <p>
	 * Note that rows and columns might be swapped due to pivoting. The row and 
	 * column permutations are returned in {@code rowmap} and {@code colmap} if 
	 * not null.
	 * <p>
	 * If integer division is not supported
	 * (see {@link NumberOperators#getDivisionSupport()}), the product of the
	 * inverse matrix and the matrix does not yield an identity matrix, but a
	 * matrix that contains only positive diagonal elements. More formally, if 
	 * division is not supported, the inverse {@code inv(M)} of a matrix 
	 * {@code M} is defined such that
	 * <pre>inv(M) * M = diag , diag(i,i) > 0, diag(i,j) = 0 if i &ne; j</pre>
	 * where {@code diag} is a diagonal matrix.
	 * <p>
	 * The method used is computing the reduced row-echelon form of the matrix
	 * [mx I], ending up in a matrix [I inv(mx)].
	 * 
	 * @param matrix	the matrix from which submatrix of maximal rank is taken
	 * 					and inverted
	 * @param rowmap 	the row mapping (out parameter), if not null, the row
	 * 					indices are filled in which have been used to produce 
	 * 					the row echelon matrix form. The mapping is only used as 
	 * 					out parameter, but its length determines how many rows 
	 * 					to use as pivot rows. If null, all rows are pivot 
	 * 					candidates
	 * @param colmap	the column mapping (out parameter), if not null, the 
	 * 					column indices are filled in which have been used to 
	 * 					to produce the row echelon matrix form. The mapping is 
	 * 					only used as out parameter, but its length determines 
	 * 					how many columns to use as pivot columns. If null, all
	 * 					columns are pivot candidates
	 * 
	 * @return the inverted matrix
	 * @throws IllegalArgumentException if the input matrix is not square
	 * @throws ArithmeticException if the input matrix is singular
	 */
	A[] invertMatrix(A[] matrix, int[] rowmap, int[] colmap);

	/**
	 * Computes the inverse of a submatrix of a non-square or singular square 
	 * matrix. The submatrix row/column size is determined by the rank of the
	 * matrix. For instance, for a rectangular matrix A with m rows and n 
	 * columns and n = rank(A) and m > n, a submatrix with n rows is chosen,
	 * such that the submatrix has full rank. The chosen columns and rows are
	 * returned in rowmap/colmap.
	 * <p>
	 * Note that if integer division is not supported
	 * (see {@link NumberOperators#getDivisionSupport()}), the product of the
	 * inverse matrix and the matrix does not yield an identity matrix, but a
	 * matrix that contains only positive diagonal elements. More formally, if 
	 * division is not supported, the inverse {@code inv(M)} of a matrix 
	 * {@code M} is defined such that
	 * <pre>inv(M) * M = diag , diag(i,i) > 0, diag(i,j) = 0 if i &ne; j</pre>
	 * where {@code diag} is a diagonal matrix.
	 * <p>
	 * The method used is computing the reduced row-echelon form of the matrix
	 * [mx I], ending up in a matrix [I inv(mx)].
	 * <p>
	 * Note that the returned rank is not necessarily equal to the rank of the 
	 * whole matrix, since the lengths of {@code rowmap} and {@code colmap} 
	 * might limit the allowed pivot rows and columns. 
	 * 
	 * @param matrix	the matrix from which submatrix of maximal rank is taken
	 * 					and inverted
	 * @param rowmap 	the row mapping (out parameter), if not null, the row
	 * 					indices are filled in which have been used to produce 
	 * 					the row echelon matrix form. The mapping is only used as 
	 * 					out parameter, but its length determines how many rows 
	 * 					to use as pivot rows. If null, all rows are pivot 
	 * 					candidates
	 * @param colmap	the column mapping (out parameter), if not null, the 
	 * 					column indices are filled in which have been used to 
	 * 					to produce the row echelon matrix form. The mapping is 
	 * 					only used as out parameter, but its length determines 
	 * 					how many columns to use as pivot columns. If null, all
	 * 					columns are pivot candidates
	 * @param ptrRank	if not null, {@code ptrRank[0]} will contain the rank
	 * 					of the returned matrix. Note that this is not 
	 * 					necessarily equal to the rank of the whole matrix, since 
	 * 					the lengths of {@code rowmap} and {@code colmap} might 
	 * 					limit the allowed pivot rows and columns.
	 * 
	 * @return the inverted (sub)matrix, always square
	 */
	A[] invertMaximalSubmatrix(A[] matrix, int[] rowmap, int[] colmap, int[] ptrRank);
}
