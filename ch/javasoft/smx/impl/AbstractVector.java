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
package ch.javasoft.smx.impl;

import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableVector;

/**
 * Abstract superclass with default implementations which might be common for
 * most vector classes for specific data types.
 */
abstract public class AbstractVector<N extends Number> implements ReadableVector<N>, WritableVector<N> {
	
	private final boolean	mIsColumnVector;
	
	public AbstractVector(boolean columnVector) {
		mIsColumnVector	= columnVector;
	}
	
	public boolean isColumnVector() {
		return mIsColumnVector;
	}
	public boolean isRowVector() {
		return !mIsColumnVector;
	}
	
	protected int getIndexForMatrixAccess(int row, int col) {
		if (isRowVector()) {
			if (row != 0) {
				throw new IndexOutOfBoundsException("row vector, row index must be 0, but was " + row);
			}
			return col;
		}
		if (col != 0) {
			throw new IndexOutOfBoundsException("column vector, column index must be 0, but was " + col);			
		}
		return row;
	}

	public int getColumnCount() {
		return isRowVector() ? getSize() : 1;
	}

	public int getRowCount() {
		return isColumnVector() ? getSize() : 1;
	}
	
	public void swapRows(int rowA, int rowB) {
		if (isRowVector()) {
			if (rowA != 0) throw new IndexOutOfBoundsException("row vector, row index must be 0, but was " + rowA);
			if (rowB != 0) throw new IndexOutOfBoundsException("row vector, row index must be 0, but was " + rowB);
			return;//nothing to swap
		}
		else {
			swapValues(rowA, rowB);
		}
	}
	public void swapColumns(int colA, int colB) {
		if (isColumnVector()) {
			if (colA != 0) throw new IndexOutOfBoundsException("column vector, column index must be 0, but was " + colA);
			if (colB != 0) throw new IndexOutOfBoundsException("column vector, column index must be 0, but was " + colB);
			return;//nothing to swap
		}
		else {
			swapValues(colA, colB);
		}
	}
	
	abstract public AbstractVector<N> transpose();
	
	@Override
	abstract public AbstractVector<N> clone();

}
