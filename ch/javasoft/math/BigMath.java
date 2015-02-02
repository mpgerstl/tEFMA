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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;


/**
 * Similar to {@link Math}, but for big numbers such as 
 * <ul>
 * <li>{@link BigInteger}</li>
 * <li>{@link BigDecimal}</li>
 * <li>{@link BigFraction}</li>
 * <ul>
 */
public class BigMath {
    private static final BigDecimal TWO 		= BigDecimal.valueOf(2);
    private static final BigDecimal FOUR 		= BigDecimal.valueOf(4);
	private static final double 	LOG10_OF_2	= Math.log10(2);

	/**
	 * Gauss-Legendre Algorithm to calculate pi upto roughly the given scale
	 * 
     * @param  scale scale of the <tt>BigDecimal</tt> PI to be returned
     * @return <tt>PI</tt>
	 */
    public static BigDecimal pi(final int scale) {
        BigDecimal a = BigDecimal.ONE;
        BigDecimal b = BigDecimal.ONE.divide(sqrt(TWO, scale), scale, RoundingMode.HALF_EVEN);
        BigDecimal t = new BigDecimal(0.25);
        BigDecimal x = BigDecimal.ONE;
        BigDecimal y;
        
        while (!a.equals(b)) {
            y = a;
            a = a.add(b).divide(TWO, scale, RoundingMode.HALF_EVEN);
            b = sqrt(b.multiply(y), scale);
            t = t.subtract(x.multiply(y.subtract(a).multiply(y.subtract(a))));
            x = x.multiply(TWO);
        }
        
        return a.add(b).multiply(a.add(b)).divide(t.multiply(FOUR), scale, RoundingMode.HALF_EVEN);
    }
    
    public static long sqrtBabylonian(long num) {
        if (num <= 1) return num;
        final long log = 63 - Long.numberOfLeadingZeros(num);
        long res = 1L << (log >>> 1);
        long old;
        do {
            old = res;
            res = ((res + num / res) >>> 1);            
        }
        while (old - res < -1 && old - res > 1);
        if (old - res != 0) {
        	if (Math.abs(num - old*old) < Math.abs(num - res*res)) return old;
        }
        return res;
        
    }
    public static long sqrt(long num) {
//    	int curLog  = 1 + (63 - Long.numberOfLeadingZeros(num)) / 2;
    	int curLog  = 32;        
        long res    = 0;
        while (curLog > 0) {
            final long resTimes2toCurLog = (res << curLog);
            curLog--;
            if (num > resTimes2toCurLog) {
                final long resAdd 	= (1L << curLog);
            	final long nextNum 	= num - resTimes2toCurLog - (resAdd << curLog);
                if (nextNum > 0) {
                    num  = nextNum;
                    res |= resAdd;
                }
                else if (nextNum == 0) return res | resAdd;
            }
        }
        return res;
    }
    public static int sqrt(int num) {
//    	int curLog  = 1 + (31 - Integer.numberOfLeadingZeros(num)) / 2;
        int curLog  = 16;        
        int res     = 0;
        while (curLog > 0) {
            final int resTimes2toCurLog = (res << curLog);
            curLog--;
            if (num > resTimes2toCurLog) {
                final int resAdd 	= (1 << curLog);
            	final int nextNum 	= num - resTimes2toCurLog - (resAdd << curLog);
                if (nextNum > 0) {
                    num  = nextNum;
                    res |= resAdd;
                }
                else if (nextNum == 0) return res | resAdd;
            }
        }
        return res;
    }
    
    /**
     * Babylonian square root method (Newton's method) to compute the square
     * root of the given big decimal number up to roughly the given scale.
     * 
     * @param  scale scale of the <tt>BigDecimal</tt> square root to be returned
     * @return <tt>sqrt(num)</tt>
     */
    public static BigDecimal sqrt(final BigDecimal num, final int scale) {
        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = estimateSqrt(num);
        
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = num.divide(x0, scale, RoundingMode.HALF_EVEN);
            x1 = x1.add(x0);
            x1 = x1.divide(TWO, scale, RoundingMode.HALF_EVEN);
        }
        
