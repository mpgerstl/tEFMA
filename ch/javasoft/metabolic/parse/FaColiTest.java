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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.fa.FaConstants;
import ch.javasoft.metabolic.fa.FaConstants.SuperNet;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.impl.DefaultMetaboliteRatio;
import ch.javasoft.metabolic.impl.DefaultReaction;
import ch.javasoft.metabolic.impl.FilteredMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;
import ch.javasoft.metabolic.util.MetabolicNetworkUtil;
import ch.javasoft.metabolic.util.Output;
import ch.javasoft.util.Arrays;

abstract public class FaColiTest extends AbstractParseTestCase {
	
	protected FaConstants.SubNet 	mSubNet;	
	protected FaConstants.SuperNet	mSuperNet;
	
	@Override
	protected void setUp() throws Exception {
		mSubNet		= null;
		mSuperNet	= null;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		mSubNet		= null;
		mSuperNet	= null;
		super.tearDown();
	}
	
	public void testSuperAll() throws Exception {
		internalTest(FaConstants.SUPERALL_UP_AMINO_EX_SUCC);
	}
	public void testSuperXXX() throws Exception {
		internalTest(FaConstants.SUPERXXX_UP_ASP_GLU_SER_THR_TRP_EX_SUCC);
	}
	public void testSuperXXTrp() throws Exception {
		internalTest(FaConstants.SUPERXX_UP_ASP_GLU_SER_TRP_EX_SUCC);
	}
	public void testSuperXXThr() throws Exception {
		internalTest(FaConstants.SUPERXX_UP_ASP_GLU_SER_THR_EX_SUCC);
	}
	public void testSuperX() throws Exception {
		internalTest(FaConstants.SUPERX_UP_ASP_GLU_SER_EX_SUCC);
	}
	public void testSuper() throws Exception {
		internalTest(FaConstants.SUPER_UP_ASP_EX_SUCC);
	}
	public void testAll() throws Exception {
		internalTest(FaConstants.SUBNET_ALL);
	}
	public void testAllAccumulate() throws Exception {
		internalTest(allowMetaboliteAccumulation(FaConstants.SUBNET_ALL));
	}
	public void testAllX() throws Exception {
		internalTest(FaConstants.SUPER_EX_SUCC);
	}
	public void testAc() throws Exception {
		internalTest(FaConstants.SUBNET_AC);
	}
	public void testAcAccumulate() throws Exception {
		internalTest(allowMetaboliteAccumulation(FaConstants.SUBNET_AC));
	}

	public void testNoEx() throws Exception {
		internalTest(FaConstants.SUBNET_NOEX);
	}
	public void testAce() throws Exception {
		internalTest(FaConstants.SUBNET_ACE);
	}
	public void testAceAccumulate() throws Exception {
		internalTest(allowMetaboliteAccumulation(FaConstants.SUBNET_ACE));
	}

	public void testGl() throws Exception {
		internalTest(FaConstants.SUBNET_GL);
	}
	public void testGl(String amino) throws Exception {
		FaConstants.SuperNet superNet = new SuperNet(
			"glpts_" + amino, new String[][] {
				FaConstants.SUCC_EX, 
				findAmino(amino)
			},
			FaConstants.GL_PTS_UP_ALL_AMINO_EX_SUCC.excludeReactions
		);	
		internalTest(superNet);
	}
	public void testGlX() throws Exception {
		internalTest(FaConstants.GL_EX_SUCC);
	}
	public void testGlX_MTHF_forward_only() throws Exception {
		SuperNet net = new SuperNet(
			"glx", new String[][] {
				FaConstants.SUCC_EX,
				{"MTHF_Synth", "ATP + NADPH --> MTHF"}
			},
			new String[] {"Succ_up", "Glyc_up", "Ac_up", "MTHF_Synth"}
		);
		internalTest(net);
	}
	public void testGlX(String amino) throws Exception {
		FaConstants.SuperNet superNet = new SuperNet(
			"glx_" + amino, new String[][] {
				FaConstants.SUCC_EX, 
				findAmino(amino)
			},
			FaConstants.GL_UP_ALL_AMINO_EX_SUCC.excludeReactions
		);	
		internalTest(superNet);
	}
	private String[] findAmino(String amino) {
		for (int i = 0; i < FaConstants.AMINO.length; i++) {
			if (FaConstants.AMINO[i][0].startsWith(amino)) {
				return FaConstants.AMINO[i];
			}
		}
		throw new IllegalArgumentException("no such amino acid: " + amino);
	}
	public void testGlXXAmino_AlaAspGluHisPheSerThr() throws Exception {
		internalTest(FaConstants.GL_UP_ALA_ASP_GLU_HIS_PHE_SER_THR_EX_SUCC);
	}
	public void testGlXAmino_AlaAspGluHisPheSer() throws Exception {
		internalTest(FaConstants.GL_UP_ALA_ASP_GLU_HIS_PHE_SER_EX_SUCC);
	}
	public void testGlAmino_AlaAspGluPheSer() throws Exception {
		internalTest(FaConstants.GL_UP_ALA_ASP_GLU_PHE_SER_EX_SUCC);
	}
	public void testGlPtsXXXAmino_AlaAspGluHisPheSerThrVal() throws Exception {
		internalTest(FaConstants.GL_PTS_UP_ALA_ASP_GLU_HIS_PHE_SER_THR_VAL_EX_SUCC);
	}
	public void testGlPtsXXAmino_AlaAspGluHisPheSerThr() throws Exception {
		internalTest(FaConstants.GL_PTS_UP_ALA_ASP_GLU_HIS_PHE_SER_THR_EX_SUCC);
	}
	public void testGlPtsXAmino_AlaAspGluHisPheSer() throws Exception {
		internalTest(FaConstants.GL_PTS_UP_ALA_ASP_GLU_HIS_PHE_SER_EX_SUCC);
	}
	public void testGlPtsAmino_AlaAspGluPheSer() throws Exception {
		internalTest(FaConstants.GL_PTS_UP_ALA_ASP_GLU_PHE_SER_EX_SUCC);
	}
	
