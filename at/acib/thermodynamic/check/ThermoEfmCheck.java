/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2015, Matthias P. Gerstl, Vienna, Austria
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


package at.acib.thermodynamic.check;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.logging.Logger;

import at.acib.thermodynamic.exception.ConcentrationErrorException;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.model.LogPkg;

public class ThermoEfmCheck {

	private int m_threadNumber;
	private ThermoChecker[] m_thermo;
	private GibbsCalculator m_gibbsC;
	private ThermoChecker m_thermo_orig;
	private InfoWriter m_infeasiblePatternWriter;
	private static final Logger LOG = LogPkg.LOGGER;

	/**
	 * constructor
	 * 
	 * @author matthias
	 * @param args
	 *            command line arguments
	 */
	public ThermoEfmCheck(double[][] sMatrix, String[] metabolites, String[] reactions, String[] compressedReactions, double stdMin, double stdMax, double temperature, double is, double pH,
			int threadNumber, String concentrationFile, String thermodynamicFile, String lpFile, String lpVariableFile, String infeasiblePatternFile, boolean appendPatternFile,
			PatternConverter patternConverter, int finalBooleanSize, boolean checkModel) {
		if (infeasiblePatternFile != null) {
			m_infeasiblePatternWriter = new InfoWriter(infeasiblePatternFile, appendPatternFile);
		}
		m_threadNumber = threadNumber;
		m_thermo = new ThermoChecker[m_threadNumber];
		m_gibbsC = new GibbsCalculator(temperature, is, pH, thermodynamicFile);

		try {
			InputHandler iHandler = new InputHandler(sMatrix, metabolites, reactions, compressedReactions, concentrationFile, stdMin, stdMax, m_gibbsC);
			m_thermo_orig = new ThermoChecker(temperature, is, pH, iHandler, m_gibbsC, lpFile, lpVariableFile, patternConverter, finalBooleanSize);
			if (checkModel) {
				if (!m_thermo_orig.isInitialModelFeasible(finalBooleanSize)) {
					ArrayList<String> inf = m_thermo_orig.getInitialInfeasibleReactions();
					
					System.out.println("Infeasible: ");
					for (String x : inf) {
						System.out.print("   " + x);
					}
					System.out.println("\n");
					if (m_infeasiblePatternWriter != null) {
						m_infeasiblePatternWriter.printPattern(StaticPatternContainer.getPattern(), StaticPatternContainer.getIterationStartPosition());
					}
				}
			}

			for (int i = 0; i < m_threadNumber; i++) {
				m_thermo[i] = m_thermo_orig.clone();
			}
		} catch (ConcentrationErrorException e) {
			System.out.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public <Col extends Column> BitSet checkMemory(AppendableMemory<Col> oldMem) {
		boolean done = false;
		while (!done) {
			done = StaticPatternContainer.setNextIteration();
		}
		int lastThread = m_threadNumber;
		try {
			int s = oldMem.getColumnCount();
			BitSet infPairs = new BitSet(s);
			if (s < m_threadNumber) {
				lastThread = s;
			}
			BitSet[] infModes = new BitSet[lastThread];
			ThermoMemCheckerThread[] tct = new ThermoMemCheckerThread[lastThread];

			for (int i = 0; i < lastThread; i++) {
				infModes[i] = new BitSet(s);
				tct[i] = new ThermoMemCheckerThread(i, lastThread, infModes[i], oldMem, m_thermo[i]);
			}
			try {
				for (int i = 0; i < lastThread; i++) {
					tct[i].getThreadObj().join();
					infPairs.or(infModes[i]);
				}
			} catch (InterruptedException e) {
				System.out.println("FATAL ERROR: Main thread interrupted.");
				System.out.println("             execution aborted.");
				System.exit(-1);
			}
			if (m_infeasiblePatternWriter != null) {
				m_infeasiblePatternWriter.printPattern(StaticPatternContainer.getPattern(), StaticPatternContainer.getIterationStartPosition());
			}
			return infPairs;
		} catch (Exception e) {
			return new BitSet(0);
		}

	}

	public <Col extends Column> BitSet checkModes(IterableMemory<Col> mem) throws IOException {
		int lastThread = m_threadNumber;
		int s = mem.getColumnCount();
		BitSet infPairs = new BitSet(s);
		if (s < m_threadNumber) {
			lastThread = s;
		}
		BitSet[] infModes = new BitSet[lastThread];
		ThermoMemCheckerThread[] tct = new ThermoMemCheckerThread[lastThread];
		for (int i = 0; i < lastThread; i++) {
			infModes[i] = new BitSet(s);
			tct[i] = new ThermoMemCheckerThread(i, lastThread, infModes[i], mem, m_thermo[i]);
		}
		try {
			for (int i = 0; i < lastThread; i++) {
				tct[i].getThreadObj().join();
				infPairs.or(infModes[i]);
			}
		} catch (InterruptedException e) {
			System.out.println("FATAL ERROR: Main thread interrupted.");
			System.out.println("             execution aborted.");
			System.exit(-1);
		}
		if (m_infeasiblePatternWriter != null) {
			m_infeasiblePatternWriter.printPattern(StaticPatternContainer.getPattern(), StaticPatternContainer.getIterationStartPosition());
		}
		return infPairs;
	}

	public int getInfeasibleCount() {
		return StaticPatternContainer.getInfeasibleCount();
	}

	public ArrayList<ArrayList<String>> getInfeasiblePattern() {
		return StaticPatternContainer.getPattern();
	}

	public boolean isInitialModelFeasible() {
		return m_thermo_orig.isInitialModelFeasible();
	}

	public ArrayList<Integer> getInfeasibleReactions() {
		return m_thermo_orig.getInfeasibleReactions();
	}
}
