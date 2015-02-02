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
package ch.javasoft.metabolic.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.genarr.ArrayIterable;

/**
 * The <code>StoichiometricMatrices</code> class contains static helper methods to
 * create different stoichiometric matrix types from a {@link MetabolicNetwork}.
 */
public class StoichiometricMatrices {
	
	private static final Logger LOG = LogPkg.LOGGER;

	/**
	 * Creates the <i>immediate</i> stoichiometrix matrix, meaning that reversible
	 * reactions are NOT duplicated.
	 * 
	 * @param metaNet	The network to transform into a stoichiometric matrix
	 * @return			The stoichiometric matrix as a 2-dim double array, where
	 * 					the rows are associated with the metabolites (1<up>st</up> dim) 
	 * 					and the columns with the reactions (1<up>nd</up> dim) respectively
	 */
	public static double[][] createStoichiometricMatrix(MetabolicNetwork metaNet) {
		Map<Metabolite, Integer>	metaPos	= new HashMap<Metabolite, Integer>();
		ArrayIterable<? extends Metabolite>	metas	= metaNet.getMetabolites();
		ArrayIterable<? extends Reaction>	reacts	= metaNet.getReactions();
		for (int ii = 0; ii < metas.length(); ii++) {
			metaPos.put(metas.get(ii), Integer.valueOf(ii));
		}
		double[][] result = new double[metas.length()][reacts.length()];
		for (int reactIndex = 0; reactIndex < reacts.length(); reactIndex++) {
			for (MetaboliteRatio metaRatio : reacts.get(reactIndex).getMetaboliteRatios()) {
				int metaIndex = metaPos.get(metaRatio.getMetabolite()).intValue();
				if (result[metaIndex][reactIndex] != 0d) {
					LOG.warning(
						"multiple occurrences of metabolite " + metaRatio.getMetabolite() + 
						" in reaction " + reacts.get(reactIndex).getName() + ": " + reacts.get(reactIndex)
					);
				}
				result[metaIndex][reactIndex] += metaRatio.getRatio();
			}
		}
		return result;
	}

	/**
	 * Creates the <i>expanded</i> stoichiometrix matrix, meaning that reversible
	 * reactions are replaced by two irreversible reactions, forward and backward.
	 * The duplicated reactions have subsequent indices. Considering two columns
	 * of the expanded stoichiometric matrix corresponding to a reversible reaction,
	 * we have <tt>col(i) = -col(i+1)</tt>, where <tt>col(i)</tt> is equal to the 
	 * column of the original unexpanded matrix.
	 * 
	 * @param metaNet	The network to transform into a stoichiometric matrix
	 * @return			The stoichiometric matrix as a 2-dim double array, where
	 * 					the rows are associated with the metabolites (1<up>st</up> dim) 
	 * 					and the columns with the reactions (1<up>nd</up> dim) respectively
	 */
	public static double[][] createStoichiometricMatrixExpandReversible(MetabolicNetwork metaNet) {
		Map<Metabolite, Integer>	metaPos	= new HashMap<Metabolite, Integer>();
		ArrayIterable<? extends Metabolite>	metas	= metaNet.getMetabolites();
		ArrayIterable<? extends Reaction>		reacts	= metaNet.getReactions();
		for (int ii = 0; ii < metas.length(); ii++) {
			metaPos.put(metas.get(ii), Integer.valueOf(ii));
		}
		//count reactions (with reversible ones)
		int reacCnt = 0;
		for (Reaction reac : reacts) {
			if (reac.getConstraints().isReversible()) reacCnt += 2;
			else reacCnt++;
		}
		double[][] result = new double[metas.length()][reacCnt];
		int reactIndex = 0;
		int reversible = 0;
		for (Reaction reac : reacts) {
			boolean forward = false;
			do {
				forward = !forward;
				if (!forward) reversible++;
				for (MetaboliteRatio metaRatio : reacts.get(reactIndex).getMetaboliteRatios()) {
					Metabolite meta = metaRatio.getMetabolite();
					int metaIndex = metaPos.get(meta).intValue();
					if (result[metaIndex][reactIndex + reversible] != 0d) {
						LOG.warning(
							"multiple occurrences of metabolite " + metaRatio.getMetabolite() + 
							" in reaction " + reacts.get(reactIndex).getName() + ": " + reacts.get(reactIndex)
						);
					}
					result[metaIndex][reactIndex + reversible] += forward ? metaRatio.getRatio() : -metaRatio.getRatio();
				}
			}
			while (forward && reac.getConstraints().isReversible());
			reactIndex++;
		}
		return result;
	}
	
