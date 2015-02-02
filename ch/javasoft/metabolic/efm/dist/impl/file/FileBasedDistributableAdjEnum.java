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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.dist.DistributedAdjEnum;
import ch.javasoft.metabolic.efm.dist.DistributedInfo;
import ch.javasoft.metabolic.efm.dist.PartIterator;
import ch.javasoft.metabolic.efm.dist.impl.DistClient;
import ch.javasoft.metabolic.efm.dist.impl.DistJobController;
import ch.javasoft.metabolic.efm.dist.impl.DistServer;
import ch.javasoft.metabolic.efm.dist.impl.DistributableAdjEnum;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.ModelPersister;

/**
 * The <tt>FileBasedDistributableAdjEnum</tt> uses {@link DistClient clients} 
 * and a master {@link DistServer server} to perform the adjacent ray 
 * enumeration. The real enumeration of adjacent modes is delegated to a 
 * {@link DistributedAdjEnum} instance. The data structures to the distributed
 * parts are exchanged via files, using {@link FileBasedDistJobController} and
 * {@link FileBasedDistJob}.
 * <p>
 * The distributed computation consists of the following steps:<br>
 * <ol>
 * 	<li>{@link #initialize(ColumnHome, Config, EfmModel, AdjEnumModel, int) Initialization}</li>
 * 	<ol>
 * 	<li>All information needed for the enumeration process are stored in 
 * 		files, such as configuration and iteration step information</li>
 * 	<li>The {@link DistServer server} is started, managing the jobs and 
 * 		collecting generated data from clients</li>
 * 	<li>The {@link DistJobController controller} is initialized. The jobs
 * 		returned by the controller first read configuration files and 
 * 		information for the enumeration process. They connect to the
 * 		server using a {@link DistClient} instance and request jobs from the 
 * 		server, sending new-born modes back to the server. The job itself is 
 * 		actually delegated to 
 * 		{@link DistributedAdjEnum#execDistributed(ColumnHome, Config, EfmModel, AdjEnumModel, DistributedInfo, PartIterator, ch.javasoft.metabolic.efm.progress.ProgressAggregator) DistributedAdjEnum.execDistributed(..)}
 * 		</li>
 * 	</ol>
 * 	<li>Running the jobs, created using the controller returned by the
 * 		{@link #initialize(ColumnHome, Config, EfmModel, AdjEnumModel, int) initialization}
 * 		method in the previous step.</li>
 * 	<li>Termination: if all jobs are assigned to a client, the next job request 
 * 		causes the requesting client to terminate. The server terminates if he 
 * 		has sent a <i>no job</i> answer to every client.</li>
 * </ol>
 */
public class FileBasedDistributableAdjEnum implements DistributableAdjEnum {
	
	public static final String NAME = "distributable-file";
	
	public String name() {
		return NAME;
	}
	
    protected File getConfigFile(File dir) {
        return new File(dir, "config.efm");
    }
    protected File getEfmModelFile(File dir) {
        return new File(dir, "efm-model.efm");
    }
    protected File getStoichFile(File dir) {
        return new File(dir, "stoich.bin");
    }
    protected File getAdjModelFile(File dir) {
		return new File(dir, name() + ".properties");
	}
    /**
	 * Performs the initialization process, as described for the initialization
	 * step in the {@link FileBasedDistributableAdjEnum class comments}, and
	 * returns the {@link FileBasedDistJobController} used to create jobs and
	 * to await completion or abort the computation.
     */
    public <Col extends Column, N extends Number> FileBasedDistJobController initialize(ColumnHome<N, Col> columnHome, Config config, EfmModel efmModel, AdjEnumModel<Col> adjModel, int nodeCount) throws IOException {
		try {
//			config.getAdjMethodFactory().createDistributedAdjEnumFromConfig();
			final File tmpDir = config.getTempDir().getPersonalizedDir();
			final DistributedAdjEnum delegate = config.getAdjMethodFactory().createDistributedAdjEnumFromConfig();
			
			delegate.execCentralized(columnHome, config, efmModel, adjModel);		

			final ModelPersister persister = delegate.getModelPersister();
			config.writeTo(getConfigFile(tmpDir));
			persister.writeStoich(efmModel.getStoichRational(), getStoichFile(tmpDir));
			persister.writeEfmModel(columnHome, efmModel, getEfmModelFile(tmpDir));
			persister.writeAdjEnumModel(columnHome, adjModel, getAdjModelFile(tmpDir));
			
			final DistServer<Col> server	= new DistServer<Col>(columnHome, config, adjModel, nodeCount);
            final String configFileName     = getConfigFile(tmpDir).getName();
            final String stoichFileName     = getStoichFile(tmpDir).getName();
            final String efmModelFileName   = getEfmModelFile(tmpDir).getName();
			final String adjModelFileName 	= getAdjModelFile(tmpDir).getName();
			final String masterHostName		= getHostName();
			final int masterPort			= server.getPort();

			server.start();
			return new FileBasedDistJobController(config, server, configFileName, stoichFileName, efmModelFileName, adjModelFileName, masterHostName, masterPort);
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final String getHostName() throws IOException {
		return InetAddress.getLocalHost().getHostName();
	}

}
