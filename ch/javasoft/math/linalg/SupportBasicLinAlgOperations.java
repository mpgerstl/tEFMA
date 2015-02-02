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
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.operator.TernaryOperator;
import ch.javasoft.util.IntArray;

/**
 * The <code>SupportBasicLinAlgOperations</code> class implements
 * {@link BasicLinAlgOperations} based on an instance of 
 * {@link NumberArrayOperations}. It uses bitwise support vectors to exploit
 * sparse matrices for operations of matrix multiplication type.
 * 
 * @type N	the number type of a single number
 * @type A	the number type of an array of numbers
 */
public class SupportBasicLinAlgOperations<N extends Number, A> extends DefaultBasicLinAlgOperations<N, A> {
	
	public SupportBasicLinAlgOperations(NumberOperators<N, A> numberOps, ArrayOperations<A> arrayOps) {
		super(numberOps, arrayOps);
	}
	public SupportBasicLinAlgOperations(NumberArrayOperations<N, A> numberArrayOps) {
		super(numberArrayOps);
	}
	public SupportBasicLinAlgOperations(BasicLinAlgOperations<N, A> basicLinAlgOps) {
		super(basicLinAlgOps.getNumberArrayOperations());
	}

	@Override
	public A[] multiply(A[] m1, A[] m2) {
		final int rows1 = arrayOps.getRowCount(m1);
		final int cols1 = arrayOps.getColumnCount(m1);
		final int rows2 = arrayOps.getRowCount(m2);
		final int cols2 = arrayOps.getColumnCount(m2);
		if (cols1 != rows2) {
			throw new IllegalArgumentException("incompatible dimension for matrix multiplication: " + rows1 + "x" + cols1 + " * " + rows2 + "x" + cols2);
		}
		final TernaryOperator<N, A> mulAdd = expressionComposer.addToFree(expressionComposer.mul());
		
		final A[] res = numberArrayOps.newZeroMatrix(rows1, cols2);
		
		IntArray indices = null;		
		final long[][] rsup = numberArrayOps.getMatrixSupportAsLongBits(m1);
		for (int c = 0; c < cols2; c++) {
			final long[] csup = numberArrayOps.getMatrixColumnSupportAsLongBits(m2, c);
			for (int r = 0; r < rows1; r++) {
				indices = toIndexArray(rsup[r], csup, indices);
				for (int i = 0; i < indices.length(); i++) {
					final int index = indices.get(i);
					//res[r][c] = res[r][c] + m1[r][index]*m2[index][c]
					mulAdd.operate(res[r], c, m1[r], index, m2[index], c, res[r], c);
				}
			}
		}
		return res;
	}
	
	private static IntArray toIndexArray(long[] supportA, long[] supportB, IntArray indices) {
		if (indices == null) {
			indices = new IntArray();
		}
		else {
			indices.clear();
		}
		for (int i = 0; i < supportA.length; i++) {
			long commonSupport = supportA[i] & supportB[i];
			while (commonSupport != 0) {
				final int index = Long.numberOfTrailingZeros(commonSupport);
				indices.add(index + i*64);
				commonSupport ^= (1L << index);
			}
		}
		return indices;
	}

	@Override
	public A multiply(A[] m, A v) {
		final int rows1 = arrayOps.getRowCount(m);
		final int cols1 = arrayOps.getColumnCount(m);
		final int rows2 = arrayOps.getLength(v);
		if (cols1 != rows2) {
			throw new IllegalArgumentException("incompatible dimension for matrix multiplication: " + rows1 + "x" + cols1 + " * " + rows2 + "x1");
		}
		final TernaryOperator<N, A> mulAdd = expressionComposer.addToFree(expressionComposer.mul());
		
		final A res = numberArrayOps.newZeroVector(rows1);

		IntArray indices = null; 
		final long[] csup = numberArrayOps.getVectorSupportAsLongBits(v);
		for (int r = 0; r < rows1; r++) {
			final long[] rsup = numberArrayOps.getMatrixRowSupportAsLongBits(m, r);
			indices = toIndexArray(rsup, csup, indices);
			for (int i = 0; i < indices.length(); i++) {
				final int index = indices.get(i);
				//res[r] = res[r] + m[r][index]*v[index]
				mulAdd.operate(res, r, m[r], index, v, index, res, r);
			}
		}
		return res;
	}

	@Override
	public A multiply(A v, A[] m) {
		final int cols1 = arrayOps.getLength(v);
		final int rows2 = arrayOps.getRowCount(m);
		final int cols2 = arrayOps.getColumnCount(m);
		if (cols1 != rows2) {
			throw new IllegalArgumentException("incompatible dimension for matrix multiplication: " + "1x" + cols1 + " * " + rows2 + "x" + cols2);
		}
		final TernaryOperator<N, A> mulAdd = expressionComposer.addToFree(expressionComposer.mul());
	
		final A res = numberArrayOps.newZeroVector(cols2);
		
		IntArray indices = null; 
		final long[] rsup = numberArrayOps.getVectorSupportAsLongBits(v);
		for (int c = 0; c < cols2; c++) {
			final long[] csup = numberArrayOps.getMatrixColumnSupportAsLongBits(m, c);
			indices = toIndexArray(rsup, csup, indices);
			for (int i = 0; i < indices.length(); i++) {
				final int index = indices.get(i);
				//res[c] = res[c] + v[index]*m[index][c]*
				mulAdd.operate(res, c, v, index, m[index], c, res, c);
			}
		}
		return res;
	}

}
