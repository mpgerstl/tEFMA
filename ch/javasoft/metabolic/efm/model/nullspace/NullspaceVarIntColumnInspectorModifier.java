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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.VarIntColumn;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.util.ColumnUtil;
import ch.javasoft.util.Arrays;

/**
 * Column {@link ColumnInspectorModifier inspector/modifier} for nullspace model
 * and {@link VarIntColumn}.
 */
public class NullspaceVarIntColumnInspectorModifier extends AbstractNullspaceColumnInspectorModifier<VarIntNumber, VarIntNumber[]> {

	public NullspaceVarIntColumnInspectorModifier(boolean convertNumericToBinaryOnMerge) {
		super(convertNumericToBinaryOnMerge);
	}
	public int getHyperplaneSign(ColumnHome<VarIntNumber, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, VarIntNumber[] numericVals, IterationStateModel iteration) {
		final int numericInd = getNumericIndex(model, binarySize, numericVals.length, iteration.getHyperplaneIndex());
		return numericVals[numericInd].signum();
	}

	public IBitSet convertBinary(ColumnHome<VarIntNumber, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, VarIntNumber[] numericVals, IterationStepModel iteration, boolean clone) {
		final int convertCount = getConvertSizeBinary(model, binarySize, numericVals.length, iteration);
		if (clone) {
			binaryVals = binaryVals.clone();
		}
		if (convertCount == 0) {
			return binaryVals;
		}
		final int hyperInd = iteration.getCurrentState().getHyperplaneIndex();
		for (int i = 0; i < convertCount; i++) {
			final int numericInd = getNumericIndex(model, binarySize, numericVals.length, hyperInd + i);

			//numeric value might already have been removed, thus, numericInd can be -1 
			final int signum = numericInd == -1 ? 0 : numericVals[numericInd].signum();
			if (signum < 0) {
				throw new IllegalStateException("pivot value is below zero: " + numericVals[numericInd]);
			}
			else if (signum == 0) {
				binaryVals.set(hyperInd + i);
			}
		}
		return binaryVals;
	}

	public VarIntNumber[] convertNumeric(ColumnHome<VarIntNumber, ?> columnHome, EfmModel model, IBitSet binaryVals, int binarySize, VarIntNumber[] numericVals, IterationStepModel iteration, boolean clone) {
		final int convertCount = getConvertSizeNumeric(model, binarySize, numericVals.length, iteration);
		if (convertCount == 0 && !clone) {
			return numericVals;
		}
//		return Arrays.copyOfRange(numericVals, convertCount, numericVals.length);
		final VarIntNumber[] newvals = Arrays.copyOfRange(numericVals, convertCount, numericVals.length);
		return columnHome.getNumberOperations().reduceVector(false /*cloneOnChange*/, newvals);
	}

	public VarIntNumber[] mergeNumeric(ColumnHome<VarIntNumber, ?> columnHome, EfmModel model, IBitSet binaryValsCol1, int binarySizeCol1, VarIntNumber[] numericValsCol1, IBitSet binaryValsCol2, int binarySizeCol2, VarIntNumber[] numericValsCol2, IterationStepModel iteration) {
		final int hyperInd = iteration.getCurrentState().getHyperplaneIndex();
		final int numerInd1 = getNumericIndex(model, binarySizeCol1, numericValsCol1.length, hyperInd);
		final int numerInd2 = getNumericIndex(model, binarySizeCol2, numericValsCol2.length, hyperInd);
		final VarIntNumber mulCol1 = numericValsCol2[numerInd2];
		final VarIntNumber mulCol2 = numericValsCol1[numerInd1];

		final NumberOperations<VarIntNumber> numOps = columnHome.getNumberOperations();

		//check probably to expensive for this data type
		final VarIntNumber check = numOps.subtract(
			numOps.multiply(numericValsCol1[numerInd1], mulCol1),	
			numOps.multiply(numericValsCol2[numerInd2], mulCol2)
		);
		if (check.signum() != 0) {
			throw new IllegalArgumentException(
				"combination of " + numericValsCol1[numerInd1] + 
				" with " + numericValsCol2[numerInd2] + " is not zero: " + check
			);
		}
		
		return ColumnUtil.mergeNumeric(model, numOps, mulCol1, numericValsCol1, mulCol2, numericValsCol2, convertNumericToBinaryOnMerge);
	}

}
