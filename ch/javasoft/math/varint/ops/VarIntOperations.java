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
package ch.javasoft.math.varint.ops;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.javasoft.io.DataInputInputStream;
import ch.javasoft.io.DataOutputOutputStream;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.ops.AbstractNumberOps;
import ch.javasoft.math.varint.VarIntFactory;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.math.varint.VarIntUtil;
import ch.javasoft.math.varint.array.VarIntOperators;

/**
 * {@link NumberOperations Number operations} for {@link VarIntNumber}s.
 * A singleton {@link #instance() instance} exists for this class. 
 */
public class VarIntOperations extends AbstractNumberOps<VarIntNumber> {
	private static VarIntOperations sInstance;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static VarIntOperations instance() {
		if (sInstance == null) {
			sInstance = new VarIntOperations();
		}
		return sInstance;
	}
	public Class<VarIntNumber> numberClass() {
		return VarIntNumber.class;
	}
	public VarIntNumber[] newArray(int size) {
		return new VarIntNumber[size];
	}
	public VarIntNumber[][] newArray(int rows, int cols) {
		return new VarIntNumber[rows][cols];
	}
	public VarIntNumber valueOf(String s) {
		return VarIntFactory.create(s);
	}
	public VarIntNumber valueOf(Number number) {
		return VarIntOperators.INTEGER_DIVISION_INSTANCE.converter().operate(number);
	}
	public VarIntNumber valueOf(long value) {
		return VarIntFactory.create(value);
	}
	public VarIntNumber valueOf(double value) {
		return valueOf(String.valueOf(value));
	}
	@Override
	public VarIntNumber abs(VarIntNumber number) {
		return number.abs();
	}
	public VarIntNumber add(VarIntNumber numA, VarIntNumber numB) {
		return numA.add(numB);
	}
	public VarIntNumber divide(VarIntNumber numA, VarIntNumber numB) {
		return numA.divide(numB);
	}
	@Override
	public VarIntNumber invert(VarIntNumber number) {
		return VarIntNumber.ONE.divide(number);
	}
	@Override
	public boolean isOne(VarIntNumber number) {
		return VarIntNumber.ONE.equals(number);
	}
	@Override
	public boolean isZero(VarIntNumber number) {
		return number.signum() == 0;
	}
	public VarIntNumber multiply(VarIntNumber numA, VarIntNumber numB) {
		return numA.multiply(numB);
	}
	public VarIntNumber negate(VarIntNumber number) {
		return number.negate();
	}
	public VarIntNumber one() {
		return VarIntNumber.ONE;
	}
	public VarIntNumber subtract(VarIntNumber numA, VarIntNumber numB) {
		return numA.subtract(numB);
	}
	public VarIntNumber zero() {
		return VarIntNumber.ZERO;
	}
	public int compare(VarIntNumber o1, VarIntNumber o2) {
		return o1.compareTo(o2);
	}
	public VarIntNumber reduce(VarIntNumber number) {
		return number;
	}
	public VarIntNumber[] reduceVector(boolean cloneOnChange, VarIntNumber... vector) {
		if (vector.length > 0) {
			VarIntNumber gcd = VarIntUtil.gcd(vector).abs();
			if (!isOne(gcd) && gcd.signum() != 0) {
				if (cloneOnChange) vector = vector.clone();
				for (int i = 0; i < vector.length; i++) {
					vector[i] = vector[i].divide(gcd);
				}
			}
		}
		return vector;
	}
	public int signum(VarIntNumber number) {
		return number.signum();
	}
	public VarIntNumber pow(VarIntNumber numA, VarIntNumber numB) {
		final int expSign = numB.signum();
		if (expSign == 0) return VarIntNumber.ONE;
		if (VarIntNumber.ONE.equals(numA)) return VarIntNumber.ONE;
		if (expSign < 0) {
			//perform 'integer division'
			return VarIntNumber.ZERO;
		}
		if (numB.compareTo(VarIntFactory.create(Integer.MAX_VALUE)) > 0) {
			throw new ArithmeticException(
				"exponent too large, only integer range supported: " + 
				numB + " > " + Integer.MAX_VALUE
			);					
		}
		return numA.pow(numB.intValue());
	}
	public byte[] toByteArray(VarIntNumber number) {
		return number.toByteArray();
	}
	public void writeTo(VarIntNumber number, DataOutput out) throws IOException {
		number.writeTo(out instanceof OutputStream ? (OutputStream)out : new DataOutputOutputStream(out));
	}
	@SuppressWarnings("cast")
    public VarIntNumber fromByteArray(byte[] bytes) {
		return VarIntFactory.create(bytes);
	}
	public VarIntNumber readFrom(DataInput in) throws IOException {
		return VarIntFactory.readFrom(in instanceof InputStream ? (InputStream)in : new DataInputInputStream(in));
	}
	public int byteLength() {
		return -1;
	}
}
