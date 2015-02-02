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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.util.DoubleArray;

/**
 * 
 * The <tt>StoichParser</tt> parses 4 text files containing stoichiometric 
 * matrix, metabolite and reaction names and reaction reversibilities. 
 */
public class StoichParser {
	
	public static final Pattern PAT_LINE_PLAIN	 		= Pattern.compile("\\s*(.*)\\s*"); 
	
	public static final Pattern PAT_SEP_WHITE			= Pattern.compile("\\s+"); 
	public static final Pattern PAT_SEP_COMMA			= Pattern.compile("\\s*,\\s*"); 
	public static final Pattern PAT_SEP_SEMICOLON		= Pattern.compile("\\s*;\\s*"); 
	
	public static final Pattern PAT_STR_DOUBLE_QUOTED	= Pattern.compile("\"(.*)\""); 
	public static final Pattern PAT_STR_SINGLE_QUOTED	= Pattern.compile("'(.*)'"); 
	
	public static final Pattern PAT_1					= Pattern.compile("1"); 
	public static final Pattern PAT_0					= Pattern.compile("0"); 

	private final Pattern mPatStoichLine, mPatStoichSep;
	private final Pattern mPatNamesLine, mPatNamesSep, mPatNamesContent;
	private final Pattern mPatRevsLine, mPatRevsSep, mPatRevsTrue, mPatRevsFalse;
	public StoichParser(Pattern patStoichLine, Pattern patStoichSep, Pattern patNamesLine, Pattern patNamesSep, Pattern patNamesContent, Pattern patRevsLine, Pattern patRevsSep, Pattern patRevsTrue, Pattern patRevsFalse) {
		mPatStoichLine		= patStoichLine;
		mPatStoichSep		= patStoichSep;
		mPatNamesLine		= patNamesLine;
		mPatNamesSep		= patNamesSep;
		mPatNamesContent	= patNamesContent;
		mPatRevsLine		= patRevsLine;
		mPatRevsSep			= patRevsSep;
		mPatRevsTrue		= patRevsTrue;
		mPatRevsFalse		= patRevsFalse;
	}
	
	public static StoichParser getSeparatorStoichParser(String separator) {
		Pattern pattern = Pattern.compile("\\s*[" + separator + "]\\s*");
		return new StoichParser(
			PAT_LINE_PLAIN, pattern, 
			PAT_LINE_PLAIN, pattern, PAT_STR_DOUBLE_QUOTED, 
			PAT_LINE_PLAIN, pattern, PAT_1, PAT_0			
		);
	}
	public static StoichParser getCommaSeparatedStoichParser() {
		return new StoichParser(
			PAT_LINE_PLAIN, PAT_SEP_COMMA, 
			PAT_LINE_PLAIN, PAT_SEP_COMMA, PAT_STR_DOUBLE_QUOTED, 
			PAT_LINE_PLAIN, PAT_SEP_COMMA, PAT_1, PAT_0			
		);
	}
	public static StoichParser getWhitespaceSeparatedStoichParser() {
		return new StoichParser(
			PAT_LINE_PLAIN, PAT_SEP_WHITE, 
			PAT_LINE_PLAIN, PAT_SEP_WHITE, PAT_STR_DOUBLE_QUOTED, 
			PAT_LINE_PLAIN, PAT_SEP_WHITE, PAT_1, PAT_0			
		);
	}
	public static StoichParser getSemicolonSeparatedStoichParser() {
		return new StoichParser(
			PAT_LINE_PLAIN, PAT_SEP_SEMICOLON, 
			PAT_LINE_PLAIN, PAT_SEP_SEMICOLON, PAT_STR_DOUBLE_QUOTED, 
			PAT_LINE_PLAIN, PAT_SEP_SEMICOLON, PAT_1, PAT_0
		);
	}
	
	public MetabolicNetwork parse(BufferedReader stoichReader, BufferedReader metaReader, BufferedReader reacReader, BufferedReader revReader) throws IOException {
		double[][] stoich	= parseStoich(stoichReader);
		String[] metaNames	= parseMetaboliteNames(metaReader);
		String[] reacNames	= parseReactionNames(reacReader);
		boolean[] revs		= parseReactionReversibilities(revReader);
		int cntM = stoich.length;
		int cntR = cntM == 0 ? 0 : stoich[0].length;
		if (cntM != metaNames.length) {
			throw new IOException("expected " + cntM + " metabolite names, but found " + metaNames.length);
		}
		if (cntR != reacNames.length) {
			throw new IOException("expected " + cntR + " reaction names, but found " + reacNames.length);
		}
		if (cntR != revs.length) {
			throw new IOException("expected " + cntR + " reversibilities, but found " + revs.length);
		}
		return new DefaultMetabolicNetwork(metaNames, reacNames, stoich, revs);
	}

