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
package ch.javasoft.metabolic.efm.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import ch.javasoft.math.NumberOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compress.CompressedMetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.nullspace.CannotReconstructFluxException;
import ch.javasoft.metabolic.efm.output.CallbackGranularity;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.util.ColumnUtil;
import ch.javasoft.util.ExceptionUtil;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.logging.LogFragmenter;

/**
 * The <code>AbstractColumnToFluxDistributionConverter</code> implements methods
 * which are common to most implementations.
 */
abstract public class AbstractColumnToFluxDistributionConverter<N extends Number, Col extends Column> implements ColumnToFluxDistributionConverter<N, Col> {
	
	private static final Logger LOG = LogPkg.LOGGER;

	protected final ColumnHome<N, Col> columnHome;
	
	public AbstractColumnToFluxDistributionConverter(ColumnHome<N, Col> columnHome) {
		this.columnHome = columnHome;
	}
	
	private static class WorkerThread extends Thread {
		private final AtomicReference<IOException> exception;
		private final BlockingQueue<Runnable> jobs;
		
		public WorkerThread(BlockingQueue<Runnable> jobs, AtomicReference<IOException> exception) {
			this.jobs 		= jobs;
			this.exception	= exception;
		}
		
		private volatile boolean endOfQueue = false;
		public void setEndOfQueue() {
			endOfQueue = true;
		}
		@Override
		public void run() {
			while (true) {
				try {
					final Runnable runnable = jobs.poll();
					if (runnable == null) {
						if (endOfQueue) {
							return;
						}
						yield();
					}
					else {
						runnable.run();
					}
				}
				catch (Exception e) {
					exception.compareAndSet(null, ExceptionUtil.toIOException(e));
				}
			}
		}
	}
	
