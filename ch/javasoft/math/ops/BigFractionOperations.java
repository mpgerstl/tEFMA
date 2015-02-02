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
import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;

/**
 * {@link NumberOperations Number operations} for {@link BigFraction} numbers.
 * A singleton {@link #instance() instance} exists for this class. 
 */
public class BigFractionOperations extends AbstractNumberOps<BigFraction> {
	
	private static BigFractionOperations sInstance;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static BigFractionOperations instance() {
		if (sInstance == null) {
			sInstance = new BigFractionOperations();
		}
		return sInstance;
	}
	public Class<BigFraction> numberClass() {
		return BigFraction.class;
	}
	public BigFraction[] newArray(int size) {
		return new BigFraction[size];
	}
	public BigFraction[][] newArray(int rows, int cols) {
		return new BigFraction[rows][cols];
	}
	public BigFraction valueOf(String s) {
		return BigFraction.valueOf(s);
	}
	public BigFraction valueOf(Number number) {
		return BigFraction.valueOf(number);
	}
	public BigFraction valueOf(long value) {
		return BigFraction.valueOf(value);
	}
	public BigFraction valueOf(double value) {
		return BigFraction.valueOf(value);
	}
	@Override
	public BigFraction abs(BigFraction number) {
		return number.abs();
	}
	public BigFraction add(BigFraction numA, BigFraction numB) {
		return numA.add(numB);
	}
	public BigFraction divide(BigFraction numA, BigFraction numB) {
		return numA.divide(numB);
	}
	@Override
	public BigFraction invert(BigFraction number) {
		return number.invert();
	}
	@Override
	public boolean isOne(BigFraction number) {
		return number.isOne();
	}
	@Override
	public boolean isZero(BigFraction number) {
		return number.isZero();
	}
	public BigFraction multiply(BigFraction numA, BigFraction numB) {
		return numA.multiply(numB);
	}
	public BigFraction negate(BigFraction number) {
		return number.negate();
	}
	public BigFraction one() {
		return BigFraction.ONE;
	}
	public BigFraction subtract(BigFraction numA, BigFraction numB) {
		return numA.subtract(numB);
	}
	public BigFraction zero() {
		return BigFraction.ZERO;
	}
	public int compare(BigFraction o1, BigFraction o2) {
		return o1.compareTo(o2);
	}
	public BigFraction reduce(BigFraction number) {
		return number.reduce();
	}
	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Note also that if the vector is NOT reduced, i.e. the current instance is
	 * returned, the individual entries might still be reducible. Otherwise,
	 * also individual positions are reduced. 
	 */
	public BigFraction[] reduceVector(boolean cloneOnChange, BigFraction... vector) {
		final BigFraction gcd = BigFraction.gcd(vector).abs();
		if (!(gcd.isOne() || gcd.isZero())) {
			if (cloneOnChange) vector = vector.clone();
			for (int i = 0; i < vector.length; i++) {
				vector[i] = vector[i].divide(gcd).reduce();
			}
		}
		return vector;
	}
	public int signum(BigFraction number) {
		return number.signum();
	}
	public BigFraction pow(BigFraction numA, BigFraction numB) {
		final int expSign = numB.signum();
		if (expSign == 0) return BigFraction.ONE;
		if (numA.isOne()) return BigFraction.ONE;
		numB = numB.reduce();
		if (numB.isInteger()) {
			if (expSign > 0 && numB.getNumerator().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
				throw new ArithmeticException(
					"exponent too large, only integer range supported: " + 
					numB.getNumerator() + " > " + Integer.MAX_VALUE
				);					
			}
			if (expSign < 0 && numB.getNumerator().compareTo(BigInteger.valueOf(Integer.MIN_VALUE + 1)) < 0) {
				throw new ArithmeticException(
					"exponent too small, only integer range supported: " + 
					numB.getNumerator() + " < " + Integer.MIN_VALUE
				);					
			}
			return numA.pow(numB.getNumerator().intValue());
		}
		throw new ArithmeticException("non-integer exponent not supported");
	}
	public byte[] toByteArray(BigFraction number) {
		final byte[] num = number.getNumerator().toByteArray();
		final byte[] den = number.getDenominator().toByteArray();
		final byte[] bytes = new byte[num.length + den.length + 4];
        bytes[0] = (byte)(num.length >>> 24);
        bytes[1] = (byte)(num.length >>> 16);
        bytes[2] = (byte)(num.length >>>  8);
        bytes[3] = (byte)(num.length >>>  0);
        System.arraycopy(num, 0, bytes, 4, num.length);
        System.arraycopy(den, 0, bytes, 4 + num.length, den.length);
        return bytes;
	}
	public void writeTo(BigFraction number, DataOutput out) throws IOException {
		final byte[] num = number.getNumerator().toByteArray();
		final byte[] den = number.getDenominator().toByteArray();
		out.writeInt(num.length);
		out.write(num);
		out.writeInt(den.length);
		out.write(den);
	}
	@SuppressWarnings("cast")
    public BigFraction fromByteArray(byte[] bytes) {
		final int numLen =
            (((int)bytes[0]) << 24) +
            (((int)bytes[1]) << 16) +
            (((int)bytes[2]) <<  8) +
            (((int)bytes[3]) <<  0);					
		final byte[] num = new byte[numLen];
		final byte[] den = new byte[bytes.length - numLen - 4];
		System.arraycopy(bytes, 4, num, 0, numLen);
		System.arraycopy(bytes, 4 + numLen, den, 0, den.length);
        return BigFraction.valueOf(new BigInteger(num), new BigInteger(den));
	}
	public BigFraction readFrom(DataInput in) throws IOException {
		final int numLen = in.readInt();
		final byte[] num = new byte[numLen];
		in.readFully(num);
		final int denLen = in.readInt();
		final byte[] den = new byte[denLen];
		in.readFully(den);
		return BigFraction.valueOf(new BigInteger(num), new BigInteger(den));
	}
	public int byteLength() {
		return -1;
	}
}
