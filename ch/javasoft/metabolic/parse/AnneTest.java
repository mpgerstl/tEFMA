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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Pattern;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;

abstract public class AnneTest extends AbstractParseTestCase {
	private static final File FOLDER_DATA	= new File("../metabolic-data/anne/");
	
	public void testAnneYeast_all() throws Exception {
		internalTest(new File(FOLDER_DATA, "anne_yeast_all.csv"));
	}
	public void testAnneYeast_glc() throws Exception {
		internalTest(new File(FOLDER_DATA, "anne_yeast_glc.csv"));
	}
	public void testAnneYeast_eth() throws Exception {
		internalTest(new File(FOLDER_DATA, "anne_yeast_eth.csv"));
	}
	
	public void testAnneYeast_iAK0815_Glc_Etoh() throws Exception {
		internalTestStoich(
			"iAK0815_Glc_Etoh-stoich.txt",
			"iAK0815_Glc_Etoh-mnames.txt",
			"iAK0815_Glc_Etoh-rnames.txt",
			"iAK0815_Glc_Etoh-revs.txt"				
		);
	}
	public void testAnneYeast_iAK0815_Glc() throws Exception {
		internalTestStoich(
			"iAK0815_Glc-stoich.txt",
			"iAK0815_Glc-mnames.txt",
			"iAK0815_Glc-rnames.txt",
			"iAK0815_Glc-revs.txt"				
		);
	}
	public void testAnneYeast_iAK0815_Etoh() throws Exception {
		internalTestStoich(
			"iAK0815_Etoh-stoich.txt",
			"iAK0815_Etoh-mnames.txt",
			"iAK0815_Etoh-rnames.txt",
			"iAK0815_Etoh-revs.txt"				
		);
	}

	private void internalTest(File file) throws Exception {
		internalTest(file, null);
	}
	
	private void internalTestStoich(String fStoich, String fMetaNames, String fReacNames, String fRev) throws Exception {
		internalTestStoich(
			new File(FOLDER_DATA, fStoich), 
			new File(FOLDER_DATA, fMetaNames), 
			new File(FOLDER_DATA, fReacNames), 
			new File(FOLDER_DATA, fRev)				
		);
	}
	private void internalTestStoich(File fStoich, File fMetaNames, File fReacNames, File fRev) throws Exception {		
		final BufferedReader rdStoich = new BufferedReader(new FileReader(fStoich));
		final BufferedReader rdMetaNames = new BufferedReader(new FileReader(fMetaNames));
		final BufferedReader rdReacNames = new BufferedReader(new FileReader(fReacNames));
		final BufferedReader rdRev = new BufferedReader(new FileReader(fRev));
		final StoichParser parser = StoichParser.getWhitespaceSeparatedStoichParser();
		final MetabolicNetwork metaNet = parser.parse(rdStoich, rdMetaNames, rdReacNames, rdRev);
		//System.out.println("parsed network: " + netSize(metaNet));
		internalTestOrDelegate(metaNet, null);			
	}
	private void internalTest(File file, Pattern externalPattern) throws Exception {
		Reaction[] reacts = externalPattern == null ? 
				new PalssonParser().parseReactions(file) : 
				new PalssonParser().parseReactions(file, externalPattern);
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(reacts);
		System.out.println("parsed network: " + netSize(metaNet));
		internalTestOrDelegate(metaNet, null);			
	}
	private static String netSize(MetabolicNetwork net) {
		return net.getMetabolites().length() + " metabolites, " + net.getReactions().length() + " reactions";
	}
}
