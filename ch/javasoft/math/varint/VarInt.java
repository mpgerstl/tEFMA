package ch.javasoft.math.varint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * A VarInt is an integer of variable length. Short values are simply
 * backed with a byte, int or long value. Long integers are backed
 * with an array of bytes. 
 * <p>
 * Subclasses usually extend the {@link VarIntNumber} class.
 * 
 * @see BigInteger
 * @see Number
 */
public interface VarInt extends Comparable<VarInt> {
	
    /**
     * The VarInt constant zero.
     */
	public static final IntVarInt ZERO = IntVarInt.valueOf(0);

    /**
     * The VarInt constant one.
     */
    public static final IntVarInt ONE = IntVarInt.valueOf(1);
    
    /**
     * The VarInt constant two.
     */
    public static final IntVarInt TWO = IntVarInt.valueOf(2);

    /**
     * The VarInt constant ten.
     */
    public static final IntVarInt TEN = IntVarInt.valueOf(10);

    /**
	 * Tries to make this VarInt as compact as possible.
	 */
	VarInt compact();
	
	/**
	 * Returns true if this VarInt is zero
	 * @return true if {@code this == 0}
	 */
	boolean isZero();
	
	/**
	 * Returns true if this VarInt is one
	 * @return true if {@code this == 1}
	 */
	boolean isOne();
	
	/**
	 * Returns true if this VarInt is negative
	 * @return true if {@code this < 0}
	 */
	boolean isNegative();
	
	/**
	 * Returns true if this VarInt is positive
	 * @return true if {@code this > 0}
	 */
	boolean isPositive();
	
	/**
	 * Returns true if this VarInt is not negative
	 * @return true if {@code this >= 0}
	 */
	boolean isNonNegative();
	
	/**
	 * Returns true if this VarInt is not positive
	 * @return true if {@code this <= 0}
	 */
	boolean isNonPositive();
	
	/**
	 * Returns true if this is an even number, that is, if the least significant
	 * bit is zero.
	 * 
	 * @return	true if this VarInt is even
	 */
	boolean isEven();
	
    /**
     * Returns <tt>true</tt> if and only if the designated bit is set.
     * (Computes <tt>((this &amp; (1&lt;&lt;n)) != 0)</tt>.)
     *
     * @param  n index of bit to test.
     * @return <tt>true</tt> if and only if the designated bit is set.
     * @throws ArithmeticException <tt>n</tt> is negative.
     */
    boolean testBit(int n);

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this VarInt, <i>excluding</i> a sign bit.
     * For positive VarInt values, this is equivalent to the number of bits in
     * the ordinary binary representation.  (Computes
     * <tt>(ceil(log<sub>2</sub>(this &lt; 0 ? -this : this+1)))</tt>.)
     *
     * @return number of bits in the minimal two's-complement
     *         representation of this VarInt, <i>excluding</i> a sign bit.
     */
    int bitLength();

    /**
     * Returns the number of bits in the two's complement representation
     * of this VarInt that differ from its sign bit.
     *
     * @return number of bits in the two's complement representation
     *         of this VarInt that differ from its sign bit.
     */
    int bitCount();
    	
    /**
	 * Conversion into a big integer value.
	 */
	BigInteger toBigInteger();

	/**
	 * Conversion into a number value. Most implementations will simply
	 * return the {@code this} pointer since they already extends the
	 * {@link Number} class
	 */
	Number numberValue();

	////// copied from BigInteger

    /**
     * Returns a VarInt whose value is <tt>(this + val)</tt>.
     *
     * @param  val value to be added to this VarInt.
     * @return <tt>this + val</tt>
     */
    VarInt add(VarInt val);

    /**
     * Returns a VarInt whose value is <tt>(this * val)</tt>.
     *
     * @param  val value to be multiplied by this VarInt.
     * @return <tt>this * val</tt>
     */
    VarInt multiply(VarInt val);

    /**
     * Returns a VarInt whose value is <tt>(this - val)</tt>.
     *
     * @param  val value to be subtracted from this VarInt.
     * @return <tt>this - val</tt>
     */
    VarInt subtract(VarInt val);

    /**
     * Returns a VarInt whose value is <tt>(this / val)</tt>.
     *
     * @param  val value by which this VarInt is to be divided.
     * @return <tt>this / val</tt>
     * @throws ArithmeticException <tt>val==0</tt>
     */
    VarInt divide(VarInt val);
    
