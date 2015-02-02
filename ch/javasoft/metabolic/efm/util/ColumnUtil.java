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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.tree.Partition;

/**
 * The <code>ColumnUtil</code> contains static helper methods related to
 * {@link Column columns}.
 */
public class ColumnUtil {
	private static final Logger LOG = LogPkg.LOGGER;

	/**
	 * Returns the number of columns in this iterable. If the iterable is not
	 * a {@link Collection} nor a {@link IterableMemory} instance, an exception
	 * is thrown.
	 * 
	 * @param columns		the column container of which the size is desired
	 * @return the number of columns
	 * @throws IOException	if the iterable type is not supported, or if the
	 * 						possibly file based memory causes an i/o exception
	 */
	public static int getColumnCount(Iterable columns) throws IOException {
		if (columns instanceof Collection) {
			return ((Collection)columns).size();
		}
		if (columns instanceof IterableMemory) {
			return ((IterableMemory)columns).getColumnCount();
		}
		throw new IOException("unknown container type: " + columns.getClass().getName());
	}

	/**
	 * Returns the boolean size of the first column in the list, or 0 if the 
	 * list is empty
	 */
	public static int getBooleanSize(List<? extends Column> cols) {
		return cols.size() == 0 ? 0 : cols.get(0).booleanSize();    	    	
	}
	/**
	 * Returns the boolean size of the first column in the indexable memory, 
	 * or 0 if the memory contains no columns
	 */
	public static int getBooleanSize(IndexableMemory<? extends Column> cols) throws IOException {
		return cols.getColumnCount() == 0 ? 0 : cols.getColumn(0).booleanSize();    	
	}

	/**
	 * Partitions the columns with respect to the criteria reflected by the 
	 * given efm model. If keep is true, the appropriate keep method will be 
	 * invoked for every column before moving it to the new partition.
	 * <p>
	 * Note that the source memory is closed after the operation in any case.
	 * 
	 * @see Column#getHyperplaneSign(EfmModel, IterationStateModel)
	 * 
	 * @param <Col>			column type
	 * @param <N>			number type
	 * @param columnHome	column home defining column and number type 
	 * @param model			the model with access to config stuff
	 * @param src			source memory containing columns to partition
	 * @param pos			destination memory for columns on strictly positive 
	 * 						side of the separating hyperplane
	 * @param zer			destination memory for columns lying within the
	 * 						separating hyperplane			
	 * @param neg			destination memory for columns on strictly negative
	 * 						side of the separating hyperplane
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * @param convert		if true, the value at the current hyperplane 
	 * 						position is converted from numeric to binary (if the
	 * 						underlying model wants to do so).
	 * @throws IOException	if an i/o exception occurs, e.g. for file-based 
	 * 						memory implementations
	 */
	public static <N extends Number, Col extends Column> void partition(ColumnHome<N, Col> columnHome, NetworkEfmModel model, IterableMemory<Col> src, AppendableMemory<Col> pos, AppendableMemory<Col> zer, AppendableMemory<Col> neg, IterationStepModel iteration, boolean convert) throws IOException {

		int removed_cols = 0;
		for (Col col : src) {
			if (convert) {
				col = col.convert(columnHome, model, iteration, false /*clone*/);
			}
			final int sgn = col.getHyperplaneSign(model, iteration.getNextState());


			if (sgn < 0) {
				neg.appendColumn(col);
			}
			else if (sgn > 0) {
				pos.appendColumn(col);
			}
			else {
				zer.appendColumn(col);
			}
		}

		LOG.finest("ColumnUtil.partion(): removed_cols=" + removed_cols);
		// System.out.println("ColumnUtil.partion(): leaving");
		src.close(true /*erase*/);
	}
	/**
	 * Partitions the columns with respect to the criteria reflected by the 
	 * given efm model, if the specified partition is not cut off. If keep is
	 * true, the appropriate keep method will be invoked for every column before 
	 * moving it to the new partition.
	 * <p>
	 * Note that the source memory is closed after the operation in any case.
	 * 
	 * @see NetworkEfmModel#cutOff(NetworkEfmModel.Partition)
	 * @see #partition(ColumnHome, NetworkEfmModel, IterableMemory, AppendableMemory, AppendableMemory, AppendableMemory, IterationStepModel, boolean)
	 * 
	 * @param <Col>			column type
	 * @param <N>			number type
	 * @param columnHome	column home defining column and number type 
	 * @param model			the model with access to config stuff
	 * @param partition		the partition to which the src columns belong to
	 * @param src			source memory containing columns to partition
	 * @param pos			destination memory for columns on strictly positive 
	 * 						side of the separating hyperplane
	 * @param zer			destination memory for columns lying within the
	 * 						separating hyperplane			
	 * @param neg			destination memory for columns on strictly negative
	 * 						side of the separating hyperplane
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * @param convert		if true, the value at the current hyperplane 
	 * 						position is converted from numeric to binary (if the
	 * 						underlying model wants to do so).
	 * @throws IOException	if an i/o exception occurs, e.g. for file-based 
	 * 						memory implementations
	 */
	public static <N extends Number, Col extends Column> void partitionOrClose(ColumnHome<N, Col> columnHome, NetworkEfmModel model, NetworkEfmModel.Partition partition, IterableMemory<Col> src, AppendableMemory<Col> pos, AppendableMemory<Col> zer, AppendableMemory<Col> neg, IterationStepModel iteration, boolean convert) throws IOException {
		// System.out.println("ColumnUtil.partitionOrClose(): entered");
		if (model.cutOff(partition)) {
			// System.out.println("ColumnUtil.partitionOrClose(): close source memory");
			src.close(true /*erase*/);
		}
		else {
			// System.out.println("ColumnUtil.partitionOrClose(): partition");
			partition(columnHome, model, src, pos, zer, neg, iteration, convert);
		}
		// System.out.println("ColumnUtil.partitionOrClose(): leaving");
	}
	/**
	 * Reads all columns from the src memory and moves them to the dst memory,
	 * if the specified partition a not cut off.
	 * If keep is true, {@link Column#convert(ColumnHome, EfmModel, IterationStepModel, boolean) Column.convert(..)}
	 * is called for every column before moving. Note that the source memory
	 * is closed after moving.
	 * 
	 * @param <Col>			column type
	 * @param <N>			number type
	 * @param columnHome	column home defining number and column type
	 * @param model			the model with access to config stuff
	 * @param partition		the partition to which the src columns belong to
	 * @param src			src memory, where the columns are read from
	 * @param dst			dst memory, where the columns are moved to
	 * @param iteration		the current iteration and related information.
	 * 						Used to identify the appropriate hyperplane
	 * @param convert		if true, the value at the current hyperplane 
	 * 						position is converted from numeric to binary (if the
	 * 						underlying model wants to do so).
	 * @throws IOException	if an i/o exception occurs, e.g. for file-based 
	 * 						memories
	 */
	public static <Col extends Column, N extends Number> void moveToOrClose(ColumnHome<N, Col> columnHome, NetworkEfmModel model, NetworkEfmModel.Partition partition, IterableMemory<Col> src, AppendableMemory<Col> dst, IterationStepModel iteration, boolean convert) throws IOException {
		if (!model.cutOff(partition)) {
			for (Col col : src) {
				if (convert) {
					col = col.convert(columnHome, model, iteration, false /*clone*/);
				}
				dst.appendColumn(col);
			}
		}
		src.close(true /*erase*/);
	}