	public String[] parseReactionNames(BufferedReader reacReader) throws IOException {
		return parseNames(reacReader);
	}

	public String[] parseMetaboliteNames(BufferedReader metaReader) throws IOException {
		return parseNames(metaReader);
	}

	public boolean[] parseReactionReversibilities(BufferedReader reader) throws IOException {
		String line = null;
		int lineNo = 0;
		try {
			line = reader.readLine();
			if (line == null) throw new IOException("unexpected end of file");
			lineNo++;
			String content;
			content = getContent(mPatRevsLine, line);
			String[] values = mPatRevsSep.split(content);
			boolean[] revs	= new boolean[values.length];
			for (int i = 0; i < revs.length; i++) {
				if (mPatRevsTrue.matcher(values[i]).matches()) revs[i] = true;
				else if (mPatRevsFalse.matcher(values[i]).matches()) revs[i] = false;
				else throw new IOException("cannot parse boolean value " + values[i]);
			}
			line = reader.readLine();
			if (line != null) {
				LogPkg.LOGGER.warning("extra lines found, will be ignored");
				LogPkg.LOGGER.warning("LINE: " + line);
			}
			return revs;
		}
		catch(IOException ex) {
			String msg = ex.getLocalizedMessage() + " [line " + lineNo + "]";
			LogPkg.LOGGER.warning(msg);
			LogPkg.LOGGER.warning("LINE: " + line);
			throw new IOException(msg);
		}		
	}
	private String[] parseNames(BufferedReader reader) throws IOException {
		String line = null;
		int lineNo = 0;
		try {
			line = reader.readLine();
			if (line == null) throw new IOException("unexpected end of file");
			lineNo++;
			String content;
			content = getContent(mPatNamesLine, line);
			String[] values = mPatNamesSep.split(content);
			String[] names = new String[values.length];
			for (int i = 0; i < names.length; i++) {
				names[i] = getContent(mPatNamesContent, values[i]);
			}
			line = reader.readLine();
			if (line != null) {
				LogPkg.LOGGER.warning("extra lines found, will be ignored");
				LogPkg.LOGGER.warning("LINE: " + line);
			}
			return names;
		}
		catch(IOException ex) {
			String msg = ex.getLocalizedMessage() + " [line " + lineNo + "]";
			LogPkg.LOGGER.warning(msg);
			LogPkg.LOGGER.warning("LINE: " + line);
			throw new IOException(msg);
		}		
	}

	public double[][] parseStoich(BufferedReader stoichReader) throws IOException {
		String line = null;
		int lineNo = 0;
		
		int cols = -1;
		try {
			List<DoubleArray> rows = new ArrayList<DoubleArray>(); 
			while ((line = stoichReader.readLine()) != null) {
				lineNo++;
				String content;
				content = getContent(mPatStoichLine, line);
				String[] values = mPatStoichSep.split(content);
				if (values.length != cols) {
					if (cols == -1) cols = values.length;
					else {
						LogPkg.LOGGER.warning("expected " + cols + " values, but found " + values.length + " [line " + lineNo + "]");
						cols = Math.max(cols, values.length);
					}
				}
				DoubleArray row = new DoubleArray(values.length);
				for (int i = 0; i < values.length; i++) {
					try {row.set(i, Double.parseDouble(values[i]));}
					catch(NumberFormatException ex) {
						LogPkg.LOGGER.warning(ex.toString());
						throw new IOException("cannot parse numeric value " + values[i]);
					}
				}
				rows.add(row);
			}
			double[][] stoich = new double[rows.size()][cols];
			for (int row = 0; row < stoich.length; row++) {
				DoubleArray vals = rows.get(row);
				for (int col = 0; col < vals.length(); col++) {
					stoich[row][col] = vals.get(col);
				}
			}
			return stoich;
		}
		catch(IOException ex) {
			String msg = ex.getLocalizedMessage() + " [line " + lineNo + "]";
			LogPkg.LOGGER.warning(msg);
			LogPkg.LOGGER.warning("LINE: " + line);
			throw new IOException(msg);
		}
	}

	private static String getContent(Pattern contentPattern, String line) throws IOException {
		Matcher lineMatcher = contentPattern.matcher(line);
		if (!lineMatcher.matches()) {
			throw new IOException("syntax error, expected pattern: " + contentPattern.pattern());
		}
		String content;
		try {content = lineMatcher.group(1);}
		catch (IndexOutOfBoundsException ex) {throw new IOException("pattern must contain a group 1");}
		return content;
	}
}
