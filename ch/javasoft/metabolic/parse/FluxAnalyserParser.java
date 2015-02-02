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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.impl.DefaultFluxDistribution;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.impl.DefaultMetabolite;
import ch.javasoft.metabolic.impl.DefaultMetaboliteRatio;
import ch.javasoft.metabolic.impl.DefaultReaction;
import ch.javasoft.metabolic.impl.DefaultReactionConstraints;
import ch.javasoft.metabolic.util.FluxNormalizer;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericArray;
import ch.javasoft.util.genarr.GenericDynamicArray;
import ch.javasoft.util.numeric.Zero;

public class FluxAnalyserParser {

	private static final Logger LOG = LogPkg.LOGGER;

	private static final String P_UINT				= "[0-9]+";
	private static final String P_INT 				= "\\-?" + P_UINT;
	private static final String P_FLOAT				= P_INT + "\\.?[0-9]*";
	private static final String P_RATE				= "#|" + P_FLOAT;
	private static final String P_RATEMINMAX		= "Inf|-Inf|" + P_FLOAT;		
	private static final String P_NAME				= "([\\w,_\\(\\)\\[\\]\\+'/&:-]+)";		
	private static final String P_REACDEF			= "(.*)[\\s]*=[\\s]*(.*)";
	private static final String P_REACCONST			= "(" + P_RATE + ")[\\s]+(" + P_RATEMINMAX + ")[\\s]+(" + P_RATEMINMAX + ").*";
	
	private static final String BIOMASS_REAC_NAME	= "mue";
	
	public static MetabolicNetwork parse(File folder) throws IOException {
		if (!folder.exists() || !folder.isDirectory() || !folder.canRead() || !folder.canWrite()) {
			throw new IOException("cannot find, read or write directory: " + folder.getAbsolutePath());
		}
		return parse(new File(folder, "reactions"), new File(folder, "macromolecules"), new File(folder, "macromolecule_synthesis"));
	}
	public static MetabolicNetwork parse(File reactionFile, File biomassFile, File macroReactionFile) throws IOException {
		return parse(new FileReader(reactionFile), biomassFile == null ? null : new FileReader(biomassFile), macroReactionFile == null ? null : new FileReader(macroReactionFile));
	}
	public static MetabolicNetwork parse(InputStream reactionStream, InputStream biomassStream, InputStream macroReactionStream) throws IOException {
		return parse(new InputStreamReader(reactionStream), biomassStream == null ? null : new InputStreamReader(biomassStream), macroReactionStream == null ? null : new InputStreamReader(macroReactionStream));
	}
	
