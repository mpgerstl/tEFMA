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
package ch.javasoft.metabolic.efm.dist.impl.adj;

import java.io.IOException;
import java.net.InetAddress;

import ch.javasoft.metabolic.efm.adj.AbstractAdjEnum;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.config.DistributedConfig;
import ch.javasoft.metabolic.efm.dist.DistributedAdjEnum;
import ch.javasoft.metabolic.efm.dist.DistributedInfo;
import ch.javasoft.metabolic.efm.dist.PartIterator;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;

/**
 * The <code>PseudoDistributingMemAdjEnum</code> delegates the real work
 * to {@link DistributedAdjEnum} instances, but not really distributes
 * processing. It is used if the number of adjacency candidates is below the
 * configured {@link DistributedConfig#getCandidateThreashold() threshold}
 * for parallelization. 
 * <p>
 * This class can also be used to test {@link DistributedAdjEnum} 
 * implementations on a single machine.
 */
public class PseudoDistributingAdjEnum extends AbstractAdjEnum {
	
	public static final String NAME = "pseudo-dist";
	
	private final DistributedAdjEnum delegate;
	
	/**
	 * Constructor for <code>PseudoDistributingMemAdjEnum</code> with delegate
	 */
	public PseudoDistributingAdjEnum(DistributedAdjEnum delegate) {
		super(NAME + ":" + delegate.name());
		this.delegate = delegate;
	}

	/**
	 * Delegates to 
	 * {@link DistributedAdjEnum#execCentralized(ColumnHome, Config, EfmModel, AdjEnumModel)}
	 * and subsequently to
	 * {@link DistributedAdjEnum#execDistributed(ColumnHome, Config, EfmModel, AdjEnumModel, DistributedInfo, PartIterator, ProgressAggregator)}
	 */
	public <Col extends Column, N extends Number> void adjacentPairs(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> itModel)throws IOException {
                System.out.println("in PseudoDistributingAdjEnum.adjacentPairs()");
		delegate.execCentralized(columnHome, getConfig(), getEfmModel(), itModel);
		final DistributedInfo distInfo = new DistributedInfo(1, 0, InetAddress.getLocalHost().getHostName(), 1);
		delegate.execDistributed(columnHome, getConfig(), getEfmModel(), itModel, distInfo, new PartIterator() {
			private volatile int part = 0;
			public int getNextPart() throws IOException {
				if (part == 0) {
					part = -1;
					return 0;
				}
				return -1;
			}
		}, null);
	}

}
