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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The <tt>BigFraction</tt> is a fractional with numerator and 
 * denominator both being {@link BigInteger} numbers.
 * <p>
 * Big integer fractionals are constant, they don't change after instantiation.
 */
public class BigFraction extends Number implements Comparable<BigFraction>, Serializable {
	
	private static final long serialVersionUID	= 1744509112026609387L;
	private static final double LOG10_OF_2		= Math.log10(2);
	
	private final BigInteger mNumerator;
	private final BigInteger mDenominator;
	
	/**
	 * Constructor with numerator and denominator. See also <tt>valueOf(..)</tt>
	 * methods for various conversions to a big integer fractional.
	 * 
	 * @param numerator		numerator of the fraction number
	 * @param denominator	denominator of the fraction number
	 */
	public BigFraction(long numerator, long denominator) {
		this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
	}
	/**
	 * Constructor with numerator and denominator. See also <tt>valueOf(..)</tt>
	 * methods for various conversions to a big integer fractional.
	 * 
	 * @param numerator		numerator of the fraction number
	 * @param denominator	denominator of the fraction number
	 */
	public BigFraction(BigInteger numerator, BigInteger denominator) {
		if (numerator == null) throw new NullPointerException("null numerator not allowed");
		if (denominator == null) throw new NullPointerException("null denominator not allowed");
		if (denominator.signum() == 0) {
			if (numerator.signum() == 0) {
				throw new ArithmeticException("Division undefined");  // NaN
			}
			throw new ArithmeticException("Division by zero");
		}
		mNumerator		= numerator;
		mDenominator	= denominator;
	}

	/**
	 * It is recommended to use {@link #valueOf(String)} instead. This method
	 * is a convenient method mainly for reflection use by frameworks.
	 * 
	 * @param s	the string to parse
	 */
	public BigFraction(String s) {
		this(valueOf(s));
	}
	//used by string constructur
	private BigFraction(BigFraction copy) {
		this(copy.mNumerator, copy.mDenominator);
	}

	/**
	 * Returns the enumerator
	 */
	public BigInteger getNumerator() {
		return mNumerator;
	}
	
