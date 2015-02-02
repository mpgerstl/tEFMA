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
package ch.javasoft.metabolic.efm.borndie.debug;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import ch.javasoft.metabolic.efm.borndie.BornDieController;
import ch.javasoft.metabolic.efm.borndie.job.JobManager;
import ch.javasoft.metabolic.efm.borndie.job.PairingJob;
import ch.javasoft.metabolic.efm.borndie.matrix.BornDieMatrix;
import ch.javasoft.metabolic.efm.borndie.range.CellRange;
import ch.javasoft.metabolic.efm.borndie.range.LowerTriangularMatrix;
import ch.javasoft.metabolic.efm.memory.IterableMemory;
import ch.javasoft.util.Null;

/**
 * The <code>SwingDebugger</code> visually displays algorithm status information 
 * in a {@link JFrame swing frame}.
 */
public class SwingDebugger implements Debugger {
	
	private static enum Ask {
		ColumnAppended("Add Col", false), 
		PairingQueued("New Job", false), PairingComplete("Job Done", false), 
		AllPairingJobsComplete("All Jobs", false),
		RowPairingJobsComplete("Bearing row", true),
		RowJobsComplete("Row Jobs", false),
		Exception("Error", true), Terminate("End", true);
		
		public final String caption;
		public final boolean defValue;
		
		private Ask(String caption, boolean defValue) {
			this.caption 	= caption;
			this.defValue	= defValue;
		}
		public boolean isToggled() {
			return !(Exception.equals(this) || Terminate.equals(this));
		}
	};
	
	private final JFrame 				frame;
	private final JTable				table;
	private final JLabel				lblStatus;
	private final JButton				btnContinue;
	private final JCheckBox[]			chkAsk;
	private final JTextArea				txtArea;
	private final BornDieController		controller;
	private final LowerTriangularMatrix tril;
	private final AbstractTableModel	tableModel;
	
	private final AtomicIntegerArray						cellModesAdded;
	private final AtomicIntegerArray						cellModesBorn;
	private final AtomicIntegerArray						bearingCellCounts;
	private final List<ConcurrentHashMap<PairingJob, Null>>	cellJobs;
	private final AtomicReference<Exception> 				exception;
	private final AtomicInteger								jobCount;
	private final AtomicInteger								jobCountNoPos;
	private final AtomicReference<Ask> 						waitFor;
	
