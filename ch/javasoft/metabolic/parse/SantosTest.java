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

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;
import ch.javasoft.util.Arrays;

abstract public class SantosTest extends AbstractParseTestCase {
	private static final File FOLDER_DATA	= new File("../metabolic-data/santos/");
	
	public void test_P_putida_core() throws Exception {
		internalTest(new File(FOLDER_DATA, "P-putida/Ppu-070426-core.csv"));
	}
	public void test_P_putida_full() throws Exception {
		internalTest(new File(FOLDER_DATA, "P-putida/Ppu-070426-full.csv"));
	}
	public void test_P_aeruginosa_full() throws Exception {
		internalTest(new File(FOLDER_DATA, "P-aeruginosa/Pae-070426-full.csv"));
	}

	private void internalTest(File file) throws Exception {
		internalTest(file, null, null);
	}
	private void internalTest(File file, Pattern externalPattern, String[] excludeReactions) throws Exception {
		Reaction[] reacts = externalPattern == null ? 
				new PalssonParser().parseReactions(file) : 
				new PalssonParser().parseReactions(file, externalPattern);
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(reacts);
		System.out.println("parsed network: " + netSize(metaNet));
		final Set<String> suppressedReacs = Arrays.asSet(excludeReactions);
		internalTestOrDelegate(metaNet, suppressedReacs);			
	}
	private static String netSize(MetabolicNetwork net) {
		return net.getMetabolites().length() + " metabolites, " + net.getReactions().length() + " reactions";
	}
}
