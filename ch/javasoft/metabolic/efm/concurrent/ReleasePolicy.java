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
package ch.javasoft.metabolic.efm.concurrent;

import ch.javasoft.metabolic.efm.config.Config;

/**
 * The <code>ReleasePolicy</code> defines the strategy to release permits
 * acquired from a {@link ConcurrentToken}. Certain policies release permits 
 * immediately, other policies wait until a certain number of permits has been
 * collected and releases all permits concurrently.
 * <p>
 * To enable factory-like instantiation, the implementors are expected to have
 * a public constructor with no arguments.
 */
public interface ReleasePolicy {

	/**
	 * Initialize this policy with the number of concurrent threads working with
	 * the same pool of permits
	 * 
	 * @param config	access to policy configuration parameters
	 * @param jobs		the number of jobs in the queue associated with this 
	 * 					pool of permits
	 * @param threads	the number of threads working with the same pool of 
	 * 					permits
	 */
	void initialize(Config config, int jobs, int threads);
	
	/**
	 * Release a permit using the specified token. Depending on the implementor 
	 * of this policy, the method might immediately release the permit. Other 
	 * policy implementations might also block the caller thread until a certain 
	 * number of permits has been collected, and release all collected permits 
	 * concurrently.
	 * 
	 * @param token		the token used to
	 * 					{@link ConcurrentToken#releasePermits(int) release}
	 * 					the permits
	 * 
	 * @throws InterruptedException if waiting was interrupted unexpectedly 
	 */
	void releasePermit(ConcurrentToken token) throws InterruptedException;
}
