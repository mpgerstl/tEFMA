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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.metabolic.efm.util.ReactionMapping.Layer;
import ch.javasoft.util.ints.BitSetIntSet;
import ch.javasoft.util.ints.IntIterator;
import ch.javasoft.util.ints.IntSet;

/**
 * The <code>FutileCycleColumnFilter</code> removes futile cycle elementary
 * modes having only a flux value at the forward and backward position of a 
 * split reversible reaction
 */
public class FutileCycleColumnFilter implements ColumnFilter {
	
	private final NetworkEfmModel model;
	public FutileCycleColumnFilter(NetworkEfmModel model) {
		this.model = model;
	}
	
	public <Col extends Column> boolean keepColumn(Col column, Config config, ReactionMapping rmap) {
		final IntSet nonzeros = new BitSetIntSet();
		int cnt = 0;
		for (int i = column.bitValues().nextClearBit(0); i < column.booleanSize(); i = column.bitValues().nextClearBit(i + 1)) {
			nonzeros.addInt(i);
			cnt++;
			if (cnt > 2) return true;
		}
                //////////////////////////////////////////////////////
                // Bugfix by Kristopher Hunt, University of Montana //
                //////////////////////////////////////////////////////
		// if (model.getFinalNumericSize() == 0) {
		if (model.getFinalNumericSize() != 0) {
			for (int i = 0; i < column.numericSize(); i++) {
				if (column.getNumericSignum(config.zero(), i) != 0) {
					nonzeros.addInt(column.booleanSize() + i);
					cnt++;
					if (cnt > 2) return true;
				}
			}
		}
		final BitSetIntSet orig = new BitSetIntSet();
		final IntIterator it = nonzeros.iterator();
		while (it.hasNext()) {
			orig.addAll(rmap.get(Layer.Sorted, it.nextInt(), Layer.Compressed));
		}
		return orig.size() != 1;//both belong to the same (reversible) reaction --> don't keep
	}

}
