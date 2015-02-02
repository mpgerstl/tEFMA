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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.CharacterData;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * The <tt>XmlPrint</tt> class contains methods to print xml nicely, that is
 * whith desired indenting.
 * <p>
 * The current implementation does not support textual content and does not 
 * print comments. A supported xml file should thus contain textual information
 * in the attributes of the elements, and elements themselves should only 
 * contain attributes and nested elements.   
 */
public class XmlPrint {
	
	protected final String mIndention;
	
	/**
	 * Constructor for default indention, that is a single tab per indention
	 * level
	 */
	public XmlPrint() {
		this("\t");
	}
	/**
	 * Constructor for given indention
	 * @param indention the desired indention per level
	 */
	public XmlPrint(String indention) {
		mIndention = indention;
	}

	/**
	 * Print the given document to the standard output
	 * @param doc the xml document
	 */
	public void print(Document doc) {
		print(doc, System.out);
	}
	/**
	 * Print the given document to the given output stream
	 * @param doc 		the xml document
	 * @param out		the output stream to write to
	 */
	public void print(Document doc, OutputStream out) {
		print(doc, new OutputStreamWriter(out));
	}
	/**
	 * Print the given document using the given writer
	 * @param doc 		the xml document
	 * @param writer	the writer to use for the output
	 */
	public void print(Document doc, Writer writer) {
		print(doc, writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer));
	}	
	/**
	 * Print the given document using the given print writer
	 * @param doc 		the xml document
	 * @param writer	the print writer to use for the output
	 */
	public void print(Document doc, PrintWriter writer) {
        printDeclaration(doc, writer);
        print(doc.getRootElement(), writer);
	}	
	
	/**
	 * Print the given element to the standard output
	 * @param elem the xml element
	 */
	public void print(Element elem) {
		print(elem, System.out);
	}
	/**
	 * Print the given element to the given output stream
	 * @param elem 		the xml element
	 * @param out		the output stream to write to
	 */
	public void print(Element elem, OutputStream out) {
		print(elem, new OutputStreamWriter(out));
	}
	/**
	 * Print the given element using the given writer
	 * @param elem the xml element
	 * @param writer	the writer to use for the output
	 */
	public void print(Element elem, Writer writer) {
		print(elem, writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer));
	}
	/**
	 * Print the given element using the given print writer
	 * @param elem the xml element
	 * @param writer	the print writer to use for the output
	 */
	public void print(Element elem, PrintWriter writer) {
		print(elem, "", writer);
	}
	/**
	 * Print the given element using the given print writer and initial 
	 * indention
	 * @param elem		the xml element
	 * @param indention	the initial indention
	 * @param writer	the print writer to use for the output
	 */
	@SuppressWarnings("unchecked")
	public void print(Element elem, String indention, PrintWriter writer) {
		writer.print(indention + "<" + elem.getName());
		
		Iterator<Attribute> itAtt	= elem.attributeIterator();
		Iterator<Element>	itElem	= elem.elementIterator();

		if (elem.hasMixedContent() || (elem.hasContent() && !itElem.hasNext())) {
			Iterator<Node> it = elem.nodeIterator();
			while (it.hasNext()) {
				Node node = it.next();
				if (node instanceof CharacterData) {					
					if (!(node instanceof Comment) && node.getText().trim().length() != 0) {
						throw new IllegalArgumentException("text content not supported: \"" + node.getText() + "\"");						
					}
				}
				else if (!(node instanceof Element || node instanceof Attribute)) {
					throw new IllegalArgumentException("only attributes and elements are supported");					
				}
			}
		}
		while (itAtt.hasNext()) {
			Attribute att = itAtt.next();
			final String attName 	= att.getName();
			final String attValue	= att.getValue();
			writer.print(" " + attName + "=\"" + escapeAttributeValue(attValue) + "\"");
		}
		if (!itElem.hasNext()) {
			writer.println("/>");
		}
		else {
			writer.println(">");
			while (itElem.hasNext()) {
				print(itElem.next(), indention + getIndention(), writer);
			}
			writer.println(indention + "</" + elem.getName() + ">");			
		}
		writer.flush();
	}
	
	/**
	 * Replaces quotes and less than by the xml escaped symbol
	 * 
	 * @param value	the unescaped value
	 * @return the escaped value
	 */
	private static String escapeAttributeValue(String value) {
		return value.replaceAll("\"", "&quot;").replaceAll("<", "&lt;");
	}
	
	/**
	 * Returns the given document as a string
	 * @param doc the xml document
	 */
	public String toString(Document doc) {
		StringWriter sw = new StringWriter();
		print(doc, sw);
		return sw.toString();
	}
	/**
	 * Returns the given element as a string
	 * @param elem the xml element
	 */
	public String toString(Element elem) {
		StringWriter sw = new StringWriter();
		print(elem, sw);
		return sw.toString();
	}
	
	
	/**
	 * Prints the xml declaration (the first line of an xml file)
	 * @param doc		the xml document
	 * @param writer	the writer to use for the output
	 */
	protected void printDeclaration(Document doc, PrintWriter writer) {
        String encoding = doc.getXMLEncoding();

        // Assume 1.0 version
        writer.write("<?xml version=\"1.0\"");
        if (encoding == null || encoding.equals("UTF8")) {
            writer.write(" encoding=\"UTF-8\"");
        } 
        else {
            writer.write(" encoding=\"" + encoding + "\"");
        }
        writer.write("?>");
        writer.println();
	}
	
	/**
	 * Returns the per-level indention. Has been specified when this 
	 * <code>XmlPrint</code> instance was instantiated.
	 */
	protected String getIndention() {
		return mIndention;
	}
	
}
