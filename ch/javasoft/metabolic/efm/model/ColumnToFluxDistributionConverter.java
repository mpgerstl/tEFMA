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

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;

/**
 * Converts columns to flux distributions, possibly involving uncompression of
 * binary to numeric values.
 * <p>
 * Implementations depend on the efm model, e.g. for canonical models, this is 
 * a rather simple process since all numeric values are already contained in 
 * the columns. For nullspace models, the binary values must be converted back 
 * into numeric values.
 * 
 * @param <N>			the number type of the numeric values
 * @param <Col>			the column type
 */
public interface ColumnToFluxDistributionConverter<N extends Number, Col extends Column> {
	/**
	 * Writes the columns to the specified callback, including uncompression of
	 * flux modes and appending of external-to-external modes. 
	 * <p>
	 * The method might use multiple threads to perform this operation. 
	 * Writes columns the the callback. For external-to-external reactions, an
	 * efm is appended. 
	 * 
	 * @param config		configuration stuff, e.g. to find out about the
	 * 						desired normalization
	 * @param model			the efm model, usually canonical or nullspace
	 * @param columns		the columns, w/o external-to-external efms
	 * @param callback		the callback to write to
	 * @throws IOException	if an i/o exception occurs
	 */
	 void writeColumnsToCallback(final Config config, final NetworkEfmModel model, final Iterable<Col> columns, final EfmOutputCallback callback) throws IOException;
	 
	/**
	 * Converts a column into a flux distribution. This might involve 
	 * uncompression of the column to derive numeric values from boolean 
	 * columns. 
	 * 
	 * @param config		configuration stuff, e.g. to find out about the
	 * 						desired normalization
	 * @param model			the efm model, usually canonical or nullspace
	 * @param column		the column
	 * @return 				the flux distribution
	 */
	FluxDistribution createFluxDistributionFromColumn(Config config, NetworkEfmModel model, Col column);

}
