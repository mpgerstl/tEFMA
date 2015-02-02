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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * define reactions and metabolites that can be used for a linear problem
 * 
 * @author matthias
 * 
 */
public class ActiveReaction {

	/**
	 * {@link HashMap} < row of stoichiometric matrix, name >
	 */
	private HashMap<Integer, String> m_metaboliteOrder;
	/**
	 * {@link HashMap} < name of metabolite, position in lp problem >
	 */
	private HashMap<String, Integer> m_activeMetaboliteNameMap;
	/**
	 * {@link HashMap} < position in lp problem, name of metabolite >
	 */
	private HashMap<Integer, String> m_activeMetaboliteReverseNameMap;
	/**
	 * {@link HashMap} < row in stoichiometric matrx, index of lp problem >
	 */
	private HashMap<Integer, Integer> m_activeReactionMap;
	/**
	 * {@link HashMap} < row in stoichiometric matrix, index of lp problem >
	 */
	private HashMap<Integer, Integer> m_activeMetaboliteMap;
	/**
	 * {@link HashMap} < index of lp problem, row in stoichiometric matrix >
	 */
	private HashMap<Integer, Integer> m_activeMetaboliteReverseMap;
	/**
	 * {@link HashMap} < name of reaction, column in stoichiometric matrix >
	 */
	private HashMap<String, Integer> m_origReactionMap;
	/**
	 * {@link HashMap} < column in stoichiometric matrix, name > of original
	 * reactions
	 */
	private HashMap<Integer, String> m_origReactionReverseMap;
	/**
	 * {@link HashMap} < index of compressed reaction, {@link HashSet} < indices
	 * of single reaction >>
	 */
	private HashMap<Integer, HashSet<Integer>> m_activeCompressedReactionMap;
	/**
	 * {@link HashMap} < index, name > of compressed reactions
	 */
	private HashMap<Integer, String> m_compressedReactionNameMap;
	/**
	 * {@link HashSet} < index > of active compressed reactions
	 */
	private HashSet<Integer> m_activeCompressedReactions;
	/**
	 * {@link HashSet} < index > indices of original reaction
	 */
	private HashSet<Integer> m_activeCompressedSingleReactions;
	/**
	 * sorted indices of active compressed single reactions (columns in
	 * compressed matrix)
	 */
	private int[] m_activeCompressedReactionArray;
	/**
	 * sorted indices of active single reactions (columns in stoichiometric
	 * matrix)
	 */
	private int[] m_activeCompressedSingleReactionArray;
	/**
	 * sorted array of active reaction indices (columns in stoichiometric
	 * matrix)
	 */
	private int[] m_activeReactionArray;
	/**
	 * sorted array of active metabolite indices (rows in stoichiometric matrix)
	 */
	private int[] m_activeMetaboliteArray;

	/**
	 * @param stoichMatrix
	 *            stoichiometric matrix
	 * @param metabolites
	 *            {@link HashMap} < Metabolite name, {@link Metabolite} > of
	 *            metabolite data
	 * @param metaboliteOrder
	 *            {@link HashMap} < index of metabolite, Metabolite name > of
	 *            metabolite order
	 * @param orphans
	 *            {@link HashSet} < index of reaction > of orphan reactions}
	 * @param compressedNames
	 *            ordered String[] of compressed names
	 * @param reactionList
	 *            ordered String[] of all reactions names
	 */
	public ActiveReaction(double[][] stoichMatrix, HashMap<String, Metabolite> metabolites, HashMap<Integer, String> metaboliteOrder, HashSet<Integer> orphans, String[] reactionList,
			String[] compressedNames) {
		// initialize hashes
		initialize();

		m_metaboliteOrder = metaboliteOrder;
		// fill original reaction maps
		for (int i = 0; i < reactionList.length; i++) {
			m_origReactionMap.put(reactionList[i], i);
			m_origReactionReverseMap.put(i, reactionList[i]);
		}

		defineActiveMetabolites(metabolites, metaboliteOrder);
		defineActiveReactions(stoichMatrix, orphans);
		defineCompressedMaps(compressedNames);
	}

