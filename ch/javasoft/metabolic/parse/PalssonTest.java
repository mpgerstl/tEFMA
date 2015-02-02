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

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

import ch.javasoft.io.Files;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;
import ch.javasoft.util.Arrays;

abstract public class PalssonTest extends AbstractParseTestCase {
	
	protected static final File FOLDER_IN	= new File("../metabolic-data/palsson/");
	protected static final File FOLDER_OUT	= Files.getTempDir();
	
	protected ThreadLocal<File> fileOut = new ThreadLocal<File>();
	
	public void testPalsson_coli_iJR904_xrev() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904(7-28-04)-reacts-xrev.csv"),
			new File(FOLDER_OUT, "iJR904_xrev.m")
		);
	}
	public void testPalsson_coli_iJR904_xspec() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904-reacts-xspec.csv"),
			new File(FOLDER_OUT, "iJR904_xspec.m")
		);
	}
	public void testPalsson_coli_iJR904_xnone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904(7-28-04)-reacts-xnone.csv"),
			new File(FOLDER_OUT, "iJR904_xnone.m")
		);
	}
	public void testPalsson_coli_iJR904_minimal() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904(7-28-04)-reacts-minimal.csv"),
			new File(FOLDER_OUT, "iJR904_minimal.m")
		);
	}
	public void testPalsson_coli_iJR904_xnone_nobio() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904(7-28-04)-reacts-xnone_nobio.csv"),
			new File(FOLDER_OUT, "iJR904_xnonenobio.m")
		);
	}
	public void testPalsson_coli_iJR904_fba_reduced_ac() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904-fba-reduced_ac.csv"),
			new File(FOLDER_OUT, "iJR904_fba_reduced_ac.m")
		);
	}
	public void testPalsson_coli_iJR904_fba_reduced() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904-fba-reduced.csv"),
			new File(FOLDER_OUT, "iJR904_fba_reduced.m")
		);
	}
	public void testPalsson_coli_iJR904_fba_reduced_xnone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "coli/iJR904-fba-reduced_xnone.csv"),
			new File(FOLDER_OUT, "iJR904_fba_reduced_xnone.m")
		);
	}
	public void testPalsson_coli_Robert() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestPalsson(
			new File(FOLDER_IN, "coli/coliRobert.csv"),
			new File(FOLDER_OUT, "coliRobert.m"),
			externalPattern, null
		);
	}
	public void testPalsson_coli_Robert_reduced() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestPalsson(
			new File(FOLDER_IN, "coli/coliRobert.csv"),
			new File(FOLDER_OUT, "coliRobert.m"),
			externalPattern, new String[] {"extract_SUCC", "extract_PYR", "focA", "mglABC", "maint_ATP"}
		);
	}
	public void testPalsson_subtilisPalsson07() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "subtilis/bacillus-subtilis-palsson-07.csv"),
			new File(FOLDER_OUT, "subtilisPalsson07.m")
		);
	}

	public void testPalsson_aureus_iSB619_xrev() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "staph_aureus/iSB619-reacts-xrev.csv") ,
			new File(FOLDER_OUT, "iSB619_xrev.m")
		);
	}
	public void testPalsson_aureus_iSB619_xspec() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "staph_aureus/iSB619-reacts-xspec.csv"),
			new File(FOLDER_OUT, "iSB619_xspec.m")
		);
	}
	public void testPalsson_aureus_iSB619_xnone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "staph_aureus/iSB619-reacts-xnone.csv"),
			new File(FOLDER_OUT, "iSB619_xnone.m")
		);
	}
	public void testPalsson_aureus_iSB619_opt() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "staph_aureus/iSB619-opt.csv"),
			new File(FOLDER_OUT, "iSB619_opt.m")
		);
	}
	public void testPalsson_aureus_iMH556() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "staph_aureus/iMH556.csv"),
			new File(FOLDER_OUT, "iMH556.m")
		);
	}
	public void testPalsson_barkeri_iAF692_xspec() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "barkeri/mbarkeri-iAF692.csv"),
			new File(FOLDER_OUT, "iAF692_xspec.m")
		);
	}
	public void testPalsson_barkeri_iAF692_opt() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "barkeri/iAF692-opt.csv"),
			new File(FOLDER_OUT, "iAF692_opt.m")
		);
	}	
	public void testPalsson_pylori_iIT341_xspec() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341.csv"),
			new File(FOLDER_OUT, "iIT341_xspec.m")
		);
	}
	public void testPalsson_pylori_iIT341_glc() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-glc.csv"),
			new File(FOLDER_OUT, "iIT341_glc.m")
		);
	}
	public void testPalsson_pylori_iIT341_glcopt() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-glcopt.csv"),
			new File(FOLDER_OUT, "iIT341_glcopt.m")
		);
	}
	public void testPalsson_pylori_iIT341_glcmin() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-glcmin.csv"),
			new File(FOLDER_OUT, "iIT341_glcmin.m")
		);
	}
	public void testPalsson_pylori_iIT341_redin() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-redin.csv"),
			new File(FOLDER_OUT, "iIT341_redin.m")
		);
	}
	public void testPalsson_pylori_iIT341_xnone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-xnone.csv"),
			new File(FOLDER_OUT, "iIT341_xnone.m")
		);
	}
	public void testPalsson_pylori_iIT341_aminomin() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-aminomin.csv"),
			new File(FOLDER_OUT, "iIT341_aminomin.m")
		);
	}
	public void testPalsson_pylori_iIT341_aminoglcmin() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-aminoglcmin.csv"),
			new File(FOLDER_OUT, "iIT341_aminoglcmin.m")
		);
	}
	public void testPalsson_pylori_iIT341_aminominGlnGly() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/iIT341-aminoGlnGly.csv"),
			new File(FOLDER_OUT, "iIT341_aminoGlnGly.m")
		);
	}
	
	public void testPalsson_pylori_iCS291() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/pylori_model_v10_iCS291.csv"),
			new File(FOLDER_OUT, "pylori_model_v10_iCS291.m"),
			externalPattern, null
		);
	}
	public void testPalsson_pylori_iCS291_glcamino() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/pylori_model_v10_iCS291_glcamino.csv"),
			new File(FOLDER_OUT, "pylori_model_v10_iCS291_glcamino.m")
		);
	}
	public void testPalsson_pylori_iCS291_specamino() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/pylori_model_v10_iCS291_specamino.csv"),
			new File(FOLDER_OUT, "pylori_model_v10_iCS291_specamino.m")
		);
	}
	public void testPalsson_pylori_iCS291_amino() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "pylori/pylori_model_v10_iCS291_amino.csv"),
			new File(FOLDER_OUT, "pylori_model_v10_iCS291_amino.m")
		);
	}

	public void testPalsson_yeastc_iND750_xnone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "yeast-cerevisiae/iND750-reacts.csv"),
			new File(FOLDER_OUT, "iND750_xnone.m"),
			null/*no externals*/, null
		);
	}
	public void testPalsson_yeastc_iND750() throws Exception {
		Pattern externalPattern = Pattern.compile(".*\\[e\\]");
		internalTestPalsson(
			new File(FOLDER_IN, "yeast-cerevisiae/iND750-reacts.csv"),
			new File(FOLDER_OUT, "iND750.m"),
//			null/*externalPattern*/
			externalPattern, null
		);
	}
	public void testPalsson_yeastc_iLL672() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt");
		internalTestPalsson(
			new File(FOLDER_IN, "yeast-cerevisiae/iLL672-reacts.csv"),
			new File(FOLDER_OUT, "iLL672.m"),
			externalPattern, null
		);
	}
	public void testPalsson_yeastc_iLL672_xnone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_IN, "yeast-cerevisiae/iLL672-reacts.csv"),
			new File(FOLDER_OUT, "iLL672.m"),
			null, null
		);
	}
	public void testPalsson_yeastc_iLL672_unbalancedMetas() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|TRE|GLYCOGEN|CLm|DEAC|PAP|RADP|SAMOB|THFG");
		internalTestPalsson(
			new File(FOLDER_IN, "yeast-cerevisiae/iLL672-reacts.csv"),
			new File(FOLDER_OUT, "iLL672_unbalanced.m"),
			externalPattern, null
		);
	}
	private void internalTestPalsson(File inFile, File outFile) throws Exception {
		internalTestPalsson(inFile, outFile, null, null);
	}
	private void internalTestPalsson(File inFile, File outFile, Pattern externalPattern, String[] excludeReactions) throws Exception {
		fileOut.set(outFile);
		Reaction[] reacts = externalPattern == null ? 
				new PalssonParser().parseReactions(inFile) : 
				new PalssonParser().parseReactions(inFile, externalPattern);
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(reacts);
		System.out.println("parsed network: " + netSize(metaNet));
		final Set<String> suppressedReacs = Arrays.asSet(excludeReactions);
		internalTestOrDelegate(metaNet, suppressedReacs);			
	}
	private static String netSize(MetabolicNetwork net) {
		return net.getMetabolites().length() + " metabolites, " + net.getReactions().length() + " reactions";
	}
}
