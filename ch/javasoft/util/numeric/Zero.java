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
package ch.javasoft.util.numeric;

/**
 * <code>Zero</code> is a helper class treating values close to zero as zero.
 * A tolerance or precision can be defined when constructing an instance of
 * <code>Zero</code>.
 */
public class Zero {
	
	/** default precision 10, meaning 10 decimal places*/
	public static final int		DEFAULT_PRECISION	= 10;
	/** default precision 10<sup>-10</sup>, meaning 10 decimal places*/
	public static final double	DEFAULT_TOLERANCE	= 0.0000000001d;
	/** normal machine precision for doubles, that is 2<sup>-52</sup>*/
	public static final double	EPS_TOLERANCE		= Math.pow(2, -52);
	
	public final int	mPrecision;
	public final double	mZeroPos;
	public final double	mZeroNeg;
	
	/**
	 * Constructor using {@link #DEFAULT_PRECISION}
	 */
	public Zero() {
		this(DEFAULT_PRECISION);
	}
	/**
	 * Constructor using the specified tolerance
	 * 
	 * @param	tolerance	a number close to zero, absolute values larger than
	 * 						the absolute value of <code>tolerance</code> are 
	 * 						treated non-zero, other values as zero
	 */
	public Zero(double tolerance) {
		if (Double.isNaN(tolerance) || Double.isInfinite(tolerance)) {
			throw new IllegalArgumentException("illegal tolerance: " + tolerance);
		}
		mZeroPos	= Math.abs(tolerance);
		mZeroNeg	= -mZeroPos;
		mPrecision	= (int)Math.round(-Math.log10(mZeroPos));
	}
	/**
	 * Constructor using the specified precision
	 * 
	 * @param	precision	the precision in decimal places, absolute values 
	 * 						larger than <code>10<sup>-abs(precision)</sup</code>
	 * 						are treated non-zero, other values as zero
	 */
	public Zero(int precision) {
		mPrecision	= Math.abs(precision);
		mZeroPos	= Math.pow(10d, -mPrecision);
		mZeroNeg	= -mZeroPos;
	}
	
	public int sgn(double value) {
		if (value < mZeroNeg) return -1;
		if (value > mZeroPos) return 1;
		return 0;
	}
	
	public boolean isZero(double value) {
		return mZeroNeg <= value && value <= mZeroPos;
	}
	public boolean isNonZero(double value) {
		return mZeroNeg > value || value > mZeroPos;
	}
	
	public boolean isZeroNeg(double value) {
		return value <= mZeroPos;
	}

	public boolean isZeroPos(double value) {
		return value >= mZeroNeg;
	}
	
	public boolean isNegative(double value) {
		return value < mZeroNeg;
	}

	public boolean isPositive(double value) {
		return value > mZeroPos;
	}
	public boolean isNegPos(double valueA, double valueB) {
		return 
			(valueA < mZeroNeg && valueB > mZeroPos) ||
			(valueB < mZeroNeg && valueA > mZeroPos);
	}
	/**
	 * Returns true if the given value is one, that is, if the difference
	 * {@code (value - 1)} {@link #isZero(double) is zero}.
	 */
	public boolean isOne(double value) {
		return isZero(value - 1);
	}
	/**
	 * Returns true if the given value is an integer, that is, if the difference
	 * {@code (value - Math.round(value))} {@link #isZero(double) is zero}.
	 */
	public boolean isInteger(double value) {
		return isZero(value - Math.round(value));
	}
	
	/**
	 * If the given value is within the zero tolerance, zero is returned, and 
	 * the given value otherwise.
	 */
	public double roundZero(double value) {
		if (value < mZeroNeg || value > mZeroPos) return value;
		return 0.0d;
	}
	
	/**
	 * If the given value is within the zero tolerance from an integer, it is
	 * converted into that integer. Otherwise, the value is returned unconverted.
	 */
	public double roundInteger(double value) {
		final long lvalue = Math.round(value);
		return isZero(value - lvalue) ? lvalue : value; 
	}
	
	/**
	 * Returns {@code true} if {@code this} zero instance is the true zero 
	 * value 0.0
	 */
	public boolean isTrueZero() {
		return mZeroPos == 0;
	}
	
	@Override
	public int hashCode() {
		final long l = Double.doubleToLongBits(mZeroPos);
		return (int)((l >>> 32) ^ (l & 0xffffffff));
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Zero) {
			return mZeroPos == ((Zero)obj).mZeroPos;
		}
		return false;
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" + mZeroPos + "}";
	}
	
}
