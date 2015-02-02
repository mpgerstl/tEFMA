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
 * The <code>IntegerUtil</code> contains static utility methods concerning
 * integers (int and long). 
 */
public class IntegerUtil {
	
	/**
	 * Returns the signum of the long value, i.e. 1/-1/0 for a positive, 
	 * negative or zero value
	 * 
	 * @see Math#signum(double)
	 */
	public static int signum(long value) {
		return value == 0 ? 0 : value > 0 ? 1 : -1;
	}
	/**
	 * Returns the signum of the int value, i.e. 1/-1/0 for a positive, 
	 * negative or zero value
	 * 
	 * @see Math#signum(double)
	 */
	public static int signum(int value) {
		return value == 0 ? 0 : value > 0 ? 1 : -1;
	}
	
	/**
	 * Calculates the greatest common divisor of the specified integer numbers.
	 * This method might be useful to scale down a vector of integer numbers. 
	 * <p>
	 * If all numbers are negative, the resulting gcd is also negative. If all 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * 
	 * @param values	values for which the gcd is to be computed
	 * @return			gcd of all values, negative if all values negative, zero
	 * 					if all values zero, positive otherwise
	 */
	public static int gcd(Integer... values) {
		if (values.length == 0) return 1;
		int allSgn	= signum(values[0].intValue());
		int gcd 	= values[0].intValue();
		for (int i = 1; i < values.length; i++) {
			if (allSgn != signum(values[i].intValue())) allSgn = 1;
			if (gcd == 0 || gcd == 1) break;
			gcd = gcd(gcd, values[i].intValue());
		}
		return allSgn == 0 ? 0 : allSgn * gcd;
	}
	/**
	 * Calculates the greatest common divisor of the specified integer numbers.
	 * This method might be useful to scale down a vector of integer numbers. 
	 * <p>
	 * If all numbers are negative, the resulting gcd is also negative. If all 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * 
	 * @param values	values for which the gcd is to be computed
	 * @return			gcd of all values, negative if all values negative, zero
	 * 					if all values zero, positive otherwise
	 */
	public static int gcd(int... values) {
		if (values.length == 0) return 1;
		int allSgn	= signum(values[0]);
		int gcd 	= values[0];
		for (int i = 1; i < values.length; i++) {
			if (allSgn != signum(values[i])) allSgn = 1;
			if (gcd == 0 || gcd == 1) break;
			gcd = gcd(gcd, values[i]);
		}
		return allSgn == 0 ? 0 : allSgn * gcd;
	}
	/**
	 * Calculates the greatest common divisor of the specified long numbers.
	 * This method might be useful to scale down a vector of long numbers. 
	 * <p>
	 * If all numbers are negative, the resulting gcd is also negative. If all 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * 
	 * @param values	values for which the gcd is to be computed
	 * @return			gcd of all values, negative if all values negative, zero
	 * 					if all values zero, positive otherwise
	 */
	public static long gcd(Long... values) {
		if (values.length == 0) return 1;
		int allSgn	= signum(values[0].longValue());
		long gcd 	= values[0].longValue();
		for (int i = 1; i < values.length; i++) {
			if (allSgn != signum(values[i].longValue())) allSgn = 1;
			if (gcd == 0 || gcd == 1) break;
			gcd = gcd(gcd, values[i].longValue());
		}
		return allSgn == 0 ? 0 : allSgn * gcd;
	}
	/**
	 * Calculates the greatest common divisor of the specified long numbers.
	 * This method might be useful to scale down a vector of long numbers. 
	 * <p>
	 * If all numbers are negative, the resulting gcd is also negative. If all 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * 
	 * @param values	values for which the gcd is to be computed
	 * @return			gcd of all values, negative if all values negative, zero
	 * 					if all values zero, positive otherwise
	 */
	public static long gcd(long... values) {
		if (values.length == 0) return 1;
		int allSgn	= signum(values[0]);
		long gcd 	= values[0];
		for (int i = 1; i < values.length; i++) {
			if (allSgn != signum(values[i])) allSgn = 1;
			if (gcd == 0 || gcd == 1) break;
			gcd = gcd(gcd, values[i]);
		}
		return allSgn == 0 ? 0 : allSgn * gcd;
	}
	/**
	 * Returns the greatest common divisor of iA and iB using standard euclidian
	 * algorithm
	 */
    public static int gcd(int iA, int iB) {
        iA = Math.abs(iA);
        iB = Math.abs(iB);
        if (iA == 0) return iB;
        if (iB == 0) return iA;
        if (iA < 0 || iB < 0) {
        	//at least one must be MIN_VALUE, which is a even number
            if (0 != ((iA | iB) & 0x1)) {
            	//the other number is not even --> GCD=1
            	return 1;
            }
        	//both are even numbers, divide by 2
            iA = Math.abs(iA >>> 1);
            iB = Math.abs(iB >>> 1);
        }
        int iMax = Math.max(iA, iB);
        int iMin = Math.min(iA, iB);
        while (iMax != iMin) {
            if (iMax % iMin == 0) return iMin;
            int tmp = iMin;
            iMin = iMax - (iMax / iMin) * iMin;
            iMax = tmp;
        }
        return iMin;
    }
    
