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
package ch.javasoft.util.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Loggers is a class with some static convenience methods to use java logging.
 * <p>
 * Log configuration is read from a properties file, 
 * "ch/javasoft/logging/Loggers.properties" by default if no other files or 
 * config classes are registered (see {@link LogManager} for details).
 */
public class Loggers {
	
	static {
		initLogManagerConfiguration();
	}
	
	/////////////////////////////////////////////////////
	// some direct static methods, for convenience only
	
	/**
	 * Returns the root logger, which is at the root of the parent hierarchy
	 * of all group loggers. It contains the default handlers for other 
	 * loggers, if no group specific logging is desired.
	 * <p>
	 * This is the same as calling {@link Logger#getLogger(String)} with
	 * an empty string as logger name. This is a convenience method, which
	 * also ensures that configuration stuff is read before returning the
	 * root logger.
	 */
	public static Logger getRootLogger() {
		return Logger.getLogger("");
	}
	
	/**
	 * Creates a new logger for the given package.
	 * 
	 * @param pkg		the package
	 * @param parts		use 0 for whole package name, positive values for package parts from start (left), 
	 * 					negative values for package parts from end (right)
	 * @return the desired logger instance
	 * 
	 * @deprecated Does not work with MATLAB 7.1.0.246 (R14) Service Pack 3
	 */
	public static Logger getLoggerForPackage(Package pkg, int parts) {
		return getLogger(pkg.getName(), parts);
	}
	/**
	 * Creates a new logger for the given class.
	 * 
	 * @param clazz		the class
	 * @param parts		use 0 for whole package name, positive values for package parts from start (left), 
	 * 					negative values for package parts from end (right)
	 * @return the desired logger instance
	 */
	public static Logger getLogger(Class clazz, int parts) {
		//NOTE: this seems not to work with MATLAB 7.1.0.246 (R14) Service Pack 3
//		return getImpl().createLogger(clazz.getPackage(), parts);
		
		final String pkgName;
		final int lastDot = clazz.getName().lastIndexOf('.');
		pkgName = lastDot < 0 ? clazz.getName() : clazz.getName().substring(0, lastDot);
		return getLogger(pkgName, parts);
	}
	/**
	 * Creates a new logger for the given package.
	 * 
	 * @param name		the package (or logger) name
	 * @param parts		use 0 for whole package name, positive values for package parts from start (left), 
	 * 					negative values for package parts from end (right)
	 * @return the desired logger instance
	 */
	public static Logger getLogger(String name, int parts) {
		if (parts != 0) {
			int absPackParts = Math.abs(parts);
			String[] packs = name.split("\\.");
			if (packs.length > absPackParts) {
				StringBuffer sb = new StringBuffer();
				for (int ii = 0; ii < absPackParts; ii++) {
					if (sb.length() > 0) sb.append('.');
					sb.append(packs[parts > 0 ? ii : packs.length - absPackParts + ii]);
				}
				name = sb.toString();
			}
		}
		return Logger.getLogger(name);
	}
	/**
	 * Creates a group logger for the group given by name. 
	 * 
	 * @param groupName the name of the log group
	 * @return the desired logger instance
	 */
	public static Logger getLogger(String groupName) {
		return Logger.getLogger(groupName);
	}
	
	/////////////////////////////////////////////////////
	// some static helper methods
	
