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

/**
 * The <code>RectangularRange</code> represents a rectangle of cells.
 */
public interface RectangularRange extends Range {
	/**
	 * Returns the cell in the upper left corner of the rectangle
	 */
	CellRange getUpperLeft();
	/**
	 * Returns the cell in the lower left corner of the rectangle
	 */
	CellRange getLowerLeft();
	/**
	 * Returns the cell in the upper right corner of the rectangle
	 */
	CellRange getUpperRight();
	/**
	 * Returns the cell in the lower right corner of the rectangle
	 */
	CellRange getLowerRight();
	/**
	 * Returns the lowest column index of the rectangle, inclusive. The 
	 * {@link #getUpperLeft() upper left} and {@link #getUpperRight() upper right}
	 * cells are two samples with this column index.
	 */
	int getBornColumnFrom();
	/**
	 * Returns the highest column index of the rectangle, exclusive. The 
	 * {@link #getLowerLeft() lower left} and {@link #getLowerRight() lower right}
	 * cells are two samples with column index one below the value returned by
	 * this method.
	 */
	int getBornColumnTo();
	/**
	 * Returns the lowest row index of the rectangle, inclusive. The 
	 * {@link #getUpperLeft() upper left} and {@link #getLowerLeft() lower left}
	 * cells are two samples with this row index.
	 */
	int getDieRowFrom();
	/**
	 * Returns the highest row index of the rectangle, exclusive. The 
	 * {@link #getUpperRight() upper right} and {@link #getLowerRight() lower right}
	 * cells are two samples with row index one below the value returned by this
	 * method.
	 */
	int getDieRowTo();
	/**
	 * Returns the width of this rectangle, that is, 
	 * {@link #getBornColumnTo()} - {@link #getBornColumnFrom()}
	 */
	int getRowWidth();
	/**
	 * Returns the height of this rectangle, that is, 
	 * {@link #getDieRowTo()} - {@link #getDieRowFrom()}
	 */
	int getColumnHeight();
	/**
	 * Convert 2-dim index to 1-dim index. The rectangle's cells are column 
	 * packed, that is, a column can be accessed with successive indices.
	 * The reverse method is {@link #indexToCell(int)}
	 * 
	 * @param dieRow		the row index, allowed from {@link #getDieRowFrom()} 
	 * 						(inclusive) to {@link #getDieRowFrom()} (exclusive)
	 * @param bornColumn	the column index, allowed from 
	 * 						{@link #getBornColumnFrom()} (inclusive) to 
	 * 						{@link #getBornColumnTo()} (exclusive)
	 * @return the column-by-column packed one dimensional index
	 * @throws IndexOutOfBoundsException
	 */
	int cellToIndex(int bornColumn, int dieRow);
	/**
	 * Convert 1-dim index as returned by {@link #cellToIndex(int, int)} back 
	 * into the 2-dim row/column index of the rectangle.
	 * 
	 * @param index the packed index as returned by {@link #cellToIndex(int, int)}
	 * @return the single cell, that is, row and column index
	 * @throws IndexOutOfBoundsException if index is negative or larger or equal
	 * 									 to {@link #getCellCount()}
	 */
	CellRange indexToCell(int index) throws IndexOutOfBoundsException;
}
