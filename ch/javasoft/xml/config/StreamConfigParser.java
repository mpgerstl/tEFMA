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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Element;

/**
 * Parses stream elements (or other enclosing elements with a type attribute)
 * of the following types:
 * <pre>
		<stream type="console">
			<console type="err"/>
		</stream>		
		<stream type="console">
			<console type="out"/>
		</stream>		
		<stream type="file">
			<file name="{work-dir}/{-log[2]}"/>
		</stream>		
		<stream type="logger">
			<logger name="" level="INFO"/>
		</stream>
 * </pre>
 */
public class StreamConfigParser {
	
	private static final String LOG_PRINT_STREAM_CLASSNAME = "ch.javasoft.util.logging.LogPrintStream";

	public static enum XmlElement implements XmlNode {
		stream, console, file, url, logger;
		public String getXmlName() {
			return name();			
		}
	}
	public static enum XmlAttribute implements XmlNode {
		name, type, value, level; 
		public String getXmlName() {return name();}		
	}
	public static enum XmlOutputStreamType {
		file, console, logger;
		public String xmlName() {
			return name();			
		}
		public static XmlOutputStreamType find(String typeAttributeValue) {
			for (XmlOutputStreamType type : values()) {
				if (type.xmlName().equals(typeAttributeValue)) return type;
			}
			return null;
		}
	}
	public static enum XmlInputStreamType {
		file, url;
		public String xmlName() {
			return name();			
		}
		public static XmlInputStreamType find(String typeAttributeValue) {
			for (XmlInputStreamType type : values()) {
				if (type.xmlName().equals(typeAttributeValue)) return type;
			}
			return null;
		}
	}
	public static enum ConsoleType {
		out, err;
		public static ConsoleType find(String typeAttributeValue) {
			for (ConsoleType type : ConsoleType.values()) {
				if (type.name().equalsIgnoreCase(typeAttributeValue)) return type;
			}
			return null;			
		}
		public PrintStream getStream() {
			return this == err ? System.err : System.out;
		}
	}
	
	/**
	 * Parses an output stream of any type, see class comments  
	 * (stream element can be named differently).
	 */
	public static OutputStream parseOutputStream(Element streamElement) throws XmlConfigException {
		String typeName	= streamElement.attributeValue(XmlAttribute.type.getXmlName());
		XmlOutputStreamType type = XmlOutputStreamType.find(typeName);
		if (type == null) {
			throw new XmlConfigException("unknown stream type '" + typeName + "'", streamElement);
		}
		switch(type) {
			case file:
				return parseFileOutputStream(streamElement);
			case console:
				return parseConsolePrintStream(streamElement);
			case logger:
				return parseLoggerStream(streamElement);
				
			default:
				//should not happen
				throw new XmlConfigException("internal error, unknown stream type " + type, streamElement);
		}		
	}
	
	/**
	 * Parses (stream element can be named differently):
	 * <pre>
		<stream type="file">
			<file name="{work-dir}/{-out[2]}"/>					
		</stream>
	 * </pre>
	 */
	public static FileOutputStream parseFileOutputStream(Element streamElement) throws XmlConfigException {
		Element elFile = XmlUtil.getRequiredSingleChildElement(streamElement, XmlElement.file);
		File file = FileConfigParser.parseFile(elFile);
		try {
			return new FileOutputStream(file);
		}
		catch (IOException ex) {			
			throw new XmlConfigException("cannot open file output stream for file '" + file.getAbsolutePath() + "'", streamElement, ex);
		}
	}
	/**
	 * Parses (stream element can be named differently):
	 * <pre>
		<stream type="console">
			<console type="out"/>
		</stream>		
	 * </pre>
	 */
	public static PrintStream parseConsolePrintStream(Element streamElement) throws XmlConfigException {
		Element elConsole = XmlUtil.getRequiredSingleChildElement(streamElement, XmlElement.console);
		String consoleName 	= elConsole.attributeValue(XmlAttribute.type.getXmlName());
		ConsoleType type	= ConsoleType.find(consoleName);
		if (type == null) {
			throw new XmlConfigException("Unknown console type '" + consoleName + "' for " + XmlElement.console.getXmlName() + " element", streamElement);
		}
		return type.getStream();
	}
	
