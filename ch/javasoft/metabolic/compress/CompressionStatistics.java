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
package ch.javasoft.metabolic.compress;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.util.logging.LogWriter;
import ch.javasoft.util.logging.Loggers;

class CompressionStatistics {
	public static final Logger LOG = Loggers.getLogger(LogPkg.LOGGER.getName() + ".stats", -2);
	
	private static enum CompressionTypeR {
		ZeroFlux, Contradicting, Coupled, UniqueFlow, DeadEnd, 
		DuplicateGeneSimple, DuplicateGeneComplex 
	}
	private static enum CompressionTypeM {
		DeadEnd, UniqueFlow, Unused, 
		InterchangeableMetaboliteSimple, InterchangeableMetaboliteComplex
	}
//	private final CompressedMetabolicNetwork mNetwork;
	protected CompressionStatistics() {
		super();
	}
	private int[][] countR = new int[CompressionTypeR.values().length][1];
	private int[][] countM = new int[CompressionTypeR.values().length][1];
	private int compressionIteration	= -1;
	private int maxNonZeroIteration		= -1;
	
	/**
	 * Increments the compression iteration, which is -1 by default. Thus, an
	 * initial incrementation is needed.
	 * Returns the new (increased) iteration count.
	 */
	public int incCompressionIteration() {
		compressionIteration++;
		return compressionIteration;
	}
	public int getCompressionIteration() {
		return compressionIteration;
	}
	public void incZeroFluxReactions() {
		incR(CompressionTypeR.ZeroFlux);
	}
	public void incContradictingReactions() {incR(CompressionTypeR.Contradicting);}
	public void incCoupledReactions() {incR(CompressionTypeR.Coupled, 1);}
	public void incCoupledReactions(int reactionCount) {incR(CompressionTypeR.Coupled, reactionCount);}
	public void incUniqueFlowReactions() {
		incR(CompressionTypeR.UniqueFlow);
		incM(CompressionTypeM.UniqueFlow);
	}
	public void incDeadEndMetaboliteReactions(int reactionCount) {
		incR(CompressionTypeR.DeadEnd, reactionCount);
		incM(CompressionTypeM.DeadEnd);
	}
	public void incUnusedMetabolite() {
		incM(CompressionTypeM.Unused);
	}
	public void incDuplicateGeneReactions(int reactionCount) {incR(CompressionTypeR.DuplicateGeneSimple, reactionCount);}
	public void incDuplicateGeneCompoundReactions(int reactionCount) {incR(CompressionTypeR.DuplicateGeneComplex, reactionCount);}
	public void incInterchangeableMetabolites() {incM(CompressionTypeM.InterchangeableMetaboliteSimple);}
	public void incInterchangeableMetabolitesComplex() {incM(CompressionTypeM.InterchangeableMetaboliteComplex);}
	private void incR(CompressionTypeR type) {
		countR[type.ordinal()] = inc(countR[type.ordinal()]);
	}
	private void incM(CompressionTypeM type) {
		countM[type.ordinal()] = inc(countM[type.ordinal()]);
	}
	private void incR(CompressionTypeR type, int inc) {
		for (int i = 0; i < inc; i++) {
			countR[type.ordinal()] = inc(countR[type.ordinal()]);				
		}
	}
	private int[] inc(int[] me) {
		if (me.length <= compressionIteration) {
			int[] arr = new int[Math.max(compressionIteration + 1, me.length * 2)];
			System.arraycopy(me, 0, arr, 0, me.length);
			me = arr;
		}
		me[compressionIteration]++;
		maxNonZeroIteration = compressionIteration;
		return me;
	}
	private int getR(int which, int iteration) {
		return get(countR[which], iteration);
	}
	private int getM(int which, int iteration) {
		return get(countM[which], iteration);
	}
	private int get(int[] me, int iteration) {
		return iteration < me.length ? me[iteration] : 0;
	}
	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		write(sw);
		return sw.toString();
	}
	public void writeToLog() {
		writeToLog(Level.FINE);
	}
	public void writeToLog(Level level) {
		write(new LogWriter(LOG, level));
	}
	public void write(OutputStream out) {
		write(new PrintWriter(new OutputStreamWriter(out)));
	}
	public void write(Writer writer) {
		PrintWriter pw = writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer);
		pw.println("compression statistics");
		for (int iteration = 0; iteration <= maxNonZeroIteration; iteration++) {
			for (int which = 0; which < CompressionTypeM.values().length; which++) {
				int value = getM(which, iteration);
				pw.println("  meta[" + iteration + "]." + CompressionTypeM.values()[which] + " = " + value);
			}
			for (int which = 0; which < CompressionTypeR.values().length; which++) {
				int value = getR(which, iteration);
				pw.println("  reac[" + iteration + "]." + CompressionTypeR.values()[which] + " = " + value);
			}			
		}
		int totalMeta = 0;
		for (int which = 0; which < CompressionTypeM.values().length; which++) {
			int total = 0;
			for (int iteration = 0; iteration <= maxNonZeroIteration; iteration++) {
				total += getM(which, iteration);
			}			
			pw.println("  meta." + CompressionTypeM.values()[which] + " = " + total);
			totalMeta += total;
		}
		int totalReac = 0;
		for (int which = 0; which < CompressionTypeR.values().length; which++) {
			int total = 0;
			for (int iteration = 0; iteration <= maxNonZeroIteration; iteration++) {
				total += getR(which, iteration);
			}			
			pw.println("  reac." + CompressionTypeR.values()[which] + " = " + total);
			totalReac += total;
		}
		pw.println("  meta = " + totalMeta);
		pw.println("  reac = " + totalReac);
		pw.flush();
	}
