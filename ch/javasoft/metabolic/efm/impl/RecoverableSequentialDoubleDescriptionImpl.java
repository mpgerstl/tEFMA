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
package ch.javasoft.metabolic.efm.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.job.ExecJob;
import ch.javasoft.job.ExecJobMonitor;
import ch.javasoft.metabolic.efm.adj.AdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.memory.outcore.OutOfCoreMemory;
import ch.javasoft.metabolic.efm.memory.outcore.OutOfCoreMemoryFactory;
import ch.javasoft.metabolic.efm.memory.outcore.Recovery;
import ch.javasoft.metabolic.efm.memory.outcore.OutOfCoreMemory.FileId;
import ch.javasoft.metabolic.efm.memory.outcore.OutOfCoreMemory.FileName;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.DefaultIterationStepModel;
import ch.javasoft.metabolic.efm.model.EfmModelFactory;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.util.ColumnUtil;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.logging.Loggers;

/**
 * Similar to the {@link SequentialDoubleDescriptionImpl}, but adds functionality
 * especially suited for out-of-core computations. Incomplete computations can
 * be recovered from intermediary files.
 * 
 * TODO add description of recovery stuff
 */
public class RecoverableSequentialDoubleDescriptionImpl extends AbstractDoubleDescriptionImpl {

    private static final Logger LOG = LogPkg.LOGGER;
    
    private final ThreadLocal<Boolean> skipModeFiltering = new ThreadLocal<Boolean>();

    /**
	 * Constructor with config access and the two factories for model and 
	 * memory. Note that most factories have default constructors without 
	 * arguments, and can thus be defined in the configuration file.
	 * <p> 
	 * <b>Note: </b>Only out-of-core memory factories are appropriate, other
	 *              memory implementations might silently ignore the recovery
	 *              stuff or also lead to an exception.
	 */
	public RecoverableSequentialDoubleDescriptionImpl(Config config, EfmModelFactory modelFactory, MemoryFactory memoryFactory) {
        super(config, modelFactory, memoryFactory);
    }
	
