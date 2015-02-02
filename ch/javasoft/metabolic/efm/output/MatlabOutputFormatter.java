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
package ch.javasoft.metabolic.efm.output;

import java.io.PrintWriter;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;

/**
 * The <tt>MatlabOutputFormatter</tt> writes the efms to a textfile which can
 * be run as a matlab script. The script re-establishes the efms as matlab  
 * matrices.
 */
public class MatlabOutputFormatter implements EfmOutputFormatter<PrintWriter> {
	
	private boolean formatAsDouble(EfmOutputCallback cb) {
		return cb.getGranularity() == CallbackGranularity.DoubleUncompressed;
	}
	protected String getVariableName() {
		return "efmout";
	}
	public void formatHeader(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt) {
		MetabolicNetwork net = Util.getNetworkUncompressedIfNeeded(cb.getGranularity(), evt);
		
		// print the reaction names
		writer.print(getVariableName() + ".reactions = {");
		boolean first = true;
		for (Reaction reac : net.getReactions()) {
			if (first) first = false;
			else writer.print(", ");
			writer.print("'" + reac.getFullName() + "'");			
		}
		writer.println("};");
		
		if (formatAsDouble(cb)) {
			writer.println(getVariableName() + ".efm = sparse(0, " + net.getReactions().length() + ");");			
		}
	}

	public void formatEfmHeader(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long efmIndex) {
		if (formatAsDouble(cb)) {
			writer.println("% " + getVariableName() + ".efm(" + (efmIndex + 1) + ",:)");
		}
		else {
			writer.print(getVariableName() + ".efm(" + (efmIndex + 1) + ",:) = ('");
		}
	}

	public void formatEfmValue(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value) {
		final double dvalue = value.doubleValue();
		if (formatAsDouble(cb)) {
			if (dvalue != 0d) {
				writer.print(getVariableName() + ".efm(" + (efmIndex + 1) + ", " + (valueIndex + 1) + ")=");
				writer.print(value);
				writer.print("; ");
			}
		}
		else {
			writer.print(dvalue == 0d ? '0' : '1');			
		}
	}

	public void formatEfmFooter(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long efmIndex) {
		if (formatAsDouble(cb)) {
			writer.println();
		}
		else {
			writer.println("' == '1');");			
		}
	}

	public void formatFooter(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long countEfms) {
		writer.println("% " + countEfms + " elementary modes.");		
	}

	public boolean isEfmValueIterationNeeded(EfmOutputCallback cb) {
		return true;
	}
}
