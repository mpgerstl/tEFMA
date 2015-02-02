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
package ch.javasoft.metabolic.efm.column;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.util.numeric.Zero;

/**
 * <tt>Column</tt> is the intermediary efm. It consists of already processed
 * entries (reactions), represented as boolean values, and entries to be 
 * processed which are still numeric values.
 */
public interface Column {
	
	/**
	 * The number of entries being represented as boolean values, that is, the
	 * values corresponding to rows (reactions, hyperplanes) which have already
	 * been processed
	 */
	int booleanSize();
	/**
	 * The number of entries being represented as numeric values, that is, the
	 * values corresponding to rows (reactions, hyperplanes) which have not yet
	 * been processed
	 */
	int numericSize();
	/**
	 * The total number of values (rows, reactions, hyperplanes). Returns the
	 * sum of {@link #booleanSize()} and {@link #numericSize()}
	 */	
	int totalSize();
	
	/**
	 * Get a value from the binary part. It is true if the corresponding 
	 * reaction has zero flux, and false otherwise.
	 * 
	 * @param bit	the binary index in the column, i.e. a value from  
	 * 				0 (inclusive) to {@link #booleanSize()} (exclusive).
	 * 				This is also the overall index since binary values 
	 * 				are before numeric values.
	 */
	boolean get(int bit);	

	/**
	 * Returns the numeric value at the given row.
	 * @param <N>			the number type
	 * @param columnHome	column home to define number type
	 * @param row			the numeric index in the column, i.e. a value
	 * 						from 0 (inclusive) to {@link #numericSize()}
	 * 						(exclusive)
	 */
	<N extends Number> N getNumeric(ColumnHome<N, ?> columnHome, int row);

	/**
	 * Returns the signum of the numeric value at the given row, i.e. a value
	 * +1/-1/0 for a positive/negative/zero value.
	 * 
	 * <p>
	 * Note that in opposition to {@link Column#get(int)}, this method returns
	 * zero for zero flux in the corresponding reaction. A return value of +1/-1
	 * corresponds to a forward/backward flux value at the related reaction.
	 * 
	 * @param zero	precision utility
	 * @param row	the numeric index in the column, i.e. a value
	 * 				from 0 (inclusive) to {@link #numericSize()} (exclusive)
	 * 
	 * @return the value +1/-1/0 for forward/backward and zero flux
	 */
	int getNumericSignum(Zero zero, int row);

	/**
	 * Returns the hyperplane sign given the column's numeric and binary values,
	 * for any given hyperplane. The sign is positive if the column lies on the
	 * strictly positive side of the hyperplane, and negative if it lies on the
	 * strictly negative side. If it lies within the hyperplane, zero is 
	 * returned.
	 * 
	 * @param model				model, e.g. to access config for precision or
	 * 							also to get access to the appropriate column
	 * 							{@link ColumnInspectorModifier inspector/modifier}
	 * 							for model and number type specific operations 
	 * @param iteration			the current iteration and related information.
	 * 							Used to identify the appropriate hyperplane
	 * 
	 * @return 	positive or negative value or zero if column is on strictly 
	 * 			positive or strictly negative side, or within the hyperplane, 
	 * 			respectively
	 */
	int getHyperplaneSign(EfmModel model, IterationStateModel iteration);

	/**
	 * The binary part is usually stored with a SimpleLongBitSet. This
	 * method returns the internal bit set, not a clone. The first bit
	 * at position 0 coincides with {@link #get(int) get(0)}.
	 */
	IBitSet bitValues();
	
	/**
	 * Convert <tt>convertCount</tt> numeric values to binary. The converted
	 * column is returned, possibly still <tt>this</tt> instance if 
	 * <tt>clone</tt> is false.
	 * 
	 * @param columnHome	column conversion utility
	 * @param model			model, e.g. to access config for precision or
	 * 						also to get access to the appropriate column
	 * 						{@link ColumnInspectorModifier inspector/modifier}
	 * 						for model and number type specific operations 
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * @param clone			if false, this column will probably be modified to
	 * 						apply the appropriate changes, otherwise, the
	 * 						returned column is always a new instance
	 * 
	 * @return 	the column after numeric-to-binary conversion, a new instance if
	 * 			<tt>clone</tt> is true, otherwise probably this instance
	 */
	<Col extends Column> Col convert(ColumnHome<?, Col> columnHome, EfmModel model, IterationStepModel iteration, boolean clone);
	
	/**
	 * Merge two adjacent columns to create an appropriate newly born column
	 * 
	 * @param columnHome	column conversion utility
	 * @param model			model, e.g. to access config for precision or
	 * 						also to get access to the appropriate column
	 * 						{@link ColumnInspectorModifier inspector/modifier}
	 * 						for model and number type specific operations 
	 * @param other			other adjacent column to be merged with
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * 
	 * @return the newly born column
	 */
	<Col extends Column> Col mergeWith(ColumnHome<?, Col> columnHome, EfmModel model, Col other, IterationStepModel iteration);
	
	/**
	 * Write this column to the given data output. See 
	 * {@link ColumnHome#readFrom(DataInput, int, int)} for the corresponding read 
	 * method.
	 */
	void writeTo(DataOutput dataOut) throws IOException;
	
	/**
	 * Returns the home object for this column implementation. The home object
	 * contains methods of static character which are column implementation 
	 * dependant.
	 */
	ColumnHome<? extends Number, ? extends Column> columnHome();

}
