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

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.math.BigFraction;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.numeric.Zero;

/**
 * Default implementation of most {@link EfmModel efm models}.
 */
public class DefaultEfmModel implements EfmModel {
	private final Config config;
	/** stoichiometric matrix*/
//	private final ReadableMatrix 					stoichiometricMatrix;
	private final ReadableMatrix<BigFraction>		stoichiometricMatrixRational;
	private final int 								stoichRank;
	private final int[]								reactionSorting;
	private final int[]								metaboliteSorting;
	private final ColumnInspectorModifierFactory	columnInspectorModifierFactory;
	
	private volatile transient ReadableMatrix<?>	stoichMatrixTyped; 				

	protected static class Init {
		protected Config							config;
//		protected ReadableMatrix 					stoichiometricMatrix;
		protected ReadableMatrix<BigFraction>		stoichiometricMatrixRational;
		protected int								stoichRank = -1;
		protected int[]								reactionSorting;
		protected int[]								metaboliteSorting;
		protected ColumnInspectorModifierFactory	columnInspectorModifierFactory;
		
		public Init() {
			super();
		}
		private <O> O notnull(O obj) {
			if (obj == null) throw new NullPointerException();
			return obj;
		}
		private int notneg(int value) {
			if (value < 0) throw new IllegalStateException("value not initialized: " + value);
			return value;
		}
	}
	protected <N extends Number, Col extends Column> DefaultEfmModel(Init init) {
		this.config		 					= init.notnull(init.config);
//		this.stoichiometricMatrix			= init.notnull(init.stoichiometricMatrix);
		this.stoichiometricMatrixRational	= init.notnull(init.stoichiometricMatrixRational);
		this.stoichRank						= init.notneg(init.stoichRank);
		this.reactionSorting				= init.notnull(init.reactionSorting);
		this.metaboliteSorting				= init.notnull(init.metaboliteSorting);
		this.columnInspectorModifierFactory	= init.notnull(init.columnInspectorModifierFactory);
//		this.columnFilter					= init.notnull(init.columnFilter);
//		this.columnPairFilter				= init.notnull(init.columnPairFilter);
	}

	public <N extends Number> ReadableMatrix<N> getStoichiometricMatrix(ColumnHome<N, ?> columnHome) {
		if (stoichMatrixTyped == null) {
			stoichMatrixTyped = columnHome.convertMatrix(stoichiometricMatrixRational, true /*allowRowScaling*/, false /*allowColumnScaling*/); 
		}
		return columnHome.castMatrix(stoichMatrixTyped);
	}
	
	public Config getConfig() {
		return config;
	}
	public Arithmetic getArithmetic() {
		return config.getArithmetic();
	}
	/**
	 * Default implementation returns {@link #getConfig() config}.{@link Config#getMaxThreads() getMaxThreads()}
	 */
	public int getAdjEnumThreads() {
		return config.getMaxThreads();
	}
	public ColumnInspectorModifierFactory getColumnInspectorModifierFactory() {
		return columnInspectorModifierFactory;
	}
	public int[] getReactionSorting() {
		return reactionSorting;
	}
	public int[] getMetaboliteSorting() {
		return metaboliteSorting;
	}
	
	///////////////////////////////////////////////////////
	public int[] getColMapping() {
		return reactionSorting;
	}

	public int getRequiredRank() {
		return stoichiometricMatrixRational.getColumnCount() - 2;
	}

	public int getRequiredCardinality() {
		return getRequiredRank() - getStoichRank();		
	}

//	public ReadableMatrix getStoich() {
//		return stoichiometricMatrix;
//	}
	public ReadableMatrix<BigFraction> getStoichRational() {
		return stoichiometricMatrixRational;
	}
	public int getStoichRank() {
		return stoichRank;
	}

	public Zero zero() {
		return config.zero();
	}
	///////////////////////////////////////////////////////
	
	public <N extends Number> void log(ColumnHome<N, ?> columnHome, Logger logger) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("efm-model:");
			logger.fine("..kind             : " + getClass().getSimpleName());
			logger.fine("..arithmetic       : " + config.getArithmetic().getNiceName());
			logger.fine("..adj enum threads : " + getAdjEnumThreads());
			logger.fine("..stoich type      : " + stoichiometricMatrixRational.getClass().getSimpleName());
			logger.fine("..stoich rank      : " + stoichRank);
			logger.fine("..reaction sorting : " + Arrays.toString(reactionSorting));
			logger.fine("..metabolite sort. : " + Arrays.toString(metaboliteSorting));
			logger.fine("..col i/m factory  : " + columnInspectorModifierFactory.getClass().getSimpleName());
		}
	}

}
