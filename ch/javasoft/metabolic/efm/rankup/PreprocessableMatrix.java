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
package ch.javasoft.metabolic.efm.rankup;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;

/**
 * A <tt>PreprocessableMatrix</tt> is a matrix for which the rank is to be 
 * computed. The first time the rank (or any information related to that) is 
 * accessed, it is actually processed, triggering processing of all parent 
 * matrices, too.
 */
public class PreprocessableMatrix implements Cloneable {
	
	public final PreprocessableMatrix		parent;
	public final IBitSet			nodeCutSet;
		
	protected transient PreprocessedMatrix	processedMatrix;
	
	private PreprocessableMatrix(PreprocessableMatrix parent, IBitSet nodeCutSet) {
		this.parent 			= parent;
		this.nodeCutSet			= nodeCutSet;
		this.processedMatrix	= null;
	}
	
	public static PreprocessableMatrix createRootMatrix(final IBitSet nodeCutSet) {
		return new PreprocessableMatrix(null, nodeCutSet) {
			@Override
			protected PreprocessedMatrix process(RankUpdateToken token, PreprocessedMatrixFactory fac) {
				if (processedMatrix == null) {
					processedMatrix = fac.createInitialPreprocessedMatrix(this, token);
				}
				return processedMatrix;
			}
		};
	}
	
	public PreprocessableMatrix createChild(IBitSet nodeCutSet) {
		return new PreprocessableMatrix(this, nodeCutSet);
	}
	
	public boolean hasRequiredRank(RankUpdateToken token, RankUpRoot root, PreprocessedMatrixFactory fac, IBitSet intersectionSet) {
		//remove this, too slow
//		if (!intersectionSet.isSubSetOf(nodeCutSet)) throw new RuntimeException("wrong nodes");
		
		return process(token, fac).hasRequiredRank(root, intersectionSet);
	}

	/**
	 * POSTCONDITION: matrix/colmap not null, rank set
	 */
	protected PreprocessedMatrix process(RankUpdateToken token, PreprocessedMatrixFactory fac) {
		if (processedMatrix == null) {
			final PreprocessedMatrix parentProc = parent.process(token, fac);
			processedMatrix = fac.createChildPreprocessedMatrix(this, token, parentProc);
		}
		return processedMatrix;
	}
	
	@Override
	public String toString() {
		return processedMatrix == null ? "preprocessable" : processedMatrix.toString();
	}

	@Override
	public PreprocessableMatrix clone() {
		final PreprocessableMatrix clone;
		if (processedMatrix == null) {
			if (parent == null) {
				clone = createRootMatrix(nodeCutSet);
			}
			else {
				clone = new PreprocessableMatrix(parent.clone(), nodeCutSet);
			}
		}
		else {
			clone = new PreprocessableMatrix(parent, nodeCutSet);
			clone.processedMatrix = processedMatrix.clone();
		}
		return clone;
	}
	
}