	public SwingDebugger(final BornDieController controller, final LowerTriangularMatrix tril) {
		final int cellCnt = tril.getCellCount();
		this.frame 				= new JFrame("Born/Die Debugger");
		this.lblStatus 			= new JLabel("Ok.");
		this.btnContinue		= new JButton(new AbstractAction("Continue") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				if (waitFor.get() != null) {
					btnContinue.setEnabled(false);
					waitFor.set(null);
				}
			}
		});
		this.txtArea 			= new JTextArea();
		this.controller			= controller;
		this.tril				= tril;
		this.cellModesAdded 	= new AtomicIntegerArray(cellCnt);
		this.cellModesBorn	 	= new AtomicIntegerArray(cellCnt);
		this.bearingCellCounts	= new AtomicIntegerArray(tril.getRowCount());
		this.cellJobs			= new ArrayList<ConcurrentHashMap<PairingJob, Null>>(cellCnt);
		this.exception 			= new AtomicReference<Exception>();
		this.jobCount			= new AtomicInteger();
		this.jobCountNoPos		= new AtomicInteger();
		this.waitFor			= new AtomicReference<Ask>();
		for (int i = 0; i < cellCnt; i++) {
			cellJobs.add(new ConcurrentHashMap<PairingJob, Null>());
		}
		for (int r = 0; r < tril.getRowCount(); r++) {
			bearingCellCounts.set(r, tril.getColumnTo(r) - tril.getColumnFrom(r));
		}
		tableModel = new DebuggerTableModel();
		table = new JTable(tableModel);
		table.addMouseListener(new DebuggerMouseListener());
		btnContinue.setEnabled(false);
		final JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(txtArea));
		table.setDefaultRenderer(Object.class, new DebuggerTableCellRenderer(controller));
		chkAsk = new JCheckBox[Ask.values().length];
		final JPanel pnlCheck = new JPanel(new FlowLayout());
		final JCheckBox chkToggle = new JCheckBox();
		chkToggle.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				final boolean select = chkToggle.isSelected();
				for (final Ask ask : Ask.values()) {
					if (ask.isToggled()) {
						chkAsk[ask.ordinal()].setSelected(select);
					}
				}
			}
		});
		chkToggle.setSelected(true);
		pnlCheck.add(chkToggle);
		for (final Ask ask : Ask.values()) {
			pnlCheck.add(chkAsk[ask.ordinal()] = new JCheckBox());
			chkAsk[ask.ordinal()].setSelected(ask.defValue);
			pnlCheck.add(new JLabel(ask.caption));
		}
		final JPanel pnlNorth = new JPanel(new BorderLayout());
		pnlNorth.add(pnlCheck, BorderLayout.NORTH);
		pnlNorth.add(lblStatus, BorderLayout.CENTER);
		pnlNorth.add(btnContinue, BorderLayout.EAST);
		frame.setLayout(new BorderLayout());		
		frame.add(pnlNorth, BorderLayout.NORTH);
		frame.add(pane, BorderLayout.CENTER);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setAlwaysOnTop(true);
		pane.setDividerLocation(0.75);
		frame.addWindowListener(new DebuggerWindowListener());
		frame.setVisible(true);
	}
	
	private class DebuggerWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (Ask.Terminate.equals(waitFor.get())) {
				waitFor.set(null);
			}
			else {
				System.exit(0);
			}
		}
	}
	
	private class DebuggerTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		public int getRowCount() {
			return tril.getRowCount();
		}
		public int getColumnCount() {
			return tril.getColumnCount();
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			final BornDieMatrix matrix = controller.getMatrix();				
			if (columnIndex > rowIndex) return null;
			if (matrix == null) return "0";
			try {
				final int brn = cellModesBorn.get(tril.cellToIndex(columnIndex, rowIndex));
				final int cur = matrix.getColumnCount(columnIndex, rowIndex);					
				if (cur > 0) {
					return cur + (brn > 0 ? ":" + brn : "");
				}
				final int add = cellModesAdded.get(tril.cellToIndex(columnIndex, rowIndex));
				if (add == 0) return "0";
				return add + (brn > 0 ? ":" + brn : "");
			} 
			catch (IOException e) {
				return e;
			}
		}
	}

	private class DebuggerMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			final int row = table.rowAtPoint(e.getPoint());
			final int col = table.columnAtPoint(e.getPoint());
			txtArea.setText(null);
			if (row >= 0 && col >= 0 && col <= row) {
				try {
					txtArea.setText(controller.getMatrix().getCellStage(col, row).toString(col, row));
					if (e.getClickCount() > 1) {
						final BornDieMatrix<?> mx = controller.getMatrix();
						if (!mx.isEmpty(col, row)) {
							final Iterable it;
							switch (mx.getCellStage(col, row)) {
							case Accumulating:
								it = mx.getForAppending(col, row);
								break;
							case Bearing:
							case Collaborating:
								it = mx.getNegForGenerating(col, row);
								break;
							case Done:
								it = mx.getFinal(col);
								break;
							default:
								return;
							}
							int mind = 0;
							for (final Object mode : it) {
								txtArea.append("\nmode[" + mind + "]=" + mode);
								mind++;
							}
						}
					}
					else {
						final int index = tril.cellToIndex(col, row);
						txtArea.append("\nbearing cells in row: " + bearingCellCounts.get(row));
						for (final PairingJob job : cellJobs.get(index).keySet()) {
							txtArea.append("\n" + job.toString());
						}
					}
				}
				catch (Exception ex) {
					txtArea.append("\n" + e);						
				}
			}
		}
	}
	
	public boolean doDebug() {
		return true;
	}

	public void notifyColumnAppended(int bornColumn, int dieRow) {
		final int index = tril.cellToIndex(bornColumn, dieRow);
		cellModesAdded.incrementAndGet(index);
		fireTableCellUpdated(dieRow, bornColumn);
		waitToContinue(Ask.ColumnAppended);
	}
	
	private void fireTableCellUpdated(final int row, final int column) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableModel.fireTableCellUpdated(row, column);
			}
		});
	}
	private void fireTableUpdated() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableModel.fireTableDataChanged();
			}
		});
	}

	public void notifyPairingQueued(PairingJob job) {
		final CellRange range = job.getCellRangeNeg();
		final int index = tril.cellToIndex(range.getBornColumn(), range.getDieRow());
		cellJobs.get(index).put(job, Null.INSTANCE);
		waitToContinue(Ask.PairingComplete, job.toString());
	}
	public void notifyPairingComplete(PairingJob job) {
		final CellRange range = job.getCellRangeNeg();
		final int index = tril.cellToIndex(range.getBornColumn(), range.getDieRow());
		cellJobs.get(index).remove(job);
		jobCount.incrementAndGet();
		try {
			cellModesBorn.addAndGet(index, job.getAppendedColumnCount());
			if (job.getMemoryPos().getColumnCount() == 0) {
				jobCountNoPos.incrementAndGet();
			}
			waitToContinue(Ask.PairingQueued, job.toString());
		} 
		catch (IOException e) {
			waitToContinue(Ask.PairingQueued, e.toString());
		}
	}
	public void notifyAllPairingJobsComplete(int bornCol, int dieRow, int leftForRow) {
		int old = bearingCellCounts.get(dieRow);
		while (old > leftForRow) {
			if (!bearingCellCounts.compareAndSet(dieRow, old, leftForRow)) {
				old = bearingCellCounts.get(dieRow);
			}
		}
		waitToContinue(Ask.AllPairingJobsComplete);
		if (leftForRow == 0) {
			waitToContinue(Ask.RowJobsComplete);			
		}
	}
	public void notifyRowPairingJobsComplete(int dieRow) {
		bearingCellCounts.set(dieRow, 0);
		fireTableUpdated();
		waitToContinue(Ask.RowPairingJobsComplete);
	}
	public void notifyException(Exception ex) {
		if (exception.compareAndSet(null, ex)) {
			ex.printStackTrace();
			waitToContinue(Ask.Exception, ex.toString());
		}
	}
	private void setStatus(String msg) {
		lblStatus.setText(msg);
		lblStatus.invalidate();
		frame.validate();		
	}
	public void notifyTerminate(JobManager jobManager, IterableMemory result) {
		String msg;
		try {
			msg = result.getColumnCount() + " modes, jobs=" + jobCount + ", empty=" + jobCountNoPos;
		}
		catch (Exception e) {
			msg = e.toString();
		}
		waitToContinue(Ask.Terminate, msg);
	}
	private void waitToContinue(Ask ask) {
		waitToContinue(ask, null);
	}
	private void waitToContinue(Ask ask, String msg) {
		final boolean stop;
		synchronized(this) {
			stop = chkAsk[ask.ordinal()].isSelected();
		}
		if (stop) {
			while (!waitFor.compareAndSet(null, ask)) {
				sleep(100);
			}
			setStatus("Stop: " + ask.caption + (msg == null ? "" : " (" + msg + ")"));
			btnContinue.setEnabled(true);
			while (waitFor.get() != null) {
				sleep(100);
			}		
		}
		else {
			if (msg != null) {
				setStatus(msg);
			}
		}
	}
	private static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}

}
