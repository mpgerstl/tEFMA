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
package ch.javasoft.metabolic.efm.output;

import ch.javasoft.metabolic.efm.output.EfmOutputEvent.Kind;

/**
 * <tt>EfmOutputCallback</tt> is a generic callback interface which is used for
 * post efm calculation processing. Typical post processing is writing the
 * calculated efms to an output file. 
 * 
 * <p>For efficiency purposes, a callback granularity has to be defined 
 * (&rarr; {@link #getGranularity()}), which can be used by the caller to 
 * optimize preparation of the efms which can be quite expensive (uncompression
 * of efms, re-establishing the double flux values from binary flux vectors). 
 * 
 * The {@link #callback(EfmOutputEvent)} method is invoked by the efm 
 * computation algorithm, if the granularity is not 
 * {@link CallbackGranularity#Null} and if the binary-only option of the 
 * efm-algorithm is not set.
 */
public interface EfmOutputCallback {
	/**
	 * Returns the callback granularity, giving hints for efm preparation to
	 * the caller of this callback. See class comments for more details.
	 * 
	 * @return	the callback granularity, giving hints for efm preparation to
	 * 			the caller of this calllback. See class comments for more details.
	 */
	CallbackGranularity getGranularity();
	
	/**
	 * The actual callback method. It is invoked by the efm computation 
	 * algorithm, if the granularity is not {@link CallbackGranularity#Null} and 
	 * if the binary-only option of the efm-algorithm is not set.
	 * 
	 * <p>This method is called with 3 different event kinds, initially with
	 * event kind {@link Kind#PRE}, once for every efm with event kind 
	 * {@link Kind#EFM_OUT} and finally once with kind {@link Kind#POST}.
	 * 
	 * @param evt	the output event object, with access to network, efm etc. 
	 */
	void callback(EfmOutputEvent evt);
	
	/**
	 * Return false if the output goes to the log file. This avoids logging
	 * statements in between the output data.
	 */
	boolean allowLoggingDuringOutput();
	
	/**
	 * Returns {@code true} if the {@code #callback(EfmOutputEvent)} can 
	 * concurrently be called by multiple threads, and {@code false} otherwise.
	 * If the callback is not thread-safe, callback calls are synchronized.
	 */
	boolean isThreadSafe();
}