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
package ch.javasoft.util;

import java.util.Random;


public class Sort {
	
	private static final int maxQuiSort =  512;//best: ~maxCpySort/(4-8)
	private static final int maxCpySort = 2048;
//	private static final int maxQuiSort = 4096;//best: ~maxCpySort/(4-8)
//	private static final int maxCpySort = 8192;
	
	/**
	 * A combination of counter sort with swapping (non-stable, in place)
	 * and counter sort with copy (stable, not in place) and quick sort.
	 */
	public static void sort(int[] arr) {
		sort(arr, maxQuiSort, maxCpySort);
	}
	/**
	 * Counter sort with swapping (non-stable, in place)
	 */
	public static void sortCountSwap(int[] arr) {
		sort(arr, 0, 0);
	}
	/**
	 * Counter sort with copying (stable, not in place)
	 */
	public static void sortCountCopy(int[] arr) {
		sort(arr, 0, Integer.MAX_VALUE);
	}
	/**
	 * Pure quick sort as used in combi sort
	 */
	public static void sortQuick(int[] arr) {
		sort(arr, Integer.MAX_VALUE, 0);
	}
	private static void sort(int[] arr, final int maxQuiSort, final int maxCpySort) {
		int andNeg = 0xffffffff;
		int andPos = 0xffffffff;
		int orNeg  = 0;
		int orPos  = 0;
		//separate positive and negative values, neg first
		int start	= 0;
		int end		= arr.length;
		while (start < end) {
			if (arr[start] < 0) {
				andNeg &= arr[start];
				orNeg  |= arr[start];
				start++;
			}
			if (arr[end - 1] >= 0) {
				end--;
				andPos &= arr[end];
				orPos  |= arr[end];
			}
			if (start < end && arr[start] >= 0 && arr[end - 1] < 0) {
				//swap
				end--;
				int tmp		= arr[end];
				arr[end]	= arr[start];
				arr[start]	= tmp;

				andNeg &= arr[start];
				orNeg  |= arr[start];
				andPos &= arr[end];
				orPos  |= arr[end];
				
				start++;
			}
		}
		final int xorNeg = andNeg ^ orNeg;
		final int xorPos = andPos ^ orPos;
		//counter sort neg
		if (start > 1) {
			if (start < maxQuiSort) {
				java.util.Arrays.sort(arr, 0, start);
			}
			else if (start < maxCpySort) {
				sort00to07cpy(arr, 0, start, xorNeg);
			}
			else {
				sort30to24(arr, 0, start, true, xorNeg);
			}
		}
		//counter sort pos
		if (arr.length - start > 1) {
			if (arr.length - start < maxQuiSort) {
				java.util.Arrays.sort(arr, start, arr.length);
			}
			else if (arr.length - start < maxCpySort) {
				sort00to07cpy(arr, start, arr.length, xorPos);
			}
			else {
				sort30to24(arr, start, arr.length, false, xorPos);
			}			
		}
	}
	@SuppressWarnings("unused")
	private static void sort30to20(int[] arr, int start, int end, boolean neg) {
		final int[] counts = new int[2048];//11 bit
		//most significant 11 bits without sign bit: 20 - 30
		for (int ii = start; ii < end; ii++) {
			int pos = (arr[ii] >> 20) & 0x000007ff;
			counts[pos]++;
		}
		counts[0] += start;
		for (int ii = 1; ii < 2048 /*11 bit*/; ii++) {
			counts[ii] = counts[ii] + counts[ii - 1];
		}
		final int[] ccounts = counts.clone();
		int index = end - 1;
		while (index >= start) {
			if ((arr[index] < 0) != neg) {
				arr[index] = arr[index] ^ 0x80000000;
				index--;
			}
			else {
				int pos = (arr[index] >> 20) & 0x000007ff;
				counts[pos]--;
				if (counts[pos] < index) {
					int tmp = arr[counts[pos]];
					arr[counts[pos]] = arr[index] ^ 0x80000000;
					arr[index] = tmp;
				}
				else {
					arr[counts[pos]] = arr[index];
					if (counts[pos] == index) index--;
				}
			}
		}
		//invoke next step
		for (int ii = 0; ii < 2048 /*11 bit*/; ii++) {
			int pStart	= ii == 0 ? start : ccounts[ii - 1];
			int pEnd	= ccounts[ii];
			if (pEnd - pStart > 1) sort19to10(arr, pStart, pEnd, neg);
			if (pEnd == end) break;
		}
	}
	private static void sort19to10(int[] arr, int start, int end, boolean neg) {
		final int[] counts = new int[1024];//10 bit
		//bits: 10 - 19, i.e. 0x000ffc00
		for (int ii = start; ii < end; ii++) {
			int pos = (arr[ii] >> 10) & 0x000003ff;
			counts[pos]++;
		}
		counts[0] += start;
		for (int ii = 1; ii < 1024 /*10 bit*/; ii++) {
			counts[ii] = counts[ii] + counts[ii - 1];
		}
		final int[] ccounts = counts.clone();
		int index = end - 1;
		while (index >= start) {
			if ((arr[index] < 0) != neg) {
				arr[index] = arr[index] ^ 0x80000000;
				index--;
			}
			else {
				int pos = (arr[index] >> 10) & 0x000003ff;
				counts[pos]--;
				if (counts[pos] < index) {
					int tmp = arr[counts[pos]];
					arr[counts[pos]] = arr[index] ^ 0x80000000;
					arr[index] = tmp;					
				}
				else {
					arr[counts[pos]] = arr[index];
					if (counts[pos] == index) index--;
				}
			}
		}
		//invoke next step
		for (int ii = 0; ii < 1024 /*10 bit*/; ii++) {
			int pStart	= ii == 0 ? start : ccounts[ii - 1];
			int pEnd	= ccounts[ii];
			if (pEnd - pStart > 1) sort09to00(arr, pStart, pEnd, neg);
			if (pEnd == end) break;
		}
	}
	private static void sort09to00(int[] arr, int start, int end, boolean neg) {
		final int[] counts = new int[1024];//10 bit
		//bits: 00 - 09, i.e. 0x000003ff
		for (int ii = start; ii < end; ii++) {
			int pos = arr[ii] & 0x000003ff;
			counts[pos]++;
		}
		counts[0] += start;
		for (int ii = 1; ii < 1024 /*10 bit*/; ii++) {
			counts[ii] = counts[ii] + counts[ii - 1];
		}
		int index = end - 1;
		while (index >= start) {
			if ((arr[index] < 0) != neg) {
				arr[index] = arr[index] ^ 0x80000000;
				index--;
			}
			else {
				int pos = arr[index] & 0x000003ff;
				counts[pos]--;
				if (counts[pos] < index) {
					int tmp = arr[counts[pos]];
					arr[counts[pos]] = arr[index] ^ 0x80000000;
					arr[index] = tmp;					
				}
				else {
					arr[counts[pos]] = arr[index];
					if (counts[pos] == index) index--;
				}
			}
		}
	}
	private static void sort30to24(int[] arr, int start, int end, boolean neg, int xor) {
		if ((xor & 0xff000000) != 0) {
			int and = 0xffffffff;
			int or  = 0;
			final int[] counts = new int[128];//7 bit
			//most significant 7 bits without sign bit: 24 - 30
			for (int ii = start; ii < end; ii++) {
				int pos = (arr[ii] >> 24) & 0x0000007f;
				counts[pos]++;
				and &= arr[ii];
				or  |= arr[ii];
			}
			xor = and^or;
			counts[0] += start;
			for (int ii = 1; ii < 128 /*7 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			final int[] ccounts = counts.clone();
			int index = end - 1;
			while (index >= start) {
				if ((arr[index] < 0) != neg) {
					arr[index] = arr[index] ^ 0x80000000;
					index--;
				}
				else {
					int pos = (arr[index] >> 24) & 0x0000007f;
					counts[pos]--;
					if (counts[pos] < index) {
						int tmp = arr[counts[pos]];
						arr[counts[pos]] = arr[index] ^ 0x80000000;
						arr[index] = tmp;
					}
					else {
						arr[counts[pos]] = arr[index];
						if (counts[pos] == index) index--;
					}
				}
			}
			//invoke next step
			for (int ii = 0; ii < 128 /*7 bit*/; ii++) {
				int pStart	= ii == 0 ? start : ccounts[ii - 1];
				int pEnd	= ccounts[ii];
				if (pEnd - pStart > 1) {
					if (pEnd - pStart < maxQuiSort) {
						java.util.Arrays.sort(arr, pStart, pEnd);
					}
					else if (pEnd - pStart < maxCpySort) {
						sort00to07cpy(arr, pStart, pEnd, xor & 0x00ffffff);
					}
					else {
						sort23to16(arr, pStart, pEnd, neg, xor & 0x00ffffff);
					}			
				}
				if (pEnd == end) break;
			}
		}
		else {
			sort23to16(arr, start, end, neg, xor & 0x00ffffff);
		}
	}
	private static void sort23to16(int[] arr, int start, int end, boolean neg, int xor) {
		if ((xor & 0x00ff0000) != 0) {
			int and = 0xffffffff;
			int or  = 0;
			final int[] counts = new int[256];//8 bit
			//bits: 16-23
			for (int ii = start; ii < end; ii++) {
				int pos = (arr[ii] >> 16) & 0x000000ff;
				counts[pos]++;
				and &= arr[ii];
				or  |= arr[ii];
			}
			xor = and^or;
			counts[0] += start;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			final int[] ccounts = counts.clone();
			int index = end - 1;
			while (index >= start) {
				if ((arr[index] < 0) != neg) {
					arr[index] = arr[index] ^ 0x80000000;
					index--;
				}
				else {
					int pos = (arr[index] >> 16) & 0x000000ff;
					counts[pos]--;
					if (counts[pos] < index) {
						int tmp = arr[counts[pos]];
						arr[counts[pos]] = arr[index] ^ 0x80000000;
						arr[index] = tmp;					
					}
					else {
						arr[counts[pos]] = arr[index];
						if (counts[pos] == index) index--;
					}
				}
			}
			//invoke next step
			for (int ii = 0; ii < 256 /*8 bit*/; ii++) {
				int pStart	= ii == 0 ? start : ccounts[ii - 1];
				int pEnd	= ccounts[ii];
				if (pEnd - pStart > 1) {
					if (pEnd - pStart < maxQuiSort) {
						java.util.Arrays.sort(arr, pStart, pEnd);
					}
					else if (pEnd - pStart < maxCpySort) {
						sort00to07cpy(arr, pStart, pEnd, xor & 0x0000ffff);
					}
					else {
						sort15to08(arr, pStart, pEnd, neg, xor & 0x0000ffff);
					}			
				}
				if (pEnd == end) break;
			}
		}
		else {
			sort15to08(arr, start, end, neg, xor & 0x0000ffff);
		}
	}
	private static void sort15to08(int[] arr, int start, int end, boolean neg, int xor) {
		if ((xor & 0x0000ff00) != 0) {
			int and = 0xffffffff;
			int or  = 0;
			final int[] counts = new int[256];//8 bit
			//bits: 08-15
			for (int ii = start; ii < end; ii++) {
				int pos = (arr[ii] >> 8) & 0x000000ff;
				counts[pos]++;
				and &= arr[ii];
				or  |= arr[ii];
			}
			xor = and^or;
			counts[0] += start;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			final int[] ccounts = counts.clone();
			int index = end - 1;
			while (index >= start) {
				if ((arr[index] < 0) != neg) {
					arr[index] = arr[index] ^ 0x80000000;
					index--;
				}
				else {
					int pos = (arr[index] >> 8) & 0x000000ff;
					counts[pos]--;
					if (counts[pos] < index) {
						int tmp = arr[counts[pos]];
						arr[counts[pos]] = arr[index] ^ 0x80000000;
						arr[index] = tmp;					
					}
					else {
						arr[counts[pos]] = arr[index];
						if (counts[pos] == index) index--;
					}
				}
			}
			//invoke next step
			for (int ii = 0; ii < 256 /*8 bit*/; ii++) {
				int pStart	= ii == 0 ? start : ccounts[ii - 1];
				int pEnd	= ccounts[ii];
				if (pEnd - pStart > 1) {
					if (pEnd - pStart < maxQuiSort) {
						java.util.Arrays.sort(arr, pStart, pEnd);
					}
					else if (pEnd - pStart < maxCpySort) {
						sort00to07cpy(arr, pStart, pEnd, xor & 0x000000ff);
					}
					else {
						sort07to00(arr, pStart, pEnd, neg, xor & 0x000000ff);
					}			
				}
				if (pEnd == end) break;
			}
		}
		else {
			sort07to00(arr, start, end, neg, xor & 0x000000ff);
		}
	}
	private static void sort07to00(int[] arr, int start, int end, boolean neg, int xor) {
		if ((xor & 0x000000ff) != 0) {
			final int[] counts = new int[256];//8 bit
			//bits: 00 - 07
			for (int ii = start; ii < end; ii++) {
				int pos = arr[ii] & 0x000000ff;
				counts[pos]++;
			}
			counts[0] += start;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			int index = end - 1;
			while (index >= start) {
				if ((arr[index] < 0) != neg) {
					arr[index] = arr[index] ^ 0x80000000;
					index--;
				}
				else {
					int pos = arr[index] & 0x000000ff;
					counts[pos]--;
					if (counts[pos] < index) {
						int tmp = arr[counts[pos]];
						arr[counts[pos]] = arr[index] ^ 0x80000000;
						arr[index] = tmp;					
					}
					else {
						arr[counts[pos]] = arr[index];
						if (counts[pos] == index) index--;
					}
				}
			}
		}
	}
	private static void sort00to07cpy(int[] arr, int start, int end, int xor) {
		final int[] dst = new int[end - start];
		if (1 == sort00to07cpy(arr, dst, start, end, 0, end - start, xor)) {
			//the sorted result is in the dst array --> copy it back
			System.arraycopy(dst, 0, arr, start, end - start);
		}
	}
	//returns 0 if result is in src, 1 if it is in dst
	private static int sort00to07cpy(int[] src, int[] dst, int srcStart, int srcEnd, int dstStart, int dstEnd, int xor) {		
		if ((xor & 0x000000ff) != 0) {			
			final int[] counts = new int[256];//8 bit
			//most significant 7 bits without sign bit: 24 - 30
			for (int ii = srcStart; ii < srcEnd; ii++) {
				int pos = src[ii] & 0x000000ff;
				counts[pos]++;
			}
			counts[0] += dstStart;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			for (int i = srcEnd - 1; i >= srcStart; i--) {
				int pos = src[i] & 0x000000ff;
				counts[pos]--;
				dst[counts[pos]] = src[i];
			}
			return (1 + sort08to17cpy(dst, src, dstStart, dstEnd, srcStart, srcEnd, xor)) % 2;			
		}
		return sort08to17cpy(src, dst, srcStart, srcEnd, dstStart, dstEnd, xor);			
	}
	@SuppressWarnings("unused")
	private static void sort08to17cpy(int[] arr, int start, int end, int xor) {
		final int[] dst = new int[end - start];
		if (1 == sort08to17cpy(arr, dst, start, end, 0, end - start, xor)) {
			//the sorted result is in the dst array --> copy it back
			System.arraycopy(dst, 0, arr, start, end - start);
		}
	}		
	private static int sort08to17cpy(int[] src, int[] dst, int srcStart, int srcEnd, int dstStart, int dstEnd, int xor) {
		if ((xor & 0x0000ff00) != 0) {			
			final int[] counts = new int[256];//8 bit
			//most significant 7 bits without sign bit: 24 - 30
			for (int ii = srcStart; ii < srcEnd; ii++) {
				int pos = (src[ii] >> 8) & 0x000000ff;
				counts[pos]++;
			}
			counts[0] += dstStart;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			for (int i = srcEnd - 1; i >= srcStart; i--) {
				int pos = (src[i] >> 8) & 0x000000ff;
				counts[pos]--;
				dst[counts[pos]] = src[i];
			}
			return (1 + sort16to23cpy(dst, src, dstStart, dstEnd, srcStart, srcEnd, xor)) % 2;			
		}
		return sort16to23cpy(src, dst, srcStart, srcEnd, dstStart, dstEnd, xor);			
	}
	@SuppressWarnings("unused")
	private static void sort16to23cpy(int[] arr, int start, int end, int xor) {
		final int[] dst = new int[end - start];
		if (1 == sort16to23cpy(arr, dst, start, end, 0, end - start, xor)) {
			//the sorted result is in the dst array --> copy it back
			System.arraycopy(dst, 0, arr, start, end - start);
		}
	}
	private static int sort16to23cpy(int[] src, int[] dst, int srcStart, int srcEnd, int dstStart, int dstEnd, int xor) {
		if ((xor & 0x00ff0000) != 0) {
			final int[] counts = new int[256];//8 bit
			//most significant 7 bits without sign bit: 24 - 30
			for (int ii = srcStart; ii < srcEnd; ii++) {
				int pos = (src[ii] >> 16) & 0x000000ff;
				counts[pos]++;
			}
			counts[0] += dstStart;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			for (int i = srcEnd - 1; i >= srcStart; i--) {
				int pos = (src[i] >> 16) & 0x000000ff;
				counts[pos]--;
				dst[counts[pos]] = src[i];
			}
			return (1 + sort24to30cpy(dst, src, dstStart, dstEnd, srcStart, srcEnd, xor)) % 2;			
		}
		return sort24to30cpy(src, dst, srcStart, srcEnd, dstStart, dstEnd, xor);			
	}
	@SuppressWarnings("unused")
	private static void sort24to30cpy(int[] arr, int start, int end, int xor) {
		final int[] dst = new int[end - start];
		if (1 == sort24to30cpy(arr, dst, start, end, 0, end - start, xor)) {
			//the sorted result is in the dst array --> copy it back
			System.arraycopy(dst, 0, arr, start, end - start);
		}		
	}
	private static int sort24to30cpy(int[] src, int[] dst, int srcStart, int srcEnd, int dstStart, int dstEnd, int xor) {
		if ((xor & 0xff000000) != 0) {
			final int[] counts = new int[256];//8 bit
			//most significant 7 bits without sign bit: 24 - 30
			for (int ii = srcStart; ii < srcEnd; ii++) {
				int pos = (src[ii] >> 24) & 0x000000ff;
				counts[pos]++;
			}
			counts[0] += dstStart;
			for (int ii = 1; ii < 256 /*8 bit*/; ii++) {
				counts[ii] = counts[ii] + counts[ii - 1];
			}
			for (int i = srcEnd - 1; i >= srcStart; i--) {
				int pos = (src[i] >> 24) & 0x000000ff;
				counts[pos]--;
				dst[counts[pos]] = src[i];
			}
			return 1;			
		}
		return 0;			
	}
	
