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

import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.array.impl.DoubleArrayOperations;
import ch.javasoft.math.linalg.DefaultLinAlgOperations;
import ch.javasoft.math.linalg.GaussPivotingFactory;
import ch.javasoft.math.linalg.LinAlgOperations;
import ch.javasoft.math.linalg.impl.DoubleGaussPivoting;
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
import ch.javasoft.util.numeric.Zero;

/**
 * Number operators for doubles. A {@link #DEFAULT default instance} is 
 * available as constant. If certain results should be rounded, a rounding 
 * specific instance can be {@link #DoubleOperators(Zero) constructed}. 
 */
public class DoubleOperators implements NumberOperators<Double, double[]> {
	
	/**
	 * Zero constant
	 */
	public static final Double ZERO = Double.valueOf(0);
	/**
	 * One constant
	 */
	public static final Double ONE = Double.valueOf(1);
	
	/**
	 * The default instance, all operators perform the plain operations without
	 * rounding
	 */
	public static final DoubleOperators DEFAULT = new DoubleOperators();
	
	private final LinAlgOperations<Double, double[]> 			linAlgOps;
	
	private final NullaryOperator<Double, double[]>[] 			nullary;
	private final UnaryOperator<Double, double[]>[] 			unary;
	private final BooleanUnaryOperator<Double, double[]>[] 		boolUnary;
	private final IntUnaryOperator<Double, double[]>[] 			intUnary;
	private final ConvertingUnaryOperator<Number, Number[], Double, double[]> converter;
	private final BinaryOperator<Double, double[]>[] 			binary;
	private final BooleanBinaryOperator<Double, double[]>[] 	boolBinary;
	private final IntBinaryOperator<Double, double[]>[] 		intBinary;
	private final AggregatingUnaryOperator<Double, double[]>[] 	aggUnary;
	private final AggregatingBinaryOperator<Double, double[]>[] aggBinary;


	// see DEFAULT constant
	protected DoubleOperators() {
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
		linAlgOps	= new DefaultLinAlgOperations<Double, double[]>(this, DoubleArrayOperations.INSTANCE, DoubleGaussPivoting.ABS_G);
	}
	/**
	 * Constructor for an instance which performs some operations with rounding.
	 * In particular, unary and binary operators returning an int or boolean
	 * value use the specified {@code zero} instance to round the result. 
	 * Typical operations affected by rounding are comparators or the signum 
	 * function.
	 * 
	 * @param zero	the zero instance used for rounded operations, such as 
	 * 				comparison operators or the signum function
	 */
	public DoubleOperators(Zero zero) {
		nullary		= initNullary();
		unary 		= initUnary(zero);
		boolUnary 	= initBoolUnary(zero);
		intUnary 	= initIntUnary(zero);
		converter	= initConverter();
		binary 		= initBinary();
		boolBinary 	= initBoolBinary(zero);
		intBinary 	= initIntBinary(zero);
		aggUnary	= initAggUnary(zero);
		aggBinary 	= initAggBinary();
		linAlgOps	= new DefaultLinAlgOperations<Double, double[]>(this, DoubleArrayOperations.INSTANCE, DoubleGaussPivoting.ABS_G);
	}
	
	public Class<Double> numberClass() {
		return Double.class;
	}
	public Class<double[]> arrayClass() {
		return double[].class;
	}
	public DivisionSupport getDivisionSupport() {
		return DivisionSupport.SUFFICIENTLY_EXACT;
	}
	public LinAlgOperations<Double, double[]> getLinAlgOperations() {
		return linAlgOps;
	}
	public LinAlgOperations<Double, double[]> getLinAlgOperations(GaussPivotingFactory<Double, double[]> gaussPivotingFactory) {
		return new DefaultLinAlgOperations<Double, double[]>(getNumberArrayOperations(), gaussPivotingFactory);
	}
	public ArrayOperations<double[]> getArrayOperations() {
		return linAlgOps.getArrayOperations();
	}
	public NumberArrayOperations<Double, double[]> getNumberArrayOperations() {
		return linAlgOps.getNumberArrayOperations();
	}
	
	public Double zero() {
		return ZERO;
	}
	public Double one() {
		return ONE;
	}
	
