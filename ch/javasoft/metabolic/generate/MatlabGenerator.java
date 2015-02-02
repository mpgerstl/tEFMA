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
package ch.javasoft.metabolic.generate;

import java.io.PrintWriter;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.util.StoichiometricMatrices;

/**
 * <tt>MatlabGenerator</tt> generates matlab output for a given metabolic 
 * network. The generated matlab .m file creates a structure containing the
 * following fields (if {@link #writeAll(MetabolicNetwork, PrintWriter)} is 
 * called): 
 * <ul>
 * <ul>
 *   <li>{@link #writeModelName(MetabolicNetwork, PrintWriter) modelName}</li>
 *   <li>{@link #writeMetaNames(MetabolicNetwork, PrintWriter) metaboliteNames} and possibly metaboliteDescriptions</li>
 *   <li>{@link #writeReactionNames(MetabolicNetwork, PrintWriter) reactionNames}</li>
 *   <li>{@link #writeReactionFormulas(MetabolicNetwork, PrintWriter) reactionFormulas}</li>
 *   <li>{@link #writeLowerBounds(MetabolicNetwork, PrintWriter) reactionLowerBounds}</li>
 *   <li>{@link #writeUpperBounds(MetabolicNetwork, PrintWriter) reactionUpperBounds}</li>
 *   <li>{@link #writeStoich(MetabolicNetwork, PrintWriter, boolean, boolean) stoich}</li>
 * </ul>
 */
public class MatlabGenerator {
	
	public static final String STRUCTURE_NAME_DEFAULT = "mnet";
	
	private final String mStructureName;
	private final String mModelName;
	
	public MatlabGenerator() {
		this(null);
	}
	public MatlabGenerator(String modelName) {
		this(STRUCTURE_NAME_DEFAULT, modelName);
	}
	public MatlabGenerator(String structureName, String modelName) {
		mStructureName	= structureName;
		mModelName		= modelName;
	}
	
