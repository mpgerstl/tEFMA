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
package ch.javasoft.math.varint;



/**
 * The <code>VarIntUtil</code> contains static utility methods concerning
 * {@link VarInt} numbers
 */
public class VarIntUtil {
	
	/**
	 * Calculates the greatest common divisor of the specified {@link VarInt} 
	 * numbers.
	 * This method might be useful to scale down a vector of variable integers. 
	 * <p>
	 * If all numbers are negative, the resulting gcd is also negative. If all 
	 * numbers are zero, the result is zero. Otherwise, the result is positive.
	 * 
	 * @param values	values for which the gcd is to be computed
	 * @return			gcd of all values, negative if all values negative, zero
	 * 					if all values zero, positive otherwise
	 */
	public static VarIntNumber gcd(VarInt... values) {
		if (values.length == 0) return VarInt.ONE;
		int allSgn	= values[0].signum();
		VarInt gcd 	= values[0];
		for (int i = 1; i < values.length; i++) {
			if (allSgn != values[i].signum()) allSgn = 1;
			if (gcd.isZero() || gcd.isOne()) break;
			if (!values[i].isZero()) {
				gcd = gcd.gcd(values[i]);
			}
		}
		if (allSgn == 0) return VarInt.ZERO;
		if (allSgn < 0) {
			return VarIntFactory.convert(gcd.negate());
		}
		return VarIntFactory.convert(gcd);
	}
	
	/**
	 * Calculates the least common multiple of the specified {@link VarInt} 
	 * numbers.
	 * This method might be useful to scale up a vector of fractions to convert
	 * them to integers.
	 * <p>
	 * The result is negative if an uneven number of single values is negative,
	 * one for no values or if all values are zero and positive otherwise.
	 * 
	 * @param values	values for which the lcm is to be computed
	 * @return			lcm of all values, negative for uneven number of 
	 * 					negative values, one for no values or if all values are zero 
	 * 					and positive otherwise
	 */
	public static VarIntNumber lcm(VarInt... values) {
		if (values.length == 0) return VarInt.ONE;
		VarInt lcm = values[0];
		for (int i = 1; i < values.length; i++) {
			if (values[i].signum() != 0) {
				final VarInt gcd = lcm.gcd(values[i]);
				if (gcd.isOne()) {
					lcm = lcm.multiply(values[i]);
				}
				else {
					if (!values[i].equals(gcd)) {
						lcm = lcm.multiply(values[i].divide(gcd));
					}
				}
			}
		}
		return VarIntFactory.convert(lcm);
	}
	
    private VarIntUtil() {
        //no instances
    }
    
}
