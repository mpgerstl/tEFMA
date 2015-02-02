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

import ch.javasoft.util.Iterables;
import ch.javasoft.util.LongArray;
import ch.javasoft.util.Unsigned;

/**
 * The <code>ExactMembershipAllCountLong</code> is a unmodifiable sorted set 
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
 * The term <i>all-count</i> in the class name refers to the way of storing and
 * accessing the bit string (high-order k bits). Here, we do not store the bits
 * as proposed in [1], but we maintain an array with the counts associated with
 * the corresponding position.
 * <p>
 * <b>References</b>
 * <br>
 * [1] "Exact Membership Tester 2 and 3" 
 *     in "Exact and approximate membership testers" 
 *     of Larry Carter, Robert Floyd, George Markowsky and Mark Wegman.
 */
public class ExactMembershipAllCountLongSet extends AbstractExactMembershipLongSet {
	
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
	public ExactMembershipAllCountLongSet(long[] values) {
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
	public ExactMembershipAllCountLongSet(long[] values, int n) {
		if (n < values.length) {
			throw new IllegalArgumentException("n must be at least the length of the values array: " + n + "<" + values.length);
		}
		final int negStart = Unsigned.sort(values);
		characteristics = StorageCharacteristics.getStorageCharacteristics(n);
		storage = characteristics.createStorage(values, negStart);
	}
	
	private ExactMembershipAllCountLongSet(Storage storage) {
		this.characteristics	= StorageCharacteristics.getStorageCharacteristics(storage);
		this.storage			= storage;
	}

	@Override
	protected Storage getStorage() {
		return storage;
	}
	
	@Override
	public ExactMembershipAllCountLongSet intersect(AbstractExactMembershipLongSet with) {
		final Storage storage = getStorage().intersect(with.getStorage());
		return new ExactMembershipAllCountLongSet(storage);
	}
	///////////////////////// internal classes and interfaces
	
	private static enum StorageCharacteristics {
		K64(10,64) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage64(sorted, negStart);
			}
			@Override
			public Storage createStorage(LongIterable sorted, int negStart) {				
				return new Storage64(sorted, negStart);
			}
		}, 
		K56(18,56) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage56(sorted, negStart);
			}
			@Override
			public Storage createStorage(LongIterable sorted, int negStart) {				
				return new Storage56(sorted, negStart);
			}
		}, 
		K48(26,48) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage48(sorted, negStart);
			}
			@Override
			public Storage createStorage(LongIterable sorted, int negStart) {				
				return new Storage48(sorted, negStart);
			}
		}, 
		K40(34,40) {
			@Override
			public Storage createStorage(long[] sorted, int negStart) {				
				return new Storage40(sorted, negStart);
			}
			@Override
			public Storage createStorage(LongIterable sorted, int negStart) {				
				return new Storage40(sorted, negStart);
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
		 * Create the storage object, based on the ascending sorted values and
		 * the index of the first negative number.
		 * 
		 * @param sorted	the values, sorted ascending
		 * @param negStart	the index of the first negative number, or the 
		 * 					length of the {@code sorted} array if all numbers 
		 * 					nonnegative
		 * @return the storage object
		 */
		abstract public Storage createStorage(LongIterable sorted, int negStart);
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
			throw new RuntimeException("internal error: no characteristics found for n=" + n);
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
		protected final int length;
		protected final int[] counts;
		/**
		 * Constructor with index of first negative number 
		 * @param k			the number of low-order bits 
		 * @param negStart	the index of the first negative number, or the 
		 * 					total number of values if all numbers are 
		 * 					nonnegative
		 */
		public AbstractHighLowStorage(int k, int negStart, int len) {
			super(k, negStart);
			final int notK = 64-k;			
			final int clen = (1 << notK);
			length = len;
			counts = new int[clen];
		}
		/**
		 * Converts the counts to cumulated counts. Should be called in
		 * subclass constructors.
		 */
		protected void makeCumSum() {
			//make cumsum in counts
			final int clen = counts.length;
			for (int i = 1; i < clen; i++) {
				counts[i] += counts[i-1];
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
		 * Returns the high order bits, used to recover the value by 
		 * {@link #get(int)}.
		 * @param index the list or unsigned mean index of the desired value
		 * @return the high order bits of the desired value
		 */
		protected long getHighOrderBits(int index) {
			final int len = counts.length;
			final int k = getK();
			int cindex = java.util.Arrays.binarySearch(counts, index);
			if (cindex < 0) cindex = -cindex-1;
			while (cindex < len && counts[cindex] == index) cindex++;
			return (((long)cindex) << k);
		}
		/**
		 * Returns the high order bits, used to recover the value by 
		 * {@link #get(int)}.
		 * @param index the list or unsigned mean index of the desired value
		 * @return the high order bits of the desired value
		 */
		abstract protected long getLowOrderBits(int index);
		
		/**
		 * Performs a binary search on the low order bits array. Note that this
		 * array is sorted if we only search within a range where the high-order 
		 * bits remain constant. This must be ensured by the caller.
		 * 
		 * @param key	the low order bits to find, already truncated
		 * @param start	the start index in the y-array, inclusive
		 * @param end	the end index in the y-array, exlusive
	     * @return index of the search key, if it is contained in this set;
	     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
	     *	       <i>insertion point</i> is defined as the point at which the
	     *	       key would be inserted into the list: the index of the first
	     *	       element in the range greater than the key,
	     *	       or <tt>toIndex</tt> if all
	     *	       elements in the range are less than the specified key.  Note
	     *	       that this guarantees that the return value will be &gt;= 0 if
	     *	       and only if the key is found.
		 */
		private int indexOfLowOrderBits(long key, int start, int end) {
			//binary search
			int low = start;
			int high = end - 1;
		
			while (low <= high) {
			    final int mid = (low + high) >>> 1;
				final long midVal = getLowOrderBits(mid);
		
			    final int cmp = Unsigned.compare(midVal, key);
			    if (cmp < 0)
			    	low = mid + 1;
			    else if (cmp > 0)
			    	high = mid - 1;
			    else
			    	return mid; // key found
			}
			return -(low + 1);  // key not found.			
		}
		
		//inherit javadoc
		public int indexOf(long value) {
			final int k = getK();
			final long high = (value >>> k);
			final long low 	= (value & (~(0xffffffffffffffffL << k)));
			
			final int cind	= (int)high; 
			final int end 	= counts[cind];
			final int start	= cind > 0 ? counts[cind-1] : 0;
			
			return indexOfLowOrderBits(low, start, end);//FIXME optimize
//			for (int i = start; i < end; i++) {
//				final long curLow = getLowOrderBits(i);
//				if (curLow == low) return i;
//				else if (curLow > low) return -i-1;
//			}
//			return -end-1;
		}
		public Storage intersect(Storage with) {
			if (with.getClass().equals(getClass())) {
				final AbstractHighLowStorage other = (AbstractHighLowStorage)with;
				final int k = getK();
				final LongArray inter = new LongArray();
				final int curFirstNeg = indexOfFirstNegativeValue();
				int myprev = 0;
				int otprev = 0;
				int newFirstNeg = -1;
				for (int c = 0; c < counts.length; c++) {
					final int mycnt = counts[c]; 
					final int otcnt = other.counts[c];
					int i = myprev; int j = otprev;
					while (i < mycnt && j < otcnt) {
						final long me = getLowOrderBits(i);
						final long ot = other.getLowOrderBits(j);
						final int cmp = Unsigned.compare(me, ot);
						if (cmp < 0) {
							i++;
						}
						else if (cmp > 0) {
							j++;
						}
						else {
							if (newFirstNeg < 0 && i >= curFirstNeg) {
								newFirstNeg = inter.length();
							}
							inter.add((((long)c) << k) | me);
							i++;
							j++;
						}
					}
					myprev = mycnt;
					otprev = otcnt;
				}
				return StorageCharacteristics.getStorageCharacteristics(inter.length()).createStorage(inter.toArray(), newFirstNeg);
			}
			throw new IllegalArgumentException("must be same storage class: " + getClass().getName() + " is not " + with.getClass().getName());
		}
	}
	/**
	 * <code>Storage56</code> stores the 24 most significant bits of the long
	 * values in the bit string, and the 40 least significant bits in arrays. 
	 */
	private static class Storage40 extends AbstractHighLowStorage {
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
			super(40, negStart, sorted.length);
			final int len = sorted.length;
			y0to31 = new int[len];
			y32to39 = new byte[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 40);
				counts[(int)index]++;
				y0to31[i] = (int)sorted[i];//low order 32 bit
				y32to39[i] = (byte)(sorted[i] >> 32);//low order bits 32 to 39
			}
			makeCumSum();
		}
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage40(LongIterable sorted, int negStart) {
			super(40, negStart, Iterables.iterableSize(sorted));
			final int len = length;
			y0to31 = new int[len];
			y32to39 = new byte[len];
			final LongIterator it = sorted.iterator();
			for (int i = 0; i < len; i++) {
				final long value = it.nextLong();
				final long index = (value >>> 40);
				counts[(int)index]++;
				y0to31[i] = (int)value;//low order 32 bit
				y32to39[i] = (byte)(value >> 32);//low order bits 32 to 39
			}
			makeCumSum();
		}
		public int size() {
			return y0to31.length;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return (0x00000000ffffffffL & y0to31[index]) | ((0x00000000000000ffL & y32to39[index]) << 32);
		}
		
		public long bytesSize() {
			return 4L*counts.length + 4L*y0to31.length + y32to39.length;
		}
	}
	/**
	 * <code>Storage56</code> stores the 16 most significant bits of the long
	 * values in the bit string, and the 48 least significant bits in arrays. 
	 */
	private static class Storage48 extends AbstractHighLowStorage {
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
			super(48, negStart, sorted.length);
			final int len = sorted.length;
			y0to31 = new int[len];
			y32to47 = new short[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 48);
				counts[(int)index]++;
				y0to31[i] = (int)sorted[i];//low order 32 bit
				y32to47[i] = (short)(sorted[i] >>> 32);//low order bits 32 to 47
			}
			makeCumSum();
		}
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage48(LongIterable sorted, int negStart) {
			super(48, negStart, Iterables.iterableSize(sorted));
			final int len = length;
			y0to31 = new int[len];
			y32to47 = new short[len];
			final LongIterator it = sorted.iterator();
			for (int i = 0; i < len; i++) {
				final long value = it.nextLong();
				final long index = (value >>> 48);
				counts[(int)index]++;
				y0to31[i] = (int)value;//low order 32 bit
				y32to47[i] = (short)(value >>> 32);//low order bits 32 to 47
			}
			makeCumSum();
		}
		public int size() {
			return y0to31.length;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return (0x00000000ffffffffL & y0to31[index]) | ((0x000000000000ffffL & y32to47[index]) << 32);
		}
		public long bytesSize() {
			return 4L*counts.length + 4L*y0to31.length + 2L*y32to47.length;
		}
	}
	/**
	 * <code>Storage56</code> stores the 8 most significant bits of the long
	 * values in the bit string, and the 56 least significant bits in arrays. 
	 */
	private static class Storage56 extends AbstractHighLowStorage {
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
			super(56, negStart, sorted.length);
			final int len = sorted.length;
			y0to31 = new int[len];
			y32to47 = new short[len];
			y48to55 = new byte[len];
			for (int i = 0; i < len; i++) {
				final long index = (sorted[i] >>> 56);
				counts[(int)index]++;
				y0to31[i] = (int)sorted[i];//low order 32 bit
				y32to47[i] = (short)(sorted[i] >>> 32);//low order bits 32 to 47
				y48to55[i] = (byte)(sorted[i] >>> 48);//low order bits 48 to 56
			}
			makeCumSum();
		}
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage56(LongIterable sorted, int negStart) {
			super(56, negStart, Iterables.iterableSize(sorted));
			final int len = length;
			y0to31 = new int[len];
			y32to47 = new short[len];
			y48to55 = new byte[len];
			final LongIterator it = sorted.iterator();
			for (int i = 0; i < len; i++) {
				final long value = it.nextLong();
				final long index = (value >>> 56);
				counts[(int)index]++;
				y0to31[i] = (int)value;//low order 32 bit
				y32to47[i] = (short)(value >>> 32);//low order bits 32 to 47
				y48to55[i] = (byte)(value >>> 48);//low order bits 48 to 56
			}
			makeCumSum();
		}
		public int size() {
			return y0to31.length;
		}
		@Override
		protected long getLowOrderBits(int index) {
			return (0x00000000ffffffffL & y0to31[index]) | ((0x000000000000ffffL & y32to47[index]) << 32) | ((0x00000000000000ffL & y48to55[index]) << 48);
		}
		public long bytesSize() {
			return 4L*counts.length + 4L*y0to31.length + 2L*y32to47.length + y48to55.length;
		}
	}
}
