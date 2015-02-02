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

import ch.javasoft.util.genarr.ArrayIterable;

/**
 * A reaction in a metabolic network consumes educt metabolites and generates
 * product metabolites. A reaction is typically associated with a column of the
 * stoichiometric matrix.
 */
public interface Reaction extends Annotateable {
	/**
	 * Returns the name of the reaction, unique throughout the the 
	 * {@link MetabolicNetwork metabolic network}
	 */
	String getName();
	/**
	 * Returns the full name of the reaction, e.g. with additional compartment
	 * information
	 */
	String getFullName();
	/**
	 * Returns all {@link MetaboliteRatio ratios} associated with product 
	 * metabolites, that is, the ratio values are negative
	 */
	ArrayIterable<? extends MetaboliteRatio> getEductRatios();
	/**
	 * Returns all {@link MetaboliteRatio ratios} associated with educt and
	 * metabolites, that is, the ratio values are positive
	 */
	ArrayIterable<? extends MetaboliteRatio> getProductRatios();
	/**
	 * Returns all {@link MetaboliteRatio ratios} associated with educt and
	 * product metabolites; ratios are negative for educts and positive for
	 * products
	 */
	ArrayIterable<? extends MetaboliteRatio> getMetaboliteRatios();//educts + products	
	/**
	 * Returns the reaction constraints, which also defines the reversiblilty of 
	 * the reaction
	 */
	ReactionConstraints getConstraints();
	/**
	 * @return	<ul><li>{@code == 0} if the given metabolite does not participate in the reaction</li>
	 * 			    <li>{@code  > 0} if it participates and is produced</li>
	 * 			    <li>{@code  < 0} if it participates and is consumed</li></ul>
	 */
	double getRatioValueForMetabolite(Metabolite metabolite);
	/** 
	 * @return true if {@link #getRatioValueForMetabolite(Metabolite)} returns not 0
	 */
	boolean isMetaboliteParticipating(Metabolite metabolite);	
	/** 
	 * @return	true if {@link #getRatioValueForMetabolite(Metabolite)} returns a 
	 * 			{@code value > 0}, which means that the given metabolite is a product
	 * 			of this reaction. Reversability is not considered.
	 */
	boolean isMetaboliteProduced(Metabolite metabolite);
	/** 
	 * @return	true if {@link #getRatioValueForMetabolite(Metabolite)} returns a 
	 * 			{@code value < 0}, which means that the given metabolite is an educt
	 * 			of this reaction. Reversability is not considered.
	 */
	boolean isMetaboliteConsumed(Metabolite metabolite);	
	/** 
	 * @return	true if this reaction is either an uptake or an extract reaction,
	 * 			which means that it has either no educts or no products.
	 * @see		#isUptake()
	 * @see		#isExtract()
	 */
	boolean isExternal();
	/** 
	 * @return	true if this reaction has no educts
	 * @see		#isExtract()
	 * @see		#isExternal()
	 */
	boolean isUptake();
	/** 
	 * @return	true if this reaction has no products
	 * @see		#isUptake()
	 * @see		#isExternal()
	 */
	boolean isExtract();
	/**
	 * Returns true if all stoichiometric coefficients are integers, and false
	 * otherwise
	 * 
	 * @return	true if all ratios are integers
	 */
	boolean hasIntegerRatios();
	/**
	 * Accept method for the visitor, implementations usually delegate back to 
	 * {@link MetabolicNetworkVisitor#visitReaction(Reaction)}
	 * 
	 * @param visitor the visitor to callback
	 */
	void accept(MetabolicNetworkVisitor visitor);
	/**
	 * Returns string representation of the formula for this reaction.
	 * 
	 * @return	the reaction formula string
	 */
	String toString();
}