	/**
	 * Takes the {@link #getDefaultConfigurationAsProperties default configuration}
	 * and overrides
	 * <ul>
	 * 	<li><tt>.level</tt> with the specified log level</li>
	 * 	<li><tt>handlers</tt> with a standard {@link FileHandler file} handler</li>
	 * 	<li><tt>java.util.logging.FileHandler.pattern</tt> with the specified
	 * 		log file</li>
	 * </ul>
	 * The log manager is {@link #initLogManagerConfiguration(Properties)} with 
	 * the resulting properties.
	 * <p>
	 * According to the default configuration properties, a 
	 * {@link LogFormatter} is used by default.
	 * 
	 * @param	file	The file to log to
	 * @param	level	The desired log level
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have LoggingPermission("control").
     * @exception  IOException if there are problems reading the properties
     * @see LogManager#readConfiguration(InputStream)
	 */
	public static void logToFile(File file, Level level) throws IOException {
		final Properties props = getDefaultConfigurationAsProperties();
		props.setProperty(".level", level.getName());
		props.setProperty("handlers", FileHandler.class.getName());
		props.setProperty(FileHandler.class.getName() + ".pattern", file.getAbsolutePath());
		initLogManagerConfiguration(props);
	}
	/**
	 * Takes the {@link #getDefaultConfigurationAsProperties default configuration}
	 * and overrides
	 * <ul>
	 * 	<li><tt>.level</tt> with the specified log level</li>
	 * 	<li><tt>handlers</tt> with a standard {@link FileHandler file} handler</li>
	 * 	<li><tt>java.util.logging.FileHandler.pattern</tt> with the specified
	 * 		log file</li>
	 * </ul>
	 * The log manager is {@link #initLogManagerConfiguration(Properties)} with 
	 * the resulting properties.
	 * <p>
	 * According to the default configuration properties, a 
	 * {@link LogFormatter} is used by default.
	 * 
	 * @param	fileName	The file to log to
	 * @param	level		The desired log level
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have LoggingPermission("control").
     * @exception  IOException if there are problems reading the properties
     * @see LogManager#readConfiguration(InputStream)
	 */
	public static void logToFile(String fileName, Level level) throws IOException {
		logToFile(new File(fileName), level);
	}
	
	/**
	 * Takes the {@link #getDefaultConfigurationAsProperties default configuration}
	 * and overrides
	 * <ul>
	 * 	<li><tt>.level</tt> with the specified log level</li>
	 * 	<li><tt>handlers</tt> with a standard {@link StandardErrHandler err} and
	 * 		{@link StandardErrHandler out} handler</li>
	 * </ul>
	 * The log manager is {@link #initLogManagerConfiguration(Properties)} with 
	 * the resulting properties.
	 * <p>
	 * According to the default configuration properties, WARNING and more 
	 * severe messages are now logged on the standard error stream, everything
	 * else on the standard output (if at least as severe as the specified 
	 * level). For formatting, a {@link LogFormatter} is used by default.
	 * 
	 * @param	level	The desired log level
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have LoggingPermission("control").
     * @exception  IOException if there are problems reading the properties
     * @see LogManager#readConfiguration(InputStream)
	 */
	public static void logToConsole(Level level) throws SecurityException, IOException {
		final Properties props = getDefaultConfigurationAsProperties();
		props.setProperty(".level", level.getName());
		props.setProperty("handlers", StandardErrHandler.class.getName() + "," + StandardOutHandler.class.getName());
		initLogManagerConfiguration(props);
	}
	
	/**
	 * Returns true if {@link Logger#isLoggable(Level)} returns true, the
	 * the logger's {@link Logger#getFilter() filter} (if any) would log a 
	 * record with the given log level and if any of the installed handlers
	 * would log that record.
	 * 
	 * @param logger	the logger to test loggability for
	 * @param level		the level to check
	 * @return true if the logger logs records for the given level
	 */
	public static boolean isLoggable(Logger logger, Level level) {
		if (!logger.isLoggable(level)) return false; 
		final LogRecord testRec = new LogRecord(level, "is loggable test record");
		if (logger.getFilter() == null || logger.getFilter().isLoggable(testRec)) {
			for (Handler hdl : logger.getHandlers()) {
				if (hdl.isLoggable(testRec)) return true;
			}
		}
		if (logger.getUseParentHandlers()) {
			Logger parent = logger.getParent();
			if (parent != null) {
				return isLoggable(parent, level);
			}
		}
		return false;
	}
	
	/**
	 * Removes all handlers of the given logger
	 * 
	 * @param	logger	the logger of which all handlers should be removed
	 * @param	close	specifies whether the removed handlers should be closed
	 */
	public static void removeAllHandlers(Logger logger, boolean close) {
		for (Handler hdl : logger.getHandlers()) {
			logger.removeHandler(hdl);
			if (close) hdl.close();
		}		
	}
	
