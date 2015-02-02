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
package ch.javasoft.metabolic.util;

import java.math.BigDecimal;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.BigMath;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.numeric.Zero;

/**
 * Normalizes flux values, meaning that either the maximum or minimum flux value 
 * becomes 1, scaling the whole flux vector appropriately.
 */
public class FluxNormalizer {
	
	public static void normalize(MetabolicNetwork net, double[] values, Zero zero, boolean normalizeMax) {
		normalize(net, values, normalizeMax, zero, true);
	}
	public static void normalizeMax(MetabolicNetwork net, double[] values, Zero zero) {
		normalize(net, values, true, zero, true);
	}
	public static void normalizeMin(MetabolicNetwork net, double[] values, Zero zero) {
		normalize(net, values, false, zero, true);
	}
	public static void normalizeNorm2(MetabolicNetwork net, double[] values, Zero zero) {
		double sumSquared = 0d;
		for (int i = 0; i < values.length; i++) {
			sumSquared += values[i] * values[i];
		}
		final double div = Math.sqrt(sumSquared);
		for (int i = 0; i < values.length; i++) {
			values[i] /= div;
		}
	}
	public static void normalizeSquared(MetabolicNetwork net, double[] values, Zero zero) {
		double sumSquared = 0d;
		for (int i = 0; i < values.length; i++) {
			double sgn = Math.signum(values[i]);
			values[i] *= values[i];
			sumSquared += values[i];
			values[i] *= sgn;
		}
		for (int i = 0; i < values.length; i++) {
			values[i] /= sumSquared;
		}
	}
	private static void normalize(MetabolicNetwork net, double[] values, boolean max, Zero zero, boolean normalizeOnlyIntegerRatios) {
		ArrayIterable<? extends Reaction> reacts = net == null ? null : net.getReactions();
		if (reacts != null && values.length != reacts.length()) {
			throw new IllegalArgumentException(
				"reaction count != value count: " + reacts.length() + " != " + values.length
			);
		}
		double refValue = max ? 0.0d : Double.MAX_VALUE;
		double iRefValue = max ? 0.0d : Double.MAX_VALUE;
		for (int ii = 0; ii < values.length; ii++) {
			double absValue = Math.abs(values[ii]);
			if (zero.isNonZero(absValue)) {
				refValue = max ? Math.max(refValue, absValue) : Math.min(refValue, absValue);
				if (!normalizeOnlyIntegerRatios || reacts == null || reacts.get(ii).hasIntegerRatios()) {
					iRefValue = max ? Math.max(iRefValue, absValue) : Math.min(iRefValue, absValue);
				}
			}				
		}
		if (iRefValue != 0.0d && iRefValue != Double.MAX_VALUE) refValue = iRefValue;			
		if (refValue == 0.0d || refValue == Double.MAX_VALUE) return;
		for (int ii = 0; ii < values.length; ii++) {
			values[ii] /= refValue;
//			if (round) {
//				if (zero.isZero(values[ii])) values[ii] = 0.0d;//avoid -0.0d
//				else values[ii] = DoubleUtil.round(values[ii], zero.mPrecision);
//			}
		}
	}
	public static <N extends Number> void normalizeMax(MetabolicNetwork net, N[] values, NumberOperations<N> numberOps, Zero zero) {
		normalize(net, values, numberOps, true, zero, true);
	}
	public static <N extends Number> void normalizeMin(MetabolicNetwork net, N[] values, NumberOperations<N> numberOps, Zero zero) {
		normalize(net, values, numberOps, false, zero, true);
	}
	/**
	 * Supported for {@link BigDecimal} and {@link BigFraction} values.
	 * For <tt>BigDecimal</tt>, the original scale is used to round the square
	 * roots. For <tt>BigFraction</tt>, the scale is chosen such that 
	 * square roots are &quot;exact&quot; if converted into double numbers.
	 */
	public static <N extends Number> void normalizeNorm2(MetabolicNetwork net, N[] values, NumberOperations<N> numberOps, Zero zero) {
		if (values instanceof BigDecimal[]) {
			normalizeSquared(net, values, numberOps, zero);
			final BigDecimal[] vals = (BigDecimal[])values;
			for (int i = 0; i < vals.length; i++) {
				final int sgn = vals[i].signum();
				if (sgn < 0) {
					vals[i] = BigMath.sqrt(vals[i].abs(), vals[i].scale()).negate();
				}
				else if (sgn > 0) {
					vals[i] = BigMath.sqrt(vals[i], vals[i].scale());
				}
			}
		}
		else if (values instanceof BigFraction[]) {
			normalizeSquared(net, values, numberOps, zero);
			for (int i = 0; i < values.length; i++) {
				final int sgn = numberOps.signum(values[i]);
				if (sgn != 0) {
					final double sqrt;
					if (sgn < 0) {
						BigFraction abs = (BigFraction)numberOps.abs(values[i]);
						sqrt = -BigMath.sqrt(abs);
					}
					else {
						sqrt = BigMath.sqrt((BigFraction)values[i]);
					}
					values[i] = numberOps.valueOf(sqrt);
				}
			}
		}
		else {
			throw new RuntimeException("not implemented for type " + values.getClass().getComponentType().getName());
		}
 	}
	public static <N extends Number> void normalizeSquared(MetabolicNetwork net, N[] values, NumberOperations<N> numberOps, Zero zero) {
		N sumSquared = numberOps.zero();
		for (int i = 0; i < values.length; i++) {
			final int sgn = numberOps.signum(values[i]);
			if (sgn != 0) {
				values[i]	= numberOps.multiply(values[i], values[i]);
				sumSquared	= numberOps.add(sumSquared, values[i]);
				sumSquared	= numberOps.reduce(sumSquared);
				if (sgn < 0) values[i] = numberOps.negate(values[i]);
			}
		}
		for (int i = 0; i < values.length; i++) {
			if (numberOps.isNonZero(values[i])) {
				values[i] = numberOps.divide(values[i], sumSquared);
				values[i] = numberOps.reduce(values[i]);
			}
		}
	}
	private static <N extends Number> void normalize(MetabolicNetwork net, N[] values, NumberOperations<N> numberOps, boolean max, Zero zero, boolean normalizeOnlyIntegerRatios) {		
		ArrayIterable<? extends Reaction> reacts = net == null ? null : net.getReactions();
		if (reacts != null && values.length != reacts.length()) {
			throw new IllegalArgumentException(
				"reaction count != value count: " + reacts.length() + " != " + values.length
			);
		}
		N refValue	= null;
		N iRefValue = null;
		for (int ii = 0; ii < values.length; ii++) {
			final N absValue = numberOps.abs(values[ii]);
			if (0 != sgn(absValue, numberOps, zero)) {
				refValue = refValue == null ? absValue : (max ? numberOps.max(refValue, absValue) : numberOps.min(refValue, absValue));
				if (!normalizeOnlyIntegerRatios || reacts == null || reacts.get(ii).hasIntegerRatios()) {
					iRefValue = iRefValue == null ? absValue : (max ? numberOps.max(iRefValue, absValue) : numberOps.min(iRefValue, absValue));
				}
			}				
		}
		if (iRefValue != null && 0 != sgn(iRefValue, numberOps, zero)) refValue = iRefValue;
		if (refValue == null || 0 == sgn(refValue, numberOps, zero)) return;
		for (int ii = 0; ii < values.length; ii++) {
			values[ii] = numberOps.divide(values[ii], refValue);
			values[ii] = numberOps.reduce(values[ii]);
		}
	}
	private static <N extends Number> int sgn(N value, NumberOperations<N> numberOps, Zero zero) {
		return zero == null ? numberOps.signum(value) : zero.sgn(value.doubleValue());
	}
	
}
