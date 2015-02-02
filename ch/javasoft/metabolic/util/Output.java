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
package ch.javasoft.metabolic.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.util.genarr.ArrayIterable;
import ch.javasoft.util.logging.LogWriter;

/**
 * Utility class to output metabolic network info, e.g. to print the network
 * size or print all reactions, either to a file, output stream or logger.
 */
public class Output {
	
	private final PrintWriter mPrintWriter;
	private boolean mAutoFlush = true;
	
	public Output() {
		this(System.out);
	}
	public Output(File file) throws IOException {
		this(new FileWriter(file));
	}
	public Output(OutputStream out) {
		this(new PrintWriter(new OutputStreamWriter(out)));
	}
	public Output(Logger logger, Level level) {
		this(new LogWriter(logger, level));
	}
	public Output(Writer writer) {
		this(writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer));
	}
	public Output(PrintWriter printWriter) {
		mPrintWriter = printWriter;
	}
	public final void setAutoFlush(boolean set) {
		mAutoFlush = set;
	}
	public final boolean isAutoFlush() {
		return mAutoFlush;
	}
	public final PrintWriter writer() {
		return mPrintWriter;
	}
	
	/**
	 * Prints reaction names and formulas, separated by colon and tab. If 
	 * desired, reactions can be sorted by name, and a newline can be appended
	 * after each reaction.
	 */
	public void printReactions(MetabolicNetwork net, boolean sort, boolean newLineAfterReaction) {
		printReactions(net.getReactions().toGenericArray(false), sort, newLineAfterReaction);
	}
	/**
	 * Prints reaction names and formulas, separated by colon and tab. If 
	 * desired, reactions can be sorted by name, and a newline can be appended
	 * after each reaction.
	 */
	public void printReactions(Collection<? extends Reaction> reactions, boolean sort, boolean newLineAfterReaction) {
		Iterable<? extends Reaction> it;
		if (sort) {
			List<Reaction> list = new ArrayList<Reaction>(reactions);//clone
			Collections.sort(list, new Comparator<Reaction>() {
				private final Collator mCollator = Collator.getInstance(Locale.US);
				public int compare(Reaction o1, Reaction o2) {
					return mCollator.compare(o1.getName(), o2.getName());
				}
			});
			it = list;
		}
		else {
			it = reactions;
		}
		for (Reaction reac : it) {
			printReaction(reac, newLineAfterReaction);
		}
		if (mAutoFlush) flush();
	}
	/**
	 * Prints reaction name and formula, separated by colon and tab. If 
	 * desired, the print statement is terminated with a newline string.
	 */
	public void printReaction(Reaction reac, boolean newLineAfterReaction) {
		mPrintWriter.print(reac.getName() + ":\t");
		mPrintWriter.print(reac);
		if (newLineAfterReaction) mPrintWriter.println();
		else mPrintWriter.print(", ");		
	}
	/**
	 * Prints metabolite names for the network. If desired, metabolites are
	 * previously sorted alphabetically, and a newline can be appended after 
	 * each metabolite.
	 */
	public void printMetabolites(MetabolicNetwork net, boolean sort, boolean newLineAfterMetabolite) {
		printMetabolites(net.getMetabolites().toGenericArray(false), sort, newLineAfterMetabolite);
	}
	/**
	 * Prints metabolite names for the network. If desired, metabolites are
	 * previously sorted alphabetically, and a newline can be appended after 
	 * each metabolite.
	 */
	public void printMetabolites(Collection<? extends Metabolite> metabolites, boolean sort, boolean newLineAfterMetabolite) {
		Iterable<? extends Metabolite> it;
		if (sort) {
			List<Metabolite> list = new ArrayList<Metabolite>(metabolites);//clone
			Collections.sort(list, new Comparator<Metabolite>() {
				private final Collator mCollator = Collator.getInstance(Locale.US);
				public int compare(Metabolite r1, Metabolite r2) {
					return mCollator.compare(r1.getName(), r2.getName());
				}
			});
			it = list;
		}
		else {
			it = metabolites;
		}
		for (Metabolite meta : it) {
			mPrintWriter.print(meta);
			if (newLineAfterMetabolite) mPrintWriter.println();
			else mPrintWriter.print(", ");
		}
		if (mAutoFlush) flush();
	}
	
	/**
	 * Prints the network size (#metabolites, #reactions, #reversible reactions),
	 * prepending the given prefix and appending newline.
	 * 
	 * @see MetabolicNetworkUtil#getNetworkSizeString(String, MetabolicNetwork)
	 */
	public void printNetworkSize(String prefix, MetabolicNetwork net) {
		mPrintWriter.println(MetabolicNetworkUtil.getNetworkSizeString(prefix, net));
		if (mAutoFlush) flush();
	}
	
	@SuppressWarnings("all")
	/**
	 * print the flux modes in the format which is parsable by
	 * {@link ch.javasoft.metabolic.parse.FluxAnalyserParser#parseEfms(MetabolicNetwork, Reader, int, boolean)}
	 * 
	 * @param fluxModes The modes to write to the output
	 */
	public void printFluxModes(Iterable<FluxDistribution> fluxModes) {
		printFluxModesInternal(fluxModes);
	}
	private void printFluxModesInternal(Iterable<FluxDistribution> fluxModes) {
		Iterator<FluxDistribution> it = fluxModes.iterator();
		if (!it.hasNext()) return;
		MetabolicNetwork net = it.next().getNetwork();
		ArrayIterable<? extends Reaction> reacts = net.getReactions();
		for (int ii = 0; ii < reacts.length(); ii++) {
			if (ii > 0) mPrintWriter.print("\t");
			mPrintWriter.print(reacts.get(ii).getName());
		}
		mPrintWriter.println();
		for (FluxDistribution flux : fluxModes) {
			if (flux.getSize() != reacts.length()) {
				throw new IllegalArgumentException(
					"flux count (" + flux.getSize() + ") not equal to number of reactions (" + 
					reacts.length() + ")"
				);
			}
			for (int ii = 0; ii < flux.getSize(); ii++) {
				if (ii > 0) mPrintWriter.print("\t");
				mPrintWriter.print(flux.getNumberRate(ii));
			}
			mPrintWriter.println();
		}
		if (mAutoFlush) flush();
	}
	
	public void flush() {
		mPrintWriter.flush();
	}
	
}
