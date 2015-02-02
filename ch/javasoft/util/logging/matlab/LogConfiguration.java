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
package ch.javasoft.util.logging.matlab;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.LogManager;

import ch.javasoft.util.logging.Loggers;
import ch.javasoft.util.logging.SystemProperties;

/**
 * The <code>LogConfiguration</code> is a modifiable log 
 * configuration as defined by {@link LogManager}, backed by an underlying 
 * {@link Properties} object.
 */
public class LogConfiguration {
	
	private final Properties properties;
	
	/**
	 * Constructor for <code>LogConfiguration</code> using the default
	 * configuration file as defined by 
	 * {@link #getConfigResourceName()} to initialize 
	 * the config values.
	 * 
	 * @throws IOException  if an i/o exception occurs when the configuration 
	 * 						file is read
	 */
	public LogConfiguration() throws IOException {
		this(getConfigProperties());
	}
	/**
	 * Constructor for <code>LogConfiguration</code> using the 
	 * specified properties object as source for the config values. Note that
	 * the properties object is not cloned, thus, changes in the properties are
	 * also reflected in this log configuration instance.
	 */
	public LogConfiguration(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * Returns the configuration value for the specified property
	 * 
	 * @param name	the property's name
	 * @return 	the value of the specified property, or null if no such property
	 * 			exists
	 */
	public String getProperty(String name) {
		return properties.getProperty(name);
	}
	
	/**
	 * Sets the specified configuration property value
	 * 
	 * @param name	the property's name
	 * @param value	the value to set
	 */
	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}
	/**
	 * Removes the specified configuration property value
	 * 
	 * @param name	the property's name
	 */
	public void removeProperty(String name) {
		properties.remove(name);
	}

	/**
	 * Returns the log configuration as input stream, readily usable to 
	 * initialize the log manager configuration by calling 
	 * {@link LogManager#readConfiguration(InputStream)} 
	 */
	public InputStream toInputStream() throws IOException {
		final ByteArrayOutputStream buf = new ByteArrayOutputStream();
		properties.store(buf, "logging config from " + getClass().getName());
		return new ByteArrayInputStream(buf.toByteArray());
	}

	/**
	 * Returns the underlying properties object, not cloned. Changes in the 
	 * returned properties also affect the logging configuration values of this
	 * instance.
	 */
	public Properties toProperties() throws IOException {
		return properties;
	}
	
	/**
	 * Returns the {@link Loggers#getDefaultConfigurationName() default configuration},
	 * if no other {@link SystemProperties#LogManagerPropertiesFile config file}
	 * is specified in the system properties. If a config file is specified, the
	 * system properties value is returned instead. 
	 */
	public static String getConfigResourceName() {
		final String file = SystemProperties.LogManagerPropertiesFile.getSystemProperty();
		if (file == null) {
			return Loggers.getDefaultConfigurationName();
		}
		return file;
	}

	/**
	 * Returns {@link #getConfigResourceName()} as properties object
	 * 
	 * @throws IOException  if an i/o exception occurs when the configuration 
	 * 						file is read
	 */
	private static Properties getConfigProperties() throws IOException {
		final InputStream in = getConfigInputStream();
		final Properties props = new Properties();
		props.load(in);
		return props;
	}
	
	/**
	 * Returns {@link #getConfigResourceName()} as input stream
	 * 
	 * @throws IOException  if an i/o exception occurs when the configuration 
	 * 						file is read
	 */
	private static InputStream getConfigInputStream() throws IOException {
		InputStream in = Loggers.class.getResourceAsStream(getConfigResourceName());
		if (in == null) {
			throw new FileNotFoundException(getConfigResourceName());
		}
		return in;
	}
	

}
