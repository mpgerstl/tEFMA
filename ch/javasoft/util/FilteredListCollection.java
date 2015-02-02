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

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


/**
 * The <code>FilteredList</code> is a collection based on an underlying list. 
 * Elements of the list can be {@link #setSelected(int, boolean) selected}, 
 * hence this collection is basically a view on a assorted elements from the
 * underlying list.
 */
public class FilteredListCollection<E> implements Collection<E> {
	
	private final BitSet	unselected = new BitSet();
	private final List<E>	unfiltered;

	public FilteredListCollection(List<E> unfiltered) {
		this.unfiltered = unfiltered;
	}
	
	/**
	 * Returns the underlying unfiltered list that was passed to the 
	 * {@link #FilteredListCollection(List) constructor}.
	 * @return the underlying unfiltered list
	 */
	public List<E> getUnfilteredList() {
		return unfiltered;
	}
	
	/**
	 * Returns true if the element specified by the index in the 
	 * {@link #getUnfilteredList() unfiltered list} is selected in this list.
	 * 
	 * @param unfilteredIndex	the index in the original 
	 * 							{@link #getUnfilteredList() unfiltered list} 
	 * @return true if this element is selected and thus visible in this list
	 * @throws IndexOutOfBoundsException	if the {@code unfilteredIndex} is
	 * 									 	out of bounds
	 */
	public boolean isSelected(int unfilteredIndex) {
		if (unfilteredIndex < 0 || unfilteredIndex > unfiltered.size()) {
			throw new IndexOutOfBoundsException(String.valueOf(unfilteredIndex));
		}
		return !unselected.get(unfilteredIndex);
	}

	/**
	 * Sets the selected state of the element specified by the index in the 
	 * {@link #getUnfilteredList() unfiltered list}. The selected state 
	 * determines if the referred element is visible in this list or not.
	 * 
	 * @param unfilteredIndex	the index in the original 
	 * 							{@link #getUnfilteredList() unfiltered list}
	 * @param selected if true, the element will be visible in this list
	 * @throws IndexOutOfBoundsException	if the {@code unfilteredIndex} is
	 * 									 	out of bounds
	 */
	public void setSelected(int unfilteredIndex, boolean selected) {
		if (unfilteredIndex < 0 || unfilteredIndex > unfiltered.size()) {
			throw new IndexOutOfBoundsException(String.valueOf(unfilteredIndex));
		}
		unselected.set(unfilteredIndex, !selected);
	}
	
	/**
	 * Selects all elements from the 
	 * {@link #getUnfilteredList() unfiltered list}. This collection and the
	 * unfiltered list contain the same elements after calling this method.
	 * Individual elements can be {@link #setSelected(int, boolean) unselected} 
	 * subsequently.
	 */
	public void selectAll() {
		unselected.clear();
	}
	
	/**
	 * Unselects all elements from the 
	 * {@link #getUnfilteredList() unfiltered list}. This collection is 
	 * virtually empty after calling this method. Individual elements can be 
	 * {@link #setSelected(int, boolean) selected} subsequently.
	 */
	public void unselectAll() {
		unselected.set(0, unfiltered.size());
	}
	
	/**
     * Appends the specified element to the end of the underlying
     * {@link #getUnfilteredList() unfiltered list} and hence also to the end of
     * this collection. The added element is by default
     * {@link #isSelected(int) selected}. Returns 
     * <tt>true</tt> if this collection changed as a result of the call.  
     * (Returns <tt>false</tt> if the underlying 
     * {@link #getUnfilteredList() unfiltered list} does not permit duplicates 
     * and already contains the specified element.)
     * <p>
     * The {@link #getUnfilteredList() unfiltered list} might not support this
     * operation or place limitations on what elements may be added 
     * (like {@code null} values).
     * <p>
     * It is an invariant that this collection always contains the specified 
     * element after this call returns.
     *
     * @param o element whose presence in this collection is to be ensured.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException <tt>add</tt> is not supported by
     *         the underlying {@link #getUnfilteredList() unfiltered list}.
     * @throws ClassCastException class of the specified element prevents it
     *         from being added to the underlying 
     *         {@link #getUnfilteredList() unfiltered list}.
     * @throws NullPointerException if the specified element is null and the
     *         the underlying  {@link #getUnfilteredList() unfiltered list} does 
     *         not support null elements.
     * @throws IllegalArgumentException some aspect of this element prevents
     *         it from being added to the underlying 
     *         {@link #getUnfilteredList() unfiltered list}.
	 */
	public boolean add(E o) {
		return unfiltered.add(o);
	}

