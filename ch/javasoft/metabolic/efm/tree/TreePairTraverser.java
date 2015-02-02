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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;

/**
 * The <code>TreePairTraverser</code> traverses two bit set trees, that is, this
 * interface is usually implemented to perform the <i>recursive enumeration</i>
 * approach for adjacent modes. 
 * <p>
 * The traverser pattern is actually a version of the visitor pattern. The
 * <code>TreePairTraverser</code> acts as visitor of all possible 
 * {@link Node tree node} combinations of the two trees.
 * <p>
 * The traversion is started with 
 * {@link #traverse(ColumnHome, AdjEnumModel, Object, BitPatternTree, BitPatternTree) traverse(..)}.
 * 
 * @see Node#traverseN(TreePairTraverser, ColumnHome, AdjEnumModel, Object, int, int, boolean, Node) Node.traverseN(..)
 * @see Node#traverseI(TreePairTraverser, ColumnHome, AdjEnumModel, Object, int, int, boolean, InterNode) Node.traverseI(..)
 * @see Node#traverseL(TreePairTraverser, ColumnHome, AdjEnumModel, Object, int, int, boolean, LeafNode) Node.traverseL(..)
 * 
 * @type T	the traverser token, an instance specific token passed through the
 * 			whole traversing process
 */
public interface TreePairTraverser<T> {
	/**
	 * Start the traversion process for the two trees
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param posTree			the bit pattern tree for positive modes
	 * @param negTree			the bit pattern tree for negative modes
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverse(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, BitPatternTree posTree, BitPatternTree negTree) throws IOException; 
	/**
	 * Visit two tree nodes of unknown type during tree traversion. Usually
	 * calls back to 
	 * {@link Node#traverseN(TreePairTraverser, ColumnHome, AdjEnumModel, Object, int, int, boolean, Node) node.traversN(..)}
	 * using any of the nodes.
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>me</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param meIsPos			true if the tree associated with <tt>me</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param me				the node of one tree
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverseNN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) throws IOException; 
	/**
	 * Visit two tree nodes during tree traversion, one node of unknown type, 
	 * the other an intermediary node. Usually calls back to 
	 * {@link Node#traverseI(TreePairTraverser, ColumnHome, AdjEnumModel, Object, int, int, boolean, InterNode) node.traversI(..)}
	 * using the node of unknown type.
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>me</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param meIsPos			true if the tree associated with <tt>me</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param me				the node of one tree
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverseIN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, Node other) throws IOException; 
	/**
	 * Visit two tree nodes during tree traversion, one node of unknown type, 
	 * the other a leaf node. Usually calls back to 
	 * {@link Node#traverseL(TreePairTraverser, ColumnHome, AdjEnumModel, Object, int, int, boolean, LeafNode) node.traversL(..)}
	 * using the node of unknown type.
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>me</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param meIsPos			true if the tree associated with <tt>me</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param me				the node of one tree
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverseLN(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, Node other) throws IOException; 
	/**
	 * Visit two tree nodes during tree traversion, both nodes are intermediary
	 * nodes. Typically, the four recursions are invoked in this method, usually
	 * after testing whether the recursion process should be continued. 
	 * <p>
	 * The recursion process might be aborted since none of the four 
	 * combinations is able to bear adjacent mode combinations. Note that 
	 * implementations might also consider parallelization, for instance by 
	 * executing the recursive invocations in separate threads.
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>me</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param meIsPos			true if the tree associated with <tt>me</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param me				the node of one tree
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverseII(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, InterNode other) throws IOException; 
	/**
	 * Visit two tree nodes during tree traversion, one an intermediary node, 
	 * the other a leaf node. Typically, the two recursions are invoked in this 
	 * method, usually after testing whether the recursion process should be 
	 * continued. 
	 * <p>
	 * The recursion process might be aborted since none of the two 
	 * combinations is able to bear adjacent mode combinations. Note that 
	 * implementations might also consider parallelization, for instance by 
	 * executing the recursive invocations in separate threads.
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>me</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param meIsPos			true if the tree associated with <tt>me</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param me				the node of one tree
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverseIL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, InterNode me, LeafNode other) throws IOException; 
	/**
	 * Visit two tree nodes during tree traversion, both leaf nodes. Here, the
	 * actual pairing of modes is usually performed, that is, every mode of one
	 * leaf node are paired with every mode of the other leaf. The resulting
	 * pairs are tested for adjacency, and if adjacent, a new mode is
	 * {@link Column#mergeWith(ColumnHome, EfmModel, Column, IterationStepModel) created} 
	 * and added to the 
	 * {@link AdjEnumModel#getMemoryForNewFromAdj() output memory}.
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param myDepth			the recursion depth of <tt>me</tt>
	 * @param otherDepth		the recursion depth of <tt>other</tt>
	 * @param meIsPos			true if the tree associated with <tt>me</tt>
	 * 							contains the positive modes, and false otherwise
	 * @param me				the node of one tree
	 * @param other				the node of the other tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverseLL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, LeafNode me, LeafNode other) throws IOException; 
}
