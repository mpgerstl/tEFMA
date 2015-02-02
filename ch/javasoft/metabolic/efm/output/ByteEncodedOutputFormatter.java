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

import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;

/**
 * Formats elementary modes in a binary way. Can be used with 
 * {@link ByteEncodedOutputCallback}.
 * 
 * Written format: 
 * <pre>
 * int			-- reaction count
 * boolean		-- true if binary flux values follow (false for doubles)
 * 
 * double (last value was false):
 * {{double}}	-- double values, #reactions x #efms, efm by efm (outer loop)
 * 
 * binary (last value was true):
 * {{byte}}		-- bit values for binary flux vectors, ((#reactions-1)/8 + 1) bytes per efm
 * long			-- number of efms (inconsistency check)
 * </pre>
 */
public class ByteEncodedOutputFormatter implements EfmOutputFormatter<DataOutput> {

	public void formatHeader(EfmOutputCallback cb, DataOutput writer, EfmOutputEvent evt) throws IOException {
		MetabolicNetwork net = Util.getNetworkUncompressedIfNeeded(cb.getGranularity(), evt);
		writer.writeInt(net.getReactions().length());
		writer.writeBoolean(cb.getGranularity().isBinarySufficient());
	}

	public void formatEfmHeader(EfmOutputCallback cb, DataOutput writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		if (cb.getGranularity().isBinarySufficient()) {
			Column col = evt.getEfmAsColumn();
			col.writeTo(writer);
		}
	}

	public void formatEfmValue(EfmOutputCallback cb, DataOutput writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value) throws IOException {
		if (!cb.getGranularity().isBinarySufficient()) {
			writer.writeDouble(value.doubleValue());
		}
	}

	public void formatEfmFooter(EfmOutputCallback cb, DataOutput writer, EfmOutputEvent evt, long efmIndex) {
		//no op
	}

	public void formatFooter(EfmOutputCallback cb, DataOutput writer, EfmOutputEvent evt, long countEfms) throws IOException {
		writer.writeLong(countEfms);
	}
	
	public boolean isEfmValueIterationNeeded(EfmOutputCallback cb) {
		return !cb.getGranularity().isBinarySufficient();
	}

}