	/**
	 * The extended euclidean algorithm solves the equation
	 * <tt>a*x + b*y = gcd(a,b)</tt>.
	 * <p>
	 * Returns an int array containing 3 elements {x, y, gcd(a, b)}.
	 */
    public static int[] extendedEuclidean(int a, int b) {
	    int x = 0; 
	    int y = 1; 
	    int lastx = 1;
	    int lasty = 0;
	    while (b != 0) {
	        int temp = b;
	        final int quotient = a / b;
	        b = a % b;
	        a = temp;
	        
	        temp = x;
	        x = lastx-quotient*x;
	        lastx = temp;
	        
	        temp = y;
	        y = lasty-quotient*y;
	        lasty = temp;
	    }
       	return new int[] {lastx, lasty, a};
    }
    
	/**
	 * Computes the reciprocal or multiplicative inverse of a (modulo mod). Note
	 * that a is only invertible if a and mod are coprime. If this is not the
	 * case, an {@link ArithmeticException} is thrown.
	 * <p>
	 * The returned value <tt>inv(a)</tt> meets the following equality:
	 * <tt>inv(a) * a = 1 (modulo mod)</tt>.
	 * 
	 * @return 	the multiplicative inverse of a (modulo mod), if it exists, and 
	 * 			throws an exception otherwise.
	 * @throws ArithmeticException if a is not invertible (modulo mod)  
	 */
    public static int modularReciprocal(int a, int mod) {
    	final int origA		= a;
    	final int origMod	= mod;
	    int x = 0; 
	    int y = 1; 
	    int lastx = 1;
	    int lasty = 0;
	    while (mod != 0) {
	        int temp = mod;
	        final int quotient = a / mod;
	        mod = a % mod;
	        a = temp;
	        
	        temp = x;
	        x = lastx-quotient*x;
	        lastx = temp;
	        
	        temp = y;
	        y = lasty-quotient*y;
	        lasty = temp;
	    }
	    if (a != 1 && a != -1) {
	    	throw new ArithmeticException(
	    		origA + " is not invertible (mod " + origMod + ") " +
	    		"since gcd(" + origA + ", " + origMod + ") = " + a + " != 1"
	    	);
	    }
       	return lastx;
    }
    
	/**
	 * Returns the greatest common divisor of iA and iB using standard euclidian
	 * algorithm
	 */
    public static long gcd(long iA, long iB) {
        iA = Math.abs(iA);
        iB = Math.abs(iB);
        if (iA == 0) return iB;
        if (iB == 0) return iA;
        if (iA < 0 || iB < 0) {
        	//at least one must be MIN_VALUE, which is a even number
            if (0 != ((iA | iB) & 0x1)) {
            	//the other number is not even --> GCD=1
            	return 1;
            }
        	//both are even numbers, divide by 2
            iA = Math.abs(iA >>> 1);
            iB = Math.abs(iB >>> 1);
        }
        long iMax = Math.max(iA, iB);
        long iMin = Math.min(iA, iB);
        while (iMax != iMin) {
            if (iMax % iMin == 0) return iMin;
            long tmp = iMin;
            iMin = iMax - (iMax / iMin) * iMin;
            iMax = tmp;
        }
        return iMin;
    }
    
	/**
	 * The extended euclidean algorithm solves the equation
	 * <tt>a*x + b*y = gcd(a,b)</tt>.
	 * <p>
	 * Returns a long array containing 3 elements {x, y, gcd(a, b)}.
	 */
    public static long[] extendedEuclidean(long a, long b) {
	    long x = 0L; 
	    long y = 1L; 
	    long lastx = 1L;
	    long lasty = 0L;
	    while (b != 0L) {
	        long temp = b;
	        final long quotient = a / b;
	        b = a % b;
	        a = temp;
	        
	        temp = x;
	        x = lastx-quotient*x;
	        lastx = temp;
	        
	        temp = y;
	        y = lasty-quotient*y;
	        lasty = temp;
	    }
       	return new long[] {lastx, lasty, a};
    }

