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
package ch.javasoft.metabolic.efm.tree.outcore;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.jbase.FixedWidthMarshaller;
import ch.javasoft.metabolic.efm.util.BitSetUtil;

/**
 * The <code>PersistentNodeEntityMarshaller</code> writes nodes of a persisted
 * pattern tree to disk. The data structure to store the tree nodes is actually
 * {@link PersistentNodeEntity}, thus this class stores such entities, not the
 * nodes directly.
 */
public class PersistentNodeEntityMarshaller implements FixedWidthMarshaller<PersistentNodeEntity> {
	
	private final int bitSetSize;
	
	public PersistentNodeEntityMarshaller(int bitSetSize) {
		this.bitSetSize = bitSetSize;
	}

	public int getByteWidth() {
		return
			/*bit set size*/ BitSetUtil.byteSize(bitSetSize) +
			2 * 4;//2 * int
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.jbase.EntityMarshaller#readFrom(java.io.DataInput)
	 */
	public PersistentNodeEntity readFrom(DataInput in) throws IOException {
		final IBitSet unionPattern = BitSetUtil.readFrom(in, bitSetSize);
		final int intA = in.readInt();
		final int intB = in.readInt();
		return new PersistentNodeEntity(unionPattern, intA, intB);
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.jbase.EntityMarshaller#writeTo(java.lang.Object, java.io.DataOutput)
	 */
	public void writeTo(PersistentNodeEntity entity, DataOutput out) throws IOException {
		BitSetUtil.writeTo(entity.unionPattern, bitSetSize, out);
		out.writeInt(entity.valueA);
		out.writeInt(entity.valueB);
	}

}
