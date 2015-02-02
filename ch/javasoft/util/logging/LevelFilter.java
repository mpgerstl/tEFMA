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

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * A filter for log messages based on the log level. Only log records above or
 * below a certain level pass the filter.
 */
public class LevelFilter implements Filter {
	
	/**
	 * The filter type, somewhat like a comparison operator for the threshold
	 * level and the log record's log level
	 */
	public static enum Type {
		/**
		 * Level of log record must be <i>more severe</i> than the threshold
		 * level.
		 * <p>
		 * Record level <tt>rec.level</tt> passes the filter if
		 * {@code rec.level.intValue() > threshold.intValue()}
		 */
		MoreSevere {
			@Override
			public boolean isLoggable(LogRecord record, Level threshold) {
				return record.getLevel().intValue() > threshold.intValue();
			}
		},
		/**
		 * Level of log record must be <i>at least as severe</i> as the 
		 * threshold level.
		 * <p>
		 * Record level <tt>rec.level</tt> passes the filter if
		 * {@code rec.level.intValue() >= threshold.intValue()}
		 */
		AtLeastAsSevere {
			@Override
			public boolean isLoggable(LogRecord record, Level threshold) {
				return record.getLevel().intValue() >= threshold.intValue();
			}
		},
		/**
		 * Level of log record must be <i>at most as severe</i> as the threshold 
		 * level.
		 * <p>
		 * Record level <tt>rec.level</tt> passes the filter if
		 * {@code rec.level.intValue() <= threshold.intValue()}
		 */
		AtMostAsSevere {
			@Override
			public boolean isLoggable(LogRecord record, Level threshold) {
				return record.getLevel().intValue() <= threshold.intValue();
			}
		},
		/**
		 * Level of log record must be <i>less severe</i> than the threshold 
		 * level.
		 * <p>
		 * Record level <tt>rec.level</tt> passes the filter if
		 * {@code rec.level.intValue() < threshold.intValue()}
		 */
		LessSevere {
			@Override
			public boolean isLoggable(LogRecord record, Level threshold) {
				return record.getLevel().intValue() < threshold.intValue();
			}
		};
		/**
		 * Performs the filtering, constant-specific implementation (see
		 * constant comments for details). Returns true if the record is
		 * loggable.
		 */
		abstract public boolean isLoggable(LogRecord record, Level threshold);
	}
	
	private final Level threshold;
	private final Type	filterType;
	
	/**
	 * Default constructor, reads filter type and threshold from logger config.
	 * <p>
	 * The config is expected to look like this:
	 * <pre
	 * 	ch.javasoft.util.logging.LevelFilter.type		:	LessSevere
	 * 	ch.javasoft.util.logging.LevelFilter.threshold	:	WARNING
	 * </pre>
	 */
	public LevelFilter() {
		this.filterType	= getTypeFromProperties();
		this.threshold	= getThresholdFromProperties();
	}
	
	/**
	 * Read the filter type from the properties file, a log config property 
	 * composed of this class's name and a '.type' suffix.
	 */
	private final Type getTypeFromProperties() {
		final String pname = getClass().getName() + ".type";
		final String str = LogManager.getLogManager().getProperty(pname);
		try {
			return Type.valueOf(str);
		}
		catch (Exception ex) {
			System.err.println("ERROR: could not parse log config " + pname + ", e=" + ex);
			return Type.LessSevere;
		}
	}
	/**
	 * Read the filter threshold from the properties file, a log config property 
	 * composed of this class's name and a '.threshold' suffix.
	 */
	private final Level getThresholdFromProperties() {
		final String pname = getClass().getName() + ".threshold";
		final String str = LogManager.getLogManager().getProperty(pname);
		try {
			return Level.parse(str);
		}
		catch (Exception ex) {
			System.err.println("ERROR: could not parse log config " + pname + ", e=" + ex);
			return Level.WARNING;
		}
	}
	
	/**
	 * Constructor with threshold and filter type. The level of the log record
	 * must be <i>filterType</i> than the threshold level to pass the filter.
	 * 
	 * @param filterType	comparison operator for levels
	 * @param threshold		threshold for right hand side of comparison
	 */
	public LevelFilter(Type filterType, Level threshold) {
		if (filterType == null) {
			throw new NullPointerException("filter type is required");
		}
		if (threshold == null) {
			throw new NullPointerException("threshold is required");
		}
		this.filterType	= filterType;
		this.threshold	= threshold;
	}
	
	/**
	 * Returns true if the level of the log record is <i>filterType</i> than the 
	 * threshold level.
	 */
	public boolean isLoggable(LogRecord record) {
		return filterType.isLoggable(record, threshold);
	}
}
