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
package ch.javasoft.metabolic.efm.tree.outcore;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.jbase.FixedWidthTable;
import ch.javasoft.jbase.Table;
import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.memory.outcore.Cache;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.Partition;
import ch.javasoft.metabolic.efm.util.ColumnUtil;

/**
 * The <code>PersistentBitPatternTree</code> has stored all needed 
 * state information in files using {@link FixedWidthTable fixed width tables}. 
 * In memory, {@link WeakReference weak references} are used such that the 
 * garbage collector is able to free resources on demand.
 */
public class PersistentBitPatternTree implements BitPatternTree {

	//profiling
//	volatile long nodeCount;
//	volatile long activeNodeCount;
//	final AtomicLong maxActiveNodeCount = new AtomicLong();
//	
//	void nodeInc() {
//		nodeCount++;
//		activeNodeCount++;
//		long max = maxActiveNodeCount.get();
//		while (activeNodeCount > max) {
//			maxActiveNodeCount.compareAndSet(max, activeNodeCount);
//			max = maxActiveNodeCount.get();
//		}
//	}
//	void nodeDec() {
//		activeNodeCount--;
//	}
	
	private final Kind kind;
	private final ConcurrentTable<PersistentNodeEntity> table;
	private final int bitSetSize;
	private final PersistentNode root;
	
	//open tree from file
	private PersistentBitPatternTree(Thread owner, Kind kind, FixedWidthTable<PersistentNodeEntity> table, int bitSetSize) throws IOException {
		this.kind		= kind;
		this.table		= new ConcurrentTable<PersistentNodeEntity>(table, owner);
		this.bitSetSize	= bitSetSize;
		this.root		= table.get(0).toNode(this, null, null);
	}
	//create tree and write to tree file
	private <Col extends Column, N extends Number> PersistentBitPatternTree(Thread owner, File tmpDir, ColumnHome<N, Col> columnHome, EfmModel efmModel, AdjEnumModel<Col> itModel, Kind kind, final int[] selectiveBits, SortableMemory<Col> columns) throws IOException {
		this.kind		= kind;
		final File file = getTreeFile(tmpDir, efmModel.getConfig(), efmModel, itModel, kind);
		final int cols = columns.getColumnCount();
		final Partition partition = ColumnUtil.partitionColumns(columns, selectiveBits[0], 0, cols);

		this.bitSetSize = itModel.getCurrentState().getBooleanSize();
		this.table 		= new ConcurrentTable<PersistentNodeEntity>(
			FixedWidthTable.create(
				file, new PersistentNodeEntityMarshaller(bitSetSize),
	            Cache.PersistentBitPatternTree.getCacheTableSize(),
	            Cache.PersistentBitPatternTree.getCacheEntrySize()				
			), owner
		);
		//this.table		= new MemoryTable<PersistentNodeEntity>();
		this.root 		= createNode(columnHome, table, selectiveBits, 0, columns, 0, cols, partition);
		this.table.flush();
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
	
	public void closeForCurrentThread() throws IOException {
		table.close(false);
	}
	
	public void close() throws IOException {
		table.close(true);
	}
	
	public PersistentNodeEntity getEntity(int tableIndex) throws IOException {
		return table.get(tableIndex);
	}
	
	public static <Col extends Column, N extends Number> PersistentBitPatternTree open(Thread owner, File folder, ColumnHome<N, Col> columnHome, EfmModel efmModel, AdjEnumModel<Col> itModel, Kind kind) throws IOException {
		final int bitSetSize = itModel.getCurrentState().getBooleanSize();
		final File file = getTreeFile(folder, efmModel.getConfig(), efmModel, itModel, kind);
		final FixedWidthTable<PersistentNodeEntity> table = FixedWidthTable.open(
            file, new PersistentNodeEntityMarshaller(bitSetSize), 
            Cache.PersistentBitPatternTree.getCacheTableSize(),
            Cache.PersistentBitPatternTree.getCacheEntrySize()
		);
		return new PersistentBitPatternTree(owner, kind, table, bitSetSize);
	}
	public static <Col extends Column, N extends Number> PersistentBitPatternTree create(Thread owner, File folder, ColumnHome<N, Col> columnHome, EfmModel efmModel, AdjEnumModel<Col> itModel, Kind kind, final int[] selectiveBits, SortableMemory<Col> columns) throws IOException {		
		return new PersistentBitPatternTree(owner, folder, columnHome, efmModel, itModel, kind, selectiveBits, columns);
	}
	
	private static File getTreeFile(File folder, Config config, EfmModel model, AdjEnumModel<? extends Column> iterationModel, Kind kind) {
		return new File(folder, "bstree-" + iterationModel.getIterationIndex() + "-" + kind.toChar() + ".tbl");
	}

	private static final int MAX_LEAF_SIZE = 4;
	private <Col extends Column, N extends Number> PersistentNode createNode(ColumnHome<N, Col> columnHome, Table<PersistentNodeEntity> table, final int[] selectiveBits, int curSelectiveBit, SortableMemory<Col> columns, int start, int end, Partition lastPartition) throws IOException {
		if (end - start <= MAX_LEAF_SIZE) {
			return createLeafNode(columnHome, selectiveBits, curSelectiveBit, columns, start, end, lastPartition);
		}
		return createInterNode(columnHome, selectiveBits, curSelectiveBit, columns, start, end, lastPartition);
	}
	protected <Col extends Column, N extends Number> PersistentInterNode createInterNode(ColumnHome<N, Col> columnHome, final int[] selectiveBits, int curSelectiveBit, SortableMemory<Col> columns, int start, int end, Partition lastPartition) throws IOException {
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
		
		final int myTableIndex = table.size();
		PersistentNodeEntity entity = PersistentNodeEntity.createForInterNode(union, 0, 0 /*place holders*/);
		table.add(entity);//reserve this place
		
		curSelectiveBit++;
		final Partition partitionLeft 	= ColumnUtil.partitionColumns(columns, selectiveBits[curSelectiveBit], start, median);
		final Partition partitionRight 	= ColumnUtil.partitionColumns(columns, selectiveBits[curSelectiveBit], median, end);
		
		//the next entry added is the left child entry
		final int leftTableIndex = table.size();
		final PersistentNode left = createNode(columnHome, table, selectiveBits, curSelectiveBit, columns, start, median, partitionLeft);

		//the next entry added is the right child entry
		final int rightTableIndex = table.size();
		final PersistentNode right = createNode(columnHome, table, selectiveBits, curSelectiveBit, columns, median, end, partitionRight);

		//rewrite entity now with correct left/right indices
		entity = PersistentNodeEntity.createForInterNode(union, leftTableIndex, rightTableIndex);
		table.set(myTableIndex, entity);
		return entity.toInterNode(this, left, right);
	}
	protected <Col extends Column, N extends Number> PersistentLeafNode createLeafNode(ColumnHome<N, Col> columnHome, final int[] selectiveBits, int curSelectiveBit, SortableMemory<Col> columns, final int start, final int end, Partition lastPartition) throws IOException {
		final IBitSet union = lastPartition.unionPattern();
		final PersistentNodeEntity entity = PersistentNodeEntity.createForLeaf(union, start, end);
		table.add(entity);
		return entity.toLeafNode(this);
	}

	//for profiling:
//	@Override
//	public String toString() {
//		return "bstree[total,active,maxactive] = [" + nodeCount + ", " + activeNodeCount + ", " + maxActiveNodeCount + "]";
//	}
}
