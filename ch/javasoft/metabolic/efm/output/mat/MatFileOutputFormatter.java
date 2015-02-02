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
package ch.javasoft.metabolic.efm.output.mat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.javasoft.metabolic.Annotation;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compartment.CompartmentMetabolite;
import ch.javasoft.metabolic.compress.CompressedMetabolicNetwork;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.output.EfmOutputFormatter;
import ch.javasoft.metabolic.efm.output.EfmProcessor;
import ch.javasoft.metabolic.efm.output.UnmappingEfmProcessor;
import ch.javasoft.metabolic.util.StoichiometricMatrices;
import ch.javasoft.util.IntArray;

/**
 * Writes multiple matlab files, e.g. due to matrix size larger than 2GB. 
 */
public class MatFileOutputFormatter implements EfmOutputFormatter<PartitionedMatFileWriter> {
	
	protected final MetabolicNetwork			mOriginalNetwork;//might be null
	protected final EfmProcessor<MatFileWriter> mEfmProcessor;
	
	public MatFileOutputFormatter() {
		this(null, new MatReservedVariableEfmProcessor());
	}
	public MatFileOutputFormatter(MetabolicNetwork originalNetwork) {
		this(originalNetwork, new UnmappingEfmProcessor<MatFileWriter>(new MatReservedVariableEfmProcessor(), originalNetwork));
	}
	public MatFileOutputFormatter(MetabolicNetwork originalNetwork, EfmProcessor<MatFileWriter> efmProcessor) {
		mOriginalNetwork	= originalNetwork;
		mEfmProcessor 		= efmProcessor;
	}
	protected MetabolicNetwork getOriginalNetwork(EfmOutputCallback cb, EfmOutputEvent evt) {
		MetabolicNetwork net = mOriginalNetwork;
		if (net == null) {
			net = evt.getMetabolicNetwork();
			if (net instanceof CompressedMetabolicNetwork && cb.getGranularity().isUncompressionNeeded()) {
				net = ((CompressedMetabolicNetwork)net).getRootNetwork();
			}			
		}
		return net;
	}
	public void formatHeader(EfmOutputCallback cb, PartitionedMatFileWriter writer, EfmOutputEvent evt) throws IOException {
		//write initial network stuff
		writer.getPartMatFileWriter(cb, evt, 0L, getOriginalNetwork(cb, evt), mEfmProcessor);
	}
	public void formatEfmHeader(EfmOutputCallback cb, PartitionedMatFileWriter writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		MatFileWriter matWriter = writer.getPartMatFileWriter(cb, evt, efmIndex, getOriginalNetwork(cb, evt), mEfmProcessor);//write initial network stuff
		mEfmProcessor.addEfm(cb, matWriter, evt, efmIndex);
	}

	public void formatEfmValue(EfmOutputCallback cb, PartitionedMatFileWriter writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value) throws IOException {
		// not called
		throw new IOException("internal error: formatEfmValue() should not be called");
	}
	public void formatEfmFooter(EfmOutputCallback cb, PartitionedMatFileWriter writer, EfmOutputEvent evt, long efmIndex) throws IOException {
		// no output here
	}
	public void formatFooter(EfmOutputCallback cb, PartitionedMatFileWriter writer, EfmOutputEvent evt, long countEfms) throws IOException {
		writer.close(cb, evt, countEfms, mEfmProcessor);
	}

	public boolean isEfmValueIterationNeeded(EfmOutputCallback cb) {
		return false;
	}

