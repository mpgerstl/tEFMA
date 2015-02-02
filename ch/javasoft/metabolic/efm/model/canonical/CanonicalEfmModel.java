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

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.model.AbstractNetworkEfmModel;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifierFactory;
import ch.javasoft.metabolic.efm.model.ColumnToFluxDistributionConverter;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.sort.SortUtil;
import ch.javasoft.metabolic.efm.util.ReactionMapping.Category;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.impl.DefaultIntMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.smx.ops.Mul;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.logging.LogWriter;

/**
 * Model for canonical implementations
 */
public class CanonicalEfmModel extends AbstractNetworkEfmModel {
	
	/** 
	 * The inverse matrix of a submatrix of the system matrix A.
	 * The system matrix A is [I ; N ; -N] if we have no nosplit reactions, 
	 * otherwise, the identity part is incomplete. Consequently, the inverse
	 * matrix is also an identity matrix for the former, but not for the latter
	 * case.
	 */
	//quadratic matrix of the form [I 0 ; inv(D)*-C  inv(D)]
	//the number of zero columns in the upper part is equal to nosplit 
	private final ReadableMatrix inverseMatrix;
	
	private boolean isNoSplit(MetabolicNetwork net, int col) {
		return getReactionMapping().getReactionCategoryBySortedIndex(col) == Category.NoSplit;
	}
	public <N extends Number> CanonicalEfmModel(ColumnHome<N, ?> columnHome, MetabolicNetwork net, Config config, ColumnInspectorModifierFactory factory) {
		super(columnHome, net, config, factory);
		final ReadableMatrix<BigFraction> stoich = getStoichRational();
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		final int nosplit = getConfig().getReactionsNoSplit().size();
		
		if (nosplit == 0) {
			//we have A = [ I ; N ; -N ] with r rows and c cols 
			//we want an initial matrix R, such that A(cols,:)R >= 0
			//we chose R = inv(I) = I --> A(cols, :) * R = I*I = I >= 0
			//obviously, R spans the full (nonnegative) space, and R contains 
			//extreme rays since c-1 inequalities are fulfilled with 0
			inverseMatrix = DefaultIntMatrix.identity(cols);
		}
		else {
			//first, put all no-split reactions to the end
			final int[] rmap = getReactionSorting();
			int len = cols;
			int ind = 0;
			while (ind < len) {
				if (rmap[ind] != ind || rmap[len-1] != len-1) {
					throw new RuntimeException("expected identity reaction sorting with map[i] = i, but found " + Arrays.toString(rmap));
				}
				while (!isNoSplit(net, ind)) ind++;
				while (isNoSplit(net, len-1)) len--;
				if (ind < len) {
					IntArray.swap(rmap, ind, len-1);
					ind++;
					len--;
				}
			}
//PRINT			
//			System.out.println(Arrays.toString(rmap));
//PRINT			
			
			//now, we have A = [ I 0 ; N ; -N ] 
			//find a maximal full-rank submatrix of A
			//it is sufficient to look at A1 = [ I 0 ; N ] = [ I 0 ; C D ; R ]
			//we must choose rows from N to [ C D ] such that A2 = [ I 0 ; C D ] is
			//square and invertible, the inverse is then
			//[ I 0 ; -inv(D) * C   inv(D) ]
			//so the only thing to find is rows such that D is invertible
		
			//construct mxD
			final int idLen = cols - nosplit;
			final BigIntegerRationalMatrix mxD = new DefaultBigIntegerRationalMatrix(rows, nosplit);
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < nosplit; col++) {
                                        // cj: b
					// mxD.setValueAt(row, col, stoich.getNumberValueAt(row, rmap[col + idLen]));
                                        System.out.println("WARNING: AAA001: change done by Christian which might cause an error!");
					mxD.setValueAt_BigFraction(row, col, stoich.getNumberValueAt(row, rmap[col + idLen]));
                                        // cj: e
				}
			}
			mxD.reduce();
//PRINT			
//			stoich.writeToMultiline(System.out);
//			mxD.writeToMultiline(System.out);
//PRINT			
			//invert mxD
			final int[][] ptrRowmap = new int[1][];
			final int[][] ptrColmap = new int[1][];
			final BigIntegerRationalMatrix mxInvD = Gauss.getRationalInstance().invertMaximalSubmatrix(mxD, ptrRowmap, ptrColmap);
			if (mxInvD.getRowCount() < nosplit) {
				throw new RuntimeException("matrix has not full rank, expected " + nosplit + " but found " + mxInvD.getRowCount());
			}
//PRINT			
//			System.out.println(Arrays.toString(ptrRowmap[0]));
//			System.out.println(Arrays.toString(ptrColmap[0]));
//			mxInvD.writeToMultiline(System.out);
//PRINT			
			
