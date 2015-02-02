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
package ch.javasoft.smx.ops;

import java.util.Arrays;
import java.util.BitSet;

/**
 * The <code>HslGateway</code> simplifies calls to {@link Hsl} methods and 
 * performs some error handling.
 */
public class HslGateway {
	
	public static Hsl.Result_mc66 callMc66(int m, int n, int[] irn, int[] jcn, int nblocks) {
		return callMc66(m, n, irn, jcn, nblocks, true, true);
	}
	public static Hsl.Result_mc66 callMc66(int m, int n, int[] irn, int[] jcn, int nblocks, boolean printOriginal, boolean printReordered) {
    	try {
    		Hsl.loadLibrary();
    	}
    	catch (UnsatisfiedLinkError err) {
    		System.err.println(err);
    		System.err.println("java.library.path = " + System.getProperty("java.library.path"));
    		throw err;
    	}
    	Hsl.Result_mc66 result = Hsl.mc66(m, n, Math.min(irn.length, jcn.length), irn, jcn, nblocks, 0);
    	
    	if (result.info.isOk()) {
    		resultOut(m, n, irn, jcn, result, printOriginal, printReordered);
    	}
    	else if (result.info.isWarning()) {
    		System.out.println("WARNING:");
    		System.out.println(result.info);
    		resultOut(m, n, irn, jcn, result, printOriginal, printReordered);
    	}
    	else {
    		System.err.println("ERROR:");
    		System.err.println(result.info);
    	}
    	return result;
	}
	
	private static void resultOut(int m, int n, int[] irn, int[] jcn, Hsl.Result_mc66 result, boolean printOriginal, boolean printReordered) {
		//prepare
		final BitSet[] nz = new BitSet[m];
		for (int i = 0; i < m; i++) {
			nz[i] = new BitSet();
		}
		for (int i = 0; i < Math.min(irn.length, jcn.length); i++) {
			nz[irn[i] - 1].set(jcn[i] - 1);
		}
		
		//original
		if (printOriginal) {
			System.out.println("Original:");		
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					System.out.print(nz[i].get(j) ? 'X' : '.');
				}
				System.out.println();
			}
		}
		//reordered
		if (printReordered) {
			System.out.println("Reordered:");		
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					System.out.print(nz[result.row_order[i] - 1].get(result.column_order[j] - 1) ? getBlockChar(i, j, result) : '.');
				}
				System.out.println();
			}
		}
		//other info
		System.out.println("row-order:\t" + Arrays.toString(result.row_order));
		System.out.println("col-order:\t" + Arrays.toString(result.column_order));
		System.out.println("netcut:\t" + result.netcut);
		System.out.println("rowdiff:\t" + result.rowdiff);
		//blocks
		for (int i = 0; i < result.kblocks; i++) {
			System.out.println("block " + (char)('A' + i) + ":\t[" + 
				result.rowptr[i] + ", " + result.colptr[i] + "] to [" + 
				(result.rowptr[i+1]-1) + ", " + (result.colptr[i+1] - 1) + "]");
		}
	}
	
	private static char getBlockChar(int i, int j, Hsl.Result_mc66 result) {
		int r = Arrays.binarySearch(result.rowptr, i + 1);
		int c = Arrays.binarySearch(result.colptr, j + 1);
		
		if (r < 0) r = -(r + 1) - 1;
		if (c < 0) c = -(c + 1) - 1;
		if (r >= result.rowptr.length - 1 || c >= result.colptr.length - 1) {
			return 'X';
		}
		if (r != c) {
			return 'Z';
		}
		return (char)(65 + r);
	}

}
