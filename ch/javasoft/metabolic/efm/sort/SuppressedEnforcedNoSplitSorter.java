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
package ch.javasoft.metabolic.efm.sort;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;

/**
 * The <code>SuppressedEnforcedNoSplitSorter</code> puts reactions to the
 * end which are excluded from the iteration loop. Such reactions are
 * suppressed reactions resulting in zero flux values, enforced reactions
 * resulting in non-zero fluxes, and no split reactions denoting reversible
 * reactions which are not split into two irreversible ones.
 */
public class SuppressedEnforcedNoSplitSorter extends RowColSorter {
	
	private final ReactionMapping mMapping;
	/**
	 * Constructor for <code>SuppressedEnforcedNoSplitSorter</code> to sort the
	 * rows of the kernel matrix, from startRow to endRow (to exclude the
	 * identity part of the row-echelon kernel matrix). 
	 */
	public SuppressedEnforcedNoSplitSorter(MetabolicNetwork net, Config config, int[] rowMappings, int startRow, int endRow) {
		super(true, startRow, endRow, -1, -1);
		mMapping = new ReactionMapping(config, net, rowMappings);
	}
	/**
	 * Constructor for <code>SuppressedEnforcedNoSplitSorter</code> to sort the
	 * columns of the stoichiometric matrix
	 */
	public SuppressedEnforcedNoSplitSorter(MetabolicNetwork net, Config config, int[] rowMappings) {
		super(false, -1, -1, 0, rowMappings.length);
		mMapping = new ReactionMapping(config, net, rowMappings);
	}
	private int getReactionOrder(int row) {
		return -mMapping.getReactionCategoryBySortedIndex(row).ordinal();
	}
	public int compare(ReadableDoubleMatrix mx, int rowA, int rowB) {
		return getReactionOrder(rowA) - getReactionOrder(rowB);
	}
}