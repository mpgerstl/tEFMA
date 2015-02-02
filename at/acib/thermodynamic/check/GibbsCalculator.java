/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2015, Matthias P. Gerstl, Vienna, Austria
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


package at.acib.thermodynamic.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * calculate gibbs energies
 * 
 * @author matthias
 */
public class GibbsCalculator {

	private static final double R = 8.31451;

	private double rt;
	private double pH_part;
	private double is_part;
	/**
	 * {@link HashMap} < name of species, instance of {@link Species} >
	 */
	private HashMap<String, Species> species;
	/**
	 * {@link HashMap} < name of species, dfG value >
	 */
	private HashMap<String, Double> dfG_map;

	/**
	 * @param temperature
	 *            [K]
	 * @param is
	 *            ionic strength [M]
	 * @param pH
	 *            pH value
	 * @param thermodynamicDataFilename
	 */
	public GibbsCalculator(double temperature, double is, double pH, String thermodynamicDataFilename) {
		rt = R * temperature / 1000;
		pH_part = rt * Math.log(Math.pow(10, -pH));
		double is_pow = Math.pow(is, 0.5);
		is_part = 2.91482 * is_pow / (1 + (1.6 * is_pow));
		dfG_map = new HashMap<String, Double>();
		SpeciesDataReader reader = new SpeciesDataReader(thermodynamicDataFilename);
		species = reader.getSpecies();
		Set<String> specKeys = species.keySet();
		ArrayList<String> l = new ArrayList<String>(specKeys);
		Collections.sort(l);
		Iterator<String> specIter = l.iterator();
		while (specIter.hasNext()) {
			Species temp = species.get(specIter.next());
			int[] tempNH = temp.getNH();
			int[] tempZI = temp.getZi();
			double[] tempDG0 = temp.getDGzero();
			double[] exponents = new double[tempNH.length];
			for (int i = 0; i < tempNH.length; i++) {
				double pHterm = tempNH[i] * pH_part;
				double isterm = (Math.pow(tempZI[i], 2) - tempNH[i]) * is_part;
				double gpfnsp = tempDG0[i] - pHterm - isterm;
				exponents[i] = -gpfnsp / rt;
			}

			double dfG = -rt * logSumOfExponentials(exponents);
			dfG_map.put(temp.getName(), dfG);
		}
	}

	/**
	 * @param name
	 *            of metabolite
	 * @return Double value of calculated delta G of formation
	 */
	protected Double getDfG(String name) {
		return dfG_map.get(name);
	}

	/**
	 * @param dfG0
	 * @param charge
	 * @param hAtoms
	 * @return calculated delta G of formation
	 */
	protected double getDfG(double dfG0, int charge, int hAtoms) {
		double pH_term = hAtoms * pH_part;
		double is_term = (Math.pow(charge, 2) - hAtoms) * is_part;
		double dfG = dfG0 - pH_term - is_term;
		return dfG;
	}

	/**
	 * calculates the sum of exponentials it is needed for high values
	 * 
	 * @param xs
	 *            array of exponential values
	 * @return double value
	 */
	private double logSumOfExponentials(double[] xs) {
		if (xs.length == 1) {
			return xs[0];
		}
		double max = maxOfDoubleArray(xs);
		double sum = 0.0;
		for (int i = 0; i < xs.length; ++i) {
			if (xs[i] != Double.NEGATIVE_INFINITY) {
				sum += Math.exp(xs[i] - max);
			}
		}
		return max + Math.log(sum);
	}

	/**
	 * searches the maximum value of a double array
	 * 
	 * @param x
	 *            array of double values
	 * @return maximum value
	 */
	private double maxOfDoubleArray(double[] x) {
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < x.length; i++) {
			if (x[i] > max) {
				max = x[i];
			}
		}
		return max;
	}

}
