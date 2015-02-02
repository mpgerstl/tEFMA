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
package ch.javasoft.metabolic.efm.progress;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * The <code>AbstractStringProgressWriter</code> encodes progress as string
 * message (stating the percentage progress), subclasses simply implement 
 * outputting of the string message.
 */
abstract public class AbstractStringProgressWriter implements ProgressMonitor {
	
	/**
	 * The mode constants represent different ways of defining the output 
	 * precision when the percentage number is converted to a string.
	 */
	public static enum Mode {
		/**
		 * Indicates that the corresponding value is a percentage decimal
		 * figure, i.e. the number of decimal places after comma of a percentage
		 * figure, e.g.<br/>
		 * 100.12% has value 2 since it has 2 decimal places, and the smallest
		 * increment needed is 14 since 2^-14 is smaller than 1^-2
		 */
		Decimal {
			@Override
			int getDecimalPrecision(int value) {
				return value;
			}
			@Override
			int getSmallestIncrement(int value) {
				//10^(-value - 2) = 2^-x
				//x = (value+2)*log2(10)
				return (int)(Math.ceil((value + 2) * Math.log(10) / Math.log(2)));
			}
		}, 
		/**
		 * Indicates that the corresponding value denotes the partitioning of 
		 * the whole. For instance, partitioning into 1024 pieces means that the
		 * smallest increment is 10 (since 2^-10 is 1024). This corresponds to
		 * a maximum percentage decimal precision of 1 since 1/1024 = 10^-1%.
		 */
		Partition {
			@Override
			int getDecimalPrecision(int value) {
				//10^(-x-2) = 1/value
				// x = ceil [log10(value) - 2]
				return (int)Math.floor(Math.log10(value) - 2);
			}
			@Override
			int getSmallestIncrement(int value) {
				//2^-x = 1/value
				//x = ceil(log2(value))
				return 32 - Integer.numberOfLeadingZeros(value - 1);
			}
		};
		abstract int getDecimalPrecision(int value);
		abstract int getSmallestIncrement(int value);
	}
	
	private final int decimalPrecision;
	private final int smallestIncrement;
	private final NumberFormat format;
	
	/**
	 * Constructor with mode and precision, interpreted as defined by the 
	 * {@link Mode mode} constant.
	 */
	public AbstractStringProgressWriter(Mode mode, int n) {
		if (mode.getDecimalPrecision(n) < 0) {
			throw new IllegalArgumentException("decimal precision cannot be negative: " + mode.getDecimalPrecision(n));
		}
		this.decimalPrecision	= mode.getDecimalPrecision(n);
		// 10^-decimalPrecision == 2^-smallestIncrement
		this.smallestIncrement 	= mode.getSmallestIncrement(n);
		this.format 			= getFormat(decimalPrecision);
	}
	private static NumberFormat getFormat(int decimalPrecision) {
		final StringBuilder sb = new StringBuilder("##0");
		if (decimalPrecision > 0) sb.append('.');
		while (decimalPrecision > 0) sb.append('0');
		sb.append('%');
		return new DecimalFormat(sb.toString());
	}
	/**
	 * Returns the decimal places after the comma, as defined when constructing
	 * this writer. Depending on the mode used, this value is derived, which
	 * might involve rounding.
	 */
	public final int getDecimalPrecision() {
		return decimalPrecision;
	}
	public final int getSmallestIncrement() {
		return smallestIncrement;
	}
	public void notifyProgress(double progress) throws IOException {
		write(progress, format.format(progress) + "\n");
	}	
	abstract protected void write(double progress, String msg) throws IOException;
	
//	public static void main(String[] args) {
//		System.out.println(Mode.Decimal.getDecimalPrecision(2));
//		System.out.println(Mode.Decimal.getSmallestIncrement(2));
//		System.out.println(Mode.Partition.getDecimalPrecision(1024));
//		System.out.println(Mode.Partition.getSmallestIncrement(1024));
//		System.out.println(Mode.Decimal.getDecimalPrecision(Mode.Partition.getDecimalPrecision(1024)));
//		System.out.println(Mode.Decimal.getSmallestIncrement(Mode.Partition.getDecimalPrecision(1024)));
//	}
	
}
