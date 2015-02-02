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
package ch.javasoft.metabolic.efm.model.nullspace;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ch.javasoft.math.BigFraction;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifierFactory;
import ch.javasoft.metabolic.efm.model.EfmModelFactory;
import ch.javasoft.metabolic.efm.util.DualKey;

public class NullspaceEfmModelFactory implements EfmModelFactory, ColumnInspectorModifierFactory {
	
	private final Map<DualKey, ColumnInspectorModifier<?,?>> columnInspectorsModifiers = new HashMap<DualKey, ColumnInspectorModifier<?,?>>();
	
	public NullspaceEfmModelFactory() {
		columnInspectorsModifiers.put(new DualKey(Double.class, double[].class), new NullspaceDoubleColumnInspectorModifier(true));
		columnInspectorsModifiers.put(new DualKey(BigFraction.class, BigFraction[].class), new NullspaceFractionalColumnInspectorModifier(true));
		columnInspectorsModifiers.put(new DualKey(BigInteger.class, BigInteger[].class), new NullspaceBigIntegerColumnInspectorModifier(true));
		columnInspectorsModifiers.put(new DualKey(VarIntNumber.class, VarIntNumber[].class), new NullspaceVarIntColumnInspectorModifier(true));
	}
	public <N extends Number, Col extends Column> NullspaceEfmModel createEfmModel(ColumnHome<N, Col> columnHome, Config config, MetabolicNetwork network) {
		return new NullspaceEfmModel(columnHome, network, config, this);
	}
	
	@SuppressWarnings("unchecked")
	public <N extends Number, A> ColumnInspectorModifier<N, A> getColumnInspectorModifier(Class<N> numericType, Class<A> numericArrayType) {
		ColumnInspectorModifier insMod = columnInspectorsModifiers.get(new DualKey(numericType, numericArrayType));
		if (insMod == null) {
			throw new IllegalArgumentException("no modifier for nullspace model and numeric/array type " + numericType.getSimpleName() + "/" + numericArrayType.getSimpleName());
		}
		return insMod;
	}
}
