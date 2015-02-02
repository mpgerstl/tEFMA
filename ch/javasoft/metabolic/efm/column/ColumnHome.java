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
import java.io.File;
import java.io.IOException;

import ch.javasoft.jbase.EntityMarshaller;
import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.numeric.Zero;

/**
 * The home object for a column implementation contains methods of static 
 * character which are column implementation dependent. Usually, one single
 * home instance exists per class implementing Column.
 * 
 * @type N		the number type for numeric values in the column
 * @type Col	the column class which this column-home belongs to
 */
public interface ColumnHome<N extends Number, Col extends Column> {
	/** 
	 * Returns a new instance of Column with given boolean/numeric sizes.
	 * No binary values are set, and all numeric values are 0.
	 */
	Col newInstance(int booleanSize, int numericSize);
	/** 
	 * Returns new instances from a matrix. The numeric values reflect the
	 * values of a column in the matrix. All bits in the binary part of the 
	 * returned columns are false, i.e. not set.
	 */
	Col[] newInstances(ReadableMatrix<N> matrix, int booleanSize);
	/** Reads a column from the data input, assuming the given boolean/numeric sizes*/
	Col readFrom(DataInput dataIn, int booleanSize, int numericSize) throws IOException;
	/** Writes the given column to the data output*/
	void writeTo(Col column, DataOutput dataOut) throws IOException;
    /** Create a table to store the columns
     * @throws IOException */
	EntityMarshaller<Col> getEntityMarshaller(int booleanSize, int numericSize) throws IOException;
    /** Create a table to store the columns
     * @throws IOException */
	ConcurrentTable<Col> createTable(File folder, String fileName, int booleanSize, int numericSize) throws IOException;
    /** Opens a table to read columns from
     * @throws IOException */
	ConcurrentTable<Col> openTable(File folder, String fileName, int booleanSize, int numericSize) throws IOException;
	/** Returns the underlying arithmetic*/
	Arithmetic getArithmetic();
	/** The number operations for numeric operations on the underlying data type*/
	NumberOperations<N> getNumberOperations();
	/** Returns a new flux distribution with the appropriate backing number type*/
	FluxDistribution createFluxDistribution(MetabolicNetwork net, N[] values);

	/**
	 * Note that matrix elements might be adjusted, and a new matrix instance
	 * is possibly returned. Thus, this method is expensive and the converted
	 * matrix should be cached. 
	 * <p>
	 * In opposition, {@link #castMatrix(ReadableMatrix)} is always fast, does
	 * not create new matrix instances and solely converts to the appropriate
	 * matrix type.
	 * <p>
	 * Note that either <tt>allowRowScaling</tt> or <tt>allowColumnScaling</tt>
	 * should be true, or a conversion might not be possible.
	 *
	 * @param matrix				the matrix to be converted
	 * @param allowRowScaling		if true, rows might be rescaled, e.g. to
	 * 								get rid of fraction number denominators 
	 * @param allowColumnScaling	if true, columns might be rescaled, e.g. to
	 * 								get rid of fraction number denominators
	 * @return the matrix converted into the column's internal data type
	 * 
	 * @see #castMatrix(ReadableMatrix)
	 */
	ReadableMatrix<N> convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling);
	/** 
	 * Casts the given untyped matrix into a matrix of this column home's 
	 * number type. If the given matrix is not of the expected type, a 
	 * {@link ClassCastException} is thrown.
	 * 
	 * @see #convertMatrix(ReadableMatrix, boolean, boolean)
	 */
	ReadableMatrix<N> castMatrix(ReadableMatrix matrix);
	/** 
	 * Casts the given untyped column into a column of this home's type. 
	 * If the given column is not of the expected, a 
	 * {@link ClassCastException} is thrown.
	 */
	Col castColumn(Column column);
	/** 
	 * Casts the given untyped number into a number of this home's type. 
	 * If the given number is not of the expected type, a 
	 * {@link ClassCastException} is thrown.
	 */
	N castNumber(Number number);

	/**
	 * Computes the rank of the given matrix, using the underlying numerics
	 */
	int rank(ReadableMatrix matrix, Zero zero);
}