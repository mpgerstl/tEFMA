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
package ch.javasoft.math.operator;

/**
 * <code>DivisionSupport</code> defines the kind of support for division or
 * inversion operations. It might be important for an algorithm to find out
 * whether it can rely on division operations. 
 * <p>
 * An algorithm usually relies on division if 
 * {@link #mightCauseException()} returns {@code false} <i>and</i>
 * {@link #isSufficientlyExact()} returns {@code true}.
 */
public interface DivisionSupport {
	/**
	 * Constant for full division support with exact division
	 */
	DivisionSupport EXACT = new DivisionSupport() {
		/**
		 * Always returns {@code false}
		 * @see DivisionSupport#alwaysCausesException()
		 */
		public boolean alwaysCausesException() {
			return false;
		}
		/**
		 * Always returns {@code false}
		 * @see DivisionSupport#mightCauseException()
		 */
		public boolean mightCauseException() {
			return false;
		}
		/**
		 * Always returns {@code true}
		 * @see DivisionSupport#isExact()
		 */
		public boolean isExact() {
			return true;
		}
		/**
		 * Always returns {@code true}
		 * @see DivisionSupport#isSufficientlyExact()
		 */
		public boolean isSufficientlyExact() {
			return true;
		}
	};
	/**
	 * Constant for full division support with sufficiently exact division
	 */
	DivisionSupport SUFFICIENTLY_EXACT = new DivisionSupport() {
		/**
		 * Always returns {@code false}
		 * @see DivisionSupport#alwaysCausesException()
		 */
		public boolean alwaysCausesException() {
			return false;
		}
		/**
		 * Always returns {@code false}
		 * @see DivisionSupport#mightCauseException()
		 */
		public boolean mightCauseException() {
			return false;
		}
		/**
		 * Always returns {@code false}
		 * @see DivisionSupport#isExact()
		 */
		public boolean isExact() {
			return false;
		}
		/**
		 * Always returns {@code true}
		 * @see DivisionSupport#isSufficientlyExact()
		 */
		public boolean isSufficientlyExact() {
			return true;
		}
	};
	/**
	 * Returns true if division always causes an {@link ArithmeticException}
	 * because it is not supported.
	 */
	boolean alwaysCausesException();
	
	/**
	 * Returns true if division might cause an {@link ArithmeticException}, not
	 * meaning division by zero, but since rounding or truncation would be 
	 * necessary. 
	 */
	boolean mightCauseException();
	
	/**
	 * Returns true if division is exact. Floating point division is not exact, 
	 * integer division is exact if {@link #alwaysCausesException()} returns
	 * {@code false} and {@link #mightCauseException()} returns {@code true}, 
	 * and fractional division is always exact.  
	 */
	boolean isExact();
	
	/**
	 * Returns true if division is sufficiently exact to be used in an numeric
	 * algorithm. For instance, floating point division is not exact, but exact 
	 * enough to be used, but integer division is not sufficiently exact. 
	 * <p>
	 * Note that division for integers might still be {@link #isExact() exact} 
	 * (and hence also sufficiently exact). If {@link #alwaysCausesException()}
	 * returns {@code false} and {@link #mightCauseException()} returns 
	 * {@code true}, integer division is only supported if no rounding is 
	 * necessary, that is, it is exact.
	 */
	boolean isSufficientlyExact();
}
