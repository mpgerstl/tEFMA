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
import ch.javasoft.math.array.Converter;
import ch.javasoft.math.array.ExpressionComposer;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.operator.AggregatingBinaryOperator;
import ch.javasoft.math.operator.AggregatingUnaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.operator.IntUnaryOperator;
import ch.javasoft.math.operator.NullaryOperator;
import ch.javasoft.math.operator.UnaryOperator;

/**
 * The <code>DefaultNumberArrayOperations</code> class implements
 * {@link NumberArrayOperations} based on an instance of {@link NumberOperators} 
 * and {@link ArrayOperations}. An instance of this class defines the (boxed) 
 * number type and the array type, such as {@link Double} and {@code double[]}. 
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public class DefaultNumberArrayOperations<N extends Number, A> implements NumberArrayOperations<N, A> {
	
	private final NumberOperators<N, A> 	operators;
	private final ArrayOperations<A> 		arrayOps;
	private final ExpressionComposer<N, A> 	expressionComposer;
	
	public DefaultNumberArrayOperations(NumberOperators<N, A> operators, ArrayOperations<A> arrayOps) {
		this.operators			= operators;
		this.arrayOps 			= arrayOps;
		this.expressionComposer	= new ExpressionComposer<N, A>(arrayOps, operators);
	}

	public final NumberOperators<N, A> getNumberOperators() {
		return operators;
	}

	public final ArrayOperations<A> getArrayOperations() {
		return arrayOps;
	}
	
	public <IN extends Number, IA> Converter<IN, IA, N, A> getConverterFrom(NumberArrayOperations<IN, IA> fromOps) {
		return new Converter<IN, IA, N, A>(fromOps, this);
	}
	
	public <RN extends Number, RA> Converter<N, A, RN, RA> getConverterTo(NumberArrayOperations<RN, RA> toOps) {
		return new Converter<N, A, RN, RA>(this, toOps);
	}
	
	public ExpressionComposer<N, A> getExpressionComposer() {
		return expressionComposer;
	}
	
	public N convertNumber(Number value) {
		return operators.converter().operate(value);
	}
	
	public Class<N> numberClass() {
		return operators.numberClass();
	}

	public Class<A> arrayClass() {
		return operators.arrayClass();
	}
	
	public A newZeroVector(int length) {
		return newVector(length, operators.zero());
	}
	public A newOneVector(int length) {
		return newVector(length, operators.one());
	}
	public A newVector(int length, N initialValue) {
		final A vec = arrayOps.newVector(length);
		if (initializationNeeded(initialValue)) {
			setAll(vec, initialValue);
		}
		return vec;
	}
	
	public A[] newZeroMatrix(int rows, int columns) {
		return newMatrix(rows, columns, operators.zero());
	}
	public A[] newOneMatrix(int rows, int columns) {
		return newMatrix(rows, columns, operators.one());
	}	
	public A[] newMatrix(int rows, int columns, N initialValue) {
		final A[] mat = arrayOps.newMatrix(rows, columns);
		if (initializationNeeded(initialValue)) {
			setAll(mat, initialValue);
		}
		return mat;
	}
	public A[] newIdentityMatrix(int size) {
		return newDiagonalMatrix(size, operators.one());
	}
	public A[] newDiagonalMatrix(int size, N diagonalValue) {		
		final A[] mat = newZeroMatrix(size, size);
		final NullaryOperator<N, A> op = operators.constant(diagonalValue);
		for (int i = 0; i < size; i++) {
			op.operate(mat[i], i);
		}
		return mat;
	}
	public A[] newDiagonalMatrix(A diagonalValues) {
		final int size = arrayOps.getLength(diagonalValues);
		final A[] mat = newZeroMatrix(size, size);
		arrayOps.copyVectorElementsToMatrixDiagonal(diagonalValues, mat);
		return mat;
	}
	
	public boolean allBoolean(A v, BooleanUnaryOperator<N, A> operator) {
		final int len = arrayOps.getLength(v);
		for (int i = 0; i < len; i++) {
			if (!evalBoolean(v, i, operator)) {
				return false;
			}
		}
		return true;
	}

	public boolean allBoolean(A v, int[] indices, BooleanUnaryOperator<N, A> operator) {
		for (int i = 0; i < indices.length; i++) {
			if (!evalBoolean(v, indices[i], operator)) {
				return false;
			}
		}
		return true;
	}

	public boolean allBoolean(A[] matrix, BooleanUnaryOperator<N, A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!evalBoolean(matrix, r, c, operator)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean allBoolean(A[] matrix, int[] rowIndices, int[] colIndices, BooleanUnaryOperator<N, A> operator) {
		for (int r = 0; r < rowIndices.length; r++) {
			for (int c = 0; c < colIndices.length; c++) {
				if (!evalBoolean(matrix, rowIndices[r], colIndices[c], operator)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean allBooleanInColumn(A[] matrix, int col, BooleanUnaryOperator<N, A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		for (int r = 0; r < rows; r++) {
			if (!evalBoolean(matrix, r, col, operator)) {
				return false;
			}
		}
		return true;
	}

	public boolean allBooleanInRange(A v, int from, int to, BooleanUnaryOperator<N, A> operator) {
		for (int i = from; i < to; i++) {
			if (!evalBoolean(v, i, operator)) {
				return false;
			}	
		}
		return true;
	}

	public boolean allBooleanInRange(A[] matrix, int rowFrom, int rowTo, int colFrom, int colTo, BooleanUnaryOperator<N, A> operator) {
		for (int r = rowFrom; r < rowTo; r++) {
			for (int c = colFrom; c < colTo; c++) {
				if (!evalBoolean(matrix, r, c, operator)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean allBooleanInRow(A[] matrix, int row, BooleanUnaryOperator<N, A> operator) {
		final int cols = arrayOps.getColumnCount(matrix);
		for (int c = 0; c < cols; c++) {
			if (!evalBoolean(matrix, row, c, operator)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean anyBoolean(A v, BooleanUnaryOperator<N, A> operator) {
		final int len = arrayOps.getLength(v);
		for (int i = 0; i < len; i++) {
			if (evalBoolean(v, i, operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean anyBoolean(A v, int[] indices, BooleanUnaryOperator<N, A> operator) {
		for (int i = 0; i < indices.length; i++) {
			if (evalBoolean(v, indices[i], operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean anyBoolean(A[] matrix, BooleanUnaryOperator<N, A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (evalBoolean(matrix, r, c, operator)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean anyBoolean(A[] matrix, int[] rowIndices, int[] colIndices, BooleanUnaryOperator<N, A> operator) {
		for (int r = 0; r < rowIndices.length; r++) {
			for (int c = 0; c < colIndices.length; c++) {
				if (evalBoolean(matrix, rowIndices[r], colIndices[c], operator)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean anyBooleanInColumn(A[] matrix, int col, BooleanUnaryOperator<N, A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		for (int r = 0; r < rows; r++) {
			if (evalBoolean(matrix, r, col, operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean anyBooleanInRange(A v, int from, int to, BooleanUnaryOperator<N, A> operator) {
		for (int i = from; i < to; i++) {
			if (evalBoolean(v, i, operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean anyBooleanInRange(A[] matrix, int rowFrom, int rowTo, int colFrom, int colTo, BooleanUnaryOperator<N, A> operator) {
		for (int r = rowFrom; r < rowTo; r++) {
			for (int c = colFrom; c < colTo; c++) {
				if (evalBoolean(matrix, r, c, operator)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean anyBooleanInRow(A[] matrix, int row, BooleanUnaryOperator<N, A> operator) {
		final int cols = arrayOps.getColumnCount(matrix);
		for (int c = 0; c < cols; c++) {
			if (evalBoolean(matrix, row, c, operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean allBooleanInDiagonal(A[] matrix, ch.javasoft.math.operator.BooleanUnaryOperator<N,A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final int len  = Math.min(rows, cols);
		for (int i = 0; i < len; i++) {
			if (!evalBoolean(matrix, i, i, operator)) {
				return false;
			}
		}
		return true;
	}

	public boolean anyBooleanInDiagonal(A[] matrix, ch.javasoft.math.operator.BooleanUnaryOperator<N,A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final int len  = Math.min(rows, cols);
		for (int i = 0; i < len; i++) {
			if (evalBoolean(matrix, i, i, operator)) {
				return true;
			}
		}
		return false;
	}
	
	public A applyToEachElement(A v, UnaryOperator<N, A> operator) {
		final int len = arrayOps.getLength(v);
		final A res = arrayOps.newVector(len);
		for (int i = 0; i < len; i++) {
			operator.operate(v, i, res, i);
		}
		return res;
	}

	public void applyToEachElement(A src, A dst, UnaryOperator<N, A> operator) {
		final int len = getAndCheckEqualVectorLength(src, dst, "source and destination vector must have same length: ");
		for (int i = 0; i < len; i++) {
			operator.operate(src, i, dst, i);
		}
	}

	public A[] applyToEachElement(A[] matrix, UnaryOperator<N, A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final A[] res = arrayOps.newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				operator.operate(matrix[r], c, res[r], c);
			}
		}
		return res;
	}

	public void applyToEachElement(A[] src, A[] dst, UnaryOperator<N, A> operator) {
		final int rows = getAndCheckEqualMatrixRowCount(src, dst, "source and destination matrix must have same row count: ");
		final int cols = getAndCheckEqualMatrixColumnCount(src, dst, "source and destination matrix must have same column count: ");
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				operator.operate(src[r], c, dst[r], c);
			}
		}
	}

	public A applyToElementByElement(A v, A u, BinaryOperator<N, A> operator) {
		final int len = getAndCheckEqualVectorLength(u, v, "source vectors must have same length: ");
		final A res = arrayOps.newVector(len);
		for (int i = 0; i < len; i++) {
			operator.operate(v, i, u, i, res, i);
		}
		return res;
	}

	public void applyToElementByElement(A src1, A src2, A dst, BinaryOperator<N, A> operator) {
		final int len = getAndCheckEqualVectorLength(src1, src2, dst, "source vectors and destination vector must have same length: ");
		for (int i = 0; i < len; i++) {
			operator.operate(src1, i, src2, i, dst, i);
		}
	}

	public A[] applyToElementByElement(A[] src1, A[] src2, BinaryOperator<N, A> operator) {
		final int rows = getAndCheckEqualMatrixRowCount(src1, src2, "source matrices must have same row count: ");
		final int cols = getAndCheckEqualMatrixColumnCount(src1, src2, "source matrices must have same column count: ");
		final A[] res = arrayOps.newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				operator.operate(src1[r], c, src2[r], c, res[r], c);
			}
		}
		return res;
	}

	public void applyToElementByElement(A[] src1, A[] src2, A[] dst, BinaryOperator<N, A> operator) {
		final int rows = getAndCheckEqualMatrixRowCount(src1, src2, dst, "source matrices and destination matrix must have same row count: ");
		final int cols = getAndCheckEqualMatrixColumnCount(src1, src2, dst, "source matrices and destination matrix must have same column count: ");
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				operator.operate(src1[r], c, src2[r], c, dst[r], c);
			}
		}
	}

	public boolean evalBoolean(A v, int index, BooleanUnaryOperator<N, A> operator) {
		return operator.booleanOperate(v, index);
	}

	public boolean evalBoolean(A[] matrix, int row, int col, BooleanUnaryOperator<N, A> operator) {
		return operator.booleanOperate(matrix[row], col);
	}

	public int evalInt(A v, int index, IntUnaryOperator<N, A> operator) {
		return operator.intOperate(v, index);
	}

	public int evalInt(A[] matrix, int row, int col, IntUnaryOperator<N, A> operator) {
		return operator.intOperate(matrix[row], col);
	}

	public N get(A vector, int index) {
		return operators.unary(UnaryOperator.Id.identity).operate(vector, index);
	}

	public N get(A[] matrix, int row, int col) {
		return operators.unary(UnaryOperator.Id.identity).operate(matrix[row], col);
	}
	
	public int getSignum(A vector, int index) {
		return operators.intUnary(IntUnaryOperator.Id.signum).intOperate(vector, index);
	}
	public int getSignum(A[] matrix, int row, int col) {
		return operators.intUnary(IntUnaryOperator.Id.signum).intOperate(matrix[row], col);
	}
	
	public long[] getVectorSupportAsLongBits(A vector) {
		final int len = arrayOps.getLength(vector);
		final long[] support = new long[(len+63)/64]; 
		for (int i = 0; i < len; i++) {
			if (0 != getSignum(vector, i)) {
				support[i/64] |= (1L << (i % 64));
			}
		}
		return support;
	}

	public long[][] getMatrixSupportAsLongBits(A[] matrix) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final long[][] support = new long[rows][(cols+63)/64];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (0 != getSignum(matrix, r, c)) {
					support[r][c/64] |= (1L << (c % 64));
				}
			}
		}
		return support;
	}
	
	public long[] getMatrixRowSupportAsLongBits(A[] matrix, int row) {
		return getVectorSupportAsLongBits(matrix[row]);
	}
	
	public long[] getMatrixColumnSupportAsLongBits(A[] matrix, int col) {
		final int rows = arrayOps.getRowCount(matrix);
		final long[] support = new long[(rows+63)/64];
		for (int r = 0; r < rows; r++) {
			if (0 != getSignum(matrix, r, col)) {
				support[r/64] |= (1L << (r % 64));
			}
		}
		return support;		
	}

	public N applyTo(A v, AggregatingUnaryOperator<N,A> operator) {
		return operator.operate(v, 0, arrayOps.getLength(v));
	}
	public N applyToRow(A[] matrix, int row, AggregatingUnaryOperator<N, A> operator) {
		return operator.operate(matrix[row], 0, arrayOps.getColumnCount(matrix));
	}
	public N applyToColumn(A[] matrix, int col, AggregatingUnaryOperator<N, A> operator) {
		return operator.operate(matrix, 0, col, arrayOps.getRowCount(matrix), 1);
	}
	public A applyToEachRow(A[] matrix, AggregatingUnaryOperator<N,A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final A res = arrayOps.newVector(rows);
		for (int r = 0; r < rows; r++) {
			operator.operate(matrix[r], 0, cols, res, r);
		}
		return res;
	}
	public void applyToEachRow(A[] matrix, A dst, AggregatingUnaryOperator<N,A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final int len  = arrayOps.getLength(dst);
		//check dimensions
		if (rows != len) {
			throw new IllegalArgumentException("matrix row count and destination vector length must be equal: " + rows + " != " + len);
		}
		for (int r = 0; r < rows; r++) {
			operator.operate(matrix[r], 0, cols, dst, r);
		}
	}
	public A applyToEachColumn(A[] matrix, AggregatingUnaryOperator<N,A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final A res = arrayOps.newVector(cols);
		for (int c = 0; c < cols; c++) {
			operator.operate(matrix, 0, c, rows, 1, res, c);
		}
		return res;
	}
	public void applyToEachColumn(A[] matrix, A dst, AggregatingUnaryOperator<N,A> operator) {
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		final int len  = arrayOps.getLength(dst);
		//check dimensions
		if (cols != len) {
			throw new IllegalArgumentException("matrix column count and destination vector length must be equal: " + cols + " != " + len);
		}
		for (int c = 0; c < cols; c++) {
			operator.operate(matrix, 0, c, rows, 1, dst, c);
		}
	}
	
	public N applyTo(A v, A u, AggregatingBinaryOperator<N,A> operator) {
		final int len = getAndCheckEqualVectorLength(u, v, "source vectors must have same length: ");
		return operator.operate(v, 0, u, 0, len);
	}
	
	//////////////////////////// helpers	
	
	private int getAndCheckEqualVectorLength(A v, A u, String errMsg) {
		final int len = arrayOps.getLength(v);
		final int chk = arrayOps.getLength(u);
		if (len == chk) {
			return len;
		}
		throw new IllegalArgumentException(errMsg + len + " != " + chk);
	}
	private int getAndCheckEqualVectorLength(A v, A u, A w, String errMsg) {
		final int len = arrayOps.getLength(v);
		final int ck1 = arrayOps.getLength(u);
		final int ck2 = arrayOps.getLength(w);
		if (len == ck1 && len == ck2) {
			return len;
		}
		throw new IllegalArgumentException(errMsg + len + ", " + ck1 + ", " + ck2);
	}
	private int getAndCheckEqualMatrixRowCount(A[] mx1, A[] mx2, String errMsg) {
		final int len = arrayOps.getRowCount(mx1);
		final int chk = arrayOps.getRowCount(mx2);
		if (len == chk) {
			return len;
		}
		throw new IllegalArgumentException(errMsg + len + " != " + chk);
	}
	private int getAndCheckEqualMatrixColumnCount(A[] mx1, A[] mx2, String errMsg) {
		final int len = arrayOps.getColumnCount(mx1);
		final int chk = arrayOps.getColumnCount(mx2);
		if (len == chk) {
			return len;
		}
		throw new IllegalArgumentException(errMsg + len + " != " + chk);
	}
	private int getAndCheckEqualMatrixRowCount(A[] mx1, A[] mx2, A[] mx3, String errMsg) {
		final int len = arrayOps.getRowCount(mx1);
		final int ck1 = arrayOps.getRowCount(mx2);
		final int ck2 = arrayOps.getRowCount(mx3);
		if (len == ck1 && len == ck2) {
			return len;
		}
		throw new IllegalArgumentException(errMsg + len + ", " + ck1 + ", " + ck2);
	}
	private int getAndCheckEqualMatrixColumnCount(A[] mx1, A[] mx2, A[] mx3, String errMsg) {
		final int len = arrayOps.getColumnCount(mx1);
		final int ck1 = arrayOps.getColumnCount(mx2);
		final int ck2 = arrayOps.getColumnCount(mx3);
		if (len == ck1 && len == ck2) {
			return len;
		}
		throw new IllegalArgumentException(errMsg + len + ", " + ck1 + ", " + ck2);
	}

	public void applyToEachColumnElement(A[] src, int srcCol, A[] dst, int dstCol, UnaryOperator<N, A> operator) {
		final int rows = getAndCheckEqualMatrixRowCount(src, dst, "source and destination matrix must have the same number of rows: ");
		for (int r = 0; r < rows; r++) {
			operator.operate(src[r], srcCol, dst[r], dstCol);
		}		
	}

	public void applyToEachColumnElement(A[] src, int srcRowStart, int srcCol, A[] dst, int dstRowStart, int dstCol, int length, UnaryOperator<N, A> operator) {
		for (int r = 0; r < length; r++) {
			operator.operate(src[srcRowStart + r], srcCol, dst[dstRowStart + r], dstCol);
		}		
	}

	public void applyToEachDiagonalElement(A[] src, A[] dst, UnaryOperator<N, A> operator) {
		final int rows = getAndCheckEqualMatrixRowCount(src, dst, "source and destination matrix must have the same number of rows: ");
		final int cols = getAndCheckEqualMatrixColumnCount(src, dst, "source and destination matrix must have the same number of columns: ");
		final int len = Math.min(rows, cols);
		for (int i = 0; i < len; i++) {
			operator.operate(src[i], i, dst[i], i);
		}		
	}

	public void applyToEachElement(A src, int srcStart, A dst, int dstStart, int length, UnaryOperator<N, A> operator) {
		for (int i = 0; i < length; i++) {
			operator.operate(src, srcStart + i, dst, dstStart + i);
		}		
	}

	public void applyToEachRowElement(A[] src, int srcRow, A[] dst, int dstRow, UnaryOperator<N, A> operator) {
		final int cols = getAndCheckEqualMatrixColumnCount(src, dst, "source and destination matrix must have the same number of columns: ");
		for (int c = 0; c < cols; c++) {
			operator.operate(src[srcRow], c, dst[dstRow], c);
		}		
	}

	public void applyToEachRowElement(A[] src, int srcRow, int srcColStart, A[] dst, int dstRow, int dstColStart, int length, UnaryOperator<N, A> operator) {
		for (int c = 0; c < length; c++) {
			operator.operate(src[srcRow], srcColStart + c, dst[dstRow], dstColStart + c);
		}		
	}

	public void set(A vector, int index, N value) {
		final NullaryOperator<N, A> op = operators.constant(value);
		op.operate(vector, index);
	}

	public void set(A[] matrix, int row, int col, N value) {
		set(matrix[row], col, value);
	}

	public void setAll(A vector, N value) {
		final NullaryOperator<N, A> op = operators.constant(value); 
		final int len = arrayOps.getLength(vector);
		for (int i = 0; i < len; i++) {
			op.operate(vector, i);
		}
	}

	public void setAll(A[] matrix, N value) {
		final NullaryOperator<N, A> op = operators.constant(value); 
		final int rows = arrayOps.getRowCount(matrix);
		final int cols = arrayOps.getColumnCount(matrix);
		for (int r = 0; r < rows; r++) {
			final A row = matrix[r];
			for (int c = 0; c < cols; c++) {
				op.operate(row, c);
			}
		}
	}
	
	/**
	 * Returns true if the initialization value is non-zero or if the number
	 * type is not a primitive type
	 */
	private boolean initializationNeeded(N value) {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isNonZero).booleanOperate(value) || !numberClass().isPrimitive();
	}

}
