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

import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.tree.incore.IncoreBitPatternTree;
import ch.javasoft.metabolic.efm.tree.outcore.PersistentBitPatternTree;

/**
 * The <code>BitPatternTree</code> is used to speed up the enumeration of
 * adjacent modes. 
 * <p>
 * Different implementations store the trees 
 * {@link IncoreBitPatternTree in-core} (in memory) or 
 * {@link PersistentBitPatternTree out-of-core} (for instance on disk). The 
 * trees can be used in combination with any adjacency test method using the 
 * concept of {@link TreePairTraverser tree traversers}.
 * <p>
 * See {@link ch.javasoft.metabolic.efm.tree package comments} for more 
 * information and references for the implementation.
 */
public interface BitPatternTree {
	/**
	 * The tree kind, that is, whether the tree stores positive, negative or
	 * zero modes. Here, positive, negative and zero refers to the separating
	 * hyperplane of the current iteration step. 
	 * <p>
	 * Positive modes fulfill the hyperplane inequality with strict inequality, 
	 * zero modes with equality and negative modes do not fulfill the 
	 * inequality. 
	 * <p>
	 * If the nullspace approach is used, a hyperplane corresponds to a 
	 * reaction, and positive, negative and zero can directly be associated with
	 * a flux value for the reaction processed at the current iteration step. 
	 */
	enum Kind {
		Pos {
			@Override
			public PartId toPartId() {
				return PartId.POS;
			}
		}, 
		Zero {
			@Override
			public PartId toPartId() {
				return PartId.ZER;
			}			
		},
		Neg {
			@Override
			public PartId toPartId() {
				return PartId.NEG;
			}			
		};
		public char toChar() {
			return Character.toLowerCase(name().charAt(0));
		}
		abstract public PartId toPartId();
	}
	/**
	 * The kind of the tree
	 */
	Kind kind();
	/**
	 * The bit set size for this tree, that is, the number of bits in the binary 
	 * part of the modes. Determines the node size for persisted nodes.
	 * 
	 * @return 	the bit set size, the length of the bit sets
	 */
	int bitSetSize();
	
	/**
	 * Returns the root node of the tree
	 * 
	 * @throws 	IOException 
	 * 			if a persistence operation (e.g. reading the tree or parts of 
	 * 			the tree from disk) causes an i/o exception
	 */
	Node root() throws IOException;
	
	/**
	 * Closes the tree for the current thread. The files still persist, only
	 * the reading resources for the current thread are closed.
	 * 
	 * @throws IOException	if closing fails
	 */
	void closeForCurrentThread() throws IOException;
	/**
	 * Clears all data and closes the file. Only the owner thread should call
	 * this method. Reading threads should use {@link #closeForCurrentThread()}
	 * instead.
	 * 
	 * @throws IOException	if erasing or closing fails
	 */
	void close() throws IOException;
}
