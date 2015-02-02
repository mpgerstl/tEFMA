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

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.acib.thermodynamic.Thermodynamic;
import at.acib.thermodynamic.ThermodynamicParameters;
import ch.javasoft.lang.management.JVMTimer;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.compress.CompressionUtil;
import ch.javasoft.metabolic.efm.ElementaryFluxModes;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.model.DefaultIterationStepModel;
import ch.javasoft.metabolic.efm.model.EfmModelFactory;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.output.CallbackGranularity;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.util.MatrixUtil;
import ch.javasoft.metabolic.efm.util.ReactionMapping;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.util.logging.LogPrintWriter;
import ch.javasoft.util.numeric.Zero;

/**
 * The <code>AbstractDoubleDescriptionImpl</code> is a common super class for
 * the double description algorithm. It is for instance used to compute
 * elementary flux modes.
 * <p>
 * This implementation works in 3 phases:
 * <ul>
 * <li><b>a {@link #preprocess(ColumnHome, MetabolicNetwork, EfmOutputCallback)
 * preprocess} phase: </b> network compression and creation of the initial
 * tableau (e.g. by computing the kernel matrix) are the main tasks in this
 * phase</li>
 * <li><b>an {@link #iterate(ColumnHome, NetworkEfmModel, AppendableMemory)
 * iterate} phase: </b> the main part of the algorithm. The initial matrix is
 * expanded by adding additional constraints. The new constraints, represented
 * by halfspaces, are intersected with the previous, intermediary cone.</li>
 * <li><b>a
 * {@link #postprocess(ColumnHome, NetworkEfmModel, IterableMemory, EfmOutputCallback)
 * postprocess} phase: </b> here, the (possibly binary) intermediary modes
 * (called columns) are converted back to the desired output format. This
 * includes removal of undesired elements, like futile cycles resulting from
 * split reversible reactions</li>
 * </ul>
 * <p>
 * <b>Note: </b>This is the new generation type of implementation. It delegates
 * the choice of memory and efm model to factories, which have to be passed to
 * the constructor. Most factories have default constructors without arguments,
 * and can thus be defined in the configuration file.
 * 
 */
abstract public class AbstractDoubleDescriptionImpl implements ElementaryFluxModes.Impl {

	private static final Logger LOG = LogPkg.LOGGER;

	private Config mConfig;
	private EfmModelFactory mEfmModelFactory;
	private MemoryFactory mMemoryFactory;

	/**
	 * Constructor with config access and the two factories for model and
	 * memory. Note that most factories have default constructors without
	 * arguments, and can thus be defined in the configuration file.
	 */
	public AbstractDoubleDescriptionImpl(Config config, EfmModelFactory modelFactory, MemoryFactory memoryFactory) {
		mConfig = config;
		mEfmModelFactory = modelFactory;
		mMemoryFactory = memoryFactory;
	}

	public Config getConfig() {
		return mConfig;
	}

	protected MemoryFactory getMemoryFactory() {
		return mMemoryFactory;
	}

	// TO IMPLEMENT:
	abstract protected <N extends Number, Col extends Column> IterableMemory<Col> iterate(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> memory) throws IOException;

	public void calculateEfms(MetabolicNetwork metabolicNetwork, EfmOutputCallback callback) {
		try {
			calculateEfms(mConfig.getArithmetic().getColumnHome(), metabolicNetwork, callback);
		} catch (RuntimeException ex) {
			LOG.severe("exception caught, ex=" + ex);
			ex.printStackTrace(new LogPrintWriter(LOG, Level.SEVERE));
			throw ex;
		} catch (Exception ex) {
			LOG.severe("exception caught, ex=" + ex);
			ex.printStackTrace(new LogPrintWriter(LOG, Level.SEVERE));
			throw new RuntimeException(ex);
		}
	}