	/**
	 * Computes the reciprocal or multiplicative inverse of a (modulo mod). Note
	 * that a is only invertible if a and mod are coprime. If this is not the
	 * case, an {@link ArithmeticException} is thrown.
	 * <p>
	 * The returned value <tt>inv(a)</tt> meets the following equality:
	 * <tt>inv(a) * a = 1 (modulo mod)</tt>.
	 * 
	 * @return 	the multiplicative inverse of a (modulo mod), if it exists, and 
	 * 			throws an exception otherwise. 
	 * @throws ArithmeticException if a is not invertible (modulo mod)  
	 */
    public static long modularReciprocal(long a, long mod) {
    	final long origA	= a;
    	final long origMod	= mod;
	    long x = 0; 
	    long y = 1; 
	    long lastx = 1;
	    long lasty = 0;
	    while (mod != 0) {
	        long temp = mod;
	        final long quotient = a / mod;
	        mod = a % mod;
	        a = temp;
	        
	        temp = x;
	        x = lastx-quotient*x;
	        lastx = temp;
	        
	        temp = y;
	        y = lasty-quotient*y;
	        lasty = temp;
	    }
	    if (a != 1L && a != -1L) {
	    	throw new ArithmeticException(
	    		origA + " is not invertible (mod " + origMod + ") " +
	    		"since gcd(" + origA + ", " + origMod + ") = " + a + " != 1"
	    	);
	    }
       	return lastx;
    }
    
	/**
	 * Computes the reciprocal or multiplicative inverse of a (modulo 2^32). 
	 * Note that a is only invertible if a and 2^32 are coprime, that is, if a
	 * is odd. If this is not the case, an {@link ArithmeticException} is 
	 * thrown.
	 * <p>
	 * The returned value <tt>inv(a)</tt> meets the following equality:
	 * <tt>inv(a) * a = 1 (modulo 2^32)</tt>.
	 * 
	 * @return 	the multiplicative inverse of a (modulo 2^32), if it exists (if 
	 * 			a is odd), and throws an exception otherwise.
	 * @throws ArithmeticException	if a is not invertible (modulo 2^32), that 
	 * 								is, iv a is even  
	 */
	public static int modularReciprocal2pow32(int a) {
		//we cannot use modularReciprocal directly 
		//since 2^32 is too large for int.
		//thus, we compute the inverse x1 (mod 2^31)
		//we want x in
		//1 = a * x + 2^32 * y (mod 2^32)
		//let us find first
		//1 = a * x1 + 2^31 * y1 (mod 2^31)
		//now, square
		//1 = a^2 * x1^2 + 2*a*x1*2^31 + 2^62*y1^2
		//all terms but the first are multiples of 2^32 and thus 0 (mod 2^32), hence
		//1 = a * (a * x1^2) (mod 2^32)
		//and
		//inv(a) = a * x1^2 (mod 2^32)
		final int x1 = IntegerUtil.modularReciprocal(a, 1 << 31);
		return a * x1 * x1;
	}
	/**
	 * Computes the reciprocal or multiplicative inverse of a (modulo 2^64). 
	 * Note that a is only invertible if a and 2^64 are coprime, that is, if a
	 * is odd. If this is not the case, an {@link ArithmeticException} is 
	 * thrown.
	 * <p>
	 * The returned value <tt>inv(a)</tt> meets the following equality:
	 * <tt>inv(a) * a = 1 (modulo 2^64)</tt>.
	 * 
	 * @return 	the multiplicative inverse of a (modulo 2^64), if it exists (if 
	 * 			a is odd), and throws an exception otherwise.
	 * @throws ArithmeticException	if a is not invertible (modulo 2^64), that 
	 * 								is, iv a is even  
	 */
	public static long modularReciprocal2pow64(long a) {
		//we cannot use modularReciprocal directly 
		//since 2^64 is too large for int.
		//thus, we compute the inverse x1 (mod 2^63)
		//we want x in
		//1 = a * x + 2^64 * y (mod 2^64)
		//let us find first
		//1 = a * x1 + 2^63 * y1 (mod 2^63)
		//now, square
		//1 = a^2 * x1^2 + 2*a*x1*2^63 + 2^126*y1^2
		//all terms but the first are multiples of 2^64 and thus 0 (mod 2^64), hence
		//1 = a * (a * x1^2) (mod 2^64)
		//and
		//inv(a) = a * x1^2 (mod 2^64)
		final long x1 = IntegerUtil.modularReciprocal(a, 1L << 63);
		return a * x1 * x1;
	}
     
    private IntegerUtil() {
        //no instances
    }
    
}
