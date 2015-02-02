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
package ch.javasoft.math.varint.array;

import java.math.BigDecimal;
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
import ch.javasoft.math.varint.VarIntFactory;
import ch.javasoft.math.varint.VarIntNumber;

/**
 * Number operators for {@link VarIntNumber} numbers. A 
 * {@link #INTEGER_DIVISION_INSTANCE default instance} is available as constant, 
 * where divisions are performed as integer division, ignoring possible 
 * remainders. Alternate instances are available, e.g. if division should throw 
 * an exception.  
 */
public class VarIntOperators implements NumberOperators<VarIntNumber, VarIntNumber[]> {
	
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
			public VarIntNumber divide(VarIntNumber dividend, VarIntNumber divisor) {
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
			public VarIntNumber divide(VarIntNumber dividend, VarIntNumber divisor) {
				return dividend.divide(divisor, RoundingMode.UNNECESSARY);
			} 
		},
		/**
		 * Integer division is used truncating remainders if necessary. Division
		 * and inversion operations never throw an exception, unless the divisor
		 * is zero.
		 */
		Integer {
			@Override
			public VarIntNumber divide(VarIntNumber dividend, VarIntNumber divisor) {
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
			public VarIntNumber divide(VarIntNumber dividend, VarIntNumber divisor) {
				return dividend.divide(divisor, RoundingMode.HALF_EVEN);
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
		abstract public VarIntNumber divide(VarIntNumber dividend, VarIntNumber divisor);
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
			public VarIntNumber convert(Number value) {
				if (value instanceof VarIntNumber) {
					return (VarIntNumber)value;
				}
				if (value instanceof BigDecimal) {
					return VarIntFactory.create(((BigDecimal)value).toBigIntegerExact());
				}
				if (value instanceof BigFraction) {
					return VarIntFactory.create(((BigFraction)value).toBigIntegerExact());
				}
				if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
					return VarIntFactory.create(value.longValue());
				}
				if (value instanceof Double || value instanceof Float) {
					return VarIntFactory.create(BigDecimal.valueOf(value.doubleValue()).toBigIntegerExact());
				}
				return VarIntFactory.create(value.toString());
			}
		},
		/**
		 * Conversion is performed using truncation if necessary. For integer
		 * fractions, integer division is used. For floating point numbers, the
		 * decimal places are simply omitted.
		 */
		Truncate {
			@Override
			public VarIntNumber convert(Number value) {
				if (value instanceof VarIntNumber) {
					return (VarIntNumber)value;
				}
				if (value instanceof BigDecimal) {
					return VarIntFactory.create(((BigDecimal)value).toBigInteger());
				}
				if (value instanceof BigFraction) {
					return VarIntFactory.create(((BigFraction)value).toBigInteger());
				}
				if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
					return VarIntFactory.create(value.longValue());
				}
				if (value instanceof Double || value instanceof Float) {
					return VarIntFactory.create(BigDecimal.valueOf(value.doubleValue()).toBigInteger());
				}
				return VarIntFactory.create(value.toString());
			}
		},
		/**
		 * Conversion is performed using rounding if necessary. The closest 
		 * integer is used as defined by the
		 * {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding mode.
		 */
		Round {
			public VarIntNumber convertBigDecimal(BigDecimal value) {
				return VarIntFactory.create(value.divide(BigDecimal.ONE, 0, RoundingMode.HALF_EVEN).toBigIntegerExact());
			}
			@Override
			public VarIntNumber convert(Number value) {
				if (value instanceof VarIntNumber) {
					return (VarIntNumber)value;
				}
				if (value instanceof BigDecimal) {
					return convertBigDecimal((BigDecimal)value);
				}
				if (value instanceof BigFraction) {
					return VarIntFactory.create(((BigFraction)value).toBigInteger(RoundingMode.HALF_EVEN));
				}
				if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof AtomicInteger || value instanceof AtomicLong) {
					return VarIntFactory.create(value.longValue());
				}
				if (value instanceof Double || value instanceof Float) {
					return convertBigDecimal(BigDecimal.valueOf(value.doubleValue()));
				}
				return VarIntFactory.create(value.toString());
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
		public abstract VarIntNumber convert(Number value);
	}
	
