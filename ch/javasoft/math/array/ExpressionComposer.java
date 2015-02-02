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

import java.math.BigInteger;

import ch.javasoft.math.array.impl.DefaultNumberArrayOperations;
import ch.javasoft.math.linalg.BasicLinAlgOperations;
import ch.javasoft.math.operator.AggregatingBinaryOperator;
import ch.javasoft.math.operator.AggregatingUnaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.BooleanBinaryOperator;
import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.operator.ConvertingUnaryOperator;
import ch.javasoft.math.operator.DivisionSupport;
import ch.javasoft.math.operator.IntBinaryOperator;
import ch.javasoft.math.operator.IntUnaryOperator;
import ch.javasoft.math.operator.NAryOperator;
import ch.javasoft.math.operator.NullaryOperator;
import ch.javasoft.math.operator.QuaternaryOperator;
import ch.javasoft.math.operator.TernaryOperator;
import ch.javasoft.math.operator.UnaryOperator;
import ch.javasoft.math.operator.compose.BinaryBinaryOperator;
import ch.javasoft.math.operator.compose.BinaryNullaryOperator;
import ch.javasoft.math.operator.compose.BinaryUnaryOperator;
import ch.javasoft.math.operator.compose.ConcatBinaryOperator;
import ch.javasoft.math.operator.compose.ConcatNAryOperator;
import ch.javasoft.math.operator.compose.ConcatNullaryOperator;
import ch.javasoft.math.operator.compose.ConcatQuaternaryOperator;
import ch.javasoft.math.operator.compose.ConcatTernaryOperator;
import ch.javasoft.math.operator.compose.ConcatUnaryOperator;
import ch.javasoft.math.operator.compose.NAryNAryOperator;
import ch.javasoft.math.operator.compose.NullaryBinaryOperator;
import ch.javasoft.math.operator.compose.NullaryNullaryOperator;
import ch.javasoft.math.operator.compose.NullaryQuaternaryOperator;
import ch.javasoft.math.operator.compose.NullaryTernaryOperator;
import ch.javasoft.math.operator.compose.NullaryUnaryOperator;
import ch.javasoft.math.operator.compose.QuaternaryNullaryOperator;
import ch.javasoft.math.operator.compose.TernaryNullaryOperator;
import ch.javasoft.math.operator.compose.TernaryUnaryOperator;
import ch.javasoft.math.operator.compose.UnaryBinaryOperator;
import ch.javasoft.math.operator.compose.UnaryNullaryOperator;
import ch.javasoft.math.operator.compose.UnaryTernaryOperator;
import ch.javasoft.math.operator.compose.UnaryUnaryOperator;

/**
 * The <code>ExpressionComposer</code> is a convenience class to compose numeric 
 * expressions. If offers direct methods for the most common operations.
 * <p>
 * For instance, consider an operation
 * <pre>
 * f(x1,x2,x3) = | x1 - x2 * x3 |
 * </pre>
 * The expression is derived as follows:
 * <pre>
 * f = abs(subFromFree(mul()));
 * </pre>
 * You can now invoke the function for different value instances:
 * <pre>
 * result = f.operate(x1, x2, x3)
 * </pre>
 */
public class ExpressionComposer<N extends Number, A> {
	
	private final ArrayOperations<A> 	arrayOps;
	private final NumberOperators<N, A> operators;

	/**
	 * Constructor with array operations and operators
	 */
	public ExpressionComposer(ArrayOperations<A> arrayOps, NumberOperators<N, A> operators) {
		this.arrayOps	= arrayOps;
		this.operators	= operators;		
	}
	/**
	 * Constructor with number array operations
	 */
	public ExpressionComposer(NumberArrayOperations<N, A> numberArrayOps) {
		this(numberArrayOps.getArrayOperations(), numberArrayOps.getNumberOperators());
	}
	/**
	 * Constructor with basic linalg operations
	 */
	public ExpressionComposer(BasicLinAlgOperations<N, A> numberArrayOps) {
		this(numberArrayOps.getArrayOperations(), numberArrayOps.getNumberOperators());
	}
	
	/**
	 * Returns the number type class for a single value
	 */
	public Class<N> numberClass() {
		return operators.numberClass();
	}

	/**
	 * Returns the number type class of an array of values
	 */
	public Class<A> arrayClass() {
		return operators.arrayClass();
	}
	
	/**
	 * Returns the kind of {@link DivisionSupport} for numbers of the current 
	 * type {@code N}.
	 */
	public DivisionSupport getDivisionSupport() {
		return operators.getDivisionSupport();
	}

	/**
	 * Returns the array operations, used to create, copy and handle vectors and
	 * matrices
	 */
	public ArrayOperations<A> getArrayOperations() {
		return arrayOps;
	}
	
	/**
	 * Returns the number operators for the number type of this operations
	 */
	public NumberOperators<N, A> getNumberOperators() {
		return operators;
	}

	/**
	 * Returns the number array operations, used to apply numeric operators to 
	 * vectors or matrices
	 */
	public NumberArrayOperations<N, A> getNumberArrayOperations() {
		return new DefaultNumberArrayOperations<N, A>(operators, arrayOps);
	}

	// constants
	/**
	 * Returns the zero constant
	 */
	public N zero() {
		return operators.zero();
	}
	/**
	 * Returns the one constant
	 */
	public N one() {
		return operators.one();
	}

