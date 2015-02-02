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
package ch.javasoft.smx.ops;

import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.smx.exception.SingularMatrixException;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.numeric.Zero;

/**
 * Matrix operations based on gauss, i.e. adding multiples of one row to another
 * row to cancel values. This can be used to create the (reduced) row echelon 
 * form of a matrix, from which rank and nullspace can be derived. One could 
 * also compute matrix inverses using gauss.
 * <p>
 * This class currently supports double and rational matrices. Default instances
 * for these cases are available at {@link #getDoubleInstance()} and 
 * {@link #getRationalInstance()}.
 */
public class Gauss {
	
	public final double mTolerance;
	
	/**
	 * Computes the rank of the given matrix
	 * @param mx	the matrix for which the rank should be computed
	 */
	public int rank(ReadableDoubleMatrix mx) {
		return rowEchelon(mx.toDoubleMatrix(true), false/*reduced*/, null, null);
	}
	/**
	 * Computes the rank of the given matrix
	 * @param mx	the matrix for which the rank should be computed
	 */
	public int rank(ReadableBigIntegerRationalMatrix mx) {
		return rowEchelon(mx.toBigIntegerRationalMatrix(true), false/*reduced*/, null, null);
	}
	/**
	 * Computes the dimension of the nullspace, that is, columns - rank
	 */
	public int nullity(ReadableDoubleMatrix mx) {
		return mx.getColumnCount() - rank(mx);
	}
	/**
	 * Computes the dimension of the nullspace, that is, columns - rank
	 */
	public int nullity(ReadableBigIntegerRationalMatrix mx) {
		return mx.getColumnCount() - rank(mx);
	}
	/**
	 * Computes the inverse of a square matrix. If the matrix is not square, or
	 * if it is singular, an exception is thrown.
	 * <p>
	 * The method used is computing the reduced row-echelon form of the matrix
	 * [mx I], ending up in a matrix [I inv(mx)].
	 * 
	 * @throws IllegalArgumentException if the matrix is not a square matrix
	 * @throws SingularMatrixException 	if the matrix is singular
	 */
	public DoubleMatrix invert(ReadableDoubleMatrix mx) {
		if (mx.getRowCount() != mx.getColumnCount()) {
			throw new IllegalArgumentException("not a square matrix: " + mx.getRowCount() + "x" + mx.getColumnCount());			
		}
		return invertMaximalSubmatrix(mx, null, null, true);
	}
	/**
	 * Computes the inverse of a submatrix of a non-square or singular square 
	 * matrix. The submatrix row/column size is determined by the rank of the
	 * matrix. For instance, for a rectangular matrix A with m rows and n 
	 * columns and n = rank(A) and m > n, a submatrix with n rows is chosen,
	 * such that the submatrix has full rank. The chosen columns and rows are
	 * returned in rowmap/colmap.
	 * <p>
	 * The method used is computing the reduced row-echelon form of the matrix
	 * [mx I], ending up in a matrix [I inv(mx)].
	 * 
	 * @param mx		the matrix from which submatrix of maximal rank is taken
	 * 					and inverted
	 * @param ptrRowmap	a pointer to the row mapping (out parameter), if not 
	 * 					null, ptrRowmap[0] will contain the row indices taken 
	 * 					for the inverted submatrix.
	 * @param ptrColmap	a pointer to the column mapping (out parameter), if not 
	 * 					null, ptrColmap[0] will contain the column indices taken 
	 * 					for the inverted submatrix.
	 * 
	 * @return the inverted (sub)matrix, always square
	 */
	public DoubleMatrix invertMaximalSubmatrix(ReadableDoubleMatrix mx, int[]/*out*/[] ptrRowmap, int[]/*out*/[] ptrColmap) {
		return invertMaximalSubmatrix(mx, ptrRowmap, ptrColmap, false);
	}
	private DoubleMatrix invertMaximalSubmatrix(ReadableDoubleMatrix mx, int[]/*out*/[] ptrRowmap, int[]/*out*/[] ptrColmap, boolean square) {
		final int rows = mx.getRowCount();
		final int cols = mx.getColumnCount();
		
		//create matrix [mx I]
		final DoubleMatrix rref = new DefaultDoubleMatrix(rows, rows + cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				rref.setValueAt(row, col, mx.getDoubleValueAt(row, col));				
			}
			rref.setValueAt(row, cols + row, 1);
		}
		//reduced row echelon
		final int[] rowmap = createIdentityMap(rows);
		final int[] colmap = createIdentityMap(cols);
		int rank = rowEchelon(rref, true /*reduced*/, rowmap, colmap);
		if (square && rank < Math.min(rows, cols)) {
			throw new SingularMatrixException("singular matrix, rank < size: " + rank + " < " + rows, rowmap[rank]);
		}
		
