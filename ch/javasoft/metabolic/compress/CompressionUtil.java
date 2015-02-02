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
package ch.javasoft.metabolic.compress;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.AbstractMetabolicNetwork;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.metabolic.util.MetabolicNetworkUtil;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.iface.WritableMatrix;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.logging.LogPrintWriter;
import ch.javasoft.util.logging.Loggers;
import ch.javasoft.util.numeric.Zero;

public class CompressionUtil {
	
	private static final Logger LOG 	 = LogPkg.LOGGER;
	private static final Logger LOG_DATA = Loggers.getLogger(LOG.getName() + ".data", -2);

	/**
	 * Compresses the given network, using zero tolerance and 
	 * {@link CompressionMethod#STANDARD standard compression}. No reactions are
	 * suppressed, duplicate gene compression is not applied. 
	 * 
	 * @param net	the network to compress
	 * @return		the compressed network
	 */
	public static CompressedMetabolicNetwork compress(MetabolicNetwork net) {
		return compress(net, null);
	}
	/**
	 * Compresses the given network, using zero tolerance and 
	 * {@link CompressionMethod#STANDARD standard compression}. The specified 
	 * reactions are suppressed, duplicate gene compression is not applied. 
	 * 
	 * @param net					the network to compress
	 * @param suppressedReactions	the reactions to suppress, i.e. to exclude
	 * 								in the compressed network
	 * @return		the compressed network
	 */
	public static CompressedMetabolicNetwork compress(MetabolicNetwork net, Set<String> suppressedReactions) {
		return compress(net, CompressionMethod.STANDARD, suppressedReactions);
	}
	/**
	 * Compresses the given network, using the default zero tolerance and 
	 * compression methods. Reactions in the suppressed reactions set are 
	 * removed in the compressed network, duplicate gene compression is not 
	 * applied. 
	 * 
	 * @param net					the network to compress
	 * @param methods				the compression methods to apply
	 * @param suppressedReactions	the reactions to suppress, i.e. to exclude
	 * 								in the compressed network
	 * @return the compressed network
	 */
	public static CompressedMetabolicNetwork compress(MetabolicNetwork net, CompressionMethod[] methods, Set<String> suppressedReactions) {
		return compress(net, methods, suppressedReactions, new Zero(0d));
	}
	/**
	 * Compresses the given network, using the given zero tolerance and 
	 * compression methods. Reactions in the suppressed reactions set are 
	 * removed in the compressed network, duplicate gene compression is not 
	 * applied. 
	 * 
	 * @param net					the network to compress
	 * @param methods				the compression methods to apply
	 * @param zero					the zero tolerance to use
	 * @param suppressedReactions	the reactions to suppress, i.e. to exclude
	 * 								in the compressed network
	 * @return the compressed network
	 */
	public static CompressedMetabolicNetwork compress(MetabolicNetwork net, CompressionMethod[] methods, Set<String> suppressedReactions, Zero zero) {
		final CompressedMetabolicNetwork cmpNet;

		final boolean[] reversible 						= net.getReactionReversibilities();
		final ReadableBigIntegerRationalMatrix stoich	= FractionNumberStoichMetabolicNetwork.getStoich(net);
		final String[] metaNames						= net.getMetaboliteNames();
		final String[] reacNames						= net.getReactionNames();
		
		final StoichMatrixCompressor.CompressionRecord rec = new StoichMatrixCompressor(methods).compress(stoich, reversible, metaNames, reacNames, suppressedReactions);
		sortMatrix(rec.cmp, rec.post, rec.reversible);
		logCompressionRecord(stoich, rec, Level.FINER);
		cmpNet = new StoichMatrixCompressedMetabolicNetwork(net, rec.pre, rec.post, rec.cmp);
		//consistency check for reversibilities:
		if (!Arrays.equals(rec.reversible, cmpNet.getReactionReversibilities())) {
			LOG.warning("reversibility missmatch:");
			LOG.warning("  reversibility(cmp.rec) = " + Arrays.toString(rec.reversible));
			LOG.warning("  reversibility(cmp.net) = " + Arrays.toString(cmpNet.getReactionReversibilities()));
			throw new RuntimeException("reversibility missmatch, see log for details");
		}
		if (LOG.isLoggable(Level.FINE)) {
			CompressionMethod.log(Level.FINE, methods);
			LOG.fine(MetabolicNetworkUtil.getNetworkSizeString("Uncompressed network size: ", net));
			LOG.fine(MetabolicNetworkUtil.getNetworkSizeString("Compressed network size: ", cmpNet));
		}
		return cmpNet;
	}
	