    /**
     * Adds all of the elements in the specified collection to this collection.
     * The behavior of this operation is undefined if the specified collection 
     * is modified while the operation is in progress. (This implies that the 
     * behavior of this call is undefined if the specified collection is this 
     * collection, and this collection is nonempty.)
     * <p>
     * All elements are appended at the end of the underlying
     * {@link #getUnfilteredList() unfiltered list} and hence also to the end of
     * this collection. The added elements are by default
     * {@link #isSelected(int) selected}. Returns 
     * <tt>true</tt> if this collection changed as a result of the call.  
     * <p>
     * The {@link #getUnfilteredList() unfiltered list} might not support this
     * operation or place limitations on what elements may be added 
     * (like {@code null} values).
     * <p>
     * It is an invariant that this collection always contains all the specified 
     * elements after this call returns.
     *
     * @param c elements to be inserted into this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException if the underlying 
     *         {@link #getUnfilteredList() unfiltered list} does not support the 
     *         <tt>addAll</tt> method.
     * @throws ClassCastException if the class of an element of the specified
     * 	       collection prevents it from being added to the underlying
     * 	       {@link #getUnfilteredList() unfiltered list}
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and the underlying
     * 	       {@link #getUnfilteredList() unfiltered list} does not support
     * 	       null elements, or if the specified collection is <tt>null</tt>.
     * @throws IllegalArgumentException some aspect of an element of the
     *	       specified collection prevents it from being added to the
     *	       underlying {@link #getUnfilteredList() unfiltered list}.
     * @see #add(Object)
     */
	public boolean addAll(Collection<? extends E> c) {
		return unfiltered.addAll(c);
	}

    /**
     * Removes all of the elements from this collection as well as from the
     * underlying {@link #getUnfilteredList() unfiltered list}. This collection 
     * will be empty after this method returns unless it throws an exception.
     * <p>
     * An alternative to removing all elements is  
     * {@link #unselectAll() unselecting all} or elements. This also virtually
     * removes all elements from this list, but does not affect the underlying
     * {@link #getUnfilteredList() unfiltered list}.
     *
     * @throws UnsupportedOperationException if the <tt>clear</tt> method is
     *         not supported by the underlying 
     *         {@link #getUnfilteredList() unfiltered list}.
     */
	public void clear() {
		unfiltered.clear();
	}

