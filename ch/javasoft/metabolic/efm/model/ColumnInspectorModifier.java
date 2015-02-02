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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.ColumnHome;

/**
 * The column inspector/modifier handles handles functions which depend on the
 * internal data of the columns and on the {@link EfmModel model}. For instance, 
 * the hyperplane sign (side) of a column for a given hyperplane can be 
 * evaluated. It also handles column modifications for different column and
 * model types. Column modifications happen if positions are changed from 
 * numeric to binary (for kept columns) or if a new column is born (from two
 * adjacent columns). 
 * 
 * @type N	the type for numeric values
 * @type A	the array type numeric values
 */
public interface ColumnInspectorModifier<N extends Number, A> {
	/**
	 * Returns the hyperplane sign given the column's numeric and binary values,
	 * for any given hyperplane. The sign is positive if the column lies on the
	 * strictly positive side of the hyperplane, and negative if it lies on the
	 * strictly negative side. If it lies within the hyperplane, zero is 
	 * returned.
	 * 
	 * @param columnHome		used as conversion utility for numeric stuff
	 * @param model 			efm model to access config, stoich matrix etc.
	 * @param binaryVals		the binary part of the column
	 * @param binarySize		the size of the binary part of the column
	 * @param numericVals		the numeric part of the column
	 * @param iterationState	the iteration state to identify the appropriate 
	 * 							hyperplane
	 * 
	 * @return 	positive or negative value or zero if column is on strictly 
	 * 			positive or strictly negative side, or within the hyperplane, 
	 * 			respectively
	 */
	int getHyperplaneSign(ColumnHome<N, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, A numericVals, IterationStateModel iterationState);
	
	/**
	 * Merge the given values of two adjacent columns to create the appropriate
	 * binary values of the newly born column
	 * 
	 * @param columnHome		used as conversion utility for numeric stuff
	 * @param model 			efm model to access config, stoich matrix etc.
	 * @param binaryValsCol1	the binary part of column 1
	 * @param binarySizeCol1	the size of the binary part of column 1
	 * @param numericValsCol1	the numeric part of column 1
	 * @param binaryValsCol2	the binary part of column 2
	 * @param binarySizeCol2	the size of the binary part of column 2
	 * @param numericValsCol2	the numeric part of column 2
	 * @param iteration			the current iteration and related information.
	 * 							Used to identify the appropriate hyperplane
	 * 
	 * @return the binary part of the newly born column
	 */
	IBitSet mergeBinary(ColumnHome<N, ?> columnHome, EfmModel model, IBitSet binaryValsCol1, int binarySizeCol1, A numericValsCol1, IBitSet binaryValsCol2, int binarySizeCol2, A numericValsCol2, IterationStepModel iteration);

	/**
	 * Merge the given values of two adjacent columns to create the appropriate
	 * numeric values of the newly born column
	 * 
	 * @param columnHome		used as conversion utility for numeric stuff
	 * @param model 			efm model to access config, stoich matrix etc.
	 * @param binaryValsCol1	the binary part of column 1
	 * @param binarySizeCol1	the size of the binary part of column 1
	 * @param numericValsCol1	the numeric part of column 1
	 * @param binaryValsCol2	the binary part of column 2
	 * @param binarySizeCol2	the size of the binary part of column 2
	 * @param numericValsCol2	the numeric part of column 2
	 * @param iteration			the current iteration and related information.
	 * 							Used to identify the appropriate hyperplane
	 * 
	 * @return the numeric part of the newly born column
	 */
	A mergeNumeric(ColumnHome<N, ?> columnHome, EfmModel model, IBitSet binaryValsCol1, int binarySizeCol1, A numericValsCol1, IBitSet binaryValsCol2, int binarySizeCol2, A numericValsCol2, IterationStepModel iteration);
	
	/**
	 * Convert binary values from numeric to binary.
	 * 
	 * @param columnHome	used as conversion utility for numeric stuff
	 * @param model 		efm model to access config, stoich matrix etc.
	 * @param binaryVals 	the binary part of the column
	 * @param binarySize	the size of the binary part of the column
	 * @param numericVals 	the numeric part of the column
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * @param clone			if true, the returned values are a new instance in
	 * 						any case, if false, it might be the same instance as
	 * 						the input values
	 * 
	 * @return the binary part of the column after numeric-to-binary conversion
	 */
	IBitSet convertBinary(ColumnHome<N, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, A numericVals, IterationStepModel iteration, boolean clone);
	/**
	 * Convert numeric values from numeric to binary.
	 * 
	 * @param columnHome	used as conversion utility for numeric stuff
	 * @param model 		efm model to access config, stoich matrix etc.
	 * @param binaryVals 	the binary part of the column
	 * @param binarySize	the size of the binary part of the column
	 * @param numericVals 	the numeric part of the column
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * @param clone			if true, the returned values are a new instance in
	 * 						any case, if false, it might be the same instance as
	 * 						the input values
	 * 
	 * @return the numeric part of the column after numeric-to-binary conversion
	 */
	A convertNumeric(ColumnHome<N, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, A numericVals, IterationStepModel iteration, boolean clone);
}
