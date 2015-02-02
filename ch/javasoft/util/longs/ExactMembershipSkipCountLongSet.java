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
package ch.javasoft.util.longs;

import java.util.NoSuchElementException;

import ch.javasoft.util.Unsigned;

/**
 * The <code>ExactMembershipSkipCountLongSet</code> is a unmodifiable sorted set 
 * containing distinct long values. For convenience, also the list interface is
 * implemented. All modifying methods throw an 
 * {@link UnsupportedOperationException}.
 * <p>
 * The <code>ExactMembershipLongSet</code> stores distinct long values. 
 * Depending on the number of values stored in the set, it uses a different 
 * storage model. For few values, they are simply kept in a sorted array. For
 * many values, the values are split into two parts according to some number k.
 * The low-order k bits of the values are stored in an array of k-bit values.
 * The high-order bits of the values are stored in a bit string. For practical
 * purposes, the value k is chosen only from 0,8,16,32,48,56 and 64. 
 * <p>
 * Let {@code n} be the number of values stored in the set. Then, the storage 
 * used is <code>n+2<sup>(64-k)</sup>+k*n</code> bits, which is approximately 
 * <code> n*(2+64-log<sub>2</sub>(n))</code>.
 * <p>
 * The term <i>skip-count</i> in the class name refers to the additional count
 * array for faster searching in the bit string, similar to the extension 
 * proposed in "Exact Membership Tester 3" of [1].
 * <p>
 * <b>References</b>
 * <br>
 * [1] "Exact Membership Tester 2 and 3" 
 *     in "Exact and approximate membership testers" 
 *     of Larry Carter, Robert Floyd, George Markowsky and Mark Wegman.
 */
public class ExactMembershipSkipCountLongSet extends AbstractExactMembershipLongSet {
	
	private final StorageCharacteristics characteristics;
	private final Storage storage;
	
	/**
	 * Creates a new <code>ExactMembershipLongSet</code> with the given values.
	 * <p>
	 * Note that the value array is not cloned and sorted and it is expected 
	 * that the vales now belong to the new instance. If the caller needs the
	 * values for something else than reading the now sorted values, it should
	 * be cloned when calling this method.
	 * 
	 * @param values	the values
	 */
	public ExactMembershipSkipCountLongSet(long[] values) {
		this(values, values.length);
	}
	/**
	 * Creates a new <code>ExactMembershipLongSet</code> with the given values.
	 * <p>
	 * Note that the value array is not cloned and sorted and it is expected 
	 * that the vales now belong to the new instance. If the caller needs the
	 * values for something else than reading the now sorted values, it should
	 * be cloned when calling this method.
	 * 
	 * @param values	the values
	 * @param n			the size values influencing the storage model of this
	 * 					set, must be at least the length of {@code values}
	 * @throws IllegalArgumentException if {@code n < values.length}
	 */
	public ExactMembershipSkipCountLongSet(long[] values, int n) {
		if (n < values.length) {
			throw new IllegalArgumentException("n must be at least the length of the values array: " + n + "<" + values.length);
		}
		final int negStart = Unsigned.sort(values);
		characteristics = StorageCharacteristics.getStorageCharacteristics(n);
		storage = characteristics.createStorage(values, negStart);
	}
	
	private ExactMembershipSkipCountLongSet(Storage storage) {
		this.characteristics	= StorageCharacteristics.getStorageCharacteristics(storage);
		this.storage			= storage;
	}

	@Override
	public ExactMembershipSkipCountLongSet intersect(AbstractExactMembershipLongSet with) {
		final Storage storage = getStorage().intersect(with.getStorage());
		return new ExactMembershipSkipCountLongSet(storage);
	}
	
	@Override
	protected Storage getStorage() {
		return storage;
	}
	
	///////////////////////// internal classes and interfaces
	
