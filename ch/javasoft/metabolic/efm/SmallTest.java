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

import java.util.Set;
import java.util.logging.Level;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.util.logging.Loggers;

/**
 * <tt>SmallTest</tt> contains unit test methods which start efm computation for
 * small artificial (reference) models, being hard-coded in java. 
 */
public class SmallTest extends ch.javasoft.metabolic.parse.SmallTest {
	
	static {
//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;
		final CompressionMethod[] compression = CompressionMethod.NONE;
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(FractionalPatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.bigint, Norm.min)) {
		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.bigint, Norm.min)) {
			Loggers.getRootLogger().setLevel(Level.FINER);
//			Loggers.setPlainFormatter(Loggers.getRootLogger());
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new CanonicalEfmModelFactory(), new InCoreMemoryFactory()));       		
//	        ElementaryFluxModes.setImpl(new BornDieDoubleDescriptionImpl(Config.getConfig(), new BornDieEfmModelFactory(), new InCoreMemoryFactory()));       		
		}
	}
	
	/**
	 * The sample of the "binary approach" paper
	 * 
	 * Expected result:
	 * efm[0]: {2, 1, 1, 0, 0, 1, 1}
	 * efm[1]: {2, 2, 0, 0, 1, 1, 1}
	 * efm[2]: {1, 1, 0, 1, 0, 0, 0}
	 * efm[3]: {2, 0, 2, 0, -1, 1, 1}
	 * efm[4]: {1, 0, 1, 1, -1, 0, 0}
	 */
	@Override
	public void testBinSample() throws Exception {
		super.testBinSample();
	}
	/**
	 * The sample of the "binary approach" paper, pre-compressed
	 */
	@Override
	public void testBinSampleCompact() throws Exception {
		super.testBinSampleCompact();
	}
	/**
	 * The sample of the "gemoetry of the flux cone" paper
	 * 
	 * Expected result:
	 * efm[0]: {-1, -1, 0, 1, 1, 0}
	 * efm[1]: {-1, 0, 1, 0, 1, 0}
	 * efm[2]: {0, 0, 0, 1, 1, 1}
	 * efm[3]: {0, 1, 1, 0, 1, 1}
	 * efm[4]: {1, 1, 0, 0, 0, 1}
	 */
	@Override
	public void testGeneratingSample() throws Exception {
		super.testGeneratingSample();
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, 
	 * pre-compressed
	 */
	@Override
	public void testBookMatrixCompressionA() throws Exception {
		super.testBookMatrixCompressionA();
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, 
	 * pre-compressed
	 */
	@Override
	public void testBookMatrixCompressionB() throws Exception {
		super.testBookMatrixCompressionB();
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book
	 * 
	 * Expected result:
	 * efm[0]: {0, 1, 1, 0, 0, 0, 0, 0, 1, 0}
	 * efm[1]: {1, 1, 1, 1, 0, 0, 1, 1, 0, 1}
	 * efm[2]: {2, 0, 1, 1, 0, 1, 1, 0, 0, 1}
	 * efm[3]: {1, 0, 1, 0, 1, 0, 0, 0, 1, 0}
	 * efm[4]: {1, 0, 1, 0, 0, 1, 0, -1, 1, 0}
	 * efm[5]: {2, 0, 1, 1, 1, 0, 1, 1, 0, 1}
	 * efm[6]: {1, -1, 0, 0, 1, 0, 0, 0, 0, 0}
	 * efm[7]: {1, -1, 0, 0, 0, 1, 0, -1, 0, 0}
	 */
	@Override
	public void testBookSample() throws Exception {
		super.testBookSample();
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, but all
	 * reactions are reversible.
	 * 
	 * Expected result: 42 EFMs
	 */
	@Override
	public void testBookSampleRev() throws Exception {
		super.testBookSampleRev();
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, the 
	 * internal reversible reaction was removed
	 */
	@Override
	public void testBookSampleRemRev() throws Exception {
		super.testBookSampleRemRev();
	}
	/**
	 * The tiny sample of the "System Modeling in Cellular Biology" book
	 * 
	 * Expected result:
	 * efm[0]: {1, 0, 1, 1}
	 * efm[1]: {1, 1, 0, 1}
	 */
	@Override
	public void testBookTiny() throws Exception {
		super.testBookTiny();
	}
	
	/**
	 * A simple test case that cause illegal EFMs. The sign of certain flux
	 * values for irreversible reactions was wrong.
	 * <p>
	 * The network is as follows:
	 * <pre>
		'--> S'
		'--> E'
		'S + E <--> ES'
		'ES --> E + P'
		'E -->'
		'P -->'
	 * </pre>
	 * <p>
	 * The following illegal EFMs were produced
	 * <pre>
	     0     1     0     0     1     0
	     1     0    -1     1     0     1
	 * </pre>
	 * <p>
	 * Note that the problem disappears if the reversible reaction is made 
	 * irreversible. It yields the correct EFMs:
	 * <pre>
	     0     1     0     0     1     0
	     1     0     1     1     0     1
	 * </pre>
	 * <p>
	 * The problem was brought up by Markus Uhr.
	 */
	@Override
	public void testSignProblem() throws Exception {
		super.testSignProblem();
	}
	
	/**
	 * The irreversible (already extended) example of the paper 
	 * "Nullspace Approach to Determine the Elementary Modes of Chemical Reaction Systems"
	 * by C. Wagner, J. Phys. Chem., 2004
	 */
	@Override
	public void testNullspaceSampleIrrev() throws Exception {
		super.testNullspaceSampleIrrev();
	}
	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (suppressedReactions != null) {
			Config.getConfig().getReactionsToSuppress().addAll(suppressedReactions);
		}
//        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new CanonicalEfmModelFactory(), new InCoreMemoryFactory()));
        
        
//		ElementaryFluxModes.calculateCallback(network, new TextOutputCallback(network, OutputMode.DoubleUncompressed, LogPkg.LOGGER, Level.INFO, 
//				new NumberTextOutputFormatter(true, true))); 
		
//		ElementaryFluxModes.calculateLogMatlab(network, new File("/local/tmp/"), "smallnet.mat");
		
		ElementaryFluxModes.calculateLogDoubles(network);
		
//		ElementaryFluxModes.calculateLogBinary(network);
//		ElementaryFluxModes.calculateLogCountOnly(network);
//		File outFile = EfmHelper.createEfmOutFile(new File("/tmp"), "smallnet", ".mat");//TODO path and file name
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new MatFileOutputCallback(originalNetwork, outFile)
//		);
//		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());

//		File file = File.createTempFile("smallnet", ".bin");
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new RandomAccessFileOutputCallback(reducedNetwork, OutputMode.BinaryUncompressed, file)
//		);
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new TextOutputCallback(reducedNetwork, OutputMode.BinaryUncompressed, System.out)
//		);
//		System.out.println("written binary file: " + file.getAbsolutePath());
	}
}
