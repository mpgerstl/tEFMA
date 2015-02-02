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

import java.awt.Color;

import ch.javasoft.metabolic.efm.borndie.range.CellRange;


/**
 * <code>CellStage</code> represents the stages of a cell in the 
 * {@link BornDieMatrix born/die matrix}. The stages during the algorithm are:
 * <ul>
 * <li>{@link #Accumulating}:	The cell is a target for modes generated from 
 * 								other cells. The cell's dependency condition is 
 * 								not yet fullfilled.</li>
 * <li>{@link #Bearing}: 		The cell has accumulated all modes. Other cell, 
 * 								which have this cell as destination target for 
 * 								their generated modes, are no longer active. In 
 * 								other words, the dependency condition is 
 * 								fulfilled. This cell is bearing since it pairs 
 * 								itself with other cells and generates modes.
 * 								</li>
 * <li>{@link #Collaborating}: 	The cell has paired itself with all partner 
 * 								cells, but is still partner for other bearing 
 * 								cells.</li>
 * <li>{@link #Done}:			No other cell will be paired with this cell 
 * 								anymore. If it is a cell of the final row, the 
 * 								final modes (part of the algorithm's result) are 
 * 								now available. Otherwise, all modes have been 
 * 								dropped.</li>
 * </ul>
 */
public enum CellStage {
	/**
	 * In the accumulating stage, modes can be appended to a cell. During this 
	 * phase, the {@link BornDieMatrix#getForAppending(int, int)} method may be 
	 * used.
	 */
	Accumulating(Color.YELLOW),
	/**
	 * In the bearing stage, the cell pairs itself with other cells and 
	 * generates modes. During this {@link #isActive() active} stage, modes can 
	 * be read from a cell by 
	 * {@link BornDieMatrix#getNegForGenerating(int, int)} or 
	 * {@link BornDieMatrix#getPosForGenerating(int, int, int)}.
	 */
	Bearing(Color.RED),
	/**
	 * In the collaborating stage, the cell is partner of other 
	 * {@link #Bearing bearing} cells. During this {@link #isActive() active} 
	 * stage, modes can be read from a cell by 
	 * {@link BornDieMatrix#getNegForGenerating(int, int)} or
	 * {@link BornDieMatrix#getPosForGenerating(int, int, int)}.
	 */
	Collaborating(Color.GREEN),
	/**
	 * In the final phase, final modes can be read from a cell. During this 
	 * phase, the {@link BornDieMatrix#getFinal(int)} method may be used.
	 */
	Done(Color.CYAN);
	
	private final Color color;
	
	private CellStage(Color color) {
		this.color = color;
	}
	
	/**
	 * Returns true if this stage happens chronologically after the 
	 * {@code other} stage.
	 */
	public boolean isAfter(CellStage other) {
		return ordinal() > other.ordinal();
	}

	/**
	 * A cell is active in the {@link #Bearing} or 
	 * {@link #Collaborating} stage. During active stages, modes can be read 
	 * from a cell by {@link BornDieMatrix#getNegForGenerating(int, int)} or 
	 * {@link BornDieMatrix#getPosForGenerating(int, int, int)}.
	 */
	public boolean isActive() {
		return this == Bearing || this == Collaborating;
	}
	
	/**
	 * Returns the first character of the stage {@link #name() name}. Note that
	 * the first characters of the stages are A,B,C,D.
	 */
	public char toChar() {
		return name().charAt(0);
	}
	
	/**
	 * Returns the stage color, for instance for debugging
	 */
	public Color toColor() {
		return color;
	}
	
	/**
	 * Returns a string representing the cell and their stage. A sample string
	 * looks as follows:
	 * <pre>
	 * B[b=0, d=21]
	 * </pre>
	 */
	public String toString(CellRange range) {
		return toString(range.getBornColumn(), range.getDieRow());
	}
	/**
	 * Returns a string representing the cell and their stage. A sample string
	 * looks as follows:
	 * <pre>
	 * B[b=0, d=21]
	 * </pre>
	 */
	public String toString(int bornCol, int dieRow) {
		return toChar() + "[b=" + bornCol + ", d=" + dieRow + "]";		
	}
	
}