	/**
	 * Parses (stream element can be named differently):
	 * <pre>
		<stream type="logger">
			<logger name="" level="INFO"/>
		</stream>
	 * </pre>
	 */
	public static PrintStream parseLoggerStream(Element streamElement) throws XmlConfigException {
		Element elLogLevel 	= XmlUtil.getRequiredSingleChildElement(streamElement, XmlElement.logger);
		String loggerName	= elLogLevel.attributeValue(XmlAttribute.name.getXmlName());
		String levelName	= elLogLevel.attributeValue(XmlAttribute.level.getXmlName());
		Level logLevel;
		try {
			logLevel = Level.parse(levelName);
		}
		catch (Exception ex) {
			throw new XmlConfigException("invalid log level: " + levelName, elLogLevel);
		}
		Logger logger = Logger.getLogger(loggerName == null ? "" : loggerName);
		try {
			Class<?> streamClass = Class.forName(LOG_PRINT_STREAM_CLASSNAME);
			Constructor cons = streamClass.getConstructor(new Class[] {Logger.class, Level.class});
			return (PrintStream)cons.newInstance(new Object[] {logger, logLevel});
		}
		catch(Exception ex) {
			throw new XmlConfigException("cannot instantiate log print stream, e=" + ex, elLogLevel);
		}
	}
	
	/**
	 * Parses (stream element can be named differently):
	 * <pre>
		<stream type="file">
			<file name="{work-dir}/{-out[2]}"/>					
		</stream>
		<stream type="url">
			<url name="http://..."/>					
		</stream>
		<input type="file">
			<file name="{work-dir}/{-out[2]}"/>					
		</input>
	 * </pre>
	 */
	public static InputStream parseInputStream(Element streamElement) throws XmlConfigException {
		String typeName	= streamElement.attributeValue(XmlAttribute.type.getXmlName());
		XmlInputStreamType type = XmlInputStreamType.find(typeName);
		if (type == null) {
			throw new XmlConfigException("unknown stream type '" + typeName + "'", streamElement);
		}
		switch(type) {
			case file:
				return parseFileInputStream(streamElement);
			case url:
				return parseURLInputStream(streamElement);
			default:
				//should not happen
				throw new XmlConfigException("internal error, unknown stream type " + type, streamElement);
		}
	}
	/**
	 * Parses (stream element can be named differently):
	 * <pre>
		<stream type="file">
			<file name="{work-dir}/{-out[2]}"/>					
		</stream>
	 * </pre>
	 */
	public static FileInputStream parseFileInputStream(Element streamElement) throws XmlConfigException {
		Element elFile = XmlUtil.getRequiredSingleChildElement(streamElement, XmlElement.file);
		File file = FileConfigParser.parseFile(elFile);
		try {
			return new FileInputStream(file);
		}
		catch (IOException ex) {			
			throw new XmlConfigException("cannot open file input stream for file '" + file.getAbsolutePath() + "'", streamElement, ex);
		}
	}

	/**
	 * Parses (stream element can be named differently):
	 * <pre>
		<stream type="url">
			<url name="{work-dir}/{-out[2]}"/>					
		</stream>
	 * </pre>
	 */
	public static InputStream parseURLInputStream(Element streamElement) throws XmlConfigException {
		Element elURL = XmlUtil.getRequiredSingleChildElement(streamElement, XmlElement.url);
		URL url = URLConfigParser.parseURL(elURL);
		try {
			return url.openStream();
		}
		catch (IOException ex) {
			throw new XmlConfigException("cannot open URL input stream url '" + url.toExternalForm() + "', e=" + ex, streamElement, ex);		
		}
	}
	
	// no instances
	private StreamConfigParser() {}
}
