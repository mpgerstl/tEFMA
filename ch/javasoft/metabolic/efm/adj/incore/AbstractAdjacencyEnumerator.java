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
package ch.javasoft.metabolic.efm.adj.incore;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.javasoft.metabolic.efm.adj.AdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.column.ColumnPair;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;

abstract public class AbstractAdjacencyEnumerator implements AdjEnum {

	protected Config 	mConfig;
	protected EfmModel	mModel;
	public AbstractAdjacencyEnumerator() {
		super();
	}
	
	/**
	 * Stores config and model into protected variables
	 */
	public <Col extends Column, N extends Number> void initialize(ColumnHome<N,Col> columnHome, Config config, EfmModel model) {
		mConfig = config;
		mModel 	= model;
	}
	
	@SuppressWarnings("unchecked")
	public <Col extends Column, N extends Number> void adjacentPairs(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> adjModel) throws IOException {
        final Queue<ColumnPair> adjacentPairs = new ConcurrentLinkedQueue<ColumnPair>();
        final SortableMemory<Column> memZer = (SortableMemory<Column>)adjModel.getMemoryZero();
        final SortableMemory<Column> memPos = (SortableMemory<Column>)adjModel.getMemoryPos();
        final SortableMemory<Column> memNeg = (SortableMemory<Column>)adjModel.getMemoryNeg();
		adjacentPairs(adjacentPairs, memZer, memPos, memNeg);
		while (!adjacentPairs.isEmpty()) {
			final ColumnPair pair = adjacentPairs.poll();
			final Col colA = columnHome.castColumn(pair.getColumnA());
			final Col colB = columnHome.castColumn(pair.getColumnB());
			final Col colN = colA.mergeWith(columnHome, mModel, colB, adjModel);
			adjModel.getMemoryForNewFromAdj().appendColumn(colN);
		}
	}
	abstract public void adjacentPairs(Queue<ColumnPair> adjacentPairs, SortableMemory<Column> zerCols, SortableMemory<Column> posCols, SortableMemory<Column> negCols) throws IOException;
	 
	public Config getConfig() {
		return mConfig;
	}
	public EfmModel getEfmModel() {
		return mModel;
	}
}
