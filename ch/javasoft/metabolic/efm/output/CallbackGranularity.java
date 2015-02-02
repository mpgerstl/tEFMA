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

/** 
 * The granularity of the output. Has impact on the post-processing
 * steps since uncompression might be needed or not, and reconstruction
 * of double-valued fluxes might be desired or not. 
 */
public enum CallbackGranularity {
	/** No output at all*/
	Null, 
	/** Counting of compressed efms*/
	CountCompressed, 
	/** Binary values (flux/no flux) for compressed reactions*/
	BinaryCompressed, 
	/** Counting of uncompressed efms*/
	CountUncompressed, 
	/** Binary values (flux/no flux) for uncompressed reactions*/
	BinaryUncompressed, 
	/** Sign values (forward/reverse/no flux = 1/-1/0) for uncompressed reactions*/
	SignUncompressed, 
	/** Double-valued fluxes for uncompressed reactions*/
	DoubleUncompressed;
	
	/**
	 * Returns <code>true</code> If uncompression of the efm's is needed, which
	 * is true if this granularity is none of {@link #Null}, 
	 * {@link #CountCompressed} or {@link #BinaryCompressed}. 
	 */
	public boolean isUncompressionNeeded() {
		return this == CountUncompressed || this == BinaryUncompressed || this == SignUncompressed || this == DoubleUncompressed;
	}
	/**
	 * Returns <code>true</code> If binary flux values are sufficient for the 
	 * output derivation, i.e. if this is not {@link #DoubleUncompressed}.
	 * Binary values are sufficient if only flux/no flux is of interest. Note
	 * that this also holds for {@link #SignUncompressed} since positive and
	 * negative flux values are expressed by separate reactions (split 
	 * reversible reactions).
	 */
	public boolean isBinarySufficient() {
		return this != DoubleUncompressed;
	}
	
	/**
	 * Returns true if output per efm is to expect, that is, returns 
	 * <tt>true</tt> for {@link #BinaryCompressed}, 
	 * {@link #BinaryUncompressed} and {@link #DoubleUncompressed}
	 */
	public boolean isPerEfmOutput() {
		return 
			this == BinaryCompressed || 
			this == BinaryUncompressed || 
			this == SignUncompressed || 
			this == DoubleUncompressed;
	}
}