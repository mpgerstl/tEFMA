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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

public class DefaultTreeFactory extends AbstractTreeFactory<Void> {
	
	public DefaultTreeFactory(EfmModel efmModel) {
		super(efmModel);
	}

	public Traverser<Void> createTraverser() {
		return new Traverser<Void>() {
			public void traverseTree(Root<Void> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Queue<ColumnPair> adjacentPairs) throws IOException {

                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                // System.out.println("in DefaultTreeFactory.traverseTree(). root.pos().getClass(): " + root.pos().getClass());
                                String strObjType = root.pos().toString();

                                if( strObjType.contains("ch.javasoft.metabolic.efm.adj.incore.tree.InterNode") )
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
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                
				root.pos().addAdjacentPairs(null, root, posCols, zeroCols, negCols, root.neg(), true /*thisIsPos*/, adjacentPairs);

                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                if( strObjType.contains("ch.javasoft.metabolic.efm.adj.incore.tree.InterNode") )
                                {
                                   ch.javasoft.metabolic.efm.adj.incore.tree.InterNode myInterNode = (ch.javasoft.metabolic.efm.adj.incore.tree.InterNode) root.pos();
                                   System.out.println("InterNode atomic_addAdjacentPairs_invocations(): " + myInterNode.get_atomic_addAdjacentPairs_invocations());

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
                                       System.out.println("UnaryLeaf.get_atomic_addAdjacentPairs_invocations(): " + myFoundChild.get_atomic_addAdjacentPairs_invocations());
                                       System.out.println("UnaryLeaf.myFoundChild.get_atomic_adjacency_tests(): " + myFoundChild.get_atomic_adjacency_tests());
                                       System.out.println("UnaryLeaf.get_atomic_found_adjacents(): " + myFoundChild.get_atomic_found_adjacents());
                                   }
                                   else if( childNode1 instanceof UnaryLeaf)
                                   {
                                       UnaryLeaf myFoundChild = (UnaryLeaf) childNode1;
                                       System.out.println("UnaryLeaf.get_atomic_addAdjacentPairs_invocations(): " + myFoundChild.get_atomic_addAdjacentPairs_invocations());
                                       System.out.println("UnaryLeaf.myFoundChild.get_atomic_adjacency_tests(): " + myFoundChild.get_atomic_adjacency_tests());
                                       System.out.println("UnaryLeaf.get_atomic_found_adjacents(): " + myFoundChild.get_atomic_found_adjacents());
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

}
