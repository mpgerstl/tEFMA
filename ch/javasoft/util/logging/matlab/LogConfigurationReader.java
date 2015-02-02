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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import ch.javasoft.util.logging.Loggers;

/**
 * The <code>LogConfigurationReader</code> is a class initializing the logging
 * configuration manually. It reads the logging configuration from the config 
 * file as defined by {@link LogConfiguration#getConfigResourceName()}. 
 * The configuration is loaded into the {@link LogManager} using
 * {@link LogManager#readConfiguration(java.io.InputStream)}. However, 
 * {@link Handler handlers}, {@link Formatter formatters} and 
 * {@link Filter filters} are installed manually, that is, 
 * the configuration is scanned and the respective classes are instantiated and
 * initialized. This is actually a workaround for situations where the 
 * class loader associated with the {@link LogManager} does not see custom 
 * logging classes (like within matlab).
 */
public class LogConfigurationReader {
	
	private final LogConfiguration logConfiguration;
	
	/**
	 * Constructor for <code>LogConfigurationReader</code> using the default
	 * configuration file as defined by 
	 * {@link LogConfiguration#getConfigResourceName()}.
	 * 
	 * @throws IOException  if an i/o exception occurs when the configuration 
	 * 						file is read
	 */
	public LogConfigurationReader() throws IOException {
		this(new LogConfiguration());
	}
	
	/**
	 * Constructor for <code>LogConfigurationReader</code> using the specified
	 * log configuration.
	 * 
	 * @param logConfiguration	the log configuration to use
	 * 
	 * @throws IOException  if an i/o exception occurs when the configuration 
	 * 						file is read
	 */
	public LogConfigurationReader(LogConfiguration logConfiguration) throws IOException {
		this.logConfiguration = logConfiguration;
		initializeLogManagerConfiguration();
	}
	
	/**
	 * Initialized the log logger {@link Handler handlers}, 
	 * {@link Formatter formatters} and {@link Filter filters} manually, as 
	 * specified in {@link LogConfigurationReader this class comments}.
	 * 
	 * @throws IOException  if an i/o exception occurs when the configuration 
	 * 						file is read
	 */
	private void initializeLogManagerConfiguration() throws SecurityException, IOException {
		final Map<String, String> handlerMap = new LinkedHashMap<String, String>();
		final Properties props = logConfiguration.toProperties();
		final Iterator keyIt = props.keySet().iterator();
		while (keyIt.hasNext()) {
			final Object key = keyIt.next();
			final String pname = key == null ? "" : key.toString();
			if (pname.equals("handlers") || pname.endsWith(".handlers")) {
				final Object value = props.get(pname);
				if (value != null) {
					handlerMap.put(pname, value.toString());
					keyIt.remove();
				}
			}
		}
		Loggers.initLogManagerConfiguration(props);
		for (final Map.Entry<String, String> handlerConfig : handlerMap.entrySet()) {
			final String[] handlers = handlerConfig.getValue().split("[\\s,;:]+");
			final Logger logger;
			if (handlerConfig.getKey().equals("handlers")) {
				logger = Loggers.getRootLogger();				
			}
			else {
				final String key = handlerConfig.getKey();
				logger = Logger.getLogger(key.substring(0, key.length() - ".handlers".length()));
			}
			Loggers.removeAllHandlers(logger, false);
			for (final String handler : handlers) {
				try {
					final Handler lh = instantiate(Handler.class, handler); 
					initHandlerFormatter(logger, lh);
					initFilters(logger, lh);
					logger.addHandler(lh);
//					System.out.println("LOG: added log handler: " + logger + " / " + lh);
				}
				catch (Exception ex) {
					System.err.println("ERROR: could not install log handler, e=" + ex);
					ex.printStackTrace();
				}
			}
		}
	}
	
	private String getInheritedLoggerProperty(Logger logger, String suffix) {
		final String propName;
		if (logger.getName() == null) {
			propName = suffix;
		}
		else {
			propName = logger.getName() + (logger.getName().length() > 0 ? "." : "") + suffix;
		}
		final String value = logConfiguration.getProperty(propName);
		if (value == null && logger.getParent() != null) {
			return getInheritedLoggerProperty(logger.getParent(), suffix);
		}
		return value;
	}
	private void initHandlerFormatter(Logger logger, Handler handler) {
		final String hformatter = logConfiguration.getProperty(handler.getClass().getName() + ".formatter");		
		if (hformatter != null) {
			final Formatter formatter = instantiate(Formatter.class, hformatter);
			handler.setFormatter(formatter);
//			System.out.println("LOG: added handler formatter: " + logger + " / " + formatter);
		}
	}
	private void initFilters(Logger logger, Handler handler) {
		final String lfilter = getInheritedLoggerProperty(logger, "filter");
		if (lfilter != null) {
			final Filter filter = instantiate(Filter.class, lfilter);
			logger.setFilter(filter);
//			System.out.println("LOG: added logger filter: " + logger + " / " + filter);
		}
		final String hfilter = logConfiguration.getProperty(handler.getClass().getName() + ".filter");
		if (hfilter != null) {
			final Filter filter = instantiate(Filter.class, hfilter);
			handler.setFilter(filter);
//			System.out.println("LOG: added handler filter: " + logger + " / " + filter);
		}
	}
	@SuppressWarnings("unchecked")
	private <T> T instantiate(Class<T> clazz, String className) {
		try {
			final Class cls = getClass().getClassLoader().loadClass(className);
			if (clazz.isAssignableFrom(cls)) {
				return (T)cls.newInstance();
			}
			throw new ClassCastException(cls.getName() + " not subclass of " + clazz.getName());
		}
		catch (ClassCastException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException("cannot instantiate " + clazz.getName() + ", e=" + ex, ex);
		}
	}
	
}
