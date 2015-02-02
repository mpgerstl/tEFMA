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
package ch.javasoft.metabolic.efm.borndie.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.model.BornDieIterationStepModel;
import ch.javasoft.metabolic.efm.borndie.range.LowerTriangularMatrix;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.DefaultIterationStepModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.NetworkEfmModel;
import ch.javasoft.util.Iterables;
import ch.javasoft.util.numeric.Zero;

/**
 * The <code>ColumnDemuxAppendableMemory</code> is attached to a column in the 
 * {@link BornDieMatrix}. Appended modes are demultiplexed to the underlying
 * cell memories, using the first infeasible flux entry of the mode to derive
 * the row index in the matrix.
 */
public class ColumnDemuxAppendableMemory<Col extends Column> implements AppendableMemory<Col> {

	private final BornDieController<Col>	controller;
	private final int 						bornColumn;
	private final AtomicInteger				appendCounter = new AtomicInteger();
	
	public ColumnDemuxAppendableMemory(BornDieController<Col> controller, int bornColumn) {
		this.controller	= controller;
		this.bornColumn	= bornColumn;
	}

	/**
	 * Returns the die-row for the given column, evaluated by inspecting the
	 * first infeasible flux value of the column. The returned value indicates 
	 * the matrix row in the born-die matrix, in which the column will be 
	 * stored. 
	 * 
	 * @param column	the column to inspect
	 * @return	the row index in the born-die matrix
	 * 
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	protected int getDieRow(Col column) throws IOException {
		final NetworkEfmModel model = controller.getModel();
		final int itCount = controller.getIterationCount();
		for (int row = column.booleanSize() - model.getBooleanSize(1); row < itCount; row++) {
			final IterationStepModel itModel = new DefaultIterationStepModel(model, row);
			if (0 > column.getHyperplaneSign(model, itModel.getNextState())) {
				return row;
			}
		}
		return itCount;		
	}
	
	public void appendColumn(Col column) throws IOException {
		final ColumnHome<?, Col> columnHome = controller.getColumnHome();
		final NetworkEfmModel model = controller.getModel();
		final int dieRow = getDieRow(column);
		
		final int iterationFrom = column.booleanSize() == 0 ? 0 : column.booleanSize() - model.getBooleanSize(1) + 1;
		final IterationStepModel itModel = new BornDieIterationStepModel(model, iterationFrom, dieRow + 1);
		column = column.convert(columnHome, model, itModel, false /*clone*/);
		
		if (controller.getConfig().selfTest()) {
			//consistency check
			for (int i = model.getBooleanSize(1); i < column.booleanSize(); i++) {
				final int numInd	= i - model.getBooleanSize(1);
				final int boolSgn	= column.get(i) ? 0 : 1;
				final int numSgn	= column.getNumericSignum(new Zero(), numInd);
				if (boolSgn != numSgn) {
					throw new RuntimeException("internal error, bool-sgn[" + i + "]=" + boolSgn + " != num-sgn[" + numInd + "]=" + numSgn + " for " + column);
				}
			}
		}
		
		controller.getMatrix().getForAppending(bornColumn, dieRow).appendColumn(column);
		appendCounter.incrementAndGet();
		if (controller.getDebugger().doDebug()) {
			controller.getDebugger().notifyColumnAppended(bornColumn, dieRow);
		}
	}

	public void appendColumns(Iterable<? extends Col> columns) throws IOException {
		for (final Col col : columns) {
			appendColumn(col);
		}
	}

	public void appendFrom(IndexableMemory<? extends Col> memory) throws IOException {
		for (final Col col : memory) {
			appendColumn(col);
		}
	}

	/**
	 * @throws IOException always because this operation is not supported
	 */
	public void flush() throws IOException {
		throw new IOException("not supported");
	}

	/**
	 * @throws IOException always because this operation is not supported
	 */
	public SortableMemory<Col> toSortableMemory() throws IOException {
		throw new IOException("not supported");
	}

	/**
	 * @throws IOException always because this operation is not supported
	 */
	public void close(boolean erase) throws IOException {
		throw new IOException("not supported");
	}

	/**
	 * @throws IOException always because this operation is not supported
	 */
	public String fileId() throws IOException {
		throw new IOException("not supported");
	}

	public int getColumnCount() throws IOException {
		final BornDieMatrix<Col> matrix = controller.getMatrix();
		int cnt = 0;
		final LowerTriangularMatrix tril = matrix.getMatrixRange();
		for (int row = tril.getRowFrom(bornColumn); row < tril.getRowTo(bornColumn); row++) {
			cnt += matrix.getForAppending(bornColumn, row).getColumnCount();
		}
		return cnt;
	}

	public Iterator<Col> iterator() {
		final BornDieMatrix<Col> matrix = controller.getMatrix();
		final LowerTriangularMatrix tril = matrix.getMatrixRange();
		final List<AppendableMemory<Col>> mems = new ArrayList<AppendableMemory<Col>>();
		for (int row = tril.getRowFrom(bornColumn); row < tril.getRowTo(bornColumn); row++) {
			try {
				mems.add(matrix.getForAppending(bornColumn, row));
			} 
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return Iterables.iterableIterable(mems).iterator();
	}
	
	/**
	 * Returns the number of columns that have been appended to this memory
	 * instance
	 */
	public int getAppendedColumnCount() {
		return appendCounter.get();
	}
	
}
