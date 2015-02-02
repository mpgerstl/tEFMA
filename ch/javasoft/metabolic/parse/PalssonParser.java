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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.compartment.CompartmentMetabolite;
import ch.javasoft.metabolic.compartment.CompartmentMetaboliteRatio;
import ch.javasoft.metabolic.compartment.CompartmentReaction;
import ch.javasoft.metabolic.impl.DefaultReactionConstraints;

public class PalssonParser {
	
	private final boolean checkConsistency;
	
	public PalssonParser() {
		this(true);
	}
	
	//no instances
	public PalssonParser(boolean checkConsistency) {
		this.checkConsistency = checkConsistency;
	}

	private static final Logger LOG = LogPkg.LOGGER;

	//sample line:
	//"ALAR"  "alanine racemase"      "[c]ala-L <==> ala-D"   "Alanine and aspartate metabolism"      "EC-5.1.1.1"
	//"NAt3_1.5"      "sodium proton antiporter (H:NA is 1.5)"        "(3) h[e] + (2) na1[c] --> (3) h[c] + (2) na1[e]"       "Transport, Extracellular"
	//
	//or:
	//"ALAR"  		"[c]ala-L <==> ala-D"   							"Alanine and aspartate metabolism"      "EC-5.1.1.1"
	//"NAt3_1.5"    "(3) h[e] + (2) na1[c] --> (3) h[c] + (2) na1[e]"   "Transport, Extracellular"

	private static final String P_TAB				= "\\t";
	private static final String P_NAME				= "\"([^\\t^\"]*)\"";//any chars in quotes, except for tabs & quotes
	
	private static final String P_LINE_3			= P_NAME + P_TAB + P_NAME + P_TAB + P_NAME + ".*";
	private static final String P_LINE_2			= P_NAME + P_TAB + P_NAME + ".*";
	
	private static Pattern PAT_LINE_3 = Pattern.compile(P_LINE_3);
	private static Pattern PAT_LINE_2 = Pattern.compile(P_LINE_2);
	
//	public static final Pattern PATTERN_EXTERNAL_METABOLITE = Pattern.compile(".*\\[e\\]");
	