	private static class PartitionImpl implements Partition {
		private int median;
		private IBitSet unionPattern;
		void add(IBitSet pat) {
			if (unionPattern == null) unionPattern = pat.clone();
			else unionPattern.or(pat);
		}
		public IBitSet unionPattern() {
			return unionPattern;
		}
		public int getMedian() {
			return median;
		}
	}

	/**
	 * Partition the columns using the specified bit index. The columns in mem
	 * are resorted to reflect the partitioning, from start (inclusive) to end
	 * index (exclusive). The returned partition contains the median index
	 * (exclusive for first, inclusive for second part) as well as the union
	 * pattern of all columns from start to end.
	 * 
	 * @param <Col>			column type
	 * @param mem			a sortable memory instance containing the columns to
	 * 						partition
	 * @param bitIndex		the bit index to use for partitioning
	 * @param start			the column start index in mem, inclusive
	 * @param end			the column end index in mem, exclusive
	 * @return	the partition with median and union pattern
	 * 
	 * @throws IOException 	if an i/o exception occurs, e.g. for memory stored
	 * 						in files
	 */
	public static <Col extends Column> Partition partitionColumns(SortableMemory<Col> mem, int bitIndex, int start, int end) throws IOException {
		final PartitionImpl partitionImpl = new PartitionImpl();
		int len = end;
		int ind = start;
		while (ind < len) {
			final Column colLeft = mem.getColumn(ind);
			if (!colLeft.bitValues().get(bitIndex)) {
				partitionImpl.add(colLeft.bitValues());
				ind++;
			}
			else {
				final Column colRight = mem.getColumn(len - 1);
				if (colRight.bitValues().get(bitIndex)) {
					partitionImpl.add(colRight.bitValues());
					len--;
				}
				else {
					partitionImpl.add(colRight.bitValues());
					partitionImpl.add(colLeft.bitValues());
					mem.swapColumns(ind, len - 1);
					ind++;
					len--;
				}
			}
		}
		assert(ind == len || ind == len+1);
		partitionImpl.median = ind;
		return partitionImpl;
	}

