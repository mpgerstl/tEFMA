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
package ch.javasoft.jbase.marshal;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.jbase.FixedWidthMarshaller;
import ch.javasoft.jbase.FixedWidthTable;

/**
 * This class contains constant {@link FixedWidthMarshaller} instances for 
 * all Java primitive data types. Note that one row (entity) corresponds to
 * a single primitive value. Alternatively, {@link PrimitiveArrayMarshallers}
 * contains marshallers for arrays of primitive types.
 * <p>
 * The constants of this class can be used as marshallers with a 
 * {@link FixedWidthTable}.
 */
public class PrimitiveMarshallers {

	public static final FixedWidthMarshaller<Byte> BYTE = new FixedWidthMarshaller<Byte>() {
		public void writeTo(Byte entity, DataOutput out) throws IOException {
			out.writeByte(entity.byteValue());
		}
		public Byte readFrom(DataInput in) throws IOException {
			return Byte.valueOf(in.readByte());
		}
		public int getByteWidth() {return 1;}
	};
	public static final FixedWidthMarshaller<Short> SHORT = new FixedWidthMarshaller<Short>() {
		public void writeTo(Short entity, DataOutput out) throws IOException {
			out.writeInt(entity.shortValue());
		}
		public Short readFrom(DataInput in) throws IOException {
			return Short.valueOf(in.readShort());
		}
		public int getByteWidth() {return 2;}
	};
	public static final FixedWidthMarshaller<Integer> INTEGER = new FixedWidthMarshaller<Integer>() {
		public void writeTo(Integer entity, DataOutput out) throws IOException {
			out.writeInt(entity.intValue());
		}
		public Integer readFrom(DataInput in) throws IOException {
			return Integer.valueOf(in.readInt());
		}
		public int getByteWidth() {return 4;}
	};
	public static final FixedWidthMarshaller<Long> LONG = new FixedWidthMarshaller<Long>() {
		public void writeTo(Long entity, DataOutput out) throws IOException {
			out.writeLong(entity.longValue());
		}
		public Long readFrom(DataInput in) throws IOException {
			return Long.valueOf(in.readLong());
		}
		public int getByteWidth() {return 8;}
	};
	public static final FixedWidthMarshaller<Float> FLOAT = new FixedWidthMarshaller<Float>() {
		public void writeTo(Float entity, DataOutput out) throws IOException {
			out.writeFloat(entity.floatValue());
		}
		public Float readFrom(DataInput in) throws IOException {
			return Float.valueOf(in.readFloat());
		}
		public int getByteWidth() {return 4;}
	};
	public static final FixedWidthMarshaller<Double> DOUBLE = new FixedWidthMarshaller<Double>() {
		public void writeTo(Double entity, DataOutput out) throws IOException {
			out.writeDouble(entity.doubleValue());
		}
		public Double readFrom(DataInput in) throws IOException {
			return Double.valueOf(in.readDouble());
		}
		public int getByteWidth() {return 8;}
	};
	public static final FixedWidthMarshaller<Boolean> BOOLEAN = new FixedWidthMarshaller<Boolean>() {
		public void writeTo(Boolean entity, DataOutput out) throws IOException {
			out.writeBoolean(entity.booleanValue());
		}
		public Boolean readFrom(DataInput in) throws IOException {
			return Boolean.valueOf(in.readBoolean());
		}
		public int getByteWidth() {return 1;}
	};
	public static final FixedWidthMarshaller<Character> CHARACTER = new FixedWidthMarshaller<Character>() {
		public void writeTo(Character entity, DataOutput out) throws IOException {
			out.writeChar(entity.charValue());
		}
		public Character readFrom(DataInput in) throws IOException {
			return Character.valueOf(in.readChar());
		}
		public int getByteWidth() {return 2;}
	};
	
	// no instances
	private PrimitiveMarshallers() {}
}
