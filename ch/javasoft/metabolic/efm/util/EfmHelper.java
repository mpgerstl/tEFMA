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
package ch.javasoft.metabolic.efm.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.ColumnToFluxDistributionConverter;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.model.nullspace.CannotReconstructFluxException;
import ch.javasoft.metabolic.efm.sort.SortUtil;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.smx.iface.BigIntegerRationalMatrix;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.smx.ops.Mul;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;
import ch.javasoft.util.logging.LogWriter;
import ch.javasoft.util.logging.Loggers;

public class EfmHelper {
	
	private static final Logger LOG = LogPkg.LOGGER;
	
	/**
	 * For every external-to-external (e2e) reactions (a reactions which is 
	 * uptake and excretion reaction at the same time), we add an EFM. This EFM 
	 * has flux value one for the e2e reaction, zero for all other reactions. If 
	 * the 2e2 reaction is reversible, another EFM with -1 at this reaction is
	 * added. 
	 */
	public static <N extends Number, Col extends Column> int appendExternalExternalFluxModes(GenericDynamicArray<FluxDistribution> fluxes, MetabolicNetwork metaNet, ColumnHome<N, Col> columnHome) {
		int count = 0;
		final NumberOperations<N> numberOps = columnHome.getNumberOperations();
		//append flux for external to external reactions
		final ArrayIterable<? extends Reaction> reacts = metaNet.getReactions();
		for (int ii = 0; ii < reacts.length(); ii++) {
			Reaction reac = reacts.get(ii);
			if (reac.isUptake() && reac.isExtract()) {
				count++;
				final N[] rates = numberOps.newArray(reacts.length());
				Arrays.fill(rates, numberOps.zero());
				rates[ii] = numberOps.one();
				fluxes.add(columnHome.createFluxDistribution(metaNet, rates));
				if (reac.getConstraints().isReversible()) {
					count++;
					final N[] ratesRev = rates.clone();
					ratesRev[ii] = numberOps.negate(numberOps.one());
					fluxes.add(columnHome.createFluxDistribution(metaNet, ratesRev));
				}
			}
		}
		return count;
	}
	/**
	 * Tests whether the given column, partly represented only by boolean 
	 * values, is still a valid EFM by converting all values to numeric. If the
	 * boolean part is buggy, this conversion will fail, since no or multiple
	 * solutions exist with the faulty boolean pattern.
	 * <p>
	 * On failure, debug information is logged and an exception is thrown.
	 * 
	 * @throws CannotReconstructFluxException if the test fails
	 */
	public static <N extends Number, Col extends Column> void efmSelfTest(ColumnToFluxDistributionConverter<N, Col> converter, Config config, NetworkEfmModel model, Col newCol, Col oldA, Col oldB) {
		//test the newly generated column
		try {
			converter.createFluxDistributionFromColumn(config, model, newCol);
		}
		catch (CannotReconstructFluxException ex) {
			FluxDistribution fluxA, fluxB;
			try {
				fluxA = converter.createFluxDistributionFromColumn(config, model, oldA);
			}
			catch (CannotReconstructFluxException e) {fluxA = null;}
			try {
				fluxB = converter.createFluxDistributionFromColumn(config, model, oldB);
			}
			catch (CannotReconstructFluxException e) {fluxB = null;}
			LOG.severe("new column is not a valid flux mode (old.A is " + (fluxA != null ? "ok" : "nok") + ", old.B is " + (fluxB != null ? "ok" : "nok") + ")");
			LOG.severe("new   = " + newCol);
			LOG.severe("old.A = " + oldA);
			LOG.severe("old.B = " + oldB);
			LOG.severe("nullspace for new is:");
			ex.getNullspace().writeToMultiline(new LogWriter(LOG, Level.SEVERE));
			int[] rowUnmappings = rowUnmappings(model.getReactionSorting());
			if (fluxA != null) LOG.severe("flux.A = " + Arrays.toString(mapRows(fluxA.getDoubleRates(), rowUnmappings)));
			if (fluxB != null) LOG.severe("flux.B = " + Arrays.toString(mapRows(fluxB.getDoubleRates(), rowUnmappings)));
			throw ex;
		}		
	}
	