	/**
	 * Merges the given double values from two columns. Each value of the first
	 * column is multiplied with <tt>mulCol1</tt>, each of the second column
	 * with <tt>mulCol2</tt>. The product resulting from the negative multiplier
	 * is subtracted from the that with the positive multiplier, resulting in 
	 * the values of the new column. 
	 * <p>
	 * Shortly, this means:
	 * <pre>
	 *   newval[i] = mulPos * posval[i] - mulNeg * negval[i]
	 * </pre>
	 * <p>
	 * If <tt>num2bool</tt> is true, the first value is cut off, i.e.
	 * <pre>
	 *   newval[i] = mulPos * posval[i+1] - mulNeg * negval[i+1]
	 * </pre>
	 * 
	 * @param model				the model, e.g. for precision stuff
	 * @param numOps			number operations for calculations
	 * @param mulCol1			the multiplier for the values of column 1
	 * @param numericValsCol1	the numeric values of column 1
	 * @param mulCol2			the multiplier for the values of column 2
	 * @param numericValsCol2	the numeric values of column 2
	 * @param num2bool			if true, the first numeric value is removed, 
	 * 							since it is always 0 and converted to binary
	 * @return the numeric values of the newly born column
	 */
	public static <N extends Number> N[] mergeNumeric(EfmModel model, NumberOperations<N> numOps, N mulCol1, N[] numericValsCol1, N mulCol2, N[] numericValsCol2, boolean num2bool) {
		if (numOps.signum(mulCol1) < 0) {
			if (numOps.signum(mulCol2) > 0) {//perform this check to avoid infinite recursing
				return mergeNumeric(model, numOps, mulCol2, numericValsCol2, mulCol1, numericValsCol1, num2bool);
			}
		}
		else {
			if (numOps.signum(mulCol2) < 0) {
				final int boolInc = num2bool ? 1 : 0;

				//final N sum = numOps.subtract(mulCol1, mulCol2);
				//numeric part
				final N[] values = numOps.newArray(numericValsCol1.length - boolInc);		
				for (int ii = 0; ii < values.length; ii++) {
					final N prodA = numOps.multiply(numericValsCol1[ii + boolInc], mulCol1);
					final N prodB = numOps.multiply(numericValsCol2[ii + boolInc], mulCol2);			
					values[ii] = numOps.subtract(prodA, prodB);

					//don't divide, to also support BigInteger
					//					values[ii] /= sum;

					values[ii] = numOps.reduce(values[ii]);
				}

				//reduce the whole vector
				return numOps.reduceVector(false /*cloneOnChange*/, values);
			}
		}
		throw new RuntimeException("multipliers must have opposite sign: " + mulCol1 + " / " + mulCol2);
	}

	/**
	 * Merges the given double values from two columns. Each value of the first
	 * column is multiplied with <tt>mulCol1</tt>, each of the second column
	 * with <tt>mulCol2</tt>. The product resulting from the negative multiplier
	 * is subtracted from the that with the positive multiplier, resulting in 
	 * the values of the new column. 
	 * <p>
	 * Shortly, this means:
	 * <pre>
	 *   newval[i] = mulPos * posval[i] - mulNeg * negval[i]
	 * </pre>
	 * <p>
	 * If <tt>num2bool</tt> is true, the first value is cut off, i.e.
	 * <pre>
	 *   newval[i] = mulPos * posval[i+1] - mulNeg * negval[i+1]
	 * </pre>
	 * 
	 * @param model				the model, e.g. for precision stuff
	 * @param mulCol1			the multiplier for the values of column 1
	 * @param numericValsCol1	the numeric values of column 1
	 * @param mulCol2			the multiplier for the values of column 2
	 * @param numericValsCol2	the numeric values of column 2
	 * @param num2bool			if true, the first numeric value is removed, 
	 * 							since it is always 0 and converted to binary
	 * @return the numeric values of the newly born column
	 */
	public static double[] mergeNumeric(EfmModel model, double mulCol1, double[] numericValsCol1, double mulCol2, double[] numericValsCol2, boolean num2bool) {
		if (mulCol1 < 0) {
			if (mulCol2 > 0) {//perform this check to avoid infinite recursing
				return mergeNumeric(model, mulCol2, numericValsCol2, mulCol1, numericValsCol1, num2bool);
			}
		}
		else {
			if (mulCol2 < 0) {
				final int boolInc = num2bool ? 1 : 0;

				final double sum = mulCol1 - mulCol2;
				//numeric part
				final double[] values = new double[numericValsCol1.length - boolInc];		
				for (int ii = 0; ii < values.length; ii++) {
					final double prodA = numericValsCol1[ii + boolInc] * mulCol1;
					final double prodB = numericValsCol2[ii + boolInc] * mulCol2;			
					values[ii] = (prodA - prodB) / sum;
				}
				return values;
			}
		}
		throw new RuntimeException("multipliers must have opposite sign: " + mulCol1 + " / " + mulCol2);
	}

	//no instances
	private ColumnUtil() {}
}