	public static MetabolicNetwork parse(Reader reactionReader, Reader biomassReader, Reader macroReactionReader) throws IOException {
		return parse(
			reactionReader instanceof BufferedReader ? (BufferedReader)reactionReader : new BufferedReader(reactionReader),					
			biomassReader == null ? null : biomassReader instanceof BufferedReader ? (BufferedReader)biomassReader : new BufferedReader(biomassReader),					
			macroReactionReader == null ? null : macroReactionReader instanceof BufferedReader ? (BufferedReader)macroReactionReader : new BufferedReader(macroReactionReader)
		);
	}
	public static MetabolicNetwork parse(BufferedReader reactionReader, BufferedReader biomassReader, BufferedReader macroReactionReader) throws IOException {
		ParserSymbolTables tables = new ParserSymbolTables();
		parseReaction(reactionReader, tables);
		if (biomassReader != null) parseBiomassRatio(biomassReader, tables);
		if (macroReactionReader != null) parseMacroReaction(macroReactionReader, tables);
		return tables.createMetabolicNetwork();
	}
	private static void parseReaction(BufferedReader reactionReader, ParserSymbolTables tables) throws IOException {
		
		//sample line:
		//Glyc::Glyc3P            1 ATP + 1 Glyc = 1 Glyc3P       |       #       0 100   0       53  419    1    1       0.01
		//R1::A   	 = 1 A 	| 	# 	-Inf           Inf             0 	73.5         97.5            1            1 	0.01 
		Pattern muePattern	= Pattern.compile("mue[\\s]*\\|[\\s]*" + P_REACCONST);
		Pattern reacPattern = Pattern.compile(P_NAME + "[\\s]+" + P_REACDEF + "[\\s]*\\|[\\s]*" + P_REACCONST);
		//reactionReader.readLine();//ignore first line
		String line = reactionReader.readLine();
		int lineNum = 0;
		while (line != null) {
			Matcher matcher = reacPattern.matcher(line);
			String reactionName, educts, products, /*flux, */lowerBound, upperBound;
			if (matcher.matches()) {
				reactionName	= matcher.group(1);
				educts			= matcher.group(2).trim();
				products		= matcher.group(3).trim();
//				flux			= matcher.group(4);
				lowerBound		= matcher.group(5);
				upperBound		= matcher.group(6);
			}
			else if ((matcher = muePattern.matcher(line)).matches()) {
				reactionName	= BIOMASS_REAC_NAME;
				educts			= "";
				products		= "";
//				flux			= matcher.group(1);
				lowerBound		= matcher.group(2);
				upperBound		= matcher.group(3);				
			}
			else {
				throw new IOException("syntax error at line [" + lineNum + "]: " + line);				
			}
			MetaboliteRatio[] rEducts, rProducts;
			try {
				rEducts		= getMetaboliteRatios(tables, educts, true);
				rProducts	= getMetaboliteRatios(tables, products, false);
			}
			catch (IOException ioe) {
				throw new ParseException(ioe, lineNum);
			}
//			if (lineNum < 20) {
//				System.out.println(reactionName);
//				System.out.println(educts);
//				System.out.println(products);
//				System.out.println(flux);
//				System.out.println(lowerBound);
//				System.out.println(upperBound);		
//				System.out.println(rEducts.length);		
//				System.out.println(rProducts.length);						
//				System.out.println("----------------------");
//			}
			final GenericArray<MetaboliteRatio> rMetabolites = new GenericDynamicArray<MetaboliteRatio>(
				rEducts.length + rProducts.length	
			);
			rMetabolites.set(0, rEducts);
			rMetabolites.set(rEducts.length, rProducts);
			ReactionConstraints cons;
			try {
				cons = tables.getReactionConstraints(
					parseConstraint(lowerBound), parseConstraint(upperBound)
				);				
			}
			catch (NumberFormatException nex) {
				throw new ParseException("cannot parse constraints: " + lowerBound + "/" + upperBound, nex, lineNum);
			}
			tables.addReaction(new DefaultReaction(reactionName, rMetabolites, cons));
			lineNum++;
			line = reactionReader.readLine();
		}		
	}
	private static double parseConstraint(String constraint) throws NumberFormatException {
		return 
			"Inf".equals(constraint) ? 
				Double.POSITIVE_INFINITY : (
				"-Inf".equals(constraint) ? 
					Double.NEGATIVE_INFINITY : 
					Double.parseDouble(constraint)
				);
	}
	private static void parseBiomassRatio(BufferedReader biomassReader, ParserSymbolTables tables) throws IOException {
//file: macromolecules
//sample lines:
//		Prot            Proteine               0.64     792.7497       655.881             2             1
//		RNA             RNA                    0.185    795.7825      865.5965             2             1
//		DNA             DNA                    0.03     797.7989      985.4775             2             1
		Pattern linePattern = Pattern.compile(P_NAME + "[\\s]+" + P_NAME + "[\\s]+(" + P_FLOAT + ")[\\s]+(" + P_FLOAT + ")[\\s]+(" + P_FLOAT + ").*");
		String line = biomassReader.readLine();
		int lineNum = 0;
		while (line != null) {
			Matcher matcher = linePattern.matcher(line);
			if (!matcher.matches()) {
				throw new IOException("syntax error at line [" + lineNum + "]: " + line);
			}
			String shortName			= matcher.group(1);
			String macroMoleculeName	= getMacroMoleculeName(shortName);
//			String longName				= matcher.group(2);
			String ratePerc				= matcher.group(3);
//			String rate??				= matcher.group(4);
//			String rate??				= matcher.group(5);
			double ratio;
			try {
				ratio = Double.parseDouble(ratePerc);
			}
			catch (Exception ex) {
				throw new ParseException(
					"cannot parse biomass ratio '" + ratePerc + 
					" for " + shortName, ex, lineNum);
			}
			tables.setBiomassRatio(macroMoleculeName, ratio);
			lineNum++;
			line = biomassReader.readLine();
		}
	}
	private static void parseMacroReaction(BufferedReader macroReactionReader, ParserSymbolTables tables) throws IOException {
		//sample line:
		//RNA      = 1.2488 ATP + 0.80488 rATP + 0.99024 rGTP + 0.61436 rCTP + 0.66341 rUTP
		Pattern linePattern = Pattern.compile(P_REACDEF);
		String line = macroReactionReader.readLine();
		int lineNum = 0;
		while (line != null) {
			Matcher matcher = linePattern.matcher(line);
			if (!matcher.matches()) {
				throw new IOException("syntax error at line [" + lineNum + "]: " + line);
			}
			String reactionName		= getMacroSynthesisName(matcher.group(1).trim());
//			String extReactionName	= "Biomass-Synthesis-" + matcher.group(1);
			String educts		= matcher.group(2).trim();
			String product		= getMacroMoleculeName(matcher.group(1).trim());
			MetaboliteRatio[] rEducts, rBiomass;
			try {
				rEducts		= getMetaboliteRatios(tables, educts, true);
				rBiomass	= new MetaboliteRatio[] {tables.getBiomassRatio(product)};
				if (rBiomass[0] == null) {
					throw new ParseException("no biomass ratio for molecule: " + product, lineNum);
				}
				if (rBiomass[0].getRatio() == 0.0d) {
					LOG.info("removing 0 biomass ratio for " + product);
					tables.removeBiomassRatio(product);
				}
			}
			catch (ParseException pe) {
				throw pe;
			}
			catch (IOException ioe) {
				throw new ParseException(ioe, lineNum);
			}
			GenericArray<MetaboliteRatio> rMetabolites = new GenericDynamicArray<MetaboliteRatio>(
				rEducts.length + 1	
			);
			rMetabolites.set(0, rEducts);
			rMetabolites.set(rEducts.length, new DefaultMetaboliteRatio(tables.getMetabolite(product), 1.0d));			
//			MetaboliteRatio[] rBiomass = new MetaboliteRatio[] {
//				new DefaultMetaboliteRatio(tables.getMetabolite(product), -1.0d)
//			};
			tables.addReaction(new DefaultReaction(reactionName, rMetabolites, DefaultReactionConstraints.DEFAULT_IRREVERSIBLE));
//			tables.addReaction(new DefaultReaction(extReactionName, rBiomass, DefaultReactionConstraints.DEFAULT_IRREVERSIBLE));
			lineNum++;
			line = macroReactionReader.readLine();
		}		
	}
	
