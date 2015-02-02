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

import org.dom4j.Element;

/**
 * The <tt>ConstConfigParser</tt> parses the configuration for constants. 
 * Supported are classes with public constructor accepting a single string argument.
 */
public class ConstConfigParser {

	public static enum XmlElement implements XmlNode {
		const_;
		public String getXmlName() {
			return this == const_ ? "const" : name();			
		}
	}
	public static enum XmlAttribute implements XmlNode {
		type, value; 
		public String getXmlName() {
			return name();
		}		
	}
	private static class Attribute implements XmlNode {
		private final org.dom4j.Attribute attribute;
		public Attribute(org.dom4j.Attribute attribute) {
			this.attribute = attribute;
		}
		public String getXmlName() {
			return attribute.getName();
		}
	}
	
	private static final Class[] METHOD_SIGNATURE = new Class[] {String.class};
	/*
		<const value="1" type="int"/>
		<const value="-10" type="Integer"/>
		<const value="Hello" type="String"/>
		<const value="1" type="java.math.BigInteger"/>
		<myconst level="1" type="java.math.BigInteger"/>
	 */
	public static int parseIntConstant(Element constElement) throws XmlConfigException {
		return parseConstant(constElement, Integer.class, false).intValue();
	}
	public static long parseLongConstant(Element constElement) throws XmlConfigException {
		return parseConstant(constElement, Long.class, false).longValue();
	}
	public static float parseFloatConstant(Element constElement) throws XmlConfigException {
		return parseConstant(constElement, Float.class, false).floatValue();
	}
	public static double parseDoubleConstant(Element constElement) throws XmlConfigException {
		return parseConstant(constElement, Double.class, false).doubleValue();
	}
	public static boolean parseBooleanConstant(Element constElement) throws XmlConfigException {
		return parseConstant(constElement, Boolean.class, false).booleanValue();
	}
	public static String parseStringConstant(Element constElement, boolean allowNull) throws XmlConfigException {
		return parseConstant(constElement, String.class, allowNull);
	}
	public static <T> T parseConstant(Class<T> type, Element constElement, boolean allowNull) throws XmlConfigException {
		return parseConstant(constElement, type, allowNull);
	}

	private static <T> T parseConstant(Element constElement, Class<T> expectedType, boolean allowNull) throws XmlConfigException {
		final String sValue;
		if (constElement.attributeCount() == 1) {		
			sValue = XmlUtil.getRequiredAttributeValue(constElement, new Attribute(constElement.attribute(0)));
		}
		else {
			sValue = XmlUtil.getRequiredAttributeValue(constElement, XmlAttribute.value);
		}
		if (allowNull && sValue.equals("null")) {
			return null;
		}
		try {
			return expectedType.getConstructor(METHOD_SIGNATURE).newInstance(new Object[] {sValue});
		}
		catch (Exception ex) {
			throw new XmlConfigException("cannot create " + expectedType.getSimpleName() + " const value '" + sValue + "', e=" + ex, constElement, ex);
		}
	}
}
