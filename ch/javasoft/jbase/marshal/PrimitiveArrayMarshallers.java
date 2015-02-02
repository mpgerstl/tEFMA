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

import ch.javasoft.jbase.EntityMarshaller;
import ch.javasoft.jbase.FixedWidthMarshaller;
import ch.javasoft.jbase.FixedWidthTable;
import ch.javasoft.jbase.VariableWidthTable;

/**
 * <tt>PrimitiveArrayMarshallers</tt> contains constants and static methods to
 * access marshallers for one dimensional arrays of primitive data types. One 
 * row (or entity) corresponds to an array of some primitive Java data type.
 * <p>
 * If the array size is previously known, the appropriate static method can
 * be used to create a {@link FixedWidthMarshaller} instance for the specified 
 * array size. Such marshallers can be used with a {@link FixedWidthTable}.
 * <p>
 * If the array size is not known and possibly varying for different entity 
 * instances (or rows), the constant marshallers can be used. They write the 
 * array length for each row and are not {@link FixedWidthMarshaller} instances.
 * Such marshallers can be used with a {@link VariableWidthTable}.
 */
public class PrimitiveArrayMarshallers {

	public static final EntityMarshaller<byte[]> BYTE = new EntityMarshaller<byte[]>() {
		public void writeTo(byte[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			out.write(entity);
		}
		public byte[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final byte[] arr = new byte[len];
			in.readFully(arr);
			return arr;
		}
	};
	public static final EntityMarshaller<short[]> SHORT = new EntityMarshaller<short[]>() {
		public void writeTo(short[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeShort(entity[i]);
			}
		}
		public short[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final short[] arr = new short[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readShort();
			}			
			return arr;
		}
	};
	public static final EntityMarshaller<int[]> INT = new EntityMarshaller<int[]>() {
		public void writeTo(int[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeInt(entity[i]);
			}
		}
		public int[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final int[] arr = new int[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readInt();
			}			
			return arr;
		}
	};
	public static final EntityMarshaller<long[]> LONG = new EntityMarshaller<long[]>() {
		public void writeTo(long[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeLong(entity[i]);
			}
		}
		public long[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final long[] arr = new long[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readLong();
			}			
			return arr;
		}
	};
	public static final EntityMarshaller<float[]> FLOAT = new EntityMarshaller<float[]>() {
		public void writeTo(float[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeFloat(entity[i]);
			}
		}
		public float[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final float[] arr = new float[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readFloat();
			}			
			return arr;
		}
	};
	public static final EntityMarshaller<double[]> DOUBLE = new EntityMarshaller<double[]>() {
		public void writeTo(double[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeDouble(entity[i]);
			}
		}
		public double[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final double[] arr = new double[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readDouble();
			}			
			return arr;
		}
	};
	public static final EntityMarshaller<boolean[]> BOOLEAN = new EntityMarshaller<boolean[]>() {
		public void writeTo(boolean[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeBoolean(entity[i]);
			}
		}
		public boolean[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final boolean[] arr = new boolean[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readBoolean();
			}			
			return arr;
		}
	};
	public static final EntityMarshaller<char[]> CHAR = new EntityMarshaller<char[]>() {
		public void writeTo(char[] entity, DataOutput out) throws IOException {
			out.writeInt(entity.length);
			for (int i = 0; i < entity.length; i++) {
				out.writeChar(entity[i]);
			}
		}
		public char[] readFrom(DataInput in) throws IOException {
			final int len = in.readInt();
			final char[] arr = new char[len];
			for (int i = 0; i < len; i++) {
				arr[i] = in.readChar();
			}			
			return arr;
		}
	};
	
	public static FixedWidthMarshaller<byte[]> getForFixedByteArray(final int arrayLength) {		
		return new FixedWidthMarshaller<byte[]>() {
			public void writeTo(byte[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				out.write(entity);
			}
			public byte[] readFrom(DataInput in) throws IOException {
				final byte[] arr = new byte[arrayLength];
				in.readFully(arr);
				return arr;
			}
			public int getByteWidth() {return arrayLength;}
		};
	}
	public static FixedWidthMarshaller<short[]> getForFixedShortArray(final int arrayLength) {
		return new FixedWidthMarshaller<short[]>() {
			public void writeTo(short[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeShort(entity[i]);
				}
			}
			public short[] readFrom(DataInput in) throws IOException {
				final short[] arr = new short[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readShort();
				}
				return arr;
			}
			public int getByteWidth() {return 2*arrayLength;}
		};
	}
	public static FixedWidthMarshaller<int[]> getForFixedIntArray(final int arrayLength) {
		return new FixedWidthMarshaller<int[]>() {
			public void writeTo(int[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeInt(entity[i]);
				}
			}
			public int[] readFrom(DataInput in) throws IOException {
				final int[] arr = new int[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readInt();
				}
				return arr;
			}
			public int getByteWidth() {return 4*arrayLength;}
		};
	}
	public static FixedWidthMarshaller<long[]> getForFixedLongArray(final int arrayLength) {
		return new FixedWidthMarshaller<long[]>() {
			public void writeTo(long[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeLong(entity[i]);
				}
			}
			public long[] readFrom(DataInput in) throws IOException {
				final long[] arr = new long[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readLong();
				}
				return arr;
			}
			public int getByteWidth() {return 8*arrayLength;}
		};
	}
	public static FixedWidthMarshaller<float[]> getForFixedFloatArray(final int arrayLength) {
		return new FixedWidthMarshaller<float[]>() {
			public void writeTo(float[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeFloat(entity[i]);
				}
			}
			public float[] readFrom(DataInput in) throws IOException {
				final float[] arr = new float[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readFloat();
				}
				return arr;
			}
			public int getByteWidth() {return 4*arrayLength;}
		};
	}
	public static FixedWidthMarshaller<double[]> getForFixedDoubleArray(final int arrayLength) {
		return new FixedWidthMarshaller<double[]>() {
			public void writeTo(double[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeDouble(entity[i]);
				}
			}
			public double[] readFrom(DataInput in) throws IOException {
				final double[] arr = new double[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readDouble();
				}
				return arr;
			}
			public int getByteWidth() {return 8*arrayLength;}
		};
	}
	public static FixedWidthMarshaller<boolean[]> getForFixedBooleanArray(final int arrayLength) {
		return new FixedWidthMarshaller<boolean[]>() {
			public void writeTo(boolean[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeBoolean(entity[i]);
				}
			}
			public boolean[] readFrom(DataInput in) throws IOException {
				final boolean[] arr = new boolean[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readBoolean();
				}
				return arr;
			}
			public int getByteWidth() {return arrayLength;}
		};
	}
	public static FixedWidthMarshaller<char[]> getForFixedCharArray(final int arrayLength) {
		return new FixedWidthMarshaller<char[]>() {
			public void writeTo(char[] entity, DataOutput out) throws IOException {
				if (entity.length != arrayLength) {
					throw new IOException("invalid array length " + 
							entity.length + ", expected " + arrayLength); 
				}
				for (int i = 0; i < arrayLength; i++) {
					out.writeChar(entity[i]);
				}
			}
			public char[] readFrom(DataInput in) throws IOException {
				final char[] arr = new char[arrayLength];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = in.readChar();
				}
				return arr;
			}
			public int getByteWidth() {return 2*arrayLength;}
		};
	}
	
	// no instances
	private PrimitiveArrayMarshallers() {}
}
