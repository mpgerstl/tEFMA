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
package ch.javasoft.jsmat.variable;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import ch.javasoft.jsmat.common.MatClass;
import ch.javasoft.jsmat.common.MatType;
import ch.javasoft.jsmat.primitive.MatDouble;

public class MatDoubleMatrix extends MatMatrix<double[]> {
	
	private final MatDouble mDoubles;
	
	/**
	 * @param values		packed by column
	 * @param rowVector		true if values is row vector
	 */
	public MatDoubleMatrix(double[] values, boolean rowVector) {
		this(values, rowVector ? 1 : values.length, rowVector ? values.length : 1);
	}
	/**
	 * @param values	packed by column
	 * @param rows		the number of rows
	 * @param cols		the number of columns
	 */
	public MatDoubleMatrix(double[] values, int rows, int cols) {
		this(values, new int[] {rows, cols});
	}
	/**
	 * @param values	rows in 1st dim, columns in 2nd
	 */
	public MatDoubleMatrix(double[][] values) {
		this(to1dimArray(values), new int[] {values.length, values.length == 0 ? 0 : values[0].length});
	}
	/**
	 * @param values	packed by column
	 * @param dims		the dimensions
	 */
	public MatDoubleMatrix(double[] values, int[] dims) {
		super(MatClass.DOUBLE, MatType.DOUBLE, dims);
		mDoubles = new MatDouble(values);		
		checkValueLength(getMatrixSize());
	}
	
	@Override
	protected int getMatrixSize() {
		return mDoubles.getArrayLength();
	}
	
	/**
	 * @param rows	list contains the rows
	 */
	public static MatDoubleMatrix createMatrixFromRows(List<double[]> rows) {
		return new MatDoubleMatrix(to1dimArray(rows, true), new int[] {rows.size(), rows.size() == 0 ? 0 : rows.get(0).length});
	}
	public static MatDoubleMatrix createMatrixFromColumns(List<double[]> columns) {
		return new MatDoubleMatrix(to1dimArray(columns, false), new int[] {columns.size() == 0 ? 0 : columns.get(0).length, columns.size()});
	}

	private static double[] to1dimArray(double[][] values) {
		int rows = values.length;
		int cols = rows == 0 ? 0 : values[0].length;
		double[] arr = new double[rows * cols];
		for (int col = 0; col < cols; col++) {
			for (int row = 0; row < rows; row++) {
                arr[row + col*rows] = values[row][col]; 				
			}
		}
		return arr;
	}
	private static double[] to1dimArray(List<double[]> data, boolean rowsInList) {
		final int rows, cols;
		if (rowsInList) {
			rows = data.size();
			cols = rows == 0 ? 0 : data.get(0).length;			
		}
		else {
			cols = data.size();
			rows = cols == 0 ? 0 : data.get(0).length;			
		}
		double[] arr = new double[rows * cols];
		if (rowsInList) {
			for (int col = 0; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
	                arr[row + col*rows] = data.get(row)[col]; 				
				}
			}
		}
		else {
			for (int col = 0; col < cols; col++) {
				double[] dcol = data.get(col);
				System.arraycopy(dcol, 0, arr, col*rows, rows);
			}			
		}
		return arr;
	}

	@Override
	public int getRawDataSize() {
		return mDoubles.getSize();
	}

	@Override
	public void writeRawData(DataOutput out) throws IOException {
		mDoubles.write(out);
	}

}
