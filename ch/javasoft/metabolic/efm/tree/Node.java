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

import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;

/**
 * The <code>Node</code> is an element of a {@link BitPatternTree}. Two 
 * different node types exist, reflected by subinterfaces:
 * {@link InterNode intermediary nodes} and {@link LeafNode leaf nodes}
 */
public interface Node {
	
	/**
	 * Returns the union pattern for this node, that is, the union of all zero
	 * sets in the subtree. Zero set is equivalent to the binary part of a 
	 * column or mode.
	 */
	IBitSet unionPattern();
	
	/**
	 * Returns a unique name for this tree node, for instance to use as file
	 * identifier if subtrees are stored in files
	 */
	String getUniqueName(BitPatternTree tree);
	
	/**
	 * Call back to the appropriate method of the given 
	 * {@link TreePairTraverser node traverser}
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param <T>				the traverser token, an instance specific token 
	 * 							passed through the whole traversing process
	 * @param traverser			the tree traverser, the visitor to call back
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 * 
	 */
	<Col extends Column, N extends Number, T> boolean traverse(TreeTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token) throws IOException;

	/**
	 * Call back to the appropriate method of the given 
	 * {@link TreePairTraverser node traverser}
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param <T>				the traverser token, an instance specific token 
	 * 							passed through the whole traversing process
	 * @param traverser			the tree traverser, the visitor to call back
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>this</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param thisIsPos			true if the tree associated with <tt>this</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number, T> void traverseN(TreePairTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean thisIsPos, Node other) throws IOException;
	/**
	 * Call back to the appropriate method of the given 
	 * {@link TreePairTraverser node traverser}
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param <T>				the traverser token, an instance specific token 
	 * 							passed through the whole traversing process
	 * @param traverser			the tree traverser, the visitor to call back
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>this</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param thisIsPos			true if the tree associated with <tt>this</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number, T> void traverseI(TreePairTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean thisIsPos, InterNode other) throws IOException;
	/**
	 * Call back to the appropriate method of the given 
	 * {@link TreePairTraverser node traverser}
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param <T>				the traverser token, an instance specific token 
	 * 							passed through the whole traversing process
	 * @param traverser			the tree traverser, the visitor to call back
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>this</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param thisIsPos			true if the tree associated with <tt>this</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number, T> void traverseL(TreePairTraverser<T> traverser, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean thisIsPos, LeafNode other) throws IOException;
}
