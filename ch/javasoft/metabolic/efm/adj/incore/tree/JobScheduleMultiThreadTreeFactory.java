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
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.concurrent.SemaphoreConcurrentToken;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

/**
 * Instead of splitting recursions into 2 parts when new threads are created,
 * the tree is traversed (usually 3 levels) and jobs are created for each 
 * recursive call. All threads are created at once (as many as permits are
 * available), competing for the jobs in the sequel.
 * <p>
 * See also {@link JobQueue}.
 */
public class JobScheduleMultiThreadTreeFactory extends AbstractTreeFactory<SemaphoreConcurrentToken> {
	
        private static AtomicLong atomic_addAdjacentPairs_invocations;
        private static final Logger LOG = LogPkg.LOGGER;
	/**
	 * Constructor with specified number of threads to use
	 */
	public JobScheduleMultiThreadTreeFactory(EfmModel efmModel) {		
		super(efmModel);
		atomic_addAdjacentPairs_invocations = new AtomicLong();
	}
	
	@Override
	public InterNode<SemaphoreConcurrentToken> createInterNode(SortableMemory<Column> cols, int[] selectiveBits, int prevSelBitIndex, int iStart, int iEnd) throws IOException {
		return new InterNode<SemaphoreConcurrentToken>(this, cols, selectiveBits, prevSelBitIndex, iStart, iEnd) {
			@Override
			public void addAdjacentPairs(final SemaphoreConcurrentToken token, final Root<SemaphoreConcurrentToken> root, final SortableMemory<Column> posCols, final SortableMemory<Column> zeroCols, final SortableMemory<Column> negCols, final Node<SemaphoreConcurrentToken> partner, final boolean thisIsPos, final Queue<ColumnPair> adjacentPairs) throws IOException {

				atomic_addAdjacentPairs_invocations.getAndIncrement();
				if (root.enterIfCandidates(token, this, partner)) {
		    		if (partner instanceof InterNode) {
						final JobQueue<SemaphoreConcurrentToken> queue = JobQueue.createQueue(efmModel.getConfig(), token, root, posCols, zeroCols, negCols, this, partner, thisIsPos, adjacentPairs);
						if (queue != null) {
							queue.execParentThread();
						}
						else {
			    			final InterNode<SemaphoreConcurrentToken> interPartner = (InterNode<SemaphoreConcurrentToken>)partner;
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



	public Traverser<SemaphoreConcurrentToken> createTraverser() {
		return new Traverser<SemaphoreConcurrentToken>() {
			public void traverseTree(Root<SemaphoreConcurrentToken> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Queue<ColumnPair> adjacentPairs) throws IOException {

                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                String strObjType = root.pos().toString();

                                // System.out.println("STRING: strObjType root.pos().toString(): " + strObjType);
                                // if( strObjType.contains("ch.javasoft.metabolic.efm.adj.incore.tree.InterNode") )
                                if( strObjType.contains("ch.javasoft.metabolic.efm.adj.incore.tree.JobScheduleMultiThreadTreeFactory") )
                                {
                                   ch.javasoft.metabolic.efm.adj.incore.tree.InterNode myInterNode = (ch.javasoft.metabolic.efm.adj.incore.tree.InterNode) root.pos();
                                   myInterNode.reset_atomic_addAdjacentPairs_invocations();

                                   ///////////////////////////////////////////
                                   // try to find a UnaryLeaf
                                   ///////////////////////////////////////////
                                   Node childNode0 = myInterNode.child0;
                                   Node childNode1 = myInterNode.child1;
                                   while( childNode0 instanceof InterNode )
                                   {
                                        childNode0 = ((InterNode)childNode0).child0;
                                   }

                                   if( childNode0 instanceof UnaryLeaf)
                                   {
                                       UnaryLeaf myFoundChild = (UnaryLeaf) childNode0;
                                       myFoundChild.reset_atomic_addAdjacentPairs_invocations();
                                       myFoundChild.reset_atomic_adjacency_tests();
                                       myFoundChild.reset_atomic_found_adjacents();
                                   }
                                   else if( childNode1 instanceof UnaryLeaf)
                                   {
                                       UnaryLeaf myFoundChild = (UnaryLeaf) childNode1;
                                       myFoundChild.reset_atomic_addAdjacentPairs_invocations();
                                       myFoundChild.reset_atomic_adjacency_tests();
                                       myFoundChild.reset_atomic_found_adjacents();
                                   }
                                   else
                                   {
                                      System.out.println("HELP: This is strange! Found an empty leaf!");
                                   }
                                   ///////////////////////////////////////////
                                }
				reset_atomic_addAdjacentPairs_invocations();
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



				SemaphoreConcurrentToken token = new SemaphoreConcurrentToken(efmModel);
				if (token.tryAcquirePermit()) {//since current thread is one of them

					atomic_addAdjacentPairs_invocations.incrementAndGet();
					LOG.finest("JobScheduleMultiThreadTreeFactory.createTraverser(): root.pos().getClass(): " + root.pos().getClass());
					root.pos().addAdjacentPairs(token, root, posCols, zeroCols, negCols, root.neg(), true /*thisIsPos*/, adjacentPairs);
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

                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				LOG.finest("JobScheduleMultiThreadTreeFactory: get_atomic_addAdjacentPairs_invocations:" + get_atomic_addAdjacentPairs_invocations());

                                // if( strObjType.contains("ch.javasoft.metabolic.efm.adj.incore.tree.InterNode") )
                                if( strObjType.contains("ch.javasoft.metabolic.efm.adj.incore.tree.JobScheduleMultiThreadTreeFactory") )
                                {
                                   ch.javasoft.metabolic.efm.adj.incore.tree.InterNode myInterNode = (ch.javasoft.metabolic.efm.adj.incore.tree.InterNode) root.pos();
                                   LOG.finest("InterNode addAdjacentPairs_invocations(): " + myInterNode.get_atomic_addAdjacentPairs_invocations());

                                   ///////////////////////////////////////////
                                   // try to find a UnaryLeaf
                                   ///////////////////////////////////////////
                                   Node childNode0 = myInterNode.child0;
                                   Node childNode1 = myInterNode.child1;
                                   while( childNode0 instanceof InterNode )
                                   {
                                        childNode0 = ((InterNode)childNode0).child0;
                                   }

                                   if( childNode0 instanceof UnaryLeaf)
                                   {
                                       UnaryLeaf myFoundChild = (UnaryLeaf) childNode0;
                                       LOG.finest("UnaryLeaf.get_atomic_addAdjacentPairs_invocations(): " + myFoundChild.get_atomic_addAdjacentPairs_invocations());
                                       LOG.finest("UnaryLeaf.myFoundChild.get_atomic_adjacency_tests(): " + myFoundChild.get_atomic_adjacency_tests());
                                       LOG.finest("UnaryLeaf.get_atomic_found_adjacents(): " + myFoundChild.get_atomic_found_adjacents());
                                   }
                                   else if( childNode1 instanceof UnaryLeaf)
                                   {
                                       UnaryLeaf myFoundChild = (UnaryLeaf) childNode1;
                                       LOG.finest("UnaryLeaf.get_atomic_addAdjacentPairs_invocations(): " + myFoundChild.get_atomic_addAdjacentPairs_invocations());
                                       LOG.finest("UnaryLeaf.myFoundChild.get_atomic_adjacency_tests(): " + myFoundChild.get_atomic_adjacency_tests());
                                       LOG.finest("UnaryLeaf.get_atomic_found_adjacents(): " + myFoundChild.get_atomic_found_adjacents());
                                   }
                                   else
                                   {
                                      System.out.println("HELP: This is strange! Found an empty leaf!");
                                   }
                                   ///////////////////////////////////////////
                                }
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



			}
		};
	}

        // atomic
        public long get_atomic_addAdjacentPairs_invocations() {
                return atomic_addAdjacentPairs_invocations.get();
        }

        public long reset_atomic_addAdjacentPairs_invocations() {
        	LOG.finest("entered JobScheduleMultiThreadTreeFactory.reset_atomic_addAdjacentPairs_invocations()");
                long ret = atomic_addAdjacentPairs_invocations.get();
                atomic_addAdjacentPairs_invocations.set(0);
                return ret;
        }
}
