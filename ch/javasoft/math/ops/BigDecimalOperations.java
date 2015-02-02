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
package ch.javasoft.math.ops;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;

/**
 * {@link NumberOperations Number operations} for {@link BigDecimal} numbers.
 * The instantiation of a {@code BigDecimalOperations} requires a 
 * {@link MathContext} which defines rounding.
 */
public class BigDecimalOperations extends AbstractNumberOps<BigDecimal> {
	
	private final MathContext mathContext;
	
	/**
	 * Constructor for <code>BigDecimalOperations</code> with specified math
	 * context defining rounding
	 */
	public BigDecimalOperations(MathContext mathContext) { 
		this.mathContext = mathContext;
	}
    /**
     * Constructs a new {@code BigDecimalOperations} with the specified
     * precision and the {@link RoundingMode#HALF_UP HALF_UP} rounding
     * mode.
     *
     * @param setPrecision The non-negative {@code int} precision setting.
     * @throws IllegalArgumentException if the {@code setPrecision} parameter is 
     * 			less than zero.
     */
    public BigDecimalOperations(int setPrecision) {
        this(new MathContext(setPrecision));
    }
    /**
     * Constructs a new {@code BigDecimalOperations} with {@code MathContext} 
     * from a string.
     *
     * The string must be in the same format as that produced by the
     * {@link MathContext#toString} method.
     * 
     * <p>An {@code IllegalArgumentException} is thrown if the precision
     * section of the string is out of range ({@code < 0}) or the string is
     * not in the format created by the {@link #toString} method.
     *
     * @param contextVal The string to be parsed
     * @throws IllegalArgumentException if the precision section is out of range
     * or of incorrect format
     * @throws NullPointerException if the argument is {@code null}
     */
    public BigDecimalOperations(String contextVal) {
    	this(new MathContext(contextVal));
    }
	
	/**
	 * Returns the math context defined when instantiating this 
	 * {@code BigDecimalOperations}
	 */
	public MathContext getMathContext() {
		return mathContext;
	}
	
	public Class<BigDecimal> numberClass() {
		return BigDecimal.class;
	}
	public BigDecimal[] newArray(int size) {
		return new BigDecimal[size];
	}
	public BigDecimal[][] newArray(int rows, int cols) {
		return new BigDecimal[rows][cols];
	}
	public BigDecimal valueOf(String s) {
		return new BigDecimal(s);
	}
	public BigDecimal valueOf(Number number) {
		if (number instanceof BigDecimal) {
			return (BigDecimal)number;
		}
		if (number instanceof BigInteger) {
			return new BigDecimal((BigInteger)number);
		}
		if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte) {
			return BigDecimal.valueOf(number.longValue());
		}
		if (number instanceof Double || number instanceof Float) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		if (number instanceof BigFraction) {
			return ((BigFraction)number).toBigDecimal(mathContext);
		}
		return valueOf(number.toString());
	}
	public BigDecimal valueOf(long value) {
		return BigDecimal.valueOf(value);
	}
	public BigDecimal valueOf(double value) {
		return BigDecimal.valueOf(value);
	}
	@Override
	public BigDecimal abs(BigDecimal number) {
		return number.abs(mathContext);
	}
	public BigDecimal add(BigDecimal numA, BigDecimal numB) {
		return numA.add(numB, mathContext);
	}
	public BigDecimal divide(BigDecimal numA, BigDecimal numB) {
		return numA.divide(numB, mathContext);
	}
	@Override
	public BigDecimal invert(BigDecimal number) {
		return divide(BigDecimal.ONE, number);
	}
	@Override
	public boolean isOne(BigDecimal number) {
		return BigDecimal.ONE.equals(number);
	}
	public BigDecimal multiply(BigDecimal numA, BigDecimal numB) {
		return numA.multiply(numB, mathContext);
	}
	public BigDecimal negate(BigDecimal number) {
		return number.negate(mathContext);
	}
	public BigDecimal one() {
		return BigDecimal.ONE;
	}
	public BigDecimal subtract(BigDecimal numA, BigDecimal numB) {
		return numA.subtract(numB, mathContext);
	}
	public BigDecimal zero() {
		return BigDecimal.ZERO;
	}
	public int compare(BigDecimal o1, BigDecimal o2) {
		return o1.compareTo(o2);
	}
	/**
	 * Returns the rounded number according to the current 
	 * {@link #getMathContext() math context}, that is, 
	 * {@link BigDecimal#round(MathContext)} is called
	 */
	public BigDecimal reduce(BigDecimal number) {
		return number.round(mathContext);
	}
	/**
	 * Returns the unchanged vector
	 */
	public BigDecimal[] reduceVector(boolean cloneOnChange, BigDecimal... vector) {
		return vector;
	}
	public int signum(BigDecimal number) {
		return number.signum();
	}
	public BigDecimal pow(BigDecimal numA, BigDecimal numB) {
		final int expSign = numB.signum();
		if (expSign == 0) return BigDecimal.ONE;
		if (isOne(numA)) return BigDecimal.ONE;
		try {
			final BigInteger inumB = numB.toBigIntegerExact();
			if (expSign > 0 && inumB.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
				throw new ArithmeticException(
					"exponent too large, only integer range supported: " + 
					inumB + " > " + Integer.MAX_VALUE
				);					
			}
			if (expSign < 0 && inumB.compareTo(BigInteger.valueOf(Integer.MIN_VALUE + 1)) < 0) {
				throw new ArithmeticException(
					"exponent too small, only integer range supported: " + 
					inumB + " < " + Integer.MIN_VALUE
				);					
			}
			return numA.pow(inumB.intValue());
		}
		catch (ArithmeticException e) {
			throw new ArithmeticException("non-integer exponent not supported");
		}
	}
	public byte[] toByteArray(BigDecimal number) {
		final int scale = number.scale();
		final BigInteger unscaled = number.unscaledValue();
		final byte[] value = unscaled.toByteArray();
		final byte[] bytes = new byte[value.length + 4];
        bytes[0] = (byte)(scale >>> 24);
        bytes[1] = (byte)(scale >>> 16);
        bytes[2] = (byte)(scale >>>  8);
        bytes[3] = (byte)(scale >>>  0);
        System.arraycopy(value, 0, bytes, 4, value.length);
        return bytes;
	}
	public void writeTo(BigDecimal number, DataOutput out) throws IOException {
		final byte[] bytes = number.unscaledValue().toByteArray();
		out.writeInt(number.scale());
		out.writeInt(bytes.length);
		out.write(bytes);
	}
	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Returns the value as stored, without applying rounding according to the
	 * {@link #getMathContext() math context}.
	 */
	@SuppressWarnings("cast")
    public BigDecimal fromByteArray(byte[] bytes) {
		final int scale =
            (((int)bytes[0]) << 24) +
            (((int)bytes[1]) << 16) +
            (((int)bytes[2]) <<  8) +
            (((int)bytes[3]) <<  0);					
		final byte[] vals = new byte[bytes.length - 4];
		System.arraycopy(bytes, 4, vals, 0, vals.length);
		final BigInteger unscaled = new BigInteger(bytes);
        return new BigDecimal(unscaled, scale);
	}
	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Returns the value as stored, without applying rounding according to the
	 * {@link #getMathContext() math context}.
	 */
	public BigDecimal readFrom(DataInput in) throws IOException {
		final int scale 	= in.readInt();
		final int length	= in.readInt();
		final byte[] value = new byte[length];
		in.readFully(value);
		final BigInteger unscaled = new BigInteger(value);
		return new BigDecimal(unscaled, scale);
	}
	public int byteLength() {
		return -1;
	}
}
