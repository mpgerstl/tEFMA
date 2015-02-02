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

/**
 * The <code>AbstractRectangularRange</code> implements common stuff for most
 * {@link RectangularRange} implementations.
 */
abstract public class AbstractRectangularRange implements RectangularRange {

	public boolean contains(CellRange cell) {
		return contains(cell.getBornColumn(), cell.getDieRow());
	}
	public boolean contains(int bornColumn, int dieRow) {
		return 
			dieRow >= getDieRowFrom() && bornColumn >= getBornColumnFrom() &&
			dieRow < getDieRowTo() && bornColumn < getBornColumnTo();
	}
	public CellRange getUpperLeft() {
		return new DefaultCellRange(getBornColumnFrom(), getDieRowFrom());
	}
	public CellRange getUpperRight() {
		return new DefaultCellRange(getBornColumnTo() - 1, getDieRowFrom());
	}
	public CellRange getLowerLeft() {
		return new DefaultCellRange(getBornColumnTo() - 1, getDieRowFrom());
	}
	public CellRange getLowerRight() {
		return new DefaultCellRange(getBornColumnTo() - 1, getDieRowTo() - 1);
	}
	public int getRowWidth() {
		return getBornColumnTo() - getBornColumnFrom();
	}
	public int getColumnHeight() {
		return getDieRowTo() - getDieRowFrom();
	}
	public int getCellCount() {
		return getRowWidth() * getColumnHeight();
	}
	/**
	 * Returns a {@link ForwardRowColumnRectangleIterator}
	 */
	public Iterator<CellRange> iterator() {
		return new ForwardRowColumnRectangleIterator(this);
	}
	public int cellToIndex(int bornColumn, int dieRow) {
		if (dieRow < getDieRowFrom()) throw new IndexOutOfBoundsException("row below start row: " + dieRow + "<" + getDieRowFrom());
		if (bornColumn < getBornColumnFrom()) throw new IndexOutOfBoundsException("column below start column: " + bornColumn + "<" + getBornColumnFrom());
		if (dieRow >= getDieRowTo()) throw new IndexOutOfBoundsException("row after last row: " + dieRow + ">=" + getDieRowTo());
		if (bornColumn >= getBornColumnTo()) throw new IndexOutOfBoundsException("column after last column: " + bornColumn + ">=" + getBornColumnTo());
		return (bornColumn - getBornColumnFrom()) * getColumnHeight() + (dieRow - getDieRowFrom()); 
	}
	public CellRange indexToCell(int index) throws IndexOutOfBoundsException {
		if (index < 0) throw new IndexOutOfBoundsException("negative index: " + index);
		if (index >= getCellCount()) throw new IndexOutOfBoundsException("index after last cell: " + index + ">=" + getCellCount());
		return new DefaultCellRange(
			getBornColumnFrom() + index / getColumnHeight(),
			getDieRowFrom() + index % getColumnHeight()			
		);
	}
	@Override
	public int hashCode() {
		return getUpperLeft().hashCode() ^ getLowerRight().hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof RectangularRange) {
			final RectangularRange other = (RectangularRange)obj;
			return getUpperLeft().equals(other.getUpperLeft()) &&
				getLowerRight().equals(other.getLowerRight());
		}
		return false;
	}
	@Override
	public String toString() {
		return "Rect{" + getUpperLeft() + ", " + getLowerRight() + "}";
	}
}
