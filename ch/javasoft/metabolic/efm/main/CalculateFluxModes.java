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
package ch.javasoft.metabolic.efm.main;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.dom4j.Element;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.ElementaryFluxModes;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.config.XmlAttribute;
import ch.javasoft.metabolic.efm.config.XmlElement;
import ch.javasoft.metabolic.efm.output.CountOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.NullOutputCallback;
import ch.javasoft.metabolic.efm.output.OutputMode;
import ch.javasoft.metabolic.efm.output.RandomAccessFileOutputCallback;
import ch.javasoft.metabolic.efm.output.mat.MatFileOutputCallback;
import ch.javasoft.metabolic.parse.ConfiguredParser;
import ch.javasoft.util.logging.LogPrintStream;
import ch.javasoft.util.logging.Loggers;
import ch.javasoft.util.logging.SystemProperties;
import ch.javasoft.util.logging.matlab.LogConfiguration;
import ch.javasoft.util.logging.matlab.LogConfigurationReader;
import ch.javasoft.xml.config.FileConfigParser;
import ch.javasoft.xml.config.MissingReferableException;
import ch.javasoft.xml.config.StreamConfigParser;
import ch.javasoft.xml.config.XmlArgException;
import ch.javasoft.xml.config.XmlConfig;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlUtil;

/**
 * Start class for the efm computation with an xml config file (e.g.
 * config/metabolic-efm.xml). The invocation kind depends on the config file
 * and on arguments and options passed to the {@link #main(String[]) main} or 
 * {@link #matlab(String[])} method.
 */
public class CalculateFluxModes {
	
	/** Exit value for normal termination */
	public static final int EXIT_VAL_OK 				=  0;
	/** Exit value for invocation with help or version option and invalid extra options */
	public static final int EXIT_VAL_HELP_VERSION_ARG   = -1;
	/** Exit value for missing or invalid input argument or option */
	public static final int EXIT_VAL_CONFIG_ARG 		= -2;
	/** Exit value for missing referable in config, e.g. no kind was specified */
	public static final int EXIT_VAL_CONFIG_REF 		= -3;
	/** Exit value for if an unexpected exception occurred */
	public static final int EXIT_VAL_EXCEPTION			= -4;
	/** Exit value with unknown reason, should not happen */
	public static final int EXIT_VAL_UNKNOWN			= -5;
	
	/**
	 * Calls the efm computation. System.exit is called after completion, with
	 * zero exit value for normal termination. For abnormal termination, a 
	 * negative exit value is returned. If elementary modes are counted, the 
	 * number of modes is used as exit value.
	 */
	public static void main(String[] args) {
		start(args, false);
	}
	/**
	 * Calls the efm computation, System.exit is not called (causes matlab to
	 * quit). The return value is 0 for normal completion and negative for
	 * abnormal completion. If elementary modes are counted, the number of
	 * modes is returned.
	 */
	public static long matlab(String[] args) {
		return start(args, true);
	}