	private static MetaboliteRatio[] getMetaboliteRatios(ParserSymbolTables tables, String str, boolean educt) throws IOException {
		if (str.length() == 0) {
			return new MetaboliteRatio[] {};
		}
		else {
			//sample: 4 ATP + 4 NADPH
			String[] parts = str.contains("+") ? str.split("[\\s]*\\+[\\s]*") : new String[] {str};
			if (parts.length == 0) {
				throw new IOException("cannot parse metabolite string " + str);
			}
			MetaboliteRatio[] result = new MetaboliteRatio[parts.length];
			for (int ii = 0; ii < result.length; ii++) {
				String[] split = parts[ii].split("[\\s]+");
				if (split.length != 2 || split[0].length() == 0 || split[1].length() == 0) {
					throw new IOException("cannot parse metabolite string " + str);
				}
				Metabolite metabolite = tables.getMetabolite(split[1]);
				result[ii] = new DefaultMetaboliteRatio(metabolite, (educt ? -1.0 : 1.0d) * Double.parseDouble(split[0]));
			}
			return result;
		}
	}
	
	private static class ParserSymbolTables {
		private final Map<String, Metabolite>						mMetabolites		= new HashMap<String, Metabolite>();
		private final Map<String, Reaction>							mReactions			= new HashMap<String, Reaction>();
		private final Map<ReactionConstraints, ReactionConstraints>	mConstraintsCache	= new HashMap<ReactionConstraints, ReactionConstraints>();
		private final Map<String, MetaboliteRatio>					mBiomassRatios		= new HashMap<String, MetaboliteRatio>();
		
		public Metabolite getMetabolite(String name) {
			Metabolite meta = mMetabolites.get(name);
			if (meta == null) {
				meta = new DefaultMetabolite(name);
				mMetabolites.put(name, meta);
			}
			return meta;
		}
		public ReactionConstraints getReactionConstraints(double lowerBound, double upperBound) {
			ReactionConstraints cons = new DefaultReactionConstraints(lowerBound, upperBound);
			ReactionConstraints res = mConstraintsCache.get(cons);
			if (res == null) {
				mConstraintsCache.put(cons, cons);
				res = cons;
			}
			return res;
		}
		public void addReaction(Reaction reaction) {
			mReactions.put(reaction.getName(), reaction);
		}
		private void establishBiomassRatios() {
			Reaction reac = mReactions.remove(BIOMASS_REAC_NAME);
			if (reac == null) return;//we have no biomass reaction
			mReactions.put(
				BIOMASS_REAC_NAME, 
				new DefaultReaction(
					BIOMASS_REAC_NAME, 
					mBiomassRatios.values().toArray(new MetaboliteRatio[mBiomassRatios.size()]),
					reac.getConstraints()
				)
			);
		}
		public MetabolicNetwork createMetabolicNetwork() {
			establishBiomassRatios();
			return new DefaultMetabolicNetwork(
				new GenericDynamicArray<Metabolite>(mMetabolites.values().toArray(new Metabolite[mMetabolites.size()])), 
				new GenericDynamicArray<Reaction>(mReactions.values().toArray(new Reaction[mReactions.size()]))				
			);
		}
		public MetaboliteRatio getBiomassRatio(String macroMoleculeName) {
			return mBiomassRatios.get(macroMoleculeName);
		}
		public void setBiomassRatio(String macroMoleculeName, double ratio) {
			if (ratio == 0.0d) {
				LOG.warning("WARNING: 0 biomass ratio for " + macroMoleculeName);
			}
			Metabolite meta = getMetabolite(macroMoleculeName);
			mBiomassRatios.put(macroMoleculeName, new DefaultMetaboliteRatio(meta, -ratio));
		}
		public void removeBiomassRatio(String macroMoleculeName) {
			mBiomassRatios.remove(macroMoleculeName);
		}
	}
	
