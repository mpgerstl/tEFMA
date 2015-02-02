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

import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.ConflictStatus;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * connect to cplex, defines problems and returns solutions
 * 
 * @author matthias
 * 
 */
public class CplexHandler {

	protected static final int DIRECTION_FWD = 1;
	protected static final int DIRECTION_REV = 2;

	private boolean m_solved;
	private IloCplex m_cplex;
	private IloObjective m_obj;
	private IloLPMatrix m_lp;

	/**
	 * Constructor
	 * 
	 * @throws IloException
	 */
	public CplexHandler() throws IloException {
		m_cplex = new IloCplex();
		m_lp = m_cplex.addLPMatrix();
		m_solved = false;
		m_obj = m_cplex.addMaximize();
		m_cplex.setOut(null);
	}

	/**
	 * @return the created lp matrix
	 * @throws IloException
	 */
	protected IloLPMatrix addLPMatrix() throws IloException {
		return m_cplex.addLPMatrix();
	}

	/**
	 * add variables to lp matrix
	 * 
	 * @param lp
	 *            lp matrix
	 * @param lb
	 *            lower bounds
	 * @param ub
	 *            upper bounds
	 * @return variables
	 * @throws IloException
	 */
	protected IloNumVar[] addVariables(double[] lb, double[] ub) throws IloException {
		IloNumVar[] x = m_cplex.numVarArray(m_cplex.columnArray(m_lp, lb.length), lb, ub);
		return x;
	}

	/**
	 * print cplex output
	 * 
	 * @throws IloException
	 */
	protected void print() throws IloException {
		System.out.println(m_cplex);
	}

	/**
	 * export the lp model to a file
	 * 
	 * @param filename
	 *            for output
	 * @throws IloException
	 */
	protected void exportModel(String filename) throws IloException {
		if (!filename.endsWith(".lp")) {
			filename = filename + ".lp";
		}
		m_cplex.exportModel(filename);
	}

	/**
	 * add constraints equal to rhs to the lp matrix
	 * 
	 * @param lp
	 *            lp matrix
	 * @param col
	 *            columns of the constraint
	 * @param val
	 *            values corresponding to the columns
	 * @param rhs
	 *            right hand side
	 * @return row index
	 * @throws IloException
	 */
	protected int addEqualConstraint(int[] col, double[] val, double rhs, String name) throws IloException {
		return addLeConstraint(col, val, rhs, rhs, name);
	}

	protected int addReaction(LpReaction lpReaction, int direction) throws IloException {
		switch (direction) {
		case DIRECTION_FWD:
			return addEqualConstraint(lpReaction.getFwdCols(), lpReaction.getFwdVals(), 0, lpReaction.getFwdName());
		case DIRECTION_REV:
			return addEqualConstraint(lpReaction.getRevCols(), lpReaction.getRevVals(), 0, lpReaction.getRevName());
		}
		return 0;
	}

	protected int addFormation(LpFormation lpFormation) throws IloException {
		return addEqualConstraint(lpFormation.getCols(), lpFormation.getVals(), lpFormation.getDfG(), lpFormation.getName());
	}

	/**
	 * add constraints >= lhs and <= rhs to the lp matrix
	 * 
	 * @param lp
	 *            lp matrix
	 * @param col
	 *            columns of the constraint
	 * @param val
	 *            values corresponding to the columns
	 * @param rhs
	 *            right hand side
	 * @param lhs
	 *            left hand side
	 * @return row index
	 * @throws IloException
	 */
	protected int addLeConstraint(int[] col, double[] val, double lhs, double rhs, String name) throws IloException {
		int rowInd = m_lp.addRow(lhs, rhs, null, null);
		int[] rows = new int[val.length];
		Arrays.fill(rows, rowInd);
		m_lp.setNZs(rows, col, val);
		m_lp.getRange(rowInd).setName(name);
		m_solved = false;
		return rowInd;
	}

	/**
	 * add objective to the lp problem
	 * 
	 * @param obj
	 *            objective variable
	 * @param maximize
	 *            true if maximize
	 * @throws IloException
	 */
	protected void addObjective(IloNumExpr obj, boolean maximize) throws IloException {
		m_cplex.remove(m_obj);
		if (maximize) {
			m_obj = m_cplex.addMaximize(obj);
		} else {
			m_obj = m_cplex.addMinimize(obj);
		}
		m_solved = false;
	}

	/**
	 * @return true if problem can be solved
	 * @throws IloException
	 */
	protected boolean solve() throws IloException {
		m_solved = m_cplex.solve();
		return m_solved;
	}

	/**
	 * returns reactions that show conflict in current lp matrix
	 * 
	 * @param lp
	 *            current calculated model
	 * @return list of conflict reactions
	 * @throws IloException
	 */
	protected ArrayList<String> getConflictReactions() throws IloException {
		ArrayList<String> conflict = new ArrayList<String>();
		IloRange[] x = m_lp.getRanges();
		double[] p = new double[x.length];
		for (int i = 0; i < p.length; i++) {
			p[i] = 1;
		}
		m_cplex.refineConflict(x, p);
		ConflictStatus[] cs = m_cplex.getConflict(x);
		for (int i = 0; i < cs.length; i++) {
			if (cs[i] == ConflictStatus.Member) {
				String name = x[i].getName();
				if (name.startsWith(ThermoChecker.RX_PREFIX) || name.startsWith(ThermoChecker.RX_REV_PREFIX)) {
					conflict.add(name);
				}
			}
		}
		return conflict;
	}

	/**
	 * @return value of the objective
	 * @throws IloException
	 */
	protected double getObjValue() throws IloException {
		return m_cplex.getObjValue();
	}

	/**
	 * remove rows from lp matrix
	 * 
	 * @param lp
	 *            lp matrix
	 * @param startIndex
	 *            first row to remove
	 * @param length
	 *            number of rows to remove
	 * @throws IloException
	 */
	protected void removeRows(int startIndex) throws IloException {
		int length = m_lp.getNrows() - startIndex;
		m_lp.removeRows(startIndex, length);
	}

}