	private static long start(String[] args, boolean noExit) {
		try {
			if (args.length >= 1) {
				final XmlConfig xmlConfig = Config.parseXmlConfig(args); 
				boolean err = args.length > 1;
				if ("--help".equals(args[0]) || "-?".equals(args[0]) || "?".equals(args[0])) {
					Config.printHelp(err ? System.err : System.out, xmlConfig);
					return exit(noExit, err ? EXIT_VAL_HELP_VERSION_ARG : EXIT_VAL_OK);
				}
				if ("--version".equals(args[0]) || "-v".equals(args[0])) {
					Config.printVersion(err ? System.err : System.out, xmlConfig);
					return exit(noExit, err ? EXIT_VAL_HELP_VERSION_ARG : EXIT_VAL_OK);
				}
			}
			try {
				final XmlConfig xmlConfig = Config.resolveXmlConfig(args); 
//				final String config = XmlUtil.getRequiredAttributeValue(xmlConfig.getDefaultConfig(), XmlAttribute.name);
//				System.out.println(xmlConfig.getAppName() + " [config: " + config + "]");
				if (noExit) {
					//MATLAB, initialize configuration manually
					initLoggingConfigurationForMatlab(xmlConfig);
				}
				else {
					Loggers.initLogManagerConfiguration(xmlConfig.getLoggingProperties());
//					xmlConfig.initLogManagerConfiguration();
				}
		        final Logger logger = ch.javasoft.util.logging.Loggers.getRootLogger();
				final LogPrintStream logStream = new LogPrintStream(logger, Level.INFO);
				Config.printVersion(logStream, xmlConfig);
				logStream.flush();
				
				final long val = start(xmlConfig);
				if (val >= 0) {
					return exit(noExit, val);
				}
			}
			catch (XmlArgException ex) {
				//already traced
				return exit(noExit, EXIT_VAL_CONFIG_ARG);
			}
			catch (MissingReferableException ex) {
				//already traced
				return exit(noExit, EXIT_VAL_CONFIG_REF);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Config.traceArgs(System.err, CalculateFluxModes.class.getName() + " with following arguments: ", args);
			return exit(noExit, EXIT_VAL_EXCEPTION);
		}
		return exit(noExit, EXIT_VAL_OK);
	}
	private static long exit(boolean noExit, long exitVal) {
		if (noExit) return exitVal;
		else {
			System.exit((int)exitVal);
			//should not get he below this line
			return EXIT_VAL_UNKNOWN;
		}
	}
	
	/**
	 * Initializes logging for matlab. Within matlab, logging cannot be 
	 * initialized using the log manager since the system class loader does not 
	 * know classes in the dynamic path.
	 */
	private static void initLoggingConfigurationForMatlab(XmlConfig config) throws XmlConfigException, IOException {
//		final Properties props = config.getLoggingProperties();
//        final MessageFormat format = new MessageFormat(props.getProperty(LogFormatter.getFormatPropertyName(LogFormatter.class)));
//        final LogFormatter formatter = new LogFormatter(format);
//        final Level level = Level.parse(props.getProperty(".level"));
//        final LevelFilter filter = new LevelFilter(LevelFilter.Type.LessSevere, level);
//        final StandardOutHandler out = new StandardOutHandler();
//        final StandardErrHandler err = new StandardErrHandler();
//        out.setFormatter(formatter);
//        err.setFormatter(formatter);
//        out.setLevel(java.util.logging.Level.ALL);
//        err.setLevel(java.util.logging.Level.WARNING);
//        out.setFilter(filter);
//		final LogConfigurationReader lcr = new LogConfigurationReader(false);
//		lcr.initializeLogHandlers();
//		System.out.println(StandardOutHandler.class.getName());
//		System.out.println(StandardErrHandler.class.getName());
//		new LogConfigurationReader();
		final Properties logProps = config.getLoggingProperties();
		final LogConfiguration logConfig = logProps == null ? new LogConfiguration() : new LogConfiguration(logProps);
		System.setProperty(SystemProperties.LogManagerPropertiesClass.getPropertyName(), new LogConfigurationReader(logConfig).getClass().getName());
//		System.getProperties().remove(SystemProperties.LogManagerPropertiesFile.getPropertyName());
        final Logger logger = ch.javasoft.util.logging.Loggers.getRootLogger();
//        Loggers.removeAllHandlers(logger, false);
//        logger.addHandler(out);
//        logger.addHandler(err);
        logger.info("logger initialized");
	}
	/**
	 * Calls efm computation, returns the number of efms for count, and -1
	 * otherwise
	 */
	private static long start(XmlConfig config) throws XmlConfigException, IOException {
		Element defConfig 			= config.getDefaultConfigElement();
		Element efmImplConfig 		= Config.getConfigEfmImpl(config);
		Element efmOutputConfig		= Config.getConfigEfmOutput(config);
		
		ElementaryFluxModes.setImpl(Config.getEfmImpl(config, efmImplConfig));
		
		MetabolicNetwork net 		= ConfiguredParser.parseConfig(defConfig);
		EfmOutputCallback callback 	= getEfmOutCb(net, efmOutputConfig);
		ElementaryFluxModes.calculateCallback(net, callback);
		return callback instanceof CountOutputCallback ?
			((CountOutputCallback)callback).getEfmCount() : -1;
	}
	
	/**
	 * <pre>
		<callback type="TextOutputCallback">
			<mode value="BinaryUncompressed"/>
			<stream ref="{file-output-stream"/>
		</callback>
		
		<callback type="CountOuputCallback">
			<uncompress value="true"/>
			<stream ref="log-output-stream"/>
		</callback>		
	 * </pre> 
	 * @param efmOutputConfig
	 * @throws XmlConfigException
	 */
	@SuppressWarnings("unchecked")
	private static EfmOutputCallback getEfmOutCb(MetabolicNetwork net, Element efmOutputConfig) throws XmlConfigException {
		Element elCallback 	= XmlUtil.getRequiredSingleChildElement(efmOutputConfig, XmlElement.callback);
		String implClass 	= XmlUtil.getRequiredAttributeValue(elCallback, XmlAttribute.type);
		try {
			Class<? extends EfmOutputCallback> clazz = (Class<? extends EfmOutputCallback>)Class.forName(implClass);
			if (!EfmOutputCallback.class.isAssignableFrom(clazz)) {
				throw new XmlConfigException("not an efm output callback class: " + implClass, elCallback);
			}
			if (clazz == CountOutputCallback.class) {
                                System.out.println("INFO: 1: output written by OutputStream\n");
				Element elUncompress 	= XmlUtil.getRequiredSingleChildElement(elCallback, XmlElement.uncompress);
				Element elStream		= XmlUtil.getRequiredSingleChildElement(elCallback, XmlElement.stream);
				boolean uncompress 		= Boolean.parseBoolean(XmlUtil.getRequiredAttributeValue(elUncompress, XmlAttribute.value));
				OutputStream out		= StreamConfigParser.parseOutputStream(elStream);
				return new CountOutputCallback(out, uncompress);
			}
			else if (clazz == NullOutputCallback.class) {
				return NullOutputCallback.INSTANCE;
			}
			else if (clazz == MatFileOutputCallback.class) {
				Element elFile			= XmlUtil.getRequiredSingleChildElement(elCallback, XmlElement.file);
				File file 				= FileConfigParser.parseFile(elFile);
				File folder 			= file.getParentFile();
				String fileName			= file.getName();
                                System.out.println("INFO: 2: output written to Matlab file\n");
				String strEfmsPerFile	= XmlUtil.getRequiredAttributeValue(elFile, XmlAttribute.efms_per_file);
				OutputMode mode			= parseOutputMode(elCallback);
				
				final int efmsPerFile;
				try {
					efmsPerFile = Integer.parseInt(strEfmsPerFile);
				}
				catch (NumberFormatException e) {
					throw new XmlConfigException("unable to parse '" + XmlAttribute.efms_per_file.getXmlName() + "' attribute, e=" + e, elFile);
				}
				
				if (efmsPerFile > 0) {
					return new MatFileOutputCallback(mode, net, folder, fileName, efmsPerFile);
				}
				else {
					return new MatFileOutputCallback(mode, net, folder, fileName);
				}
			}
			else if (clazz == RandomAccessFileOutputCallback.class) {
				Element elFile	= XmlUtil.getRequiredSingleChildElement(elCallback, XmlElement.file);
				File file 		= FileConfigParser.parseFile(elFile);
				OutputMode mode	= parseOutputMode(elCallback);
                                System.out.println("INFO: 3: output written by RandomAccessFileOutputCallback() to file '" + file + "'\n");
				return new RandomAccessFileOutputCallback(net, mode, file);
			}
			else {
                                System.out.println("INFO: 4: output written by OutputStream()\n");
				Element elStream	= XmlUtil.getRequiredSingleChildElement(elCallback, XmlElement.stream);
				OutputStream out	= StreamConfigParser.parseOutputStream(elStream);
				OutputMode mode		= parseOutputMode(elCallback);
				final Class[] signature = new Class[] {MetabolicNetwork.class, OutputMode.class, OutputStream.class};
				Constructor<? extends EfmOutputCallback> cons = clazz.getConstructor(signature);
				return cons.newInstance(new Object[] {net, mode, out});
			}
		}
		catch (Exception ex) {
			throw new XmlConfigException("cannot instantiate callback class '" + implClass + "', e=" + ex, efmOutputConfig, ex);
		}
	}
	
	private static OutputMode parseOutputMode(Element elCallback) throws XmlConfigException {
		Element elMode	= XmlUtil.getRequiredSingleChildElement(elCallback, XmlElement.mode);
		String  strMode = XmlUtil.getRequiredAttributeValue(elMode, XmlAttribute.value);				
		try {
			return OutputMode.valueOf(strMode);
		}
		catch (Exception ex) {
			throw new XmlConfigException("invalid output mode '" + strMode + "', e=" + ex, elMode, ex);
		}		
	}

	//no instances
	private CalculateFluxModes() {
		super();
	}
	
}
