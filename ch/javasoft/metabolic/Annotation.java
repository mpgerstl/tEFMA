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
package ch.javasoft.metabolic;

import java.util.Date;

/**
 * The <code>Annotation</code> enumeration constaints standard annotations
 * usually applied to metabolic networks, such as organism and model name. The
 * annotations are motivated by the mnet structure.
 */
public enum Annotation {
	/**
	 * Model name annotation of type {@link String}
	 */
	ModelName(String.class),
	/**
	 * Version annotation of type {@link String}
	 */
	Version(String.class),
	/**
	 * Date annotation of type {@link Date}. Note that for the mnet structure, 
	 * this will be converted to a string.
	 */
	Date(Date.class),
	/**
	 * Organism annotation of type {@link String}
	 */
	Organism(String.class),
	/**
	 * Biomass reaction annotation of type {@link Reaction}. Note that for the 
	 * mnet structure, this reference will be converted to an integer.
	 */
	BiomassReaction(Reaction.class);
	
	private final Class annotationClass;
	
	private Annotation(Class annotationClass) {
		this.annotationClass = annotationClass;
	}
	/**
	 * Returns the constant's {@link #name() name} after changing the first
	 * letter to lower case. For instance, the constant {@link #ModelName} 
	 * returns {@code "modelName"}. 
	 */
	public String getMnetName() {
		final char[] chars = name().toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return String.valueOf(chars);
	}
	/**
	 * Returns the data type of the annotation, which is usually string.
	 */
	public Class<?> getAnnotationClass() {
		return annotationClass;
	}
	
	/**
	 * Adds an annotation of the kind represented by {@code this} annotation to
	 * the specified metabolic network. If the specified annotation 
	 * {@code value} is not null, its type is checked to be compatible with the
	 * {@link #getAnnotationClass() class} defined by this annotation. If null,
	 * possibly existing annotations are removed. 
	 * 
	 * @param network	the network to annotate
	 * @param value		the annotation value, or {@code null} to remove existing
	 * 					annotations
	 */
	public void addAnnotation(AnnotateableMetabolicNetwork network, Object value) {
		checkValue(value);
		network.addAnnotation(network, name(), value);
	}
	
	/**
	 * Returns the annotation of the kind represented by {@code this} annotation
	 * from specified metabolic network. If no such annotation exists, 
	 * {@code null} is returned. The type of the returned value is not 
	 * {@link #checkValue(Object) checked}.
	 */
	public Object getAnnotation(MetabolicNetwork network) {
		final Object value = network.getAnnotation(network, name());
		return value;
	}
	
	/**
	 * Checks whether the given value is compatible with the
	 * {@link #getAnnotationClass() class} defined by this annotation. If it is
	 * not compatible, an exception is thrown. Null values are always
	 * compatible.
	 * 
	 * @param value	the value to check
	 * @throws IllegalArgumentException	if the given value is not compatible 
	 * 									with the type defined by this annotation
	 */
	public void checkValue(Object value) throws IllegalArgumentException {
		if (value != null && !getAnnotationClass().isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("illegal value type, expected " + 
					getAnnotationClass().getName() + " but found " + 
					value.getClass().getName() + ": " + value);
		}
	}
	
}
