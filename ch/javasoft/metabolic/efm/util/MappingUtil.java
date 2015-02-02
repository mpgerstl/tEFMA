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
package ch.javasoft.metabolic.efm.util;

/**
 * <tt>MappingUtil</tt> contains static utility functions for row/column mapping
 * arrays.
 */
public class MappingUtil {

	/**
	 * Returns an initial mapping, where map[i] = i.
	 */
    public static int[] getInitialMapping(final int size) {
    	final int[] res = new int[size];
    	for (int col = 0; col < size; col++) res[col] = col;
    	return res;
    }
	/**
	 * Returns a clone of the given mapping. This method is faster than the 
	 * clone method of an array
	 */
    public static int[] clone(final int[] original) {
		final int[] clone = new int[original.length];
		System.arraycopy(original, 0, clone, 0, original.length);
		return clone;
    }
    
    /**
     * Inverts the mapping and returs it. In the resulting mapping,
     * inv[orig[i]] = i
     */
    public static int[] getInvertedMapping(int[] mapping) {
    	final int[] inverted = new int[mapping.length];
    	invertMapping(mapping, inverted);
    	return inverted;
    }
    /**
     * Inverts the mapping into the source array. In the resulting mapping,
     * inv[orig[i]] = i
     */
    public static void invertMapping(int[] mapping) {
    	invertMapping(mapping, mapping);
    }
    /**
     * Inverts the mapping into the source array. In the resulting mapping,
     * inv[orig[i]] = i. The method works also if orig and inv is the same 
     * array.
     */
    public static void invertMapping(int[] orig, final int[] inv) {
    	if (inv == orig) {
    		orig = clone(orig);
    	}
    	for (int ii = 0; ii < orig.length; ii++) {
			inv[orig[ii]] = ii;
		}
    }

	//no instances
	private MappingUtil() {}
}
