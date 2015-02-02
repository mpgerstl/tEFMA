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
package ch.javasoft.math;

import ch.javasoft.util.IntArray;


/**
 * The <tt>Prime</tt> contains large primes for different integer sizes, and
 * functions around primes
 * 
 * see http://primes.utm.edu/lists/2small/0bit.html
 * see http://numbers.computation.free.fr/Constants/Primes/countingPrimes.html
 */
public class Prime {
	
	private static final int MAX_CACHE_SIZE = 4096;
	
	private static final long POW_2_TO_63 = (1L << 63);
	private static final long POW_2_TO_32 = (1L << 32);
	private static final int POW_2_TO_31 = (1 << 31);
	private static final int POW_2_TO_30 = (1 << 30);
	private static final int POW_2_TO_16 = (1 << 16);
	private static final int POW_2_TO_15 = (1 << 15);
	private static final int POW_2_TO_08 = (1 << 8);
	private static final int POW_2_TO_07 = (1 << 7);
	private static final int[] POW_2_TO_63_OFFSETS = {25, 165, 259, 301, 375, 387, 391, 409, 457, 471};
	private static final int[] POW_2_TO_32_OFFSETS = {5, 17, 65, 99, 107, 135, 153, 185, 209, 267};
	private static final int[] POW_2_TO_31_OFFSETS = {1, 19, 61, 69, 85, 99, 105, 151, 159, 171};
	private static final int[] POW_2_TO_30_OFFSETS = {35, 41, 83, 101, 105, 107, 135, 153, 161, 173};
	private static final int[] POW_2_TO_16_OFFSETS = {15, 17, 39, 57, 87, 89, 99, 113, 117, 123};
	private static final int[] POW_2_TO_15_OFFSETS = {19, 49, 51, 55, 61, 75, 81, 115, 121, 135};
	private static final int[] POW_2_TO_08_OFFSETS = {5, 15, 17, 23, 27, 29, 33, 45, 57, 59};
	//73     79     83     89     97    101    103    107    109    113		127
	private static final int[] POW_2_TO_07_OFFSETS = {1, 15, 21, 25, 27, 31, 39, 45, 49, 55};
	
	/**
	 * Ten largest 63 bit primes: (2^63 - k), k index of POW_2_TO_63_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_63_OFFSETS) 
	 * @return 63 bit prime 2^63 - POW_2_TO_63_OFFSETS[index]
	 */
	public static long getPrime63(int index) {
		return POW_2_TO_63 - POW_2_TO_63_OFFSETS[index];
	}
	/**
	 * Ten largest  32 bit primes: (2^32 - k), k index of POW_2_TO_32_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_32_OFFSETS) 
	 * @return 32 bit prime 2^32 - POW_2_TO_32_OFFSETS[index]
	 */
	public static long getPrime32(int index) {
		return POW_2_TO_32 - POW_2_TO_32_OFFSETS[index];
	}
	/**
	 * Ten largest  31 bit primes: (2^31 - k), k index of POW_2_TO_31_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_31_OFFSETS) 
	 * @return 31 bit prime 2^31 - POW_2_TO_31_OFFSETS[index]
	 */
	public static int getPrime31(int index) {
		return POW_2_TO_31 - POW_2_TO_31_OFFSETS[index];
	}
	/**
	 * Ten largest  30 bit primes: (2^30 - k), k index of POW_2_TO_30_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_30_OFFSETS) 
	 * @return 30 bit prime 2^30 - POW_2_TO_30_OFFSETS[index]
	 */
	public static int getPrime30(int index) {
		return POW_2_TO_30 - POW_2_TO_30_OFFSETS[index];
	}
	/**
	 * Ten largest  16 bit primes: (2^16 - k), k index of POW_2_TO_16_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_16_OFFSETS) 
	 * @return 16 bit prime 2^16 - POW_2_TO_16_OFFSETS[index]
	 */
	public static int getPrime16(int index) {
		return POW_2_TO_16 - POW_2_TO_16_OFFSETS[index];
	}
	/**
	 * Ten largest  15 bit primes: (2^15 - k), k index of POW_2_TO_15_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_15_OFFSETS) 
	 * @return 32 bit prime 2^15 - POW_2_TO_15_OFFSETS[index]
	 */
	public static int getPrime15(int index) {
		return POW_2_TO_15 - POW_2_TO_15_OFFSETS[index];
	}
	/**
	 * Ten largest  8 bit primes: (2^8 - k), k index of POW_2_TO_08_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_08_OFFSETS) 
	 * @return 8 bit prime 2^8 - POW_2_TO_08_OFFSETS[index]
	 */
	public static int getPrime08(int index) {
		return POW_2_TO_08 - POW_2_TO_08_OFFSETS[index];
	}
	/**
	 * Ten largest  7 bit primes: (2^7 - k), k index of POW_2_TO_07_OFFSETS
	 * 
	 * @param index	use 0..length(POW_2_TO_07_OFFSETS) 
	 * @return 7 bit prime 2^7 - POW_2_TO_07_OFFSETS[index]
	 */
	public static int getPrime07(int index) {
		return POW_2_TO_07 - POW_2_TO_07_OFFSETS[index];
	}
	
