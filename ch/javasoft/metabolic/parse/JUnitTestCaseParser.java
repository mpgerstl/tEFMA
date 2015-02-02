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
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Set;

import junit.framework.TestResult;
import junit.textui.TestRunner;
import ch.javasoft.io.NullOutputStream;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.impl.FilteredMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;
import ch.javasoft.metabolic.parse.junit.TestDelegate;

/**
 * Derives a metabolic network by running a junit test case. The networks to 
 * parse are hard-coded in the unit test. Test cases being subclasses of
 * {@link AbstractParseTestCase} can be used here. The parse process sets the
 * delegate to get access to the parsed network.
 * 
 * @see AbstractParseTestCase#setTestDelegate(TestDelegate)
 * @see TestDelegate
 */
public class JUnitTestCaseParser {
	
	/**
	 * Parses a metabolic network by invoking the test method of the given test
	 * case and returning the metabolic network which is parsed in the given 
	 * test, or null if this test did not call 
	 * {@link AbstractParseTestCase#internalTestMetabolicNetwork(MetabolicNetwork, Set)}
	 * 
	 * @param testCase		the test case
	 * @param testMethod	the test method to invoke
	 * @return the parsed network, if any, or null (should normally not happen)
	 */
	public static MetabolicNetwork parse(final AbstractParseTestCase testCase, final String testMethod) {
		final class Result {
			MetabolicNetwork network;
			Set<String>		 suppressedReactions;
		}
		final Result result = new Result();
		final TestDelegate delegate = new TestDelegate() {
			public void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
				result.network 				= network;
				result.suppressedReactions	= suppressedReactions;
			}
		};
		testCase.setName(testMethod);
		testCase.setTestDelegate(delegate);
		TestRunner testRunner = new TestRunner(new PrintStream(NullOutputStream.INSTANCE));
		try {
			TestResult testResult = testRunner.doRun(testCase, false);
			if (testResult.errorCount() > 0) {
				final String msg = "junit parsing failed, errors occurred.";
				LogPkg.LOGGER.warning(msg);
				log(testResult.errors());
				throw new RuntimeException(msg);
			}
			if (testResult.failureCount() > 0) {
				final String msg = "junit parsing completed with failures.";
				LogPkg.LOGGER.warning(msg);
				log(testResult.failures());
				throw new RuntimeException(msg);
			}
		}
		finally {
			testCase.setName(null);
			testCase.setTestDelegate(null);
		}
		if (result.suppressedReactions == null || result.suppressedReactions.isEmpty()) {
			return result.network;
		}
		final FilteredMetabolicNetwork filtered = new FilteredMetabolicNetwork(result.network);
		filtered.excludeReactions(result.suppressedReactions.toArray(new String[0]));
		return filtered;
	}
	
	private static void log(Enumeration testFailures) {
		while (testFailures.hasMoreElements()) {
			LogPkg.LOGGER.warning("\t" + testFailures.nextElement());
		}
	}
	
}
