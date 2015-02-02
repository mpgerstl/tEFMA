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
package ch.javasoft.factory;

/**
 * The <code>FactoryUtil</code> contains static helper methods used with
 * {@link Factory factories}.
 */
public class FactoryUtil {
	
	/**
	 * Creates an instance of the given factory class and uses it to create and 
	 * return an object using the specified configuration object.
	 *  
	 * @param <T>				type of objects produced by the factory 
	 * @param clazz				class of objects produced by the factory
	 * @param factoryClassName	fully qualified class name of the factory
	 * @param config			the element containing configuration settings 
	 * 							for the object to create
	 * 
	 * @throws FactoryNotFoundException	if the factory class was not found
	 * @throws IllegalFactoryException	if the factory class was not a factory,
	 * 									if the factory could not be instantiated,
	 * 									if the config was not of the expected 
	 * 									type for this factory, or if the object 
	 * 									created by the factory was not 
	 * 									compatible with the type specified by 
	 * 									{@code clazz} 
	 * @throws ConfigException 			if the config content was not as 
	 * 									expected
	 */
	@SuppressWarnings("unchecked")
	public static <T, C, F extends Factory<T, C>> T create(Class<T> clazz, String factoryClassName, C config) throws FactoryNotFoundException, IllegalFactoryException, ConfigException {
		final Class<? extends Factory> factoryClass = loadFactoryClass(factoryClassName);
		final Factory factory = createFactory(clazz, factoryClass);
		final Object val;
		try {
			val = factory.create((C)config);
		}
		catch (ClassCastException e) {
			throw new IllegalFactoryException("illegal config type for factory " + factory + ": " + config);
		}
		if (val == null || clazz.isInstance(val)) {
			return clazz.cast(val);
		}
		throw new IllegalFactoryException("factory " + factoryClassName + " created not an instance of " + clazz.getName() + ": " + val);
	}
	/**
	 * Creates an instance of the given factory class and uses it to create and 
	 * return an object using the specified configuration object.
	 *  
	 * @param <T>			type of objects produced by the factory 
	 * @param clazz			class of objects produced by the factory
	 * @param factoryClass	factory class
	 * @param config		the element containing configuration settings for 
	 * 						the object to create
	 * 
	 * @throws FactoryNotFoundException	if the factory class was not found
	 * @throws IllegalFactoryException	if the factory could not be instantiated
	 * @throws ConfigException 			if the config content was not as 
	 * 									expected
	 */
	public static <T, C, F extends Factory<T, C>> T create(Class<T> clazz, Class<F> factoryClass, C config) throws FactoryNotFoundException, IllegalFactoryException, ConfigException {
		final F factory = createFactory(clazz, factoryClass);
		return factory.create(config);
	}
	/**
	 * Loads the factory class specified by class name and returns it.
	 *  
	 * @param factoryClassName	fully qualified class name of the factory
	 * 
	 * @throws FactoryNotFoundException	if the factory class was not found
	 * @throws IllegalFactoryException	if the factory could not be instantiated
	 */
	public static Class<? extends Factory> loadFactoryClass(String factoryClassName) throws FactoryNotFoundException, IllegalFactoryException {
		final Class<?> factoryClass;
		try {
			factoryClass = Class.forName(factoryClassName);
		}
		catch (ClassNotFoundException e) {
			throw new FactoryNotFoundException(factoryClassName, e);
		}
		try {
			return factoryClass.asSubclass(Factory.class);
		}
		catch (ClassCastException e) {
			throw new IllegalFactoryException("not an instance of " + Factory.class.getName() + ": " + factoryClassName);
		}
	}
	/**
	 * Creates an instance of the given factory class and returns it.
	 *  
	 * @param <T>			type of objects produced by the factory 
	 * @param clazz			class of objects produced by the factory
	 * @param factoryClass	factory class
	 * 
	 * @throws IllegalFactoryException 	if factory instantiation fails
	 */
	public static <T, C, F extends Factory<T, C>> F createFactory(Class<T> clazz, Class<F> factoryClass) throws IllegalFactoryException {
		try {
			return factoryClass.newInstance();
		}
		catch (IllegalAccessException e) {
			throw new IllegalFactoryException("could not instantiated factory " + factoryClass.getName() + ", e=" + e, e);
		} 
		catch (InstantiationException e) {
			throw new IllegalFactoryException("could not instantiated factory " + factoryClass.getName() + ", e=" + e, e);
		}
	}

	//no instances
	private FactoryUtil() {
		super();
	}
}
