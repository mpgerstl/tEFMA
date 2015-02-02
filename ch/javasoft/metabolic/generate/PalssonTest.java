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
import java.util.Set;

import ch.javasoft.io.Print;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.impl.FilteredMetabolicNetwork;

public class PalssonTest extends ch.javasoft.metabolic.parse.PalssonTest {
	
	

	@Override
	public void testPalsson_aureus_iMH556() throws Exception {
		super.testPalsson_aureus_iMH556();
	}

	@Override
	public void testPalsson_aureus_iSB619_xnone() throws Exception {
		super.testPalsson_aureus_iSB619_xnone();
	}

	@Override
	public void testPalsson_aureus_iSB619_xrev() throws Exception {
		super.testPalsson_aureus_iSB619_xrev();
	}

	@Override
	public void testPalsson_aureus_iSB619_xspec() throws Exception {
		super.testPalsson_aureus_iSB619_xspec();
	}

	@Override
	public void testPalsson_barkeri_iAF692_xspec() throws Exception {
		super.testPalsson_barkeri_iAF692_xspec();
	}

	@Override
	public void testPalsson_coli_iJR904_xnone_nobio() throws Exception {
		super.testPalsson_coli_iJR904_xnone_nobio();
	}

	@Override
	public void testPalsson_coli_iJR904_xnone() throws Exception {
		super.testPalsson_coli_iJR904_xnone();
	}

	@Override
	public void testPalsson_coli_iJR904_xrev() throws Exception {
		super.testPalsson_coli_iJR904_xrev();
	}

	@Override
	public void testPalsson_coli_iJR904_xspec() throws Exception {
		super.testPalsson_coli_iJR904_xspec();
	}

	@Override
	public void testPalsson_coli_Robert_reduced() throws Exception {
		super.testPalsson_coli_Robert_reduced();
	}

	@Override
	public void testPalsson_coli_Robert() throws Exception {
		super.testPalsson_coli_Robert();
	}

	@Override
	public void testPalsson_pylori_iIT341_glc() throws Exception {
		super.testPalsson_pylori_iIT341_glc();
	}
	@Override
	public void testPalsson_pylori_iIT341_redin() throws Exception {
		super.testPalsson_pylori_iIT341_redin();
	}

	@Override
	public void testPalsson_pylori_iIT341_xspec() throws Exception {
		super.testPalsson_pylori_iIT341_xspec();
	}
	
	@Override
	public void testPalsson_subtilisPalsson07() throws Exception {
		super.testPalsson_subtilisPalsson07();
	}

	@Override
	public void testPalsson_yeastc_iLL672_unbalancedMetas() throws Exception {
		super.testPalsson_yeastc_iLL672_unbalancedMetas();
	}

	@Override
	public void testPalsson_yeastc_iLL672() throws Exception {
		super.testPalsson_yeastc_iLL672();
	}

	@Override
	public void testPalsson_yeastc_iND750() throws Exception {
		super.testPalsson_yeastc_iND750();
	}

	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		final FilteredMetabolicNetwork filtered = new FilteredMetabolicNetwork(network, suppressedReactions);
		internalTestMetabolicNetwork(network, filtered);
	}
	public void internalTestMetabolicNetwork(MetabolicNetwork originalNetwork, MetabolicNetwork reducedNetwork) throws Exception {
		final File file = fileOut.get();
		new MatlabGenerator("mnet").writeAll(reducedNetwork, Print.createWriter(file));
		LogPkg.LOGGER.info("written to file: " + file.getAbsolutePath());
	}

}
