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
package ch.javasoft.metabolic.efm.column;

import java.io.IOException;
import java.util.BitSet;

import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifierFactory;
import ch.javasoft.metabolic.efm.model.EfmModel;


abstract public class AbstractColumn implements Column {
	
	public void set(int bit) {
		bitValues().set(bit);
	}

	public void clear(int bit) {
		bitValues().clear(bit);
	}

	public void clear() {
		bitValues().clear();
	}

	public void flip(int bit) {
		bitValues().flip(bit);
	}

	public boolean get(int bit) {
		return bitValues().get(bit);
	}

	public boolean isSubSetOf(Column of) {
		return bitValues().isSubSetOf(of.bitValues());
	}

	public void and(Column with) {
		bitValues().and(with.bitValues());
	}

	public void or(Column with) {
		bitValues().or(with.bitValues());
	}

	public int length() {
		return bitValues().length();
	}

	public int cardinality() {
		return bitValues().cardinality();
	}

	public int cardinality(int fromBit, int toBit) {
		return bitValues().cardinality(fromBit, toBit);
	}

	public int nextSetBit(int from) {
		return bitValues().nextSetBit(from);
	}

	public int nextClearBit(int from) {
		return bitValues().nextClearBit(from);
	}

	public BitSet toBitSet() {
		return bitValues().toBitSet();
	}

	public int totalSize() {
		return booleanSize() + numericSize();
	}
	
	@Override
	abstract public Column clone();

    public int hashCodeObj() {
		return super.hashCode();
	}
	
	@Override
	public int hashCode() {
		return bitValues().hashCode();
	}
	
	/**
	 * Helper method for shorter writing when a column inspector/modifier is
	 * needed
	 * 
	 * @param <N>				the numeric type
	 * @param <A>				the numeric array type
	 * @param model				the model with access to the column 
	 * @param numericType		class defining the numeric array type
	 * @param numericArrayType	class defining the numeric array type
	 * @return 	the column modifier for the given model and the specified
	 * 			numeric types
	 * @throws IllegalArgumentException if the specified numeric type is not 
	 * 									supported for this model	
	 * 
	 * @see EfmModel#getColumnInspectorModifierFactory()
	 * @see ColumnInspectorModifierFactory#getColumnInspectorModifier(Class, Class)
	 */
	/*default*/ static <N extends Number, A> ColumnInspectorModifier<N, A> getColumnInspectorModifier(EfmModel model, Class<N> numericType, Class<A> numericArrayType) {
		return model.getColumnInspectorModifierFactory().getColumnInspectorModifier(numericType, numericArrayType);
	}
	
    /**
     * Partitions the given columns[start:end] into a left and a right part, where start index 
     * is inclusive, end index exclusive. The left part contains the columns having a 0 bit 
     * at the given bit position, the bit in the right part is 1. The returned index figures
     * as the end index of the left part (exclusive) and as the start index of the right
     * part (inclusive).
     * 
     * @throws IOException	if an i/o exception occurs, for instance caused by
     * 						file based memory access 
     */
    public static int partition(SortableMemory<Column> cols, int start/*incl*/, int end /*excl*/, int bit) throws IOException {
    	//zero left, one right
    	int ind = start;
    	int len = end;
    	while (ind < len) {
    		if (!cols.getColumn(ind).get(bit)) ind++;
    		else if (cols.getColumn(len - 1).get(bit)) len--;
    		else {
    			cols.swapColumns(ind, len - 1);
    			ind++;
    			len--;
    		}
    	}
    	assert(ind == len || ind == len+1);
    	return ind;
    }
}
