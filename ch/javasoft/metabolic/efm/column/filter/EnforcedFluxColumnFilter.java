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
package ch.javasoft.metabolic.efm.column.filter;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.metabolic.efm.util.ReactionMapping.Layer;
import ch.javasoft.util.ints.IntList;

/**
 * The <code>EnforcedFluxColumnFilter</code> removes columns if they contain 
 * zero fluxes for enforced flux reactions. 
 */
public class EnforcedFluxColumnFilter implements ColumnFilter {

	private Map<String, IntList> enforceIndices = null;
	
	public <Col extends Column> boolean keepColumn(Col column, Config config, ReactionMapping rmap) {
		ensureInitialized(config, rmap);
		boolean keep = true;			
		for (final String reac : enforceIndices.keySet()) {
			final IntList sinds = enforceIndices.get(reac);
			boolean any = false;			
			for (int j = 0; keep && j < sinds.size(); j++) {
				final boolean allowNeg;
				final int sgn;
				final int sind = sinds.getInt(j);
				if (sind < column.booleanSize()) {
					sgn = column.get(sind) ? 0 : 1;					
					allowNeg = false;//is anyway not negative
				}
				else {
					final int nind = sind - column.booleanSize();
					sgn = column.getNumericSignum(config.zero(), nind);
					allowNeg = sgn < 0 ? allowNegativeFlux(rmap, sind, column.booleanSize()) : false;
				}
				keep &= allowNeg ? sgn != 0 : sgn > 0;
				any |= sgn != 0;
			}
			keep &= any;
		}
		return keep;
	}
	private boolean allowNegativeFlux(ReactionMapping rmap, int sind, int booleanSize) {
		return 
			rmap.isReactionReversibleBySortedIndex(sind) &&
			rmap.getSortedReactionIndexOfTwinPart(sind) >= booleanSize;		
	}
	private void ensureInitialized(Config config, ReactionMapping rmap) {
		if (enforceIndices == null) {
			final Map<String, IntList> map = new LinkedHashMap<String, IntList>();
			for (final String reac : config.getReactionsToEnforce()) {
				map.put(reac, rmap.getByOriginalReactionName(reac, Layer.Sorted));
			}
			enforceIndices = map;
		}
	}
}
