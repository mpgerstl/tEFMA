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
package ch.javasoft.metabolic.efm.output;

import java.io.IOException;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compress.CompressedMetabolicNetwork;

/**
 * Uses another formatter (the delegate) and performs unmapping of the reactions
 * to their original position. All write calls are passed to the delegate
 * formatter.
 */
public class UnmappingEfmOutputFormatter<W> implements EfmOutputFormatter<W> {
	
	private final MetabolicNetwork 		originalNetwork;
	private final EfmOutputFormatter<W> delegate;
	private int[] mMetaboliteMapping;
	private int[] mReactionMapping;
	
	public UnmappingEfmOutputFormatter(EfmOutputFormatter<W> delegate, MetabolicNetwork originalNetwork) {
		this.delegate 			= delegate;
		this.originalNetwork 	= originalNetwork;
	}

	public void formatHeader(EfmOutputCallback cb, W writer, EfmOutputEvent evt) throws IOException {
		MetabolicNetwork net = evt.getMetabolicNetwork();
		if (net instanceof CompressedMetabolicNetwork) {
			if (!cb.getGranularity().isUncompressionNeeded()) throw new IOException("compressed network, but uncompressed output");
			net = ((CompressedMetabolicNetwork)net).getRootNetwork();
		}
		int mlen = net.getMetabolites().length();
		int rlen = net.getReactions().length();
//		int olen = originalNetwork.getReactions().length();
		mMetaboliteMapping	= new int[mlen];
		mReactionMapping	= new int[rlen];
//		BitSet usedReacts	= new BitSet(olen);
		
		// initialize mapping
		for (int i = 0; i < mlen; i++) {
			Metabolite meta = net.getMetabolites().get(i);
			mMetaboliteMapping[i] = originalNetwork.getMetaboliteIndex(meta.getName());
		}
		for (int i = 0; i < rlen; i++) {
			Reaction reac = net.getReactions().get(i);
			int index = originalNetwork.getReactionIndex(reac.getName());
			mReactionMapping[i] = index;
			if (mReactionMapping[i] == -1) {
				throw new IOException("reaction not found in original network: " + reac.getName());
			}
//			usedReacts.set(index);
		}
		
//		// write removed reaction indices
//		int[] unused = new int[olen - usedReacts.cardinality()];
//		int index = 0;
//		for (int i = usedReacts.nextClearBit(0); i < olen; i = usedReacts.nextClearBit(i + 1)) {
//			unused[index++] = i + 1;//matlab is 1 based!
//		}
		delegate.formatHeader(cb, writer, evt);
	}

	public void formatEfmHeader(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		final int curLen = evt.getEfm().getSize();
		final FluxDistribution fluxDist = evt.getEfm().create(originalNetwork);
		for (int i = 0; i < curLen; i++) {
			Number value = evt.getEfm().getNumberRate(i);
			fluxDist.setRate(mReactionMapping[i], value);
		}
		final EfmOutputEvent mappedEvt = new EfmOutputEvent(originalNetwork, fluxDist, evt.getEfmCount());
		delegate.formatEfmHeader(cb, writer, mappedEvt, efmIndex);
		if (isEfmValueIterationNeeded(cb)) {
			for (int i = 0; i < mappedEvt.getEfm().getSize(); i++) {
				Number val = mappedEvt.getEfm().getNumberRate(i);
				delegate.formatEfmValue(cb, writer, mappedEvt, efmIndex, i, val);
			}			
		}
		delegate.formatEfmFooter(cb, writer, mappedEvt, efmIndex);
	}

	public void formatEfmValue(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value) throws IOException {
		//nothing to do, already done
	}

	public void formatEfmFooter(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		//nothing to do, already done
	}

	public void formatFooter(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long countEfms) throws IOException {
		mMetaboliteMapping 	= null;
		mReactionMapping	= null;
		delegate.formatFooter(cb, writer, evt, countEfms);
	}

	public boolean isEfmValueIterationNeeded(EfmOutputCallback cb) {
		return delegate.isEfmValueIterationNeeded(cb);
	}

}
