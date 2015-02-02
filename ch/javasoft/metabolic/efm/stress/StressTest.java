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
package ch.javasoft.metabolic.efm.stress;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.logging.Level;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import ch.javasoft.io.Files;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.ColiTest;
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.sort.SortUtil;
import ch.javasoft.util.logging.Loggers;

public class StressTest extends TestCase {
	
	private static File FOLDER = Files.getTempDir();
	
	public void testColiGlRandom() throws IOException {
		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;
		final int runs 			= 1;
		final String testName	= "testAll";

		ColiTest coliTest = new ColiTest();
		coliTest.setName(testName);

		NumberFormat fmt = NumberFormat.getIntegerInstance();
		fmt.setMinimumIntegerDigits(7);
		fmt.setGroupingUsed(false);
		
		Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.fractional);
//		Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "Random", compression, false, Arithmetic.fractional);
		long timeTotal = 0L;
		for (int i = 1; i <= runs; i++) {
			File logOut = new File(FOLDER, testName + "-" + i + "-log.txt"); 
			File kernel = new File(FOLDER, testName + "-" + i + "-kernel.txt"); 
			File iterat = new File(FOLDER, testName + "-" + i + "-iteration.txt");			
			Loggers.logToFile(logOut, Level.ALL);
			SortUtil.setTraceSortingFile(kernel);
//			((AbstractImpl)ElementaryFluxModes.getImpl()).setTraceIterationsFile(iterat);
			long timeStart = System.currentTimeMillis();
			new TestRunner().doRun(coliTest, false);
			long timeEnd = System.currentTimeMillis();
			long time = (timeEnd - timeStart);
			String strTime = fmt.format(time);
			logOut.renameTo(new File(FOLDER, strTime + "ms-" + logOut.getName()));
			kernel.renameTo(new File(FOLDER, strTime + "ms-" + kernel.getName()));
			iterat.renameTo(new File(FOLDER, strTime + "ms-" + iterat.getName()));
			timeTotal += time;
		}
		final String msg = "average running time: " + (timeTotal / runs) + "ms";
		PrintWriter pw = new PrintWriter(new FileWriter(new File(FOLDER, "average-" + testName + ".txt")));
		pw.println(msg);
		pw.flush();
		pw.close();
		System.out.println(msg);
	}
	public void testColiTspShifts() throws IOException {
		final String testName	= "testAll";
		
		final String rowOrderingPref = "Fixed:1:MostZerosOrAbsLexMin:";
		final String rowOrderingPerm = "1:3:8:16:19:15:18:17:20:21:25:24:23:26:22:5:2:6:14:12:9:7:13:11:10:4";
		final CompressionMethod[] compression = CompressionMethod.STANDARD;
		
		ColiTest coliTest = new ColiTest();
		coliTest.setName(testName);

		NumberFormat fmt = NumberFormat.getIntegerInstance();
		fmt.setMinimumIntegerDigits(7);
		fmt.setGroupingUsed(false);

		int i = 0;
		String rowOrderingP = rowOrderingPerm;
		do {
			i++;
			File logOut = new File(FOLDER, testName + "-" + i + "-log.txt"); 
			File kernel = new File(FOLDER, testName + "-" + i + "-kernel.txt"); 
			File iterat = new File(FOLDER, testName + "-" + i + "-iteration.txt");			
			final String rowOrdering = rowOrderingPref + rowOrderingP;
			Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, rowOrdering, compression, Arithmetic.double_);
			Loggers.logToFile(logOut, Level.ALL);
			SortUtil.setTraceSortingFile(kernel);
//			((AbstractImpl)ElementaryFluxModes.getImpl()).setTraceIterationsFile(iterat);
			long timeStart = System.currentTimeMillis();
			new TestRunner().doRun(coliTest, false);
			long timeEnd = System.currentTimeMillis();
			long time = (timeEnd - timeStart);
			String strTime = fmt.format(time);
			logOut.renameTo(new File(FOLDER, strTime + "ms-" + logOut.getName()));
			kernel.renameTo(new File(FOLDER, strTime + "ms-" + kernel.getName()));
			iterat.renameTo(new File(FOLDER, strTime + "ms-" + iterat.getName()));
			
			//shift: put first row to end
			int index = rowOrderingP.indexOf(':');
			rowOrderingP = rowOrderingP.substring(index + 1) + ':' + rowOrderingP.substring(0, index);
		}
		while (!rowOrderingP.equals(rowOrderingPerm));
		
		
	}
}
