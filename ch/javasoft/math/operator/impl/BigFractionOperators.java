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
package ch.javasoft.math.operator.impl;

import java.math.BigInteger;
import java.util.Random;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.array.impl.DefaultArrayOperations;
import ch.javasoft.math.linalg.DefaultLinAlgOperations;
import ch.javasoft.math.linalg.GaussPivotingFactory;
import ch.javasoft.math.linalg.LinAlgOperations;
import ch.javasoft.math.linalg.impl.BigFractionGaussPivoting;
import ch.javasoft.math.operator.AbstractBinaryOperator;
import ch.javasoft.math.operator.AbstractBooleanBinaryOperator;
import ch.javasoft.math.operator.AbstractBooleanUnaryOperator;
import ch.javasoft.math.operator.AbstractIntBinaryOperator;
import ch.javasoft.math.operator.AbstractIntUnaryOperator;
import ch.javasoft.math.operator.AbstractNullaryOperator;
import ch.javasoft.math.operator.AbstractUnaryOperator;
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
import ch.javasoft.math.operator.AggregatingBinaryOperator.Id;

/**
 * Number operators for {@link BigFraction} numbers. The single 
 * {@link #INSTANCE instance} is available as constant. 
 */
public class BigFractionOperators implements NumberOperators<BigFraction, BigFraction[]> {
	
	/**
	 * The default instance, all operators perform the plain operations without
	 * rounding
	 */
	public static final BigFractionOperators INSTANCE = new BigFractionOperators();
	
	private final LinAlgOperations<BigFraction, BigFraction[]> 				linAlgOps;
	
	private final NullaryOperator<BigFraction, BigFraction[]>[] 			nullary;
	private final UnaryOperator<BigFraction, BigFraction[]>[] 				unary;
	private final BooleanUnaryOperator<BigFraction, BigFraction[]>[] 		boolUnary;
	private final IntUnaryOperator<BigFraction, BigFraction[]>[] 			intUnary;
	private final ConvertingUnaryOperator<Number, Number[], BigFraction, BigFraction[]> converter;
	private final BinaryOperator<BigFraction, BigFraction[]>[] 				binary;
	private final BooleanBinaryOperator<BigFraction, BigFraction[]>[] 		boolBinary;
	private final IntBinaryOperator<BigFraction, BigFraction[]>[] 			intBinary;
	private final AggregatingUnaryOperator<BigFraction, BigFraction[]>[] 	aggUnary;
	private final AggregatingBinaryOperator<BigFraction, BigFraction[]>[]	aggBinary;


	// see DEFAULT constant
	protected BigFractionOperators() {
		nullary		= initNullary();
		unary 		= initUnary();
		boolUnary 	= initBoolUnary();
		intUnary 	= initIntUnary();
		converter	= initConverter();
		binary 		= initBinary();
		boolBinary 	= initBoolBinary();
		intBinary 	= initIntBinary();
		aggUnary	= initAggUnary();
		aggBinary 	= initAggBinary();
		linAlgOps	= new DefaultLinAlgOperations<BigFraction, BigFraction[]>(
			this, new DefaultArrayOperations<BigFraction>(BigFraction[].class), BigFractionGaussPivoting.LEN_PRODUCT_L
		);
	}
	
	public Class<BigFraction> numberClass() {
		return BigFraction.class;
	}
	public Class<BigFraction[]> arrayClass() {
		return BigFraction[].class;
	}
	public DivisionSupport getDivisionSupport() {
		return DivisionSupport.EXACT;
	}
	public LinAlgOperations<BigFraction, BigFraction[]> getLinAlgOperations() {
		return linAlgOps;
	}
	public LinAlgOperations<BigFraction, BigFraction[]> getLinAlgOperations(GaussPivotingFactory<BigFraction, BigFraction[]> gaussPivotingFactory) {
		return new DefaultLinAlgOperations<BigFraction, BigFraction[]>(getNumberArrayOperations(), gaussPivotingFactory);
	}
	public ArrayOperations<BigFraction[]> getArrayOperations() {
		return linAlgOps.getArrayOperations();
	}
	public NumberArrayOperations<BigFraction, BigFraction[]> getNumberArrayOperations() {
		return linAlgOps.getNumberArrayOperations();
	}
	
