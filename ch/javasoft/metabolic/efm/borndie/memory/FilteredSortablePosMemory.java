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
package ch.javasoft.metabolic.efm.borndie.memory;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.DefaultIterationStateModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.IntArray;

/**
 * The <code>FilteredSortablePosMemory</code> wraps around an 
 * {@link IterableMemory} instance and implements the sort and swap functions 
 * with an internal index mapping array. Not all columns of the original memory
 * are used, thus, it is also a fitered memory.
 */
public class FilteredSortablePosMemory<Col extends Column> implements SortableMemory<Col>, Cloneable {
	
	private final IndexableMemory<Col>	baseMemory;
	private final int 					partnerDieRow;
	private final int[]					mapping; 
	
	public FilteredSortablePosMemory(BornDieController<Col> controller, IndexableMemory<Col> baseMemory, int partnerDieRow) throws IOException {
		this.baseMemory		= baseMemory;
		this.partnerDieRow	= partnerDieRow;
		final IterationStateModel itModel = new DefaultIterationStateModel(controller.getModel(), partnerDieRow + 1);
		this.mapping = filter(baseMemory, controller.getModel(), itModel);
	}
	/**
	 * Constructor used by {@link #clone()} method
	 */
	protected FilteredSortablePosMemory(IndexableMemory<Col> baseMemory, int partnerDieRow, int[] mapping) {
		this.baseMemory		= baseMemory;
		this.partnerDieRow	= partnerDieRow;
		this.mapping 		= mapping;
	}

	private int[] filter(IndexableMemory<Col> srcMemory, EfmModel efmModel, IterationStateModel itModel) throws IOException {		
		final IntArray mapping = new IntArray();
		final int cols = srcMemory.getColumnCount();
		for (int i = 0; i < cols; i++) {
			final Col col = srcMemory.getColumn(i);
			if (col.getHyperplaneSign(efmModel, itModel) > 0) {
				mapping.add(i);
			}
		}
		return mapping.toArray();
	}

	public String fileId() throws IOException {
		return baseMemory.fileId() + "-pd" + partnerDieRow;
	}

	public int getColumnCount() throws IOException {
		return mapping.length;
	}

	public Col getColumn(int index) throws IOException {
		return baseMemory.getColumn(mapping[index]);
	}

	public Iterator<Col> iterator() {
		return new Iterator<Col>() {
			int index = 0;
			public boolean hasNext() {
				return index < mapping.length;
			}
			public Col next() {
				try {
					final Col col = getColumn(index);
					index++;
					return col;
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ArrayIndexOutOfBoundsException e) {
					throw new NoSuchElementException();
				}
			}
			public void remove() {
				throw new UnsupportedOperationException("not modifiable");
			}
		};
	}

	public void swapColumns(int indexA, int indexB) throws IOException {
		IntArray.swap(mapping, indexA, indexB);
	}

	public void flush() throws IOException {
		//nothing to do, swap mapping is kept in memory
	}

	public void close(boolean erase) throws IOException {
		if (erase) {
			throw new IllegalArgumentException("cannot erase a filtered memory");
		}
		baseMemory.close(false);
	}
	
	@Override
	public FilteredSortablePosMemory<Col> clone() {
		return new FilteredSortablePosMemory<Col>(baseMemory, partnerDieRow, Arrays.copyOf(mapping, mapping.length));
	}

}
