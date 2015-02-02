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
package ch.javasoft.metabolic.efm.sort;

import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.util.numeric.Zero;

public class FewestNegPosSorter extends RowColSorter {
    private final Zero mZero;
    public FewestNegPosSorter(boolean compareRows, int startRowCol, int endRowCol, int startColRow, int endColRow, Zero zero) {
        super(compareRows, startRowCol, endRowCol, startColRow, endColRow);
        mZero = zero;
    }
    public int compare(ReadableDoubleMatrix mx, int rowOrColA, int rowOrColB) {
        int start   = startCol(mx);
        int cnt     = count(mx);
        long cntNegA = 0;
        long cntPosA = 0;
        long cntNegB = 0;
        long cntPosB = 0;
        for (int colOrRow = start; colOrRow < cnt; colOrRow++) {
            double valA = value(mx, rowOrColA, colOrRow);
            double valB = value(mx, rowOrColB, colOrRow);
            int sgnA = mZero.sgn(valA);
            int sgnB = mZero.sgn(valB);
            if (sgnA < 0) cntNegA++;
            else if (sgnA > 0) cntPosA++;
            if (sgnB < 0) cntNegB++;
            else if (sgnB > 0) cntPosB++;
        }
        long delta = cntNegA * cntPosA - cntNegB * cntPosB;
        return delta < 0L ? -1 : (delta > 0L ? 1 : 0);
    }
}