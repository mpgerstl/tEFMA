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
package ch.javasoft.metabolic.efm.column;

import java.io.OutputStream;
import java.io.Writer;
import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.varint.VarIntFactory;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.math.varint.ops.VarIntOperations;
import ch.javasoft.smx.iface.BigIntegerMatrix;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.ops.MatrixOperations;

/**
 * The <code>VarIntMatrix</code> wraps around a {@link BigIntegerMatrix}
 */
public class VarIntMatrix implements ReadableBigIntegerMatrix<VarIntNumber>, WritableMatrix<VarIntNumber> {

	private final BigIntegerMatrix matrix; 
	
	public VarIntMatrix(BigIntegerMatrix matrix) {
		this.matrix = matrix;
	}

	public VarIntNumber[][] getNumberRows() {
		final int rows = getRowCount();
		final int cols = getColumnCount();
		final VarIntNumber[][] data = new VarIntNumber[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				data[r][c] = VarIntFactory.create(matrix.getBigIntegerValueAt(r, c));
			}
		}
		return data;
	}

	public VarIntNumber getNumberValueAt(int row, int col) {
		return VarIntFactory.create(matrix.getNumberValueAt(row, col));
	}

	public int getSignumAt(int row, int col) {
		return matrix.getSignumAt(row, col);
	}

	public WritableMatrix<VarIntNumber> newInstance(int rows, int cols) {
		return new VarIntMatrix(matrix.newInstance(rows, cols));
	}

	public WritableMatrix<VarIntNumber> newInstance(VarIntNumber[][] data, boolean rowsInDim1) {
		final int rows = data.length;
		final BigInteger[][] bdata = new BigInteger[rows][];
		for (int r = 0; r < data.length; r++) {
			final int cols = data[r].length;
			bdata[r] = new BigInteger[cols];
			for (int c = 0; c < cols; c++) {
				bdata[r][c] = data[r][c].toBigInteger();
			}
		}
		return new VarIntMatrix(matrix.newInstance(bdata, rowsInDim1));
	}

	public WritableMatrix<VarIntNumber> toWritableMatrix(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}

	public VarIntMatrix transpose() {
		return new VarIntMatrix(matrix.transpose());
	}

	public VarIntMatrix transposeR() {
		return new VarIntMatrix(matrix.transpose());
	}

	public VarIntMatrix transposeW() {
		return new VarIntMatrix(matrix.transpose());
	}

	public int getColumnCount() {
		return matrix.getColumnCount();
	}

	public MatrixOperations<VarIntNumber> getMatrixOperations() {
		//FIXME impl
		throw new RuntimeException("not implemented");
	}

	public NumberOperations<VarIntNumber> getNumberOperations() {
		return VarIntOperations.instance();
	}

	public int getRowCount() {
		return matrix.getRowCount();
	}

	public String toMultilineString() {
		return matrix.toMultilineString();
	}

	public void writeTo(Writer writer) {
		matrix.writeTo(writer);
	}

	public void writeTo(OutputStream out) {
		matrix.writeTo(out);
	}

	public void writeToMultiline(Writer writer) {
		matrix.writeToMultiline(writer);
	}

	public void writeToMultiline(OutputStream out) {
		matrix.writeToMultiline(out);
	}

	public void negate(int row, int col) {
		matrix.negate(row, col);
	}

	public void setValueAt(int row, int col, VarIntNumber value) {
                // cj: b
		// matrix.setValueAt(row, col, value.toBigInteger());
		matrix.setValueAt_BigInteger(row, col, value.toBigInteger());
                // cj: e
	}

	public void swapColumns(int colA, int colB) {
		matrix.swapColumns(colA, colB);
	}

	public void swapRows(int rowA, int rowB) {
		matrix.swapRows(rowA, rowB);
	}

	public ReadableMatrix<VarIntNumber> toReadableMatrix(boolean enforceNewInstance) {
		return enforceNewInstance ? clone() : this;
	}
	
	@Override
	public VarIntMatrix clone() {
		return new VarIntMatrix(matrix.clone());
	}

	public BigInteger[] getBigIntegerColumn(int col) {
		return matrix.getBigIntegerColumn(col);
	}

	public BigInteger[][] getBigIntegerColumns() {
		return matrix.getBigIntegerColumns();
	}

	public BigInteger[] getBigIntegerRow(int row) {
		return matrix.getBigIntegerRow(row);
	}

	public BigInteger[][] getBigIntegerRows() {
		return matrix.getBigIntegerRows();
	}
	public BigInteger getBigIntegerValueAt(int row, int col) {
		return matrix.getBigIntegerValueAt(row, col);
	}

	public ReadableBigIntegerMatrix subBigIntegerMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
		return matrix.subBigIntegerMatrix(rowStart, rowEnd, colStart, colEnd);
	}

	public void toArray(BigInteger[] array) {
		matrix.toArray(array);
	}

	public BigInteger[] toBigIntegerArray() {
		return matrix.toBigIntegerArray();
	}

	public BigIntegerMatrix toBigIntegerMatrix(boolean enforceNewInstance) {
		return matrix.toBigIntegerMatrix(enforceNewInstance);
	}

	public BigFraction getBigFractionValueAt(int row, int col) {
		return matrix.getBigFractionValueAt(row, col);
	}

	public BigInteger getBigIntegerDenominatorAt(int row, int col) {
		return matrix.getBigIntegerDenominatorAt(row, col);
	}

	public BigInteger getBigIntegerNumeratorAt(int row, int col) {
		return matrix.getBigIntegerNumeratorAt(row, col);
	}

	public BigIntegerRationalMatrix subBigIntegerRationalMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
		return matrix.subBigIntegerRationalMatrix(rowStart, rowEnd, colStart, colEnd);
	}

	public BigIntegerRationalMatrix toBigIntegerRationalMatrix(boolean enforceNewInstance) {
		return matrix.toBigIntegerRationalMatrix(enforceNewInstance);
	}
	public double[] getDoubleColumn(int col) {
		return matrix.getDoubleColumn(col);
	}

	public double[][] getDoubleColumns() {
		return matrix.getDoubleColumns();
	}

	public double[] getDoubleRow(int row) {
		return matrix.getDoubleRow(row);
	}

	public double[][] getDoubleRows() {
		return matrix.getDoubleRows();
	}

	public double getDoubleValueAt(int row, int col) {
		return matrix.getDoubleValueAt(row, col);
	}

	public DoubleMatrix subDoubleMatrix(int rowStart, int rowEnd, int colStart, int colEnd) {
		return matrix.subDoubleMatrix(rowStart, rowEnd, colStart, colEnd);
	}

	public void toArray(double[] array) {
		matrix.toArray(array);
	}

	public double[] toDoubleArray() {
		return matrix.toDoubleArray();
	}

	public DoubleMatrix toDoubleMatrix(boolean enforceNewInstance) {
		return matrix.toDoubleMatrix(enforceNewInstance);
	}

}
