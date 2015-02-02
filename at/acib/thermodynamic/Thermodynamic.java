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


package at.acib.thermodynamic;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

import at.acib.thermodynamic.check.PatternConverter;
import at.acib.thermodynamic.check.ThermoEfmCheck;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.smx.iface.ReadableMatrix;

/**
 * @author matthias
 */
public class Thermodynamic {

	private ThermoEfmCheck m_thermoEfmCheck;

	/**
	 * @param efmModel
	 *            {@link NetworkEfmModel}
	 * @param checkModel
	 *            true if model should be checked for feasibility
	 */
	public Thermodynamic(NetworkEfmModel efmModel, int threads, boolean checkModel, boolean appendPatternFile) {
		MetabolicNetwork origNet = ThermodynamicParameters.getOriginalMetabolicNetwork();
		double[][] origMatrix = getSmatrix(origNet);
		String[] metabolites = origNet.getMetaboliteNames();
		String[] reactions = origNet.getReactionNames();
		String[] compReactions = efmModel.getMetabolicNetwork().getReactionNames();
		int finalBooleanSize = efmModel.getFinalBooleanSize();
		double stdMin = ThermodynamicParameters.getStdMin();
		double stdMax = ThermodynamicParameters.getStdMax();
		double temperature = ThermodynamicParameters.getTemperature();
		double pH = ThermodynamicParameters.getPH();
		double ionStrength = ThermodynamicParameters.getIonicStrength();
		String concFile = ThermodynamicParameters.getConcentrationFile();
		String thermoFile = ThermodynamicParameters.getThermodynamicFile();
		String infPatternFile = ThermodynamicParameters.getInfeasiblePatternFile();
		String lpFile = ThermodynamicParameters.getLpFile();
		String lpVarFile = ThermodynamicParameters.getLpVariableFile();
		PatternConverter pc = new PatternConverter(efmModel);
		m_thermoEfmCheck = new ThermoEfmCheck(origMatrix, metabolites, reactions, compReactions, stdMin, stdMax, temperature, ionStrength, pH, threads, concFile, thermoFile, lpFile, lpVarFile,
				infPatternFile, appendPatternFile, pc, finalBooleanSize, checkModel);
	}

	private double[][] getSmatrix(MetabolicNetwork origNet) {
		ReadableMatrix<?> m = origNet.getStoichiometricMatrix();
		Double[][] r = (Double[][]) m.getNumberRows();
		double[][] stoich = new double[r.length][];
		for (int i = 0; i < r.length; i++) {
			stoich[i] = new double[r[i].length];
			for (int j = 0; j < r[i].length; j++) {
				stoich[i][j] = r[i][j];
			}
		}
		return stoich;
	}

	public <Col extends Column> void getFeasibleMemory(AppendableMemory<Col> oldMem, AppendableMemory<Col> newMem) {
		BitSet allInfPairs = m_thermoEfmCheck.checkMemory(oldMem);

		// remove infeasible pairs from adjacentPairs
		int card = allInfPairs.cardinality();
		if (card > 0) {
			int actIndex = -1;
			Iterator<Col> memIter = oldMem.iterator();
			while (memIter.hasNext()) {
				actIndex++;
				if (!allInfPairs.get(actIndex)) {
					try {
						newMem.appendColumn(memIter.next());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					memIter.next();
				}
			}
		} else {
			try {
				newMem.appendColumns(oldMem);
			} catch (IOException e) {
				System.out.println(this.getClass() + ":  Sorry you should never end up here ");
				System.out.println("Could not append columns");
				e.printStackTrace();
			}
		}
	}

	public <Col extends Column> BitSet getInfeasibleModes(IterableMemory<Col> mem) {
		try {
			BitSet allInfPairs = m_thermoEfmCheck.checkModes(mem);
			return allInfPairs;
		} catch (IOException e) {
			BitSet allInfPairs = new BitSet();
			return allInfPairs;
		}
	}

}