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
package ch.javasoft.math;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Comparator;

/**
 * Number operations is a collection of arithmetic and other operations 
 * usually supported by different number types.
 */
public interface NumberOperations<N extends Number> extends Comparator<N> {
	/** The class representing numbers of this number operations instance*/
	Class<N> numberClass();
	/** Create a new array of the underlying number type*/
	N[] newArray(int size);
	/** Create a new 2 dimensional array of the underlying number type*/
	N[][] newArray(int rows, int cols);
	/** 
	 * Create an instance of the underlying number type based on the 
	 * given string representation of the number
	 * 
	 * @throws NumberFormatException If converting the string to a number fails
	 */
	N valueOf(String s) throws NumberFormatException;
	/** 
	 * Create an instance of the underlying number type based on the 
	 * submitted number
	 * 
	 * @throws IllegalArgumentException If converting into the underlying number 
	 * 									type fails or is not supported
	 */
	N valueOf(Number n) throws IllegalArgumentException;
	/** 
	 * Create an instance of the underlying number type based on the 
	 * submitted long integer value
	 * 
	 * @throws IllegalArgumentException If converting into the underlying number 
	 * 									type fails or is not supported
	 */
	N valueOf(long value) throws IllegalArgumentException;
	/** 
	 * Create an instance of the underlying number type based on the 
	 * submitted double value
	 * 
	 * @throws IllegalArgumentException If converting into the underlying number 
	 * 									type fails or is not supported
	 */
	N valueOf(double value) throws IllegalArgumentException;
	/** 
	 * Returns the absolute value of the given number, i.e. a non-negative
	 * value <tt>abs(number)</tt>
	 */
	N abs(N number);
	/** Returns the negated value, i.e. <tt>-number</tt> */
	N negate(N number);
	/** Returns the sum of the two values, i.e. <tt>numA + numB</tt> */
	N add(N numA, N numB);
	/** Returns the difference of the two values, i.e. <tt>numA - numB</tt> */
	N subtract(N numA, N numB);
	/** Returns the product of the two values, i.e. <tt>numA * numB</tt> */
	N multiply(N numA, N numB);
	/** 
	 * Returns the ratio of the two values, i.e. <tt>numA / numB</tt>.
	 * <p>
	 * For integer types, an integer division is performed, ignoring possible
	 * remainders
	 */
	N divide(N numA, N numB);
	/** 
	 * Returns the inverted value, i.e. <tt>1 / number</tt>
	 * 
	 * @throws IllegalArgumentException If inversion of the value leads to a number
	 * 									which cannot be converted to the underlying 
	 * 									number type 
	 */
	N invert(N number) throws IllegalArgumentException;
	/** 
	 * Returns the powered value, i.e. <tt>numA<sup>numB</sup></tt>.
	 * Negative exponents are allowed, also for integer types, without causing
	 * an exception. E.g. 3^-1 yields 0.
	 */
	N pow(N numA, N numB);
	/** Returns the value representing zero */
	N zero();
	/** Returns the value representing one */
	N one();
	/**
	 * Converts the number into some reduced form. This can for instance mean 
	 * reduction for fraction numbers. If no reduction can be made, the number
	 * is returned unchanged.
	 */
	N reduce(N number);
	/**
	 * Converts a vector into some reduced form. For integer or fraction number
	 * vectors, this can for instance mean that the vector is shortened by the
	 * greatest common divisor of all vector components. 
	 * <p>
	 * Note that the vector length might change by this operation. If only 
	 * individual components of the vector should be reduced, which is not
	 * affecting the vector's length, {@link #reduce(Number) reduce} each number
	 * separately.
	 * 
	 * @param cloneOnChange	if a reduction can be made, the array is cloned 
	 * 						and the original array is not changed. If no 
	 * 						reduction can be made, or if <tt>cloneOnChange</tt>
	 * 						is false, the original array will be returned,
	 * 						unmodified in the former case, and with reduced
	 * 						values in the latter case.
	 * @param vector		the vector to reduce
	 */
	N[] reduceVector(boolean cloneOnChange, N... vector);
	/** Returns signum of the value, i.e. -1, 0, 1 for negative, zero or positive values */
	int signum(N number);
	/** Returns true if this value is numerically equal to one, i.e. <tt>number == 1</tt> */
	boolean isOne(N number);
	/** Returns true if this value is numerically equal to zero, i.e. <tt>number == 0</tt> */
	boolean isZero(N number);
	/** Returns true if this value is numerically unequal to zero, i.e. <tt>number != 0</tt> */
	boolean isNonZero(N number);
	/** Returns true if this value positive, i.e. <tt>number > 0</tt> */
	boolean isPositive(N number);
	/** Returns true if this value not positive, i.e. <tt>number <= 0</tt> */
	boolean isNonPositive(N number);
	/** Returns true if this value negative, i.e. <tt>number < 0</tt> */
	boolean isNegative(N number);
	/** Returns true if this value not negative, i.e. <tt>number >= 0</tt> */
	boolean isNonNegative(N number);
	/** Returns the larger value of the two numbers, i.e. <tt>max(valA, valB)</tt> */
	N max(N valA, N valB);
	/** Returns the largest value of the given numbers, i.e. <tt>max(vals)</tt> */
	N max(N... vals);
	/** Returns the smaller value of the two numbers, i.e. <tt>min(valA, valB)</tt> */
	N min(N valA, N valB);
	/** Returns the smallest value of the given numbers, i.e. <tt>min(vals)</tt> */
	N min(N... vals);
	/** 
	 * Converts this number to binary data and returns it as a byte array. 
	 * Back-conversion is possible using the {@link #fromByteArray(byte[])} method.
	 */
	byte[] toByteArray(N number);
	/** 
	 * Converts the byte array containing binary data into a new number instance. 
	 * Forward-conversion is possible using the {@link #toByteArray(Number)} method.
	 */
	N fromByteArray(byte[] bytes);
	/** 
	 * Converts this number to binary data and writes it to the data output.
	 * The counterpart of this method reading binary data is 
	 * {@link #readFrom(DataInput)}.
	 */
	void writeTo(N number, DataOutput out) throws IOException;
	/** 
	 * Reads binary data from the given data input. Binary data is converted and
	 * a new number instance is returned. The counterpart of this method writing
	 * binary data is {@link #writeTo(Number, DataOutput)}
	 */
	N readFrom(DataInput in) throws IOException;
	/**
	 * Returns the byte array length if {@link #toByteArray(Number)} is used and if
	 * this size is fixed, and -1 if the size may vary depending on the number
	 * instance
	 */
	int byteLength();
}
