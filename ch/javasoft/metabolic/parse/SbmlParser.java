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
package ch.javasoft.metabolic.parse;

import static ch.javasoft.metabolic.sbml.SbmlConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.XMLReader;

import ch.javasoft.metabolic.Annotateable;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compartment.CompartmentMetabolicNetwork;
import ch.javasoft.metabolic.compartment.CompartmentMetabolite;
import ch.javasoft.metabolic.compartment.CompartmentMetaboliteRatio;
import ch.javasoft.metabolic.compartment.CompartmentReaction;
import ch.javasoft.util.genarr.ArrayIterable;

/**
 * Parses an sbml file.
 */
/*
<?xml version="1.0" encoding="UTF-8"?>
<sbml xmlns="http://www.sbml.org/sbml/level2" level="2" version="1">
  <model name="S. cerevisiae compartmented">
    <listOfCompartments>
      <compartment id="external" name="external" size="0.0" />
      <compartment id="Cytosol" name="Cytosol" size="0.0" />
      <compartment id="Nucleus" name="Nucleus" size="0.0" />
      <compartment id="Mitochondria" name="Mitochondria" size="0.0" />
    </listOfCompartments>
    <listOfSpecies>
      <species id="g6p_Cytosol" name="g6p" compartment="Cytosol" initialConcentration="1.0" />
      <species id="adp_Cytosol" name="adp" compartment="Cytosol" initialConcentration="1.0" />
    </listOfSpecies>
    <listOfReactions>
      <reaction id="hexose kinases" name="ATP:D-hexose 6-phosphotransferase, ATP:D-glucose 6-phosphotransferase (Glk1)" reversible="false">
        <listOfReactants>
          <speciesReference species="glc_Cytosol" stoichiometry="1.0" />
          <speciesReference species="atp_Cytosol" stoichiometry="1.0" />
        </listOfReactants>
        <listOfProducts>
          <speciesReference species="g6p_Cytosol" stoichiometry="1.0" />
          <speciesReference species="adp_Cytosol" stoichiometry="1.0" />
          <speciesReference species="h_Cytosol" stoichiometry="1.0" />
        </listOfProducts>
      </reaction>
    </listOfReactions>
  </model>
</sbml>
 */
public class SbmlParser {

	public static final String JAXP_SCHEMA_LANGUAGE	= "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	public static final String JAXP_SCHEMA_SOURCE	= "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	public static final String W3C_XML_SCHEMA 		= "http://www.w3.org/2001/XMLSchema"; 
//	public static final String SBML_SCHEMA2_FILE	= "/local/eth/sbml/sbml.xsd";
	public static final String SBML_SCHEMA2_URL		= "http://www.sbml.org/sbml/level2";

	
	public final String		compartmentName;
	public final boolean	compartmentOnly;
	public final boolean	validateSbml;
	
	public SbmlParser() {
		this("external", false, true);
	}
	public SbmlParser(String externalCompartmentName) {
		this(externalCompartmentName, false, true);
	}
	public SbmlParser(String externalCompartmentName, boolean validateSbmlSchema) {
		this(externalCompartmentName, false, validateSbmlSchema);
	}
	public SbmlParser(String externalCompartmentName, boolean parseCompartmentOnly, boolean validateSbmlSchema) {
		compartmentName	= externalCompartmentName;
		compartmentOnly	= parseCompartmentOnly;
		validateSbml	= validateSbmlSchema;
	}
	
