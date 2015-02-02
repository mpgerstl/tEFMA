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
package ch.javasoft.metabolic.generate;

import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_COMPARTMENT;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_ID;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_INITIAL_CONCENTRATION;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_NAME;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_REVERSIBLE;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_SIZE;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_SPECIES;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ATTRIBUTE_STOICHIOMETRY;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_COMPARTMENT;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_COMPARTMENTS;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_EDUCT;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_EDUCTS;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_METABOLITE;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_METABOLITES;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_MODEL;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_PRODUCT;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_PRODUCTS;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_REACTION;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_REACTIONS;
import static ch.javasoft.metabolic.sbml.SbmlConstants.ELEMENT_ROOT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compartment.CompartmentMetabolicNetwork;
import ch.javasoft.metabolic.compartment.CompartmentMetabolite;

/**
 * Generates an sbml file.
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
public class SbmlGenerator {
	
	private static final Logger LOG = LogPkg.LOGGER;

	private final MetabolicNetwork		mNet;
	private final String				mModelName;
	private final CompartmentHandler	mCompartmentHandler;
	
	public interface CompartmentHandler {
		Iterable<String> compartments();
		String compartmentForMetabolite(Metabolite meta);
		CompartmentMetabolite externalReactionMetabolite(Reaction reac);
		boolean exportMetabolite(Metabolite meta);
		boolean exportReaction(Reaction reac);
	}
	
	public static final CompartmentHandler DEFAULT_COMPARTMENT_HANDLER = new DefaultCompartmentHandler();
	
	public static class DefaultCompartmentHandler implements CompartmentHandler {
		public static final String				DEFAULT_COMPARTMENT		= "default";
		public static final String				EXTERNAL_COMPARTMENT	= "external";
		public static final String				EXTERNAL_METABOLITE_PRE	= "external_";
		
		protected final String mDefaultCompartmentName;
		protected final String mExternalCompartmentName;
		private final Iterable<String> mCompartments;
		public DefaultCompartmentHandler() {
			this(DEFAULT_COMPARTMENT, EXTERNAL_COMPARTMENT);
		}
		public DefaultCompartmentHandler(String defaultCompartmentName, String externalCompartmentName) {
			mDefaultCompartmentName		= defaultCompartmentName;
			mExternalCompartmentName	= externalCompartmentName;
			mCompartments = Arrays.asList(new String[] {defaultCompartmentName, externalCompartmentName});
		}
		public Iterable<String> compartments() {
			return mCompartments;
		}
		public String compartmentForMetabolite(Metabolite meta) {
			return mDefaultCompartmentName;
		}
		public CompartmentMetabolite externalReactionMetabolite(Reaction reac) {
			final String name;
			if (reac.isUptake() && reac.getProductRatios().length() == 1) {
				name = reac.getProductRatios().get(0).getMetabolite().getName();
			}
			else if (reac.isExtract() && reac.getEductRatios().length() == 1) {
				name = reac.getEductRatios().get(0).getMetabolite().getName();
			}
			else {
				name = reac.getFullName();
			}
			return externalReactionMetabolite(name);
		}
		protected CompartmentMetabolite externalReactionMetabolite(String name) {
			return new CompartmentMetabolite(EXTERNAL_METABOLITE_PRE + name, mExternalCompartmentName);			
		}
		public boolean exportMetabolite(Metabolite meta) {
			return true;
		}
		public boolean exportReaction(Reaction reac) {
			return true;
		}		
	}
	
	private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.0###################");
	
	public static class CompartmentMetabolicNetworkHandler implements CompartmentHandler {
		private final CompartmentMetabolicNetwork net;
		public CompartmentMetabolicNetworkHandler(CompartmentMetabolicNetwork cmpNet) {
			net = cmpNet;
		}
		public Iterable<String> compartments() {
			return net.getCompartmentNames();
		}
		public String compartmentForMetabolite(Metabolite meta) {
			return net.getMetabolite(meta.getName()).getCompartment();
		}
		public CompartmentMetabolite externalReactionMetabolite(Reaction reac) {
//			CompartmentMetabolite meta = DEFAULT_COMPARTMENT_HANDLER.externalReactionMetabolite(reac);
//			for (String cmp : net.getCompartments()) {
//				if (cmp.equals(meta.getCompartment())) return meta;
//			}
//			throw new RuntimeException(
//				"Please specify an individual compartment handler, since default external compartment '" + 
//				DefaultCompartmentHandler.EXTERNAL_COMPARTMENT + "' is not a compartment of this network, " +
//				"i.e. it is not in " + net.getCompartments()
//			);
			throw new RuntimeException("please specify an individual compartment handler");
		}
		public boolean exportMetabolite(Metabolite meta) {
			return true;
		}
		public boolean exportReaction(Reaction reac) {
			return !reac.isExternal();
		}
	}
	
	public SbmlGenerator(MetabolicNetwork net, String modelName) {
		this(
			net, modelName, 
			net instanceof CompartmentMetabolicNetwork ? 
				new CompartmentMetabolicNetworkHandler((CompartmentMetabolicNetwork)net) :
				DEFAULT_COMPARTMENT_HANDLER
		);
	}
	public SbmlGenerator(MetabolicNetwork net, String modelName, CompartmentHandler cmpHandler) {
		mNet				= net;
		mModelName			= modelName;
		mCompartmentHandler	= cmpHandler;
		
	}
	
	public void write(OutputStream out) {
		write(new OutputStreamWriter(out));
	}
	public void write(File file) throws IOException {
		write(new FileWriter(file));
	}
	public void write(Writer writer) {
		write(writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer));
	}
	public void write(PrintWriter printWriter) {
		LOG.fine("writing sbml ...");
		TokWriter writer = new TokWriter(printWriter);
		writeEncodingNode(writer);
		openRootNode(writer);
		openNode(writer, ELEMENT_MODEL, ATTRIBUTE_NAME, mModelName);
		openNode(writer, ELEMENT_COMPARTMENTS);
		writeCompartments(writer);
		closeNode(writer, ELEMENT_COMPARTMENTS);
		openNode(writer, ELEMENT_METABOLITES);
		writeMetabolites(writer);
		closeNode(writer, ELEMENT_METABOLITES);
		openNode(writer, ELEMENT_REACTIONS);
		writeReactions(writer);
		closeNode(writer, ELEMENT_REACTIONS);
		closeNode(writer, ELEMENT_MODEL);
		closeRootNode(writer);
		printWriter.flush();
		LOG.fine("writing sbml complete.");
	}
	
	private void writeCompartments(TokWriter writer) {
		for (String cmp : mCompartmentHandler.compartments()) {
			openCloseNode(
				writer, ELEMENT_COMPARTMENT, 
				new String[] {ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_SIZE},
				new String[] {encodeId(cmp), cmp, "0.0"}
			);
		}		
	}
	private void writeMetabolites(TokWriter writer) {
		Set<String> metaNames	= new HashSet<String>(); 
		Set<String> xtMetaNames	= new HashSet<String>(); 
		for (Metabolite meta : mNet.getMetabolites()) {
			if (mCompartmentHandler.exportMetabolite(meta)) {
				String metaName	= meta.getName();
				String cmp		= mCompartmentHandler.compartmentForMetabolite(meta);
				//String metaName	= getNameFromId(metaId, cmp);
				openCloseNode(
					writer, ELEMENT_METABOLITE, 
					new String[] {ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_COMPARTMENT, ATTRIBUTE_INITIAL_CONCENTRATION},
					new String[] {encodeId(metaName), metaName, encodeId(cmp), "1.0"}
				);
				if (metaNames.contains(metaName)) {
					//should not happen
					throw new RuntimeException("duplicate metabolite: " + metaName);
				}
				metaNames.add(metaName);
			}
			else {
				LOG.info("... ommitted metabolite: " + meta);
			}
		}
		
		//external metabolites for exchange reactions
		for (Reaction reac : mNet.getReactions()) {
			if (reac.isExternal() && mCompartmentHandler.exportReaction(reac)) {
				CompartmentMetabolite xtMeta = mCompartmentHandler.externalReactionMetabolite(reac);
				if (mCompartmentHandler.exportMetabolite(xtMeta)) {
					String metaName	= xtMeta.getName();
					String cmp		= xtMeta.getCompartment();
//					String metaName	= getNameFromId(metaId, cmp);
					if (metaNames.contains(metaName)) {
						//should not happen
						throw new RuntimeException("duplicate metabolite: " + metaName);
					}
					if (!xtMetaNames.contains(metaName)) {
						xtMetaNames.add(metaName);
						openCloseNode(
							writer, ELEMENT_METABOLITE, 
							new String[] {ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_COMPARTMENT, ATTRIBUTE_INITIAL_CONCENTRATION},
							new String[] {encodeId(metaName), metaName, encodeId(cmp), "1.0"}
						);						
					}
				}
			}
		}
	}
	private void writeReactions(TokWriter writer) {
		for (Reaction reac : mNet.getReactions()) {
			if (mCompartmentHandler.exportReaction(reac)) {
				String reacId	= reac.getName();
				String reacName	= reac.getFullName();
				openNode(
					writer, ELEMENT_REACTION, 
					new String[] {ATTRIBUTE_ID, ATTRIBUTE_NAME, ATTRIBUTE_REVERSIBLE},
					new String[] {encodeId(reacId), reacName, Boolean.toString(reac.getConstraints().isReversible())}
				);
				openNode(writer, ELEMENT_EDUCTS);
				if (reac.isUptake()) {
					CompartmentMetabolite xtMeta = mCompartmentHandler.externalReactionMetabolite(reac);
					openCloseNode(
						writer, ELEMENT_EDUCT, 
						new String[] {ATTRIBUTE_SPECIES, ATTRIBUTE_STOICHIOMETRY},
						new String[] {encodeId(xtMeta.getName()), NUMBER_FORMAT.format(1d)}
					);					
				}
				else {
					for (MetaboliteRatio ratio : reac.getEductRatios()) {
						openCloseNode(
							writer, ELEMENT_EDUCT, 
							new String[] {ATTRIBUTE_SPECIES, ATTRIBUTE_STOICHIOMETRY},
							new String[] {encodeId(ratio.getMetabolite().getName()), NUMBER_FORMAT.format(-ratio.getRatio())}
						);
					}					
				}
				closeNode(writer, ELEMENT_EDUCTS);
				openNode(writer, ELEMENT_PRODUCTS);
				if (reac.isExtract()) {
					CompartmentMetabolite xtMeta = mCompartmentHandler.externalReactionMetabolite(reac);
					openCloseNode(
						writer, ELEMENT_PRODUCT, 
						new String[] {ATTRIBUTE_SPECIES, ATTRIBUTE_STOICHIOMETRY},
						new String[] {encodeId(xtMeta.getName()), NUMBER_FORMAT.format(1d)}
					);
				}
				else {
					for (MetaboliteRatio ratio : reac.getProductRatios()) {
						openCloseNode(
							writer, ELEMENT_PRODUCT, 
							new String[] {ATTRIBUTE_SPECIES, ATTRIBUTE_STOICHIOMETRY},
							new String[] {encodeId(ratio.getMetabolite().getName()), NUMBER_FORMAT.format(ratio.getRatio())}
						);
					}
				}
				closeNode(writer, ELEMENT_PRODUCTS);
				closeNode(writer, ELEMENT_REACTION);				
			}
			else {
				LOG.info("... ommitted reaction: " + reac.getName());
			}
		}
	}
	private static void writeEncodingNode(TokWriter writer) {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	}
	private static void openRootNode(TokWriter writer) {
		openNode(
			writer, ELEMENT_ROOT,
			new String[] {"xmlns", "level", "version"},
			new String[] {"http://www.sbml.org/sbml/level2", "2", "1"}
		);
	}
	private static void closeRootNode(TokWriter writer) {
		closeNode(writer, ELEMENT_ROOT);
	}
	private static void openNode(TokWriter writer, String nodeName) {
		openNode(writer, nodeName, (String[])null, (String[])null);
	}
//	private static void openCloseNode(TokWriter writer, String nodeName) {
//		openCloseNode(writer, nodeName, (String[])null, (String[])null);
//	}
	private static void openNode(TokWriter writer, String nodeName, String attributeName, String attributeValue) {
		openNode(writer, nodeName, new String[] {attributeName}, new String[]{attributeValue});
	}
//	private static void openCloseNode(TokWriter writer, String nodeName, String attributeName, String attributeValue) {
//		openCloseNode(writer, nodeName, new String[] {attributeName}, new String[]{attributeValue});
//	}
	private static void openNode(TokWriter writer, String nodeName, String[] attributeNames, String[] attributeValues) {
		openNode(writer, nodeName, attributeNames, attributeValues, false /*alsoClose*/);		
	}
	private static void openCloseNode(TokWriter writer, String nodeName, String[] attributeNames, String[] attributeValues) {
		openNode(writer, nodeName, attributeNames, attributeValues, true /*alsoClose*/);
	}
	private static void openNode(TokWriter writer, String nodeName, String[] attributeNames, String[] attributeValues, boolean alsoClose) {
		writer.print("<" + nodeName);
		if (attributeNames != null && attributeNames.length > 0) {
			for (int ii = 0; ii < attributeNames.length; ii++) {
				writer.print(" ");
				writer.print(attributeNames[ii] + "=\"" + attributeValues[ii] + "\"");
			}
		}
		if (alsoClose) {
			writer.println("/>");
		}
		else {
			writer.println(">");		
			writer.incIndention();			
		}
	}
	private static void closeNode(TokWriter writer, String nodeName) {
		writer.decIndention();			
		writer.println("</" + nodeName + ">");
	}
	
	///////////////////////////////////////////////////////////////
	// helpers
	
	private static String encodeId(String id) {
		StringBuilder sb = new StringBuilder(id.length());
		for (int ii = 0; ii < id.length(); ii++) {
			char ch = id.charAt(ii);
			if (
				(ch >= 'a' && ch <= 'z') || 
				(ch >= 'A' && ch <= 'Z') || 
				ch == '_' || 
				(ch >= '0' && ch <= '9')
			) {
				if (ii == 0 && (ch >= '0' && ch <= '9')) {
					sb.append('_');
				}
				sb.append(ch);
			}
			else {
				sb.append('_');				
			}
		}
		return sb.toString();
	}
//	private static String getNameFromId(String id /*non-encoded*/, String compartment) {
//		if (id.endsWith("_" + compartment)) return id.substring(0, id.length() - compartment.length() - 1);
//		if (id.endsWith(compartment)) return id.substring(0, id.length() - compartment.length());
//		if (id.startsWith(compartment + "_")) return id.substring(compartment.length() + 1);
//		if (id.startsWith(compartment)) return id.substring(compartment.length());
//		return id;
//	}
	private static class TokWriter {
		public TokWriter(PrintWriter writer) {pw = writer;}
		public final PrintWriter pw; 
		private int indent = 0;
		private boolean afterLine = true;
		public void println(String str) {
			if (afterLine) pw.print(indention());
			pw.println(str);
			afterLine = true;
		}
		public void print(String str) {
			if (afterLine) {
				pw.print(indention());
				afterLine = false;
			}
			pw.print(str);
		}
		public String indention() {
			StringBuilder sb = new StringBuilder(indent);
			for (int ii = 0; ii < indent; ii++) {
				sb.append('\t');
			}
			return sb.toString();
		}
		public void incIndention() {indent++;}
		public void decIndention() {indent--;}
	}
	
	
}
