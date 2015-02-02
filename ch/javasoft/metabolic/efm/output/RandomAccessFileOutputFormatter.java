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
package ch.javasoft.metabolic.efm.output;

import java.io.IOException;
import java.io.RandomAccessFile;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;

/**
 * Formats elementary modes in a binary way using random access files. Can be 
 * used with {@link RandomAccessFileOutputCallback}.
 * 
 * Written format: 
 * <pre>
 * long			-- number of efms
 * int			-- reaction count
 * boolean		-- true if binary flux values follow (false for doubles)
 * 
 * double (last value was false):
 * {{double}}	-- double values, #reactions x #efms, efm by efm (outer loop)
 * 
 * binary (last value was true):
 * {{byte}}		-- bit values for binary flux vectors, ((#reactions-1)/8 + 1) bytes per efm
 * </pre>
 */
public class RandomAccessFileOutputFormatter implements EfmOutputFormatter<RandomAccessFile> {

	private long startFilePointer;
	
	public void formatHeader(EfmOutputCallback cb, RandomAccessFile writer, EfmOutputEvent evt) throws IOException {
		MetabolicNetwork net = Util.getNetworkUncompressedIfNeeded(cb.getGranularity(), evt);
		startFilePointer = writer.getFilePointer();
		writer.writeLong(0L);//place holder
		writer.writeInt(net.getReactions().length());
		writer.writeBoolean(cb.getGranularity().isBinarySufficient());
	}

	public void formatEfmHeader(EfmOutputCallback cb, RandomAccessFile writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		if (cb.getGranularity().isBinarySufficient()) {
//			Column col = evt.getEfmAsColumn();
//			col.writeTo(writer);
			final FluxDistribution fd = evt.getEfm();
			byte b = 0;
			byte m = 1;
//			String str = "";
			for (int i = 0; i < fd.getSize(); i++) {
//				System.out.print(fd.getNumberRate(i).doubleValue() != 0d ? '1' : '0');
				if (fd.getNumberRate(i).doubleValue() != 0d) {
					b |= m;
				}
				if (i % 8 == 7 || i + 1 == fd.getSize()) {
					writer.writeByte(b);
//					String s = Integer.toHexString(b); 
//					str += ("00" + s).substring(s.length());
					b = 0;
					m = 1;
				}
				else {
					m <<= 1;
				}
			}
//			System.out.println(": " + str);
		}
	}

	public void formatEfmValue(EfmOutputCallback cb, RandomAccessFile writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value) throws IOException {
		if (!cb.getGranularity().isBinarySufficient()) {
			writer.writeDouble(value.doubleValue());
		}
	}

	public void formatEfmFooter(EfmOutputCallback cb, RandomAccessFile writer, EfmOutputEvent evt, long efmIndex) {
		//no op
	}

	public void formatFooter(EfmOutputCallback cb, RandomAccessFile writer, EfmOutputEvent evt, long countEfms) throws IOException {
		final long curPtr = writer.getFilePointer();
		writer.seek(startFilePointer);
		writer.writeLong(countEfms);
		writer.seek(curPtr);
	}
	
	public boolean isEfmValueIterationNeeded(EfmOutputCallback cb) {
		return !cb.getGranularity().isBinarySufficient();
	}

}