	public CompartmentReaction[] parseReactions(File file) throws IOException {
		return parseReactions(file, null);
	}
	public CompartmentReaction[] parseReactions(InputStream in) throws IOException {
		return parseReactions(new InputStreamReader(in));
	}
	public CompartmentReaction[] parseReactions(Reader reader) throws IOException {
		return parseReactions(reader, null);
	}
	public CompartmentReaction[] parseReactions(InputStream in, Pattern ptnExternalMetabolite) throws IOException {
		return parseReactions(new InputStreamReader(in), ptnExternalMetabolite);
	}
	public CompartmentReaction[] parseReactions(File file, Pattern ptnExternalMetabolite) throws IOException {
		return parseReactions(new FileReader(file), ptnExternalMetabolite);
	}
	public CompartmentReaction[] parseReactions(Reader reader, Pattern ptnExternalMetabolite) throws IOException {
		return parseReactions(reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader), ptnExternalMetabolite);
	}
	public CompartmentReaction[] parseReactions(BufferedReader reader, Pattern ptnExternalMetabolite) throws IOException {		
		List<CompartmentReaction> reacts = new ArrayList<CompartmentReaction>();
		String line;
		int lineNo = 0;
		line = reader.readLine();lineNo++;
		while (line != null) {
			final String shortName, formula;
			Matcher matcher = PAT_LINE_3.matcher(line);
			if (matcher.matches()) {
				shortName	= matcher.group(1);
//				longName	= matcher.group(2);
				formula		= matcher.group(3);
//				location	= matcher.group(4);//!!!: missing for biomass line
			}
			else {
				matcher = PAT_LINE_2.matcher(line);
				if (!matcher.matches()) {
					throw new IOException("syntax error on line " + lineNo + ": " + line);
				}
				shortName	= matcher.group(1);
				formula		= matcher.group(2);
//				location	= matcher.group(3);//!!!: missing for biomass line
			}
			
			CompartmentReaction react = parseReaction(shortName, formula, lineNo);
			reacts.add(react);
			
			line = reader.readLine();lineNo++;
		}
		addExchangeReactions(reacts, ptnExternalMetabolite);
		if (checkConsistency) checkConsistency(reacts);
		
		CompartmentReaction[] result = new CompartmentReaction[reacts.size()];
		reacts.toArray(result);
		return result;
	}
	
	private static void addExchangeReactions(List<CompartmentReaction> reactions, Pattern ptnExternalMetabolite) throws IOException {
		if (ptnExternalMetabolite == null) return;
		
		LOG.info("adding exchange reactions for metabolite pattern: " + ptnExternalMetabolite.toString());
		
		Set<CompartmentMetabolite> xtMetas = new HashSet<CompartmentMetabolite>();
		for (CompartmentReaction react : reactions) {
			for (CompartmentMetaboliteRatio ratio : react.getMetaboliteRatios()) {
				if (ptnExternalMetabolite.matcher(ratio.getMetabolite().getName()).matches()) {
					xtMetas.add(ratio.getMetabolite());
				}
			}
		}
		for (CompartmentMetabolite meta : xtMetas) {
			String name = "ex_" + meta.getName();
			CompartmentReaction reac = new CompartmentReaction(name, name, new CompartmentMetaboliteRatio[] {new CompartmentMetaboliteRatio(meta, 1d)}, true);
			reactions.add(reac);
			LOG.fine("added exchange reaction '" + reac.getName() + "': " + reac);
		}		
	}
	
	private static CompartmentReaction parseReaction(String name, String formula, int lineNo) throws IOException {
		// NOTE:
		//   a) from left to right: left ones must not be prefix of right ones!
		//   b) parse order is rev[0], irrev[0], rev[1], irrev[1], ..., so rev[i] must not be a prefix of irrev[j>=i]
		final String[] reacTypeRev		= new String[] {"<-->", "<==>", "<->", "<=>"};	
		final String[] reacTypeIrrevFw	= new String[] {"-->", "==>", "->", "=>"};		
		final String[] reacTypeIrrevBw	= new String[] {"<--", "<==", "<-", "<="};		
		
		/*
		 * [c] : cytosol    [e] : extracellular    [g] : Golgi appratus    [m] : mitochondrion    [n] : nucleus    [r] : endoplasmic reticulum    [v] : vacoule    [x] : peroxisome
		 */
		final String[] locationPrefixes		= new String[] {"[c]", "[e]", "[g]", "[m]", "[n]", "[r]", "[v]", "[x]", ""};
		final Pattern[] locationPatterns	= new Pattern[] {
			Pattern.compile("\\[c\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[e\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[g\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[m\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[n\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[r\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[v\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("\\[x\\][\\s]*:?[\\s]*(.*)[\\s]*"),
			Pattern.compile("[\\s]*(.*)[\\s]*")
		};
		
		ReactionConstraints constraints = null;
		String reacType					= null;
		boolean backwards				= false;
		for (int ii = 0; constraints == null && ii < reacTypeRev.length; ii++) {
			if (formula.contains(reacTypeRev[ii])) {
				constraints = DefaultReactionConstraints.DEFAULT_REVERSIBLE;
				reacType	= reacTypeRev[ii];
				backwards	= false;
			}			
			else if (formula.contains(reacTypeIrrevFw[ii])) {
				constraints = DefaultReactionConstraints.DEFAULT_IRREVERSIBLE;
				reacType	= reacTypeIrrevFw[ii];
				backwards	= false;
			}
			else if (formula.contains(reacTypeIrrevBw[ii])) {
				constraints = DefaultReactionConstraints.DEFAULT_IRREVERSIBLE;
				reacType	= reacTypeIrrevBw[ii];
				backwards	= true;
			}
		}
		if (constraints == null) {
			throw new IOException(
				"not an irreversible, nor a reversible reaction at line " + lineNo + ": " + formula
			);
		}

		String loc = null;
		for (int ii = 0; loc == null && ii < locationPatterns.length; ii++) {
			Matcher matcher = locationPatterns[ii].matcher(formula);
			if (matcher.matches()) {
				loc		= locationPrefixes[ii];
				formula	= matcher.group(1);
			}
		}
		if (loc == null) {
			throw new IOException(
				"none of the reaction patterns matches at line " + lineNo + ": " + formula
			);
		}
		
		//
		List<CompartmentMetaboliteRatio> ratios	= new ArrayList<CompartmentMetaboliteRatio>();
		Map<String, CompartmentMetabolite> metas	= new HashMap<String, CompartmentMetabolite>();
		String[] parts = formula.split("[\\s]*" + reacType + "[\\s]*");
		if (parts.length == 2 && parts[0].length() == 0) {
			parts = new String[] {parts[1]};
		}
		
		final boolean parseProds = backwards ? false : true;
		if (parts.length == 2) { 
			parseRatios(ratios, metas, loc, parts[0], !parseProds /*products*/, lineNo);
			parseRatios(ratios, metas, loc, parts[1], parseProds /*products*/, lineNo);
		}
		else if (parts.length == 1) {
			//products or educts?
			if (formula.matches(".*" + reacType + "[\\s]*")) {
				//educts, sink reaction
				parseRatios(ratios, metas, loc, parts[0], !parseProds /*products*/, lineNo);				
			}
			else {
				//products, uptake reaction
				parseRatios(ratios, metas, loc, parts[0], parseProds /*products*/, lineNo);				
			}
		}
		else {
			throw new IOException(
				"expected substrates and/or products, but found " + parts.length + 
				" parts at line " + lineNo + ": " + formula
			);				
		}
		
		//remove external metabolites, if any
		CompartmentMetaboliteRatio[] arrRatios = new CompartmentMetaboliteRatio[ratios.size()];
		ratios.toArray(arrRatios);
		return new CompartmentReaction(name, name, arrRatios, constraints);			
	}
	
	private static void parseRatios(List<CompartmentMetaboliteRatio> result, Map<String, CompartmentMetabolite> metas, String locPostfix, String formula, boolean products, int lineNo) throws IOException {
		String[] parts = formula.contains("+") ? formula.split("[\\s]+\\+[\\s]+") : new String[] {formula};
		for (int ii = 0; ii < parts.length; ii++) {
			String part = parts[ii].replaceAll("[\\s]+", " ").trim();
			int spaceIndex = part.indexOf(' ');
			
			final String metaName;
			final double coeff;
			if (spaceIndex == -1) {
				//no coefficent, it is 1
				metaName = part + locPostfix;
				coeff = 1d;
			}
			else {
				metaName 		= part.substring(spaceIndex + 1) + locPostfix;
				String stoich	= part.substring(0, spaceIndex);
				if (stoich.startsWith("(") && stoich.endsWith(")")) {
					stoich = stoich.substring(1, stoich.length() - 1);
				}
				coeff = parseRatio(stoich, lineNo);
			}
			CompartmentMetabolite meta = getMetabolite(metas, metaName);
			CompartmentMetaboliteRatio ratio = new CompartmentMetaboliteRatio(meta, products ? coeff : -coeff);			
			result.add(ratio);
		}
	}
	
	private static CompartmentMetabolite getMetabolite(Map<String, CompartmentMetabolite> metas, String name) throws IOException {
		CompartmentMetabolite meta = metas.get(name);
		if (meta == null) {
			String[] rawNameCmpName = getNameCompartmentForMetabolite(name);
			metas.put(name, meta = new CompartmentMetabolite(name, rawNameCmpName[1]));
		}
		return meta;
	}
	
	private static double parseRatio(String str, int lineNo) throws IOException {
		try {
			return Double.parseDouble(str);
		}
		catch (Exception ex) {
			throw new IOException(
				"cannot parse stoichiometric quantity '" + str + "' at line " + lineNo + ", e=" + ex
			);
		}
	}
	
	private static final Pattern PTN_META = Pattern.compile("([^\\[\\]]+)\\[([^\\[\\]]+)\\]");
	private static String[] getNameCompartmentForMetabolite(String name) throws IOException {
		Matcher matcher = PTN_META.matcher(name);
		String rawName;
		String cmpName;
		if (matcher.matches()) {
			rawName	= matcher.group(1);
			cmpName	= matcher.group(2);
		}
		else {
			rawName	= name;
			cmpName	= "default";			
			//throw new IOException("cannot parse compartment metabolite: " + name);
			LOG.finest("cannot parse compartment metabolite: " + name);
		}
		return new String[] {rawName, cmpName};
	}
	
	private static void checkConsistency(Iterable<CompartmentReaction> reacts) throws IOException {
		TreeMap<String, TreeMap<String, List<CompartmentReaction>>> map = new TreeMap<String, TreeMap<String, List<CompartmentReaction>>>(Collator.getInstance());
		for (CompartmentReaction react : reacts) {
			for (CompartmentMetaboliteRatio ratio : react.getMetaboliteRatios()) {
				CompartmentMetabolite meta = ratio.getMetabolite();
				String[] nameCmp = getNameCompartmentForMetabolite(meta.getName());
				String rawName = nameCmp[0];
				String cmpName = nameCmp[1];
				TreeMap<String, List<CompartmentReaction>> metaMap = map.get(rawName);
				if (metaMap == null) {
					metaMap = new TreeMap<String, List<CompartmentReaction>>(Collator.getInstance());
					map.put(rawName, metaMap);
				}
				List<CompartmentReaction> cmpList = metaMap.get(cmpName);
				if (cmpList == null) {
					metaMap.put(cmpName, cmpList = new ArrayList<CompartmentReaction>());
				}
				cmpList.add(react);
			}
		}
			
		//trace
		for (String metaName : map.keySet()) {
			for (String cmpName : map.get(metaName).keySet()) {
				if (map.get(metaName).get(cmpName).size() < 2) {
					CompartmentReaction reac = map.get(metaName).get(cmpName).get(0);
					LOG.warning("[DEAD-END-META] " + metaName + '[' + cmpName + "] / " + reac.getFullName() + ": " + reac);
				}
			}
		}
		for (String metaName : map.keySet()) {
			if (map.get(metaName).keySet().size() >= 2) {
				//check for transport reaction from 1 compartement to the other
				List<String> compartments = new ArrayList<String>(map.get(metaName).keySet());
				for (int cmp1 = 0; cmp1 < compartments.size(); cmp1++) {
					for (int cmp2 = cmp1 + 1; cmp2 < compartments.size(); cmp2++) {
						String cmpName1 = compartments.get(cmp1); 
						String cmpName2 = compartments.get(cmp2); 
						boolean hasTransport = false;
						for (CompartmentReaction react : map.get(metaName).get(cmpName1)) {
							if (map.get(metaName).get(cmpName2).contains(react)) {
								hasTransport = true;
								break;
							}
						}
						if (!hasTransport) {
							LOG.warning("[NO-TRANSPORT] " + metaName + "[" + cmpName1 + "] <--> [" + cmpName2 + "]");
							for (CompartmentReaction react : map.get(metaName).get(cmpName1)) {
								if (react.isCompartmentInternal()) {
									LOG.info("\t" + react.getName() + "[" + cmpName1 + "]: " + react);									
								}
								else {
									LOG.warning("\t" + react.getName() + "[" + cmpName1 + "]: " + react);									
								}
							}
							for (CompartmentReaction react : map.get(metaName).get(cmpName2)) {
								if (react.isCompartmentInternal()) {
									LOG.info("\t" + react.getName() + "[" + cmpName2 + "]: " + react);									
								}
								else {
									LOG.warning("\t" + react.getName() + "[" + cmpName2 + "]: " + react);									
								}
							}
						}						
					}					
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static String getReactionNames(Iterable<CompartmentReaction> reacts) {
		StringBuilder sb = new StringBuilder();
		for (CompartmentReaction react : reacts) {
			if (sb.length() > 0) sb.append(" / ");
			sb.append(react.getName());
		}
		return sb.toString();
	}
}