	public NullaryOperator<Double, double[]> constant(Double value) {
		final double dval = value.doubleValue();
		if (dval == 0) {
			return nullary(NullaryOperator.Id.zero);
		}
		else if (dval == 1) {
			return nullary(NullaryOperator.Id.one);
		}
		return new DoubleConstOperator(dval);
	}
	public ConvertingUnaryOperator<Number, Number[], Double, double[]> converter() {
		return converter;
	}
	public NullaryOperator<Double, double[]> nullary(NullaryOperator.Id id) {
		return nullary[id.ordinal()];
	}
	public UnaryOperator<Double, double[]> unary(UnaryOperator.Id id) {
		return unary[id.ordinal()];
	}

	public BooleanUnaryOperator<Double, double[]> booleanUnary(BooleanUnaryOperator.Id id) {
		return boolUnary[id.ordinal()];
	}

	public IntUnaryOperator<Double, double[]> intUnary(IntUnaryOperator.Id id) {
		return intUnary[id.ordinal()];
	}

	public BinaryOperator<Double, double[]> binary(BinaryOperator.Id id) {
		return binary[id.ordinal()];
	}

	public BooleanBinaryOperator<Double, double[]> booleanBinary(BooleanBinaryOperator.Id id) {
		return boolBinary[id.ordinal()];
	}

	public IntBinaryOperator<Double, double[]> intBinary(IntBinaryOperator.Id id) {
		return intBinary[id.ordinal()];
	}
	
	public AggregatingUnaryOperator<Double, double[]> aggregatingUnary(AggregatingUnaryOperator.Id id) {
		return aggUnary[id.ordinal()];
	}
	
	public AggregatingBinaryOperator<Double, double[]> aggregatingBinary(Id id) {
		return aggBinary[id.ordinal()];
	}
	
