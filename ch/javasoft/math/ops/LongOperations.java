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
 * {@link NumberOperations Number operations} for {@link Long} numbers.
 * A singleton {@link #instance() instance} exists for this class. 
 */
public class LongOperations extends AbstractNumberOps<Long> {
	
	private static LongOperations sInstance;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static LongOperations instance() {
		if (sInstance == null) {
			sInstance = new LongOperations();
		}
		return sInstance;
	}
	public Class<Long> numberClass() {
		return Long.class;
	}
	@Override
	public Long abs(Long number) {
		return Long.valueOf(Math.abs(number.longValue()));
	}
	public Long add(Long numA, Long numB) {
		return Long.valueOf(numA.longValue() + numB.longValue());
	}
	public Long[] newArray(int size) {
		return new Long[size];
	}
	public Long[][] newArray(int rows, int cols) {
		return new Long[rows][cols];
	}
	public Long valueOf(String s) {
		return Long.valueOf(s);
	}
	public Long valueOf(Number number) {
		if (number.longValue() != number.doubleValue()) {
			throw new IllegalArgumentException("value not an integer value:  " + number);
		}
		return valueOf(number.longValue());
	}
	public Long valueOf(long value) {
		return Long.valueOf(value);
	}
	public Long valueOf(double value) {
		final long lvalue = (long)value;
		if (lvalue != value) {
			throw new IllegalArgumentException("value not convertable to long: " + value);
		}
		return Long.valueOf(lvalue);
	}
	public Long divide(Long numA, Long numB) {
		return Long.valueOf(numA.longValue() / numB.longValue());
	}
	@Override
	public Long invert(Long number) {
		long val = number.longValue();
		if (val == 1 || val == -1) return number;
		throw new IllegalArgumentException("inversion would not yield an integer: " + val);
	}
	@Override
	public boolean isOne(Long number) {
		return number.longValue() == 1;
	}
	@Override
	public boolean isZero(Long number) {
		return number.longValue() == 0;
	}
	@Override
	public boolean isNonZero(Long number) {
		return number.longValue() != 0;
	}
	@Override
	public boolean isNegative(Long number) {
		return number.longValue() < 0;
	}
	@Override
	public boolean isPositive(Long number) {
		return number.longValue() > 0;
	}
	public Long multiply(Long numA, Long numB) {
		return Long.valueOf(numA.longValue() * numB.longValue());
	}
	public Long negate(Long number) {
		return Long.valueOf(-number.longValue());
	}
	public Long reduce(Long number) {
		return number;
	}
	public Long[] reduceVector(boolean cloneOnChange, Long... vector) {
		final long gcd = Math.abs(IntegerUtil.gcd(vector));
		if (gcd != 0 && gcd != 1) {
			if (cloneOnChange) vector = vector.clone();
			for (int i = 0; i < vector.length; i++) {
				vector[i] = Long.valueOf(vector[i].longValue() / gcd);
			}
		}
		return vector;
	}
	public Long one() {
		return Long.valueOf(1);
	}
	public Long pow(Long numA, Long numB) {
		return Long.valueOf((long)Math.pow(numA.longValue(), numB.longValue()));
	}
	public int signum(Long number) {
		long val = number.longValue();
		return val < 0 ? -1 : val > 0 ? 1 : 0;
	}
	public Long subtract(Long numA, Long numB) {
		return Long.valueOf(numA.longValue() - numB.longValue());
	}
	public Long zero() {
		return Long.valueOf(0);
	}
	public int compare(Long o1, Long o2) {
		return o1.compareTo(o2);
	}
	public byte[] toByteArray(Long number) {
		//copied from DataOutputStream.writeLong()
		final long v = number.longValue();
		final byte[] bytes = new byte[8];
        bytes[0] = (byte)(v >>> 56);
        bytes[1] = (byte)(v >>> 48);
        bytes[2] = (byte)(v >>> 40);
        bytes[3] = (byte)(v >>> 32);
        bytes[4] = (byte)(v >>> 24);
        bytes[5] = (byte)(v >>> 16);
        bytes[6] = (byte)(v >>>  8);
        bytes[7] = (byte)(v >>>  0);
        return bytes;
	}
	public void writeTo(Long number, DataOutput out) throws IOException {
		out.writeLong(number.longValue());
	}
	public Long fromByteArray(byte[] bytes) {
		//copied from DataInputStream.readLong()
        return Long.valueOf(
        		((long)bytes[0] << 56) +
                ((long)(bytes[1] & 255) << 48) +
                ((long)(bytes[2] & 255) << 40) +
                ((long)(bytes[3] & 255) << 32) +
                ((long)(bytes[4] & 255) << 24) +
                ((bytes[5] & 255) << 16) +
                ((bytes[6] & 255) <<  8) +
                ((bytes[7] & 255) <<  0));		
	}
	public Long readFrom(DataInput in) throws IOException {
		return Long.valueOf(in.readLong());
	}
	public int byteLength() {
		return 8;
	}
}
