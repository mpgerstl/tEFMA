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



/**
 * The <tt>EfmProcessor</tt> handles the writing of efms during the output 
 * process. Due to the large data amount, efm writers might cache or partition 
 * efms, other writers can be used to remap the reactions to the original 
 * positions. 
 *
 * @param <W>	the writer used during the output process
 */
public interface EfmProcessor<W> {

	
	/**
	 * @return the unused reaction indices, if any 
	 */
	abstract int[] initialize(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmCount) throws IOException;
	
	/**
	 * @param cb
	 * @param writer
	 * @param evt
	 * @param efmIndex
	 * 
	 * @return	the cache size after adding the efm
	 */
	abstract int addEfm(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex) throws IOException;
	
	/**
	 * @param cb
	 * @param writer
	 * @param evt
	 * @throws IOException
	 */
	abstract void finalize(EfmOutputCallback cb, W writer, EfmOutputEvent evt) throws IOException;

}