    /**
     * Returns a <tt>VarInt</tt> whose value is <tt>(this / val)</tt>. If 
     * rounding must be performed to generate an integer result, the specified 
     * rounding mode is applied.
     * 
     * @param  val value by which this <tt>divisor</tt> is to be divided.
     * @param  roundingMode rounding mode to apply.
     * @return <tt>this / val</tt>
     * @throws ArithmeticException if <tt>val</tt> is zero or if
     *         <tt>roundingMode==RoundingMode.UNNECESSARY</tt> and the result
     *         is not an integer.
     * @since 1.5
     */
    VarInt divide(VarInt val, RoundingMode roundingMode);
    
    /**
     * Returns an array of two VarInts containing <tt>(this / val)</tt>
     * followed by <tt>(this % val)</tt>.
     *
     * @param  val value by which this VarInt is to be divided, and the
     *	       remainder computed.
     * @return an array of two VarInts: the quotient <tt>(this / val)</tt>
     *	       is the initial element, and the remainder <tt>(this % val)</tt>
     *	       is the final element.
     * @throws ArithmeticException <tt>val==0</tt>
     */
    VarInt[] divideAndRemainder(VarInt val);
    
    /**
     * Returns a VarInt whose value is <tt>(this % val)</tt>.
     *
     * @param  val value by which this VarInt is to be divided, and the
     *	       remainder computed.
     * @return <tt>this % val</tt>
     * @throws ArithmeticException <tt>val==0</tt>
     */
    VarInt remainder(VarInt val);
    
    /**
     * Returns a VarInt whose value is <tt>(this<sup>exponent</sup>)</tt>.
     * Note that <tt>exponent</tt> is an integer rather than a VarInt.
     *
     * @param  exponent exponent to which this VarInt is to be raised.
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException <tt>exponent</tt> is negative.  (This would
     *	       cause the operation to yield a non-integer value.)
     */
    VarInt pow(int exponent);
    
    /**
     * Returns a VarInt whose value is the greatest common divisor of
     * <tt>abs(this)</tt> and <tt>abs(val)</tt>.  Returns 0 if
     * <tt>this==0 &amp;&amp; val==0</tt>.
     *
     * @param  val value with which the GCD is to be computed.
     * @return <tt>GCD(abs(this), abs(val))</tt>
     */
    VarInt gcd(VarInt val);    
    
	/**
     * Returns a VarInt whose value is the absolute value of this
     * VarInt. 
     *
     * @return <tt>abs(this)</tt>
     */
    VarInt abs();

    /**
     * Returns a VarInt whose value is <tt>(-this)</tt>.
     *
     * @return <tt>-this</tt>
     */
    VarInt negate();

    /**
     * Returns the signum function of this VarInt.
     *
     * @return -1, 0 or 1 as the value of this VarInt is negative, zero or
     *	       positive.
     */
    int signum();

    // Modular Arithmetic Operations

    /**
     * Returns a VarInt whose value is <tt>(this mod m</tt>).  This method
     * differs from <tt>remainder</tt> in that it always returns a
     * <i>non-negative</i> VarInt.
     *
     * @param  m the modulus.
     * @return <tt>this mod m</tt>
     * @throws ArithmeticException <tt>m &lt;= 0</tt>
     * @see    #remainder
     */
    VarInt mod(VarInt m);
    
    /**
     * Returns a VarInt whose value is <tt>(this &lt;&lt; n)</tt>.
     * The shift distance, <tt>n</tt>, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
     *
     * @param  n shift distance, in bits.
     * @return <tt>this &lt;&lt; n</tt>
     * @see #shiftRight
     */
    VarInt shiftLeft(int n);
    
    /**
     * Returns a VarInt whose value is <tt>(this &gt;&gt; n)</tt>.  Sign
     * extension is performed.  The shift distance, <tt>n</tt>, may be
     * negative, in which case this method performs a left shift.
     * (Computes <tt>floor(this / 2<sup>n</sup>)</tt>.) 
     *
     * @param  n shift distance, in bits.
     * @return <tt>this &gt;&gt; n</tt>
     * @see #shiftLeft
     */
    VarInt shiftRight(int n);
    
    /**
     * Compares this VarInt with the specified Object for equality.
     *
     * @param  x Object to which this VarInt is to be compared.
     * @return <tt>true</tt> if and only if the specified Object is a
     *	       VarInt whose value is numerically equal to this VarInt.
     */
    boolean equals(Object x);