		//rref has form [I inv(mx)], but rows are swapped as reflected in colmap
		final DoubleMatrix inv = new DefaultDoubleMatrix(rank, rank);
		for (int row = 0; row < rank; row++) {
			final int dstRow = square ? colmap[row] : row;
			for (int col = 0; col < rank; col++) {
				final int dstCol = square ? rowmap[col] : col;
				inv.setValueAt(dstRow, dstCol, rref.getDoubleValueAt(row, cols + rowmap[col]));
			}
		}
		if (ptrRowmap != null) {
			ptrRowmap[0] = Arrays.copyOfRange(rowmap, 0, rank);
		}
		if (ptrColmap != null) {
			ptrColmap[0] = Arrays.copyOfRange(colmap, 0, rank);
		}
		return inv;
	}
	/**
	 * Computes the inverse of a square matrix. If the matrix is not square, or
	 * if it is singular, an exception is thrown.
	 * <p>
	 * The method used is computing the reduced row-echelon form of the matrix
	 * [mx I], ending up in a matrix [I inv(mx)].
	 * 
	 * @throws IllegalArgumentException if the matrix is not a square matrix
	 * @throws SingularMatrixException 	if the matrix is singular
	 */
	public BigIntegerRationalMatrix invert(ReadableBigIntegerRationalMatrix mx) {
		if (mx.getRowCount() != mx.getColumnCount()) {
			throw new IllegalArgumentException("not a square matrix: " + mx.getRowCount() + "x" + mx.getColumnCount());			
		}
		return invertMaximalSubmatrix(mx, null, null, true);
	}
	/**
	 * Computes the inverse of a submatrix of a non-square or singular square 
	 * matrix. The submatrix row/column size is determined by the rank of the
	 * matrix. For instance, for a rectangular matrix A with m rows and n 
	 * columns and n = rank(A) and m > n, a submatrix with n rows is chosen,
	 * such that the submatrix has full rank. The chosen columns and rows are
	 * returned in rowmap/colmap.
	 * <p>
	 * The method used is computing the reduced row-echelon form of the matrix
	 * [mx I], ending up in a matrix [I inv(mx)].
	 * 
	 * @param mx		the matrix from which submatrix of maximal rank is taken
	 * 					and inverted
	 * @param ptrRowmap	a pointer to the row mapping (out parameter), if not 
	 * 					null, ptrRowmap[0] will contain the row indices taken 
	 * 					for the inverted submatrix.
	 * @param ptrColmap	a pointer to the column mapping (out parameter), if not 
	 * 					null, ptrColmap[0] will contain the column indices taken 
	 * 					for the inverted submatrix.
	 * 
	 * @return the inverted (sub)matrix, always square
	 */
	public BigIntegerRationalMatrix invertMaximalSubmatrix(ReadableBigIntegerRationalMatrix mx, int[]/*out*/[] ptrRowmap, int[]/*out*/[] ptrColmap) {
		return invertMaximalSubmatrix(mx, ptrRowmap, ptrColmap, false);
	}
	private BigIntegerRationalMatrix invertMaximalSubmatrix(ReadableBigIntegerRationalMatrix mx, int[]/*out*/[] ptrRowmap, int[]/*out*/[] ptrColmap, boolean square) {
		final int rows = mx.getRowCount();
		final int cols = mx.getColumnCount();
		
		//create matrix [mx I]
		final BigIntegerRationalMatrix rref = new DefaultBigIntegerRationalMatrix(rows, rows + cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				rref.setValueAt(row, col, mx.getBigIntegerNumeratorAt(row, col), mx.getBigIntegerDenominatorAt(row, col));				
			}
			rref.setValueAt(row, cols + row, 1);
		}
		//reduced row echelon
		final int[] rowmap = createIdentityMap(rows);
		final int[] colmap = createIdentityMap(cols);
		int rank = rowEchelon(rref, true /*reduced*/, rowmap, colmap);
		if (square && rank < Math.min(rows, cols)) {
			throw new SingularMatrixException("singular matrix, rank < size: " + rank + " < " + rows, rowmap[rank]);
		}
		
		//rref has form [I inv(mx)], but rows are swapped as reflected in colmap
		final BigIntegerRationalMatrix inv = new DefaultBigIntegerRationalMatrix(rank, rank);
		for (int row = 0; row < rank; row++) {
			final int dstRow = square ? colmap[row] : row;
			for (int col = 0; col < rank; col++) {
				final int dstCol = square ? rowmap[col] : col;
				inv.setValueAt(dstRow, dstCol,
					rref.getBigIntegerNumeratorAt(row, cols + rowmap[col]), 
					rref.getBigIntegerDenominatorAt(row, cols + rowmap[col])
				);
			}
		}
		if (ptrRowmap != null) {
			ptrRowmap[0] = Arrays.copyOfRange(rowmap, 0, rank);
		}
		if (ptrColmap != null) {
			ptrColmap[0] = Arrays.copyOfRange(colmap, 0, rank);
		}
		inv.reduce();
		return inv;
	}

	/**
	 * Computes a basis for the nullspace using gauss with full pivoting.
	 * The reduced row echelon matrix which is computed from the input has the
	 * structure [ I, M ; 0 ], thus the nullspace is simply [ -M ; I ].
	 * 
	 * @param mx	the input matrix
	 * @return		the kernel, a basis for the nullspace
	 */
	public DoubleMatrix nullspace(ReadableDoubleMatrix mx) {
		int cols = mx.getColumnCount();
		int[] colmap	= createIdentityMap(cols);
		DoubleMatrix rref = mx.toDoubleMatrix(true /*enforceNewInstance*/);
		int rank = rowEchelon(rref, true /*reduced*/, null, colmap);
		int ndim = cols - rank;
		DoubleMatrix ker = new DefaultDoubleMatrix(cols, ndim);
		for (int row = 0; row < rank; row++) {
			for (int col = 0; col < ndim; col++) {
				ker.setValueAt(colmap[row], col, -rref.getDoubleValueAt(row, col + rank));
			}
		}
		for (int row = 0; row < ndim; row++) {
			ker.setValueAt(colmap[row + rank], row, 1d);			
		}
		return ker;
	}
	
	/**
	 * Computes a basis for the nullspace using gauss with full pivoting.
	 * The reduced row echelon matrix which is computed from the input has the
	 * structure [ I, M ; 0 ], thus the nullspace is simply [ -M ; I ].
	 * 
	 * @param mx	the input matrix
	 * @return		the kernel, a basis for the nullspace
	 */
	public BigIntegerRationalMatrix nullspace(ReadableBigIntegerRationalMatrix mx) {
		int cols = mx.getColumnCount();
		int[] colmap = createIdentityMap(cols);
		BigIntegerRationalMatrix rref = mx.toBigIntegerRationalMatrix(true /*enforceNewInstance*/);
		int rank = rowEchelon(rref, true /*reduced*/, null, colmap);
		int ndim = cols - rank;
		BigIntegerRationalMatrix ker = new DefaultBigIntegerRationalMatrix(cols, ndim);
		for (int row = 0; row < rank; row++) {
			for (int col = 0; col < ndim; col++) {
//				ker.setValueAt(colmap[row], col, -rref.getDoubleValueAt(row, col + rank));
				BigInteger num = rref.getBigIntegerNumeratorAt(row, col + rank);
				BigInteger den = rref.getBigIntegerDenominatorAt(row, col + rank);
				ker.setValueAt(colmap[row], col, num.negate(), den);//should be canceled

			}			
		}
		for (int row = 0; row < ndim; row++) {
                        // cj: b
			// ker.setValueAt(colmap[row + rank], row, BigFraction.ONE);			
			ker.setValueAt_BigFraction(colmap[row + rank], row, BigFraction.ONE);			
                        // cj: b
		}
		return ker;
	}
	
	/**
	 * Row echelon form using gauss with full pivoting. Note that columns and
	 * rows might be swapped due to the full pivoting.
	 *  
	 * @param mx	the matrix to reduce
	 * @return		the rank of the matrix
	 */
	public int rowEchelon(DoubleMatrix mx) {
		return rowEchelon(mx, false /*reduced*/, null, null);		
	}
	/**
	 * Reduced row echelon form using gauss with full pivoting. Note that 
	 * columns and rows might be swapped due to the full pivoting.
	 *  
	 * @param mx	the matrix to reduce
	 * @return		the rank of the matrix
	 */
	public int reducedRowEchelon(DoubleMatrix mx) {
		return rowEchelon(mx, true /*reduced*/, null, null);		
	}
	/**
	 * Row echelon form using gauss with full pivoting. Note that columns and
	 * rows might be swapped due to the full pivoting, returned in rowmap and 
	 * colmap if they are not null.
	 *  
	 * @param mx		the matrix to reduce
	 * @param reduced	if true, the reduced row echelon form is returned, that
	 * 					is, zeros above the diagonal, too
	 * @param rowmap 	the row mapping to reestablish the original row 
	 * 					ordering. the mapping is only used as out parameter, but
	 * 					its length determines how many rows to use as pivot rows
	 * @param colmap 	the column mapping to reestablish the original column 
	 * 					ordering. the mapping is only used as out parameter, but
	 * 					its length determines how many columns to use as pivot 
	 * 					columns
	 * @return			the rank of the matrix
	 */
	public int rowEchelon(DoubleMatrix mx, boolean reduced, int[] rowmap, int[] colmap) {
		final int rows = mx.getRowCount();
		final int cols = mx.getColumnCount();
		final int prows = rowmap == null ? rows : Math.min(rows, rowmap.length);
		final int pcols = colmap == null ? cols : Math.min(cols, colmap.length);
		final int pivs = Math.min(prows, pcols);
		
		//find pivot row/column
		int prow = -1;
		int pcol = -1;
		double pval = -Double.MAX_VALUE;
		for (int row = 0; row < prows; row++) {
			for (int col = 0; col < pcols; col++) {
				double val = Math.abs(mx.getDoubleValueAt(row, col));
				if (val > pval) {
					pval = val;
					prow = row;
					pcol = col;
				}
			}
		}
		
		//precondition (each iteration): prow/pcol/pval are set
		for (int pivot = 0; pivot < pivs; pivot++) {
			if (pval <= mTolerance) return pivot;
			
			//swap rows / columns
			if (prow != pivot) {
				mx.swapRows(prow, pivot);
				if (rowmap != null) {
					IntArray.swap(rowmap, prow, pivot);
				}
			}
			if (pcol != pivot) {
				mx.swapColumns(pcol, pivot);
				if (colmap != null) {
					IntArray.swap(colmap, pcol, pivot);
				}
			}
			
			//divide pivot row
			pval = mx.getDoubleValueAt(pivot, pivot);
			for (int col = pivot + 1; col < cols; col++) {
				mx.multiply(pivot, col, 1d / pval);
			}
			mx.setValueAt(pivot, pivot, 1d);

			//subtract pivot row from other rows
			//find next pivot at the same time
			pval = -Double.MAX_VALUE;
			for (int row = pivot + 1; row < rows; row++) {
				double rpiv = mx.getDoubleValueAt(row, pivot);				
				mx.setValueAt(row, pivot, 0d);
				for (int col = pivot + 1; col < cols; col++) {
					double val = mx.getDoubleValueAt(row, col);
					double sub = mx.getDoubleValueAt(pivot, col);
					val -= sub * rpiv;
					mx.setValueAt(row, col, val);
					// is this our new pivot?
					if (row < prows && col < pcols) {
						if (val < 0d) val = -val;
						if (val > pval) {
							pval = val;
							prow = row;
							pcol = col;
						}
					}
				}
			}
			if (reduced) {
				//subtract pivot from rows above pivot row, too
				for (int row = 0; row < pivot; row++) {
					double rpiv = mx.getDoubleValueAt(row, pivot);				
					mx.setValueAt(row, pivot, 0d);
					for (int col = pivot + 1; col < cols; col++) {
						double val = mx.getDoubleValueAt(row, col);
						double sub = mx.getDoubleValueAt(pivot, col);
						mx.setValueAt(row, col, val - sub * rpiv);
					}
				}
			}
		}
		return pivs;
	}

	/**
	 * Row echelon form using gauss with full pivoting. Note that columns and
	 * rows might be swapped due to the full pivoting, returned in rowmap and 
	 * colmap if they are not null.
	 *  
	 * @param mx		the matrix to reduce
	 * @param reduced	if true, the reduced row echelon form is returned, that
	 * 					is, zeros above the diagonal, too
	 * @param rowmap 	the row mapping to reestablish the original row 
	 * 					ordering. the mapping is only used as out parameter, but
	 * 					its length determines how many rows to use as pivot rows
	 * @param colmap 	the column mapping to reestablish the original column 
	 * 					ordering. the mapping is only used as out parameter, but
	 * 					its length determines how many columns to use as pivot 
	 * 					columns
	 * @return			the rank of the matrix
	 */
	public int rowEchelon(BigIntegerRationalMatrix mx, boolean reduced, int[] rowmap, int[] colmap) {
		final BigFraction zero = mTolerance == 0d ? null : BigFraction.valueOfAdjusted(mTolerance);
		
		final int rows = mx.getRowCount();
		final int cols = mx.getColumnCount();
		final int prows = rowmap == null ? rows : Math.min(rows, rowmap.length);
		final int pcols = colmap == null ? cols : Math.min(cols, colmap.length);
		final int pivs 	= Math.min(prows, pcols);
		
		final IntArray prowNonZeroIndices = new IntArray(cols);
		for (int pivot = 0; pivot < pivs; pivot++) {
			//find pivot row/column
			final GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> pivoting = new GaussPivoting.BiLenProductL(pivot);
//			final GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> pivoting = new GaussPivoting.BiLenProductLorEandMoreRowZeros(pivot);			
//			final GaussPivoting<ReadableBigIntegerRationalMatrix<BigFraction>> pivoting = new GaussPivoting.BiLenSumLorEandMoreRowZeros(pivot);
			
			for (int row = pivot; row < prows; row++) {
				final int rowResult = pivoting.checkCandidateRow(mx, pivot, row);
				boolean cont = true;
				for (int col = pivot; col < pcols && cont; col++) {
					if (!isZero(mx, row, col, zero)) {
//						if (pivot == 0) mx.reduceValueAt(row, col);
						cont = pivoting.checkCandidateCol(mx, pivot, row, col, rowResult);
					}
				}
			}
			final int prow = pivoting.getPivotRow();
			final int pcol = pivoting.getPivotCol();
						
//			System.out.println("pivot " + pivot + " of " + pivs + " has length " + plen + ": " + (plen < 128*128 ? mx.getBigIntegerFractionNumberValueAt(prow, pcol) : "-"));
			if (isZero(mx, prow, pcol, zero)) {
//				System.out.println("exit at rank/pivot " + pivot);
				return pivot;
			}			
			
			//swap rows / columns
			if (prow != pivot) {
				mx.swapRows(prow, pivot);
				if (rowmap != null) {
					IntArray.swap(rowmap, prow, pivot);
				}
			}
			if (pcol != pivot) {
				mx.swapColumns(pcol, pivot);
				if (colmap != null) {
					IntArray.swap(colmap, pcol, pivot);
				}
			}
			
			//divide pivot row
			prowNonZeroIndices.clear();
			final BigFraction pval = mx.getBigFractionValueAt(pivot, pivot);
			if (!pval.isOne()) {
				for (int col = pivot + 1; col < cols; col++) {
					mx.multiply(pivot, col, /*1d / pval*/ pval.getDenominator(), pval.getNumerator());
					if (isZero(mx, pivot, col, zero)) {
                                                // cj: b
						// mx.setValueAt(pivot, col, BigFraction.ZERO);
						mx.setValueAt_BigFraction(pivot, col, BigFraction.ZERO);
                                                // cj: e
					}
					else {
						mx.reduceValueAt(pivot, col);
						prowNonZeroIndices.add(col);
					}
				}
			}
			else {
				for (int col = pivot + 1; col < cols; col++) {
					if (!isZero(mx, pivot, col, zero)) {
						prowNonZeroIndices.add(col);						
					}
				}
			}
                        //cj: b
			// mx.setValueAt(pivot, pivot, BigFraction.ONE);
			mx.setValueAt_BigFraction(pivot, pivot, BigFraction.ONE);
                        // cj: e

			//subtract pivot row from other rows
			//find next pivot at the same time
			for (int row = pivot + 1; row < rows; row++) {
				BigFraction rpiv = mx.getBigFractionValueAt(row, pivot);				
                                // cj: b
				// mx.setValueAt(row, pivot, BigFraction.ZERO);
				mx.setValueAt_BigFraction(row, pivot, BigFraction.ZERO);
                                // 
				if (rpiv.isNonZero()) {
					for (int i = 0; i < prowNonZeroIndices.length(); i++) {
						final int col = prowNonZeroIndices.get(i);
						BigFraction val = mx.getBigFractionValueAt(row, col);
						BigFraction sub = mx.getBigFractionValueAt(pivot, col);
						val = val.subtract(sub.multiply(rpiv));
//						val = val.multiply(BigFraction.valueOf(rpiv.getDenominator())).cancel();//makes values shorter
                                                // cj: b
						// mx.setValueAt(row, col, val);
						mx.setValueAt_BigFraction(row, col, val);
                                                // cj: e
                                                // cj: b
						// if (isZero(mx, row, col, zero)) mx.setValueAt(row, col, BigFraction.ZERO);
						if (isZero(mx, row, col, zero)) mx.setValueAt_BigFraction(row, col, BigFraction.ZERO);
                                                // cj: e
						else mx.reduceValueAt(row, col);					
					}
				}
			}
			if (reduced) {
				//subtract pivot from rows above pivot row, too
				for (int row = 0; row < pivot; row++) {
					BigFraction rpiv = mx.getBigFractionValueAt(row, pivot);				
                                        // cj: b
					// mx.setValueAt(row, pivot, BigFraction.ZERO);
					mx.setValueAt_BigFraction(row, pivot, BigFraction.ZERO);
                                        // cj: e
					if (rpiv.isNonZero()) {
						for (int i = 0; i < prowNonZeroIndices.length(); i++) {
							final int col = prowNonZeroIndices.get(i);
							BigFraction val = mx.getBigFractionValueAt(row, col);
							BigFraction sub = mx.getBigFractionValueAt(pivot, col);
							val = val.subtract(sub.multiply(rpiv));
//							val = val.multiply(BigFraction.valueOf(rpiv.getDenominator())).cancel();//makes values shorter
                                                        // cj: b
							// mx.setValueAt(row, col, val);
							mx.setValueAt_BigFraction(row, col, val);
                                                        // cj: e
                                                        // cj: b
							// if (isZero(mx, row, col, zero)) mx.setValueAt(row, col, BigFraction.ZERO);
							if (isZero(mx, row, col, zero)) mx.setValueAt_BigFraction(row, col, BigFraction.ZERO);
                                                        // cj: e
							else mx.reduceValueAt(row, col);
						}
					}
				}
			}
		}
		return pivs;
	}
	private boolean isZero(BigIntegerRationalMatrix mx, int row, int col, BigFraction zero) {
		return (0 == mx.getSignumAt(row, col)) || 
			(zero != null && mx.getBigFractionValueAt(row, col).abs().compareTo(zero) < 0);		
	}
	private static int[] createIdentityMap(int size) {
		int[] map = new int[size];
		for (int i = 0; i < map.length; i++) {
			map[i] = i;
		}
		return map;
	}
	
	/**
	 * Constructor for <code>Gauss</code> with tolerance, a usually positive
	 * number close to zero, indicating which values have to be treated as
	 * zero. For rational computations, zero is typically used.
	 *  
	 * @param tolerance	positive number close to zero, e.g. 1e-10
	 */
	public Gauss(double tolerance) {
		mTolerance = Math.abs(tolerance);
	}
	
	private static Gauss statInstDbl;
	private static Gauss statInstRat;
	
	/**
	 * Returns the default instance for double computations, i.e. using 
	 * tolerance 10e-10.
	 */
	public static Gauss getDoubleInstance() {
		if (statInstDbl == null) statInstDbl = new Gauss(Zero.DEFAULT_TOLERANCE);
		return statInstDbl;
	}
	
	/**
	 * Returns the default instance for rational computations, i.e. using 
	 * zero tolerance.
	 */
	public static Gauss getRationalInstance() {
		if (statInstRat == null) statInstRat = new Gauss(0d);
		return statInstRat;
	}
	
	/**
	 * Returns the default instance depending on the specified number type, 
	 * which is:<ul>
	 * <li>{@link #getRationalInstance() <b>rational instance</b>} for one of<ul>
	 *   <li>{@link BigFraction}</li>
	 *   <li>{@link BigInteger}</li>
	 *   <li>{@link Long}</li>
	 *   <li>{@link Integer}</li>
	 * </ul></li>
	 * 
	 * <li>{@link #getDoubleInstance() <b>double instance</b>} for one of<ul>
	 *   <li>{@link Double}</li>
	 *   <li>{@link Float}</li>
	 * </ul></li>
	 * </ul>
	 * 
	 * For other class arguments, an illegal argument exception is thrown.
	 */
	public static Gauss getDefaultInstance(Class<? extends Number> numberClass) {
		if (BigFraction.class.isAssignableFrom(numberClass) ||
			BigInteger.class.isAssignableFrom(numberClass) ||
			Long.class.isAssignableFrom(numberClass) ||
			Integer.class.isAssignableFrom(numberClass)
		) {
			return getRationalInstance();
		}
		else if (Double.class.isAssignableFrom(numberClass) ||
				Float.class.isAssignableFrom(numberClass)) {
			return getDoubleInstance();
		}
		throw new IllegalArgumentException("unsupported number type: " + numberClass.getName());
	}
	
	
}
