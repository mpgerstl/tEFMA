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

import ch.javasoft.metabolic.efm.borndie.range.CellRange;
import ch.javasoft.metabolic.efm.borndie.range.DefaultRectangularRange;
import ch.javasoft.metabolic.efm.borndie.range.LowerTriangularMatrix;
import ch.javasoft.metabolic.efm.borndie.range.RectangularRange;

/**
 * The <code>PairingRule</code> defines partner cells for a cell. Partner cells
 * contain surviving modes. The cell associated with this pairing rule contains
 * dying modes. It pairs itself with the partner cells.
 */
public class PairingRule {

	private final LowerTriangularMatrix	matrix;
	private final CellRange 			cell;
	/**
	 * Constructor for <code>PairingRule</code>
	 */
	public PairingRule(LowerTriangularMatrix matrix, CellRange cell) {
		if (!matrix.contains(cell)) {
			throw new IllegalArgumentException("cell not contained in born/die matrix: " + cell + " not in " + matrix);
		}
		this.matrix	= matrix;
		this.cell	= cell;
	}
	
	/**
	 * Returns the number of iterations
	 */
	public int getIterationCount() {
		return matrix.getRowCount() - 1;
	}

	/**
	 * Returns the partner cells. For a cell {@code [b, d]}, partner cells
	 * are {@code [0:d, d+1:n]}, where {@code n} denotes the number of 
	 * iterations.
	 */
	public RectangularRange getPartnerCells() {
		return new DefaultRectangularRange(0, cell.getDieRow() + 1, cell.getDieRow() + 1, getIterationCount() + 1);
	}
	
}
