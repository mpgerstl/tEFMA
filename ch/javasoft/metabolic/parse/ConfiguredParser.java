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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.DocumentException;
import org.dom4j.Element;

import at.acib.thermodynamic.ThermodynamicParameters;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;
import ch.javasoft.xml.config.ConstConfigParser;
import ch.javasoft.xml.config.FileConfigParser;
import ch.javasoft.xml.config.StreamConfigParser;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlNode;
import ch.javasoft.xml.config.XmlUtil;

/**
 * The <tt>ConfiguredParser</tt> reads from the xml config what to parse (which
 * format, which input sources and parse options).
 * 
 * Snipplet from such a config file:
 * 
 * <pre>
 * 		<parse type="flux-analyzer">
 * 			<input name="directory" type="file">							
 * 				<file name="{work-dir}/{-in[1]}"/>	<!-- directory, containing files "reactions", "macromolecules", "macromolecule_synthesis" -->
 * 			</input>
 * 		</parse>
 * 		<parse type="flux-analyzer">
 * 			<input name="reactions-file" type="file">							
 * 				<file name="{work-dir}/{-in[1]}"/>	<!-- reactions -->
 * 			</input>
 * 			<input name="macromolecules-file" type="file">							
 * 				<file name="{work-dir}/{-in[2]}"/>	<!-- macromolecules -->
 * 			</input>
 * 			<input name="macromolecule-synthesis-file" type="file">							
 * 				<file name="{work-dir}/{-in[3]}"/>	<!-- macromolecule_synthesis -->
 * 			</input>
 * 		</parse>
 * 		<parse type="reaction-list">
 * 			<input name="text-file" type="file">					
 * 				<file name="{work-dir}/{-in[1]}"/>
 * 			</input>
 * 		</parse>
 * 		<parse type="reaction-excel">
 * 			<input name="excel-file" type="file">					
 * 				<file name="{work-dir}/{-in[1]}"/>	<!-- excel file -->
 * 			</input>
 * 			<input name="excel-sheet" type="const">
 * 				<const value="Sheet-1" type="String"/>
 * <!--				<const value="1" type="int"/>	<!-- 1 based -->
 * 			</input>
 * 			<input name="reaction-column" type="const">
 * 				<const value="1" type="int"/>	<!-- 1 based -->
 * 			</input>
 * 			<input name="reaction-name-column" type="const">
 * 				<const value="1" type="int"/>	<!-- 1 based -->
 * 			</input>
 * 			<input name="header-rows" type="const">
 * 				<const value="1" type="int"/>	<!-- 1 based -->
 * 			</input>
 * 			<external pattern="xt.*"/>
 * 		</parse>
 * 		<parse type="stoichiometry">
 * 			<input name="stoichiometry-file" type="file">
 * 				<file name="{work-dir}/{-stoich[1]}"/>	<!-- stoichiometric matrix -->
 * 			</input>
 * 			<input name="reversibilities-file" type="file">
 * 				<file name="{work-dir}/{-rev[1]}"/>	<!-- reaction reversibilities -->
 * 			</input>
 * 			<input name="metabolite-names-file" type="file">
 * 				<file name="{work-dir}/{-meta[1]}"/>	<!-- metabolite names -->
 * 			</input>
 * 			<input name="reaction-names-file" type="file">
 * 				<file name="{work-dir}/{-reac[1]}"/>	<!-- reaction names -->
 * 			</input>
 * 			<separator value="{-sep[1]: }"/>
 * 		</parse>
 * 		<parse type="sbml">
 * 			<input type="file">
 * 				<file name="{work-dir}/{-in[1]}"/>
 * 			</input>
 * 			<external-compartment name="external"/>
 * 			<sbml-validate-schema>
 * 				<const value="true" type="boolean"/>
 * 			</sbml-validate-schema>			
 * 		</parse>
 * </pre>
 */
public class ConfiguredParser {
	public static enum XmlElements implements XmlNode {
		metabolic_parse, parse, input, file, const_, separator, generulefile, rulesort, loopremoval, external, external_compartment, sbml_validate_schema, test, drgub, cmin, cmax, temperature, ph, ionstrength, thermothreads, concentration_file, thermodynamic_file, pattern_file, lp_file, lpvar_file, proton, thermomanner;
		public String getXmlName() {
			return this == const_ ? "const" : name().replaceAll("_", "-");
		}
	}