//	public void write(Writer writer) {
//		PrintWriter pw = writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer);
//		for (int which = 0; which < CompressionTypeR.values().length; which++) {
//			int total = 0;
//			for (int iteration = 0; iteration <= compressionIteration; iteration++) {
//				int value = get(which, iteration);
//				pw.println("  ." + CompressionTypeR.values()[which] + "[" + iteration + "] = " + value);
//				total += value;
//			}			
//			pw.println("  ." + CompressionTypeR.values()[which] + " = " + total);
//		}
//		pw.println("  .zero-flux-metas: ");
//		writeZeroFluxMetas("  ... ", pw);
//		pw.flush();
//	}
//	private void writeZeroFluxMetas(String indent, PrintWriter pw) {
//		Map<Metabolite, int[]> metaSet = new HashMap<Metabolite, int[]>();
//		for (Reaction react : deadEndReactions) {
//			for (MetaboliteRatio ratio : react.getMetaboliteRatios()) {
//				int[] cnt = metaSet.get(ratio.getMetabolite());
//				if (cnt == null) {
//					metaSet.put(ratio.getMetabolite(), cnt = new int[] {0, 0});
//				}
//				cnt[0]++;
//			}
//		}
//		for (Reaction react : mNetwork.getParentNetwork().getReactions()) {
//			for (MetaboliteRatio ratio : react.getMetaboliteRatios()) {
//				int[] cnt = metaSet.get(ratio.getMetabolite());
//				if (cnt != null) {
//					cnt[1]++;
//				}
//			}	
//		}
//		List<Map.Entry<Metabolite, int[]>> list = new ArrayList<Map.Entry<Metabolite, int[]>>();
//		list.addAll(metaSet.entrySet());
//		Collections.sort(list, new Comparator<Map.Entry<Metabolite, int[]>>() {
//			public int compare(Entry<Metabolite, int[]> o1, Entry<Metabolite, int[]> o2) {
//				double perc1 = getPerc(o1.getValue());
//				double perc2 = getPerc(o2.getValue());
//				return perc1 == perc2 ? o2.getValue()[0] - o1.getValue()[0] : perc1 > perc2 ? -1 : 1;
//			}
//		});
//		for (Map.Entry<Metabolite, int[]> entry : list) {
//			pw.print(indent);
//			pw.print(entry.getKey());
//			pw.print(": ");
//			pw.print(NumberFormat.getPercentInstance().format(getPerc(entry.getValue())));
//			pw.print(" [");
//			pw.print(entry.getValue()[0]);
//			pw.print("/");
//			pw.print(entry.getValue()[1]);
//			pw.println("]");
//		}
//	}
//	private double getPerc(int[] frac) {
//		return frac[1] == 0 ? Double.POSITIVE_INFINITY : ((double)frac[0]) / frac[1];
//	}
}