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
package ch.javasoft.util;

import java.util.Collection;

/**
 * Static helper methods around iterables
 */
public class Iterables {
	
	/**
	 * Returns the size of an iterable, i.e. the number of elements returned by
	 * the given iterable. This method is typically efficient if the iterable is an 
	 * instance of {@link Collection}, since {@link Collection#size()} is called.
	 * Otherwise, all elements are iterated and counted.
	 * 
	 * @param iterable the iterable for which to count its elements 
	 * @return the number of elements returned by the given iterable
	 */
	public static int iterableSize(Iterable<?> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<?>)iterable).size();
		}
		int size = 0;
		for (@SuppressWarnings("unused") Object obj : iterable) size++;
		return size;
	}
	
	/**
	 * A compound iterable, linking together various iterables to a single logical
	 * iterable.
	 * 
	 * @param <E>		the content type of the returned iterable 
	 * @param iterables	the source iterables
	 * @return a single logical iterable nesting the source iterables
	 */
	public static final <E> Iterable<E> iterableIterable(Iterable<? extends E>[] iterables) {
		return new IterableIterable<E>(iterables);
	}
	
	/**
	 * A compound iterable, linking together various iterables to a single logical
	 * iterable.
	 * 
	 * @param <E>		the content type of the returned iterable 
	 * @param iterables	the source iterables
	 * @return a single logical iterable nesting the source iterables
	 */
	public static final <E> Iterable<E> iterableIterable(Iterable<? extends Iterable<? extends E>> iterables) {
		return new IterableIterable<E>(iterables);
	}

	private Iterables() {
		super();
	}

}
