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
package ch.javasoft.metabolic.efm.memory;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.util.MappingUtil;
import ch.javasoft.util.IntArray;

/**
 * The <code>MappedSortableMemory</code> wraps around an {@link IterableMemory} 
 * instance and implements the sort and swap functions with an internal index 
 * mapping array.
 */
public class MappedSortableMemory<Col extends Column> implements SortableMemory<Col> {
	
	private final IndexableMemory<Col>	baseMemory;
	private final int[]					mapping; 
	
	public MappedSortableMemory(IndexableMemory<Col> baseMemory) throws IOException {
		this.baseMemory	= baseMemory;
		this.mapping 	= MappingUtil.getInitialMapping(baseMemory.getColumnCount());
	}

	public String fileId() throws IOException {
		return baseMemory.fileId();
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
			throw new IllegalArgumentException("cannot erase a mapped sortable memory");
		}
		baseMemory.close(false);
	}
	
}
