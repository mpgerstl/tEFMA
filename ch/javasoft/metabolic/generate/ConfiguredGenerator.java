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
package ch.javasoft.metabolic.generate;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.dom4j.Element;

import ch.javasoft.io.Print;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.xml.config.StreamConfigParser;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlNode;
import ch.javasoft.xml.config.XmlUtil;

/**
 * The <tt>ConfiguredGenerator</tt> reads from the xml config what to generate
 * (which format, which output sources and options).
 * 
 * Snipplet from such a config file:
 * <pre>
		<generate type="sbml">
			<output type="file">
				<file name="{-out[1]}"/>
			</output>
			<model name="{-name[1]}"/>
		</generate>
		<generate type="matlab">
			<output type="file">
				<file name="{-out[1]}"/>
			</output>
		</generate>
 * </pre>
 */
public class ConfiguredGenerator {
	public static enum XmlElements implements XmlNode {
		metabolic_generate, generate, output, model, struct;
		public String getXmlName() {
			return name().replaceAll("_", "-");
		}
	}
	public static enum XmlAttributes implements XmlNode {
		type, name;
		public String getXmlName() {
			return name().replaceAll("_", "-");
		}
	}
	public static enum GenerateType {
		sbml, matlab;
		String getXmlName() {
			return name().replaceAll("_", "-");
		}
		static GenerateType find(String type) {
			for (GenerateType gType : values()) {
				if (gType.getXmlName().equals(type)) return gType;
			}
			return null;
		}
	}
	/**
	 * @param parentElement			the element which contains the metabolic_parse child
	 * @throws XmlConfigException	if an xml configuration exception occurs,
	 * 								for instance due to invalid xml structure
     * @throws IOException			if an i/o exception occurs, for instance 
     * 								caused by file access
	 */
	public static void generateConfig(Element parentElement, MetabolicNetwork net) throws XmlConfigException, IOException {
		Element parseElement = XmlUtil.getRequiredSingleChildElement(parentElement, XmlElements.metabolic_generate);
		generate(parseElement, net);
	}
	public static void generate(Element metabolicParseElement, MetabolicNetwork net) throws XmlConfigException, IOException {
		XmlUtil.checkExpectedElementName(metabolicParseElement, XmlElements.metabolic_generate);
		Element parseElement = XmlUtil.getRequiredSingleChildElement(metabolicParseElement, XmlElements.generate);
		String type = parseElement.attributeValue(XmlAttributes.type.getXmlName());
		GenerateType gType = GenerateType.find(type);
		if (gType == null) {
			throw new XmlConfigException("unknown generate type '" + type + "'", parseElement);
		}
		switch (gType) {
			case sbml:
				generateSbml(parseElement, net);
				return;
			case matlab:
				generateMatlab(parseElement, net);
				return;
			default:
				//should not happen
				throw new XmlConfigException("internal error: unknown parse type " + gType, parseElement);
		}
	}

	
	/**
	 * Generates sbml configured like this:
	 * 
	 * <pre>
		<generate type="sbml">
			<output type="file">
				<file name="{-out[1]}"/>
			</output>
			<model name="{-name[1]}"/>
		</generate>
	 * </pre>
	 * @throws XmlConfigException 
	 * @throws IOException 
	 */
	private static void generateSbml(Element parseElement, MetabolicNetwork net) throws XmlConfigException, IOException {
		Element elOutput	= XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.output);
		Element elModel		= XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.model);
		OutputStream out 	= StreamConfigParser.parseOutputStream(elOutput);
		String modelName	= XmlUtil.getRequiredAttributeValue(elModel, XmlAttributes.name);
		new SbmlGenerator(net, modelName).write(out);
	}

	/**
	 * Generates matlab configured like this:
	 * 
	 * <pre>
		<generate type="matlab">
			<output type="file">
				<file name="{-out[1]}"/>
			</output>
			<struct name="metanet"/>
		</generate>
	 * </pre>
	 * 
	 * @throws XmlConfigException 
	 * @throws IOException 
	 */
	private static void generateMatlab(Element parseElement, MetabolicNetwork net) throws XmlConfigException, IOException {
		Element elOutput	= XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.output);
		Element elStruct	= XmlUtil.getRequiredSingleChildElement(parseElement, XmlElements.struct);
		OutputStream out 	= StreamConfigParser.parseOutputStream(elOutput);
		String structName	= XmlUtil.getRequiredAttributeValue(elStruct, XmlAttributes.name);
		PrintWriter pw		= Print.createWriter(out);
		new MatlabGenerator(structName).writeAll(net, pw);
		pw.flush();
		pw.close();
	}
	
}
