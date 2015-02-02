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
package ch.javasoft.metabolic.efm.output.mat;

import java.io.File;
import java.io.IOException;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.output.AbstractFormattedOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputFormatter;
import ch.javasoft.metabolic.efm.output.OutputMode;

/**
 * The <code>MatFileOutputCallback</code> writes data to a matlab .mat file.
 * The metabolic network is written to the file (stoich matrix, metabolite and
 * reaction names, reaction upper and lower bounds etc.). If the EFMs are to
 * large for a single file (limited by integer size in bytes), multiple files
 * are written. The network information is only contained in the first file.
 * <p>
 * The mat file contains a structure called mnet with fields for the mentioned
 * information. The number of modes per file is estimated, but can also be
 * predefined.
 */
public class MatFileOutputCallback extends AbstractFormattedOutputCallback<PartitionedMatFileWriter> {

	public static final String STRUCT_NAME = "mnet";

	public MatFileOutputCallback(MetabolicNetwork originalNetwork, File folder, String fileName) throws IOException {
		this(OutputMode.DoubleUncompressed, originalNetwork, folder, fileName);
	}
	public MatFileOutputCallback(MetabolicNetwork originalNetwork, File folder, String fileName, long efmsPerPart) throws IOException {
		this(OutputMode.DoubleUncompressed, originalNetwork, folder, fileName, efmsPerPart);
	}
	public MatFileOutputCallback(OutputMode mode, MetabolicNetwork originalNetwork, File folder, String fileName) throws IOException {
		this(mode, new MatFileOutputFormatter(originalNetwork), createPartitionedMatFileWriter(originalNetwork, mode, folder, fileName));
	}
	public MatFileOutputCallback(OutputMode mode, MetabolicNetwork originalNetwork, File folder, String fileName, long efmsPerPart) throws IOException {
		this(mode, new MatFileOutputFormatter(originalNetwork), createPartitionedMatFileWriter(originalNetwork, mode, folder, fileName, efmsPerPart));
	}
	public MatFileOutputCallback(OutputMode mode, EfmOutputFormatter<PartitionedMatFileWriter> formatter, PartitionedMatFileWriter writer) {
		super(mode, formatter, writer);
	}	

	public boolean allowLoggingDuringOutput() {
		return true;
	}
	
	private static PartitionedMatFileWriter createPartitionedMatFileWriter(MetabolicNetwork originalNetwork, OutputMode mode, File folder, String fileName) {
		final long estimate = estimateEfmsPerPart(originalNetwork, mode);
		LogPkg.LOGGER.info("estimated efms-per-file: " + estimate);
		return new DefaultPartitionedMatFileWriter(folder, fileName, estimate);
	}
	private static PartitionedMatFileWriter createPartitionedMatFileWriter(MetabolicNetwork originalNetwork, OutputMode mode, File folder, String fileName, long efmsPerPart) {
		checkEfmsPerPart(originalNetwork, mode, efmsPerPart);
		return new DefaultPartitionedMatFileWriter(folder, fileName, efmsPerPart);
	}
	
	/** Displays a warning*/
	private static void checkEfmsPerPart(MetabolicNetwork originalNetwork, OutputMode mode, long efmsPerPart) {
		final long estimate = estimateEfmsPerPart(originalNetwork, mode);
		LogPkg.LOGGER.info("estimated efms-per-file: " + estimate);
		LogPkg.LOGGER.info("specified efms-per-file: " + efmsPerPart);
		if (efmsPerPart > estimate) {
			LogPkg.LOGGER.warning("specified efms-per-file is larger than estimated figure: " + efmsPerPart + " > " + estimate);
		}
	}
	
	private static long estimateEfmsPerPart(MetabolicNetwork originalNetwork, OutputMode mode) {
		//8 bytes per value (double), 1 byte for signs
		//offset 4 * stoich should be sufficient (stoich is stored 3 times)
		final int valueSize	= mode.granularity.isBinarySufficient() ? 1 : 8;
		final int offset	= 4 * 8 * originalNetwork.getReactions().length() * originalNetwork.getMetabolites().length();	
		
		int estimate = (Integer.MAX_VALUE - offset) / originalNetwork.getReactions().length() / valueSize;
		LogPkg.LOGGER.finer("estimated efms-per-file (unrounded): " + estimate);
		//round down somewhat (to 1/10 of the size)
		int round = ((int)Math.pow(10, (int)Math.log10(estimate))) / 10;		
		if (round > 0) {
			estimate -= estimate % round;
		}
		return estimate;
	}
}
