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
package ch.javasoft.metabolic.efm.model;

import ch.javasoft.metabolic.efm.column.Column;

public interface IterationStateModel {
	/**
	 * Returns the index of the hyperplane associated with the current 
	 * iteration. During this iteration step, the returned hyperplane is used to 
	 * separate kept from removed intermediary modes.
	 * 
	 * <p>
	 * <b>Notes: </b><br>
	 * <i>Nullspace approach</i><br>
	 * For the nullspace approach, a hyperplane is associated with a reaction,
	 * i.e. with a column in the stoichiometric matrix or a row in
	 * the kernel matrix or within a {@link Column}. The index returned here is
	 * directly applicable to the numeric part of the column, i.e. it is the
	 * index for the numeric value corresponding to the hyperplane. To apply it
	 * to kernel matrix rows, the binary size of the column has to be added.
	 * Kernel matrix and columns are sorted according to
	 * {@link NetworkEfmModel#getReactionSorting() reaction sorting}. Thus, 
	 * the index must be unmapped to use it to access appropriate stoich matrix 
	 * columns, e.g. by using the {@link NetworkEfmModel#getReactionMapping() reaction mapping}.
	 * <br>
	 * <i>Canonical approach</i><br>
	 * For the canonical approach, a hyperplane is associated with a stoich
	 * matrix row. Since the stoich matrix itself is not resorted, hyperplane
	 * sorting is reflected by {@link NetworkEfmModel#getMetaboliteSorting() metabolite sorting}.
	 * The returned index performs this mapping, i.e. the index can be used to
	 * address the appropriate row in the stoich matrix.
	 * 
	 * @return 	the index of the hyperplane as described above
	 * 
	 * @see NetworkEfmModel#getHyperplaneIndex(int)
	 */
	int getHyperplaneIndex();

	/**
	 * Returns the size of the boolean part of an intermediary mode.
	 * @see NetworkEfmModel#getBooleanSize(int)
	 */
	int getBooleanSize();
	
	/**
	 * Returns the size of the numeric part of an intermediary mode
	 * @see NetworkEfmModel#getNumericSize(int)
	 */
	int getNumericSize();

}