	/**
	 * Installs the {@link LogFormatter#FORMAT_PLAIN plain} log format to all
	 * handlers of the specified {@code logger}
	 */
	public static void setPlainFormatter(Logger logger) {
		setFormatter(logger, new LogFormatter(LogFormatter.FORMAT_PLAIN));
	}
	/**
	 * Installs the specified {@code formatter} to all handlers of the 
	 * specified {@code logger}.
	 */
	public static void setFormatter(Logger logger, LogFormatter formatter) {
		for (final Handler h : logger.getHandlers()) {
			h.setFormatter(formatter);
		}		
	}
	
	/**
	 * Returns the file name of the default configuration, which is 
	 * <tt>ch/javasoft/util/logging/Loggers.properties</tt>
	 */
	public static String getDefaultConfigurationName() {
		return Loggers.class.getSimpleName() + ".properties";
	}

	/**
	 * Initializes the {@link LogManager} with a
	 * {@link #getDefaultConfiguration() default configuration} defined by this
	 * package, but only if neither
	 * {@link SystemProperties#LogManagerPropertiesClass} nor 
	 * {@link SystemProperties#LogManagerPropertiesFile} is set in the system
	 * properties.
	 * <p>
	 * If none of the mentioned properties is defined, 
	 * {@link LogManager#readConfiguration(InputStream) LogManager#readConfiguration(InputStream)}
	 * is called with the 
	 * {@link #getDefaultConfiguration() default configuration}, otherwise,
	 * {@link LogManager#readConfiguration()} is called.
	 */
	private static void initLogManagerConfiguration() {
		try {
			if (SystemProperties.LogManagerPropertiesClass.getSystemProperty() == null) {
				if (SystemProperties.LogManagerPropertiesFile.getSystemProperty() == null) {
					LogManager.getLogManager().readConfiguration(getDefaultConfiguration());
				}
				else {
					LogManager.getLogManager().readConfiguration();
				}
			}		
			else {
				LogManager.getLogManager().readConfiguration();
			}
		}
		catch (Exception ex) {
			System.err.println(
				"cannot initialize log manager configuration, e=" + ex
			);
			ex.printStackTrace();
		}
	}
	/**
	 * Initializes the logger configuration with the given configuration
	 * properties. Calls {@link LogManager#readConfiguration(InputStream)}.
	 * 
	 * @param config				the configuration values
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have LoggingPermission("control").
     * @exception  IOException if there are problems reading the properties
     * @see LogManager#readConfiguration(InputStream)
	 */
	public static void initLogManagerConfiguration(Properties config) throws SecurityException, IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		config.store(buf, "configuration from properties");
		ByteArrayInputStream in = new ByteArrayInputStream(buf.toByteArray());
		LogManager.getLogManager().readConfiguration(in);
	}

	/**
	 * Returns the default configuration for the log manager, see 
	 * {@link #getDefaultConfigurationName()}, but as properties object. The
	 * properties may be changed and reapplied as logger configuration by
	 * calling {@link #initLogManagerConfiguration(Properties)}.
	 */
	public static Properties getDefaultConfigurationAsProperties() throws IOException {
		final String propFile = getDefaultConfigurationName();
		InputStream in = Loggers.class.getResourceAsStream(propFile);
		if (in == null) {
			throw new FileNotFoundException(propFile);
		}
		final Properties props = new Properties();
		props.load(in);
		return props;
	}
	/**
	 * Returns the default configuration for the log manager, see 
	 * {@link #getDefaultConfigurationName()}
	 */
	private static InputStream getDefaultConfiguration() throws IOException {
		final String propFile = getDefaultConfigurationName();
		InputStream in = Loggers.class.getResourceAsStream(propFile);
		if (in == null) {
			throw new FileNotFoundException(propFile);
		}
		return in;
	}
	
	//no instances
	private Loggers() {
		super();
	}
	
}
