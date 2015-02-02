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
package ch.javasoft.metabolic.efm.dist.impl;

import java.io.IOException;

import ch.javasoft.metabolic.efm.adj.AdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;

/**
 * The <code>DistributableAdjEnum</code> interface allows for enumeration of
 * adjacent in a distributed manner. The enumeration is split into two phases, 
 * a non-distributed phase, where common data structures (like pattern
 * trees stored in files) are set up, and a distributed phase, possibly running
 * concurrently on different host machines.
 */
public interface DistributableAdjEnum {
	
	/**
	 * Name for factories
	 */
	String name();
	
    /**
     * This method is called first in every iteration step, before distributing
     * the job to multiple hosts. Common structures (e.g. trees stored in files)
     * can be set up here. Compared to standard {@link AdjEnum} this is a merge 
     * of {@link AdjEnum#initialize(ColumnHome, Config, EfmModel) OutCoreAdjacencyEnumerator.initialize(...)}
     * and the first undistributed phase of 
     * {@link AdjEnum#adjacentPairs(ColumnHome, AdjEnumModel) OutCoreAdjacencyEnumerator.adjacentPairs(...)}.
     * The part corresponding to the second distributed phase is executed in
     * separate processes or threads. Such jobs can be instantiated using the 
     * {@link DistJobController} returned by this method.
     * 
     * @param columnHome	The column home to define column and number types
     * @param config		The constraints with precision, log level etc.
     * @param efmModel		The model, with stoichiometric matrix, reaction mapping etc.
     * @param itModel		The model with access to positive, zero and
     * 						negative source columns, and writable access for
     * 						new born columns from adjacent positive/negative
     * 						column pairs.
     * @param nodeCount		The number of parallel nodes to start up, usually
     * 						the smaller of number of jobs and number of nodes
     * 						in the distributed config
     * @return 	a factory to derive the jobs for distributed computation, either
     * 			in an own process (in another jvm) or as new thread (same jvm)
     */
	<Col extends Column, N extends Number> DistJobController initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel, AdjEnumModel<Col> itModel, int nodeCount) throws IOException;
}
