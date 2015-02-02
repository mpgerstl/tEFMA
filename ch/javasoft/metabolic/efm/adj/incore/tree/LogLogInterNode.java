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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.AbstractColumn;
import ch.javasoft.metabolic.efm.column.AdjCandidates;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

public class LogLogInterNode<T /*traversing token*/> extends Node<T> {
	private final Node<T>[] children;
	public LogLogInterNode(TreeFactory<T> treeFactory, SortableMemory<Column> cols, int[] selectiveBits, int prevSelBitIndex, int iStart, int iEnd) throws IOException {
		super(Node.calculateUnionPattern(cols, iStart, iEnd));
        final int bitsToUse = getBitsToUse(iStart, iEnd, selectiveBits, prevSelBitIndex);
        children = createChildArray(1 << bitsToUse);
        initChildren(treeFactory, cols, 0, iStart, iEnd, selectiveBits, prevSelBitIndex, bitsToUse);
	}
    /**
     * Use approximately half of the remaining bits, round down
     */
    protected int getBitsToUse(int start, int end, int[] selectiveBits, int prevSelBitIndex) {
    	final int bitsLeft = 32 - Integer.numberOfLeadingZeros(end-start-1);
//    	return 1;//86s
    	return Math.max(1, bitsLeft >>> 3);//86s
//    	return Math.max(1, bitsLeft >>> 4);//87s
//    	return Math.max(1, bitsLeft >>> Math.max(3, 31 - Integer.numberOfLeadingZeros(prevSelBitIndex)));//81s, 87s
//    	return Math.max(1, bitsLeft >>> Math.max(2, 32 - Integer.numberOfLeadingZeros(prevSelBitIndex + 1)));//88s
//    	return Math.max(1, bitsLeft >>> Math.max(3, 30 - Integer.numberOfLeadingZeros(prevSelBitIndex)));//<81s, 87s
//    	return Math.max(1, bitsLeft >>> Math.max(4, 31 - Integer.numberOfLeadingZeros(prevSelBitIndex)));//82s
    }
    @SuppressWarnings("unchecked")
    private static <TT> Node<TT>[] createChildArray(int len) {
        return new Node[len];
    }
	private void initChildren(TreeFactory<T> treeFactory, SortableMemory<Column> cols, int childOffset, int start, int end, int[] selectiveBits, int prevSelBitIndex, int bitsToUse) throws IOException {
		if (end > start) {
	        final int median = AbstractColumn.partition(cols, start, end, selectiveBits[++prevSelBitIndex]);
	        if (bitsToUse > 1 && end > start + Leaf.MAX_LEAF_SIZE) {
	            initChildren(treeFactory, cols, childOffset, start, median, selectiveBits, prevSelBitIndex, bitsToUse - 1);
	            initChildren(treeFactory, cols, childOffset + (1 << (bitsToUse - 1)), median, end, selectiveBits, prevSelBitIndex, bitsToUse - 1);
	        }
	        else {
	            children[childOffset] = treeFactory.createNode(cols, selectiveBits, prevSelBitIndex, start, median);
	            children[childOffset + 1] = treeFactory.createNode(cols, selectiveBits, prevSelBitIndex, median, end);
	        }
		}
    }
	@Override
	public void addAdjacentPairs(T token, Root<T> root, SortableMemory<Column> posCols, SortableMemory<Column> zeroCols, SortableMemory<Column> negCols, Node<T> partner, boolean thisIsPos, Queue<ColumnPair> adjacentPairs) throws IOException {
                System.out.println("in void LogLogInterNode.addAdjacentPairs()");
		if (root.enterIfCandidates(token, this, partner)) {
    		if (partner instanceof Leaf) {
                for (int i = 0; i < children.length; i++) {
                    final Node<T> child = children[i];
                    if (child != null) {
                        child.addAdjacentPairs(token, root, posCols, zeroCols, negCols, partner, thisIsPos, adjacentPairs);
                    }
                }
    		}
    		else {
    			final LogLogInterNode<T> interPartner = (LogLogInterNode<T>)partner;
                for (int i = 0; i < children.length; i++) {
                    final Node<T> child = children[i];
                    if (child != null) {
                        for (int j = 0; j < interPartner.children.length; j++) {
                            final Node<T> other = interPartner.children[j];
                            if (other != null) {
                                child.addAdjacentPairs(token, root, posCols, zeroCols, negCols, other, thisIsPos, adjacentPairs);
                            }
                        }
                    }
                }
    		}
			root.leave(token, this, partner);
		}
	}
	@Override
	public IBitSet filterAdjacentPairs(IBitSet filterCutPattern, SortableMemory<Column> cols, AdjCandidates<Column> adjCandidates) throws IOException {
		for (int i = 0; i < children.length && filterCutPattern != null; i++) {
            final Node<T> child = children[i];
            if (child != null && filterCutPattern.isSubSetOf(child.unionPattern)) {
                filterCutPattern = child.filterAdjacentPairs(filterCutPattern, cols, adjCandidates);
            }
        }
		return filterCutPattern;
	}
}
