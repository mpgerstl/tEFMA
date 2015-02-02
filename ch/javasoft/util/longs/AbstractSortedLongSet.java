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
package ch.javasoft.util.longs;

import java.util.Set;

/**
 * Implementation of IntSet, also implementing SortedSet, but with 
 * support limited to positive integers.
 */
abstract public class AbstractSortedLongSet extends AbstractLongCollection implements SortedLongSet {

	public AbstractSortedLongSet() {
		super();
	}
	public AbstractSortedLongSet(LongCollection set) {
		super(set);
	}
	public AbstractSortedLongSet(long[] values) {
		super(values);
	}

	public SortedLongSet subSet(Long fromElement, Long toElement) {
		return subSet(fromElement.longValue(), toElement.longValue());
	}
	public SortedLongSet headSet(long toElement) {
		return subSet(firstLong(), toElement);
	}
	public SortedLongSet headSet(Long toElement) {
		return headSet(toElement.longValue());
	}
	public SortedLongSet tailSet(long fromElement) {
		return subSet(fromElement, lastLong());
	}
	public SortedLongSet tailSet(Long fromElement) {
		return tailSet(fromElement.longValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj instanceof LongSet) {
			LongSet oSet = (LongSet)obj;
			if (size() != oSet.size()) return false;
			final LongIterator it = oSet.iterator();
			while (it.hasNext()) {
				if (!containsLong(it.nextLong())) return false;
			}
			return true;
		}
		if (obj instanceof Set) {
			return super.equals(obj);
		}
		return false;
	}

}
