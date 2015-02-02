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

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.tree.InterNode;
import ch.javasoft.metabolic.efm.tree.LeafNode;
import ch.javasoft.metabolic.efm.tree.Node;
import ch.javasoft.metabolic.efm.tree.TreePairTraverser;
import ch.javasoft.metabolic.efm.tree.TreeTraverser;

/**
 * The <code>AbstractInterNode</code> is an abstract intermediary node, 
 * implementing functionality which is common to in-core and persistent 
 * subclasses. 
 */
abstract public class AbstractInterNode extends AbstractNode implements InterNode {

	public AbstractInterNode(IBitSet unionPattern) {
		super(unionPattern);
	}
	
	public <Col extends Column, N extends Number, T> boolean traverse(TreeTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token) throws IOException {
		return traverser.traverseI(columnHome, iterationModel, token, this);
	}
	public <Col extends Column, N extends Number, T> void traverseN(TreePairTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean thisIsPos, Node other) throws IOException {
		traverser.traverseIN(columnHome, iterationModel, token, myDepth, otherDepth, thisIsPos, this, other);
	}
	public <Col extends Column, N extends Number, T> void traverseI(TreePairTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean thisIsPos, InterNode other) throws IOException {
		traverser.traverseII(columnHome, iterationModel, token, myDepth, otherDepth, thisIsPos, this, other);
	}
	public <Col extends Column, N extends Number, T> void traverseL(TreePairTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean thisIsPos, LeafNode other) throws IOException {
		traverser.traverseIL(columnHome, iterationModel, token, myDepth, otherDepth, thisIsPos, this, other);
	}

}
