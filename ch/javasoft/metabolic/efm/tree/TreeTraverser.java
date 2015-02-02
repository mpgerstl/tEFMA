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

/**
 * The <code>TreeTraverser</code> traverses a bit set tree. This interface is
 * for instance implemented to perform the <i>combinatorial adjacency test</i>
 * by searching a superset of the tested intersection zero set.
 * <p>
 * The traversion is started with 
 * {@link #traverse(ColumnHome, AdjEnumModel, Object, BitPatternTree) traverse(..)}.
 * 
 * @see Node#traverse(TreeTraverser, ColumnHome, AdjEnumModel, Object) Node.traverse(..)
 * 
 * @type T	the traverser token, an instance specific token passed through the
 * 			whole traversing process
 */
public interface TreeTraverser<T> {
	/**
	 * Start the traversion process for the tree
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param tree				the bit pattern tree to traverse
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 */
	<Col extends Column, N extends Number> void traverse(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, BitPatternTree tree) throws IOException; 

	/**
	 * Visit intermediary tree node during tree traversion. Typically, the two 
	 * subtree recursions are invoked in this method. 
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param node				the intermediary node of the tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 * @return true if the recursion should be continued
	 */
	<Col extends Column, N extends Number> boolean traverseI(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, InterNode node) throws IOException; 
	/**
	 * Visit leaf tree node during tree traversion. 
	 * 
	 * @param <Col>				the column type
	 * @param <N>				the number type
	 * @param columnHome		column home defining column and number type
	 * @param iterationModel	the iteration model, with access to columns and
	 * 							(sources) output for new-born modes from
	 * 							adjacent modes 
	 * @param token				the traverser token
	 * @param node				the leaf node of the tree
	 * @throws IOException	if an i/o exception occurs, for instance due to
	 * 						access of modes stored in files
	 * @return true if the recursion should be continued
	 */
	<Col extends Column, N extends Number> boolean traverseL(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, LeafNode node) throws IOException; 
}
