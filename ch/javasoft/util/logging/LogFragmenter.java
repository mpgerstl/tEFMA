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

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.util.logging.LogFormatter.LogParameter;

/**
 * The <code>LogFragmenter</code> allows fragmented logging of a log record, 
 * which might for instance result as a single line in the log file. Normally,
 * a record is logged as one line. With this class, multiple log records can be 
 * logged on a single line. 
 * <p>
 * Fragmented logging starts with one of the <tt>xxxStart(..)</tt> methods, for
 * instance, {@link #infoStart(String)} to log with the INFO log level. The 
 * subsequent calls which should appear on the same line are logged with {@link
 * #append(String)}, and the line is completed with {@link #end()} or 
 * {@link #end(String)}.
 * <p>
 * Sample usage:
 * <pre>
 *   Logger logger      = Logger.getLogger("myLogger");
 *   LogFragmenter frag = new LogFragmenter(logger);
 *   frag.start("Counting to 10: ", Level.INFO);
 *   for (int i = 1; i <= 10; i++) {
 *      if (i > 1) frag.append(", ");
 *      frag.append(String.valueOf(i));
 *   }
 *   frag.end();
 * </pre>
 * <p>
 * NOTE: this class is intended to be used with the {@link LogFormatter}.
 */
public class LogFragmenter {

    private final Logger mLogger;
    
    private Level mLevel = null;
    
    /** 
     * Constructor with name of the logger to log to
     * @param logger the name of the logger to log to
     * @see Logger#getLogger(String)
     */
    public LogFragmenter(String logger) {
        this(Logger.getLogger(logger));
    }
    
    /** 
     * Constructor with logger to log to
     * @param logger the logger to log to
     */
    public LogFragmenter(Logger logger) {
        mLogger = logger;
    }
    
    /**
     * Start fragmented logging with the given message at the given level
     * 
     * @param msg	the message to start logging with
     * @param level	the level on which all log messages (until end is called)
     * 				will be logged
     * @throws IllegalStateException	if the logger has already been started
     */
    public void start(String msg, Level level) throws IllegalStateException {
        if (mLevel != null) {
            throw new IllegalStateException("fragment logging already started");
        }
        mLevel = level;
        mLogger.logp(mLevel, null, null, msg, LogParameter.FRAGMENTED_LOG_START);
    }

    /**
     * Start fragmented logging with the given message at log level SEVERE
     * 
     * Calls {@link #start(String, Level)} with {@link Level#SEVERE}
     */
    public void severeStart(String msg) {
    	start(msg, Level.SEVERE);
    }
    /**
     * Start fragmented logging with the given message at log level WARNING
     * 
     * Calls {@link #start(String, Level)} with {@link Level#WARNING}
     */
    public void warningStart(String msg) {
    	start(msg, Level.WARNING);
    }
    /**
     * Start fragmented logging with the given message at log level INFO
     * 
     * Calls {@link #start(String, Level)} with {@link Level#INFO}
     */
    public void infoStart(String msg) {
    	start(msg, Level.INFO);
    }
    /**
     * Start fragmented logging with the given message at log level CONFIG
     * 
     * Calls {@link #start(String, Level)} with {@link Level#CONFIG}
     */
    public void configStart(String msg) {
    	start(msg, Level.CONFIG);
    }
    /**
     * Start fragmented logging with the given message at log level FINE
     * 
     * Calls {@link #start(String, Level)} with {@link Level#FINE}
     */
    public void fineStart(String msg) {
    	start(msg, Level.FINE);
    }
    /**
     * Start fragmented logging with the given message at log level FINER
     * 
     * Calls {@link #start(String, Level)} with {@link Level#FINER}
     */
    public void finerStart(String msg) {
    	start(msg, Level.FINER);
    }
    /**
     * Start fragmented logging with the given message at log level FINEST
     * 
     * Calls {@link #start(String, Level)} with {@link Level#FINEST}
     */
    public void finestStart(String msg) {
    	start(msg, Level.FINEST);
    }
    
    /**
     * Append the given message to the log, usually printed on the same log line
     * 
     * @param msg	the message to append to the log
     * @throws IllegalStateException	if the logger has not yet been started
     */
    public void append(String msg) {
        if (mLevel == null) {
            throw new IllegalStateException("fragment logging not started yet");
        }
        mLogger.logp(mLevel, null, null, msg, LogParameter.FRAGMENTED_LOG_CONTINUE);
    }
    
    /**
     * End fragmented logging without any additional message
     * 
     * @throws IllegalStateException	if the logger has not yet been started
     */
    public void end() {
        end("");
    }
    /**
     * End fragmented logging, appending the given message before logging is 
     * closed
     * 
     * @throws IllegalStateException	if the logger has not yet been started
     */
    public void end(String msg) {
        if (mLevel == null) {
            throw new IllegalStateException("fragment logging not started yet");
        }
        mLogger.logp(mLevel, null, null, msg, LogParameter.FRAGMENTED_LOG_END);
        mLevel = null;
    }
    
    /**
     * End fragmented logging if it is started, or do nothing otherwise
     */
    public void cleanUp() {
    	if (mLevel != null) end();
    }

    /**
     * @return <tt>true</tt> if fragmented logging has been started
     */
    public boolean isStarted() {
    	return mLevel != null;
    }

}
