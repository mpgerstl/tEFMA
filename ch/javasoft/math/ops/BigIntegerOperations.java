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

import ch.javasoft.math.NumberOperations;
import ch.javasoft.util.numeric.BigIntegerUtil;

/**
 * {@link NumberOperations Number operations} for {@link BigInteger} numbers.
 * A singleton {@link #instance() instance} exists for this class. 
 */
public class BigIntegerOperations extends AbstractNumberOps<BigInteger> {
	private static BigIntegerOperations sInstance;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static BigIntegerOperations instance() {
		if (sInstance == null) {
			sInstance = new BigIntegerOperations();
		}
		return sInstance;
	}
	public Class<BigInteger> numberClass() {
		return BigInteger.class;
	}
	public BigInteger[] newArray(int size) {
		return new BigInteger[size];
	}
	public BigInteger[][] newArray(int rows, int cols) {
		return new BigInteger[rows][cols];
	}
	public BigInteger valueOf(String s) {
		return new BigInteger(s);
	}
	public BigInteger valueOf(Number number) {
		return BigIntegerUtil.valueOf(number);
	}
	public BigInteger valueOf(long value) {
		return BigInteger.valueOf(value);
	}
	public BigInteger valueOf(double value) {
		return valueOf(String.valueOf(value));
	}
	@Override
	public BigInteger abs(BigInteger number) {
		return number.abs();
	}
	public BigInteger add(BigInteger numA, BigInteger numB) {
		return numA.add(numB);
	}
	public BigInteger divide(BigInteger numA, BigInteger numB) {
		return numA.divide(numB);
	}
	@Override
	public BigInteger invert(BigInteger number) {
		return BigInteger.ONE.divide(number);
	}
	@Override
	public boolean isOne(BigInteger number) {
		return BigInteger.ONE.equals(number);
	}
	@Override
	public boolean isZero(BigInteger number) {
		return number.signum() == 0;
	}
	public BigInteger multiply(BigInteger numA, BigInteger numB) {
		return numA.multiply(numB);
	}
	public BigInteger negate(BigInteger number) {
		return number.negate();
	}
	public BigInteger one() {
		return BigInteger.ONE;
	}
	public BigInteger subtract(BigInteger numA, BigInteger numB) {
		return numA.subtract(numB);
	}
	public BigInteger zero() {
		return BigInteger.ZERO;
	}
	public int compare(BigInteger o1, BigInteger o2) {
		return o1.compareTo(o2);
	}
	public BigInteger reduce(BigInteger number) {
		return number;
	}
	public BigInteger[] reduceVector(boolean cloneOnChange, BigInteger... vector) {
		final BigInteger gcd = BigIntegerUtil.gcd(vector).abs();
		if (!isOne(gcd) && gcd.signum() != 0) {
			if (cloneOnChange) vector = vector.clone();
			for (int i = 0; i < vector.length; i++) {
				vector[i] = vector[i].divide(gcd);
			}
		}
		return vector;
	}
	public int signum(BigInteger number) {
		return number.signum();
	}
	public BigInteger pow(BigInteger numA, BigInteger numB) {
		final int expSign = numB.signum();
		if (expSign == 0) return BigInteger.ONE;
		if (BigInteger.ONE.equals(numA)) return BigInteger.ONE;
		if (expSign < 0) {
			//perform 'integer division'
			return BigInteger.ZERO;
		}
		if (numB.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
			throw new ArithmeticException(
				"exponent too large, only integer range supported: " + 
				numB + " > " + Integer.MAX_VALUE
			);					
		}
		return numA.pow(numB.intValue());
	}
	public byte[] toByteArray(BigInteger number) {
		return number.toByteArray();
	}
	public void writeTo(BigInteger number, DataOutput out) throws IOException {
		final byte[] num = number.toByteArray();
		out.writeInt(num.length);
		out.write(num);
	}
	@SuppressWarnings("cast")
    public BigInteger fromByteArray(byte[] bytes) {
		return new BigInteger(bytes);
	}
	public BigInteger readFrom(DataInput in) throws IOException {
		final int numLen = in.readInt();
		final byte[] num = new byte[numLen];
		in.readFully(num);
		return new BigInteger(num);
	}
	public int byteLength() {
		return -1;
	}
}
