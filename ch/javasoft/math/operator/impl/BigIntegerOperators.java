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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.array.ArrayOperations;
import ch.javasoft.math.array.NumberArrayOperations;
import ch.javasoft.math.array.NumberOperators;
import ch.javasoft.math.array.impl.DefaultArrayOperations;
import ch.javasoft.math.linalg.DefaultLinAlgOperations;
import ch.javasoft.math.linalg.GaussPivotingFactory;
import ch.javasoft.math.linalg.LinAlgOperations;
import ch.javasoft.math.linalg.impl.BigIntegerGaussPivoting;
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
 * Number operators for {@link BigInteger} numbers. A 
 * {@link #INTEGER_DIVISION_INSTANCE default instance} is available as constant, where divisions
 * are performed as integer division, ignoring possible remainders. Alternate
 * instances are available, e.g. if division should throw an exception.  
 */
public class BigIntegerOperators implements NumberOperators<BigInteger, BigInteger[]> {
	
	/**
	 * <code>DivisionMode</code> is the method used to divide two integer
	 * values, which might result in a remainder value.
	 */
	public static enum DivisionMode implements DivisionSupport {
		/**
		 * No division is supported. All division and inversion operations throw 
		 * an {@link ArithmeticException}, even if the operation would yield
		 * an integer value.
		 */
		None {
			@Override
			public BigInteger divide(BigInteger dividend, BigInteger divisor) {
				throw new ArithmeticException("division not supported");
			} 
		},
		/**
		 * Integer division is if exact division is possible. If exact division
		 * is not possible, that is, if the remainder is unequal to zero, an
		 * {@link ArithmeticException} is thrown.
		 */
		Exact {
			@Override
			public BigInteger divide(BigInteger dividend, BigInteger divisor) {
				if (BigInteger.ZERO.equals(divisor)) {
					throw new ArithmeticException("division by 0");
				}
				if (BigInteger.ONE.equals(divisor)) {
					return dividend;
				}
				final BigDecimal a = new BigDecimal(dividend);
				final BigDecimal b = new BigDecimal(divisor);
				return a.divide(b, 0, RoundingMode.UNNECESSARY).toBigIntegerExact();
			} 
		},
		/**
		 * Integer division is used truncating remainders if necessary. Division
		 * and inversion operations never throw an exception, unless the divisor
		 * is zero.
		 */
		Integer {
			@Override
			public BigInteger divide(BigInteger dividend, BigInteger divisor) {
				return dividend.divide(divisor);
			} 
		}, 
		/**
		 * Division is performed using rounding if necessary. The closest 
		 * integer is used as defined by the
		 * {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding mode. Division
		 * and inversion operations never throw an exception, unless the divisor
		 * zero.
		 */
		Round {
			@Override
			public BigInteger divide(BigInteger dividend, BigInteger divisor) {
				if (BigInteger.ZERO.equals(divisor)) {
					throw new ArithmeticException("division by 0");
				}
				if (BigInteger.ONE.equals(divisor)) {
					return dividend;
				}
				final BigDecimal a = new BigDecimal(dividend);
				final BigDecimal b = new BigDecimal(divisor);
				return a.divide(b, 0, RoundingMode.HALF_EVEN).toBigIntegerExact();
			} 
		};
		/**
		 * Returns true if division always causes an {@link ArithmeticException}. 
		 * The method returns {@code true} for {@link #None} division mode.
		 */
		public boolean alwaysCausesException() {
			return None.equals(this);
		}
		/**
		 * Returns true if division might cause an {@link ArithmeticException},
		 * not meaning division by zero, but since rounding or truncation would
		 * be necessary. The method returns {@code true} for {@link #None} and 
		 * {@link #Exact} division mode.
		 */
		public boolean mightCauseException() {
			return None.equals(this) || Exact.equals(this);
		}
		/**
		 * Returns true if division is exact, that is, for {@link #Exact} 
		 * division mode.
		 */
		public boolean isExact() {
			return DivisionMode.Exact.equals(this);
		}
		/**
		 * Returns true if division is sufficiently exact to be used in an 
		 * numeric algorithm. The method returns {@code true} for 
		 * {@link #Exact} division mode.
		 */
		public boolean isSufficientlyExact() {
			return DivisionMode.Exact.equals(this);
		}
		/**
		 * Performs the mode specific integer division
		 * 
		 * @param dividend	the dividend
		 * @param divisor	the divisor
		 * @return	{@code divident / divisor}
		 */
		abstract public BigInteger divide(BigInteger dividend, BigInteger divisor);
	}
	/**
	 * <code>ConversionMode</code> is the method to use when other numeric
	 * types are converted into a big integer.
	 */
	public static enum ConversionMode {
		/**
		 * Conversion from other number type throws an 
		 * {@link ArithmeticException} if the number did not represent 
		 * an integer value, that is, if rounding or truncating would be 
		 * necessary for conversion.		 
		 */
		Exact {
			@Override
			public BigInteger convert(Number value) {
				if (value instanceof BigInteger) {
					return (BigInteger)value;
				}
				if (value instanceof BigDecimal) {
					return ((BigDecimal)value).toBigIntegerExact();
				}
				if (value instanceof BigFraction) {
					return ((BigFraction)value).toBigIntegerExact();
				}
				if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
					return BigInteger.valueOf(value.longValue());
				}
				if (value instanceof Double || value instanceof Float) {
					return BigDecimal.valueOf(value.doubleValue()).toBigIntegerExact();
				}
				return new BigInteger(value.toString());
			}
		},
		/**
		 * Conversion is performed using truncation if necessary. For integer
		 * fractions, integer division is used. For floating point numbers, the
		 * decimal places are simply omitted.
		 */
		Truncate {
			@Override
			public BigInteger convert(Number value) {
				if (value instanceof BigInteger) {
					return (BigInteger)value;
				}
				if (value instanceof BigDecimal) {
					return ((BigDecimal)value).toBigInteger();
				}
				if (value instanceof BigFraction) {
					return ((BigFraction)value).toBigInteger();
				}
				if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
					return BigInteger.valueOf(value.longValue());
				}
				if (value instanceof Double || value instanceof Float) {
					return BigDecimal.valueOf(value.doubleValue()).toBigInteger();
				}
				return new BigInteger(value.toString());
			}
		},
		/**
		 * Conversion is performed using rounding if necessary. The closest 
		 * integer is used as defined by the
		 * {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding mode.
		 */
		Round {
			public BigInteger convertBigDecimal(BigDecimal value) {
				return value.divide(BigDecimal.ONE, 0, RoundingMode.HALF_EVEN).toBigIntegerExact();
			}
			@Override
			public BigInteger convert(Number value) {
				if (value instanceof BigInteger) {
					return (BigInteger)value;
				}
				if (value instanceof BigDecimal) {
					return convertBigDecimal((BigDecimal)value);
				}
				if (value instanceof BigFraction) {
					return ((BigFraction)value).toBigInteger(RoundingMode.HALF_EVEN);
				}
				if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
					return BigInteger.valueOf(value.longValue());
				}
				if (value instanceof Double || value instanceof Float) {
					return convertBigDecimal(BigDecimal.valueOf(value.doubleValue()));
				}
				return new BigInteger(value.toString());
			}
		};
		
		/**
		 * Returns true if conversion from another number value might cause
		 * an {@link ArithmeticException}. This is only true for {@link #Exact}
		 * conversion mode.
		 */
		public boolean conversionMightCauseException() {
			return Exact.equals(this);
		}
		/**
		 * Performs the mode specific conversion operation
		 * 
		 * @param value		the value to convert
		 * @return	the {@code value} converted into a big integer
		 */
		public abstract BigInteger convert(Number value);
	}
	
	/**
	 * The default instance, all operators perform the plain operations without
	 * rounding. Division is performed as integer division, possible remainders
	 * are ignored. This implementation uses {@link DivisionMode#Integer} and
	 * {@link ConversionMode#Truncate}.
	 */
	public static final BigIntegerOperators INTEGER_DIVISION_INSTANCE = new BigIntegerOperators(DivisionMode.Integer, ConversionMode.Truncate);
	/**
	 * The instance without division, that is, all division operations throw
	 * an {@link ArithmeticException}. All conversion operations from another
	 * number type also throw an {@link ArithmeticException} if the number did
	 * not represent an integer value. This implementation uses 
	 * {@link DivisionMode#None} and {@link ConversionMode#Exact}.
	 */
	public static final BigIntegerOperators NO_DIVISION_INSTANCE = new BigIntegerOperators(DivisionMode.None, ConversionMode.Exact);
	/**
	 * The instance with exact integer division, that is, division operations 
	 * throw an {@link ArithmeticException} if it does not yield an integer 
	 * value. All conversion operations from another number type also throw an 
	 * {@link ArithmeticException} if the number did not represent an integer 
	 * value. This implementation uses {@link DivisionMode#Exact} and 
	 * {@link ConversionMode#Exact}.
	 */
	public static final BigIntegerOperators EXACT_DIVISION_INSTANCE = new BigIntegerOperators(DivisionMode.Exact, ConversionMode.Exact);
	/**
	 * The instance with rounded integer division, that is, division operations
	 * use {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding. This 
	 * implementation uses {@link DivisionMode#Round} and
	 * {@link ConversionMode#Round}.
	 */
	public static final BigIntegerOperators ROUNDED_DIVISION_INSTANCE = new BigIntegerOperators(DivisionMode.Round, ConversionMode.Round);
	
	private final LinAlgOperations<BigInteger, BigInteger[]> 			linAlgOps;
	
	private final DivisionMode											divisionMode;
	private final ConversionMode										conversionMode;
	private final NullaryOperator<BigInteger, BigInteger[]>[] 			nullary;
	private final UnaryOperator<BigInteger, BigInteger[]>[] 			unary;
	private final BooleanUnaryOperator<BigInteger, BigInteger[]>[] 		boolUnary;
	private final IntUnaryOperator<BigInteger, BigInteger[]>[] 			intUnary;
	private final ConvertingUnaryOperator<Number, Number[], BigInteger, BigInteger[]> converter;
	private final BinaryOperator<BigInteger, BigInteger[]>[] 			binary;
	private final BooleanBinaryOperator<BigInteger, BigInteger[]>[] 	boolBinary;
	private final IntBinaryOperator<BigInteger, BigInteger[]>[] 		intBinary;
	private final AggregatingUnaryOperator<BigInteger, BigInteger[]>[] 	aggUnary;
	private final AggregatingBinaryOperator<BigInteger, BigInteger[]>[]	aggBinary;


	/**
	 * Constructor for <code>BigIntegerNumberOperators</code> with division and
	 * conversion mode. Note that constants are available for the most common 
	 * modes.
	 * 
	 * @param divMode	the division mode
	 * @param convMode	the conversion mode
	 */
	public BigIntegerOperators(DivisionMode divMode, ConversionMode convMode) {
		divisionMode	= divMode;
		conversionMode	= convMode;
		nullary				= initNullary();
		unary 				= initUnary(divMode);
		boolUnary 			= initBoolUnary();
		intUnary 			= initIntUnary();
		converter			= initConverter(convMode);
		binary 				= initBinary(divMode);
		boolBinary 			= initBoolBinary();
		intBinary 			= initIntBinary();
		aggUnary			= initAggUnary();
		aggBinary 			= initAggBinary();
		linAlgOps			= new DefaultLinAlgOperations<BigInteger, BigInteger[]>(
			this, new DefaultArrayOperations<BigInteger>(BigInteger[].class), BigIntegerGaussPivoting.LEN_L
		);
	}
	
	public Class<BigInteger> numberClass() {
		return BigInteger.class;
	}
	public Class<BigInteger[]> arrayClass() {
		return BigInteger[].class;
	}
	
	/**
	 * Returns the division mode for this big integer operators, defining how
	 * the method used to divide two integer values, which might result in a 
	 * remainder value.
	 */
	public DivisionMode getDivisionMode() {
		return divisionMode;
	}
	public LinAlgOperations<BigInteger, BigInteger[]> getLinAlgOperations() {
		return linAlgOps;
	}
	public LinAlgOperations<BigInteger, BigInteger[]> getLinAlgOperations(GaussPivotingFactory<BigInteger, BigInteger[]> gaussPivotingFactory) {
		return new DefaultLinAlgOperations<BigInteger, BigInteger[]>(getNumberArrayOperations(), gaussPivotingFactory);
	}
	public ArrayOperations<BigInteger[]> getArrayOperations() {
		return linAlgOps.getArrayOperations();
	}
	public NumberArrayOperations<BigInteger, BigInteger[]> getNumberArrayOperations() {
		return linAlgOps.getNumberArrayOperations();
	}
	/**
	 * Returns the conversion mode for this big integer operators, defining 
	 * how numeric values are converted into big integer values.
	 */
	public ConversionMode getConversionMode() {
		return conversionMode;
	}

	public DivisionSupport getDivisionSupport() {
		return divisionMode;
	}
	
	public BigInteger zero() {
		return BigInteger.ZERO;
	}
	public BigInteger one() {
		return BigInteger.ONE;
	}
	
	public NullaryOperator<BigInteger, BigInteger[]> constant(BigInteger value) {
		if (value.equals(BigInteger.ZERO)) {
			return nullary(NullaryOperator.Id.zero);
		}
		else if (value.equals(BigInteger.ONE)) {
			return nullary(NullaryOperator.Id.one);
		}
		return new BigIntegerConstOperator(value);
	}
	public ConvertingUnaryOperator<Number, Number[], BigInteger, BigInteger[]> converter() {
		return converter;
	}
	public NullaryOperator<BigInteger, BigInteger[]> nullary(NullaryOperator.Id id) {
		return nullary[id.ordinal()];
	}
	public UnaryOperator<BigInteger, BigInteger[]> unary(UnaryOperator.Id id) {
		return unary[id.ordinal()];
	}

	public BooleanUnaryOperator<BigInteger, BigInteger[]> booleanUnary(BooleanUnaryOperator.Id id) {
		return boolUnary[id.ordinal()];
	}

	public IntUnaryOperator<BigInteger, BigInteger[]> intUnary(IntUnaryOperator.Id id) {
		return intUnary[id.ordinal()];
	}

	public BinaryOperator<BigInteger, BigInteger[]> binary(BinaryOperator.Id id) {
		return binary[id.ordinal()];
	}

	public BooleanBinaryOperator<BigInteger, BigInteger[]> booleanBinary(BooleanBinaryOperator.Id id) {
		return boolBinary[id.ordinal()];
	}

	public IntBinaryOperator<BigInteger, BigInteger[]> intBinary(IntBinaryOperator.Id id) {
		return intBinary[id.ordinal()];
	}
	
	public AggregatingUnaryOperator<BigInteger, BigInteger[]> aggregatingUnary(AggregatingUnaryOperator.Id id) {
		return aggUnary[id.ordinal()];
	}
	
	public AggregatingBinaryOperator<BigInteger, BigInteger[]> aggregatingBinary(Id id) {
		return aggBinary[id.ordinal()];
	}
	
	@SuppressWarnings("unchecked")
	private static NullaryOperator<BigInteger, BigInteger[]>[] initNullary() {
		final NullaryOperator<BigInteger, BigInteger[]>[] ops = new NullaryOperator[NullaryOperator.Id.values().length];
		ops[NullaryOperator.Id.zero.ordinal()] = new BigIntegerConstOperator(BigInteger.ZERO);
		ops[NullaryOperator.Id.one.ordinal()] = new BigIntegerConstOperator(BigInteger.ONE);
		ops[NullaryOperator.Id.random.ordinal()] = new AbstractNullaryOperator<BigInteger, BigInteger[]>() {
			private final Random rnd = new Random();
			public BigInteger operate() {
				return BigInteger.valueOf(rnd.nextLong());
			}
			public void operate(BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = BigInteger.valueOf(rnd.nextLong());
			}
		};
		return checkComplete(ops, NullaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static UnaryOperator<BigInteger, BigInteger[]>[] initUnary(final DivisionMode divMode) {
		final UnaryOperator<BigInteger, BigInteger[]>[] ops = new UnaryOperator[UnaryOperator.Id.values().length];
		ops[UnaryOperator.Id.identity.ordinal()] = new AbstractUnaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand) {
				return operand;
			}
			public BigInteger operate(BigInteger[] src, int srcIndex) {
				return src[srcIndex];
			}
			public void operate(BigInteger[] src, int srcIndex, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex];
			}
		};
		ops[UnaryOperator.Id.normalize.ordinal()] = ops[UnaryOperator.Id.identity.ordinal()];
		ops[UnaryOperator.Id.abs.ordinal()] = new AbstractUnaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand) {
				return operand.abs();
			}
			public BigInteger operate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].abs();
			}
			public void operate(BigInteger[] src, int srcIndex, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].abs();
			}
		};
		ops[UnaryOperator.Id.negate.ordinal()] = new AbstractUnaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand) {
				return operand.negate();
			}
			public BigInteger operate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].negate();
			}
			public void operate(BigInteger[] src, int srcIndex, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].negate();
			}
		};
		ops[UnaryOperator.Id.invert.ordinal()] = new AbstractUnaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand) {
				return divMode.divide(BigInteger.ONE, operand);
			}
			public BigInteger operate(BigInteger[] src, int srcIndex) {
				return divMode.divide(BigInteger.ONE, src[srcIndex]);
			}
			public void operate(BigInteger[] src, int srcIndex, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = divMode.divide(BigInteger.ONE, src[srcIndex]);
			}
		};
		ops[UnaryOperator.Id.square.ordinal()] = new AbstractUnaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand) {
				return operand.pow(2);
			}
			public BigInteger operate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].pow(2);
			}
			public void operate(BigInteger[] src, int srcIndex, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].pow(2);
			}
		};
		return checkComplete(ops, UnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntUnaryOperator<BigInteger, BigInteger[]>[] initIntUnary() {
		final IntUnaryOperator<BigInteger, BigInteger[]>[] ops = new IntUnaryOperator[IntUnaryOperator.Id.values().length];
		ops[IntUnaryOperator.Id.signum.ordinal()] = new AbstractIntUnaryOperator<BigInteger, BigInteger[]>() {
			public int intOperate(BigInteger operand) {
				return operand.signum();
			}
			public int intOperate(BigInteger[] operand, int index) {
				return operand[index].signum();
			}
			public void operate(BigInteger[] src, int srcIndex, int[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum();
			}
		};
		return checkComplete(ops, IntUnaryOperator.Id.values());
	}
	private ConvertingUnaryOperator<Number, Number[], BigInteger, BigInteger[]> initConverter(final ConversionMode convMode) {
		return new ConvertingUnaryOperator<Number, Number[], BigInteger, BigInteger[]>() {
			public BigInteger operate(Number operand) {
				return convMode.convert(operand);
			}
			public void operate(Number[] src, int srcIndex, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = convMode.convert(src[srcIndex]);
			}
		};
	}	
	@SuppressWarnings("unchecked")
	private static BooleanUnaryOperator<BigInteger, BigInteger[]>[] initBoolUnary() {
		final BooleanUnaryOperator<BigInteger, BigInteger[]>[] ops = new BooleanUnaryOperator[BooleanUnaryOperator.Id.values().length];
		ops[BooleanUnaryOperator.Id.isZero.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return BigInteger.ZERO.equals(operand);
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return BigInteger.ZERO.equals(src[srcIndex]);
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = BigInteger.ZERO.equals(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isNonZero.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return !BigInteger.ZERO.equals(operand);
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return !BigInteger.ZERO.equals(src[srcIndex]);
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !BigInteger.ZERO.equals(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isOne.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return BigInteger.ONE.equals(operand);
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return BigInteger.ONE.equals(src[srcIndex]);
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = BigInteger.ONE.equals(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isPositive.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return operand.signum() > 0;
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].signum() > 0;
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() > 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNegative.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return operand.signum() < 0;
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].signum() < 0;
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() < 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonPositive.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return operand.signum() <= 0;
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].signum() <= 0;
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() <= 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonNegative.ordinal()] = new AbstractBooleanUnaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand) {
				return operand.signum() >= 0;
			}
			public boolean booleanOperate(BigInteger[] src, int srcIndex) {
				return src[srcIndex].signum() >= 0;
			}
			public void operate(BigInteger[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() >= 0;
			}
		};
		return checkComplete(ops, BooleanUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BinaryOperator<BigInteger, BigInteger[]>[] initBinary(final DivisionMode divMode) {
		final BinaryOperator<BigInteger, BigInteger[]>[] ops = new BinaryOperator[BinaryOperator.Id.values().length];
		ops[BinaryOperator.Id.add.ordinal()] = new AbstractBinaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand1, BigInteger operand2) {
				return operand1.add(operand2);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].add(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.subtract.ordinal()] = new AbstractBinaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand1, BigInteger operand2) {
				return operand1.subtract(operand2);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].subtract(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.multiply.ordinal()] = new AbstractBinaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand1, BigInteger operand2) {
				return operand1.multiply(operand2);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].multiply(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.divide.ordinal()] = new AbstractBinaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand1, BigInteger operand2) {
				return divMode.divide(operand1, operand2);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = divMode.divide(operand1[index1], operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.min.ordinal()] = new AbstractBinaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand1, BigInteger operand2) {
				return operand1.min(operand2);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].min(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.max.ordinal()] = new AbstractBinaryOperator<BigInteger, BigInteger[]>() {
			public BigInteger operate(BigInteger operand1, BigInteger operand2) {
				return operand1.max(operand2);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, BigInteger[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].max(operand2[index2]);
			}
		};
		return checkComplete(ops, BinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanBinaryOperator<BigInteger, BigInteger[]>[] initBoolBinary() {
		final BooleanBinaryOperator<BigInteger, BigInteger[]>[] ops = new BooleanBinaryOperator[BooleanBinaryOperator.Id.values().length];
		ops[BooleanBinaryOperator.Id.less.ordinal()] = new AbstractBooleanBinaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand1, BigInteger operand2) {
				return operand1.compareTo(operand2) < 0;
			}
			public boolean booleanOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) < 0;
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) < 0;
			}
		};
		ops[BooleanBinaryOperator.Id.lessOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand1, BigInteger operand2) {
				return operand1.compareTo(operand2) <= 0;
			}
			public boolean booleanOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) <= 0;
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) <= 0;
			}
		};
		ops[BooleanBinaryOperator.Id.equal.ordinal()] = new AbstractBooleanBinaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand1, BigInteger operand2) {
				return operand1.equals(operand2);
			}
			public boolean booleanOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return operand1[index1].equals(operand2[index2]);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].equals(operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.unequal.ordinal()] = new AbstractBooleanBinaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand1, BigInteger operand2) {
				return !operand1.equals(operand2);
			}
			public boolean booleanOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return !operand1[index1].equals(operand2[index2]);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !operand1[index1].equals(operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.greaterOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand1, BigInteger operand2) {
				return operand1.compareTo(operand2) >= 0;
			}
			public boolean booleanOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) >= 0;
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) >= 0;
			}
		};
		ops[BooleanBinaryOperator.Id.greater.ordinal()] = new AbstractBooleanBinaryOperator<BigInteger, BigInteger[]>() {
			public boolean booleanOperate(BigInteger operand1, BigInteger operand2) {
				return operand1.compareTo(operand2) > 0;
			}
			public boolean booleanOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) > 0;
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) > 0;
			}
		};
		return checkComplete(ops, BooleanBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntBinaryOperator<BigInteger, BigInteger[]>[] initIntBinary() {
		final IntBinaryOperator<BigInteger, BigInteger[]>[] ops = new IntBinaryOperator[IntBinaryOperator.Id.values().length];
		ops[IntBinaryOperator.Id.compare.ordinal()] = new AbstractIntBinaryOperator<BigInteger, BigInteger[]>() {
			public int intOperate(BigInteger operand1, BigInteger operand2) {
				return operand1.compareTo(operand2);
			}
			public int intOperate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]);
			}
			public void operate(BigInteger[] operand1, int index1, BigInteger[] operand2, int index2, int[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]);
			}
		};
		return checkComplete(ops, IntBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingUnaryOperator<BigInteger, BigInteger[]>[] initAggUnary() {
		final AggregatingUnaryOperator<BigInteger, BigInteger[]>[] ops = new AggregatingUnaryOperator[AggregatingUnaryOperator.Id.values().length];
		ops[AggregatingUnaryOperator.Id.min.ordinal()] = new BigIntegerAggregatingUnaryOperator() {
			public BigInteger operate(BigInteger[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return null;
				}
				BigInteger min = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					min = min.min(src[srcIndexFrom + i]);
				}
				return min;
			}
			public BigInteger operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return null;
				}
				BigInteger min = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						min = min.min(src[srcRowFrom + r][srcColFrom + c]);						
					}
				}
				return min;
			}
		};
		ops[AggregatingUnaryOperator.Id.max.ordinal()] = new BigIntegerAggregatingUnaryOperator() {
			public BigInteger operate(BigInteger[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return null;
				}
				BigInteger max = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					max = max.max(src[srcIndexFrom + i]);
				}
				return max;
			}
			public BigInteger operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return null;
				}
				BigInteger max = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						max = max.max(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return max;
			}
		};
		ops[AggregatingUnaryOperator.Id.sum.ordinal()] = new BigIntegerAggregatingUnaryOperator() {
			public BigInteger operate(BigInteger[] src, int srcIndexFrom, int length) {
				BigInteger sum = BigInteger.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src[srcIndexFrom + i]);
				}
				return sum;
			}
			public BigInteger operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				BigInteger sum = BigInteger.ZERO;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.add(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.prod.ordinal()] = new BigIntegerAggregatingUnaryOperator() {
			public BigInteger operate(BigInteger[] src, int srcIndexFrom, int length) {
				BigInteger sum = BigInteger.ONE;
				for (int i = 0; i < length; i++) {
					sum = sum.multiply(src[srcIndexFrom + i]);
				}
				return sum;
			}
			public BigInteger operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				BigInteger sum = BigInteger.ONE;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.multiply(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.sumSquared.ordinal()] = new BigIntegerAggregatingUnaryOperator() {
			public BigInteger operate(BigInteger[] src, int srcIndexFrom, int length) {
				BigInteger sum = BigInteger.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src[srcIndexFrom + i].pow(2));
				}
				return sum;
			}
			public BigInteger operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				BigInteger sum = BigInteger.ZERO;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.add(src[srcRowFrom + r][srcColFrom + c].pow(2));
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.normDivisor.ordinal()] = new BigIntegerAggregatingUnaryOperator() {
			public BigInteger operate(BigInteger[] src, int srcIndexFrom, int length) {
				if (length == 0) return BigInteger.ZERO;
				BigInteger gcd = src[srcIndexFrom].abs();
				for (int i = 1; i < length; i++) {
					final BigInteger val = src[srcIndexFrom + i];
					if (gcd.equals(BigInteger.ONE)) {
						break;
					}
					gcd = gcd.gcd(val);
				}				
				return gcd.signum() == 0 ? BigInteger.ZERO : gcd;
			}
			public BigInteger operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) return BigInteger.ZERO;
				BigInteger gcd = src[srcRowFrom][srcColFrom].abs();
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						final BigInteger val = src[srcRowFrom + r][srcColFrom + c];
						if (gcd.equals(BigInteger.ONE)) {
							break;
						}
						gcd = gcd.gcd(val);
					}
				}
				return gcd.signum() == 0 ? BigInteger.ZERO : gcd;
			}
		};
		ops[AggregatingUnaryOperator.Id.squeezeDivisor.ordinal()] = ops[AggregatingUnaryOperator.Id.normDivisor.ordinal()];
		return checkComplete(ops, AggregatingUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingBinaryOperator<BigInteger, BigInteger[]>[] initAggBinary() {
		final AggregatingBinaryOperator<BigInteger, BigInteger[]>[] ops = new AggregatingBinaryOperator[AggregatingBinaryOperator.Id.values().length];
		ops[AggregatingBinaryOperator.Id.innerProduct.ordinal()] = new BigIntegerAggregatingBinaryOperator() {
			public BigInteger operate(BigInteger[] src1, int src1IndexFrom, BigInteger[] src2, int src2IndexFrom, int length) {
				BigInteger sum = BigInteger.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1IndexFrom + i].multiply(src2[src2IndexFrom + i]));
				}
				return sum;
			}
			public BigInteger operate(BigInteger[][] src1, int src1RowFrom, int src1Col, BigInteger[] src2, int src2IndexFrom, int length) {
				BigInteger sum = BigInteger.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1RowFrom + i][src1Col].multiply(src2[src2IndexFrom + i]));
				}
				return sum;
			}
			public BigInteger operate(BigInteger[][] src1, int src1RowFrom, int src1Col, BigInteger[][] src2, int src2RowFrom, int src2Col, int length) {
				BigInteger sum = BigInteger.ZERO;
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
	
	private static class BigIntegerConstOperator extends AbstractNullaryOperator<BigInteger, BigInteger[]> {
		private final BigInteger value;
		public BigIntegerConstOperator(BigInteger value) {
			this.value = value;
		}
		public BigInteger operate() {
			return value;
		}
		public void operate(BigInteger[] dst, int dstIndex) {
			dst[dstIndex] = value;
		}
	}
	private abstract static class BigIntegerAggregatingUnaryOperator implements AggregatingUnaryOperator<BigInteger, BigInteger[]> {
		public void operate(BigInteger[] src, int srcIndexFrom, int srcIndexTo, BigInteger[] dst, int dstIndex) {
			dst[dstIndex] = operate(src, srcIndexFrom, srcIndexTo);
		}
		public void operate(BigInteger[][] src, int srcRowFrom, int srcColFrom, int rows, int cols, BigInteger[] dst, int dstIndex) {
			dst[dstIndex] = operate(src, srcRowFrom, srcColFrom, rows, cols);
		}
	}
	private abstract static class BigIntegerAggregatingBinaryOperator implements AggregatingBinaryOperator<BigInteger, BigInteger[]> {
		public void operate(BigInteger[] src1, int src1IndexFrom, BigInteger[] src2, int src2IndexFrom, BigInteger[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1IndexFrom, src2, src2IndexFrom, length);
		}
		public void operate(BigInteger[][] src1, int src1RowFrom, int src1Col, BigInteger[] src2, int src2IndexFrom, BigInteger[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1RowFrom, src1Col, src2, src2IndexFrom, length);
		}
		public void operate(BigInteger[][] src1, int src1RowFrom, int src1Col, BigInteger[][] src2, int src2RowFrom, int src2Col, BigInteger[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1RowFrom, src1Col, src2, src2RowFrom, src2Col, length);
		}
	}

}