	public String getStructureName() {
		return mStructureName;
	}
	public String getModelName() {
		return mModelName;
	}
	protected void writeMatrix(double[][] mx, String mxName, PrintWriter pw, boolean sparse) {		
		int rows = mx.length;
		int cols = mx.length > 0 ? mx[0].length : 0;
		if (sparse) {
			pw.printf(getStructureName() + "." + mxName + " = sparse(%d, %d);", Integer.valueOf(rows), Integer.valueOf(cols));
			pw.println();
			for (int row = 0; row < rows; row++) {
				boolean first = true;
				for (int col = 0; col < mx[row].length; col++) {
					final double val = mx[row][col];
					if (val != 0d) {
						if (first) first = false;
						else pw.print(" "); 
						pw.printf(getStructureName() + "." + mxName + "(%d, %d)=%s;", Integer.valueOf(row+1), Integer.valueOf(col+1), String.valueOf(val));				
					}
				}
				if (!first) {
					pw.println();
				}
			}
		}
		else {
			pw.print(getStructureName() + "." + mxName + "=[");
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					if (col == 0) pw.println();
					else pw.print(", ");
					pw.print(mx[row][col]);				
				}
			}
			pw.println("];");			
		}
	}
	public void writeStoich(MetabolicNetwork net, PrintWriter pw, boolean expandReversible, boolean sparse) {
		double[][] stoich = expandReversible ?
			StoichiometricMatrices.createStoichiometricMatrixExpandReversible(net) :
			StoichiometricMatrices.createStoichiometricMatrix(net);
		
		int rows = stoich.length;
		int cols = stoich.length > 0 ? stoich[0].length : 0;
		if (sparse) {
			pw.printf(mStructureName + ".stoich = sparse(%d, %d);", Integer.valueOf(rows), Integer.valueOf(cols));
			pw.println();
			for (int col = 0; col < stoich[0].length; col++) {
				boolean first = true;
				for (int row = 0; row < stoich.length; row++) {
					double val = stoich[row][col];
					if (val != 0d) {
						if (first) first = false;
						else pw.print(" "); 
						pw.printf(mStructureName + ".stoich(%d, %d)=%s;", Integer.valueOf(row+1), Integer.valueOf(col+1), String.valueOf(val));				
					}
				}
				if (!first) {
					if (expandReversible) pw.println();
					else {
						Reaction reac = net.getReactions().get(col);
						pw.println(" % " + reac.getName() + ": " + reac);
					}
				}
			}				
		}
		else {
			writeMatrix(stoich, "stoich", pw, sparse);
		}
	}
	public void writeLowerBounds(MetabolicNetwork net, PrintWriter pw) {
		boolean first = true;
		pw.print(mStructureName + ".reactionLowerBounds=[");
		for (Reaction reac : net.getReactions()) {
			if (first) first = false;			
			else pw.print(", ");
			if (reac.getConstraints().isReversible()) pw.print("-Inf");
			else pw.print("0");
		}
		pw.println("];");
	}
	public void writeUpperBounds(MetabolicNetwork net, PrintWriter pw) {
		boolean first = true;
		pw.print(mStructureName + ".reactionUpperBounds=[");
		for (@SuppressWarnings("unused") Reaction reac : net.getReactions()) {
			if (first) first = false;			
			else pw.print(", ");
			pw.print("Inf");
		}
		pw.println("];");
	}
	public void writeMetaNames(MetabolicNetwork net, PrintWriter pw) {
		boolean anyDesc	= false;
		boolean first	= true;
		pw.print(mStructureName + ".metaboliteNames={");
		for (Metabolite meta : net.getMetabolites()) {
			if (first) first = false;			
			else pw.print(", ");
			pw.print('\'');
			pw.print(escape(meta.getName()));
			pw.print('\'');
			anyDesc |= (meta.getDescription() != null);
		}
		pw.println("};");
		if (anyDesc) {
			first = true;
			pw.print(mStructureName + ".metaboliteDescriptions={");
			for (Metabolite meta : net.getMetabolites()) {
				final String desc = meta.getDescription();
				if (first) first = false;		
				else pw.print(", ");
				pw.print('\'');
				pw.print(desc == null ? "" : escape(desc));
				pw.print('\'');
			}
			pw.println("};");
		}
	}
	public void writeReactionNames(MetabolicNetwork net, PrintWriter pw) {
		boolean first = true;
		pw.print(mStructureName + ".reactionNames={");
		for (Reaction reac : net.getReactions()) {
			if (first) first = false;			
			else pw.print(", ");
			pw.print('\'');
			pw.print(escape(reac.getName()));
			pw.print('\'');
		}
		pw.println("};");
	}
	public void writeReactionFormulas(MetabolicNetwork net, PrintWriter pw) {
		boolean first = true;
		pw.print(mStructureName + ".reactionFormulas={");
		for (Reaction reac : net.getReactions()) {
			if (first) first = false;			
			else pw.print(", ");
			pw.print('\'');
			pw.print(escape(reac.toString()));
			pw.print('\'');
		}
		pw.println("};");
	}
	/**
	 * Writes the whole network, reversible reactions are not expanded, the
	 * output matrix is sparse
	 * 
	 * @param net	the network to output
	 * @param pw	the writer to write to
	 */
	public void writeAll(MetabolicNetwork net, PrintWriter pw) {
		writeAll(net, pw, false, true);
	}
	public void writeModelName(MetabolicNetwork net, PrintWriter pw) {
		if (mModelName != null) pw.println(mStructureName + ".modelName='" + mModelName + "';");
	}
	/**
	 * Writes the whole network, reversible reactions are expanded if desired, 
	 * the output matrix are sparse if specified so.
	 * <p>
	 * The following methods are called:
	 * <ul>
	 *   <li>{@link #writeModelName(MetabolicNetwork, PrintWriter)}</li>
	 *   <li>{@link #writeMetaNames(MetabolicNetwork, PrintWriter)}</li>
	 *   <li>{@link #writeReactionNames(MetabolicNetwork, PrintWriter)}</li>
	 *   <li>{@link #writeReactionFormulas(MetabolicNetwork, PrintWriter)}</li>
	 *   <li>{@link #writeLowerBounds(MetabolicNetwork, PrintWriter)}</li>
	 *   <li>{@link #writeUpperBounds(MetabolicNetwork, PrintWriter)}</li>
	 *   <li>{@link #writeStoich(MetabolicNetwork, PrintWriter, boolean, boolean)}</li>
	 * </ul>
	 * 
	 * @param net				the network to output
	 * @param pw				the writer to write to
	 * @param expandReversible	if true, a negated row is appended to the 
	 * 							stoichiometric matrix for every reversible 
	 * 							reaction
	 * @param sparse			if true, matrices are written in matlab sparse 
	 * 							matrix format
	 */
	public void writeAll(MetabolicNetwork net, PrintWriter pw, boolean expandReversible, boolean sparse) {
		writeModelName(net, pw);
		writeMetaNames(net, pw);
		writeReactionNames(net, pw);
		writeReactionFormulas(net, pw);
		writeLowerBounds(net, pw);
		writeUpperBounds(net, pw);
		writeStoich(net, pw, expandReversible, sparse);
		pw.flush();
	}
	private String escape(String str) {
		return str.replace("'", "''");
	}
}
