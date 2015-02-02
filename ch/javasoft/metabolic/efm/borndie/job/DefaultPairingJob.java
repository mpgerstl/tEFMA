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
package ch.javasoft.metabolic.efm.borndie.job;

import java.io.IOException;

import ch.javasoft.metabolic.efm.adj.AdjEnum;
import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.memory.ColumnDemuxAppendableMemory;
import ch.javasoft.metabolic.efm.borndie.range.CellRange;
import ch.javasoft.metabolic.efm.borndie.range.DefaultColumnRange;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;

/**
 * Default implementation of {@link PairingJob}
 */
public class DefaultPairingJob<Col extends Column> implements PairingJob<Col> {

	private final BornDieController<Col>	controller;
//	private final BearingCell<Col>			owner;
	private final CellRange					neg;
	private final CellRange					pos;
	
	private SortableMemory<Col> 				memNeg;
	private SortableMemory<Col> 				memPos;
	private ColumnDemuxAppendableMemory<Col>	memApp;
	
	public DefaultPairingJob(BornDieController<Col> controller, CellRange neg, CellRange pos) {
		this.controller	= controller;
		this.neg		= neg;
		this.pos		= pos;
	}
	public BornDieMatrix<Col> getBornDieMatrix() {
		return controller.getMatrix();
	}
	public CellRange getCellRangeNeg() {
		return neg;
	}
	public CellRange getCellRangePos() {
		return pos;
	}
	public int getIteration() {
		return getCellRangeNeg().getDieRow() + 1;
	}
	public ColumnDemuxAppendableMemory<Col> getMemoryForAppending() throws IOException {
		if (memApp == null) {
			final CellRange neg = getCellRangeNeg();
			memApp = new DefaultColumnRange(neg.getDieRow() + 1, neg.getDieRow() + 1, controller.getIterationCount() + 1).getForAppending(controller);
		}
		return memApp;
	}
	public SortableMemory<Col> getMemoryNeg() throws IOException {
		if (memNeg == null) {
			final BornDieMatrix<Col> matrix = getBornDieMatrix();
			final CellRange neg = getCellRangeNeg();
			memNeg = neg.getNegForGenerating(matrix);
		}
		return memNeg;
	}
	public SortableMemory<Col> getMemoryPos() throws IOException {
		if (memPos == null) {
			final BornDieMatrix<Col> matrix = getBornDieMatrix();
			memPos = pos.getPosForGenerating(matrix, getCellRangeNeg().getDieRow());
		}
		return memPos;
	}
	
	public int getAppendedColumnCount() {
		return memApp == null ? 0 : memApp.getAppendedColumnCount();
	}
	public int getColumnCountNeg() {
		try {
			return getMemoryNeg().getColumnCount();
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public int getColumnCountPos() {
		try {
			return getMemoryPos().getColumnCount();
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void run() {
		try {
			//init adjacency enum
			final SortableMemory<Col> memPos = getMemoryPos();
			if (memPos.getColumnCount() > 0) {
				final Config config = controller.getConfig();
				final NetworkEfmModel model = controller.getModel();
		        final AdjEnum adjEnum = config.getAdjMethodFactory().createAdjEnumFromConfig();
		        adjEnum.initialize(controller.getColumnHome(), config, model);
				final AdjEnumModel<Col> adjModel = new AdjEnumModel<Col>(
						model, getIteration(), 
						memPos, null /*job.getMemoryZero()*/, 
						getMemoryNeg(), 
						getMemoryForAppending());
				adjEnum.adjacentPairs(controller.getColumnHome(), adjModel);
			}
			controller.getMatrix().notifyPairingJobCompleted(this);
			if (controller.getDebugger().doDebug()) {
				controller.getDebugger().notifyPairingComplete(this);
			}
		} 
		catch (Exception e) {
			controller.handleJobException(this, e);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{neg=" + getCellRangeNeg() + "(" + 
			getColumnCountNeg() + "), pos=" + getCellRangePos() + "(" +
			getColumnCountPos() + "), new=" + getAppendedColumnCount() + "}";
	}

}
