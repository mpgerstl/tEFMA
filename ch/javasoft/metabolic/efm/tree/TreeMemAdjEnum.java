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
package ch.javasoft.metabolic.efm.tree;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Queue;
import java.util.Random;

import ch.javasoft.job.Job;
import ch.javasoft.job.MultiJobExecutable;
import ch.javasoft.metabolic.efm.adj.AbstractAdjEnum;
import ch.javasoft.metabolic.efm.adj.incore.tree.AbstractRoot;
import ch.javasoft.metabolic.efm.adj.incore.tree.Root;
import ch.javasoft.metabolic.efm.adj.incore.tree.Traverser;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.dist.DistributedInfo;
import ch.javasoft.metabolic.efm.dist.PartIterator;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.memory.outcore.Recovery;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.progress.IntProgressAggregator;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;
import ch.javasoft.metabolic.efm.progress.ProgressNotifiable;
import ch.javasoft.metabolic.efm.tree.BitPatternTree.Kind;
import ch.javasoft.metabolic.efm.tree.impl.DefaultTreePairTraverser;
import ch.javasoft.metabolic.efm.tree.impl.SubtreePairTraverser;
import ch.javasoft.metabolic.efm.tree.outcore.PersistentBitPatternTree;
import ch.javasoft.metabolic.efm.util.ColumnUtil;
import ch.javasoft.metabolic.efm.util.PreconditionUtil;
import ch.javasoft.util.ExceptionUtil;

/**
 * The <code>TreeMemAdjEnum</code> is the superclass of most adjacency 
 * enumerators which base the enumeration on bit pattern trees.
 */
abstract public class TreeMemAdjEnum<T extends ConcurrentToken> extends AbstractAdjEnum {

	public TreeMemAdjEnum(String name) {
		super(name);
	}
	
	/**
	 * The adjacent pair enumeration is started here, which consists of the
	 * following actions:<ol>
	 * 	<li>Two bit pattern trees are {@link #createTree(Thread, ColumnHome, AdjEnumModel, BitPatternTree.Kind, int[], SortableMemory) created},
	 * 		one for the positive and one for the negative columns.
	 *  </li>
	 * 	<li>A tree traverser is {@link #createTreeTraverser(ColumnHome, AdjEnumModel, BitPatternTree, BitPatternTree, AdjacencyPrecondition) created}.</li>
	 * 	<li>A token is {@link #createToken(ColumnHome, AdjEnumModel, BitPatternTree, BitPatternTree, ProgressAggregator) created}.</li>
	 * 	<li>The trees are {@link Traverser#traverseTree(Root, SortableMemory, SortableMemory, SortableMemory, Queue) traversed}
	 * 		using the tree traverser
	 *  </li> 
	 * 	<li>The token is {@link #releaseToken(ColumnHome, AdjEnumModel, BitPatternTree, BitPatternTree, ConcurrentToken) released}.
	 * </ol>
	 * Note that if no candidates exist, none of the above actions is executed.
	 */
	public <Col extends Column, N extends Number> void adjacentPairs(final ColumnHome<N, Col> columnHome, final AdjEnumModel<Col> itModel) throws IOException {
		if (itModel.getMemoryPos().getColumnCount() == 0 || itModel.getMemoryNeg().getColumnCount() == 0) {
			return;
		}
                System.out.println("in TreeMemAdjEnum.adjacentPairs()");
		
		final ProgressAggregator progress;
		final ProgressNotifiable prog = getConfig().getProgressType().createProgressNotifiable(getConfig(), itModel);
		if (prog == null || getConfig().getProgressPartition() <= 0) {
			progress = null;
		}
		else {
			progress = new IntProgressAggregator(prog);
		}
		
		final Thread treeOwner = Thread.currentThread();//we own the trees
		final Trees trees = createOrRecoverTrees(treeOwner, columnHome, itModel);
        final TreePairTraverser<T> traverser = createTreeTraverser(columnHome, itModel, trees.getPosTree(), trees.getNegTree(), null);
		traverseTrees(columnHome, itModel, trees, traverser, progress);
		
		//close trees and erase tree files
		trees.getPosTree().close();
		trees.getNegTree().close();
	}
	public <Col extends Column, N extends Number> void execCentralized(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel, AdjEnumModel<Col> itModel) throws IOException {
		if (itModel.getMemoryPos().getColumnCount() > 0 && itModel.getMemoryNeg().getColumnCount() > 0) {
			initialize(columnHome, config, efmModel);
			createOrRecoverTrees(Thread.currentThread(), columnHome, itModel);
		}
	}

