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
package ch.javasoft.metabolic.efm.adj;

import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;

/**
 * An <code>AdjEnum</code> solves the adjacency test, or more 
 * precisely the enumeration of adjacent rays. To be usable in a factory, it 
 * should have a public constructor with no arguments.
 */
public interface AdjEnum {
    /**
     * Initialize is called once, sometime after constructing this enumerator. 
     * 
     * @param columnHome	The column home to define column and number types
     * @param config		The constraints with precision, log level etc.
     * @param efmModel		The model, with stoichiometric matrix, reaction mapping etc.
     */
	<Col extends Column, N extends Number> void initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel);
    /**
     * This method is called in every iteration step of the dd method to 
     * enumerate the adjacent columns. The input columns are partitioned by
     * sign. New born columns derive from adjacent columns (in.pos, in.neg)
     * and are added to out, partitioned by the sign for the next iteration.
     * 
     * @param columnHome	The column home to define col and number types
     * @param itModel		The model with access to positive, zero and
     * 						negative source columns, and writable access for
     * 						new born columns from adjacent positive/negative
     * 						column pairs.
     */
	<Col extends Column, N extends Number> void adjacentPairs(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel) throws IOException;
	/**
	 * @return	The enumerator's name, used to identify it in a factory.
	 */
	String name();
}
