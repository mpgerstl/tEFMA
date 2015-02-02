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
package ch.javasoft.metabolic.efm.adj;

import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.tree.BitPatternTree;
import ch.javasoft.metabolic.efm.tree.BitPatternTree.Kind;
import ch.javasoft.metabolic.efm.tree.incore.IncoreBitPatternTree;

/**
 * The <code>ModIntPrimeInCoreAdjEnum</code> uses rank updating with integer
 * primes, such that each rank computation operation fits into a 32 bit integer
 * register. The pattern trees are kept in memory, that is, in-core.
 */
public class ModIntPrimeInCoreAdjEnum extends AbstractModIntPrimeAdjEnum {

	public static final String NAME = "rankup-modpi-incore";
	
	protected ModIntPrimeInCoreAdjEnum(String name) {
		super(name);
	}
	public ModIntPrimeInCoreAdjEnum() {
		this(NAME);
	}
	
	/**
	 * Creates and returns an {@link IncoreBitPatternTree} instance
	 * 
	 * @see IncoreBitPatternTree#IncoreBitPatternTree(Thread, ColumnHome, EfmModel, AdjEnumModel, BitPatternTree.Kind, int[], SortableMemory)
	 */
	@Override
	protected <Col extends Column, N extends Number> BitPatternTree createTree(Thread treeOwner, ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel, Kind kind, int[] selectiveBits, SortableMemory<Col> columns) throws IOException {
		return new IncoreBitPatternTree(treeOwner, columnHome, getEfmModel(), itModel, kind, selectiveBits, columns);
	}
	
}
