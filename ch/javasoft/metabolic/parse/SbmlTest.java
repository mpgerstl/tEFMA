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

import ch.javasoft.io.Files;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;

abstract public class SbmlTest extends AbstractParseTestCase {
	
	protected static final File FOLDER_IN	= new File("../metabolic-data/sbml/");
	protected static final File FOLDER_OUT	= Files.getTempDir();
	
	protected ThreadLocal<File> fileOut = new ThreadLocal<File>();

	public void testUrea_iAbaylyiv4() throws Exception {
		internalTest(
				new File(FOLDER_IN, "iAbaylyiv4.xml"),
				new File(FOLDER_OUT, "iAbaylyiv4.m"),
				"Extraorganism"
			);
	}
	public void testSbml_yest_dirk1() throws Exception {
		internalTest(
			new File(FOLDER_IN, "Yeast_model_Dirk.xml"),
			new File(FOLDER_OUT, "Yeast_model_Dirk.m"),
			"external"
		);
	}
	public void testSbml_yest_dirk2() throws Exception {
		internalTest(
			new File(FOLDER_IN, "Yeast_model_Dirk2.xml"),
			new File(FOLDER_OUT, "Yeast_model_Dirk2.m"),
			"external"
		);
	}
	public void test_yest_jamboree_model_final_annotated() throws Exception {
		internalTest(
			new File(FOLDER_IN, "jamboree_model_final_annotated.xml"),
			new File(FOLDER_OUT, "jamboree_model_final_annotated.m"),
			"Extracellular"
		);
	}
	public void testSmallExample() throws Exception {
		internalTest(
			new File(FOLDER_IN, "SmallExample.xml"),
			new File(FOLDER_OUT, "SmallExample.m"),
			"external" /*does not really exist! */
		);
	}
	private void internalTest(File inFile, File outFile, String externalCompartment) throws Exception {
		fileOut.set(outFile);
		MetabolicNetwork metaNet = new SbmlParser(externalCompartment, false).parse(inFile);
		internalTestOrDelegate(metaNet, null);			
	}
}
