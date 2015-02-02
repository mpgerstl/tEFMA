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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;

public class CompartmentMetabolicNetwork extends DefaultMetabolicNetwork {
	
	private Map<String, String> mCompartments = null;

	public CompartmentMetabolicNetwork(CompartmentMetabolite[] metabolites,
			String[] reactionNames, double[][] stoichMatrix,
			boolean[] reversible) {
		super(metabolites, reactionNames, stoichMatrix, reversible);
	}

	public CompartmentMetabolicNetwork(CompartmentReaction[] reactions) {
		super(reactions);
	}

	public CompartmentMetabolicNetwork(Iterable<? extends CompartmentMetabolite> metabolites,
			Iterable<? extends CompartmentReaction> reactions) {
		super(metabolites, reactions);
	}

	public CompartmentMetabolicNetwork(Metabolite[] metabolites,
			Reaction[] reactions) {
		super(metabolites, reactions);
	}

	public CompartmentMetabolicNetwork(
			GenericDynamicArray<? extends CompartmentMetabolite> metabolites,
			GenericDynamicArray<? extends CompartmentReaction> reactions) {
		super(metabolites, reactions);
	}
	
	@Override
	public CompartmentMetabolite getMetabolite(String name) {
		return (CompartmentMetabolite)super.getMetabolite(name);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayIterable<? extends CompartmentMetabolite> getMetabolites() {
		return (ArrayIterable<? extends CompartmentMetabolite>)super.getMetabolites();
	}
	
	@Override
	public CompartmentReaction getReaction(String name) {
		return (CompartmentReaction)super.getReaction(name);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayIterable<? extends CompartmentReaction> getReactions() {
		return (ArrayIterable<? extends CompartmentReaction>)super.getReactions();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ArrayIterable<? extends CompartmentReaction> getReactions(Metabolite metabolite) {
		return (ArrayIterable<? extends CompartmentReaction>)super.getReactions(metabolite);
	}
	
	public Iterable<String> getCompartmentNames() {
		if (mCompartments == null) {
			mCompartments = new LinkedHashMap<String, String>();
			for (CompartmentMetabolite meta : getMetabolites()) {
				mCompartments.put(meta.getCompartment(), meta.getCompartment());
			}
		}
		return Collections.unmodifiableSet(mCompartments.keySet());
	}
	/**
	 * Returns the (full) compartment name for the given name, or null if no 
	 * such compartment exists. If no specific long name exists, the id itself 
	 * is returned.
	 */
	public String getCompartmentFullName(String name) {
		getCompartmentNames();//ensure it exists
		return mCompartments.get(name);
	}
	/**
	 * Sets the (full) compartment name for the compartment given by its name. 
	 * If no such compartment exists, an exception is thrown, as well as if the 
	 * specified name is null.
	 * 
	 * @throws IllegalArgumentException	if no such compartment exists
	 * @throws NullPointerException		if full name is null
	 */
	public void setCompartmentFullName(String name, String fullName) {
		if (fullName == null) throw new NullPointerException("null name not allowed");
		getCompartmentNames();//ensure it exists
		if (!mCompartments.containsKey(name)) {
			throw new IllegalArgumentException("no such compartment: " + name);
		} 
		mCompartments.put(name, fullName);
	}
	
	public ArrayIterable<? extends CompartmentMetabolite> getMetabolitesForCompartment(String compartment) {
		GenericDynamicArray<CompartmentMetabolite> metas = new GenericDynamicArray<CompartmentMetabolite>();
		for (CompartmentMetabolite meta : getMetabolites()) {
			if (compartment.equals(meta.getCompartment())) metas.add(meta);
		}
		return metas;
	}

}
