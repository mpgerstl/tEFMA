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
package ch.javasoft.metabolic.efm.borndie.matrix;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.logging.Logger;

import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.borndie.job.DefaultPairingJob;
import ch.javasoft.metabolic.efm.borndie.job.PairingJob;
import ch.javasoft.metabolic.efm.borndie.memory.FilteredSortablePosMemory;
import ch.javasoft.metabolic.efm.borndie.range.CellRange;
import ch.javasoft.metabolic.efm.borndie.range.DefaultCellRange;
import ch.javasoft.metabolic.efm.borndie.range.LowerTriangularMatrix;
import ch.javasoft.metabolic.efm.borndie.range.RectangularRange;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.DefaultMemoryPart;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.metabolic.efm.memory.MappedSortableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryPart;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <code>ConcurrentBornDieMatrix</code> implements {@link BornDieMatrix} in
 * a thread-safe, lock-free manner.
 */
public class ConcurrentBornDieMatrix<Col extends Column> implements BornDieMatrix<Col> {
	
	private final Logger LOG = LogPkg.LOGGER;

	//e.g. if we have 2 iterations, we have
	//3 rows, where	row[0] contains modes dying in iteration 1		
	//              row[1] contains modes dying in iteration 2
	//              row[2] contains the final modes
	//3 cols, where col[0] contains initial modes (e.g. kernel matrix columns)
	//				col[1] contains modes born in iteration 1
	//				col[2] contains modes born in iteration 2
	//6 cells, i.e. (((2 + 1) * (2 + 2)) / 2), namely		
	//cell[0, 0]
	//cell[1, 0] cell[1, 1]
	//cell[2, 0] cell[2, 1] cell[2, 2]
	//
	//access column by column, i.e. a column can be accessed with successive indices
	private final LowerTriangularMatrix 							tril;
	private final BornDieController<Col> 							controller;
	private final AtomicReferenceArray<AppendableMemory<Col>>		appMemoriesByCell;
	private final AtomicReferenceArray<SortableMemory<Col>>			sortMemoriesByCell;
	private final ConcurrentHashMap<PosMemKey, FilteredSortablePosMemory<Col>>	posMemoriesByNegPos;
	private final AtomicInteger										lowestBearingRow;	
	private final AtomicIntegerArray								bearingCellCountByRow;
	private final AtomicIntegerArray								jobCountByCell;
	
	public ConcurrentBornDieMatrix(BornDieController<Col> controller, LowerTriangularMatrix tril) throws IOException {
		this.controller					= controller;
		this.tril						= tril;
		this.appMemoriesByCell			= new AtomicReferenceArray<AppendableMemory<Col>>(tril.getCellCount());
		this.sortMemoriesByCell			= new AtomicReferenceArray<SortableMemory<Col>>(tril.getCellCount());
		this.posMemoriesByNegPos 		= new ConcurrentHashMap<PosMemKey, FilteredSortablePosMemory<Col>>();
		this.lowestBearingRow			= new AtomicInteger(-1);
		this.bearingCellCountByRow		= new AtomicIntegerArray(tril.getRowCount());
		this.jobCountByCell 			= new AtomicIntegerArray(tril.getCellCount());
		for (int r = 0; r < tril.getRowCount(); r++) {
			bearingCellCountByRow.set(r, tril.getRowWidth(r));
			for (int c = tril.getColumnFrom(r); c < tril.getColumnTo(r); c++) {
				final MemoryPart part = new DefaultMemoryPart("b" + c + "-d" + r); 
				final int index = tril.cellToIndex(c, r);
				final AppendableMemory<Col> mem = controller.getMemoryFactory().createConcurrentAppendableMemory(
					controller.getColumnHome(), controller.getModel(), c, part);
				appMemoriesByCell.set(index, mem);
				final PairingRule rule = new PairingRule(tril, new DefaultCellRange(c, r));
				jobCountByCell.set(index, rule.getPartnerCells().getCellCount());
			}
		}
	}
	
	public LowerTriangularMatrix getMatrixRange() {
		return tril;
	}
	
	public int getIterationCount() {
		return tril.getLength() - 1;
	}
	
//	private Cell<Col> getCell(int bornCol, int dieRow) {
//		return matrix.get(tril.cellToIndex(bornCol, dieRow));
//	}
	
	public CellStage getCellStage(int bornCol, int dieRow) {
		final int refRow = lowestBearingRow.get(); 
		if (dieRow < refRow) {
			return CellStage.Done;
		}
		if (bornCol > refRow) {
			return CellStage.Accumulating;
		}
		return jobCountByCell.get(tril.cellToIndex(bornCol, dieRow)) == 0 ? CellStage.Collaborating : CellStage.Bearing;
	}
	
