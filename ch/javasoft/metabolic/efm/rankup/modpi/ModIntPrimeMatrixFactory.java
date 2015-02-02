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
package ch.javasoft.metabolic.efm.rankup.modpi;

import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrixFactory;
import ch.javasoft.metabolic.efm.rankup.RankUpRoot;

/**
 * The <code>ModIntPrimeMatrixFactory</code> is the default factory for matrices
 * using residue arithmetic, modulo a large 32bit integer prime number,
 * typically <tt>32,749</tt>, the largest prime below 
 * <tt>&radic;(Integer.MAX_VALUE/2)</tt>.
 */
public class ModIntPrimeMatrixFactory implements PreprocessedMatrixFactory {
	
	private final RankUpRoot root;
	
	/**
	 * Constructor with rank update root, which is for instance used to access 
	 * the stoichiometric matrix or to store intermediate partly triangularized
	 * matrices for any recursion depth.
	 */
	public ModIntPrimeMatrixFactory(RankUpRoot root) {
		this.root = root;
	}

	public PreprocessedMatrix createInitialPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token) {
		return new ModIntPrimePreprocessedMatrix(owner, root);
	}
	public PreprocessedMatrix createChildPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token, PreprocessedMatrix parentPreprocessedMatrix) {
		return new ModIntPrimePreprocessedMatrix(owner, root, (ModIntPrimePreprocessedMatrix)parentPreprocessedMatrix);
	}

}
