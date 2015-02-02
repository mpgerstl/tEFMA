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
import java.util.Collection;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.util.IntArray;

/**
 * The <code>AdjCandidates</code> is a list with adjacent column candidates. The
 * list stores only the indices of the columns in the 
 * {@link IndexableMemory memory} object.
 */
public class AdjCandidates<Col extends Column> {
	
	private final IndexableMemory<Col> posMemory;
	private final IndexableMemory<Col> negMemory;	
	
	private final IntArray posIndices;
	private final IntArray negIndices;
	
	public AdjCandidates(IndexableMemory<Col> posMemory, IndexableMemory<Col> negMemory, int capacity) {
		this.posMemory	= posMemory;
		this.negMemory 	= negMemory;
		this.posIndices = new IntArray(capacity);
		this.negIndices = new IntArray(capacity);
	}
	
	public void add(IndexableMemory<Col> memoryA, int indexA, IndexableMemory<Col> memoryB, int indexB) {
		if (memoryA == posMemory) {
			posIndices.add(indexA);
			negIndices.add(indexB);
		}
		else {
			posIndices.add(indexB);
			negIndices.add(indexA);
		}
	}
	public void add(AdjCandidates<Col> candidates, int index) {
		posIndices.add(candidates.posIndices.get(index));
		negIndices.add(candidates.negIndices.get(index));
	}
	public void addAll(AdjCandidates<Col> candidates) {
		posIndices.addAll(candidates.posIndices);
		negIndices.addAll(candidates.negIndices);
	}
	public int size() {
		return posIndices.length();
	}
	public int getColumnIndexPos(int pairIndex) throws IOException {
		return posIndices.get(pairIndex);
	}
	public int getColumnIndexNeg(int pairIndex) throws IOException {
		return negIndices.get(pairIndex);
	}
	public Col getColumnPos(int pairIndex) throws IOException {
		return posMemory.getColumn(posIndices.get(pairIndex));
	}
	public Col getColumnNeg(int pairIndex) throws IOException {
		return negMemory.getColumn(negIndices.get(pairIndex));
	}
	public boolean isAncestor(int pairIndex, IndexableMemory<Col> ancestorMemory, int ancestorIndex) {
		if (ancestorMemory == posMemory) {
			return posIndices.get(pairIndex) == ancestorIndex;
		}
		if (ancestorMemory == negMemory) {
			return negIndices.get(pairIndex) == ancestorIndex;
		}
		return false;
	}
	
	public IBitSet getIntersection(int pairIndex) throws IOException {
		final Col colPos = getColumnPos(pairIndex);
		final Col colNeg = getColumnNeg(pairIndex);
		return colPos.bitValues().getAnd(colNeg.bitValues());
	}
	public int getIntersectionCardinality(int pairIndex) throws IOException {
		final Col colPos = getColumnPos(pairIndex);
		final Col colNeg = getColumnNeg(pairIndex);
		return colPos.bitValues().getAndCardinality(colNeg.bitValues());
	}
	/**
	 * Returns true if a superset of the specified column pair is found in the
	 * specified range of super memory columns.
	 *  
	 * @param pairIndex			the candidate pair index
	 * @param supMemory			the super memory
	 * @param superIndexStart	the start index, inclusive
	 * @param superIndexEnd		the end index, exclusive
	 */
	// public boolean hasSuperSet(int pairIndex, IndexableMemory<Col> supMemory, int superIndexStart, int superIndexEnd) throws IOException {
	public boolean hasSuperSet(int pairIndex, IndexableMemory<Col> supMemory, int superIndexStart, int superIndexEnd) throws IOException {
		final IBitSet setP = getColumnPos(pairIndex).bitValues();
		final IBitSet setN = getColumnNeg(pairIndex).bitValues();
                // System.out.println("in AdjCandidates.hasSuperSet.");
		for (int i = superIndexStart; i < superIndexEnd; i++) {
			if (!isAncestor(pairIndex, supMemory, i)) {
				final Col colSup = supMemory.getColumn(i);
				if (colSup.bitValues().isSuperSetOfIntersection(setP, setN)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void swap(int pairIndexA, int pairIndexB) {
		posIndices.swap(pairIndexA, pairIndexB);
		negIndices.swap(pairIndexA, pairIndexB);
	}
	public void removeLast() {
		posIndices.removeLast();
		negIndices.removeLast();
	}
	public void removeAll() {
		posIndices.clear();
		negIndices.clear();
	}
	
	/**Removes the last column and appends it as new column to the memory*/
	public <N extends Number> void appendLastColumn(ColumnHome<N, Col> columnHome, EfmModel efmModel, AdjEnumModel<Col> adjModel) throws IOException {
		final int i = posIndices.length() - 1;
		final Col colA = getColumnPos(i);
		final Col colB = getColumnNeg(i);
		final Col colN = colA.mergeWith(columnHome, efmModel, colB, adjModel);
		adjModel.getMemoryForNewFromAdj().appendColumn(colN);
		removeLast();
	}
	public <N extends Number> void appendNewColumns(ColumnHome<N, Col> columnHome, EfmModel efmModel, AdjEnumModel<Col> adjModel) throws IOException {
		for (int i = 0; i < posIndices.length(); i++) {
			final Col colA = getColumnPos(i);
			final Col colB = getColumnNeg(i);
			final Col colN = colA.mergeWith(columnHome, efmModel, colB, adjModel);
			adjModel.getMemoryForNewFromAdj().appendColumn(colN);
		}
	}
	public void appendPairsTo(Collection<ColumnPair> dst) throws IOException {
		for (int i = 0; i < posIndices.length(); i++) {
			final ColumnPair pair = new ColumnPair<Column>(getColumnPos(i), getColumnNeg(i));
			dst.add(pair);
		}
	}
	public void appendPairTo(Collection<ColumnPair> dst, int index) throws IOException {
		final ColumnPair pair = new ColumnPair<Column>(getColumnPos(index), getColumnNeg(index));
		dst.add(pair);
	}
	
}
