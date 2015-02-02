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

/**
 * {@link NumberOperations Number operations} for {@link Double} numbers.
 * A singleton {@link #instance() instance} exists for this class. 
 */
public class DoubleOperations extends AbstractNumberOps<Double> {
	
	private static DoubleOperations sInstance;
	
	/**
	 * Returns the singleton instance
	 * @return the singleton instance
	 */
	public static DoubleOperations instance() {
		if (sInstance == null) {
			sInstance = new DoubleOperations();
		}
		return sInstance;
	}
	public Class<Double> numberClass() {
		return Double.class;
	}
	public Double[] newArray(int size) {
		return new Double[size];
	}
	public Double[][] newArray(int rows, int cols) {
		return new Double[rows][cols];
	}
	public Double valueOf(String s) {
		return Double.valueOf(s);
	}
	public Double valueOf(Number number) {
		return Double.valueOf(number.doubleValue());
	}
	public Double valueOf(long value) {
		return Double.valueOf(value);
	}
	public Double valueOf(double value) {
		return Double.valueOf(value);
	}
	@Override
	public Double abs(Double number) {
		return Double.valueOf(Math.abs(number.doubleValue()));
	}
	public Double add(Double numA, Double numB) {
		return Double.valueOf(numA.doubleValue() + numB.doubleValue());
	}
	public Double divide(Double numA, Double numB) {
		return Double.valueOf(numA.doubleValue() / numB.doubleValue());
	}
	@Override
	public Double invert(Double number) {
		return Double.valueOf(1d / number.doubleValue());
	}
	@Override
	public boolean isOne(Double number) {
		return number.doubleValue() == 1d;
	}
	@Override
	public boolean isZero(Double number) {
		return number.doubleValue() == 0d;
	}
	public Double multiply(Double numA, Double numB) {
		return Double.valueOf(numA.doubleValue() * numB.doubleValue());
	}
	public Double negate(Double number) {
		return Double.valueOf(-number.doubleValue());
	}
	public Double reduce(Double number) {
		return number;
	}
	public Double[] reduceVector(boolean cloneOnChange, Double... vector) {
		return vector;
	}
	public Double one() {
		return Double.valueOf(1d);
	}
	public Double pow(Double numA, Double numB) {
		return Double.valueOf(Math.pow(numA.doubleValue(), numB.doubleValue()));
	}
	public int signum(Double number) {
		return (int)Math.signum(number.doubleValue());
	}
	public Double subtract(Double numA, Double numB) {
		return Double.valueOf(numA.doubleValue() - numB.doubleValue());
	}
	public Double zero() {
		return Double.valueOf(0d);
	}
	public int compare(Double o1, Double o2) {
		return o1.compareTo(o2);
	}
	public byte[] toByteArray(Double number) {
		//copied from DataOutputStream.writeLong()
		final long v = Double.doubleToLongBits(number.doubleValue());
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
	public void writeTo(Double number, DataOutput out) throws IOException {
		out.writeDouble(number.doubleValue());
	}
	public Double fromByteArray(byte[] bytes) {
		//copied from DataInputStream.readLong()
        final long longBits = 
    		((long)bytes[0] << 56) +
            ((long)(bytes[1] & 255) << 48) +
            ((long)(bytes[2] & 255) << 40) +
            ((long)(bytes[3] & 255) << 32) +
            ((long)(bytes[4] & 255) << 24) +
            ((bytes[5] & 255) << 16) +
            ((bytes[6] & 255) <<  8) +
            ((bytes[7] & 255) <<  0);
        return Double.valueOf(Double.longBitsToDouble(longBits));
	}
	public Double readFrom(DataInput in) throws IOException {
		return Double.valueOf(in.readDouble());
	}
	public int byteLength() {
		return 8;
	}
}