	public <Col extends Column, N extends Number> void execDistributed(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel, AdjEnumModel<Col> itModel, DistributedInfo distInfo, PartIterator partIterator, ProgressAggregator progress) throws IOException {
		if (itModel.getMemoryPos().getColumnCount() == 0 || itModel.getMemoryNeg().getColumnCount() == 0) {
			return;
		}
		initialize(columnHome, config, efmModel);
		int subTreeIndex = partIterator.getNextPart();
		if (subTreeIndex >= 0) {
			int subTreeLevel = 0;//partition count = 4^subTreeLevel
			while ((1 << (subTreeLevel << 1)) < distInfo.getPartitionCount()) {
				subTreeLevel++;
			}
			
			final Thread treeOwner = null /*we are not owners of the trees*/;
			final Trees trees = openTrees(treeOwner, columnHome, itModel);
			
			do {
		        final TreePairTraverser<T> traverser = createTreeTraverser(columnHome, itModel, trees.getPosTree(), trees.getNegTree(), null);
				traverseTrees(columnHome, itModel, trees, new SubtreePairTraverser<T>(subTreeLevel, subTreeIndex, traverser), progress);
				subTreeIndex = partIterator.getNextPart();
			}
			while (subTreeIndex >= 0);	

			//close trees, but do not erase tree files since they're possibly 
			//still read by other processes
			trees.getPosTree().closeForCurrentThread();
			trees.getNegTree().closeForCurrentThread();
		}
	}

	private static interface Trees {
		BitPatternTree getPosTree();
		BitPatternTree getNegTree();
	}
	private static class TreeJob {
		private final MultiJobExecutable<BitPatternTree> executable;
		public TreeJob(Job<BitPatternTree> job1, Job<BitPatternTree> job2) {
			executable = new MultiJobExecutable<BitPatternTree>(job1, job2);
		}
		public Trees execAndWait() throws IOException {
			final BitPatternTree posTree, negTree;
			try {
				final Queue<BitPatternTree> trees = executable.execAndWaitThrowException();
				if (trees.peek().kind() == Kind.Pos) {
					posTree = trees.remove();
					negTree = trees.remove();
				}
				else {
					negTree = trees.remove();
					posTree = trees.remove();
				}
			} 			
	        catch (Throwable e) {
	        	throw ExceptionUtil.toRuntimeExceptionOr(IOException.class, e);
	        }
	        return new Trees() {
	    		public BitPatternTree getPosTree() {
	    			return posTree;
	    		}
	    		public BitPatternTree getNegTree() {
	    			return negTree;
	    		}
	        };
		}
	}
	
