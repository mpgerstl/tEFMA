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

import java.util.logging.Logger;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.rankup.RankUpRoot;
import ch.javasoft.smx.iface.ReadableMatrix;

/**
 * The <code>EfmModel</code> contains information needed throughout the efm
 * computation, such as stoichiometric matrix, configuration etc. A common 
 * extension is available, also supporting access to the metabolic network 
 * (see {@link NetworkEfmModel}.
 * <p>
 * The efm model is not only a data holder object, but also offers model 
 * specific functionality, e.g. to partition columns during an iteration.
 * 
 */
public interface EfmModel extends RankUpRoot {
	/**
	 * Returns access to the configuration stuff
	 */
	Config getConfig();
	
	/**
	 * Returns the current arithmetic, e.g. double or fractional
	 */
	Arithmetic getArithmetic();
	
	/**
	 * Returns the number of threads to use for adjacent mode enumeration. This
	 * is usually the same as {@link Config#getMaxThreads()}, but might also
	 * differ if the algorithm itself also uses multi-threading.
	 *  
	 * @return	the number of threads to use for adjacent mode enumeration
	 */
	int getAdjEnumThreads();
	
	/**
	 * Returns the stoichiometric matrix
	 * 
	 * @param columnHome	the column home to determine the number type
	 */
	<N extends Number> ReadableMatrix<N> getStoichiometricMatrix(ColumnHome<N, ?> columnHome);
	
	/**
	 * Returns the reaction sorting. The value at [0] contains the original 
	 * reaction index of the reaction which is currently at position 0. For 
	 * instance, the first column in the stoichiometric matrix reflects the
	 * reaction sorting[0].  
	 * <p>
	 * Note that this is not a clone, thus, changes are effecting the algorithm.
	 */
	int[] getReactionSorting();
	/**
	 * Returns the metabolite sorting. The value at [0] contains the original 
	 * metabolite index of the metabolite which is currently at position 0. For 
	 * instance, the first row in the stoichiometric matrix reflects the
	 * metabolite sorting[0].  
	 * <p>
	 * Note that this is not a clone, thus, changes are effecting the algorithm.
	 */
	int[] getMetaboliteSorting();
	
	/**
	 * Factory to create the appropriate column inspector/modifier factory, 
	 * which can be used to inspect column data or to modify columns, e.g. to
	 * {@link Column#mergeWith(ColumnHome, EfmModel, Column, IterationStepModel) merge}
	 * columns.
	 */
	ColumnInspectorModifierFactory getColumnInspectorModifierFactory();
	
	/**
	 * Logs the efm model contents at different log levels. Subclasses
	 * should override this method, but initially call the super method.
	 * 
	 * @param logger	the logger to log with
	 */
	 <N extends Number> void log(ColumnHome<N, ?> columnHome, Logger logger);
}
