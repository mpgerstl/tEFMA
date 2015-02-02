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

import ch.javasoft.util.numeric.Zero;

/**
 * A <code>FluxDistribution</code> is a vector with flux values, associating a
 * flux value to each reaction of the metabolic network. Note that different 
 * data types are supported for flux distributions.
 */
public interface FluxDistribution extends Cloneable {
	MetabolicNetwork getNetwork();
	/** Returns the number of reactions or flux values*/
	int getSize();
	/** Returns the rates, not necessarily a clone!*/
	double[] getDoubleRates();
	/** 
	 * Returns the rate as a number, number type is defined by data type of
	 * this flux distribution, i.e. by implementor
	 * 
	 * @param reaction the reaction for which the rate value should be returned
	 * @return the number value, the type depending on the implementor
	 */
	Number getNumberRate(Reaction reaction);
	/** 
	 * Returns the rate as a number, number type is defined by data type of
	 * this flux distribution, i.e. by implementor
	 * 
	 * @param index the reaction index for which the rate value should be returned
	 * @return the number value, the type depending on the implementor
	 */
	Number getNumberRate(int index);
	/** 
	 * Returns the rate signum, i.e. 0/1/-1 for no flux/positive forward flux/
	 * negative backward flux
	 * 
	 * @param index the reaction index for which the rate value should be returned
	 */
	int getRateSignum(int index);

	/**
	 * Set the rate for the specified reaction. Throws an exception if no such
	 * reaction exists in the metabolic network underlying this flux 
	 * distribution.
	 * 
	 * @param reaction	the reaction for which the value should be set
	 * @param rate		the flux value to set
	 * 
	 * @throws IllegalArgumentException if the reaction is not found in the 
	 * 									underlying network
	 */
	void setRate(Reaction reaction, Number rate);
	
	/**
	 * Set the rate for the specified reaction.
	 * 
	 * @param index	the index of the reaction for which the value should be set
	 * @param rate	the flux value to set
	 */
	void setRate(int index, Number rate);
	
	/** 
	 * Returns the number type defined by the implementor. Values returned by
	 * {@link #getNumberRate(int)} return instances of this class.
	 * 
	 * @return 	the class representing the number type which is internally used
	 * 			to represent flux values
	 */	
	Class<? extends Number> getPreferredNumberClass();
	/**
	 * Returns the index at which the flux value will be found for the given
	 * reaction, or -1 if no such reaction belongs to the network of this
	 * flux distribution
	 * 
	 * @param reaction	the reaction for which the flux value index is desired
	 */
	int getReactionIndex(Reaction reaction);
	
	/**
	 * Returns a clone of this flux distribution
	 */
	FluxDistribution clone();
	/**
	 * Creates a new flux distribution with the given network. The number of
	 * reactions, and thus flux values, is taken from the network
	 */
	FluxDistribution create(MetabolicNetwork network);
	
	/** 
	 * Norm the flux vector using the given normalization method. The flux 
	 * values will possibly change as a concequence of calling this method, i.e.
	 * this method affects the state of this flux distribution.
	 * 
	 * @param norm	the normalization to use
	 * @param zero	value to treat as zero, for double stuff
	 */
	void norm(Norm norm, Zero zero);
}