	/**
	 * Returns the denominator
	 */
	public BigInteger getDenominator() {
		return mDenominator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double doubleValue() {
		return toDouble(mNumerator, mDenominator);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public float floatValue() {
		return toFloat(mNumerator, mDenominator);
	}

    /**
     * Converts this BigFraction to an <code>int</code>.  This
     * conversion is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363"><i>narrowing
     * primitive conversion</i></a> from <code>long</code> to
     * <code>int</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification</a>: if this BigFraction is too big to fit in an
     * <code>int</code>, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigFraction value as well as return a
     * result with the opposite sign.
     *
     * @return this BigFraction converted to an <code>int</code>.
     */
	@Override
	public int intValue() {
		return toBigInteger().intValue();
	}

    /**
     * Converts this BigFraction to a <code>long</code>.  This
     * conversion is analogous to a <a
     * href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#25363"><i>narrowing
     * primitive conversion</i></a> from <code>long</code> to
     * <code>int</code> as defined in the <a
     * href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification</a>: if this BigFraction is too big to fit in a
     * <code>long</code>, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigFraction value as well as return a
     * result with the opposite sign.
     *
     * @return this BigFraction converted to a <code>long</code>.
     */
	@Override
	public long longValue() {
		return toBigInteger().longValue();
	}
	
    /**
     * Returns a BigInteger whose value is <tt>(numerator / denominator)</tt>, 
     * using integer division (no rounding).
     *
     * @return <tt>numerator / denominator using integer division</tt>
     */
	public BigInteger toBigInteger() {
		return mNumerator.divide(mDenominator);
	}
	
    /**
     * Returns a BigInteger whose value is <tt>(numerator / denominator)</tt>, 
     * using decimal division. If rounding must be performed to generate an
     * integer result, the specified rounding mode is applied.
     *
     * @param roundingMode	Mode rounding mode to apply
     * @return 				<tt>numerator / denominator</tt> using decimal 
     * 						division and rounding if necessary
     */
	public BigInteger toBigInteger(RoundingMode roundingMode) {
		return toBigDecimal(0, roundingMode).toBigIntegerExact();
	}
    /**
	 * Returns a BigInteger whose value is <tt>(numerator / denominator)</tt>, 
     * checking for lost information.  An exception is thrown if this
     * <tt>BigFraction</tt> has a nonzero reminder after the integer division.
     *
     * @return this <tt>BigFraction</tt> converted to a <tt>BigInteger</tt>.
     * @throws ArithmeticException if <tt>(numerator / denominator)</tt> has a 
     * 			nonzero remainder.
     */
    public BigInteger toBigIntegerExact() {
    	return toBigInteger(RoundingMode.UNNECESSARY);
    }
	
    /**
     * Returns a <tt>BigDecimal</tt> whose value is <tt>(numerator / 
     * denominator)</tt>, and whose preferred scale is <tt>0</tt>; if the exact 
     * quotient cannot be represented (because it has a non-terminating decimal
     * expansion) an <tt>ArithmeticException</tt> is thrown.
     *
     * @throws ArithmeticException if the exact quotient does not have a
     *         terminating decimal expansion
     * @return <tt>numerator / denominator</tt>
     */
	public BigDecimal toBigDecimal() {
		return new BigDecimal(mNumerator).divide(new BigDecimal(mDenominator));
	}
    /**
     * Returns a <tt>BigDecimal</tt> whose value is <tt>(numerator /
     * divisor)</tt>, with rounding according to the context settings.
     *
     * @param  mc the context to use.
     * @return <tt>numerator / divisor</tt>, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is <tt>UNNECESSARY</tt> or 
     *         <tt>mc.precision == 0</tt> and the quotient has a 
     *         non-terminating decimal expansion.
     */
	public BigDecimal toBigDecimal(MathContext mc) {
		return new BigDecimal(mNumerator).divide(new BigDecimal(mDenominator), mc);
	}
    /**
     * Returns a <tt>BigDecimal</tt> whose value is <tt>(numerator /
     * divisor)</tt>, and whose scale is as specified.  If rounding must
     * be performed to generate a result with the specified scale, the
     * specified rounding mode is applied.
     * 
     * @param  scale scale of the <tt>BigDecimal</tt> quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @return <tt>this / divisor</tt>
     * @throws ArithmeticException if <tt>divisor</tt> is zero,
     *         <tt>roundingMode==RoundingMode.UNNECESSARY</tt> and
     *         the specified scale is insufficient to represent the result
     *         of the division exactly.
     */
	public BigDecimal toBigDecimal(int scale, RoundingMode roundingMode) {
		return new BigDecimal(mNumerator).divide(new BigDecimal(mDenominator), scale, roundingMode);
	}
	
    /**
     * Returns the hash code for this BigFraction.
     *
     * @return hash code for this BigFraction.
     */
	@Override
	public int hashCode() {
		return mNumerator.hashCode() ^ mDenominator.hashCode();
	}
	
	/**
	 * Returns <tt>true</tt> if and only if the numerator and denominator of 
	 * the two objects are equal. For numerical equality, use 
	 * {@link #equalsNumerically(BigFraction)} or
	 * {@link #compareTo(BigFraction)}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() == getClass()) {
			BigFraction other = (BigFraction)obj;
			return
				mNumerator.equals(other.mNumerator) && 
				mDenominator.equals(other.mDenominator);
		}
		return false;
	}
	
    /**
     * Compares this BigFraction with the specified BigInteger.  
     * This method is provided in preference to individual methods for each of 
     * the six boolean comparison operators (&lt;, ==, &gt;, &gt;=, !=, &lt;=).  
     * The suggested idiom for performing these comparisons is:
     * <tt>(x.compareTo(y)</tt> &lt;<i>op</i>&gt; <tt>0)</tt>,
     * where &lt;<i>op</i>&gt; is one of the six comparison operators.
     *
     * @param  other fractional number to which this fraction is to be compared.
     * @return -1, 0 or 1 as this fraction is numerically less than, equal
     *         to, or greater than <tt>val</tt>.
     */
	public int compareTo(BigFraction other) {
		if (other == this) return 0;
		if (0 == mDenominator.compareTo(other.mDenominator)) {
			return mNumerator.compareTo(other.mNumerator);
		}
		BigInteger mulThis	= mNumerator.multiply(other.mDenominator);
		BigInteger mulOther	= other.mNumerator.multiply(mDenominator);
		return mulThis.compareTo(mulOther);
	}

    /**
     * Returns the signum function of this BigFraction.
     *
     * @return -1, 0 or 1 as the value of this fraction is negative, zero or
     *	       positive.
     */
	public int signum() {
		return mNumerator.signum() * mDenominator.signum();
	}

    /**
     * Returns a BigFraction whose value is the absolute value of 
     * this BigFraction. 
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     *
     * @return <tt>abs(this)</tt>
     */
	public BigFraction abs() {
		int sgnNumerator	= mNumerator.signum();
		int sgnDenominator	= mDenominator.signum();
		if (sgnNumerator * sgnDenominator < 0) {
			if (sgnDenominator < 0) {
				return new BigFraction(mNumerator, mDenominator.negate());
			}
			return new BigFraction(mNumerator.negate(), mDenominator);			
		}
		return this;
	}

	/**
     * Returns a BigFraction whose value is <tt>(-this)</tt>.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     *
     * @return <tt>-this</tt>
     */
	public BigFraction negate() {
		int sgnNumerator	= mNumerator.signum();
		if (sgnNumerator == 0) return BigFraction.ZERO;
		int sgnDenominator	= mDenominator.signum();
		if (sgnDenominator < 0) {
			return new BigFraction(mNumerator, mDenominator.negate());
		}
		return new BigFraction(mNumerator.negate(), mDenominator);
	}

	/**
     * Returns a BigFraction whose value is <tt>(1/this)</tt>.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     * <p>
     * The resulting denominator is guaranteed to be non-negative.
     *
     * @return <tt>1/this</tt>
     */
	public BigFraction invert() {
		int sgn = mNumerator.signum();
		if (sgn == 0) {
			throw new ArithmeticException("Division by zero");
		}
		else if (sgn < 0) {
			return new BigFraction(mDenominator.negate(), mNumerator.negate());
		}
		else {
			if (isOne()) {
				return BigFraction.ONE;
			}
			return new BigFraction(mDenominator, mNumerator);
		}
	}

	/**
     * Returns a BigFraction whose value is <tt>(this + val)</tt>.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     *
     * @param  val value to be added to this BigFraction.
     * @return <tt>this + val</tt>
     */
	public BigFraction add(BigFraction val) {
		if (signum() == 0) return val;
		if (val.signum() == 0) return this;
		if (mDenominator.equals(val.mDenominator)) {
			return new BigFraction(mNumerator.add(val.mNumerator), mDenominator);
		}
		return BigFraction.valueOf(
			mNumerator.multiply(val.mDenominator).add(val.mNumerator.multiply(mDenominator)),
			mDenominator.multiply(val.mDenominator)
		);
	}

    /**
     * Returns a BigFraction whose value is <tt>(this - val)</tt>.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     *
     * @param  val value to be subtracted from this BigFraction.
     * @return <tt>this - val</tt>
     */
	public BigFraction subtract(BigFraction val) {
		if (signum() == 0) return val.negate();
		if (val.signum() == 0) return this;
		if (mDenominator.equals(val.mDenominator)) {
			return new BigFraction(mNumerator.subtract(val.mNumerator), mDenominator);
		}
		return new BigFraction(
			mNumerator.multiply(val.mDenominator).subtract(val.mNumerator.multiply(mDenominator)),
			mDenominator.multiply(val.mDenominator)
		);
	}
	
    /**
     * Returns a BigFraction whose value is <tt>(this * val)</tt>.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     *
     * @param  val value to be multiplied by this BigFraction.
     * @return <tt>this * val</tt>
     */
	public BigFraction multiply(BigFraction val) {
		if (signum() == 0 || val.signum() == 0) {
			return BigFraction.ZERO;
		}
		if (val.isOne()) {
			return this;
		}
		if (isOne()) {
			return val;
		}
		return new BigFraction(
			mNumerator.multiply(val.mNumerator),
			mDenominator.multiply(val.mDenominator)
		);
	}

    /**
     * Returns a BigFraction whose value is <tt>(this / val) =
     * (this.numerator * by.denominator) / (this.denominator * by.numerator)
     * </tt>.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1. 
     *
     * @param  by value by which this BigFraction is to be divided.
     * @return <tt>this / val</tt>
     * @throws ArithmeticException <tt>val==0</tt>
     */
	public BigFraction divide(BigFraction by) {
		final int sgn = signum();
		if (by.signum() == 0) {
			if (sgn == 0) {
				throw new ArithmeticException("Division undefined: 0/0");  // NaN
			}
			throw new ArithmeticException("Division by zero");
		}
		if (sgn == 0) {
			return BigFraction.ZERO;
		}
		if (by.isOne()) {
			return this;
		}
		if (isOne()) {
			return BigFraction.valueOf(by.mDenominator, by.mNumerator);
		}
		return BigFraction.valueOf(
			mNumerator.multiply(by.mDenominator),
			mDenominator.multiply(by.mNumerator)
		);
	}
	
    /**
     * Returns a BigFraction whose value is 
     * <tt>(this<sup>exponent</sup>) == (this.nominator<sup>exponent</sup> / 
     * this.denominator<sup>exponent</sup>)</tt>.
     * Note that <tt>exponent</tt> is an integer rather than a 
     * BigFraction.
     * The fractional number is not {@link #reduce() reduced}, that is, 
     * gcd(nominator, denominator) might be != 1.
     *  
     * @param  exponent exponent to which this BigFraction is to be 
     * 					raised.
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException <tt>exponent</tt> is negative and 
     * 		   <tt>this</tt> is zero.  (This would cause a division by zero.)
     */
	public BigFraction pow(int exponent) {
		if (mNumerator.signum() == 0) {
			if (exponent == 0) return BigFraction.ONE;
			if (exponent < 0) {
				throw new ArithmeticException("Division by zero");
			}
			return BigFraction.ZERO;
		}
		if (exponent == 0 || isOne()) {
			return BigFraction.ONE;
		}
		if (exponent < 0) {
			if (-exponent < 0) throw new ArithmeticException("exponent to small: " + exponent);
			return new BigFraction(
				mDenominator.pow(-exponent), mNumerator.pow(-exponent)
			);
		}
		return new BigFraction(
			mNumerator.pow(exponent), mDenominator.pow(exponent)
		);
	}
	
    /**
     * Returns a BigInteger whose value is <tt>(this &lt;&lt; n)</tt>.
     * The shift distance, <tt>n</tt>, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>this * 2<sup>n</sup></tt>.)
     * <p>
     * To achieve that the resulting fraction number is smallest possible, the 
     * denominator is shifted to the right as far as possible. The remaining 
     * positions are left shifts of the numerator. 
     *
     * @param  n shift distance, in bits.
     * @return <tt>this &lt;&lt; n</tt>
     * @see #shiftRight
     */
	public BigFraction shiftLeft(int n) {
		if (isZero()) return BigFraction.ZERO;
		if (n == 0) return this;
		if (n < 0) return shiftRight(-n);
		
		final int shiftDen = Math.min(n, Math.max(0, mDenominator.getLowestSetBit()));
		final int shiftNom = Math.max(0, n - shiftDen);
		return new BigFraction(
			mNumerator.shiftLeft(shiftNom), mDenominator.shiftRight(shiftDen)
		);
	}
	
    /**
     * Returns a BigFraction whose value is <tt>(this &gt;&gt; n)</tt>. 
     * The shift distance, <tt>n</tt>, may be negative, in which case this 
     * method performs a left shift.
     * (Computes <tt>this / 2<sup>n</sup></tt>.) 
     * <p>
     * To achieve that the resulting fraction number is smallest possible, the 
     * numerator is shifted to the right as far as possible. The remaining 
     * positions are left shifts of the denominator. 
     *
     * @param  n shift distance, in bits.
     * @return <tt>this &gt;&gt; n</tt>
     * @see #shiftLeft
     */
	public BigFraction shiftRight(int n) {
		if (isZero()) return BigFraction.ZERO;
		if (n == 0) return this;
		if (n < 0) return shiftLeft(-n);
		
		final int shiftNom = Math.min(n, Math.max(0, mNumerator.getLowestSetBit()));
		final int shiftDen = Math.max(0, n - shiftNom);
		return new BigFraction(
			mNumerator.shiftRight(shiftNom), mDenominator.shiftLeft(shiftDen)
		);
	}

    /**
     * Returns the maximum of this BigFraction and <tt>val</tt>.
     *
     * @param  with value with which the maximum is to be computed.
     * @return the BigFraction whose value is the greater of this 
     * 			and <tt>val</tt>.  If they are equal, either may be returned.
     */
	public BigFraction max(BigFraction with) {
		if (compareTo(with) < 0) return with;
		return this;
	}
    /**
     * Returns the minimum of this BigFraction and <tt>val</tt>.
     *
     * @param  with value with which the minimum is to be computed.
     * @return the BigFraction whose value is the lesser of this 
     * 			and <tt>val</tt>.  If they are equal, either may be returned.
     */
	public BigFraction min(BigFraction with) {
		if (compareTo(with) > 0) return with;
		return this;
	}
	
	/**
	 * Divides denominator and numerator by their greatest common divisor
	 * (gcd). The denominator of the returned integer fractional will always be 
	 * positive. If the numerator is 0, the denominator will be one.
	 * 
	 * @see #isReduced()
	 */
	public BigFraction reduce() {
		int sgnNumerator = mNumerator.signum();
		if (0 == sgnNumerator) {
			return BigFraction.ZERO;
		}
		if (mDenominator.equals(BigInteger.ONE)) {
			return this;
		}
		BigInteger gcd = mNumerator.gcd(mDenominator);
		if (mDenominator.signum() < 0) {
			gcd = gcd.negate();
		}
		else if (gcd.equals(BigInteger.ONE)) {
			return this;
		}
		return valueOf(
			mNumerator.divide(gcd), mDenominator.divide(gcd)
		);
	}
	
	/**
	 * Calculates the greatest common divisor of this fraction number and
	 * <code>val</code>. The gcd of two fraction numbers is gcd of numerators 
	 * divided by gcd of deniminators.
	 * <p>
	 * If both numbers are negative, the resulting gcd is also negative. If both 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * 
	 * @param val	value with which the gcd is to be computed
	 * @return		sgn * gcd(this.numerator, val.numberator) / 
	 * 				gcd(this.denominator, val.denominator)
	 */
	public BigFraction gcd(BigFraction val) {
		final int sgnThis	= signum();
		final int sgnOther	= signum();
		if (sgnThis == 0 && sgnOther == 0) {
			return BigFraction.ZERO;
		}
		final BigInteger num 	= mNumerator.gcd(val.mNumerator);
		final BigInteger den	= mDenominator.gcd(val.mDenominator);
		if (sgnThis < 0 && sgnOther < 0) {
			return valueOf(num.negate(), den);
		}
		return valueOf(num, den);
	}
	
	/**
	 * Calculates the greatest common divisor of the specified fraction numbers.
	 * The gcd of fraction numbers is gcd of numerators divided by gcd of 
	 * denominators.
	 * <p>
	 * This method might be useful to scale down a vector of fraction numbers. 
	 * <p>
	 * If all numbers are negative, the resulting gcd is also negative. If all 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * For a zero-length vector, the result is one.
	 * 
	 * @param values	values for which the gcd is to be computed
	 * @return			sgn * gcd(values[*].numberator) / gcd(values[*].denominator)
	 */
	public static BigFraction gcd(BigFraction... values) {
		if (values.length == 0) return BigFraction.ONE;
		int allSgn		= values[0].signum();
		BigInteger num 	= values[0].mNumerator;
		BigInteger den	= allSgn == 0 ? BigInteger.ZERO : values[0].mDenominator;
		for (int i = 1; i < values.length; i++) {
			final BigFraction val = values[i];
			if (allSgn != val.signum()) allSgn = 1;
			if (num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE)) {
				break;
			}
			num = num.gcd(val.mNumerator);
			if (val.signum() != 0) {
				den = den.gcd(val.mDenominator);
			}
		}
		if (allSgn == 0) return BigFraction.ZERO;
		if (allSgn < 0) {
			return valueOf(num.negate(), den);
		}
		return valueOf(num, den);
	}

	/**
	 * Convenience method to check whether this big integer fraction number is
	 * numerically equal to zero, say this == 0
	 * 
	 * @see #signum()
	 * @see #compareTo(BigFraction)
	 * @see #equalsNumerically(BigFraction)
	 */
	public boolean isZero() {
		return mNumerator.signum() == 0;
	}
	
	/**
	 * Returns true if this fraction is already reduced, i.e. if the greatest
	 * common divisor of numerator and denominator is one.
	 * 
	 * @see #reduce()
	 */
	public boolean isReduced() {
		if (BigInteger.ONE.equals(mDenominator)) {
			return true;
		}
		return BigInteger.ONE.equals(mNumerator.gcd(mDenominator));
	}
	
	/**
	 * Convenience method to check whether this big integer fraction number is
	 * numerically not equal to zero, say this != 0
	 * 
	 * @see #signum()
	 * @see #compareTo(BigFraction)
	 * @see #equalsNumerically(BigFraction)
	 */
	public boolean isNonZero() {
		return mNumerator.signum() != 0;
	}
	
	/**
	 * Convenience method to check whether this big integer fraction number is
	 * negative, say this < 0
	 * 
	 * @see #signum()
	 * @see #compareTo(BigFraction)
	 */
	public boolean isNegative() {
		return signum() < 0;
	}
	
	/**
	 * Convenience method to check whether this big integer fraction number is
	 * non-negative, say this >= 0
	 * 
	 * @see #signum()
	 * @see #compareTo(BigFraction)
	 */
	public boolean isNonNegative() {
		return signum() >= 0;
	}
	
	/**
	 * Convenience method to check whether this big integer fraction number is
	 * positive, say this > 0
	 *
	 * @see #signum()
	 * @see #compareTo(BigFraction)
	 */
	public boolean isPositive() {
		return signum() > 0;
	}
	
	/**
	 * Convenience method to check whether this big integer fraction number is
	 * non-positive, say this <= 0
	 *
	 * @see #signum()
	 * @see #compareTo(BigFraction)
	 */
	public boolean isNonPositive() {
		return signum() <= 0;
	}

	/**
	 * Convenience method to check whether this big integer fraction number is
	 * numerically equal to one. This is true if numerator and denominator are
	 * numerically equal.
	 * 
	 * @see #equalsNumerically(BigFraction)
	 * @see #compareTo(BigFraction)
	 */
	public boolean isOne() {
		return this == ONE || mNumerator.equals(mDenominator);
	}
	
	/**
	 * Returns true if this number represents an integer, that is, if remainder
	 * of numerator/denominator equals zero.
	 */
	public boolean isInteger() {
		if (mNumerator.equals(mDenominator)) return true;
		if (mDenominator.equals(BigInteger.ONE)) return true;
		return BigInteger.ZERO.equals(mNumerator.remainder(mDenominator.abs()));
	}
	
	/**
	 * Returns true iff this big integer fraction number is numerically equal 
	 * to <tt>other</tt>. Note that {@link #equals(Object)} only returns true
	 * if both nominator and denominator are equal, and false otherwise, even if 
	 * both fractional numbers represent the same numeric value.
	 * 
	 * @see #equals(Object)
	 */
	public boolean equalsNumerically(BigFraction other) {
		return compareTo(other) == 0;
	}
	
	/**
	 * Returns a string representation of this BigFraction.
	 * If the denominator is 1, the numerator's string representation is
	 * returned. Otherwise, <tt>numerator/denominator</tt> is returned.
	 * <p>
	 * Strings returned here are parsable by {@link #valueOf(String)}
	 */
	@Override
	public String toString() {
		return (0 == mDenominator.compareTo(BigInteger.ONE)) ? 
			mNumerator.toString() : mNumerator + "/" + mDenominator;
	}
	
    /**
     * Translates string representation of a fractional number into a
     * BigFraction instance. Valid strings are those of 
     * {@link BigInteger} and {@link BigDecimal}, or strings of the form
     * <tt>numerator/denominator</tt> with two big integers.
     *
     * @param s  the string representation of a big integer fraction to parse
     * @throws NumberFormatException <tt>val</tt> is not a valid representation
     *	       of a BigFraction.
     * @see    BigInteger#BigInteger(String)
     * @see    BigDecimal#BigDecimal(String)
     */
	public static BigFraction valueOf(String s) throws NumberFormatException {
		if (s.startsWith("+")) {
			s = s.substring(1);
		}
		int slashIndex = s.indexOf('/');		
		if (slashIndex < 0) {
			int dotIndex = s.indexOf('.');
			if (dotIndex < 0) {
				return valueOf(new BigInteger(s), BigInteger.ONE);
			}
			return valueOf(new BigDecimal(s));
		}
		return valueOf(
			new BigInteger(s.substring(0, slashIndex)),
			new BigInteger(s.substring(slashIndex + 1))
		);
	}

	/**
	 * Translates a {@link BigInteger} to a BigFraction
	 * 
	 * @param val	the big integer to translate
	 * @return		the BigFraction representing <tt>val</tt>
	 */
	public static BigFraction valueOf(BigInteger val) {
		return valueOf(val, BigInteger.ONE);
	}
	/**
	 * Translates a {@link BigDecimal} to a BigFraction
	 * 
	 * @param val	the big decimal to translate
	 * @return		the BigFraction representing <tt>val</tt>
	 */
	public static BigFraction valueOf(BigDecimal val) {
		final int scale = val.scale();
		if (scale > 0) {
			return valueOf(
				val.unscaledValue(), BigInteger.TEN.pow(val.scale())				
			);
		}
		return valueOf(val.toBigIntegerExact());
	}
	/**
	 * Translates a long to a BigFraction
	 * 
	 * @param val	the long to translate
	 * @return		the BigFraction representing <tt>val</tt>
	 * @see			BigDecimal#valueOf(long)
	 */
	public static BigFraction valueOf(long val) {
		if (val == 0) return BigFraction.ZERO;
		if (val == 1) return BigFraction.ONE;
		if (val == 2) return BigFraction.TWO;
		return valueOf(val, 1);
	}

	/**
	 * Translates a double to a BigFraction
	 * 
	 * @param val	the double to translate
	 * @return		the BigFraction representing <tt>val</tt>
	 * @see			BigDecimal#valueOf(double)
	 */
	public static BigFraction valueOf(double val) {
		return valueOf(BigDecimal.valueOf(val));
	}
	
	/**
	 * Returns a BigFraction from long numerator and denominator. This
	 * is the same as {@link #BigFraction(long, long)}, but possibly,
	 * some caching is used for commonly used values.
	 * 
	 * @param numerator		the numerator for the fraction number to be created
	 * @param denominator	the denominator for the fraction number to be created
	 * @return		the BigFraction representing <tt>numerator/denominator</tt>
	 * @see			#BigFraction(long, long)
	 */
	public static BigFraction valueOf(long numerator, long denominator) {
		if (denominator == 1) {
			if (numerator == 0) return BigFraction.ZERO;
			if (numerator == 1) return BigFraction.ONE;
			if (numerator == 2) return BigFraction.TWO;
			if (numerator == 10) return BigFraction.TEN;
			return new BigFraction(
				BigInteger.valueOf(numerator), BigInteger.ONE
			);
		}
		return new BigFraction(numerator, denominator);
	}
	
	/**
	 * Returns a BigFraction from big integer numerator and denominator. 
	 * This is the same as {@link #BigFraction(BigInteger, BigInteger)}, 
	 * but possibly, some caching is used for commonly used values.
	 * 
	 * @param numerator		the numerator for the fraction number to be created
	 * @param denominator	the denominator for the fraction number to be created
	 * @return		the BigFraction representing <tt>numerator/denominator</tt>
	 * @see			#BigFraction(BigInteger, BigInteger)
	 */
	public static BigFraction valueOf(BigInteger numerator, BigInteger denominator) {
		if (denominator.equals(BigInteger.ONE)) {
			if (numerator.signum() == 0) return BigFraction.ZERO;
			if (numerator.equals(BigInteger.ONE)) return BigFraction.ONE;
//			if (numerator == 2) return BigFraction.TWO;
			if (numerator.equals(BigInteger.TEN)) return BigFraction.TEN;
			return new BigFraction(
				numerator, BigInteger.ONE
			);
		}
		return new BigFraction(numerator, denominator);
	}
	
	private static interface ValueOf<N extends Number> {
		BigFraction valueOf(N val);
	}
	private static Map<Class<? extends Number>, ValueOf> sValueOfMap;
	static {
		sValueOfMap = new HashMap<Class<? extends Number>, ValueOf>();
		sValueOfMap.put(BigFraction.class, new ValueOf<BigFraction>() {
			public BigFraction valueOf(BigFraction val) {
				return val;
			}
		});
		sValueOfMap.put(BigInteger.class, new ValueOf<BigInteger>() {
			public BigFraction valueOf(BigInteger val) {
				return BigFraction.valueOf(val);
			}
		});
		sValueOfMap.put(BigDecimal.class, new ValueOf<BigDecimal>() {
			public BigFraction valueOf(BigDecimal val) {
				return BigFraction.valueOf(val);
			}
		});
		sValueOfMap.put(Long.class, new ValueOf<Long>() {
			public BigFraction valueOf(Long val) {
				return BigFraction.valueOf(val.longValue());
			}
		});
		sValueOfMap.put(AtomicLong.class, new ValueOf<AtomicLong>() {
			public BigFraction valueOf(AtomicLong val) {
				return BigFraction.valueOf(val.longValue());
			}
		});
		sValueOfMap.put(Integer.class, new ValueOf<Integer>() {
			public BigFraction valueOf(Integer val) {
				return BigFraction.valueOf(val.longValue());
			}
		});
		sValueOfMap.put(AtomicInteger.class, new ValueOf<AtomicInteger>() {
			public BigFraction valueOf(AtomicInteger val) {
				return BigFraction.valueOf(val.longValue());
			}
		});
		sValueOfMap.put(Short.class, new ValueOf<Short>() {
			public BigFraction valueOf(Short val) {
				return BigFraction.valueOf(val.longValue());
			}
		});
		sValueOfMap.put(Byte.class, new ValueOf<Byte>() {
			public BigFraction valueOf(Byte val) {
				return BigFraction.valueOf(val.longValue());
			}
		});
		sValueOfMap.put(Double.class, new ValueOf<Double>() {
			public BigFraction valueOf(Double val) {
				return BigFraction.valueOf(val.doubleValue());
			}
		});
		sValueOfMap.put(Float.class, new ValueOf<Float>() {
			public BigFraction valueOf(Float val) {
				return BigFraction.valueOf(val.doubleValue());
			}
		});
		sValueOfMap.put(Number.class, new ValueOf<Number>() {
			public BigFraction valueOf(Number val) {
				return BigFraction.valueOf(val.toString());
			}
		});
	}
	
	/**
	 * Translates any number into a big integer fractional number. The
	 * most appropriate <tt>valueOf(..)</tt> method is used. If no better 
	 * method is found, {@link #valueOf(String)} is invoked after converting
	 * the given number to a string value.
	 * <p>
	 * This method is guaranteed to work for java number types, including
	 * {@link BigInteger} and {@link BigDecimal}.
	 */
	@SuppressWarnings("unchecked")
	public static BigFraction valueOf(Number val) {
		ValueOf valOf = sValueOfMap.get(val.getClass());
		if (valOf == null) valOf = sValueOfMap.get(Number.class);
		return valOf.valueOf(val);
	}
	
	/**
	 * Translates a double to a BigFraction, but tries to adjust
	 * the value to a nice fractional number, with numerator/denominator as
	 * small as possible. The returned value is numerically equal to {@code val} 
	 * if converted back to a double value using {@link #doubleValue()}.
	 * <p>
	 * This method is especially useful for double values representing 1/3, 1/6 
	 * etc. but it works also fine with any/10^i.
	 * 
	 * @param val	the double to translate
	 * @return		the BigFraction representing <tt>val</tt>
	 * @see			#valueOf(double)
	 * @throws	ArithmeticException if {@code val} is NaN or infinite
	 */
	public static BigFraction valueOfAdjusted(double val) {
		return valueOfAdjusted(val, 0d);
	}
	/**
	 * Translates a double to a BigFraction, but tries to adjust
	 * the value to a nice fractional number, with numerator/denominator as
	 * small as possible, using the specified {@code tolerance}. 
	 * <p>
	 * The difference between {@code val} and the returned value, converted back 
	 * to a double value using {@link #doubleValue()}}, is no more than the
	 * specified {@code tolerance}.
	 * <p>
	 * This method is especially useful for double values representing 1/3, 1/6 
	 * etc. but it works also fine with any/10^i.
	 * 
	 * @param val		the double to translate
	 * @param tolerance	a positive value close to zero used as tolerance as 
	 * 					described above
	 * @return		the BigFraction representing <tt>val</tt>
	 * @see			#valueOf(double)
	 * @throws	ArithmeticException 		if {@code val} is NaN or infinite
	 * @throws	IllegalArgumentException	if {@code tolerance} is negative,
	 * 										NaN or infinite
	 */
	public static BigFraction valueOfAdjusted(double val, double tolerance) {
		if (Double.isNaN(val) || Double.isInfinite(val)) {
			throw new ArithmeticException("cannot convert double into fraction number: " + val);
		}
		if (tolerance < 0 || Double.isNaN(tolerance) || Double.isInfinite(tolerance)) {
			throw new IllegalArgumentException("illegal tolerance value: " + tolerance);
		}
		final int sgn;
		if (val < 0d) {
			sgn = -1;
			val = -val;
		}
		else sgn = 1;
		long powOfTen		 = 1L;
		long ratioLargeSmall = 1L;
		do {	
			long powOfSmall = 1L;
			while (powOfSmall <= ratioLargeSmall) {
				long powOfLarge = ratioLargeSmall * powOfSmall;
				
				//first, try pow of ten value
				if (powOfTen / powOfLarge <= 1L) {
					long l = Math.round(val * powOfTen);
					if (Math.abs(val - ((double)l) / powOfTen) <= tolerance) {
						return valueOf(sgn*l, powOfTen);
					}				
					powOfTen *= 10L;
				}

				//now try to eliminate the period part
				if (powOfLarge / powOfSmall == ratioLargeSmall /*avoid overflow*/) {					
					double small 	= val * powOfSmall;
					double large	= val * powOfLarge;
					double diff		= Math.abs(large - small);
					if (diff <= Long.MAX_VALUE) {
						long diffL		= Math.round(diff);
						double guess	= ((double)diffL) / (powOfLarge - powOfSmall);
						if (Math.abs(guess - val) <= tolerance) {				
							return valueOf(sgn*diffL, powOfLarge - powOfSmall);
						}
					}
					powOfSmall <<= 1;
				}
				else {
					powOfSmall = ratioLargeSmall + 1;//make the loop end			
				}
			}
		}
		while ((ratioLargeSmall <<= 1) > 0L /*avoid overflow*/);
		return valueOf(sgn*val);
	}
	
    /**
     * The BigFraction constant zero.
     */
	public static final BigFraction ZERO 	= new BigFraction(BigInteger.ZERO, BigInteger.ONE);
    /**
     * The BigFraction constant one.
     */
	public static final BigFraction ONE	= new BigFraction(BigInteger.ONE, BigInteger.ONE);
    /**
     * The BigFraction constant two.
     */
	public static final BigFraction TWO	= new BigFraction(BigInteger.valueOf(2), BigInteger.ONE);
    /**
     * The BigFraction constant ten.
     */
	public static final BigFraction TEN	= new BigFraction(BigInteger.TEN, BigInteger.ONE);
	
	/**
	 * Converts big integer numerator/denominator arguments to a double value.
	 * <p>
	 * The transformation is performed using {@link BigDecimal#divide(BigDecimal, int, RoundingMode) BigDecimal division}
	 * with {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding mode. The scale
	 * is chosen appropriately for double precision floating point numbers.
	 */
	public static double toDouble(BigInteger numerator, BigInteger denominator) {
		//slow and unprecise:
//		return numerator.doubleValue() / denominator.doubleValue();
		
		//even slower, but better precision
//    	BigInteger[] vals = numerator.divideAndRemainder(denominator);
//    	return vals[0].doubleValue() + vals[1].doubleValue() / denominator.doubleValue();
		
		//faster and precise
		final int numBits	= numerator.bitLength();	//log2 estimate
		final int denBits	= denominator.bitLength();	//log2 estimate
		final int scale		= (int)((52 - numBits + denBits + 2) * LOG10_OF_2) + 1;
		return new BigDecimal(numerator).divide(
			new BigDecimal(denominator), scale, RoundingMode.HALF_EVEN
		).doubleValue();
	}
	
	/**
	 * Converts big integer numerator/denominator arguments to a float value
	 * <p>
	 * The transformation is performed using {@link BigDecimal#divide(BigDecimal, int, RoundingMode) BigDecimal division}
	 * with {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding mode. The scale
	 * is chosen appropriately for single precision floating point numbers.
	 */
	public static float toFloat(BigInteger numerator, BigInteger denominator) {
		final int numBits	= numerator.bitLength();	//log2 estimate
		final int denBits	= denominator.bitLength();	//log2 estimate
		final int scale		= (int)((23 - numBits + denBits + 2) * LOG10_OF_2) + 1;
		return new BigDecimal(numerator).divide(
			new BigDecimal(denominator), scale, RoundingMode.HALF_EVEN
		).floatValue();
	}
	
}
