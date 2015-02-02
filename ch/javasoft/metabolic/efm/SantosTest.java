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
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.output.mat.MatFileOutputCallback;
import ch.javasoft.util.logging.Loggers;

public class SantosTest extends ch.javasoft.metabolic.parse.SantosTest {

	static {
		final CompressionMethod[] compression = CompressionMethod.STANDARD;
		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
			Loggers.getRootLogger().setLevel(Level.FINE);			
			Logger.getLogger("compress.data").setLevel(Level.INFO);
		}
	}

	@Override
	public void test_P_aeruginosa_full() throws Exception {
		super.test_P_aeruginosa_full();
	}
	@Override
	public void test_P_putida_full() throws Exception {
		super.test_P_putida_full();
	}
	@Override
	public void test_P_putida_core() throws Exception {
		super.test_P_putida_core();
	}
	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (suppressedReactions != null) {
			Config.getConfig().getReactionsToSuppress().addAll(suppressedReactions);
		}
		File outFile = new File("/home/terzerm/eth/metabolic-efm-run/results-other/mnet_santos.mat");
		ElementaryFluxModes.calculateCallback(
			network, new MatFileOutputCallback(
				network, outFile.getParentFile(), outFile.getName()/*, 2000000*/
			)
		);
		LogPkg.LOGGER.info("written modes to file(s): " + outFile.getAbsolutePath());
	}

}
