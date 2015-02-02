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
package ch.javasoft.metabolic.efm.memory.outcore;

import java.io.File;

/**
 * The <code>Recovery</code> class handles recovery of crashed computations from
 * intermediary files.
 * <p>
 * A recovery flag looks for instance like this:<br/>
 * <tt>recover-trees:$recoverdir</tt>
 */
public class Recovery {
	
	public static enum FlagPrefix {
		//order is important, since second is prefix of first
		recover_trees, recover;
		public String configName() {
			return name().replace('_', '-');
		}
		public static FlagPrefix parse(String flag) {
			if (flag == null) return null;
			for (final FlagPrefix prefix : values()) {
				if (flag.startsWith(prefix.configName() + ":")) {
					return prefix;
				}
			}
			return null;
		}
	}
	
	private final FlagPrefix	prefix;
	private final String		flag;
	
	private Recovery(FlagPrefix prefix, String flag) {
		this.prefix	= prefix;
		this.flag	= flag;
	}
	
	/**
	 * Returns a recovery instance, if the flag represents such, and null 
	 * otherwise
	 */
	public static Recovery getRecovery(String flag) {
		final FlagPrefix prefix = FlagPrefix.parse(flag);
		return prefix == null ? null : new Recovery(prefix, flag);
	}
	
	public FlagPrefix getFlagPrefix() {
		return prefix;
	}
	
	public boolean isTreeRecovery() {
		return prefix == FlagPrefix.recover_trees;
	}
	
	public File getRecoveryFolder() {
		return new File(flag.substring(prefix.configName().length() + 1));
	}

}
