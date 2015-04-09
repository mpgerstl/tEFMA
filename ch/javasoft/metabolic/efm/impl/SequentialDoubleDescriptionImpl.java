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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.acib.thermodynamic.Thermodynamic;
import at.acib.thermodynamic.ThermodynamicParameters;
import ch.javasoft.metabolic.efm.adj.AdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryFactory;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.DefaultIterationStepModel;
import ch.javasoft.metabolic.efm.model.EfmModelFactory;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.metabolic.efm.util.ColumnUtil;

/**
 * The <code>SequentialDoubleDescriptionImpl</code> implementation is the
 * usually known implementation of the double description method. The iteration
 * phase (see {@link AbstractDoubleDescriptionImpl superclass} for the phases)
 * processes each missing constraint sequentially, i.e. step by step.
 */
public class SequentialDoubleDescriptionImpl extends AbstractDoubleDescriptionImpl {

	private static final Logger LOG = LogPkg.LOGGER;
	private Thermodynamic m_thermo;
	private int thermo_threads;
	private String thermo_manner;

	/**
	 * Constructor with config access and the two factories for model and
	 * memory. Note that most factories have default constructors without
	 * arguments, and can thus be defined in the configuration file.
	 */
	public SequentialDoubleDescriptionImpl(Config config, EfmModelFactory modelFactory, MemoryFactory memoryFactory) {
		super(config, modelFactory, memoryFactory);
	}

	private <Col extends Column, N extends Number> void initializeThermodynamicChecks(NetworkEfmModel model, ColumnHome<N, Col> columnHome) {
		StringBuilder build = new StringBuilder();
		if (ThermodynamicParameters.isActive()) {
			thermo_threads = ThermodynamicParameters.getThermoThreads();
			thermo_manner = ThermodynamicParameters.getManner();
			boolean printPattern = ThermodynamicParameters.isPatternPrinted();
			boolean printLp = ThermodynamicParameters.isLpPrinted();
			boolean printLpVar = ThermodynamicParameters.isLpVariablePrinted();
			
			build.append("\n" + "Thermodynamic parameters:\n");
			build.append("Concentration file:             " + ThermodynamicParameters.getConcentrationFile() + "\n");
			build.append("Thermodynamic file:             " + ThermodynamicParameters.getThermodynamicFile() + "\n");
			if (printPattern) {
				build.append("Ineasible pattern file:         " + ThermodynamicParameters.getInfeasiblePatternFile() + "\n");
			}
			if (printLp) {
				build.append("LP file:                        " + ThermodynamicParameters.getLpFile() + "\n");
			}
			if (printLpVar) {
				build.append("LP variable file:               " + ThermodynamicParameters.getLpVariableFile() + "\n");
			}

			build.append("Temperature:                    " + ThermodynamicParameters.getTemperature() + " K\n");
			build.append("Ionic strength:                 " + ThermodynamicParameters.getIonicStrength() + "\n");
			build.append("pH:                             " + ThermodynamicParameters.getPH() + "\n");
			build.append("Standard minimum concentration: " + ThermodynamicParameters.getStdMin() + "\n");
			build.append("Standard maximum concentration: " + ThermodynamicParameters.getStdMax() + "\n");
			build.append("Proton name in model:           " + ThermodynamicParameters.getProton() + "\n");
			build.append("Solver:                         CPLEX\n");
			build.append("Threads for solver:             " + thermo_threads + "\n");
			build.append("manner:                         " + thermo_manner + "\n\n");
			m_thermo = new Thermodynamic(model, thermo_threads, true, false);
		} else {
			build.append("no thermodynamic check");
		}
		LOG.info(build.toString());
	}