	private <N extends Number, Col extends Column> void calculateEfms(ColumnHome<N, Col> columnHome, MetabolicNetwork metabolicNetwork, EfmOutputCallback callback) throws IOException {
		long tStart = System.currentTimeMillis();

		// preprocess
		final NetworkEfmModel efmModel = preprocess(columnHome, metabolicNetwork, callback);
		final AppendableMemory<Col> memory = efmModel.createInitialMemory(columnHome, mMemoryFactory);

		final IterableMemory<Col> results;
		if (!mConfig.parseOnly()) {
			// timing initialize
			long tCpuStart = JVMTimer.getProcessCpuTimeMS();
			JVMTimer timer = null;
			;
			if (LOG.isLoggable(Level.FINER)) {
				timer = new JVMTimer(100);
				timer.start();
			}
			long tItStart = System.currentTimeMillis();

			// iterations
			results = iterate(columnHome, efmModel, memory);

			// timing output
			final long tEnd = System.currentTimeMillis();
			final long tCpuEnd = JVMTimer.getProcessCpuTimeMS();
			LOG.info("TIME iterate: " + (tEnd - tItStart) + "ms");
			LOG.fine("TIME jvm (total): " + (tCpuEnd - tCpuStart) + "ms");
			LOG.fine("TIME jvm (per core): " + (tCpuEnd - tCpuStart) / getConfig().getMaxThreads() + "ms");
			if (timer != null) {
				timer.stop();
				LOG.finer("TIME java (threads): cpu=" + timer.getTotalCpuTimeMS() + "ms, user=" + timer.getTotalUserTimeMS() + "ms, system=" + timer.getTotalSystemTimeMS() + "ms");
			}
		} else {
			results = mMemoryFactory.createReadWriteMemory(columnHome, efmModel, efmModel.getIterationCount(), null);
		}

		// postprocess
		postprocess(columnHome, efmModel, results, callback);

		long tEnd = System.currentTimeMillis();
		LOG.info("overall computation time: " + (tEnd - tStart) + "ms");
	}

	/**
	 * Input options logging and network compression, if desired
	 * 
	 * @param metaNet
	 *            the metabolic network
	 * @return the model
	 */
	protected <N extends Number, Col extends Column> NetworkEfmModel preprocess(ColumnHome<N, Col> columnHome, MetabolicNetwork metaNet, EfmOutputCallback callback) {

		ThermodynamicParameters.setOriginalMetabolicNetwork(metaNet);

		long tStart = System.currentTimeMillis();

		// compression
		logOptions(metaNet, callback);
		LogPkg.logNetwork(metaNet, Level.FINEST);
		LogPkg.infoNetworkSize("original network: ", metaNet);
		if (mConfig.compressNetwork(true)) {
			boolean preprocessDupl = mConfig.getPreprocessDuplicateGenes();
			if (preprocessDupl && CompressionMethod.DuplicateGene.containedIn(mConfig.getCompressionMethods(true))) {
				metaNet = CompressionUtil.compressDuplicateGeneReactions(metaNet, mConfig.zero(), mConfig.getCompressionMethods(true));
				LogPkg.infoNetworkSize("duplicate-free network size: ", metaNet);
			}
			if (mConfig.compressNetwork(!preprocessDupl)) {
				metaNet = CompressionUtil.compress(metaNet, mConfig.getCompressionMethods(!preprocessDupl), mConfig.getReactionsToSuppress(), mConfig.zero());
				LogPkg.infoNetworkSize("compressed network: ", metaNet);
			}
		} else {
			if (!(metaNet instanceof FractionNumberStoichMetabolicNetwork)) {
				metaNet = new FractionNumberStoichMetabolicNetwork(metaNet.getMetaboliteNames(), metaNet.getReactionNames(), MatrixUtil.convertToBigIntegerRationalMatrix(
						metaNet.getStoichiometricMatrix(), zero(), false /* enforceNewInstance */),
				// MatrixUtil.convertToBigIntegerRationalMatrix(metaNet.getStoichiometricMatrix(),
				// false /*enforceNewInstance*/),
						metaNet.getReactionReversibilities());
			}
		}
		final NetworkEfmModel efmModel = mEfmModelFactory.createEfmModel(columnHome, mConfig, metaNet);
		efmModel.log(columnHome, LOG);
		long tEnd = System.currentTimeMillis();
		LOG.info("TIME preprocessing: " + (tEnd - tStart) + "ms");
		return efmModel;
	}

