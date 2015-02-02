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
package ch.javasoft.metabolic.efm.model.nullspace;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.model.AbstractNetworkEfmModel;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifierFactory;
import ch.javasoft.metabolic.efm.model.ColumnToFluxDistributionConverter;
import ch.javasoft.metabolic.efm.model.DefaultIterationStepModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.util.EfmHelper;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.logging.LogPrintWriter;

/**
 * Model for nullspace implementations
 */
public class NullspaceEfmModel extends AbstractNetworkEfmModel {
	/** the nullspace (kernel) matrix*/
	protected final ReadableMatrix kernelMatrix;
	
	public <N extends Number> NullspaceEfmModel(ColumnHome<N, ?> columnHome, MetabolicNetwork net, Config config, ColumnInspectorModifierFactory factory) {
		super(columnHome, net, config, factory);
		//FIXME stoich/kernel matrix type stuff
		final ReadableMatrix stoich = EfmHelper.createKernel(net, getStoichRational(), getReactionSorting(), config, false /*log*/);
		kernelMatrix = columnHome.convertMatrix(stoich, false /*allowRowScaling*/, true /*allowColumnScaling*/);
		getReactionMapping().refreshSortMapping();
	}
	
	/**
	 * Returns a new {@link NullspaceColumnToFluxDistributionConverter} instance
	 */
	public <N extends Number, Col extends Column> ColumnToFluxDistributionConverter<N, Col> getColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome) {
		return new NullspaceColumnToFluxDistributionConverter<N, Col>(columnHome);
	}

	public <N extends Number, Col extends Column> AppendableMemory<Col> createInitialMemory(ColumnHome<N, Col> columnHome, MemoryFactory memoryFactory) throws IOException {
		final Col[] cols = columnHome.newInstances(getKernelMatrix(columnHome), 0);
		//set binary entries, an identity matrix with false values on the 
		//diagonal, and off-diagonal true values reflecting the zero flux values
		final IterationStepModel itModel = new DefaultIterationStepModel(this, 0);//the 'before-first' iteration
		for (int i = 0; i < cols.length; i++) {
			cols[i] = cols[i].convert(columnHome, this, itModel, false /*clone*/);
		}
		final AppendableMemory<Col> memory = memoryFactory.createReadWriteMemory(columnHome, this, 1, null);
		memory.appendColumns(Arrays.asList(cols));
		return memory;
	}
	
	/**
	 * Returns true for the negative partition.
	 * 
	 * @see NetworkEfmModel#cutOff(Partition)
	 */
	public boolean cutOff(Partition partition) {
		return Partition.Negative.equals(partition);
	}
    
	public int getHyperplaneIndex(int iteration) {
		return getBooleanSize(iteration);
	}
	
	public <N extends Number> ReadableMatrix<N> getKernelMatrix(ColumnHome<N, ?> columnHome) {
		return columnHome.castMatrix(kernelMatrix);
	}
	
	public int getIterationCount() {
		return 
			kernelMatrix.getRowCount() - kernelMatrix.getColumnCount() -
			getOutOfIterationLoopCount();
	}
	
	public int getBooleanSize(int iteration) {
		return iteration == 0 ? 0 : kernelMatrix.getColumnCount() + iteration - 1;
	}		
	public int getNumericSize(int iteration) {
		return kernelMatrix.getRowCount() - getBooleanSize(iteration);
	}
	
	public int getFinalBooleanSize() {
		return kernelMatrix.getRowCount() - getFinalNumericSize();
	}

	public int getFinalNumericSize() {
		return getOutOfIterationLoopCount();
	}

	@Override
	public <N extends Number> void log(ColumnHome<N, ?> columnHome, Logger logger) {
		super.log(columnHome, logger);
		logger.info(
			"kernel matrix has dimensions " + 
			kernelMatrix.getRowCount() + "x" + kernelMatrix.getColumnCount()
		);
		if (logger.isLoggable(Level.FINER)) {
			final LogPrintWriter finerWriter = new LogPrintWriter(logger, Level.FINER);
			finerWriter.println("kernel matrix (unmapped):");
			kernelMatrix.writeToMultiline(finerWriter);
			finerWriter.println("kernel matrix:");
			ReactionMapping.unsortKernelMatrixRows(getKernelMatrix(columnHome), getReactionSorting()).writeToMultiline(finerWriter);
			finerWriter.println("mx * kernel:");
			EfmHelper.mulMapped(getStoichiometricMatrix(columnHome), getKernelMatrix(columnHome), getReactionSorting()).writeToMultiline(finerWriter);			
		}
	}

}