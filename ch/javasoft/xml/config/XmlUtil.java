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

import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import ch.javasoft.xml.config.XmlConfig.XmlAttribute;

public class XmlUtil {
	
	/**
	 * Returns the child element of the given type which has the desired 
	 * attribute value in the given attribute. In xpath, this would be somewhat
	 * like <code>/child[@attribute=$attributeValue]</code>
	 * <p>
	 * If multiple children match the criteria, an exception is thrown. If no
	 * child matching the criteria is found, then either <code>null</code> is
	 * returned (if <code>throwExceptionIfNull==false</code>), or an exception
	 * is thrown (otherwise).
	 * 
	 * @param element				the parent element containing the desired 
	 * 								child element
	 * @param child					specifies the name of the child element
	 * @param attribute				specifies the name of the identifying attribute
	 * @param attributeValue		the desired attribute value
	 * @param throwExceptionIfNull	if <code>true</code>, an exception is thrown 
	 * 								if no child matches the criteria
	 * @return						the desired child element, or 
	 * 								<code>null</code> if 
	 * 								<code>throwExceptionIfNull==false</code> and
	 * 								no child element matched the criteria
	 * @throws XmlConfigException	If multiple matches occur, or if no match
	 * 								was found and exception throwing is desired
	 * 								for this case (see <code>throwExceptionIfNull</code>)
	 */
	@SuppressWarnings("unchecked")
	public static Element getChildElementByAttributeValue(Element element, XmlNode child, XmlNode attribute, String attributeValue, boolean throwExceptionIfNull) throws XmlConfigException {
		Element res = null;
		Iterator<Element> it = element.elementIterator(child.getXmlName());
		while (it.hasNext()) {
			Element el = it.next();
			String attValue = el.attributeValue(attribute.getXmlName());
			if (attributeValue.equals(attValue)) {
				if (res == null) res = el;
				else {
					throw new XmlConfigException(
						"multiple matches for " + child.getXmlName() + " children with attribute " + 
						attribute.getXmlName() + "='" + attributeValue + "'", element
					);
				}
			}
		}
		if (res == null && throwExceptionIfNull) {
			throw new XmlConfigException(
				"missing " + child.getXmlName() + " child with attribute " + 
				attribute.getXmlName() + "='" + attributeValue + "'", element
			);
		}
		return res;
	}
	
	/**
	 * Returns a typed iterator for child elements of a certain type
	 * 
	 * @param element 	parent of which certain child elements are desired
	 * @param childType node defining the name of the desired child elements
	 */
	@SuppressWarnings("unchecked")
	public static Iterator<Element> getChildElements(Element element, XmlNode childType) throws XmlConfigException {
		return element.elementIterator(childType.getXmlName());		
	}
	
	/**
	 * Returns the desired single child element, or throws an exception if none
	 * or multiple are found.
	 * 
	 * @param element				the parent of the desired child
	 * @param child					specifies the desired child element name
	 * @return						the child element
	 * @throws XmlConfigException	if no or multiple children match the 
	 * 								criteria
	 */
	public static Element getRequiredSingleChildElement(Element element, XmlNode child) throws XmlConfigException {
		final Element result = getOptionalSingleChildElement(element, child);
		if (result == null) {
			throw new XmlConfigException("missing " + child.getXmlName() + " child element", element);
		}
		return result;
	}
	/**
	 * Returns the desired single child element, or throws an exception if none
	 * or multiple are found.
	 * 
	 * @param element				the parent of the desired child
	 * @param child					specifies the desired child element name
	 * @return						the child element
	 * @throws XmlConfigException	if no or multiple children match the 
	 * 								criteria
	 */
	public static Element getOptionalSingleChildElement(Element element, XmlNode child) throws XmlConfigException {
		@SuppressWarnings("unchecked")
		List<Element> list = element.elements(child.getXmlName());
		if (list.isEmpty()) {
			return null;
		}
		else if (list.size() > 1) {
			throw new XmlConfigException("expected single " + child.getXmlName() + " child, but found " + list.size(), element);
		}
		return list.get(0);
	}