			//store reactionSorting
			final int[] rmapOrig = ch.javasoft.util.Arrays.copyOfRange(rmap, idLen, rmap.length);
			for (int i = idLen; i < cols; i++) {
				rmap[i] = rmapOrig[ptrColmap[0][i - idLen]];
			}
//PRINT			
//			System.out.println(Arrays.toString(rmap));
//PRINT			

			//construct mxNegC and metaboliteSorting
			final BigIntegerRationalMatrix mxNegC = new DefaultBigIntegerRationalMatrix(nosplit, idLen);
			final BitSet usedRows = new BitSet(rows);
			for (int irow = 0; irow < rows; irow++) {
				if (getMetaboliteSorting()[irow] != irow) {
					throw new RuntimeException("expected identity metabolite sorting with map[i] = i, but found " + Arrays.toString(getMetaboliteSorting()));
				}
				final int row;
				if (irow < nosplit) {
					row = ptrRowmap[0][irow];
					for (int icol = 0; icol < idLen; icol++) {
						final int col = rmap[icol];
                                                // cj: b
						// mxNegC.setValueAt(irow, icol, stoich.getNumberValueAt(row, col).negate());
						mxNegC.setValueAt_BigFraction(irow, icol, stoich.getNumberValueAt(row, col).negate());
                                                // cj: e
					}
				}
				else {
					row = usedRows.nextClearBit(0);
				}
				usedRows.set(row);
				getMetaboliteSorting()[irow] = row;
			}
			mxNegC.reduce();
//PRINT			
//			mxNegC.writeToMultiline(System.out);
//PRINT			
			//construct mxInvDxNegC = inv(D)*-C
			final BigIntegerRationalMatrix mxInvDxNegC = (BigIntegerRationalMatrix)Mul.multiplyGeneric(mxInvD, mxNegC);
//PRINT			
//			mxInvDxNegC.writeToMultiline(System.out);
//PRINT			
			//construct mxInvA2 = inv(A2) = [I 0 ; inv(D)*-C  inv(D)]
			final BigIntegerRationalMatrix mxInvA2 = new DefaultBigIntegerRationalMatrix(cols, cols);
			for (int i = 0; i < idLen; i++) {
				mxInvA2.setValueAt(i, i, 1);
			}
			for (int row = idLen; row < cols; row++) {
				for (int col = 0; col < idLen; col++) {
                                        // cj: b
					// mxInvA2.setValueAt(row, col, mxInvDxNegC.getBigFractionValueAt(row - idLen, col));
					mxInvA2.setValueAt_BigFraction(row, col, mxInvDxNegC.getBigFractionValueAt(row - idLen, col));
                                        // cj: e
				}
				for (int col = idLen; col < cols; col++) {
                                        // cj: b
					// mxInvA2.setValueAt(row, col, mxInvD.getBigFractionValueAt(row - idLen, col - idLen));
					mxInvA2.setValueAt_BigFraction(row, col, mxInvD.getBigFractionValueAt(row - idLen, col - idLen));
                                        // cj: e
				}
			}
//PRINT			
//			mxInvA2.writeToMultiline(System.out);
//PRINT			
			inverseMatrix = stoich instanceof BigIntegerRationalMatrix ? mxInvA2 : mxInvA2.toDoubleMatrix(false /*enforce new instance*/);
			getReactionMapping().refreshSortMapping();
		}
		LogPkg.LOGGER.info("initial inverse matrix has dimensions " + inverseMatrix.getRowCount() + "x" + inverseMatrix.getColumnCount());
		if (LogPkg.LOGGER.isLoggable(Level.FINER)) {
			final LogWriter lw = new LogWriter(LogPkg.LOGGER, Level.FINER);
			LogPkg.LOGGER.info("initial inverse matrix:");
			inverseMatrix.writeToMultiline(lw);
			lw.close();
		}
//PRINT			
//		System.out.println(Arrays.toString(getMetaboliteSorting()));
//PRINT			
//		//FIXME cloning is very inefficient here for large matrices
		SortUtil.sortStoich(new DefaultBigIntegerRationalMatrix(stoich), nosplit, getMetaboliteSorting(), net, config);
