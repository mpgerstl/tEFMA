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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;
import ch.javasoft.metabolic.util.Output;

/**
 * Abstract test class for excel tests. Subclasses must implement
 * {@link #internalTestMetabolicNetwork(MetabolicNetwork, Set)} and 
 * typically also override the test<ii>xxx</ii> methods for more convenient 
 * invocation.
 * <p>
 * The model contains the following external metabolites (some might be excluded
 * for certain configurations), together with the transporter reaction:
 * <ul>
 *   <li>Uptake<ul>
 * 		<li>GLCNxt: Gluconate / gluconate uptake</li>
 * 		<li>LACxt: D-Lactate / lactate permease (lactate transporter)</li>
 * 		<li>DACxt: Dicarboxylate / dicarboxylate uptake</li>
 * 		<li>ACExt: Acetoin / Acetoin uptake R1</li>
 * 		<li>ACxt: Acetate / Acetate uptake R2</li>
 * 		<li>RIBxt: Ribose / ribose uptake</li>
 * 		<li>GLCxt: D-Glucose / pts system</li>
 *   </ul></li>
 *   <li>Extract<ul>
 * 		<li>CO2xt: Carbon dioxyde / Transporter CO2</li>
 * 		<li>O2xt: Oxygen / Transporter O2</li>
 * 		<li>ACExt: Acetoin / Transporter Acetoin</li>
 * 		<li>ACxt: Acetate / Transporter AC</li>
 * 		<li>SUCCxt: Succinate / Transporter SUCC</li>
 * 		<li>PYRxt: Pyrovate / Transporter PYR</li>
 * 		<li>LACxt: D-Lactate / Transporter LAC</li>
 * 		<li>DACxt: Dicarboxylate / Transporter Diacetyl</li>
 * 		<li>RIBxt: Ribose / Transporter RIB</li>
 *   </ul></li>
 *   <li>Update &amp; Extract<ul>
 * 		<li>GLCNxt: Gluconate / Transporter GLCN</li>
 *   </ul></li>
 * </ul>
 */
abstract public class ExcelTest extends AbstractParseTestCase {

	public static final String VERSION_ROBERT_COLI 	= "v03";
	public static final String VERSION_ROBERT_SUBTILIS = "v05";
	private static final File FOLDER = new File("../metabolic-data/excel");
	private static final File FILE_ROBERT_COLI = new File(FOLDER, "Coli-robert-" + VERSION_ROBERT_COLI + ".xls");
	private static final String WORKSHEET_ROBERT_COLI = "Sheet1";
	private static final File FILE_ROBERT_SUBTILIS = new File(FOLDER, "Subtilis-robert-" + VERSION_ROBERT_SUBTILIS + ".xls");
	private static final String WORKSHEET_ROBERT_SUBTILIS = "stoichiometry";
	//private static final String WORKSHEET_ROBERT_SUBTILIS = "Sheet1";

