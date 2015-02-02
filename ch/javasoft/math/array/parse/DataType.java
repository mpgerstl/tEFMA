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

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

import ch.javasoft.math.BigFraction;

/**
 * The <code>DataType</code> defines the number type used to describe a 
 * polytope. It is generally {@link BigFraction}, but might also be 
 * {@link BigInteger}, or if small enough also {@code long} or {@code int}.
 */
public enum DataType {
	/**
	 * Indicates that fraction numbers are necessary to describe the polytope
	 */
	Fractional(BigFraction[].class) {
		@Override
		public BigFraction[] asVector(BigFraction[] vector) {
			return vector;
		}
		@Override
		public Void asLooserVector(DataType desired, Object vector) throws ClassCastException {
			throw new IllegalArgumentException(desired + " must be looser than " + this);
		}
		@Override
		public BigFraction getMatrixValue(Object[] matrix, int row, int col) {
			final BigFraction[][] mx = castMatrix(BigFraction[].class, matrix);
			return mx[row][col];
		}
		@Override
		public int getVectorLength(Object vector) throws ClassCastException {
			final BigFraction[] vec = castVector(BigFraction[].class, vector);
			return vec.length;
		}
	},
	/**
	 * Indicates that integers are sufficient to describe the polytope, but they
	 * are too large to be stored as {@code long} or {@code int}.
	 */
	BigInt(BigInteger[].class) {
		@Override
		public BigInteger[] asVector(BigFraction[] vector) {
			final BigInteger[] converted = new BigInteger[vector.length];
			for (int i = 0; i < converted.length; i++) {
				converted[i] = vector[i].toBigInteger(RoundingMode.UNNECESSARY);
			}
			return converted;
		}
		@Override
		public BigFraction[] asLooserVector(DataType desired, Object vector) throws ClassCastException {
			final BigInteger[] vals = castVector(BigInteger[].class, vector);
			if (DataType.Fractional.equals(desired)) {
				final BigFraction[] conv = new BigFraction[vals.length];
				for (int i = 0; i < conv.length; i++) {
					conv[i] = BigFraction.valueOf(vals[i]);
				}
				return conv;
			}
			throw new IllegalArgumentException(desired + " must be looser than " + this);
		}
		@Override
		public BigInteger getMatrixValue(Object[] matrix, int row, int col) {
			final BigInteger[][] mx = castMatrix(BigInteger[].class, matrix);
			return mx[row][col];
		}
		@Override
		public int getVectorLength(Object vector) throws ClassCastException {
			final BigInteger[] vec = castVector(BigInteger[].class, vector);
			return vec.length;
		}
	},
	/**
	 * Indicates that integers are sufficient to describe the polytope, but they
	 * are too large to be stored as {@code int}.
	 */
	Long(long[].class) {
		@Override
		public long[] asVector(BigFraction[] vector) {
			final long[] converted = new long[vector.length];
			for (int i = 0; i < converted.length; i++) {
				final BigInteger bigint = vector[i].toBigInteger(RoundingMode.UNNECESSARY);
				if (bigint.bitLength() >= 64) {
					throw new ArithmeticException("integer too large for long: " + bigint);
				}
				converted[i] = bigint.longValue();
			}
			return converted;
		}
		@Override
		public Number[] asLooserVector(DataType desired, Object vector) throws ClassCastException {
			final long[] vals = castVector(long[].class, vector);
			if (DataType.Fractional.equals(desired)) {
				final BigFraction[] conv = new BigFraction[vals.length];
				for (int i = 0; i < conv.length; i++) {
					conv[i] = BigFraction.valueOf(vals[i]);
				}
				return conv;
			}
			else if (DataType.BigInt.equals(desired)) {
				final BigInteger[] conv = new BigInteger[vals.length];
				for (int i = 0; i < conv.length; i++) {
					conv[i] = BigInteger.valueOf(vals[i]);
				}
				return conv;
			}
			throw new IllegalArgumentException(desired + " must be looser than " + this);
		}
		@Override
		public java.lang.Long getMatrixValue(Object[] matrix, int row, int col) {
			final long[][] mx = castMatrix(long[].class, matrix);
			return java.lang.Long.valueOf(mx[row][col]);
		}
		@Override
		public int getVectorLength(Object vector) throws ClassCastException {
			final long[] vec = castVector(long[].class, vector);
			return vec.length;
		}
	},
	/**
	 * Indicates that integers are sufficient to describe the polytope, and they
	 * are short enough to be stored as {@code int}.
	 */
	Int(int[].class) {
		@Override
		public int[] asVector(BigFraction[] vector) {
			final int[] converted = new int[vector.length];
			for (int i = 0; i < converted.length; i++) {
				final BigInteger bigint = vector[i].toBigInteger(RoundingMode.UNNECESSARY);
				if (bigint.bitLength() >= 32) {
					throw new ArithmeticException("integer too large for int: " + bigint);
				}
				converted[i] = bigint.intValue();
			}
			return converted;
		}
		@Override
		public Object asLooserVector(DataType desired, Object vector) throws ClassCastException {
			final int[] vals = castVector(int[].class, vector);
			if (DataType.Fractional.equals(desired)) {
				final BigFraction[] conv = new BigFraction[vals.length];
				for (int i = 0; i < conv.length; i++) {
					conv[i] = BigFraction.valueOf(vals[i]);
				}
				return conv;
			}
			else if (DataType.BigInt.equals(desired)) {
				final BigInteger[] conv = new BigInteger[vals.length];
				for (int i = 0; i < conv.length; i++) {
					conv[i] = BigInteger.valueOf(vals[i]);
				}
				return conv;
			}
			else if (DataType.Long.equals(desired)) {
				final long[] conv = new long[vals.length];
				for (int i = 0; i < conv.length; i++) {
					conv[i] = vals[i];
				}
				return conv;
			}
			throw new IllegalArgumentException(desired + " must be looser than " + this);
		}
		@Override
		public Integer getMatrixValue(Object[] matrix, int row, int col) {
			final int[][] mx = castMatrix(int[].class, matrix);
			return Integer.valueOf(mx[row][col]);
		}
		@Override
		public int getVectorLength(Object vector) throws ClassCastException {
			final int[] vec = castVector(int[].class, vector);
			return vec.length;
		}
	};
	
