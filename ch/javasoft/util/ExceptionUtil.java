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

import java.io.IOException;

/**
 * <tt>ExceptionUtil</tt> is a utility class with static helper methods for
 * exception handling, for instance to convert exceptions to some desired
 * exception type.
 */
public class ExceptionUtil {

	/**
	 * Converts the given <tt>throwable</tt> into a {@link RuntimeException}, if 
	 * necessary, and returns it. 
	 * 
	 * <p>If <tt>throwable</tt> already was a runtime exception, it is returned
	 * as is. If <tt>throwable</tt> was an {@link Error}, it is rethrown. In
	 * every other case, a new runtime exception is created, nesting the 
	 * original <tt>throwable</tt> 
	 * (see {@link RuntimeException#RuntimeException(Throwable)}).
	 * 
	 * <p>The intended use of this method could somehow look like this:
	 * <pre>
	 * void myMethod(...) throws RuntimeException {
	 *   try {
	 *      ... //any kinds of exceptions might occur 
	 *   }
	 *   catch(Throwable th) {
	 *     throw ExceptionUtil.toRuntimeException(th);
	 *     //you never get here (and the compiler knows this)
	 *   }
	 * }
	 * </pre>
	 *  
	 * @param throwable	the given exception to convert, if necessary
	 * @return			the converted exception, nesting the original exception
	 * 					if this was not a {@link RuntimeException} nor an
	 * 					{@link Error}
	 * @throws Error	if <tt>throwable</tt> was an instance of {@link Error}
	 */
	public static RuntimeException toRuntimeException(Throwable throwable) throws Error {
		if (throwable instanceof Error) throw (Error)throwable;
		return throwable instanceof RuntimeException ?
			(RuntimeException)throwable : new RuntimeException(throwable);		
	}
	
	/**
	 * Returns the given <tt>throwable</tt> if it is of the expected type and 
	 * returns it, or converts it into a {@link RuntimeException}, if 
	 * necessary, and throws it. 
	 * 
	 * If <tt>throwable</tt> is of the desired exception class, it is returned.
	 * If not, an {@link Error} is thrown if <tt>throwable</tt> was such.
	 * Otherwise, a {@link RuntimeException} is thrown, either the 
	 * <tt>throwable</tt> itself or a newly created {@link RuntimeException}
	 * nesting <tt>throwable</tt> 
	 * (see {@link RuntimeException#RuntimeException(Throwable)}).
	 *  
	 * <p>The intended use of this method could somehow look like this:
	 * <pre>
	 * void myMethod(...) throws MyException {
	 *   try {
	 *      ... //any kinds of exceptions might occur 
	 *   }
	 *   catch(Throwable th) {
	 *     throw ExceptionUtil.toRuntimeExceptionOr(MyException.class, th);
	 *     //you never get here (and the compiler knows this)
	 *   }
	 * }
	 * </pre>
	 *
	 * @param throwable	the given exception to validate
	 * @return			the validated exception of type <tt>exClass</tt>
	 * @throws Error	if <tt>throwable</tt> was such an instance 
	 * 					and <tt>exClass</tt> was {@link Error} not nor a super 
	 * 					class
	 * @throws RuntimeException	if <tt>throwable</tt> was such an instance 
	 * 					and <tt>exClass</tt> was not {@link RuntimeException}
	 * 					nor a super class. If <tt>throwable</tt> was not an
	 * 					instance of the expected exception class 
	 * 					<tt>exClass</tt>, a runtime exception is throw nesting
	 * 					the original <tt>throwable</tt>. 
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T toRuntimeExceptionOr(Class<T> exClass, Throwable throwable) throws Error, RuntimeException {
		if (exClass.isAssignableFrom(throwable.getClass())) return (T)throwable;
		if (throwable instanceof RuntimeException) throw (RuntimeException)throwable;
		if (throwable instanceof Error) throw (Error)throwable;
		throw new RuntimeException(throwable);
	}
	
	/**
	 * Jdk 1.6++ contains constructors with causes for {@link IOException}, but
	 * older jdk versions do not. This method simulates the missing constructor.
	 * 
	 * @see IOException
	 * @see Exception#Exception(String, Throwable)
	 */
	public static IOException toIOException(String msg, Throwable cause) {
		final IOException ex = new IOException(msg);
		ex.initCause(cause);
		return ex;
	}
	/**
	 * Jdk 1.6++ contains constructors with causes for {@link IOException}, but
	 * older jdk versions do not. This method simulates the missing constructor.
	 * 
	 * @see IOException
	 * @see Exception#Exception(Throwable)
	 */
	public static IOException toIOException(Throwable cause) {
		final IOException ex = new IOException(cause.toString());
		ex.initCause(cause);
		return ex;
	}
	
	// no instances
	private ExceptionUtil() {
		super();
	}

}
