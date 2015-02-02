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

public class FaColiTest extends ch.javasoft.metabolic.parse.FaColiTest {
	
	private ThreadLocal<String> mModelName	= new ThreadLocal<String>();
	private ThreadLocal<String> mFileName	= new ThreadLocal<String>();
	
	private final boolean sbml = false;//if false: matlab
	
	private static final File FOLDER_OUT = new File(System.getProperty("java.io.tmpdir"));
	

	@Override
	public void testAce() throws Exception {
		mModelName.set("E.coli, configuration -ace-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-ace");
		super.testAce();
	}
	@Override
	public void testAc() throws Exception {
		mModelName.set("E.coli, configuration -ac-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-ac");
		super.testAc();
	}
	@Override
	public void testSuc() throws Exception {
		mModelName.set("E.coli, configuration -suc-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-suc");
		super.testSuc();
	}
	@Override
	public void testGl() throws Exception {
		mModelName.set("E.coli, configuration -gl-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-gl");
		super.testGl();
	}
	@Override
	public void testGly() throws Exception {
		mModelName.set("E.coli, configuration -gly-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-gly");
		super.testGly();
	}
	@Override
	public void testStandard() throws Exception {
		mModelName.set("E.coli, configuration -standard-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-ac");
		super.testStandard();
	}
	@Override
	public void testAll() throws Exception {
		mModelName.set("E.coli, configuration -all-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-all");
		super.testAll();
	}
	
	@Override
	public void testSuper() throws Exception {
		mModelName.set("E.coli, configuration -super-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-super");
		super.testSuperX();
	}
	@Override
	public void testSuperX() throws Exception {
		mModelName.set("E.coli, configuration -super-x-: Stelling etc., Nature, 420:190-193, 2002");
		mFileName.set("coli-superx");
		super.testSuper();
	}
	
	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		final FilteredMetabolicNetwork filtered = new FilteredMetabolicNetwork(network, suppressedReactions);
		internalTestMetabolicNetwork(network, filtered);
	}
	@SuppressWarnings("all")
	public void internalTestMetabolicNetwork(MetabolicNetwork originalNetwork, MetabolicNetwork reducedNetwork) throws Exception {
		final File fileOut = new File(FOLDER_OUT, mFileName.get() + (sbml ? ".sbml" : ".m"));
		final String modelName = mModelName.get();
		if (sbml) {
			new SbmlGenerator(reducedNetwork, modelName).write(fileOut);
		}
		else {
			new MatlabGenerator("mnet", modelName).writeAll(reducedNetwork, Print.createWriter(fileOut));			
		}
		LogPkg.LOGGER.info("written to file: " + fileOut.getAbsolutePath());
	}
}