    //iterate (standard or recover)
    @Override
	protected <N extends Number, Col extends Column> IterableMemory<Col> iterate(ColumnHome<N,Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> memory) throws IOException {
    	skipModeFiltering.set(Boolean.FALSE);
    	final String flag = getConfig().getFlag();
    	if (flag == null) {
    		return iterateUnpartitioned(columnHome, efmModel, memory, 0); 
    	}
    	else {
    		LOG.info("found flag: " + flag);
    		final Recovery recovery = Recovery.getRecovery(flag);
    		if (recovery == null) {
    			final String msg = "not a recovery flag: " + flag;
    			LOG.severe(msg);
    			throw new IllegalArgumentException(msg);
    		}
    		return iterateRecover(columnHome, efmModel, memory, recovery);
    	}
    }
    //the real iterate process
    private <N extends Number, Col extends Column> IterableMemory<Col> iterateUnpartitioned(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> memory, int iterationStart) throws IOException {
        final long tStart = System.currentTimeMillis();
        AppendableMemory<Col> pos = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iterationStart + 1, PartId.POS);
        AppendableMemory<Col> zer = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iterationStart + 1, PartId.ZER);
        AppendableMemory<Col> neg = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iterationStart + 1, PartId.NEG);

        final IterationStepModel itModel = new DefaultIterationStepModel(efmModel, 0);
        ColumnUtil.partition(columnHome, efmModel, memory, pos, zer, neg, itModel, false /*keep*/);

        return iteratePartitioned(columnHome, efmModel, pos, zer, neg, iterationStart, tStart);
    }
    private <N extends Number, Col extends Column> IterableMemory<Col> iteratePartitioned(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> pos, AppendableMemory<Col> zer, AppendableMemory<Col> neg, int iterationStart, long tStart) throws IOException {
        final AdjEnum adjEnum = getConfig().getAdjMethodFactory().createAdjEnumFromConfig();
        
        adjEnum.initialize(columnHome, getConfig(), efmModel);
        
        AppendableMemory<Col> memory = null;
    	final int itCount  = efmModel.getIterationCount();

    	int cntPos = pos.getColumnCount();
    	int cntZer = zer.getColumnCount();
    	int cntNeg = neg.getColumnCount();
    	
    	int iteration 	= iterationStart;
        long timeStart	= System.currentTimeMillis();
        long timeEnd    = timeStart;
    	int colCount 	= cntPos + cntZer + cntNeg;
    	
        while (colCount > 0 && iteration < itCount) {
        	
        	final String openFileCount = Loggers.isLoggable(LOG, Level.FINEST) ? getOpenFileCountString() : "" ;
            LOG.info(
                "iteration " + iteration + "/" + itCount + ": " + colCount + " modes, dt=" + (timeEnd - timeStart) + "ms." +
                "\t{ next " + (iteration+1) + "/" + itCount + ": " + ((long)cntPos) * ((long)cntNeg) + " adj candidates, " +
                		"[+/0/-] = [" + cntPos + "/" + cntZer + "/" + cntNeg + "] }" + openFileCount
            );
            iteration++;
            timeStart = System.currentTimeMillis();

            memory = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, null);

            //generate new rays from adjacent ray pairs
            final AdjEnumModel<Col> adjModel = new AdjEnumModel<Col>(efmModel, iteration, pos.toSortableMemory(), zer.toSortableMemory(), neg.toSortableMemory(), memory);
            if (cntPos > 0 && cntNeg > 0) {
            	adjEnum.adjacentPairs(columnHome, adjModel);
            }
            
            if (iteration < itCount) {
                final AppendableMemory<Col> npos = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration, PartId.POS);
                final AppendableMemory<Col> nzer = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration, PartId.ZER);
                final AppendableMemory<Col> nneg = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration, PartId.NEG);
                
                ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Positive, pos, npos, nzer, nneg, adjModel, true /*keep*/);
                ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Negative, neg, npos, nzer, nneg, adjModel, true /*keep*/);
                ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Zero, zer, npos, nzer, nneg, adjModel, true /*keep*/);
                ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Zero, memory, npos, nzer, nneg, adjModel, false /*keep*/);//the new columns
                
	            pos = npos;
	            zer = nzer;
	            neg = nneg;
	        	cntPos = pos.getColumnCount();
	        	cntZer = zer.getColumnCount();
	        	cntNeg = neg.getColumnCount();
            }
            else {
            	ColumnUtil.moveToOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Positive, pos, memory, adjModel, true /*keep*/);
            	ColumnUtil.moveToOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Zero, zer, memory, adjModel, true /*keep*/);
            	ColumnUtil.moveToOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Negative, neg, memory, adjModel, true /*keep*/);
            }
            
        	colCount = cntPos + cntZer + cntNeg;
            timeEnd = System.currentTimeMillis();
        }
        if (iteration < itCount) {
            LOG.info("iteration " + iteration + "/" + itCount + ": discontinued since no modes left.");            
        }
        else {
            LOG.info("iteration " + iteration + "/" + itCount + ": " + memory.getColumnCount() + " modes, dt=" + (timeEnd - timeStart) + "ms.");            
        }
        final long tEnd = System.currentTimeMillis();
        LOG.info("TIME iterate " + (tEnd - tStart) + "ms");
        return memory;
    }
    //the recovery process
    private <N extends Number, Col extends Column> IterableMemory<Col> iterateRecover(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> memory, Recovery recovery) throws IOException {
    	final File dataFolder = recovery.getRecoveryFolder(); 
    	if (!dataFolder.exists() || !dataFolder.isDirectory()) {
    		final String msg = "recover data folder does not exist or is not a directory: " + dataFolder.getAbsolutePath();
    		LOG.severe(msg);
    		throw new IOException(msg);
    	}
		LOG.info("starting recovery in data folder: " + dataFolder.getAbsolutePath());
		
		//reload original config, if it is present in tmp folder
		final File cfgFile = new File(dataFolder, OutOfCoreMemoryFactory.CONFIG_FILE_NAME);
		if (cfgFile.exists()) {
			try {
				//NOTE: there are other members of config which are not
				//      updated with the loaded config, e.g. in efmModel.
				//      this update here is thought that alternate output
				//      configurations are possible. all other config stuff
				//      should anyway be identical in the runtime and the 
				//		restored config
//				mConfig = Config.getFromXmlConfig(XmlConfig.fromXmlDocument(cfgFile));
				LOG.warning("config ignored: " + cfgFile.getAbsolutePath());
			}
			catch (Exception e) {
				throw ExceptionUtil.toRuntimeExceptionOr(IOException.class, e);
			}
		}
		else {
			LOG.info("no config file found, using runtime config");			
		}
    	
		//evaluate iteration step
        final int itCount = efmModel.getIterationCount();
    	int colCount = memory.getColumnCount();
		LOG.info("evaluating iteration status from recovery data:");			
		LOG.info("..iterations                           : " + itCount);
		LOG.info("..modes at step 0                      : " + colCount);
		
		
		//the further the better, try in reverse order:
		//  1) PartId.FLT 			--> memory already filtered, iteration phase already terminated
		//  2) PartId.POS/NEG/ZER 	--> memory already partitioned
		//  3) FileId.NORMAL		--> unpartitioned memory
		
		//  1) PartId.FLT 			--> memory already filtered, iteration phase already terminated
		final OutOfCoreMemory<Col> flt = recoverMemoryFile(columnHome, efmModel, dataFolder, PartId.FLT);
		if (flt != null) {
			if (recovery.isTreeRecovery()) {
				throw new IOException("recovery error: filtered memory found, but recover-tree option specified");
			}
			LOG.info("..recovered final filtered memory file : " + flt.mFile.getAbsolutePath());
			LOG.info("..number of modes                      : " + flt.getColumnCount());
			LOG.info("..last completed iteration             : " + flt.mIteration);
			if (flt.mIteration != itCount) {
				throw new IOException("recovery error: iteration " + flt.mIteration + " is not " + itCount);
			}
			if (flt.mNumericSize != 0) {
				throw new IOException("recovery error: numeric size is non-zero: " + flt.mNumericSize);
			}
			skipModeFiltering.set(Boolean.TRUE);
			return flt;
		}
        
		//  2) PartId.POS/NEG/ZER 	--> memory already partitioned
		final OutOfCoreMemory<Col> pos = recoverMemoryFile(columnHome, efmModel, dataFolder, PartId.POS);
		if (pos != null) {
			LOG.info("..partitioned memory, tree option      : " + (recovery.isTreeRecovery() ? "on" : "off"));
			LOG.info("..recovered pos memory file            : " + pos.mFile.getAbsolutePath());
			LOG.info("..number of pos modes                  : " + pos.getColumnCount());
			final OutOfCoreMemory<Col> zer = recoverMemoryFile(columnHome, efmModel, dataFolder, PartId.ZER);
			LOG.info("..recovered zer memory file            : " + zer.mFile.getAbsolutePath());
			LOG.info("..number of zer modes                  : " + zer.getColumnCount());
			final OutOfCoreMemory<Col> neg = recoverMemoryFile(columnHome, efmModel, dataFolder, PartId.NEG);
			LOG.info("..recovered neg memory file            : " + neg.mFile.getAbsolutePath());
			LOG.info("..number of neg modes                  : " + neg.getColumnCount());
			if (pos.mIteration != zer.mIteration || pos.mIteration != neg.mIteration) {
				throw new IOException("recovery error: pos/zer/neg iteration indices not matching");
			}
			if (pos.mIteration > itCount) {
				throw new IOException("recovery error: iteration index " + pos.mIteration + " to large");
			}
			if (pos.mBooleanSize != zer.mBooleanSize || pos.mBooleanSize != neg.mBooleanSize) {
				throw new IOException("recovery error: pos/zer/neg boolean sizes not matching");
			}
			if (pos.mNumericSize != zer.mNumericSize || pos.mNumericSize != neg.mNumericSize) {
				throw new IOException("recovery error: pos/zer/neg numeric sizes not matching");
			}
			LOG.info("..last completed iteration             : " + pos.mIteration);
			if (pos.mIteration < itCount) {
				LOG.info("restarting iteration at step " + (pos.mIteration + 1));
				return iteratePartitioned(columnHome, efmModel, pos, zer, neg, pos.mIteration, System.currentTimeMillis());
			}
			else {
				throw new IOException("iteration phase complete, but partitioned memories found");
			}
		}
		
		//  3) FileId.NORMAL		--> unpartitioned memory
		final OutOfCoreMemory<Col> mem = recoverMemoryFile(columnHome, efmModel, dataFolder, null);
		if (mem != null) {
			if (recovery.isTreeRecovery()) {
				throw new IOException("recovery error: unpartitioned memory found, but recover-tree option specified");
			}
			LOG.info("..recovered unpartitioned memory file  : " + mem.mFile.getAbsolutePath());
			if (mem.mIteration > itCount) {
				throw new IOException("recovery error: iteration index " + mem.mIteration + " to large");
			}
			LOG.info("..last completed iteration             : " + mem.mIteration);
			if (mem.mIteration < itCount) {
				LOG.info("restarting iteration at step " + (mem.mIteration + 1));
				return iterateUnpartitioned(columnHome, efmModel, memory, mem.mIteration);
			}
			else {
				LOG.info("iteration phase already completed.");
				return mem;
			}
		}
		
		// no recoverable memory files found 
		throw new IOException("recovery error: no recoverable memory files found");		
    }
    
	/**
	 * tries to recover a memory file of the specified type, or returns null if 
	 * this is not successful. If partId is null, a normal unpartitioned memory
	 * is recovered.
	 */
	private static <Col extends Column> OutOfCoreMemory<Col> recoverMemoryFile(ColumnHome<?, Col> columnHome, NetworkEfmModel efmModel, File dataFolder, PartId partId) {
		final FileName fileName = partId == null ? FileName.NORMAL : FileName.getPartFileName(partId);
		
		OutOfCoreMemory<Col> mem = null;
		for (final File file : dataFolder.listFiles()) {
			final int iteration = fileName.getIteration(file.getName()); 
			if (iteration >= 0 && (mem == null || iteration > mem.mIteration)) {
				final FileId<Col> fileId = fileName.getFileId(columnHome, dataFolder, iteration, efmModel.getNumericSize(iteration), efmModel.getBooleanSize(iteration), true);
				try {
					mem = new OutOfCoreMemory<Col>(fileId);
				}
				catch (Exception e) {
					LOG.severe("recovering memory file " + fileId.getFile().getAbsolutePath() + " failed, e=" + e);
				}
			}
		}
		return mem;
	}
	
	@Override
	protected <N extends Number, Col extends Column> IterableMemory<Col> filterModes(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, IterableMemory<Col> memory) throws IOException {
		if (skipModeFiltering.get().booleanValue()) {
			return memory;
		}
		return super.filterModes(columnHome, efmModel, memory);
	}
	
	private String getOpenFileCountString() {
//		final ExecJob job = new ExecJob("/usr/bin/lsof");
		final String cmd1 = "lsof -c java";
		final String cmd2 = "grep -c " + getConfig().getTempDir().getPersonalizedDir().getAbsolutePath().replace(File.separatorChar, '.');
		final ExecJob job1 = new ExecJob(cmd1);
		final ExecJob job2 = new ExecJob(cmd2);
		final ExecJobMonitor mon1 = job1.exec();
		final ExecJobMonitor mon2 = job2.exec();
		mon1.pipeTo(mon2, true);
		try {
			return ", " + mon2.waitForStdOutString().trim() + " open files";
		} 
		catch (Exception e) {
			return ", exec=" + cmd1 + " | " + cmd2 + ", exc=" + e;
		}
	}
    
}
