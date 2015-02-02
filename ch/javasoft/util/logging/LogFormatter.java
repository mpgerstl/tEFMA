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

import java.text.Format;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * The <tt>LogFormatter</tt> formats log statements with the help of a 
 * {@link MessageFormat}. The format string can be configured in the log 
 * configuration (<tt>ch.javasoft.util.logging.LogFormatter.format</tt> property).
 * <p>
 * The following arguments are passed to the message format:
 * <ul>	<li><tt>0</tt> current date/time</li>
 * 		<li><tt>1</tt> name of current thread</li>
 * 		<li><tt>2</tt> logger name</li>
 * 		<li><tt>3</tt> log level</li>
 * 		<li><tt>4</tt> log message</li>
 * 		<li><tt>5</tt> termination</li></ul>
 * Note that all strings (all arguments but 0 and 5) have fixed length, meaning
 * that the strings are either truncated or filled with spaces.
 * <p>
 * For instance, the {@link #FORMAT_DEFAULT default format} produces log 
 * statements like this:
 * <pre>
 * 2004-10-13  22:18:50.932  main        JavaLogger      INFO	mymessage\n
 * </pre>
 * <p>
 * Others might prefere the more compact {@link #FORMAT_PLAIN plain format}:
 * <pre>
 * mymessage\n
 * </pre>
 */
public class LogFormatter extends Formatter {
    
	/**
	 * Constants for interplay with {@link LogFragmenter}
	 */
    public static enum LogParameter {
    	/**
    	 * Parameter indicating that fragmented logging started
    	 */
    	FRAGMENTED_LOG_START, 
    	/**
    	 * Parameter indicating that fragmented logging is to continue
    	 */
    	FRAGMENTED_LOG_CONTINUE, 
    	/**
    	 * Parameter indicating that fragmented logging ends
    	 */
    	FRAGMENTED_LOG_END
    }

    //newline constant
    private static final String NL = System.getProperty("line.separator");
    
    /**
     * Default log format, a sample log line looks like this:
     * <pre>
     * 2004-10-13  22:18:50.932  main        JavaLogger      INFO	mymessage\n
     * </pre>
     * To configure this format, set the
     * <tt>ch.javasoft.util.logging.LogFormatter.format</tt> logging config 
     * property to
     * <pre>
     * {0,date,yyyy-MM-dd}  {0,time,HH:mm:ss.SSS}  {1}  {2}  {3}  | {4}{5}
     * </pre>
     */
	public static final MessageFormat FORMAT_DEFAULT = new MessageFormat(
		"{0,date,yyyy-MM-dd}  {0,time,HH:mm:ss.SSS}  {1}  {2}  {3}  | {4}{5}"
	);
    /**
     * Plain log format, a sample log line looks like this:
     * <pre>
     * mymessage\n
     * </pre>
     * To configure this format, set the
     * <tt>ch.javasoft.util.logging.LogFormatter.format</tt> logging config 
     * property to
     * <pre>
     * {4}{5}
     * </pre>
     */
	public static final MessageFormat FORMAT_PLAIN = new MessageFormat(
		"{4}{5}"
	);
	
	/**
	 * Read the logger format from the properties file
	 */
	/**
	 * Read the logger format from the properties file, a log config property 
	 * composed of this class's name and a '.format' suffix.
	 */
	protected MessageFormat getFormatFromProperties() {
		final String pname = getClass().getName() + ".format";
		final String fmt = LogManager.getLogManager().getProperty(pname);
		return new MessageFormat(fmt);
	}
	/**
	 * Returns the default format. The default format is read from the logger 
	 * properties file (see {@link #getFormatFromProperties()}), if this is not
	 * possible, for instance since no property exists of due to an invalid
	 * format expression, {@link #FORMAT_DEFAULT} is used instead.
	 */
	public final MessageFormat getDefaultFormat() {
		try {
			return getFormatFromProperties();
		}
		catch (Exception e) {
			System.err.println("ERROR: Using default logger format, cannot use format from properties file, e=" + e);
			return FORMAT_DEFAULT;
		}
	}
	
	private final Format mFormat;
	
	/**
	 * Constructor using the {@link #getDefaultFormat() default format}.
	 */
	public LogFormatter() {
		mFormat = getDefaultFormat();
	}
	
	/**
	 * Constructor using the specified {@code format}
	 */
	public LogFormatter(Format format) {
		mFormat = format;
	}

	/**
	 * Formats the record as described in the {@link LogFormatter class 
	 * comments}.
	 */
	@Override
	public String format(LogRecord record) {
	    LogParameter param = getLogParameter(record);
        final String termination;
        if (LogParameter.FRAGMENTED_LOG_START.equals(param) || LogParameter.FRAGMENTED_LOG_CONTINUE.equals(param)) {
            termination = "";
        }
        else {
            termination = NL;
        }
        if (LogParameter.FRAGMENTED_LOG_CONTINUE.equals(param) || LogParameter.FRAGMENTED_LOG_END.equals(param)) {
            return record.getMessage() + termination;
        }
        return mFormat.format(
            new Object[] {
                new Date(),
                fixedLen(Thread.currentThread().getName(), 6, ' ', false),
                fixedLen(record.getLoggerName(), 15, ' ', false),
                fixedLen(record.getLevel().getName(), 7, ' ', false),
                record.getMessage(),
                termination
            }
        );
	}
    
    private LogParameter getLogParameter(LogRecord record) {
        Object[] params = record.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
            	for (final LogParameter param : LogParameter.values()) {
                    if (param.equals(params[i])) return param;
                }
            }
        }
        return null;
    }

	/** 
	 * Cut string or append fillers to the given string to fit the requested length
	 * 
	 * @param str		The string to format with a fixed length
	 * @param len		The desired length
	 * @param filler	The filler char for strings being to short
	 * @param left		True if the filler should be (left) inserted, false if it should be (right) appended
	 */
	private static String fixedLen(String str, int len, char filler, boolean left) {
		if (str == null) str = "" + null;
		if (str.length() < len) {
			StringBuffer sb = new StringBuffer(str);
			do {
				if (left) sb.insert(0, filler);
				else sb.append(filler);
			} 
			while (sb.length() < len);
			str = sb.toString();
		}
		else if (str.length() > len) {
			str = str.substring(0, len);
		}
		return str;
	}
}
