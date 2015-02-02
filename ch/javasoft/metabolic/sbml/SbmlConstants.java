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
package ch.javasoft.metabolic.sbml;

/**
 * Tag and attribute constants for an sbml file.<br/>
 * </br>
 * An sbml file looks like this:
 * 
 * <pre>
 * <?xml version="1.0" encoding="UTF-8"?>
 * <sbml xmlns="http://www.sbml.org/sbml/level2" level="2" version="1">
 *   <model name="S. cerevisiae compartmented">
 *     <listOfCompartments>
 *       <compartment id="external" name="external" size="0.0" />
 *       <compartment id="Cytosol" name="Cytosol" size="0.0" />
 *       <compartment id="Nucleus" name="Nucleus" size="0.0" />
 *       <compartment id="Mitochondria" name="Mitochondria" size="0.0" />
 *     </listOfCompartments>
 *     <listOfSpecies>
 *       <species id="g6p_Cytosol" name="g6p" compartment="Cytosol" initialConcentration="1.0" />
 *       <species id="adp_Cytosol" name="adp" compartment="Cytosol" initialConcentration="1.0" />
 *     </listOfSpecies>
 *     <listOfReactions>
 *       <reaction id="hexose kinases" name="ATP:D-hexose 6-phosphotransferase, ATP:D-glucose 6-phosphotransferase (Glk1)" reversible="false">
 *         <listOfReactants>
 *           <speciesReference species="glc_Cytosol" stoichiometry="1.0" />
 *           <speciesReference species="atp_Cytosol" stoichiometry="1.0" />
 *         </listOfReactants>
 *         <listOfProducts>
 *           <speciesReference species="g6p_Cytosol" stoichiometry="1.0" />
 *           <speciesReference species="adp_Cytosol" stoichiometry="1.0" />
 *           <speciesReference species="h_Cytosol" stoichiometry="1.0" />
 *         </listOfProducts>
 *       </reaction>
 *     </listOfReactions>
 *   </model>
 * </sbml>
 * </pre>
 */
public interface SbmlConstants {
	
	String ELEMENT_ROOT						= "sbml";
	String ELEMENT_MODEL					= "model";
	String ELEMENT_COMPARTMENTS				= "listOfCompartments";
	String ELEMENT_COMPARTMENT				= "compartment";
	String ELEMENT_METABOLITES				= "listOfSpecies";
	String ELEMENT_METABOLITE				= "species";
	String ELEMENT_REACTIONS				= "listOfReactions";
	String ELEMENT_REACTION					= "reaction";
	String ELEMENT_EDUCTS					= "listOfReactants";
	String ELEMENT_EDUCT					= "speciesReference";
	String ELEMENT_PRODUCTS					= "listOfProducts";
	String ELEMENT_PRODUCT					= "speciesReference";
	String ELEMENT_NOTES					= "notes";
	String ELEMENT_HTML_P					= "html:p";
	
	String ATTRIBUTE_NAME					= "name";
	String ATTRIBUTE_ID						= "id";
	String ATTRIBUTE_COMPARTMENT			= "compartment";
	String ATTRIBUTE_REVERSIBLE				= "reversible";
	String ATTRIBUTE_SPECIES				= "species";
	String ATTRIBUTE_STOICHIOMETRY			= "stoichiometry";
	String ATTRIBUTE_SIZE					= "size";
	String ATTRIBUTE_INITIAL_CONCENTRATION	= "initialConcentration";

}
