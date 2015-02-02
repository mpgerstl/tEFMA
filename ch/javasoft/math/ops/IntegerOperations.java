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

import ch.javasoft.math.NumberOperations;
import ch.javasoft.util.numeric.IntegerUtil;

/**
 * {@link NumberOperations Number operations} for {@link Integer} numbers.
 * A singleton {@link #instance() instance} exists for this class. 
 */
public class IntegerOperations extends AbstractNumberOps<Integer> {
	private static IntegerOperations sInstance;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static IntegerOperations instance() {
		if (sInstance == null) {
			sInstance = new IntegerOperations();
		}
		return sInstance;
	}
	public Class<Integer> numberClass() {
		return Integer.class;
	}
	@Override
	public Integer abs(Integer number) {
		return Integer.valueOf(Math.abs(number.intValue()));
	}
	public Integer add(Integer numA, Integer numB) {
		return Integer.valueOf(numA.intValue() + numB.intValue());
	}
	public Integer[] newArray(int size) {
		return new Integer[size];
	}
	public Integer[][] newArray(int rows, int cols) {
		return new Integer[rows][cols];
	}
	public Integer valueOf(String s) {
		return Integer.valueOf(s);
	}
	public Integer valueOf(Number number) {
		if (number.longValue() != number.doubleValue()) {
			throw new IllegalArgumentException("value not an integer value:  " + number);
		}
		return valueOf(number.longValue());
	}
	public Integer valueOf(long value) {
		if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
			throw new IllegalArgumentException("value out of integer range: " + value);
		}
		return Integer.valueOf((int)value);
	}
	public Integer valueOf(double value) {
		final int ivalue = (int)value;
		if (ivalue != value) {
			throw new IllegalArgumentException("value not convertable to integer: " + value);
		}
		return Integer.valueOf(ivalue);
	}
	public Integer divide(Integer numA, Integer numB) {
		return Integer.valueOf(numA.intValue() / numB.intValue());
	}
	@Override
	public Integer invert(Integer number) {
		int val = number.intValue();
		if (val == 1 || val == -1) return number;
		throw new IllegalArgumentException("inversion would not yield an integer: " + val);
	}
	@Override
	public boolean isOne(Integer number) {
		return number.intValue() == 1;
	}
	@Override
	public boolean isZero(Integer number) {
		return number.intValue() == 0;
	}
	public Integer multiply(Integer numA, Integer numB) {
		return Integer.valueOf(numA.intValue() * numB.intValue());
	}
	public Integer negate(Integer number) {
		return Integer.valueOf(-number.intValue());
	}
	public Integer reduce(Integer number) {
		return number;
	}
	public Integer[] reduceVector(boolean cloneOnChange, Integer... vector) {
		final int gcd = Math.abs(IntegerUtil.gcd(vector));
		if (gcd != 0 && gcd != 1) {
			if (cloneOnChange) vector = vector.clone();
			for (int i = 0; i < vector.length; i++) {
				vector[i] = Integer.valueOf(vector[i].intValue() / gcd);
			}
		}
		return vector;
	}
	public Integer one() {
		return Integer.valueOf(1);
	}
	public Integer pow(Integer numA, Integer numB) {
		return Integer.valueOf((int)Math.pow(numA.intValue(), numB.intValue()));
	}
	public int signum(Integer number) {
		int val = number.intValue();
		return val < 0 ? -1 : val > 0 ? 1 : 0;
	}
	public Integer subtract(Integer numA, Integer numB) {
		return Integer.valueOf(numA.intValue() - numB.intValue());
	}
	public Integer zero() {
		return Integer.valueOf(0);
	}
	public int compare(Integer o1, Integer o2) {
		return o1.compareTo(o2);
	}
	public byte[] toByteArray(Integer number) {
		//copied from DataOutputStream.writeInt()
		final int v = number.intValue();
		final byte[] bytes = new byte[4];
        bytes[0] = (byte)(v >>> 24);
        bytes[1] = (byte)(v >>> 16);
        bytes[2] = (byte)(v >>>  8);
        bytes[3] = (byte)(v >>>  0);
        return bytes;
	}
	public void writeTo(Integer number, DataOutput out) throws IOException {
		out.writeInt(number.intValue());
	}
	@SuppressWarnings("cast")
    public Integer fromByteArray(byte[] bytes) {
		//copied from DataInputStream.readLong()
        return Integer.valueOf(
            (((int)bytes[0]) << 24) +
            (((int)bytes[1]) << 16) +
            (((int)bytes[2]) <<  8) +
            (((int)bytes[3]) <<  0));		
	}
	public Integer readFrom(DataInput in) throws IOException {
		return Integer.valueOf(in.readInt());
	}
	public int byteLength() {
		return 4;
	}
}
