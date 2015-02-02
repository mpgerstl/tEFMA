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
package ch.javasoft.smx.util;

import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.util.numeric.DoubleUtil;

public class SmxDoubleUtil {
	
    public static DoubleMatrix roundMatrix(ReadableDoubleMatrix mx, int precision) {
        DoubleMatrix result = mx.toDoubleMatrix(true);
        for (int row = 0; row < result.getRowCount(); row++) {
            for (int col = 0; col < result.getColumnCount(); col++) {
                result.setValueAt(row, col, DoubleUtil.round(result.getDoubleValueAt(row, col), precision));
            }
        }
        return result;
    }
    
//    private static final long[] TEN_POWS = initPrecisionPows();    
//    private static long[] initPrecisionPows() {
//        //Long.MAX_VALUE=9223372036854775807, which has 19 digits and is somewhat smaller than 10^19
//        long[] ret = new long[19];
//        ret[0] = 1;
//        for (int ii = 1; ii < ret.length; ii++) {
//            ret[ii] = 10 * ret[ii - 1];
//            //System.out.println("10^" + ii + "=" + ret[ii]);
//        }
//        return ret;
//    }
//    
//    public static void main(String[] args) {
////        System.out.println((byte)127);
////        System.out.println((byte)128);
////        System.out.println((byte)255);
////        System.out.println((byte)256);
////        
////        System.out.println((char)127);
////        System.out.println((char)128);
////        System.out.println((char)127 + (char)1);
////        System.out.println((char)255);
////        System.out.println((char)256);
//
//        System.out.println(round(4.999999999999999, 5));
//        System.out.println(round(-4.999999999999999, 5));
//        System.out.println(round(4.999999999999999, 10));
//        System.out.println(round(-4.999999999999999, 10));
//        System.out.println(round(0.99999999999999999, 17));
//        System.out.println(round(-0.99999999999999999, 17));
//        
//        System.out.println(round(4.999999999999999, 1));
//        System.out.println(round(-4.999999999999999, 1));
//        System.out.println(round(4.999999999999999, 0));
//        System.out.println(round(-4.999999999999999, 0));
//        System.out.println(round(4.9999999999999, -1));
//        System.out.println(round(-4.9999999999999, -1));
//        System.out.println(round(5.0000000000001, -1));
//        System.out.println(round(-5.0000000000001, -1));
//        System.out.println(round(4449.99999999999999999999999999, -2));
//        System.out.println(round(-4450.9999999999999999999999999999, -2));
//
////        System.out.println(round(0.99, 1));
////        System.out.println(round(0.99, 2));
////        System.out.println(round(0.99, 3));
////        System.out.println(round(-0.99, 1));
////        System.out.println(round(-0.99, 2));
////        System.out.println(round(-0.99, 3));
////
////        System.out.println(round(100000.95, 1));
////        System.out.println(round(100000.95000000001, 1));
////        System.out.println(round(100000.95, 2));
////        System.out.println(round(100000.95000000001, 2));
////        System.out.println(round(-100000.95, 1));
////        System.out.println(round(-100000.95000000001, 1));
////        System.out.println(round(-100000.95, 2));
////        System.out.println(round(-100000.95000000001, 2));
//        
//    }

    private SmxDoubleUtil() {
        //no instances
    }

}
