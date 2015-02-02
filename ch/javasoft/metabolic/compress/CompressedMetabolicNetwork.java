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
package ch.javasoft.metabolic.compress;

import java.util.List;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.util.map.IntIntMultiValueMap;

/**
 * An extension of metabolic network supporting uncompression of flux modes
 * associated with this compressed network.
 */
public interface CompressedMetabolicNetwork extends MetabolicNetwork {
	/** The top level uncompressed network*/
	MetabolicNetwork getRootNetwork();
	/** The next upper level uncompressed or compressed network*/
	MetabolicNetwork getParentNetwork();
	/** returns the uncompressed flux distributions, does not expand duplicate genes*/
	FluxDistribution uncompressFluxDistribution(FluxDistribution fluxDistribution);

	/** 
	 * Returns the mapped possibly compressed/composite metabolites in the
	 * same order as in the input list. The output list contains null if the
	 * respective metabolite has been removed (dead end). 
	 * 
	 * @param original	The list of metabolites from the original uncompressed
	 * 					network.
	 * @return			The mapped metabolites in the same order as in 
	 * 					<tt>original</tt>. They are equal as the original if the
	 * 					metabolite has not been removed nor compressed. They are
	 * 					<tt>null</tt> if they have been removed, and some new 
	 * 					instance of a composite metabolite otherwise.
	 */
	List<Metabolite> getMappedMetabolites(List<Metabolite> original);
	/** 
	 * Returns the mapped possibly compressed/composite reactions in the
	 * same order as in the input list. The output list contains null if the
	 * respective reaction has been removed (zero flux reaction). 
	 * 
	 * @param original	The list of reactions from the original uncompressed
	 * 					network.
	 * @return			The mapped reactions in the same order as in 
	 * 					<tt>original</tt>. They are equal as the original if the
	 * 					reaction has not been removed nor compressed. They are
	 * 					<tt>null</tt> if they have been removed, and some new 
	 * 					instance otherwise.
	 */
	List<Reaction> getMappedReactions(List<Reaction> original);

	/**
	 * Returns the reaction mapping as a one to many map. The key in the map is
	 * the original reaction index, the values are the new reaction indices in
	 * the compressed network. One reaction might be mapped to none, one or 
	 * multiple reactions, and a mapped reaction might consist of one to many
	 * original ones.
	 *  
	 * @return	the mapping of original to compressed reactions
	 */
	IntIntMultiValueMap getReactionMapping();
}
