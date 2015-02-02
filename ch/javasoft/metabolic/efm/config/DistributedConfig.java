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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.logging.Loggers;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlUtil;

/**
 * The <code>DistributedConfig</code> is configuration concerning distributed
 * cluster computation.
 */
public class DistributedConfig {
	
	private final int			partition;
	private final long			candidateThreashold;
	private final List<String> 	nodeNames;
	private final List<String> 	vmArgs;
	private final List<String> 	logLevels;

	//	/usr/bin/rsh [hostname] cd [workdir] ; /usr/bin/java [vmargs] -cp [classpath] [class] [args]
	private final String command;

	public DistributedConfig() throws XmlConfigException, IOException {
		this(getDistConfigFromPackage());
////		partition	= 65536;
//		partition	= 256;
////		nodeNames	= Collections.singletonList("localhost");
////		vmArgs		= Collections.singletonList("-Xmx500M");
//		nodeNames	= Arrays.asList("localhost", "localhost");
//		vmArgs		= Arrays.asList("-Xmx500M", "-Xmx500M");
//		command		= "/usr/bin/java [vmargs] -cp [classpath] [class] [args]";
	}
	private static final Element getDistConfigFromPackage() throws XmlConfigException, IOException {
		final String xmlName = "config/metabolic-efm.xml";
		InputStream xmlIn = DistributedConfig.class.getClassLoader().getResourceAsStream(xmlName);
		if (xmlIn == null) {
			throw new IOException("cannot find resource: " + xmlName);
		}
		try {
			final SAXReader reader = new SAXReader();
			final Document doc = reader.read(xmlIn);
			final Element root = doc.getRootElement();
			final Element elDist = root.elementByID(XmlElement.distribute.getXmlName());
//			final String defName = root.attributeValue("default", "default");
//			final Element elConfig 	= XmlUtil.getChildElementByAttributeValue(root, ch.javasoft.xml.config.XmlConfig.XmlElement.config, XmlAttribute.name, defName, true /*throwExceptionIfNull*/);
//			final Element elMetEfm 	= XmlUtil.getRequiredSingleChildElement(elConfig, ch.javasoft.metabolic.efm.main.CalculateFluxModes.XmlElement.metabolic_efm);
//			final Element elImpl   	= XmlUtil.getRequiredSingleChildElement(elMetEfm, ch.javasoft.metabolic.efm.main.CalculateFluxModes.XmlElement.efm_impl);
//			final Element elImplCfg	= XmlUtil.getRequiredSingleChildElement(elImpl, ch.javasoft.metabolic.efm.main.CalculateFluxModes.XmlElement.config);
//			final Element elDist   	= XmlUtil.getRequiredSingleChildElement(elImplCfg, XmlElement.distribute);
			return elDist;
		} 
		catch (DocumentException e) {
			throw ExceptionUtil.toIOException("cannot parse " + xmlName + ", e=" + e, e);
		} 
		finally {
			xmlIn.close();
		}
	}
	/**
	 * Constructor for <code>DistributedConfig</code> with xml configuration.
	 * The configuration element looks like this:
	 * <pre>
			<distribute partition="65536"><!--use power of 4-->
				<nodes vmargs = "-Xmx2500M" level="OFF">
					<node name="node01.cluster.lan" vmargs="-Xmx1G" level="INFO"/>
					<node name="node02.cluster.lan"/>
					<node name="node03.cluster.lan"/>
					<node name="node04.cluster.lan"/>
				</nodes>
				<command value="/usr/bin/rsh [nodename] cd [workdir] ; /usr/bin/java [vmargs] -cp [classpath] [class] [args]"/>
			</distribute>
	 * </pre>
	 * @param elDistribute			the <tt>distribute</tt> xml element
	 * @throws XmlConfigException	if an xml configuration exception occurs,
	 * 								for instance due to invalid xml structure
	 */
	public DistributedConfig(Element elDistribute) throws XmlConfigException {
		final Element elNodes = XmlUtil.getRequiredSingleChildElement(elDistribute, XmlElement.nodes);
		final Element elCommand = XmlUtil.getRequiredSingleChildElement(elDistribute, XmlElement.command);
		final String sPartition = XmlUtil.getRequiredAttributeValue(elDistribute, XmlAttribute.partition);
		final String sThreshold = XmlUtil.getOptionalAttributeValue(elDistribute, XmlAttribute.candidate_threshold, "0");

		final List<String> nodeNames 	= new ArrayList<String>();
		final List<String> vmArgs		= new ArrayList<String>();
		final List<String> logLevels	= new ArrayList<String>();
		
		@SuppressWarnings("unchecked")
		final Iterator<Element> nodeIt = elNodes.elementIterator(XmlElement.node.getXmlName());
		while (nodeIt.hasNext()) {
			final Element elNode = nodeIt.next();
			nodeNames.add(XmlUtil.getRequiredAttributeValue(elNode, XmlAttribute.name));
			//vmargs
			String vmargs = elNode.attributeValue(XmlAttribute.vmargs.getXmlName());
			if (vmargs == null) vmargs = elNodes.attributeValue(XmlAttribute.vmargs.getXmlName());
			vmArgs.add(vmargs);
			//log level
			String logLevel = elNode.attributeValue(XmlAttribute.level.getXmlName());
			if (logLevel == null) logLevel = elNodes.attributeValue(XmlAttribute.level.getXmlName());
			if (logLevel == null) logLevel = Level.INFO.getName();
			logLevels.add(logLevel);
		}
		this.nodeNames	= Collections.unmodifiableList(nodeNames);
		this.vmArgs		= Collections.unmodifiableList(vmArgs);
		this.logLevels	= Collections.unmodifiableList(logLevels);
		this.command 	= XmlUtil.getRequiredAttributeValue(elCommand, XmlAttribute.value);
		try {
			this.partition = Integer.parseInt(sPartition);
		}
		catch (Exception e) {
			throw new XmlConfigException("cannot parse distribute attribute 'partitions': " + sPartition + ", e=" + e, elDistribute, e);
		}
		try {
			this.candidateThreashold = Long.parseLong(sThreshold);
		}
		catch (Exception e) {
			throw new XmlConfigException("cannot parse distribute attribute 'candidate-threshold': " + sThreshold + ", e=" + e, elDistribute, e);
		}
		//check partition, must be power of 4
		int cur = 1;
		while (partition / cur > 1) {
			cur <<= 2;// *= 4
		}
		if (partition != cur) {
			throw new IllegalArgumentException("distributed partition must be a power of 4, but is " + partition);
		}
	}
	
