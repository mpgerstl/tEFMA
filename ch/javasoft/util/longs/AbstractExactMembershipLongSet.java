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

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

import ch.javasoft.util.LongArray;
import ch.javasoft.util.Unsigned;

/**
 * The <code>AbstractExactMembershipLongSet</code> is a unmodifiable sorted set 
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
 * <b>References</b>
 * <br>
 * [1] "Exact Membership Tester 2 and 3" 
 *     in "Exact and approximate membership testers" 
 *     of Larry Carter, Robert Floyd, George Markowsky and Mark Wegman.
 */
public abstract class AbstractExactMembershipLongSet extends AbstractSortedLongSet implements LongList {
	
	/**
	 * Returns the underlying storage model.
	 */
	abstract protected Storage getStorage();
	
	public int size() {
		return getStorage().size();
	}
	
	public long getLong(int index) {
		final Storage storage = getStorage();
		final int unsignedIndex = storage.convertSignedToUnsignedIndex(index);
		return storage.get(unsignedIndex);
	}
	
	public LongListIterator listIterator(int index) {
		return listIterator(index, size());
	}
	public LongListIterator listIterator(int start, int end) {
		return getStorage().listIterator(start, end);
	}
	
	public Long get(int index) {
		return Long.valueOf(getLong(index));
	}
	
