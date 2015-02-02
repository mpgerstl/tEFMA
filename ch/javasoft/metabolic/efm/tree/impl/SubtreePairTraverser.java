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
package ch.javasoft.metabolic.efm.tree.impl;

import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.InterNode;
import ch.javasoft.metabolic.efm.tree.LeafNode;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;

/**
 * The <code>SubtreeTraverser</code> traverses only a subtree, not the whole 
 * tree. The subtree is selected by specifying a number of zero bits (left child 
 * tree) and one bits (right child tree) for both trees.
 * <p>
 * The traversing of the subtree is delegated to another {@link TreePairTraverser}.
 */
public class SubtreePairTraverser<T extends ConcurrentToken> implements TreePairTraverser<T> {

//	private final int subTreeLevel;
	private final int subTreeIndex;
	private final int bitsPerTree;
	private final int posLeftRightBits, negLeftRightBits;
	private final TreePairTraverser<T> delegate;
	
	/**
	 * Constructor for <code>SubtreePairTraverser</code>
	 * 
	 * @param subTreeLevel	total partitions equals {@code 4^subTreeLevel}
	 * @param subTreeIndex	the current index, a number in {@code [0..4^subTreeLevel-1]}
	 * @param delegate 		the real traverser 
	 */
	public SubtreePairTraverser(int subTreeLevel, int subTreeIndex, TreePairTraverser<T> delegate) {
//		this.subTreeLevel 		= subTreeLevel;
		this.subTreeIndex		= subTreeIndex;
		this.bitsPerTree		= subTreeLevel;
		final int oneBits		= (0xffffffff >>> (32 - bitsPerTree));
		this.posLeftRightBits	= oneBits & subTreeIndex;
		this.negLeftRightBits	= ((oneBits << bitsPerTree) & subTreeIndex) >>> bitsPerTree;
		this.delegate		= delegate;
	}

	public <Col extends Column, N extends Number> void traverse(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, BitPatternTree posTree, BitPatternTree negTree) throws IOException {
		traverseNN(columnHome, iterationModel, token, 0, 0, true, posTree.root(), negTree.root());
	}

	public <Col extends Column, N extends Number> void traverseNN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) throws IOException {
		me.traverseN(this, columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, other);
	}
	
	private Node getChildTree(int depth, boolean isPos, InterNode node) {
		final int mask = 1 << depth;
		final int bits = isPos ? posLeftRightBits : negLeftRightBits;
		return ((mask & bits) == 0) ? node.left() : node.right();
	}
	private boolean visitLeafChildTree(int myDepth, int otherDepth, boolean isPos) {
		final int bits = isPos ? posLeftRightBits : negLeftRightBits;
		int curDepth = Math.max(myDepth, otherDepth);
		int mask = 1 << curDepth;
		for (int i = curDepth; i < bitsPerTree; i++) {
			if ((mask & bits) != 0) return false;
			mask <<= 1;
		}
		return true;
	}
	
	public <Col extends Column, N extends Number> void traverseIN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, Node other) throws IOException {
		other.traverseI(this, columnHome, iterationModel, token, otherDepth, myDepth, !meIsPos, me);
	}
	public <Col extends Column, N extends Number> void traverseLN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, Node other) throws IOException {
		other.traverseL(this, columnHome, iterationModel, token, otherDepth, myDepth, !meIsPos, me);
	}
	public <Col extends Column, N extends Number> void traverseII(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, InterNode other) throws IOException {
		if (myDepth < bitsPerTree && otherDepth < bitsPerTree) {
			final Node myChild 		= getChildTree(myDepth, meIsPos, me);
			final Node otherChild	= getChildTree(otherDepth, !meIsPos, other);
			traverseNN(columnHome, iterationModel, token, myDepth + 1, otherDepth + 1, meIsPos, myChild, otherChild);
		}
		else {
			delegate.traverseII(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
		}
	}
	public <Col extends Column, N extends Number> void traverseIL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, LeafNode other) throws IOException {
		if (myDepth < bitsPerTree && otherDepth < bitsPerTree) {
			if (visitLeafChildTree(otherDepth, myDepth, !meIsPos)) {
//				System.out.println("visit-" + (meIsPos ? "IL" : "LI") + ": " + this + "\t--> " + (meIsPos ? myDepth : otherDepth) + "/" + (!meIsPos ? myDepth : otherDepth));
				final Node myChild = getChildTree(myDepth, meIsPos, me);
				traverseLN(columnHome, iterationModel, token, otherDepth, myDepth + 1, !meIsPos, other, myChild);
			}
//			else {
//				System.out.println("no-visit-" + (meIsPos ? "IL" : "LI") + ": " + this + "\t--> " + (meIsPos ? myDepth : otherDepth) + "/" + (!meIsPos ? myDepth : otherDepth));
//			}
		}
		else {
			delegate.traverseIL(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
		}
	}
	public <Col extends Column, N extends Number> void traverseLL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, LeafNode other) throws IOException {
		if (myDepth < bitsPerTree && otherDepth < bitsPerTree) {
			if (visitLeafChildTree(myDepth, otherDepth, meIsPos) && visitLeafChildTree(otherDepth, myDepth, !meIsPos)) {
//				System.out.println("visit-LL: " + this + "\t--> " + (meIsPos ? myDepth : otherDepth) + "/" + (!meIsPos ? myDepth : otherDepth));
				delegate.traverseLL(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
			}
//			else {
//				System.out.println("no-visit-LL: " + this + "\t--> " + (meIsPos ? myDepth : otherDepth) + "/" + (!meIsPos ? myDepth : otherDepth));				
//			}
		}
		else {
			delegate.traverseLL(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
		}
	}
	
	@Override
	public String toString() {
		return "subtree[" + subTreeIndex + "/" + (1 << (bitsPerTree << 1)) + 
			": pos/neg=" + Integer.toBinaryString(posLeftRightBits) + 
			"/" + Integer.toBinaryString(negLeftRightBits) + "]";
	}
}
