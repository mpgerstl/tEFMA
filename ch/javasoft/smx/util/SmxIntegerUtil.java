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
package ch.javasoft.smx.util;

import ch.javasoft.smx.iface.WritableIntRationalMatrix;
import ch.javasoft.util.numeric.IntegerUtil;


/**
 * The <code>SmxIntegerUtil</code> TODO type javadoc for SmxIntegerUtil
 */
public class SmxIntegerUtil {

	public static int checkIntegerRange(long value) throws ArithmeticException {
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new ArithmeticException("integer overflow: " + value);
        }
        return (int)value;
    }
    
    public static void checkLongRangeAfterAddition(long summandA, long summandB, long sum) throws ArithmeticException {
        int sigA = IntegerUtil.signum(summandA);
        if (sigA == IntegerUtil.signum(summandB) && sigA != IntegerUtil.signum(sum)) {
            throw new ArithmeticException("long overflow: " + sum);                
        }
    }
    public static void checkLongRangeAfterMultiplication(long factorA, long factorB, long product) throws ArithmeticException {
        if (IntegerUtil.signum(factorA) * IntegerUtil.signum(factorB) != IntegerUtil.signum(product)) {
            throw new ArithmeticException("long overflow: " + product);                
        }
    }
    
    public static void multiply(int dividendA, int divisorA, int dividendB, int divisorB, WritableIntRationalMatrix dst, int row, int col) {
        if (divisorA == 0 || divisorB == 0) {
            throw new ArithmeticException("div by 0");
        }
        if (dividendA == 0 || dividendB == 0) {
            dst.setValueAt(row, col, 0, 1);
        }
        else {
            long newDividend	= ((long)dividendA) * ((long)dividendB);
            long newDivisor	= ((long)divisorA) * ((long)divisorB);
            long gcd			= IntegerUtil.gcd(newDividend, newDivisor);
            newDividend	/= gcd;
            newDivisor	/= gcd;
            checkIntegerRange(newDividend);
            checkIntegerRange(newDivisor);
            dst.setValueAt(row, col, (int)newDividend, (int)newDivisor);
        }        
    }
    
    public static void add(int dividendA, int divisorA, int dividendB, int divisorB, WritableIntRationalMatrix dst, int row, int col) {
        if (divisorA == divisorB) {
            long sum	= ((long)dividendA) + ((long)dividendB);
            long gcd	= IntegerUtil.gcd(sum, divisorA);
            sum /= gcd;
            checkIntegerRange(sum);
            dst.setValueAt(row, col, (int)sum, divisorA / (int)gcd /*can't be larger than an int since 1 input was an int*/);
        }
        else {            
            long prodA	= ((long)dividendA) * ((long)divisorB);
            long prodB	= ((long)dividendB) * ((long)divisorA);
            long dividend	= prodA + prodB;
            checkLongRangeAfterAddition(prodA, prodB, dividend);
            long divisor	= ((long)dividendA) * ((long)dividendB);
            long gcd		= IntegerUtil.gcd(dividend, divisor);
            dividend		/= gcd;
            divisor		/= gcd;
            checkIntegerRange(dividend);
            checkIntegerRange(divisor);
            dst.setValueAt(row, col, (int)dividend, (int)divisor);
        }        
    }
    
    private SmxIntegerUtil() {
        //no instances
    }
}
