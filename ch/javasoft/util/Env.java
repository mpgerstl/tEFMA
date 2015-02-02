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
package ch.javasoft.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the environment settings, i.e. {@link System#getenv()}
 */
public class Env {

	private final Map<String, String> mEnvPlus = new LinkedHashMap<String, String>();
	
	/**
	 * Constructor for <code>Env</code> with no values. Use one of
	 * {@link #toMapAppended()}, {@link #toMapInserted()}, 
	 * {@link #toEnvpAppended()} or {@link #toEnvpInserted()} to append or 
	 * insert the system environment values. 
	 */
	public Env() {
		super();
	}
	
	public void append(String key, String value) {
		mEnvPlus.put(key, value);
	}
	
	/**
	 * Original env stuff first, then the appendings, i.e. original values are
	 * overriden by appended values.
	 */
	public Map<String, String> toMapAppended() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.putAll(java.lang.System.getenv());
		map.putAll(mEnvPlus);
		return map;
	}
	/**
	 * Appendings first, then the original env stuff, i.e. appended values are
	 * defaults only.
	 */
	public Map<String, String> toMapInserted() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.putAll(mEnvPlus);
		map.putAll(java.lang.System.getenv());
		return map;
	}
	public Map<String, String> getAppendings() {
		return mEnvPlus;
	}
	
	/**
	 * Original env stuff first, then the appendings, i.e. original values are
	 * overriden by appended values.
	 */
	public String[] toEnvpAppended() {
		return toEnvp(toMapAppended());
	}
	/**
	 * Appendings first, then the original env stuff, i.e. appended values are
	 * defaults only.
	 */
	public String[] toEnvpInserted() {
		return toEnvp(toMapInserted());
	}
	
	/**
	 * Returns the {@link System#getenv() system environment} as string array
	 */
	public static String[] toEnvp() {
		return toEnvp(java.lang.System.getenv());
	}
	/**
	 * Returns the given map values as string array
	 */
	public static String[] toEnvp(Map<String, String> env) {
		String[] envp = new String[env.size()];
		int index = 0;
		for(Map.Entry<String, String> e : env.entrySet()) {
			envp[index] = e.getKey() + "=" + e.getValue();
			index++;
		}
		return envp;
	}
	
}
