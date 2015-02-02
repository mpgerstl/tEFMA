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
package ch.javasoft.metabolic.efm.memory.incore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.ReadWriteMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * 
 * The <code>InCoreMemory</code> stores columns in an array list.
 * 
 * <p>
 * NOTE: this class is not thread save!
 */
public class InCoreMemory<Col extends Column> implements ReadWriteMemory<Col> {
	
	private final List<Col> mColumns = new ArrayList<Col>();
	
	public String fileId() throws IOException {
		throw new IOException("in core memory has no associated file id");
	}
	
	public Iterator<Col> iterator() {
		return Collections.unmodifiableList(mColumns).iterator();
	}
	public void appendColumn(Col column) {
		mColumns.add(column);
	}
	public void appendColumns(Iterable<? extends Col> columns) {
		if (columns instanceof InCoreMemory) {
			mColumns.addAll(((InCoreMemory<? extends Col>)columns).mColumns);
		}
		else if (columns instanceof Collection) {
			mColumns.addAll((Collection<? extends Col>)columns);
		}
		else {
			for (Col col : columns) mColumns.add(col);
		}
	}
	public int getColumnCount() {
		return mColumns.size();
	}
	public Col getColumn(int index) {
		return mColumns.get(index);
	}
	public void swapColumns(int indexA, int indexB) throws IOException {
		final Col colA = mColumns.get(indexA);
		mColumns.set(indexA, mColumns.get(indexB));
		mColumns.set(indexB, colA);
	}
	public void clear() {
		mColumns.clear();
	}

    public void appendFrom(IndexableMemory<? extends Col> memory) {
        if (memory instanceof InCoreMemory) {
            mColumns.addAll(((InCoreMemory<? extends Col>)memory).mColumns);
        }
        else {
            appendColumns(memory);
        }
    }
    public SortableMemory<Col> toSortableMemory() throws IOException {
    	return this;
    }
    public void flush() throws IOException {
    	//nothing to do
    }
    public void close(boolean erase) throws IOException {
    	//nothing to do
    }
}