	private void checkExpectedStage(int bornCol, int dieRow, CellStage expected) {
		switch (expected) {
			case Accumulating:
				if (bornCol <= lowestBearingRow.get()) {
					throw createIllegalStageException(bornCol, dieRow, expected);
				}
				return;
			case Done:
				if (dieRow >= lowestBearingRow.get()) {
					throw createIllegalStageException(bornCol, dieRow, expected);
				}
				return;
			default:
				if (!getCellStage(bornCol, dieRow).equals(expected)) {
					throw createIllegalStageException(bornCol, dieRow, expected);
				}
		}
	}
	private void checkActiveStage(int bornCol, int dieRow) {
		final int refRow = lowestBearingRow.get(); 
		if (bornCol > refRow || dieRow < refRow) {
			throw new IllegalStateException("cell " + new DefaultCellRange(bornCol, dieRow) + " is not in an active stage: " + getCellStage(bornCol, dieRow));
		}
	}
	
	private IllegalStateException createIllegalStageException(int bornCol, int dieRow, CellStage expected) {
		return new IllegalStateException("cell " + new DefaultCellRange(bornCol, dieRow) + " is not " + expected + ": " + getCellStage(bornCol, dieRow));
	}
	public void schedulePairingJobs(int bornCol, int dieRow, int activeBornColumnFrom, int activeBornColumnTo) throws IOException, IllegalStateException, IllegalArgumentException {
		final CellRange owner = new DefaultCellRange(bornCol, dieRow);
		final RectangularRange partners = new PairingRule(tril, owner).getPartnerCells();
		if (partners.getCellCount() > 0) {
			if (isEmpty(bornCol, dieRow)) {
				notifyAllJobsEmpty(owner);
			}
			else {
				checkExpectedStage(bornCol, dieRow, CellStage.Bearing);
				for (int c = activeBornColumnFrom; c < activeBornColumnTo; c++) {
					for (int r = partners.getDieRowFrom(); r < partners.getDieRowTo(); r++) {
						final CellRange partnerCell = new DefaultCellRange(c, r);
						if (isEmpty(c, r)) {
							notifyPairingJobCompleted(owner, partnerCell, 0, true);
						}
						else {
							final PairingJob<Col> job = new DefaultPairingJob<Col>(controller, owner, partnerCell);
							controller.addPairingJob(job);
						}
					}
				}
			}
		}
		else {
			throw new IllegalArgumentException("final row " + dieRow + " cannot schedule pairing jobs");
		}
	}
	public void notifyPairingJobCompleted(PairingJob<Col> job) throws IOException {
		notifyPairingJobCompleted(job.getCellRangeNeg(), job.getCellRangePos(), job.getAppendedColumnCount(), false);
	}
	private void notifyPairingJobCompleted(CellRange owner, CellRange partner, int newCount, boolean empty) throws IOException {
		final int negIndex = tril.cellToIndex(owner.getBornColumn(), owner.getDieRow());
		final int left = jobCountByCell.decrementAndGet(negIndex);
		if (empty) {
			LOG.finest("job empty for cells {neg=" + owner + ", pos=" + partner + "}, owner jobs left:" + left);
		}
		else {
			LOG.finest("job completed for cells {neg=" + owner + ", pos=" + partner + "}, new modes=" + newCount + ", owner jobs left:" + left);
		}
		if (0 == left) {
			LOG.finest("all jobs completed for cell " + owner);
			notifyAllJobsCompleted(owner);
		}
	}
	/**
	 * Notify that all pairing jobs of the specified {@code owner} cell are
	 * empty, since the cell contains no modes
	 * 
	 * @param owner	owner cell, containing dying modes of the pairing jobs
	 * @throws IOException	if an i/o exception occurs, e.g. due to file-based 
	 * 						memory
	 */
	private void notifyAllJobsEmpty(CellRange owner) throws IOException {
		if (jobCountByCell.getAndSet(tril.cellToIndex(owner.getBornColumn(), owner.getDieRow()), 0) > 0) {
			LOG.finest("all jobs empty for cell " + owner);
			notifyAllJobsCompleted(owner);
		}
	}
	/**
	 * Notify that all pairing jobs of the specified {@code owner} cell have
	 * completed successfully
	 * 
	 * @param owner	owner cell, containing dying modes of the pairing jobs
	 * @throws IOException 
	 */
	private void notifyAllJobsCompleted(CellRange owner) throws IOException {
		final int bornCol	= owner.getBornColumn();
		final int dieRow 	= owner.getDieRow();
		final int left		= bearingCellCountByRow.decrementAndGet(dieRow);
		if (0 == left) {
			LOG.finest("row " + dieRow + " completed bearing");
			notifyRowJobsCompleted(dieRow);
		}
		else {
			LOG.finest("row " + dieRow + " bearing left: " + left);
		}
		if (controller.getDebugger().doDebug()) {
			controller.getDebugger().notifyAllPairingJobsComplete(bornCol, dieRow, left);
		}		
	}
	public void notifyInitialColumnComplete() throws IOException {
		notifyRowJobsCompleted(-1);
	}
	private void notifyRowJobsCompleted(int dieRow) throws IOException {
		if (lowestBearingRow.get() == dieRow) {
			final int newBearingCol = lowestBearingRow.get() + 1;
			//convert appendable to sortable memory for the new bearing cells
			for (int r = tril.getRowFrom(newBearingCol); r < tril.getRowTo(newBearingCol); r++) {
				final int index = tril.cellToIndex(newBearingCol, r);
				final AppendableMemory<Col> appMem = appMemoriesByCell.getAndSet(index, null);
				if (appMem != null) {
					//if null, it has already been set by another terminating job
					if (!sortMemoriesByCell.compareAndSet(index, null, appMem.toSortableMemory())) {
						throw new RuntimeException("internal error: sortable memory aready set");
					}
				}
			}
			//drop columns of completed row
			for (int c = tril.getColumnFrom(dieRow); c < tril.getColumnTo(dieRow); c++) {
				final int index = tril.cellToIndex(c, dieRow);
				sortMemoriesByCell.set(index, null);
			}
			final int newLowestDieRow = dieRow + 1;
			if (lowestBearingRow.compareAndSet(dieRow, newLowestDieRow)) {
				if (tril.isFinalRow(newLowestDieRow)) {
					lowestBearingRow.incrementAndGet();
					controller.terminate();
				}
				else {
					controller.switchColumnToBearingStage(newBearingCol);
					if (0 == bearingCellCountByRow.get(newLowestDieRow)) {
						notifyRowJobsCompleted(newLowestDieRow);
					}
				}
			}			
		}
		if (dieRow >= 0) {
			controller.getDebugger().notifyRowPairingJobsComplete(dieRow);
		}
	}

