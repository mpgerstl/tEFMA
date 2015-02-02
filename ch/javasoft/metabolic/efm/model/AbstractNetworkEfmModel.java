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
package ch.javasoft.metabolic.efm.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.filter.ColumnFilter;
import ch.javasoft.metabolic.efm.column.filter.CompoundColumnFilter;
import ch.javasoft.metabolic.efm.column.filter.EnforcedFluxColumnFilter;
import ch.javasoft.metabolic.efm.column.filter.FutileCycleColumnFilter;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.util.MatrixUtil;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.ops.Hsl;
import ch.javasoft.smx.ops.HslGateway;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.logging.LogPrintWriter;

/**
 * Abstract default implementation of the {@link NetworkEfmModel}, usually 
 * suitable for most subclass implementations.
 */
abstract public class AbstractNetworkEfmModel extends DefaultEfmModel implements NetworkEfmModel {	
	/** possibly compressed*/
	private final MetabolicNetwork 	metabolicNetwork;
	/** stoichiometric matrix*/
	private final ReactionMapping	reactionMapping;
	private final ColumnFilter		columnFilter;
	
	private final int 				outOfLoopReactionCnt;
	
	public <N extends Number, Col extends Column> AbstractNetworkEfmModel(ColumnHome<N, Col> columnHome, MetabolicNetwork net, Config config, ColumnInspectorModifierFactory columnInspectorModifierFactory) {
		super(init(columnHome, net, config, columnInspectorModifierFactory));
		metabolicNetwork 		= net;		
		columnFilter			= new CompoundColumnFilter(new FutileCycleColumnFilter(this), new EnforcedFluxColumnFilter());
		reactionMapping 		= new ReactionMapping(config, metabolicNetwork, getReactionSorting());		
		outOfLoopReactionCnt	= reactionMapping.getExpandedReactionCountOutOfIterationLoop();
	}

	private static <N extends Number, Col extends Column> Init init(ColumnHome<N, Col> columnHome, MetabolicNetwork net, Config config, ColumnInspectorModifierFactory columnInspectorModifierFactory) {
		final Init init = new Init();
		
		final ReadableMatrix<BigFraction> stoichRational = MatrixUtil.convertToBigIntegerRationalMatrix(net.getStoichiometricMatrix(), config.zero(), false /*enforceNewInstance*/);
//		final ReadableMatrix<N> stoich 	= columnHome.convertMatrix(net.getStoichiometricMatrix(), true /*allowRowScaling*/, false /*allowColumnScaling*/);

//		Hsl.Result_mc66 sort66 = sortStoichMc66(net.getStoichiometricMatrix());
//		final ReadableMatrix<N> stoich 	= sortStoich(columnHome.castMatrix(net.getStoichiometricMatrix()), sort66);

		final boolean[] split	= getReacsToSplit(net, config);
//		final N[][] expStoich	= StoichiometricMatrices.createStoichiometricMatrixExpandReversible(
//				stoich, split); 
		final BigFraction[][] expStoich	= StoichiometricMatrices.createStoichiometricMatrixExpandReversible(
				stoichRational, split); 
		
		init.config							= config;
		init.stoichiometricMatrixRational	= stoichRational.newInstance(expStoich, true /*rowsInDim1*/).toReadableMatrix(false /*new instance*/);
//		init.stoichiometricMatrix			= stoich.newInstance(expStoich, true /*rowsInDim1*/).toReadableMatrix(false /*new instance*/);
		
		init.metaboliteSorting				= createInitialMapping(expStoich.length);
		init.reactionSorting				= createInitialMapping(expStoich.length == 0 ? 0 : expStoich[0].length);
//		init.reactionSorting				= createInitialMapping(split, sort66);
		
		// remove ugly cast and case statement
//		if (stoichiometricMatrix instanceof DoubleMatrix) {
//			sortStoich(config, (DoubleMatrix)stoichiometricMatrix);
//		}
//		else if (stoichiometricMatrix instanceof BigIntegerRationalMatrix) {
//			sortStoich(config, (BigIntegerRationalMatrix)stoichiometricMatrix);
//		}
//		else {
//			throw new RuntimeException("unsupported matrix type: " + stoichiometricMatrix.getClass().getName());
//		}

		init.stoichRank 					= columnHome.rank(init.stoichiometricMatrixRational, config.zero());
		init.columnInspectorModifierFactory	= columnInspectorModifierFactory;

		return init;
	}
	
