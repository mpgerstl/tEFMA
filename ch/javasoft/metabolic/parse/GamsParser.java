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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.impl.DefaultReactionConstraints;

public class GamsParser {

	public static MetabolicNetwork parse(File folder, String filePrefix) throws IOException {
		return parse(
			new File(folder, filePrefix + ".metas"), 
			new File(folder, filePrefix + ".reacts"), 
			new File(folder, filePrefix + ".N"), 
			new File(folder, filePrefix + ".lower"), 
			new File(folder, filePrefix + ".upper") 
		);
	}
	
	public static MetabolicNetwork parse(File metasFile, File reactsFile, File stoichFile, File lowerFile, File upperFile) throws IOException {
		List<String> 	metas	= parseList(metasFile);
		List<String>	reacts	= parseList(reactsFile);
		double[][]		stoich	= parseStoich(metas, reacts, stoichFile);
		double[]		lower	= parseBounds(reacts, lowerFile);
		double[]		upper	= parseBounds(reacts, upperFile);
		return createMetabolicNetwork(metas, reacts, stoich, lower, upper);
	}
	
	private static List<String> parseList(File file) throws IOException {
		List<String> result = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("/")) {
				line = line.substring(1);
			}
			if (line.endsWith("/")) {
				line = line.substring(0, line.length() - 1);
			}
			String[] split = line.split(",");
			for (String str : split) {
				str = str.trim();
				if (str.length() > 0) {
					result.add(str.trim());					
				}
				else {
					//throw new IOException("empty");
				}
			}
		}
		return result;
	}
	
	/*
	10fthf.AICART -1
	10fthf.FTHFD -1
	10fthf.GARFT -1
	10fthf.MTHFC 1
	12dgr_EC.DAGK_EC -0.02
	12dgr_EC.PAPA_EC 0.02
	12dgr_EC.PEPT_EC 0.02
	*/
	private static double[][] parseStoich(List<String> metas, List<String> reacts, File stoichFile) throws IOException {
		double[][] result = new double[metas.size()][reacts.size()]; 
		Map<String, Integer> iMetas		= createIndexMap(metas);
		Map<String, Integer> iReacts	= createIndexMap(reacts);
		BufferedReader reader = new BufferedReader(new FileReader(stoichFile));
		String line;		
		while ((line = reader.readLine()) != null) {
			try {
				int dotIndex	= line.indexOf('.');
				int spaceIndex	= line.indexOf(' ', dotIndex + 1);
				String metaName = line.substring(0, dotIndex);
				String reacName	= line.substring(dotIndex + 1, spaceIndex);			
				String sCoeff	= line.substring(spaceIndex + 1);
				int metaIndex	= iMetas.get(metaName).intValue();
				int reacIndex	= iReacts.get(reacName).intValue();
				double coeff	= Double.parseDouble(sCoeff);
				result[metaIndex][reacIndex] = coeff;				
			}
			catch (Exception ex) {
				String msg = "cannot parse line: " + line;
				System.err.println(msg);
				ex.printStackTrace();
				throw new IOException(msg);
			}
		}
		return result;
	}
	
	/*
	/(*) 0/
	/(12PPDt,5DGLCNR,A5PISO,ACACT1r,ACACt2,ACALDt,ACCOACr,ACKr
			XTSNt2r,XYLI1) -1000/
	*/
	private static final Pattern BOUNDS_PTN = Pattern.compile("/\\((.*)\\)\\s+(.*)/");
	private static double[] parseBounds(List<String> reactions, File file) throws IOException {
		double[] result = new double[reactions.size()];
		Map<String, Integer> iReacts = createIndexMap(reactions);
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;		
		while ((line = reader.readLine()) != null) {
			try {
				Matcher matcher = BOUNDS_PTN.matcher(line);
				if (!matcher.matches()) {
					throw new IOException("syntac error in file '" + file.getAbsolutePath() + "': " + line);
				}
				String sReacts	= matcher.group(1);
				String sValue	= matcher.group(2);
				String[] reacts = sReacts.split(",");
				double value	= Double.parseDouble(sValue);
				if (reacts.length == 1 && reacts[0].equals("*")) {
					for (int ii = 0; ii < result.length; ii++) {
						result[ii] = value;
					}
				}
				else {
					for (String react : reacts) {
						int reactIndex = iReacts.get(react.trim()).intValue();
						result[reactIndex] = value;
					}					
				}
			}
			catch (Exception ex) {
				String msg = "cannot parse line: " + line;
				System.err.println(msg);
				ex.printStackTrace();
				throw new IOException(msg);
			}			
		}
		return result;
	}
	
	private static MetabolicNetwork createMetabolicNetwork(List<String> metas, List<String> reacts, double[][] stoich, double[] lower, double[] upper) {
		String[] aMetas = new String[metas.size()];
		aMetas = metas.toArray(aMetas);
		String[] aReacts = new String[reacts.size()];
		aReacts = reacts.toArray(aReacts);
		
		return new DefaultMetabolicNetwork(aMetas, aReacts, stoich, createReactionConstraints(lower, upper));
	}
	
	private static ReactionConstraints[] createReactionConstraints(double[] lower, double[] upper) {
		Map<ReactionConstraints, ReactionConstraints> reuse = new HashMap<ReactionConstraints, ReactionConstraints>();
		ReactionConstraints[] constr = new ReactionConstraints[lower.length];
		for (int ii = 0; ii < constr.length; ii++) {
			ReactionConstraints instance = new DefaultReactionConstraints(lower[ii], upper[ii]);
			if (!reuse.containsKey(instance)) {
				reuse.put(instance, instance);
			}
			else {
				instance = reuse.get(instance);
			}
			constr[ii] = instance;
		}
		return constr;
	}
	
	private static Map<String, Integer> createIndexMap(List<String> list) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (int index = 0; index < list.size(); index++) {
			map.put(list.get(index), Integer.valueOf(index));
		}
		return map;
	}
	
	// no instances
	private GamsParser() {
		super();
	}

}