	// nullary operators
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = 0
	 * </pre>
	 */
	public NullaryOperator<N, A> constantZero() {
		return operators.nullary(NullaryOperator.Id.zero);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = 1
	 * </pre>
	 */
	public NullaryOperator<N, A> constantOne() {
		return operators.nullary(NullaryOperator.Id.one);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = value
	 * </pre>
	 */
	public NullaryOperator<N, A> constant(N value) {
		return operators.constant(value);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = rnd
	 * </pre>
	 * Random operator, returns a pseudo random number, usually with 
	 * approximately uniform distribution in the number range. For number 
	 * formats supporting (nearly) infinite size, such as {@link BigInteger} 
	 * numbers, uniformity might be restricted to a subrange.
	 * 
	 * @see NullaryOperator.Id#random
	 * @see Math#random()
	 */
	public NullaryOperator<N, A> rnd() {
		return operators.nullary(NullaryOperator.Id.random);
	}
	
	// unary operators

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = |x|
	 * </pre>
	 */
	public UnaryOperator<N, A> abs() {
		return operators.unary(UnaryOperator.Id.abs);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = -x
	 * </pre>
	 */
	public UnaryOperator<N, A> neg() {
		return operators.unary(UnaryOperator.Id.negate);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = 1/x
	 * </pre>
	 */
	public UnaryOperator<N, A> inv() {
		return operators.unary(UnaryOperator.Id.invert);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = normalize(x)
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public UnaryOperator<N, A> normalize() {
		return operators.unary(UnaryOperator.Id.normalize);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = x<sup>2</sup>
	 * </pre>
	 */
	public UnaryOperator<N, A> square() {
		return operators.unary(UnaryOperator.Id.square);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = x
	 * </pre>
	 */
	public UnaryOperator<N, A> identity() {
		return operators.unary(UnaryOperator.Id.identity);
	}
	
	// boolean unary operators

	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x == 0 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isZero() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isZero);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x != 0 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isNonZero() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isNonZero);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x == 1 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isOne() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isOne);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x < 0 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isNegative() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isNegative);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x >= 0 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isNonNegative() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isNonNegative);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x > 0 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isPositive() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isPositive);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x) = ( x <= 0 )
	 * </pre>
	 */
	public BooleanUnaryOperator<N, A> isNonPositive() {
		return operators.booleanUnary(BooleanUnaryOperator.Id.isNonPositive);
	}
	
	// boolean binary operators

	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x1,x2) = ( x1 == x2 )
	 * </pre>
	 */
	public BooleanBinaryOperator<N, A> isEqual() {
		return operators.booleanBinary(BooleanBinaryOperator.Id.equal);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x1,x2) = ( x1 != x2 )
	 * </pre>
	 */
	public BooleanBinaryOperator<N, A> isUnequal() {
		return operators.booleanBinary(BooleanBinaryOperator.Id.unequal);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x1,x2) = ( x1 >= x2 )
	 * </pre>
	 */
	public BooleanBinaryOperator<N, A> isGreaterOrEqual() {
		return operators.booleanBinary(BooleanBinaryOperator.Id.greaterOrEqual);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x1,x2) = ( x1 > x2 )
	 * </pre>
	 */
	public BooleanBinaryOperator<N, A> isGreater() {
		return operators.booleanBinary(BooleanBinaryOperator.Id.greater);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x1,x2) = ( x1 <= x2 )
	 * </pre>
	 */
	public BooleanBinaryOperator<N, A> isLessOrEqual() {
		return operators.booleanBinary(BooleanBinaryOperator.Id.lessOrEqual);
	}
	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *   operator(x1,x2) = ( x1 < x2 )
	 * </pre>
	 */
	public BooleanBinaryOperator<N, A> isLess() {
		return operators.booleanBinary(BooleanBinaryOperator.Id.less);
	}
	
