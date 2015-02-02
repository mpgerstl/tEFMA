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

import java.io.StringReader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.impl.SequentialDoubleDescriptionImpl;
import ch.javasoft.metabolic.efm.memory.incore.InCoreMemoryFactory;
import ch.javasoft.metabolic.efm.model.canonical.CanonicalEfmModelFactory;
import ch.javasoft.metabolic.fa.FaConstants;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.impl.FilteredMetabolicNetwork;
import ch.javasoft.metabolic.parse.PalssonParser;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.ops.HslGateway;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.logging.Loggers;

/**
 * <tt>ColiTest</tt> contains unit test methods which start efm computation for
 * the classical e-coli model, parsing flux analyser files 
 * (<tt>../metabolic-data/FluxAnalyzer50a/coli</tt>).
 */
@SuppressWarnings("unused")
public class ColiTest extends ch.javasoft.metabolic.parse.FaColiTest {
	
	static {
		
//		final CompressionMethod[] compression = CompressionMethod.STANDARD;
		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;
//		final CompressionMethod[] compression = CompressionMethod.ALL;
//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_COMBINE;

//		if (Config.initForJUnitTest(FastRankTestAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModRankTestAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(LinearSearchAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeSearchAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(PatternTreeLinearSearchAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 1)) {

//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 4)) {		
		
//		if (Config.initForJUnitTest(SearchInCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 4, ProgressType.None)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 1, ProgressType.None)) {
		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.rawint)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 0)) {
		
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(MultiProcessedAdjEnum.name(ModIntPrimeOutCoreAdjEnum.NAME), "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(MultiThreadedAdjEnum.name(ModIntPrimeOutCoreAdjEnum.NAME), "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {		
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.bigint)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.bigint)) {
		
		
//		if (Config.initForJUnitTest(SearchInCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 0, ProgressType.None)) {		
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 0, ProgressType.None)) {		
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 4, ProgressType.None)) {
			
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 4)) {		
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrLexMin", compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, SortUtil.DEFAULT_SORTER, compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, Arithmetic.double_)) {
//        if (Config.initForJUnitTest(PatternTreeLogLogAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//        if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(new Config(new Zero(), PatternTreeMinZerosAdjacencyEnumerator.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 2, Arithmetic.double_, -1, Generator.Efm, Normalize.min))) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(Fractional2PatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_)) {

//		if (Config.initForJUnitTest(DistributingMemAdjEnum.name(ModIntPrimeOutCoreAdjEnum.NAME), compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(new Config(new Zero(), ModIntPrimeOutCoreAdjEnum.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 1, Arithmetic.double_, -1, Generator.Efm, Normalize.min, ))) {
//		if (Config.initForJUnitTest(new Config(new Zero(), ModIntPrimeOutCoreAdjEnum.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 1, Arithmetic.double_, -1, Generator.Efm, Normalize.min))) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "Monet", compression, Arithmetic.double_)) {

//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeModRankAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(new Config(new Zero(), ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 1, Arithmetic.double_, -1, Generator.Efm, Normalize.min))) {
//		if (Config.initForJUnitTest(new Config(new Zero(), ModIntPrimeOutCoreAdjEnum.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 1, Arithmetic.double_, -1, Generator.Efm, Normalize.min))) {
//		if (Config.initForJUnitTest(new Config(new Zero(), ModIntPrimeOutCoreAdjEnum.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 16, Arithmetic.double_, -1, Generator.Efm, Normalize.min))) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(new Config(new Zero(), ModIntPrimeOutCoreAdjEnum.NAME, SortUtil.DEFAULT_SORTER, compression, true, false, 1, Arithmetic.fractional, -1, Generator.Efm, Normalize.min))) {
			
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, SortUtil.DEFAULT_SORTER, compression, false, Arithmetic.fractional, Norm.norm2)) {
//		if (Config.initForJUnitTest(PatternTreeRankAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeModRankAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(FastRankTestAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModRankTestAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(DoublePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(Double2PatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(FractionalPatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(FractionalPatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(Fractional2PatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(Fractional2PatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(Mod64PatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//			Loggers.getRootLogger().setLevel(Level.FINE);
			Loggers.getRootLogger().setLevel(Level.FINE);
//			Loggers.setPlainFormatter(Loggers.getRootLogger());

			final Config config = Config.getConfig();
//	        config.getReactionsToEnforce().add("mue");
//	        config.getReactionsToEnforce().add("Glc_PTS_up");
	        //config.getReactionsToEnforce().add("Glc_ATP_up");
	        //config.getReactionsToEnforce().add("O2_up");
//	        config.getReactionsToSuppress().add("O2_up");
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new NullspaceEfmModelFactory(), new OutOfCoreMemoryFactory()));
//	        try {
//				Config.getConfig().getTempDir().mkdirPersonalized();
//			} 
//	        catch (Exception e) {
//				throw new RuntimeException(e);
//			}       		
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new NullspaceEfmModelFactory(), new OutOfCoreMemoryFactory()));       		
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new NullspaceEfmModelFactory(), new SortInCoreOutOfCoreMemoryFactory()));       		
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new NullspaceEfmModelFactory(), new InCoreMemoryFactory()));       		
	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new CanonicalEfmModelFactory(), new InCoreMemoryFactory()));       		
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new CanonicalEfmModelFactory(), new OutOfCoreMemoryFactory()));       		
//	        ElementaryFluxModes.setImpl(new BornDieDoubleDescriptionImpl(Config.getConfig(), new BornDieEfmModelFactory(), new InCoreMemoryFactory()));       		
//	        ElementaryFluxModes.setImpl(new ReversibleReactionDivideAndConquerImpl(ElementaryFluxModes.getImpl()));       		
			
		}
		
		//row ordering for All:
		//~170 seconds:
