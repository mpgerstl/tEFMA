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
 * The iteration step model contains information about the current iteration 
 * step, which typically involves information for data structures involved in 
 * the current and the next iteration. 
 * <p>
 * Important figures for the iteration step include both the current and the
 * next iteration, since columns are read according to the current key figures,
 * but are written in the format of the next step.
 */
public interface IterationStepModel {
	/**
	 * The current iteration index, where 0 is before the first iteration. Thus,
	 * this index is 1 based. The highest possible index is determined by
	 * {@link NetworkEfmModel#getIterationCount()}.
	 * 
	 * @return	the current iteration index
	 */
	int getIterationIndex();
	
	/**
	 * Returns iteration state information reflecting data structures 
	 * <i>before</i> performing this step. This refers for instance to the 
	 * hyperplane which is currently processed, and to column sizes of modes
	 * which are <i>read</i> during this iteration.
	 */
	IterationStateModel getCurrentState();
	
	/**
	 * Returns iteration state information reflecting data structures 
	 * <i>after</i> executing this step. This refers for instance to the 
	 * hyperplane which will be used next, and thus is used to separate columns
	 * generated during this step. Column sizes hence refer to modes
	 * <i>written</i> during this iteration.
	 */
	IterationStateModel getNextState();

	/**
	 * Close underlying resources for the current thread.
	 */
	void closeForThread() throws IOException;

}
