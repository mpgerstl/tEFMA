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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AbstractColumnToFluxDistributionConverter;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.output.CallbackGranularity;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.logging.LogFragmenter;
import ch.javasoft.util.logging.LogPrintWriter;
import ch.javasoft.util.numeric.Zero;

/**
 * The <code>NullspaceColumnToFluxDistributionConverter</code> creates flux 
 * distributions from nullspace columns. The columns contain binary values,
 * thus, multiple threads are used to speed up the uncompression process. 
 */
public class NullspaceColumnToFluxDistributionConverter<N extends Number, Col extends Column> extends AbstractColumnToFluxDistributionConverter<N, Col> {
	
	private static final Logger LOG = LogPkg.LOGGER;
	
	/**
	 * Constructor with column home to specify the number and column type
	 */
	public NullspaceColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome) {
		super(columnHome);
	}
	
	/**
	 * Creates a numeric flux distribution from a binary column. The nullspace
	 * is computed to do so. Furthermore, duplicated reversible reactions are
	 * compressed again.
	 */
	@Override
	protected FluxDistribution createFluxDistributionFromColumn(LogFragmenter log, Config config, NetworkEfmModel model, Col column, CallbackGranularity granularity) {
		final ReadableMatrix<N> stoichMatrix = model.getStoichiometricMatrix(columnHome);
		final NumberOperations<N> numberOps = stoichMatrix.getNumberOperations();
		final Zero zero	= config.zero();
		
		//collect nonzero columns
		int nonZeroCount	= 0;
//		int size			= column.totalSize();
		int size			= model.getReactionSorting().length;
		int boolSize		= column.booleanSize();
		int numSize			= column.numericSize();
		int[] colIndices	= new int[size];
		for (int ii = 0; ii < size; ii++) {
			if (ii < boolSize && !column.get(ii) || ii >= boolSize && 0 != column.getNumericSignum(zero, numSize + ii - size)) {
				//true means fulfilled with 0, so true is 0, false is non-zero
				colIndices[nonZeroCount] = model.getReactionSorting()[ii];
				nonZeroCount++;
			}
		}

		//reconstruct flux values
		final N[] values = numberOps.newArray(size);
		Arrays.fill(values, numberOps.zero());
		if (granularity.isBinarySufficient()) {
			for (int i = 0; i < nonZeroCount; i++) {
				values[colIndices[i]] = numberOps.one();
			}
		}
		else {
			final N[][] subStoichValues	= numberOps.newArray(
					model.getMetabolicNetwork().getMetabolites().length(), nonZeroCount);
			for (int row = 0; row < subStoichValues.length; row++) {
				for (int col = 0; col < subStoichValues[row].length; col++) {
					subStoichValues[row][col] = stoichMatrix.getNumberValueAt(row, colIndices[col]);
				}
			}
			final ReadableMatrix<N> subStoichMatrix = stoichMatrix.newInstance(subStoichValues, true).toReadableMatrix(false /*enforceNewInstance*/);
			final ReadableMatrix untypedFluxes = nullspace(subStoichMatrix, zero);
			final ReadableMatrix<N> fluxes = columnHome.convertMatrix(untypedFluxes, false /*allowRowScaling*/, true /*allowColumnScaling*/);
		
			//valid flux?
			if (fluxes.getColumnCount() != 1) {
	//			LOG.warning("cannot reconstruct flux for efm " + column + " [" + fluxes.getColumnCount() + " matches]");
	//			LOG.warning("nullspace for " + (traceStr == null ? "efm" : traceStr) + " is:");
	//			Transpose.transpose(fluxes).writeToMultiline(new LogWriter(LOG, Level.WARNING));
	//			LOG.warning("singular values: " + Arrays.toString(new MtOpsImpl(zero().precision()).getSignularValues(subStoichMatrix)));
				if (log != null) log.cleanUp();
				LOG.warning("uncompression failed for efm.");
				LOG.warning("sub-stoich-matrix for this efm:");
				LogPrintWriter logWriter = new LogPrintWriter(LOG, Level.INFO);
				subStoichMatrix.writeToMultiline(logWriter);
				logWriter.flush();
				throw new CannotReconstructFluxException(column, fluxes);			
			}
		
			//create flux & make all positive
			int negCnt = 0;
			int posCnt = 0;
			for (int ii = 0; ii < fluxes.getRowCount(); ii++) {
				final N value = fluxes.getNumberValueAt(ii, 0);
				values[colIndices[ii]] = value;
				final int sgn = numberOps.signum(value);
				if (sgn > 0) posCnt++;
				else if (sgn < 0) negCnt++;
			}
			if (posCnt > 0 && negCnt > 0 && size == boolSize) {
				throw new CannotReconstructFluxException(
					"negative and positive fluxes: " + Arrays.toString(values), column, fluxes
				);
			}
			else {
				if (negCnt > posCnt) {
					//make them all positive
					for (int ii = 0; ii < values.length; ii++) {
						values[ii] = numberOps.negate(values[ii]);
					}				
				}
			}
		}
		//unexpand flux values
		final N[] unexpanded = model.getReactionMapping().getUnexpandedFluxValues(columnHome, values);
		return columnHome.createFluxDistribution(model.getMetabolicNetwork(), unexpanded);
	}
	@SuppressWarnings("unchecked")
	private static <N extends Number> ReadableMatrix<N> nullspace(ReadableMatrix<N> subStoich, Zero zero) {
		if (subStoich instanceof DoubleMatrix) {
			return (ReadableMatrix<N>)new Gauss(zero.mZeroPos).nullspace((DoubleMatrix)subStoich);
		}
		else if (subStoich instanceof BigIntegerRationalMatrix) {
			return (ReadableMatrix<N>)Gauss.getRationalInstance().nullspace((BigIntegerRationalMatrix)subStoich);
		}
		else if (subStoich instanceof ReadableBigIntegerRationalMatrix) {
			return (ReadableMatrix<N>)Gauss.getRationalInstance().nullspace((ReadableBigIntegerRationalMatrix)subStoich);
		}
		throw new IllegalArgumentException("unsupported matrix type: " + subStoich.getClass());
	}

}
