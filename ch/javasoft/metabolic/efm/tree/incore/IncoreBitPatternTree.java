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
package ch.javasoft.metabolic.efm.tree.incore;

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.Partition;
import ch.javasoft.metabolic.efm.util.ColumnUtil;

/**
 * The <code>IncoreBitPatternTree</code> is a bit pattern tree which is stored
 * in memory only.
 */
public class IncoreBitPatternTree implements BitPatternTree {

	private final Kind kind;
	private final int bitSetSize;
	private final InCoreNode root;
	
	public <Col extends Column, N extends Number> IncoreBitPatternTree(Thread owner, ColumnHome<N, Col> columnHome, EfmModel efmModel, AdjEnumModel<Col> itModel, Kind kind, final int[] selectiveBits, SortableMemory<Col> columns) throws IOException {
		this.kind = kind;
		this.bitSetSize = itModel.getCurrentState().getBooleanSize();
		
		final int cols = columns.getColumnCount();
		final Partition partition = ColumnUtil.partitionColumns(columns, selectiveBits[0], 0, cols);
		this.root = createNode(columnHome, selectiveBits, 0, columns, 0, cols, partition);
	}
		
	public Kind kind() {
		return kind;
	}

	public int bitSetSize() {
		return bitSetSize;
	}

	public Node root() {
		return root;
	}

	/**
	 * Does nothing
	 */
	public void closeForCurrentThread() throws IOException {
		//nothing to do
	}
	/**
	 * Does nothing
	 */
	public void close() throws IOException {
		//nothing to do
	}
	
	private static final int MAX_LEAF_SIZE = 4;
	private <Col extends Column, N extends Number> InCoreNode createNode(ColumnHome<N, Col> columnHome, final int[] selectiveBits, int curSelectiveBit, SortableMemory<Col> columns, int start, int end, Partition lastPartition) throws IOException {
		if (end - start <= MAX_LEAF_SIZE) {
			return createLeafNode(columnHome, selectiveBits, curSelectiveBit, columns, start, end, lastPartition);
		}
		return createInterNode(columnHome, selectiveBits, curSelectiveBit, columns, start, end, lastPartition);
	}
	protected <Col extends Column, N extends Number> IncoreInterNode createInterNode(ColumnHome<N, Col> columnHome, final int[] selectiveBits, int curSelectiveBit, SortableMemory<Col> columns, int start, int end, Partition lastPartition) throws IOException {
		//this loop does path-shortening:
		//- intermediary nodes with only one child are shortened
		//- this saves memory and enhances performance significantly
		while (lastPartition.getMedian() == start || lastPartition.getMedian() == end) {
			curSelectiveBit++;
			lastPartition = ColumnUtil.partitionColumns(columns, selectiveBits[curSelectiveBit], start, end);
		}

		//now, create the inter node
		final IBitSet union = lastPartition.unionPattern();
		final int median = lastPartition.getMedian();
		
		curSelectiveBit++;
		final Partition partitionLeft 	= ColumnUtil.partitionColumns(columns, selectiveBits[curSelectiveBit], start, median);
		final Partition partitionRight 	= ColumnUtil.partitionColumns(columns, selectiveBits[curSelectiveBit], median, end);
		
		final InCoreNode left 	= createNode(columnHome, selectiveBits, curSelectiveBit, columns, start, median, partitionLeft);
		final InCoreNode right 	= createNode(columnHome, selectiveBits, curSelectiveBit, columns, median, end, partitionRight);

		return new IncoreInterNode(this, union, left, right);
	}
	protected <Col extends Column, N extends Number> IncoreLeafNode createLeafNode(ColumnHome<N, Col> columnHome, final int[] selectiveBits, int curSelectiveBit, SortableMemory<Col> columns, final int start, final int end, Partition lastPartition) throws IOException {
		final IBitSet union = lastPartition.unionPattern();
		return new IncoreLeafNode(union, start, end);
	}

}
