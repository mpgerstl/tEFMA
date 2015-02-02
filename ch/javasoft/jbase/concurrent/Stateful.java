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
package ch.javasoft.jbase.concurrent;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A <code>Stateful</code> object uses member variables to store state 
 * information during method calls. Thus, concurrently invoking the object's 
 * method is not possible, synchronization or locking is necessary. A stateful
 * object is similar to a cloneable object, only that creating a copy here
 * is for reading only, and a read/write lock is available (the write lock is
 * held when copying).
 * <p>
 * If synchronization implemented with a {@link ReadWriteLock read/write lock},  
 * a copy of the state is needed for read concurrent readers. Such a read copy
 * of the state can be created by calling {@link #createReadCopy(ReadWriteLock)}.
 */
public interface Stateful {
	/**
	 * Returns a read copy of this stateful object. The returned read copy 
	 * contains its own state compared to another read copy instance, i.e. read 
	 * copies can be used by different threads without causing conflicts 
	 * considering the state of the stateful object. 
	 * <p>
	 * The submitted read/write lock can be used later on e.g. for lazy opening
	 * of additional files. When this method is invoked, the write lock of the
	 * submitted read/write lock is held. 
	 * 
	 * @param 	lock A read/write lock, the write lock is held. The lock, 
	 * 				 however, might also be used for other functionality of the
	 * 				 returned read copy, for instance, if the read copy has to
	 * 				 be put in sync with the main object.
	 * @return	a new read copy instance, with copied state compared to other
	 * 			read copy instances
	 */
	Object createReadCopy(ReadWriteLock lock) throws IOException;
}
