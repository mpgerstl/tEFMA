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
 * Abstract superclass for column inspector/modifier for all model types. Mainly
 * some helper methods are available to subclasses.
 */
abstract public class AbstractColumnInspectorModifier<N extends Number, A> implements ColumnInspectorModifier<N, A> {

	/**
	 * Returns the number of binary values to add when a column is converted for
	 * the next iteration state. This should be a non-negative value. 
	 * <p>
	 * This is usually one for the nullspace approach, and zero for the 
	 * canonical approach, if it is not the initial and not the final conversion 
	 * step.
	 * 
	 * @return {@code numericSize - }{@link IterationStepModel#getNextState() iteration.getNextState()}.{@link IterationStateModel#getNumericSize() getNumericSize()}
	 */
	protected int getConvertSizeNumeric(EfmModel model, int binarySize, int numericSize, IterationStepModel iteration) {
		return numericSize - iteration.getNextState().getNumericSize();
	}
	/**
	 * Returns the number of numeric values to remove when a column is converted 
	 * for the next iteration state.
	 * <p>
	 * This is usually one for the nullspace approach, and zero for the 
	 * canonical approach, if it is not the initial and not the final conversion 
	 * step.
	 * 
	 * @return {@link IterationStepModel#getNextState() iteration.getNextState()}.{@link IterationStateModel#getBooleanSize() getBooleanSize()}{@code - binarySize}
	 */
	protected int getConvertSizeBinary(EfmModel model, int binarySize, int numericSize, IterationStepModel iteration) {
		return iteration.getNextState().getBooleanSize() - binarySize;
	}
	
	/**
	 * Returns the index in the numeric values array for the specified
	 * hyperplane index
	 */
	protected int getNumericIndex(EfmModel model, int binarySize, int numericSize, int hyperplaneIndex) {
		return hyperplaneIndex - model.getReactionSorting().length + numericSize;  
	}

}
