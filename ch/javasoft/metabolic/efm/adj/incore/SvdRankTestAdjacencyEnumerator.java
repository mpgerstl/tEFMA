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
package ch.javasoft.metabolic.efm.adj.incore;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.smx.ops.jlapack.JLapackImpl;

public class SvdRankTestAdjacencyEnumerator extends FastRankTestAdjacencyEnumerator {

    @SuppressWarnings("hiding")
	public static final String NAME = "svd-rank";
	
	protected JLapackImpl mJLapackImpl;
	
	public SvdRankTestAdjacencyEnumerator() {
		this(false);
	}
	public SvdRankTestAdjacencyEnumerator(boolean isMinCardinalityTested) {
		super(isMinCardinalityTested);
	}
	
	@Override
	public String name() {
		return NAME;
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.metabolic.efm.adj.FastRankTestAdjacencyEnumerator#initialize(ch.javasoft.metabolic.efm.impl.Column.Home, ch.javasoft.metabolic.efm.config.Config, ch.javasoft.metabolic.efm.impl.EfmModel)
	 */
	@Override
	public <Col extends Column, N extends Number> void initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel model) {
		super.initialize(columnHome, config, model);
		mJLapackImpl = new JLapackImpl(mConfig.zero());
	}
	
	@Override
	protected boolean isRankGaussFullPivoting(double[][] arr, int rankSoFar, int rank) {
		int restRank = rank - rankSoFar;
		return restRank <= 0 || restRank <= mJLapackImpl.rank(arr);
	}

}