	/**
	 * Returns the desired attribute value, or throws an exception no such
	 * attribute exists.
	 * 
	 * @param element				the element to get the attribute value from
	 * @param attribute				specifies the desired attribute name
	 * @return						the attribute value
	 * @throws XmlConfigException	if no such attribute value is defined
	 */
	public static String getRequiredAttributeValue(Element element, XmlNode attribute) throws XmlConfigException {
		Attribute att = element.attribute(attribute.getXmlName());
		if (att == null) {
			throw new XmlConfigException("missing " + attribute.getXmlName() + " attribute for " + element.getName() + " element", element);
		}
		return att.getValue();
	}
	/**
	 * Returns the desired attribute value, or the given default value if no 
	 * such attribute exists.
	 * 
	 * @param element				the element to get the attribute value from
	 * @param attribute				specifies the desired attribute name
	 * @param defaultValue			the default value to use if no such 
	 * 								attribute exists
	 * @return						the attribute value, or the default if no
	 * 								such attribute exists
	 */
	public static String getOptionalAttributeValue(Element element, XmlNode attribute, String defaultValue) throws XmlConfigException {
		Attribute att = element.attribute(attribute.getXmlName());
		if (att == null) {
			return defaultValue;
		}
		return att.getValue();
	}
	
	/**
	 * Check whether the given element has the desired name. Throws an exception
	 * otherwise.
	 * 
	 * @param element				the element to check
	 * @param expected				specifies the expected element name
	 * @throws XmlConfigException	if the check fails
	 */
	public static void checkExpectedElementName(Element element, XmlNode expected) throws XmlConfigException {
		if (!isExpectedElementName(element, expected)) {
			throw new XmlConfigException(expected.getXmlName() + " element expected, but " + element.getName() + " found", element);
		}
	}
	/**
	 * Returns true if the given element has the desired name.
	 * 
	 * @param element				the element to check
	 * @param expected				specifies the expected element name
	 * @return true if the check is successful
	 */
	public static boolean isExpectedElementName(Element element, XmlNode expected) {
		return expected.getXmlName().equals(element.getName());
	}
	
	/**
	 * Check whether the given element has the desired value for a specific
	 * attribute. Throws an exception otherwise.
	 * 
	 * @param element				the element to check
	 * @param attribute				specifies the attribute name
	 * @param expectedValue			the desired attribute value
	 * @throws XmlConfigException	if the check fails
	 */
	public static void checkExpectedAttributeValue(Element element, XmlNode attribute, String expectedValue) throws XmlConfigException {
		String value = element.attributeValue(attribute.getXmlName());
		if (!expectedValue.equals(value)) {
			throw new XmlConfigException(
				"expected value '" + expectedValue + "' for attribute '" + 
				attribute.getXmlName() + "', but found '" + value + "'",
				element
			);
		}
	}
	
	/**
	 * Returns an xpath like string for the given xml node
	 * 
	 * @param node				the node to convert to a string
	 * @param recurseParents	true if parents should be included
	 * 
	 * @return an xpath like string for the given node
	 */
	public static String getNodePath(Node node, boolean recurseParents) {
		if (node instanceof Element) {
			return getElementPath((Element)node, recurseParents);
		}
		final StringBuilder sb = new StringBuilder(node.getName());
		if (recurseParents && node.getParent() != null) {
			sb.insert(0, node instanceof Attribute ? '@' : '/');
			sb.insert(0, getElementPath(node.getParent(), recurseParents));
		}
		return sb.toString();
	}
	
	/**
	 * Returns an xpath like string for the given xml element
	 * 
	 * @param elem				the element to convert to a string
	 * @param recurseParents	true if parents should be included
	 * 
	 * @return an xpath like string for the given element
	 */
	public static String getElementPath(Element elem, boolean recurseParents) {
		StringBuilder sb = new StringBuilder(elem.getName());
		String name = elem.attributeValue(XmlAttribute.name.getXmlName());
		if (name != null) {
			sb.append("[" + name + "]");
		}
		if (recurseParents && !elem.isRootElement() && elem.getParent() != null) {
			sb.insert(0, '/');
			sb.insert(0, getElementPath(elem.getParent(), recurseParents));
		}
		return sb.toString();
	}
	
	//no instances
	private XmlUtil() {}
}