	@SuppressWarnings("unchecked")
	private static NullaryOperator<Double, double[]>[] initNullary() {
		final NullaryOperator<Double, double[]>[] ops = new NullaryOperator[NullaryOperator.Id.values().length];
		ops[NullaryOperator.Id.zero.ordinal()] = new DoubleConstOperator(0);
		ops[NullaryOperator.Id.one.ordinal()] = new DoubleConstOperator(1);
		ops[NullaryOperator.Id.random.ordinal()] = new AbstractNullaryOperator<Double, double[]>() {
			public Double operate() {
				return Double.valueOf(Math.random());
			}
			public void operate(double[] dst, int dstIndex) {
				dst[dstIndex] = Math.random();
			}
		};
		return checkComplete(ops, NullaryOperator.Id.values());
	}
	private static UnaryOperator<Double, double[]>[] initUnary() {
		final UnaryOperator<Double, double[]>[] ops = initUnary(null);
		ops[UnaryOperator.Id.normalize.ordinal()] = ops[UnaryOperator.Id.identity.ordinal()];
		return ops;
	}
	@SuppressWarnings("unchecked")
	private static UnaryOperator<Double, double[]>[] initUnary(final Zero zero) {
		final UnaryOperator<Double, double[]>[] ops = new UnaryOperator[UnaryOperator.Id.values().length];
		ops[UnaryOperator.Id.identity.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
			public Double operate(Double operand) {
				return operand;
			}
			public Double operate(double[] src, int srcIndex) {
				return Double.valueOf(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex];
			}
		};
//		ops[UnaryOperator.Id.normalize.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
//			public Double operate(Double operand) {
//				return zero.isZero(operand.doubleValue()) ? Double.valueOf(0) : operand;
//			}
//			public Double operate(double[] src, int srcIndex) {
//				return Double.valueOf(zero.roundZero(src[srcIndex]));
//			}
//			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
//				dst[dstIndex] = zero.roundZero(src[srcIndex]);
//			}
//		};
		ops[UnaryOperator.Id.normalize.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
			public Double operate(Double operand) {
				return zero.isInteger(operand.doubleValue()) ?
						Double.valueOf(zero.roundInteger(operand.doubleValue())) : operand;
			}
			public Double operate(double[] src, int srcIndex) {
				return Double.valueOf(zero.roundInteger(src[srcIndex]));
			}
			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
				dst[dstIndex] = zero.roundInteger(src[srcIndex]);
			}
		};
		ops[UnaryOperator.Id.abs.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
			public Double operate(Double operand) {
				return Double.valueOf(Math.abs(operand.doubleValue()));
			}
			public Double operate(double[] src, int srcIndex) {
				return Double.valueOf(Math.abs(src[srcIndex]));
			}
			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
				dst[dstIndex] = Math.abs(src[srcIndex]);
			}
		};
		ops[UnaryOperator.Id.negate.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
			public Double operate(Double operand) {
				return Double.valueOf(-operand.doubleValue());
			}
			public Double operate(double[] src, int srcIndex) {
				return Double.valueOf(-src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
				dst[dstIndex] = -src[srcIndex];
			}
		};
		ops[UnaryOperator.Id.invert.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
			public Double operate(Double operand) {
				return Double.valueOf(1 / operand.doubleValue());
			}
			public Double operate(double[] src, int srcIndex) {
				return Double.valueOf(1 / src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
				dst[dstIndex] = 1 / src[srcIndex];
			}
		};
		ops[UnaryOperator.Id.square.ordinal()] = new AbstractUnaryOperator<Double, double[]>() {
			public Double operate(Double operand) {
				final double val = operand.doubleValue();
				return Double.valueOf(val * val);
			}
			public Double operate(double[] src, int srcIndex) {
				final double val = src[srcIndex];
				return Double.valueOf(val * val);
			}
			public void operate(double[] src, int srcIndex, double[] dst, int dstIndex) {
				final double val = src[srcIndex];
				dst[dstIndex] = val * val;
			}
		};
		return checkComplete(ops, UnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntUnaryOperator<Double, double[]>[] initIntUnary() {
		final IntUnaryOperator<Double, double[]>[] ops = new IntUnaryOperator[IntUnaryOperator.Id.values().length];
		ops[IntUnaryOperator.Id.signum.ordinal()] = new AbstractIntUnaryOperator<Double, double[]>() {
			public int intOperate(Double operand) {
				return (int)Math.signum(operand.doubleValue());
			}
			public int intOperate(double[] operand, int index) {
				return (int)Math.signum(operand[index]);
			}
			public void operate(double[] src, int srcIndex, int[] dst, int dstIndex) {
				dst[dstIndex] = (int)Math.signum(src[srcIndex]);
			}
		};
		return checkComplete(ops, IntUnaryOperator.Id.values());
	}
	private ConvertingUnaryOperator<Number, Number[], Double, double[]> initConverter() {
		return new ConvertingUnaryOperator<Number, Number[], Double, double[]>() {
			public Double operate(Number operand) {
				return operand instanceof Double ? ((Double)operand) : Double.valueOf(operand.doubleValue());
			}
			public void operate(Number[] src, int srcIndex, double[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].doubleValue();
			}
		};
	}
	@SuppressWarnings("unchecked")
	private static IntUnaryOperator<Double, double[]>[] initIntUnary(final Zero zero) {
		final IntUnaryOperator<Double, double[]>[] ops = new IntUnaryOperator[IntUnaryOperator.Id.values().length];
		ops[IntUnaryOperator.Id.signum.ordinal()] = new AbstractIntUnaryOperator<Double, double[]>() {
			public int intOperate(Double operand) {
				return zero.sgn(operand.doubleValue());
			}
			public int intOperate(double[] operand, int index) {
				return zero.sgn(operand[index]);
			}
			public void operate(double[] src, int srcIndex, int[] dst, int dstIndex) {
				dst[dstIndex] = zero.sgn(src[srcIndex]);
			}
		};
		return checkComplete(ops, IntUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanUnaryOperator<Double, double[]>[] initBoolUnary() {
		final BooleanUnaryOperator<Double, double[]>[] ops = new BooleanUnaryOperator[BooleanUnaryOperator.Id.values().length];
		ops[BooleanUnaryOperator.Id.isZero.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() == 0;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] == 0;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] == 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonZero.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() != 0;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] != 0;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] != 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isOne.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() == 1;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] == 1;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] == 1;
			}
		};
		ops[BooleanUnaryOperator.Id.isPositive.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() > 0;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] > 0;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] > 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNegative.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() < 0;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] < 0;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] < 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonPositive.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() <= 0;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] <= 0;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] <= 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonNegative.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return operand.doubleValue() >= 0;
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return src[srcIndex] >= 0;
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex] >= 0;
			}
		};
		return checkComplete(ops, BooleanUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanUnaryOperator<Double, double[]>[] initBoolUnary(final Zero zero) {
		final BooleanUnaryOperator<Double, double[]>[] ops = new BooleanUnaryOperator[BooleanUnaryOperator.Id.values().length];
		ops[BooleanUnaryOperator.Id.isZero.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return zero.isZero(operand.doubleValue());
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return zero.isZero(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isZero(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isNonZero.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return zero.isNonZero(operand.doubleValue());
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return zero.isNonZero(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isNonZero(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isOne.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return zero.isZero(operand.doubleValue() - 1);
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return zero.isZero(src[srcIndex] - 1);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isZero(src[srcIndex] - 1);
			}
		};
		ops[BooleanUnaryOperator.Id.isPositive.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return zero.isPositive(operand.doubleValue());
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return zero.isPositive(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isPositive(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isNegative.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return zero.isNegative(operand.doubleValue());
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return zero.isNegative(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isNegative(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isNonPositive.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return !zero.isPositive(operand.doubleValue());
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return !zero.isPositive(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !zero.isPositive(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isNonNegative.ordinal()] = new AbstractBooleanUnaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand) {
				return !zero.isNegative(operand.doubleValue());
			}
			public boolean booleanOperate(double[] src, int srcIndex) {
				return !zero.isNegative(src[srcIndex]);
			}
			public void operate(double[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !zero.isNegative(src[srcIndex]);
			}
		};
		return checkComplete(ops, BooleanUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BinaryOperator<Double, double[]>[] initBinary() {
		final BinaryOperator<Double, double[]>[] ops = new BinaryOperator[BinaryOperator.Id.values().length];
		ops[BinaryOperator.Id.add.ordinal()] = new AbstractBinaryOperator<Double, double[]>() {
			public Double operate(Double operand1, Double operand2) {
				return Double.valueOf(operand1.doubleValue() + operand2.doubleValue());
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, double[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] + operand2[index2];
			}
		};
		ops[BinaryOperator.Id.subtract.ordinal()] = new AbstractBinaryOperator<Double, double[]>() {
			public Double operate(Double operand1, Double operand2) {
				return Double.valueOf(operand1.doubleValue() - operand2.doubleValue());
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, double[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] - operand2[index2];
			}
		};
		ops[BinaryOperator.Id.multiply.ordinal()] = new AbstractBinaryOperator<Double, double[]>() {
			public Double operate(Double operand1, Double operand2) {
				return Double.valueOf(operand1.doubleValue() * operand2.doubleValue());
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, double[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] * operand2[index2];
			}
		};
		ops[BinaryOperator.Id.divide.ordinal()] = new AbstractBinaryOperator<Double, double[]>() {
			public Double operate(Double operand1, Double operand2) {
				return Double.valueOf(operand1.doubleValue() / operand2.doubleValue());
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, double[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] / operand2[index2];
			}
		};
		ops[BinaryOperator.Id.min.ordinal()] = new AbstractBinaryOperator<Double, double[]>() {
			public Double operate(Double operand1, Double operand2) {
				return operand1.compareTo(operand2) <= 0 ? operand1 : operand2;
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, double[] dst, int dstIndex) {
				dst[dstIndex] = Math.min(operand1[index1], operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.max.ordinal()] = new AbstractBinaryOperator<Double, double[]>() {
			public Double operate(Double operand1, Double operand2) {
				return operand1.compareTo(operand2) >= 0 ? operand1 : operand2;
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, double[] dst, int dstIndex) {
				dst[dstIndex] = Math.max(operand1[index1], operand2[index2]);
			}
		};
		return checkComplete(ops, BinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanBinaryOperator<Double, double[]>[] initBoolBinary() {
		final BooleanBinaryOperator<Double, double[]>[] ops = new BooleanBinaryOperator[BooleanBinaryOperator.Id.values().length];
		ops[BooleanBinaryOperator.Id.less.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return operand1.doubleValue() < operand2.doubleValue();
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return operand1[index1] < operand2[index2];
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] < operand2[index2];
			}
		};
		ops[BooleanBinaryOperator.Id.lessOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return operand1.doubleValue() <= operand2.doubleValue();
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return operand1[index1] <= operand2[index2];
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] <= operand2[index2];
			}
		};
		ops[BooleanBinaryOperator.Id.equal.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return operand1.doubleValue() == operand2.doubleValue();
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return operand1[index1] == operand2[index2];
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] == operand2[index2];
			}
		};
		ops[BooleanBinaryOperator.Id.unequal.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return operand1.doubleValue() != operand2.doubleValue();
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return operand1[index1] != operand2[index2];
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] != operand2[index2];
			}
		};
		ops[BooleanBinaryOperator.Id.greaterOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return operand1.doubleValue() >= operand2.doubleValue();
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return operand1[index1] >= operand2[index2];
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] >= operand2[index2];
			}			
		};
		ops[BooleanBinaryOperator.Id.greater.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return operand1.doubleValue() > operand2.doubleValue();
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return operand1[index1] > operand2[index2];
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1] > operand2[index2];
			}
		};
		return checkComplete(ops, BooleanBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanBinaryOperator<Double, double[]>[] initBoolBinary(final Zero zero) {
		final BooleanBinaryOperator<Double, double[]>[] ops = new BooleanBinaryOperator[BooleanBinaryOperator.Id.values().length];
		ops[BooleanBinaryOperator.Id.less.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return zero.isNegative(operand1.doubleValue() - operand2.doubleValue());
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return zero.isNegative(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isNegative(operand1[index1] - operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.lessOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return !zero.isPositive(operand1.doubleValue() - operand2.doubleValue());
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return !zero.isPositive(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !zero.isPositive(operand1[index1] - operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.equal.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return zero.isZero(operand1.doubleValue() - operand2.doubleValue());
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return zero.isZero(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isZero(operand1[index1] - operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.unequal.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return zero.isNonZero(operand1.doubleValue() - operand2.doubleValue());
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return zero.isNonZero(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isNonZero(operand1[index1] - operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.greaterOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return !zero.isNegative(operand1.doubleValue() - operand2.doubleValue());
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return !zero.isNegative(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !zero.isNegative(operand1[index1] - operand2[index2]);
			}			
		};
		ops[BooleanBinaryOperator.Id.greater.ordinal()] = new AbstractBooleanBinaryOperator<Double, double[]>() {
			public boolean booleanOperate(Double operand1, Double operand2) {
				return zero.isPositive(operand1.doubleValue() - operand2.doubleValue());
			}
			public boolean booleanOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return zero.isPositive(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = zero.isPositive(operand1[index1] - operand2[index2]);
			}
		};
		return checkComplete(ops, BooleanBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntBinaryOperator<Double, double[]>[] initIntBinary() {
		final IntBinaryOperator<Double, double[]>[] ops = new IntBinaryOperator[IntBinaryOperator.Id.values().length];
		ops[IntBinaryOperator.Id.compare.ordinal()] = new AbstractIntBinaryOperator<Double, double[]>() {
			public int intOperate(Double operand1, Double operand2) {
				return operand1.compareTo(operand2);
			}
			public int intOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return Double.compare(operand1[index1], operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, int[] dst, int dstIndex) {
				dst[dstIndex] = Double.compare(operand1[index1], operand2[index2]);
			}
		};
		return checkComplete(ops, IntBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntBinaryOperator<Double, double[]>[] initIntBinary(final Zero zero) {
		final IntBinaryOperator<Double, double[]>[] ops = new IntBinaryOperator[IntBinaryOperator.Id.values().length];
		ops[IntBinaryOperator.Id.compare.ordinal()] = new AbstractIntBinaryOperator<Double, double[]>() {
			public int intOperate(Double operand1, Double operand2) {
				return zero.sgn(operand1.doubleValue() - operand2.doubleValue());
			}
			public int intOperate(double[] operand1, int index1, double[] operand2, int index2) {
				return zero.sgn(operand1[index1] - operand2[index2]);
			}
			public void operate(double[] operand1, int index1, double[] operand2, int index2, int[] dst, int dstIndex) {
				dst[dstIndex] = zero.sgn(operand1[index1] - operand2[index2]);
			}
		};
		return checkComplete(ops, IntBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingUnaryOperator<Double, double[]>[] initAggUnary() {
		final AggregatingUnaryOperator<Double, double[]>[] ops = new AggregatingUnaryOperator[AggregatingUnaryOperator.Id.values().length];
		ops[AggregatingUnaryOperator.Id.min.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return Double.NaN;
				}
				double min = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					min = Math.min(min, src[srcIndexFrom + i]);
				}
				return min;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return Double.NaN;
				}
				double min = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						min = Math.min(min, src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return min;
			}
		};
		ops[AggregatingUnaryOperator.Id.max.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return Double.NaN;
				}
				double max = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					max = Math.max(max, src[srcIndexFrom + i]);
				}
				return max;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return Double.NaN;
				}
				double max = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						max = Math.max(max, src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return max;
			}
		};
		ops[AggregatingUnaryOperator.Id.sum.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				double sum = 0;
				for (int i = 0; i < length; i++) {
					sum += src[srcIndexFrom + i];
				}
				return sum;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				double sum = 0;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum += src[srcRowFrom + r][srcColFrom + c];
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.prod.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				double sum = 1;
				for (int i = 0; i < length; i++) {
					sum *= src[srcIndexFrom + i];
				}
				return sum;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				double sum = 1;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum *= src[srcRowFrom + r][srcColFrom + c];
					}
				}
				return sum;
			}
		};
		final DoubleAggregatingUnaryOperator sumSquared = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				double sum = 0;
				for (int i = 0; i < length; i++) {
					final double val = src[srcIndexFrom + i];
					sum += (val * val);
				}
				return sum;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				double sum = 0;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						final double val = src[srcRowFrom + r][srcColFrom + c];
						sum += (val * val);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.sumSquared.ordinal()] = sumSquared;
		ops[AggregatingUnaryOperator.Id.normDivisor.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				final double sum2 = sumSquared.doubleOperate(src, srcIndexFrom, length);
				return Math.sqrt(sum2);
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				final double sum2 = sumSquared.doubleOperate(src, srcRowFrom, srcColFrom, rows, cols);
				return Math.sqrt(sum2);
			}
		};
		ops[AggregatingUnaryOperator.Id.squeezeDivisor.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				double min = 0;
				for (int i = 0; i < length; i++) {
					final double abs = Math.abs(src[srcIndexFrom + i]);
					if (abs > 0) {
						if (min == 0) {
							min = abs;
						}
						else if (min > abs) {
							min = Math.min(min, abs);
						}
					}
				}
				return min;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				double min = 0;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						final double abs = Math.abs(src[srcRowFrom + r][srcColFrom + c]);
						if (abs > 0) {
							if (min == 0) {
								min = abs;
							}
							else if (min > abs) {
								min = Math.min(min, abs);
							}
						}
					}
				}
				return min;
			}
		};
		return checkComplete(ops, AggregatingUnaryOperator.Id.values());
	}
	private static AggregatingUnaryOperator<Double, double[]>[] initAggUnary(final Zero zero) {
		final AggregatingUnaryOperator<Double, double[]>[] ops = initAggUnary();
		ops[AggregatingUnaryOperator.Id.squeezeDivisor.ordinal()] = new DoubleAggregatingUnaryOperator() {
			@Override
			protected double doubleOperate(double[] src, int srcIndexFrom, int length) {
				double min = 0;
				for (int i = 0; i < length; i++) {
					final double abs = Math.abs(zero.roundZero(src[srcIndexFrom + i]));
					if (abs > 0) {
						if (min == 0) {
							min = abs;
						}
						else if (min > abs) {
							min = Math.min(min, abs);
						}
					}
				}
				return min;
			}
			@Override
			protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				double min = 0;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						final double abs = Math.abs(zero.roundZero(src[srcRowFrom + r][srcColFrom + c]));
						if (abs > 0) {
							if (min == 0) {
								min = abs;
							}
							else if (min > abs) {
								min = Math.min(min, abs);
							}
						}
					}
				}
				return min;
			}
		};
		return checkComplete(ops, AggregatingUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingBinaryOperator<Double, double[]>[] initAggBinary() {
		final AggregatingBinaryOperator<Double, double[]>[] ops = new AggregatingBinaryOperator[AggregatingBinaryOperator.Id.values().length];
		ops[AggregatingBinaryOperator.Id.innerProduct.ordinal()] = new DoubleAggregatingBinaryOperator() {
			@Override
			protected double doubleOperate(double[] src1, int src1IndexFrom, double[] src2, int src2IndexFrom, int length) {
				double sum = 0;
				for (int i = 0; i < length; i++) {
					sum += (src1[src1IndexFrom + i] * src2[src2IndexFrom + i]);
				}
				return sum;
			}
			@Override
			protected double doubleOperate(double[][] src1, int src1RowFrom, int src1Col, double[] src2, int src2IndexFrom, int length) {
				double sum = 0;
				for (int i = 0; i < length; i++) {
					sum += (src1[src1RowFrom + i][src1Col] * src2[src2IndexFrom + i]);
				}
				return sum;
			}
			@Override
			protected double doubleOperate(double[][] src1, int src1RowFrom, int src1Col, double[][] src2, int src2RowFrom, int src2Col, int length) {
				double sum = 0;
				for (int i = 0; i < length; i++) {
					sum += (src1[src1RowFrom + i][src1Col] * src2[src2RowFrom + i][src2Col]);
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
	
	private static class DoubleConstOperator extends AbstractNullaryOperator<Double, double[]> {
		private final double value;
		public DoubleConstOperator(double value) {
			this.value = value;
		}
		public Double operate() {
			return Double.valueOf(value);
		}
		public void operate(double[] dst, int dstIndex) {
			dst[dstIndex] = value;
		}
	}
	private abstract static class DoubleAggregatingUnaryOperator implements AggregatingUnaryOperator<Double, double[]> {
		abstract protected double doubleOperate(double[] src, int srcIndexFrom, int srcIndexTo);
		abstract protected double doubleOperate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols);
		public Double operate(double[] src, int srcIndexFrom, int srcIndexTo) {
			return Double.valueOf(doubleOperate(src, srcIndexFrom, srcIndexTo));
		}
		public void operate(double[] src, int srcIndexFrom, int srcIndexTo, double[] dst, int dstIndex) {
			dst[dstIndex] = doubleOperate(src, srcIndexFrom, srcIndexTo);
		}
		public Double operate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
			return Double.valueOf(doubleOperate(src, srcRowFrom, srcColFrom, rows, cols));
		}
		public void operate(double[][] src, int srcRowFrom, int srcColFrom, int rows, int cols, double[] dst, int dstIndex) {
			dst[dstIndex] = doubleOperate(src, srcRowFrom, srcColFrom, rows, cols);
		}
	}
	private abstract static class DoubleAggregatingBinaryOperator implements AggregatingBinaryOperator<Double, double[]> {
		abstract protected double doubleOperate(double[] src1, int src1IndexFrom, double[] src2, int src2IndexFrom, int length);
		abstract protected double doubleOperate(double[][] src1, int src1RowFrom, int src1Col, double[] src2, int src2IndexFrom, int length);
		abstract protected double doubleOperate(double[][] src1, int src1RowFrom, int src1Col, double[][] src2, int src2RowFrom, int src2Col, int length);
		public Double operate(double[] src1, int src1IndexFrom, double[] src2, int src2IndexFrom, int length) {
			return Double.valueOf(doubleOperate(src1, src1IndexFrom, src2, src2IndexFrom, length));
		}
		public void operate(double[] src1, int src1IndexFrom, double[] src2, int src2IndexFrom, double[] dst, int dstIndex, int length) {
			dst[dstIndex] = doubleOperate(src1, src1IndexFrom, src2, src2IndexFrom, length);
		}
		public Double operate(double[][] src1, int src1RowFrom, int src1Col, double[] src2, int src2IndexFrom, int length) {
			return Double.valueOf(doubleOperate(src1, src1RowFrom, src1Col, src2, src2IndexFrom, length));
		}
		public void operate(double[][] src1, int src1RowFrom, int src1Col, double[] src2, int src2IndexFrom, double[] dst, int dstIndex, int length) {
			dst[dstIndex] = doubleOperate(src1, src1RowFrom, src1Col, src2, src2IndexFrom, length);
		}
		public Double operate(double[][] src1, int src1RowFrom, int src1Col, double[][] src2, int src2RowFrom, int src2Col, int length) {
			return Double.valueOf(doubleOperate(src1, src1RowFrom, src1Col, src2, src2RowFrom, src2Col, length));
		}
		public void operate(double[][] src1, int src1RowFrom, int src1Col, double[][] src2, int src2RowFrom, int src2Col, double[] dst, int dstIndex, int length) {
			dst[dstIndex] = doubleOperate(src1, src1RowFrom, src1Col, src2, src2RowFrom, src2Col, length);
		}
	}

}