	public BigFraction zero() {
		return BigFraction.ZERO;
	}
	public BigFraction one() {
		return BigFraction.ONE;
	}
	
	public NullaryOperator<BigFraction, BigFraction[]> constant(BigFraction value) {
		if (value.isZero()) {
			return nullary(NullaryOperator.Id.zero);
		}
		else if (value.isOne()) {
			return nullary(NullaryOperator.Id.one);
		}
		return new BigFractionConstOperator(value);
	}
	public ConvertingUnaryOperator<Number, Number[], BigFraction, BigFraction[]> converter() {
		return converter;
	}
	public NullaryOperator<BigFraction, BigFraction[]> nullary(NullaryOperator.Id id) {
		return nullary[id.ordinal()];
	}
	public UnaryOperator<BigFraction, BigFraction[]> unary(UnaryOperator.Id id) {
		return unary[id.ordinal()];
	}

	public BooleanUnaryOperator<BigFraction, BigFraction[]> booleanUnary(BooleanUnaryOperator.Id id) {
		return boolUnary[id.ordinal()];
	}

	public IntUnaryOperator<BigFraction, BigFraction[]> intUnary(IntUnaryOperator.Id id) {
		return intUnary[id.ordinal()];
	}

	public BinaryOperator<BigFraction, BigFraction[]> binary(BinaryOperator.Id id) {
		return binary[id.ordinal()];
	}

	public BooleanBinaryOperator<BigFraction, BigFraction[]> booleanBinary(BooleanBinaryOperator.Id id) {
		return boolBinary[id.ordinal()];
	}

	public IntBinaryOperator<BigFraction, BigFraction[]> intBinary(IntBinaryOperator.Id id) {
		return intBinary[id.ordinal()];
	}
	
	public AggregatingUnaryOperator<BigFraction, BigFraction[]> aggregatingUnary(AggregatingUnaryOperator.Id id) {
		return aggUnary[id.ordinal()];
	}
	
	public AggregatingBinaryOperator<BigFraction, BigFraction[]> aggregatingBinary(Id id) {
		return aggBinary[id.ordinal()];
	}
	
