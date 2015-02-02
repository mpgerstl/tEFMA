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
package ch.javasoft.metabolic.efm.borndie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.efm.borndie.debug.Debugger;
import ch.javasoft.metabolic.efm.borndie.debug.NullDebugger;
import ch.javasoft.metabolic.efm.borndie.job.JobFailedException;
import ch.javasoft.metabolic.efm.borndie.job.JobManager;
import ch.javasoft.metabolic.efm.borndie.job.PairingJob;
import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.matrix.ConcurrentBornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.memory.ColumnDemuxAppendableMemory;
import ch.javasoft.metabolic.efm.borndie.model.BornDieEfmModel;
import ch.javasoft.metabolic.efm.borndie.range.DefaultCellRange;
import ch.javasoft.metabolic.efm.borndie.range.LowerTriangularMatrix;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.ComposedIterableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.util.logging.Loggers;

/**
 * The <code>BornDieController</code> controls the born/die algorithm. After
 * instantiating a controller, the algorithm is 
 * {@link #start(IterableMemory) started} by specifying the initial columns. 
 * Multiple threads might be used for the calculation, thus the initiating 
 * thread must {@link #awaitTermination() await termination} to get the result.
 */
public class BornDieController<Col extends Column> {
	
	private final Logger LOG = LogPkg.LOGGER;
	
	private final Debugger						debugger;
	
	private final ColumnHome<?, Col> 			columnHome; 
	private final Config						config; 
	private final BornDieEfmModel 				model;
	private final MemoryFactory 				memoryFactory;
	private final BornDieMatrix<Col> 			matrix;
	private final JobManager<Col> 				jobManager;
	private final AtomicReference<Exception> 	exception;
	private final CyclicBarrier					barrier;
	private final AtomicLong					time;
	
	public BornDieController(ColumnHome<?, Col> columnHome, Config config, BornDieEfmModel model, MemoryFactory memoryFactory) throws IOException {
		final LowerTriangularMatrix tril = new LowerTriangularMatrix(model.getIterationCount() + 1);
		this.columnHome		= columnHome;
		this.config			= config;
		this.debugger		= NullDebugger.INSTANCE;
//		this.debugger		= new SwingDebugger(this, tril);
		this.model			= model;
		this.memoryFactory	= memoryFactory; 
		this.matrix 		= new ConcurrentBornDieMatrix<Col>(this, tril);
		this.jobManager		= new JobManager<Col>(this);
//		this.jobManager		= new JobManager<Col>(this, 1024);
		this.exception		= new AtomicReference<Exception>();
		this.barrier		= new CyclicBarrier(2);
		time				= new AtomicLong(System.currentTimeMillis());
	}
	public ColumnHome<?, Col> getColumnHome() {
		return columnHome;
	}
	public Config getConfig() {
		return config;
	}
	public NetworkEfmModel getModel() {
		return model;
	}
	/**
	 * @return the memory factory
	 */
	public MemoryFactory getMemoryFactory() {
		return memoryFactory;
	}
	public BornDieMatrix<Col> getMatrix() {
		return matrix;
	}
	public int getIterationCount() {
		return model.getIterationCount();
	}
	