	protected <N extends Number, Col extends Column> void postprocess(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, IterableMemory<Col> memory, EfmOutputCallback callback)
			throws IOException {
		LOG.info("efm count before postprocessing: " + memory.getColumnCount());
		final long tStart = System.currentTimeMillis();
		final long efmCount;

		// ===========================================================================================
		// check thermodynamic feasibility of modes
		// implemented by Matthias Gerstl
		if (ThermodynamicParameters.isActive())
		{
		    int before = memory.getColumnCount();
		    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		    Calendar cal = Calendar.getInstance();
		    int thermo_threads = ThermodynamicParameters.getThermoThreads();
		    if (thermo_threads == 0)
		    {
			thermo_threads = mConfig.getMaxThreads();
		    }
		    Thermodynamic thermo = new Thermodynamic(efmModel, thermo_threads,
			    false, true);

		    LOG.info("Postprocess thermodynamic check of " + before + " modes\t"
			    + dateFormat.format(cal.getTime()));
		    BitSet infModes = thermo.getInfeasibleModes(memory);
		    int removed = 0;
		    if (infModes.cardinality() > 0)
		    {
			final AppendableMemory<Col> myFiltered =
			    mMemoryFactory.createConcurrentAppendableMemory(columnHome, efmModel,
				    efmModel.getIterationCount() + 1, PartId.FLT);
			Iterator<Col> myIter = memory.iterator();
			int bitC = -1;
			while (myIter.hasNext())
			{
			    Col myCol = myIter.next();
			    bitC++;
			    if (infModes.get(bitC))
			    {
				removed++;
			    }
			    else
			    {
				myFiltered.appendColumn(myCol);
			    }
			}
			memory = myFiltered;
		    }
		    LOG.info("Thermodynamically removed: " + removed + " modes");
		}
		// end of post thermodynamic check
		// ===========================================================================================

		if (!mConfig.parseOnly()) {
			memory = filterModes(columnHome, efmModel, memory);
			System.out.println("DEBUG: after AbstractDoubleDescriptionImpl.filterModes()");
			efmCount = getRealEfmCount(columnHome, efmModel, memory);
		} else {
			efmCount = 0L;
		}
		LOG.info("efm count after filtering/consolidation: " + efmCount);
		LOG.info("uncompressing modes (can take a while)");

		// MemoryMonitor.checkMemory();

		final EfmOutputEvent evtPre = new EfmOutputEvent(EfmOutputEvent.Kind.PRE, efmModel.getMetabolicNetwork(), efmCount);
		callback.callback(evtPre);
		if (!mConfig.parseOnly() && callback.getGranularity() != CallbackGranularity.Null) {
			efmModel.getColumnToFluxDistributionConverter(columnHome).writeColumnsToCallback(mConfig, efmModel, memory, callback);
		}
		callback.callback(new EfmOutputEvent(EfmOutputEvent.Kind.POST, efmModel.getMetabolicNetwork(), efmCount));
		final long tEnd = System.currentTimeMillis();
		LOG.info("TIME postprocessing: " + (tEnd - tStart) + "ms");

		// ===============================
		// added by matthias
		// memory usage information
		// MemoryMonitor.checkMemory();
		// LOG.info("Max used memory: " + MemoryMonitor.getMaxUsedMemory());
		// end of modification
		// ===============================

	}

	/**
	 * Returns the real EFM count. The given memory contains the computed EFMs,
	 * already {@link #filterModes(ColumnHome, NetworkEfmModel, IterableMemory)
	 * filtered}. Here, EFMs are considered resulting from external-2-external
	 * reactions, for which an EFM is added (1 EFM for irreversible, 2 for
	 * reversible external-to-external reactions).
	 */
	private <N extends Number, Col extends Column> long getRealEfmCount(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, IterableMemory<Col> memory) throws IOException {
		long efmCount = memory.getColumnCount();
		for (final Reaction reac : efmModel.getMetabolicNetwork().getReactions()) {
			if (reac.isUptake() && reac.isExtract()) {
				efmCount++;
				if (reac.getConstraints().isReversible()) {
					efmCount++;
				}
			}
		}
		return efmCount;
	}

	/**
	 * Filters the modes in <code>memory</code> and returns the filtered ones.
	 * The returned memory containing filtered columns might be the same or a
	 * new instance of memory.
	 * <p>
	 * Filtering includes, but is not limited to, removing modes with no flux
	 * for enforced reactions.
	 */
	protected <N extends Number, Col extends Column> IterableMemory<Col> filterModes(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, IterableMemory<Col> memory) throws IOException {
		final int finalIteration = efmModel.getIterationCount() + 1;
		final AppendableMemory<Col> filtered = mMemoryFactory.createConcurrentAppendableMemory(columnHome, efmModel, finalIteration, PartId.FLT);
		// for (int i = 0; i < efmModel.getOutOfIterationLoopCount(); i++) {
		// filtered = filtered.createNextIterationMemory(efmModel);
		// }
		// if (memory == filtered) {
		// filtered = memory.createPartMemory(PartId.FLT);
		// }
		final ReactionMapping rmap = new ReactionMapping(mConfig, efmModel.getMetabolicNetwork(), efmModel.getReactionSorting());

		final IterationStepModel itModel = DefaultIterationStepModel.getFinal(efmModel);
		for (Col col : memory) {
			if (efmModel.getColumnFilter().keepColumn(col, mConfig, rmap)) {
				col = col.convert(columnHome, efmModel, itModel, false /* clone */);
				filtered.appendColumn(col);
			}
		}
		return filtered;
	}

