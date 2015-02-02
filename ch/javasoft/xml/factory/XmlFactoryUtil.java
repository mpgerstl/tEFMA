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
package ch.javasoft.xml.factory;

import org.dom4j.Element;

import ch.javasoft.factory.ConfigException;
import ch.javasoft.factory.FactoryNotFoundException;
import ch.javasoft.factory.FactoryUtil;
import ch.javasoft.factory.IllegalFactoryException;
import ch.javasoft.xml.config.XmlUtil;

/**
 * Similar to {@link FactoryUtil}, this class contains static helper methods 
 * used with {@link XmlConfiguredFactory}.
 */
public class XmlFactoryUtil {
	
	/**
	 * The factory class name is taken from the xml configuration using the 
	 * specified attribute name. The factory is instantiated and an object 
	 * created and returned, again using the xml configuration.
	 *  
	 * @param <T>					type of objects produced by the factory 
	 * @param clazz					class of objects produced by the factory
	 * @param config				the element containing configuration 
	 * 								settings for the object to create
	 * @param factoryClassAttribute	name of the xml attribute containing the 
	 * 								class name of the factory
	 * @throws ConfigException 
	 * @throws IllegalFactoryException 
	 * @throws FactoryNotFoundException 
	 * 
	 * @throws ConfigException 			if the factory class attribute was not
	 * 									found in {@code config}, or if the 
	 * 									config content was not as expected
	 * @throws FactoryNotFoundException	if the factory class was not found
	 * @throws IllegalFactoryException	if the factory class was not a factory,
	 * 									if the factory could not be instantiated,
	 * 									if the config was not of the expected 
	 * 									type for this factory, or if the object 
	 * 									created by the factory was not 
	 * 									compatible with the type specified by 
	 * 									{@code clazz} 
	 */
	public static <T, F extends XmlConfiguredFactory<T>> T create(Class<T> clazz, Element config, String factoryClassAttribute) throws FactoryNotFoundException, IllegalFactoryException, ConfigException {
		final String factoryClassName = config.attributeValue(factoryClassAttribute);
		if (factoryClassName == null) {
			throw new ConfigException("attribute with factory class not found: " + factoryClassAttribute, XmlUtil.getElementPath(config, true));
		}
		return FactoryUtil.create(clazz, factoryClassName, config);
	}

	//no instances
	private XmlFactoryUtil() {
		super();
	}
}
