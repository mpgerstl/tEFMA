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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.EfmModel;

abstract public class AbstractTreeFactory<T /*traversing token*/> implements TreeFactory<T> {
	
	protected final EfmModel efmModel;
	
	public AbstractTreeFactory(EfmModel efmModel) {
		this.efmModel = efmModel;
	}

	protected Node<T> createInterNode(SortableMemory<Column> cols, int[] selectiveBits, int prevSelBitIndex, int iStart, int iEnd) throws IOException {
		return new InterNode<T>(this, cols, selectiveBits, prevSelBitIndex, iStart, iEnd);
	}
	
	protected Node<T> createLeaf(SortableMemory<Column> cols, int[] selectiveBits, int prevSelBitIndex, int iStart, int iEnd) throws IOException {
//		return new Leaf<T>(this, cols, iStart, iEnd);
		if (iEnd-iStart == 1) {
			return new UnaryLeaf<T>(this, cols, iStart);
		}
		if (iEnd-iStart == 0) {
			return EmptyLeaf.instance();
		}
		throw new IllegalArgumentException("start=" + iStart + ", end=" + iEnd);
	}

	public Node<T> createNode(final SortableMemory<Column> cols, final int[] selectiveBits, final int prevSelBitIndex, int iStart, int iEnd) throws IOException {
		return (iEnd - iStart <= Leaf.MAX_LEAF_SIZE) ?
			createLeaf(cols, selectiveBits, prevSelBitIndex, iStart, iEnd) :
			createInterNode(cols, selectiveBits, prevSelBitIndex, iStart, iEnd);
	}

}
