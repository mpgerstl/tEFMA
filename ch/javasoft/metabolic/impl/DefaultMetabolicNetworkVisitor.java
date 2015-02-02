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
package ch.javasoft.metabolic.impl;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.MetabolicNetworkVisitor;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;

/**
 * Default visitor implementation, which visits all network elements, but does
 * not perform any operation on the elements (other then recursing to visit 
 * nested elements).
 * <p>
 * Subclasses typically override the methods of interest, also performing the
 * super call to ensure that all nested elements are visited.
 */
public class DefaultMetabolicNetworkVisitor implements MetabolicNetworkVisitor {

	public DefaultMetabolicNetworkVisitor() {
		super();
	}

	public void visitMetabolicNetwork(MetabolicNetwork net) {
		for (Metabolite meta : net.getMetabolites()) {
			meta.accept(this);
		}
		for (Reaction react : net.getReactions()) {
			react.accept(this);
		}
	}

	public void visitMetabolite(Metabolite metabolite) {
		// default: empty
	}

	public void visitReaction(Reaction reaction) {
		reaction.getConstraints().accept(this);
		for (MetaboliteRatio ratio : reaction.getMetaboliteRatios()) {
			ratio.accept(this);
		}
	}

	public void visitMetaboliteRatio(MetaboliteRatio ratio) {
		// default: empty
	}

	public void visitReactionConstraints(ReactionConstraints constraints) {
		// default: empty
	}


}