        return x1;
    }
    /**
     * Babylonian square root method (Newton's method) to compute the square
     * root of the given big integer up to roughly the given scale.
     * 
     * @param  scale scale of the <tt>BigDecimal</tt> square root to be returned
     * @return <tt>sqrt(num)</tt>
     */
    public static BigDecimal sqrt(final BigInteger num, final int scale) {
    	return sqrt(BigFraction.valueOf(num), scale);
    }
    /**
     * Babylonian square root method (Newton's method) to compute the square
     * root of the given big fraction number up to roughly the given scale.
     * 
     * @param  scale scale of the <tt>BigDecimal</tt> square root to be returned
     * @return <tt>sqrt(num)</tt>
     */
    public static BigDecimal sqrt(final BigFraction num, final int scale) {
    	if (num.isZero()) return BigDecimal.ZERO;
    	
    	BigFraction x0 = BigFraction.ZERO;
    	BigFraction x1 = estimateSqrt(num);
        
    	final int binaryScale = 1 + (int)(scale / LOG10_OF_2);
    	for (int i = 0; i < scale; i+=2) {
            x0 = x1;
            x1 = num.divide(x0);
            x1 = x1.add(x0);
            
            final int numLen	= x1.getNumerator().bitLength();
            final int denLen	= x1.getDenominator().bitLength();
            final int scaleDown = denLen - Math.max(0, numLen - denLen) - binaryScale;
            if (scaleDown > 0) {
            	x1 = new BigFraction(
               		x1.getNumerator().shiftRight(scaleDown + 1), 
               		x1.getDenominator().shiftRight(scaleDown)
           		);
            }
            else {
                x1 = x1.shiftRight(1);            	
            }
		}
        
        return x1.toBigDecimal(scale, RoundingMode.HALF_EVEN);
    }
    /**
     * Babylonian square root method (Newton's method) to compute the square
     * root of the given big decimal up to double precision.
     * 
     * @return <tt>sqrt(num)</tt>
     */
    public static double sqrt(final BigDecimal num) {
    	final int intScale	= num.precision() - num.scale();
    	final int scale		= 1 + (int)(52 * LOG10_OF_2 - intScale/2 + 1);
    	return sqrt(num, scale).doubleValue();
    }
    /**
     * Babylonian square root method (Newton's method) to compute the square
     * root of the given big integer up to double precision.
     * 
     * @return <tt>sqrt(num)</tt>
     */
    public static double sqrt(final BigInteger num) {
    	return sqrt(BigFraction.valueOf(num));
    }
    /**
     * Babylonian square root method (Newton's method) to compute the square
     * root of the given big fraction number up to double precision.
     * 
     * @return <tt>sqrt(num)</tt>
     */
    public static double sqrt(final BigFraction num) {
    	final int numBits	= num.getNumerator().bitLength();	//log2 estimate
		final int denBits	= num.getDenominator().bitLength();	//log2 estimate
		final int intBits	= numBits - denBits;
		final int scale		= 1 + (int)((52 - intBits/2) * LOG10_OF_2 + 1);
    	return sqrt(num, scale).doubleValue();
    }
  
    /** Estimate square root of val, used as start value for babylonian method*/
    private static BigInteger estimateSqrt(BigInteger val) {
    	return val.shiftRight(val.bitLength() / 2);
    }
    /** Estimate square root of val, used as start value for babylonian method*/
    private static BigDecimal estimateSqrt(BigDecimal val) {
    	return new BigDecimal(estimateSqrt(val.unscaledValue()), val.scale()/2);
    }
    /** Estimate square root of val, used as start value for babylonian method*/
    private static BigFraction estimateSqrt(BigFraction val) {
    	return new BigFraction(
    		estimateSqrt(val.getNumerator()), estimateSqrt(val.getDenominator())
    	);
    }

}
