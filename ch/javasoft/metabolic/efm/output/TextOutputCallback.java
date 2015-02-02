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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.io.Print;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent.Kind;
import ch.javasoft.metabolic.efm.output.text.AbstractTextOutputFormatter;
import ch.javasoft.metabolic.efm.output.text.BinaryTextOutputFormatter;
import ch.javasoft.metabolic.efm.output.text.DoubleTextOutputFormatter;
import ch.javasoft.metabolic.efm.output.text.SignTextOutputFormatter;
import ch.javasoft.util.logging.LogPrintWriter;

/**
 * The <tt>TextOutputCallback</tt> generates text output for the computed efms.
 * The output format depends on the {@link EfmOutputFormatter} to be used,
 * which is a {@link AbstractTextOutputFormatter} by default. Output streams and
 * writers can be defined as output destinations, by default, logging on log
 * level info is used.
 * 
 * <p>The <tt>TextOutputCallback</tt> as an extension of
 * {@link AbstractFormattedOutputCallback} is a convenience class. It mainly
 * defines some constructors and determines the writer template of the 
 * superclass with {@link PrintWriter}.
 */
public class TextOutputCallback extends AbstractFormattedOutputCallback<PrintWriter> {
	
	private final boolean mAllowLoggingDuringOutput;
	
	/** 
	 * Constructor with given output mode, using a {@link AbstractTextOutputFormatter
	 * text formatter} and writing to the log (log level info).
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode) {
		this(originalNetwork, mode, getOutputFormatter(true, mode)); 
			
	}
	/** 
	 * Constructor with given output mode and formatter, writing to the log 
	 * (log level info).
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param formatter	the formatter defining the concrete output format, e.g.
	 * 					a {@link AbstractTextOutputFormatter} for human readable
	 * 					output, or a {@link MatlabOutputFormatter} to write the
	 * 					efms to a matlab script file
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, EfmOutputFormatter<PrintWriter> formatter) {
		this(originalNetwork, mode, LogPkg.LOGGER, Level.INFO, formatter);
	}
	/** 
	 * Constructor with given output mode and formatter, writing to the logger 
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param logger	the logger to write to
	 * @param level		the log level to be used for output logging
	 * @param formatter	the formatter defining the concrete output format, e.g.
	 * 					a {@link AbstractTextOutputFormatter} for human readable
	 * 					output, or a {@link MatlabOutputFormatter} to write the
	 * 					efms to a matlab script file
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, Logger logger, Level level, EfmOutputFormatter<PrintWriter> formatter) {
		this(originalNetwork, mode, new LogPrintWriter(logger, level), formatter, !mode.granularity.isPerEfmOutput());
	}
	/** 
	 * Constructor with given output mode, using a {@link AbstractTextOutputFormatter}
	 * and writing to the given output stream 
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param out	the output stream to write to
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, OutputStream out) {
		this(originalNetwork, mode, out, getOutputFormatter(false, mode), true);
	}
	/** 
	 * Constructor with given output mode, using a {@link AbstractTextOutputFormatter}
	 * and writing to the given output stream 
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param out	the output stream to write to
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, OutputStream out, boolean allowLoggingDuringOutput) {
		this(originalNetwork, mode, out, getOutputFormatter(false, mode), allowLoggingDuringOutput);
	}
	/** 
	 * Constructor with given output mode and formatter, writing to the given
	 * output stream 
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param out	the output stream to write to
	 * @param formatter	the formatter defining the concrete output format, e.g.
	 * 					a {@link AbstractTextOutputFormatter} for human readable
	 * 					output, or a {@link MatlabOutputFormatter} to write the
	 * 					efms to a matlab script file
	 * @param allowLoggingDuringOutput	set to false if the output goes to the
	 * 									log file and should not be interrupted
	 * 									by log statements
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, OutputStream out, EfmOutputFormatter<PrintWriter> formatter, boolean allowLoggingDuringOutput) {
		super(mode, new UnmappingEfmOutputFormatter<PrintWriter>(formatter, originalNetwork), Print.createWriter(out));
		mAllowLoggingDuringOutput = allowLoggingDuringOutput;
	}
	/** 
	 * Constructor with given output mode and formatter, writing to the given
	 * writer
	 * 
	 * @param originalNetwork the original network for reaction unmapping
	 * @param mode	the output mode, i.e. uncompressed/compressed, binary/double
	 * @param writer	the writer to be used for efm ouput
	 * @param formatter	the formatter defining the concrete output format, e.g.
	 * 					a {@link AbstractTextOutputFormatter} for human readable
	 * 					output, or a {@link MatlabOutputFormatter} to write the
	 * 					efms to a matlab script file
	 * @param allowLoggingDuringOutput	set to false if the output goes to the
	 * 									log file and should not be interrupted
	 * 									by log statements
	 */
	public TextOutputCallback(MetabolicNetwork originalNetwork, OutputMode mode, Writer writer, EfmOutputFormatter<PrintWriter> formatter, boolean allowLoggingDuringOutput) {
		super(mode, new UnmappingEfmOutputFormatter<PrintWriter>(formatter, originalNetwork), Print.createWriter(writer));
		mAllowLoggingDuringOutput = allowLoggingDuringOutput;
	}
	
	private static EfmOutputFormatter<PrintWriter> getOutputFormatter(boolean log, OutputMode mode) {
		switch(mode.granularity) {
			case BinaryUncompressed://fall through
			case BinaryCompressed:
				return new BinaryTextOutputFormatter(log, log);
			case SignUncompressed:
				return new SignTextOutputFormatter(log, log);
			case DoubleUncompressed:
				return new DoubleTextOutputFormatter(log, log);
			default:
				throw new IllegalArgumentException("unsupported mode: " + mode);
		}
	}
	
	/**
	 * Called once at the end of efm outputting. Event kind is
	 * is {@link Kind#POST}.
	 * 
	 * <p>Calls the {@link AbstractFormattedOutputCallback#callbackPost(EfmOutputEvent) superclass method}
	 * and invokes {@link PrintWriter#flush()}.
	 * 
	 * @param evt			the event of kind {@link Kind#POST}
	 * @throws IOException	if an io exception occurs when writing the output
	 */
	@Override
	protected void callbackPost(EfmOutputEvent evt) throws IOException {
		super.callbackPost(evt);
		mWriter.flush();
	}
	
	public boolean allowLoggingDuringOutput() {
		return mAllowLoggingDuringOutput;
	}
	
}
