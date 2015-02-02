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
package ch.javasoft.metabolic.compartment;

import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.impl.DefaultReaction;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;

public class CompartmentReaction extends DefaultReaction {
	
	private final String mFullName;
	
	public CompartmentReaction(String name, String fullName, Iterable<? extends CompartmentMetaboliteRatio> metaboliteRatios, boolean reversible) {
		super(name, metaboliteRatios, reversible);
		mFullName = fullName;
	}

	public CompartmentReaction(String name, String fullName, CompartmentMetaboliteRatio[] metaboliteRatios,
			boolean reversible) {
		super(name, metaboliteRatios, reversible);
		mFullName = fullName;
	}

	public CompartmentReaction(String name, String fullName,
			ArrayIterable<? extends CompartmentMetaboliteRatio> metaboliteRatios, boolean reversible) {
		super(name, metaboliteRatios, reversible);
		mFullName = fullName;
	}

	public CompartmentReaction(String name, String fullName, CompartmentMetaboliteRatio[] metaboliteRatios,
			ReactionConstraints constraints) {
		super(name, metaboliteRatios, constraints);
		mFullName = fullName;
	}

	public CompartmentReaction(String name, String fullName,
			ArrayIterable<? extends CompartmentMetaboliteRatio> metaboliteRatios,
			ReactionConstraints constraints) {
		super(name, metaboliteRatios, constraints);
		mFullName = fullName;
	}
	
	/**
	 * @return the full name of the reaction, as specified in the constructor
	 */
	@Override
	public String getFullName() {
		return mFullName;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getMetaboliteRatios() {
		return (ArrayIterable<? extends CompartmentMetaboliteRatio>)super.getMetaboliteRatios();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getEductRatios() {
		return (ArrayIterable<? extends CompartmentMetaboliteRatio>)super.getEductRatios();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getProductRatios() {
		return (ArrayIterable<? extends CompartmentMetaboliteRatio>)super.getProductRatios();
	}
	
	public boolean isCompartmentInternal() {
		String cmp = null;
		for (CompartmentMetaboliteRatio ratio : getMetaboliteRatios()) {
			if (cmp == null) cmp = ratio.getMetabolite().getCompartment();
			else {
				if (!cmp.equals(ratio.getMetabolite().getCompartment())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getMetabolieRatiosForCompartment(String compartment) {
		return getMetabolieRatiosForCompartment(compartment, MetaKind.ALL, true);
	}
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getMetabolieRatiosExcludeCompartment(String compartment) {
		return getMetabolieRatiosForCompartment(compartment, MetaKind.ALL, false);
	}
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getEductRatiosForCompartment(String compartment) {
		return getMetabolieRatiosForCompartment(compartment, MetaKind.EDUCTS, true);
	}
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getEductRatiosExcludeCompartment(String compartment) {
		return getMetabolieRatiosForCompartment(compartment, MetaKind.EDUCTS, false);
	}
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getProductRatiosForCompartment(String compartment) {
		return getMetabolieRatiosForCompartment(compartment, MetaKind.PRODUCTS, true);
	}
	public ArrayIterable<? extends CompartmentMetaboliteRatio> getProductRatiosExcludeCompartment(String compartment) {
		return getMetabolieRatiosForCompartment(compartment, MetaKind.PRODUCTS, false);
	}
	private static enum MetaKind{ALL, PRODUCTS, EDUCTS}
	private ArrayIterable<? extends CompartmentMetaboliteRatio> getMetabolieRatiosForCompartment(String compartment, MetaKind kind, boolean include) {
		GenericDynamicArray<CompartmentMetaboliteRatio> ratios = new GenericDynamicArray<CompartmentMetaboliteRatio>();
		Iterable<? extends CompartmentMetaboliteRatio> itRatios;
		switch(kind) {
			case ALL: 
				itRatios = getMetaboliteRatios(); break;
			case PRODUCTS: 
				itRatios = getProductRatios(); break;
			case EDUCTS: 
				itRatios = getEductRatios(); break;
			default:
				throw new IllegalArgumentException("illegal kind: " + kind);
		}
		for (CompartmentMetaboliteRatio ratio : itRatios) {
			boolean fit = compartment.equals(ratio.getMetabolite().getCompartment());
			if (fit == include) ratios.add(ratio);
		}
		return ratios;
	}
}
