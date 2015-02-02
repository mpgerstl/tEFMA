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
package ch.javasoft.cdd.parser;

/**
 * The <code>CddFileType</code> represents cdd {@code .ine} or {@code .ext} 
 * files, that is, polyhedra definition files in <b>ine</b>quality or in 
 * <b>ext</b>reme ray/vertex form. For exact definitions of the file formats, 
 * see
 * <pre>ftp://ftp.ifor.math.ethz.ch/pub/fukuda/cdd/cddman/node3.html</pre>
 */
public enum CddFileType {
	/**
	 * A cdd file defining a polyhedra in inequality form, that is, as the
	 * intersection of halfspaces.
	 */
	Ine("H-representation"), 
	/**
	 * A cdd file defining a polyhedra in extreme ray/vertex form.
	 */
	Ext("V-representation");
	
	/**
	 * The preamble in the file, indicating that the definition starts now.
	 */
	public final String preamble;
	
	private CddFileType(String preamble) {
		this.preamble = preamble;
	}
	
	/**
	 * Parses the preamble line and returns the corresponding file type, or 
	 * returns null if the line does not represent a preamble line. Whitespace
	 * at the end of the line is ignored.
	 */
	public static CddFileType parsePreamble(String line) {
		if (line == null) return null;
		for (final CddFileType type : values()) {
			if (line.startsWith(type.preamble)) {
				if (line.substring(type.preamble.length()).trim().length() == 0) {
					return type;
				}
			}
		}
		return null;
	}
}