	/**
	 * initialize members
	 */
	private void initialize() {
		m_activeCompressedReactions = new HashSet<Integer>();
		m_activeCompressedSingleReactions = new HashSet<Integer>();
		m_activeMetaboliteMap = new HashMap<Integer, Integer>();
		m_activeMetaboliteReverseMap = new HashMap<Integer, Integer>();
		m_activeReactionMap = new HashMap<Integer, Integer>();
		m_activeMetaboliteReverseNameMap = new HashMap<Integer, String>();
		m_compressedReactionNameMap = new HashMap<Integer, String>();
		m_origReactionReverseMap = new HashMap<Integer, String>();
		m_origReactionMap = new HashMap<String, Integer>();
		m_activeMetaboliteNameMap = new HashMap<String, Integer>();
		m_activeCompressedReactionMap = new HashMap<Integer, HashSet<Integer>>();
	}

	/**
	 * @return {@link HashMap} < name of metabolite, position in lp problem >
	 */
	protected HashMap<String, Integer> getActiveMetaboliteNameMap() {
		return m_activeMetaboliteNameMap;
	}

	/**
	 * @return {@link HashMap} < position in lp problem, name of metabolite >
	 */
	protected HashMap<Integer, String> getActiveMetaboliteReverseNameMap() {
		return m_activeMetaboliteReverseNameMap;
	}

	/**
	 * @return {@link HashMap} < index, name > of compressed reactions
	 */
	protected HashMap<Integer, String> getCompressedReactionNameMap() {
		return m_compressedReactionNameMap;
	}

	/**
	 * @return {@link HashMap} < index, name > of original reactions
	 */
	protected HashMap<Integer, String> getSingleReactionNameMap() {
		return m_origReactionReverseMap;
	}

	/**
	 * @return {@link HashMap} < index of compressed reaction, {@link HashSet} <
	 *         indices of single reaction >>
	 */
	protected HashMap<Integer, HashSet<Integer>> getActiveCompressedReactionMap() {
		return m_activeCompressedReactionMap;
	}

	/**
	 * @return {@link HashSet} < index > of active compressed reactions
	 */
	protected HashSet<Integer> getActiveCompressedReactions() {
		return m_activeCompressedReactions;
	}

	/**
	 * @return {@link HashSet} < index > indices of original reaction
	 */
	protected HashSet<Integer> getActiveCompressedSingleReactions() {
		return m_activeCompressedSingleReactions;
	}

	/**
	 * for reverse see
	 * 
	 * @return {@link HashMap} < row in stoichiometric matrix, index of lp
	 *         problem >
	 */
	protected HashMap<Integer, Integer> getActiveMetaboliteMap() {
		return m_activeMetaboliteMap;
	}

	/**
	 * for reverse see
	 * 
	 * @return {@link HashMap} < index of lp problem, row in stoichiometric
	 *         matrix >
	 */
	protected HashMap<Integer, Integer> getActiveMetaboliteReverseMap() {
		return m_activeMetaboliteReverseMap;
	}

	/**
	 * @return {@link HashMap} < index in stoichiometric matrx, index of lp
	 *         problem >
	 */
	protected HashMap<Integer, Integer> getActiveReactionMap() {
		return m_activeReactionMap;
	}

	/**
	 * @return sorted array of active metabolite indices (rows in stoichiometric
	 *         matrix)
	 */
	protected int[] getActiveMetaboliteKeys() {
		return m_activeMetaboliteArray;
	}

	/**
	 * @return sorted array of active reaction indices (columns in
	 *         stoichiometric matrix)
	 */
	protected int[] getActiveReactionKeys() {
		return m_activeReactionArray;
	}

	/**
	 * @return sorted indices of active compressed single reactions (columns in
	 *         compressed matrix)
	 */
	protected int[] getActiveCompressedReactionKeys() {
		return m_activeCompressedReactionArray;
	}

	/**
	 * @return sorted indices of active single reactions (columns in
	 *         stoichiometric matrix)
	 */
	protected int[] getActiveCompressedSingleReactionKeys() {
		return m_activeCompressedSingleReactionArray;
	}

