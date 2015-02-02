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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import junit.framework.TestCase;

import org.dom4j.DocumentException;

import ch.javasoft.io.Print;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.fa.FaConstants;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.impl.FilteredMetabolicNetwork;
import ch.javasoft.metabolic.parse.FluxAnalyserParser;
import ch.javasoft.metabolic.parse.GamsParser;
import ch.javasoft.metabolic.parse.PalssonParser;
import ch.javasoft.metabolic.parse.SbmlParser;
import ch.javasoft.util.logging.Loggers;

public class MatlabTest extends TestCase {
	static {
		try {
			Loggers.logToFile("/tmp/eclipse-console.log", Level.FINEST);
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static final File FOLDER_COMPRESS	= new File("../metabolic-data/palsson");
	private static final File FOLDER_MATLAB		= new File(FOLDER_COMPRESS, "matlab");
	
	
	public void testColi_All() throws IOException, DocumentException {
		internalTestFluxAnalyzer(
			FaConstants.SUBNET_ALL,
			new File(FOLDER_MATLAB, "fba_coli_all.m"),
			"mue"
		);
	}
	public void testPalsson_coli_iJR904_sbml() throws IOException, DocumentException {
		internalTestSbml(
			new File(FOLDER_COMPRESS, "coli/Ec_iJR904.xml"),
			new File(FOLDER_MATLAB, "fba_iJR904_sbml.m"),
			"Extra_organism", "R_BiomassEcoli"
		);
	}
	public void testPalsson_coli_iJR904_sbml_flux1() throws IOException, DocumentException {
		internalTestSbml(
			new File(FOLDER_COMPRESS, "coli/Ec_iJR904_flux1.xml"),
			new File(FOLDER_MATLAB, "fba_iJR904_sbml_flux1.m"),
			"Extra_organism", "R_BiomassEcoli"
		);
	}
	public void testPalsson_coli_iJR904_gams() throws IOException, DocumentException {
		internalTestGams(
			new File(FOLDER_COMPRESS, "coli"), "EcoliiJR904.gms",
			new File(FOLDER_MATLAB, "fba_EcoliiJR904.m"),
			"BiomassEcoli"
		);
	}
	public void testPalsson_coli_iJR904_xrev() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "coli/iJR904(7-28-04)-reacts-xrev.csv"),
			new File(FOLDER_MATLAB, "fba_iJR904_xrev.m"),
			"mue"

		);
	}
	public void testPalsson_coli_iJR904_xspec() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "coli/iJR904(7-28-04)-reacts-xspec.csv"),
			new File(FOLDER_MATLAB, "fba_iJR904_xspec.m"),
			"mue"

		);
	}
	public void testPalsson_coli_iJR904_xnone() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "coli/iJR904(7-28-04)-reacts-xnone.csv"),
			new File(FOLDER_MATLAB, "fba_iJR904_xnone.m"),
			"mue"

		);
	}
	public void testPalsson_coli_iJR904_xnone_nobio() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "coli/iJR904(7-28-04)-reacts-xnone_nobio.csv"),
			new File(FOLDER_MATLAB, "fba_iJR904_xnonenobio.m"),
			"mue"
		);
	}
	public void testPalsson_coli_Robert() throws IOException, DocumentException {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "coli/coliRobert.csv"),
			new File(FOLDER_MATLAB, "fba_coliRobert.m"),
			externalPattern, "biomass"
		);
	}
	public void testPalsson_aureus_iSB619_xrev() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "staph_aureus/iSB619-reacts-xrev.csv") ,
			new File(FOLDER_MATLAB, "fba_iSB619_xrev.m"),
			"biomass_SA_8a"
		);
	}
	public void testPalsson_aureus_iSB619_xspec() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "staph_aureus/iSB619-reacts-xspec.csv"),
			new File(FOLDER_MATLAB, "fba_iSB619_xspec.m"),
			"biomass_SA_8a"
		);
	}
	public void testPalsson_aureus_iSB619_xnone() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "staph_aureus/iSB619-reacts-xnone.csv"),
			new File(FOLDER_MATLAB, "fba_iSB619_xnone.m"),
			"mue"
		);
	}
	public void testPalsson_aureus_iMH556() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "staph_aureus/iMH556.csv"),
			new File(FOLDER_MATLAB, "fba_iMH556.m"),
			"BX"
		);
	}
	public void testPalsson_barkeri_iAF692_xspec() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "barkeri/mbarkeri-iAF692.csv"),
			new File(FOLDER_MATLAB, "fba_iAF692_xspec.m"),
			"biomass"
		);
	}
	public void testPalsson_pylori_iIT341_xspec() throws IOException {
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "pylori/iIT341.csv"),
			new File(FOLDER_MATLAB, "fba_iIT341_xspec.m"),
			"mue"
		);
	}
	public void testPalsson_yeastc_iND750() throws IOException {
		Pattern externalPattern = Pattern.compile(".*\\[e\\]");
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "yeast-cerevisiae/iND750-reacts.csv"),
			new File(FOLDER_MATLAB, "fba_iND750.m"),
