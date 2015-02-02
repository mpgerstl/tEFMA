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


package at.acib.thermodynamic;

import ch.javasoft.metabolic.MetabolicNetwork;

/**
 * hold thermodynamic parameters given by command line
 * 
 * @author matthias
 */
public class ThermodynamicParameters {

	public final static String THERMO_SPEED = "speed";
	public final static String THERMO_MEMORY = "memory";

	private static String m_concentrationFile;
	private static String m_thermodynamicFile;
	private static String m_infeasiblePatternFile;
	private static String m_lpFile;
	private static String m_lpVariableFile;
	private static String m_manner = THERMO_MEMORY;
	private static double m_stdMin = 1e-7;
	private static double m_stdMax = 1;
	private static double m_pH = 7;
	private static double m_is = 0.15;
	private static double m_temperature = 310.15;
	private static int m_thermoThreads = 0;
	private static boolean thermoSet = false;
	private static boolean m_printPattern = false;
	private static boolean m_lpSet = false;
	private static boolean m_lpVarSet = false;
	private static MetabolicNetwork m_metaNet;

	/**
	 * @param infeasiblePatternFile
	 *            name of file to store infeasible pattern
	 */
	public static void setInfeasiblePatternFile(String infeasiblePatternFile) {
		m_infeasiblePatternFile = infeasiblePatternFile;
		m_printPattern = true;
	}

	/**
	 * minimum information needed to run thermodynamic check
	 * 
	 * @param thermodynamicFile
	 *            file name of thermodynamic data
	 */
	public static void setThermodynamicFile(String thermodynamicFile) {
		m_thermodynamicFile = thermodynamicFile;
		thermoSet = true;
	}

	/**
	 * set name for linear problem output file
	 * 
	 * @param lpFile
	 */
	public static void setLpFile(String lpFile) {
		m_lpFile = lpFile;
		m_lpSet = true;
	}

	/**
	 * set output filename for linear problem variables
	 * 
	 * @param lpVariableFile
	 */
	public static void setLpVariableFile(String lpVariableFile) {
		m_lpVariableFile = lpVariableFile;
		m_lpVarSet = true;
	}

	/**
	 * original metabolic network is needed to define each single reaction, as
	 * the metabolic network is then be compressed during preprocessing
	 * 
	 * @param metaNet
	 *            {@link MetabolicNetwork}
	 */
	public static void setOriginalMetabolicNetwork(final MetabolicNetwork metaNet) {
		m_metaNet = metaNet;
	}

	/**
	 * @param concentrationFile
	 *            file name of metabolite concentration data
	 */
	public static void setConcentrationFile(String concentrationFile) {
		m_concentrationFile = concentrationFile;
	}

	public static void setManner(String manner) {
		if (manner.equalsIgnoreCase(THERMO_SPEED)) {
			m_manner = THERMO_SPEED;
		} else if (manner.equalsIgnoreCase(THERMO_MEMORY)) {
			m_manner = THERMO_MEMORY;
		}
	}

	/**
	 * @param stdMin
	 *            standard minimum concentration value of metabolites
	 */
	public static void setStdMin(double stdMin) {
		m_stdMin = stdMin;
	}

	/**
	 * @param stdMax
	 *            standard maximum concentration value of metabolites
	 */
	public static void setStdMax(double stdMax) {
		m_stdMax = stdMax;
	}

	/**
	 * @param temperature
	 *            temperature of environment in K
	 */
	public static void setTemperature(double temperature) {
		m_temperature = temperature;
	}

	/**
	 * @param pH
	 *            value of environment
	 */
	public static void setPH(double pH) {
		m_pH = pH;
	}

	/**
	 * @param ionic
	 *            strength of environment
	 */
	public static void setIonicStrength(double is) {
		m_is = is;
	}

	/**
	 * @param thermoThreads
	 *            maximum number of threads to use for thermodynamic feasibility
	 *            check
	 */
	public static void setThermoThreads(int thermoThreads) {
		m_thermoThreads = thermoThreads;
	}

	/**
	 * @return if thermodynamic check can be used
	 */
	public static boolean isActive() {
		return (thermoSet);
	}

	/**
	 * @return name of thermodynamic file
	 */
	public static String getThermodynamicFile() {
		return m_thermodynamicFile;
	}

	/**
	 * @return original metabolic network
	 */
	public static MetabolicNetwork getOriginalMetabolicNetwork() {
		return m_metaNet;
	}

	/**
	 * @return name of metabolite concentration file
	 */
	public static String getConcentrationFile() {
		return m_concentrationFile;
	}

	/**
	 * @return output filename for lp file
	 */
	public static String getLpFile() {
		return m_lpFile;
	}

	/**
	 * @return output filename for lp variables
	 */
	public static String getLpVariableFile() {
		return m_lpVariableFile;
	}

	/**
	 * @return standard value of minimum concentration values
	 */
	public static double getStdMin() {
		return m_stdMin;
	}

	/**
	 * @return standard value of maximum concentration values
	 */
	public static double getStdMax() {
		return m_stdMax;
	}

	/**
	 * @return temperature of the environment in K
	 */
	public static double getTemperature() {
		return m_temperature;
	}

	/**
	 * @return pH value of the environment
	 */
	public static double getPH() {
		return m_pH;
	}

	/**
	 * @return ionic strength of the environment
	 */
	public static double getIonicStrength() {
		return m_is;
	}

	/**
	 * @return max threads to use for thermodynamic feasibility check
	 */
	public static int getThermoThreads() {
		return m_thermoThreads;
	}

	/**
	 * @return filename for infeasible patterns
	 */
	public static String getInfeasiblePatternFile() {
		return m_infeasiblePatternFile;
	}

	/**
	 * @return true if infeasible patterns should be printed to file
	 */
	public static boolean isPatternPrinted() {
		return m_printPattern;
	}

	/**
	 * @return true if lp should be written to file
	 */
	public static boolean isLpPrinted() {
		return m_lpSet;
	}

	/**
	 * @return true if lp variables should be written to file
	 */
	public static boolean isLpVariablePrinted() {
		return m_lpVarSet;
	}

	public static String getManner() {
		return m_manner;
	}

}