	private final Class vectorClass;
	private DataType(Class vectorClass) {
		this.vectorClass = vectorClass;
	}
	public Class getVectorClass() {
		return vectorClass;
	}
	abstract public Object asVector(BigFraction[] vector);
	public static DataType getTightestFit(BigFraction value) {
		if (value.isInteger()) {
			final BigInteger bi = value.toBigInteger();
			final int bitlen = bi.bitLength();
			if (bitlen >= 64) return DataType.BigInt;
			if (bitlen >= 32) return DataType.Long;
			return DataType.Int;
		}
		return DataType.Fractional;		
	}
	/**
	 * Returns the tightest {@link #Int} type
	 * 
	 * @see #isTighterThan(DataType)
	 * @see #getLoosestType()
	 */
	public static DataType getTightestType() {
		return values()[values().length - 1];
	}
	/**
	 * Returns the loosest {@link #Fractional} type
	 * 
	 * @see #isTighterThan(DataType)
	 * @see #getTightestType()
	 */
	public static DataType getLoosestType() {
		return values()[0];
	}
	/**
	 * Returns the loosest type of the given types.
	 * 
	 * @see #isTighterThan(DataType)
	 * @see #getLoosestType()
	 * @see #getTightestType()
	 */
	public static DataType getLoosestType(DataType... types) {
		int min = values().length;
		for (int i = 0; i < types.length; i++) {
			min = Math.min(min, types[i].ordinal());
		}
		return DataType.values()[min];
	}
	public boolean isTighterThan(DataType other) {
		return ordinal() > other.ordinal();
	}
	abstract public int getVectorLength(Object vector) throws ClassCastException;
	abstract public Number getMatrixValue(Object[] matrix, int row, int col);
	abstract public Object asLooserVector(DataType desired, Object vector) throws ClassCastException;
	public Object[] toMatrix(List<? extends Object> data) throws ArrayStoreException {
		final int rows = data.size();
		final int cols = rows == 0 ? 0 : getVectorLength(data.get(0));
		final Object[] matrix = (Object[])Array.newInstance(vectorClass.getComponentType(), new int[] {rows, cols});
		for (int r = 0; r < rows; r++) {
			matrix[r] = data.get(r);
		}
		return matrix;
	}
	@SuppressWarnings("unchecked")
	public static <A> A castVector(Class<A> arrayClass, Object vector) throws ClassCastException {
		if (arrayClass.isAssignableFrom(vector.getClass())) {
			return (A)vector;
		}
		throw new ClassCastException(arrayClass.getName() + ": " + vector); 
	}
	@SuppressWarnings("unchecked")
	public static <A> A[] castMatrix(Class<A> arrayClass, Object[] matrix) throws ClassCastException {
		if (arrayClass.isAssignableFrom(matrix.getClass().getComponentType())) {
			return (A[])matrix;
		}
		throw new ClassCastException(arrayClass.getName() + "[]: " + matrix); 
	}
	
}
