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
package ch.javasoft.metabolic.convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.dom4j.Element;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.generate.ConfiguredGenerator;
import ch.javasoft.metabolic.parse.ConfiguredParser;
import ch.javasoft.xml.config.MissingReferableException;
import ch.javasoft.xml.config.XmlArgException;
import ch.javasoft.xml.config.XmlConfig;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlNode;
import ch.javasoft.xml.config.XmlUtil;

/**
 * The <tt>Convert</tt> class encapsulates the common stuff for
 * {@link Convert2Sbml} and {@link Convert2Matlab}.
 */
public class Convert {
	
	public static final String CONFIG_FILE	= "config/convert2sbml.xml";

	public static enum XmlAttribute implements XmlNode {
		name;
		public String getXmlName() {
			return name().replaceAll("_", "-");
		}
	}
	
	private final String mConfigEntry;
	private final String mHelpName;
	private final String mUsageName;
	
	public Convert(String configEntry, String helpName, String usageName) {
		mConfigEntry	= configEntry;
		mHelpName		= helpName;
		mUsageName		= usageName;
	}

	/**
	 * Start, called from main method in subclasses after some basic argument
	 * checks 
	 * 
	 * @param args command line arguments passed to main method
	 */
	protected void start(String[] args) {
		try {
			final InputStream in;
			String config = mConfigEntry;
			if (args.length >= 1 && !args[0].startsWith("-")) {
				in = new FileInputStream(args[0]);
				if (args.length >= 2 && !args[1].startsWith("-")) {
					config = args[1];
				}
			}
			else {
				File file = new File(CONFIG_FILE);
				if (file.canRead()) {
					in = new FileInputStream(CONFIG_FILE);
				}
				else {
					in = Convert.class.getResourceAsStream("/" + CONFIG_FILE); 
				}
				if (in == null) throw new FileNotFoundException("/" + CONFIG_FILE);
			}
			XmlConfig xmlConfig = XmlConfig.getXmlConfig(in, args);
			String appName = getClass().getSimpleName();
			xmlConfig.setAppName(appName);
			if (args.length >= 1 && ("--help".equals(args[0]) || "-?".equals(args[0]) || "?".equals(args[0]))) {
				boolean err = args.length > 1;
				printHelp(err ? System.err : System.out, xmlConfig);
				System.exit(err ? 1 : 0);
			}
			try {
				if (config != null) {
					xmlConfig.setDefaultConfigName(config);
				}
				else {
					config = XmlUtil.getRequiredAttributeValue(xmlConfig.getDefaultConfigElement(), XmlAttribute.name);
				}
				LogPkg.LOGGER.info(appName + " [config: " + config + "]");
				start(xmlConfig);				
			}
			catch (XmlArgException ex) {
				if (ex.isOption()) {
					System.err.println("ERROR:   missing " + ex.getOptionWithIndex() + " option");
				}
				System.err.println("DETAILS: " + ex.getLocalizedMessage());
				printUsage(System.err, xmlConfig);
				System.exit(2);
			}
			catch (MissingReferableException ex) {
				if (("config[" + mConfigEntry + "]/metabolic-parse/parse").equals(ex.getPath())) {
					System.err.println("ERROR:   invalid input kind option '" + ex.getReferable() + "'");
				}
				System.err.println("DETAILS: " + ex.getLocalizedMessage());
				printUsage(System.err, xmlConfig);
				System.exit(2);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(3);
		}
		
	}
	private static void start(XmlConfig config) throws XmlConfigException, IOException {
		Element defConfig = config.getDefaultConfigElement();
		MetabolicNetwork net = ConfiguredParser.parseConfig(defConfig);		
		ConfiguredGenerator.generateConfig(defConfig, net);
	}

	private void printHelp(PrintStream stream, XmlConfig xmlConfig) throws XmlConfigException {
		xmlConfig.printUsage(stream, mHelpName);
	}
	private void printUsage(PrintStream stream, XmlConfig xmlConfig) throws XmlConfigException {
		xmlConfig.printUsage(stream, mUsageName);
	}

}
