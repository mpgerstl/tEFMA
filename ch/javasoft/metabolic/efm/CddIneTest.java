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
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.util.logging.Loggers;

public class CddIneTest extends TestCase {
	
    private static final File FOLDER_INE = new File("../cdd-data/ine");

	static {
		//LogFormatter.setDefaultFormat(LogFormatter.FORMAT_PLAIN);
//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;
//		final CompressionMethod[] compression = CompressionMethod.methods(CompressionMethod.UniqueFlows, CompressionMethod.DeadEnd, CompressionMethod.Recursive);
//		final CompressionMethod[] compression = CompressionMethod.methods(CompressionMethod.UniqueFlows, CompressionMethod.Recursive);
		final CompressionMethod[] compression = CompressionMethod.NONE;
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "AbsLexMin", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.double_, Norm.min)) {
		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "LexMin", compression, false, Arithmetic.double_, Norm.min)) {
//		if (Config.initForJUnitTest(PatternTreeRankAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
			Loggers.getRootLogger().setLevel(Level.FINE);
		}
	}

    public void testCube2() throws IOException {
        internalTestIne("cube2.ine");
    }
    public void testCube3() throws IOException {
        internalTestIne("cube3.ine");
    }
    public void testCube4() throws IOException {
        internalTestIne("cube4.ine");
    }
    public void testCube6() throws IOException {
        internalTestIne("cube6.ine");
    }
    public void testCube8() throws IOException {
        internalTestIne("cube8.ine");
    }
    public void testCube10() throws IOException {
        internalTestIne("cube10.ine");
    }
    
    public void testCube14() throws IOException {
        internalTestIne("cube14.ine");
    }
    
    public void testCube16() throws IOException {
        internalTestIne("cube16.ine");
    }
    public void testCube18() throws IOException {
        internalTestIne("cube18.ine");
    }
    public void testCube20() throws IOException {
        internalTestIne("cube20.ine");
    }
    
    public void testCross6() throws IOException {
        internalTestIne("cross6.ine");
    }
    
    public void testCross8() throws IOException {
        internalTestIne("cross8.ine");
    }
    
    public void testCross10() throws IOException {
        internalTestIne("cross10.ine");
    }
    
    public void testCross12() throws IOException {
        internalTestIne("cross12.ine");
    }
    
    public void testCcp4rev() throws IOException {
        internalTestIne("ccp4.ine");
    }
//    public void testCcp4() throws IOException {
//        internalTestIneFile(FOLDER_EXT, "ccp4.ext");
//    }
    public void testCcp5rev() throws IOException {
        internalTestIne("ccp5.ine");
    }
//    public void testCcp5() throws IOException {
//        internalTestIneFile(FOLDER_EXT, "ccp5.ext");
//    }
    public void testCcp6rev() throws IOException {
        internalTestIne("ccp6.ine");
    }
//    public void testCcp6() throws IOException {
//        internalTestIneFile(FOLDER_EXT, "ccp6.ext");
//    }
    public void testCcp7rev() throws IOException {
        internalTestIne("ccp7.ine");
    }
//    public void testCcp7() throws IOException {
//        internalTestIneFile(FOLDER_EXT, "ccp7.ext");
//    }
//    public void testCcc6() throws IOException {
//        internalTestIneFile(FOLDER_EXT, "ccc6.ext");
//    }
//    public void testCcc7() throws IOException {
//        internalTestIneFile(FOLDER_EXT, "ccc7.ext");
//    }
    public void testReg600_5() throws IOException {
        internalTestIne("reg600-5.ine");
    }
    public void testProdmT5() throws IOException {
        internalTestIne("prodmt5.ine");
    }
    public void testProdmT62() throws IOException {
        internalTestIne("prodst62.ine");
    }
    /** 18553 vertices */
    public void testMit31_20() throws IOException {
        internalTestIne("mit31-20.ine");
    }
    /** 29108 vertices */
    public void testMit41_16() throws IOException {
        internalTestIne("mit41-16.ine");
    }
    /** 3149579 vertices */
    public void testMit71_61() throws IOException {
        internalTestIne("mit71-61.ine");
    }
    /** 323188 ? vertices */
    public void testMit90_86() throws IOException {
        internalTestIne("mit90-86.ine");
    }
    public void testMit729_9() throws IOException {
        internalTestIne("mit729-9.ine");
    }
    
    /** 367,525 vertices */
    public void testStein27() throws Exception {
        internalTestIne("stein27.ine");
    }

    public void internalTestIne(String fileName) throws FileNotFoundException, IOException {
		File file = new File(FOLDER_INE, fileName);
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
		ElementaryFluxModes.calculateLogCountOnly(metaNet);
	}

}
