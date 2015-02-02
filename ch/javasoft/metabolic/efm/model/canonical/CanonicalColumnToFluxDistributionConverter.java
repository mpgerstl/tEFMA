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
package ch.javasoft.metabolic.efm.model.canonical;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AbstractColumnToFluxDistributionConverter;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.output.CallbackGranularity;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.logging.LogFragmenter;
import ch.javasoft.util.numeric.Zero;

/**
 * The <code>CanonicalColumnToFluxDistributionConverter</code> creates flux 
 * distributions from canonical columns. The columns already contain the numeric
 * values, thus, it does not use multiple threads for this process. 
 */
public class CanonicalColumnToFluxDistributionConverter<N extends Number, Col extends Column> extends AbstractColumnToFluxDistributionConverter<N, Col> {
	
	/**
	 * Constructor with column home to specify the number and column type
	 */
	public CanonicalColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome) {
		super(columnHome);
	}
	
	/**
	 * Creates a numeric flux distribution from a binary column. The columns 
	 * already contain the numeric values, thus, only reaction reordering is
	 * needed. Duplicated reversible reactions are compressed again.
	 */
	@Override
	protected FluxDistribution createFluxDistributionFromColumn(LogFragmenter log, Config config, NetworkEfmModel model, Col column, CallbackGranularity granularity) {
		final ReadableMatrix<N> stoichMatrix = model.getStoichiometricMatrix(columnHome);
		final NumberOperations<N> numberOps = stoichMatrix.getNumberOperations();
		final Zero zero		= config.zero();
		
		int size = column.numericSize();
		//reconstruct flux values
		final N[] values = numberOps.newArray(size);
		for (int ii = 0; ii < size; ii++) {
			final N value;
			if (granularity.isBinarySufficient()) {
				value = column.getNumericSignum(zero, ii) == 0 ? numberOps.zero() : numberOps.one();
			}
			else {
				 value = column.getNumeric(columnHome, ii);
			}
			values[model.getReactionSorting()[ii]] = value;
		}

		//unexpand flux values
		final N[] unexpanded = model.getReactionMapping().getUnexpandedFluxValues(columnHome, values);
		return columnHome.createFluxDistribution(model.getMetabolicNetwork(), unexpanded);
	}

}
