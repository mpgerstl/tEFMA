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
package ch.javasoft.metabolic.compress.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import ch.javasoft.metabolic.compress.CompressionMethod;

/**
 * Xml configuration parser utility for compression stuff, expects an entry
 * like the following:
 * <pre>
	<metabolic-compression>
		<preprocess-duplicate-genes value="true"/>
		<compression-methods>
			<method name="CoupledZero" value="false"/>
			<method name="CoupledContradicting" value="false"/>
			<method name="CoupledCombine" value="false"/>
			<method name="UniqueFlows" value="false"/>
			<method name="DuplicateGene" value="false"/>
			<method name="InterchangeableMetabolite" value="false"/>
		</compression-methods>
	</metabolic-compression>	
 * </pre>
 */
public class MetabolicCompressionConfig {
	
	private final Element mRoot;
	
	public MetabolicCompressionConfig(Element elMetabolicCompression) {
		mRoot = elMetabolicCompression;
	}
	
	/**
	 * Returns the preprocess-duplicate-genes flag, or false if no such flag
	 * is configured
	 * 
	 * @return	the preprocess-duplicate-genes flag
	 */
	public boolean getPreprocessDuplicateGenes() {
		Element el = mRoot.element(XmlElement.preprocess_duplicate_genes.getXmlName());
		return el == null ? false : Boolean.parseBoolean(el.attributeValue(XmlAttribute.value.getXmlName()));
	}
	/**
	 * Returns the compression methods derived from the xml configuration, or 
	 * {@link CompressionMethod#NONE} if no methods are specified.
	 * 
	 * @return	the compression methods	
	 * @throws XmlConfigException		if a method cannot be parsed 
	 */
	public CompressionMethod[] getCompressionMethods() throws XmlConfigException {
		Element elMethods = mRoot.element(XmlElement.compression_methods.getXmlName());
		if (elMethods == null) return CompressionMethod.NONE;
		Iterator it = elMethods.elementIterator(XmlElement.method.getXmlName());
		List<CompressionMethod> cmpMethods = new ArrayList<CompressionMethod>();
		while (it.hasNext()) {
			Element elMethod	= (Element)it.next();
			String methodName	= elMethod.attributeValue(XmlAttribute.name.getXmlName());
			String strOn		= elMethod.attributeValue(XmlAttribute.value.getXmlName());
			boolean on			= strOn == null ? true : Boolean.parseBoolean(strOn);
			if (on) {
				try {
					cmpMethods.add(CompressionMethod.valueOf(methodName));
				}
				catch (Exception ex) {
					throw new XmlConfigException("invalid compression method: " + methodName, elMethod);
				}
			}
		}
		CompressionMethod[] res = new CompressionMethod[cmpMethods.size()];
		return cmpMethods.toArray(res);
	}
	
}