    /**
     * Returns the minimum of this VarInt and <tt>val</tt>.
     *
     * @param  val value with which the minimum is to be computed.
     * @return the VarInt whose value is the lesser of this VarInt and 
     *	       <tt>val</tt>.  If they are equal, either may be returned.
     */
    VarInt min(VarInt val);

    /**
     * Returns the maximum of this VarInt and <tt>val</tt>.
     *
     * @param  val value with which the maximum is to be computed.
     * @return the VarInt whose value is the greater of this and
     *         <tt>val</tt>.  If they are equal, either may be returned.
     */
    VarInt max(VarInt val);

    // Hash Function

    /**
     * Returns the hash code for this VarInt.
     *
     * @return hash code for this VarInt.
     */
    int hashCode();
    
    /**
     * Returns the String representation of this VarInt in the
     * given radix.  If the radix is outside the range from {@link
     * Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive,
     * it will default to 10 (as is the case for
     * <tt>Integer.toString</tt>).  The digit-to-character mapping
     * provided by <tt>Character.forDigit</tt> is used, and a minus
     * sign is prepended if appropriate.  (This representation is
     * compatible with {@link VarIntFactory#create(String, int)}.
     *
     * @param  radix  radix of the String representation.
     * @return String representation of this VarInt in the given radix.
     * @see    Integer#toString
     * @see    Character#forDigit
     * @see    VarIntFactory#create(java.lang.String, int)
     */
    String toString(int radix);

    /**
     * Returns the decimal String representation of this VarInt.
     * The digit-to-character mapping provided by
     * <tt>Character.forDigit</tt> is used, and a minus sign is
     * prepended if appropriate.  This representation is compatible
     * with {@link VarIntFactory#create(String)}.
     *
     * @return decimal String representation of this VarInt.
     * @see    Character#forDigit
     * @see    VarIntFactory#create(String)
     * @see    #toString(int)
     */
    String toString();

    /**
     * Returns a byte array containing the two's-complement
     * representation of this VarInt.  The byte array will be in
     * <i>big-endian</i> byte-order: the most significant byte is in
     * the zeroth element.  The array will contain the minimum number
     * of bytes required to represent this VarInt, including at
     * least one sign bit. This representation is compatible with 
     * {@link VarIntFactory#create(byte[])}.
     *
     * @return a byte array containing the two's-complement representation of
     *	       this VarInt.
     * @see    VarIntFactory#create(byte[])
     */
    byte[] toByteArray();
    
    /**
     * Writes bytes to the output stream such that a VarInt can be reconstructed
     * if read again. If the value contains at most 7 bits, only one byte is 
     * written, containing the value directly. Otherwise, the bits 1 to 7 
     * (ommiting the first bit) contains the number of bytes used to reconstruct the 
     * value. The method is compatible with {@link VarIntFactory#readFrom(InputStream)}.
     * <p>
     * The bytes contain the two's-complement representation of this VarInt.  
     * The bytes are written in <i>big-endian</i> byte-order: the most significant 
     * byte is written first. Writes the minimum number of bytes required to represent 
     * this VarInt, including at least one sign bit.
     * 
     * @out the stream to write the bytes to
     * @throws IOException	if an I/O exception occurrs as a consequence of the 
     * 						write operation
     * @see VarIntFactory#readFrom(InputStream)
     */
    void writeTo(OutputStream out) throws IOException;
    
	////// copied from Number
    /**
     * Returns the value of the specified number as an <code>int</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>int</code>.
     */
    int intValue();

    /**
     * Returns the value of the specified number as a <code>long</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>long</code>.
     */
    long longValue();

    /**
     * Returns the value of the specified number as a <code>float</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>float</code>.
     */
    float floatValue();

    /**
     * Returns the value of the specified number as a <code>double</code>.
     * This may involve rounding.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>double</code>.
     */
    double doubleValue();

    /**
     * Returns the value of the specified number as a <code>byte</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>byte</code>.
     * @since   JDK1.1
     */
    byte byteValue();
    /**
     * Returns the value of the specified number as a <code>short</code>.
     * This may involve rounding or truncation.
     *
     * @return  the numeric value represented by this object after conversion
     *          to type <code>short</code>.
     * @since   JDK1.1
     */
    short shortValue();
}
