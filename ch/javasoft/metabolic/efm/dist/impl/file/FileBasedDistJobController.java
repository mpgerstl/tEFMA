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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.job.AbstractJob;
import ch.javasoft.job.ExecJob;
import ch.javasoft.job.Executable;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.config.DistributedConfig;
import ch.javasoft.metabolic.efm.dist.impl.DistJobController;
import ch.javasoft.metabolic.efm.dist.impl.DistServer;
import ch.javasoft.util.logging.LogPrintStream;
import ch.javasoft.util.logging.Loggers;

/**
 * The <code>FileBasedDistJobController</code> uses a file based configuration 
 * to initialize the jobs. Jobs returned are basically invoking one of the main 
 * method of {@link FileBasedDistJob}, either as an own process or in a separate 
 * thread.
 */
public class FileBasedDistJobController implements DistJobController {
	
	private final Config config;
	private final String configFileName;
	private final String stoichFileName;
	private final String efmModelFileName;
	private final String adjModelFileName;
	private final String masterHostName;
	private final int masterPort;	
	private final DistServer server;
	
	/**
	 * Constructor with file infos, and server which is already started
	 */
	public FileBasedDistJobController(Config config, DistServer server, String configFileName, String stoichFileName, String efmModelFileName, String adjModelFileName, String masterHostName, int masterPort) {
		this.config				= config;
		this.configFileName		= configFileName;
		this.stoichFileName		= stoichFileName;
		this.efmModelFileName	= efmModelFileName;
		this.adjModelFileName	= adjModelFileName;
		this.masterHostName		= masterHostName;
		this.masterPort			= masterPort;
		this.server				= server;
	}

	/**
	 * Composes the command array, as defined in the {@link DistributedConfig
	 * distributed configuration} for this job's node. 
	 * <p>
	 * The command in the distributed config can use the following place 
	 * holders, being replaced with the actual values before the command is 
	 * invoked:
	 * <ul>
	 * 	<li>[nodename]	the hostname of the node</li>
	 * 	<li>[workdir]	the current working directory, as returned by
	 * 					{@link System#getProperty(String)} for 
	 * 					<tt>user.dir</tt></li>
	 * 	<li>[classpath]	the class path to use, as returned by
	 * 					{@link RuntimeMXBean#getClassPath()}</li>
	 * 	<li>[class]		the fully qualified class name of <tt>FileBasedDistJobController</tt></li>
	 * 	<li>[vmargs]	the virtual machine arguments, as defined in the
	 * 					{@link DistributedConfig#getVmArgs() distributed config}
	 * 					for this node</li>
	 * 	<li>[args]		the arguments passed to the 
	 * 					{@link FileBasedDistJob#main(String[])} 
	 * 					method, as returned by {@link #getArgs(int)}</li>
	 * </ul>
	 * 
	 * @return 	the command array, split into individual strings using white 
	 * 			space as token separator
	 */
	private String[] getCommandArray(int nodeIndex) {
		final DistributedConfig distConfig = config.getDistributedConfig();
		//e.g.: /usr/bin/rsh [nodename] cd [workdir] ; /usr/bin/java -cp [classpath] [class] [vmargs] [args]"
		final String rawCommand = distConfig.getCommand();
		final String command = rawCommand
			.replace("[nodename]", distConfig.getNodeNames().get(nodeIndex))
			.replace("[workdir]", System.getProperty("user.dir"))
			.replace("[classpath]", ManagementFactory.getRuntimeMXBean().getClassPath())
			.replace("[class]", FileBasedDistJob.class.getName())
			.replace("[vmargs]", distConfig.getVmArgs().get(nodeIndex))
			.replace("[args]", getArgs(nodeIndex));
		return command.split("\\s+");
	}
	
	/**
	 * Composes the arguments as used by the 
	 * {@link FileBasedDistJob#main(String[])} method and
	 * defined by {@link Args}
	 * 
	 * @return	a string with the arguments passed to the main method
	 */
	private String getArgs(int nodeIndex) {
		final StringBuilder sb = new StringBuilder();
		sb.append(config.getTempDir().getPersonalizedDir().getAbsolutePath());
		sb.append(' ');
		sb.append(config.getDistributedConfig().getLogLevels().get(nodeIndex));
        sb.append(' ');
        sb.append(configFileName);
		sb.append(' ');
        sb.append(stoichFileName);
		sb.append(' ');
        sb.append(efmModelFileName);
		sb.append(' ');
		sb.append(adjModelFileName);
        sb.append(' ');
		sb.append(nodeIndex);
		sb.append(' ');
		sb.append(masterHostName);
		sb.append(' ');
		sb.append(masterPort);
		return sb.toString();
	}

	//inherit javadoc
	public ExecJob createExecJob(int nodeIndex) {
		return new ExecJob(getCommandArray(nodeIndex));
	}
	//inherit javadoc
	public Executable<Void> createExecutable(final int nodeIndex) {
		return new AbstractJob<Void>() {
			public Void run() throws Throwable {
				Logger logger = Loggers.getLogger(FileBasedDistJob.class.getSimpleName());
				logger.setLevel(Level.WARNING);
				FileBasedDistJob.main(getArgs(nodeIndex).split("\\s"), logger);
				return null;
			}		
		};
	}
	
	//inherit javadoc
	public void awaitCompletion() throws InterruptedException {
		server.awaitCompletion();
		abort();
	}
	
	//inherit javadoc
	public void abort() {
		try {
			server.close();
		}
		catch (Exception ex) {
			LogPkg.LOGGER.warning("could not close server, e=" + ex);
			ex.printStackTrace(new LogPrintStream(LogPkg.LOGGER, Level.WARNING));
		}
	}

}
