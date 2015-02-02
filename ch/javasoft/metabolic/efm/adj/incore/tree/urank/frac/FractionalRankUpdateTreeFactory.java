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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank.frac;

import ch.javasoft.metabolic.efm.adj.incore.tree.urank.RankUpdateJobScheduleTreeFactory;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.RankUpdateRoot;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.ops.Gauss;

public class FractionalRankUpdateTreeFactory extends RankUpdateJobScheduleTreeFactory {
	
	
	/**
	 * Constructor with specified number of threads to use
	 */
	public FractionalRankUpdateTreeFactory(EfmModel efmModel) {		
		super(efmModel);
	}
	@Override
	protected PreprocessedMatrix createInitialPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token, RankUpdateRoot root) {
		return new FractionalPreprocessedMatrix(owner, root);
	}
	@Override
	protected PreprocessedMatrix createChildPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token, RankUpdateRoot root, PreprocessedMatrix parentPreprocessedMatrix) {
		return new FractionalPreprocessedMatrix(owner, root, (FractionalPreprocessedMatrix)parentPreprocessedMatrix);
	}
	
	public int getRank(ReadableMatrix<? extends Number> matrix) {
		final BigIntegerRationalMatrix mx = convertToBigIntegerRationalMatrix(matrix);
		return new Gauss(0d).rank(mx);
	}
	private static BigIntegerRationalMatrix convertToBigIntegerRationalMatrix(ReadableMatrix mx) {
		if (mx instanceof ReadableBigIntegerRationalMatrix<?>) {
			return ((ReadableBigIntegerRationalMatrix<?>)mx).toBigIntegerRationalMatrix(true /*enforceNewInstance*/);
		}
		else if (mx instanceof ReadableDoubleMatrix<?>) {
			final double[] values = ((ReadableDoubleMatrix<?>)mx).toDoubleArray();
			return new DefaultBigIntegerRationalMatrix(values, mx.getRowCount(), mx.getColumnCount(), false /*adjust values*/);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	

}
