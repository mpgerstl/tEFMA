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
package ch.javasoft.util.ints;

import java.util.Set;


/**
 * Implementation of IntSet, also implementing SortedSet, but with 
 * support limited to positive integers.
 */
abstract public class AbstractSortedIntSet extends AbstractIntCollection implements SortedIntSet {

	public AbstractSortedIntSet() {
		super();
	}
	public AbstractSortedIntSet(IntCollection set) {
		super(set);
	}
	public AbstractSortedIntSet(int[] values) {
		super(values);
	}

	public SortedIntSet subSet(Integer fromElement, Integer toElement) {
		return subSet(fromElement.intValue(), toElement.intValue());
	}
	public SortedIntSet headSet(int toElement) {
		return subSet(firstInt(), toElement);
	}
	public SortedIntSet headSet(Integer toElement) {
		return headSet(toElement.intValue());
	}
	public SortedIntSet tailSet(int fromElement) {
		return subSet(fromElement, lastInt());
	}
	public SortedIntSet tailSet(Integer fromElement) {
		return tailSet(fromElement.intValue());
	}

        // cj: b
        // abstract public IntIterator iterator();
        // cj: e

	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj instanceof IntSet) {
			IntSet oSet = (IntSet)obj;
			if (size() != oSet.size()) return false;
			final IntIterator it = oSet.iterator();
			while (it.hasNext()) {
				if (!containsInt(it.nextInt())) return false;
			}
			return true;
		}
		if (obj instanceof Set) {
			return super.equals(obj);
		}
		return false;
	}

}
