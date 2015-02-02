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

import java.io.IOException;

/**
 * Default implementation for {@link IterationStepModel}. Note that usually, an
 * {@link AdjEnumModel} is used, which also implements {@link IterationStepModel},
 * but adds accessors for intermediary modes.
 */
public class DefaultIterationStepModel implements IterationStepModel {
	private final int 					iterationIndex;
	private final IterationStateModel	currentState;
	private final IterationStateModel	nextState;
	
	/**
	 * Default constructor. Iteration state models for current and next 
	 * iteration step are derived from the efm model and the given iteration
	 * index.
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
	public DefaultIterationStepModel(NetworkEfmModel efmModel, int iteration) {
		this(iteration,
			new DefaultIterationStateModel(efmModel, iteration), 
			new DefaultIterationStateModel(efmModel, iteration + 1)
		);
	}
	
	/**
	 * Returns the iteration step model for the final step, that is, after the
	 * last iteration. This pseudo iteration step is used for post-processing.
	 * The iteration index is {@link NetworkEfmModel#getIterationCount() iteration count}{@code + 1}.
	 */
	public static DefaultIterationStepModel getFinal(NetworkEfmModel efmModel) {
		return new DefaultIterationStepModel(
			efmModel.getIterationCount() + 1,
			new DefaultIterationStateModel(efmModel, efmModel.getIterationCount() + 1),	
			DefaultIterationStateModel.getFinal(efmModel)	
		);
	}
	
	/**
	 * Constructor for subclasses, for instance used for distributed computation
	 * 
	 * @param model	the model where all the information is taken from
	 */
	protected DefaultIterationStepModel(IterationStepModel model) {
		this(model.getIterationIndex(), model.getCurrentState(), model.getNextState());
	}
	/**
	 * Constructor for package and subclass use only
	 * 
	 * @param iterationIndex	The current iteration index, where 0 is before 
	 * 							the first iteration. Thus, this index is 1 
	 * 							based. The highest possible index is determined 
	 * 							by {@link NetworkEfmModel#getIterationCount()}.
	 * @param current	iteration state information reflecting data structures 
	 * 					<i>before</i> performing this step. This refers for 
	 * 					instance to the hyperplane which is currently processed, 
	 * 					and to column sizes of modes which are <i>read</i> 
	 * 					during this iteration.
	 * @param next		iteration state information reflecting data structures 
	 * 					<i>after</i> executing this step. This refers for 
	 * 					instance to the hyperplane which will be used next, and 
	 * 					thus is used to separate columns generated during this 
	 * 					step. Column sizes hence refer to modes <i>written</i> 
	 * 					during this iteration.
	 */
	protected DefaultIterationStepModel(int iterationIndex, IterationStateModel current, IterationStateModel next) {
		this.iterationIndex	= iterationIndex;
		this.currentState	= current;
		this.nextState		= next;
	}
	
	//inherit javadoc
	public int getIterationIndex() {
		return iterationIndex;
	}
	
	//inherit javadoc
	public IterationStateModel getCurrentState() {
		return currentState;
	}
	
	//inherit javadoc
	public IterationStateModel getNextState() {
		return nextState;
	}
	
	/**
	 * Default implementation performs no op
	 */
	public void closeForThread() throws IOException {
		// no op
	}
	
	@Override
	public String toString() {
		return "IterationStep[it=" + iterationIndex + "]:" + currentState + "-->" + nextState;
	}
	
}