	public static ArrayIterable<FluxDistribution> parseEfms(MetabolicNetwork net, File file, int precision, boolean normalizeMax) throws IOException {
		return parseEfms(net, new FileReader(file), precision, normalizeMax);
	}
	public static ArrayIterable<FluxDistribution> parseEfms(MetabolicNetwork net, InputStream in, int precision, boolean normalizeMax) throws IOException {
		return parseEfms(net, new InputStreamReader(in), precision, normalizeMax);
	}
	public static ArrayIterable<FluxDistribution> parseEfms(MetabolicNetwork net, Reader reader, int precision, boolean normalizeMax) throws IOException {
		return parseEfms(net, reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader), precision, normalizeMax);
	}
	public static ArrayIterable<FluxDistribution> parseEfms(MetabolicNetwork net, BufferedReader reader, int precision, boolean normalizeMax) throws IOException {
		Zero zero = new Zero(precision);
		GenericDynamicArray<FluxDistribution> result = new GenericDynamicArray<FluxDistribution>();
		Map<String, Integer> reactIndexByReactName = new HashMap<String, Integer>();
		ArrayIterable<? extends Reaction> reacts = net.getReactions();
		for (int index = 0; index < reacts.length(); index++) {
			reactIndexByReactName.put(reacts.get(index).getName(), Integer.valueOf(index));
		}
		int lineIndex = 0;
		String line;
		line = reader.readLine();lineIndex++;
		String[] reactNames = null;
		while (line != null) {
			String[] parts = line.trim().split("[\\s]+");
			if (reactNames == null) {
				reactNames = parts;
			}
			else {
				if (reactNames.length != parts.length) {
					throw new ParseException(
						"not enough flux values, expected " + reactNames.length + 
						" but found " + parts.length, lineIndex
					);
				}
				double[] values = new double[reacts.length()];
				for (int ii = 0; ii < parts.length; ii++) {
					double value;
					try {
						value = Double.parseDouble(parts[ii]);
					}
					catch (NumberFormatException nex) {
						throw new ParseException(
							"cannot parse flux value '" + parts[ii] + "', e=" + nex, nex, lineIndex
						);
					}
					Integer index = reactIndexByReactName.get(reactNames[ii]);
					if (index == null) {
						throw new ParseException("reaction not found: " + reactNames[ii], lineIndex);
					}
					values[index.intValue()] = value;
				}
				FluxNormalizer.normalize(net, values, zero, normalizeMax);
				result.add(new DefaultFluxDistribution(net, values));
			}
			line = reader.readLine();lineIndex++;
		}
		return result;
	}
	
	private static class ParseException extends IOException {
		private static final long serialVersionUID = -6317078823123534799L;
		private Throwable mCause;
		public ParseException(String message, int lineNumber) {
			super("parse error at line [" + lineNumber + "]: " + message);
		}
		public ParseException(Throwable cause, int lineNumber) {
			this(cause.getMessage(), lineNumber);
			mCause = cause;
		}
		public ParseException(String message, Throwable cause, int lineNumber) {
			this(message, lineNumber);
			mCause = cause;
		}
		@Override
		public Throwable getCause() {
			return mCause;
		}
	}
	
	private static String getMacroMoleculeName(String macroMolecule) {
		return macroMolecule + "-M#";
	}
	private static String getMacroSynthesisName(String macroMolecule) {
		return macroMolecule + "-MS#";
	}
	
	//no instances
	private FluxAnalyserParser() {
		super();
	}
	
}