    private static DoubleMatrix createKernelDbl(MetabolicNetwork metaNet, DoubleMatrix mx, int[] rowMapping, Config config, boolean log) {
    	BigIntegerRationalMatrix biMx = new DefaultBigIntegerRationalMatrix(
        	mx.toDoubleArray(), mx.getRowCount(), mx.getColumnCount(), true /*adjust double values*/	
        );
    	return createKernel(metaNet, biMx, rowMapping, config, log).toDoubleMatrix(false /*enfordeNewInstance*/);
    }
    //FIXME resolve stoich/kernel type stuff
    @SuppressWarnings("unchecked")
	public static <M extends ReadableMatrix<?>> M createKernel(MetabolicNetwork metaNet, M mx, int[] rowMapping, Config config, boolean log) {
	  	if (mx instanceof BigIntegerRationalMatrix) {
	  		return (M)createKernelBI(metaNet, (BigIntegerRationalMatrix)mx, rowMapping, config, log);
	  	}
	  	else if (mx instanceof ReadableBigIntegerRationalMatrix) {
	  		return (M)createKernelBI(metaNet, (ReadableBigIntegerRationalMatrix)mx, rowMapping, config, log);
	  	}
	  	else if (mx instanceof DoubleMatrix) {
	  		return (M)createKernelDbl(metaNet, (DoubleMatrix)mx, rowMapping, config, log);
//	  		return (M)createKernel(metaNet, (DoubleMatrix)mx, rowMapping, config, log);
	  	}
	  	throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
    }
    //This method is not really stable since nullspace computation uses large pivots, but its better to use 1 pivots since
    //many math examples contain a lot of ones
    @SuppressWarnings("unused")
	private static DoubleMatrix createKernelDblOld(MetabolicNetwork metaNet, DoubleMatrix mx, int[] rowMapping, Config config, boolean log) {
    	final DoubleMatrix biKn = new Gauss(config.zero().mZeroPos).nullspace(mx);    	
    	final DoubleMatrix kn	= formatKernel(metaNet, biKn, rowMapping, config);
    	sortKernel(metaNet, mx, kn, rowMapping, config, log);
    	return kn;
    }
    private static BigIntegerRationalMatrix createKernelBI(MetabolicNetwork metaNet, ReadableBigIntegerRationalMatrix<BigFraction> mx, int[] rowMapping, Config config, boolean log) {
    	final BigIntegerRationalMatrix biKn = Gauss.getRationalInstance().nullspace(mx);
    	final BigIntegerRationalMatrix kn	= formatKernel(metaNet, biKn, rowMapping, config);
    	sortKernel(metaNet, mx, kn, rowMapping, config, log);
    	return kn;
    }
    private static <N extends Number, M extends ReadableDoubleMatrix<N>> void sortKernel(MetabolicNetwork metaNet, ReadableDoubleMatrix<N> stoich, M kernel, int[] rowMapping, Config config, boolean log) {
   //  private static <N extends Number, M extends ReadableDoubleMatrix<N> & WritableMatrix<N>> void sortKernel(MetabolicNetwork metaNet, ReadableDoubleMatrix<N> stoich, M kernel, int[] rowMapping, Config config, boolean log) {
        if (log && LOG.isLoggable(Level.FINE)) {
            LogWriter fineWriter = new LogWriter(LOG, Level.FINE);
            LOG.finer("stoichiometrix matrix");
            new DefaultDoubleMatrix(
                StoichiometricMatrices.createStoichiometricMatrix(metaNet), true
            ).writeToMultiline(fineWriter);
        }
        if (log && LOG.isLoggable(Level.FINER)) {
            LogWriter finerWriter = new LogWriter(LOG, Level.FINER);
            LOG.finer("expanded stoichiometric matrix");
            stoich.writeToMultiline(finerWriter);
            LOG.finer("kernel matrix (unmapped):");
        	kernel.writeToMultiline(finerWriter);
        	LOG.finer("kernel matrix:");
        	ReactionMapping.unsortKernelMatrixRows(kernel, rowMapping).writeToMultiline(finerWriter);
        }
        SortUtil.sortKernel(kernel, rowMapping, metaNet, config);
        if (log && LOG.isLoggable(Level.FINE)) {          
        	LOG.fine("reaction-sorting  (KERNEL): " + Arrays.toString(rowMapping));
        }
        if (log && LOG.isLoggable(Level.FINER)) {          
            LogWriter finerWriter = new LogWriter(LOG, Level.FINER);
            LOG.finer("initial kernel matrix (sorted):");
            kernel.writeToMultiline(finerWriter);
            LOG.finer("mx * kernel:");
            EfmHelper.mulMapped(stoich, kernel, rowMapping).writeToMultiline(finerWriter);
        }
    }
	
    
	//it already might be an echolon form:
	//if kernel was computed with gauss, k=[-M I]' contains an identity 
	//matrix, but the id metas can be anywhere.
	//first, we try to account for this.
    //if this does not work, we use the double matrix method formatKernel
    private static <N extends Number, M extends ReadableDoubleMatrix<N>> M formatKernel(MetabolicNetwork metaNet, M kn, int[] rowMapping, Config config) {
    // private static <N extends Number, M extends ReadableDoubleMatrix<N> & WritableMatrix<N>> M formatKernel(MetabolicNetwork metaNet, M kn, int[] rowMapping, Config config) {
    	final int rows = kn.getRowCount();
    	final int cols = kn.getColumnCount();
    	final NumberOperations<N> nops = kn.getNumberOperations();
    	final N zero = nops.valueOf(config.zero().mZeroPos);
    	final N oneU = nops.add(nops.one(), zero);
    	final N oneL = nops.subtract(nops.one(), zero);
    	BitSet pivots = new BitSet(cols);
    	int rowEnd = rows;
    	int row = 0;
    	while (row < rowEnd) {
    		if (pivots.get(row)) {
    			row++;
    		}
    		else {
        		int cnt0 = 0;
        		int pos1 = -1;
    			for (int col = 0; col < cols; col++) {
    				if (kn.getSignumAt(row, col) == 0) {
    					cnt0++;
    				}
    				else {
    					final N val = kn.getNumberValueAt(row, col);
    					final N abs = nops.abs(val);
    					if (nops.compare(abs, zero) < 0) {
    						cnt0++;
    						kn.setValueAt(row, col, nops.zero());
    					}
    					else if (pos1 == -1 && nops.isOne(val)) { 
    						pos1 = col;
    					}
    					else if (pos1 == -1 && 
    						(nops.compare(val, oneU) < 0 && nops.compare(val, oneL) > 0)
    					) {
    						
    						pos1 = col;
    						kn.setValueAt(row, col, nops.one());
    					}
    					else {
    						break;
    					}
    				}
    			}
    			if (pos1 != -1 && cnt0 == cols - 1) {
    				if (pivots.get(pos1)) {
    					//we have already such a pivot row
    					//(NOTE, we could clear the one, so this should probably
    					//not happen)
    					
    					//move this row to the end
    					rowEnd--;
    					if (row != rowEnd) {
                            kn.swapRows(row, rowEnd);
                            int tmp = rowMapping[row];
                            rowMapping[row]		= rowMapping[rowEnd];
                            rowMapping[rowEnd]  = tmp;    					
    					}
    				}
    				else {
    					//found a pivot row, move it to pivot position
    					pivots.set(pos1);
    					if (row != pos1) {
	                        kn.swapRows(row, pos1);
	                        int tmp = rowMapping[row];
	                        rowMapping[row]		= rowMapping[pos1];
	                        rowMapping[pos1]  	= tmp;
    					}
    					else {
       						row++;    						
    					}
    				}
    			}
    			else {
    				row++;
    			}
    		}
		}
    	if (pivots.length() == cols && pivots.cardinality() == cols) {
    		//we have found all metas of the identity matrix, so we're done
    		return reestablishReactionCategoryOrder(metaNet, kn, rowMapping, config);
    	}
    	else {
    		final String msg = 
    			"identity matrix not found in big integer rational matrix, found " + 
				pivots.cardinality() + " of " + cols + ": " + pivots;
    		LOG.warning(msg);
    		throw new RuntimeException(msg);
    		//return formatKernel(kn.toDoubleMatrix(false), rowMapping, constraints);
    	}
    }
    /**
     * Due to kernel formatting (formatting the row-echelon kernel matrix), the
     * category ordering (put enforced and no-split reactions to end) is 
     * intermixed. Here, we reestablish this order, but still preserving the
     * row-echelon form.
     * 
     * E.g. we have
     * [ I ]
     * [ M ]
     * and some reaction i in the identity part has to be moved to the end due
     * to some category constraint. We exchange row i and the last one. Now, we
     * have
     * [1 0 ... 0 ]
     * [0 1 ... 0 ]
     * [x x     x ] <-- row i
     * [0 ...   1 ]
     * [    M'    ]
     * By adding/subtracting columns, we can reestablish the identity matrix at
     * row i. 
     * Note: if the last row did not have a non-zero entry at column i, we 
     * search for another row in the M part which has an non-zero entry at
     * column i and swap it with the last row.
     */
    private static <N extends Number, M extends ReadableDoubleMatrix<N>> M reestablishReactionCategoryOrder(MetabolicNetwork metaNet, M kn, int[] rowMapping, Config config) {
    // private static <N extends Number, M extends ReadableDoubleMatrix<N> & WritableMatrix<N>> M reestablishReactionCategoryOrder(MetabolicNetwork metaNet, M kn, int[] rowMapping, Config config) {
    	final LogWriter logFinest = new LogWriter(LOG, Level.FINEST);
		if (Loggers.isLoggable(LOG, Level.FINEST)) {
			LOG.finest("formatted kernel before reestablishing reaction category sorting:");
			kn.writeToMultiline(logFinest);
		}
		final NumberOperations<N> nops = kn.getNumberOperations();
    	final ReactionMapping rmap = new ReactionMapping(config, metaNet, rowMapping);
    	final int cols = kn.getColumnCount();
    	final int rows = kn.getRowCount();
    	int rowsActive = rows;
    	for (final ReactionMapping.Category cat : ReactionMapping.Category.values()) {
    		if (cat.isSpecial()) {
            	for (int row = 0; row < cols; row++) {
        			if (cat == rmap.getReactionCategoryBySortedIndex(row)) {
						LOG.finest("row " + row + " is " + cat);
        				//swap this row with last active row
        				rowsActive--;
						LOG.finest("swapping rows " + row + " and " + rowsActive);
        				kn.swapRows(row, rowsActive);
        				ch.javasoft.util.Arrays.swap(rowMapping, row, rowsActive);
        				if (Loggers.isLoggable(LOG, Level.FINEST)) {
        					LOG.finest("formatted after swapping last/" + row);
        					kn.writeToMultiline(logFinest);
        				}
        				//check the replacement row, is it
        				//  a) a row with non-zero pivot
        				//  b) not a special category row  
        				if (kn.getSignumAt(row, row) == 0 || rmap.getReactionCategoryBySortedIndex(row).isSpecial()) {
        					//look for another non-special category row with non-zero pivot
        					int nonZeroRow = -1;
        					for (int i = cols; i < rowsActive; i++) {
    							if (kn.getSignumAt(i, row) != 0) {
    								if (rmap.getReactionCategoryBySortedIndex(i).isSpecial()) {
    									LOG.finest("swap candidate " + i + " is " + rmap.getReactionCategoryBySortedIndex(i));
    								}
    								else {
	    								nonZeroRow = i;
	    								break;
    								}
    							}
    						}
        					if (nonZeroRow < 0) {
//        						LOG.info("cannot process special reaction " + row + " at end, no replacement row found in kernel matrix");
//        						//swap back
//        						LOG.finest("back-swapping rows " + row + " and " + rowsActive);
//                				kn.swapRows(row, rowsActive);
//                				ch.javasoft.util.Arrays.swap(rowMapping, row, rowsActive);
//                				if (Loggers.isLoggable(LOG, Level.FINEST)) {
//                					LOG.finest("formatted after back-swapping last/" + row);
//                					kn.writeToMultiline(logFinest);
//                				}
//                				rowsActive++;
        						kn.writeToMultiline(new LogWriter(LOG, Level.WARNING));    	
        						throw new RuntimeException("no replacement row found with non-zero pivot to reastablish row-echelon form of kernel matrix for row " + row);
        					}
        					else {
								LOG.finest("swapping rows " + row + " and " + nonZeroRow);
	        					kn.swapRows(row, nonZeroRow);
	        					ch.javasoft.util.Arrays.swap(rowMapping, row, nonZeroRow);
	            				if (Loggers.isLoggable(LOG, Level.FINEST)) {
	            					LOG.finest("formatted after swapping rows " + row + " and " + nonZeroRow);
	            					kn.writeToMultiline(logFinest);
	            				}
        					}
        				}
        				//reestablish identity part now
        				if (!nops.isOne(kn.getNumberValueAt(row, row))) {
    						//make pivot a 1
    						final N piv = kn.getNumberValueAt(row, row);
    						for (int r = cols; r < rows; r++) {
    							final N div = nops.divide(kn.getNumberValueAt(r, row), piv);
    							kn.setValueAt(r, row, nops.reduce(div));
    						}
    						kn.setValueAt(row, row, nops.one());
    					}
        				for (int col = 0; col < cols; col++) {
    						if (col != row) {
    							//make entries 0
    							if (kn.getSignumAt(row, col) != 0) {
    								// col = col - colpiv * pivval
    								final N colpiv = kn.getNumberValueAt(row, col);
    								for (int r = cols; r < rows; r++) {
    									final N pivval 	= kn.getNumberValueAt(r, row); 
    									final N val 	= kn.getNumberValueAt(r, col);
    									final N sub		= nops.multiply(pivval, colpiv);
    									final N newval	= nops.subtract(val, sub);
    									kn.setValueAt(r, col, nops.reduce(newval));
    								}
    								kn.setValueAt(row, col, nops.zero());
    							}
    						}
    					}
        			}
        		}
    		}
    	}
		if (Loggers.isLoggable(LOG, Level.FINEST)) {
			LOG.finest("formatted kernel after reestablishing reaction category sorting:");
			kn.writeToMultiline(logFinest);
		}
    	return kn;
    }
    
	public static <N extends Number> ReadableMatrix<N> mulMapped(ReadableMatrix<N> stoich, ReadableMatrix<N> kn, int[] rowMapping) {
		final ReadableMatrix<N> knUnmapped = rowMapping == null ? kn : ReactionMapping.unsortKernelMatrixRows(kn, rowMapping);
		return Mul.multiplyGeneric(stoich, knUnmapped);
	}
	
	private static double[] mapRows(double[] values, int[] rowMapping) {
		double[] res = new double[values.length];
		for (int ii = 0; ii < res.length; ii++) {
			res[rowMapping[ii]] = values[ii];
		}
		return res;
	}
	
	private static int[] rowUnmappings(int[] rowMapping) {
		int[] unmapping = new int[rowMapping.length];
		for (int ii = 0; ii < unmapping.length; ii++) {
			unmapping[rowMapping[ii]] = ii;
		}
		return unmapping;
	}
	
}
