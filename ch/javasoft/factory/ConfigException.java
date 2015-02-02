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
package ch.javasoft.factory;

/**
 * The <code>ConfigException</code> is thrown by a {@link Factory} if the 
 * configuration is illegal, such that no object can be created by the factory.
 */
public class ConfigException extends FactoryException {

	private static final long serialVersionUID = 6906838910552837071L;

	/**
	 * Constructor for <code>FactoryConfigException</code> with messge and
	 * config path
	 * 
	 * @param message		the error message
	 * @param configPath	path or element in configuration to localize the 
	 * 						possibly faulty entry 
	 */
	public ConfigException(String message, String configPath) {
		super(message + " at " + configPath);
	}

	/**
	 * Constructor for <code>FactoryConfigException</code> with causing error
	 * and config path
	 * 
	 * @param cause			the causing exception
	 * @param configPath	path or element in configuration to localize the 
	 * 						possibly faulty entry 
	 */
	public ConfigException(Throwable cause, String configPath) {
		super("exception caught at " + configPath + ", e=" + cause, cause);
	}

	/**
	 * Constructor for <code>FactoryConfigException</code> with causing message
	 * and causing error and config path
	 * 
	 * @param message		the error message
	 * @param cause			the causing exception
	 * @param configPath	path or element in configuration to localize the 
	 * 						possibly faulty entry 
	 */
	public ConfigException(String message, Throwable cause, String configPath) {
		super(message + " at " + configPath, cause);
	}

}