	@SuppressWarnings("unchecked")
	private static NullaryOperator<BigFraction, BigFraction[]>[] initNullary() {
		final NullaryOperator<BigFraction, BigFraction[]>[] ops = new NullaryOperator[NullaryOperator.Id.values().length];
		ops[NullaryOperator.Id.zero.ordinal()] = new BigFractionConstOperator(BigFraction.ZERO);
		ops[NullaryOperator.Id.one.ordinal()] = new BigFractionConstOperator(BigFraction.ONE);
		ops[NullaryOperator.Id.random.ordinal()] = new AbstractNullaryOperator<BigFraction, BigFraction[]>() {
			private final Random rnd = new Random();
			public BigFraction operate() {
				return BigFraction.valueOf(rnd.nextLong(), rnd.nextLong());
			}
			public void operate(BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = BigFraction.valueOf(rnd.nextLong(), rnd.nextLong());
			}
		};
		return checkComplete(ops, NullaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static UnaryOperator<BigFraction, BigFraction[]>[] initUnary() {
		final UnaryOperator<BigFraction, BigFraction[]>[] ops = new UnaryOperator[UnaryOperator.Id.values().length];
		ops[UnaryOperator.Id.identity.ordinal()] = new AbstractUnaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand) {
				return operand;
			}
			public BigFraction operate(BigFraction[] src, int srcIndex) {
				return src[srcIndex];
			}
			public void operate(BigFraction[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex];
			}
		};
		ops[UnaryOperator.Id.normalize.ordinal()] = new AbstractUnaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand) {
				return operand.reduce();
			}
			public BigFraction operate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].reduce();
			}
			public void operate(BigFraction[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].reduce();
			}
		};
		ops[UnaryOperator.Id.abs.ordinal()] = new AbstractUnaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand) {
				return operand.abs();
			}
			public BigFraction operate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].abs();
			}
			public void operate(BigFraction[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].abs();
			}
		};
		ops[UnaryOperator.Id.negate.ordinal()] = new AbstractUnaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand) {
				return operand.negate();
			}
			public BigFraction operate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].negate();
			}
			public void operate(BigFraction[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].negate();
			}
		};
		ops[UnaryOperator.Id.invert.ordinal()] = new AbstractUnaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand) {
				return operand.invert();
			}
			public BigFraction operate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].invert();
			}
			public void operate(BigFraction[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].invert();
			}
		};
		ops[UnaryOperator.Id.square.ordinal()] = new AbstractUnaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand) {
				return operand.pow(2);
			}
			public BigFraction operate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].pow(2);
			}
			public void operate(BigFraction[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].pow(2);
			}
		};
		return checkComplete(ops, UnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntUnaryOperator<BigFraction, BigFraction[]>[] initIntUnary() {
		final IntUnaryOperator<BigFraction, BigFraction[]>[] ops = new IntUnaryOperator[IntUnaryOperator.Id.values().length];
		ops[IntUnaryOperator.Id.signum.ordinal()] = new AbstractIntUnaryOperator<BigFraction, BigFraction[]>() {
			public int intOperate(BigFraction operand) {
				return operand.signum();
			}
			public int intOperate(BigFraction[] operand, int index) {
				return operand[index].signum();
			}
			public void operate(BigFraction[] src, int srcIndex, int[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum();
			}
		};
		return checkComplete(ops, IntUnaryOperator.Id.values());
	}
	private ConvertingUnaryOperator<Number, Number[], BigFraction, BigFraction[]> initConverter() {
		return new ConvertingUnaryOperator<Number, Number[], BigFraction, BigFraction[]>() {
			public BigFraction operate(Number operand) {
				return BigFraction.valueOf(operand);
			}
			public void operate(Number[] src, int srcIndex, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = BigFraction.valueOf(src[srcIndex]);
			}
		};
	}
	@SuppressWarnings("unchecked")
	private static BooleanUnaryOperator<BigFraction, BigFraction[]>[] initBoolUnary() {
		final BooleanUnaryOperator<BigFraction, BigFraction[]>[] ops = new BooleanUnaryOperator[BooleanUnaryOperator.Id.values().length];
		ops[BooleanUnaryOperator.Id.isZero.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isZero();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isZero();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isZero();
			}
		};
		ops[BooleanUnaryOperator.Id.isNonZero.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isNonZero();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isNonZero();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isNonZero();
			}
		};
		ops[BooleanUnaryOperator.Id.isOne.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isOne();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isOne();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isOne();
			}
		};
		ops[BooleanUnaryOperator.Id.isPositive.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isPositive();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isPositive();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isPositive();
			}
		};
		ops[BooleanUnaryOperator.Id.isNegative.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isNegative();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isNegative();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isNegative();
			}
		};
		ops[BooleanUnaryOperator.Id.isNonPositive.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isNonPositive();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isNonPositive();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isNonPositive();
			}
		};
		ops[BooleanUnaryOperator.Id.isNonNegative.ordinal()] = new AbstractBooleanUnaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand) {
				return operand.isNonNegative();
			}
			public boolean booleanOperate(BigFraction[] src, int srcIndex) {
				return src[srcIndex].isNonNegative();
			}
			public void operate(BigFraction[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].isNonNegative();
			}
		};
		return checkComplete(ops, BooleanUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BinaryOperator<BigFraction, BigFraction[]>[] initBinary() {
		final BinaryOperator<BigFraction, BigFraction[]>[] ops = new BinaryOperator[BinaryOperator.Id.values().length];
		ops[BinaryOperator.Id.add.ordinal()] = new AbstractBinaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand1, BigFraction operand2) {
				return operand1.add(operand2);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].add(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.subtract.ordinal()] = new AbstractBinaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand1, BigFraction operand2) {
				return operand1.subtract(operand2);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].subtract(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.multiply.ordinal()] = new AbstractBinaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand1, BigFraction operand2) {
				return operand1.multiply(operand2);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].multiply(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.divide.ordinal()] = new AbstractBinaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand1, BigFraction operand2) {
				return operand1.divide(operand2);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].divide(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.min.ordinal()] = new AbstractBinaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand1, BigFraction operand2) {
				return operand1.min(operand2);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].min(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.max.ordinal()] = new AbstractBinaryOperator<BigFraction, BigFraction[]>() {
			public BigFraction operate(BigFraction operand1, BigFraction operand2) {
				return operand1.max(operand2);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, BigFraction[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].max(operand2[index2]);
			}
		};
		return checkComplete(ops, BinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanBinaryOperator<BigFraction, BigFraction[]>[] initBoolBinary() {
		final BooleanBinaryOperator<BigFraction, BigFraction[]>[] ops = new BooleanBinaryOperator[BooleanBinaryOperator.Id.values().length];
		ops[BooleanBinaryOperator.Id.less.ordinal()] = new AbstractBooleanBinaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand1, BigFraction operand2) {
				return operand1.compareTo(operand2) < 0;
			}
			public boolean booleanOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) < 0;
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) < 0;
			}
		};
		ops[BooleanBinaryOperator.Id.lessOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand1, BigFraction operand2) {
				return operand1.compareTo(operand2) <= 0;
			}
			public boolean booleanOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) <= 0;
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) <= 0;
			}
		};
		ops[BooleanBinaryOperator.Id.equal.ordinal()] = new AbstractBooleanBinaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand1, BigFraction operand2) {
				return operand1.equalsNumerically(operand2);
			}
			public boolean booleanOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return operand1[index1].equalsNumerically(operand2[index2]);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].equalsNumerically(operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.unequal.ordinal()] = new AbstractBooleanBinaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand1, BigFraction operand2) {
				return !operand1.equalsNumerically(operand2);
			}
			public boolean booleanOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return !operand1[index1].equalsNumerically(operand2[index2]);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !operand1[index1].equalsNumerically(operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.greaterOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand1, BigFraction operand2) {
				return operand1.compareTo(operand2) >= 0;
			}
			public boolean booleanOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) >= 0;
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) >= 0;
			}
		};
		ops[BooleanBinaryOperator.Id.greater.ordinal()] = new AbstractBooleanBinaryOperator<BigFraction, BigFraction[]>() {
			public boolean booleanOperate(BigFraction operand1, BigFraction operand2) {
				return operand1.compareTo(operand2) > 0;
			}
			public boolean booleanOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) > 0;
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) > 0;
			}
		};
		return checkComplete(ops, BooleanBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntBinaryOperator<BigFraction, BigFraction[]>[] initIntBinary() {
		final IntBinaryOperator<BigFraction, BigFraction[]>[] ops = new IntBinaryOperator[IntBinaryOperator.Id.values().length];
		ops[IntBinaryOperator.Id.compare.ordinal()] = new AbstractIntBinaryOperator<BigFraction, BigFraction[]>() {
			public int intOperate(BigFraction operand1, BigFraction operand2) {
				return operand1.compareTo(operand2);
			}
			public int intOperate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]);
			}
			public void operate(BigFraction[] operand1, int index1, BigFraction[] operand2, int index2, int[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]);
			}
		};
		return checkComplete(ops, IntBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingUnaryOperator<BigFraction, BigFraction[]>[] initAggUnary() {
		final AggregatingUnaryOperator<BigFraction, BigFraction[]>[] ops = new AggregatingUnaryOperator[AggregatingUnaryOperator.Id.values().length];
		ops[AggregatingUnaryOperator.Id.min.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return null;
				}
				BigFraction min = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					min = min.min(src[srcIndexFrom + i]);
				}
				return min;
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return null;
				}
				BigFraction min = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						min = min.min(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return min;
			}
		};
		ops[AggregatingUnaryOperator.Id.max.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return null;
				}
				BigFraction max = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					max = max.max(src[srcIndexFrom + i]);
				}
				return max;
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return null;
				}
				BigFraction max = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						max = max.max(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return max;
			}
		};
		ops[AggregatingUnaryOperator.Id.sum.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				BigFraction sum = BigFraction.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src[srcIndexFrom + i]);
				}
				return sum;
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				BigFraction sum = BigFraction.ZERO;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.add(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.prod.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				BigFraction sum = BigFraction.ONE;
				for (int i = 0; i < length; i++) {
					sum = sum.multiply(src[srcIndexFrom + i]);
				}
				return sum;
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				BigFraction sum = BigFraction.ONE;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.multiply(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.sumSquared.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				BigFraction sum = BigFraction.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src[srcIndexFrom + i].pow(2));
				}
				return sum;
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				BigFraction sum = BigFraction.ZERO;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.add(src[srcRowFrom + r][srcColFrom + c].pow(2));
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.normDivisor.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				if (length == 0) return BigFraction.ZERO;
				BigInteger num 	= src[srcIndexFrom].getNumerator().abs();
				BigInteger den	= src[srcIndexFrom].isZero() ? BigInteger.ZERO : src[srcIndexFrom].getDenominator();
				for (int i = 1; i < length; i++) {
					if (num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE)) {
						break;
					}
					final BigFraction val = src[srcIndexFrom + i];
					num = num.gcd(val.getNumerator());
					if (val.signum() != 0) {
						den = den.gcd(val.getDenominator());
					}
				}
				return num.signum() == 0 ? BigFraction.ZERO : BigFraction.valueOf(num, den);
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) return BigFraction.ZERO;
				BigInteger num 	= src[srcRowFrom][srcColFrom].getNumerator().abs();
				BigInteger den	= src[srcRowFrom][srcColFrom].isZero() ? BigInteger.ZERO : src[srcRowFrom][srcColFrom].getDenominator();
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						if (num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE)) {
							break;
						}
						final BigFraction val = src[srcRowFrom + r][srcColFrom + c];
						num = num.gcd(val.getNumerator());
						if (val.signum() != 0) {
							den = den.gcd(val.getDenominator());
						}
					}
				}
				return num.signum() == 0 ? BigFraction.ZERO : BigFraction.valueOf(num, den);
			}
		};
		ops[AggregatingUnaryOperator.Id.squeezeDivisor.ordinal()] = new BigFractionAggregatingUnaryOperator() {
			public BigFraction operate(BigFraction[] src, int srcIndexFrom, int length) {
				if (length == 0) return BigFraction.ZERO;
				BigInteger num 	= src[srcIndexFrom].getNumerator().abs();
				BigInteger den	= src[srcIndexFrom].getDenominator();
				for (int i = 1; i < length; i++) {
					final BigFraction val = src[srcIndexFrom + i];
					num = num.gcd(val.getNumerator());
					if (val.signum() != 0) {
						final BigInteger gcd = den.gcd(val.getDenominator());
						den = den.multiply(val.getDenominator().abs()).divide(gcd);
					}					
				}
				return num.signum() == 0 ? BigFraction.ZERO : BigFraction.valueOf(num, den);
			}
			public BigFraction operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) return BigFraction.ZERO;
				BigInteger num 	= src[srcRowFrom][srcColFrom].getNumerator().abs();
				BigInteger den	= src[srcRowFrom][srcColFrom].getDenominator();
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						final BigFraction val = src[srcRowFrom + r][srcColFrom + c];
						num = num.gcd(val.getNumerator());
						if (val.signum() != 0) {
							final BigInteger gcd = den.gcd(val.getDenominator());
							den = den.multiply(val.getDenominator().abs()).divide(gcd);
						}
					}
				}
				return num.signum() == 0 ? BigFraction.ZERO : BigFraction.valueOf(num, den);
			}
		};
		return checkComplete(ops, AggregatingUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingBinaryOperator<BigFraction, BigFraction[]>[] initAggBinary() {
		final AggregatingBinaryOperator<BigFraction, BigFraction[]>[] ops = new AggregatingBinaryOperator[AggregatingBinaryOperator.Id.values().length];
		ops[AggregatingBinaryOperator.Id.innerProduct.ordinal()] = new BigFractionAggregatingBinaryOperator() {
			public BigFraction operate(BigFraction[] src1, int src1IndexFrom, BigFraction[] src2, int src2IndexFrom, int length) {
				BigFraction sum = BigFraction.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1IndexFrom + i].multiply(src2[src2IndexFrom + i]));
				}
				return sum;
			}
			public BigFraction operate(BigFraction[][] src1, int src1RowFrom, int src1Col, BigFraction[] src2, int src2IndexFrom, int length) {
				BigFraction sum = BigFraction.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1RowFrom + i][src1Col].multiply(src2[src2IndexFrom + i]));
				}
				return sum;
			}
			public BigFraction operate(BigFraction[][] src1, int src1RowFrom, int src1Col, BigFraction[][] src2, int src2RowFrom, int src2Col, int length) {
				BigFraction sum = BigFraction.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1RowFrom + i][src1Col].multiply(src2[src2RowFrom + i][src2Col]));
				}
				return sum;
			}
		};
		return checkComplete(ops, AggregatingBinaryOperator.Id.values());
	}
	
	private static <A> A[] checkComplete(final A[] arr, Enum[] ids) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) throw new RuntimeException("internal error, implementation missing for " + arr.getClass().getComponentType().getName() + " for constant " + ids[i]);
		}
		return arr;
	}
	
	private static class BigFractionConstOperator extends AbstractNullaryOperator<BigFraction, BigFraction[]> {
		private final BigFraction value;
		public BigFractionConstOperator(BigFraction value) {
			this.value = value;
		}
		public BigFraction operate() {
			return value;
		}
		public void operate(BigFraction[] dst, int dstIndex) {
			dst[dstIndex] = value;
		}
	}
	private abstract static class BigFractionAggregatingUnaryOperator implements AggregatingUnaryOperator<BigFraction, BigFraction[]> {
		public void operate(BigFraction[] src, int srcIndexFrom, int srcIndexTo, BigFraction[] dst, int dstIndex) {
			dst[dstIndex] = operate(src, srcIndexFrom, srcIndexTo);
		}
		public void operate(BigFraction[][] src, int srcRowFrom, int srcColFrom, int rows, int cols, BigFraction[] dst, int dstIndex) {
			dst[dstIndex] = operate(src, srcRowFrom, srcColFrom, rows, cols);
		}
	}
	private abstract static class BigFractionAggregatingBinaryOperator implements AggregatingBinaryOperator<BigFraction, BigFraction[]> {
		public void operate(BigFraction[] src1, int src1IndexFrom, BigFraction[] src2, int src2IndexFrom, BigFraction[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1IndexFrom, src2, src2IndexFrom, length);
		}
		public void operate(BigFraction[][] src1, int src1RowFrom, int src1Col, BigFraction[] src2, int src2IndexFrom, BigFraction[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1RowFrom, src1Col, src2, src2IndexFrom, length);
		}
		public void operate(BigFraction[][] src1, int src1RowFrom, int src1Col, BigFraction[][] src2, int src2RowFrom, int src2Col, BigFraction[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1RowFrom, src1Col, src2, src2RowFrom, src2Col, length);
		}
	}

}
