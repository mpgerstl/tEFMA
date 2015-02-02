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

/**
 * The <code>AbstractBitSet</code> stores integer values as bits in a long 
 * array.
 */
public class AbstractBitSet {
	
	private final int mask;
	private final long[] units;
	
	public AbstractBitSet(int bits) {
		mask = 0xffffffff >>> (32-bits);
		units = new long[1 << (bits-6)];//2^bits/64 = 2^(bits-6)
	}
	
	protected int getMask() {
		return mask;
	}

	public void set(int value) {
		final int mvalue = mask & value;
		final long mask = 1L << (mvalue & 0x3f /*mod 64*/);
		units[mvalue>>6 /*div 64*/] |= mask;
	}
	public boolean contains(int value) {
		final int mvalue = mask & value;
		final long mask = 1L << (mvalue & 0x3f /*mod 64*/);
		return 0 != (units[mvalue>>6 /*div 64*/] & mask);
	}

	public int indexOf(int value) {
		final int mvalue = mask & value;
		final int div64 = mvalue >> 6;
		final int mod64 = mvalue & 0x3f;
		
		int count = 0;
		for (int i = 0; i < div64; i++) {
			count += Long.bitCount(units[i]);
		}
		final long bitMask = 1L << mod64;
		final long andMask = 0xffffffffffffffffL >>> (64 - mod64);
		final long curValue = units[div64];
		count += Long.bitCount(curValue & andMask);//the ones before our bit
		return 0 != (curValue & bitMask) ? count : -count-1;
	}
	public int get(int index) {
		int unit  = 0;
		int count = Long.bitCount(units[unit]);
		int before = 0;
		while (before + count <= index && unit+1 < units.length) {
			before += count;
			unit++;
			count = Long.bitCount(units[unit]);
		}
		if (before + count <= index || index < 0) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
		long cur = units[unit];
		int clear = index - before;
		for (int i = 0; i < clear; i++) {
			cur ^= Long.lowestOneBit(cur);
		}
		return (unit << 6 /*mul 64*/) + Long.numberOfTrailingZeros(cur);
	}
	
	public long byteLength() {
		return 8L*units.length;
	}

}
