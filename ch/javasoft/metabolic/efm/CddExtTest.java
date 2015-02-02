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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import junit.framework.TestCase;
import ch.javasoft.cdd.parser.CddParser;
import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.util.logging.Loggers;

public class CddExtTest extends TestCase {
	
    private static final File FOLDER_EXT = new File("../cdd-data/ext");

	static {
//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;
		final CompressionMethod[] compression = CompressionMethod.NONE;
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "LexMin", compression, false, Arithmetic.double_, Norm.min)) {
		
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPos", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_)) {
		//fastest for ccp7 and most others:
		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.varint)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.bigint)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_, Norm.min, 1)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "AbsLexMin", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
			Loggers.getRootLogger().setLevel(Level.FINE);			
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new NullspaceEfmModelFactory(), new OutOfCoreMemoryFactory()));       		
		}
	}

    public void testCcp4() throws IOException {
        internalTestExt("ccp4.ext");
    }
    public void testCcp5() throws IOException {
        internalTestExt("ccp5.ext");
    }
    public void testCcp6() throws IOException {
        internalTestExt("ccp6.ext");
    }
    public void testCcp7() throws IOException {
        internalTestExt("ccp7.ext");
    }
    public void testCcp8() throws IOException {
        internalTestExt("ccp8.ext");
    }
    public void testCcc6() throws IOException {
        internalTestExt("ccc6.ext");
    }
    public void testCcc7() throws IOException {
        internalTestExt("ccc7.ext");
    }
    public void testCube6() throws IOException {
    	internalTestExt("cube6.ext");
    }
    public void testCube8() throws IOException {
    	internalTestExt("cube8.ext");
    }
    public void testCube10() throws IOException {
    	internalTestExt("cube10.ext");
    }
    public void testCross6() throws IOException {
    	internalTestExt("cross6.ext");
    }
    public void testCross8() throws IOException {
    	internalTestExt("cross8.ext");
    }
    public void testCross10() throws IOException {
    	internalTestExt("cross10.ext");
    }
    public void testProdmT62() throws IOException {
    	internalTestExt("prodmT62.ext");
    }
    /** 432 facets   */
    public void testTc7_30() throws IOException {
    	internalTestExt("tc7-30.ext");
    }
    /** 1675 facets   */
    public void testTc8_38() throws IOException {
    	internalTestExt("tc8-38.ext");
    }
    /** 6875 facets   */
    public void testTc9_48() throws IOException {
    	internalTestExt("tc9-48.ext");
    }
    /** 41591 facets   */
    public void testTc10_83() throws IOException {
    	internalTestExt("tc10-83.ext");
    }
    /** 250279 facets   */
    public void testTc11_106() throws IOException {
    	internalTestExt("tc11-106.ext");
    }
    /** 1975935 facets   */
    public void testTc12_152() throws IOException {
    	internalTestExt("tc12-152.ext");
    }
    /** 17464356 facets   */
    public void testTc13_254() throws IOException {
    	internalTestExt("tc13-254.ext");
    }

    public void internalTestExt(String fileName) throws FileNotFoundException, IOException {
		File file = new File(FOLDER_EXT, fileName);
		CddParser parser = new CddParser(file);
		BigFraction[][] mx = CddHelper.getMatrix(parser);
		final int dims = (mx.length == 0 ? 0 : mx[0].length);
		LogPkg.LOGGER.info(mx.length + "x" + mx[0].length + " matrix for cdd file: " + file.getAbsolutePath());
		mx = CddHelper.addSlackVariables(mx);
		boolean[] reversible = new boolean[mx.length == 0 ? 0 : mx[0].length];
		for (int i = 0; i < dims; i++) {
			reversible[i] = true;
		}
		
		MetabolicNetwork metaNet = new FractionNumberStoichMetabolicNetwork(new DefaultBigIntegerRationalMatrix(mx, true), reversible);
		
		if (!Config.getConfig().getReactionsNoSplit().isEmpty()) throw new RuntimeException("non-empty no split reactions");
		for (int i = 0; i < dims; i++) {
			Config.getConfig().getReactionsNoSplit().add(metaNet.getReactions().get(i).getName());
		}
//		ElementaryFluxModes.calculateLogCountOnly(metaNet);
		ElementaryFluxModes.calculateFileMatlab(metaNet, new File("/tmp"), "efms.mat");
	}
    

}
