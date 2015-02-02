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
package ch.javasoft.jbase.util;

import java.io.IOException;

import ch.javasoft.jbase.Table;

/**
 * An <code>UnsupportedOperationException</code> is thrown by 
 * {@link Table table} methods if the concerned operation is not supported. For 
 * instance, unmodifiable tables throw such an exception if a write operation
 * is invoked. 
 */
public class UnsupportedOperationException extends IOException {

	private static final long serialVersionUID = -1611658243538306294L;

	/**
	 * Constructor for <code>UnsupportedOperationException</code> without
	 * message
	 */
	public UnsupportedOperationException() {
		super();
	}

	/**
	 * Constructor for <code>UnsupportedOperationException</code> with message
	 * and nested exception
	 * 
	 * @param message	the exception message
	 * @param cause		the nested exception which caused this exception
	 */
	public UnsupportedOperationException(String message, Throwable cause) {
//		super(message, cause);//only jdk1.6++
		super(message);
		initCause(cause);
	}

	/**
	 * Constructor for <code>UnsupportedOperationException</code> with message
	 * 
	 * @param message	the exception message
	 */
	public UnsupportedOperationException(String message) {
		super(message);
	}

	/**
	 * Constructor for <code>UnsupportedOperationException</code> without 
	 * message but with causing nested exception
	 * 
	 * @param cause		the nested exception which caused this exception
	 */
	public UnsupportedOperationException(Throwable cause) {
//		super(cause);//only for jdk1.6++
		super(cause.getMessage());
		initCause(cause);		
	}

}
