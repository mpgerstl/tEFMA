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

import org.dom4j.Attribute;

import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlNode;

public enum XmlElement implements XmlNode {
	metabolic_compression, metabolic_efm, efm_impl, config, model, memory, 
	efm_output, callback, stream, file, mode, uncompress,
	row_ordering, adjacency_method, maxthreads,  self_test, parse_only, 
	numeric, arithmetic, precision, zero, normalize, generator,
	reactions_to_suppress, reactions_to_enforce, reactions_no_split, temp_dir,
	progress, flag, distribute, nodes, node, command, factories, clazz;
	public String getXmlName() {
		return this == clazz ? "class" : name().replaceAll("_", "-");
	}
	
	public static int parsePrecision(Attribute attribute) throws XmlConfigException {
		final String str = attribute.getValue();
		try {
			return Integer.parseInt(str);
		}
		catch (NumberFormatException ex) {
			throw new XmlConfigException(
				"invalid value for precision: " + str, attribute);
		}
	}
	public static double parseZero(Attribute attribute) throws XmlConfigException {
		final String str = attribute.getValue();
		try {
			return Double.parseDouble(str);
		}
		catch (NumberFormatException ex) {
			throw new XmlConfigException(
				"invalid value for precision: " + str, attribute);
		}
	}

}