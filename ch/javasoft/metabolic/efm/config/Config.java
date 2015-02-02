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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.DocumentException;
import org.dom4j.Element;

import ch.javasoft.io.Files;
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.compress.config.MetabolicCompressionConfig;
import ch.javasoft.metabolic.efm.ElementaryFluxModes;
import ch.javasoft.metabolic.efm.adj.AdjMethodFactory;
import ch.javasoft.metabolic.efm.impl.SequentialDoubleDescriptionImpl;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.memory.incore.InCoreMemoryFactory;
import ch.javasoft.metabolic.efm.model.EfmModelFactory;
import ch.javasoft.metabolic.efm.model.nullspace.NullspaceEfmModelFactory;
import ch.javasoft.metabolic.efm.progress.ProgressType;
import ch.javasoft.metabolic.efm.sort.SortUtil;
import ch.javasoft.metabolic.efm.util.TempDir;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.logging.Loggers;
import ch.javasoft.util.numeric.Zero;
import ch.javasoft.xml.config.MissingReferableException;
import ch.javasoft.xml.config.XmlArgException;
import ch.javasoft.xml.config.XmlConfig;
import ch.javasoft.xml.config.XmlConfigException;
import ch.javasoft.xml.config.XmlUtil;

public class Config {
	
	public static final String CONFIG_FILE	= "config/metabolic-efm.xml";

	public static final String DEFAULT_CONFIG_NAME	= "default";
	public static final String DEFAULT_APP_NAME		= "efmtool";
	public static final String HELP_NAME			= "efm-help";
	public static final String USAGE_NAME			= "efm-usage";
	public static final String VERSION_NAME			= "efm-version";
	
	private final XmlConfig				mXmlConfig;//might be zero
	private final AdjMethodFactory		mAdjFactory;
	private final Zero					mZero;
	private final String				mAdjMethod;
	private final String				mRowOrdering;
	private final boolean				mSelfTest;
	private final boolean				mParseOnly;
	private final int					mMaxThreads;
	private final Normalize				mNormalize;
	private final Arithmetic			mArithmetic;
	private final int					mPrecision;
	private final int 					mProgressPartition;//100 for 1%, 50 for 2%, 0 for off
	private final ProgressType			mProgressType;//	none / file / swing
	private final DistributedConfig		mDistributedConfig;
	private final Generator				mGenerator;
	private final TempDir				mTempDir;
	private final Set<String>			mReactionsToSuppress;
	private final Set<String>			mReactionsToEnforce;
	private final Set<String>			mReactionsNoSplit;
	private final String				mFlag;
	
    private final CompressionMethod[]	mCompressionMethods;
    private final boolean				mPreprocessDuplicateGenes;
    
