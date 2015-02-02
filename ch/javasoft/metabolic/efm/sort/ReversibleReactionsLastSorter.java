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
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;

public class ReversibleReactionsLastSorter extends RowColSorter {
		private final MetabolicNetwork	mNet;
		private final int[]				mRowMappings;
		public ReversibleReactionsLastSorter(MetabolicNetwork net, int[] rowMappings, int startRowCol, int endRowCol) {
			super(true, startRowCol, endRowCol, -1, -1);
			mNet			= net;
			mRowMappings	= rowMappings;
		}
		private int getReactionIndex(int row) {
			int reacIndex	= 0;
			int rowIndex	= 0;
			while (reacIndex < mNet.getReactions().length()) {
				if (rowIndex == mRowMappings[row]) return reacIndex;
				if (mNet.getReactions().get(reacIndex).getConstraints().isReversible()) {
					rowIndex++;
//					if (rowIndex == mRowMappings[row]) return reacIndex;
//					rowIndex++;
				}
				else {
					rowIndex++;
				}
				reacIndex++;				
			}
			throw new IllegalArgumentException("no such row: " + row);
		}
		public int compare(ReadableDoubleMatrix mx, int rowA, int rowB) {
//			Reaction reacA = mNet.getReactions().get(mRowMappings[rowA]);
//			Reaction reacB = mNet.getReactions().get(mRowMappings[rowB]);
			Reaction reacA = mNet.getReactions().get(getReactionIndex(rowA));
			Reaction reacB = mNet.getReactions().get(getReactionIndex(rowB));
			boolean revA = reacA.getConstraints().isReversible();
			boolean revB = reacB.getConstraints().isReversible();
			return revA == revB ? 0 : (revA ? 1 : -1);
		}
	}