	@SuppressWarnings("unused")
	private static Hsl.Result_mc66 sortStoichMc66(ReadableMatrix<?> stoich) {
		final IntArray irn = new IntArray();
		final IntArray jcn = new IntArray();
		for (int i = 0; i < stoich.getRowCount(); i++) {
			for (int j = 0; j < stoich.getColumnCount(); j++) {
				if (0 != stoich.getSignumAt(i, j)) {
					irn.add(i+1);
					jcn.add(j+1);
				}
			}
		}
		return HslGateway.callMc66(
				stoich.getRowCount(), stoich.getColumnCount(), 
				irn.toArray(), jcn.toArray(), 5);
	}
	@SuppressWarnings("unused")
	private static int[] createInitialMapping(boolean[] split, Hsl.Result_mc66 sort66) {
		final IntArray sort = new IntArray();
		int add = 0;
		for (int i = 0; i < split.length; i++) {
			sort.add(sort66.column_order[i] - 1 + add);
			if (split[i]) {
				add++;
				sort.add(sort66.column_order[i] - 1 + add);
			}			
		}
		return sort.toArray(); 
	}
	
	@SuppressWarnings("unused")
	private static <N extends Number> ReadableMatrix<N> sortStoich(ReadableMatrix<N> stoich, Hsl.Result_mc66 sort66) {
		WritableMatrix<N> mx = stoich.newInstance(stoich.getRowCount(), stoich.getColumnCount());
		for (int i = 0; i < sort66.row_order.length; i++) {
			for (int j = 0; j < sort66.column_order.length; j++) {
				mx.setValueAt(i, j, stoich.getNumberValueAt(sort66.row_order[i] - 1, j));
			}
		}
		
		return mx.toReadableMatrix(false /*forceNewInstance*/);
	}

	private static boolean[] getReacsToSplit(MetabolicNetwork net, Config config) {
		final boolean[] split = new boolean[net.getReactions().length()];
		int index = 0;
		for (final Reaction reac : net.getReactions()) {			
			split[index] = ReactionMapping.isSplitReaction(config, net, reac);
			index++;
		}
		return split;
	}
	
	public MetabolicNetwork getMetabolicNetwork() {
		return metabolicNetwork;
	}
	public ColumnFilter getColumnFilter() {
		return columnFilter;
	}
	public ReactionMapping getReactionMapping() {
		return reactionMapping;
	}

	public int getOutOfIterationLoopCount() {
		return outOfLoopReactionCnt;
	}

//	private <N extends Number, M extends ReadableDoubleMatrix<N> & WritableMatrix<N>> void sortStoich(Config config, M matrix) {
//		final int rows = matrix.getRowCount();
//		final int cols = matrix.getColumnCount();
////        MatrixSorter rowSorter = new CascadingSorter(
////               	new MostZerosSorter(true, 0, rows, 0, cols, new Zero()),
////               	new AbsLexMinSorter(true, 0, rows, 0, cols)        		
////            );
////      SortUtil.sortMatrixRows(matrix, createInitialMapping(rows), rowSorter, false);
//        MatrixSorter colSorter = new CascadingSorter(
//        	new SuppressedEnforcedNoSplitSorter(metabolicNetwork, config, reactionSorting),
//           	new MostZerosSorter(false, 0, rows, 0, cols, new Zero())
////           	new AbsLexMinSorter(false, 0, rows, 0, cols)        		
//        );
//        LogPkg.LOGGER.fine("reaction-sorting (INITIAL): " + Arrays.toString(reactionSorting));
//        SortUtil.sortMatrixColumns(matrix, reactionSorting, colSorter, false);
//        LogPkg.LOGGER.fine("reaction-sorting  (STOICH): " + Arrays.toString(reactionSorting));
//	}
	
	private static int[] createInitialMapping(int count) {
		final int[] mapping = new int[count];
		for (int i = 0; i < count; i++) {
			mapping[i] = i;
		}
		return mapping;
	}
	
	@Override
	public <N extends Number> void log(ColumnHome<N, ?> columnHome, Logger logger) {
		super.log(columnHome, logger);
		final ReadableMatrix<BigFraction> stoich = getStoichRational();
		logger.info(
			"stoich expanded has dimensions " + 
			stoich.getRowCount() + "x" + stoich.getColumnCount()
		);
		if (logger.isLoggable(Level.FINER)) {
			final LogPrintWriter finerWriter = new LogPrintWriter(logger, Level.FINER);
			finerWriter.println("stoichiometric matrix");
			metabolicNetwork.getStoichiometricMatrix().writeToMultiline(finerWriter);
			finerWriter.println("expanded stoichiometric matrix");
			stoich.writeToMultiline(finerWriter);				
		}
	}
}