	public int indexOf(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return indexOfLong(((Number)o).longValue());
		}
		return -1;
	}
	public int lastIndexOf(Object o) {
		if (o instanceof Long || o instanceof Integer || o instanceof Short || o instanceof Byte) {
			return lastIndexOfLong(((Number)o).longValue());
		}
		return -1;
	}
	public int lastIndexOfLong(long value) {
		return indexOfLong(value);//since we are a set, this is the same
	}
	public int indexOfLong(long value) {
		final Storage storage = getStorage();
		final int unsignedIndex = storage.indexOf(value);
		return unsignedIndex < 0 ? -1 : storage.convertUnsignedToSignedIndex(unsignedIndex);
	}
	
    /**
     * Searches for the specified value using the binary search algorithm.
     *
     * @param value	the value to be searched for
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
	public int binarySearch(long value) {
		final Storage storage = getStorage();
		final int unsignedIndex = storage.indexOf(value);
		return unsignedIndex >= 0 ? storage.convertSignedToUnsignedIndex(unsignedIndex) : -storage.convertUnsignedToSignedIndex(-unsignedIndex-1)-1;
	}
	
	@Override
	public boolean containsLong(long value) {
		return indexOfLong(value) != -1;
	}
	
	public LongList subList(final int fromIndex, final int toIndex) {
		return new AbstractLongList() {
			public LongList subList(int newfromIndex, int newtoIndex) {
				return AbstractExactMembershipLongSet.this.subList(fromIndex+newfromIndex, Math.min(fromIndex+newtoIndex, toIndex));
			}
			public LongListIterator listIterator(int start, int end) {
				return AbstractExactMembershipLongSet.this.listIterator(fromIndex+start, Math.min(fromIndex+end, toIndex));
			}
			public long getLong(int index) {
				return AbstractExactMembershipLongSet.this.getLong(fromIndex + index);
			}
			public int size() {
				return toIndex - fromIndex;
			}
			// modifying operations, unsupported
			public int setLong(int index, long value) {
				throw new UnsupportedOperationException("unmodifiable set");
			}
			public long removeLongAt(int index) {
				throw new UnsupportedOperationException("unmodifiable set");
			}
			public boolean addLong(int index, long value) {
				throw new UnsupportedOperationException("unmodifiable set");
			}
			public void clear() {
				throw new UnsupportedOperationException("unmodifiable set");
			}
		};
	}
	
	public SortedLongSet subSet(long fromElement, long toElement) {
		throw new UnsupportedOperationException("not implemented");
	}
	
	public Comparator<? super Long> comparator() {
		return null;
	}
	
	public LongIterator iterator() {
		return listIterator();
	}
	
	public LongListIterator listIterator() {
		return listIterator(0, size());
	}
	
	/**
	 * Returns a set with the long values common to {@code this} set and 
	 * {@code with}.
	 * 
	 * @param with the set to intersect with
	 * @return	the set containing elements which are contained in {@code this}
	 * 			set as well as in the {@code with} set
	 */
	abstract public AbstractExactMembershipLongSet intersect(AbstractExactMembershipLongSet with);
	
	/**
	 * Returns the total number of bytes used to store the values. Constant
	 * parts such as size value or array length field are omitted.
	 * 
	 * @return the number of bytes used to store the values
	 */
	public long bytesSize() {
		return getStorage().bytesSize();
	}
	
	
	///////////////////////// modifying operations throw an exception

	public boolean addLong(long value) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	public boolean addLong(int index, long value) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	public void add(int index, Long element) {
		throw new UnsupportedOperationException("unmodifiable set");
	}

	public boolean addAll(int index, Collection<? extends Long> c) {
		throw new UnsupportedOperationException("unmodifiable set");
	}

	public int setLong(int index, long value) {
		throw new UnsupportedOperationException("unmodifiable set");
	}

	public boolean removeLong(long value) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	public Long set(int index, Long element) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	public long removeLongAt(int index) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	public Long remove(int index) {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	public void clear() {
		throw new UnsupportedOperationException("unmodifiable set");
	}
	
	///////////////////////// internal classes and interfaces
	
	/**
	 * The <code>Storage</code> implementation performs the actual storage of
	 * the long values. Depending on the number {@code n}, usually the length
	 * of the value array, the {@code k} parameter is chosen differently 
	 * indicating how many bits should be stored in a bit string form, and how    
	 * many in raw form. Each implementation of this class stands for a separate
	 * {@code k} value.
	 */
	protected static interface Storage {
		/**
		 * The number of long values stored here
		 */
		int size();
		/**
		 * Returns the number of low-order bits
		 */
		int getK();
		/**
		 * Returns the index of the first negative number, or {@link #size()}  
		 * if all values are nonnegative
		 * @return 	the index of the first negative number, or {@code #size()} 
		 * 			if all values are nonnegative
		 */
		int indexOfFirstNegativeValue();
		/**
		 * Converts an index referring to a sorted array using signed comparison 
		 * into an referring to a list containing the same numbers but sorted
		 * with unsigned comparison
		 * 
		 * @param signedIndex	the index in a signed sorted list
		 * @return the index in an unsigned sorted list
		 */
		int convertSignedToUnsignedIndex(int signedIndex);
		/**
		 * Converts an index referring to a sorted array using unsigned 
		 * comparison into an referring to a list containing the same numbers 
		 * but sorted with signed comparison
		 * 
		 * @param unsignedIndex	the index in a unsigned sorted list
		 * @return the index in an signed sorted list
		 */
		int convertUnsignedToSignedIndex(int unsignedIndex);
		/**
		 * Returns a long value by index. The index is also the mean index of 
		 * the unsigned value, meaning that {@code get(k)} returns the 
		 * <code>k<sup>th</sup> smallest value according to unsigned long
		 * comparison.  
		 * 
		 * @param index the list or unsigned mean index of the desired value
		 * @return the value at the specified position
		 */
		long get(int index);
	    /**
	     * Searches for the specified value using the binary search algorithm 
	     * and treating all values as unsigned long values. The index, if it is 
	     * found, is unique since this is a set, and it reflects the index in a 
	     * ascending unsigned sorted set. 
	     *
	     * @param value	the value to be searched for
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
		int indexOf(long value);
		/**
		 * Returns a list iterator to iterate all long values from {@code start} 
		 * (inclusive) to {@code end} (exclusive)
		 * @param start	the index of the first value, inclusive 
		 * @param end	the index of the last value, exclusive 
		 */
		LongListIterator listIterator(int start, int end);
		/**
		 * Returns a storage object with the long values common to {@code this} 
		 * and {@code with}.
		 * 
		 * @param with the storage object to intersect with
		 * @return	the storage object containing elements which are contained 
		 * 			in {@code this} as well as in {@code with}
		 */
		Storage intersect(Storage with);
		/**
		 * Returns the total number of bytes used to store the values. Constant
		 * parts such as size value or array length field are omitted.
		 * 
		 * @return the number of bytes used to store the values
		 */
		long bytesSize();
	}
	protected abstract static class AbstractStorage implements Storage {
		private final int negStart;
		private final int k;
		/**
		 * Constructor with index of first nonnegative number
		 * @param k			the number of low-order bits 
		 * @param negStart	the index of the first negative number, or the 
		 * 					total number of values if all numbers are 
		 * 					nonnegative
		 */
		public AbstractStorage(int k, int negStart) {
			this.k 			= k;
			this.negStart 	= negStart;
		}
		public int getK() {
			return k;
		}
		public int indexOfFirstNegativeValue() {
			return negStart;
		}
		public int convertSignedToUnsignedIndex(int signedIndex) {
			return (negStart + signedIndex) % size();
		}
		public int convertUnsignedToSignedIndex(int unsignedIndex) {
			final int size = size();
			return (unsignedIndex + negStart + size) % size;
		}		
		public LongListIterator listIterator(final int start, final int end) {
			return new AbstractLongListIterator() {
				int current = start;				
				public long previousLong() {
					if (current <= start) throw new NoSuchElementException();
					return get(convertSignedToUnsignedIndex(--current));
				}
				public int previousIndex() {
					return current <= start ? -1 : current;
				}
				
				public long nextLong() {
					if (current >= end) throw new NoSuchElementException();
					return get(convertSignedToUnsignedIndex(current++));
				}
				
				public int nextIndex() {
					return current < end ? current : end;
				}
				
				public boolean hasPrevious() {
					return current > start;
				}
				
				public boolean hasNext() {
					return current < end;
				}				
			};
		}
	}
	/**
	 * <code>Storage64</code> simply stores the sorted long values.
	 */
	protected static class Storage64 extends AbstractStorage {
		private final long[] y;
		/**
		 * Constructor with unsigned sorted values and index of first negative 
		 * number
		 * 
		 * @param sorted	the values, sorted ascending unsigned
		 * @param negStart	the index of the first negative number, or length 
		 * 					of the {@code sorted} array if all numbers are 
		 * 					nonnegative
		 */
		public Storage64(long[] sorted, int negStart) {
			super(64, negStart);
			this.y = sorted;
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
		public Storage64(LongIterable sorted, int negStart) {
			super(64, negStart);
			final LongArray arr = sorted instanceof Collection ? new LongArray(((Collection)sorted).size()) : new LongArray();
			final LongIterator it = sorted.iterator();
			while (it.hasNext()) {
				arr.add(it.nextLong());
			}
			this.y = arr.yieldArray();
		}
		
		public int size() {
			return y.length;
		}
		public long get(int index) {
			return y[index];
		}
		public int indexOf(long value) {
			return Unsigned.binarySearch(y, 0, y.length, value);
		}
		/**
		 * Performs a merge to intersect the sets 
		 */
		public Storage64 intersect(Storage with) {
			final int mylen = y.length;
			final int otlen = with.size();
			final LongArray inter = new LongArray();
			int i = 0; int j = 0; int n = -1;
			while (i < mylen && j < otlen) {
				final long me = y[i];
				final long ot = with.get(j);
				final int cmp = Unsigned.compare(me, ot);
				if (cmp < 0) {
					i++;
				}
				else if (cmp > 0) {
					j++;
				}
				else {
					if (n < 0 && me < 0) {
						n = inter.length();
					}
					inter.add(me);
					i++;
					j++;
				}
			}
			return new Storage64(inter.toArray(), n < 0 ? inter.length() : n);
		}
		public long bytesSize() {
			return 8L*y.length;
		}
	}
	
}
