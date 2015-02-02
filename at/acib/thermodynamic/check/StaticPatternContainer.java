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
import java.util.BitSet;
import java.util.Iterator;

public class StaticPatternContainer {

	private static boolean wait = false;
	private static int m_infeasibleCount = 0;
	private static int m_actualPosition = 0;
	private static ArrayList<BitSet> m_bitPattern = new ArrayList<BitSet>();
	private static ArrayList<ArrayList<String>> m_infeasiblePattern = new ArrayList<ArrayList<String>>();

	public static ArrayList<BitSet> getBitPattern() {
		return m_bitPattern;
	}

	public static boolean addBitPattern(ArrayList<BitSet> pat) {
		if (wait) {
			return false;
		} else {
			wait = true;
			for (BitSet x : pat) {
				if (!m_bitPattern.contains(x)) {
					m_bitPattern.add(x);
				}
			}
			wait = false;
			return true;
		}
	}

	public static boolean setNextIteration() {
		if (wait) {
			return false;
		} else {
			wait = true;
			m_actualPosition = m_infeasiblePattern.size();
			wait = false;
			return true;
		}
	}

	/**
	 * increases infeasible count by count
	 * 
	 * @param count
	 */
	public static boolean increaseInfeasibleCount(int count) {
		if (wait) {
			return false;
		} else {
			wait = true;
			m_infeasibleCount += count;
			wait = false;
			return true;
		}
	}

	/**
	 * @return infeasible count
	 */
	public static int getInfeasibleCount() {
		return m_infeasibleCount;
	}

	/**
	 * add patterns to the list
	 * 
	 * @param pat
	 *            ArrayList of pattern
	 */
	public static boolean addPattern(ArrayList<ArrayList<String>> pat) {
		if (wait) {
			return false;
		} else {
			wait = true;
			Iterator<ArrayList<String>> iter = pat.iterator();
			while (iter.hasNext()) {
				ArrayList<String> i = iter.next();
				if (!m_infeasiblePattern.contains(i)) {
					m_infeasiblePattern.add(i);
				}
			}
			wait = false;
			return true;
		}
	}

	/**
	 * add a pattern to the list
	 * 
	 * @param pat
	 *            ArrayList of pattern
	 */
	public static boolean addSinglePattern(ArrayList<String> pat) {
		if (wait) {
			return false;
		} else {
			wait = true;
			if (!m_infeasiblePattern.contains(pat)) {
				m_infeasiblePattern.add(pat);
			}
			wait = false;
			return true;
		}

	}

	public static boolean containsPattern(ArrayList<String> pat) {
		int patL = pat.size();
		for (int i = 0; i < m_infeasiblePattern.size(); i++) {
			ArrayList<String> temp = m_infeasiblePattern.get(i);
			if (temp.size() == patL) {
				boolean isInside = true;
				for (int j = 0; j < patL; j++) {
					if (!(temp.get(j).equals(pat.get(j)))) {
						isInside = false;
						j = patL;
					}
				}
				if (isInside) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return list of all patterns
	 */
	public static ArrayList<ArrayList<String>> getPattern() {
		return m_infeasiblePattern;
	}

	public static int getIterationStartPosition() {
		return m_actualPosition;
	}

}