//PRINT			
//		System.out.println(Arrays.toString(getMetaboliteSorting()));
//PRINT			
		getReactionMapping().refreshSortMapping();
	}
	
	/**
	 * Returns a new {@link CanonicalColumnToFluxDistributionConverter} instance
	 */
	public <N extends Number, Col extends Column> ColumnToFluxDistributionConverter<N, Col> getColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome) {
		return new CanonicalColumnToFluxDistributionConverter<N, Col>(columnHome);
	}
	
	public <N extends Number, Col extends Column> AppendableMemory<Col> createInitialMemory(ColumnHome<N, Col> columnHome, MemoryFactory memoryFactory) throws IOException {
		final ReadableMatrix<N> stoich = getStoichiometricMatrix(columnHome);
		final ReadableMatrix<N> invert = columnHome.castMatrix(inverseMatrix);
		final NumberOperations<N> nops = stoich.getNumberOperations();
//		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		final int nosplit = getConfig().getReactionsNoSplit().size();
		final int idLen = cols - nosplit;
		
		final Col[] columns = columnHome.newInstances(columnHome.castMatrix(inverseMatrix), getBooleanSize(0));
		//make first entries binary
		for (int col = 0; col < cols; col++) {
			//invert matrix has format [I 0 ; inv(D)*-C  inv(D)]
			//to check consistency, we compute stoich*invert, 
			//should result in an identity matrix
			
			int cntNonZeros = 0;
			//the upper 'identity' rows
			for (int row = 0; row < idLen; row++) {
				final boolean isZero = row != col;
				columns[col].bitValues().set(row, isZero);
				if (!isZero) cntNonZeros++;
			}
			//the lower 'stoich' rows, we perform a consistency check here
			for (int row = idLen; row < cols; row++) {
				final int stoichRow = getMetaboliteSorting()[row - idLen];
				N sum = nops.zero();
				for (int k = 0; k < cols; k++) {
					final int stoichCol = getReactionSorting()[k];
					final N valA = stoich.getNumberValueAt(stoichRow, stoichCol);
					final N valB = invert.getNumberValueAt(k, col);
					sum = nops.add(sum, nops.multiply(valA, valB));
					sum = nops.reduce(sum);
				}
				final boolean isZero = zero().isZero(sum.doubleValue());
				columns[col].bitValues().set(row, isZero);
				if (!isZero) cntNonZeros++;
			}
			//consistency check, we expect a single non-zero entry
			if (cntNonZeros != 1) {
				throw new RuntimeException("column should have a single non-zero entry in the boolean part: " + columns[col]);
			}
		}
		final AppendableMemory<Col> memory = memoryFactory.createReadWriteMemory(columnHome, this, 1, null);
		memory.appendColumns(Arrays.asList(columns));
		return memory;
	}
	
	/**
	 * Returns true for the positive and negative partition.
	 * 
	 * @see NetworkEfmModel#cutOff(Partition)
	 */
	public boolean cutOff(Partition partition) {
		return !Partition.Zero.equals(partition);
	}
	
	public int getHyperplaneIndex(int iteration) {
		final int[] sort = getMetaboliteSorting();
		return iteration == 0 || iteration > sort.length ? -1 : sort[iteration - 1];//iteration is 1-based
	}
	
	/**
	 * Returns the number of iterations, which is exactly the same as the number
	 * of metabolites. Note that this is also true if we have reactions which
	 * are kept out of the iteration loop. For those reactions, the identity
	 * part in the system matrix is missing (mx = [I 0; N]), and we used as
	 * many rows from the stoichiometric matrix as no-split reactions. But since
	 * the initial matrix only accounts for mx*col &ge; 0, we must reprocess
	 * these rows to ensure equality instead of inequality.
	 */
	public int getIterationCount() {
		return getStoichRational().getRowCount();
	}

	/**
	 * We keep all values in numeric, and all inequality values in boolean, too.
	 * Note that all equality constraints will always be fullfilled with 
	 * equality by all columns, thus we do not have to add those to the binary
	 * part, since it would only contain true values. Thus, the binary part only
	 * affects the reaction fluxes, typically all reactions, but without those
	 * that are not treated within the iteration loop.
	 */
	public int getBooleanSize(int iteration) {
		//NOTE:	all numeric values are kept, thus we just return the original values
		//NOTE: we do not need to enlarge the boolean part since it would anyway
		//		contain nothing but true values, since all equalities must be
		//		fullfilled with equality
		return getFinalBooleanSize();
	}
	
	/**
	 * We keep all values in numeric, but already process inequalities are also
	 * kept in boolean form. Thus, this method returns the same as 
	 * {@link #getFinalNumericSize()}.
	 */
	public int getNumericSize(int iteration) {
		//NOTE:	all numeric values are kept, thus we just return the original values
		//NOTE: we do not need to enlarge the boolean part since it would anyway
		//		contain nothing but true values, since all equalities must be
		//		fullfilled with equality
		return getFinalNumericSize();
	}

	public int getFinalBooleanSize() {
		return getStoichRational().getColumnCount();
	}
	/**
	 * We keep all values in numeric, but already process inequalities are also
	 * kept in boolean form. Thus, this method returns the always the number of
	 * reactions, or more precise, the number of columns in the expanded 
	 * stoichiometric matrix. 
	 */
	public int getFinalNumericSize() {
		return getStoichRational().getColumnCount();
	}
	
//	private static DoubleMatrix toDoubleMatrix(ReadableMatrix mx, boolean clone) {
//	  	if (mx instanceof ReadableDoubleMatrix) {
//			final ReadableDoubleMatrix dm = (ReadableDoubleMatrix)mx;
//			return dm.toDoubleMatrix(clone);
//	  	}
//	  	throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
//	}
	
}
