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

import ch.javasoft.math.BigFraction;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.numeric.Zero;

/**
 * The <code>RankUpRoot</code> class offers some methods commonly used by
 * rank update adjacency, such as minimum required rank etc.
 */
public interface RankUpRoot {

//	/**
//	 * Returns the stoichiometric matrix
//	 */
//	ReadableMatrix getStoich();
	/**
	 * Returns the stoichiometric matrix as rational matrix
	 */
	ReadableMatrix<BigFraction> getStoichRational();
	
	/**
	 * Returns the rank of the stoichiometric matrix
	 */
	int getStoichRank();

	/**
	 * Returns the rank required to pass the adjacency test, that is, number of 
	 * reactions minus 2
	 * 
	 * @return returns q - 2
	 */
	int getRequiredRank();
	
	/**
	 * Returns the required number of zeros (true bits) in the intersection set,
	 * which is <tt>requiredRank - stoichRank</tt> or simply 
	 * <tt>q - 2 - rank(stoich)</tt>
	 * 
	 * @return q - 2 - rank(stoich)
	 */
	int getRequiredCardinality();

	/**
	 * Returns the current reaction or stoich matrix column mapping
	 */
	int[] getColMapping();

	/**
	 * Returns the zero object for rounding and related operations
	 */
	Zero zero();

}
