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

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.tree.impl.AbstractInterNode;

/**
 * The <code>PersistentInterNode</code> is an intermediary node of the tree, also
 * stored in the tree file. The left and right child tree are soft references,
 * that means that they are cleared in memory if the garbage collector demands
 * so. However, when needed the next time, left/right child tree can be 
 * reconstructed on the fly from the tree file. 
 */
public class PersistentInterNode extends AbstractInterNode implements PersistentNode {

	//non-volatile state
	private final PersistentBitPatternTree tree;
	public final int tableIndexLeft, tableIndexRight;
	
	//reduntant volatile state
	private Reference<PersistentNode> left, right;
	
	public PersistentInterNode(PersistentBitPatternTree tree, IBitSet unionPattern, int tableIndexLeft, int tableIndexRight, PersistentNode leftNode, PersistentNode rightNode) {
		super(unionPattern);
		this.tree				= tree;
		this.tableIndexLeft		= tableIndexLeft;
		this.tableIndexRight	= tableIndexRight;
		this.left				= new WeakReference<PersistentNode>(leftNode);
		this.right				= new WeakReference<PersistentNode>(rightNode);
		//test:
//		this.left				= new SoftReference<AbstractPersistedNode>(null);
//		this.right				= new SoftReference<AbstractPersistedNode>(null);

//		tree.nodeInc();//for profiling
	}

	public PersistentNode left() {
		PersistentNode node = left.get();
		if (node == null) {
			node = loadEntity(tableIndexLeft).toNode(tree, null, null);
			left = new SoftReference<PersistentNode>(node);
		}
		return node;
	}

	public PersistentNode right() {
		PersistentNode node = right.get();
		if (node == null) {
			node = loadEntity(tableIndexRight).toNode(tree, null, null);
			right = new SoftReference<PersistentNode>(node);
		}
		return node;
	}
	
	protected PersistentNodeEntity loadEntity(int tableIndex) {
		try {
			return tree.getEntity(tableIndex);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//for profiling:
//	@Override
//	protected void finalize() throws Throwable {
//		if (tree != null) {
//			tree.nodeDec();
//			tree = null;
//		}
//	}

}