//		final String rowOrdering = "Fixed:1:MostZerosOrAbsLexMin:1:3:8:16:19:15:18:17:20:21:25:24:23:26:22:5:2:6:14:12:9:7:13:11:10:4";
		//~74/131 seconds:
//		final String rowOrdering = "Fixed:1:MostZerosOrAbsLexMin:22:5:2:6:14:12:9:7:13:11:10:4:1:3:8:16:19:15:18:17:20:21:25:24:23:26";
		//~65/133 seconds:
//		final String rowOrdering = "Fixed:1:MostZerosOrAbsLexMin:5:2:6:14:12:9:7:13:11:10:4:1:3:8:16:19:15:18:17:20:21:25:24:23:26:22";
		//~300 seconds:
//		final String rowOrdering = "Fixed:1:MostZerosOrAbsLexMin:2:6:14:12:9:7:13:11:10:4:1:3:8:16:19:15:18:17:20:21:25:24:23:26:22:5";
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, rowOrdering, compression, Arithmetic.fractional)) {
//			Loggers.getRootLogger().setLevel(Level.FINER);
//		}
	}
	private static final Logger LOG = LogPkg.LOGGER;

	/**
	 * No exchange reactions, only internal cycles.
	 * 
	 * Expected number of efms: 1
	 */
	@Override
	public void testNoEx() throws Exception {
		super.testNoEx();
	}
	/**
	 * Expected number of efms: 599
	 */
	@Override
	public void testAce() throws Exception {
//		Config.getConfig().getReactionsToEnforce().clear();
//		Config.getConfig().getReactionsToEnforce().add("mue");
		super.testAce();
	}
	/**
	 * Expected number of efms: ???
	 */
	@Override
	public void testAceAccumulate() throws Exception {
		super.testAceAccumulate();
	}
	/**
	 * Expected number of efms: 1061
	 */
	@Override
	public void testAc() throws Exception {
		super.testAc();
	}
	/**
	 * Expected number of efms: ???
	 */
	@Override
	public void testAcAccumulate() throws Exception {
		super.testAcAccumulate();
	}
	/**
	 * Expected number of efms: 7,055
	 */
	@Override
	public void testSuc() throws Exception {
		super.testSuc();
	}
	/**
	 * Expected number of efms: 11,333
	 */
	@Override
	public void testGly() throws Exception {
		super.testGly();
	}
	/**
	 * Expected number of efms: 27,100
	 */
	@Override
	public void testStandard() throws Exception {
		super.testStandard();
	}
	/**
	 * Expected number of efms: ???
	 */
	@Override
	public void testStandardAccumulate() throws Exception {
		super.testStandardAccumulate();
	}
	/**
	 * Expected number of efms: 53,942
	 */
	@Override
	public void testGl() throws Exception {
		super.testGl();
	}
	/**
	 * Expected number of efms: 178,575
	 */
	@Override
	public void testGlX() throws Exception {
		super.testGlX();
	}
	/**
	 * Expected number of efms: 35,680
	 */
	@Override
	public void testGlX_MTHF_forward_only() throws Exception {
		super.testGlX_MTHF_forward_only();
	}
	/**
	 * Expected number of efms: 164,558
	 */
	public void testGlX_mueAtp() throws Exception {
		testGlX_mueAtp(60);
	}
	protected void testGlX_mueAtp(final double atpMMolPerGMue) throws Exception {
		final ColiTest test = new ColiTest() {
			@Override
			public void testGlX_mueAtp() throws Exception {
				super.testGlX();//like GlX, modifications are done below
			}
			@Override
			protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
				String mue = network.getReaction("mue").toString();
				String[][] mueAtp = new String[][] {{"mue", atpMMolPerGMue + " ATP + " + mue.substring(0, mue.length() - 2)}};
				Reaction[] addReacts = new PalssonParser(false /*don't check consistency*/).parseReactions(new StringReader(FaConstants.SuperNet.toString(mueAtp)));
				FilteredMetabolicNetwork fNet = new FilteredMetabolicNetwork(network);
				fNet.excludeReactions("ATPdrain", "mue");
				Reaction[] reacts = new Reaction[fNet.getReactions().length() + addReacts.length];
				fNet.getReactions().toArray(reacts);
				System.arraycopy(addReacts, 0, reacts, fNet.getReactions().length(), addReacts.length);
				MetabolicNetwork supNet = new DefaultMetabolicNetwork(reacts);
				super.internalTest(supNet, suppressedReactions);
			}
		};
		test.testGlX_mueAtp();
	}
	
	private final String PROP_NAME_AMINO = "ColiTest.SingleAmino";
	public void testGlSingleAmino() throws Exception {
		final String amino = System.getProperty(PROP_NAME_AMINO);
		if (amino == null) {
			throw new IllegalStateException("missing system property " + PROP_NAME_AMINO);
		}
		super.testGl(amino);
	}
	public void testGlXSingleAmino() throws Exception {
		final String amino = System.getProperty(PROP_NAME_AMINO);
		if (amino == null) {
			throw new IllegalStateException("missing system property " + PROP_NAME_AMINO);
		}
		super.testGlX(amino);
	}
	/**
	 * Expected number of efms: 52,385,648
	 */
	@Override
	public void testGlXXAmino_AlaAspGluHisPheSerThr() throws Exception {
		super.testGlXXAmino_AlaAspGluHisPheSerThr();
	}
	/**
	 * Expected number of efms: 26,381,168
	 */
	@Override
	public void testGlXAmino_AlaAspGluHisPheSer() throws Exception {
		super.testGlXAmino_AlaAspGluHisPheSer();
	}
	/**
	 * Expected number of efms: 13,175,456
	 */
	@Override
	public void testGlAmino_AlaAspGluPheSer() throws Exception {
		super.testGlAmino_AlaAspGluPheSer();
	}
	
	/**
	 * Expected number of efms: 58,834,376
	 */
	@Override
	public void testGlPtsXXXAmino_AlaAspGluHisPheSerThrVal() throws Exception {
		super.testGlPtsXXXAmino_AlaAspGluHisPheSerThrVal();
	}
	/**
	 * Expected number of efms: 29,101,912
	 */
	@Override
	public void testGlPtsXXAmino_AlaAspGluHisPheSerThr() throws Exception {
		super.testGlPtsXXAmino_AlaAspGluHisPheSerThr();
	}
	/**
	 * Expected number of efms: 14,642,914
	 */
	@Override
	public void testGlPtsXAmino_AlaAspGluHisPheSer() throws Exception {
		super.testGlPtsXAmino_AlaAspGluHisPheSer();
	}
	/**
	 * Expected number of efms: 7,309,118
	 */
	@Override
	public void testGlPtsAmino_AlaAspGluPheSer() throws Exception {
		super.testGlPtsAmino_AlaAspGluPheSer();
	}
	/**
	 * Expected number of efms: 507,632
	 */
	@Override
	public void testAll() throws Exception {
		super.testAll();
	}
	/**
	 * Expected number of efms: ???
	 */
	@Override
	public void testAllAccumulate() throws Exception {
		super.testAllAccumulate();
	}
	/**
	 * Expected number of efms: 894,865
	 */
	@Override
	public void testAllX() throws Exception {
		super.testAllX();
	}
	/**
	 * Expected number of efms: 2,450,787
	 * 
	 * NOTE: runs faster with MostZerosOrFewestNegPos/MostZerosOrLexMin row 
	 * 		ordering
	 */
	@Override
	public void testSuper() throws Exception {		
		super.testSuper();
	}
	/**
	 * Expected number of efms: 16,991,345
	 */
	@Override
	public void testSuperX() throws Exception {
		super.testSuperX();
	}
	
	/**
	 * Expected number of efms: 33,778,152
	 */
	@Override
	public void testSuperXXThr() throws Exception {
		super.testSuperXXThr();
	}
	
	/**
	 * Expected number of efms: 33,777,432
	 */
	@Override
	public void testSuperXXTrp() throws Exception {
		super.testSuperXXTrp();
	}
	
	/**
	 * Expected number of efms: 67,211,104
	 */
	@Override
	public void testSuperXXX() throws Exception {
		super.testSuperXXX();
	}

	/**
	 * Uptake of all amino acids.
	 * 
	 * Expected number of efms: ?
	 */
	@Override
	public void testSuperAll() throws Exception {
		super.testSuperAll();
	}

	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (suppressedReactions != null) {
			Config.getConfig().getReactionsToSuppress().addAll(suppressedReactions);
		}
		