	/**
	 * Performs the duplicate gene compression. This compression method is 
	 * usually made in advance, and efms for the duplicate-free network is 
	 * computed. It might be a good idea to call 
	 * {@link #convertToNoncompressed(MetabolicNetwork)} with the 
	 * returned network since uncompressing efms for duplicate gene compressed
	 * networks does not work properly.
	 */
	public static MetabolicNetwork compressDuplicateGeneReactions(MetabolicNetwork net, Zero zero, CompressionMethod... methods) {
		//if (true) return convertToNoncompressed(compress(net, CompressionMethod.methods(CompressionMethod.DuplicateGene), zero, true));
		final boolean[] reversible 						= net.getReactionReversibilities();
		final ReadableBigIntegerRationalMatrix stoich	= FractionNumberStoichMetabolicNetwork.getStoich(net);
		final String[] metaNames						= net.getMetaboliteNames();
		final String[] reacNames						= net.getReactionNames();
		final boolean doExtendedDuplicates				= CompressionMethod.DuplicateGeneExtended.containedIn(methods);
		
		final DuplicateGeneCompressor.CompressionRecord rec = DuplicateGeneCompressor.compress(stoich, reversible, metaNames, reacNames, doExtendedDuplicates);
		sortMatrix(rec.dupfree, rec.dupelim, rec.reversible);
		logCompressionRecord(stoich, rec, Level.FINER);
		final MetabolicNetwork dupfreeNet;
		dupfreeNet = new FractionNumberStoichMetabolicNetwork(rec.metaNames, rec.reacNames, rec.dupfree, rec.reversible);
		if (LOG.isLoggable(Level.FINE)) {
			CompressionMethod.log(Level.FINE, CompressionMethod.DuplicateGene);
			LOG.fine(MetabolicNetworkUtil.getNetworkSizeString("Uncompressed network size: ", net));
			LOG.fine(MetabolicNetworkUtil.getNetworkSizeString("Duplicate free network size: ", dupfreeNet));
		}
		return dupfreeNet;
	}
	
	/**
	 * Nests the given compressed network such that the returned network is no
	 * more recognizable as a compressed network. This can be useful if 
	 * uncompression is performed automatically by recognizing compressed 
	 * networks, but uncompression is not desired.
	 * 
	 * @param cNet	The compressed network to convert to a noncompressed one
	 * @return		A metabolic network nesting the given compressed network
	 */
	public static MetabolicNetwork convertToNoncompressed(final MetabolicNetwork cNet) {
		return new AbstractMetabolicNetwork() {
			public ArrayIterable<? extends Metabolite> getMetabolites() {
				return cNet.getMetabolites();
			}
			public ArrayIterable<? extends Reaction> getReactions() {
				return cNet.getReactions();
			}
			public ReadableMatrix<?> getStoichiometricMatrix() {
				return cNet.getStoichiometricMatrix();
			}
		};
	}
	
