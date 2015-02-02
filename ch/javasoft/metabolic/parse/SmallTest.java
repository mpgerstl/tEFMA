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
package ch.javasoft.metabolic.parse;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;
import ch.javasoft.metabolic.parse.junit.AbstractParseTestCase;

/**
 * A collection of small artificial test networks, mostly taken from literature
 * samples, being hard-coded in java.
 */
abstract public class SmallTest extends AbstractParseTestCase {
	
	/**
	 * The sample of the "binary approach" paper, pre-compressed
	 */
	public void testBinSampleCompact() throws Exception {
		double[][] values = new double[][] {
			{ 1,  0, -1, -1, -1},
			{ 0,  1,  0,  1, -1}
		};
		boolean[] reversible = new boolean[] {
			false, false, false, true, false
		};
		String[] metaNames = new String[] {"B", "C"};
		String[] reacNames = new String[] {"R1c", "R2c", "R3c", "R4c", "R5c"};
		MetabolicNetwork net = new DefaultMetabolicNetwork(metaNames, reacNames, values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The sample of the "binary approach" paper
	 */
	public void testBinSample() throws Exception {
		double[][] values = new double[][] {
			{ 1, -1, -1,  0,  0,  0,  0},
			{ 0,  1,  0, -1, -1, -1,  0},
			{ 0,  0,  1,  0,  1, -1,  0},
			{ 0,  0,  0,  0,  0,  1, -1}
		};
		String[] metaNames = new String[] {"A", "B", "C", "D"};
		String[] reacNames = new String[] {"R1", "R2", "R3", "R4", "R5", "R6", "R7"};
		boolean[] reversible = new boolean[] {
			false, false, false, false, true, false, false
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(metaNames, reacNames, values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The sample of the "gemoetry of the flux cone" paper
	 */
	public void testGeneratingSample() throws Exception {
		double[][] values = new double[][] {
			{ 1, -1,  1,  0,  0,  0},	//A
			{ 0,  1,  0,  1,  0, -1},	//B
			{ 0,  0, -1, -1,  1,  0}	//C
		};
		boolean[] reversible = new boolean[] {
			true, true, false, false, false, false
		};
		String[] metaNames = new String[] {"A", "B", "C"};
		String[] reacNames = new String[] {"J1", "J2", "J3", "J4", "J5", "J6"};
		MetabolicNetwork net = new DefaultMetabolicNetwork(metaNames, reacNames, values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The tiny sample of the "System Modeling in Cellular Biology" book
	 */
	public void testBookTiny() throws Exception {
		double[][] values = new double[][] {
			{ 1, -1, -1,  0},
			{ 0,  1,  1, -1}
		};
		boolean[] reversible = new boolean[] {
			false, false, false, false
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(values, reversible);
		internalTestOrDelegate(net, null);		
	}
	
	/**
	 * A simple test case that cause illegal EFMs. The sign of certain flux
	 * values for irreversible reactions was wrong.
	 * <p>
	 * The network is as follows:
	 * <pre>
		'--> S'
		'--> E'
		'S + E <--> ES'
		'ES --> E + P'
		'E -->'
		'P -->'
	 * </pre>
	 * <p>
	 * The following illegal EFMs were produced
	 * <pre>
	     0     1     0     0     1     0
	     1     0    -1     1     0     1
	 * </pre>
	 * <p>
	 * Note that the problem disappears if the reversible reaction is made 
	 * irreversible. It yields the correct EFMs:
	 * <pre>
	     0     1     0     0     1     0
	     1     0     1     1     0     1
	 * </pre>
	 * <p>
	 * The problem was brought up by Markus Uhr.
	 */
	public void testSignProblem() throws Exception {
		double[][] stoich = new double[][] {
			{	1,	0,	-1,	 0,	 0,	 0	},
			{	0,	1,	-1,	 1,	-1,	 0	},
			{	0,	0,	 1,	-1,	 0,	 0	},
			{	0,	0,	 0,	 1,	 0,	-1	}			
		};
		String[] mnames = new String[] {"S", "E", "ES", "P"};
		String[] rnames = new String[] {"S_up", "E_up", "R_ES", "R_P", "E_ex", "P_ex"};
		boolean[] reversible = new boolean[] {
			false, true, true, false, false, false 
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(mnames, rnames, stoich, reversible);
		internalTestOrDelegate(net, null);		
	}
	
	/**
	 * The sample of the "System Modeling in Cellular Biology" book
	 */
	public void testBookSample() throws Exception {
		double[][] values = new double[][] {
			{  1.0 ,  0.0 ,  0.0 ,  0.0 , -1.0 , -1.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 },//A
			{  0.0 ,  1.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , -1.0 , -1.0 ,  0.0 },//B
			{  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  1.0 ,  0.0 , -1.0 },//C
			{  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , -1.0 },//D
			{  0.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 },//E
			{  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  1.0 } //P
		};
		boolean[] reversible = new boolean[] {
			false, true, false, false, false, false, false, true, false, false 
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, but all
	 * reactions are reversible
	 */
	public void testBookSampleRev() throws Exception {
		double[][] values = new double[][] {
			{  1.0 ,  0.0 ,  0.0 ,  0.0 , -1.0 , -1.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 },//A
			{  0.0 ,  1.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , -1.0 , -1.0 ,  0.0 },//B
			{  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  1.0 ,  0.0 , -1.0 },//C
			{  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  0.0 ,  0.0 , -1.0 },//D
			{  0.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 },//E
			{  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,  1.0 } //P
		};
		boolean[] reversible = new boolean[] {
			true, true, true, true, true, true, true, true, true, true 
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, the 
	 * internal reversible reaction was removed
	 */
	public void testBookSampleRemRev() throws Exception {
		double[][] values = new double[][] {
			{  1.0 ,  0.0 ,  0.0 ,  0.0 , -1.0 , -1.0 , -1.0 ,/*  0.0 ,*/  0.0 ,  0.0 },//A
		/*	{  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,   -1.0 ,    0.0 ,  0.0 },//B	*/
			{  0.0 ,  1.0 ,  0.0 ,  0.0 ,  1.0 ,  1.0 ,  0.0 ,/*  1.0 ,*/ -1.0 , -1.0 },//C
			{  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,  1.0 ,/*  0.0 ,*/  0.0 , -1.0 },//D
			{  0.0 ,  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 ,/*  0.0 ,*/  0.0 ,  1.0 },//E
			{  0.0 ,  0.0 , -1.0 ,  0.0 ,  0.0 ,  0.0 ,  0.0 ,/*  0.0 ,*/  1.0 ,  1.0 } //P
		};
		boolean[] reversible = new boolean[] {
			false, true, false, false, false, false, false, /*true,*/ false, false 
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, 
	 * pre-compressed
	 */
	public void testBookMatrixCompressionA() throws Exception {
		double[][] values = new double[][] {
			{  1.0 , -1.0 ,  1.0 ,  0.0},
			{  0.0 , -1.0 ,  0.0 ,  1.0}
		};
		boolean[] reversible = new boolean[] {
			true, false, false, false 
		};
//		String[] metaNames = new String[] {"A", "B", "C", "D", "E", "P"};
//		String[] reacNames = new String[] {"R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10"};
		String[] metaNames = new String[] {"C", "D"};
		String[] reacNames = new String[] {"R8", "R10", "R6", "R7"};
		MetabolicNetwork net = new DefaultMetabolicNetwork(metaNames, reacNames, values, reversible);
		internalTestOrDelegate(net, null);		
	}
	/**
	 * The sample of the "System Modeling in Cellular Biology" book, 
	 * pre-compressed
	 */
	public void testBookMatrixCompressionB() throws Exception {
		double[][] values = new double[][] {
			{  1.0 , -1.0 ,  0.0 ,  0.0},
			{  1.0 ,  0.0 ,  1.0 , -1.0}
		};
//		String[] metaNames = new String[] {"A", "B", "C", "D", "E", "P"};
//		String[] reacNames = new String[] {"R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10"};
		String[] metaNames = new String[] {"E", "P"};
		String[] reacNames = new String[] {"R10", "R4", "R9", "R3"};
		boolean[] reversible = new boolean[] {
			false, false, false, false 
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(metaNames, reacNames, values, reversible);
		internalTestOrDelegate(net, null);		
	}
	
	/**
	 * The irreversible (already extended) example of the paper 
	 * "Nullspace Approach to Determine the Elementary Modes of Chemical Reaction Systems"
	 * by C. Wagner, J. Phys. Chem., 2004
	 */
	public void testNullspaceSampleIrrev() throws Exception {
		double[][] values = new double[][] {
			{  0,  0, -1, -2,  0,  1,  0,  0,  0,  0,  0,  1,  2},
			{  0, -1,  0,  0,  0,  0,  1, -1,  0,  0,  1,  0,  0},
			{  1,  0,  0,  2,  0,  0, -1,  1,  0, -1,  0,  0, -2},
			{  0,  2,  0,  1, -1,  0,  0,  0,  0,  0, -2,  0, -1},
			{  0,  0,  1, -1,  0,  0,  0,  0, -1,  0,  0, -1,  1},
		};
		boolean[] reversible = new boolean[] {
			false, false, false, false, false, false, false, false, false, false, false, false, false 
		};
		MetabolicNetwork net = new DefaultMetabolicNetwork(values, reversible);
		internalTestOrDelegate(net, null);		
	}
}
