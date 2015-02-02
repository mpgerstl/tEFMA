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

import ch.javasoft.metabolic.impl.DefaultMetabolite;

public class CompartmentMetabolite extends DefaultMetabolite {

	private final String mCompartment;
	
	/**
	 * @param index			Metabolite index, to be unique even without compartment
	 * @param compartment	Compartment name
	 */
	public CompartmentMetabolite(int index, String compartment) {
		super(index);
		mCompartment = compartment;
	}

	/**
	 * @param name			Metabolite name, to be unique even without compartment.
	 * 						Usually, the name contains the compartment, e.g. as a
	 * 						postfix to keep metabolites unique if a molecule
	 * 						appears in different compartments.
	 * @param compartment	Compartment name
	 */
	public CompartmentMetabolite(String name, String compartment) {
		super(name);
		mCompartment = compartment;
	}
	/**
	 * @param name			Metabolite name, to be unique even without compartment.
	 * 						Usually, the name contains the compartment, e.g. as a
	 * 						postfix to keep metabolites unique if a molecule
	 * 						appears in different compartments.
	 * @param description	Metabolite desciptive name, not necessarily unique
	 * 						and may also be null 
	 * @param compartment	Compartment name
	 */
	public CompartmentMetabolite(String name, String description, String compartment) {
		super(name, description);
		mCompartment = compartment;
	}
	
	public String getCompartment() {
		return mCompartment;
	}
	
}
