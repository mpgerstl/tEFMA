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

import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * The <code>JProgress</code> shows progress information in a swing progress
 * window. {@link ProgressMonitor} is used to display the progress information
 * window.
 */
public class JProgress extends AbstractStringProgressWriter {

	private final ProgressMonitor jmonitor;
	/**
	 * Constructor with mode and precision, interpreted as defined by the 
	 * {@link AbstractStringProgressWriter.Mode mode} constant. 
	 */
	public JProgress(Mode mode, int n, int iteration) {
		this(mode, n, iteration, 0, 0);
	}
	public JProgress(Mode mode, int n, int iteration, long posCount, long negCount) {
		super(mode, n);
		final String title;
		if (posCount > 0 && negCount > 0) {
			final NumberFormat nf = (NumberFormat)NumberFormat.getIntegerInstance().clone();
			nf.setGroupingUsed(true);
			title = "Iteration " + iteration + "\nAdjacency candidates to check: " + nf.format(posCount * negCount) + "\n\n";
		}
		else {
			title = "Iteration " + iteration + "\nChecking adjacency candidates...\n\n";
		}
		jmonitor = new ProgressMonitor(null, title, "Progress: 0%", 0, 1000);
	}

	@Override
	protected void write(final double progress, final String msg) throws IOException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (jmonitor.isCanceled()) {
					System.exit(1);
					return;
				}
				jmonitor.setNote("Progress: " + msg);
				jmonitor.setProgress((int)(1000 * progress));
			}
		});
	}

	public void close() throws IOException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jmonitor.close();
			}
		});
	}

}
