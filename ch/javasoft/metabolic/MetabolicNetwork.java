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
package ch.javasoft.metabolic;

import java.util.Map;

import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.genarr.ArrayIterable;

/**
 * The <tt>MetabolicNetwork</tt> is a collection of {@link Reaction reactions}.
 * Some convenience methods allow better searching and iterating over 
 * metabolites (the educts and products of reactions) and reactions.
 */
public interface MetabolicNetwork extends Annotateable {
	/**
	 * Returns the list of all metabolites
	 */
	ArrayIterable<? extends Metabolite> getMetabolites();
	/**
	 * returns the desired metabolite, or throws an IllegalArgumentException
	 * if no metabolite exists with the given name
	 */
	Metabolite getMetabolite(String name);
	/**
	 * returns the index of the given metabolite, or -1 if no metabolite exists 
	 * with the given name
	 */
	int getMetaboliteIndex(String name);
	
	/**
	 * Returns the list of all reactions
	 */
	ArrayIterable<? extends Reaction> getReactions();
	
	/**
	 * returns the reactions in which the given metabolite participates, either
	 * as product or as educt. If this metabolite does not participate in any
	 * reaction, of if it is not a metabolite of this network, an empty
	 * list is returned
	 */
	ArrayIterable<? extends Reaction> getReactions(Metabolite metabolite);
	/**
	 * returns the desired reaction, or throws an IllegalArgumentException
	 * if no reaction exists with the given name
	 */
	Reaction getReaction(String name);
	/**
	 * returns the index of the given reaction, or -1 if no reaction exists with 
	 * the given name
	 */
	int getReactionIndex(String name);
	/** 
	 * visitor accept method, calls 
	 * {@link MetabolicNetworkVisitor#visitMetabolicNetwork(MetabolicNetwork)} of 
	 * <tt>visitor</tt>
	 */
	void accept(MetabolicNetworkVisitor visitor);
	/**
	 * The reaction's {@link Reaction#toString() toString} method results, 
	 * collected in a list (that is, in square brackets, separated by comma)
	 */
	String toString();
	/**
	 * The reaction name, followed by semicolon and the reaction's 
	 * {@link Reaction#toString() toString} method result, collected
	 * in a list (that is, in square brackets, separated by comma)
	 */
	String toStringVerbose();
	
	/**
	 * Convenience method to get reaction reversibilities directly without
	 * looping through all reactions
	 */
	boolean[] getReactionReversibilities();
	
	/**
	 * Convenience method to get metabolite names directly without looping 
	 * through all metabolites
	 */
	String[] getMetaboliteNames();
	
	/**
	 * Convenience method to get reaction names directly without
	 * looping through all reactions
	 */
	String[] getReactionNames();
	
	/**
	 * Returns the stoichiometrix matrix for this network. Depending on the
	 * implementation, the matrix might be a member of the network, or it might
	 * have to be implemented on the fly.
	 */
	ReadableMatrix<?> getStoichiometricMatrix();
	
	/**
	 * Returns the specified annotation for the given element, or null if no
	 * such annotation exists
	 *  
	 * @param element	the element for which an annotation should be returned
	 * @param name		the name of the desired annotation
	 */
	Object getAnnotation(Annotateable element, String name);
	
	/**
	 * Returns annotations for the given element as name/value pairs. If no
	 * annotations exist for the given element, an empty iterable is returned.
	 *  
	 * @param element the element for which annotations should be returned
	 */
	Iterable<Map.Entry<String, Object>> getAnnotations(Annotateable element);
}
