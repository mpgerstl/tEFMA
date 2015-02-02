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
package ch.javasoft.metabolic.efm.adj.incore.tree.urank;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Callable;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.adj.incore.tree.AbstractTreeFactory;
import ch.javasoft.metabolic.efm.adj.incore.tree.InterNode;
import ch.javasoft.metabolic.efm.adj.incore.tree.Node;
import ch.javasoft.metabolic.efm.adj.incore.tree.Root;
import ch.javasoft.metabolic.efm.adj.incore.tree.Traverser;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.concurrent.RankUpdateToken;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.rankup.PreprocessableMatrix;
import ch.javasoft.metabolic.efm.rankup.PreprocessedMatrix;

/**
 * The old version for rank update strategies, has been replaced by the faster
 * {@link RankUpdateJobScheduleTreeFactory}, which shows better scaling for more 
 * than 2 threads
 */
abstract public class RankUpdateTreeFactory extends AbstractTreeFactory<RankUpdateToken> {
	
	/**
	 * Constructor with specified number of threads to use
	 */
	public RankUpdateTreeFactory(EfmModel efmModel) {		
		super(efmModel);
	}
	
	@Override
	public InterNode<RankUpdateToken> createInterNode(SortableMemory<Column> cols, int[] selectiveBits, int prevSelBitIndex, int iStart, int iEnd) throws IOException {
		return new InterNode<RankUpdateToken>(this, cols, selectiveBits, prevSelBitIndex, iStart, iEnd) {
			@Override
			public void addAdjacentPairs(final RankUpdateToken token, final Root<RankUpdateToken> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, Node<RankUpdateToken> partner, final boolean thisIsPos, final Queue<ColumnPair> adjacentPairs) throws IOException {
                                System.out.println("in InterNode<RankUpdateToken>  RankUpdateTreeFactory.addAdjacentPairs()");
				if (root.enterIfCandidates(token, this, partner)) {
		    		if (partner instanceof InterNode) {
		    			final InterNode<RankUpdateToken> interPartner = (InterNode<RankUpdateToken>)partner;
						if (token.tryAcquirePermit()) {
							final Callable<Void> callable = new Callable<Void>() {
								public Void call() {
									try {
										child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
										child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);
									} 
									catch (IOException ex) {
										throw new RuntimeException(ex);
									}
									finally {
										token.releasePermit();
									}
									return null;//void
								}
							};
							token.createChildThread(callable, null).start();
							child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
							child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);
						}
						else {
							child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
							child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);
							child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child0, thisIsPos, adjacentPairs);
							child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, interPartner.child1, thisIsPos, adjacentPairs);    					
						}
		    		}
		    		else {
						child0.addAdjacentPairs(token, root, posCols, zeroCols, negCols, partner, thisIsPos, adjacentPairs);
						child1.addAdjacentPairs(token, root, posCols, zeroCols, negCols, partner, thisIsPos, adjacentPairs);		    			
		    		}
		    		root.leave(token, this, partner);
				}
			}
		};
	}
	
	public Traverser<RankUpdateToken> createTraverser() {
		return new Traverser<RankUpdateToken>() {
			public void traverseTree(Root<RankUpdateToken> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Queue<ColumnPair> adjacentPairs) throws IOException {
				RankUpdateToken token = new RankUpdateToken(efmModel);
				if (token.tryAcquirePermit()) {//since current thread is one of them
					final IBitSet cut = root.pos().unionPattern.getAnd(root.neg().unionPattern);
					token.addRootRankMatrix(cut);
					root.pos().addAdjacentPairs(token, root, posCols, zeroCols, negCols, root.neg(), true /*thisIsPos*/, adjacentPairs);
					token.removeRootRankMatrix();
					token.releasePermit();
					try {
						token.waitForChildThreads();
					} 
					catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
				else {
					throw new RuntimeException("no initial thread");
				}
			}
		};
	}
	
	abstract protected PreprocessedMatrix createInitialPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token, RankUpdateRoot root);
	abstract protected PreprocessedMatrix createChildPreprocessedMatrix(PreprocessableMatrix owner, RankUpdateToken token, RankUpdateRoot root, PreprocessedMatrix parentPreprocessedMatrix);
	
}
