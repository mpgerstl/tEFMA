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

/**
 * A <code>Table</code> stores entity objects, sometimes called row. Entities 
 * can be added, removed and accessed by index. 
 * <p>
 * The underlying mechanism to store the entities can be persistent (e.g. in a file 
 * or database) or volatile (e.g. stored in memory only). Different strategies might
 * be used, for instance, entities with predefined size might be stored in a more
 * efficient way than variable size objects. It is also possible to use caches in
 * front of persistent tables to improve performance.
 */
public interface Table<E> {
    /**
     * Returns the size of the table, i.e. the number of rows or entities.
     */
    int size() throws IOException;
    /**
     * Returns the entity at the given position. Note that (row) indices
     * might change if entities are added or removed. 
     */
    E get(int index) throws IOException;
    /**
     * Replaces the entity at the given position by the specified
     * entity. 
     */
    void set(int index, E entity) throws IOException;
    /**
     * Swaps the two entities specified by their index.
     */
    void swap(int indexA, int indexB) throws IOException;
    /**
     * Adds a new entity to the table. The index of the added entity
     * is at end of table, i.e. {@link #size() size}-1 after adding
     * the new entity.
     * 
     * @return the position of the added entity, i.e. size-1 
     */
    int add(E entity) throws IOException;
    /**
     * Removes the entity at the given position from the table. If
     * this was the last entity, it is just removed. If it is another
     * (not the last) row, the entity at the last row is moved to
     * the specified position of the entity to remove.
     */
    void remove(int index) throws IOException;
    /**
     * Closes this table. Pending write operations are {@link #flush() flushed}, 
     * and underlying files are closed. Subsequent access to the table
     * is not allowed and causes exceptions. Multiple calls to this 
     * <tt>close</tt> method do not cause any exceptions.
     */
    void removeAll() throws IOException;
    /**
     * Flush ensures that all write operations are persisted. Writing
     * to files ore databases might involve caches (operating system 
     * caches or application caches). Calling this method enforces that
     * all cached data is written to the underlying layer(s).
     */
    void flush() throws IOException;
    /**
     * Closes this table. Pending write operations are {@link #flush() flushed}, 
     * and underlying files are closed. Subsequent access to the table
     * is not allowed and causes exceptions. Multiple calls to this 
     * <tt>close</tt> method do not cause any exceptions.
     */
    void close(boolean erase) throws IOException;
}