	/**
	 * Returns the host names of the cluster nodes in an unmodifiable list
	 */
	public List<String> getNodeNames() {
		return nodeNames;
	}

	/**
	 * Returns the vmargs for each node in an unmodifiable list. The list has
	 * as many elements as there are nodes, the elements might be null
	 */
	public List<String> getVmArgs() {
		return vmArgs;
	}

	/**
	 * Returns the log levels for each node in an unmodifiable list. The list
	 * has as many elements as there are nodes. The elements are never null,
	 * default log level INFO is set if no levels are configured.
	 */
	public List<String> getLogLevels() {
		return logLevels;
	}

	/**
	 * Returns the command to execute on a node
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * Returns the number of partitions to distribute to the nodes.  
	 */
	public int getPartition() {
		return partition;
	}
	
	/**
	 * Returns the minimum number of adjacency candidates to invoke distributed
	 * computation. Below this threshold, standard sequential computation is
	 * used.
	 */
	public long getCandidateThreashold() {
		return candidateThreashold;
	}

	/**
	 * Write all configuration settings to the log using the given log level
	 */
	public void log(Logger logger, Level level) {
		if (Loggers.isLoggable(logger, level)) {
			logger.log(level, "Distributed Config:");
			logger.log(level, "..node count       : " + getNodeNames().size());
			logger.log(level, "..nodes            : " + getNodeNames());
			logger.log(level, "..vmargs           : " + getVmArgs());
			logger.log(level, "..command          : " + getCommand());
			logger.log(level, "..partition        : " + getPartition());
			logger.log(level, "..cand. threshold  : " + getCandidateThreashold());
		}
	}
	
}
