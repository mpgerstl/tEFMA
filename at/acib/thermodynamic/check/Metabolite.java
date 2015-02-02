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

import at.acib.thermodynamic.exception.ConcentrationErrorException;

/**
 * Container class for metabolite information
 * 
 * @author matthias
 * 
 */
public class Metabolite {

	private String m_name;
	private String m_abbr;
	private double m_cmin;
	private double m_cmax;
	private double m_dfG;

	/**
	 * constructor
	 * 
	 * @param abbreviation
	 *            if the metabolite used in mfile
	 * @param name
	 *            of the metabolite used in thermodynamic file
	 * @param cmin
	 *            minimum concentration
	 * @param cmax
	 *            maximum concentration
	 * @param dfG
	 *            delta G0 of formation
	 */
	public Metabolite(String abbreviation, String name, double cmin, double cmax, double dfG) throws ConcentrationErrorException {
		m_abbr = abbreviation;
		m_name = name;
		m_cmin = cmin;
		m_cmax = cmax;
		m_dfG = name.equalsIgnoreCase("proton") ? 0 : dfG;
		if (Math.log(m_cmin) > Math.log(m_cmax)) {
			throw new ConcentrationErrorException(m_name, m_cmin, m_cmax);
		}
	}

	/**
	 * @return abbreviation used in mfile
	 */
	protected String getAbbr() {
		return m_abbr;
	}

	/**
	 * @return name of metabolite used in thermodynamic file
	 */
	protected String getName() {
		return m_name;
	}

	/**
	 * @return minimum concentration of the metabolite
	 */
	protected double getCmin() {
		return m_cmin;
	}

	/**
	 * @return maximum concentration of the metabolite
	 */
	protected double getCmax() {
		return m_cmax;
	}

	/**
	 * @return delta G0 of formation
	 */
	protected double getDfg0() {
		return m_dfG;
	}

}
