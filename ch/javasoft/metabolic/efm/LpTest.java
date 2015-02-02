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
package ch.javasoft.metabolic.efm;

import java.util.logging.Level;

import junit.framework.TestCase;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.incore.tree.search.PatternTreeMinZerosAdjacencyEnumerator;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.util.LinearProgramming;
import ch.javasoft.util.logging.Loggers;

public class LpTest extends TestCase {
	
	static {
		final CompressionMethod[] compression = CompressionMethod.STANDARD;
		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
			Loggers.getRootLogger().setLevel(Level.FINE);
		}
	}

	/**
	 * Cormen, p. 781,
	 * maximize  2x1 - 3x2 + 3x3
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 *               
	 * The optimal solution is at x=[6 1 0], which must also be one of the
	 * extreme points, that is, one efm.
	 */
	public void testCormen781() {
		double[] cost = new double[] {2, -3, 3};
		double[][] mxA = new double[][] {
			{ 1,  1, -1},
			{-1, -1,  1},
			{ 1, -2,  2}
		};
		double[]		b	= new double[] { 7, -7, 4};
//		boolean[] nonneg	= new boolean[] {true, true, true};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	
	/**
	 * Cormen, p. 791,
	 * maximize  3x1 + x2 + 2x3
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 *               
	 * The optimal solution is at x=[8 4 0], which must also be one of the
	 * extreme points, that is, one efm.
	 */
	public void testCormen791() {
		double[] cost = new double[] {3, 1, 2};
		double[][] mxA = new double[][] {
			{ 1,  1,  3},
			{ 2,  2,  5},
			{ 4,  1,  2}
		};
		double[]		b	= new double[] { 30, 24, 36};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}

	/**
	 * solve sat problem (~a + ~b + ~c)
	 * maximize  a + b + c
	 * minimize  a + b + c
	 * subj. to  a >= 0  
	 *           a <= 1
	 *           b >= 0
	 *           b <= 1
	 *           c >= 0
	 *           c <= 1
	 *   a + b + c <= 2
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx1a() {
		double[] cost = new double[] {1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0},
			{ 0,  1,  0},
			{ 0,  0,  1},
			{ 1,  1,  1}
		};
		double[]		b	= new double[] { 1, 1, 1, 2};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	/**
	 * solve sat problem ( a + ~b + ~c)
	 * maximize  a + b + c
	 * minimize  a + b + c
	 * subj. to  a >= 0  
	 *           a <= 1
	 *           b >= 0
	 *           b <= 1
	 *           c >= 0
	 *           c <= 1
	 *  -a + b + c <= 1
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx1b() {
		double[] cost = new double[] {1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0},
			{ 0,  1,  0},
			{ 0,  0,  1},
			{-1,  1,  1}
		};
		double[]		b	= new double[] { 1, 1, 1, 1};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	/**
	 * solve sat problem (~a + ~b + ~c) * ( a + ~b + ~c)
	 * maximize  a + b + c
	 * minimize  a + b + c
	 * subj. to  a >= 0  
	 *           a <= 1
	 *           b >= 0
	 *           b <= 1
	 *           c >= 0
	 *           c <= 1
	 *   a + b + c <= 2
	 *  -a + b + c <= 1
	 *       b + c <= 1
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx2() {
		double[] cost = new double[] {1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0},
			{ 0,  1,  0},
			{ 0,  0,  1},
			{ 1,  1,  1},
			{-1,  1,  1},
			{ 0,  1,  1}
		};
		double[]		b	= new double[] { 1, 1, 1, 2, 1, 1};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	
	/**
	 * solve sat problem (~a + ~b + ~c) * (a + ~b + ~d) * (c + d + e)
	 * maximize  a + b + c + d + e
	 * minimize  a + b + c + d + e
	 * subj. to  [a b c d e]' >=  0  
	 *           [a b c d e]' <=  1  
	 *      a + b + c         <=  2 (A)
	 *     -a + b     + d     <=  1 (B)
     *            - c - d - e <= -1 (C)
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx3() {
		double[] cost = new double[] {1, 1, 1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0,  0,  0},
			{ 0,  1,  0,  0,  0},
			{ 0,  0,  1,  0,  0},
			{ 0,  0,  0,  1,  0},
			{ 0,  0,  0,  0,  1},
			{ 1,  1,  1,  0,  0}, //A
			{-1,  1,  0,  1,  0}, //B
			{ 0,  0, -1, -1, -1}, //C
		};
		double[]		b	= new double[] { 1, 1, 1, 1, 1, 2, 1, -1};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	/**
	 * solve sat problem (~a + ~b + ~c) * (a + ~b + ~d) * (c + d + e)
	 * maximize  a + b + c + d + e
	 * minimize  a + b + c + d + e
	 * subj. to  [a b c d e]' >=  0  
	 *           [a b c d e]' <=  1  
	 *      a + b + c         <=  2 (A)
	 *     -a + b     + d     <=  1 (B)
     *            - c - d - e <= -1 (C)
	 *           
	 *         2b + c + d     <=  3 (A+B)
	 * ==>      b + c + d     <=  2
	 * 
	 *     -a + b + c +2d + e <=  2 (B-C)
	 * ==> -a + b + c     + e <=  2
	 *  &  -a + b + c + d + e <=  2
	 *  
	 *      a + b +2c + d + e <=  2 (A-C)
	 * ==>  a + b     + d + e <=  2
	 *  &   a + b + c + d + e <=  2
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx3ab() {
		double[] cost = new double[] {1, 1, 1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0,  0,  0},
			{ 0,  1,  0,  0,  0},
			{ 0,  0,  1,  0,  0},
			{ 0,  0,  0,  1,  0},
			{ 0,  0,  0,  0,  1},
			{ 1,  1,  1,  0,  0}, //A
			{-1,  1,  0,  1,  0}, //B
			{ 0,  0, -1, -1, -1}, //C
			{ 0,  2,  1,  1,  0}, //A+B
			//NOTE: we cannot subtract, since the inequality would then
			//      have to change direction, i.e. become >=
/*			{-1,  1,  1,  0,  1}, //B-C
			{-1,  1,  1,  1,  1}, //B-C
			{ 1,  1,  0,  1,  1}, //A-C
			{ 1,  1,  1,  1,  1}, //A-C*/			
		};
		double[]		b	= new double[] { 1, 1, 1, 1, 1, 2, 1, -1, 3};//, 2, 2, 2, 2};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	/**
	 * solve sat problem (~a + ~b + ~c) * (a + ~b + ~d) * (c + d + e)
	 * maximize  a + b + c + d + e
	 * minimize  a + b + c + d + e
	 * subj. to  [a b c d e]' >=  0  
	 *           [a b c d e]' <=  1  
	 *      a + b + c         <=  2 (A)
	 *     -a + b     + d     <=  1 (B)
     *            - c - d - e <= -1 (C)
	 *           
	 *         2b + c + d     <=  3 (A+B)
	 * ==>      b + c + d     <=  2
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx3abMod() {
		double[] cost = new double[] {1, 1, 1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0,  0,  0},
			{ 0,  1,  0,  0,  0},
			{ 0,  0,  1,  0,  0},
			{ 0,  0,  0,  1,  0},
			{ 0,  0,  0,  0,  1},
			{ 1,  1,  1,  0,  0}, //A
			{-1,  1,  0,  1,  0}, //B
			{ 0,  0, -1, -1, -1}, //C
			{ 0,  1,  1,  1,  0}, //A+B
			//NOTE: we cannot subtract, since the inequality would then
			//      have to change direction, i.e. become >=
/*			{-1,  1,  1,  0,  1}, //B-C
			{-1,  1,  1,  1,  1}, //B-C
			{ 1,  1,  0,  1,  1}, //A-C
			{ 1,  1,  1,  1,  1}, //A-C*/			
		};
		double[]		b	= new double[] { 1, 1, 1, 1, 1, 2, 1, -1, 2};//, 2, 2, 2, 2};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}
	/**
	 * solve sat problem (~a + ~b + ~c) * (a + ~b + ~d) * (c + d + e)
	 * maximize  a + b + c + d + e
	 * minimize  a + b + c + d + e
	 * subj. to  [a b c d e]' >=  0  
	 *           [a b c d e]' <=  1  
	 *      a + b + c         <=  2 (A)
	 *     -a + b     + d     <=  1 (B)
     *            - c - d - e <= -1 (C)
	 *           
	 *         2b + c + d     <=  3 (A+B)
	 * ==>      b + c + d     <=  2
	 * 
	 *     -a + b - c     - e <=  0 (B+C)
	 *  
	 *      a + b     - d - e <=  1 (A+C)
	 *   
	 * subj. to  mxA*x <= b
	 *               x >= 0
	 */
	public void testQubeSATx3abbcac() {
		double[] cost = new double[] {1, 1, 1, 1, 1};
		double[][] mxA = new double[][] {
			{ 1,  0,  0,  0,  0},
			{ 0,  1,  0,  0,  0},
			{ 0,  0,  1,  0,  0},
			{ 0,  0,  0,  1,  0},
			{ 0,  0,  0,  0,  1},
			{ 1,  1,  1,  0,  0}, //A
			{-1,  1,  0,  1,  0}, //B
			{ 0,  0, -1, -1, -1}, //C
			{ 0,  1,  1,  1,  0}, //A+B
			{-1,  1, -1,  0, -1}, //B+C
			{ 1,  1,  0, -1, -1}, //A+C
		};
		double[]		b	= new double[] { 1, 1, 1, 1, 1, 2, 1, -1, 2, 0, 1};
//		boolean[] nonneg	= new boolean[] {};
		
		double[][] mxB = LinearProgramming.standardToSlackForm(mxA, b);
		boolean[] reversible = new boolean[] {};//irreversible is default
		MetabolicNetwork metaNet = new DefaultMetabolicNetwork(mxB, reversible);
		ElementaryFluxModes.calculateLogMinMax(metaNet, cost, true);
	}

}
