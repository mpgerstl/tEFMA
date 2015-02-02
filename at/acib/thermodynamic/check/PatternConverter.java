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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.compress.CompressedMetabolicNetwork;
import ch.javasoft.metabolic.compress.StoichMatrixCompressedMetabolicNetwork;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.util.ints.IntList;

/**
 * convert a bitset to readable reaction pattern
 * 
 * @author matthias
 */
public class PatternConverter {

	private NetworkEfmModel m_model;
	private ReactionMapping m_rxMap;
	private HashMap<Integer, Integer> m_reversibleMap;
	private String[] m_rxNames;
	private int m_reactionCount;
	private double[][] m_postMatrix;

	/**
	 * @param NetworkEfmModel
	 */
	public PatternConverter(NetworkEfmModel model) {
		m_model = model;
		MetabolicNetwork metaNet = m_model.getMetabolicNetwork();
		MetabolicNetwork comprMetaNet = metaNet instanceof CompressedMetabolicNetwork ? ((CompressedMetabolicNetwork) metaNet).getRootNetwork() : metaNet;
		m_rxNames = comprMetaNet.getReactionNames();
		m_reactionCount = m_rxNames.length;
		m_rxMap = m_model.getReactionMapping();
		StoichMatrixCompressedMetabolicNetwork stNet = (StoichMatrixCompressedMetabolicNetwork) m_model.getMetabolicNetwork();
		BigIntegerRationalMatrix post = stNet.getPostMatrix();
		m_postMatrix = post.getDoubleRows();
		defineReversibleMap();
	}

	/**
	 * creates a readable string with those reactions in the pattern that carry
	 * a flux. Reactions that are reversed in the pattern carry a minus.
	 * 
	 * @param pat
	 *            ... the pattern for which the corresponding reactions are
	 *            wished
	 * @param booleanSize
	 *            ... size of the boolean values in actual column
	 * @return String with reactions carry a flux
	 */
	public String getPattern(IBitSet pat, int booleanSize) {
		return getPatternFromBitSet(pat.toBitSet(), booleanSize);
	}

