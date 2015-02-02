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

import java.io.IOException;

/**
 * The <tt>AbstractFormattedOutputCallback</tt> is an output callback which uses
 * {@link EfmOutputFormatter output formatters} to format the actual output.
 *
 * @param <W>	the formatter specific output writer
 */
abstract public class AbstractFormattedOutputCallback<W> extends AbstractOutputCallback {

	protected final OutputMode				mMode;
	protected final EfmOutputFormatter<W>	mFormatter;
	protected final W						mWriter;
	
	protected long cnt;
	
	/**
	 * Constructor with output mode, formatter and output writer. All variables
	 * are accessible to subclasses as protected final members.
	 */
	public AbstractFormattedOutputCallback(OutputMode mode, EfmOutputFormatter<W> formatter, W writer) {
		mMode 		= mode;
		mFormatter 	= formatter;
		mWriter		= writer;
	}

	//inherit javadoc comments
	@Override
	protected void callbackPre(EfmOutputEvent evt) throws IOException {
		cnt = 0L;
		mFormatter.formatHeader(this, mWriter, evt);
	}

	//inherit javadoc comments
	@Override
	protected void callbackEfmOut(EfmOutputEvent evt) throws IOException {
		mFormatter.formatEfmHeader(this, mWriter, evt, cnt);
		if (mFormatter.isEfmValueIterationNeeded(this)) {
			for (int i = 0; i < evt.getEfm().getSize(); i++) {
				Number val = evt.getEfm().getNumberRate(i);
				mFormatter.formatEfmValue(this, mWriter, evt, cnt, i, val);			
			}			
		}
		mFormatter.formatEfmFooter(this, mWriter, evt, cnt);
		cnt++;
	}

	//inherit javadoc comments
	@Override
	protected void callbackPost(EfmOutputEvent evt) throws IOException {
		mFormatter.formatFooter(this, mWriter, evt, cnt);
		cnt = 0L;
	}

	//inherit javadoc comments
	public CallbackGranularity getGranularity() {
		return mMode.granularity;
	}

	/**
	 * Returns {@code false}
	 * @see EfmOutputCallback#isThreadSafe()
	 */
	public boolean isThreadSafe() {
		return false;
	}
}