	// int unary operators

	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *                 { -1  if  x < 0
	 *   operator(x) = {  0  if  x == 0
	 *                 { +1  if  x > 0 
	 * </pre>
	 */
	public IntUnaryOperator<N, A> sign() {
		return operators.intUnary(IntUnaryOperator.Id.signum);
	}

	// int binary operators

	/**
	 * Returns a boolean operator performing
	 * <pre>
	 *                     { -1  if  x1 < x2 
	 *   operator(x1,x2) = {  0  if  x1 == x2
	 *                     { +1  if  x1 > x2 
	 * </pre>
	 */
	public IntBinaryOperator<N, A> comparator() {
		return operators.intBinary(IntBinaryOperator.Id.compare);
	}
	
	/**
	 * Returns a converter that is able to convert any {@link Number} {@code x} 
	 * to an number instance of the current type {@code N}:
	 * <pre>
	 *   converter(Number x) = convert x to N
	 * </pre>
	 */
	public ConvertingUnaryOperator<Number, Number[], N, A> converter() {
		return operators.converter();
	}
	
	/**
	 * Returns a converter that is able to convert matrices, vectors and numbers 
	 * of input type {@code IN} to the current type {@code N}.
	 * <pre>
	 *   converter(IN x) = convert x to N
	 * </pre>
	 */
	public <IN extends Number, IA> Converter<IN, IA, N, A> converterFrom(final NumberArrayOperations<IN, IA> fromOps) {
		return getNumberArrayOperations().getConverterFrom(fromOps);
	}
	
	/**
	 * Returns a converter that is able to convert matrices, vectors and numbers 
	 * of the current type {@code N} to the result type {@code RN}.
	 * <pre>
	 *   converter(N x) = convert x to RN
	 * </pre>
	 */
	public <RN extends Number, RA> Converter<N, A, RN, RA> converterTo(final NumberArrayOperations<RN, RA> toOps) {
		return getNumberArrayOperations().getConverterTo(toOps);
	}
	
	// aggregating unary operators

	/**
	 * Returns an operator performing the following vector operation on vector 
	 * {@code vec}:
	 * <pre>
	 *   operator(vec) = min<sub>i</sub>( vec(i) )
	 * </pre>
	 */
	public AggregatingUnaryOperator<N, A> vectorMin() {
		return operators.aggregatingUnary(AggregatingUnaryOperator.Id.min);
	}
	/**
	 * Returns an operator performing the following vector operation on vector 
	 * {@code vec}:
	 * <pre>
	 *   operator(vec) = max<sub>i</sub>( vec(i) )
	 * </pre>
	 */
	public AggregatingUnaryOperator<N, A> vectorMax() {
		return operators.aggregatingUnary(AggregatingUnaryOperator.Id.max);
	}
	/**
	 * Returns an operator performing the following vector operation on vector 
	 * {@code vec}:
	 * <pre>
	 *   operator(vec) = &Sigma;<sub>i</sub>( vec(i) )
	 * </pre>
	 */
	public AggregatingUnaryOperator<N, A> vectorSum() {
		return operators.aggregatingUnary(AggregatingUnaryOperator.Id.sum);
	}
	/**
	 * Returns an operator performing the following vector operation on vector 
	 * {@code vec}:
	 * <pre>
	 *   operator(vec) = &Sigma;<sub>i</sub>( vec(i)<sup>2</sup> )
	 * </pre>
	 */
	public AggregatingUnaryOperator<N, A> vectorSumSquared() {
		return operators.aggregatingUnary(AggregatingUnaryOperator.Id.sumSquared);
	}
	
	//NOTE: sync doc with AggregatingUnaryOperator
	/**
	 * Returns an operator performing the norm-divisor operation on a
	 * vector {@code vec}. The norm-divisor is a value by which a vector 
     * can be divided to derive a normalized vector. It is zero for a zero
     * vector (including a vector of length zero), and positive for all 
     * other vectors, that is, dividing the vector does not change the 
     * direction of the vector.
     * <p>
     * For standard floating point numbers, the normalizer is the length of
     * the vector, that is, the square root of {@link #vectorSumSquared()}. This
     * implies that a normalized floating point vector has length one.
     * <p>
     * For big integers or fraction numbers, the normalizer is the greatest 
     * common divisor (GCD). Dividing the vector changes its length such 
     * that the element values become as small as possible (treating 
     * numerator and denominator values individually for fraction numbers, 
     * that is, {@code GCD(numerator)/GCD(denominator)}).
     * <p>
     * Returns zero if for zero vectors and for the zero length vector. If
     * no meaningful vector normalizer can be defined for a certain number 
     * type, one is returned.
	 * <p>
	 * The {@code normDivisor} operator as described above performs an operation 
	 * on a vector {@code vec} as follows:
	 * <pre>
	 *   operator(vec) = normDivisor<sub>i</sub>( vec(i) )
	 * </pre>
	 * 
	 * @see AggregatingUnaryOperator.Id#normDivisor
	 */
	//NOTE: sync doc with AggregatingUnaryOperator
	public AggregatingUnaryOperator<N, A> vectorNormDivisor() {
		return operators.aggregatingUnary(AggregatingUnaryOperator.Id.normDivisor);
	}
	
	//NOTE: sync doc with AggregatingUnaryOperator
	/**
	 * Returns a operator performing the squeeze-divisor operation on a
	 * vector {@code vec}. The squeeze-divisor is a value by which a vector can 
     * be divided to derive a squeezed vector. It is zero for a zero
     * vector (including a vector of length zero), and positive for all 
     * other vectors, that is, dividing the vector does not change the 
     * direction of the vector. It changes the vector length such that 
     * certain element values are squeezed out. 
     * <p>
     * For standard floating point numbers, the squeezer is the same as
     * <code> 1/min(abs(x)), x &ne; 0</code>, that is, the inverse of the 
     * smallest nonzero absolute value in the vector. This implies that the 
     * smallest absolute value of nonzero elements in a squeezed floating 
     * point vector is one. All values between {@code -1} and {@code 1} are
     * squeezed out. 
     * <p>
     * For big integers or fraction numbers, the squeezer is defined as the
     * quotient of greatest common divisor of numerators and least common 
     * multiple of denominators, or {@code GCD(numerator)/LCM(denominator)}
     * for short. For integer numbers, it is the GCD of all values, and 
     * squeezing is the same as normalizing (see {@link #vectorNormDivisor()}). 
     * For fraction numbers, squeezing turns all vector elements into integer 
     * values. The integer values are smallest possible, non-integer values 
     * are squeezed out.
     * <p>
     * Returns zero if for zero vectors and for the zero length vector. If
     * no meaningful vector normalizer can be defined for a certain number 
     * type, one is returned.
	 * <p>
	 * The {@code squeezeDivisor} operator as described above performs an 
	 * operation on a vector {@code vec} as follows:
	 * <pre>
	 *   operator(vec) = squeezeDivisor<sub>i</sub>( vec(i) )
	 * </pre>
	 * 
	 * @see AggregatingUnaryOperator.Id#squeezeDivisor
	 */
	//NOTE: sync doc with AggregatingUnaryOperator
	public AggregatingUnaryOperator<N, A> vectorSqueezeDivisor() {
		return operators.aggregatingUnary(AggregatingUnaryOperator.Id.squeezeDivisor);
	}

	// aggregating binary operators
	
	/**
	 * Returns the inner product operator, returning the sum of the element by 
	 * element products of the two operand vectors, that is, the following
	 * operation is performed on two operand vectors {@code vec1} and
	 * {@code vec2}:
	 * <pre>
	 *   operator(vec1,vec2) = &Sigma;<sub>i</sub>( vec1(i) * vec2(i) )
	 * </pre>
	 */
	public AggregatingBinaryOperator<N, A> vectorInnerProduct() {
		return operators.aggregatingBinary(AggregatingBinaryOperator.Id.innerProduct);
	}
	
	// unary operators applied to nullary operand

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = | op() |
	 * </pre>
	 */
	public NullaryOperator<N, A> abs(NullaryOperator<N, A> op) {
		return concat(abs(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = -op()
	 * </pre>
	 */
	public NullaryOperator<N, A> neg(NullaryOperator<N, A> op) {
		return concat(neg(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = 1/op()
	 * </pre>
	 */
	public NullaryOperator<N, A> inv(NullaryOperator<N, A> op) {
		return concat(inv(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = normalize( op() )
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public NullaryOperator<N, A> normalize(NullaryOperator<N, A> op) {
		return concat(normalize(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op()<sup>2</sup>
	 * </pre>
	 */
	public NullaryOperator<N, A> square(NullaryOperator<N, A> op) {
		return concat(square(), op);
	}

	// unary operators applied to unary operand

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = | op(x) |
	 * </pre>
	 */
	public UnaryOperator<N, A> abs(UnaryOperator<N, A> op) {
		return concat(abs(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = -op(x)
	 * </pre>
	 */
	public UnaryOperator<N, A> neg(UnaryOperator<N, A> op) {
		return concat(neg(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = 1/op(x)
	 * </pre>
	 */
	public UnaryOperator<N, A> inv(UnaryOperator<N, A> op) {
		return concat(inv(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = normalize( op(x1) )
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public UnaryOperator<N, A> normalize(UnaryOperator<N, A> op) {
		return concat(normalize(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op(x)<sup>2</sup>
	 * </pre>
	 */
	public UnaryOperator<N, A> square(UnaryOperator<N, A> op) {
		return concat(square(), op);
	}

	// unary operators applied to binary operand

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = | op(x1,x2) |
	 * </pre>
	 */
	public BinaryOperator<N, A> abs(BinaryOperator<N, A> op) {
		return concat(abs(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = -op(x1,x2) 
	 * </pre>
	 */
	public BinaryOperator<N, A> neg(BinaryOperator<N, A> op) {
		return concat(neg(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = 1/op(x1,x2) 
	 * </pre>
	 */
	public BinaryOperator<N, A> inv(BinaryOperator<N, A> op) {
		return concat(inv(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = normalize( op(x1,x2) )
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public BinaryOperator<N, A> normalize(BinaryOperator<N, A> op) {
		return concat(normalize(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op(x1,x2)<sup>2</sup> 
	 * </pre>
	 */
	public BinaryOperator<N, A> square(BinaryOperator<N, A> op) {
		return concat(square(), op);
	}

	// unary operators applied to ternary operand

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = | op(x1,x2,x3) |
	 * </pre>
	 */
	public TernaryOperator<N, A> abs(TernaryOperator<N, A> op) {
		return concat(abs(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = -op(x1,x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> neg(TernaryOperator<N, A> op) {
		return concat(neg(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = 1/op(x1,x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> inv(TernaryOperator<N, A> op) {
		return concat(inv(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = normalize( op(x1,x2,x3) )
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public TernaryOperator<N, A> normalize(TernaryOperator<N, A> op) {
		return concat(normalize(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op(x1,x2,x3)<sup>2</sup>
	 * </pre>
	 */
	public TernaryOperator<N, A> square(TernaryOperator<N, A> op) {
		return concat(square(), op);
	}

	// unary operators applied to quaternary operand

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = | op(x1,x2,x3,x4) |
	 * </pre>
	 */
	public QuaternaryOperator<N, A> abs(QuaternaryOperator<N, A> op) {
		return concat(abs(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = -op(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> neg(QuaternaryOperator<N, A> op) {
		return concat(neg(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = 1/op(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> inv(QuaternaryOperator<N, A> op) {
		return concat(inv(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = normalize( op(x1,x2,x3,x4) )
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public QuaternaryOperator<N, A> normalize(QuaternaryOperator<N, A> op) {
		return concat(normalize(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op(x1,x2,x3,x4)<sup>2</sup>
	 * </pre>
	 */
	public QuaternaryOperator<N, A> square(QuaternaryOperator<N, A> op) {
		return concat(square(), op);
	}

	// unary operators applied to n-ary operand

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*) = | op(x*) |
	 * </pre>
	 */
	public NAryOperator<N, A> abs(NAryOperator<N, A> op) {
		return concat(abs(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*) = -op(x*)
	 * </pre>
	 */
	public NAryOperator<N, A> neg(NAryOperator<N, A> op) {
		return concat(neg(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*) = 1/op(x*)
	 * </pre>
	 */
	public NAryOperator<N, A> inv(NAryOperator<N, A> op) {
		return concat(inv(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*) = normalize( op(x*) )
	 * </pre>
	 * 
	 * Normalizing means for instance reducing fraction numbers or rounding
	 * almost-zero values
	 * 
	 * @see UnaryOperator.Id#normalize
	 */
	public NAryOperator<N, A> normalize(NAryOperator<N, A> op) {
		return concat(normalize(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*) = op(x*)<sup>2</sup>
	 * </pre>
	 */
	public NAryOperator<N, A> square(NAryOperator<N, A> op) {
		return concat(square(), op);
	}

	// binary operators

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 + x2
	 * </pre>
	 */
	public BinaryOperator<N, A> add() {
		return operators.binary(BinaryOperator.Id.add);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 - x2
	 * </pre>
	 */
	public BinaryOperator<N, A> sub() {
		return operators.binary(BinaryOperator.Id.subtract);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 * x2
	 * </pre>
	 */
	public BinaryOperator<N, A> mul() {
		return operators.binary(BinaryOperator.Id.multiply);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 / x2
	 * </pre>
	 */
	public BinaryOperator<N, A> div() {
		return operators.binary(BinaryOperator.Id.divide);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = min(x1,x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> min() {
		return operators.binary(BinaryOperator.Id.min);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = max(x1,x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> max() {
		return operators.binary(BinaryOperator.Id.max);
	}

	// binary operator applied to free variable and nullary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = x + op()
	 * </pre>
	 */
	public UnaryOperator<N, A> add(NullaryOperator<N, A> op) {
		return concat(add(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = x * op()
	 * </pre>
	 */
	public UnaryOperator<N, A> mul(NullaryOperator<N, A> op) {
		return concat(mul(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = min( x , op() )
	 * </pre>
	 */
	public UnaryOperator<N, A> min(NullaryOperator<N, A> op) {
		return concat(min(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = max( x , op() )
	 * </pre>
	 */
	public UnaryOperator<N, A> max(NullaryOperator<N, A> op) {
		return concat(max(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op() - x
	 * </pre>
	 */
	public UnaryOperator<N, A> subFreeFrom(NullaryOperator<N, A> op) {
		return concat(sub(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = x - op()
	 * </pre>
	 */
	public UnaryOperator<N, A> subFromFree(NullaryOperator<N, A> op) {
		return concat(sub(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = x / op()
	 * </pre>
	 */
	public UnaryOperator<N, A> divFreeBy(NullaryOperator<N, A> op) {
		return concat(div(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op() / x
	 * </pre>
	 */
	public UnaryOperator<N, A> divByFree(NullaryOperator<N, A> op) {
		return concat(div(), op, identity());
	}

	// binary operator applied to free variable and unary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 + op(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> addToFree(UnaryOperator<N, A> op) {
		return concat(add(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op(x1) + x1
	 * </pre>
	 */
	public BinaryOperator<N, A> addFreeTo(UnaryOperator<N, A> op) {
		return concat(add(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 * op(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> mulFreeBy(UnaryOperator<N, A> op) {
		return concat(mul(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op(x1) * x2
	 * </pre>
	 */
	public BinaryOperator<N, A> mulByFree(UnaryOperator<N, A> op) {
		return concat(mul(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = min( x1 , op(x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> minFreeWith(UnaryOperator<N, A> op) {
		return concat(min(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = min( op(x1) , x2 )
	 * </pre>
	 */
	public BinaryOperator<N, A> minWithFree(UnaryOperator<N, A> op) {
		return concat(min(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = max( x1 , op(x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> maxFreeWith(UnaryOperator<N, A> op) {
		return concat(max(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = max( op(x1) , x2 )
	 * </pre>
	 */
	public BinaryOperator<N, A> maxWithFree(UnaryOperator<N, A> op) {
		return concat(max(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op(x1) - x2
	 * </pre>
	 */
	public BinaryOperator<N, A> subFreeFrom(UnaryOperator<N, A> op) {
		return concat(sub(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 - op(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> subFromFree(UnaryOperator<N, A> op) {
		return concat(sub(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = x1 / op(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> divFreeBy(UnaryOperator<N, A> op) {
		return concat(div(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op(x1) / x2
	 * </pre>
	 */
	public BinaryOperator<N, A> divByFree(UnaryOperator<N, A> op) {
		return concat(div(), op, identity());
	}

	// binary operator applied to free variable and binary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = x1 + op(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> addToFree(BinaryOperator<N, A> op) {
		return concat(add(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op(x1,x2) + x3
	 * </pre>
	 */
	public TernaryOperator<N, A> addFreeTo(BinaryOperator<N, A> op) {
		return concat(add(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = x1 * op(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> mulFreeBy(BinaryOperator<N, A> op) {
		return concat(mul(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op(x1,x2) * x3
	 * </pre>
	 */
	public TernaryOperator<N, A> mulByFree(BinaryOperator<N, A> op) {
		return concat(mul(), op, identity());
	}	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = min( x1 , op(x2, x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> minFreeWith(BinaryOperator<N, A> op) {
		return concat(min(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = min( op(x1, x2) , x3 )
	 * </pre>
	 */
	public TernaryOperator<N, A> minWithFree(BinaryOperator<N, A> op) {
		return concat(min(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = max( x1 , op(x2, x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> maxFreeWith(BinaryOperator<N, A> op) {
		return concat(max(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = max( op(x1, x2) , x3 )
	 * </pre>
	 */
	public TernaryOperator<N, A> maxWithFree(BinaryOperator<N, A> op) {
		return concat(max(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op(x1,x2) - x3
	 * </pre>
	 */
	public TernaryOperator<N, A> subFreeFrom(BinaryOperator<N, A> op) {
		return concat(sub(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = x1 - op(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> subFromFree(BinaryOperator<N, A> op) {
		return concat(sub(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = x1 / op(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> divFreeBy(BinaryOperator<N, A> op) {
		return concat(div(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op(x1,x2) / x3
	 * </pre>
	 */
	public TernaryOperator<N, A> divByFree(BinaryOperator<N, A> op) {
		return concat(div(), op, identity());
	}

	// binary operator applied to free variable and ternary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = x1 + op(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> addToFree(TernaryOperator<N, A> op) {
		return concat(add(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op(x1,x2,x3) + x4
	 * </pre>
	 */
	public QuaternaryOperator<N, A> addFreeTo(TernaryOperator<N, A> op) {
		return concat(add(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = x1 * op(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mulFreeBy(TernaryOperator<N, A> op) {
		return concat(mul(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op(x1,x2,x3) * x4
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mulByFree(TernaryOperator<N, A> op) {
		return concat(mul(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( x1 , op(x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> minFreeWith(TernaryOperator<N, A> op) {
		return concat(min(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( op(x1,x2,x2) , x4 )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> minWithFree(TernaryOperator<N, A> op) {
		return concat(min(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( x1 , op(x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> maxFreeWith(TernaryOperator<N, A> op) {
		return concat(max(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( op(x1,x2,x2) , x4 )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> maxWithFree(TernaryOperator<N, A> op) {
		return concat(max(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op(x1,x2,x3) - x4
	 * </pre>
	 */
	public QuaternaryOperator<N, A> subFreeFrom(TernaryOperator<N, A> op) {
		return concat(sub(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = x1 - op(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> subFromFree(TernaryOperator<N, A> op) {
		return concat(sub(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = x1 / op(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> divFreeBy(TernaryOperator<N, A> op) {
		return concat(div(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op(x1,x2,x3) / x4
	 * </pre>
	 */
	public QuaternaryOperator<N, A> divByFree(TernaryOperator<N, A> op) {
		return concat(div(), op, identity());
	}

	// binary operator applied to free variable and quaternary or n-ary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x,y1,...,yn) = x + op(y1,...,yn)
	 * </pre>
	 */
	public NAryOperator<N, A> addToFree(NAryOperator<N, A> op) {
		return concat(add(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,...,xn,y) = op(x1,...,xn) + y
	 * </pre>
	 */
	public NAryOperator<N, A> addFreeTo(NAryOperator<N, A> op) {
		return concat(add(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x,y1,...,yn) = x * op(y1,...,yn)
	 * </pre>
	 */
	public NAryOperator<N, A> mullFreeBy(NAryOperator<N, A> op) {
		return concat(mul(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,...,xn,y) = op(x1,...,xn) * y
	 * </pre>
	 */
	public NAryOperator<N, A> mulByFree(NAryOperator<N, A> op) {
		return concat(mul(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x,y1,...,yn) = min( x, op(y1,...,yn) )
	 * </pre>
	 */
	public NAryOperator<N, A> minFreeWith(NAryOperator<N, A> op) {
		return concat(min(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,...,xn,y) = min( op(x1,...,xn) , y )
	 * </pre>
	 */
	public NAryOperator<N, A> minWithFree(NAryOperator<N, A> op) {
		return concat(min(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x,y1,...,yn) = max( x, op(y1,...,yn) )
	 * </pre>
	 */
	public NAryOperator<N, A> maxFreeWith(NAryOperator<N, A> op) {
		return concat(max(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,...,xn,y) = max( op(x1,...,xn) , y )
	 * </pre>
	 */
	public NAryOperator<N, A> maxWithFree(NAryOperator<N, A> op) {
		return concat(max(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y) = op(x*) - y
	 * </pre>
	 */
	public NAryOperator<N, A> subFreeFrom(NAryOperator<N, A> op) {
		return concat(sub(), op, identity());
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x,y*) = x - op(y*)
	 * </pre>
	 */
	public NAryOperator<N, A> subFromFree(NAryOperator<N, A> op) {
		return concat(sub(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x,y*) = x / op(y*)
	 * </pre>
	 */
	public NAryOperator<N, A> divFreeBy(NAryOperator<N, A> op) {
		return concat(div(), identity(), op);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y) = op(x*) / y
	 * </pre>
	 */
	public NAryOperator<N, A> divByFree(NAryOperator<N, A> op) {
		return concat(div(), op, identity());
	}

	// binary operator applied to two nullary operators
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op1() + op2()
	 * </pre>
	 */
	public NullaryOperator<N, A> add(NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op1() - op2()
	 * </pre>
	 */
	public NullaryOperator<N, A> sub(NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op1() * op2()
	 * </pre>
	 */
	public NullaryOperator<N, A> mul(NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op1() / op2()
	 * </pre>
	 */
	public NullaryOperator<N, A> div(NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = min( op1() , op2() )
	 * </pre>
	 */
	public NullaryOperator<N, A> min(NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = max( op1() , op2() )
	 * </pre>
	 */
	public NullaryOperator<N, A> max(NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to a nullary and a unary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1() + op2(x)
	 * </pre>
	 */
	public UnaryOperator<N, A> add(NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1() - op2(x)
	 * </pre>
	 */
	public UnaryOperator<N, A> sub(NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1() * op2(x)
	 * </pre>
	 */
	public UnaryOperator<N, A> mul(NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1() / op2(x)
	 * </pre>
	 */
	public UnaryOperator<N, A> div(NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = min( op1() , op2(x) )
	 * </pre>
	 */
	public UnaryOperator<N, A> min(NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = max( op1() , op2(x) )
	 * </pre>
	 */
	public UnaryOperator<N, A> max(NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to a unary and a nullary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1(x) + op2()
	 * </pre>
	 */
	public UnaryOperator<N, A> add(UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1(x) - op2()
	 * </pre>
	 */
	public UnaryOperator<N, A> sub(UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1(x) * op2()
	 * </pre>
	 */
	public UnaryOperator<N, A> mul(UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op1(x) / op2()
	 * </pre>
	 */
	public UnaryOperator<N, A> div(UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = min( op1(x) , op2() )
	 * </pre>
	 */
	public UnaryOperator<N, A> min(UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = max( op1(x) , op2() )
	 * </pre>
	 */
	public UnaryOperator<N, A> max(UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to two unary operators
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1) + op2(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> add(UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1) - op2(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> sub(UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1) * op2(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> mul(UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1) / op2(x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> div(UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = min( op1(x1) , op2(x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> min(UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = max( op1(x1) , op2(x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> max(UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to a nullary and a binary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1() + op2(x1,x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> add(NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1() - op2(x1,x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> sub(NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1() * op2(x1,x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> mul(NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1() / op2(x1,x2)
	 * </pre>
	 */
	public BinaryOperator<N, A> div(NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = min( op1() , op2(x1,x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> min(NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = max( op1() , op2(x1,x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> max(NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to a binary and a nullary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1,x2) + op2()
	 * </pre>
	 */
	public BinaryOperator<N, A> add(BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1,x2) - op2()
	 * </pre>
	 */
	public BinaryOperator<N, A> sub(BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1,x2) * op2()
	 * </pre>
	 */
	public BinaryOperator<N, A> mul(BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op1(x1,x2) / op2()
	 * </pre>
	 */
	public BinaryOperator<N, A> div(BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = min( op1(x1,x2) , op2() )
	 * </pre>
	 */
	public BinaryOperator<N, A> min(BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = max( op1(x1,x2) , op2() )
	 * </pre>
	 */
	public BinaryOperator<N, A> max(BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to a unary and a binary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1) + op2(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> add(UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1) - op2(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> sub(UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1) * op2(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> mul(UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1) / op2(x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> div(UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = min( op1(x1) , op2(x2,x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> min(UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = max( op1(x1) , op2(x2,x3) )
	 * </pre>
	 */
	public NAryOperator<N, A> max(UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to a binary and a unary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2) + op2(x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> add(BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2) - op2(x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> sub(BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2) * op2(x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> mul(BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2) / op2(x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> div(BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = min( op1(x1,x2) , op2(x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> min(BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = max( op1(x1,x2) , op2(x3) )
	 * </pre>
	 */
	public NAryOperator<N, A> max(BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to a nullary and a ternary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1() + op2(x1,x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> add(NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1() - op2(x1,x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> sub(NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1() * op2(x1,x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> mul(NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1() / op2(x1,x2,x3)
	 * </pre>
	 */
	public TernaryOperator<N, A> div(NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = min( op1() , op2(x1,x2,x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> min(NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = max( op1() , op2(x1,x2,x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> max(NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to a ternary and a nullary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2,x3) + op2()
	 * </pre>
	 */
	public TernaryOperator<N, A> add(TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2,x3) - op2()
	 * </pre>
	 */
	public TernaryOperator<N, A> sub(TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2,x3) * op2()
	 * </pre>
	 */
	public TernaryOperator<N, A> mul(TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op1(x1,x2,x3) / op2()
	 * </pre>
	 */
	public TernaryOperator<N, A> div(TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = min( op1(x1,x2,x3) , op2() )
	 * </pre>
	 */
	public TernaryOperator<N, A> min(TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = max( op1(x1,x2,x3) , op2() )
	 * </pre>
	 */
	public TernaryOperator<N, A> max(TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}
	
	// binary operator applied to two binary operators
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2) + op2(x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> add(BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2) - op2(x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> sub(BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2) * op2(x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mul(BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2) / op2(x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> div(BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( op1(x1,x2) , op2(x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> min(BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( op1(x1,x2) , op2(x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> max(BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to a unary and a ternary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1) + op2(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> add(UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1) - op2(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> sub(UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1) * op2(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mul(UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1) / op2(x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> div(UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( op1(x1) , op2(x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> min(UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( op1(x1) , op2(x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> max(UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to a ternary and a unary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3) + op2(x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> add(TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3) - op2(x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> sub(TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3) * op2(x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mul(TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3) / op2(x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> div(TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( op1(x1,x2,x3) , op2(x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> min(TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( op1(x1,x2,x3) , op2(x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> max(TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to a nullary and a quaternary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1() + op2(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> add(NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1() - op2(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> sub(NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1() * op2(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mul(NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1() / op2(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> div(NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( op1() , op2(x1,x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> min(NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( op1() , op2(x1,x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> max(NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to a quaternary and a nullary operator
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3,x4) + op2(x1,x2,x3,x4)
	 * </pre>
	 */
	public QuaternaryOperator<N, A> add(QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3,x4) - op2()
	 * </pre>
	 */
	public QuaternaryOperator<N, A> sub(QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3,x4) * op2()
	 * </pre>
	 */
	public QuaternaryOperator<N, A> mul(QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op1(x1,x2,x3,x4) / op2()
	 * </pre>
	 */
	public QuaternaryOperator<N, A> div(QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = min( op1(x1,x2,x3,x4) , op2() )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> min(QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = max( op1(x1,x2,x3,x4) , op2() )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> max(QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// binary operator applied to two n-ary operators
	
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = op1(x*) + op2(y*)
	 * </pre>
	 */
	public NAryOperator<N, A> add(NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return concat(add(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = op1(x*) - op2(y*)
	 * </pre>
	 */
	public NAryOperator<N, A> sub(NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return concat(sub(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = op1(x*) * op2(y*)
	 * </pre>
	 */
	public NAryOperator<N, A> mul(NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return concat(mul(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = op1(x*) / op2(y*)
	 * </pre>
	 */
	public NAryOperator<N, A> div(NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return concat(div(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = min( op1(x*) , op2(y*) )
	 * </pre>
	 */
	public NAryOperator<N, A> min(NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return concat(min(), op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = max( op1(x*) , op2(y*) )
	 * </pre>
	 */
	public NAryOperator<N, A> max(NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return concat(max(), op1, op2);
	}

	// concatenation unary operators

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op( operand() )
	 * </pre>
	 */
	public NullaryOperator<N, A> concat(UnaryOperator<N, A> op, NullaryOperator<N, A> operand) {
		return new ConcatNullaryOperator<N, A>(op, operand);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op( operand(x) )
	 * </pre>
	 */
	public UnaryOperator<N, A> concat(UnaryOperator<N, A> op, UnaryOperator<N, A> operand) {
		return new ConcatUnaryOperator<N, A>(op, operand);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op( operand(x1,x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> concat(UnaryOperator<N, A> op, BinaryOperator<N, A> operand) {
		return new ConcatBinaryOperator<N, A>(op, operand);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op( operand(x1,x2,x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> concat(UnaryOperator<N, A> op, TernaryOperator<N, A> operand) {
		return new ConcatTernaryOperator<N, A>(op, operand);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op( operand(x1,x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> concat(UnaryOperator<N, A> op, QuaternaryOperator<N, A> operand) {
		return new ConcatQuaternaryOperator<N, A>(op, operand);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*) = op( operand(x*) )
	 * </pre>
	 */
	public NAryOperator<N, A> concat(UnaryOperator<N, A> op, NAryOperator<N, A> operand) {
		return new ConcatNAryOperator<N, A>(op, operand);
	}

	// concatenation with binary operator

	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator() = op( op1() , op2() )
	 * </pre>
	 */
	public NullaryOperator<N, A> concat(BinaryOperator<N, A> op, NullaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return new NullaryNullaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op( op1() , op2(x) )
	 * </pre>
	 */
	public UnaryOperator<N, A> concat(BinaryOperator<N, A> op, NullaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return new NullaryUnaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x) = op( op1(x) , op2() )
	 * </pre>
	 */
	public UnaryOperator<N, A> concat(BinaryOperator<N, A> op, UnaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return new UnaryNullaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op( op1(x1) , op2(x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> concat(BinaryOperator<N, A> op, UnaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return new UnaryUnaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op( op1() , op2(x1,x2) )
	 * </pre>
	 */
	public BinaryOperator<N, A> concat(BinaryOperator<N, A> op, NullaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return new NullaryBinaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2) = op( op1(x1,x2) , op2() )
	 * </pre>
	 */
	public BinaryOperator<N, A> concat(BinaryOperator<N, A> op, BinaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return new BinaryNullaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op( op1(x1) , op2(x2,x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> concat(BinaryOperator<N, A> op, UnaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return new UnaryBinaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op( op1(x1,x2) , op2(x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> concat(BinaryOperator<N, A> op, BinaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return new BinaryUnaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op( op1() , op2(x1,x2,x3) )
	 * </pre>
	 */
	public TernaryOperator<N, A> concat(BinaryOperator<N, A> op, NullaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return new NullaryTernaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3) = op( op1(x1,x2,x3) , op2() )
	 * </pre>
	 */
	public TernaryOperator<N, A> concat(BinaryOperator<N, A> op, TernaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return new TernaryNullaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op( op1(x1,x2) , op2(x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> concat(BinaryOperator<N, A> op, BinaryOperator<N, A> op1, BinaryOperator<N, A> op2) {
		return new BinaryBinaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op( op1(x1) , op2(x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> concat(BinaryOperator<N, A> op, UnaryOperator<N, A> op1, TernaryOperator<N, A> op2) {
		return new UnaryTernaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op( op1(x1,x2,x3) , op2(x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> concat(BinaryOperator<N, A> op, TernaryOperator<N, A> op1, UnaryOperator<N, A> op2) {
		return new TernaryUnaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op( op1() , op2(x1,x2,x3,x4) )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> concat(BinaryOperator<N, A> op, NullaryOperator<N, A> op1, QuaternaryOperator<N, A> op2) {
		return new NullaryQuaternaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x1,x2,x3,x4) = op( op1(x1,x2,x3,x4) , op2() )
	 * </pre>
	 */
	public QuaternaryOperator<N, A> concat(BinaryOperator<N, A> op, QuaternaryOperator<N, A> op1, NullaryOperator<N, A> op2) {
		return new QuaternaryNullaryOperator<N, A>(arrayOps, op, op1, op2);
	}
	/**
	 * Returns an operator performing
	 * <pre>
	 *   operator(x*,y*) = op( op1(x*) , op2(y*) )
	 * </pre>
	 */
	public NAryOperator<N, A> concat(BinaryOperator<N, A> op, NAryOperator<N, A> op1, NAryOperator<N, A> op2) {
		return new NAryNAryOperator<N, A>(arrayOps, op, op1, op2);
	}
	
}