	/**
	 * Adds the pairing job to the job queue. Might block the caller if the job
	 * queue has limited capacity.
	 * 
	 * @param job	the job to add to the queue
	 */
	public void addPairingJob(PairingJob<Col> job) {
		try {
			jobManager.addJob(job);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Start the born/die algorithm. The algorithm might use multiple threads
	 * and does not block. Use {@link #awaitTermination()} to wait until the
	 * calculation is complete.
	 *  
	 * @param initialColumns	the initial columns from the kernel matrix
	 * @throws IOException	if a i/o exception occurs
	 */
	public void start(IterableMemory<Col> initialColumns) throws IOException {
		final ColumnDemuxAppendableMemory<Col> memory = new ColumnDemuxAppendableMemory<Col>(this, 0);
		for (Col col : initialColumns) {
			memory.appendColumn(col);
		}
		matrix.notifyInitialColumnComplete();
	}
	
	/**
	 * Terminates the algorithm
	 */
	public void terminate() throws IOException {
		try {
			barrier.await();
			final long now 	= System.currentTimeMillis();
			final long then	= time.getAndSet(now);
			final LowerTriangularMatrix tril = matrix.getMatrixRange();
			final int lastCol = tril.getColumnCount() - 1;
			final int modeCount = matrix.getColumnCount(lastCol, tril.getRowTo(lastCol) - 1);
			LOG.info("column " + lastCol + " of [0.." + lastCol + "] complete, " + 
					modeCount + " new feasible modes, dt=" + (now - then) + "ms.");				
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	/**
	 * Blocks the calling thread until the born/die algorithm completes and
	 * returns the memory with the final modes.
	 * 
	 * @return the final modes, the algorithm's result
	 * @throws IOException 				if an I/O exception occurred
	 * @throws IllegalStateException 	if waiting was interrupted
	 */
	public IterableMemory<Col> awaitTermination() throws IOException {
		try {
			barrier.await();
			jobManager.terminate();
		} 
		catch (RuntimeException e) {
			if (debugger.doDebug()) debugger.notifyException(e);
			throw e;
		} 
		catch (IOException e) {
			if (debugger.doDebug()) debugger.notifyException(e);
			throw e;
		}
		catch (Exception e) {
			if (debugger.doDebug()) debugger.notifyException(e);
			throw new RuntimeException(e);
		}
		//collect columns in last matrix row
		final int len = matrix.getMatrixRange().getLength();
        final List<IterableMemory<Col>> lastRow = new ArrayList<IterableMemory<Col>>(len);
        for (int i = 0; i < len; i++) {
			lastRow.add(matrix.getFinal(i));
		}
		final IterableMemory<Col> res = new ComposedIterableMemory<Col>(lastRow);
		debugger.notifyTerminate(jobManager, res);
		if (Loggers.isLoggable(LOG, Level.FINE)) {
			LOG.fine(jobManager.toString());
		}
		return res;
	}
	
	public void switchColumnToBearingStage(int bornColumn) throws IOException {
		final long now 	= System.currentTimeMillis();
		final long then	= time.getAndSet(now);
		final LowerTriangularMatrix tril = matrix.getMatrixRange();
		final int activeRows = Math.max(0, tril.getColumnHeight(bornColumn) - 2);		
		final int adjThreads = config.getMaxThreads() >>> Math.min(
			31 - Integer.numberOfLeadingZeros(config.getMaxThreads()), activeRows);
		model.setAdjEnumThreads(Math.max(1, adjThreads));
		final int rowFrom	= tril.getRowFrom(bornColumn);
		final int rowTo		= tril.getRowTo(bornColumn);
		int modeCountTotal = 0;
		for (int r = rowFrom; r < rowTo; r++) {
			final int modeCount = matrix.getColumnCount(bornColumn, r);
			modeCountTotal += modeCount;
			if (tril.isFinalRow(r)) {
				LOG.info("column " + bornColumn + " of [0.." + 
					(tril.getColumnCount() - 1) + "] now bearing, with " +
					model.getAdjEnumThreads() + " threads and " + 
					modeCountTotal + " modes, " + modeCount + " new feasible, dt=" + (now - then) + "ms.");				
			}
			else {
				if (Loggers.isLoggable(LOG, Level.FINEST)) {
					LOG.finer("cell " + new DefaultCellRange(bornColumn, r) + " has collected all modes: " + matrix.getColumnCount(bornColumn, r));
				}
			}
		}
		
		for (int r = rowFrom; r < rowTo; r++) {
			if (!tril.isFinalRow(r)) {
				matrix.schedulePairingJobs(bornColumn, r, 0, bornColumn + 1);
			}
		}
		
		for (int c = 0; c < bornColumn; c++) {
			final int rFrom = tril.getRowFrom(bornColumn);
			final int rTo	= tril.getRowTo(c);
			for (int r = rFrom; r < rTo; r++) {
				if (!tril.isFinalRow(r)) {
					matrix.schedulePairingJobs(c, r, bornColumn, bornColumn + 1);
				}
			}
		}
	}
	
	/**
	 * Handle an exception which occurred in a pairing job executed in its own
	 * thread. If it was the first exception, it is kept and rethrown after 
	 * terminating the algorithm.
	 * 
	 * @param job	the job that caused the exception
	 * @param e		the exception which occurred
	 */
	public void handleJobException(PairingJob<Col> job, Exception e) {
		handleCommandException(new JobFailedException(job, e));
	}
	/**
	 * Handle an exception which occurred in a command executed in its own 
	 * thread. If it was the first exception, it is kept and rethrown after 
	 * terminating the algorithm.
	 * 
	 * @param e		the exception which occurred
	 */
	public void handleCommandException(Exception e) {
		exception.compareAndSet(null, e);
		try {
			terminate();
		}
		catch (IOException inner) {
			//ignore
		}		
	}
	
	/**
	 * Returns the exception, if one has been set, or {@code null} otherwise.
	 */
	public Exception getException() {
		return exception.get();
	}

	public Debugger getDebugger() {
		return debugger;
	}
	
}
