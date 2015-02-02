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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * A <code>AbstractCellRange</code> implements methods common to most 
 * {@link CellRange} implementations, such as the methods of the 
 * {@link RectangularRange}
 */
abstract public class AbstractCellRange extends AbstractRectangularRange implements CellRange {
	
	@Override
	public boolean contains(int bornColumn, int dieRow) {
		return dieRow == getDieRow() && bornColumn == getBornColumn();
	}
	public int getDieRowFrom() {
		return getDieRow();
	}
	public int getDieRowTo() {
		return getDieRow() + 1;
	}
	public int getBornColumnFrom() {
		return getBornColumn();
	}
	public int getBornColumnTo() {
		return getBornColumn() + 1;
	}
	
	/**
	 * Always returns one for a single cell
	 */
	@Override
	public int getCellCount() {
		return 1;
	}

	public <Col extends Column> AppendableMemory<Col> getForAppending(BornDieMatrix<Col> matrix) throws IllegalStateException, IOException {
		return matrix.getForAppending(getBornColumn(), getDieRow());
	}
	
	public <Col extends Column> SortableMemory<Col> getNegForGenerating(BornDieMatrix<Col> matrix) throws IllegalStateException, IOException {
		return matrix.getNegForGenerating(getBornColumn(), getDieRow());
	}
	public <Col extends Column> SortableMemory<Col> getPosForGenerating(BornDieMatrix<Col> matrix, int dieRow) throws IllegalStateException, IOException {
		return matrix.getPosForGenerating(getBornColumn(), getDieRow(), dieRow);
	}
	
	@Override
	public Iterator<CellRange> iterator() {
		return Collections.singleton((CellRange)this).iterator();
	}
	
	@Override
	public int hashCode() {
		return getBornColumn() & getDieRow();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof CellRange) {
			final CellRange other = (CellRange)obj;
			return getBornColumn() == other.getBornColumn() &&
				getDieRow() == other.getDieRow();
		}
		if (obj instanceof RectangularRange) {
			return super.equals(obj);
		}
		return false;
	}

	@Override
	public String toString() {
		return "[b=" + getBornColumn() + ", d=" + getDieRow() + "]";
	}

}
 