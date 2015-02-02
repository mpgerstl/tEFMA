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
package ch.javasoft.bitset.search;

import java.util.ArrayList;
import java.util.List;

import ch.javasoft.bitset.IBitSet;

/**
 * The <code>ListSearch</code> class performs a linear search on an array list
 * to find a subset or superset.
 */
public class ListSearch implements SuperSetSearch, SubSetSearch {
	
	private final List<IBitSet> sets = new ArrayList<IBitSet>(); 

	/**
	 * Adds the given set to the structure, or returns false if such a set is
	 * already contained within this search set
	 *  
	 * @param set	the set to add
	 * @return true if it has been added, and false if it was already contained
	 * 				in the structure
	 */
	public boolean add(IBitSet set) {
		return sets.add(set);
	}
	
	/**
	 * Removes the specified set from the structure and returns true if such
	 * as set was found and removed. False indicates that no set was found and
	 * the structure was not modified.
	 * 
	 * @param set	the set to remove
	 * @return	true if the structure was modified.
	 */
	public boolean remove(IBitSet set) {
		return sets.remove(set);
	}

	public IBitSet findSuperSet(IBitSet of) {
		for (final IBitSet s : sets) {
			if (of.isSubSetOf(s)) return s;
		}
		return null;
	}

	public IBitSet findSuperSet(IBitSet of, IBitSet after) {
		int index = indexOf(after);
		for (int i = index + 1; i < sets.size(); i++) {
			final IBitSet s = sets.get(i);
			if (of.isSubSetOf(s)) return s;
		}
		return null;
	}
	
	public IBitSet findSubSet(IBitSet of) {
		for (final IBitSet s : sets) {
			if (s.isSubSetOf(of)) return s;
		}
		return null;
	}
	
	public IBitSet findSubSet(IBitSet of, IBitSet after) {
		int index = indexOf(after);
		for (int i = index + 1; i < sets.size(); i++) {
			final IBitSet s = sets.get(i);
			if (s.isSubSetOf(of)) return s;
		}
		return null;
	}
	
	private int indexOf(IBitSet set) {
		for (int i = 0; i < sets.size(); i++) {
			if (set.equals(sets.get(i))) {
				return i;
			}
		}
		throw new IllegalArgumentException("after set not found");
	}
	

}
