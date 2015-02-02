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
package ch.javasoft.util;

/**
 * The <tt>StringUtil</tt> contains common constants and static utility methods 
 * concerning strings.
 */
public class StringUtil {
	/**
	 * Operating system dependant line separator
	 */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /**
     * Converts a string to title case. In the returned string, each word starts
     * with a title (upper) case letter, all other word letters are lower case.
     * For instance, passing "HELLo mY DoG" will be returned as "Hello My Dog".
     * Null and empty string is returned unchanged.
     */
    public static String toTitleCase(String str) {
    	if (str == null || str.length() == 0) return str;
    	final char[] chars = str.toLowerCase().toCharArray();
    	boolean doTitle = true;
    	for (int i = 0; i < chars.length; i++) {
			if (doTitle) chars[i] = Character.toTitleCase(chars[i]);
			doTitle = Character.isWhitespace(chars[i]);
		}
    	return String.valueOf(chars);
    }
    
    //no instances
    private StringUtil() {}
}
