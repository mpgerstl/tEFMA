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
package ch.javasoft.metabolic.efm.model;

/**
 * Default implementation for {@link IterationStateModel}.
 */
public class DefaultIterationStateModel implements IterationStateModel {
	private final int 	hyperplaneIndex;
	private final int 	booleanSize;
	private final int 	numericSize;
	
	/**
	 * Default constructor. Hyperplane index, boolean and numeric size are 
	 * derived from the efm model and the given iteration index.
	 * 
	 * @param efmModel		efm model, from which current and next iteration
	 * 						state is derived
	 * @param iteration		the current iteration, <code>0</code> for initial, 
	 * 						<code>1..n</code> for iterations 1 to n, and 
	 * 						<code>n+1</code> for the final memory. The iteration 
	 * 						index must be that index which defines the state of 
	 * 						the columns added to the memory. For instance, if a 
	 * 						memory is initialized in iteration 0 with columns to 
	 * 						be used in iteration one, the iteration index should 
	 * 						be 1.
	 */
	public DefaultIterationStateModel(NetworkEfmModel efmModel, int iteration) {
		this(
			efmModel.getHyperplaneIndex(iteration), 
			efmModel.getBooleanSize(iteration), 
			efmModel.getNumericSize(iteration)
		);
	}
	/**
	 * Constructor used from serialized instances stored in files, and 
	 * internally from the other constructor. Direct use is not recommended. Use
	 * {@link #DefaultIterationStateModel(NetworkEfmModel, int)} instead.
	 */
	public DefaultIterationStateModel(int hyperplaneIndex, int booleanSize, int numericSize) {
		this.hyperplaneIndex	= hyperplaneIndex;
		this.booleanSize		= booleanSize;
		this.numericSize		= numericSize;
	}
	
	/**
	 * Returns the iteration state model for the final step, that is, after the
	 * last iteration. This pseudo iteration step is used for post-processing.
	 * {@link NetworkEfmModel#getFinalBooleanSize() Final boolean} and
	 * {@link NetworkEfmModel#getFinalNumericSize() final numeric} size are 
	 * used, and the hyperplane index is set to -1.
	 */
	public static DefaultIterationStateModel getFinal(NetworkEfmModel efmModel) {
		return new DefaultIterationStateModel(-1, efmModel.getFinalBooleanSize(), efmModel.getFinalNumericSize());
	}
	

	//inherit javadoc
	public int getHyperplaneIndex() {
		return hyperplaneIndex;
	}
	//inherit javadoc
	public int getBooleanSize() {
		return booleanSize;
	}
	//inherit javadoc
	public int getNumericSize() {
		return numericSize;
	}
	@Override
	public String toString() {
		return "{h=" + hyperplaneIndex + ", b=" + booleanSize + ", n=" + numericSize + "}";
	}
	
}
