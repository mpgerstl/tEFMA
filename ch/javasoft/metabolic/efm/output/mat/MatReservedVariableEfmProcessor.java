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
package ch.javasoft.metabolic.efm.output.mat;

import java.io.IOException;

import ch.javasoft.jsmat.ReservedMatrixWriter;
import ch.javasoft.jsmat.variable.MatReservedMatrix;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.output.EfmProcessor;
import ch.javasoft.util.Arrays;

/**
 * The <tt>MatReservedVariableEfmProcessor</tt> uses {@link MatReservedMatrix} to
 * write the elementary modes. A reserved matrix is a memory efficient ways to
 * write large matrices to matlab .mat files.
 */
public class MatReservedVariableEfmProcessor implements EfmProcessor<MatFileWriter> {

	private ReservedMatrixWriter<double[]> mReservedWriterDbl;
	private ReservedMatrixWriter<int[]> mReservedWriterInt8;
	
	public int[] initialize(EfmOutputCallback cb, MatFileWriter writer, EfmOutputEvent evt, long efmCount) throws IOException {
		int reactionCount = evt.getMetabolicNetwork().getReactions().length();
		if (cb.getGranularity().isBinarySufficient()) {
			MatReservedMatrix<int[]> mx = MatReservedMatrix.createInt8Matrix(reactionCount, (int)efmCount);
			mReservedWriterInt8 = writer.createReservedWriter("efms", mx);
		}
		else {
			MatReservedMatrix<double[]> mx = MatReservedMatrix.createDoubleMatrix(reactionCount, (int)efmCount);
			mReservedWriterDbl = writer.createReservedWriter("efms", mx);
		}
		return Arrays.EMPTY_INT;
	}
	public int addEfm(EfmOutputCallback cb, MatFileWriter writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		if (mReservedWriterInt8 == null && mReservedWriterDbl == null) {
			throw new IOException("reserved writer not initialized, must call initialize()");
		}
		if (mReservedWriterDbl != null) {
			final double[] values = evt.getEfm().getDoubleRates();
			mReservedWriterDbl.append(values);
		}
		else {
			final int[] sgns = new int[evt.getEfm().getSize()];
			for (int i = 0; i < sgns.length; i++) {
				sgns[i] = evt.getEfm().getRateSignum(i);
			}
			mReservedWriterInt8.append(sgns);
		}
		return 0;//cache size
	}
	public void finalize(EfmOutputCallback cb, MatFileWriter writer, EfmOutputEvent evt) throws IOException {
		if (mReservedWriterDbl != null) {
			mReservedWriterDbl.close();
			mReservedWriterDbl = null;
		}
		if (mReservedWriterInt8 != null) {
			mReservedWriterInt8.close();
			mReservedWriterInt8 = null;
		}
	}

}
