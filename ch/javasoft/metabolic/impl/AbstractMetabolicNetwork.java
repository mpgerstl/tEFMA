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
package ch.javasoft.metabolic.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.javasoft.metabolic.Annotateable;
import ch.javasoft.metabolic.AnnotateableMetabolicNetwork;
import ch.javasoft.metabolic.MetabolicNetworkVisitor;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;

abstract public class AbstractMetabolicNetwork implements AnnotateableMetabolicNetwork {
	
	protected Map<Annotateable, Map<String, Object>> mAnnotations;//lazy initialization
	
	public Metabolite getMetabolite(String name) {
		for (Metabolite meta : getMetabolites()) {
			if (meta.getName().equals(name)) return meta;
		}
		throw new IllegalArgumentException("no such metabolite: " + name);
	}

	public ArrayIterable<? extends Reaction> getReactions(Metabolite metabolite) {
		ArrayIterable<? extends Reaction> reacts = getReactions();
		GenericDynamicArray<Reaction> result = new GenericDynamicArray<Reaction>();
		for (int ii = 0; ii < reacts.length(); ii++) {
			Reaction reaction = reacts.get(ii); 
			if (reaction.isMetaboliteParticipating(metabolite)) {
				result.add(reaction);
			}
		}
		return result;
	}
	
	public Reaction getReaction(String name) {
		for (Reaction react : getReactions()) {
			if (react.getName().equals(name)) return react;
		}
		throw new IllegalArgumentException("no such reaction: " + name);
	}
	
	public int getMetaboliteIndex(String name) {
		int index = 0;
		for (Metabolite meta : getMetabolites()) {
			if (meta.getName().equals(name)) return index;
			index++;
		}
		return -1;
	}
	
	public int getReactionIndex(String name) {
		int index = 0;
		for (Reaction react : getReactions()) {
			if (react.getName().equals(name)) return index;
			index++;
		}
		return -1;
	}
	
	public void accept(MetabolicNetworkVisitor visitor) {
		visitor.visitMetabolicNetwork(this);
	}
	
	@Override
	public String toString() {
		return getReactions().toString();
	}
	
	public String toStringVerbose() {
		StringBuilder sb = new StringBuilder();
		for (Reaction react : getReactions()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(react.getName() + ": " + react);
		}
		return "[" + sb.toString() + "]";
	}
	
	public String[] getMetaboliteNames() {
		String[] names = new String[getMetabolites().length()];
		for (int i = 0; i < names.length; i++) {
			names[i] = getMetabolites().get(i).getName();
		}
		return names;
	}
	public String[] getReactionNames() {
		String[] names = new String[getReactions().length()];
		for (int i = 0; i < names.length; i++) {
			names[i] = getReactions().get(i).getName();
		}
		return names;
	}
	public boolean[] getReactionReversibilities() {
		boolean[] revs = new boolean[getReactions().length()];
		for (int i = 0; i < revs.length; i++) {
			revs[i] = getReactions().get(i).getConstraints().isReversible();
		}
		return revs;
	}
	
	public void addAnnotation(Annotateable element, String name, Object value) {
		Map<String, Object> annots = getAnnotationMap(element, value != null);
		if (value == null) {
			annots.remove(name);
		}
		else {
			annots.put(name, value);
		}
	}
	
	public Object getAnnotation(Annotateable element, String name) {
		Map<String, Object> annots = getAnnotationMap(element, false);
		return annots == null ? null : annots.get(name);
	}
	
	public Iterable<Entry<String, Object>> getAnnotations(Annotateable element) {
		Map<String, Object> annots = getAnnotationMap(element, false);
		if (annots == null) {
			return Collections.emptySet();
		}
		return annots.entrySet();
	}
	
	protected Map<String, Object> getAnnotationMap(Annotateable element, boolean createIfNeeded) {
		if (mAnnotations == null) {
			if (createIfNeeded) {
				mAnnotations = new LinkedHashMap<Annotateable, Map<String,Object>>();
			}
			else return null;
		}
		Map<String, Object> annotationMap = mAnnotations.get(element);
		if (annotationMap == null && createIfNeeded) {
			annotationMap = new LinkedHashMap<String, Object>();
			mAnnotations.put(element, annotationMap);
		}
		return annotationMap;
	}
	
}