	protected static void writeNetworkFootPrint(MetabolicNetwork net, MatFileWriter writer, String fileNameWithoutEnding, String partFileName, int partIndex, int partCount) {
//		writer.write("modelName", net.);
//		writer.write("version", metaNames);
//		writer.write("date", metaNames);
//		writer.write("organism", metaNames);
//		writer.write("biomassReaction", metaNames);
		writeAnnotations(net, writer, fileNameWithoutEnding, partFileName, partIndex, partCount);
		
		//compartments and metabolites
		Map<String, Integer> compartmentNames = new LinkedHashMap<String, Integer>();
		IntArray metaboliteCompartments = new IntArray();		
		String[] metaNames = new String[net.getMetabolites().length()];
		for (int i = 0; i < metaNames.length; i++) {
			final Metabolite meta = net.getMetabolites().get(i); 
			metaNames[i] = meta.getName();
			String cmpName = "Default";
			if (meta instanceof CompartmentMetabolite) {
				cmpName = ((CompartmentMetabolite)meta).getCompartment();
			}
			Integer index = compartmentNames.get(cmpName);
			if (index == null) {
				compartmentNames.put(cmpName, index = Integer.valueOf(compartmentNames.size() + 1));
			}
			metaboliteCompartments.add(index.intValue());
		}
		//write compartmentNames
		writer.write("compartmentNames", compartmentNames.keySet().toArray(new String[compartmentNames.size()]));
		//write metaboliteNames
		writer.write("metaboliteNames", metaNames);		
		//write metaboliteCompartments
		writer.write("metaboliteCompartments", metaboliteCompartments.toArray());
		//write unbalancedMetabolites		
		writer.write("unbalancedMetabolites", IntArray.EMPTY_ARRAY);

		//write reactions (names, formulas, bounds, exchange)
		IntArray exchangeIndices = new IntArray();
		final int rlen = net.getReactions().length();
		String[] reacNames	= new String[rlen];
		String[] reacForms	= new String[rlen];
		double[] reacLb		= new double[rlen];	
		double[] reacUb		= new double[rlen];	
		for (int i = 0; i < rlen; i++) {
			Reaction reac = net.getReactions().get(i);
			reacNames[i]	= reac.getName();
			reacForms[i]	= reac.toString();
			reacLb[i]		= reac.getConstraints().getLowerBound();
			reacUb[i]		= reac.getConstraints().getUpperBound();
			if (reac.isExternal()) {
				exchangeIndices.add(i + 1);
			}
		}
		writer.write("reactionNames", reacNames);
		writer.write("reactionFormulas", reacForms);
		writer.write("reactionLowerBounds", reacLb);
		writer.write("reactionUpperBounds", reacUb);
		writer.write("exchangeReactions", exchangeIndices.toArray());

		//write stoichiometric matrix
		writeStoich(net, writer);

		// "efms" 
		//written in MatReservedVariableEfmProcessor.initialize()
		//called by DefaultPartitionedMatFileWriter.createAndInitWriter()
		// "disabledReactions"
		//written in DefaultPartitionedMatFileWriter.createAndInitWriter()
		
	}
	private static void writeStoich(MetabolicNetwork net, MatFileWriter writer) {
		double[][] stoich = StoichiometricMatrices.createStoichiometricMatrix(net);
		writer.write("stoichSubstrates", getSubStoich(stoich, -1));
		writer.write("stoichProducts", getSubStoich(stoich, +1));
		writer.write("stoich", stoich);
	}
	
	private static double[][] getSubStoich(double[][] stoich, int sgn) {
		final double[][] subStoich = new double[stoich.length][];
		for (int r = 0; r < subStoich.length; r++) {
			subStoich[r] = new double[stoich[r].length];
			for (int c = 0; c < subStoich[r].length; c++) {
				subStoich[r][c] = stoich[r][c];
				if (sgn * Math.signum(subStoich[r][c]) < 0) {
					subStoich[r][c] = 0;
				}
			}
		}
		return subStoich;
	}
	private static void writeAnnotations(MetabolicNetwork net, MatFileWriter writer, String fileNameWithoutEnding, String partFileName, int partIndex, int partCount) {
		for (final Annotation annot : Annotation.values()) {
			Object value = annot.getAnnotation(net);
			if (value != null) {
				annot.checkValue(value);
			}
			switch (annot) {
				case ModelName:
					if (value == null) value = fileNameWithoutEnding;
					//fallthrough
				case Organism:	//fallthrough
				case Version:	//fallthrough
					if (value == null) value = "";
					writer.write(annot.getMnetName(), (String)value);
					break;
				case Date:
					if (value == null) value = new Date();
					final DateFormat fmt = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss");
//					writer.write(annot.getMnetName(), "Created by efmtool on " + fmt.format(value));
					writer.write(annot.getMnetName(), "File " + partFileName + 
							" (" +(partIndex + 1) + " of " + partCount + 
							") created by efmtool on " + fmt.format(value));
					break;
				case BiomassReaction:
					if (value == null) {
						writer.write(annot.getMnetName(), IntArray.EMPTY_ARRAY);
					}
					else {
						final int index = net.getReactionIndex(((Reaction)value).getName());
						if (index == -1) throw new IllegalStateException("unknown reaction in network: " + value);
						writer.write(annot.getMnetName(), index + 1);
					}
					break;
				default:
					throw new RuntimeException("unknown annotation: " + annot);
			}
		}
	}
	
}