	/**
	 * Creates the <i>expanded</i> stoichiometrix matrix, meaning that reversible
	 * reactions are replaced by two irreversible reactions, forward and backward.
	 * The duplicated reactions have subsequent indices. Considering two columns
	 * of the expanded stoichiometric matrix corresponding to a reversible reaction,
	 * we have <tt>col(i) = -col(i+1)</tt>, where <tt>col(i)</tt> is equal to the 
	 * column of the original unexpanded matrix.
	 * 
	 * 
	 * @param stoich		The stoichiometric matrix in the unexpanded form
	 * @param reversible	The reversible flags, indices corresponding to the
	 * 						columns of the matrix
	 * @return				The stoichiometric matrix as a 2-dim array, where
	 * 						the rows are associated with the metabolites 
	 * 						(1<up>st</up> dim) and the columns with the 
	 * 						reactions (1<up>nd</up> dim) respectively
	 */
	@SuppressWarnings("unchecked")
	public static <N extends Number> N[][] createStoichiometricMatrixExpandReversible(ReadableMatrix<N> stoich, boolean[] reversible) {
		int revs = 0;
		for (int i = 0; i < reversible.length; i++) {
			if (reversible[i]) revs++;
		}
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		final NumberOperations<N> numberOps = stoich.getNumberOperations();
		N[][] result = (N[][])Array.newInstance(numberOps.numberClass(), new int[] {rows, cols+revs});
		for (int row = 0; row < rows; row++) {
			int rev = 0;
			for (int col = 0; col < cols; col++) {
				result[row][col+rev] = stoich.getNumberValueAt(row, col);
				if (reversible[col]) {
					rev++;
					result[row][col+rev] = numberOps.negate(result[row][col+rev-1]);
				}
			}
		}
		return result;
	}
	
	/**
	 * Creates the <i>boolean</i> stoichiometrix matrix, meaning that flows are
	 * not returned quantitatively. A <code>true</code> value in the matrix 
	 * indicates that the respective metabolite (row) is participating in the
	 * reaction related (column). There is no differentiation between irreversible
	 * and reversible reactions.
	 * 
	 * @param metaNet	The network to transform into a stoichiometric matrix
	 * @return			The stoichiometric matrix as a 2-dim boolean array, where
	 * 					the rows are associated with the metabolites (1<up>st</up> dim) 
	 * 					and the columns with the reactions (1<up>nd</up> dim) respectively
	 */
	public static boolean[][] createStoichiometricMatrixBoolean(MetabolicNetwork metaNet) {
		Map<Metabolite, Integer>	metaPos	= new HashMap<Metabolite, Integer>();
		ArrayIterable<? extends Metabolite>	metas	= metaNet.getMetabolites();
		ArrayIterable<? extends Reaction>		reacts	= metaNet.getReactions();
		for (int ii = 0; ii < metas.length(); ii++) {
			metaPos.put(metas.get(ii), Integer.valueOf(ii));
		}
		boolean[][] result = new boolean[metas.length()][reacts.length()];
		for (int reactIndex = 0; reactIndex < reacts.length(); reactIndex++) {
			for (MetaboliteRatio metaRatio : reacts.get(reactIndex).getMetaboliteRatios()) {
				int metaIndex = metaPos.get(metaRatio.getMetabolite()).intValue();
				result[metaIndex][reactIndex] = metaRatio.getRatio() != 0.0d;//should always be != 0.0d
			}
		}
		return result;
	}
	
	//no instances
	private StoichiometricMatrices() {
		super();
	}
	
}
