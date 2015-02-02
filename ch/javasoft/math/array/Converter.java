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
package ch.javasoft.math.array;

import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.operator.ConvertingUnaryOperator;
import ch.javasoft.math.operator.UnaryOperator;

/**
 * The <code>Converter</code> convert values, vectors and matrices from one 
 * number type to another type.
 * 
 * @param <IN>	the type of a single number of the original (I)nput type
 * @param <IA>	the type of an array of numbers of the original (I)nput type
 * @param <RN>	the type of a single number of the target (R)esult type
 * @param <RA>	the type of an array of numbers of the target (R)esult type
 */
public class Converter<IN extends Number, IA, RN extends Number, RA> {
	
	private final NumberArrayOperations<IN, IA> naopsO;
	private final NumberArrayOperations<RN, RA> naopsT;
	private final ArrayOperations<IA> 			aopsO;
	private final ArrayOperations<RA>		 	aopsT;
	private final NumberOperators<IN, IA> 		nopsO;
	private final NumberOperators<RN, RA> 		nopsT;
	private final BooleanUnaryOperator<IN, IA> 	isZeroO;
	private final BooleanUnaryOperator<IN, IA> 	isOneO;
	
	private final ConvertingUnaryOperator<Number, Number[], RN, RA> converter;
	private final ExpressionComposer<IN, IA> 						composerO;
	
	/**
	 * Constructor with number array operations for the original (input) type 
	 * and the target result type
	 * 
	 * @param originalOps	number array operations for original input type
	 * @param targetOps		number array operations for target result type
	 */
	public Converter(final NumberArrayOperations<IN, IA> originalOps, final NumberArrayOperations<RN, RA> targetOps) {
		this.naopsO 	= originalOps;
		this.naopsT 	= targetOps;
		this.aopsO		= originalOps.getArrayOperations();
		this.aopsT		= targetOps.getArrayOperations();
		this.nopsO 		= originalOps.getNumberOperators();
		this.nopsT 		= targetOps.getNumberOperators();
		this.isZeroO 	= nopsO.booleanUnary(BooleanUnaryOperator.Id.isZero);		
		this.isOneO 	= nopsO.booleanUnary(BooleanUnaryOperator.Id.isOne);			
		this.converter	= nopsT.converter();
		this.composerO	= naopsO.getExpressionComposer();
	}
	
	/**
	 * Returns the number type class for a single input value
	 */
	public Class<IN> numberClassInput() {
		return naopsO.numberClass();
	}
	/**
	 * Returns the number type class of an array of input values
	 */
	public Class<IA> arrayClassInput() {
		return naopsO.arrayClass();
	}
	/**
	 * Returns the number array operations for input values
	 */
	public NumberArrayOperations<IN, IA> getNumberArrayOperationsInput() {
		return naopsO;
	}
	/**
	 * Returns the number type class for a single result value
	 */
	public Class<RN> numberClassResult() {
		return naopsT.numberClass();
	}
	/**
	 * Returns the number type class of an array of result values
	 */
	public Class<RA> arrayClassResult() {
		return naopsT.arrayClass();
	}
	/**
	 * Returns the number array operations for result values
	 */
	public NumberArrayOperations<RN, RA> getNumberArrayOperationsResult() {
		return naopsT;
	}
	/**
	 * Returns {@code true} if number class and array class of input and result
	 * are equal
	 */
	public boolean isIdentityConverter() {
		return numberClassInput().equals(numberClassResult()) &&
			arrayClassInput().equals(arrayClassResult());
	}
	
