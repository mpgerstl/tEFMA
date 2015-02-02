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
package ch.javasoft.metabolic.efm.memory.outcore;

import ch.javasoft.jbase.BufferedRandomAccessPersister;
import ch.javasoft.jbase.FixedWidthTable;
import ch.javasoft.jbase.Table;
import ch.javasoft.jbase.VariableWidthTable;
import ch.javasoft.metabolic.efm.column.BigIntegerColumn;
import ch.javasoft.metabolic.efm.column.DoubleColumn;
import ch.javasoft.metabolic.efm.column.FractionalColumn;

/**
 * Cache settings for {@link Table} use for different persisted objects.
 * 
 * @see BufferedRandomAccessPersister#BufferedRandomAccessPersister(java.io.File, int, int)
 * @see FixedWidthTable#create(java.io.File, ch.javasoft.jbase.FixedWidthMarshaller, int, int)
 * @see VariableWidthTable#create(java.io.File, String, int, ch.javasoft.jbase.EntityMarshaller, int, int)
 */
public enum Cache {
	/**
	 * The cache specification for tables used by {@link OutOfCoreMemory}
	 * for {@link DoubleColumn}
	 */
	DoubleMemoryTable(16, 4096), 
	/**
	 * The cache specification for tables used by {@link OutOfCoreMemory}
	 * for {@link FractionalColumn}
	 */
	BigFractionMemoryTable(16, 1024), 
	/**
	 * The cache specification for tables used by {@link OutOfCoreMemory}
	 * for {@link BigIntegerColumn}
	 */
	BigIntegerMemoryTable(16, 1024), 
	/**
	 * The cache specification for tables used by {@link OutOfCoreMemory}
	 * for {@link BigIntegerColumn}
	 */
	VarIntMemoryTable(16, 1024), 
	/**
	 * The cache specification for tables used by 
	 * {@link ch.javasoft.metabolic.efm.tree.outcore.PersistentBitPatternTree PersistentBitPatternTree}
	 */
	PersistentBitPatternTree(16, 4096);

	private final int cacheEntrySize;
	private final int cacheTableSize;
	private Cache(int cacheTableSize, int cacheEntrySize) {
		this.cacheTableSize	= cacheTableSize;
		this.cacheEntrySize	= cacheEntrySize;		
	}
	/**
	 * Setting for use with {@link BufferedRandomAccessPersister},
	 * {@link FixedWidthTable} and {@link VariableWidthTable}
	 */
	public int getCacheTableSize() {
		return cacheTableSize;
	}
	/**
	 * Setting for use with {@link BufferedRandomAccessPersister},
	 * {@link FixedWidthTable} and {@link VariableWidthTable}
	 */
	public int getCacheEntrySize() {
		return cacheEntrySize;
	}
}