	private <Col extends Column, N extends Number> Trees createOrRecoverTrees(final Thread treeOwner, final ColumnHome<N, Col> columnHome, final AdjEnumModel<Col> itModel) throws IOException {
		final Recovery recovery = Recovery.getRecovery(getConfig().getFlag());
		if (recovery != null && recovery.isTreeRecovery()) {
			return openTrees(treeOwner, columnHome, itModel);
		}
		else {
			//no sorting, seems almost as fast since trees use path-shortening
//			final int[] selectiveBits = MappingUtil.getInitialMapping(ColumnUtil.getBooleanSize(itModel.getMemoryPos()));
			//sorting
			final int[] selectiveBits = calculateBitOrder(itModel.getMemoryPos(), itModel.getMemoryNeg());
			return createTrees(treeOwner, columnHome, itModel, selectiveBits);
		}
	}
	private <Col extends Column, N extends Number> Trees createTrees(final Thread treeOwner, final ColumnHome<N, Col> columnHome, final AdjEnumModel<Col> itModel,final int[] selectiveBits) throws IOException {
		final TreeJob treeJob = new TreeJob(
			createTreeJob(Thread.currentThread(), columnHome, itModel, BitPatternTree.Kind.Pos, selectiveBits),	
			createTreeJob(Thread.currentThread(), columnHome, itModel, BitPatternTree.Kind.Neg, selectiveBits)
		);
		return treeJob.execAndWait();
	}
	private <Col extends Column, N extends Number> Job<BitPatternTree> createTreeJob(final Thread treeOwner, final ColumnHome<N, Col> columnHome, final AdjEnumModel<Col> itModel, final BitPatternTree.Kind kind, final int[] selectiveBits) throws IOException {
		final SortableMemory<Col> mem = kind == BitPatternTree.Kind.Pos ? itModel.getMemoryPos() : itModel.getMemoryNeg();
		return new Job<BitPatternTree>() {
			public BitPatternTree run() throws IOException {
				final BitPatternTree tree = createTree(treeOwner, columnHome, itModel, kind, selectiveBits, mem);
				mem.close(false /*erase*/);
				if (treeOwner != Thread.currentThread()) {
					tree.closeForCurrentThread();//we can close our tree if we do not own it
				}
				return tree;
			}
		};
	}
	private <Col extends Column, N extends Number> Trees openTrees(final Thread treeOwner, final ColumnHome<N, Col> columnHome, final AdjEnumModel<Col> itModel) throws IOException {
		final BitPatternTree posTree = openTree(treeOwner, columnHome, itModel, Kind.Pos);
		final BitPatternTree negTree = openTree(treeOwner, columnHome, itModel, Kind.Neg);
		return new Trees() {
			public BitPatternTree getPosTree() {
				return posTree;
			}
			public BitPatternTree getNegTree() {
				return negTree;
			}
		};
	}
	
	private <Col extends Column, N extends Number> void traverseTrees(final ColumnHome<N, Col> columnHome, final AdjEnumModel<Col> itModel, final Trees trees, TreePairTraverser<T> treeTraverser, ProgressAggregator progress) throws IOException {
		//profiling:
//		System.out.println("pos/neg: " + posTree + " / " + negTree);
		
		final T token = createToken(columnHome, itModel, trees.getPosTree(), trees.getNegTree(), progress);		
		treeTraverser.traverse(columnHome, itModel, token, trees.getPosTree(), trees.getNegTree());
		
		//profiling:
//		System.out.println("pos/neg: " + posTree + " / " + negTree);
		
		releaseToken(columnHome, itModel, trees.getPosTree(), trees.getNegTree(), token);
		
		//profiling:
//		System.out.println("pos/neg: " + posTree + " / " + negTree);		
	}

	/**
	 * 
	 * Opens a {@link PersistentBitPatternTree} from an existing tree file.
	 * 
	 * @see #execDistributed(ColumnHome, Config, EfmModel, AdjEnumModel, DistributedInfo, PartIterator, ProgressAggregator)
	 * @see ch.javasoft.metabolic.efm.tree.outcore.PersistentBitPatternTree#open(Thread, File, ColumnHome, EfmModel, AdjEnumModel, BitPatternTree.Kind)
	 */
	protected <Col extends Column, N extends Number> BitPatternTree openTree(Thread treeOwner, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree.Kind kind) throws IOException {
		final Recovery recovery = Recovery.getRecovery(getConfig().getFlag());
		
		final File folder;
		if (recovery != null && recovery.isTreeRecovery()) {
			folder = recovery.getRecoveryFolder();
		}
		else {
			folder = getConfig().getTempDir().getPersonalizedDir();
		}
		return PersistentBitPatternTree.open(treeOwner, folder, columnHome, getEfmModel(), itModel, kind);
	}
	
