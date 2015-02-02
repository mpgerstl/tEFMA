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

import ch.javasoft.metabolic.MetabolicNetworkVisitor;
import ch.javasoft.metabolic.Metabolite;

public class DefaultMetabolite implements Metabolite {
	
	private final String mName;
	private final String mDesc;
	
	public DefaultMetabolite(int index) {
		this(name(index));
	}
	public DefaultMetabolite(String name) {
		this(name, null);
	}
	public DefaultMetabolite(String name, String description) {
		if (name == null) {
			throw new NullPointerException("null name not allowed");
		}
//		if (name.matches(".*[\\s].*")) {
//			throw new IllegalArgumentException("no whitespace allowed in metabolite name");
//		}
		mName = name;
		mDesc = description;
	}
	
	public String getName() {
		return mName;
	}
	public String getDescription() {
		return mDesc;
	}
	
	@Override
	public int hashCode() {
		return mName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return 
			obj == this ||
			(obj instanceof Metabolite && mName.equals(((Metabolite)obj).getName()));
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public void accept(MetabolicNetworkVisitor visitor) {
		visitor.visitMetabolite(this);
	}
	
	public static String name(int index) {
		int charRange = 'Z' - 'A' + 1;
		StringBuilder sb = new StringBuilder();
		do {
			char ch = (char)('A' + (index % charRange));
			index = index / charRange - 1;
			sb.insert(0, ch);
		}
		while (index >= 0);
		return sb.toString();
	}
	public static String[] names(int count, String prefix) {
		String[] res = new String[count];
		for (int ii = 0; ii < res.length; ii++) {
			res[ii] = prefix + name(ii);
		}
		return res;
	}
	public static void main(String[] args) {
		System.out.println(name(0));
		System.out.println(name(20));
		System.out.println(name(23));
		System.out.println(name(24));
		System.out.println(name(25));
		System.out.println(name(26));
		System.out.println(name(27));
		System.out.println(name(50));
		System.out.println(name(51));
		System.out.println(name(52));
		System.out.println(name(649));
		System.out.println(name(650));
		System.out.println(name(651));
		System.out.println(name(652));
		System.out.println(name(675));
		System.out.println(name(676));
		System.out.println(name(677));
		System.out.println(name(700));
		System.out.println(name(701));
		System.out.println(name(702));
		System.out.println(name(703));
		System.out.println(name(5487));
	}
	
}
