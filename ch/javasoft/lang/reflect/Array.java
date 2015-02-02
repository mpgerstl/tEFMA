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
package ch.javasoft.lang.reflect;

/**
 * The <code>Array</code> class contains static methods to instantiate arrays
 * for generic types.
 */
public class Array {

	/**
	 * Creates a new array of the given component type. The new array contains
	 * null values
	 * 
	 * @param <T>						the component type generic parameter
	 * @param componentType				the component type class
	 * @param length					the length of the new array
	 * @return							the array
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[] newInstance(Class<T> componentType, int length) {
		return (T[])java.lang.reflect.Array.newInstance(componentType, length);
	}
	
	/**
	 * Creates a new array of the given component type. The new array contains
	 * null values
	 * 
	 * @param <T>						the component type generic parameter
	 * @param componentType				the component type class
	 * @param rows						the length of the first dimension of the 
	 * 									new array
	 * @param cols						the length of the second dimension of 
	 * 									the	new array
	 * @return							the array
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T[][] newInstance(Class<T> componentType, int rows, int cols) {
		return (T[][])java.lang.reflect.Array.newInstance(componentType, new int[] {rows, cols});
	}
	
	/**
	 * Creates a new array of the given component type, and initializes the 
	 * array with new instances by calling the default constructor of 
	 * componentType
	 * 
	 * @param <T>						the component type generic parameter
	 * @param componentType				the component type class
	 * @param length					the length of the new array
	 * @return							the array with new instances
	 * @throws InstantiationException	if calling the default constructor fails
	 * @throws IllegalAccessException	if calling the default constructor fails
	 */
	public static final <T> T[] newInstanceInstantiate(Class<T> componentType, int length) throws InstantiationException, IllegalAccessException {
		final T[] arr = newInstance(componentType, length);
		for (int i = 0; i < length; i++) {
			arr[i] = componentType.newInstance();
		}
		return arr;
	}
	
	/**
	 * Creates a new array of the given component type, and initializes the 
	 * array with new instances by calling the default constructor of 
	 * componentType
	 * 
	 * @param <T>						the component type generic parameter
	 * @param componentType				the component type class
	 * @param rows						the length of the first dimension of the 
	 * 									new array
	 * @param cols						the length of the second dimension of 
	 * 									the	new array
	 * @return							the array with new instances
	 * @throws InstantiationException	if calling the default constructor fails
	 * @throws IllegalAccessException	if calling the default constructor fails
	 */
	public static final <T> T[][] newInstanceInstantiate(Class<T> componentType, int rows, int cols) throws InstantiationException, IllegalAccessException {
		final T[][] arr = newInstance(componentType, rows, cols);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				arr[row][col] = componentType.newInstance();
			}
		}
		return arr;
	}
	
	//no instances
	private Array() {}
}
