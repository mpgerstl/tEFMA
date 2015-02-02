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
package ch.javasoft.metabolic.efm.config;

import java.math.BigInteger;
import java.util.Arrays;

import org.dom4j.Attribute;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.metabolic.efm.column.BigIntegerColumn;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.DoubleColumn;
import ch.javasoft.metabolic.efm.column.FractionalColumn;
import ch.javasoft.metabolic.efm.column.RawBigIntegerColumn;
import ch.javasoft.metabolic.efm.column.VarIntColumn;
import ch.javasoft.util.numeric.Zero;
import ch.javasoft.xml.config.XmlConfigException;

public enum Arithmetic {
	double_ {
		@Override
		public ColumnHome<Double, DoubleColumn> getColumnHome() {
			return DoubleColumn.HOME;
		}
		@Override
		public Zero getDefaultZero() {
			return new Zero();
		}
		@Override
		public boolean isExact() {
			return false;
		}
	}, fractional {
		@Override
		public ColumnHome<BigFraction, FractionalColumn> getColumnHome() {
			return FractionalColumn.HOME;
		}		
		@Override
		public Zero getDefaultZero() {
			return new Zero(0d);
		}
		@Override
		public boolean isExact() {
			return true;
		}
	}, bigint {
		@Override
		public ColumnHome<BigInteger, BigIntegerColumn> getColumnHome() {
			return BigIntegerColumn.HOME;
		}		
		@Override
		public Zero getDefaultZero() {
			return new Zero(0d);
		}
		@Override
		public boolean isExact() {
			return true;
		}
	}, rawint {
		@Override
		public ColumnHome<BigInteger, RawBigIntegerColumn> getColumnHome() {
			return RawBigIntegerColumn.HOME;
		}		
		@Override
		public Zero getDefaultZero() {
			return new Zero(0d);
		}
		@Override
		public boolean isExact() {
			return true;
		}
	}, varint {
		@Override
		public ColumnHome<VarIntNumber, VarIntColumn> getColumnHome() {
			return VarIntColumn.HOME;
		}		
		@Override
		public Zero getDefaultZero() {
			return new Zero(0d);
		}
		@Override
		public boolean isExact() {
			return true;
		}
	};
	
	public static Arithmetic parse(String name) throws IllegalArgumentException {
		try {
			return valueOf(name.toLowerCase());
		}
		catch (IllegalArgumentException ex) {
			try {
				return valueOf(name.toLowerCase() + "_");
			}
			catch (IllegalArgumentException ex2) {
				throw new IllegalArgumentException(
					"invalid value for arithmetic, expected " + 
					Arrays.toString(values()) + ", but found: " + name);
			}
		}
	}
	public static Arithmetic parse(Attribute attribute) throws XmlConfigException {
		try {
			return parse(attribute.getValue());
		}
		catch (IllegalArgumentException ex) {
			throw new XmlConfigException(ex.getLocalizedMessage(), attribute);
		}
	}
	public String getNiceName() {
		final String name = name();
		return name.endsWith("_") ? name.substring(0, name.length()-1) : name;
	}
	@Override
	public String toString() {
		return getNiceName();
	}
	public NumberOperations<?> getNumberOperations() {
		return getColumnHome().getNumberOperations();
	}
	/**
	 * Returns true if this arithmetic is exact, and false if truncation or
	 * rounding might occur due to arithmetic operations.
	 */
	abstract public boolean isExact();
	abstract public ColumnHome<?, ?> getColumnHome();
	abstract public Zero getDefaultZero();
}
