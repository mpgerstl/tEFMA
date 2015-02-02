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
package ch.javasoft.math.array.sort;

import java.util.Arrays;
import java.util.Comparator;

/**
 * The <code>MatrixSortUtil</code> contains static methods to sort matrix rows 
 * or columns using a specified comparator.
 */
public class MatrixSortUtil {

	/**
	 * Sorts the matrix rows using the given comparator. The matrix is sorted in
	 * place, that is, the input matrix is modified.
	 * 
	 * @type T	number type
	 * @type A	array type of number
	 * 
	 * @param matrix		the matrix to sort
	 * @param comparator	the comparator applied to the matrix rows
	 */
	public static final <A> void sortMatrixRows(A[] matrix, Comparator<? super A> comparator) {
		Arrays.sort(matrix, comparator);
	}
	/**
	 * Sorts the rows of the row submatrix using the given comparator. The 
	 * matrix is sorted in place, that is, the input matrix is modified.
	 * 
	 * @type T	number type
	 * @type A	array type of number
	 * 
	 * @param matrix		the matrix to sort
	 * @param fromRow		the first matrix row to sort, inclusive
	 * @param toRow			the first matrix row after the last row to sort
	 * @param comparator	the comparator applied to the matrix rows
	 */
	public static final <A> void sortMatrixRows(A[] matrix, int fromRow, int toRow, Comparator<? super A> comparator) {
		Arrays.sort(matrix, fromRow, toRow, comparator);
	}
	
	/**
	 * Sorts the rows of the matrix using the given comparator. The input matrix 
	 * is not modified, but the row indices are returned, resulting to the 
	 * desired sorted matrix if applied to the input matrix.
	 * <p>
	 * The returned mapping contains indices {@code [0, ..., rows-1]}, 
	 * and the sorted matrix can be achieved by accessing the rows via the 
	 * indices in the mapping. The following code iterates through the sorted 
	 * matrix rows:
	 * <pre>
	 * int[] map = getSortedMatrixRows(matrix, comparator);
	 * for (int i = 0; i < rows; i++) {
	 *     matrix[map[i]];
	 * }
	 * </pre>
	 * 
	 * @type T	number type
	 * @type A	array type of number
	 * 
	 * @param matrix		the matrix to sort
	 * @param comparator	the comparator applied to the matrix rows
	 * @return 	the row indices to apply to the input mapping to get the sorted
	 * 			matrix
	 */
	public static final <A> int[] getSortedMatrixRows(A[] matrix, Comparator<? super A> comparator) {
		return getSortedMatrixRows(matrix, 0, matrix.length, comparator);
	}
	/**
	 * Sorts the rows of the row submatrix using the given comparator. The 
	 * input matrix is not modified, but the row indices are returned, resulting
	 * to the desired sorted matrix if applied to the input matrix.
	 * <p>
	 * The returned mapping contains indices {@code [fromRow, ..., toRow-1]}, 
	 * and the sorted matrix can be achieved by accessing the rows via the 
	 * indices in the mapping. The following code iterates through the sorted 
	 * matrix rows:
	 * <pre>
	 * int[] map = getSortedMatrixRows(matrix, fromRow, toRow, comparator);
	 * for (int i = 0; i < toRow-fromRow; i++) {
	 *     matrix[map[i]];
	 * }
	 * </pre>
	 * 
	 * @type T	number type
	 * @type A	array type of number
	 * 
	 * @param matrix		the matrix to sort
	 * @param fromRow		the first matrix row to sort, inclusive
	 * @param toRow			the first matrix row after the last row to sort
	 * @param comparator	the comparator applied to the matrix rows
	 * @return 	the row indices to apply to the input mapping to get the sorted
	 * 			matrix
	 */
	public static final <A> int[] getSortedMatrixRows(A[] matrix, int fromRow, int toRow, Comparator<? super A> comparator) {
		final IndexEntry<A>[] entries = createIndexEntryArrayFromRows(matrix, fromRow, toRow);
		Arrays.sort(entries, new IndexEntryComparator<A>(comparator));
		return entriesToMapping(entries);
	}
	
//	public static final <A> int[] getSortedMatrixColumns(A[] matrix, ArrayOperations<A> arrayOps, int fromColumn, int toColumn, Comparator<? super A> comparator) {
//		final IndexEntry<A>[] entries = createIndexEntryArrayFromColumns(matrix, arrayOps, fromColumn, toColumn);
//		Arrays.sort(entries, new IndexEntryComparator<A>(comparator));
//		return entriesToMapping(entries);
//	}
	
	private static class IndexEntry<T> {
		public final int	index;
		public final T 		value;
		public IndexEntry(int index, T value) {
			this.index = index;
			this.value = value;
		}
	}
	private static class IndexEntryComparator<T> implements Comparator<IndexEntry<T>> {
		private final Comparator<? super T> comparator;
		public IndexEntryComparator(Comparator<? super T> comparator) {
			this.comparator = comparator;
		}
		public int compare(IndexEntry<T> o1, IndexEntry<T> o2) {
			return comparator.compare(o1.value, o2.value);
		}
	}
	@SuppressWarnings("unchecked")
	private static <A> IndexEntry<A>[] createIndexEntryArrayFromRows(A[] matrix, int fromRow, int toRow) {
		final IndexEntry[] entries = new IndexEntry[toRow - fromRow];
		for (int i = 0; i < entries.length; i++) {
			entries[i] = new IndexEntry<A>(fromRow + i, matrix[fromRow + i]);
		}
		return (IndexEntry<A>[])entries;
	}
//	@SuppressWarnings("unchecked")
//	private static <A> IndexEntry<A>[] createIndexEntryArrayFromColumns(A[] matrix, ArrayOperations<A> arrayOps, int fromColumn, int toColumn) {
//		final IndexEntry[] entries = new IndexEntry[toColumn - fromColumn];
//		for (int i = 0; i < entries.length; i++) {
//			entries[i] = new IndexEntry<A>(fromColumn + i, arrayOps.copyOfMatrixColumn(matrix, fromColumn + i));
//		}
//		return (IndexEntry<A>[])entries;
//	}
	private static int[] entriesToMapping(IndexEntry<?>[] entries) {
		final int[] mapping = new int[entries.length];
		for (int i = 0; i < mapping.length; i++) {
			mapping[i] = entries[i].index;
		}
		return mapping;
	}
	
	//no instances
	private MatrixSortUtil() {
		super();
	}
}