	public Config(Zero zero, String adjMethod, String rowOrdering, CompressionMethod[] compressionMethods, boolean preprocessDuplicateGenes, boolean selfTest, boolean parseOnly, int maxThreads, Arithmetic arithmetic, int precision, Generator generator, Normalize normalize, String reactionsToSuppress, String reactionsToEnforce, String reactionsNoSplit, File tempDir, int progressPartition, ProgressType progressType, String flag, DistributedConfig distConfig) {
		this(zero, adjMethod, rowOrdering, compressionMethods, preprocessDuplicateGenes, selfTest, parseOnly, maxThreads, arithmetic, precision, generator, normalize, toSet(reactionsToSuppress), toSet(reactionsToEnforce), toSet(reactionsNoSplit), tempDir, progressPartition, progressType, flag, distConfig);
	}
	public Config(Zero zero, String adjMethod, String rowOrdering, CompressionMethod[] compressionMethods, boolean preprocessDuplicateGenes, boolean selfTest, boolean parseOnly, int maxThreads, Arithmetic arithmetic, int precision, Generator generator, Normalize normalize, Set<String> reactionsToSuppress, Set<String> reactionsToEnforce, Set<String> reactionsNoSplit, File tempDir, int progressPartition, ProgressType progressType, String flag, DistributedConfig distConfig) {
		this(null, zero, adjMethod, rowOrdering, compressionMethods, preprocessDuplicateGenes, selfTest, parseOnly, maxThreads, arithmetic, precision, generator, normalize, reactionsToSuppress, reactionsToEnforce, reactionsNoSplit, tempDir, progressPartition, progressType, flag, distConfig);
	}
	public Config(XmlConfig xmlConfig, Zero zero, String adjMethod, String rowOrdering, CompressionMethod[] compressionMethods, boolean preprocessDuplicateGenes, boolean selfTest, boolean parseOnly, int maxThreads, Arithmetic arithmetic, int precision, Generator generator, Normalize normalize, String reactionsToSuppress, String reactionsToEnforce, String reactionsNoSplit, File tempDir, int progressPartition, ProgressType progressType, String flag, DistributedConfig distConfig) {
		this(xmlConfig, zero, adjMethod, rowOrdering, compressionMethods, preprocessDuplicateGenes, selfTest, parseOnly, maxThreads, arithmetic, precision, generator, normalize, toSet(reactionsToSuppress), toSet(reactionsToEnforce), toSet(reactionsNoSplit), tempDir, progressPartition, progressType, flag, distConfig);
	}
	public Config(XmlConfig xmlConfig, Zero zero, String adjMethod, String rowOrdering, CompressionMethod[] compressionMethods, boolean preprocessDuplicateGenes, boolean selfTest, boolean parseOnly, int maxThreads, Arithmetic arithmetic, int precision, Generator generator, Normalize normalize, Set<String> reactionsToSuppress, Set<String> reactionsToEnforce, Set<String> reactionsNoSplit, File tempDir, int progressPartition, ProgressType progressType, String flag, DistributedConfig distConfig) {
		mZero						= zero;
		mAdjMethod					= (adjMethod == null || adjMethod.length() == 0 || "default".equalsIgnoreCase(adjMethod)) ? AdjMethodFactory.DEFAULT_ADJ_METHOD : adjMethod;
		mRowOrdering				= (rowOrdering == null || rowOrdering.length() == 0 || "default".equalsIgnoreCase(rowOrdering)) ? SortUtil.DEFAULT_SORTER : rowOrdering;
		mSelfTest					= selfTest;
		mParseOnly					= parseOnly;
		mPreprocessDuplicateGenes	= preprocessDuplicateGenes;
		mCompressionMethods			= compressionMethods == null ? CompressionMethod.STANDARD : compressionMethods;
        mMaxThreads					= maxThreads <= 0 ? Runtime.getRuntime().availableProcessors() : maxThreads;
        mArithmetic					= arithmetic;
        mGenerator					= generator;
        mPrecision					= precision;
        mProgressPartition			= progressPartition;
        mProgressType				= progressType;
        mNormalize					= normalize;
        mTempDir					= new TempDir(tempDir);
        mReactionsToSuppress		= reactionsToSuppress;
        mReactionsToEnforce			= reactionsToEnforce;
        mReactionsNoSplit			= reactionsNoSplit;
        mXmlConfig					= xmlConfig;
        mFlag						= flag;
        mDistributedConfig			= distConfig == null ? getDistributedConfig(xmlConfig) : distConfig;
		mAdjFactory					= initAdjFactory(this);
	}
	private static AdjMethodFactory initAdjFactory(Config config) {
		try {
			return new AdjMethodFactory(config);
		} 
		catch (Exception e) {
			throw new RuntimeException("cannot initialize AdjFactories, e=" + e, e);
		}
	}
	private static DistributedConfig getDistributedConfig(XmlConfig xmlConfig) {
		try {
			if (xmlConfig == null) {
				return new DistributedConfig();
			}
			Element efmImplConfig	= getConfigEfmImplConfig(xmlConfig);
			Element efmDistConfig	= XmlUtil.getOptionalSingleChildElement(efmImplConfig, XmlElement.distribute);
			if (efmDistConfig == null) {
				return new DistributedConfig();				
			}
			return new DistributedConfig(efmDistConfig);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	private static Set<String> toSet(String reacs) {
		if (reacs == null || reacs.trim().length() == 0) {
			return new LinkedHashSet<String>(0);
		}
		return ch.javasoft.util.Arrays.asSet(reacs.trim().split("[\\s,]+"));
	}
	private static File getDefaultTempDir() {
		final File localTemp = new File("/local/tmp/");
		if (localTemp.exists() && localTemp.isDirectory() && localTemp.canRead() && localTemp.canWrite()) {
			return localTemp;
		}
		return Files.getTempDir(); 
	}
	public Zero zero() {
		return mZero;
	}
	public AdjMethodFactory getAdjMethodFactory() {
		return mAdjFactory;
	}
	public String getAdjMethod() {
		return mAdjMethod;
	}
	public String getRowOrdering() {
		return mRowOrdering;
	}
	public Arithmetic getArithmetic() {
		return mArithmetic;
	}
	public Generator getGenerator() {
		return mGenerator;
	}
	/**
	 * Returns the (fractional) precision in bits, or -1 for infinite precision
	 */
	public int getPrecision() {
		return mPrecision;
	}
	public Normalize getNormalize() {
		return mNormalize;
	}
	
	public int getProgressPartition() {
		return mProgressPartition;
	}
	public ProgressType getProgressType() {
		return mProgressType;
	}
	
	/**
	 * Returns the flag value, a non-empty trimmed string, or null if no flag
	 * is defined or it was empty or whitespace only
	 */
	public String getFlag() {
		return mFlag;
	}
	
	public boolean compressNetwork(boolean includeDuplicateGenesCompression) {
		if (mCompressionMethods == null || mCompressionMethods.length == 0) return false;
		if (mCompressionMethods.length > 1) return true;
		return includeDuplicateGenesCompression || mCompressionMethods[0] != CompressionMethod.DuplicateGene;
	}
	public boolean getPreprocessDuplicateGenes() {
		return mPreprocessDuplicateGenes;
	}
	/**
	 * Returns the compression methods, with or without duplicate gene methods
	 * 
	 * @param includeDuplicateGenesCompression	whether or not to include 
	 * 											methods for duplicate gene 
	 * 											compression
	 */
	public CompressionMethod[] getCompressionMethods(boolean includeDuplicateGenesCompression) {
		if (!includeDuplicateGenesCompression) {
			return CompressionMethod.removeDuplicateGeneMethods(mCompressionMethods);
		}
		return mCompressionMethods;
	}
	public boolean selfTest() {
		return mSelfTest;
	}
	public boolean parseOnly() {
		return mParseOnly;
	}
	
	/**
	 * Returns the names of reactions which are suppressed, i.e. their flux
	 * value is always zero
	 * <p>
	 * Note: the returned set is modifiable, but the caller has to ensure that
	 * modifications are made before starting the efm computation. Otherwise,
	 * the outcome of the computation is undefined. 
	 * 
	 * @return	An (modifiable) set containing the names of reactions to suppress
	 */
	public Set<String> getReactionsToSuppress() {
		return mReactionsToSuppress;
	}
	/**
	 * Returns the names of reactions which are enforced, i.e. their flux
	 * value is never zero
	 * <p>
	 * Note: the returned set is modifiable, but the caller has to ensure that
	 * modifications are made before starting the efm computation. Otherwise,
	 * the outcome of the computation is undefined. 
	 * 
	 * @return	An (modifiable) set containing the names of reactions to enforce
	 */
	public Set<String> getReactionsToEnforce() {
		return mReactionsToEnforce;
	}
	/**
	 * Returns the names of reactions which for which negative flux values are
	 * allowed. Such reversible reactions are not split into two irreversible 
	 * reactions. With this option, elementary modes, extreme pathways and
	 * generators can be supported (different treatment of reversible reactions).
	 * <p>
	 * Note: the returned set is modifiable, but the caller has to ensure that
	 * modifications are made before starting the efm computation. Otherwise,
	 * the outcome of the computation is undefined. 
	 * 
	 * @return	An (modifiable) set containing the names of reactions for which 
	 * 			negative flux values are allowed
	 */
	public Set<String> getReactionsNoSplit() {
		return mReactionsNoSplit;
	}
	
	/**
	 * Returns the distributed configuration, never null;
	 */
	public DistributedConfig getDistributedConfig() {
		return mDistributedConfig;
	}
	
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified configuration values and
	 * defaults for missing values. 
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #initForJUnitTest(Config)
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(String adjMethod, CompressionMethod[] cmpMethods, Arithmetic arithmetic) {
		final Zero zero		= arithmetic == Arithmetic.fractional ? new Zero(0d) : new Zero();
		final Config defCfg = new Config(			
			zero, adjMethod, SortUtil.DEFAULT_SORTER, cmpMethods, true, 
			false, false, Runtime.getRuntime().availableProcessors(), 
			arithmetic, -1, Generator.Efm, Normalize.norm2, 
			(String)null, (String)null, (String)null, getDefaultTempDir(), 
			100, ProgressType.Swing,
			null, null
		);
		return initForJUnitTest(defCfg);
	}
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified configuration values and
	 * defaults for missing values. 
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #initForJUnitTest(Config)
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(String adjMethod, String rowOrdering, CompressionMethod[] cmpMethods, Arithmetic arithmetic) {
		final Config defCfg = new Config(
			arithmetic.getDefaultZero(), adjMethod, rowOrdering, cmpMethods, 
			true, false, false, Runtime.getRuntime().availableProcessors(), 
			arithmetic, -1, Generator.Efm, Normalize.norm2, 
			(String)null, (String)null, (String)null, getDefaultTempDir(), 
			100, ProgressType.Swing, 
			null /*flag*/, null /*dist config*/
		);
		return initForJUnitTest(defCfg);		
	}
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified configuration values and
	 * defaults for missing values. 
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #initForJUnitTest(Config)
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(String adjMethod, String rowOrdering, CompressionMethod[] cmpMethods, boolean selfTest, Arithmetic arithmetic) {
		final Config defCfg = new Config(
			arithmetic.getDefaultZero(), adjMethod, rowOrdering, cmpMethods, 
			true, false, selfTest, Runtime.getRuntime().availableProcessors(), 
			arithmetic, -1, Generator.Efm, Normalize.norm2, 
			(String)null, (String)null, (String)null, getDefaultTempDir(), 
			100, ProgressType.Swing, 
			null /*flag*/, null /*dist config*/
		);
		return initForJUnitTest(defCfg);
	}
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified configuration values and
	 * defaults for missing values. 
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #initForJUnitTest(Config)
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(String adjMethod, String rowOrdering, CompressionMethod[] cmpMethods, boolean selfTest, Arithmetic arithmetic, Norm normalize) {
		final Config defCfg = new Config(
			arithmetic.getDefaultZero(), adjMethod, rowOrdering, cmpMethods, 
			true, false, selfTest, Runtime.getRuntime().availableProcessors(), 
			arithmetic, -1, Generator.Efm, Normalize.valueOf(normalize),
			(String)null, (String)null, (String)null, getDefaultTempDir(), 
			100, ProgressType.Swing, 
			null /*flag*/, null /*dist config*/
		);
		return initForJUnitTest(defCfg);
	}
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified configuration values and
	 * defaults for missing values. 
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #initForJUnitTest(Config)
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(String adjMethod, String rowOrdering, CompressionMethod[] cmpMethods, boolean selfTest, Arithmetic arithmetic, Norm normalize, int maxThreads) {
		final Config defCfg = new Config(
			arithmetic.getDefaultZero(), adjMethod, rowOrdering, cmpMethods, 
			true, false, selfTest, maxThreads, 
			arithmetic, -1, Generator.Efm, Normalize.valueOf(normalize),
			(String)null, (String)null, (String)null, getDefaultTempDir(), 
			100, ProgressType.Swing, 
			null /*flag*/, null /*dist config*/
		);
		return initForJUnitTest(defCfg);
	}
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified configuration values and
	 * defaults for missing values. 
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #initForJUnitTest(Config)
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(String adjMethod, String rowOrdering, CompressionMethod[] cmpMethods, boolean selfTest, Arithmetic arithmetic, Norm normalize, int maxThreads, ProgressType progressType) {
		final Config defCfg = new Config(
			arithmetic.getDefaultZero(), adjMethod, rowOrdering, cmpMethods, 
			true, false, selfTest, maxThreads, 
			arithmetic, -1, Generator.Efm, Normalize.valueOf(normalize),
			(String)null, (String)null, (String)null, getDefaultTempDir(), 
			100, progressType, 
			null /*flag*/, null /*dist config*/
		);
		return initForJUnitTest(defCfg);
	}
	/**
	 * Initializes elementary flux mode calculation for junit tests. If there is 
	 * already a configuration, false is returned. Otherwise, binary nullspace 
	 * implementation is initalized with the specified default configuration.
	 *
	 * @return 	true if configured as specified, false if an existing 
	 * 			configuration was found
	 *
	 * @see #getConfig()
	 */
	public static boolean initForJUnitTest(Config defaultConfig) {
		try {
			getConfig();
			return false;
		}
		catch (IllegalStateException ex) {
			ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(defaultConfig, new NullspaceEfmModelFactory(), new InCoreMemoryFactory()));
			traceArgs(Loggers.getRootLogger(), Level.INFO, "initForJUnitTest() with following (derived) arguments: ", defaultConfig.getAsArgs());
			return true;
		}
	}
	
	/**
	 * Returns the current config from the elementary flux mode computation 
	 * implementation
	 * 
	 * @throws IllegalStateException	If no configuration can be determined
	 */
	public static Config getConfig() throws IllegalStateException {
		final ElementaryFluxModes.Impl impl = ElementaryFluxModes.getImpl();
		if (impl == null) {
			throw new IllegalStateException("no impl set in ElementaryFluxModes");
		}
		final Config config = impl.getConfig();
		if (config == null) {
			throw new IllegalStateException("no config in ElementaryFluxModes.getImpl()");
		}
		return config;
	}
	
	/**
	 * Derives the configuration from the xml file stored in the system 
	 * properties.
	 * @return						the config
	 * @throws XmlConfigException	if invalid configuration settings were 
	 * 								detected
	 */
	public static Config getFromXmlConfig(XmlConfig xmlConfig) throws XmlConfigException {
		Element efmCompConfig	= getConfigMetabolicCompression(xmlConfig);		
		Element efmImplConfig	= getConfigEfmImplConfig(xmlConfig);
		Element efmOutputConfig	= getConfigEfmOutput(xmlConfig);
		Element efmNumConfig	= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.numeric);
		Element efmDistConfig	= XmlUtil.getOptionalSingleChildElement(efmImplConfig, XmlElement.distribute);
		Element elGenerator		= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.generator);
		Element elReacsToSup	= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.reactions_to_suppress);
		Element elReacsToEnf	= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.reactions_to_enforce);
		Element elReacsNoSplit	= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.reactions_no_split);
		Element elTmpDir		= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.temp_dir);
		Element elProgress		= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.progress);
		Element elFlag			= XmlUtil.getOptionalSingleChildElement(efmImplConfig, XmlElement.flag);		
		String rowOrdering		= getAttributeValue(efmImplConfig, XmlElement.row_ordering, XmlAttribute.value);
		String adjMethod		= getAttributeValue(efmImplConfig, XmlElement.adjacency_method, XmlAttribute.value);
		int maxThreads			= Integer.parseInt(getAttributeValue(efmImplConfig, XmlElement.maxthreads, XmlAttribute.value));
		Element elArithmetic	= XmlUtil.getRequiredSingleChildElement(efmNumConfig, XmlElement.arithmetic);
		Element elPrecision		= XmlUtil.getRequiredSingleChildElement(efmNumConfig, XmlElement.precision);
		Element elZero			= XmlUtil.getRequiredSingleChildElement(efmNumConfig, XmlElement.zero);
		int precision			= XmlElement.parsePrecision(elPrecision.attribute(XmlAttribute.value.getXmlName()));
		double dblZero			= XmlElement.parseZero(elZero.attribute(XmlAttribute.value.getXmlName()));
		Arithmetic arithmetic	= Arithmetic.parse(elArithmetic.attribute(XmlAttribute.value.getXmlName()));
		Normalize normalize		= Normalize.parse(efmOutputConfig.attribute(XmlAttribute.normalize.getXmlName()));
		Generator generator		= Generator.parse(elGenerator.attribute(XmlAttribute.value.getXmlName()));
		String reacsToSuppress	= XmlUtil.getRequiredAttributeValue(elReacsToSup, XmlAttribute.value);
		String reacsToEnforce	= XmlUtil.getRequiredAttributeValue(elReacsToEnf, XmlAttribute.value);
		String reacsNoSplit		= XmlUtil.getRequiredAttributeValue(elReacsNoSplit, XmlAttribute.value);
		Zero zero				= Double.isNaN(dblZero) ? arithmetic.getDefaultZero() : new Zero(dblZero);
		File tmpDir				= new File(XmlUtil.getRequiredAttributeValue(elTmpDir, XmlAttribute.name));
		String strProgType		= XmlUtil.getRequiredAttributeValue(elProgress, XmlAttribute.type);
		String strProgPartition	= XmlUtil.getRequiredAttributeValue(elProgress, XmlAttribute.partition);
		String flag				= elFlag == null ? null : XmlUtil.getOptionalAttributeValue(elFlag, XmlAttribute.value, null);
		final int progPartition;
		try {
			progPartition = Integer.parseInt(strProgPartition);
		}
		catch (Exception ex) {
			throw new XmlConfigException("invalid value for progress partition, expected integer value, but found: " + strProgPartition, elProgress);
		}
		final ProgressType progType = ProgressType.parse(strProgType);

		MetabolicCompressionConfig cmpCfg = new MetabolicCompressionConfig(efmCompConfig);	
		final boolean				preprocessDuplicateGenes;
		final CompressionMethod[]	cmpMethods;
		try {
			preprocessDuplicateGenes	= cmpCfg.getPreprocessDuplicateGenes();
			cmpMethods					= cmpCfg.getCompressionMethods();
		}
		catch (ch.javasoft.metabolic.compress.config.XmlConfigException ex) {
			throw new XmlConfigException(ex.getMessage(), ex.getNode());
		}
		
		if (flag == null || (flag = flag.trim()).length() == 0) flag = null;		
		final DistributedConfig distConfig = new DistributedConfig(efmDistConfig);
		
		boolean selfTest	= Boolean.parseBoolean(getAttributeValue(efmImplConfig, XmlElement.self_test, XmlAttribute.value));
		boolean parseOnly	= Boolean.parseBoolean(getAttributeValue(efmImplConfig, XmlElement.parse_only, XmlAttribute.value));
		return new Config(
			xmlConfig, zero, adjMethod, rowOrdering, cmpMethods, 
			preprocessDuplicateGenes, selfTest, parseOnly, maxThreads, 
			arithmetic, precision, generator, normalize, 
			reacsToSuppress, reacsToEnforce, reacsNoSplit, 
			tmpDir, progPartition, progType, flag, distConfig);
	}
	
	public XmlConfig getXmlConfig() throws IOException {
		try {
            if (mXmlConfig != null) {
			    return mXmlConfig;
//	            if (mXmlConfig == null) {
//				return XmlConfig.getXmlConfig(new File(CalculateFluxModes.CONFIG_FILE));
			}
			return resolveXmlConfig(getAsArgs());
		} 
		catch (DocumentException e) {
			throw ExceptionUtil.toIOException(e);
		} 
		catch (XmlConfigException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}
	
	/**
	 * Same as {@link #parseXmlConfig(String[])}, but resolving is forced, and
	 * potential resolve errors are displayed on the system error stream.
	 */
	public static XmlConfig resolveXmlConfig(String[] args) throws FileNotFoundException, DocumentException, XmlConfigException {
		final XmlConfig xmlConfig = parseXmlConfig(args);
		try {
			xmlConfig.getDefaultConfigDocument();
		} 
		catch (XmlArgException ex) {
			if (ex.isOption()) {
				System.err.println("ERROR:   missing " + ex.getOptionWithIndex() + " option");
			}
			System.err.println("DETAILS: " + ex.getLocalizedMessage());
			ex.printStackTrace();//NOTE remove this?
			System.err.println("use --help option to display help message");
			throw ex;
		}
		catch (MissingReferableException ex) {
			if (("config[" + DEFAULT_CONFIG_NAME + "]/metabolic-parse/parse").equals(ex.getPath())) {
				System.err.println("ERROR:   invalid input kind option '" + ex.getReferable() + "'");
			}
			System.err.println("DETAILS: " + ex.getLocalizedMessage());
			System.err.println("use --help option to display help message");
			throw ex;
		}
		catch (XmlConfigException ex) {
			throw ex;
		}
		return xmlConfig;		
	}
	/**
	 * Parses the config, but does not resolve it, that is, all options 
	 * specified by {@code args} are not replaced yet in the configuration file. 
	 * However, default config, application name and the specified arguments are 
	 * stored in the returned xml config instance. 
	 */
	public static XmlConfig parseXmlConfig(String[] args) throws FileNotFoundException, DocumentException {
		final InputStream in;
		String config = DEFAULT_CONFIG_NAME;
		if (args.length >= 1 && !args[0].startsWith("-")) {
//			System.out.println("using config file: " + args[0]);
			in = new FileInputStream(args[0]);
			if (args.length >= 2 && !args[1].startsWith("-")) {
				config = args[1];
//				System.out.println("using config entry: " + args[1]);
			}
		}
		else {
			final File file = new File(CONFIG_FILE);
			if (file.canRead()) {
//				System.out.println("using config file: " + file.getAbsolutePath());
				in = new FileInputStream(CONFIG_FILE);
			}
			else {
//				System.out.println("using package config file: " + "/" + CONFIG_FILE);
				in = Config.class.getResourceAsStream("/" + CONFIG_FILE); 
			}
			if (in == null) throw new FileNotFoundException("/" + CONFIG_FILE);
		}
		final XmlConfig xmlConfig = XmlConfig.getXmlConfig(in, args);
		xmlConfig.setAppName(DEFAULT_APP_NAME);
		if (config != null) {
			xmlConfig.setDefaultConfigName(config);
		}
		return xmlConfig;
	}

	public static void printVersion(PrintStream stream, XmlConfig xmlConfig) throws XmlConfigException {
		xmlConfig.printUsage(stream, VERSION_NAME);
	}
	public static void printHelp(PrintStream stream, XmlConfig xmlConfig) throws XmlConfigException {
		xmlConfig.printUsage(stream, HELP_NAME);
	}
	public static void printUsage(PrintStream stream, XmlConfig xmlConfig) throws XmlConfigException {
		xmlConfig.printUsage(stream, USAGE_NAME);
	}

	private String[] getAsArgs() {
		return new String[] {
			"-rowordering", mRowOrdering,
			"-adjacency-method", mAdjMethod,
			"-maxthreads", String.valueOf(mMaxThreads),
			"-arithmetic", mArithmetic.getNiceName(),
			"-precision", String.valueOf(mPrecision),
			"-zero", String.valueOf(mZero.mZeroPos),
			"-normalize", mNormalize.name(),
			"-generator", mGenerator.name(),
			"-tmpdir", mTempDir.getBaseDir().getAbsolutePath(),
			"-level", Loggers.getRootLogger().getLevel().getName(),
			
			//we don't know about:
			"-compression", "default",
			"-kind", "built-in", 
			"-in", "unknown", "unknown",
			"-out", "count"
		};
	}

	/**
	 * Writes the contents of this config to the specified file.
	 * <p>
	 * The config instance can be restored from this file by calling
	 * {@link #readFrom(File)}.
	 *   
	 * @param file			the file to write to
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	public void writeTo(File file) throws IOException {
		final FileWriter writer = new FileWriter(file);
		try {
			writeTo(writer);
		}
		finally {
			writer.close();
		}
	}
	
	/**
	 * Restores the config instance from file, written by
	 * {@link #writeTo(File)}
	 * 
	 * @param file	the file containing the configuration information
	 * @return	the restored config instance
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	public static Config readFrom(File file) throws IOException {
		final FileReader reader = new FileReader(file);
		try {
			return Config.getFromXmlConfig(XmlConfig.fromXmlDocument(reader));
		}
		catch (XmlConfigException e) {
			throw ExceptionUtil.toIOException(e);
		} 
		catch (DocumentException e) {
			throw ExceptionUtil.toIOException(e);
		} 
		finally {
			reader.close();
		}
	}

	public void writeTo(OutputStream out) throws IOException {
		getXmlConfig().writeTo(out);
	}
	public void writeTo(Writer writer) throws IOException {
		getXmlConfig().writeTo(writer);
	}
	
	public static String getAttributeValue(Element efmImplConfig, XmlElement element, XmlAttribute attribute) throws XmlConfigException {
		Element el = XmlUtil.getRequiredSingleChildElement(efmImplConfig, element);
		return XmlUtil.getRequiredAttributeValue(el, attribute);
	}
	public int getMaxThreads() {
		return mMaxThreads;
	}
	public TempDir getTempDir() {
		return mTempDir;
	}
	
	public static Element getConfigMetabolicCompression(XmlConfig config) throws XmlConfigException {
		final Element defConfig = config.getDefaultConfigElement();
		return XmlUtil.getRequiredSingleChildElement(defConfig, XmlElement.metabolic_compression);		
	}
	public static Element getConfigMetabolicEfm(XmlConfig config) throws XmlConfigException {
		final Element defConfig = config.getDefaultConfigElement();
		return XmlUtil.getRequiredSingleChildElement(defConfig, XmlElement.metabolic_efm);		
	}
	public static Element getConfigEfmImpl(XmlConfig config) throws XmlConfigException {
		return XmlUtil.getRequiredSingleChildElement(getConfigMetabolicEfm(config), XmlElement.efm_impl);
	}
	public static Element getConfigEfmImplConfig(XmlConfig config) throws XmlConfigException {
		return XmlUtil.getRequiredSingleChildElement(getConfigEfmImpl(config), XmlElement.config);		
	}
	public static Element getConfigEfmOutput(XmlConfig config) throws XmlConfigException {
		return XmlUtil.getRequiredSingleChildElement(getConfigMetabolicEfm(config), XmlElement.efm_output);
	}	
	
	/**
	 * Returns the ElementaryFluxMode implementation as configured in the
	 * given xml config
	 */
	@SuppressWarnings("unchecked")
	public static ElementaryFluxModes.Impl getEfmImpl(XmlConfig config, Element efmImplConfig) throws XmlConfigException {
		//impl
		String implClass 	= XmlUtil.getRequiredAttributeValue(efmImplConfig, XmlAttribute.class_);
		//model
		Element elModel		= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.model);
		String modelClass 	= XmlUtil.getRequiredAttributeValue(elModel, XmlAttribute.factory);
		//memory
		Element elMemory	= XmlUtil.getRequiredSingleChildElement(efmImplConfig, XmlElement.memory);
		String memoryClass 	= XmlUtil.getRequiredAttributeValue(elMemory, XmlAttribute.factory);
		
		try {
			Class<ElementaryFluxModes.Impl> clsImpl = (Class<ElementaryFluxModes.Impl>)Class.forName(implClass);
			
			Class<EfmModelFactory> clsModel = (Class<EfmModelFactory>)Class.forName(modelClass);
			Class<MemoryFactory> clsMemory = (Class<MemoryFactory>)Class.forName(memoryClass);
			
			Constructor<ElementaryFluxModes.Impl> cons = clsImpl.getConstructor(new Class[] {Config.class, EfmModelFactory.class, MemoryFactory.class});
			
			//instantiate arguments (the factories)
			EfmModelFactory facModel	= clsModel.newInstance();
			MemoryFactory facMemory		= clsMemory.newInstance();
			
			return cons.newInstance(new Object[] {Config.getFromXmlConfig(config), facModel, facMemory});
		}
		catch (Exception ex) {
			throw new XmlConfigException("cannot instantiate efm-impl class '" + implClass + "', e=" + ex, efmImplConfig, ex);
		}
	}
	/**
	 * Write all configuration settings to the log using the given log level
	 */
	public void log(Logger logger, Level level) {
		if (Loggers.isLoggable(logger, level)) {
			logger.log(level, "Config:");
			logger.log(level, "..generator        : " + getGenerator());
			logger.log(level, "..adj method       : " + getAdjMethod());
			logger.log(level, "..row ordering     : " + getRowOrdering());
			logger.log(level, "..arithmetic       : " + getArithmetic() + " (prec: " + getPrecision() + " / zero: " + zero().mZeroPos + ")");
			logger.log(level, "..compression      : " + (compressNetwork(true) ? "on" : "off"));
			logger.log(level, "..compr. methods   : " + Arrays.toString(getCompressionMethods(true)));
			logger.log(level, "..normalize        : " + getNormalize());
			logger.log(level, "..max threads      : " + getMaxThreads());
			logger.log(level, "..self test        : " + (selfTest() ? "on" : "off"));
			logger.log(level, "..progress type    : " + getProgressType());			
			logger.log(level, "..progress part.   : " + getProgressPartition());			
			logger.log(level, "..suppress         : " + getReactionsToSuppress());
			logger.log(level, "..enforce          : " + getReactionsToEnforce());
			logger.log(level, "..nosplit          : " + getReactionsNoSplit());
			logger.log(level, "..temp dir         : " + getTempDir());
			logger.log(level, "..flag             : " + (getFlag() == null ? "(none)" : getFlag()));
		}
		getDistributedConfig().log(logger, level);
		if (getArithmetic().isExact() && !getNormalize().isExact()) {
			logger.warning("using exact arithmetic [" + getArithmetic() + "], but normalization [" + getNormalize() + "] might cause truncation");
		}
	}
	
	/**
	 * Traces the given (command line) arguments using the specified stream
	 * 
	 * @param print		the stream for tracing
	 * @param prefix	the prefix to use for the log line
	 * @param args		the (command line) arguments to trace
	 */
	public static void traceArgs(PrintStream print, String prefix, String[] args) {
		print.println(traceArgsString(prefix, args));
		
	}
	/**
	 * Traces the given (command line) arguments using the specified logger and 
	 * level
	 * 
	 * @param logger	the logger for tracing
	 * @param level		the log level to use for tracing
	 * @param prefix	the prefix to use for the log line
	 * @param args		the (command line) arguments to trace
	 */
	public static void traceArgs(Logger logger, Level level, String prefix, String[] args) {
		logger.log(level, traceArgsString(prefix, args));
	}
	/**
	 * Returns the given arguments as a command-line-like string
	 * 
	 * @param prefix	the prefix to use for the command line
	 * @param args		the (command line) arguments to trace
	 */
	public static String traceArgsString(String prefix, String[] args) {
		final StringBuilder sb = new StringBuilder(prefix);
		for (int i = 0; i < args.length; i++) {
			if (i > 0) sb.append(' ');
			sb.append(args[i]);
		}
		return sb.toString();
	}
}
