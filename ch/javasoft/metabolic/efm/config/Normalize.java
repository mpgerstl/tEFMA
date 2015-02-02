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

import ch.javasoft.metabolic.Norm;
import ch.javasoft.xml.config.XmlConfigException;

/**
 * <tt>Normalize</tt> defines the normalization method to use when outputting
 * flux values. This class is used for configuration purposes, its constants
 * correspond to thos of {@link Norm}.
 */
public enum Normalize {
	/** Maximum absolute value is 1*/
	max(ch.javasoft.metabolic.Norm.max),
	/** Minimum absolute value is 1*/
	min(ch.javasoft.metabolic.Norm.min),
	/** Use norm 2, that is, the vector length is 1*/
	norm2(ch.javasoft.metabolic.Norm.norm2),
	/** 
	 * Like norm 2, but all values are squared, possibly negative. To get the
	 * original value, take the square root of the absolute value and keep the
	 * sign, that is, <tt>v = sgn(v2) * sqrt(abs(v2))</tt>
	 * <p>
	 * This normalization method is useful if exact arithmetic is used, and no
	 * square root is supported for fraction numbers. 
	 */
	squared(ch.javasoft.metabolic.Norm.squared),
	/** Do not normalize at all*/
	none(ch.javasoft.metabolic.Norm.none);
	private Normalize(Norm norm) {
		this.norm = norm;
	}
	public final Norm norm;
	
	/**
	 * Parse a norm string and return the appropriate constant, or throw
	 * an exception if not recognized as such.
	 */
	public static Normalize parse(Attribute attribute) throws XmlConfigException {
		final String str = attribute.getValue();
		try {
			return valueOf(str.toLowerCase());
		}
		catch (IllegalArgumentException ex) {
			throw new XmlConfigException(
				"invalid value for norm, expected " + 
				Arrays.toString(values()) + ", but found: " + str, attribute);
		}
	}
	
	/**
	 * Returns the Normalize instance corresponding to the given Norm value.
	 */
	public static Normalize valueOf(Norm norm) {
		for (Normalize n : values()) {
			if (n.norm == norm) return n;
		}
		throw new RuntimeException("no such normalize constant: " + norm);
	}
	
	/**
	 * Returns true if the normalization method does not cause truncation.
	 */
	public boolean isExact() {
		return !Normalize.norm2.equals(this);
	}
}