	/**
	 * Converts a single value
	 * 
	 * @param original	the input value to convert
	 * @return	the converted result value
	 */
	public RN convertValue(final IN original) {
		if (naopsT.numberClass().equals(naopsO.numberClass())) {
			return naopsT.numberClass().cast(original);
		}
		//convert
		if (isZeroO.booleanOperate(original)) {
			return nopsT.zero();
		}
		else if (isOneO.booleanOperate(original)) {
			return nopsT.one();
		}
		else {
			return converter.operate(original);
		}
	}
	/**
	 * Converts the values of vector {@code src} to {@code dst}, that is,
	 * <p>
	 * {@code dst[srcStart : srcStart+length-1] = convert (src[dstStart : dstStart+length-1] )}
	 * 
	 * @param src		the input vector to convert
	 * @param srcStart	the first index in {@code src}
	 * @param dst		the converted result vector
	 * @param dstStart	the first index in {@code dst}
	 * @param length	the number of elements to copy
	 */
	public void convertVector(final IA src, int srcStart, final RA dst, int dstStart, int length) {
		if (naopsO.arrayClass().equals(naopsT.arrayClass())) {
			if (src != dst) {
				final RA srcC = naopsT.arrayClass().cast(src);
				naopsT.getArrayOperations().copyVectorElements(srcC, srcStart, dst, dstStart, length);
			}
		}
		else {
			convertVectorInternal(src, srcStart, dst, dstStart, length);
		}		
	}
	/**
	 * Converts and returns a vector. The values in the converted vector are not
	 * normalized.
	 * 
	 * @param original	the input vector to convert
	 * @return	the converted result vector
	 */
	public RA convertVector(final IA original) {
		if (naopsO.arrayClass().equals(naopsT.arrayClass())) {
			return naopsT.arrayClass().cast(original);
		}
		
		final int len = aopsO.getLength(original);
		final RA converted = aopsT.newVector(len);
		return convertVectorInternal(original, 0, converted, 0, len);
	}
	/**
	 * Converts and returns a vector. If {@code normalize} is {@code true}, the
	 * converted values are normalized. If {@code allowScaling} is {@code true}, 
	 * the vector is scaled before conversion if necessary.
	 * <p>
	 * A vector might for instance need scaling if converted into an integer 
	 * vector. Note that the values are scaled in the {@code original} vector.
	 * 
	 * @param original	the input vector to convert
	 * @return	the converted result vector
	 */
	public RA convertVector(final IA original, boolean normalize, boolean allowScaling) {
		final RA converted; 
		if (naopsO.arrayClass().equals(naopsT.arrayClass())) {
			converted = naopsT.arrayClass().cast(original);
		}
		else {
			final int len = aopsO.getLength(original);
			converted = aopsT.newVector(len);
			try {
				convertVectorInternal(original, 0, converted, 0, len);
			}
			catch(ArithmeticException e) {
				if (allowScaling) {
					final IN div = composerO.vectorSqueezeDivisor().operate(original, 0, len);
					if (!isZeroO.booleanOperate(div) && !isOneO.booleanOperate(div)) {
						final UnaryOperator<IN, IA> norm = composerO.normalize(composerO.divFreeBy(composerO.constant(div)));
						naopsO.applyToEachElement(original, original, norm);
					}
					convertVectorInternal(original, 0, converted, 0, len);				
				}
				else {
					throw e;
				}
			}
			
		}
		if (normalize) {
			final ExpressionComposer<RN, RA> composer = naopsT.getExpressionComposer();
			final UnaryOperator<RN, RA> norm = composer.normalize();
			naopsT.applyToEachElement(converted, converted, norm);
		}
		return converted;
	}
	private RA convertVectorInternal(final IA src, int srcStart, final RA dst, int dstStart, final int length) {
		for (int i = 0; i < length; i++) {
			if (isZeroO.booleanOperate(src, i)) {
				naopsT.set(dst, dstStart + i, nopsT.zero());
			}
			else if (isOneO.booleanOperate(src, i)) {
				naopsT.set(dst, dstStart + i, nopsT.one());
			}
			else {
				final Number num = naopsO.get(src, srcStart + i);
				final RN value = converter.operate(num);
				naopsT.set(dst, dstStart + i, value);
			}
		}
		return dst;
	}
	/**
	 * Converts a matrix
	 * 
	 * @param original	the input matrix to convert
	 * @return	the converted result matrix
	 */
	@SuppressWarnings("unchecked")
	public RA[] convertMatrix(final IA[] original) {
		if (naopsO.arrayClass().equals(naopsT.arrayClass())) {
			return (RA[])original;
		}
		return convertMatrixInternal(original);
	}
	/**
	 * Converts and returns a matrix. If {@code normalize} is {@code true}, the
	 * converted values are normalized. If any of the {@code allowScalingXXX}
	 * values is {@code true}, values are scaled before conversion if necessary.  
	 * <p>
	 * A matrix might for instance need scaling if converted into an integer 
	 * matrix. Note that the values are scaled in the {@code original} matrix.
	 * <p>
	 * If {@code allowRowScaling} is {@code true}, row vectors are scaled before 
	 * conversion if necessary. Otherwise, if {@code allowColumnScaling} is 
	 * {@code true}, column vectors are scaled before conversion if necessary.
	 * Last, if {@code allowMatrixScaling} is {@code true}, the matrix is
	 * scaled before conversion if necessary. If no scaling is allowed, the
	 * conversion might fail with an exception.
	 * 
	 * @param original	the input matrix to convert
	 * @return	the converted result matrix
	 */
	public RA[] convertMatrix(final IA[] original, boolean normalize, boolean allowRowScaling, boolean allowColumnScaling, boolean allowMatrixScaling) {
		final RA[] converted;
		if (naopsO.arrayClass().equals(naopsT.arrayClass())) {
			converted = convertMatrix(original);
		}
		else {
			RA[] conv;
			try {
				conv = convertMatrixInternal(original);
			}
			catch(ArithmeticException e) {
				final int rows = aopsO.getRowCount(original);
				final int cols = aopsO.getColumnCount(original);
				if (allowRowScaling) {
					for (int r = 0; r < rows; r++) {
						final IN div = composerO.vectorSqueezeDivisor().operate(original[r], 0, cols);
						if (!isZeroO.booleanOperate(div) && !isOneO.booleanOperate(div)) {
							final UnaryOperator<IN, IA> norm = composerO.normalize(composerO.divFreeBy(composerO.constant(div)));
							naopsO.applyToEachRowElement(original, r, original, r, norm);
						}
					}
					conv = convertMatrixInternal(original);					
				}
				else if (allowColumnScaling) {
					for (int c = 0; c < cols; c++) {
						final IN div = composerO.vectorSqueezeDivisor().operate(original, 0, c, rows, 1);
						if (!isZeroO.booleanOperate(div) && !isOneO.booleanOperate(div)) {
							final UnaryOperator<IN, IA> norm = composerO.normalize(composerO.divFreeBy(composerO.constant(div)));
							naopsO.applyToEachColumnElement(original, c, original, c, norm);
						}
					}
					conv = convertMatrixInternal(original);					
				}
				else if (allowMatrixScaling) {
					final IN div = composerO.vectorSqueezeDivisor().operate(original, 0, 0, rows, cols);
					if (!isZeroO.booleanOperate(div) && !isOneO.booleanOperate(div)) {
						final UnaryOperator<IN, IA> norm = composerO.normalize(composerO.divFreeBy(composerO.constant(div)));
						naopsO.applyToEachElement(original, original, norm);
					}
					conv = convertMatrixInternal(original);					
				}
				else {
					throw e;
				}
			}
			converted = conv;
		}
		if (normalize) {
			final ExpressionComposer<RN, RA> composer = naopsT.getExpressionComposer();
			final UnaryOperator<RN, RA> norm = composer.normalize();
			naopsT.applyToEachElement(converted, converted, norm);
		}
		return converted;
	}
	private RA[] convertMatrixInternal(final IA[] original) {
		final int rows = aopsO.getRowCount(original);
		final int cols = aopsO.getColumnCount(original);
		final RA[] converted = aopsT.newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			converted[r] = convertVector(original[r]);
		}
		return converted;
	}
	
}
