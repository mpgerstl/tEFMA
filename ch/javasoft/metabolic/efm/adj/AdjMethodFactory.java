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
package ch.javasoft.metabolic.efm.adj;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;

import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.config.XmlAttribute;
import ch.javasoft.metabolic.efm.config.XmlElement;
import ch.javasoft.metabolic.efm.dist.DistributedAdjEnum;
import ch.javasoft.xml.config.XmlConfig;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlUtil;

/**
 * The <code>AdjMethodFactory</code> reads implementors for adjacency methods
 * from the xml config file and configures the factories accordingly. If some
 * factory classes cannot be loaded, an error is reported in the log file, but
 * loading is continued.
 */
public class AdjMethodFactory {
	
	private final Config config;
	
	public AdjMethodFactory(Config config) throws XmlConfigException, IOException {
		this.config = config;
		init(config.getXmlConfig());
	}
	
	public static final String DEFAULT_ADJ_METHOD = PatternTreeMinZerosAdjacencyEnumerator.NAME;
	
	private final Map<String, AdjEnum>				adjacencyEnums	= new HashMap<String, AdjEnum>();
	private final Map<String, DistributedAdjEnum>	distAdjEnums	= new HashMap<String, DistributedAdjEnum>();

	public AdjEnum createAdjEnumFromConfig() {
		return createAdjEnum(config.getAdjMethod());
	}
	public AdjEnum createAdjEnum(String adjMethod) {
		int index = adjMethod.indexOf(':');
		String name = index == -1 ? adjMethod : adjMethod.substring(0, index);
		AdjEnum enu = adjacencyEnums.get(name);
		if (enu == null) throw new RuntimeException("adjacency enumerator '" + name + "' not found");
		return enu;
	}
	public DistributedAdjEnum createDistributedAdjEnumFromConfig() {
		return createDistributedAdjEnum(config.getAdjMethod());
	}
	public DistributedAdjEnum createDistributedAdjEnum(String adjMethod) {
		String name = adjMethod.substring(adjMethod.indexOf(':') + 1);
		DistributedAdjEnum enu = distAdjEnums.get(name);
		if (enu == null) throw new RuntimeException("distributed out core adjacency enumerator '" + name + "' not found");
		return enu;
	}
	
	private void addAdjEnum(AdjEnum enu) {
		adjacencyEnums.put(enu.name(), enu);
	}
	private void addDistributedAdjEnum(DistributedAdjEnum enu) {
		distAdjEnums.put(enu.name(), enu);
	}
	
	private void init(XmlConfig xmlConfig) throws XmlConfigException {
		final Element impConfig = Config.getConfigEfmImplConfig(xmlConfig);
		final Element adjConfig = XmlUtil.getRequiredSingleChildElement(impConfig, XmlElement.adjacency_method);
		final Element facConfig = XmlUtil.getRequiredSingleChildElement(adjConfig, XmlElement.factories);
		final Iterator<Element> it = XmlUtil.getChildElements(facConfig, XmlElement.clazz);
		while (it.hasNext()) {
			final Element el = it.next();
			final String className = XmlUtil.getRequiredAttributeValue(el, XmlAttribute.name);
			
			try {
				final Class clazz = Class.forName(className);
				final Object instance = clazz.newInstance();
				int cnt = 0;
				if (instance instanceof AdjEnum) {
					addAdjEnum((AdjEnum)instance);
					cnt++;
					LogPkg.LOGGER.finer("registered adjacency-method: " + className);
				}
				if (instance instanceof DistributedAdjEnum) {
					addDistributedAdjEnum((DistributedAdjEnum)instance);
					cnt++;
					LogPkg.LOGGER.finer("registered distributed adjacency-method: " + className);
				}
				if (cnt == 0) {
					LogPkg.LOGGER.warning("not a recognized adjacency-method: " + className);
				}
			}
			catch (Exception ex) {
				LogPkg.LOGGER.warning("cannot instantiate adjacency-method '" + className + "', e=" + ex);
			}
			
		}
		
		// incore enums
//		addAdjEnum(new FastRankTestAdjacencyEnumerator(false /*isMinCardinalityTested*/));
//		addAdjEnum(new ModRankTestAdjacencyEnumerator(false /*isMinCardinalityTested*/));
//		addAdjEnum(new SvdRankTestAdjacencyEnumerator(false /*isMinCardinalityTested*/));
//		addAdjEnum(new NewRankTestAdjacencyEnumerator(false /*isMinCardinalityTested*/));
//		addAdjEnum(new DefaultRankTestAdjacencyEnumerator(false /*isMinCardinalityTested*/));
//		addAdjEnum(new PatternTreeMinZerosAdjacencyEnumerator());
//		addAdjEnum(new PatternTreeRankAdjacencyEnumerator());
//        addAdjEnum(new PatternTreeLogLogAdjacencyEnumerator());
//		addAdjEnum(new PatternTreeModRankAdjacencyEnumerator());
//		addAdjEnum(new DoublePatternTreeRankUpdateAdjacencyEnumerator());
//		addAdjEnum(new Double2PatternTreeRankUpdateAdjacencyEnumerator());
//		addAdjEnum(new FractionalPatternTreeRankUpdateAdjacencyEnumerator());
//		addAdjEnum(new Fractional2PatternTreeRankUpdateAdjacencyEnumerator());
//		addAdjEnum(new ModPrimePatternTreeRankUpdateAdjacencyEnumerator());
//		addAdjEnum(new ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator());
//		// generic enums
//		addAdjEnum(new MultiProcessedAdjEnum());
//		addAdjEnum(new MultiThreadedAdjEnum());
//		addAdjEnum(new ModIntPrimeOutCoreAdjEnum());
//		addAdjEnum(new ModIntPrimeInCoreAdjEnum());
//		addDistributedAdjEnum(new ModIntPrimeOutCoreAdjEnum());
//		// impls
////		addAdjacencyEnumeratorImpl(new NativeAdjacencyEnumeratorImpl(NativeAdjacencyEnumeratorImpl.NAME_ARR_ALL));
//		// subset-supports below in another static section
	}
	
}
