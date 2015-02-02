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

import ch.javasoft.math.linalg.GaussPivotingFactory;
import ch.javasoft.math.linalg.LinAlgOperations;
import ch.javasoft.math.operator.AggregatingBinaryOperator;
import ch.javasoft.math.operator.AggregatingUnaryOperator;
import ch.javasoft.math.operator.BinaryOperator;
import ch.javasoft.math.operator.BooleanBinaryOperator;
import ch.javasoft.math.operator.BooleanUnaryOperator;
import ch.javasoft.math.operator.ConvertingUnaryOperator;
import ch.javasoft.math.operator.DivisionSupport;
import ch.javasoft.math.operator.IntBinaryOperator;
import ch.javasoft.math.operator.IntUnaryOperator;
import ch.javasoft.math.operator.NullaryOperator;
import ch.javasoft.math.operator.UnaryOperator;


/**
 * The <code>NumberOperators</code> defines standard operators for different
 * number types. An instance of this class defines the (boxed) number type and 
 * the array type, such as {@link Double} and {@code double[]}. 
 * 
 * @type T	number type
 * @type A	array type of number
 */
public interface NumberOperators<N extends Number, A> {
	/**
	 * Returns the number type class for a single value
	 */
	Class<N> numberClass();

	/**
	 * Returns the number type class of an array of values
	 */
	Class<A> arrayClass();
	
	/**
	 * Returns the kind of {@link DivisionSupport} for this operators instance
	 */
	DivisionSupport getDivisionSupport();
	
	/**
	 * Returns the array operations instance based on {@code this} operators 
	 * instance
	 */
	ArrayOperations<A> getArrayOperations();
	/**
	 * Returns the number array operations instance based on {@code this} 
	 * operators instance
	 */
	NumberArrayOperations<N, A> getNumberArrayOperations();
	/**
	 * Returns the linear algebra operations instance based on {@code this}
	 * operators instance
	 */
	LinAlgOperations<N, A> getLinAlgOperations();
	/**
	 * Returns the linear algebra operations instance based on {@code this}
	 * operators instance with Gauss pivoting created the given factory  
	 */
	LinAlgOperations<N, A> getLinAlgOperations(GaussPivotingFactory<N, A> gaussPivotingFactory);
	

	/**
	 * Returns the number value representing zero
	 */
	N zero();

	/**
	 * Returns the number value representing one
	 */
	N one();
	
	/**
	 * Returns a const operator for the specified constant {@code value}, 
	 * matching number and array type defined by this {@code NumberOperators} 
	 * instance.
	 * 
	 * @param value	the constant value returned by the constant operator
	 * @return	the operator implementation for the current number/array type
	 */
	NullaryOperator<N, A> constant(N value);

	/**
	 * Returns a converting unary operator which is able to convert any
	 * {@link Number} instance to a specific number instance.
	 * 
	 * @return	the unary converter
	 */
	ConvertingUnaryOperator<Number, Number[], N, A> converter();
	
	/**
	 * Returns the nullary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	NullaryOperator<N, A> nullary(NullaryOperator.Id id);

	/**
	 * Returns the unary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	UnaryOperator<N, A> unary(UnaryOperator.Id id);
	/**
	 * Returns the int-unary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 *  
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	IntUnaryOperator<N, A> intUnary(IntUnaryOperator.Id id);
	/**
	 * Returns the boolean-unary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	BooleanUnaryOperator<N, A> booleanUnary(BooleanUnaryOperator.Id id);
	
	/**
	 * Returns the binary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	BinaryOperator<N, A> binary(BinaryOperator.Id id);
	/**
	 * Returns the int-binary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	IntBinaryOperator<N, A> intBinary(IntBinaryOperator.Id id);
	/**
	 * Returns the boolean-binary operator identified by operator id, matching
	 * number and array type defined by this {@code NumberOperators} instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	BooleanBinaryOperator<N, A> booleanBinary(BooleanBinaryOperator.Id id);
	/**
	 * Returns the aggregating unary operator identified by operator id, 
	 * matching number and array type defined by this {@code NumberOperators} 
	 * instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	AggregatingUnaryOperator<N, A> aggregatingUnary(AggregatingUnaryOperator.Id id);
	/**
	 * Returns the aggregating binary operator identified by operator id, 
	 * matching number and array type defined by this {@code NumberOperators} 
	 * instance.
	 * 
	 * @param id	the identifier for the desired operator
	 * @return	the operator implementation for the current number/array type
	 */
	AggregatingBinaryOperator<N, A> aggregatingBinary(AggregatingBinaryOperator.Id id);

}
