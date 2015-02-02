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
package ch.javasoft.junit.textui;

import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.runner.Version;
import junit.textui.ResultPrinter;

/**
 * A command line based tool to run tests.
 * <pre>
 * java junit.textui.TestRunner [-wait] TestCaseClass
 * </pre>
 * TestRunner expects the name of a TestCase class as argument.
 * If this class defines a static <code>suite</code> method it 
 * will be invoked and the returned test is run. Otherwise all 
 * the methods starting with "test" having no arguments are run.
 * <p>
 * When the wait command line argument is given TestRunner
 * waits until the users types RETURN.
 * <p>
 * TestRunner prints a trace as the tests are executed followed by a
 * summary at the end. 
 */
public class TestRunner extends junit.textui.TestRunner {

	/**
	 * Constructs a TestRunner.
	 */
	public TestRunner() {
		super();
	}

	/**
	 * Constructs a TestRunner using the given stream for all the output
	 */
	public TestRunner(PrintStream writer) {
		super(writer);
	}
	
	/**
	 * Constructs a TestRunner using the given ResultPrinter all the output
	 */
	public TestRunner(ResultPrinter printer) {
		super(printer);
	}
	
	@Override
	public Test getTest(String suiteClassName) {
		String className	= suiteClassName;
		String methodName	= null;
		int index = suiteClassName.indexOf('#'); 
		if (index > 0) {
			className 	= suiteClassName.substring(0, index);
			methodName	= suiteClassName.substring(index + 1);
		}
		Test test = super.getTest(className);
		if (methodName != null && test instanceof TestSuite) {
			TestSuite suite = (TestSuite)test;
			for (int ii = 0; ii < suite.testCount(); ii++) {
				Test methodTest = suite.testAt(ii);
				if (methodTest instanceof TestCase) {
					if (methodName.equals(((TestCase)methodTest).getName())) {
						return methodTest;
					}
				}
			}
			runFailed("StackTrace \"" + methodName + "\" not found in class \"" + className + "\"");
			return null;
		}
		return test;
	}
	
	/**
	 * Starts a test run. Analyzes the command line arguments
	 * and runs the given test suite.
	 */
	@Override
	public TestResult start(String args[]) throws Exception {
		String testCase= "";
		boolean wait= false;
		
		for (int i= 0; i < args.length; i++) {
			if (args[i].equals("-wait"))
				wait= true;
			else if (args[i].equals("-c")) 
				testCase= extractClassName(args[++i]);
			else if (args[i].equals("-v"))
				System.err.println("JUnit "+Version.id()+" by Kent Beck and Erich Gamma");
			else
				testCase= args[i];
		}
		
		if (testCase.equals("")) 
			throw new Exception("Usage: TestName[#testCaseName] [-wait], where TestName is the name of the TestCase class, testCaseName that of a test method");

		try {
			Test suite= getTest(testCase);
			return doRun(suite, wait);
		}
		catch(Exception e) {
			throw new Exception("Could not create and run test suite: "+e);
		}
	}

	public static void main(String args[]) {
		TestRunner aTestRunner= new TestRunner();
		try {
			TestResult r= aTestRunner.start(args);
			if (!r.wasSuccessful()) 
				System.exit(FAILURE_EXIT);
			System.exit(SUCCESS_EXIT);
		} catch(Exception e) {
			System.err.println(e.getMessage());
			System.exit(EXCEPTION_EXIT);
		}
	}	
	
}