	/**
	 * Logs the given compression record to the LOG_DATA logger, using the 
	 * specified log level.
	 * 
	 * @param rec		the compression record to trace
	 * @param level		the log level used for tracing
	 */
	public static void logCompressionRecord(final StoichMatrixCompressor.CompressionRecord rec, Level level) {
		logCompressionRecord(null, rec, level);
	}
	/**
	 * Logs the given compression record and stoichiometric matrix to the 
	 * LOG_DATA logger, using the specified log level.
	 * 
	 * @param stoich	the stoichiometric matrix to trace
	 * @param rec		the compression record to trace
	 * @param level		the log level used for tracing
	 */
	public static void logCompressionRecord(final ReadableMatrix<?> stoich, final StoichMatrixCompressor.CompressionRecord rec, Level level) {
		if (Loggers.isLoggable(LOG_DATA, level)) {
			LogPrintWriter dataLogger = new LogPrintWriter(LOG_DATA, level);
			LOG_DATA.log(level, "compression matrices:");
			LOG_DATA.log(level, "  pre * stoich * post  = cmp");
			LOG_DATA.log(level, "efm uncompression:");
			LOG_DATA.log(level, "  stoich * post * efmc = 0");
			LOG_DATA.log(level, "  stoich * efm         = 0");
			LOG_DATA.log(level, "  -->      efm         = post * efmc");
			if (stoich != null) {
				LOG_DATA.log(level, "stoich: ");
				stoich.writeToMultiline(dataLogger);
			}
			LOG_DATA.log(level, "pre: ");
			rec.pre.writeToMultiline(dataLogger);
			LOG_DATA.log(level, "post: ");
			rec.post.writeToMultiline(dataLogger);
			LOG_DATA.log(level, "cmp: ");
			rec.cmp.writeToMultiline(dataLogger);
			LOG_DATA.log(level, "cmp_reversibilities = " + Arrays.toString(rec.reversible));
		}		
	}
	/**
	 * Logs the given compression record to the LOG_DATA logger, using the 
	 * specified log level.
	 * 
	 * @param rec		the compression record to trace
	 * @param level		the log level used for tracing
	 */
	public static void logCompressionRecord(final DuplicateGeneCompressor.CompressionRecord rec, Level level) {
		logCompressionRecord(null, rec, level);
	}
	/**
	 * Logs the given compression record and stoichiometric matrix to the 
	 * LOG_DATA logger, using the specified log level.
	 * 
	 * @param stoich	the stoichiometric matrix to trace
	 * @param rec		the compression record to trace
	 * @param level		the log level used for tracing
	 */
	public static void logCompressionRecord(final ReadableMatrix<?> stoich, final DuplicateGeneCompressor.CompressionRecord rec, Level level) {
		if (Loggers.isLoggable(LOG_DATA, level)) {
			LogPrintWriter dataLogger = new LogPrintWriter(LOG_DATA, level);
			LOG_DATA.log(level, "compression matrices:");
			LOG_DATA.log(level, "  stoich * dupelim = dupfree");
			if (stoich != null) {
				LOG_DATA.log(level, "stoich: ");
				stoich.writeToMultiline(dataLogger);
			}
			LOG_DATA.log(level, "dupelim: ");
			rec.dupelim.writeToMultiline(dataLogger);
			LOG_DATA.log(level, "dupfree: ");
			rec.dupfree.writeToMultiline(dataLogger);
			LOG_DATA.log(level, "dupfree_reversibilities = " + Arrays.toString(rec.reversible));
			LOG_DATA.log(level, "dupgroups: " + Arrays.toString(rec.dupgroups));
		}		
	}
	
	private static <N extends Number, M extends ReadableMatrix<N> > void sortMatrix(M stoich, M post, boolean[] reversibilities) {
		//sortMatrixNone(stoich, post, reversibilities);

		//sometimes not too bad
		sortMatrixDiagStoich(stoich, post, reversibilities);
		//sortMatrixFewNonZeroColsStoich(stoich, post, reversibilities);

		//not so good
		//sortMatrixDiagPost(stoich, post, reversibilities);
		//sortMatrixFewNonZeroColsPost(stoich, post, reversibilities);
	}

