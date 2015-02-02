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
package ch.javasoft.xml.config;

import org.dom4j.Element;

/**
 * The <tt>XmlArgException</tt> is thrown when an input argument or option
 * is missing or of wrong format.
 */
public class XmlArgException extends XmlConfigException {

	public static final String ARG = "arg";
	
	private static final long serialVersionUID = 1L;

	private final String mOption;
	private final int	 mArgIndex;
	
	public XmlArgException(int argIndex, String message, Element path) {
		this(ARG, argIndex, message, path);
	}
	public XmlArgException(String option, String message, Element path) {
		this(option, 0, message, path);
	}
	public XmlArgException(String option, int argIndex, String message, Element path) {
		super(message, path);
		mOption		= option;
		mArgIndex	= argIndex;
	}

	public XmlArgException(int argIndex, String message, String path) {
		this(ARG, argIndex, message, path);
	}
	public XmlArgException(String option, String message, String path) {
		this(option, 0, message, path);
	}
	public XmlArgException(String option, int argIndex, String message, String path) {
		super(message, path);
		mOption		= option;
		mArgIndex	= argIndex;
	}
	
	public String getOption() {
		return mOption;
	}
	public boolean isOption() {
		return !ARG.equals(mOption);
	}
	public String getOptionWithIndex() {
		return mArgIndex == 0 ? getOption() : getOption() + "[" + getArgIndex() + "]";
	}
	
	public int getArgIndex() {
		return mArgIndex;
	}
	
}
