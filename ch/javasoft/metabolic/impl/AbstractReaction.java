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
import java.util.Comparator;
import java.util.List;

import ch.javasoft.metabolic.MetabolicNetworkVisitor;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.MetaboliteRatio;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.ReactionConstraints;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.genarr.GenericDynamicArray;

abstract public class AbstractReaction implements Reaction {
	
	public AbstractReaction() {}

	abstract public String getName();
	abstract public ArrayIterable<? extends MetaboliteRatio> getMetaboliteRatios();
	abstract public ReactionConstraints getConstraints();

	/**
	 * @return {@link #getName()}
	 */
	public String getFullName() {
		return getName();
	}
	
	public ArrayIterable<? extends MetaboliteRatio> getEductRatios() {
		GenericDynamicArray<MetaboliteRatio> educts = new GenericDynamicArray<MetaboliteRatio>();
		ArrayIterable<? extends MetaboliteRatio> metabolites = getMetaboliteRatios();
		for (int ii = 0; ii < metabolites.length(); ii++) {
			MetaboliteRatio metaRatio = metabolites.get(ii); 
			if (metaRatio.getRatio() < 0.0d) {
				educts.add(metaRatio);
			}
		}
		return educts;		
	}

	public ArrayIterable<? extends MetaboliteRatio> getProductRatios() {
		GenericDynamicArray<MetaboliteRatio> products = new GenericDynamicArray<MetaboliteRatio>();
		ArrayIterable<? extends MetaboliteRatio> metabolites = getMetaboliteRatios();
		for (int ii = 0; ii < metabolites.length(); ii++) {
			MetaboliteRatio metaRatio = metabolites.get(ii); 
			if (metaRatio.getRatio() > 0.0d) {
				products.add(metaRatio);
			}
		}
		return products;		
	}

	public boolean isMetaboliteParticipating(Metabolite metabolite) {
		return getRatioValueForMetabolite(metabolite) != 0d;
	}
	public boolean isMetaboliteConsumed(Metabolite metabolite) {
		return getRatioValueForMetabolite(metabolite) < 0d;
	}
	public boolean isMetaboliteProduced(Metabolite metabolite) {
		return getRatioValueForMetabolite(metabolite) > 0d;
	}

	public double getRatioValueForMetabolite(Metabolite metabolite) {
		ArrayIterable<? extends MetaboliteRatio> metabolites = getMetaboliteRatios();
		for (int ii = 0; ii < metabolites.length(); ii++) {
			MetaboliteRatio metaRatio = metabolites.get(ii); 
			if (metaRatio.getMetabolite().equals(metabolite)) {
				return metaRatio.getRatio();
			}
		}
		return 0.0d;
	}
	
	@Override
	public String toString() {
		return toString(getMetaboliteRatios(), getConstraints().isReversible());
	}
	public static String toString(Iterable<? extends MetaboliteRatio> metaRatios, boolean reversible) {
		StringBuilder educ = new StringBuilder();
		StringBuilder prod = new StringBuilder();
		for (MetaboliteRatio metaRatio : metaRatios) {
			if (metaRatio.getRatio() < 0.0d) {
				if (educ.length() > 0) educ.append(" + ");
				educ.append(metaRatio.toStringAbs());
			}
			else if (metaRatio.getRatio() > 0.0d) {
				if (prod.length() > 0) prod.append(" + ");
				prod.append(metaRatio.toStringAbs());
			}
			else {
				if (prod.length() > 0) prod.append(" + ");
				prod.append(metaRatio.toStringAbs());
			}
		}
		return 
			(educ.length() > 0 ? educ : "#") + 
			(reversible ? " <--> " : " --> ") + 
			(prod.length() > 0 ? prod : "#");
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;
		if (obj.getClass() == getClass()) {
			Reaction reac = (Reaction)obj;
			if (getName().equals(reac.getName())) {
				List<? extends MetaboliteRatio> mine, others;
				mine	= getMetaboliteRatios().toGenericArray(true /*new instance*/);
				others	= reac.getMetaboliteRatios().toGenericArray(true /*new instance*/);
				if (mine.size() == others.size()) {
					//sort the metabolite ratios alphabetically
					Comparator<MetaboliteRatio> cmp = new Comparator<MetaboliteRatio>() {
						public int compare(MetaboliteRatio o1, MetaboliteRatio o2) {
							int cmp = o1.getMetabolite().getName().compareTo(o2.getMetabolite().getName());
							if (cmp == 0) {
								if (o1.getRatio() == o2.getRatio()) cmp = 0;
								else cmp = o1.getRatio() < o2.getRatio() ? -1 : 1;
							}
							return cmp;
						}
					};
					Collections.sort(mine, cmp);
					Collections.sort(others, cmp);
					for (int ii = 0; ii < mine.size(); ii++) {
						if (!mine.get(ii).equals(others.get(ii))) {
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isExternal() {
		return isUptake() || isExtract();
	}
	
	public boolean isUptake() {
		return getEductRatios().isEmpty();
	}
	
	public boolean isExtract() {
		return getProductRatios().isEmpty();
	}
	
	public boolean hasIntegerRatios() {
		for (MetaboliteRatio ratio : getMetaboliteRatios()) {
			if (!ratio.isIntegerRatio()) return false;
		}
		return true;
	}
	
	public void accept(MetabolicNetworkVisitor visitor) {
		visitor.visitReaction(this);
	}
	
	protected int objHashCode() {
		return super.hashCode();
	}

}
