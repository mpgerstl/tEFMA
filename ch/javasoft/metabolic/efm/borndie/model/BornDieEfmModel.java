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
package ch.javasoft.metabolic.efm.borndie.model;

import java.util.concurrent.atomic.AtomicInteger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifierFactory;
import ch.javasoft.metabolic.efm.model.nullspace.NullspaceEfmModel;

/**
 * Model for born/die implementation, based on nullspace approach. It is 
 * basically the same as the {@link NullspaceEfmModel nullspace model}, but
 * numeric values cannot be converted to binary. Furthermore, multi-threading
 * for adjacency enumeration is different since the born/die algorithm uses its
 * own paralellization strategy.
 */
public class BornDieEfmModel extends NullspaceEfmModel {
	
	private final AtomicInteger adjEnumThreads = new AtomicInteger(1);

	/**
	 * Constructor
	 */
	public <N extends Number> BornDieEfmModel(ColumnHome<N, ?> columnHome, MetabolicNetwork net, Config config, ColumnInspectorModifierFactory factory) {
		super(columnHome, net, config, factory);
	}
	
	/**
	 * Returns the number of threads to use for adjacency enumeration. Returns 
	 * usually one, but increases towards {@link Config#getMaxThreads()} for the 
	 * last bearing columns.
	 */
	@Override
	public int getAdjEnumThreads() {
		return adjEnumThreads.get();
	}
	
	/**
	 * Set the current number of threads to use for adjacency enumeration. This
	 * method is used by {@link BornDieController}.
	 */
	public void setAdjEnumThreads(int threads) {
		if (threads < 1) throw new IllegalArgumentException("at least one thread is required: " + threads);
		adjEnumThreads.set(threads);
	}
	
//	/**
//	 * Returns a new {@link BornDieColumnToFluxDistributionConverter} instance
//	 */
//	@Override
//	public <N extends Number, Col extends Column> ColumnToFluxDistributionConverter<N, Col> getColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome) {
//		return new BornDieColumnToFluxDistributionConverter<N, Col>(columnHome);
//	}

//	@Override
//	public <N extends Number, Col extends Column> AppendableMemory<Col> createInitialMemory(ColumnHome<N, Col> columnHome, MemoryFactory memoryFactory) throws IOException {
//		final Col[] cols = columnHome.newInstances(getKernelMatrix(columnHome), 0);
//		final AppendableMemory<Col> memory = memoryFactory.createReadWriteMemory(columnHome, this, 1, null);
//		memory.appendColumns(Arrays.asList(cols));
//		return memory;
//	}

//	/**
//	 * Returns zero since we can drop all binary values in the post processing
//	 * step
//	 */
//	@Override
//	public int getFinalBooleanSize() {
//		return kernelMatrix.getRowCount();
//	}
	/**
	 * Returns the number of rows in the kernel matrix, minus the initial 
	 * boolean size. In the born/die algorithm, numeric values cannot be 
	 * converted to binary since they are still used by pairing jobs.
	 */
	@Override
	public int getFinalNumericSize() {
		return kernelMatrix.getRowCount() - getBooleanSize(1);
	}
	/**
	 * Returns the {@link #getFinalNumericSize() final numeric size} since
	 * numeric values cannot be converted to binary.
	 */
	@Override
	public int getNumericSize(int iteration) {
		return getFinalNumericSize();
	}
	

}
