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
package ch.javasoft.metabolic.efm.borndie;

import java.io.IOException;

import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.model.BornDieEfmModel;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.impl.AbstractDoubleDescriptionImpl;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.model.EfmModelFactory;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;

/**
 * The <code>BornDieDoubleDescriptionImpl</code> implementation does not work
 * off the iteration steps sequentially. Instead, a {@link BornDieMatrix} is
 * created, which stores the intermediary modes in matrix cells. Each cell 
 * stands for a born-die index, i.e. each mode is born at a certain iteration 
 * step, and dies at a later step. With this approach, the mode generation is
 * split into smaller subtasks which can be run partially in parallel.
 */
public class BornDieDoubleDescriptionImpl extends AbstractDoubleDescriptionImpl {
	
    /**
	 * Constructor with config access and the two factories for model and 
	 * memory. Note that most factories have default constructors without 
	 * arguments, and can thus be defined in the configuration file.
	 */
	public BornDieDoubleDescriptionImpl(Config config, EfmModelFactory modelFactory, MemoryFactory memoryFactory) {
		super(config, modelFactory, memoryFactory);
	}

	@Override
	protected <N extends Number, Col extends Column> IterableMemory<Col> iterate(final ColumnHome<N,Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> memory) throws IOException {
		//start algorithm
		final BornDieController<Col> controller = new BornDieController<Col>(columnHome, getConfig(), (BornDieEfmModel)efmModel, getMemoryFactory());
		controller.start(memory);

		//await termination
		final IterableMemory<Col> result = controller.awaitTermination();
        return result;
    }
	
}
