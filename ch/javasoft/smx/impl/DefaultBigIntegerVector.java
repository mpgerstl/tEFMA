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

import ch.javasoft.math.BigFraction;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableVector;
import ch.javasoft.smx.iface.WritableVector;

/**
 * The <code>DefaultBigIntegerVector</code> ... TODO javadoc-DefaultBigIntegerVector-type
 * 
 */
public class DefaultBigIntegerVector extends DefaultBigIntegerRationalMatrix implements ReadableVector<BigFraction>, WritableVector<BigFraction>,
	BigIntegerRationalMatrix {
	
	private final boolean isColumnVector;

	public DefaultBigIntegerVector(int size, boolean isColumnVector) {
		super(isColumnVector ? size : 1, isColumnVector ? 1 : size);
		this.isColumnVector = isColumnVector;
	}
	/**
	 * Constructor, does not clone values!
	 */
	public DefaultBigIntegerVector(BigFraction[] values, boolean isColumnVector) {
		super(new BigFraction[][] {values}, !isColumnVector);
		this.isColumnVector = isColumnVector;
	}
	
	@Override
	public DefaultBigIntegerVector clone() {
		final int size = getSize();
		final DefaultBigIntegerVector clone = new DefaultBigIntegerVector(size, isColumnVector);
		for (int i = 0; i < size; i++) {
			clone.setValueAt(i, getNumberValueAt(i));
		}
		return clone;
	}

	@Override
	public DefaultBigIntegerVector transpose() {
		final int size = getSize();
		final DefaultBigIntegerVector clone = new DefaultBigIntegerVector(size, !isColumnVector);
		for (int i = 0; i < size; i++) {
			clone.setValueAt(i, getNumberValueAt(i));
		}
		return clone;
	}

	public BigFraction getNumberValueAt(int index) {
		return isColumnVector ? getNumberValueAt(index, 0) : getNumberValueAt(0, index);
	}

	public ReadableVector<BigFraction> toReadableVector(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}
	public WritableVector<BigFraction> toWritableVector(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}

	public int getSize() {
		return getColumnCount() * getRowCount();
	}
	public boolean isColumnVector() {
		return isColumnVector;
	}
	public boolean isRowVector() {
		return !isColumnVector;
	}
	public void setValueAt(int index, BigFraction value) {
		if (isColumnVector) {
			setValueAt(index, 0, value);
		}
		else {
			setValueAt(0, index, value);
		}
	}
	public void swapValues(int indexA, int indexB) {
		final BigFraction valA = getNumberValueAt(indexA);
		setValueAt(indexA, getNumberValueAt(indexB));
		setValueAt(indexB, valA);
	}

}