	/**
	 * The default instance, all operators perform the plain operations without
	 * rounding. Division is performed as integer division, possible remainders
	 * are ignored. This implementation uses {@link DivisionMode#Integer} and
	 * {@link ConversionMode#Truncate}.
	 */
	public static final VarIntOperators INTEGER_DIVISION_INSTANCE = new VarIntOperators(DivisionMode.Integer, ConversionMode.Truncate);
	/**
	 * The instance without division, that is, all division operations throw
	 * an {@link ArithmeticException}. All conversion operations from another
	 * number type also throw an {@link ArithmeticException} if the number did
	 * not represent an integer value. This implementation uses 
	 * {@link DivisionMode#None} and {@link ConversionMode#Exact}.
	 */
	public static final VarIntOperators NO_DIVISION_INSTANCE = new VarIntOperators(DivisionMode.None, ConversionMode.Exact);
	/**
	 * The instance with exact integer division, that is, division operations 
	 * throw an {@link ArithmeticException} if it does not yield an integer 
	 * value. All conversion operations from another number type also throw an 
	 * {@link ArithmeticException} if the number did not represent an integer 
	 * value. This implementation uses {@link DivisionMode#Exact} and 
	 * {@link ConversionMode#Exact}.
	 */
	public static final VarIntOperators EXACT_DIVISION_INSTANCE = new VarIntOperators(DivisionMode.Exact, ConversionMode.Exact);
	/**
	 * The instance with rounded integer division, that is, division operations
	 * use {@link RoundingMode#HALF_EVEN HALF_EVEN} rounding. This 
	 * implementation uses {@link DivisionMode#Round} and
	 * {@link ConversionMode#Round}.
	 */
	public static final VarIntOperators ROUNDED_DIVISION_INSTANCE = new VarIntOperators(DivisionMode.Round, ConversionMode.Round);
	
	private final LinAlgOperations<VarIntNumber, VarIntNumber[]> 			linAlgOps;
	
	private final DivisionMode											divisionMode;
	private final ConversionMode										conversionMode;
	private final NullaryOperator<VarIntNumber, VarIntNumber[]>[] 			nullary;
	private final UnaryOperator<VarIntNumber, VarIntNumber[]>[] 			unary;
	private final BooleanUnaryOperator<VarIntNumber, VarIntNumber[]>[] 		boolUnary;
	private final IntUnaryOperator<VarIntNumber, VarIntNumber[]>[] 			intUnary;
	private final ConvertingUnaryOperator<Number, Number[], VarIntNumber, VarIntNumber[]> converter;
	private final BinaryOperator<VarIntNumber, VarIntNumber[]>[] 			binary;
	private final BooleanBinaryOperator<VarIntNumber, VarIntNumber[]>[] 	boolBinary;
	private final IntBinaryOperator<VarIntNumber, VarIntNumber[]>[] 		intBinary;
	private final AggregatingUnaryOperator<VarIntNumber, VarIntNumber[]>[] 	aggUnary;
	private final AggregatingBinaryOperator<VarIntNumber, VarIntNumber[]>[]	aggBinary;


	/**
	 * Constructor for <code>VarIntNumberNumberOperators</code> with division and
	 * conversion mode. Note that constants are available for the most common 
	 * modes.
	 * 
	 * @param divMode	the division mode
	 * @param convMode	the conversion mode
	 */
	public VarIntOperators(DivisionMode divMode, ConversionMode convMode) {
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
		linAlgOps			= new DefaultLinAlgOperations<VarIntNumber, VarIntNumber[]>(
			this, new DefaultArrayOperations<VarIntNumber>(VarIntNumber[].class), VarIntGaussPivoting.LEN_L
		);
	}
	