//		final String outFileName = getFromConfig("metabolic-efm/efm-output/callback/stream/file/@name");
//		File outFile = new File(outFileName);
		
//		MetabolicNetwork ucNet = EfmHelper.compressDuplicates(net);
//		MetabolicNetwork ucNet = network;
//		ElementaryFluxModes.calculateCallback(
//			ucNet, new TextOutputCallback(OutputMode.BinaryUncompressed, new FileOutputStream(outFile))
//		);
//		
//		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());
		
//		File outFile = new File("/local/tmp", "colinature.mat");
//		ElementaryFluxModes.calculateCallback(
//			ucNet, new MatFileOutputCallback(originalNetwork, outFile)
//		);
//		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());		

//		File file = File.createTempFile("colinature", ".bin");
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new RandomAccessFileOutputCallback(reducedNetwork, OutputMode.BinaryUncompressed, file)
//		);
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new TextOutputCallback(reducedNetwork, OutputMode.BinaryUncompressed, System.out)
//		);
//		System.out.println("written binary file: " + file.getAbsolutePath());
		
//		LOG.info("red: " + reducedNetwork.getMetabolites());
//		callMonet(network, 2);
//		callMonet(network, 3);
//		callMonet(network, 4);
//		callMonet(network, 5);
//		callMonet(network, 8);
//		callMonet(network, 10);
//		callMonet(network, 12);
//		callMonet(network, 15);
//		callMonet(network, 18);
//		callMonet(network, 20);
		