	public MetabolicNetwork parse(File file) throws DocumentException, IOException, FileNotFoundException {
		return parse(createSAXReader(validateSbml).read(file));
	}
	public MetabolicNetwork parse(InputStream in) throws DocumentException, IOException {
		return parse(createSAXReader(validateSbml).read(in));
	}
	public MetabolicNetwork parse(URL url) throws DocumentException, IOException {
		return parse(createSAXReader(validateSbml).read(url));
	}
	public MetabolicNetwork parse(File file, String modelName) throws DocumentException, IOException, FileNotFoundException {
		return parse(createSAXReader(validateSbml).read(file), modelName);
	}
	public MetabolicNetwork parse(InputStream in, String modelName) throws DocumentException, IOException {
		return parse(createSAXReader(validateSbml).read(in), modelName);
	}
	public MetabolicNetwork parse(URL url, String modelName) throws DocumentException, IOException {
		return parse(createSAXReader(validateSbml).read(url), modelName);
	}
	public MetabolicNetwork parse(Document sbml) throws DocumentException {
		return parse(sbml, null);
	}
	protected SAXReader createSAXReader(final boolean validate) {
		if (!validate) return new SAXReader(validate);
		return new SAXReader(validate) {
			@Override
			protected XMLReader createXMLReader() throws org.xml.sax.SAXException {
				XMLReader reader = super.createXMLReader();
				reader.setFeature("http://apache.org/xml/features/validation/schema", true);
				try {
					reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", W3C_XML_SCHEMA);
					reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", new URL(SBML_SCHEMA2_URL).openStream());
				}
				catch (Exception ex) {
					ex.printStackTrace();					
				}		        	
				return reader;
			}
		};
	}
	public MetabolicNetwork parse(Document sbml, String modelName) throws DocumentException {
		Element root	= sbml.getRootElement();
		Element model;
		if (modelName == null) {
			model = root.element(ELEMENT_MODEL);
		}
		else {
			Node node = root.selectSingleNode("model[@name=\"" + modelName + "\"]");
			if (node instanceof Element) {
				model = (Element)node;
			}
			else {
				throw new IllegalArgumentException("model not found: " + modelName);
			}
		}
		Map<Annotateable, Map<String, String>>	annotations	= new LinkedHashMap<Annotateable, Map<String,String>>();
		Map<String, CompartmentMetabolite>		metas		= parseMetabolites(model.element(ELEMENT_METABOLITES), annotations);
		Set<CompartmentReaction>				reacts		= parseReactions(model.element(ELEMENT_REACTIONS), metas, annotations);		
		return createNetwork(model, metas, reacts, annotations);
	}
	
	private Map<String, CompartmentMetabolite> parseMetabolites(Element elMetas, Map<Annotateable, Map<String, String>> annotations) throws DocumentException {
		Map<String, CompartmentMetabolite> metas = new LinkedHashMap<String, CompartmentMetabolite>();
		Iterator it = elMetas.elementIterator(ELEMENT_METABOLITE);
		while (it.hasNext()) {
			Element el = (Element)it.next();
			String metaId		= el.attributeValue(ATTRIBUTE_ID);
			String metaName		= el.attributeValue(ATTRIBUTE_NAME);
			String compartment	= el.attributeValue(ATTRIBUTE_COMPARTMENT);
			CompartmentMetabolite meta	= metaName == null ?
				new CompartmentMetabolite(metaId, compartment) :
				new CompartmentMetabolite(metaId, metaName, compartment);
					
			if (metas.put(metaId, meta) != null) {
				throw new DocumentException("duplicate metabolite: " + metaId);
			}
			parseAnnotations(el, meta, annotations);
		}
		return metas;
	}
	private static Set<CompartmentReaction> parseReactions(Element elReacts, Map<String, CompartmentMetabolite> metas, Map<Annotateable, Map<String, String>> annotations) throws DocumentException {
		final Set<CompartmentReaction> reacts = new LinkedHashSet<CompartmentReaction>();
		final Iterator<Element> it = elementIterator(elReacts, ELEMENT_REACTION);
		while (it.hasNext()) {
			boolean any = false;
			final Element el 	= it.next();
			String reactId		= el.attributeValue(ATTRIBUTE_ID);			
			String reactName	= el.attributeValue(ATTRIBUTE_NAME);
			boolean reversible	= !Boolean.FALSE.toString().equalsIgnoreCase(el.attributeValue(ATTRIBUTE_REVERSIBLE));//default is true
			final List<CompartmentMetaboliteRatio> ratios = new ArrayList<CompartmentMetaboliteRatio>();
			final Iterator<Element> eduIt = elementIterator(el.element(ELEMENT_EDUCTS), ELEMENT_EDUCT);
			any |= eduIt.hasNext();
			while (eduIt.hasNext()) {
				Element edu = (Element)eduIt.next();
				CompartmentMetaboliteRatio ratio = parseMetaboliteRatio(el, edu, metas, true /*educt*/);
				ratios.add(ratio);
			}
			final Iterator<Element> proIt = elementIterator(el.element(ELEMENT_PRODUCTS), ELEMENT_PRODUCT);
			any |= proIt.hasNext();
			while (proIt.hasNext()) {
				final Element pro = proIt.next();
				CompartmentMetaboliteRatio ratio = parseMetaboliteRatio(el, pro, metas, false /*educt*/);
				ratios.add(ratio);
			}
			if (!any) {
				throw new DocumentException("reaction has neither educts nor products: " + reactId + "/" + reactName);
			}
			final CompartmentReaction reac = new CompartmentReaction(reactId, reactName, ratios, reversible); 
			if (!reacts.add(reac)) {
				throw new DocumentException("duplicate reaction: " + reactId);
			}
			parseAnnotations(el, reac, annotations);
		}
		return reacts;		
	}
	