	public static enum XmlAttributes implements XmlNode {
		type, name, value, pattern, validate, class_, method;
		public String getXmlName() {
			return this == class_ ? "class" : name();
		}
	}

	public static enum ParseType {
		flux_analyzer, reaction_list, reaction_excel, stoichiometry, sbml, junit;
		String getXmlName() {
			return name().replaceAll("_", "-");
		}

		static ParseType find(String type) {
			for (ParseType pType : values()) {
				if (pType.getXmlName().equals(type))
					return pType;
			}
			return null;
		}
	}

	public static enum FluxAnalyzerFileType {
		directory, reactions_file, macromolecules_file, macromolecule_synthesis_file;
		String getXmlName() {
			return name().replaceAll("_", "-");
		}

		static FluxAnalyzerFileType find(String type) {
			for (FluxAnalyzerFileType fType : values()) {
				if (fType.getXmlName().equals(type))
					return fType;
			}
			return null;
		}
	}

	public static enum ExcelInputType {
		excel_file, excel_sheet, reaction_column, reaction_name_column, header_rows;
		String getXmlName() {
			return name().replaceAll("_", "-");
		}

		static ExcelInputType find(String type) {
			for (ExcelInputType eType : values()) {
				if (eType.getXmlName().equals(type))
					return eType;
			}
			return null;
		}
	}

	public static enum StoichInputType {
		stoichiometry_file, reversibilities_file, metabolite_names_file, reaction_names_file, gene_rules_file;
		String getXmlName() {
			return name().replaceAll("_", "-");
		}

		static StoichInputType find(String type) {
			for (StoichInputType sType : values()) {
				if (sType.getXmlName().equals(type))
					return sType;
			}
			return null;
		}
	}

	/**
	 * @param parentElement
	 *            the element which contains the metabolic_parse child
	 * @throws XmlConfigException
	 *             if an xml configuration exception occurs, for instance due to
	 *             invalid xml structure
	 * @throws IOException
	 *             if an i/o exception occurs, for instance caused by file
	 *             access
	 */
	public static MetabolicNetwork parseConfig(Element parentElement) throws XmlConfigException, IOException {
		Element parseElement = XmlUtil.getRequiredSingleChildElement(parentElement, XmlElements.metabolic_parse);
		return parse(parseElement);
	}

