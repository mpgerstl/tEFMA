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
package ch.javasoft.metabolic.efm.util;

import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.concurrent.ConcurrentToken;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.tree.AdjacencyPrecondition;
import ch.javasoft.metabolic.efm.tree.Node;

/**
 * The <code>PreconditionUtil</code> contains static utility methods for 
 * {@link AdjacencyPrecondition}s, e.g. to concatenate two or more existing
 * conditions using a logical operator such as and/or.
 */
public class PreconditionUtil {

	/**
	 * Concatenation of two factors using the logical AND operator
	 */
	public static <T extends ConcurrentToken> AdjacencyPrecondition<T> and(final AdjacencyPrecondition<T> factorA, final AdjacencyPrecondition<T> factorB) {
		return new AdjacencyPrecondition<T>() {

			public <Col extends Column, N extends Number> boolean enterIfMet(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) {
				if (factorA.enterIfMet(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other)) {
					if (factorB.enterIfMet(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other)) {
						return true;
					}
					factorA.leave(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
				}
				return false;
			}

			public <Col extends Column, N extends Number> boolean isMet(ColumnHome<N,Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int posColIndex, int negColIndex) throws IOException {
				return 
					factorA.isMet(columnHome, iterationModel, token, posColIndex, negColIndex) &&
					factorB.isMet(columnHome, iterationModel, token, posColIndex, negColIndex);
			}

			public <Col extends Column, N extends Number> void leave(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> iterationModel, T token, int myDepth, int otherDepth, boolean meIsPos, Node me, Node other) {
				factorB.leave(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
				factorA.leave(columnHome, iterationModel, token, myDepth, otherDepth, meIsPos, me, other);
			}
			
		};
	}
	//no instances
	private PreconditionUtil() {}
}
