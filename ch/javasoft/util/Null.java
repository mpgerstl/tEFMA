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

/**
 * Null is a (single instance) helper object which can be used for equality
 * comparison with null. Its {@link #equals(Object)} method returns <code>true</code>
 * for <code>null</code> and for itself.
 * <p/>
 * Making use of the <code>Null</code> object avoids special case treatment for 
 * <code>null</code> values when checking for equality.
 * <p/>
 * Sample:<br/>
 * <pre>
 *	public int indexOf(Object obj) {
 *		if (obj == null) obj = Null.INSTANCE;
 *		int len = length();
 *		for (int ii = 0; ii < len; ii++) {
 *			if (obj.equals(get(ii))) return ii;
 *		}
 *		return -1;
 *	}
 * </pre>
 */
public class Null {

	/**
	 * The single instance
	 */
	public static final Null INSTANCE = new Null();
	
	/**
	 * private constructor since there is a single instance, 
	 * see {@link #INSTANCE}
	 */
	private Null() {
		super();
	}
	
	/**
	 * @return 0
	 */
	@Override
	public int hashCode() {
		return 0;
	}
	
	/**
	 * @return true for <code>null</code> and for <code>this</code> 
	 */
	@Override
	public boolean equals(Object obj) {
		return obj == null || obj == this;
	}

}
