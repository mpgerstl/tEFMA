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
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.urank.modpi.ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.output.mat.MatFileOutputCallback;
import ch.javasoft.metabolic.impl.FilteredMetabolicNetwork;
import ch.javasoft.util.logging.Loggers;

public class AnneTest extends ch.javasoft.metabolic.parse.AnneTest {

	static {
		final CompressionMethod[] compression = CompressionMethod.STANDARD;
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(FractionalPatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, compression, Arithmetic.fractional)) {
		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
			Loggers.getRootLogger().setLevel(Level.FINE);			
			Logger.getLogger("compress.data").setLevel(Level.INFO);
//	        ElementaryFluxModes.setImpl(new BinaryNullspaceOutOfCoreImpl(Config.getConfig()));       		
//	        ElementaryFluxModes.setImpl(new PseudoOutOfCoreImpl(Config.getConfig()));       		
		}
	}
	private static final Logger LOG = LogPkg.LOGGER;
	
	@Override
	public void testAnneYeast_all() throws Exception {
		super.testAnneYeast_all();
	}
	@Override
	public void testAnneYeast_glc() throws Exception {
		super.testAnneYeast_glc();
	}
	@Override
	public void testAnneYeast_eth() throws Exception {
		super.testAnneYeast_eth();
	}
	@Override
	public void testAnneYeast_iAK0815_Glc_Etoh() throws Exception {
		super.testAnneYeast_iAK0815_Glc_Etoh();
	}
	@Override
	public void testAnneYeast_iAK0815_Glc() throws Exception {
		super.testAnneYeast_iAK0815_Glc();
	}
	@Override
	public void testAnneYeast_iAK0815_Etoh() throws Exception {
		super.testAnneYeast_iAK0815_Etoh();
	}
	
	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (suppressedReactions != null && !suppressedReactions.isEmpty()) {
			final FilteredMetabolicNetwork filtered = new FilteredMetabolicNetwork(network, suppressedReactions);
			internalTestMetabolicNetwork(network, filtered);
		}
		else {
			internalTestMetabolicNetwork(network, network);
		}
	}
	public void internalTestMetabolicNetwork(MetabolicNetwork originalNetwork, MetabolicNetwork reducedNetwork) throws Exception {
//		ElementaryFluxModes.calculateCallback(
//			ucNet, new TextOutputCallback(OutputMode.BinaryUncompressed, new FileOutputStream(outFile))
//		);
//		
//		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());
		
		File outFile = new File("/local/tmp/anne_yeast_red.mat");
		String fileNameNoEnding = outFile.getName().substring(0, outFile.getName().lastIndexOf('.'));
		ElementaryFluxModes.calculateCallback(
				reducedNetwork, new MatFileOutputCallback(
				originalNetwork, outFile.getParentFile(), fileNameNoEnding, 500000
			)
		);
		LOG.info("written modes to file(s): " + outFile.getAbsolutePath());

//		ElementaryFluxModes.calculateLogCountOnly(reducedNetwork);
		LOG.info("done.");
	}

}
