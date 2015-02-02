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
package ch.javasoft.cdd.parser;

import java.math.BigInteger;

import ch.javasoft.math.BigFraction;

public enum CddNumberFormat {
	integer {
		@Override
		public Class<BigInteger[]> getArrayClass() {
			return BigInteger[].class;
		}
		@Override
		public Class<BigInteger> getNumberClass() {
			return BigInteger.class;
		}
		@Override
		public double parseDouble(String str) {
			return new BigInteger(str).doubleValue();
		}
		@Override
		public BigInteger parseNumber(String str) {
			final BigInteger val = new BigInteger(str);
			if (BigInteger.ZERO.equals(val)) return BigInteger.ZERO; 
			if (BigInteger.ONE.equals(val)) return BigInteger.ONE; 
			if (BigInteger.TEN.equals(val)) return BigInteger.TEN;
			return val;
		}
		@Override
		public BigInteger[][] newMatrix(int rows, int cols) {
			return new BigInteger[rows][cols];
		}
		@Override
		public int getColumnCount(Object[] matrix) {
			final BigInteger[][] mx = ((BigInteger[][])matrix);
			return mx.length == 0 ? 0 : mx[0].length;
		}
		@Override
		public BigInteger getNumber(Object[] matrix, int row, int col) {
			return ((BigInteger[][])matrix)[row][col];
		}
		@Override
		public void parseAndSet(Object[] matrix, int row, int col, String value) {
			((BigInteger[][])matrix)[row][col] = parseNumber(value);
		}
	},
	rational {
		@Override
		public Class<BigFraction[]> getArrayClass() {
			return BigFraction[].class;
		}
		@Override
		public Class<BigFraction> getNumberClass() {
			return BigFraction.class;
		}
		@Override
		public double parseDouble(String str) {
			return BigFraction.valueOf(str).doubleValue();
		}			
		@Override
		public BigFraction parseNumber(String str) {
			return BigFraction.valueOf(str);
		}
		@Override
		public BigFraction[][] newMatrix(int rows, int cols) {
			return new BigFraction[rows][cols];
		}
		@Override
		public int getColumnCount(Object[] matrix) {
			final BigFraction[][] mx = ((BigFraction[][])matrix);
			return mx.length == 0 ? 0 : mx[0].length;
		}
		@Override
		public BigFraction getNumber(Object[] matrix, int row, int col) {
			return ((BigFraction[][])matrix)[row][col];
		}
		@Override
		public void parseAndSet(Object[] matrix, int row, int col, String value) {
			((BigFraction[][])matrix)[row][col] = parseNumber(value);
		}
	},
	real {
		@Override
		public Class<double[]> getArrayClass() {
			return double[].class;
		}
		@Override
		public Class<Double> getNumberClass() {
			return Double.TYPE;
		}
		@Override
		public double parseDouble(String str) {
			return Double.parseDouble(str);
		}			
		@Override
		public Double parseNumber(String str) {
			return Double.valueOf(str);
		}
		@Override
		public double[][] newMatrix(int rows, int cols) {
			return new double[rows][cols];
		}
		@Override
		public int getColumnCount(Object[] matrix) {
			final double[][] mx = ((double[][])matrix);
			return mx.length == 0 ? 0 : mx[0].length;
		}
		@Override
		public Double getNumber(Object[] matrix, int row, int col) {
			return Double.valueOf(((double[][])matrix)[row][col]);
		}
		@Override
		public void parseAndSet(Object[] matrix, int row, int col, String value) {
			((double[][])matrix)[row][col] = parseDouble(value);
		}
	};
	abstract public Class<?> getArrayClass();
	abstract public Class<? extends Number> getNumberClass();
	abstract public double parseDouble(String str);
	abstract public Number parseNumber(String str);
	abstract public Object[] newMatrix(int rows, int cols);
	abstract public int getColumnCount(Object[] matrix);
	abstract public Number getNumber(Object[] matrix, int row, int col);
	abstract public void parseAndSet(Object[] matrix, int row, int col, String value);

	@SuppressWarnings("unchecked")
	public static <A> A[] castMatrix(Class<A> arrayClass, Object[] matrix) throws ClassCastException {
		if (arrayClass.isAssignableFrom(matrix.getClass().getComponentType())) {
			return (A[])matrix;
		}
		throw new ClassCastException(arrayClass.getName() + "[]: " + matrix); 
	}
}
