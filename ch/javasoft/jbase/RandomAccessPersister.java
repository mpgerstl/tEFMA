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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReadWriteLock;

import ch.javasoft.jbase.concurrent.Stateful;

/**
 * Abstraction of storage which can be accessed randomly, such as
 * {@link RandomAccessFile}s.
 * <p>
 * Random access of a storage device means setting a byte offset, and
 * reading or writing at this offset position in the sequal.
 */
public interface RandomAccessPersister extends Stateful {
    /** 
     * Returns the current byte position. Note that any read
     * or write operation increments the position. 
     */
    long getPosition() throws IOException;
    /** 
     * Sets the byte offset position. Subsequent read or write
     * operations start at the specified position, even if it is 
     * after the actual length of the file or store. However, the
     * length does not change before actually writing at this
     * position.
     */
    void setPosition(long bytePos) throws IOException;
    /**
     * Sets the length of the storage. If this is shorter than
     * the current length, the data will be truncated. If it is
     * after the current length, additional data is appended,
     * but its content is undefined. 
     */
    void setLength(long byteLength) throws IOException;
    /**
     * Returns a data input object to read from. Read
     * operations increment the current byte position
     */
    DataInput getInput() throws IOException;
    /**
     * Returns a data input object to write to. Write
     * operations increment the current byte position
     */
    DataOutput getOutput() throws IOException;
    /**
     * Ensures that any possibly cached data is written
     * to the underlying store
     */
    void flush() throws IOException;
    /**
     * Closes this persistor, subsequent read or write
     * calls will cause an exception. Multiple close
     * calls, however, do not cause any exceptions.
     * 
     * @param erase if true, underlying files are deleted upon close
     */
    void close(boolean erase) throws IOException;
    
    /** 
     * Override to specialize return type
     * 
     * @see Stateful#createReadCopy(ReadWriteLock)
     */
    public RandomAccessPersister createReadCopy(ReadWriteLock lock) throws IOException;
}
