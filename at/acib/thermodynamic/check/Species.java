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

/**
 * Container class to store information on metabolites
 * 
 * @author matthias
 */
public class Species {

	private String m_name;
	private double[] m_dGzero;
	private int[] m_zi;
	private int[] m_nH;

	/**
	 * class to store information on metabolites
	 * 
	 * @param name
	 *            name of metabolite
	 * @param dGzero
	 *            delta G0 of formation of metabolite
	 * @param zi
	 *            charge states of metabolite
	 * @param nH
	 *            number of h-Atoms of metabolite
	 */
	public Species(String name, double[] dGzero, int[] zi, int[] nH) {
		m_name = name;
		m_dGzero = dGzero;
		m_zi = zi;
		m_nH = nH;
	}

	/**
	 * @return name of metabolite
	 */
	protected String getName() {
		return m_name;
	}

	/**
	 * @return delta G0 of formation of the metabolite
	 */
	protected double[] getDGzero() {
		return m_dGzero;
	}

	/**
	 * @return charge states of metabolite
	 */
	protected int[] getZi() {
		return m_zi;
	}

	/**
	 * @return number of H-atoms of metabolite
	 */
	protected int[] getNH() {
		return m_nH;
	}

}