	/**
	 * Returns the <tt>index</tt><sup>th</sup> prime, for instance, 2 if index 
	 * is 0, 3 if index=1 and so on. The underlying algorithm is approximately
	 * linear in index.
	 *  
	 * @param index	the index of the desired prime, 0 for prime 2, 1 for prime 3
	 * 				and so on
	 * @return the <tt>index</tt><sup>th</sup> prime
	 */
	public static int getPrime(int index) {
		if (index < 0) throw new IndexOutOfBoundsException("index must be non-negative: " + index);
		if (index == 0) return 2;
		
		final int cacheSizeLog2 = Math.max(2, 32 - Integer.numberOfLeadingZeros(index) / 2);
		
		//initialize "file globals"
		//n ~= x log n <= x log (n log n)
		//for n < 2^31 : n <= x * log (2^31 * log(2^31)) <= x * 25
		final int n					= (index + 1) * 25;
		final int cacheSize			= Math.min(MAX_CACHE_SIZE, 1 << cacheSizeLog2);
		final float sq				= (float)Math.sqrt(n);
		final int[] offsetPtr 		= new int[] {-1};
		final IntArray[] uncached	= new IntArray[] {new IntArray(), new IntArray()};
		final int[] cache			= new int[cacheSize];
		
		//go
		int curIndex = 0;
		
		int prime = nextPrime(cache, uncached, offsetPtr, n, sq, 1);
		while (prime <= n) {
			curIndex++;
			if (curIndex == index) return prime;
			prime = nextPrime(cache, uncached, offsetPtr, n, sq, prime);
		}
		throw new RuntimeException("internal error: n is too small, n=" + n + ", cur prime has index " + curIndex + ", desired is " + index);
	}
	/**
	 * Returns the largest prime below n. The underlying algorithm is 
	 * approximately linear in n.
	 */
	public static int getPrimeBelow(int n) {
		if (n < 3) throw new IllegalArgumentException("no primes below " + n);
		if (n == 3) return 2;
		
		final int cacheSizeLog2 = Math.max(2, 32 - Integer.numberOfLeadingZeros(n) / 2);
		
		final int cacheSize			= Math.min(MAX_CACHE_SIZE, 1 << cacheSizeLog2);
		final float sq				= (float)Math.sqrt(n);
		final int[] offsetPtr 		= new int[] {-1};
		final IntArray[] uncached	= new IntArray[] {new IntArray(), new IntArray()};
		final int[] cache			= new int[cacheSize];
		
		//go
		int curIndex = 0;
		
		int last  = 2;
		int prime = nextPrime(cache, uncached, offsetPtr, n, sq, 1);
		while (prime < n) {
			curIndex++;
			last = prime;
			prime = nextPrime(cache, uncached, offsetPtr, n, sq, prime);
		}
		return last;
	}
	
//	2<<16
//	#define UNCACHED_SIZE 101072
//	2<<22
//	#define CACHED_SIZE		8388608
//	#define OUT_SHIFT	25

//	unsigned int* uncachedMultiple;
//	unsigned int* uncachedPrime;
//	unsigned int* cache;

//	unsigned int end;
//	float sq;
//	unsigned int	offset;
//	unsigned int	cacheSize;

	private static void put(final int[] cache, final IntArray[] uncached, int offset, int end, int multiple, int prime) {
		final int cacheSize = cache.length;
		final int prime2x = prime << 1;
		while (multiple <= end) {
			if ((multiple / cacheSize) == offset) {
				final int pos = multiple % cache.length;
				if (cache[pos] == 0) {
					cache[pos] = prime;
					return;
				}
				multiple += prime2x;
			} 
			else {
				uncached[0].add(multiple);
				uncached[1].add(prime);
				return;
			}				
		}
	}

