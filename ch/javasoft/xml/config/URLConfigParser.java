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
package ch.javasoft.xml.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.dom4j.Element;

/**
 * The <tt>URLConfigParser</tt> parses the configuration for an URL.
 */
public class URLConfigParser {

	public static enum XmlElement implements XmlNode {
		url;
		public String getXmlName() {
			return name();			
		}
	}
	public static enum XmlAttribute implements XmlNode {
		name; 
		public String getXmlName() {
			return name();
		}		
	}
	
	/**
	 * Parses:
	 * <pre>
		<url name="{work-dir}/{-out[2]}"/>					
	 * </pre>
	 */
	public static URL parseURL(Element urlElement) throws XmlConfigException {
		XmlUtil.checkExpectedElementName(urlElement, XmlElement.url);
		String urlName = urlElement.attributeValue(XmlAttribute.name.getXmlName());
		if (urlName == null) {
			throw new XmlConfigException(XmlAttribute.name.getXmlName() + " attribute missing for " + XmlElement.url.getXmlName() + " element", urlElement);
		}
		try {
			return new URL(urlName);
		}
		catch (MalformedURLException ex) {
			throw new XmlConfigException("cannot parse url '" + urlName + "', e=" + ex, urlElement, ex);
		}
	}
}
