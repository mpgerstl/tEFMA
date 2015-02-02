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

public class SbmlTest extends ch.javasoft.metabolic.parse.SbmlTest {
	
	private ThreadLocal<String> mModelName		= new ThreadLocal<String>();
	private ThreadLocal<String> mMatFileName	= new ThreadLocal<String>();
	private ThreadLocal<String> mXlsFileName	= new ThreadLocal<String>();
	
	@Override
	public void testUrea_iAbaylyiv4() throws Exception {
		mModelName.set("iAbaylyiv4");
		mXlsFileName.set("iAbaylyiv4.xls");
		mMatFileName.set("iAbaylyiv4.m");
		super.testUrea_iAbaylyiv4();
	}
	@Override
	public void test_yest_jamboree_model_final_annotated() throws Exception {
		mModelName.set("Yeast: jamboree_model_final_annotated");
		mXlsFileName.set("jamboree_model_final_annotated.xls");
		mMatFileName.set("jamboree_model_final_annotated.m");
		super.test_yest_jamboree_model_final_annotated();
	}
	
	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		final FilteredMetabolicNetwork filtered = new FilteredMetabolicNetwork(network, suppressedReactions);
		internalTestMetabolicNetwork(network, filtered);
	}
	public void internalTestMetabolicNetwork(MetabolicNetwork originalNetwork, MetabolicNetwork reducedNetwork) throws Exception {
		final File matFileOut	= new File(FOLDER_OUT, mMatFileName.get());
		final File xlsFileOut	= new File(FOLDER_OUT, mXlsFileName.get());
		final String modelName	= mModelName.get();
		new MatlabGenerator(modelName).writeAll(reducedNetwork, Print.createWriter(matFileOut));
		LogPkg.LOGGER.info("written to file: " + matFileOut.getAbsolutePath());
		new ExcelGenerator(reducedNetwork).writeTo(xlsFileOut);
		LogPkg.LOGGER.info("written to file: " + xlsFileOut.getAbsolutePath());
	}
}