	public Class<VarIntNumber> numberClass() {
		return VarIntNumber.class;
	}
	public Class<VarIntNumber[]> arrayClass() {
		return VarIntNumber[].class;
	}
	
	/**
	 * Returns the division mode for this big integer operators, defining how
	 * the method used to divide two integer values, which might result in a 
	 * remainder value.
	 */
	public DivisionMode getDivisionMode() {
		return divisionMode;
	}
	public LinAlgOperations<VarIntNumber, VarIntNumber[]> getLinAlgOperations() {
		return linAlgOps;
	}
	public LinAlgOperations<VarIntNumber, VarIntNumber[]> getLinAlgOperations(GaussPivotingFactory<VarIntNumber, VarIntNumber[]> gaussPivotingFactory) {
		return new DefaultLinAlgOperations<VarIntNumber, VarIntNumber[]>(getNumberArrayOperations(), gaussPivotingFactory);
	}
	public ArrayOperations<VarIntNumber[]> getArrayOperations() {
		return linAlgOps.getArrayOperations();
	}
	public NumberArrayOperations<VarIntNumber, VarIntNumber[]> getNumberArrayOperations() {
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
	
	public VarIntNumber zero() {
		return VarIntNumber.ZERO;
	}
	public VarIntNumber one() {
		return VarIntNumber.ONE;
	}
	
	public NullaryOperator<VarIntNumber, VarIntNumber[]> constant(VarIntNumber value) {
		if (value.equals(VarIntNumber.ZERO)) {
			return nullary(NullaryOperator.Id.zero);
		}
		else if (value.equals(VarIntNumber.ONE)) {
			return nullary(NullaryOperator.Id.one);
		}
		return new VarIntNumberConstOperator(value);
	}
	public ConvertingUnaryOperator<Number, Number[], VarIntNumber, VarIntNumber[]> converter() {
		return converter;
	}
	public NullaryOperator<VarIntNumber, VarIntNumber[]> nullary(NullaryOperator.Id id) {
		return nullary[id.ordinal()];
	}
	public UnaryOperator<VarIntNumber, VarIntNumber[]> unary(UnaryOperator.Id id) {
		return unary[id.ordinal()];
	}

	public BooleanUnaryOperator<VarIntNumber, VarIntNumber[]> booleanUnary(BooleanUnaryOperator.Id id) {
		return boolUnary[id.ordinal()];
	}

	public IntUnaryOperator<VarIntNumber, VarIntNumber[]> intUnary(IntUnaryOperator.Id id) {
		return intUnary[id.ordinal()];
	}

	public BinaryOperator<VarIntNumber, VarIntNumber[]> binary(BinaryOperator.Id id) {
		return binary[id.ordinal()];
	}

	public BooleanBinaryOperator<VarIntNumber, VarIntNumber[]> booleanBinary(BooleanBinaryOperator.Id id) {
		return boolBinary[id.ordinal()];
	}

	public IntBinaryOperator<VarIntNumber, VarIntNumber[]> intBinary(IntBinaryOperator.Id id) {
		return intBinary[id.ordinal()];
	}
	
	public AggregatingUnaryOperator<VarIntNumber, VarIntNumber[]> aggregatingUnary(AggregatingUnaryOperator.Id id) {
		return aggUnary[id.ordinal()];
	}
	
	public AggregatingBinaryOperator<VarIntNumber, VarIntNumber[]> aggregatingBinary(Id id) {
		return aggBinary[id.ordinal()];
	}
	
	@SuppressWarnings("unchecked")
	private static NullaryOperator<VarIntNumber, VarIntNumber[]>[] initNullary() {
		final NullaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new NullaryOperator[NullaryOperator.Id.values().length];
		ops[NullaryOperator.Id.zero.ordinal()] = new VarIntNumberConstOperator(VarIntNumber.ZERO);
		ops[NullaryOperator.Id.one.ordinal()] = new VarIntNumberConstOperator(VarIntNumber.ONE);
		ops[NullaryOperator.Id.random.ordinal()] = new AbstractNullaryOperator<VarIntNumber, VarIntNumber[]>() {
			private final Random rnd = new Random();
			public VarIntNumber operate() {
				return VarIntFactory.create(rnd.nextLong());
			}
			public void operate(VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = VarIntFactory.create(rnd.nextLong());
			}
		};
		return checkComplete(ops, NullaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static UnaryOperator<VarIntNumber, VarIntNumber[]>[] initUnary(final DivisionMode divMode) {
		final UnaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new UnaryOperator[UnaryOperator.Id.values().length];
		ops[UnaryOperator.Id.identity.ordinal()] = new AbstractUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand) {
				return operand;
			}
			public VarIntNumber operate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex];
			}
			public void operate(VarIntNumber[] src, int srcIndex, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex];
			}
		};
		ops[UnaryOperator.Id.normalize.ordinal()] = ops[UnaryOperator.Id.identity.ordinal()];
		ops[UnaryOperator.Id.abs.ordinal()] = new AbstractUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand) {
				return operand.abs();
			}
			public VarIntNumber operate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].abs();
			}
			public void operate(VarIntNumber[] src, int srcIndex, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].abs();
			}
		};
		ops[UnaryOperator.Id.negate.ordinal()] = new AbstractUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand) {
				return operand.negate();
			}
			public VarIntNumber operate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].negate();
			}
			public void operate(VarIntNumber[] src, int srcIndex, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].negate();
			}
		};
		ops[UnaryOperator.Id.invert.ordinal()] = new AbstractUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand) {
				return divMode.divide(VarIntNumber.ONE, operand);
			}
			public VarIntNumber operate(VarIntNumber[] src, int srcIndex) {
				return divMode.divide(VarIntNumber.ONE, src[srcIndex]);
			}
			public void operate(VarIntNumber[] src, int srcIndex, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = divMode.divide(VarIntNumber.ONE, src[srcIndex]);
			}
		};
		ops[UnaryOperator.Id.square.ordinal()] = new AbstractUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand) {
				return operand.pow(2);
			}
			public VarIntNumber operate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].pow(2);
			}
			public void operate(VarIntNumber[] src, int srcIndex, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].pow(2);
			}
		};
		return checkComplete(ops, UnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntUnaryOperator<VarIntNumber, VarIntNumber[]>[] initIntUnary() {
		final IntUnaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new IntUnaryOperator[IntUnaryOperator.Id.values().length];
		ops[IntUnaryOperator.Id.signum.ordinal()] = new AbstractIntUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public int intOperate(VarIntNumber operand) {
				return operand.signum();
			}
			public int intOperate(VarIntNumber[] operand, int index) {
				return operand[index].signum();
			}
			public void operate(VarIntNumber[] src, int srcIndex, int[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum();
			}
		};
		return checkComplete(ops, IntUnaryOperator.Id.values());
	}
	private ConvertingUnaryOperator<Number, Number[], VarIntNumber, VarIntNumber[]> initConverter(final ConversionMode convMode) {
		return new ConvertingUnaryOperator<Number, Number[], VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(Number operand) {
				return convMode.convert(operand);
			}
			public void operate(Number[] src, int srcIndex, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = convMode.convert(src[srcIndex]);
			}
		};
	}	
	@SuppressWarnings("unchecked")
	private static BooleanUnaryOperator<VarIntNumber, VarIntNumber[]>[] initBoolUnary() {
		final BooleanUnaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new BooleanUnaryOperator[BooleanUnaryOperator.Id.values().length];
		ops[BooleanUnaryOperator.Id.isZero.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return VarIntNumber.ZERO.equals(operand);
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return VarIntNumber.ZERO.equals(src[srcIndex]);
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = VarIntNumber.ZERO.equals(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isNonZero.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return !VarIntNumber.ZERO.equals(operand);
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return !VarIntNumber.ZERO.equals(src[srcIndex]);
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !VarIntNumber.ZERO.equals(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isOne.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return VarIntNumber.ONE.equals(operand);
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return VarIntNumber.ONE.equals(src[srcIndex]);
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = VarIntNumber.ONE.equals(src[srcIndex]);
			}
		};
		ops[BooleanUnaryOperator.Id.isPositive.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return operand.signum() > 0;
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].signum() > 0;
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() > 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNegative.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return operand.signum() < 0;
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].signum() < 0;
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() < 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonPositive.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return operand.signum() <= 0;
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].signum() <= 0;
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() <= 0;
			}
		};
		ops[BooleanUnaryOperator.Id.isNonNegative.ordinal()] = new AbstractBooleanUnaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand) {
				return operand.signum() >= 0;
			}
			public boolean booleanOperate(VarIntNumber[] src, int srcIndex) {
				return src[srcIndex].signum() >= 0;
			}
			public void operate(VarIntNumber[] src, int srcIndex, boolean[] dst, int dstIndex) {
				dst[dstIndex] = src[srcIndex].signum() >= 0;
			}
		};
		return checkComplete(ops, BooleanUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BinaryOperator<VarIntNumber, VarIntNumber[]>[] initBinary(final DivisionMode divMode) {
		final BinaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new BinaryOperator[BinaryOperator.Id.values().length];
		ops[BinaryOperator.Id.add.ordinal()] = new AbstractBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.add(operand2);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].add(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.subtract.ordinal()] = new AbstractBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.subtract(operand2);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].subtract(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.multiply.ordinal()] = new AbstractBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.multiply(operand2);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].multiply(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.divide.ordinal()] = new AbstractBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand1, VarIntNumber operand2) {
				return divMode.divide(operand1, operand2);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = divMode.divide(operand1[index1], operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.min.ordinal()] = new AbstractBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.min(operand2);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].min(operand2[index2]);
			}
		};
		ops[BinaryOperator.Id.max.ordinal()] = new AbstractBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public VarIntNumber operate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.max(operand2);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, VarIntNumber[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].max(operand2[index2]);
			}
		};
		return checkComplete(ops, BinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static BooleanBinaryOperator<VarIntNumber, VarIntNumber[]>[] initBoolBinary() {
		final BooleanBinaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new BooleanBinaryOperator[BooleanBinaryOperator.Id.values().length];
		ops[BooleanBinaryOperator.Id.less.ordinal()] = new AbstractBooleanBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.compareTo(operand2) < 0;
			}
			public boolean booleanOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) < 0;
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) < 0;
			}
		};
		ops[BooleanBinaryOperator.Id.lessOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.compareTo(operand2) <= 0;
			}
			public boolean booleanOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) <= 0;
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) <= 0;
			}
		};
		ops[BooleanBinaryOperator.Id.equal.ordinal()] = new AbstractBooleanBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.equals(operand2);
			}
			public boolean booleanOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return operand1[index1].equals(operand2[index2]);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].equals(operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.unequal.ordinal()] = new AbstractBooleanBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return !operand1.equals(operand2);
			}
			public boolean booleanOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return !operand1[index1].equals(operand2[index2]);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = !operand1[index1].equals(operand2[index2]);
			}
		};
		ops[BooleanBinaryOperator.Id.greaterOrEqual.ordinal()] = new AbstractBooleanBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.compareTo(operand2) >= 0;
			}
			public boolean booleanOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) >= 0;
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) >= 0;
			}
		};
		ops[BooleanBinaryOperator.Id.greater.ordinal()] = new AbstractBooleanBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public boolean booleanOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.compareTo(operand2) > 0;
			}
			public boolean booleanOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]) > 0;
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, boolean[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]) > 0;
			}
		};
		return checkComplete(ops, BooleanBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static IntBinaryOperator<VarIntNumber, VarIntNumber[]>[] initIntBinary() {
		final IntBinaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new IntBinaryOperator[IntBinaryOperator.Id.values().length];
		ops[IntBinaryOperator.Id.compare.ordinal()] = new AbstractIntBinaryOperator<VarIntNumber, VarIntNumber[]>() {
			public int intOperate(VarIntNumber operand1, VarIntNumber operand2) {
				return operand1.compareTo(operand2);
			}
			public int intOperate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2) {
				return operand1[index1].compareTo(operand2[index2]);
			}
			public void operate(VarIntNumber[] operand1, int index1, VarIntNumber[] operand2, int index2, int[] dst, int dstIndex) {
				dst[dstIndex] = operand1[index1].compareTo(operand2[index2]);
			}
		};
		return checkComplete(ops, IntBinaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingUnaryOperator<VarIntNumber, VarIntNumber[]>[] initAggUnary() {
		final AggregatingUnaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new AggregatingUnaryOperator[AggregatingUnaryOperator.Id.values().length];
		ops[AggregatingUnaryOperator.Id.min.ordinal()] = new VarIntNumberAggregatingUnaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return null;
				}
				VarIntNumber min = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					min = min.min(src[srcIndexFrom + i]);
				}
				return min;
			}
			public VarIntNumber operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return null;
				}
				VarIntNumber min = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						min = min.min(src[srcRowFrom + r][srcColFrom + c]);						
					}
				}
				return min;
			}
		};
		ops[AggregatingUnaryOperator.Id.max.ordinal()] = new VarIntNumberAggregatingUnaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src, int srcIndexFrom, int length) {
				if (length == 0) {
					return null;
				}
				VarIntNumber max = src[srcIndexFrom];
				for (int i = 1; i < length; i++) {
					max = max.max(src[srcIndexFrom + i]);
				}
				return max;
			}
			public VarIntNumber operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) {
					return null;
				}
				VarIntNumber max = src[srcRowFrom][srcColFrom];
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						max = max.max(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return max;
			}
		};
		ops[AggregatingUnaryOperator.Id.sum.ordinal()] = new VarIntNumberAggregatingUnaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src, int srcIndexFrom, int length) {
				VarIntNumber sum = VarIntNumber.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src[srcIndexFrom + i]);
				}
				return sum;
			}
			public VarIntNumber operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				VarIntNumber sum = VarIntNumber.ZERO;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.add(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.prod.ordinal()] = new VarIntNumberAggregatingUnaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src, int srcIndexFrom, int length) {
				VarIntNumber sum = VarIntNumber.ONE;
				for (int i = 0; i < length; i++) {
					sum = sum.multiply(src[srcIndexFrom + i]);
				}
				return sum;
			}
			public VarIntNumber operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				VarIntNumber sum = VarIntNumber.ONE;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.multiply(src[srcRowFrom + r][srcColFrom + c]);
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.sumSquared.ordinal()] = new VarIntNumberAggregatingUnaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src, int srcIndexFrom, int length) {
				VarIntNumber sum = VarIntNumber.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src[srcIndexFrom + i].pow(2));
				}
				return sum;
			}
			public VarIntNumber operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				VarIntNumber sum = VarIntNumber.ZERO;
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						sum = sum.add(src[srcRowFrom + r][srcColFrom + c].pow(2));
					}
				}
				return sum;
			}
		};
		ops[AggregatingUnaryOperator.Id.normDivisor.ordinal()] = new VarIntNumberAggregatingUnaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src, int srcIndexFrom, int length) {
				if (length == 0) return VarIntNumber.ZERO;
				VarIntNumber gcd = src[srcIndexFrom].abs();
				for (int i = 1; i < length; i++) {
					final VarIntNumber val = src[srcIndexFrom + i];
					if (gcd.equals(VarIntNumber.ONE)) {
						break;
					}
					gcd = gcd.gcd(val);
				}				
				return gcd.signum() == 0 ? VarIntNumber.ZERO : gcd;
			}
			public VarIntNumber operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols) {
				if (rows == 0 || cols == 0) return VarIntNumber.ZERO;
				VarIntNumber gcd = src[srcRowFrom][srcColFrom].abs();
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						final VarIntNumber val = src[srcRowFrom + r][srcColFrom + c];
						if (gcd.equals(VarIntNumber.ONE)) {
							break;
						}
						gcd = gcd.gcd(val);
					}
				}
				return gcd.signum() == 0 ? VarIntNumber.ZERO : gcd;
			}
		};
		ops[AggregatingUnaryOperator.Id.squeezeDivisor.ordinal()] = ops[AggregatingUnaryOperator.Id.normDivisor.ordinal()];
		return checkComplete(ops, AggregatingUnaryOperator.Id.values());
	}
	@SuppressWarnings("unchecked")
	private static AggregatingBinaryOperator<VarIntNumber, VarIntNumber[]>[] initAggBinary() {
		final AggregatingBinaryOperator<VarIntNumber, VarIntNumber[]>[] ops = new AggregatingBinaryOperator[AggregatingBinaryOperator.Id.values().length];
		ops[AggregatingBinaryOperator.Id.innerProduct.ordinal()] = new VarIntNumberAggregatingBinaryOperator() {
			public VarIntNumber operate(VarIntNumber[] src1, int src1IndexFrom, VarIntNumber[] src2, int src2IndexFrom, int length) {
				VarIntNumber sum = VarIntNumber.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1IndexFrom + i].multiply(src2[src2IndexFrom + i]));
				}
				return sum;
			}
			public VarIntNumber operate(VarIntNumber[][] src1, int src1RowFrom, int src1Col, VarIntNumber[] src2, int src2IndexFrom, int length) {
				VarIntNumber sum = VarIntNumber.ZERO;
				for (int i = 0; i < length; i++) {
					sum = sum.add(src1[src1RowFrom + i][src1Col].multiply(src2[src2IndexFrom + i]));
				}
				return sum;
			}
			public VarIntNumber operate(VarIntNumber[][] src1, int src1RowFrom, int src1Col, VarIntNumber[][] src2, int src2RowFrom, int src2Col, int length) {
				VarIntNumber sum = VarIntNumber.ZERO;
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
	
	private static class VarIntNumberConstOperator extends AbstractNullaryOperator<VarIntNumber, VarIntNumber[]> {
		private final VarIntNumber value;
		public VarIntNumberConstOperator(VarIntNumber value) {
			this.value = value;
		}
		public VarIntNumber operate() {
			return value;
		}
		public void operate(VarIntNumber[] dst, int dstIndex) {
			dst[dstIndex] = value;
		}
	}
	private abstract static class VarIntNumberAggregatingUnaryOperator implements AggregatingUnaryOperator<VarIntNumber, VarIntNumber[]> {
		public void operate(VarIntNumber[] src, int srcIndexFrom, int srcIndexTo, VarIntNumber[] dst, int dstIndex) {
			dst[dstIndex] = operate(src, srcIndexFrom, srcIndexTo);
		}
		public void operate(VarIntNumber[][] src, int srcRowFrom, int srcColFrom, int rows, int cols, VarIntNumber[] dst, int dstIndex) {
			dst[dstIndex] = operate(src, srcRowFrom, srcColFrom, rows, cols);
		}
	}
	private abstract static class VarIntNumberAggregatingBinaryOperator implements AggregatingBinaryOperator<VarIntNumber, VarIntNumber[]> {
		public void operate(VarIntNumber[] src1, int src1IndexFrom, VarIntNumber[] src2, int src2IndexFrom, VarIntNumber[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1IndexFrom, src2, src2IndexFrom, length);
		}
		public void operate(VarIntNumber[][] src1, int src1RowFrom, int src1Col, VarIntNumber[] src2, int src2IndexFrom, VarIntNumber[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1RowFrom, src1Col, src2, src2IndexFrom, length);
		}
		public void operate(VarIntNumber[][] src1, int src1RowFrom, int src1Col, VarIntNumber[][] src2, int src2RowFrom, int src2Col, VarIntNumber[] dst, int dstIndex, int length) {
			dst[dstIndex] = operate(src1, src1RowFrom, src1Col, src2, src2RowFrom, src2Col, length);
		}
	}

}
