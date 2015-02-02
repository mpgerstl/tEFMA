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

public class ReactionBuildHelper {

	private String m_reactionName;
	private ArrayList<Double> m_leftVal;
	private ArrayList<String> m_leftMetabols;
	private ArrayList<Metabolite> m_leftMetab;
	private ArrayList<String> m_leftName;
	private ArrayList<Double> m_rightVal;
	private ArrayList<String> m_rightMetabols;
	private ArrayList<Metabolite> m_rightMetab;
	private ArrayList<String> m_rightName;

	public ReactionBuildHelper(String reactionName, ArrayList<Double> lVal, ArrayList<String> lMetab, ArrayList<Metabolite> lMeta, ArrayList<String> lName, ArrayList<Double> rVal,
			ArrayList<String> rMetab, ArrayList<Metabolite> rMeta, ArrayList<String> rName) {
		m_reactionName = reactionName;
		m_leftVal = lVal;
		m_leftMetabols = lMetab;
		m_leftMetab = lMeta;
		m_leftName = lName;
		m_rightVal = rVal;
		m_rightMetabols = rMetab;
		m_rightMetab = rMeta;
		m_rightName = rName;
	}

	public String getReactionName() {
		return m_reactionName;
	}

	public ArrayList<Double> getLeftVal() {
		return m_leftVal;
	}

	public ArrayList<String> getLeftMetabolName() {
		return m_leftMetabols;
	}

	public ArrayList<Metabolite> getLeftMetabolite() {
		return m_leftMetab;
	}

	public ArrayList<String> getLeftName() {
		return m_leftName;
	}

	public ArrayList<Double> getRightVal() {
		return m_rightVal;
	}

	public ArrayList<String> getRightMetabolName() {
		return m_rightMetabols;
	}

	public ArrayList<Metabolite> getRightMetabolite() {
		return m_rightMetab;
	}

	public ArrayList<String> getRightName() {
		return m_rightName;
	}

}