	public String getImplName() {
		return getClass().getSimpleName();
	}

	private void logOptions(MetabolicNetwork metaNet, EfmOutputCallback callback) {
		final RuntimeMXBean rtBean = ManagementFactory.getRuntimeMXBean();
		final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
		final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		LOG.info("Elemetary flux mode computation");
		LOG.info("Implementation:");
		LOG.info("..algorithm name   : " + getImplName());
		LOG.info("..model type       : " + getFactoryName(mEfmModelFactory));
		LOG.info("..memory type      : " + getFactoryName(mMemoryFactory));
		LOG.info("..output type      : " + getCallbackName(callback));
		LOG.info("System:");
		LOG.info("..hostname         : " + getHostName());
		LOG.info("..operating system : " + osBean.getArch() + "/" + osBean.getName() + "/" + osBean.getVersion());
		LOG.info("..processors       : " + osBean.getAvailableProcessors());
		LOG.info("..vm               : " + rtBean.getVmVendor() + "/" + rtBean.getVmName() + "/" + rtBean.getVmVersion());
		LOG.info("..vm-spec          : " + rtBean.getSpecVendor() + "/" + rtBean.getSpecName() + "/" + rtBean.getSpecVersion());
		LOG.info("..vm arguments     : " + rtBean.getInputArguments());
		LOG.info("..memory, commited : " + (memBean.getHeapMemoryUsage().getCommitted() / 1000 / 1000) + "M");
		LOG.info("..memory, used     : " + (memBean.getHeapMemoryUsage().getUsed() / 1000 / 1000) + "M");
		mConfig.log(LOG, Level.INFO);
	}

	private static String getFactoryName(Object factory) {
		return getSuffixFree("Factory", factory);
	}

	private static String getCallbackName(Object factory) {
		return getSuffixFree("OutputCallback", factory);
	}

	private static String getSuffixFree(String suffix, Object instance) {
		if (instance == null)
			return "<none>";
		final String className = instance.getClass().getSimpleName();

		if (className.endsWith(suffix)) {
			return className.substring(0, className.length() - suffix.length());
		}
		return className;
	}

	private static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			return "unknown";
		}
	}

	protected Zero zero() {
		return mConfig.zero();
	}

	@SuppressWarnings("unused")
	private void traceEfms(Iterable<?> efms) {
		int i = 0;
		for (Object o : efms) {
			LOG.finest("[" + i + "]: " + o);
			i++;
		}
	}

	// int[][][] stats;
	// private void traceEfmStats(NetworkEfmModel efmModel, Iterable<? extends
	// Column> efms, int iteration, boolean firstIsBinary) {
	// if (stats == null) {
	// stats = new int[3][efmModel.getIterationCount() +
	// 1][efmModel.getIterationCount() + 1];
	// }
	// if (efms == null) {
	// System.out.println("born-die[-]:");
	// new DefaultIntMatrix(stats[0], true).writeToMultiline(System.out);
	// System.out.println("born-die[0]:");
	// new DefaultIntMatrix(stats[1], true).writeToMultiline(System.out);
	// System.out.println("born-die[+]:");
	// new DefaultIntMatrix(stats[2], true).writeToMultiline(System.out);
	// System.out.println("born-die[c]:");//adj candidates
	// final LongMatrix adj = new
	// DefaultLongMatrix(efmModel.getIterationCount(),
	// efmModel.getIterationCount());
	// for (int row = 0; row < adj.getRowCount(); row++) {
	// for (int col = 0; col <= row; col++) {
	// adj.setValueAt(row, col, stats[0][row][col]);
	// long factor = 0L;
	// for (int poscol = 0; poscol <= row; poscol++) {
	// factor += stats[2][row][poscol];
	// }
	// adj.multiply(row, col, factor);
	// }
	// }
	// adj.writeToMultiline(System.out);
	// }
	// else {
	// for (final Column col : efms) {
	// boolean died = false;
	//
	// for (int i = efmModel.getBooleanSize(0) + iteration; !died && i <
	// col.totalSize() - efmModel.getOutOfIterationLoopCount(); i++) {
	// final int sgn;
	// if (i < col.booleanSize()) {
	// sgn = col.get(i) ? 0 : 1;
	// }
	// else {
	// sgn = col.signum(efmModel.zero(), i - col.booleanSize());
	// }
	// died = sgn < 0;
	// stats[sgn + 1][i - efmModel.getBooleanSize(0)][iteration]++;
	// }
	// if (!died) {
	// stats[0][efmModel.getIterationCount()][iteration]++;
	// }
	// }
	// }
	// }

}
