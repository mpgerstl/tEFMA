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
package ch.javasoft.util.logging;

import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <tt>LogWriter</tt> is a special writer to support the log 
 * {@link LogFormatter formatting} functionality provided by this package. 
 * <p>
 * If a program wants to write some output, it might be desired to write the 
 * output to the log file, formatted just like standard log messages. Since the 
 * program possibly writes multiple lines, this log writer uses a cache and
 * flushes complete lines to the log file, using the formatter configured
 * with the used {@link Logger logger}.
 * <p>
 * A sample usage of the log writer involves logging of exception traces. The
 * following code traces an exception to the log file, sample output is given
 * below. Note that a {@link LogPrintWriter} is used here, which is based on
 * the <tt>LogWriter</tt>:
 * <pre>
 	...
	catch (Exception e) {
		Logger logger = Logger.getLogger("mylogger");
		e.printStackTrace(new LogPrintWriter(logger, Level.WARNING));
	}
 * </pre>
 * Sample output looks like this
 * <pre>
2008-09-13  10:47:45.990  main    mylogger         WARNING  | java.lang.ArithmeticException: / by zero
2008-09-13  10:47:45.992  main    mylogger         WARNING  | 	at ch.javasoft.util.logging.LogWriter.divideByZero(LogWriter.java:37)
2008-09-13  10:47:45.992  main    mylogger         WARNING  | 	at ch.javasoft.util.logging.LogWriter.main(LogWriter.java:42)
 * </pre>
 */
public class LogWriter extends Writer {
	
	private final Logger						mLogger;
	private final Level							mLevel;
	private final ThreadLocal<StringBuilder>	mLineBuffer = new ThreadLocal<StringBuilder>() {
		@Override
		protected StringBuilder initialValue() {return new StringBuilder();}
	};
	
	//newline constant
	private static final String NL = System.getProperty("line.separator");
	
	/**
	 * Constructor with logger name and level on which the messages are logged
	 * 
	 * @param loggerName	the logger's name
	 * @param level			the log level to use for tracing
	 */
	public LogWriter(String loggerName, Level level) {
		this(Logger.getLogger(loggerName), level);
	}
	
	/**
	 * Constructor with logger and level on which the messages are logged
	 * 
	 * @param logger	the logger
	 * @param level		the log level to use for tracing
	 */
	public LogWriter(Logger logger, Level level) {
		mLogger = logger;
		mLevel	= level;
	}

	/**
	 * Caches the characters, until a newline is detected. If a newline encoding
	 * is found, the buffered line is flushed to the logger, the remaining 
	 * characters are kept in the buffer.
	 */
	@Override
	public void write(char[] cbuf, int off, int len) {
		final String nl = newLine();
		final int end = off + len;		
		for (int ii = off; ii < end; ii++) {
			int jj = 0;
			while (jj >= 0 && jj < nl.length() && ii + jj < end) {
				if (cbuf[ii + jj] == nl.charAt(jj)) jj++;
				else jj = -1;
			}
			if (jj != -1) {
				//new line found
				mLineBuffer.get().append(cbuf, off, ii - off);
				flush();
				int restLen = len - (ii - off + nl.length());
				if (restLen > 0) {
					write(cbuf, ii + nl.length(), restLen);
				}
				return;
			}
		}
		//no new line found
		mLineBuffer.get().append(cbuf, off, len);
	}
	
	/**
	 * The newline which is used for, default is the platform specific newline
	 * encoding as defined by the <tt>line.separator</tt> system property.
	 * However, subclasses might want to override this method, for instance to
	 * return an operating system independent newline encoding.
	 */
	protected String newLine() {
		return NL;
	}
	
	/**
	 * Flushes the current content in the buffer to the log file, and clears the
	 * line buffer.
	 */
	@Override
	public void flush() {
		final StringBuilder buf = mLineBuffer.get();
		if (buf.length() > 0) {
			mLogger.log(mLevel, buf.toString());
			buf.delete(0, buf.length());
		}
	}

	/**
	 * Calls {@link #flush()}
	 */
	@Override
	public void close() {
		flush();
	}
	
}
