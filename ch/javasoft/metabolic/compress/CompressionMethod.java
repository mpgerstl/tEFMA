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
package ch.javasoft.metabolic.compress;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * The supported compression methods. The enum constants and 
 * collections (arrays) of them can be used to enable/disable
 * specific compression methods.
 * If no user specific configuration is made, STANDARD is used. 
 */
public enum CompressionMethod {
	CoupledZero, CoupledContradicting, CoupledCombine, 
	UniqueFlows, DeadEnd,
	DuplicateGene, DuplicateGeneExtended,
	InterchangeableMetabolite,
	Recursive; 
	/** all compression methods */
	public static final CompressionMethod[] ALL			= values();
	public static final CompressionMethod[] NONE		= new CompressionMethod[] {};
	/** standard uses CoupledZero, CoupledContradicting, CoupledCombine, UniqueFlowsDeadEn, DeadEnd, Recursive, DuplicateGene compression (ommiting InterchangeableMetabolite)*/
	public static final CompressionMethod[] STANDARD	= methods(CoupledZero, CoupledContradicting, CoupledCombine, UniqueFlows, DeadEnd, Recursive, DuplicateGene);
	/** like standard, but without duplicate gene removal, uses CoupledZero, CoupledContradicting, CoupledCombine, UniqueFlows, DeadEnd, Recursive compression (ommiting DuplicateGene, DuplicateGeneExtended and InterchangeableMetabolite)*/
	public static final CompressionMethod[] STANDARD_NO_DUPLICATE = methods(CoupledZero, CoupledContradicting, CoupledCombine, UniqueFlows, DeadEnd, Recursive);
	/** like standard, but without compression, i.e. only removal of inconsistencies and duplicate genes. uses CoupledZero, CoupledContradicting, DeadEnd, DuplicateGene, Recursive compression*/
	public static final CompressionMethod[] STANDARD_NO_COMBINE = methods(CoupledZero, CoupledContradicting, DeadEnd, DuplicateGene, Recursive );
	/** like standard, but without nullspace compression. uses CoupledZero, CoupledContradicting, UniqueFlows, DeadEnd, DuplicateGene, Recursive compression*/
	public static final CompressionMethod[] STANDARD_NO_NULL = methods(UniqueFlows, DeadEnd, DuplicateGene, Recursive );
	/** like standard, but nullspace compression only for removal of zero fluxes. uses CoupledZero, CoupledContradicting, UniqueFlows, DeadEnd, DuplicateGene, Recursive compression*/
	public static final CompressionMethod[] STANDARD_NO_NULL_COMBINE = methods(CoupledZero, CoupledContradicting, UniqueFlows, DeadEnd, DuplicateGene, Recursive );
	public boolean containedIn(CompressionMethod... methods) {
		for (int i = 0; i < methods.length; i++) {
			if (this == methods[i]) return true;
		}
		return false;
	}
	/**
	 * Returns true if any of the methods is {@link #isCoupled() coupled}
	 */
	public static boolean isContainingCoupled(CompressionMethod... methods) {
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isCoupled()) return true;
		}
		return false;
	}
	public static boolean isContainingRecursive(CompressionMethod... methods) {
		return Recursive.containedIn(methods);
	}
	/**
	 * Returns true if any of the methods is {@link #isDublicateGene() duplicate gene}
	 */
	public static boolean isContainingDuplicateGene(CompressionMethod... methods) {
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isDublicateGene()) return true;
		}
		return false;
	}
	
	public static CompressionMethod[] methods(CompressionMethod... methods) {
		return methods;
	}
	/** 
	 * Logs the compression methods. The output string has the form:
	 * "network compression methods are [CoupledZero, Recursive]"
	 * 
	 * @param logLevel	the log level on which the methods should be logged
	 * @param methods	the compression methods to log
	 */
	public static void log(Level logLevel, CompressionMethod ... methods) {
		LogPkg.LOGGER.log(logLevel, "network compression methods are " + Arrays.toString(methods));		
	}
	/**
	 * Logs that the given compression methods are unsupported. The log string
	 * looks like this:
	 * "NOTE: ignoring unsupported network compression methods: CoupledZero, Recursive"
	 * 
	 * @param logLevel	the log level on which to log
	 * @param methods	the compression methods which have been specified
	 * @param supported	the compression methods which are supported
	 */
	public static void logUnsupported(Level logLevel, CompressionMethod[] methods, CompressionMethod... supported) {		
		final StringBuilder sb = new StringBuilder();
		int cnt = 0;
		for (CompressionMethod method : methods) {
			if (!method.containedIn(supported)) {
				if (sb.length() > 0) sb.append(", ");
				sb.append(method);
				cnt++;
			}
		}
		if (cnt > 0) {
			LogPkg.LOGGER.log(logLevel, 
				"NOTE: ignoring unsupported network compression method" +
				(cnt > 1 ? "s" : "") + ": " + sb);				
		}
	}
	
	/**
	 * Returns true if this method is DuplicateGene or DuplicateGeneExtended
	 */
	public boolean isDublicateGene() {
		return this == DuplicateGene || this == DuplicateGeneExtended;
	}
	/**
	 * Returns true if this method is CoupledZero, CoupledContradicting or
	 * CoupledCombine
	 */
	public boolean isCoupled() {
		return this == CoupledZero || this == CoupledContradicting || this == CoupledCombine;		
	}
	
	/**
	 * Returns the methods after removing DuplicateGene and 
	 * DuplicateGeneExtended if contained in methods
	 */
	public static CompressionMethod[] removeDuplicateGeneMethods(CompressionMethod... methods) {
		int cnt = 0;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isDublicateGene()) cnt++;
		}
		if (cnt == 0) return methods;
		CompressionMethod[] res = new CompressionMethod[methods.length - cnt];
		cnt = 0;
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isDublicateGene()) cnt++;
			else res[i - cnt] = methods[i];
		}
		return res;
	}
	
}