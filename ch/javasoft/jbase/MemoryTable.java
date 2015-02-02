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
package ch.javasoft.jbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * The <code>MemoryTable</code> stores the entities in memory in an 
 * array list.
 */
public class MemoryTable<E> implements Table<E> {

    private final List<E> list;
    
    /**
     * Constructor for an empty <code>MemoryTable</code> with storing entities
     * in an {@link ArrayList} instance.
     */
    public MemoryTable() {
    	this(new ArrayList<E>());
    }
    /**
     * Constructor for a <code>MemoryTable</code> based on the specified list
     * instance. Changes to the list are also reflected by this memory table
     * (and vice versa).
     */
    public MemoryTable(List<E> list) {
    	this.list = list;
    }
    
    public int add(E entity) {
        list.add(entity);
        return list.size() - 1;
    }

    public void close(boolean erase) {
        list.clear();
    }

    public void flush() {
        //nothing to do
    }

    public E get(int index) {
        return list.get(index);
    }

    public void remove(int index) {
        if (index >= list.size()) {
            throw new ArrayIndexOutOfBoundsException("no such entry: " + index);
        }
        final E last = list.remove(list.size() - 1);
        if (index != list.size()) {
            list.set(index, last);
        }
    }

    public void set(int index, E entity) {
        list.set(index, entity);
    }
    public void swap(int indexA, int indexB) {
    	Collections.swap(list, indexA, indexB);
    }
    
    public void removeAll() throws IOException {
    	list.clear();
    }

    public int size() {
        return list.size();
    }

}
