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

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.util.Iterables;

/**
 * A composed memory is a memory putting together a composition of memories.
 * For instance, any set of memories can be combined to one logical memory.
 *  
 * @param <Col>	the column type
 */
public class ComposedIterableMemory<Col extends Column> implements IterableMemory<Col> {
	
	private final Iterable<? extends IterableMemory<? extends Col>> components;
	
	public ComposedIterableMemory(Iterable<? extends IterableMemory<? extends Col>> components) {
		this.components = components;
	}

	public void close(boolean erase) throws IOException {
		for (IterableMemory<? extends Col> cmp : components) {
			cmp.close(erase);
		}
	}

	public String fileId() throws IOException {
		throw new IOException("not supported");
	}

	public int getColumnCount() throws IOException {
		int cnt = 0;
		for (IterableMemory<? extends Col> cmp : components) {
			cnt += cmp.getColumnCount();
		}
		return cnt;
	}

	public Iterator<Col> iterator() {
		return Iterables.iterableIterable(components).iterator();
	}

}
