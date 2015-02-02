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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.VarIntColumn;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.util.CanonicalUtil;
import ch.javasoft.metabolic.efm.util.ColumnUtil;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.Arrays;

/**
 * Column {@link ColumnInspectorModifier inspector/modifier} for canonical model
 * and {@link VarIntColumn}
 */
public class CanonicalVarIntColumnInspectorModifier extends AbstractCanonicalColumnInspectorModifier<VarIntNumber, VarIntNumber[]> {

	@Override
	protected VarIntNumber[] copyOf(VarIntNumber[] numericVals) {
		return Arrays.copyOf(numericVals, numericVals.length);
	}
	public int getHyperplaneSign(ColumnHome<VarIntNumber, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, VarIntNumber[] numericVals, IterationStateModel iteration) {
		final ReadableMatrix<VarIntNumber> stoich = model.getStoichiometricMatrix(columnHome);
		final int hyperIndex = iteration.getHyperplaneIndex();
		final VarIntNumber val = CanonicalUtil.getInequalityValue(stoich, hyperIndex, numericVals, model.getReactionSorting());
		return val.signum();
	}

	public VarIntNumber[] mergeNumeric(ColumnHome<VarIntNumber, ?> columnHome, EfmModel model, IBitSet binaryValsCol1, int binarySizeCol1, VarIntNumber[] numericValsCol1, IBitSet binaryValsCol2, int binarySizeCol2, VarIntNumber[] numericValsCol2, IterationStepModel iteration) {
		final ReadableMatrix<VarIntNumber> stoich = model.getStoichiometricMatrix(columnHome);
		final int hyperplaneIndex = iteration.getCurrentState().getHyperplaneIndex();
		VarIntNumber mulCol1 = CanonicalUtil.getInequalityValue(stoich, hyperplaneIndex, numericValsCol2, model.getReactionSorting());
		VarIntNumber mulCol2 = CanonicalUtil.getInequalityValue(stoich, hyperplaneIndex, numericValsCol1, model.getReactionSorting());
		
		final NumberOperations<VarIntNumber> numOps = columnHome.getNumberOperations();

		return ColumnUtil.mergeNumeric(model, numOps, mulCol1, numericValsCol1, mulCol2, numericValsCol2, false /*num2bool*/);
	}
	
}
