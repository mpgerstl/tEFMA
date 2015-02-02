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

import java.util.NoSuchElementException;

public class DefaultIntListIterator extends AbstractIntListIterator {

	private final int		mStartIndex;
	private final IntList	mList;
	
	private int mIndex;
	private int mIndexToMutate;
	
	public DefaultIntListIterator(int startIndex, IntList list) {
		mStartIndex 	= startIndex;
		mList			= list;
		mIndex			= mStartIndex;
		mIndexToMutate	= -1;
	}
	@Override
	public boolean hasNext() {
		return mIndex < mList.size();
	}

	@Override
	public boolean hasPrevious() {
		return mIndex > mStartIndex /*or 0 ?*/;
	}

	@Override
	public int nextIndex() {
		return hasNext() ? mIndex : mList.size();
	}

	@Override
	public int nextInt() {
		if (hasNext()) {
			mIndexToMutate = mIndex;
			int val = mList.getInt(mIndex);
			mIndex++;
			return val;
		}
		throw new NoSuchElementException();
	}

	@Override
	public int previousIndex() {
		return hasPrevious() ? mIndex - 1 : -1;
	}

	@Override
	public int previousInt() {
		if (hasPrevious()) {
			mIndex--;
			mIndexToMutate = mIndex;
			return mList.getInt(mIndex);
		}
		throw new NoSuchElementException();
	}

	@Override
	public void addInt(int value) {
		if (mIndexToMutate == -1) {
			throw new IllegalStateException("next or previous not called, or element has already been mutated");
		}
		mList.addInt(mIndexToMutate, value);
		mIndexToMutate = -1;
	}

	@Override
	public void setInt(int value) {
		if (mIndexToMutate == -1) {
			throw new IllegalStateException("next or previous not called, or element has already been mutated");
		}
		mList.setInt(mIndexToMutate, value);
		mIndexToMutate = -1;
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.AbstractIntIterator#remove()
	 */
	@Override
	public void remove() {
		if (mIndexToMutate == -1) {
			throw new IllegalStateException("next or previous not called, or element has already been mutated");
		}
		mList.removeIntAt(mIndexToMutate);
		mIndexToMutate = -1;
		mIndex--;
	}

}
