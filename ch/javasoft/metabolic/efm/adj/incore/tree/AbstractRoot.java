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
package ch.javasoft.metabolic.efm.adj.incore.tree;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.TreeMemAdjEnum;
import ch.javasoft.metabolic.efm.util.ColumnUtil;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.numeric.Zero;

abstract public class AbstractRoot<T /*traversing token*/> implements Root<T> {

	protected final Config			mConfig;
	protected final EfmModel 		mModel;
	protected final TreeFactory<T>	mTreeFactory;
	protected final int[]			mSelectiveBits;
	protected final Node<T>			mPos;
	protected final Node<T>			mNeg;
	
	@SuppressWarnings("unchecked")
	public AbstractRoot(Config config, EfmModel model, TreeFactory<T> treeFactory, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols) {
		mConfig 		= config;
		mModel			= model;
		mTreeFactory	= treeFactory;

		try {
			//no sorting, seems as fast since trees use path-shortening
//			mSelectiveBits = MappingUtil.getInitialMapping(ColumnUtil.getBooleanSize(posCols));
			//seems fastest
//			mSelectiveBits = calculateBitOrder(posCols.getColumnCount() > negCols.getColumnCount() ? posCols : negCols);
//			mSelectiveBits = calculateBitOrder(posCols, negCols, zeroCols);
			mSelectiveBits = calculateXorBitOrder(posCols, negCols);
			mPos = treeFactory.createNode(posCols, mSelectiveBits, -1 /*prevSelBitIndex*/, 0, posCols.getColumnCount());
			mNeg = treeFactory.createNode(negCols, mSelectiveBits, -1 /*prevSelBitIndex*/, 0, negCols.getColumnCount());
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public Zero zero() {
		return mConfig.zero();
	}
	
	public Node<T> pos() {
		return mPos;
	}
	public Node<T> neg() {
		return mNeg;
	}
	public int booleanSize() {
		return mSelectiveBits.length;
	}
	public int numericSize() {
		return getStoichRational().getColumnCount() - booleanSize();
	}
	
//	public ReadableMatrix getStoich() {
//		return mModel.getStoich();
//	}
	public ReadableMatrix<BigFraction> getStoichRational() {
		return mModel.getStoichRational();
	}
	
	
//	public boolean keepByColumnPairFilter(ColumnPair pair) {
//		return mModel.columnPairFilter.keepPair(pair, mConfig, mModel.reactionMapping);
//	}
	
	public boolean enterIfCandidates(T token, Node<T> nodeA, Node<T> nodeB) {
		final int interCard = nodeA.unionPattern.getAndCardinality(nodeB.unionPattern);
		return isRequiredZeroBitCount(token, interCard);// && mModel.columnPairFilter.keepPair(booleanSize(), numericSize(), unionCut, mConfig, mModel.reactionMapping);		
	}
	public void leave(T token, Node<T> nodeA, Node<T> nodeB) {
		//nothing to do here
	}

	/**
	 * the ration zero/one which gives best performance.
	 * a ration 1f means a well balanced tree, that is, the same number of zeros
	 * (zero child node) as ones (one child node).
	 * 
	 * old optimal value was 0.8, meaning that we have 4 parts zeros, 5 parts 
	 * ones, i.e. 4/9 and 5/9.
	 * 
	 * current best value: balanced 1.0
	 * 
	 * NOTE: current implementation uses xor samples, and zeroToOneOptimum is 1
	 *       since then, all bits would be different 
	 * 
	 * @see TreeMemAdjEnum#calculateBitOrder
	 */
//    private static final float zeroToOneOptimum = 0.8f;
    private static final float zeroToOneOptimum = 1f;
	public static int[] calculateCardinalityBitOrder(IndexableMemory<Column>... columns) {
		try {
			final int bitCount = ColumnUtil.getBooleanSize(columns[0]);
	
			//prescan for bit selection
			int[][] cnt = new int[bitCount][3];//0: zero 1: one 2: orig index
			for (int i = 0; i < columns.length; i++) {
				for (int bit = 0; bit < bitCount; bit++) {
					for(Column col : columns[i]) {
						if (col.get(bit)) cnt[bit][1]++;
						else cnt[bit][0]++;
					}
					cnt[bit][2] = bit;
				}
			}
			return calculateBitOrder(cnt);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	public static int[] calculateXorBitOrder(IndexableMemory<Column>... columns) {
		try {
			final Random rnd = new Random();
			// final Random rnd = new Random(0);
			final int bitCount = ColumnUtil.getBooleanSize(columns[0]);
	
			//prescan for bit selection
			int[][] cnt = new int[bitCount][3];//0: zero 1: one 2: orig index
			for (int i = 0; i < columns.length; i++) {
				final int colCount = columns[i].getColumnCount();
//				final int tstCount = Math.min(4 * colCount, 512);
				for (int bit = 0; bit < bitCount; bit++) {
					for (int j = 0; j < 1024 /*number of random samples*/; j++) {
						final Column col0 = columns[i].getColumn(rnd.nextInt(colCount));
						final Column col1 = columns[i].getColumn(rnd.nextInt(colCount));
						if (col0.get(bit) != col1.get(bit)) {
//							final SimpleLongBitSet xor = SimpleLongBitSet.getXor(col0.bitValues(), col1.bitValues());
//							cnt[bit][0] += xor.cardinality();
							cnt[bit][0] += col0.bitValues().getXorCardinality(col1.bitValues());
							cnt[bit][1] += bitCount;
						}
						else {
							cnt[bit][1]++;							
						}
					}
					cnt[bit][2] = bit;					
				}
			}
			return calculateBitOrder(cnt);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	private static int[] calculateBitOrder(final int[][] cnt) {
		Arrays.sort(cnt, new Comparator<int[]>() {
			public int compare(int[] o1, int[] o2) {
				float rel1	= (float)o1[0]/(float)o1[1];
				float rel2	= (float)o2[0]/(float)o2[1];
//				float diff1	= (rel1 - zeroToOneOptimum) * (rel1 - zeroToOneOptimum); 
//				float diff2	= (rel2 - zeroToOneOptimum) * (rel2 - zeroToOneOptimum);
				float diff1	= Math.abs(rel1 - zeroToOneOptimum); 
				float diff2	= Math.abs(rel2 - zeroToOneOptimum);
				return diff1 < diff2 ? -1 : diff1 > diff2 ? 1 : 0;
			}
		});
		int[] result = new int[cnt.length]; 
		for (int ii = 0; ii < cnt.length; ii++) {
			result[ii] = cnt[ii][2];
		}
		
		return result;
	}
	
}