    /**
     * Writes the columns to the specified callback, including uncompression of
     * flux modes and appending of external-to-external modes. The method uses
     * multiple threads to perform this operation.
     */
	public void writeColumnsToCallback(final Config config, final NetworkEfmModel model, final Iterable<Col> columns, final EfmOutputCallback callback) throws IOException {
		final long efmCount = writeE2eReactionsToCallback(config, model, columns, callback);

		int logcnt = 1000;
		final int offset = (int)(efmCount - ColumnUtil.getColumnCount(columns));
		final LogFragmenter log;
		if (callback.allowLoggingDuringOutput()) {
			log = new LogFragmenter(LOG);
			log.fineStart("uncompressing efms: ");
		}
		else {
			log = null;
		}
		final AtomicReference<IOException> exception = new AtomicReference<IOException>(); 
		final int threadCnt = Math.min(Runtime.getRuntime().availableProcessors(), config.getMaxThreads());
		final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(Math.max(threadCnt*2, 256));
		final WorkerThread[] threads = new WorkerThread[threadCnt];
		for (int i = 0; i < threadCnt; i++) {
			final WorkerThread thread = new WorkerThread(queue, exception);
			threads[i] = thread;
			thread.start();
		}
		int index = 0;
		for (final Col col : columns) {
			index++;
			final int curindex = index + offset;
			if (curindex % logcnt == 0) {
				if (log != null) log.append(curindex + "...");
				if (curindex % (10*logcnt) == 0) logcnt *= 10;
			}
			final Runnable runnable = new Runnable() {
				public void run() {
					FluxDistribution dist;
					try {
						dist = createFluxDistributionFromColumn(log, config, model, col, callback.getGranularity());
					}
					catch (CannotReconstructFluxException ex) {
						//log already ended at createFluxDistributionFromColumn()
//						LOG.warning(ex + " at mode " + curindex + " of " + efmCount);
//						LOG.warning("writing a NaN vector to continue");
//						double[] vals = new double[col.totalSize()];
//						Arrays.fill(vals, Double.NaN);
//						dist = new DefaultFluxDistribution(model.getMetabolicNetwork(), vals);
//						if (log != null) log.fineStart("... uncompressing ...");
						throw ex;
					} 
					writeFluxDistributionToCallback(model.getMetabolicNetwork(), config, dist, efmCount, callback);
				}
			};
			try {
				queue.put(runnable);
			} 
			catch (InterruptedException e) {
				throw ExceptionUtil.toIOException(e);
			}
			if (exception.get() != null) {
				queue.clear();
				break;
			}
		}			
		for (final WorkerThread thread : threads) {
			thread.setEndOfQueue();			
		}
		for (final Thread thread : threads) {
			try {
				thread.join();
			} 
			catch (InterruptedException e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
		if (log != null && log.isStarted()) log.end(efmCount + " done.");
		if (exception.get() != null) {
			throw exception.get();
		}
	}
	
	/**
	 * For every external-to-external (e2e) reactions (a reactions which is 
	 * uptake and excretion reaction at the same time), we add an EFM. This EFM 
	 * has flux value one for the e2e reaction, zero for all other reactions. If 
	 * the 2e2 reaction is reversible, another EFM with -1 at this reaction is
	 * added. 
	 * 
	 * @return the total number of EFMs, including the e2e EFMs already written
	 */
	protected long writeE2eReactionsToCallback(Config config, NetworkEfmModel model, Iterable<Col> columns, EfmOutputCallback callback) throws IOException {
		final MetabolicNetwork metaNet = model.getMetabolicNetwork();
		long efmCount = ColumnUtil.getColumnCount(columns);
		final NumberOperations<N> numberOps = columnHome.getNumberOperations();
		//first, count only
		final ArrayIterable<? extends Reaction> reacts = metaNet.getReactions();
		for (int ii = 0; ii < reacts.length(); ii++) {
			Reaction reac = reacts.get(ii);
			if (reac.isUptake() && reac.isExtract()) {
				efmCount++;
				if (reac.getConstraints().isReversible()) {
					efmCount++;
				}
			}
		}
		//now, create and write to callback
		for (int ii = 0; ii < reacts.length(); ii++) {
			Reaction reac = reacts.get(ii);
			if (reac.isUptake() && reac.isExtract()) {
				final N[] rates = numberOps.newArray(reacts.length());
				Arrays.fill(rates, numberOps.zero());
				rates[ii] = numberOps.one();
				writeFluxDistributionToCallback(metaNet, config, columnHome.createFluxDistribution(metaNet, rates), efmCount, callback);
				if (reac.getConstraints().isReversible()) {
					final N[] ratesRev = rates.clone();
					ratesRev[ii] = numberOps.negate(numberOps.one());
					writeFluxDistributionToCallback(metaNet, config, columnHome.createFluxDistribution(metaNet, ratesRev), efmCount, callback);
				}
			}
		}
		return efmCount;
	}
	
	/**
	 * Uncompresses the flux distribution, normalizes it (if necessary) and 
	 * writes it to the given callback. Note that this method is called from
	 * multiple threads, thus, the actual call to the 
	 * {@link EfmOutputCallback callback} is synchronized.
	 */
	protected void writeFluxDistributionToCallback(final MetabolicNetwork metaNet, Config config, FluxDistribution dist, long efmCount, EfmOutputCallback callback) {		
		boolean doNormalize = true;
		if (callback.getGranularity().isUncompressionNeeded() && metaNet instanceof CompressedMetabolicNetwork) {
			dist = ((CompressedMetabolicNetwork)metaNet).uncompressFluxDistribution(dist);
			
			if (callback.getGranularity() == CallbackGranularity.BinaryUncompressed || callback.getGranularity() == CallbackGranularity.SignUncompressed) {
				//renormalize the flux values to 0/1/-1
				for (int i = 0; i < dist.getSize(); i++) {
					dist.setRate(i, Integer.valueOf(dist.getRateSignum(i)));
				}
				doNormalize = false;
			}
		}
		if (doNormalize) {
			dist.norm(config.getNormalize().norm, config.zero());
		}
		if (callback.isThreadSafe()) {
			callback.callback(new EfmOutputEvent(metaNet, dist, efmCount));
		}
		else {
			synchronized(callback) {
				callback.callback(new EfmOutputEvent(metaNet, dist, efmCount));
			}
		}
	}

	/**
	 * Creates a numeric flux distribution from a binary column. The nullspace
	 * is computed to do so. Furthermore, duplicated reversible reactions are
	 * compressed again.
	 * <p>
	 * Deletages to {@link #createFluxDistributionFromColumn(LogFragmenter, Config, NetworkEfmModel, Column, CallbackGranularity)}
	 * with <code>null</code> as log fragmenter.
	 */
	public FluxDistribution createFluxDistributionFromColumn(Config config, NetworkEfmModel model, Col column) {
		return createFluxDistributionFromColumn(null, config, model, column, CallbackGranularity.DoubleUncompressed);
	}

	abstract protected FluxDistribution createFluxDistributionFromColumn(LogFragmenter log, Config config, NetworkEfmModel model, Col column, CallbackGranularity granularity);

}
