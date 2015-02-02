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
package ch.javasoft.metabolic.generate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import ch.javasoft.io.Print;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.util.numeric.DoubleUtil;

/*
SAMPLE FILE:

H-representation
begin
16 7 rational
 2  0 -1 -1  0  0 -1
 2 -1 -1  0 -1  0  0
 2  0  0  0 -1 -1 -1
 2 -1  0 -1  0 -1  0
 0  0  0  0  1  1 -1
 0 -1  0  1  0  1  0
 0  0  1  1  0  0 -1
 0 -1  1  0  1  0  0
 0  1  0 -1  0  1  0
 0  0  0  0 -1  1  1
 0  1  1  0 -1  0  0
 0  0  1 -1  0  0  1
 0  1 -1  0  1  0  0
 0  0 -1  1  0  0  1
 0  1  0  1  0 -1  0
 0  0  0  0  1 -1  1
end

 */

public class CddGenerator {
	
	public static final int PRECISION = 8;

	public static void writeIneFile(MetabolicNetwork net, File outFile) throws IOException {
		writeIneFile(StoichiometricMatrices.createStoichiometricMatrixExpandReversible(net), outFile);
	}
	
	public static void writeIneFile(double[][] stoich, File outFile) throws IOException {
		PrintWriter pw = Print.createWriter(outFile);
		pw.println("H-representation");
		pw.println("begin");
		pw.println(2 * stoich.length + " " + (stoich.length > 0 ? stoich[0].length + 1 : 1) + " rational");
		for (int row = 0; row < stoich.length; row++) {
			int sgn = 1;
			do {
				pw.print("0");
				for (int col = 0; col < (stoich.length > 0 ? stoich[0].length : 0); col++) {
					pw.print(' ');
					pw.print(doubleToString(stoich[row][col] * sgn, PRECISION));
				}
				pw.println();
				sgn = -sgn;
			}
			while (sgn == -1);
		}
		pw.println("end");
		pw.flush();
		pw.close();
	}
	
	private static String doubleToString(double orig, int precision) {
		double val = DoubleUtil.round(orig, precision);
		if (((int)val) == val) {
			return String.valueOf((int)val);
		}
		String valStr	= String.valueOf(val);
		int index		= valStr.indexOf('.');
		try {
			String intg = valStr.substring(0, index); 
			String frac = valStr.substring(index + 1);
			return intg + frac + "/" + ((long)Math.pow(10, frac.length()));			
		}
		catch (Exception ex) {
			throw new RuntimeException("cannot convert to rational: " + val + " / " + valStr, ex);
		}
	}
	
	// no instances
	private CddGenerator() {
		super();
	}

}
