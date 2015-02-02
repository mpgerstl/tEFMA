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
package ch.javasoft.metabolic.efm;

import ch.javasoft.cdd.parser.CddParser;
import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberMatrixConverter;

/**
 * The <code>CddHelper</code> contains helper methods for the cdd tests
 */
public class CddHelper extends AnneMatthias {
	
	/**
	 * Returns the parsed cdd matrix as big fraction 2d array
	 */
	public static BigFraction[][] getMatrix(CddParser parser) {
		try {
			return parser.getMatrixCast(BigFraction[].class);
		}
		catch (ClassCastException e) {
			return parser.getMatrixConverted(new NumberMatrixConverter<BigFraction[]>() {
				public BigFraction[][] newMatrix(int rows, int cols) {
					return new BigFraction[rows][cols];
				}				
				public void copy(Number number, BigFraction[][] dstMatrix, int row, int col) {
					dstMatrix[row][col] = BigFraction.valueOf(number);
				}
			});
			
		}
	}
	
	/**
	 * Adds the slack variables to the given matrix
	 * 
	 * @param mx	the original matrix without slack variables
	 * @return {@code [ mx , -I ]}
	 */
	public static BigFraction[][] addSlackVariables(BigFraction[][] mx) {
		final int rows = mx.length;
		final int cols = rows == 0 ? 0 : mx[0].length;
		final BigFraction[][] res = new BigFraction[rows][cols + rows];
		final BigFraction minusOne = BigFraction.ONE.negate();
		for (int r = 0; r < rows; r++) {
			System.arraycopy(mx[r], 0, res[r], 0, cols);
			for (int c = 0; c < rows; c++) {
				res[r][c + cols] = r == c ? minusOne : BigFraction.ZERO;
			}
		}
		return res;
	}

}
