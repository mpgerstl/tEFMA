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
package ch.javasoft.metabolic.efm.borndie.range;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;

/**
 * The <code>LowerTriangularMatrix</code> represents the range of a lower 
 * triangular matrix, starting at row/column <code>(0,0)</code> as upper left 
 * cell. Note that the {@link BornDieMatrix} is such a lower trianguar matrix.
 */
public class LowerTriangularMatrix implements Range {
	
	private final int length;
	
	public LowerTriangularMatrix(int length) {
		this.length = length;
	}
	
	/**
	 * Returns the number of rows, equal to the {@link #getLength() side length}
	 * of the square matrix
	 */
	public int getRowCount() {
		return length;
	}
	/**
	 * Returns the number of columns, equal to the {@link #getLength() side length}
	 * of the square matrix
	 */
	public int getColumnCount() {
		return length;
	}
	/**
	 * Returns the first row index for the specified column, which is actually
	 * the column index itself.
	 */
	public int getRowFrom(int column) {
		return column;		
	}
	/**
	 * Returns index of the row after the last row for the specified column, 
	 * which is actually {@link #getLength() length}
	 */
	public int getRowTo(int column) {
		return length;
	}
	/**
	 * Returns the first column index for the specified row, which is actually
	 * <code>0</code>
	 */
	public int getColumnFrom(int row) {
		return 0;
	}
	/**
	 * Returns index of the column after the last column for the specified row, 
	 * which is actually <code>row + 1</code>
	 */
	public int getColumnTo(int row) {
		return row + 1;
	}
	/**
	 * Returns the number of cells for the specified column, which is actually
	 * {@link #getLength() length} <code>- column</code>
	 */
	public int getColumnHeight(int column) {
		return length - column;
	}
	/**
	 * Returns the number of cells for the specified row, which is actually
	 * <code>row + 1</code>
	 */
	public int getRowWidth(int row) {
		return row + 1;
	}
	
	/**
	 * Returns the side length of the square matrix
	 */
	public int getLength() {
		return length;
	}
	
	public boolean contains(CellRange cell) {
		return contains(cell.getBornColumn(), cell.getDieRow());
	}
	public boolean contains(int bornColumn, int dieRow) {
		return bornColumn >= 0 && dieRow >= bornColumn && dieRow < length;
	}
	
	/**
	 * Returns the final row index. Final row cells contain never dying modes, 
	 * the result of the algorithm. 
	 *  
	 * @return	{@link #getLength() length}{@code - 1}
	 */
	public int getFinalRow() {
		return getLength() - 1;
	}
	/**
	 * Returns true if the specified {@code dieRow} is the final row. Final row
	 * cells contain never dying modes, the result of the algorithm. 
	 *  
	 * @param dieRow	the row to check
	 * @return	{@code true} if {@code dieRow == }{@link #getLength() length}{@code - 1}
	 */
	public boolean isFinalRow(int dieRow) {
		return dieRow == getFinalRow();
	}

	/**
	 * Returns the number of cells contained in this lower triangular matrix,
	 * which is <code>(len * (len + 1)) / 2</code>, where <code>len</code> is 
	 * the {@link #getLength() side length} of the matrix.
	 */
	public int getCellCount() {
		return (length * (length + 1)) / 2;
	}
	
	/**
	 * Convert 2-dim index to 1-dim index. The lower triangular matrix cells are
	 * column packed, that is, a column can be accessed with successive indices.
	 * The reverse method is {@link #indexToCell(int)}
	 * 
	 * @param bornCol	the column index, from 0 to n, i.e. n+1 columns for n iterations
	 * @param dieRow	the row index, from 0 to n, i.e. n+1 rows for n iterations
	 * @return the column-by-column packed one dimensional index
	 * @throws IndexOutOfBoundsException if row or column index is negative, if
	 * 									 row or column index is larger or equal
	 * 									 to {@link #getLength() length}, or if
	 * 									 the column index is larger than the row
	 * 									 index 
	 */
	public int cellToIndex(int bornCol, int dieRow) throws IndexOutOfBoundsException {
		if (dieRow < 0) throw new IndexOutOfBoundsException("negative row index: " + dieRow);
		if (bornCol < 0) throw new IndexOutOfBoundsException("negative column index: " + bornCol);
		if (dieRow >= length) throw new IndexOutOfBoundsException("row index >= length: " + dieRow + " >= " + length);
		if (bornCol >= length) throw new IndexOutOfBoundsException("column index >= length: " + bornCol + " >= " + length);
		if (bornCol > dieRow) throw new IndexOutOfBoundsException("column index is larger than row index: " + bornCol + " > " + dieRow);
		
		//the overall lower-tri-mx length:			length * (length + 1) / 2
		//the right  lower-tri-mx length to subtract,
		//		includes current column:			(length - col) *	(length - col + 1) / 2
		//the row index to add:						row - col
		//
		//MATLAB
		//syms c s r real
		//simplify((s * (s+1) - (s-c)*(s-c+1))/2 + r - c)
		//s*c-1/2*c^2-1/2*c+r
		return bornCol*length - ((bornCol*bornCol + bornCol) / 2) + dieRow;
	}
	
	/**
	 * Convert 1-dim index as returned by {@link #cellToIndex(int, int)} back 
	 * into the 2-dim row/column index of the lower triangular matrix. The 
	 * current implementation is quite inefficient and takes <code>O(n)</code>,
	 * where <code>n</code> is the column/row length of the square lower 
	 * triangular matrix.
	 * 
	 * @param index the packed index as returned by {@link #cellToIndex(int, int)}
	 * @return the single cell, that is, row and column index
	 * @throws IndexOutOfBoundsException if index is negative or larger or equal
	 * 									 to {@link #getCellCount()}
	 */
	public CellRange indexToCell(int index) throws IndexOutOfBoundsException {
		if (index < 0) throw new IndexOutOfBoundsException("negative index: " + index);
		if (index >= getCellCount()) throw new IndexOutOfBoundsException("index after last cell: " + index + ">=" + getCellCount());
		for (int col = 0; col < length; col++) {
			final int colSize = length - col;
			if (index < colSize) {
				return new DefaultCellRange(col, index + col);
			}
			index -= colSize;
		}
		throw new IndexOutOfBoundsException("not in cell: " + index + " >= " + getCellCount());
	}
	
	/**
	 * Returns the cells column by column, starting from left. The rows per 
	 * column are iterated from top to down.
	 */
	public Iterator<CellRange> iterator() {
		return new Iterator<CellRange>() {
			int row = 0;
			int col = 0;
			public boolean hasNext() {
				return row < length;
			}
			public CellRange next() {
				final int cur = row;
				if (row < length) {
					row++;
					if (row >= length && (col + 1) < length) {
						row = 0;
						col++;
					}
					return new DefaultCellRange(col, cur);
				}
				throw new NoSuchElementException();
			}
			public void remove() {
				throw new UnsupportedOperationException("immutable");
			}
		};
	}
	
	@Override
	public int hashCode() {
		return length;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof LowerTriangularMatrix) {
			final LowerTriangularMatrix other = (LowerTriangularMatrix)obj;
			return getLength() == other.getLength();
		}
		return false;
	}
	@Override
	public String toString() {
		return "Tril{" + getLength() + "}";
	}
	
}
