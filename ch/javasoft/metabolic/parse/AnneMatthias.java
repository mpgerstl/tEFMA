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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;

abstract public class AnneMatthias extends AbstractParseTestCase {
	private static final File FOLDER_DATA	= new File("../metabolic-data/anne_matth/");
	
	public void testDirectionsDefault() throws Exception {
		internalTestPalsson(
			new File(FOLDER_DATA, "reactions.txt"),
			new File(FOLDER_DATA, "directionsdefault.txt")
		);
	}
	public void testDirectionsQuinone() throws Exception {
		internalTestPalsson(
			new File(FOLDER_DATA, "reactions.txt"),
			new File(FOLDER_DATA, "directionsquinone.txt")
		);
	}
	
	private void internalTestPalsson(File reactionsFile, File directionsFile) throws Exception {
		final String[] symbols = new String[] {"<--", "<==>", "-->"};//must correspond to -1,0,1 by index, see *1

		BufferedReader reactReader = new BufferedReader(new FileReader(reactionsFile));
		BufferedReader direcReader = new BufferedReader(new FileReader(directionsFile));
		String reactLine, directLine;
		
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(byteOut);
		
		int lineIndex = 0;
		while ((reactLine = reactReader.readLine()) != null && (directLine = direcReader.readLine()) != null) {
			lineIndex++;
			int curDirIndex = -1;
			int index		= -1;
			for (int i = 0; i < symbols.length; i++) {
				index = reactLine.indexOf(symbols[i]);
				if (index >= 0) {
					curDirIndex = i;
					break;
				}
			}
			if (index == -1) {
				throw new IOException("no symbol matched at line " + lineIndex + ": " + reactLine);
			}
			int defDirIndex = Integer.parseInt(directLine) + 1;//*1

			final String fixedLine;
			if (curDirIndex == defDirIndex) {
				fixedLine = reactLine;
			}
			else {
				fixedLine = reactLine.substring(0, index) + symbols[defDirIndex] + reactLine.substring(index + symbols[curDirIndex].length());
			}
			pw.println("\"r" + lineIndex + "\"\t\"r" + lineIndex + "\"\t\"" + fixedLine + "\"");
			
		}
		pw.flush();
		if (reactReader.readLine() != null) {
			throw new IOException("expected end of react file");
		}
		if (direcReader.readLine() != null) {
			throw new IOException("expected end of direct file");
		}
		
		Reaction[] reacts = new PalssonParser().parseReactions(new ByteArrayInputStream(byteOut.toByteArray())); 
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(reacts);
		System.out.println("parsed network: " + netSize(metaNet));
		internalTestOrDelegate(metaNet, null);			
	}
	private static String netSize(MetabolicNetwork net) {
		return net.getMetabolites().length() + " metabolites, " + net.getReactions().length() + " reactions";
	}
}