	////////////// helpers
	@SuppressWarnings("unused")
	private static <N extends Number, M extends ReadableMatrix<N> > void sortMatrixNone(M stoich, M post, boolean[] reversibilities) {
		//dont sort
	}
	/**
	 * Sorts the matrices by swapping columns. Columns with fewer non-zero 
	 * entries are placed at lower indices. If 2 colums have the same number of 
	 * non-zero entries, the column with the lower row index for the first 
	 * non-zero entry is placed at a lower index.
	 */
	@SuppressWarnings("unused")
	private static <N extends Number, M extends ReadableMatrix<N> > void sortMatrixFewNonZeroColsPost(M stoich, M post, boolean[] reversibilities) {
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		for (int piv = 0; piv < cols; piv++) {
			//find the column with an entry in the lowest row in post
			int pivcol = piv;
			int pivcnt = Integer.MAX_VALUE;
			int pivrow = Integer.MAX_VALUE;
			for (int col = piv; col < cols; col++) {
				int rowFirst = Integer.MAX_VALUE;
				int cnt = 0;
				for (int row = 0; row < rows; row++) {
					if (post.getSignumAt(row, col) != 0) {
						rowFirst = Math.min(row, rowFirst);
						cnt++;
					}
				}
				if (cnt < pivcnt || (cnt == pivcnt && rowFirst < pivrow)) {
					pivcol = col;
					pivcnt = cnt;
					pivrow = rowFirst;
				}
			}
			
			if (pivcol != piv) {
				stoich.swapColumns(pivcol, piv);
				post.swapColumns(pivcol, piv);
				ch.javasoft.util.Arrays.swap(reversibilities, pivcol, piv);
			}
		}		
	}
	/**
	 * Sorts the matrices by swapping columns. Columns with fewer non-zero 
	 * entries are placed at lower indices. If 2 colums have the same number of 
	 * non-zero entries, the column with the lower row index for the first 
	 * non-zero entry is placed at a lower index.
	 */
	@SuppressWarnings("unused")
	private static <N extends Number, M extends ReadableMatrix<N> > void sortMatrixFewNonZeroColsStoich(M stoich, M post, boolean[] reversibilities) {
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		for (int piv = 0; piv < cols; piv++) {
			//find the column with an entry in the lowest row in stoich
			int pivcol = piv;
			int pivcnt = Integer.MAX_VALUE;
			int pivrow = Integer.MAX_VALUE;
			for (int col = piv; col < cols; col++) {
				int rowFirst = Integer.MAX_VALUE;
				int cnt = 0;
				for (int row = 0; row < rows; row++) {
					if (stoich.getSignumAt(row, col) != 0) {
						rowFirst = Math.min(row, rowFirst);
						cnt++;
					}
				}
				if (cnt < pivcnt || (cnt == pivcnt && rowFirst < pivrow)) {
					pivcol = col;
					pivcnt = cnt;
					pivrow = rowFirst;
				}
			}
			
			if (pivcol != piv) {
				stoich.swapColumns(pivcol, piv);
				post.swapColumns(pivcol, piv);
				ch.javasoft.util.Arrays.swap(reversibilities, pivcol, piv);
			}
		}		
	}
	/**
	 * Sorts the matrices by swapping columns. The matrix post was initially an
	 * identity matrix. Thus, entries should lie on the diagonal. Here, we try
	 * to resort the matrix in such a way that earlier columns have an earlier
	 * occurrance of non-zero elements in post.
	 */
	@SuppressWarnings("unused")
	private static <N extends Number, M extends ReadableMatrix<N> > void sortMatrixDiagPost(M stoich, M post, boolean[] reversibilities) {
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		for (int piv = 0; piv < cols; piv++) {
			//find the column with an entry in the lowest row in post
			int pivcol = piv;
			int pivrow = Integer.MAX_VALUE;
			for (int col = piv; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					if (post.getSignumAt(row, col) != 0) {
						if (row < pivrow) {
							pivrow = row;
							pivcol = col;
						}
						break;
					}
				}
				if (pivrow == 0) break;
			}
			
			if (pivcol != piv) {
				stoich.swapColumns(pivcol, piv);
				post.swapColumns(pivcol, piv);
				ch.javasoft.util.Arrays.swap(reversibilities, pivcol, piv);
			}
		}
	}
	/**
	 * Sorts the matrices by swapping columns. The heuristics here are that the
	 * stoichiometrix matrix should be approximately diagonal, i.e. entries 
	 * should lie on the diagonal. We try to sort the matrix in such a way that 
	 * earlier columns have an earlier occurrance of non-zero elements in stoich.
	 */
	private static <N extends Number, M extends ReadableMatrix<N> > void sortMatrixDiagStoich(M stoich, M post, boolean[] reversibilities) {
		final int rows = stoich.getRowCount();
		final int cols = stoich.getColumnCount();
		for (int piv = 0; piv < cols; piv++) {
			//find the column with an entry in the lowest row in stoich
			int pivcol = piv;
			int pivrow = Integer.MAX_VALUE;
			for (int col = piv; col < cols; col++) {
				for (int row = 0; row < rows; row++) {
					if (stoich.getSignumAt(row, col) != 0) {
						if (row < pivrow) {
							pivrow = row;
							pivcol = col;
						}
						break;
					}
				}
				if (pivrow == 0) break;
			}
			
			if (pivcol != piv) {
				stoich.swapColumns(pivcol, piv);
				post.swapColumns(pivcol, piv);
				ch.javasoft.util.Arrays.swap(reversibilities, pivcol, piv);
			}
		}
	}
	
	//no instances
	private CompressionUtil() {
		super();
	}
}
