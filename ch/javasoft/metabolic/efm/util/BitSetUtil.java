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
package ch.javasoft.metabolic.efm.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.bitset.BitSetFactory;
import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

/**
 * The <code>BitSetUtil</code> offers some common methods related to bit sets.
 */
public class BitSetUtil {
	
	/**
	 * Returns the factory for bit set instantiation
	 */
	public static BitSetFactory factory() {
		return LongBitSet.FACTORY;//fastest
//		return IntBitSet.FACTORY;//ok
//		return ByteBitSet.FACTORY;//very slow
//		return DefaultBitSet.FACTORY;//slow
	}
	
	/**
	 * Returns the number of longs needed to store a {@link IBitSet}
	 * of the given bit capacity. Capacity here is the same as the boolean size 
	 * of the columns
	 */
	public static int longSize(int bitCapacity) {
		return (bitCapacity - 1) / 64 + 1;
	}
	/**
	 * Returns the number of bytes needed to store a {@link IBitSet}
	 * of the given bit capacity using 
	 * {@link #writeTo(IBitSet, int, DataOutput)}.
	 * Capacity here is the same as the boolean size of the columns
	 */
	public static int byteSize(int bitCapacity) {
		return (bitCapacity - 1) / 8 + 1;
	}
	
	/**
	 * Returns a long array containing the bits of the given bit set in raw
	 * form. 
	 * <p>
	 * This method is the opponent to {@link #fromLongArray(long[], boolean)}
	 * 
	 * @param bitSet	the bit set to encode
	 * @return the array with the bits in raw form
	 */
	public static long[] toLongArray(IBitSet bitSet) {
		return LongBitSet.FACTORY.convert(bitSet).toLongArray();
	}
	
	/**
	 * Returns a new bit set instance based on the given long array containing
	 * the bits. If {@code cloneArray} is true, the long array is cloned. The
	 * {@link #factory() factory} is used to get an instance of the current bit 
	 * set type.
	 * <p>
	 * This method is the opponent to {@link #toLongArray(IBitSet)}
	 * 
	 * @param longs			the bit data in its raw form
	 * @param cloneArray	if true, the array is cloned
	 * @return the new bit set instance
	 */
	public static IBitSet fromLongArray(long[] longs, boolean cloneArray) {
		return factory().convert(new LongBitSet(longs, cloneArray));
	}
	
	/**
	 * Creates a new bit set instance by reading bytes from the given data 
	 * input assuming the specified bit capacity. 
	 * <p>
	 * The bit capacity is to be specified because it is not contained in the 
	 * raw data to save storage space, since all bit sets have usually the same 
	 * capacity. Capacity here is the same as the boolean size of the columns.
	 */
	public static IBitSet readFrom(DataInput in, int bitCapacity) throws IOException {
		final long[] longs = new long[longSize(bitCapacity)];
		final int byteSize = byteSize(bitCapacity);
		for (int i = 0; i < byteSize; i++) {
			byte bt = in.readByte();
			long lg = 0x00000000000000ffL & bt;//the signed/unsigned conflict when converting byte to long
			longs[i / 8] |= (lg << (8 * (i % 8)));
		}
		return fromLongArray(longs, false /*cloneArray*/);
	}
	/**
	 * Writes the given bit set to the data output, assuming the specified bit 
	 * capacity.
	 * <p>
	 * The bit capacity is to be specified because it is not contained in the 
	 * raw data to save storage space, since all bit sets have usually the same 
	 * capacity. Capacity here is the same as the boolean size of the columns.
	 */
	public static void writeTo(IBitSet bitSet, int bitCapacity, DataOutput out) throws IOException {
		final long[] longs = toLongArray(bitSet);
		final int byteSize = byteSize(bitCapacity);
		for (int i = 0; i < byteSize; i++) {
			int index = i / 8;
			long lVal = index < longs.length ? longs[index] : 0L;
			byte bVal = (byte)(0x00000000000000ffL & (lVal >>> (8 * (i % 8))));
			out.writeByte(bVal);
		}
	}

	/**
	 * Returns the shorted possible (rounded to byte) hex string for the given
	 * bit set. This string can for instance be used as a (unique) identifier,
	 * e.g. to create lock files for a identifying bit set.
	 *  
	 * @param bitSet		the bit setto create the hex string for
	 * @param bitSetSize	the bit set size
	 * @return the hex string for the given set
	 */
	public static String toHexString(IBitSet bitSet, int bitSetSize) {
		final int bytes = 1 + (bitSetSize / 8);
		final StringBuilder sb = new StringBuilder(bytes + 8);
		for (final long l : toLongArray(bitSet)) {
			sb.append(Long.toHexString(l));
		}
		sb.setLength(bytes);
		return sb.toString();
	}
	
    //no instances
    private BitSetUtil() {}

}
