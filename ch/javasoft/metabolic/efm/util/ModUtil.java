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
package ch.javasoft.metabolic.efm.util;

import java.math.BigInteger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.numeric.IntegerUtil;

/**
 * <tt>ModUtil</tt> contains static utility methods concerning modulo 
 * computations
 */
public class ModUtil {

	/**
	 * Converts the given rational stoichiometric matrix into an int matrix. The
	 * values of the returned matrix are residue values, for every original 
	 * rational value <tt>v=n/d</tt>, the residue value is 
	 * <tt>u=(n mod p)(inv(d) mod p) mod p</tt>. The conversion is save, that 
	 * is, if any denominator is not coprime with p, the row is multiplied by
	 * an appropriate value before conversion.  
	 */
	public static int[][] toIntArray(final ReadableMatrix<BigFraction> stoich, final int iprime) {
		return toIntArray(stoich, iprime, stoich.getRowCount());
	}
	/**
	 * Converts the given rational stoichiometric matrix into an int matrix. The
	 * values of the returned matrix are residue values, for every original 
	 * rational value <tt>v=n/d</tt>, the residue value is 
	 * <tt>u=(n mod p)(inv(d) mod p) mod p</tt>. The conversion is save, that 
	 * is, if any denominator is not coprime with p, the row is multiplied by
	 * an appropriate value before conversion.  
	 * <p>
	 * Here, only the first <tt>rows</tt> number of rows are considered. This is
	 * usefull if some preprocessing has put zero rows to the end of the matrix.
	 */
	public static int[][] toIntArray(final ReadableMatrix<BigFraction> stoich, final int iprime, final int rows) {
        final BigInteger prime = BigInteger.valueOf(iprime);
        final int cols = stoich.getColumnCount();
        final int[][] arr = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
        	int maxPowOfMod = 0;
        	//we must ensure that the denominator is not a multiple of our
        	//prime, since this would lead to a "division by zero".
        	//if any denominator contains prime factors, we multiply the whole 
        	//row with those prime factors
            for (int col = 0; col < cols; col++) {
                if (stoich.getSignumAt(row, col) != 0) {
//                	BigInteger den = stoich.getBigIntegerDenominatorAt(row, col);
                	BigInteger den = stoich.getNumberValueAt(row, col).getDenominator();
                	int powOfMod = 0;
                	while (den.remainder(prime).intValue() == 0) {
                		powOfMod++;
                		den = den.divide(prime);
                	}
                	maxPowOfMod = Math.max(maxPowOfMod, powOfMod);
                }
            }
            final BigFraction mul = maxPowOfMod == 0 ? null : BigFraction.valueOf(prime.pow(maxPowOfMod));
            for (int col = 0; col < cols; col++) {
                if (stoich.getSignumAt(row, col) != 0) {
                    BigFraction val = stoich.getNumberValueAt(row, col);
                    if (mul != null) {
                    	val = val.multiply(mul).reduce();
                    }
                    final BigInteger num = val.getNumerator();
                    final BigInteger den = val.getDenominator();
                    final int iNum = num.remainder(prime).intValue();
                    final int iDen = den.remainder(prime).intValue();
                    final long inv = invert(iDen, iprime);
                    arr[row][col] = (int)((iNum * inv) % iprime);
                }
                else {
                    arr[row][col] = 0;
                }
            }
        }
		return arr;
	}
	/**
	 * Converts the given rational stoichiometric matrix into an int matrix. The
	 * values of the returned matrix are residue values. For every original 
	 * rational value <tt>v=n/d</tt>, the row is multiplied with the least 
	 * common multiple (LCM) of the denominators d(i). Then, the residue value
	 * is <tt>u=(n * LCM / d) mod p</tt>.
	 */
	public static int[][] toIntArrayNoInversion(final ReadableMatrix<BigFraction> stoich, final int iprime) {
		return toIntArrayNoInversion(stoich, iprime, stoich.getRowCount());
	}
	/**
	 * Converts the given rational stoichiometric matrix into an int matrix. The
	 * values of the returned matrix are residue values. For every original 
	 * rational value <tt>v=n/d</tt>, the row is multiplied with the least 
	 * common multiple (LCM) of the denominators d(i). Then, the residue value
	 * is <tt>u=(n * LCM / d) mod p</tt>.
	 * <p>
	 * Here, only the first <tt>rows</tt> number of rows are considered. This is
	 * usefull if some preprocessing has put zero rows to the end of the matrix.
	 */
	public static int[][] toIntArrayNoInversion(final ReadableMatrix<BigFraction> stoich, final int iprime, final int rows) {
		final BigInteger prime = BigInteger.valueOf(iprime);
        final int cols = stoich.getColumnCount();
        final int[][] arr = new int[rows][cols];
        for (int row = 0; row < rows; row++) {
        	final BigInteger lcm = BigInteger.ONE;
            for (int col = 0; col < cols; col++) {
                if (stoich.getSignumAt(row, col) != 0) {
//                	BigInteger den = stoich.getBigIntegerDenominatorAt(row, col);
                	final BigInteger den = stoich.getNumberValueAt(row, col).getDenominator();
                	final BigInteger gcd = lcm.gcd(den);
                	if (!gcd.equals(den)) {
                		lcm.multiply(den);
                		lcm.divide(gcd);
                	}
                }
            }
            final boolean isOne = lcm.equals(BigInteger.ONE);
            for (int col = 0; col < cols; col++) {
                if (stoich.getSignumAt(row, col) != 0) {
                    final BigFraction val = stoich.getNumberValueAt(row, col);
                    BigInteger biv = val.getNumerator();
                    if (!isOne) biv = biv.multiply(lcm).divide(val.getDenominator());
                    arr[row][col] = biv.mod(prime).intValue();
                }
                else {
                    arr[row][col] = 0;
                }
            }
        }
		return arr;
	}
	
	/**
	 * Computes the reciprocal or multiplicative inverse of a (modulo prime). 
	 * Note that a is only invertible if a and prime are coprime. If this is not 
	 * the case, an {@link ArithmeticException} is thrown.
	 * <p>
	 * The returned value <tt>inv(a)</tt> meets the following equality:
	 * <tt>inv(a) * a = 1 (modulo prime)</tt>.
	 * 
	 * @return 	the multiplicative inverse of a (modulo prime), if it exists, 
	 * 			and throws an exception otherwise.
	 * @throws ArithmeticException if a is not invertible (modulo prime)  
	 */
	public static int invert(final int a, final int prime) {
		return IntegerUtil.modularReciprocal(a, prime);
	}
	
	//no instances
	private ModUtil() {}
}