	public static MetabolicNetwork parse(Element metabolicParseElement) throws XmlConfigException, IOException {
		XmlUtil.checkExpectedElementName(metabolicParseElement, XmlElements.metabolic_parse);
		Element parseElement = XmlUtil.getRequiredSingleChildElement(metabolicParseElement, XmlElements.parse);
		String type = parseElement.attributeValue(XmlAttributes.type.getXmlName());
		ParseType pType = ParseType.find(type);
		if (pType == null) {
			throw new XmlConfigException("unknown parse type '" + type + "'", parseElement);
		}
		switch (pType) {
		case flux_analyzer:
			return parseFluxAnalyzer(parseElement);
		case reaction_list:
			return parseReactionList(parseElement);
		case reaction_excel:
			return parseReactionExcel(parseElement);
		case stoichiometry:
			return parseStoichiometry(parseElement);
		case sbml:
			return parseSbml(parseElement);
		case junit:
			return parseJUnit(parseElement);
		default:
			// should not happen
			throw new XmlConfigException("internal error: unknown parse type " + pType, parseElement);
		}
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<parse type="flux-analyzer">
	 * 			<input name="directory" type="file">							
	 * 				<file name="{work-dir}/{-in[1]}"/>	<!-- directory, containing files "reactions", "macromolecules", "macromolecule_synthesis" -->
	 * 			</input>
	 * 		</parse>
	 * 		<parse type="flux-analyzer">
	 * 			<input name="reactions-file" type="file">							
	 * 				<file name="{work-dir}/{-in[1]}"/>	<!-- reactions -->
	 * 			</input>
	 * 			<input name="macromolecules-file" type="file">							
	 * 				<file name="{work-dir}/{-in[2]}"/>	<!-- macromolecules -->
	 * 			</input>
	 * 			<input name="macromolecule-synthesis-file" type="file">							
	 * 				<file name="{work-dir}/{-in[3]}"/>	<!-- macromolecule_synthesis -->
	 * 			</input>
	 * 		</parse>
	 * </pre>
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static MetabolicNetwork parseFluxAnalyzer(Element parseElement) throws XmlConfigException, IOException {
		List<Element> inputs = parseElement.elements(XmlElements.input.getXmlName());
		if (inputs.size() == 1) {
			Element input = inputs.get(0);
			XmlUtil.checkExpectedAttributeValue(input, XmlAttributes.name, FluxAnalyzerFileType.directory.getXmlName());
			Element fileEl = XmlUtil.getRequiredSingleChildElement(input, XmlElements.file);
			File file = FileConfigParser.parseFile(fileEl);
			return FluxAnalyserParser.parse(file);
		} else if (inputs.size() == 3) {
			Element inReactions = XmlUtil
					.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, FluxAnalyzerFileType.reactions_file.getXmlName(), true /* throwExceptionIfNull */);
			Element inMacromolecules = XmlUtil
					.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, FluxAnalyzerFileType.macromolecules_file.getXmlName(), true /* throwExceptionIfNull */);
			Element inMacromolSynth = XmlUtil
					.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, FluxAnalyzerFileType.macromolecule_synthesis_file.getXmlName(), true /* throwExceptionIfNull */);
			InputStream stReactions = StreamConfigParser.parseInputStream(inReactions);
			InputStream stMacromolecules = StreamConfigParser.parseInputStream(inMacromolecules);
			InputStream stMacromolSynth = StreamConfigParser.parseInputStream(inMacromolSynth);
			return FluxAnalyserParser.parse(stReactions, stMacromolecules, stMacromolSynth);
		} else {
			throw new XmlConfigException("expected input directory or 3 input files for flux analyzer parse config", parseElement);
		}
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<parse type="reaction-list">
	 * 			<input name="text-file" type="file">					
	 * 				<file name="{work-dir}/{-in[1]}"/>
	 * 			</input>
	 * 			<external pattern=""/>
	 * 		</parse>
	 * </pre>
	 */
	private static MetabolicNetwork parseReactionList(Element parseElement) throws XmlConfigException, IOException {
		Element input = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.input);
		Pattern externalPat = parseExternalPattern(parseElement);
		InputStream in = StreamConfigParser.parseInputStream(input);
		Reaction[] reacts = externalPat == null ? new PalssonParser().parseReactions(in) : new PalssonParser().parseReactions(in, externalPat);
		return new DefaultMetabolicNetwork(reacts);
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<parse type="reaction-excel">
	 * 			<input name="excel-file" type="file">					
	 * 				<file name="{work-dir}/{-in[1]}"/>	<!-- excel file -->
	 * 			</input>
	 * 			<input name="excel-sheet" type="const">
	 * 				<const value="Sheet-1" type="String"/>
	 * <!--				<const value="1" type="int"/>	<!-- 1 based -->
	 * 			</input>
	 * 			<input name="reaction-column" type="const">
	 * 				<const value="1" type="int"/>	<!-- 1 based -->
	 * 			</input>
	 * 			<input name="reaction-name-column" type="const">
	 * 				<const value="1" type="int"/>	<!-- 1 based -->
	 * 			</input>
	 * 			<input name="header-rows" type="const">
	 * 				<const value="1" type="int"/>	<!-- 1 based -->
	 * 			</input>
	 * 			<external pattern="xt.*"/>
	 * 		</parse>
	 * </pre>
	 */
	private static MetabolicNetwork parseReactionExcel(Element parseElement) throws XmlConfigException, IOException {
		Element inputFile = XmlUtil.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, ExcelInputType.excel_file.getXmlName(), true /* throwExceptionIfNull */);
		Element inputSheet = XmlUtil.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, ExcelInputType.excel_sheet.getXmlName(), false /* throwExceptionIfNull */);
		Element inputRCol = XmlUtil.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, ExcelInputType.reaction_column.getXmlName(), true /* throwExceptionIfNull */);
		Element inputRNCol = XmlUtil
				.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, ExcelInputType.reaction_name_column.getXmlName(), true /* throwExceptionIfNull */);
		Element inputHRows = XmlUtil.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, ExcelInputType.header_rows.getXmlName(), true /* throwExceptionIfNull */);
		Pattern externalPat = parseExternalPattern(parseElement);
		int sheetIndex = 0;
		String sheetName = null;
		if (inputSheet != null) {
			Element elConst = XmlUtil.getRequiredSingleChildElement(inputSheet, XmlElements.const_);
			try {
				sheetIndex = ConstConfigParser.parseIntConstant(elConst);
			} catch (Exception e) {
				try {
					sheetName = ConstConfigParser.parseStringConstant(elConst, false /* allowNull */);
				} catch (Exception e2) {
					throw new XmlConfigException("string or int value expected for " + ExcelInputType.excel_sheet + ", e=" + e2, elConst);
				}
			}
		}
		// InputStream in = StreamConfigParser.parseInputStream(inputFile);
		Element elFile = XmlUtil.getRequiredSingleChildElement(inputFile, XmlElements.file);
		File file = FileConfigParser.parseFile(elFile);
		ExcelParser parser = sheetName == null ? new ExcelParser(file, sheetIndex - 1 /*
																					 * 0
																					 * based
																					 * in
																					 * ExcelParser
																					 */) : new ExcelParser(file, sheetName);

		int reactCol = ConstConfigParser.parseIntConstant(XmlUtil.getRequiredSingleChildElement(inputRCol, XmlElements.const_));
		int reactNameColumn = ConstConfigParser.parseIntConstant(XmlUtil.getRequiredSingleChildElement(inputRNCol, XmlElements.const_));
		int headerRows = ConstConfigParser.parseIntConstant(XmlUtil.getRequiredSingleChildElement(inputHRows, XmlElements.const_));
		if (externalPat == null) {
			return parser.parse(reactCol, reactNameColumn, headerRows);
		} else {
			return parser.parse(reactCol - 1 /* 0 based in ExcelParser */, reactNameColumn - 1 /*
																							 * 0
																							 * based
																							 * in
																							 * ExcelParser
																							 */, headerRows, externalPat);
		}
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<parse type="stoichiometry">
	 * 			<input name="stoichiometry-file" type="file">
	 * 				<file name="{work-dir}/{-stoich[1]}"/>	<!-- stoichiometric matrix -->
	 * 			</input>
	 * 			<input name="reversibilities-file" type="file">
	 * 				<file name="{work-dir}/{-rev[1]}"/>	<!-- reaction reversibilities -->
	 * 			</input>
	 * 			<input name="metabolite-names-file" type="file">
	 * 				<file name="{work-dir}/{-meta[1]}"/>	<!-- metabolite names -->
	 * 			</input>
	 * 			<input name="reaction-names-file" type="file">
	 * 				<file name="{work-dir}/{-reac[1]}"/>	<!-- reaction names -->
	 * 			</input>
	 *                         <generulefile   value="${-generule[1]: }"/>
	 * 			<separator value="{-sep[1]: }"/>
	 * 		</parse>
	 * </pre>
	 * 
	 * @throws XmlConfigException
	 * @throws IOException
	 */
	private static MetabolicNetwork parseStoichiometry(Element parseElement) throws XmlConfigException, IOException {
		Element elStoich = XmlUtil.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, StoichInputType.stoichiometry_file.getXmlName(), true /* throwExceptionIfNull */);
		Element elRev = XmlUtil.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, StoichInputType.reversibilities_file.getXmlName(), false /* throwExceptionIfNull */);
		Element elMetaNames = XmlUtil
				.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, StoichInputType.metabolite_names_file.getXmlName(), true /* throwExceptionIfNull */);
		Element elReacNames = XmlUtil
				.getChildElementByAttributeValue(parseElement, XmlElements.input, XmlAttributes.name, StoichInputType.reaction_names_file.getXmlName(), true /* throwExceptionIfNull */);

		// parse thermodynamic information
		Element elDrgub = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.drgub);
		String drgub = XmlUtil.getRequiredAttributeValue(elDrgub, XmlAttributes.value);
		drgub = drgub.trim();
		if (!drgub.isEmpty()) {
			double temp = Double.valueOf(drgub);
            if (temp >= 0) {
                temp *= -1;
                ThermodynamicParameters.setDrgUb(temp);
            }
		}
		Element elCmin = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.cmin);
		String cmin = XmlUtil.getRequiredAttributeValue(elCmin, XmlAttributes.value);
		cmin = cmin.trim();
		if (!cmin.isEmpty()) {
			double temp = Double.valueOf(cmin);
			if (temp > 0) {
				ThermodynamicParameters.setStdMin(temp);
			}
		}
		Element elCmax = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.cmax);
		String cmax = XmlUtil.getRequiredAttributeValue(elCmax, XmlAttributes.value);
		cmax = cmax.trim();
		if (!cmax.isEmpty()) {
			double temp = Double.valueOf(cmax);
			if (temp > 0) {
				ThermodynamicParameters.setStdMax(temp);
			}
		}
		Element elTemperature = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.temperature);
		String temperature = XmlUtil.getRequiredAttributeValue(elTemperature, XmlAttributes.value);
		temperature = temperature.trim();
		if (!temperature.isEmpty()) {
			double temp = Double.valueOf(temperature);
			if (temp > 0) {
				ThermodynamicParameters.setTemperature(temp);
			}
		}
		Element elPH = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.ph);
		String ph = XmlUtil.getRequiredAttributeValue(elPH, XmlAttributes.value);
		ph = ph.trim();
		if (!ph.isEmpty()) {
			double temp = Double.valueOf(ph);
			if (temp > 0) {
				ThermodynamicParameters.setPH(temp);
			}
		}
		Element elIonStrength = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.ionstrength);
		String ionStrength = XmlUtil.getRequiredAttributeValue(elIonStrength, XmlAttributes.value);
		ionStrength = ionStrength.trim();
		if (!ionStrength.isEmpty()) {
			double temp = Double.valueOf(ionStrength);
			if (temp > 0) {
				ThermodynamicParameters.setIonicStrength(temp);
			}
		}
		Element elThermoThreads = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.thermothreads);
		String thermothreads = XmlUtil.getRequiredAttributeValue(elThermoThreads, XmlAttributes.value);
		thermothreads = thermothreads.trim();
		if (!thermothreads.isEmpty()) {
			int temp = Integer.valueOf(thermothreads);
			if (temp > 0) {
				ThermodynamicParameters.setThermoThreads(temp);
			}
		}
		Element elThermomanner = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.thermomanner);
		String thermomanner = XmlUtil.getRequiredAttributeValue(elThermomanner, XmlAttributes.value);
		thermomanner = thermomanner.trim();
		if (!thermomanner.isEmpty()) {
			ThermodynamicParameters.setManner(thermomanner);
		}
		Element elConcFile = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.concentration_file);
		String concentration_file = XmlUtil.getRequiredAttributeValue(elConcFile, XmlAttributes.value);
		concentration_file = concentration_file.trim();
		if (!concentration_file.isEmpty()) {
			ThermodynamicParameters.setConcentrationFile(concentration_file);
		}
		Element elThermFile = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.thermodynamic_file);
		String thermodynamic_file = XmlUtil.getRequiredAttributeValue(elThermFile, XmlAttributes.value);
		thermodynamic_file = thermodynamic_file.trim();
		if (!thermodynamic_file.isEmpty()) {
			ThermodynamicParameters.setThermodynamicFile(thermodynamic_file);
		}
		Element elPatternFile = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.pattern_file);
		String pattern_file = XmlUtil.getRequiredAttributeValue(elPatternFile, XmlAttributes.value);
		pattern_file = pattern_file.trim();
		if (!pattern_file.isEmpty()) {
			ThermodynamicParameters.setInfeasiblePatternFile(pattern_file);
		}
		Element elLpFile = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.lp_file);
		String lp_file = XmlUtil.getRequiredAttributeValue(elLpFile, XmlAttributes.value);
		lp_file = lp_file.trim();
		if (!lp_file.isEmpty()) {
			ThermodynamicParameters.setLpFile(lp_file);
		}
		Element elLpVarFile = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.lpvar_file);
		String lpvar_file = XmlUtil.getRequiredAttributeValue(elLpVarFile, XmlAttributes.value);
		lpvar_file = lpvar_file.trim();
		if (!lpvar_file.isEmpty()) {
			ThermodynamicParameters.setLpVariableFile(lpvar_file);
		}
		Element elProton = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.proton);
		String proton = XmlUtil.getRequiredAttributeValue(elProton, XmlAttributes.value);
		proton = proton.trim();
		if (!proton.isEmpty()) {
			ThermodynamicParameters.setProton(proton);
		}
		// end of modification by matthias
		// ==============================================

		Element elSeparator = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.separator);
		String separator = XmlUtil.getRequiredAttributeValue(elSeparator, XmlAttributes.value);

		BufferedReader rdStoich = toBufferedReader(StreamConfigParser.parseInputStream(elStoich));
		BufferedReader rdRev = toBufferedReader(StreamConfigParser.parseInputStream(elRev));
		BufferedReader rdMetaNames = toBufferedReader(StreamConfigParser.parseInputStream(elMetaNames));
		BufferedReader rdReacNames = toBufferedReader(StreamConfigParser.parseInputStream(elReacNames));

		StoichParser parser;
		if (separator.trim().length() == 0) {
			parser = StoichParser.getWhitespaceSeparatedStoichParser();
		} else {
			parser = StoichParser.getSeparatorStoichParser(separator);
		}

		return parser.parse(rdStoich, rdMetaNames, rdReacNames, rdRev);
	}

	private static final BufferedReader toBufferedReader(InputStream in) {
		return new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<parse type="sbml">
	 * 			<input type="file">
	 * 				<file name="{work-dir}/{-in[1]}"/>
	 * 			</input>
	 * 			<external-compartment name="external"/>
	 * 			<sbml-validate-schema>
	 * 				<const value="true" type="boolean"/>
	 * 			</sbml-validate-schema>			
	 * 		</parse>
	 * </pre>
	 * 
	 * @throws XmlConfigException
	 * @throws IOException
	 */
	private static MetabolicNetwork parseSbml(Element parseElement) throws XmlConfigException, IOException {
		Element elInput = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.input);
		Element elExtCmp = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.external_compartment);
		Element elSbmlVal = parseElement.element(XmlElements.sbml_validate_schema.getXmlName());
		String extCmp = XmlUtil.getRequiredAttributeValue(elExtCmp, XmlAttributes.name);
		boolean validate = true;
		if (elSbmlVal != null) {
			Element cnst = XmlUtil.getRequiredSingleChildElement(elSbmlVal, XmlElements.const_);
			validate = ConstConfigParser.parseBooleanConstant(cnst);
		}
		InputStream in = StreamConfigParser.parseInputStream(elInput);
		try {
			return new SbmlParser(extCmp, validate).parse(in);
		} catch (DocumentException ex) {
			throw new IOException("cannot parse sbml document, e=" + ex);
		}
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<parse type="junit">
	 * 			<test class="JUnitTestClass" method="methodName"/>
	 * 		</parse>
	 * </pre>
	 * 
	 * @throws XmlConfigException
	 */
	private static MetabolicNetwork parseJUnit(Element parseElement) throws XmlConfigException {
		Element elTest = XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.test);
		String className = XmlUtil.getRequiredAttributeValue(elTest, XmlAttributes.class_);
		String methodName = XmlUtil.getRequiredAttributeValue(elTest, XmlAttributes.method);

		String errMsg;
		Exception ex;
		try {
			AbstractParseTestCase testCase = (AbstractParseTestCase) Class.forName(className).newInstance();
			MetabolicNetwork net = JUnitTestCaseParser.parse(testCase, methodName);
			if (net != null)
				return net;
			errMsg = "unit test method " + className + "." + methodName + " did not yield a metabolic network";
			ex = null;
		} catch (ClassNotFoundException e) {
			errMsg = "no such unit test: " + className;
			ex = e;
		} catch (InstantiationException e) {
			errMsg = "cannot instantiate unit test " + className + ", e=" + e;
			ex = e;
		} catch (Exception e) {
			errMsg = "cannot invoke unit test method " + className + "." + methodName + ", e=" + e;
			ex = e;
		}
		final XmlConfigException xex = new XmlConfigException(errMsg, elTest);
		if (ex != null)
			xex.initCause(ex);
		throw xex;
	}

	/**
	 * Parses:
	 * 
	 * <pre>
	 * 		<external pattern="xt.*"/>
	 * </pre>
	 */
	private static Pattern parseExternalPattern(Element parseElement) throws XmlConfigException {
		Element external = parseElement.element(XmlElements.external.getXmlName());
		Pattern externalPat = null;
		if (external != null) {
			String patString = external.attributeValue(XmlAttributes.pattern.getXmlName());
			try {
				externalPat = Pattern.compile(patString);
			} catch (Exception ex) {
				throw new XmlConfigException("cannot parse external pattern '" + patString + "', e=" + ex, external, ex);
			}
		}
		return externalPat;
	}

}
