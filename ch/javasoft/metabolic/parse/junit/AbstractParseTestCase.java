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
package ch.javasoft.metabolic.parse.junit;

import java.util.Set;

import junit.framework.TestCase;
import ch.javasoft.metabolic.MetabolicNetwork;

/**
 * The <tt>AbstractParseTestCase</tt> is a special junit test which can be 
 * called and executed locally (as usual junit test) or from an outside program
 * invocation using a main method.
 * <p>
 * The test methods (defined in sublcasses) provide the metabolic networks to
 * be tested. For a normal junit test execution, invoking the test will result
 * in a call to {@link #internalTestMetabolicNetwork(MetabolicNetwork, Set)}, 
 * which is also provided by the implementing class. However, if the tool is
 * invoked from a main method, a {@link #setTestDelegate(TestDelegate) delegate
 * might have been set}, and the actual test is
 * {@link TestDelegate#internalTestMetabolicNetwork(MetabolicNetwork, Set) delegated}.
 */
abstract public class AbstractParseTestCase extends TestCase {

	private TestDelegate testDelegate = null;
	
	/**
	 * Set a test delegate, that is, running the test will end up in the test
	 * method of the delegate instead of 
	 * {@link #internalTestMetabolicNetwork(MetabolicNetwork, Set)}
	 * of this class.
	 */
	public void setTestDelegate(TestDelegate delegate) {
		testDelegate = delegate;
	}
	/**
	 * If a delegate is present, its method with the same signature is called.
	 * Otherwise, {@link #internalTestMetabolicNetwork(MetabolicNetwork, Set)}
	 * is invoked, implemented by the subclasses.
	 * 
	 * @param network				the metabolic network to process (or test)
	 * @param suppressedReactions	the reactions to suppress (remove), or null
	 * 								if all reactions shall be considered
	 */
	protected final void internalTestOrDelegate(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (testDelegate == null) {
			internalTestMetabolicNetwork(network, suppressedReactions);
		}
		else {
			testDelegate.internalTestMetabolicNetwork(network, suppressedReactions);
		}
	}
	
	/**
	 * Called on test execution if no delegate has been set
	 * 
	 * @param network				the original network without setting any 
	 * 								reaction fluxes to zero
	 * @param suppressedReactions	the reactions which should be suppressed, 
	 * 								that is, they cannot carry any flux value.
	 * 								In a compressed network, these reactions are
	 * 								removed
	 * @throws Exception subclasses can throw any exception here
	 */
	abstract protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception;
	
}
