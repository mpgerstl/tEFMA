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
package ch.javasoft.metabolic.efm.dist;

import java.io.IOException;

import ch.javasoft.metabolic.efm.adj.AdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.ModelPersister;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;

/**
 * The <code>DistributedMemAdjEnum</code> enumerates adjacent rays in two
 * phases, a non-distributed phase, where common data structures (e.g. pattern
 * trees stored in files) are set up, and a distributed phase, possibly running
 * concurrently on different host machines.
 */
public interface DistributedAdjEnum {
	
	/**
	 * Name for factories
	 */
	String name();
	
	/**
	 * Returns the model persister, used to store and restore distributed model 
	 * information to and from files
	 */
	ModelPersister getModelPersister();
	
    /**
     * This method is called first in every iteration step, before distributing
     * the job to multiple hosts. Common structures (e.g. trees stored in files)
     * can be set up here. It is a merge of 
     * {@link AdjEnum#initialize(ColumnHome, Config, EfmModel) OutCoreAdjacencyEnumerator.initialize(...)}
     * and the first undistributed phase of 
     * {@link AdjEnum#adjacentPairs(ColumnHome, AdjEnumModel) OutCoreAdjacencyEnumerator.adjacentPairs(...)}.
     * The part corresponding to the second distributed phase is executed in
     * {@link #execDistributed(ColumnHome, Config, EfmModel, AdjEnumModel, DistributedInfo, PartIterator, ProgressAggregator) execDistributed(...)}.
     * 
     * @param columnHome	The column home to define column and number types
     * @param config		The constraints with precision, log level etc.
     * @param efmModel		The model, with stoichiometric matrix, reaction mapping etc.
     * @param itModel		The model with access to positive, zero and
     * 						negative source columns, and writable access for
     * 						new born columns from adjacent positive/negative
     * 						column pairs.
     */
	<Col extends Column, N extends Number> void execCentralized(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel, AdjEnumModel<Col> itModel) throws IOException;
    /**
     * This method is called in every iteration step and represents the second 
     * distributed part typically executed on multiple hosts concurrently. The
     * real adjacent ray enumeration is performed here (e.g. by traversing the
     * trees). It corresponds to the second distributed phase of 
     * {@link AdjEnum#adjacentPairs(ColumnHome, AdjEnumModel) OutCoreAdjacencyEnumerator.adjacentPairs(...)}.
     * The part corresponding to the first undistributed phase is executed in
     * {@link #execCentralized(ColumnHome, Config, EfmModel, AdjEnumModel) execCentralized(...)}.
     * 
     * 
     * @param columnHome	The column home to define col and number types
     * @param config		The constraints with precision, log level etc.
     * @param efmModel		The model, with stoichiometric matrix, reaction mapping etc.
     * @param itModel		The model with access to positive, zero and
     * 						negative source columns, and writable access for
     * 						new born columns from adjacent positive/negative
     * 						column pairs.
     * @param distInfo		information for distributed computation concerning
     * 						the current node, e.g. node index etc.
     * @param partIterator	the part iterator to access the next partition after
     * 						finishing the current part. 
     * @param progress		the progress aggregator to use, e.g. aggregates 
     * 						sends progress information and sends aggregated
     * 						progress to a master process
     */
	<Col extends Column, N extends Number> void execDistributed(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel, AdjEnumModel<Col> itModel, DistributedInfo distInfo, PartIterator partIterator, ProgressAggregator progress) throws IOException;

}
