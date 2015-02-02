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
package ch.javasoft.metabolic.util;

/**
 * <tt>LinearProgramming</tt> class contains static methods, for instance
 * utility methods and transformations, to be used with linear programming (LP).
 */
public class LinearProgramming {
	
	/**
	 * Input is a linear programming (LP) problem given in standard form:
	 * <pre>
	 *   maximize        c' * x      (objective function)
	 *   subject to     mxA * x <= b (inequality constraints)
	 *              nonneg' * x >= 0 (nonnegativity constraints)
	 * </pre>
	 * 
	 * Output is the equivalent problem in slack form:
	 * <pre>
	 *   maximize        c' * x      (objective function)
	 *   subj. to   b - mxA * x  = y (equality constraints)
	 *              nonneg' * x >= 0 (nonneg. constr. for nonbasic variables)
	 *                        y >= 0 (nonneg. constr. for basic/slack variables)
	 * </pre>
	 * 
	 * <p>
	 * <b>Notes:</b><br/>
	 * <i>nonneg</i><br/>
	 * The vectory nonneg contains values in [0, 1], namely 1 if a nonnegativity
	 * constraint exists for the appropriate basic variable, 0 otherwise.
	 * <p>
	 * <i>equality constraints</i><br/>
	 * The equality constraints
	 * <pre>
	 *      b - mxA * x = y
	 * </pre>
	 * can be rewritten as
	 * <pre>
	 *          mxB * z = 0
	 *   with       mxB = [-mxA, -I, b]
	 *                z = [ x ; y ; t ]
	 *                t = 1
	 * </pre>
	 * The cone defined by mxB is pointed, that is the origin is an extreme
	 * point. The solution to the original cone defined by mxA can be derived
	 * by setting t=1.
	 * 
	 * @param mxA		The m times n inequality constraint matrix
	 * @param b			The vector of size m, the right hand size of the 
	 * 					inequality constraints
	 * @return			The matrix mxB = [-mxA, -I, b]
	 */
	public static double[][] standardToSlackForm(double[][] mxA, double[] b) {
		int rows = mxA.length;
		int cols = rows == 0 ? 0 : mxA[0].length;
		double[][] res = new double[rows][cols + rows + 1];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				res[row][col] = -mxA[row][col];		// -mxA
			}
			res[row][cols + row]	= -1d;		// -I
			res[row][cols + rows]	= b[row];	// b
		}
		return res;
	}
	/**
	 * Input is a linear programming (LP) problem given in standard form:
	 * <pre>
	 *   maximize        c' * x      (objective function)
	 *   subject to     mxA * x <= 0 (inequality constraints)
	 *              nonneg' * x >= 0 (nonnegativity constraints)
	 * </pre>
	 * 
	 * Output is the equivalent problem in slack form:
	 * <pre>
	 *   maximize        c' * x      (objective function)
	 *   subj. to     - mxA * x  = y (equality constraints)
	 *              nonneg' * x >= 0 (nonneg. constr. for nonbasic variables)
	 *                        y >= 0 (nonneg. constr. for basic/slack variables)
	 * </pre>
	 * 
	 * <p>
	 * <b>Notes:</b><br/>
	 * <i>nonneg</i><br/>
	 * The vectory nonneg contains values in [0, 1], namely 1 if a nonnegativity
	 * constraint exists for the appropriate basic variable, 0 otherwise.
	 * <p>
	 * <i>equality constraints</i><br/>
	 * The equality constraints
	 * <pre>
	 *        - mxA * x = y
	 * </pre>
	 * can be rewritten as
	 * <pre>
	 *          mxB * z = 0
	 *   with       mxB = [-mxA, -I]
	 *                z = [ x ; y]
	 * </pre>
	 * The input cone defined by mxA as well that given by mxB is pointed.
	 * by setting t=1.
	 * 
	 * @param mxA		The m times n inequality constraint matrix
	 * @return			The matrix mxB = [-mxA, -I]
	 */
	public static double[][] standardToSlackForm(double[][] mxA) {
		int rows = mxA.length;
		int cols = rows == 0 ? 0 : mxA[0].length;
		double[][] res = new double[rows][cols + rows];
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				res[row][col] = -mxA[row][col];		// -mxA
			}
			res[row][cols + row]	= -1d;		// -I
		}
		return res;
	}
	
	//no instances
	private LinearProgramming() {
		super();
	}
}