	/**
	 * creates a readable string with those reactions in the pattern that carry
	 * a flux. Reactions that are reversed in the pattern carry a minus.
	 * 
	 * @param pat
	 *            ... the pattern for which the corresponding reactions are
	 *            wished
	 * @param booleanSize
	 *            ... size of the boolean values in actual column
	 * @return String with reactions carry a flux
	 */
	public String getPatternFromBitSet(final BitSet pat, int booleanSize) {
		BitSet t_pat = (BitSet) pat.clone();
		t_pat.flip(0, booleanSize);
		StringBuilder pattern = new StringBuilder();
		boolean appendNext = false;
		int bit = t_pat.nextSetBit(0);
		while (bit >= 0) {
			if (appendNext) {
				pattern.append("   ");
			} else {
				appendNext = true;
			}
			IntList se = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Expanded);
			IntList sc = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Compressed);
			IntList so = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Original);
			int negative = (m_reversibleMap.containsKey(se.get(0))) ? -1 : 1;
			boolean started = false;
			Iterator<Integer> scIter = sc.iterator();
			while (scIter.hasNext()) {
				int nextSc = scIter.next();
				Iterator<Integer> soIter = so.iterator();
				while (soIter.hasNext()) {
					int nextSo = soIter.next();
					if (started) {
						pattern.append("::");
					} else {
						started = true;
					}
					if ((m_postMatrix[nextSo][nextSc] * negative) < 0) {
						pattern.append(ThermoChecker.RX_REV_PREFIX);
					} else {
						pattern.append(ThermoChecker.RX_PREFIX);
					}
					pattern.append(m_rxNames[nextSo]);
				}
				bit = t_pat.nextSetBit(bit + 1);
			}
		}
		return pattern.toString();
	}

	public ArrayList<Integer> getIntPatternFromBitSet(BitSet pat, int booleanSize) {
		pat.flip(0, booleanSize);
		ArrayList<Integer> pattern = new ArrayList<Integer>();
		int bit = pat.nextSetBit(0);
		while (bit >= 0) {
			IntList se = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Expanded);
			IntList sc = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Compressed);
			IntList so = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Original);
			int negative = (m_reversibleMap.containsKey(se.get(0))) ? -1 : 1;
			Iterator<Integer> scIter = sc.iterator();
			while (scIter.hasNext()) {
				int nextSc = scIter.next();
				Iterator<Integer> soIter = so.iterator();
				while (soIter.hasNext()) {
					int nextSo = soIter.next();
					if ((m_postMatrix[nextSo][nextSc] * negative) < 0) {
						pattern.add(-1 * nextSo);
					} else {
						pattern.add(nextSo);
					}
				}
				bit = pat.nextSetBit(bit + 1);
			}
		}
		return pattern;
	}

	public String getBooleanPatternFromBitSet(BitSet pat, int booleanSize) {
		HashSet<Integer> set = new HashSet<Integer>();
		boolean negative = false;
		int bit = pat.nextSetBit(0);
		while (bit >= 0) {
			IntList se = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Expanded);
			IntList sc = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Compressed);
			IntList so = m_rxMap.get(ReactionMapping.Layer.Sorted, bit, ReactionMapping.Layer.Original);
			negative = (m_reversibleMap.containsKey(se.get(0))) ? true : false;
			Iterator<Integer> scIter = sc.iterator();
			int multiplicator = 1;
			while (scIter.hasNext()) {
				int nextSc = scIter.next();
				Iterator<Integer> soIter = so.iterator();
				while (soIter.hasNext()) {
					int nextSo = soIter.next();
					if (m_postMatrix[nextSo][nextSc] > 0 && negative || m_postMatrix[nextSo][nextSc] < 0 && !negative) {
						multiplicator = -1;
					} else {
						multiplicator = 1;
					}
					int val = nextSo * multiplicator;
					set.add(val);
				}
				bit = pat.nextSetBit(bit + 1);
			}
		}
		StringBuilder pattern = new StringBuilder();
		for (int i = 0; i < m_reactionCount; i++) {
			if (set.contains(i)) {
				pattern.append("1\t");
			} else if (set.contains(i * -1)) {
				pattern.append("-1\t");
			} else {
				pattern.append("0\t");
			}
		}
		return pattern.toString();
	}

	public String getReactionsFromIndex(int index) {
		int size = index + 1;
		BitSet pat = new BitSet(size);
		pat.set(index);
		pat.flip(0, size);
		return getPatternFromBitSet(pat, size);
	}

	public ArrayList<Integer> getIntReactionsFromIndex(int index) {
		int size = index + 1;
		BitSet pat = new BitSet(size);
		pat.set(index);
		pat.flip(0, size);
		return getIntPatternFromBitSet(pat, size);
	}

	/**
	 * defines a map, which is needed to calculate back to forward and back
	 * direction of a reaction
	 */
	private void defineReversibleMap() {
		m_reversibleMap = new HashMap<Integer, Integer>();
		for (int i = 1; i < m_model.getReactionSorting().length; i++) {
			IntList tempA = m_rxMap.get(ReactionMapping.Layer.Expanded, i, ReactionMapping.Layer.Compressed);
			IntList tempB = m_rxMap.get(ReactionMapping.Layer.Expanded, i - 1, ReactionMapping.Layer.Compressed);
			if (areIntListsEqual(tempA, tempB)) {
				m_reversibleMap.put(i, i - 1);
			}
		}
	}

	/**
	 * compares if two IntLists are equal
	 * 
	 * @param IntList
	 *            a
	 * @param IntList
	 *            b
	 * @return true if both lists are equal
	 */
	private boolean areIntListsEqual(IntList a, IntList b) {
		int s = a.size();
		if (s == b.size()) {
			for (int i = 0; i < s; i++) {
				if (a.get(i) != b.getInt(i)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

}
