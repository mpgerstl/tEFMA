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
package ch.javasoft.metabolic.efm.output.text;

import java.io.PrintWriter;
import java.util.Date;

import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.output.EfmOutputFormatter;

/**
 * Superclass for all text formatters. The formatters ptionally writes a header 
 * line with output options and number of modes. Then, the flux values of the 
 * efm's are written in a subclass dependent format. Each mode is printed on a 
 * new line. If desired, the 0-based mode index is prefixed on each line.
 */
abstract public class AbstractTextOutputFormatter implements EfmOutputFormatter<PrintWriter> {
	
	protected final boolean headerLine;
	protected final boolean modeIndex;
	
	/**
	 * Constructor with options
	 * 
	 * @param headerLine	if true, a header line will be added to the output
	 * @param modeIndex		if true, each efm line is prefixed with the mode
	 * 						index (0 based)
	 */
	public AbstractTextOutputFormatter(boolean headerLine, boolean modeIndex) {
		this.headerLine	= headerLine;
		this.modeIndex	= modeIndex;
	}
	
	public void formatHeader(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt) {
		if (headerLine) {
			writer.println("efm-output[" + evt.getEfmCount() + " modes, " + getClass().getSimpleName() + ", " + new Date() + "]");
		}
	}

	public void formatEfmHeader(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long efmIndex) {
		if (modeIndex) {
			writer.print(efmIndex + "\t");
		}
	}

	//implemented by subclasses
	abstract public void formatEfmValue(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value);

	public void formatEfmFooter(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long efmIndex) {
		writer.println();
	}

	public void formatFooter(EfmOutputCallback cb, PrintWriter writer, EfmOutputEvent evt, long countEfms) {
		//nothing to do
	}
	public boolean isEfmValueIterationNeeded(EfmOutputCallback cb) {
		return true;
	}

}
