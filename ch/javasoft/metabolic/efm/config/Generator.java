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
package ch.javasoft.metabolic.efm.config;

import java.util.Arrays;

import org.dom4j.Attribute;

import ch.javasoft.metabolic.Reaction;
import ch.javasoft.util.StringUtil;
import ch.javasoft.xml.config.XmlConfigException;

/**
 * <tt>Generator</tt> specifies the type of generating set to be computed. 
 * Different generating sets define the flux cone, {@link #Gfm} is the minimal
 * set of generators. The sets differ in how reversible reactions are handled.  
 */
public enum Generator {
	/** 
	 * Elementary Flux Modes
	 * <p>
	 * All reversible reactions are split into 2 irreversible reactions. 
	 * <p>  
	 * Two-cycles corresponding to split reversible reactions are excluded.
	 */
	Efm, 
	/** 
	 * Extreme Pathways
	 * <p>
	 * Internal reversible reactions are split into 2 irreversible reactions. 
	 * Reversible exchange reactions are kept unchanged.
	 * <p>  
	 * Two-cycles corresponding to split reversible reactions are excluded.
	 */
	Ep, 
	/** 
	 * Generating Flux Modes
	 * <p>
	 * Reversible reactions are not split since they do not shape the flux cone.
	 */
	Gfm;
	
	public static Generator parse(Attribute attribute) throws XmlConfigException {
		final String str = attribute.getValue();
		try {
			return valueOf(StringUtil.toTitleCase(str));
		}
		catch (IllegalArgumentException ex) {
			throw new XmlConfigException(
				"invalid value for generator, expected " + 
				Arrays.toString(values()) + ", but found: " + str, attribute);
		}
	}

	/**
	 * Returns true if reactions are split during computation, which is always
	 * true for Efm, and true for Ep if it is not an exchange reaction. For 
	 * Gfm, this method always returns false. 
	 */
	public boolean splitReversibleReactions(boolean exchange) {
		return this == Efm || !exchange && this == Ep;
	}
	
	/**
	 * Returns true if this reaction is to split, without considering no-split
	 * reactions from the config. This is true for reversible reactions for 
	 * Efm and true for reversible non-exchange reactions for Ep.
	 */
	public boolean splitReaction(Reaction reac) {
		if (this == Gfm) return false;
		if (!reac.getConstraints().isReversible()) return false;
		return this == Efm || this == Ep && !reac.isExternal();
	}
}
