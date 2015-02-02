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
package ch.javasoft.metabolic.efm.dist.impl.file;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.dist.DistributedAdjEnum;
import ch.javasoft.metabolic.efm.dist.DistributedInfo;
import ch.javasoft.metabolic.efm.dist.impl.DistClient;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.ModelPersister;
import ch.javasoft.metabolic.efm.progress.IntProgressAggregator;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.logging.LogWriter;
import ch.javasoft.util.logging.Loggers;

/**
 * The <code>FileBasedDistJob</code> contains represents a file based 
 * distributed job; the class also contains the main methods called if executed
 * in a separate process or thread.
 */
public class FileBasedDistJob {
	
	/**
	 * Returns the host name of the local host
	 * @see InetAddress#getLocalHost()
	 * @see InetAddress#getHostName()
	 */
	private static final String getHostName() throws IOException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	/**
	 * Main method, invoked by the new jvm process. Initializes the logger and
	 * calls {@link #main(String[], Logger)}.
	 * 
	 * @param args	the expected {@link Args command line arguments}
	 */
	public static void main(String[] args) {
		final String strTmpDir 		= Args.TempDir.getFromArgs(args);
		final String strLogLevel	= Args.LogLevel.getFromArgs(args);
		
		try {
			final File tmpDir 	= new File(strTmpDir);
			final File logFile 	= new File(tmpDir, getHostName() + ".log");
			final Level logLevel = Level.parse(strLogLevel);
			Loggers.logToFile(logFile, logLevel);
			final Logger logger = Loggers.getLogger(FileBasedDistJob.class.getSimpleName());
			main(args, logger);
		} 
		catch (Exception e) {
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/**
	 * Main method with logger already initialized. Invoked by a new jvm process
	 * from the {@link #main(String[])} method, or directly for jobs executed in
	 * the current jvm in a new thread.
	 * 
	 * @param args	the expected {@link Args command line arguments}
	 */
	@SuppressWarnings("unchecked")
	/*package*/ static void main(String[] args, Logger logger) throws Exception {
		final String strTmpDir 			= Args.TempDir.getFromArgs(args);
		final String strConfigFile 		= Args.ConfigFile.getFromArgs(args);
		final String strStoichFile 		= Args.StoichFile.getFromArgs(args);
		final String strEfmModelFile 	= Args.EfmModelFile.getFromArgs(args);
		final String strAdjModelFile	= Args.AdjEnumModelFile.getFromArgs(args);
		final String strMasterHost 		= Args.MasterHost.getFromArgs(args);
		final int nodeIndex		= Args.NodeIndex.getIntFromArgs(args);
		final int masterPort 	= Args.MasterPort.getIntFromArgs(args);
		
		final File tmpDir 		= new File(strTmpDir);
		final File cfgFile		= new File(tmpDir, strConfigFile);
		final File stoichFile	= new File(tmpDir, strStoichFile);
		final File efmFile		= new File(tmpDir, strEfmModelFile);
		final File adjFile		= new File(tmpDir, strAdjModelFile);
		
		final Config config = Config.readFrom(cfgFile);
		final DistributedAdjEnum delegate = config.getAdjMethodFactory().createDistributedAdjEnumFromConfig();
		config.getTempDir().mkdirPersonalized(tmpDir.getName(), true /*allowExists*/);
		config.log(logger, Level.FINER);

		final ModelPersister persister = delegate.getModelPersister();
		final ReadableMatrix stoich = persister.readStoich(stoichFile);
		final EfmModel efmModel = persister.readEfmModel(config, stoich, efmFile);
		final ColumnHome<?, ?> columnHome = efmModel.getArithmetic().getColumnHome();
		
		long id = Thread.currentThread().getId();
		logger.fine("thread: " + id);
		logger.fine("host: " + getHostName());
		logger.fine("args: " + Arrays.toString(args));
		efmModel.log(columnHome, logger);
		if (Loggers.isLoggable(logger, Level.FINEST)) {
			logger.finest("stoich: ");
			efmModel.getStoichRational().writeToMultiline(new LogWriter(logger, Level.FINEST));
		}

		final DistClient client = new DistClient(columnHome, persister, adjFile, strMasterHost, masterPort);
		final AdjEnumModel adjModel = client.getAdjEnumModel();
		
		final DistributedInfo distInfo = new DistributedInfo(efmModel.getConfig().getDistributedConfig(), nodeIndex);
		final ProgressAggregator prog = new IntProgressAggregator(client.getProgressAggregator());
		delegate.execDistributed(columnHome, efmModel.getConfig(), efmModel, adjModel, distInfo, client, prog);

	}
	
}