	@Override
	protected <N extends Number, Col extends Column> IterableMemory<Col> iterate(ColumnHome<N, Col> columnHome, NetworkEfmModel efmModel, AppendableMemory<Col> memory) throws IOException {
		initializeThermodynamicChecks(efmModel, columnHome);

		AppendableMemory<Col> pos = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, 1, PartId.POS);
		AppendableMemory<Col> zer = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, 1, PartId.ZER);
		AppendableMemory<Col> neg = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, 1, PartId.NEG);

		final IterationStepModel initialItModel = new DefaultIterationStepModel(efmModel, 0);
		ColumnUtil.partition(columnHome, efmModel, memory, pos, zer, neg, initialItModel, false /* keep */);

		final AdjEnum adjEnum = getConfig().getAdjMethodFactory().createAdjEnumFromConfig();
		adjEnum.initialize(columnHome, getConfig(), efmModel);

		final int itCount = efmModel.getIterationCount();

		int cntPos = pos.getColumnCount();
		int cntZer = zer.getColumnCount();
		int cntNeg = neg.getColumnCount();

		int iteration = 0;
		long timeStart = System.currentTimeMillis();
		long timeEnd = timeStart;
		int colCount = cntPos + cntZer + cntNeg;
		while (colCount > 0 && iteration < itCount) {
			// =======================================================================
			// implemented by Matthias Gerstl
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			// =============================

			LOG.info(dateFormat.format(cal.getTime()) + "\titeration " + iteration + "/" + itCount + ": " + colCount + " modes, dt=" + (timeEnd - timeStart) + "ms." + "\t{ next " + (iteration + 1)
					+ "/" + itCount + ": " + ((long) cntPos) * ((long) cntNeg) + " adj candidates, " + "[+/0/-] = [" + cntPos + "/" + cntZer + "/" + cntNeg + "] }");
			if (LOG.isLoggable(Level.ALL)) {
				traceCols("col:+", 0, pos);
				traceCols("col:0", cntPos, zer);
				traceCols("col:-", cntPos + cntZer, neg);
			}

			iteration++;
			timeStart = System.currentTimeMillis();

			memory = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, null);

			/*
			 * remove infeasible columns
			 */
			if (ThermodynamicParameters.isActive()) {
				int pBefore = pos.getColumnCount();
				int nBefore = neg.getColumnCount();
				if (pBefore > 0 && nBefore > 0) {
					if (cntNeg > 0 && (pBefore < nBefore || thermo_manner.equals(ThermodynamicParameters.THERMO_MEMORY) || iteration >= itCount)) {
						LOG.info(dateFormat.format(cal.getTime()) + "\t   Thermodynamic check of " + pBefore + " pos");
						AppendableMemory<Col> tpos = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration, PartId.POS);
						m_thermo.getFeasibleMemory(pos, tpos);
						// if (tpos.getColumnCount() > 0) {
						int rm = pBefore - tpos.getColumnCount();
						cal = Calendar.getInstance();
						LOG.info(dateFormat.format(cal.getTime()) + "\t   removed: " + rm + " pos");
						pos = tpos;
						// } else {
						// tpos =
						// getMemoryFactory().createConcurrentAppendableMemory(columnHome,
						// efmModel, iteration, PartId.POS);
						// tpos.appendColumn(pos.iterator().next());
						// pos = tpos;
						// int rm = pBefore - 1;
						// cal = Calendar.getInstance();
						// LOG.info(dateFormat.format(cal.getTime()) +
						// "\t   Thermodynamically removed: " + rm +
						// " pos; still 1 infeasible");
						// }
						cntPos = pos.getColumnCount();
					}
					if (cntPos > 0 && (pBefore >= nBefore || thermo_manner.equals(ThermodynamicParameters.THERMO_MEMORY) || iteration >= itCount)) {
						LOG.info(dateFormat.format(cal.getTime()) + "\t   Thermodynamic check of " + nBefore + " neg");
						AppendableMemory<Col> tneg = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration, PartId.NEG);
						m_thermo.getFeasibleMemory(neg, tneg);
						// if (tneg.getColumnCount() > 0) {
						int rm = nBefore - tneg.getColumnCount();
						cal = Calendar.getInstance();
						LOG.info(dateFormat.format(cal.getTime()) + "\t   removed: " + rm + " neg");
						neg = tneg;
						// } else {
						// tneg =
						// getMemoryFactory().createConcurrentAppendableMemory(columnHome,
						// efmModel, iteration, PartId.NEG);
						// tneg.appendColumn(neg.iterator().next());
						// neg = tneg;
						// int rm = nBefore - 1;
						// cal = Calendar.getInstance();
						// LOG.info(dateFormat.format(cal.getTime()) +
						// "\t   Thermodynamically removed: " + rm +
						// " neg; still 1 infeasible");
						// }
						cntNeg = neg.getColumnCount();
					}
				}
				LOG.info("                   " + "\t   new adj candidates " + ((long) cntPos) * ((long) cntNeg));
			}

			// generate new rays from adjacent ray pairs
			final AdjEnumModel<Col> adjModel = new AdjEnumModel<Col>(efmModel, iteration, pos.toSortableMemory(), zer.toSortableMemory(), neg.toSortableMemory(), memory);

			if (cntPos > 0 && cntNeg > 0) {
				adjEnum.adjacentPairs(columnHome, adjModel);
				pos.flush();
				zer.flush();
				neg.flush();
			}

			if (iteration < itCount) {
				final AppendableMemory<Col> npos = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, PartId.POS);
				final AppendableMemory<Col> nzer = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, PartId.ZER);
				final AppendableMemory<Col> nneg = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, PartId.NEG);

				ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Positive, pos, npos, nzer, nneg, adjModel, true /* keep */);
				ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Negative, neg, npos, nzer, nneg, adjModel, true /* keep */);
				ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Zero, zer, npos, nzer, nneg, adjModel, true /* keep */);
				ColumnUtil.partitionOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Zero, memory, npos, nzer, nneg, adjModel, false /* keep */);// the
																																						// new
																																						// columns
				pos = npos;
				zer = nzer;
				neg = nneg;
				cntPos = pos.getColumnCount();
				cntZer = zer.getColumnCount();
				cntNeg = neg.getColumnCount();
			} else {
				/*
				 * remove infeasible columns
				 */
				// if (ThermodynamicParameters.isActive()) {
				// 	int mBefore = memory.getColumnCount();
				// 	LOG.info(dateFormat.format(cal.getTime()) + "\t   Thermodynamic check of " + mBefore + " memory");
				// 	if (mBefore > 0) {
				// 		AppendableMemory<Col> tmem = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, null);
				// 		m_thermo.getFeasibleMemory(memory, tmem);
				// 		if (tmem.getColumnCount() > 0) {
				// 			int rm = mBefore - tmem.getColumnCount();
				// 			LOG.info(dateFormat.format(cal.getTime()) + "\t   removed: " + rm + " memory");
				// 			memory = tmem;
				// 		} else {
				// 			tmem = getMemoryFactory().createConcurrentAppendableMemory(columnHome, efmModel, iteration + 1, null);
				// 			tmem.appendColumn(memory.iterator().next());
				// 			memory = tmem;
				// 			int rm = mBefore - 1;
				// 			LOG.info(dateFormat.format(cal.getTime()) + "\t   removed: " + rm + " memory; still 1 infeasible");
				// 		}
				// 	}
				// }

				ColumnUtil.moveToOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Positive, pos, memory, adjModel, true /* keep */);
				ColumnUtil.moveToOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Negative, neg, memory, adjModel, true /* keep */);
				ColumnUtil.moveToOrClose(columnHome, efmModel, NetworkEfmModel.Partition.Zero, zer, memory, adjModel, true /* keep */);

			}
			colCount = cntPos + cntZer + cntNeg;

			timeEnd = System.currentTimeMillis();

			// LOG.finest("Max used memory: " +
			// MemoryMonitor.getMaxUsedMemory());

		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		if (iteration < itCount) {
			LOG.info(dateFormat.format(cal.getTime()) + "\titeration " + iteration + "/" + itCount + ": discontinued since no modes left.");
		} else {
			LOG.info(dateFormat.format(cal.getTime()) + "\titeration " + iteration + "/" + itCount + ": " + memory.getColumnCount() + " modes, dt=" + (timeEnd - timeStart) + "ms.");
			if (LOG.isLoggable(Level.ALL)) {
				traceCols("cols:", 0, memory);
			}
		}
		return memory;
	}

	private static void traceCols(String prefix, int indexOffset, Iterable<?> cols) {
		int index = indexOffset;
		for (final Object c : cols) {
			LOG.finest(prefix + "[" + index + "]: " + c);
			index++;
		}
	}
}
