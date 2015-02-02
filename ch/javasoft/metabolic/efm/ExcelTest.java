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
package ch.javasoft.metabolic.efm;

import java.io.File;
import java.util.Set;
import java.util.logging.Level;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.modpi.ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.output.mat.MatFileOutputCallback;
import ch.javasoft.metabolic.efm.sort.SortUtil;
import ch.javasoft.util.logging.Loggers;

/**
 * <tt>ExcelTest</tt> contains unit test methods which start efm computation for
 * roberts e-coli model, parsing the excel file in <tt>../metabolic-data/excel/</tt>.
 */
public class ExcelTest extends ch.javasoft.metabolic.parse.ExcelTest {
	static {
		final CompressionMethod[] compression = CompressionMethod.STANDARD;
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, SortUtil.DEFAULT_SORTER, compression, false, Arithmetic.fractional, Norm.norm2)) {
			Loggers.getRootLogger().setLevel(Level.FINE);			
		}
	}
	
	/**
	 * Computes efms for Roberts <i>E.coli</i> model with all reactions
	 */
	@Override
	public void test_coli_Robert() throws Exception {
		super.test_coli_Robert();
	}
	
	/**
	 * Computes efms for Roberts <i>E.coli</i> model with glucose uptake only.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * Transporter GLCN, Transporter NO3, Transporter NO2, 
	 * Galactose ABC transporter
	 * </tt>
	 * <p> The following reversible exchange reactions are set to extract only: 
	 * <tt>ex_ETHxt, ex_ACxt</tt> becoming <tt>ETH extract, AC extract</tt>
	 */
	@Override
	public void test_coli_Robert_glc() throws Exception {
		super.test_coli_Robert_glc();
	}
	
	
	/**
	 * Computes efms for Roberts <i>E.coli</i> model without 
	 * <tt>Glucokinase</tt> reaction
	 */
	@Override
	public void test_coli_Robert_gk() throws Exception {
		super.test_coli_Robert_gk();
	}
	
	/**
	 * Computes efms for Roberts <i>E.coli</i> model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_PYRxt, Glucokinase</tt>
	 */
	@Override
	public void test_coli_Robert_pyrGk() throws Exception {
		super.test_coli_Robert_pyrGk();
	}
	

	/**
	 * Computes efms for Roberts <i>E.coli</i> reduced model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_SUCCxt, ex_PYRxt, Glucokinase</tt>
	 */
	@Override
	public void test_coli_Robert_reduced() throws Exception {
		super.test_coli_Robert_reduced();
	}
	
	/**
	 * Computes efms for Roberts <i>E.coli</i> reduced model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_PYRxt, ex_GLCNxt, Glucokinase, 
	 * Glucose dehydrogenase, Gluconokinase I, Gluconokinase II, 
	 * Galactose ABC transporter
	 * </tt>
	 */
	@Override
	public void test_coli_Robert_exclPyrGlcn() throws Exception {
		super.test_coli_Robert_exclPyrGlcn();
	}
	
	/**
	 * Computes efms for Roberts <i>E.coli</i> reference model
	 * 
	 * a) compressed with duplicate gene compression option; <br/>
	 * expected number of efms: 1,392,168 (compressed) / TODO (uncompressed).
	 * 
	 * b) normal compression, with pre-removal of duplicate genes:
	 * expected number of efms: 1,870,740
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_SUCCxt, ex_PYRxt, ex_GLCNxt, ex_FORxt, ex_LACxt, Glucokinase, 
	 * Glucose dehydrogenase, Gluconokinase I, Gluconokinase II, 
	 * Galactose ABC transporter
	 * </tt>
	 */
	@Override
	public void test_coli_Robert_reference() throws Exception {
		super.test_coli_Robert_reference();
	}

	/**
	 * Computes efms for Roberts <i>B.subtilis</i> model with all reactions.
	 */
	@Override
	public void test_subtilis_Robert() throws Exception {
		super.test_subtilis_Robert();
	}
	
	/**
	 * Computes efms for Roberts <i>B.subtilis</i> model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_PYRxt, Glucokinase</tt>
	 */
	@Override
	public void test_subtilis_Robert_pyrGk() throws Exception {
		super.test_subtilis_Robert_pyrGk();
	}
	
	/**
	 * Computes efms for Roberts <i>B.subtilis</i> reduced model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>ex_SUCCxt, ex_PYRxt, Glucokinase</tt>
	 */
	@Override
	public void test_subtilis_Robert_reduced() throws Exception {
		super.test_subtilis_Robert_reduced();
	}
	
	/**
	 * Computes efms for Roberts <i>B.subtilis</i> model with glucose uptake 
	 * only.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * gluconate uptake, lactate permease (lactate transporter), 
	 * dicarboxylate uptake, Acetoin uptake R1, Acetate uptake R2, ribose uptake
	 * </tt>
	 */
	@Override
	public void test_subtilis_Robert_glc() throws Exception {
		super.test_subtilis_Robert_glc();
	}
	
	/**
	 * Computes efms for Roberts <i>B.subtilis</i> reference model.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_SUCCxt, ex_PYRxt, ex_GLCNxt, ex_LACxt, Glucokinase
	 * </tt>
	 */
	@Override
	public void test_subtilis_Robert_reference() throws Exception {
		super.test_subtilis_Robert_reference();
	}
	
	/**
	 * Computes efms for Roberts <i>B.subtilis</i> model for csb course (exc)
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * ex_SUCCxt, ex_PYRxt, ex_GLCNxt, ex_LACxt, ex_DACxt, ex_GLCxt, ex_ACExt
	 * </tt>
	 */
	@Override
	public void test_subtilis_Robert_csbexc() throws Exception {
		super.test_subtilis_Robert_csbexc();
	}
	
	/**
	 * Computes efms for Roberts <i>B.subtilis</i> model for GLC/MAP uptake.
	 * 
	 * <p> The following reactions are excluded (flux set to 0):
	 * <tt>
	 * Ribose transport system permease protein rbsC, 
	 * Proton glutamate symport protein
	 * </tt>
	 */
	@Override
	public void test_subtilis_Robert_GLCMALup() throws Exception {
		super.test_subtilis_Robert_GLCMALup();		
	}

	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (suppressedReactions != null) {
			Config.getConfig().getReactionsToSuppress().addAll(suppressedReactions);
		}

//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new TextOutputCallback(OutputMode.BinaryUncompressed, new FileOutputStream(outFile))
//		);
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new TextOutputCallback(OutputMode.DoubleUncompressed, new FileOutputStream(outFile))
//		);

		
//		File outFile = EfmHelper.createEfmOutFile(new File("/home/terzerm/eth/metabolic-efm-run/results-robert"), "robert", ".mat");//TODO path and file name
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new MatFileOutputCallback(originalNetwork, outFile)
//		);
//		
//		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());
		
		
		File outFile = new File("/home/terzerm/eth/metabolic-efm-run/results-robert/mnet_robert.mat");
		ElementaryFluxModes.calculateCallback(
				network, new MatFileOutputCallback(
				network, outFile.getParentFile(), outFile.getName()/*, 2000000*/
			)
		);
		LogPkg.LOGGER.info("written modes to file(s): " + outFile.getAbsolutePath());
	}

}
