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
package ch.javasoft.math.array.parse;

import java.util.List;

import ch.javasoft.math.NumberMatrixConverter;

public class DefaultMatrixData implements MatrixData {
	private final DataType 	type;
	private final Object[]	matrix;
	public DefaultMatrixData(DataType type, List<? extends Object> values) {
		this.type	= type;
		this.matrix = type.toMatrix(values);
	}
	public DataType getDataType() {
		return type;
	}
	public int getRowCount() {
		return matrix.length;
	}
	public int getColumnCount() {
		return matrix.length == 0 ? 0 : type.getVectorLength(matrix[0]);
	}
	public Number getValue(int row, int col) {
		return type.getMatrixValue(matrix, row, col);
	}
	/**
	 * Casts the matrix into an array of the specified class and returns it. The 
	 * returned matrix is the internally kept matrix instance. Consider using 
	 * {@link #getMatrixConverted(NumberMatrixConverter)} instead.
	 * <p>
	 * The polyhedral cone is either defined in hyperplane (ine) or extreme 
	 * ray/vertex (ext) format. The returned matrix {@code M} is defined as
	 * follows:
	 * <ul>
	 * 	<li>ine: <tt>P = { b - Ax &ge; 0} = { Ax &leq; b }, M=[b -A]</tt></li>
	 * 	<li>ext: <tt>P = conv(v) + nonneg(r), M=[ v ; r ], v(*,0)=1, r(*,0)=0</tt></li>
	 * </ul>
	 * 
	 * @param	arrayClass	the class of the array into which the matrix is cast 
	 * @return the matrix {@code M} as described above
	 * @throws ClassCastException	if the matrix is not an array instance of 
	 * 								the specified class
	 *  
	 * @see #getMatrixConverted(NumberMatrixConverter)
	 */
	public <A> A[] getMatrixCast(Class<A> arrayClass) throws ClassCastException {
		return DataType.castMatrix(arrayClass, matrix);
	}
	/**
	 * Converts the matrix using the specified converter and returns it. The 
	 * returned matrix is always a new instance. Consider using 
	 * {@link #getMatrixCast(Class)} instead.
	 * <p>
	 * The polyhedral cone is either defined in hyperplane (ine) or extreme 
	 * ray/vertex (ext) format. The returned matrix {@code M} is defined as
	 * follows:
	 * <ul>
	 * 	<li>ine: <tt>P = { b - Ax &ge; 0} = { Ax &leq; b }, M=[b -A]</tt></li>
	 * 	<li>ext: <tt>P = conv(v) + nonneg(r), M=[ v ; r ], v(*,0)=1, r(*,0)=0</tt></li>
	 * </ul>
	 * 
	 * @param	converter	the converter used to create the matrix and to 
	 * 						convert the values into the desired format
	 * @return the matrix {@code M} as described above
	 *  
	 * @see #getMatrixCast(Class)
	 */
	public <A> A[] getMatrixConverted(NumberMatrixConverter<A> converter) throws ClassCastException {
		final int rows = matrix.length;
		final int cols = rows == 0 ? 0 : type.getVectorLength(matrix[0]);
		final A[] dst = converter.newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				final Number val = type.getMatrixValue(matrix, r, c);
				converter.copy(val, dst, r, c);
			}
		}
		return dst;
	}
	
	@Override
	public String toString() {
		final int rows = getRowCount();
		final int cols = getColumnCount();
		return rows + "x" + cols + " (" + type + ")"; 
	}
}