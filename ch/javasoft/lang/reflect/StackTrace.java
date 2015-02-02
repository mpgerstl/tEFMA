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
package ch.javasoft.lang.reflect;

/**
 * The <code>StackTrace</code> class determines the {@link StackTraceElement} of
 * any of the calling methods.
 */
public class StackTrace {
	
	/**
	 * Returns the stack trace element belonging to the direct caller method, 
	 * that is the method which invokes this method.
	 */
	public static StackTraceElement getCurrentMethodStackTraceElement() {
		return internalGetCurrentMethodStackTraceElement(1);
	}
	/**
	 * Returns the stack trace element belonging to the 
	 * <code>callerOffset<code><sup>th</sup> caller of the calling 
	 * method.
	 * 
	 * @param	callerOffset	The index of the caller of the method which 
	 * 							invokes this method
	 */
	public static StackTraceElement getCurrentMethodStackTraceElement(int callerOffset) {
		return internalGetCurrentMethodStackTraceElement(callerOffset + 1);
	}
	private static StackTraceElement internalGetCurrentMethodStackTraceElement(int callerOffset) {
		//trc[0] Thread.dumpThreads(native)
		//trc[1] Thread.getStackTrace(..)
		//trc[2] StackTrace.internalGetCurrentMethodStackTraceElement(..)
		//trc[3] is what we want for offset == 0!
		
		//NOTE sometimes also
		//trc[0] Thread.getStackTrace(..)
		//trc[1] StackTrace.internalGetCurrentMethodStackTraceElement(..)
		//trc[2] is what we want for offset == 0!
		StackTraceElement[] trc = Thread.currentThread().getStackTrace();
		for (int i = 0; i < trc.length; i++) {
			if (trc[i].getClassName().equals(StackTrace.class.getName()) && trc[i].getMethodName().equals("internalGetCurrentMethodStackTraceElement")) {
				return trc[callerOffset + i + 1];
			}
		}
		throw new RuntimeException("internal error: current method not found");
	}
	
	// no instances
	private StackTrace() {}
}