	private static void refillCache(final int[] cache, final IntArray[] uncached, int[] offsetPtr, int end) {
		final int cacheSize = cache.length;
		final int offset = ++offsetPtr[0];
		int index = uncached[0].length() - 1;
		while (index >= 0) {
			final int mul = uncached[0].get(index);
			if ((mul / cacheSize) == offset) {
				final int prime = uncached[1].get(index);
				//remove from uncached, put to cached
				//remove by moving the last uncached to this position
				if (index == uncached[0].length() - 1) {
					uncached[0].removeLast();
					uncached[1].removeLast();
				}
				else {
					uncached[0].set(index, uncached[0].removeLast());
					uncached[1].set(index, uncached[1].removeLast());
				}
				put(cache, uncached, offset, end, mul, prime);
			}
			index--;
		}
	}

	private static boolean sieve(final int[] cache, final IntArray[] uncached, int offset, int end, int value) {
		final int pos	= value % cache.length;
		final int prime	= cache[pos];
		if (prime == 0) return false;
		cache[pos] = 0;
		put(cache, uncached, offset, end, value + (prime << 1), prime);
		return true;
	}

	private static int nextPrime(final int[] cache, final IntArray[] uncached, int[] offsetPtr, int end, float sq, int previous) {
		final int cacheSize = cache.length;
		int offset = offsetPtr[0];
		int prime = previous;
		do {
			prime += 2;
			if ((prime / cacheSize) != offset) {
				refillCache(cache, uncached, offsetPtr, end);
				offset = offsetPtr[0];
			}
		}
		while (sieve(cache, uncached, offset, end, prime));
		if (prime < sq) put(cache, uncached, offset, end, prime * prime, prime);
		return prime;
	}

	/**
	 * Counts the primes until (inclusive) till, using default cache size,
	 * tracing intermediary counts if trace is true, and printing all primes
	 * if traceAllPrimes is true. Returns the number of primes found.
	 */
	public static int countPrimes(int till, boolean trace, boolean traceAllPrimes) {
		final int cacheSize = Math.min(MAX_CACHE_SIZE, Math.max(4, 1 << (32 - Integer.numberOfLeadingZeros(till) / 2)));
		return countPrimes(till, cacheSize, trace, traceAllPrimes);
	}
	/**
	 * Counts the primes until (inclusive) till, using a cache of size cacheSize,
	 * tracing intermediary counts if trace is true, and printing all primes
	 * if traceAllPrimes is true. Returns the number of primes found.
	 */
	@SuppressWarnings("boxing")
	public static int countPrimes(int till, int cacheSize, boolean trace, boolean traceAllPrimes) {
		if (trace) System.out.printf("enumerating primes till %d\n", till);

		if (cacheSize < 3) {
			throw new IllegalArgumentException("cacheSize must be at least 3");
		}
		//initialize "file globals"
		final float sq				= (float)Math.sqrt(till);
		final int[] offsetPtr 		= new int[] {-1};
		final IntArray[] uncached	= new IntArray[] {new IntArray(), new IntArray()};
		final int[] cache			= new int[cacheSize];
		
		//go
		final long tStart = System.currentTimeMillis();
		int count	= 1;
		
		if (traceAllPrimes) System.out.printf("%d", 2);
		int last	= 2;
		int prime	= nextPrime(cache, uncached, offsetPtr, till, sq, 1);
		while (prime <= till) {
			count++;
			//output stuff
			//output stuff
			if (traceAllPrimes) {
				if (prime / 100 != last / 100) System.out.printf("\n");
				else System.out.printf(", ");				
				System.out.printf("%d", prime);				
			}
			else {
				if (trace && (prime / 1000000 != last / 1000000)) {
					System.out.printf(
						"primes till %d: %d (cache size is %d)\n", 
						(1000000 * (prime / 1000000)), (count - 1), uncached[0].length()
					);
				}				
			}
			//next
			last = prime;
			prime = nextPrime(cache, uncached, offsetPtr, till, sq, prime);
		}

		final long tEnd = System.currentTimeMillis();

		if (trace) {
			System.out.printf("\n");
			System.out.printf("cache after all: %d\n", uncached[0].length());
			System.out.printf("\n");
			System.out.printf("\n");
			System.out.printf("%d primes found in %dms.\n", count, (tEnd - tStart));
		}
		return count;
	}

}
