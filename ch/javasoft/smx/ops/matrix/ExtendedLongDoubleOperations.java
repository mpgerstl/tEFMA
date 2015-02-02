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
package ch.javasoft.smx.ops.matrix;

import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.ops.ExtendedMatrixOperations;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.numeric.Zero;

public class ExtendedLongDoubleOperations implements ExtendedMatrixOperations<Long, Double> {
	
	private final Zero 	zero;
	private final Gauss	gauss;
	
	public ExtendedLongDoubleOperations(Zero zero) { 
		this.zero 	= zero;
		this.gauss	= new Gauss(zero.mZeroPos);
	}
	public ExtendedLongDoubleOperations(double tolerance) {
		this(new Zero(tolerance));
	}
	
	public Zero getZero() {
		return zero;
	}
	public Gauss getGauss() {
		return gauss;
	}

	public int rank(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableDoubleMatrix) {
			return gauss.rank((ReadableDoubleMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	public int nullity(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableDoubleMatrix) {
			return gauss.nullity((ReadableDoubleMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}
	public ReadableMatrix<Double> invert(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableDoubleMatrix) {
			return gauss.invert((ReadableDoubleMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}

	public ReadableMatrix<Double> nullspace(ReadableMatrix<Long> mx) {
		if (mx instanceof ReadableDoubleMatrix) {
			return gauss.nullspace((ReadableDoubleMatrix)mx);
		}
		throw new RuntimeException("unsupported matrix type: " + mx.getClass().getName());
	}

}