	/**
	 * Subclasses create and return an instance of {@link BitPatternTree}, which
	 * can be stored in-core or out-of-core
	 */
	abstract protected <Col extends Column, N extends Number> BitPatternTree createTree(Thread treeOwner, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree.Kind kind, final int[] selectiveBits, SortableMemory<Col> columns) throws IOException;

	/**
	 * Subclasses create and return the actual tree traverser here. Usually, an
	 * instance of {@link DefaultTreePairTraverser} can be used with subclass 
	 * specific {@link AdjacencyPrecondition} and {@link AdjacencyFilter}.
	 * 
	 * @param 	precondition might be null, if not, it can be combined with 
	 * 			additional conditions using 
	 * 			{@link PreconditionUtil#and(AdjacencyPrecondition, AdjacencyPrecondition)}
	 * 						
	 */
	abstract protected <Col extends Column, N extends Number> TreePairTraverser<T> createTreeTraverser(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree posTree, BitPatternTree negTree, AdjacencyPrecondition<T> precondition) throws IOException;
	/**
	 * Subclasses create and return a new token here. Note that the token must
	 * be initialized, e.g. the main thread should already have acquired a 
	 * permit, or initial resources must be registered.
	 */
	abstract protected <Col extends Column, N extends Number> T createToken(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree posTree, BitPatternTree negTree, ProgressAggregator progress) throws IOException;
	/**
	 * Subclasses implement this method to free token related resources which
	 * have been initialized when the token was created. It might also be
	 * required that the main thread waits for child thread termination here.
	 */
	abstract protected <Col extends Column, N extends Number> void releaseToken(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, BitPatternTree posTree, BitPatternTree negTree, T token) throws IOException;
	
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
	 * @see AbstractRoot#calculateBitOrder
	 */
//    private static final float zeroToOneOptimum = 0.8f;
    private static final float zeroToOneOptimum = 1.0f;
	protected static int[] calculateBitOrder(SortableMemory<? extends Column> pos, SortableMemory<? extends Column> neg) throws IOException {		
		final int bitCount = Math.max(ColumnUtil.getBooleanSize(pos), ColumnUtil.getBooleanSize(neg));

		//prescan for bit selection
		int[][] cnt = new int[bitCount][3];//0: zero 1: one 2: orig index
		for (int bit = 0; bit < bitCount; bit++) {
			cnt[bit][2] = bit;
		}
		final Random rnd = new Random();
		// final Random rnd = new Random(0);
		//sort with both
		updateCount(pos, cnt, bitCount, rnd);
		updateCount(neg, cnt, bitCount, rnd);
		//sort with larger only
//		updateCount(pos.getColumnCount() > neg.getColumnCount() ? pos : neg, cnt);
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
		int[] result = new int[bitCount]; 
		for (int ii = 0; ii < cnt.length; ii++) {
			result[ii] = cnt[ii][2];
		}
		
		return result;
	}
	
	private static void updateCount(SortableMemory<? extends Column> columns, int[][] cnt, final int bitCount, final Random rnd) throws IOException {
		final int count = columns.getColumnCount();
		for (int i = 0; i < 1024 /*random sample size*/; i++) {
			final Column col0 = columns.getColumn(rnd.nextInt(count));
			final Column col1 = columns.getColumn(rnd.nextInt(count));
			for (int bit = 0; bit < cnt.length; bit++) {
				if (col0.get(bit) != col1.get(bit)) {
					cnt[bit][0] += col0.bitValues().getXorCardinality(col1.bitValues());
					cnt[bit][1] += bitCount;
				}
				else {
					cnt[bit][1]++;							
				}
			}			
		}
	}
//	private static void updateCount(SortableMemory<? extends Column> columns, int[][] cnt) throws IOException {
//		final int count = columns.getColumnCount();
//		for (int i = 0; i < count; i++) {
//			final Column col = columns.getColumn(i);
//			for (int bit = 0; bit < cnt.length; bit++) {
//				if (col.get(bit)) cnt[bit][1]++;
//				else cnt[bit][0]++;
//			}			
//		}
//	}
	
}
