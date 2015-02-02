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
package ch.javasoft.math.linalg;

import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.array.Converter;
import ch.javasoft.math.array.ExpressionComposer;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.array.impl.DefaultNumberArrayOperations;
import ch.javasoft.math.operator.AggregatingBinaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.IntUnaryOperator;
import ch.javasoft.math.operator.UnaryOperator;

/**
 * The <code>DefaultBasicLinAlgOperations</code> class implements
 * {@link BasicLinAlgOperations} based on an instance of 
 * {@link NumberArrayOperations}.
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public class DefaultBasicLinAlgOperations<N extends Number, A> implements BasicLinAlgOperations<N, A> {
	
	protected final NumberArrayOperations<N, A> numberArrayOps;
	protected final NumberOperators<N, A> 		numberOps;
	protected final ArrayOperations<A> 			arrayOps;
	protected final ExpressionComposer<N, A> 	expressionComposer;
	
	public DefaultBasicLinAlgOperations(NumberOperators<N, A> numberOps, ArrayOperations<A> arrayOps) {
		this(new DefaultNumberArrayOperations<N, A>(numberOps, arrayOps));
	}
	public DefaultBasicLinAlgOperations(NumberArrayOperations<N, A> numberArrayOps) {
		this.numberArrayOps		= numberArrayOps;
		this.numberOps			= numberArrayOps.getNumberOperators();
		this.arrayOps			= numberArrayOps.getArrayOperations();
		this.expressionComposer = new ExpressionComposer<N, A>(arrayOps, numberOps);
	}

	public final NumberArrayOperations<N, A> getNumberArrayOperations() {
		return numberArrayOps;
	}
	
	public final NumberOperators<N, A> getNumberOperators() {
		return numberOps;
	}

	public final ArrayOperations<A> getArrayOperations() {
		return arrayOps;
	}
	
	public <IN extends Number, IA> Converter<IN, IA, N, A> getConverterFrom(NumberArrayOperations<IN, IA> fromOps) {
		return numberArrayOps.getConverterFrom(fromOps);
	}

	public <RN extends Number, RA> Converter<N, A, RN, RA> getConverterTo(NumberArrayOperations<RN, RA> toOps) {
		return numberArrayOps.getConverterTo(toOps);
	}
	
	public ExpressionComposer<N, A> getExpressionComposer() {
		return expressionComposer;
	}
	
	public Class<N> numberClass() {
		return numberArrayOps.numberClass();
	}

	public Class<A> arrayClass() {
		return numberArrayOps.arrayClass();
	}

	public A abs(A vector) {
		return numberArrayOps.applyToEachElement(vector, numberOps.unary(UnaryOperator.Id.abs));
	}

	public A[] abs(A[] matrix) {
		return numberArrayOps.applyToEachElement(matrix, numberOps.unary(UnaryOperator.Id.abs));
	}

	public A add(A v, A u) {
		return numberArrayOps.applyToElementByElement(v, u, numberOps.binary(BinaryOperator.Id.add));
	}

	public A[] add(A[] m1, A[] m2) {
		return numberArrayOps.applyToElementByElement(m1, m2, numberOps.binary(BinaryOperator.Id.add));
	}

	public A divideElementByElement(A v, A u) {
		return numberArrayOps.applyToElementByElement(v, u, numberOps.binary(BinaryOperator.Id.divide));
	}

	public A[] divideElementByElement(A[] m1, A[] m2) {
		return numberArrayOps.applyToElementByElement(m1, m2, numberOps.binary(BinaryOperator.Id.divide));
	}

	public N get(A vector, int index) {
		return numberOps.unary(UnaryOperator.Id.identity).operate(vector, index);
	}

	public N get(A[] matrix, int row, int col) {
		return numberOps.unary(UnaryOperator.Id.identity).operate(matrix[row], col);
	}

	public A[] multiply(A[] m1, A[] m2) {
		final int rows1 = arrayOps.getRowCount(m1);
		final int cols1 = arrayOps.getColumnCount(m1);
		final int rows2 = arrayOps.getRowCount(m2);
		final int cols2 = arrayOps.getColumnCount(m2);
		if (cols1 != rows2) {
			throw new IllegalArgumentException("incompatible dimension for matrix multiplication: " + rows1 + "x" + cols1 + " * " + rows2 + "x" + cols2);
		}
		final AggregatingBinaryOperator<N, A> inner = numberOps.aggregatingBinary(AggregatingBinaryOperator.Id.innerProduct);
		final A[] res = arrayOps.newMatrix(rows1, cols2);
		for (int r = 0; r < rows1; r++) {
			for (int c = 0; c < cols2; c++) {
				inner.operate(m2, 0, c, m1[r], 0, res[r], c, cols1);
			}
		}
		return res;
	}

	public A multiply(A[] m, A v) {
		final int rows1 = arrayOps.getRowCount(m);
		final int cols1 = arrayOps.getColumnCount(m);
		final int rows2 = arrayOps.getLength(v);
		if (cols1 != rows2) {
			throw new IllegalArgumentException("incompatible dimension for matrix multiplication: " + rows1 + "x" + cols1 + " * " + rows2 + "x1");
		}
		final AggregatingBinaryOperator<N, A> inner = numberOps.aggregatingBinary(AggregatingBinaryOperator.Id.innerProduct);
		final A res = arrayOps.newVector(rows1);
		for (int r = 0; r < rows1; r++) {
			inner.operate(m[r], 0, v, 0, res, r, cols1);
		}
		return res;
	}

	public A multiply(A v, A[] m) {
		final int cols1 = arrayOps.getLength(v);
		final int rows2 = arrayOps.getRowCount(m);
		final int cols2 = arrayOps.getColumnCount(m);
		if (cols1 != rows2) {
			throw new IllegalArgumentException("incompatible dimension for matrix multiplication: " + "1x" + cols1 + " * " + rows2 + "x" + cols2);
		}
		final AggregatingBinaryOperator<N, A> inner = numberOps.aggregatingBinary(AggregatingBinaryOperator.Id.innerProduct);
		final A res = arrayOps.newVector(cols2);
		for (int c = 0; c < cols2; c++) {
			inner.operate(m, 0, c, v, 0, res, c, cols1);
		}
		return res;
	}

	public A multiplyElementByElement(A v, A u) {
		return numberArrayOps.applyToElementByElement(v, u, numberOps.binary(BinaryOperator.Id.multiply));
	}

	public A[] multiplyElementByElement(A[] m1, A[] m2) {
		return numberArrayOps.applyToElementByElement(m1, m2, numberOps.binary(BinaryOperator.Id.multiply));
	}

	public N multiplyInner(A v, A u) {
		return numberArrayOps.applyTo(v, u, numberOps.aggregatingBinary(AggregatingBinaryOperator.Id.innerProduct));
	}

	public A[] multiplyOuter(A v, A u) {
		final int rows = arrayOps.getLength(u);
		final int cols = arrayOps.getLength(v);
		final A[] res = arrayOps.newMatrix(rows, cols);
		
		final BinaryOperator<N, A> mul = numberOps.binary(BinaryOperator.Id.multiply);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				mul.operate(u, r, v, c, res[r], c);
			}
		}
		return res;
	}

	public A negate(A vector) {
		return numberArrayOps.applyToEachElement(vector, numberOps.unary(UnaryOperator.Id.negate));
	}

	public A[] negate(A[] matrix) {
		return numberArrayOps.applyToEachElement(matrix, numberOps.unary(UnaryOperator.Id.negate));
	}

	public int signum(A vector, int index) {
		return numberArrayOps.evalInt(vector, index, numberOps.intUnary(IntUnaryOperator.Id.signum));
	}

	public int signum(A[] matrix, int row, int col) {
		return numberArrayOps.evalInt(matrix[row], col, numberOps.intUnary(IntUnaryOperator.Id.signum));
	}

	public A subtract(A v, A u) {
		return numberArrayOps.applyToElementByElement(v, u, numberOps.binary(BinaryOperator.Id.subtract));
	}

	public A[] subtract(A[] m1, A[] m2) {
		return numberArrayOps.applyToElementByElement(m1, m2, numberOps.binary(BinaryOperator.Id.subtract));
	}
	
}
