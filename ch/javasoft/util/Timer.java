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


public class Timer {
	private long	mStart, mEnd;
	private long[]	mIntermediates;
	private int 	mIntermediate;
	
	public Timer() {
		this(3);
	}
	public Timer(int intermediateCount) {
		mIntermediates = new long[intermediateCount];
	}
	
	public void start() {
		mStart = System.currentTimeMillis();
		mIntermediate = 0;
	}
	
	public void stop() {
		mEnd = System.currentTimeMillis();
		stopIntermediate(mEnd);
	}
	
	public void stopIntermediate() {
		stopIntermediate(System.currentTimeMillis());
	}
	private void stopIntermediate(long time) {
		if (mIntermediate == mIntermediates.length) {
			long[] intermediates = new long[mIntermediates.length * 2];
			System.arraycopy(mIntermediates, 0, intermediates, 0, mIntermediate);
			mIntermediates = intermediates;
		}
		mIntermediates[mIntermediate] = time;
		mIntermediate++;
	}
	
	public void clearIntermediates() {
		mIntermediate = 0;
	}
	
	public String getString() {
		return getString(mStart, mEnd);
	}
	public String getStringIntermediate(int index) {
		return getString(index == 0 ? mStart : mIntermediates[index - 1], mIntermediates[index]);
	}
	public String getStringLastIntermediate() {
		return getStringIntermediate(mIntermediate - 1);
	}
	public long getTime() {
		return mEnd - mStart;
	}
	public long getIntermediateTime(int index) {
		if (index == 0) return mIntermediates[index] - mStart; 
		return mIntermediates[index] - mIntermediates[index - 1];
	}
	public long getTotalIntermediateTime(int index) {
		return mIntermediates[index] - mStart; 		
	}
	private static String getString(long start, long end) {
		long delta	= end - start;
		StringBuilder sb = new StringBuilder();
		sb.insert(0, fixSizeString(delta % 1000L, 3));
		delta /= 1000L;
		if (delta == 0L) return "0." + sb.toString();
		sb.insert(0, fixSizeString(delta % 60L, 2) + ".");
		delta /= 60L;
		if (delta == 0L) return sb.toString();
		sb.insert(0, fixSizeString(delta % 60L, 2) + ":");
		delta /= 60L;
		if (delta == 0L) return sb.toString();
		sb.insert(0, delta + ":");
		return sb.toString();
	}
	private static String fixSizeString(long num, int size) {
		final String zeros = "000";
		String str = String.valueOf(num);
		return zeros.substring(0, size - str.length()) + str;
	}
	
	public static long tic() {
		return System.currentTimeMillis();
	}
	public static String toc(long tic) {
		long toc = System.currentTimeMillis();
		return getString(tic, toc);
	}
	public static long tocOut(long tic) {
		long toc = System.currentTimeMillis();
		System.out.print(getString(tic, toc));
		return toc;
	}
	public static long tocOutLn(long tic) {
		long toc = System.currentTimeMillis();
		System.out.println(getString(tic, toc));
		return toc;
	}
}