	public AppendableMemory<Col> getForAppending(int bornCol, int dieRow) throws IllegalStateException, IOException {
		checkExpectedStage(bornCol, dieRow, CellStage.Accumulating);
		return appMemoriesByCell.get(tril.cellToIndex(bornCol, dieRow));
	}

	public SortableMemory<Col> getNegForGenerating(int bornCol, int dieRow) throws IllegalStateException, IOException {
		checkActiveStage(bornCol, dieRow);
		return new MappedSortableMemory<Col>(sortMemoriesByCell.get(tril.cellToIndex(bornCol, dieRow)));
	}
	public SortableMemory<Col> getPosForGenerating(int bornCol, int dieRow, int partnerDieRow) throws IllegalStateException, IOException {
		checkActiveStage(bornCol, dieRow);
		final PosMemKey key = new PosMemKey(bornCol, dieRow, partnerDieRow);
		FilteredSortablePosMemory<Col> mem = posMemoriesByNegPos.get(key);
		if (mem == null) {
			mem = new FilteredSortablePosMemory<Col>(controller, getNegForGenerating(bornCol, dieRow), partnerDieRow);
			posMemoriesByNegPos.put(key, mem);
		}
		return mem.clone();
	}

	public IterableMemory<Col> getFinal(int bornCol) throws IllegalStateException, IOException {
		final int dieRow = tril.getFinalRow();
		checkExpectedStage(bornCol, dieRow, CellStage.Done);
		return sortMemoriesByCell.get(tril.cellToIndex(bornCol, dieRow));
	}
	
	public int getColumnCount(int bornCol, int dieRow) throws IOException {
		final CellStage stage = getCellStage(bornCol, dieRow);
		switch (stage) {
			case Accumulating:
				return getForAppending(bornCol, dieRow).getColumnCount();
			case Bearing:	//fallthrough
			case Collaborating:
				final IterableMemory<Col> mem = getNegForGenerating(bornCol, dieRow);
//				return mem == null ? 0 : mem.getColumnCount();
				return mem.getColumnCount();
			case Done:
				return tril.isFinalRow(dieRow) ? getFinal(bornCol).getColumnCount() : 0;
		}
		throw new IllegalStateException("unknown cell stage: " + stage);
	}
	public boolean isEmpty(int bornCol, int dieRow) throws IOException {
		return getColumnCount(bornCol, dieRow) == 0;
	}
	
	@Override
	public String toString() {
		final int it = getIterationCount();
		final StringBuilder sb = new StringBuilder(it + "x" + it);
		try {
			for (int row = 0; row <= it; row++) {
				for (int col = 0; col <= row; col++) {
					final CellStage stage = getCellStage(col, row);
					if (col > 0) sb.append(' ');
					sb.append(stage.toChar() + "[" + row + ", " + col + "]=" + getColumnCount(col, row));
				}
				sb.append('\n');
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			sb.append(ex);
		}
		return sb.toString();
	}
	
	private static class PosMemKey {
		private final int negBornCol;
		private final int negDieRow;
		private final int posDieRow;
		public PosMemKey(int negBornCol, int negDieRow, int posDieRow) {
			this.negBornCol	= negBornCol;
			this.negDieRow	= negDieRow;
			this.posDieRow	= posDieRow;
		}
		@Override
		public int hashCode() {
			return negBornCol ^ negDieRow ^ posDieRow;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj instanceof PosMemKey) {
				final PosMemKey other = (PosMemKey)obj;
				return 
					negBornCol == other.negBornCol && 
					negDieRow == other.negDieRow && 
					posDieRow == other.posDieRow;
			}
			return false;
		}
		@Override
		public String toString() {
			return "[neg-born-col=" + negBornCol + ", neg-die-row=" + negDieRow + ", pos-die-row=" + posDieRow + "]";
		}
	}
	
}
