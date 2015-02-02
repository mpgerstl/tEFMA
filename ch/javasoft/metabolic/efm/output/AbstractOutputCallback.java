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

import java.io.IOException;

import ch.javasoft.metabolic.efm.output.EfmOutputEvent.Kind;

/**
 * An abstract pre-implementation of {@link EfmOutputCallback}, distributing the
 * different event kinds in {@link #callback(EfmOutputEvent)} to the abstract
 * per-kind methods {@link #callbackPre(EfmOutputEvent)}, 
 * {@link #callbackEfmOut(EfmOutputEvent)}, and 
 * {@link #callbackPost(EfmOutputEvent)}.
 */
abstract public class AbstractOutputCallback implements EfmOutputCallback {

	//inherit javadoc comments
	public void callback(EfmOutputEvent evt) {
		try {
			switch(evt.getKind()) {
				case PRE:
					callbackPre(evt);
					break;
				case EFM_OUT:
					callbackEfmOut(evt);
					break;
				case POST:
					callbackPost(evt);
					break;
				default:	
					//no such kind, should not happen
					throw new RuntimeException("unknown kind " + evt.getKind());					
			}
		}
		catch (IOException ex) {			
			throw new RuntimeException(evt.toString(), ex);
		}
	}
	
	/**
	 * Called once initially indicating that efm output starts now. Event kind
	 * is {@link Kind#PRE}.
	 * 
	 * @param evt			the event of kind {@link Kind#PRE}
	 * @throws IOException	if an io exception occurs when writing the output
	 */
	abstract protected void callbackPre(EfmOutputEvent evt) throws IOException;

	/**
	 * Called once for each elementary mode. Event kind is {@link Kind#EFM_OUT}.
	 * 
	 * @param evt			the event of kind {@link Kind#EFM_OUT}
	 * @throws IOException	if an io exception occurs when writing the output
	 */
	abstract protected void callbackEfmOut(EfmOutputEvent evt) throws IOException;

	/**
	 * Called once at the end of efm outputting. Event kind is
	 * is {@link Kind#POST}.
	 * 
	 * @param evt			the event of kind {@link Kind#POST}
	 * @throws IOException	if an io exception occurs when writing the output
	 */
	abstract protected void callbackPost(EfmOutputEvent evt) throws IOException;

}