	private static enum StorageCharacteristics {
		K64(4,64) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage64(sorted, negStart);
			}
		}, 
		K56(12,56) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage56(sorted, negStart);
			}
		}, 
		K48(20,48) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage48(sorted, negStart);
			}
		}, 
		K40(28,40) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage40(sorted, negStart);
			}
		}, 
		K32(36,32) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage32(sorted, negStart);
			}
		};
		private StorageCharacteristics(int upper, int k) {
			this.upper	= upper;
			this.k		= k;
		};
		public final int upper;
		public final int k;
		/**
		 * Create the storage object, based on the ascending sorted values and
		 * the index of the first negative number.
		 * 
		 * @param sorted	the values, sorted ascending
		 * @param negStart	the index of the first negative number, or the 
		 * 					length of the {@code sorted} array if all numbers 
		 * 					nonnegative
		 * @return the storage object
		 */
		abstract public Storage createStorage(long[] sorted, int negStart);
		/**
		 * Returns the storage characteristics constant based on the value size.
		 * The characteristics are chosen such that the storage uses as little
		 * memory as possible.
		 */
		public static StorageCharacteristics getStorageCharacteristics(int n) {
			if (n < 0) {
				throw new IllegalArgumentException("n must be non-negative: " + n);
			}
			for (StorageCharacteristics val : values()) {
				if ((1L << val.upper) > n) return val;				
			}
			//should not get here
			throw new RuntimeException("internal error: no characteristics found for " + n);
		}
		/**
		 * Returns the storage characteristics constant based on the 
		 * {@link AbstractExactMembershipLongSet.Storage#getK() k-value} of the 
		 * specified storage.
		 */
		public static StorageCharacteristics getStorageCharacteristics(Storage storage) {
			final int k = storage.getK();
			for (StorageCharacteristics val : values()) {
				if (val.k == k) return val;				
			}
			//should not get here
			throw new RuntimeException("internal error: no characteristics found for k=" + k);
		}
	}

	/**
	 * <code>AbstractHighLowStorage</code> separates the storage of the long
	 * values into two parts. A bit string is used for the high order (most 
	 * significant) bits (see {@link #getHighOrderBits(int)}), and one or 
	 * multiple arrays for the low order (least significant) bits (see 
	 * {@link #getLowOrderBits(int)}). The size of the high- and low-order part
	 * is determined by the subclasses.
	 */
	private abstract static class AbstractHighLowStorage extends AbstractStorage {
		/**
		 * Constructor with index of first negative number 
		 * @param k			the number of low-order bits 
		 * @param negStart	the index of the first negative number, or the 
		 * 					total number of values if all numbers are 
		 * 					nonnegative
		 */
		public AbstractHighLowStorage(int k, int negStart) {
			super(k, negStart);
		}
		/**
		 * The <code>CountSkipList</code> is a helper class counting the number
		 * of one bits per 8 z-array entries.
		 */
		protected static class CountSkipList {
			private final int[] count1;
			public CountSkipList(long[] z) {
				final int zlen = z.length;
				final int clen = 1 + (zlen>>>3);//div 8
				count1 = new int[clen];
				
				int cnt1 = 0;
				
				for (int i = 0; i < zlen; i++) {
					final int cnt = Long.bitCount(z[i]);
					cnt1 += cnt;
					final int next = i+1;
					if ((next%8) == 0 || next == zlen) {
						final int cindex = i>>>3;
						count1[cindex] = cnt1;
					}
				}
				
			}
			/**
			 * 
			 * Finds the first one-count value that is larger than or equal to 
			 * the specified {@code value}. Returns an array {@code ret} of size 
			 * two, where {@code ret[0]} refers to z-index of the found value
			 * and {@code ret[1]} the one-count value.
			 *  
			 * @param value the value to search
			 * @return 	A length-two array {@code ret}, where {@code ret[0]} 
			 * 			refers to the z-index of the first one-count value such 
			 * 			that <code>ret[1] &ge; value</code>
			 */
			public int[] find1(int value) {
				int index = java.util.Arrays.binarySearch(count1, value);
				if (index < 0) {
					index = -index - 1;
				}
				else {
					while (index > 0 && count1[index-1] >= value) {
						index--;
					}
				}
				return new int[] {index<<3, index == 0 ? 0 : count1[index-1]};
			}
			/**
			 * Returns the total number of bytes used to store the values. Constant
			 * parts such as size value or array length field are omitted.
			 * 
			 * @return the number of bytes used to store the values
			 */
			public long bytesSize() {
				return 4L*count1.length;
			}
		}
		
		/**
		 * Implementation of 
		 * {@link AbstractExactMembershipLongSet.Storage#get(int)} 
		 * reconstructing the value from {@code (high[index] | low[index])}, 
		 * where {@code high} and {@code low} are the values returned by 
		 * {@link #getHighOrderBits(int)} and {@link #getLowOrderBits(int)}, 
		 * respectively.
		 */
		public long get(int index) {
			return getHighOrderBits(index) | getLowOrderBits(index);
		}		
		/**
		 * Returns the uncloned bit string array {@code ret}. The first bit is 
		 * stored in {@code ret[0]} as least significant bit.
		 * @return the raw bit string array
		 */
		abstract protected long[] bitString();
		/**
		 * Returns the count-skip-list for faster searching in the bit string
		 * @return the count-skip-list
		 */
		abstract protected CountSkipList getCountSkipList();
		/**
		 * Returns the high order bits, used to recover the value by 
		 * {@link #get(int)}.
		 * @param index the list or unsigned mean index of the desired value
		 * @return the high order bits of the desired value
		 */
		abstract protected long getHighOrderBits(int index);
		/**
		 * Returns the high order bits, used to recover the value by 
		 * {@link #get(int)}.
		 * @param index the list or unsigned mean index of the desired value
		 * @return the high order bits of the desired value
		 */
		abstract protected long getLowOrderBits(int index);
		/**
		 * Returns the high order bits without shifting, which is performed
		 * specifically by the subclass to implement 
		 * {@link #getHighOrderBits(int)}.
		 * @param index the list or unsigned mean index of the desired value
		 * @return the high order bits of the desired value
		 */
		protected long getHighOrderBitsUnshifted(int index) {
			final int bitsToSee = index+1;
			final long[] z = bitString();
			final int[] indexBits = getCountSkipList().find1(index); 
			int zIndex	= indexBits[0]-1;
			int oneBits	= indexBits[1];
//			int zIndex	= -1;
//			int oneBits	= 0;
			int curBits	= 0;
			while (oneBits + curBits < bitsToSee && zIndex < z.length) {
				zIndex++;
				oneBits += curBits;
				curBits = Long.bitCount(z[zIndex]);
			}
			if (curBits + oneBits < bitsToSee) {
				throw new NoSuchElementException("index=" + index);
			}
			final int clearBits = bitsToSee - oneBits - 1;
			
			long cur = z[zIndex];
			
			for (int i = 0; i < clearBits; i++) {
				cur ^= Long.lowestOneBit(cur);
				curBits--;
			}
			return 64L*zIndex+Long.numberOfTrailingZeros(cur)-index;
		}
		//inherit javadoc
		public int indexOf(long value) {
			//TODO: there is probably a more efficient way to do this
			
			//binary search
			int low = 0;
			int high = size() - 1;
		
			while (low <= high) {
			    final int mid = (low + high) >>> 1;
				final long midVal = get(mid);
		
			    final int cmp = Unsigned.compare(midVal, value);
			    if (cmp < 0)
			    	low = mid + 1;
			    else if (cmp > 0)
			    	high = mid - 1;
			    else
			    	return mid; // key found
			}
			return -(low + 1);  // key not found.
		}
		/** Always throws an exception*/
		public Storage intersect(Storage with) {
			throw new UnsupportedOperationException("intersect not supported");
		}
	}
	/**
	 * <code>Storage56</code> stores the 32 most significant bits of the long
	 * values in the bit string, and the 32 least significant bits in array(s). 
	 */
	private static class Storage32 extends AbstractHighLowStorage {
		private final CountSkipList counts;
		private final long[] z;
		private final int[] y;
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage32(long[] sorted, int negStart) {
			super(32, negStart);
			final int len = sorted.length;
			final long max = len > 0 ? (sorted[len-1] >>> 32) : 0;			
			final long bits = len + max;
			final long zlen = 1+((bits-1)>>6)/*div 64*/;
			z = new long[(int)zlen];
			y = new int[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 32) + i;
				z[(int)(index>>6/*div 64*/)] |= (1L << (index & 0x0000007f /*mod 64*/));
				y[i] = (int)sorted[i];//low order 32 bit
			}
			counts = new CountSkipList(z);
		}
		@Override
		public CountSkipList getCountSkipList() {
			return counts;
		}
		public int size() {
			return y.length;
		}
		@Override
		public long[] bitString() {
			return z;
		}
		@Override
		protected long getHighOrderBits(int index) {			
			return getHighOrderBitsUnshifted(index) << 32;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return 0x00000000ffffffffL & y[index];
		}
		public long bytesSize() {
			return 8L*z.length + 4L*y.length + counts.bytesSize();
		}
	}
	/**
	 * <code>Storage56</code> stores the 24 most significant bits of the long
	 * values in the bit string, and the 40 least significant bits in arrays. 
	 */
	private static class Storage40 extends AbstractHighLowStorage {
		private final CountSkipList counts;
		private final long[] z;
		private final int[] y0to31;
		private final byte[] y32to39;
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage40(long[] sorted, int negStart) {
			super(40, negStart);
			final int len = sorted.length;
			final long max = len > 0 ? (sorted[len-1] >>> 40) : 0;			
			final long bits = len + max;
			final long zlen = 1+((bits-1)>>6)/*div 64*/;
			z = new long[(int)zlen];
			y0to31 = new int[len];
			y32to39 = new byte[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 40) + i;
				z[(int)(index>>6/*div 64*/)] |= (1L << (index & 0x0000007f /*mod 64*/));
				y0to31[i] = (int)sorted[i];//low order 32 bit
				y32to39[i] = (byte)(sorted[i] >> 32);//low order bits 32 to 39
			}
			counts = new CountSkipList(z);
		}
		@Override
		public CountSkipList getCountSkipList() {
			return counts;
		}
		@Override
		public long[] bitString() {
			return z;
		}
		public int size() {
			return y0to31.length;
		}
		@Override
		protected long getHighOrderBits(int index) {			
			return getHighOrderBitsUnshifted(index) << 40;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return (0x00000000ffffffffL & y0to31[index]) | ((0x00000000000000ffL & y32to39[index]) << 32);
		}
		
		public long bytesSize() {
			return 8L*z.length + 4L*y0to31.length + y32to39.length + counts.bytesSize();
		}
	}
	/**
	 * <code>Storage56</code> stores the 16 most significant bits of the long
	 * values in the bit string, and the 48 least significant bits in arrays. 
	 */
	private static class Storage48 extends AbstractHighLowStorage {
		private final CountSkipList counts;
		private final long[] z;
		private final int[] y0to31;
		private final short[] y32to47;
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage48(long[] sorted, int negStart) {
			super(48, negStart);
			final int len = sorted.length;
			final long max = len > 0 ? (sorted[len-1] >>> 48) : 0;			
			final long bits = len + max;
			final long zlen = 1+((bits-1)>>6)/*div 64*/;
			z = new long[(int)zlen];
			y0to31 = new int[len];
			y32to47 = new short[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 48) + i;
				z[(int)(index>>6/*div 64*/)] |= (1L << (index & 0x0000007f /*mod 64*/));
				y0to31[i] = (int)sorted[i];//low order 32 bit
				y32to47[i] = (short)(sorted[i] >>> 32);//low order bits 32 to 47
			}
			counts = new CountSkipList(z);
		}
		@Override
		public CountSkipList getCountSkipList() {
			return counts;
		}
		@Override
		public long[] bitString() {
			return z;
		}
		public int size() {
			return y0to31.length;
		}
		@Override
		protected long getHighOrderBits(int index) {			
			return getHighOrderBitsUnshifted(index) << 48;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return (0x00000000ffffffffL & y0to31[index]) | ((0x000000000000ffffL & y32to47[index]) << 32);
		}
		public long bytesSize() {
			return 8L*z.length + 4L*y0to31.length + 2L*y32to47.length + counts.bytesSize();
		}
	}
	/**
	 * <code>Storage56</code> stores the 8 most significant bits of the long
	 * values in the bit string, and the 56 least significant bits in arrays. 
	 */
	private static class Storage56 extends AbstractHighLowStorage {
		private final CountSkipList counts;
		private final long[] z;
		private final int[] y0to31;
		private final short[] y32to47;
		private final byte[] y48to55;
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage56(long[] sorted, int negStart) {
			super(56, negStart);
			final int len = sorted.length;
			final long max = len > 0 ? (sorted[len-1] >>> 56) : 0;			
			final long bits = len + max;
			final long zlen = 1+((bits-1)>>6)/*div 64*/;
			z = new long[(int)zlen];
			y0to31 = new int[len];
			y32to47 = new short[len];
			y48to55 = new byte[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 56) + i;
				z[(int)(index>>6/*div 64*/)] |= (1L << (index & 0x0000007f /*mod 64*/));
				y0to31[i] = (int)sorted[i];//low order 32 bit
				y32to47[i] = (short)(sorted[i] >>> 32);//low order bits 32 to 47
				y48to55[i] = (byte)(sorted[i] >>> 48);//low order bits 48 to 56
			}
			counts = new CountSkipList(z);
		}
		@Override
		public CountSkipList getCountSkipList() {
			return counts;
		}
		@Override
		public long[] bitString() {
			return z;
		}
		public int size() {
			return y0to31.length;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return (0x00000000ffffffffL & y0to31[index]) | ((0x000000000000ffffL & y32to47[index]) << 32) | ((0x00000000000000ffL & y48to55[index]) << 48);
		}
		@Override
		protected long getHighOrderBits(int index) {			
			return getHighOrderBitsUnshifted(index) << 56;
		}
		public long bytesSize() {
			return 8L*z.length + 4L*y0to31.length + 2L*y32to47.length + y48to55.length + counts.bytesSize();
		}
	}
}
