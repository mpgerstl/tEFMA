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
import java.util.BitSet;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compress.CompressedMetabolicNetwork;

/**
 * The unmapping efm processor maps the efm entries back to the position 
 * of the original network.
 */
public class UnmappingEfmProcessor<W> implements EfmProcessor<W> {

	private final EfmProcessor<W>	mDelegate;
	private final MetabolicNetwork	mOriginalNetwork;
	
	private int[] mMetaboliteMapping;
	private int[] mReactionMapping;
	
	public UnmappingEfmProcessor(EfmProcessor<W> delegate, MetabolicNetwork originalNetwork) {
		mOriginalNetwork	= originalNetwork;
		mDelegate			= delegate;
	}

	public int[] initialize(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmCount) throws IOException {		
		MetabolicNetwork net = evt.getMetabolicNetwork();
		if (net instanceof CompressedMetabolicNetwork) {
			if (!cb.getGranularity().isUncompressionNeeded()) throw new IOException("compressed network, but uncompressed output");
			net = ((CompressedMetabolicNetwork)net).getRootNetwork();
		}
		int mlen = net.getMetabolites().length();
		int rlen = net.getReactions().length();
		int olen = mOriginalNetwork.getReactions().length();
		mMetaboliteMapping	= new int[mlen];
		mReactionMapping	= new int[rlen];
		BitSet usedReacts	= new BitSet(olen);
		
		// initialize mapping
		for (int i = 0; i < mlen; i++) {
			Metabolite meta = net.getMetabolites().get(i);
			mMetaboliteMapping[i] = mOriginalNetwork.getMetaboliteIndex(meta.getName());
//			if (mMetaboliteMapping[i] == -1) {
//				throw new IOException("metabolite not found in original network: " + meta.getName());
//			}
		}
		for (int i = 0; i < rlen; i++) {
			Reaction reac = net.getReactions().get(i);
			int index = mOriginalNetwork.getReactionIndex(reac.getName());
			mReactionMapping[i] = index;
			if (mReactionMapping[i] == -1) {
				throw new IOException("reaction not found in original network: " + reac.getName());
			}
			usedReacts.set(index);
		}
		
		// write removed reaction indices
		int[] unused = new int[olen - usedReacts.cardinality()];
		int index = 0;
		for (int i = usedReacts.nextClearBit(0); i < olen; i = usedReacts.nextClearBit(i + 1)) {
			unused[index++] = i + 1;//matlab is 1 based!
		}
		mDelegate.initialize(cb, writer, new EfmOutputEvent(EfmOutputEvent.Kind.PRE, mOriginalNetwork, efmCount), efmCount);
		return unused;
	}

	public int addEfm(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		final int curLen = evt.getEfm().getSize();
		FluxDistribution fluxDist = evt.getEfm().create(mOriginalNetwork);
		for (int i = 0; i < curLen; i++) {
			Number value = evt.getEfm().getNumberRate(i);
			fluxDist.setRate(mReactionMapping[i], value);
		}
		return mDelegate.addEfm(cb, writer, new EfmOutputEvent(mOriginalNetwork, fluxDist, evt.getEfmCount()), efmIndex);
	}
	
	public void finalize(EfmOutputCallback cb, W writer, EfmOutputEvent evt) throws IOException {
		mDelegate.finalize(cb, writer, evt);
	}
}
