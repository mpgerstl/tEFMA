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
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.array.impl.DefaultNumberArrayOperations;
import ch.javasoft.math.operator.AggregatingUnaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.operator.TernaryOperator;
import ch.javasoft.math.operator.UnaryOperator;
import ch.javasoft.util.IntArray;

/**
 * The <code>DefaultLinAlgOperations</code> class extends 
 * {@link DefaultBasicLinAlgOperations} and adds default implementations for 
 * more complex linear algebra functions defined by {@link LinAlgOperations}. 
 * <p>
 * An instance of this class defines the (boxed) number type and the array type, 
 * such as {@link Double} and {@code double[]}. 
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public class DefaultLinAlgOperations<N extends Number, A> extends DefaultBasicLinAlgOperations<N, A> implements LinAlgOperations<N, A> {
	
	private final GaussPivotingFactory<N, A> gaussPivotingFactory;
	
	//algebraic expressions
	private final UnaryOperator<N, A>	negater;
	private final BinaryOperator<N, A>	multiplier;
	private final BinaryOperator<N, A>	divider;
	private final BinaryOperator<N, A>	multiplierNormalizer;
	private final BinaryOperator<N, A>	dividerNormalizer;
	/** see {@link #subtractPivotRowMultiple(Object[], int, int, int, Object)} */
	private final TernaryOperator<N, A>	pivotRowMultipleSubtracter;

	public DefaultLinAlgOperations(NumberOperators<N, A> numberOps, ArrayOperations<A> arrayOps, GaussPivotingFactory<N, A> gaussPivotingFactory) {
		this(new DefaultNumberArrayOperations<N, A>(numberOps, arrayOps), gaussPivotingFactory);
	}
	public DefaultLinAlgOperations(NumberArrayOperations<N, A> numberArrayOps, GaussPivotingFactory<N, A> gaussPivotingFactory) {
		super(numberArrayOps);
		this.gaussPivotingFactory = gaussPivotingFactory;

		negater = expressionComposer.neg();
		multiplier = expressionComposer.mul();
		divider = expressionComposer.div();
		multiplierNormalizer = expressionComposer.normalize(expressionComposer.mul());
		dividerNormalizer = expressionComposer.normalize(expressionComposer.div());
		//matrix[row][col] = matrix[row][col] - matrix[row][piv] * matrix[piv][col] 
		//result = x1 - x2 * x3
		pivotRowMultipleSubtracter = expressionComposer.normalize(expressionComposer.subFromFree(expressionComposer.mul()));
	}
	
	public A[] kernel(A[] matrix, int[] rowmap, int[] colmap, int[] ptrNullity) {
		final int cols = arrayOps.getColumnCount(matrix);
		final int[] ptrRank = new int[1];
		if (colmap == null) {
			colmap = new int[cols];
		}
		final A[] rref = rowEchelon(matrix, true /*reduced*/, rowmap, colmap, ptrRank);
		final int rank = ptrRank[0];
		final int ndim = cols - rank;
		final A[] ker = numberArrayOps.newZeroMatrix(cols, ndim);

		final boolean isDivSup = 
			numberOps.getDivisionSupport().isSufficientlyExact() &&
			!numberOps.getDivisionSupport().mightCauseException();

		if (isDivSup) {
			//diagonal
			for (int i = 0; i < ndim; i++) {
				numberArrayOps.set(ker, colmap[i + rank], i, numberOps.one());
			}
			//rest
			for (int row = 0; row < rank; row++) {
				for (int col = 0; col < ndim; col++) {
					arrayOps.copyMatrixElement(rref, row, col + rank, ker, colmap[row], col);
					negater.operate(ker[colmap[row]], col, ker[colmap[row]], col);
				}
			}
		}
		else {			
			//if we had fraction numbers, we would divide the whole row by 
			//the diagonal element (including the diagonal element)
			//for integers, we multiply by LCM(diag(:))/diag(i) instead
			final AggregatingUnaryOperator<N, A> gcdOp = numberOps.aggregatingUnary(AggregatingUnaryOperator.Id.normDivisor);

			//first, reduce each row by its gcd
			A tmp = arrayOps.newVector(2);
			for (int row = 0; row < rank; row++) {
				//copy diag element to tmp[0]
				arrayOps.copyMatrixRowElementsToVector(rref, row, row, tmp, 0, 1);
				//copy gcd of non-diag elements to tmp[1]
				gcdOp.operate(rref[row], rank, ndim, tmp, 1);
				//gcd of tmp[0] and tmp[1], stored in tmp[0]
				gcdOp.operate(tmp, 0, 2, tmp, 0);
				if (!isOne(tmp, 0)) {
					final N gcd = numberArrayOps.get(tmp, 0);
					final UnaryOperator<N, A> divOp = expressionComposer.divFreeBy(expressionComposer.constant(gcd));
					//divide diag element
					divOp.operate(rref[row], row, rref[row], row);
					//divide non-diag elements
					for (int col = 0; col < ndim; col++) {
						divOp.operate(rref[row], col + rank, rref[row], col + rank);
					}
				}
			}
			//now, make all diag elements equal, i.e. mul by LCM(diag(:))/diag(i)
			numberArrayOps.set(tmp, 0, numberOps.one());
			for (int row = 0; row < rank; row++) {
				arrayOps.copyMatrixRowElementsToVector(rref, row, row, tmp, 1, 1);
				gcdOp.operate(tmp, 0, 2, tmp, 1);
				multiplier.operate(tmp, 0, rref[row], row, tmp, 0);
				divider.operate(tmp, 0, tmp, 1, tmp, 0);
			}
			//LCM is in tmp[0]
			final N lcm = numberArrayOps.get(tmp, 0);
			for (int row = 0; row < rank; row++) {
				final UnaryOperator<N, A> mulOp =
					expressionComposer.div(
						expressionComposer.mul(expressionComposer.constant(lcm)), 
						expressionComposer.constant(numberArrayOps.get(rref, row, row))					
					);
				//multiply diagonal element
				mulOp.operate(rref[row], row, rref[row], row);
				//copy diagonal
				for (int i = 0; i < ndim; i++) {
					numberArrayOps.set(ker, colmap[i + rank], i, lcm);
				}
				//multiply & copy rest
				for (int col = 0; col < ndim; col++) {
					mulOp.operate(rref[row], col + rank, rref[row], col + rank);
					arrayOps.copyMatrixElement(rref, row, col + rank, ker, colmap[row], col);
					negater.operate(ker[colmap[row]], col, ker[colmap[row]], col);
				}
			}
		}
		
		if (ptrNullity != null) {
			ptrNullity[0] = ndim;
		}
		return ker;
	}
	public A[] invertMatrix(A[] matrix, int[] rowmap, int[] colmap) {
		if (rowmap == null) {
			final int rows = arrayOps.getRowCount(matrix);
			final int cols = arrayOps.getColumnCount(matrix);
			rowmap = new int[rows];
			colmap = new int[cols];
			initializeMapping(rows, rowmap);
			initializeMapping(cols, colmap);
		}
		return invertMaximalSubmatrixInternal(matrix, rowmap, colmap, null, true /*square*/);
	}
	public A[] invertMaximalSubmatrix(A[] matrix, int[] rowmap, int[] colmap, int[] ptrRank) {
		return invertMaximalSubmatrixInternal(matrix, rowmap, colmap, ptrRank, false /*square*/);
	}
	private A[] invertMaximalSubmatrixInternal(A[] matrix, int[] rowmap, int[] colmap, int[] ptrRank, boolean square) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		if (square && rows != cols) {
			throw new IllegalArgumentException("matrix must be square to be invertible: " + rows + "x" + cols);
		}
		
		//create matrix [mx I]
		final A[] rref = arrayOps.newMatrix(rows, rows + cols);
		arrayOps.copyMatrixElements(matrix, 0, 0, rref, 0, 0, rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < rows; col++) {
				numberArrayOps.set(rref, row, cols + col, numberOps.zero());
			}
			numberArrayOps.set(rref, row, cols + row, numberOps.one());
		}
		//reduced row echelon
		final int rank = rowEchelon(rref, rref, true/*reduced*/, rowmap, colmap);
		if (ptrRank != null) {
			ptrRank[0] = rank;
		}
		if (square && rank < Math.min(rows, cols)) {
			throw new ArithmeticException("singular matrix, rank < size: " + rank + " < " + rows);
		}
		
		//rref has form [I inv(mx)], but rows are swapped as reflected in colmap
		final A[] inv = arrayOps.newMatrix(rank, rank);
		for (int row = 0; row < rank; row++) {
			final int dstRow = square ? colmap[row] : row;
			for (int col = 0; col < rank; col++) {
				final int dstCol = square ? rowmap[col] : col;
				arrayOps.copyMatrixElement(rref, row, cols + rowmap[col], inv, dstRow, dstCol);
			}
		}
		return inv;
	}

	public int nullity(A[] matrix) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final A[] res = arrayOps.newMatrix(rows, cols);
		return cols - rowEchelon(matrix, res, false /*reduced*/, null, null);
	}

	public int rank(A[] matrix) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final A[] res = arrayOps.newMatrix(rows, cols);
		return rowEchelon(matrix, res, false /*reduced*/, null, null);
	}

	public A[] rowEchelon(A[] matrix, boolean reduced, int[] rowmap, int[] colmap, int[] ptrRank) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final A[] res = arrayOps.newMatrix(rows, cols);
		final int rank = rowEchelon(matrix, res, reduced, rowmap, colmap);
		if (ptrRank != null && ptrRank.length > 0) {
			ptrRank[0] = rank;
		}
		return res;
	}

	public int rowEchelon(A[] src, A[] dst, boolean reduced, int[] rowmap, int[] colmap) {
		final boolean isDivSup = 
			numberOps.getDivisionSupport().isSufficientlyExact() &&
			!numberOps.getDivisionSupport().mightCauseException();
		final int rows = arrayOps.getRowCount(dst);
		final int cols = arrayOps.getColumnCount(dst);
		
		if (src != dst) {
			arrayOps.copyMatrixElements(src, 0, 0, dst, 0, 0, rows, cols);
		}

		final int prows = initializeMapping(rows, rowmap);
		final int pcols = initializeMapping(cols, colmap);
		final int pivs 	= Math.min(prows, pcols);
		
		final IntArray prowNonZeroIndices = new IntArray(cols);
		for (int pivot = 0; pivot < pivs; pivot++) {
			//find pivot row/column
			final GaussPivoting<N, A> pivoting = gaussPivotingFactory.getGaussPivoting(numberArrayOps, pivot);
			
			for (int row = pivot; row < prows; row++) {
				final int rowResult = pivoting.checkCandidateRow(dst, pivot, row);
				boolean cont = true;
				for (int col = pivot; col < pcols && cont; col++) {
					if (isNonZero(dst, row, col)) {
//						if (pivot == 0) mx.reduceValueAt(row, col);
						cont = pivoting.checkCandidateCol(dst, pivot, row, col, rowResult);
					}
				}
			}
			final int prow = pivoting.getPivotRow();
			final int pcol = pivoting.getPivotCol();
						
//			System.out.println("pivot " + pivot + " of " + pivs + " has length " + plen + ": " + (plen < 128*128 ? mx.getBigIntegerFractionNumberValueAt(prow, pcol) : "-"));
			if (isZero(dst, prow, pcol)) {
//				System.out.println("exit at rank/pivot " + pivot);
				return pivot;
			}			
			
			//swap rows / columns
			if (prow != pivot) {
				arrayOps.swapMatrixRows(dst, prow, pivot);
				if (rowmap != null) {
					IntArray.swap(rowmap, prow, pivot);
				}
			}
			if (pcol != pivot) {
				arrayOps.swapMatrixColumns(dst, pcol, pivot);
				if (colmap != null) {
					IntArray.swap(colmap, pcol, pivot);
				}
			}
			
			//normalize pivot row
			prowNonZeroIndices.clear();
			if (isDivSup) {
				final boolean divide = !isOne(dst, pivot, pivot);
				//collect non-zero row values and divide by pivot if necessary
				for (int col = pivot + 1; col < cols; col++) {
					if (isNonZero(dst, pivot, col)) {
						if (divide) {
							divide(dst, pivot, col, pivot, pivot);
						}
						prowNonZeroIndices.add(col);						
					}
				}
				numberArrayOps.set(dst, pivot, pivot, numberOps.one());					
			}
			else {
				final boolean negate = isNeg(dst, pivot, pivot);
				//collect non-zero row values and negate if necessary
				for (int col = pivot + 1; col < cols; col++) {
					if (isNonZero(dst, pivot, col)) {
						if (negate) {
							negate(dst, pivot, col);
						}
						prowNonZeroIndices.add(col);						
					}
				}
				if (negate) {
					negate(dst, pivot, pivot);
				}
			}

			//subtract pivot row from other rows
			for (int row = pivot + 1; row < rows; row++) {
				if (isNonZero(dst, row, pivot)) {
					if (isDivSup) {
						for (int i = 0; i < prowNonZeroIndices.length(); i++) {
							final int col = prowNonZeroIndices.get(i);
							subtractPivotRowMultiple(dst, row, col, pivot);
						}
					}
					else {
						int i = 0;
						for (int col = pivot + 1; col < cols; col++) {
							if (i < prowNonZeroIndices.length() && prowNonZeroIndices.get(i) == col) {
								i++;
								//non-zero row pivot, multiply and subtract row
								multiply(dst, row, col, pivot, pivot, false);
								subtractPivotRowMultiple(dst, row, col, pivot);
							}
							else {
								//zero row pivot, multiply only
								multiply(dst, row, col, pivot, pivot, true);
							}
						}
					}
					numberArrayOps.set(dst, row, pivot, numberOps.zero());
				}
			}
			if (reduced) {
				//subtract pivot from rows above pivot row, too
				for (int row = 0; row < pivot; row++) {
					if (isNonZero(dst, row, pivot)) {
						if (isDivSup) {
							for (int i = 0; i < prowNonZeroIndices.length(); i++) {
								final int col = prowNonZeroIndices.get(i);
								subtractPivotRowMultiple(dst, row, col, pivot);
							}
						}
						else {
							int i = 0;
							for (int col = pivot + 1; col < cols; col++) {
								if (i < prowNonZeroIndices.length() && prowNonZeroIndices.get(i) == col) {
									i++;
									//non-zero row pivot, multiply and subtract row
									multiply(dst, row, col, pivot, pivot, false);
//									pivotRowMultipleSubtracter.operate(dst[row], col, dst[row], pivot, dst[pivot], col, dst[row], col);
//									subtractPivotRowMultiple(dst, row, col, pivot, ptrTmp);
									subtractPivotRowMultiple(dst, row, col, pivot);
								}
								else {
									//zero row pivot, multiply only
									multiply(dst, row, col, pivot, pivot, true);
								}
							}
							//important: also multiply the row pivot element
							multiply(dst, row, row, pivot, pivot, true);
						}
						numberArrayOps.set(dst, row, pivot, numberOps.zero());
					}
				}
			}
		}
		return pivs;
	}
	
	private boolean isZero(A[] matrix, int row, int col) {
		return numberOps.booleanUnary(BooleanUnaryOperator.Id.isZero).booleanOperate(matrix[row], col);
	}
	private boolean isNonZero(A[] matrix, int row, int col) {
		return numberOps.booleanUnary(BooleanUnaryOperator.Id.isNonZero).booleanOperate(matrix[row], col);
	}
	private boolean isOne(A[] matrix, int row, int col) {
		return numberOps.booleanUnary(BooleanUnaryOperator.Id.isOne).booleanOperate(matrix[row], col);
	}
	private boolean isOne(A vec, int index) {
		return numberOps.booleanUnary(BooleanUnaryOperator.Id.isOne).booleanOperate(vec, index);
	}
	private boolean isNeg(A[] matrix, int row, int col) {
		return numberOps.booleanUnary(BooleanUnaryOperator.Id.isNegative).booleanOperate(matrix[row], col);
	}
	/**
	 * Negates the specified value, that is, 
	 * <pre>
	 * 	matrix[row][col] =-matrix[row][col]
	 * </pre>
	 */
	private void negate(A[] matrix, int row, int col) {
		negater.operate(matrix[row], col, matrix[row], col);
	}
	/**
	 * Divides and normalizes, that is, 
	 * <pre>
	 * 	matrix[row][col] = normalize( matrix[row][col] / matrix[divRow][divCol] )
	 * </pre>
	 */
	private void divide(A[] matrix, int row, int col, int divRow, int divCol) {
		dividerNormalizer.operate(matrix[row], col, matrix[divRow], divCol, matrix[row], col);
	}
	/**
	 * Multiplies, that is, 
	 * <pre>
	 * 	matrix[row][col] = matrix[row][col] * matrix[mulRow][mulCol]
	 * </pre>
	 * The result value is normalized if {@code normalize = true}.
	 */
	private void multiply(A[] matrix, int row, int col, int mulRow, int mulCol, boolean normalize) {
		if (normalize) {
			multiplierNormalizer.operate(matrix[row], col, matrix[mulRow], mulCol, matrix[row], col);			
		}
		else {
			multiplier.operate(matrix[row], col, matrix[mulRow], mulCol, matrix[row], col);			
		}
	}
	/**
	 * Operates a single value of subtracting the pivot row from the current 
	 * row:
	 * <pre>
	 * matrix[row][col] = matrix[row][col] - matrix[row][piv] * matrix[piv][col] 
	 * </pre>
	 * The value is normalized.
	 * <p>
	 * <b>Note:</b> If the pivot value is unequal to one, the matrix value must
	 * be multiplied with it before calling this method, that is, perform
	 * <pre>
	 * matrix[row][col] = matrix[piv][piv] * matrix[row][col] 
	 * </pre>
	 * in advance to get the correct overall operation
	 * <pre>
	 * matrix[row][col] = matrix[piv][piv] * matrix[row][col] - matrix[row][piv] * matrix[piv][col] 
	 * </pre>
	 */
	private void subtractPivotRowMultiple(A[] matrix, int row, int col, int piv) {
		pivotRowMultipleSubtracter.operate(matrix[row], col, matrix[row], piv, matrix[piv], col, matrix[row], col);
	}
	
	/**
	 * Fills the mapping {@code map} such that {@code map[i] = i} and returns
	 * {@code min(size, length(map))}. If {@code map == null}, {@code size}
	 * is returned.
	 */
	private static int initializeMapping(int size, int[] map) {
		if (map == null) return size;
		for (int i = 0; i < map.length; i++) {
			map[i] = i;
		}
		return Math.min(size, map.length);
	}

}