	/**
	 * Roberts <i>E.coli</i> model with all reactions
	 */
	public void test_coli_Robert() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, new String[] {}
		);
	}
	/**
	 * Roberts <i>E.coli</i> model with glucose uptake only
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * Transporter GLCN, Transporter NO3, Transporter NO2, 
	 * Galactose ABC transporter
	 * </tt>
	 * <p> The following reversible exchange reactions are set to extract only: 
	 * <tt>ex_ETHxt, ex_ACxt</tt> becoming <tt>ETH extract, AC extract</tt>
	 */
	public void test_coli_Robert_glc() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, new String[] {
				"Transporter GLCN", "Transporter NO3", "Transporter NO2",
				"Galactose ABC transporter",
				"ex_ETHxt", "ex_ACxt"
			},
			new String[][] {
				{"ETH extract", "ETHxt -->"},
				{"AC extract", "ACxt -->"}
			}
		);
	}
	/**
	 * Roberts <i>E.coli</i> model without <tt>Glucokinase</tt> reaction
	 */
	public void test_coli_Robert_gk() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, new String[] {"Glucokinase"}
		);
	}
	/**
	 * Roberts <i>E.coli</i> model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_PYRxt, Glucokinase</tt>
	 */
	public void test_coli_Robert_pyrGk() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, new String[] {"ex_PYRxt", "Glucokinase"}
		);
	}
	/**
	 * Roberts <i>E.coli</i> reduced model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_SUCCxt, ex_PYRxt, Glucokinase</tt>
	 */
	public void test_coli_Robert_reduced() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, new String[] {"ex_SUCCxt", "ex_PYRxt", "Glucokinase"}
		);
	}
	
	/**
	 * Roberts <i>E.coli</i> reduced model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_PYRxt, ex_GLCNxt, Glucokinase, 
	 * Glucose dehydrogenase, Gluconokinase I, Gluconokinase II, 
	 * Galactose ABC transporter
	 * </tt>
	 */
	public void test_coli_Robert_exclPyrGlcn() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, 
			new String[] {
				"ex_PYRxt", "ex_GLCNxt", "Glucokinase", "Glucose dehydrogenase", 
				"Gluconokinase I", "Gluconokinase II", "Galactose ABC transporter"
			}
		);
	}

	/**
	 * Roberts <i>E.coli</i> reference model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_SUCCxt, ex_PYRxt, ex_GLCNxt, ex_FORxt, ex_LACxt, Glucokinase, 
	 * Glucose dehydrogenase, Gluconokinase I, Gluconokinase II, 
	 * Galactose ABC transporter
	 * </tt>
	 */
	public void test_coli_Robert_reference() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_COLI, WORKSHEET_ROBERT_COLI, 
			3/*reactionColumn*/, 4 /*reactionNameColumn*/, 2 /*headerRows*/,
			externalPattern, 
			new String[] {
				"ex_SUCCxt", "ex_PYRxt", "ex_GLCNxt", "ex_FORxt", "ex_LACxt", 
				"Glucokinase", "Glucose dehydrogenase", "Gluconokinase I", "Gluconokinase II", "Galactose ABC transporter"
			}
		);
	}
	/**
	 * Roberts <i>B.subtilis</i> model with all reactions.
	 * 
	 * <p>Excel file version: v0.5
	 */
	public void test_subtilis_Robert() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, new String[] {}
		);
	}
	/**
	 * Roberts <i>B.subtilis</i> model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_PYRxt, Glucokinase</tt>
	 * 
	 * <p>Excel file version: v0.5
	 */
	public void test_subtilis_Robert_pyrGk() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, new String[] {"ex_PYRxt", "Glucokinase"}
		);
	}
	/**
	 * Roberts <i>B.subtilis</i> reduced model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_SUCCxt, ex_PYRxt, Glucokinase</tt>
	 */
	public void test_subtilis_Robert_reduced() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, new String[] {"ex_SUCCxt", "ex_PYRxt", "Glucokinase"}
		);
	}
	/**
	 * Roberts <i>B.subtilis</i> model with glucose uptake only
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * gluconate uptake, lactate permease (lactate transporter), 
	 * dicarboxylate uptake, Acetoin uptake R1, Acetate uptake R2, ribose uptake
	 * </tt>
	 */
	public void test_subtilis_Robert_glc() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, new String[] {"gluconate uptake", 
				"lactate permease (lactate transporter)", 
				"dicarboxylate uptake", "Acetoin uptake R1", 
				"Acetate uptake R2", "ribose uptake"
			}
		);
	}
	/**
	 * Roberts <i>B.subtilis</i> reference model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_SUCCxt, ex_PYRxt, ex_GLCNxt, ex_LACxt, Glucokinase
	 * </tt>
	 */
	public void test_subtilis_Robert_reference() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, 
			new String[] {
				"ex_SUCCxt", "ex_PYRxt", "ex_GLCNxt", "ex_LACxt", 
				"Glucokinase"
			}
		);
	}
	
	/**
	 * Roberts <i>B.subtilis</i> model for csb course (exercise)
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_SUCCxt, ex_PYRxt, ex_GLCNxt, ex_LACxt, ex_DACxt, ex_GLCxt, ex_ACExt
	 * </tt>
	 */
	public void test_subtilis_Robert_csbexc() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, 
			new String[] {
				"ex_SUCCxt", "ex_PYRxt", "ex_GLCNxt", "ex_LACxt", "ex_DACxt", "ex_GLCxt", "ex_ACExt"
			}
		);
	}
	
	/**
	 * Roberts <i>B.subtilis</i> model for GLC uptake only
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * Transporter GLCN, gluconate uptake, lactate permease (lactate transporter), 
	 * dicarboxylate uptake, Acetoin uptake R1, ribose uptake
	 * </tt>
	 */
	public void test_subtilis_Robert_GLCup() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, 
			new String[] {
				"Transporter GLCN", "gluconate uptake", "lactate permease (lactate transporter)", 
				"dicarboxylate uptake", "Acetoin uptake R1", "ribose uptake", /* keep GLC: "pts system"*/
			}
		);
	}

	/**
	 * Roberts <i>B.subtilis</i> model for GLC/MAL uptake
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * Ribose transport system permease protein rbsC, 
	 * Proton glutamate symport protein
	 * </tt>
	 */
	public void test_subtilis_Robert_GLCMALup() throws Exception {
		Pattern externalPattern = Pattern.compile(".*xt|BM");
		internalTestExcel(
			FILE_ROBERT_SUBTILIS, WORKSHEET_ROBERT_SUBTILIS, 
			3/*reactionColumn*/, 8 /*reactionNameColumn*/, 3 /*headerRows*/,
			externalPattern, 
			new String[] {
				"Ribose transport system permease protein rbsC", 
				"Proton glutamate symport protein"
			}
		);
	}

	private void internalTestExcel(File file, String worksheetName, int reactionColumn, int reactionNameColumn, int headerRows, Pattern externalPattern, String[] excludeReactions) throws Exception {
		internalTestExcel(file, worksheetName, reactionColumn, reactionNameColumn, headerRows, externalPattern, excludeReactions, null);
	}
	private void internalTestExcel(File file, String worksheetName, int reactionColumn, int reactionNameColumn, int headerRows, Pattern externalPattern, String[] excludeReactions, String[][] additionalReactions) throws Exception {
		ExcelParser parser = worksheetName == null ? new ExcelParser(file) : new ExcelParser(file, worksheetName);
		MetabolicNetwork metaNet = externalPattern == null ? 
				parser.parse(reactionColumn, reactionNameColumn, headerRows) : 
				parser.parse(reactionColumn, reactionNameColumn, headerRows, externalPattern);
		Output log = new Output(LogPkg.LOGGER, Level.INFO);
		log.printNetworkSize("parsed network: ", metaNet);
		if (additionalReactions != null) {
			metaNet = addReactions(metaNet, additionalReactions);
		}
		final Set<String> excl = excludeReactions == null ? null : new LinkedHashSet<String>(Arrays.asList(excludeReactions));
		internalTestOrDelegate(metaNet, excl);
	}
	
	private MetabolicNetwork addReactions(MetabolicNetwork original, String[][] additionalReactions) throws IOException {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (String[] reac : additionalReactions) {
			pw.println("\"" + reac[0] + "\"\t\"" + reac[0] + "\"\t\"" + reac[1] + "\"");
		}
		pw.flush();
		Reaction[] add = new PalssonParser().parseReactions(new StringReader(sw.toString()));
		Reaction[] all = new Reaction[original.getReactions().length() + add.length];
		original.getReactions().toArray(all);
		System.arraycopy(add, 0, all, original.getReactions().length(), add.length);
		return new DefaultMetabolicNetwork(all);
	}
}