	public void testGly() throws Exception {
		internalTest(FaConstants.SUBNET_GLY);
	}

	public void testStandard() throws Exception {
		internalTest(FaConstants.SUBNET_STANDARD);
	}
	public void testStandardAccumulate() throws Exception {
		internalTest(allowMetaboliteAccumulation(FaConstants.SUBNET_STANDARD));
	}
	
	public void testSuc() throws Exception {
		internalTest(FaConstants.SUBNET_SUC);
	}
	
	private FaConstants.SubNet allowMetaboliteAccumulation(FaConstants.SubNet subNet) {
		return new FaConstants.SubNet(subNet.name + "-accumulate", subNet.excludeReactions, true);
	}
	
	private void internalTest(FaConstants.SuperNet superNet) throws Exception {
		MetabolicNetwork net	= FluxAnalyserParser.parse(FaConstants.COLI_FOLDER);
		Reaction[] addReacts	= new PalssonParser(false /*don't check consistency*/).parseReactions(new StringReader(superNet.toString()));
		//test whether metabolites exist
		for (Reaction reac : addReacts) {
			for (MetaboliteRatio ratio : reac.getMetaboliteRatios()) {
				String metaName = ratio.getMetabolite().getName();
				if (net.getMetabolite(metaName) == null) {
					new Output(System.err).printMetabolites(net, true, true);
					throw new RuntimeException("metabolite " + metaName + " not found in network.");					
				}
			}
		}
		Reaction[] reacts = new Reaction[net.getReactions().length() + addReacts.length];
		net.getReactions().toArray(reacts);
		System.arraycopy(addReacts, 0, reacts, net.getReactions().length(), addReacts.length);
		MetabolicNetwork supNet = new DefaultMetabolicNetwork(reacts);
		if (superNet.excludeReactions.length > 0) {
			FilteredMetabolicNetwork fNet = new FilteredMetabolicNetwork(supNet);
			fNet.excludeReactions(superNet.excludeReactions);
			supNet = fNet;
		}
		mSuperNet = superNet;
		internalTest(supNet, null);
	}

	private void internalTest(FaConstants.SubNet subNet) throws Exception {
		MetabolicNetwork net = FluxAnalyserParser.parse(FaConstants.COLI_FOLDER);
		if (subNet.allowMetaboliteAccumulation) {
			Set<String> hasEx = new HashSet<String>();
			for (Reaction reac : net.getReactions()) {
				if (reac.isExtract() && reac.getMetaboliteRatios().length() == 1) {
					hasEx.add(reac.getMetaboliteRatios().get(0).getMetabolite().getName());
				}
			}
			List<Reaction> ex = new ArrayList<Reaction>();
			for (Metabolite meta : net.getMetabolites()) {
				ex.add(new DefaultReaction(meta.getName() + "_ex", new MetaboliteRatio[] {new DefaultMetaboliteRatio(meta, -1)}, false));
			}
			Reaction[] reacts = new Reaction[net.getReactions().length() + ex.size()];
			net.getReactions().toArray(reacts);
			System.arraycopy(ex.toArray(), 0, reacts, net.getReactions().length(), ex.size());
			net = new DefaultMetabolicNetwork(reacts);
		}
		
		final Set<String> suppressedReacs = Arrays.asSet(subNet.excludeReactions);

		mSubNet = subNet;
		internalTest(net, suppressedReacs);
	}
	
	protected void internalTest(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		LogPkg.LOGGER.finer("original network:     " + MetabolicNetworkUtil.getReactionNamesString(network));
		LogPkg.LOGGER.finer("suppressed reactions: " + suppressedReactions);
		internalTestOrDelegate(network, suppressedReactions);		
	}
	
}
