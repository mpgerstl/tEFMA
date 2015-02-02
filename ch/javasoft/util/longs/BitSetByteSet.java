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
 * The <code>BitSetByteSet</code> stores the byte values as bits in four long
 * values.
 */
public class BitSetByteSet extends AbstractBitSet implements ByteSet {
	
	public BitSetByteSet() {
		super(8);
	}
	
	public static void main(String[] args) {
		BitSetByteSet set = new BitSetByteSet();
		set.set(2);
		set.set(3);
		set.set(8);
		set.set(-4);
		set.set(-2);
		set.set(-1);
		for (int i = 0; i < 256; i++) {
			if (set.contains(i)) {
				System.out.println(i + ":" + set.indexOf((byte)i));
			}
		}
		System.out.println(set.indexOf(7));
		System.out.println(set.indexOf(-5));
		System.out.println(set.indexOf(-3));
		System.out.println(set.indexOf(-1));

		System.out.println();
		System.out.println(set.get(0));
		System.out.println(set.get(1));
		System.out.println(set.get(2));
		System.out.println(set.get(3));
		System.out.println(set.get(4));
		System.out.println(set.get(5));
//		System.out.println(set.get(6));
	}

}