    /**
     * Returns <tt>true</tt> if this collection contains the specified
     * element. The elements in this collection are the 
     * {@link #isSelected(int) selected} elements from the
     * {@link #getUnfilteredList() unfiltered list}.
     * <p>
     * More formally, returns <tt>true</tt> if and only if this
     * collection contains at least one element <tt>e</tt> such that
     * <tt>(o==null ? e==null : o.equals(e))</tt>.
     *
     * @param o element whose presence in this collection is to be tested.
     * @return <tt>true</tt> if this collection contains the specified
     *         element
     * @throws ClassCastException if the type of the specified element
     * 	       is incompatible with this collection (optional).
     * @throws NullPointerException if the specified element is null and this
     *         collection does not support null elements (optional).
     */
	public boolean contains(Object o) {
		final int index = unfiltered.indexOf(o);
		return index >= 0 && !unselected.get(index);
	}

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection. The elements in this collection are the 
     * {@link #isSelected(int) selected} elements from the
     * {@link #getUnfilteredList() unfiltered list}.
     *
     * @param  c collection to be checked for containment in this collection.
     * @return <tt>true</tt> if this collection contains all of the elements
     *	       in the specified collection
     * @throws ClassCastException if the types of one or more elements
     *         in the specified collection are incompatible with the underlying
     *         {@link #getUnfilteredList() unfiltered list} (optional).
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and the underlying 
     *         {@link #getUnfilteredList() unfiltered list} does not support 
     *         null elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     * @see    #contains(Object)
     */
	public boolean containsAll(Collection<?> c) {
		for (final Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

    /**
     * Compares the specified object with this collection for equality. Returns
     * <tt>true</tt> if either (i) the other object is not a filtered list and
     * <tt>o.equals(this)</tt> returns true, or (ii) the other object is also a 
     * filtered list, both collections have the same size, and all corresponding 
     * pairs of elements in the two collections are <i>equal</i>. 
     * (Two elements <tt>e1</tt> and <tt>e2</tt> are <i>equal</i> if 
     * <tt>(e1==null ? e2==null : e1.equals(e2))</tt>.).
     *
     * @param o the object to be compared for equality with this list.
     * @return <tt>true</tt> if the specified object is equal to this filtered 
     * 						 list.
     * 
     * @see Object#equals(Object)
     * @see Collection#equals(Object)
     * @see Set#equals(Object)
     * @see List#equals(Object)
     */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof FilteredListCollection) {
			
		}
		//ensure that the other object is not calling our equals method, if it
		//does so, we return false
		if (flag.get() == null) {
			flag.set(Boolean.TRUE);
			final boolean result = o.equals(this);
			flag.remove();
			return result;
		}
		flag.remove();
		return false;
	}
	private final ThreadLocal<Boolean> flag = new ThreadLocal<Boolean>(); 

    /**
     * Returns the hash code value for this filtered list.  The hash code of a
     * filtered list is defined to be the result of the following calculation:
     * <pre>
     *  hashCode = 1;
     *  Iterator it = iterator();
     *  while (it.hasNext()) {
     *      Object obj = it.next();
     *      hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
     *  }
     * </pre>
     * This ensures that <tt>list1.equals(list2)</tt> implies that
     * <tt>list1.hashCode()==list2.hashCode()</tt> for any two filtered lists,
     * <tt>list1</tt> and <tt>list2</tt>, as required by the general
     * contract of <tt>Object.hashCode</tt>.
     *
     * @return the hash code value for this filtered list.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
	@Override
	public int hashCode() {
		int hashCode = 1;
		final int size = unfiltered.size();
		for (int i = unselected.nextClearBit(0); i < size; i = unselected.nextClearBit(i+1)) {
			final Object obj = unfiltered.get(i);
			hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
		}
		return hashCode;
	}

    /**
     * Returns <tt>true</tt> if this collection contains no selected elements.
     *
     * @return <tt>true</tt> if this collection contains no selected elements
     */
	public boolean isEmpty() {
		final int len = unselected.length();
		return unfiltered.size() == len && unselected.nextClearBit(0) == len;
	}

    /**
     * Returns an iterator over the elements in this collection. The elements 
     * in this collection are the {@link #isSelected(int) selected} elements 
     * from the {@link #getUnfilteredList() unfiltered list}. The order in the
     * unfiltered list is preserved by the returned iterator.
     * 
     * @return an <tt>Iterator</tt> over the selected elements in this collection
     */
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int remove = -1;
			int index = unselected.nextClearBit(0);
			public boolean hasNext() {
				return index < unfiltered.size();
			}
			public E next() {
				if (index < unfiltered.size()) {
					final int last = index;
					index = unselected.nextClearBit(last + 1);
					return unfiltered.get(last);
				}
				throw new NoSuchElementException();
			}
			public void remove() {
				if (remove >= 0) {
					unfiltered.remove(remove);
					index--;
				}
				throw new NoSuchElementException();
			}
		};
	}

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present and currently {@link #isSelected selected}. 
     * More formally, removes an element <tt>e</tt> such that 
     * <tt>(o==null ?  e==null : o.equals(e))</tt>, if this collection contains 
     * one or more such elements.  Returns true if this collection contained the 
     * specified element (or equivalently, if this collection changed as a 
     * result of the call).
     *
     * @param o element to be removed from this collection, if present and 
     * 					selected.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws ClassCastException if the type of the specified element
     * 	       is incompatible with the underlying 
     *         {@link #getUnfilteredList() unfiltered list} (optional).
     * @throws NullPointerException if the specified element is null and the
     *         underlying {@link #getUnfilteredList() unfiltered list} does not 
     *         support null elements (optional).
     * @throws UnsupportedOperationException remove is not supported by the
     *         underlying {@link #getUnfilteredList() unfiltered list}.
     */
	public boolean remove(Object o) {
		int index = unfiltered.indexOf(o);
		if (index >= 0) {
			if (unselected.get(index)) {
				final int last = unfiltered.lastIndexOf(o);
				if (index != last) {
					//we must iterate now until we find an object that is 
					//selected
					final int first = index + 1;
					index = -1;
					for (int i = first; i <= last && index < 0; i++) {
						if (!unselected.get(i) && eq(o, unfiltered.get(i))) {
							index = i;
						}
					}
				}
			}
			if (index >= 0) {
				unfiltered.remove(index);
				//shift unselected bits to left
				final int len = unselected.length();
				for (int i = index+1; i < len; i++) {
					unselected.set(i-1, unselected.get(i));
				}
				unselected.clear(len-1);
				return true;
			}
		}
		return false;
	}
	
	/** Equality with null support*/
	private static boolean eq(Object o1, Object o2) {
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}

    /**
     * 
     * Removes all this collection's elements that are selected and also 
     * contained in the specified collection (optional operation).  After this 
     * call returns, this collection will contain no elements in common with the 
     * specified collection.
     *
     * @param c elements to be removed from this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException if the <tt>remove</tt> method
     * 	       is not supported by the underlying
     * 	       {@link #getUnfilteredList() unfiltered list}.
     * @throws ClassCastException if the types of one or more elements
     *         in this collection are incompatible with the specified
     *         collection (optional).
     * @throws NullPointerException if this collection contains one or more
     *         null elements and the specified collection does not support
     *         null elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     * @see #remove(Object)
     * @see #contains(Object)
     */
	public boolean removeAll(Collection<?> c) {
		boolean any = false;
		for (final Object o : c) {
			boolean removed = remove(o);
			any |= removed;
			//there might be multiple occurrences
			while (removed) {
				removed = remove(o);
			}
		}
		return any;
	}

    /**
     * Retains only the selected elements in this collection that are contained 
     * in the specified collection (optional operation).  In other words, 
     * removes from this collection all of its elements that are not contained 
     * in the specified collection.
     *
     * @param c elements to be retained in this collection.
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * 
     * @throws UnsupportedOperationException if the <tt>remove</tt> method
     * 	       is not supported by the underlying
     * 	       {@link #getUnfilteredList() unfiltered list}.
     * @throws ClassCastException if the types of one or more elements
     *         in this collection are incompatible with the specified
     *         collection (optional).
     * @throws NullPointerException if this collection contains one or more
     *         null elements and the specified collection does not support null 
     *         elements (optional).
     * @throws NullPointerException if the specified collection is
     *         <tt>null</tt>.
     * @see #remove(Object)
     * @see #contains(Object)
     */
	public boolean retainAll(Collection<?> c) {
		boolean any = false;
		for (int i = 0; i < unfiltered.size(); i++) {
			if (!unselected.get(i)) {
				final Object o = unfiltered.get(i);
				if (!c.contains(o)) {
					any |= remove(o);
				}
			}
		}
		return any;
	}

    /**
     * Returns the number of elements in this collection. The elements in this
     * collection are the {@link #isSelected(int) selected} elements from the
     * {@link #getUnfilteredList() unfiltered list}.   
     * 
     * <p>
     * If this collection contains more than <tt>Integer.MAX_VALUE</tt> 
     * elements, returns <tt>Integer.MAX_VALUE</tt>.
     * 
     * @return the number of selected elements in this collection
     */
	public int size() {
		return unfiltered.size() - unselected.cardinality();
	}

    /**
     * Returns an array containing all of the elements in this collection. The 
     * elements  in this collection are the {@link #isSelected(int) selected} 
     * elements from the {@link #getUnfilteredList() unfiltered list}. The order 
     * in the unfiltered list is preserved by the returned iterator.      
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.<p>
     *
     * This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing the selected elements from this collection
     */
	public Object[] toArray() {
		return toArray(null);
	}

    /**
     * Returns an array containing all of the elements in this collection; 
     * the runtime type of the returned array is that of the specified array.  
     * If the collection fits in the specified array, it is returned therein.  
     * Otherwise, a new array is allocated with the runtime type of the 
     * specified array and the size of this collection.<p>
     *
     * If this collection fits in the specified array with room to spare
     * (i.e., the array has more elements than this collection), the element
     * in the array immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of this
     * collection <i>only</i> if the caller knows that this collection does
     * not contain any <tt>null</tt> elements.)<p>
     *
     * The elements  in this collection are the {@link #isSelected(int) selected} 
     * elements from the {@link #getUnfilteredList() unfiltered list}. The order 
     * in the unfiltered list is preserved by the returned iterator.      
     * <p>
     *
     * Like the <tt>toArray</tt> method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs<p>
     *
     * Suppose <tt>l</tt> is a <tt>List</tt> known to contain only strings.
     * The following code can be used to dump the list into a newly allocated
     * array of <tt>String</tt>:
     *
     * <pre>
     *     String[] x = (String[]) v.toArray(new String[0]);
     * </pre><p>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this collection are to be
     *        stored, if it is big enough; otherwise, a new array of the same
     *        runtime type is allocated for this purpose.
     * @return an array containing the selected elements of this collection
     * 
     * @throws ArrayStoreException the runtime type of the specified array is
     *         not a supertype of the runtime type of every element in this
     *         collection.
     * @throws NullPointerException if the specified array is <tt>null</tt>.
     */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		final int size = size();
		if (a == null) {
			a = (T[])new Object[size];
		}
		else if (a.length < size) {
			a = Arrays.newArray(a, size);
		}
		final Object[] arr = a;
		int index = 0;
		for (int i = unselected.nextClearBit(0); i < size; i = unselected.nextClearBit(i+1)) {
			arr[index++] = unfiltered.get(i);
		}
		return a;
	}
	
	
}