//			null/*externalPattern*/
			externalPattern, "mue"
		);
	}
	public void testPalsson_yeastc_iLL672() throws IOException {
		Pattern externalPattern = Pattern.compile(".*xt");
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "yeast-cerevisiae/iLL672-reacts.csv"),
			new File(FOLDER_MATLAB, "fba_iLL672.m"),
			externalPattern, "biomass-1"
		);
	}
	public void testPalsson_yeastc_iLL672_unbalancedMetas() throws IOException {
		Pattern externalPattern = Pattern.compile(".*xt|TRE|GLYCOGEN|CLm|DEAC|PAP|RADP|SAMOB|THFG");
		internalTestPalsson(
			new File(FOLDER_COMPRESS, "yeast-cerevisiae/iLL672-reacts.csv"),
			new File(FOLDER_MATLAB, "fba_iLL672_unbalanced.m"),
			externalPattern, "biomass-1"
		);
	}
	public void testPalsson_yeastc_Dirk() throws IOException, DocumentException {
		internalTestSbml(
			new File(FOLDER_COMPRESS, "yeast-cerevisiae/Yeast_model_Dirk.xml"),
			new File(FOLDER_MATLAB, "fba_Yeast_model_Dirk.m"),
			"external", "biomass formation"
		);
	}
	private void internalTestPalsson(File inFile, File outFile, String biomassReactionName) throws IOException {
		internalTestPalsson(inFile, outFile, null, biomassReactionName);
	}
	private void internalTestPalsson(File inFile, File outFile, Pattern externalPattern, String biomassReactionName) throws IOException {
		Reaction[] reacts = externalPattern == null ? 
				new PalssonParser().parseReactions(inFile) : 
				new PalssonParser().parseReactions(inFile, externalPattern);
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(reacts);
		System.out.println("parsed network: " + netSize(metaNet));
		writeMatlabFile(metaNet, outFile, biomassReactionName);
	}
	private void internalTestSbml(File inFile, File outFile, String externalCompartment, String biomassReactionName) throws IOException, DocumentException {
		MetabolicNetwork metaNet = new SbmlParser(externalCompartment, false /*validate*/).parse(inFile);
		System.out.println("parsed network: " + netSize(metaNet));
		writeMatlabFile(metaNet, outFile, biomassReactionName);
	}
	private void internalTestGams(File inFolder, String inFileName, File outFile, String biomassReactionName) throws IOException {
		MetabolicNetwork metaNet = GamsParser.parse(inFolder, inFileName);
		System.out.println("parsed network: " + netSize(metaNet));
		writeMatlabFile(metaNet, outFile, biomassReactionName);
	}
	private void internalTestFluxAnalyzer(FaConstants.SubNet subNet, File outFile, String biomassReactionName) throws IOException {
		MetabolicNetwork net = FluxAnalyserParser.parse(FaConstants.COLI_FOLDER);
		FilteredMetabolicNetwork fNet = new FilteredMetabolicNetwork(net);
		fNet.excludeReactions(subNet.excludeReactions);
		System.out.println("parsed network: " + netSize(fNet));
		writeMatlabFile(fNet, outFile, biomassReactionName);
	}
	private void writeMatlabFile(MetabolicNetwork metaNet, File outFile, String biomassReactionName) throws IOException {
		if (outFile.exists()) {
			String ans = JOptionPane.showInputDialog("The file below already exists. \nYou can change the file name or overwrite it.", outFile.getAbsoluteFile());
			if (ans == null) {
				System.out.println("aborted.");
				return;
			}
			outFile = new File(ans);
		}
		PrintWriter pw = Print.createWriter(outFile);
		MatlabGenerator matlabGen = new MatlabGenerator("metanet");
		matlabGen.writeStoich(metaNet, pw, false /*expandReversible*/, true /*sparse*/);
		matlabGen.writeMetaNames(metaNet, pw);
		matlabGen.writeReactionNames(metaNet, pw);
		matlabGen.writeLowerBounds(metaNet, pw);
		matlabGen.writeUpperBounds(metaNet, pw);
		pw.flush();
		System.out.println("written matlab file: " + outFile.getAbsolutePath());		
	}
	private static String netSize(MetabolicNetwork net) {
		return net.getMetabolites().length() + " metabolites, " + net.getReactions().length() + " reactions";
	}
}