	/**
	 * Sorts like quicksort, but instead of choosing a median pivot, the bits
	 * are taken to split into two groups 
	 * 
	 * FIXME still buggy
	 */
	public static void bitQuickSort(int[] arr) {
		int and = 0xffffffff;
		int or  = 0;
		for (int i = 0; i < arr.length; i++) {
			and &= arr[i];
			or  |= arr[i];
		}
		final int bit = 31 - Integer.numberOfLeadingZeros(and^or);
		if (bit >= 0) {
			bitQuickSort(arr, 0, arr.length, bit);
		}
	}
	private static void bitQuickSort(int[] arr, int start, int end, int bit) {
		final int mask = 0xffffffff >>> (32 - bit);
		int and = 0xffffffff;
		int or  = 0;
		final int pBit = 1 << bit;
		final int eBit = bit == 31 ? 0 : pBit;
		int lastStart  = start;
		int lastEnd	   = end;
		while (start < end) {
			if ((arr[start] & pBit) != eBit) {
				and &= arr[start];
				or  |= arr[start];			
				start++;
			}
			if ((arr[end - 1] & pBit) == eBit) {
				end--;
				and &= arr[end];
				or  |= arr[end];			
			}
			if (start < end && (arr[start] & pBit) == eBit && (arr[end - 1] & pBit) != eBit) {
				end--;					
				int tmp		= arr[end];
				arr[end]	= arr[start];
				arr[start]	= tmp;
				
				and &= arr[start];
				or  |= arr[start];			
				and &= arr[end];
				or  |= arr[end];			
				
				start++;
			}
		}
		if(start != end) throw new RuntimeException();
		bit = 31 - Integer.numberOfLeadingZeros(mask & (and^or));
		if (bit >= 0) {
			if (lastStart < start) bitQuickSort(arr, lastStart, start, bit);			
			if (start < lastEnd) bitQuickSort(arr, start, lastEnd, bit);			
		}
	}
	public static void main(String[] args) {
		final int cnt = 10000000;
		final int dif = 1000;
		final Random rnd = new Random();
		int[] templates = new int[dif];
		for (int i = 0; i < templates.length; i++) {
			templates[i] = rnd.nextInt();
		}
		int[] vals = new int[cnt];
		for (int ii = 0; ii < vals.length; ii++) {
//			vals[ii] = templates[rnd.nextInt(templates.length)];
//			vals[ii] = rnd.nextInt(dif)-dif/2;
//			vals[ii] = rnd.nextInt(Integer.MAX_VALUE);
//			vals[ii] = rnd.nextInt(dif);
			vals[ii] = rnd.nextInt();
		}
		Timer timer = new Timer();
		timer.start();
//		sort(vals);//
		java.util.Arrays.sort(vals);//System.out.println(java.util.Arrays.toString(sort(vals)));
//		bitQuickSort(vals);
		timer.stop();
		System.out.println(timer.getString());
		for (int ii = 1; ii < vals.length; ii++) {
			if (vals[ii] < vals[ii - 1]) throw new InternalError("wrong: " + vals[ii-1] + ", " + vals[ii]);
		}
		//System.out.println(java.util.Arrays.toString(vals));
	}

	//no instances
	private Sort() {
		super();
	}
}