	private static CompartmentMetaboliteRatio parseMetaboliteRatio(Element elReact, Element elRatio, Map<String, CompartmentMetabolite> metas, boolean educt) throws DocumentException {
		String metaId 	= elRatio.attributeValue(ATTRIBUTE_SPECIES);
		String sStoich	= elRatio.attributeValue(ATTRIBUTE_STOICHIOMETRY);
		double stoich 	= sStoich == null ? 1d : Double.parseDouble(sStoich);
		CompartmentMetabolite meta = metas.get(metaId);
		if (meta == null) {
			throw new DocumentException(
				"metabolite '" + metaId + "' not found for reaction: " + 
				elReact.attributeValue(ATTRIBUTE_ID)
			);				
		}
		return new CompartmentMetaboliteRatio(meta, educt ? -stoich : stoich);
	}
	
	private static void parseAnnotations(Element element, Annotateable annotateable, Map<Annotateable, Map<String, String>> annotations) throws DocumentException {
		Iterator noteIt = element.elementIterator(ELEMENT_NOTES);
		while (noteIt.hasNext()) {
			Element notes = (Element)noteIt.next();
			Iterator htmlpIt = notes.elementIterator(ELEMENT_HTML_P);
			while (htmlpIt.hasNext()) {
				Element note	= (Element)htmlpIt.next();
				String txt		= note.getText();
				String[] keyVal	= txt.trim().split(":");
				if (keyVal.length != 2) {
					throw new DocumentException("invalid annotation format: " + txt);	
				}
				String key = keyVal[0].trim();
				String val = keyVal[1].trim();
				getAnnotationMap(annotations, annotateable, true).put(key, val);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private CompartmentMetabolicNetwork createNetwork(Element model, Map<String, CompartmentMetabolite> metas, Set<CompartmentReaction> reactions, Map<Annotateable, Map<String, String>> annotations) throws DocumentException {
		final CompartmentMetabolicNetwork net;
		if (compartmentOnly) {
			//only a network for the desired compartment, everything else is external
			Set<CompartmentMetabolite>	cMetas	= new LinkedHashSet<CompartmentMetabolite>();
			Set<CompartmentReaction>	cReacts	= new LinkedHashSet<CompartmentReaction>();
			for (CompartmentMetabolite meta : metas.values()) {
				if (compartmentName.equals(meta.getCompartment())) {
					if (!cMetas.add(meta)) {
						throw new DocumentException("duplicate metabolite: " + meta.getName());
					}
				}
			}
			for (CompartmentReaction react : reactions) {
				ArrayIterable<? extends CompartmentMetaboliteRatio> ratios = react.getMetabolieRatiosForCompartment(compartmentName);
				if (!ratios.isEmpty()) {
					List<CompartmentMetaboliteRatio> newRatios = new ArrayList<CompartmentMetaboliteRatio>(ratios.toGenericArray(false));
					if (!react.getEductRatiosExcludeCompartment(compartmentName).isEmpty()) {
						//we have also educts from other compartments, which makes this
						//reaction being an external one						
						CompartmentMetabolite		cMeta	= new CompartmentMetabolite(getExchangeMetaboliteName(react, false /*product*/), compartmentName);
						CompartmentMetaboliteRatio	ratio	= new CompartmentMetaboliteRatio(cMeta, -1d);
						CompartmentReaction 		xReact	= createExchangeReaction(cMeta, true /*uptake*/, react.getConstraints().isReversible());
						newRatios.add(ratio);
						if (!cMetas.add(cMeta)) {
							throw new DocumentException("duplicate metabolite: " + cMeta.getName());
						}
						if (!cReacts.add(xReact)) {
							throw new DocumentException("duplicate reaction: " + xReact.getName());							
						}
					}
					if (!react.getProductRatiosExcludeCompartment(compartmentName).isEmpty()) {
						CompartmentMetabolite		cMeta	= new CompartmentMetabolite(getExchangeMetaboliteName(react, true /*product*/), compartmentName);
						CompartmentMetaboliteRatio	ratio	= new CompartmentMetaboliteRatio(cMeta, +1d);
						CompartmentReaction 		xReact	= createExchangeReaction(cMeta, false /*uptake*/, react.getConstraints().isReversible());
						newRatios.add(ratio);
						if (!cMetas.add(cMeta)) {
							throw new DocumentException("duplicate metabolite: " + cMeta.getName());
						}
						if (!cReacts.add(xReact)) {
							throw new DocumentException("duplicate reaction: " + xReact.getName());							
						}
					}
					CompartmentReaction cReact = new CompartmentReaction(react.getName(), react.getFullName(), newRatios, react.getConstraints().isReversible());
					if (!cReacts.add(cReact)) {
						throw new DocumentException("duplicate reaction: " + cReact.getName());							
					}
				}
			}
			net = new CompartmentMetabolicNetwork(cMetas, cReacts);
		}
		else {
			//add the exchange reactions for the external compartment metabolites
			for (CompartmentMetabolite meta : metas.values()) {
				if (compartmentName.equals(meta.getCompartment())) {
					CompartmentReaction react = createExchangeReaction(meta, false /*uptake*/, true /*reversible*/);
					if (!reactions.add(react)) {
						throw new DocumentException("duplicate reaction: " + react.getName());
					}					
				}
			}
			net = new CompartmentMetabolicNetwork(metas.values(), reactions);
		}
		
		//set compartment full names
		final Element elCmps = model.element(ELEMENT_COMPARTMENTS);
		for (Element elCmp : (List<Element>)elCmps.elements(ELEMENT_COMPARTMENT)) {
			final String cmpName 		= elCmp.attributeValue(ATTRIBUTE_ID);
			final String cmpFullName	= elCmp.attributeValue(ATTRIBUTE_NAME);
			if (cmpFullName != null && !cmpFullName.equals(cmpName)) {
				net.setCompartmentFullName(cmpName, cmpFullName);
			}
		}	
	
		//add the annotations
		//a) for the network
		parseAnnotations(model, net, annotations);
		//b) for the elements
		for (Annotateable elem : annotations.keySet()) {
			for (Map.Entry<String, String> annot : annotations.get(elem).entrySet()) {
				net.addAnnotation(elem, annot.getKey(), annot.getValue());
			}
		}
		return net;
	}
	
	private static CompartmentReaction createExchangeReaction(CompartmentMetabolite meta, boolean uptake, boolean reversible) {
		CompartmentMetaboliteRatio ratio = new CompartmentMetaboliteRatio(meta, uptake ? 1d : -1d);
		String reactName = getExchangeReactionName(meta);
		String reactFullName = getExchangeReactionFullName(meta);
		return new CompartmentReaction(reactName, reactFullName, new CompartmentMetaboliteRatio[] {ratio}, reversible);		
	}
	
	private static String getExchangeReactionName(Metabolite meta) {
		return "xchg_" + meta.getName();
	}
	private static String getExchangeReactionFullName(Metabolite meta) {
		return "exchange reaction for " + meta.getName();
	}
	private static String getExchangeMetaboliteName(Reaction reac, boolean product) {
		return (product ? "xpro_" : "xedu_") + reac.getName();
	}
	
	private static Map<String, String> getAnnotationMap(Map<Annotateable, Map<String, String>> annotations, Annotateable element, boolean createIfNeeded) {
		Map<String, String> elMap = annotations.get(element);
		if (elMap == null && createIfNeeded) {
			elMap = new LinkedHashMap<String, String>();
			annotations.put(element, elMap);
		}
		return elMap;
	}
	
	@SuppressWarnings("unchecked")
	private static Iterator<Element> elementIterator(Element parent, String childName) {
		return parent == null ? Collections.emptyList().iterator() : parent.elementIterator(childName);
	}
	
}
