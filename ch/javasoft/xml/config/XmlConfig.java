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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * The <tt>XmlConfig</tt> class is a generic xml configuration reader, 
 * supporting features like referred configuration blocks, accessing system
 * property values and program invocation parameters (args of the main method).
 */
public class XmlConfig {
		
	public static final String NO_APP_NAME = "main";
	
	/**
	 * Predefined and recognized xml elements
	 * <p>
	 * In xml, the element names equal the enum constant names.
	 */
	public static enum XmlElement implements XmlNode {
		application, referable, config_list, config, arg, logging, property, usage, line;
		/** The element name in xml, same as enum constant name*/
		public String getXmlName() {return name().replace('_', '-');}
	}
	/**
	 * Predefined and recognized xml attributes.
	 * <p>
	 * In xml, the attribute names equal the enum constant names, only def
	 * is called "default" in xml (default cannot be a constant name in java). 
	 */
	public static enum XmlAttribute implements XmlNode {
		def, name, ref, value;
		/** The attribute name in xml, same as enum constant name, but "default" for def*/
		public String getXmlName() {
			return this == def ? "default" : name();
		}
	}
	/**
	 * Key elements be used in {...} brackets, will be resolved by the 
	 * corresponding content. Can be used to address the working directory,
	 * application name, system properties, command line arguments/options
	 * respectively.
	 * <p>
	 * In xml, the names of the keys equal the enum constant names, but 
	 * replacing _ with -. 
	 */
	public static enum ResolveKey{
		work_dir, app_name, sys_prop, date, time, now, arg, opt, list;
		/** The key name in xml, same as enum constant name, but _ replaced by -*/
		public String xmlName() {
			return name().replaceAll("_", "-");
		}
		public String resolve(Attribute att, String path, String value, XmlConfig config) throws XmlConfigException {
			switch (this) {
				case work_dir:	//{work-dir}
					return new File(".").getAbsolutePath();
				case app_name:	//{app-name}
					return config.mAppName;
				case date:
					return DATE_FORMAT.format(config.mTimestamp);
				case time:
					return TIME_FORMAT.format(config.mTimestamp);
				case now:
					return TIMESTAMP_FORMAT.format(config.mTimestamp);
				case sys_prop:	//{sys-prop:user.home} or {sys-prop:myprop:mydefault}
					String[] parts	= value.split(":");
					if (parts.length < 2) {
						throw new XmlConfigException("property name missing for '" + xmlName() + "' for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}
					String propName	= parts[1];
					String propDef	= parts.length < 3 ? "" : parts[2];
					return System.getProperty(propName, propDef);
				case arg:		//{1} for first argument, {2}, ...
				{
					String defValue = null;
					int defIndex = value.indexOf(':');
					if (defIndex != -1) {
						defValue	= value.substring(defIndex + 1);
						value		= value.substring(0, defIndex);
					}
					int argLen = 0;
					while (argLen < config.mArgs.length) {
						if (config.mArgs[argLen].startsWith("-")) {
							break;
						}
						argLen++;
					}
					int index = Integer.parseInt(value) - 1;//should not fail
					if (index >= 0 && index < argLen) return config.mArgs[index];
					if (defIndex != -1) return defValue;
					throw new XmlArgException(index, "cannot resolve arg[" + (index + 1) + "] for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);
				}
				case opt:		//{-opt[1]} for an option called opt, the first argument after it, {-opt[2]} for the second ... 
				{
					String defValue = null;
					int defIndex = value.indexOf(':');
					if (defIndex != -1) {
						defValue	= value.substring(defIndex + 1);
						value		= value.substring(0, defIndex);
					}
					int sbStart = value.indexOf('[', 1);
					if (sbStart == -1) {
						throw new XmlConfigException("opening [ missing for option argument for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}
					int sbEnd	= value.indexOf(']', sbStart + 1);
					if (sbEnd == -1) {
						throw new XmlConfigException("closing ] missing for option argument for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}					
					String optName 	= value.substring(1, sbStart);
					int optIndex 	= -1;
					for (int i = 0; i < config.mArgs.length; i++) {
						if (config.mArgs[i].equals("-" + optName)) optIndex = i;
					}
					if (optIndex == -1) {
						if (defIndex != -1) return defValue;
						throw new XmlArgException(optName, "no option '" + optName + "' found in argument list for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}
					int optArgIndex = -1;
					try {
						optArgIndex = Integer.parseInt(value.substring(sbStart + 1, sbEnd));
					}
					catch (Exception ex) {}
					if (optArgIndex == -1) {
						throw new XmlConfigException("cannot parse option argument index for option '" + optName + "' for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}
					int index = optIndex + optArgIndex;
					int optLen = optIndex + 1;
					while (optLen < config.mArgs.length) {
						if (config.mArgs[optLen].startsWith("-")) {
							break;
						}
						optLen++;
					}					
					if (index >= 0 && index < optLen) return config.mArgs[index];
					if (defIndex != -1) return defValue;
					throw new XmlArgException(optName, optArgIndex, "cannot resolve arg[" + (index + 1) + "] for option argument '" + optName + "[" + optArgIndex + "]'" + " for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);
				}
				case list:		//{-list[*,*]} for an option called list, returned as comma separated string 
				{
					String defValue = null;
					int defIndex = value.indexOf(':');
					if (defIndex != -1) {
						defValue	= value.substring(defIndex + 1);
						value		= value.substring(0, defIndex);
					}
					int sbStart = value.indexOf('[', 1);
					if (sbStart == -1) {
						throw new XmlConfigException("opening [ missing for list argument for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}
					int sbEnd	= value.indexOf(']', sbStart + 1);
					if (sbEnd == -1) {
						throw new XmlConfigException("closing ] missing for list argument for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);						
					}					
					String lstName 	= value.substring(1, sbStart);
					String lstFill	= value.substring(sbStart + 2, sbEnd - 1);//ommit the leading and tailing *
					int lstStart 	= -1;
					for (int i = 0; i < config.mArgs.length; i++) {
						if (config.mArgs[i].equals("-" + lstName)) lstStart = i;
					}
					int lstEnd = lstStart + 1;
					if (lstStart != -1) {
						for (int i = lstStart + 1; i < config.mArgs.length; i++) {
							if (config.mArgs[i].startsWith("-")) {
								break;
							}
							else {
								lstEnd++;
							}
						}
					}
					if (lstStart + 1 >= lstEnd) {
						if (defIndex != -1) return defValue;
						return "";												
					}
					final StringBuilder sb = new StringBuilder();
					for (int i = lstStart + 1; i < lstEnd; i++) {
						if (sb.length() > 0) sb.append(lstFill);
						sb.append(config.mArgs[i]);
					}
					return sb.toString();
				}
				default:
					//should not happen
					throw new XmlConfigException("cannot resolve '" + xmlName() + "' for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);					
			}
		}
		public boolean matches(String value) {
			if (this == arg || this == opt || this == list) {
				//arg, opt and list can have default values
				//arg: {1:mydefault}
				//opt: {-myopt[1]:mydefault}
				//list: {-mylist[*,*]:mydefault}		list returned as comma separated string
				int ind = value.indexOf(':');
				if (ind != -1) value = value.substring(0, ind);
				if (this == arg) {
					return isInt(value);
				}
				else {
					if (!value.startsWith("-")) return false;
					final int start = value.indexOf('[');
					final int end   = value.indexOf(']', start + 1);
					if (start < 0 || end < 0) {
						return false;
					}
					value = value.substring(start + 1, end);
					if (this == opt) {
						return isInt(value);
					}
					else if (this == list) {
						return value.startsWith("*") && value.endsWith("*") &&
						value.length() >= 2;
					}
					else throw new InternalError();					
				}
			}
			String xmlName = xmlName();
			return this == sys_prop ? value.startsWith(xmlName) : value.equals(xmlName);
		}
	}
	
	private static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		}
		catch (NumberFormatException ex) {
			return false;
		}		
	}
	
	private static final DateFormat DATE_FORMAT 		= new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat TIME_FORMAT 		= new SimpleDateFormat("HH:mm:ss.SSS");
	private static final DateFormat TIMESTAMP_FORMAT	= new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
	
	protected final Document	mDocument;
	protected final String[]	mArgs;
	protected final Date		mTimestamp = new Date();

	private String 	mAppName;
	
	protected XmlConfig(String appName, Document xmlDoc) {
		this(appName, xmlDoc, null);
	}
	protected XmlConfig(String appName, Document xmlDoc, String[] args) {
		mAppName	= appName;
		mDocument	= xmlDoc;
		mArgs		= args == null ? new String[] {} : args;
	}
	
	public static XmlConfig getXmlConfig(File xmlFile) throws DocumentException {
		return getXmlConfig(NO_APP_NAME, xmlFile);
	}
	public static XmlConfig getXmlConfig(Reader xmlFile) throws DocumentException {
		return getXmlConfig(NO_APP_NAME, xmlFile);
	}
	public static XmlConfig getXmlConfig(InputStream xmlFile) throws DocumentException {
		return getXmlConfig(NO_APP_NAME, xmlFile);
	}
	public static XmlConfig getXmlConfig(Document xmlDocument) throws DocumentException {
		return getXmlConfig(NO_APP_NAME, xmlDocument);
	}
	public static XmlConfig getXmlConfig(String appName, File xmlFile) throws DocumentException {
		return getXmlConfig(appName, new SAXReader().read(xmlFile));
	}
	public static XmlConfig getXmlConfig(String appName, Reader xmlFile) throws DocumentException {
		return getXmlConfig(appName, new SAXReader().read(xmlFile));
	}
	public static XmlConfig getXmlConfig(String appName, InputStream xmlFile) throws DocumentException {
		return getXmlConfig(appName, new SAXReader().read(xmlFile));
	}
	public static XmlConfig getXmlConfig(String appName, Document xmlDocument) throws DocumentException {
		return new XmlConfig(appName, xmlDocument);
	}
	public static XmlConfig getXmlConfig(File xmlFile, String[] args) throws DocumentException {
		return getXmlConfig(NO_APP_NAME, xmlFile, args);
	}
	public static XmlConfig getXmlConfig(InputStream xmlFile, String[] args) throws DocumentException {
		return getXmlConfig(NO_APP_NAME, xmlFile, args);
	}
	public static XmlConfig getXmlConfig(String appName, File xmlFile, String[] args) throws DocumentException {
		return new XmlConfig(appName, new SAXReader().read(xmlFile), args);
	}
	public static XmlConfig getXmlConfig(String appName, InputStream xmlFile, String[] args) throws DocumentException {
		return new XmlConfig(appName, new SAXReader().read(xmlFile), args);
	}
//	/**
//	 * Returns the xml config from the system properties. If it is not stored 
//	 * there, an {@link IllegalStateException} is thrown. It can be stored in
//	 * the system properties by calling 
//	 * {@link #putXmlConfigToSystemProperties()}.
//	 * 
//	 * @throws IllegalStateException	if the config has not been stored using
//	 * 									putXmlConfigToSystemProperties()
//	 */
//	public static XmlConfig getXmlConfigFromSystemProperties() {
//		final String xml = System.getProperty(SYSTEM_PROPERTY, null);
//		if (xml == null) {
//			throw new IllegalStateException(
//				"no xml config found in system properties, use putXmlConfigToSystemProperties() before accessing it"
//			);
//		}
//		final Document doc;
//		try {
//			doc = new SAXReader().read(new StringReader(xml));
//			return fromXmlDocument(doc);
//		}
//		catch (Exception ex) {
//			System.out.println(xml);
//			throw new RuntimeException("cannot restore xml config element, e=" + ex, ex);			
//		}
//	}
//	/**
//	 * Stores the current <code>XmlConfig</code> instance in the system 
//	 * properties under the key {@link #SYSTEM_PROPERTY}, which is the fully
//	 * qualified class name of <code>XmlConfig</code>.
//	 * @throws XmlConfigException 
//	 */
//	public void putXmlConfigToSystemProperties() throws XmlConfigException {
//		staticConfig = toXmlDocument();
//	}
	
	/**
	 * Writes this xml config to the given output stream
	 */
	public void writeTo(OutputStream out) {
		final Document doc = toXmlDocument();
		new XmlPrint().print(doc, out);
	}
	/**
	 * Writes this xml config to the given writer
	 */
	public void writeTo(Writer writer) {
		final Document doc = toXmlDocument();
		new XmlPrint().print(doc, writer);
	}
	
	/**
	 * Returns a copy of the underlying xml document, including main arguments 
	 * if any have been specified. From the returned document, 
	 */
	public Document toXmlDocument() {
		final Document doc = (Document)mDocument.clone();
		doc.getRootElement().elements(XmlElement.application.getXmlName()).clear();
		final Element app = doc.getRootElement().addElement(XmlElement.application.getXmlName());
		app.addAttribute(XmlAttribute.name.getXmlName(), mAppName);
		for (String arg : mArgs) {
			final Element elArg = app.addElement(XmlElement.arg.getXmlName());
			elArg.addAttribute(XmlAttribute.value.getXmlName(), arg);
		}
		return doc;
	}
	
    /**
     * Parses the given document and returns an XmlConfig instance. Note that
     * this document is expected to contain an application element (direct child
     * of root element) with a name attribute and possible with arg child nodes.
     * Such a document is usually received by calling {@link #toXmlDocument()}.
     * 
     * @throws DocumentException	if a dom4j xml exception occurs 
     */
    public static XmlConfig fromXmlDocument(File file) throws DocumentException {
        return fromXmlDocument(new SAXReader().read(file));
    }
    /**
     * Parses the given document and returns an XmlConfig instance. Note that
     * this document is expected to contain an application element (direct child
     * of root element) with a name attribute and possible with arg child nodes.
     * Such a document is usually received by calling {@link #toXmlDocument()}.
     * @throws DocumentException	if a dom4j xml exception occurs 
     */
    public static XmlConfig fromXmlDocument(Reader reader) throws DocumentException {
        return fromXmlDocument(new SAXReader().read(reader));
    }
	/**
	 * Parses the given document and returns an XmlConfig instance. Note that
	 * this document is expected to contain an application element (direct child
	 * of root element) with a name attribute and possible with arg child nodes.
	 * Such a document is usually received by calling {@link #toXmlDocument()}.
	 */
	@SuppressWarnings("unchecked")
	public static XmlConfig fromXmlDocument(Document doc) {
		final Element app = doc.getRootElement().element(XmlElement.application.getXmlName());
		final String appName = app.attributeValue(XmlAttribute.name.getXmlName());
		final List<String> args = new ArrayList<String>();
		final Iterator<Element> it = app.elementIterator(XmlElement.arg.getXmlName());
		while (it.hasNext()) {
			args.add(it.next().attributeValue(XmlAttribute.value.getXmlName()));
		}
		doc.remove(app);
		return new XmlConfig(appName, doc, args.toArray(new String[args.size()]));
	}
	
	protected int getArgCount() {
		return mArgs.length;
	}
	protected String getArg(int index) {
		return mArgs[index];
	}
	/**
	 * Returns a copy of the command line argument array
	 */
	public String[] getArgs() {
		final String[] copy = new String[mArgs.length];
		System.arraycopy(mArgs, 0, copy, 0, copy.length);
		return copy;
	}
	
	protected Element getRootElement() {
		return mDocument.getRootElement();
	}
	public String getAppName() {
		return mAppName;
	}
	public void setAppName(String appName) {
		mAppName = appName;
	}
	
	public void setDefaultConfigName(String name) {
		getRootElement().addAttribute(XmlAttribute.def.getXmlName(), name);
	}
	public String getDefaultConfigName() {
		return getRootElement().attributeValue(XmlAttribute.def.getXmlName());
	}
	public Document getDefaultConfigDocument() throws XmlConfigException {
		return getConfigDocument(getDefaultConfigName());
	}
	public Element getDefaultConfigElement() throws XmlConfigException {
		return getConfigElement(getDefaultConfigName());
	}
	public Element getConfigElement(String name) throws XmlConfigException {
		final Document doc = getConfigDocument(name);
		final Element root = doc.getRootElement();
		return XmlUtil.getRequiredSingleChildElement(root, XmlElement.config);		
	}
	@SuppressWarnings("unchecked")
	public Document getConfigDocument(String name) throws XmlConfigException {
		if (name == null) throw new NullPointerException("name argument is required");
		Iterator<Element> it = getRootElement().elementIterator(XmlElement.config.getXmlName());
		while (it.hasNext()) {
			Element cfg		= it.next();
			String cfgName	= cfg.attributeValue(XmlAttribute.name.getXmlName());
			if (name.equals(cfgName)) {
				final Element copy = cfg.createCopy();
				List<Element> resolved = resolve(copy, XmlUtil.getElementPath(copy, false /*recurseParents*/));
				if (resolved.size() != 1) {
					throw new XmlConfigException("resolved config not unique", cfg);
				}
				return createDocument(resolved.get(0), name);
			}
		}
		throw new IllegalArgumentException("no such config: " + name);
	}
	private static Document createDocument(Element configElement, String configName) {
		final Document doc = DocumentHelper.createDocument();
		final Element configList = DocumentHelper.createElement(XmlElement.config_list.getXmlName());
		configElement.addAttribute(XmlAttribute.name.getXmlName(), configName);
		configList.addAttribute(XmlAttribute.def.getXmlName(), configName);				
		configList.add(configElement);
		doc.setRootElement(configList);
		return doc;
	}
	
	protected List<Element> resolve(Iterator<Element> elementIterator, String path) throws XmlConfigException {
		final List<Element> resolved = new ArrayList<Element>();
		while (elementIterator.hasNext()) {
			final Element child = elementIterator.next();
			final String childPath = path == null ?
				XmlUtil.getElementPath(child, true /*recurseParents*/) :
				path + "/" + XmlUtil.getElementPath(child, false /*recurseParents*/);
			resolved.addAll(resolve(child, childPath));
		}
		return resolved;
	}
	@SuppressWarnings("unchecked")
	protected List<Element> resolve(Element element, String path) throws XmlConfigException {
		final List<Element> resolved = new ArrayList<Element>();
		Attribute refAtt = element.attribute(XmlAttribute.ref.getXmlName());
		if (refAtt == null) {
			resolved.add(element);
		}
		else {
			resolveAttributeValue(refAtt, path);
			List<Element> refElCont = getReferredElementContent(refAtt.getValue(), path);
			for (final Element el : refElCont) {
				final Element newEl = el.createCopy();
				element.getParent().add(newEl);
				resolved.addAll(resolve(newEl, XmlUtil.getElementPath(el, true/*recurseParents*/)));
			}
			if (!element.getParent().remove(element)) {
				throw new RuntimeException("internal error: should have been removed");
			}
		}
		
		for (Element elem : resolved) {
			Iterator<Attribute> itA = elem.attributeIterator();
			while (itA.hasNext()) {
				Attribute att = itA.next();
				resolveAttributeValue(att, path);
			}
			
//			resolve(elem.elementIterator(), path);
			Iterator<Element> itE = elem.elementIterator();
			while (itE.hasNext()) {
				Element child = itE.next();
				resolve(child, path + "/" + XmlUtil.getElementPath(child, false /*recurseParents*/));
			}
			if (elem.attribute(XmlAttribute.ref.getXmlName()) != null) {
				throw new RuntimeException("internal error: should have been resolved");
			}
		}
		return resolved;
	}
	
	protected void resolveAttributeValue(Attribute att, String path) throws XmlConfigException {
		int start;
		int end = -1;
				
		String value	= att.getValue();
		String resValue	= "";
		while ((start = value.indexOf("${", end + 1)) != -1) {
			resValue += value.substring(end + 1, start);
			end = value.indexOf('}', start + 2);
			if (end == -1) {
				throw new XmlConfigException("closing } missing for attribute \"" + att.getName() + "=" + value + "\"", path);
			}
			resValue += getResolvedAttributeValue(att, path, value.substring(start + 2, end));
		}
		resValue += value.substring(end + 1, value.length());
		att.setValue(resValue);
	}

	protected String getResolvedAttributeValue(Attribute att, String path, String resolveKey) throws XmlConfigException {
		for (ResolveKey key : ResolveKey.values()) {
			if (key.matches(resolveKey)) {
				return key.resolve(att, path, resolveKey, this);
			}
		}
		throw new XmlConfigException("cannot resolve '" + resolveKey + "' for attribute \"" + att.getName() + "=" + att.getValue() + "\"", path);		
	}

	@SuppressWarnings("unchecked")
	protected List<Element> getReferredElementContent(String name, String path) throws XmlConfigException, MissingReferableException {
		Iterator<Element> it = getRootElement().elementIterator(XmlElement.referable.getXmlName());
		while (it.hasNext()) {
			Element ref = it.next();
			String refName = ref.attributeValue(XmlAttribute.name.getXmlName());
			if (name.equals(refName)) {
				return ref.elements();
			}
		}
		throw new MissingReferableException(name, "cannot find referred element '" + name + "'", path);
	}
		
	/**
	 * Initializes the log manager configuration with logging configuration from
	 * the the {@link #getDefaultConfigDocument() default config}. Calls
	 * {@link LogManager#readConfiguration(InputStream)} with 
	 * properties read from the <tt>logging</tt> element.
	 * <p>
	 * The logging element is expected to look like this:
	 * <pre>
		<logging>
	 		<prop name=".level" 	value="INFO"/>	
	 		<prop name="handlers" 	value="ch.javasoft.util.logging.StandardOutHandler,ch.javasoft.util.logging.StandardErrHandler"/>
	 		...
	 	</logging>
	 * </pre>
	 * 
	 * @throws XmlConfigException	if an xml configuration error occurs
	 */
	public void initLogManagerConfiguration() throws XmlConfigException {
		initLogManagerConfiguration(getDefaultConfigElement());
	}
	/**
	 * Initializes the log manager configuration with logging configuration from
	 * the the specified {@link #getConfigDocument(String) config}. Calls
	 * {@link LogManager#readConfiguration(InputStream)} with 
	 * properties read from the <tt>logging</tt> element.
	 * <p>
	 * The logging element is expected to look like this:
	 * <pre>
		<logging>
	 		<prop name=".level" 	value="INFO"/>	
	 		<prop name="handlers" 	value="ch.javasoft.util.logging.StandardOutHandler,ch.javasoft.util.logging.StandardErrHandler"/>
	 		...
	 	</logging>
	 * </pre>
	 * 
	 * @param configName			the config to use, corresponds to 
	 * 								{@link #getConfigDocument(String)}
	 * @throws XmlConfigException	if an xml configuration error occurs
	 */
	public void initLogManagerConfiguration(String configName) throws XmlConfigException {
		initLogManagerConfiguration(getConfigElement(configName));
	}
	/**
	 * Returns the logging properties read from the <tt>logging</tt> element, or
	 * <code>null</code> if no logging properties found in the xml config.
	 * <p>
	 * The logging element is expected to look like this:
	 * <pre>
		<logging>
	 		<prop name=".level" 	value="INFO"/>	
	 		<prop name="handlers" 	value="ch.javasoft.util.logging.StandardOutHandler,ch.javasoft.util.logging.StandardErrHandler"/>
	 		...
	 	</logging>
	 * </pre>
	 * 
	 * @throws XmlConfigException	if an xml configuration error occurs
	 */
	public Properties getLoggingProperties() throws XmlConfigException {
		return getLoggingProperties(getDefaultConfigElement());
	}
	/**
	 * Returns the logging properties read from the <tt>logging</tt> element, or
	 * <code>null</code> if no logging properties found in the xml config.
	 * <p>
	 * The logging element is expected to look like this:
	 * <pre>
		<logging>
	 		<prop name=".level" 	value="INFO"/>	
	 		<prop name="handlers" 	value="ch.javasoft.util.logging.StandardOutHandler,ch.javasoft.util.logging.StandardErrHandler"/>
	 		...
	 	</logging>
	 * </pre>
	 * 
	 * @param configName			the config to use, corresponds to 
	 * 								{@link #getConfigDocument(String)}
	 * @throws XmlConfigException	if an xml configuration error occurs
	 */
	public Properties getLoggingProperties(String configName) throws XmlConfigException {
		return getLoggingProperties(getConfigElement(configName));
	}
	/**
	 * Returns the logging properties read from the <tt>logging</tt> element, or
	 * <code>null</code> if no logging properties found in the xml config.
	 * <p>
	 * The logging element is expected to look like this:
	 * <pre>
		<logging>
	 		<prop name=".level" 	value="INFO"/>	
	 		<prop name="handlers" 	value="ch.javasoft.util.logging.StandardOutHandler,ch.javasoft.util.logging.StandardErrHandler"/>
	 		...
	 	</logging>
	 * </pre>
	 * 
	 * @param config				the config element to use
	 * @throws XmlConfigException	if an xml configuration error occurs
	 */
	@SuppressWarnings("unchecked")
	private Properties getLoggingProperties(Element config) throws XmlConfigException {
		final Element logging = XmlUtil.getOptionalSingleChildElement(config, XmlElement.logging);
		final List<Element> props = (List<Element>)logging.elements(XmlElement.property.getXmlName());
		
		if (props != null) {
			final Properties loggingProps = new Properties();
			for (final Element property : props) {
				String path = XmlUtil.getElementPath(property, true /*recurseParents*/);
				
				final Attribute attRef = property.attribute(XmlAttribute.ref.getXmlName());
				final Iterable<Element> resolved; 
				if (attRef != null) {
					resolved = resolve(property, path);
				}
				else {
					resolved = Collections.singleton(property);
				}
				for (Element prop : resolved) {
					final Attribute attName 	= prop.attribute(XmlAttribute.name.getXmlName()); 
					final Attribute attValue	= prop.attribute(XmlAttribute.value.getXmlName());
					if (attName == null) {
						throw new XmlConfigException("name attribute missing", path);
					}
	 				resolveAttributeValue(attName, path);
					final String name = XmlUtil.getRequiredAttributeValue(prop, XmlAttribute.name); 
	 				path += "[name=" + name + "]";
	 				resolveAttributeValue(attValue, path);
					final String value	= XmlUtil.getRequiredAttributeValue(prop, XmlAttribute.value); 
					loggingProps.put(name, value);
				}
			}
			return loggingProps;
		}
		return null;
	}
	/**
	 * Initializes the log manager configuration with logging configuration from
	 * the the given element. Calls
	 * {@link LogManager#readConfiguration(InputStream)} with 
	 * properties read from the <tt>logging</tt> child element of 
	 * <tt>config</tt>.
	 * <p>
	 * The logging element is expected to look like this:
	 * <pre>
		<logging>
	 		<prop name=".level" 	value="INFO"/>	
	 		<prop name="handlers" 	value="ch.javasoft.util.logging.StandardOutHandler,ch.javasoft.util.logging.StandardErrHandler"/>
	 		...
	 	</logging>
	 * </pre>
	 * 
	 * @param config				the config element, which contains the
	 * 								logging child element
	 * @throws XmlConfigException	if an xml configuration error occurs
	 */
	private void initLogManagerConfiguration(Element config) throws XmlConfigException {
		final Properties loggingProps = getLoggingProperties(config);
		
		if (loggingProps != null) {
			final ByteArrayOutputStream buf = new ByteArrayOutputStream();
			try {
				final String msg = "logging properties from xml-config: " + XmlUtil.getElementPath(config, true /*recurse parents*/);
				loggingProps.store(buf, msg);
				final ByteArrayInputStream in = new ByteArrayInputStream(buf.toByteArray());
				LogManager.getLogManager().readConfiguration(in);
				Logger.getLogger(getClass().getSimpleName()).info("initialized logging properties from xml-config");
			}
			catch (Exception ex) {
				throw new XmlConfigException("cannot set logging config, e=" + ex, config);
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void printUsage(PrintStream stream, String usageName) throws XmlConfigException {
		Element element = XmlUtil.getChildElementByAttributeValue(getRootElement(), XmlElement.usage, XmlAttribute.name, usageName, true /*throwExceptionIfNull*/);
		printUsageLines(stream, element.elementIterator());
	}
	@SuppressWarnings("unchecked")
	private void printUsageLines(PrintStream stream, Iterator<Element> usageChildIt) throws XmlConfigException {
		while (usageChildIt.hasNext()) {
			Element el = usageChildIt.next();
			if (XmlUtil.isExpectedElementName(el, XmlElement.line)) {
				Element copy = el.createCopy();
				List<Element> res = resolve(copy, XmlUtil.getElementPath(copy, true /*recurseParents*/));
				for (Element r : res) {
					stream.println(r.attributeValue(XmlAttribute.value.getXmlName()));
				}
			}
			else if (XmlUtil.isExpectedElementName(el, XmlElement.usage)) {
				Element parentCopy = el.getParent().createCopy();
				Element copy = el.createCopy();
				parentCopy.add(copy);
				List<Element> res = resolve(copy, XmlUtil.getElementPath(copy, true /*recurseParents*/));
				for (Element r : res) {
					printUsageLines(stream, r.elementIterator());
				}
			}
			
		}
	}
	
}