//		ElementaryFluxModes.calculateLogNull(network);
		ElementaryFluxModes.calculateLogCountOnly(network);
				
//		ElementaryFluxModes.calculateLogDoubles(network);
//		File outFile = new File("/local/tmp/mnet_colinature.txt"); 
//		ElementaryFluxModes.calculateFileDoubles(network, outFile);
//		ElementaryFluxModes.calculateFileSigns(network, outFile);

//		File outFile = new File("/local/tmp/mnet_colinature.mat"); 
//		ElementaryFluxModes.calculateFileMatlab(network, outFile.getParentFile(), outFile.getName());
//		LOG.info("written output to " + outFile.getAbsolutePath());
		
//		ElementaryFluxModes.calculateLogMatlab(network, OutputMode.SignUncompressed, new File("/local/tmp/"), "mnet_colinature.mat");
//		ElementaryFluxModes.calculateLogDoubles(reducedNetwork);
//		ElementaryFluxModes.calculateLogBinary(reducedNetwork);
//		LOG.info("done.");
//		RankUpdateRoot.trace(new LogPrintStream(LOG, Level.INFO));
	}
	
	private void callMonet(MetabolicNetwork net, int nblocks) {
		final ReadableMatrix<?> stoich = net.getStoichiometricMatrix().transposeR();
		final IntArray irn = new IntArray();
		final IntArray jcn = new IntArray();
		for (int i = 0; i < stoich.getRowCount(); i++) {
			for (int j = 0; j < stoich.getColumnCount(); j++) {
				if (stoich.getSignumAt(i, j) != 0) {
					irn.add(i + 1);
					jcn.add(j + 1);
				}
			}
		}
		HslGateway.callMc66(stoich.getRowCount(), stoich.getColumnCount(), irn.toArray(), jcn.toArray(), nblocks, false, true);
	}
	
	//////////////////////////// helpers
	private static boolean[] getReversible(MetabolicNetwork net) {
		final boolean[] rev = new boolean[net.getReactions().length()];
		for (int i = 0; i < rev.length; i++) {
			rev[i] = net.getReactions().get(i).getConstraints().isReversible();
		}
		return rev;
	}
	private static ReadableBigIntegerRationalMatrix getStoich(MetabolicNetwork net) {
		return new DefaultBigIntegerRationalMatrix(
			StoichiometricMatrices.createStoichiometricMatrix(net), 
			true /*rowIsFirstDim*/, true /*adjustValues*/
		);
	}
	
}