	/**
	 * define metabolites that can be used by linear problem
	 * 
	 * @param metabolites
	 *            map of all metabolites
	 * @param metaboliteOrder
	 *            indices and names of metabolites
	 */
	private void defineActiveMetabolites(HashMap<String, Metabolite> metabolites, HashMap<Integer, String> metaboliteOrder) {
		ArrayList<Integer> intKeys = new ArrayList<Integer>();
		int counter = 0;
		for (int i = 0; i < metaboliteOrder.size(); i++) {
			if (metabolites.containsKey(metaboliteOrder.get(i))) {
				m_activeMetaboliteNameMap.put(m_metaboliteOrder.get(i), counter);
				m_activeMetaboliteReverseNameMap.put(counter, m_metaboliteOrder.get(i));
				m_activeMetaboliteMap.put(i, counter);
				m_activeMetaboliteReverseMap.put(counter, i);
				intKeys.add(i);
				counter++;
			}
		}
		m_activeMetaboliteArray = new int[intKeys.size()];
		for (int i = 0; i < intKeys.size(); i++) {
			m_activeMetaboliteArray[i] = intKeys.get(i);
		}
	}

	/**
	 * define reactions that can be used by linear problem
	 * 
	 * @param stoich
	 *            stoichiometric matrix
	 * @param orphans
	 *            set of orphan reactions
	 */
	private void defineActiveReactions(double[][] stoich, HashSet<Integer> orphans) {
		ArrayList<Integer> intKeys = new ArrayList<Integer>();
		int reactNr = stoich[0].length;
		int metabNr = stoich.length;
		int counter = 0;
		for (int i = 0; i < reactNr; i++) {
			if (!orphans.contains(i)) {
				boolean act = true;
				for (int j = 0; j < metabNr; j++) {
					if (stoich[j][i] != 0) {
						if (!m_activeMetaboliteMap.containsKey(j)) {
							// do not use this reaction, as information of at
							// least one metabolite is missing
							act = false;
							j = metabNr;
						}
					}
				}
				if (act) {
					m_activeReactionMap.put(i, counter);
					intKeys.add(i);
					counter++;
				}
			}
		}
		m_activeReactionArray = new int[intKeys.size()];
		for (int i = 0; i < intKeys.size(); i++) {
			m_activeReactionArray[i] = intKeys.get(i);
		}
	}

	/**
	 * define maps of compressed to single reactions
	 * 
	 * @param compressedNames
	 *            array with names of compressed reactions
	 */
	private void defineCompressedMaps(String[] compressedNames) {
		for (int i = 0; i < compressedNames.length; i++) {
			String[] names = getSingleReactions(compressedNames[i]);
			HashSet<Integer> indices = new HashSet<Integer>();
			boolean addToMap = true;
			for (int j = 0; j < names.length; j++) {
				int t_ind = m_origReactionMap.get(names[j]);
				if (m_activeReactionMap.containsKey(t_ind)) {
					indices.add(t_ind);
				} else {
					// do not use this compressed reaction, as at least one
					// single reaction is not defined
					addToMap = false;
					j = names.length;
				}
			}
			if (addToMap) {
				m_activeCompressedReactions.add(i);
				m_activeCompressedReactionMap.put(i, indices);
				m_compressedReactionNameMap.put(i, compressedNames[i]);
				Iterator<Integer> iter = indices.iterator();
				while (iter.hasNext()) {
					int temp = iter.next();
					if (!m_activeCompressedSingleReactions.contains(temp)) {
						m_activeCompressedSingleReactions.add(temp);
					}
				}
			}
		}
		m_activeCompressedReactionArray = hashSet2sortedIntegerArray(m_activeCompressedReactions);
		m_activeCompressedSingleReactionArray = hashSet2sortedIntegerArray(m_activeCompressedSingleReactions);
	}

	/**
	 * sorts an Integer HashSet and returns an int[]
	 * 
	 * @param set
	 *            {@link HashSet} < Integer values >
	 * @return sorted int array
	 */
	private int[] hashSet2sortedIntegerArray(HashSet<Integer> set) {
		int size = set.size();
		int[] a = new int[size];
		int index = 0;
		Iterator<Integer> iter = set.iterator();
		while (iter.hasNext()) {
			a[index] = iter.next();
			index++;
		}
		Arrays.sort(a);
		return a;
	}

	/**
	 * split compressed reaction name to single names
	 * 
	 * @param compressedName
	 * @return array of single reaction names
	 */
	private String[] getSingleReactions(String compressedName) {
		String[] singleRx;
		if (compressedName.contains("::")) {
			singleRx = compressedName.split("::");
		} else {
			singleRx = new String[1];
			singleRx[0] = compressedName;
		}
		return singleRx;
	}

}
