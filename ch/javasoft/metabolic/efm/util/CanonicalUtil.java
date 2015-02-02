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

import ch.javasoft.math.NumberOperations;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;

/**
 * Some static utility functions concerning the canonical EFM approach 
 */
public class CanonicalUtil {
	
	/**
	 * Returns the value stoich[row]*fluxVals, where stoich is an m times q 
	 * matrix and fluxVals a (column) vector of length q. 
	 * <p>
	 * The returned value should probably be rounded around zero
	 */
	public static double getInequalityValue(ReadableMatrix<Double> stoich, int row, double[] fluxVals, int[] stoichColumnSorting) {
		double sum = 0d;
		if (stoich instanceof ReadableDoubleMatrix) {
			final ReadableDoubleMatrix<Double> dblMx = (ReadableDoubleMatrix<Double>)stoich;
			for (int i = 0; i < fluxVals.length; i++) {
				final int stoichCol = stoichColumnSorting[i];
				sum += fluxVals[i] * dblMx.getDoubleValueAt(row, stoichCol);
			}
		}
		else {
			for (int i = 0; i < fluxVals.length; i++) {
				final int stoichCol = stoichColumnSorting[i];
				sum += fluxVals[i] * stoich.getNumberValueAt(row, stoichCol).doubleValue();
			}
		}
		return sum;
	}
	/**
	 * Returns the value stoich[row]*fluxVals, where stoich is an m times q 
	 * matrix and fluxVals a (column) vector of length q. 
	 * <p>
	 * The returned value is not yet canceled.
	 */
	public static <N extends Number> N getInequalityValue(ReadableMatrix<N> stoich, int row, N[] fluxVals, int[] stoichColumnSorting) {
		final NumberOperations<N> numOps = stoich.getNumberOperations();
		N sum = numOps.zero();
		for (int i = 0; i < fluxVals.length; i++) {
			final int stoichCol = stoichColumnSorting[i];
			sum = numOps.add(sum, numOps.multiply(fluxVals[i], stoich.getNumberValueAt(row, stoichCol)));
		}
		return sum;
	}
	
	//no instances
	private CanonicalUtil() {}
}
