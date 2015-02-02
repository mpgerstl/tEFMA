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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;

public class ThermoMemCheckerThread implements Runnable {

	private int t_id;
	private int num_threads;
	private BitSet m_infeasiblePairs;
	@SuppressWarnings("rawtypes")
	private AppendableMemory m_mem;
	@SuppressWarnings("rawtypes")
	private IterableMemory m_memory;
	private Thread t;
	private ThermoChecker m_thermo;
	private boolean m_postProcess;

	public <Col extends Column> ThermoMemCheckerThread(int thread_id, int num, BitSet infeasiblePairs, AppendableMemory<Col> mem, ThermoChecker thermo) {
		t_id = thread_id;
		num_threads = num;
		m_infeasiblePairs = infeasiblePairs;
		m_mem = mem;
		m_thermo = thermo;
		m_postProcess = false;
		t = new Thread(this, "ThermodynamicThread");
		t.start();
	}

	public <Col extends Column> ThermoMemCheckerThread(int thread_id, int num, BitSet infeasiblePairs, IterableMemory<Col> mem, ThermoChecker thermo) {
		t_id = thread_id;
		num_threads = num;
		m_infeasiblePairs = infeasiblePairs;
		m_memory = mem;
		m_postProcess = true;
		m_thermo = thermo;
		t = new Thread(this, "ThermodynamicThread");
		t.start();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run() {
		ArrayList<ArrayList<String>> infPattern = new ArrayList<ArrayList<String>>();
		ArrayList<BitSet> infBitPattern = (ArrayList<BitSet>) StaticPatternContainer.getBitPattern().clone();
		ArrayList<BitSet> newPattern = new ArrayList<BitSet>();
		if (m_postProcess) {
			Iterator<Column> myIt = m_memory.iterator();
			int col_cnt = 0;
			while (myIt.hasNext()) {
				Column n = myIt.next();
				if (col_cnt % num_threads == t_id) {
					BitSet myBitSet = n.bitValues().toBitSet();
					int l = n.booleanSize();
					// if (!myBitSet.get(l-1)) {
					if (!matchPattern(infBitPattern, myBitSet, l)) {
						if (!m_thermo.isFeasible(myBitSet, l)) {
							ArrayList<String> conflictReactions = m_thermo.getConflictReactions();
							infPattern.add(conflictReactions);
							m_infeasiblePairs.set(col_cnt);
							BitSet conflictPattern = m_thermo.getConflictBitPattern(myBitSet, l, conflictReactions);
							infBitPattern.add(conflictPattern);
							newPattern.add(conflictPattern);
						}
					} else {
						m_infeasiblePairs.set(col_cnt);
					}
					// }
				}
				col_cnt++;
			}
		} else {
			Iterator myIt = m_mem.iterator();
			int col_cnt = 0;
			while (myIt.hasNext()) {
				Column col = (Column) myIt.next();
				if (col_cnt % num_threads == t_id) {
					int l = col.booleanSize();
					IBitSet iBit = col.bitValues();
					BitSet myBitSet = iBit.toBitSet();
					if (!matchPattern(infBitPattern, myBitSet, l)) {
						if (!m_thermo.isFeasible(myBitSet, l)) {
							ArrayList<String> conflictReactions = m_thermo.getConflictReactions();
							infPattern.add(conflictReactions);
							m_infeasiblePairs.set(col_cnt);
							BitSet conflictPattern = m_thermo.getConflictBitPattern(myBitSet, l, conflictReactions);
							infBitPattern.add(conflictPattern);
							newPattern.add(conflictPattern);
						}
					} else {
						m_infeasiblePairs.set(col_cnt);
					}
				}
				col_cnt++;
			}
		}
		boolean done = false;
		while (!done) {
			done = StaticPatternContainer.addPattern(infPattern);
		}
		done = false;
		while (!done) {
			done = StaticPatternContainer.addBitPattern(newPattern);
		}

	}

	private boolean matchPattern(ArrayList<BitSet> pattern, BitSet mode, int booleanSize) {
		BitSet temp = (BitSet) mode.clone();
		temp.flip(0, booleanSize);
		for (BitSet a : pattern) {
			BitSet b = (BitSet) temp.clone();
			b.and(a);
			if (b.equals(a)) {
				return true;
			}
		}
		return false;
	}

	public Thread getThreadObj() {
		return (t);
	}

}
