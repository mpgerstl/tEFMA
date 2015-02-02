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

public class LpReaction {

	private int m_reactionIndex;
	private int[] m_fwdCols;
	private int[] m_revCols;
	private double[] m_fwdVals;
	private double[] m_revVals;
	private String m_fwdName;
	private String m_revName;

	public LpReaction(int reactionIndex) {
		m_reactionIndex = reactionIndex;
		m_fwdCols = new int[0];
		m_revCols = new int[0];
		m_fwdVals = new double[0];
		m_revVals = new double[0];
		m_fwdName = null;
		m_revName = null;
	}

	public void setFwdReaction(int[] dfGcols, double[] dfGvals, String name) {
		int sc = dfGcols.length;
		int sv = dfGvals.length;
		if (sc == sv) {
			m_fwdCols = new int[sc + 1];
			m_fwdVals = new double[sc + 1];
			for (int i = 0; i < dfGcols.length; i++) {
				m_fwdCols[i] = dfGcols[i];
				m_fwdVals[i] = dfGvals[i];
			}
			m_fwdCols[sc] = m_reactionIndex;
			m_fwdVals[sc] = 1;
		}
		m_fwdName = name;
	}

	public void setRevReaction(int[] dfGcols, double[] dfGvals, String name) {
		int sc = dfGcols.length;
		int sv = dfGvals.length;
		if (sc == sv) {
			m_revCols = new int[sc + 1];
			m_revVals = new double[sc + 1];
			for (int i = 0; i < dfGcols.length; i++) {
				m_revCols[i] = dfGcols[i];
				m_revVals[i] = dfGvals[i];
			}
			m_revCols[sc] = m_reactionIndex;
			m_revVals[sc] = 1;
		}
		m_revName = name;
	}

	public int getReactionIndex() {
		return m_reactionIndex;
	}

	public int[] getFwdCols() {
		return m_fwdCols;
	}

	public int[] getRevCols() {
		return m_revCols;
	}

	public double[] getFwdVals() {
		return m_fwdVals;
	}

	public double[] getRevVals() {
		return m_revVals;
	}

	public String getFwdName() {
		return m_fwdName;
	}

	public String getRevName() {
		return m_revName;
	}

}
