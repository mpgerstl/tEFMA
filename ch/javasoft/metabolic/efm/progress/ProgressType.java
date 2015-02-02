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
package ch.javasoft.metabolic.efm.progress;

import java.io.File;
import java.io.IOException;

import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.progress.AbstractStringProgressWriter.Mode;

/**
 * <code>ProgressType</code> are constants for the config to define which
 * progress writer to use. 
 */
public enum ProgressType {
	/**
	 * Defines that no {@link ProgressMonitor} should be used
	 */
	None {
		@Override
		public ProgressNotifiable createProgressNotifiable(Config config, AdjEnumModel<?> itModel) {
			return null;//indicates that no progress is reported
		}
	},
	/**
	 * Defines that a {@link FileProgressWriter} should be used to monitor the
	 * progress
	 */
	File {
		@Override
		public ProgressNotifiable createProgressNotifiable(Config config, AdjEnumModel<?> itModel) {
			File tempDir = null;
			try {
				tempDir = config.getTempDir().getPersonalizedDir();
			}
			catch (Exception e) {
				tempDir = config.getTempDir().getBaseDir();
			}
			final File progFile = new File(tempDir, "progress-" + itModel.getIterationIndex() + ".txt");
			return new FileProgressWriter(progFile, Mode.Partition, config.getProgressPartition());
		}		
	},
	/**
	 * Defines that a {@link JProgress} instance should be used to monitor the
	 * progress
	 */
	Swing {
		@Override
		public ProgressNotifiable createProgressNotifiable(Config config, AdjEnumModel<?> itModel) throws IOException {
			return new JProgress(
				Mode.Partition, config.getProgressPartition(),					
				itModel.getIterationIndex(), 
				itModel.getMemoryPos().getColumnCount(),
				itModel.getMemoryNeg().getColumnCount()
			);
		}
	};
	
	/**
	 * Returns the xml value, which is the constant's name in lower case
	 */
	public String getXmlValue() {
		return name().toLowerCase();
	}
	/**
	 * Returns the corresponding constant, identified by comparing
	 * <code>xmlValue</code> with {@link #getXmlValue()} of the constant, or
	 * throws an exception if no match is found.
	 * 
	 * @param 	xmlValue	the xml value for to identify the constant
	 * @throws	IllegalArgumentException if no such constant exists
	 */
	public static ProgressType parse(String xmlValue) {
		for (final ProgressType type : values()) {
			if (type.getXmlValue().equals(xmlValue)) return type;
		}
		throw new IllegalArgumentException("no such progress type: " + xmlValue);
	}
	
	/**
	 * Creates a progress notifyable instance according to {@code this} type.
	 * 
	 * @param config		the config
	 * @param itModel		the iteration model of the current iteration
	 * @return the progress notifyable, or null if no progress should be notified
	 * 
	 * @throws IOException	if an I/O exception occurred
	 */
	abstract public ProgressNotifiable createProgressNotifiable(Config config, AdjEnumModel<?> itModel) throws IOException;
	
}
