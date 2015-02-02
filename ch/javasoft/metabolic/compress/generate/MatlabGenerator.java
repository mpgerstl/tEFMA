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
package ch.javasoft.metabolic.compress.generate;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.compress.CompressionUtil;
import ch.javasoft.metabolic.compress.DuplicateGeneCompressor;
import ch.javasoft.metabolic.compress.StoichMatrixCompressedMetabolicNetwork;
import ch.javasoft.metabolic.compress.StoichMatrixCompressor;
import ch.javasoft.metabolic.impl.FractionNumberStoichMetabolicNetwork;
import ch.javasoft.metabolic.util.MetabolicNetworkUtil;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;

/**
 * Extended <tt>MatlabGenerator</tt>, adds functionality to export compression 
 * information.
 */
public class MatlabGenerator extends ch.javasoft.metabolic.generate.MatlabGenerator {
	
	private static final Logger LOG = LogPkg.LOGGER;
	
	/**
	 * Constructure, uses "mnet" as structure name and no model name
	 */
	public MatlabGenerator() {
		super();
	}
	/**
	 * Constructure, uses "mnet" as structure name and the given model name
	 */
	public MatlabGenerator(String modelName) {
		super(modelName);
	}
	
	/**
	 * Writes duplicate gene compression information and matrices to the matlab
	 * script file. If no duplicate gene compression methods are specified, an
	 * exception is thrown.
	 * <p>
	 * If no duplicate genes were found, only the empty groups array is written.
	 * <p>
	 * The duplicate free network is returned for further processing.
	 * 
	 * @param net					the network to output
	 * @param pw					the writer to write to
	 * @param sparse				if true, matrices are written in matlab 
	 * 								sparse matrix format
	 * @param compressionMethods	the compression methods to apply
	 * 
	 * @return the duplicate free (compressed) network
	 * @throws IllegalArgumentException if no duplicate gene compression method
	 * 									was specified 
	 */
	public MetabolicNetwork writeDuplicateGeneCompression(MetabolicNetwork net, PrintWriter pw, boolean sparse, CompressionMethod... compressionMethods) {
		if (CompressionMethod.isContainingDuplicateGene(compressionMethods)) {
			ReadableBigIntegerRationalMatrix stoich = FractionNumberStoichMetabolicNetwork.getStoich(net);
			DuplicateGeneCompressor.CompressionRecord rec = DuplicateGeneCompressor.compress(
				stoich, net.getReactionReversibilities(), 
				net.getMetaboliteNames(), net.getReactionNames(), 
				CompressionMethod.DuplicateGeneExtended.containedIn(compressionMethods)
			);
			CompressionUtil.logCompressionRecord(rec, Level.FINER);
			for (int i = 0; i < rec.dupgroups.length; i++) {				
				pw.print(getStructureName() + ".dupgroups{" + (i+1) + "}=[");
				for (int j = 0; j < rec.dupgroups[i].length(); j++) {
					if (j > 0) pw.print(", ");
					pw.print(rec.dupgroups[i].get(j) + 1);
				}
				pw.println("];");
			}
			if (rec.dupgroups.length != 0) {
				writeMatrix(rec.dupelim.getDoubleRows(), "dupelim", pw, sparse);
				writeMatrix(rec.dupfree.getDoubleRows(), "dupfree", pw, sparse);
				pw.flush();
				return new FractionNumberStoichMetabolicNetwork(rec.metaNames, rec.reacNames, rec.dupfree, rec.reversible);			
			}
			pw.flush();
			return net;
		}
		throw new IllegalArgumentException("no duplicate gene compression methods specified: " + Arrays.toString(compressionMethods));
	}
	
	/**
	 * Writes gene compression information and matrices to the matlab
	 * script file. No duplicate gene compression is applied and written. The
	 * compressed network is returned for further processing.
	 * 
	 * @param net					the network to output
	 * @param pw					the writer to write to
	 * @param sparse				if true, matrices are written in matlab 
	 * 								sparse matrix format
	 * @param suppressedReactions	reactions to suppress, i.e. they are removed
	 * 								in the compressed network
	 * @param compressionMethods	the compression methods to apply
	 * 
	 * @return the compressed network
	 */
	public StoichMatrixCompressedMetabolicNetwork writeNonDuplicateGeneCompression(MetabolicNetwork net, PrintWriter pw, boolean sparse, Set<String> suppressedReactions, CompressionMethod... compressionMethods) {
		ReadableBigIntegerRationalMatrix stoich = FractionNumberStoichMetabolicNetwork.getStoich(net);
		StoichMatrixCompressor cmp = new StoichMatrixCompressor(CompressionMethod.removeDuplicateGeneMethods(compressionMethods));
		StoichMatrixCompressor.CompressionRecord rec = cmp.compress(
			stoich, net.getReactionReversibilities(), 
			net.getMetaboliteNames(), net.getReactionNames(),
			suppressedReactions
		);
		CompressionUtil.logCompressionRecord(rec, Level.FINER);
		
		writeMatrix(rec.pre.getDoubleRows(), "cmppre", pw, sparse);
		writeMatrix(rec.post.getDoubleRows(), "cmppost", pw, sparse);
		writeMatrix(rec.cmp.getDoubleRows(), "cmp", pw, sparse);
		pw.flush();
		StoichMatrixCompressedMetabolicNetwork cmpNet = new StoichMatrixCompressedMetabolicNetwork(net, rec.pre, rec.post, rec.cmp);
		//consistency check for reversibilities:
		if (!Arrays.equals(rec.reversible, cmpNet.getReactionReversibilities())) {
			LOG.warning("reversibility missmatch:");
			LOG.warning("  reversibility(cmp.rec) = " + Arrays.toString(rec.reversible));
			LOG.warning("  reversibility(cmp.net) = " + Arrays.toString(cmpNet.getReactionReversibilities()));
		}
		return cmpNet;
	}
	
	/**
	 * Writes the whole network with duplicate gene and compression information.
	 * Reversible reactions are not expanded, the output matrix is sparse.
	 * <p>
	 * Duplicate gene compression info and matrices are only written if any
	 * duplicate gene compression methods are specified. If no duplicate gene
	 * reactions were found, only the empty duplicate gene groups array is 
	 * written.
	 * 
	 * @param net					the network to output
	 * @param pw					the writer to write to
	 * @param suppressedReactions	reactions to suppress, i.e. they are removed
	 * 								in the compressed network
	 * @param compressionMethods	the compression methods to apply
	 */
	public void writeAll(MetabolicNetwork net, PrintWriter pw, Set<String> suppressedReactions, CompressionMethod... compressionMethods) {
		writeAll(net, pw, false, true, suppressedReactions, compressionMethods);
	}
	
	/**
	 * Writes the whole network with duplicate gene and compression information.
	 * Reversible reactions are not expanded, the output matrix is sparse.
	 * <p>
	 * Duplicate gene compression info and matrices are only written if any
	 * duplicate gene compression methods are specified. If no duplicate gene
	 * reactions were found, only the empty duplicate gene groups array is 
	 * written.
	 * <p>
	 * The following methods are called:
	 * <ul>
	 *   <li>{@link ch.javasoft.metabolic.generate.MatlabGenerator#writeAll(MetabolicNetwork, PrintWriter, boolean, boolean) super.writeAll()}</li>
	 *   <li>{@link #writeDuplicateGeneCompression(MetabolicNetwork, PrintWriter, boolean, CompressionMethod...)}</li>
	 *   <li>{@link #writeNonDuplicateGeneCompression(MetabolicNetwork, PrintWriter, boolean, Set, CompressionMethod...)}</li>
	 * </ul>
	 * 
	 * @param net					the network to output
	 * @param pw					the writer to write to
	 * @param expandReversible		if true, a negated row is appended to the 
	 * 								stoichiometric matrix for every reversible 
	 * 								reaction
	 * @param sparse				if true, matrices are written in matlab 
	 * 								sparse matrix format
	 * @param suppressedReactions	reactions to suppress, i.e. they are removed
	 * 								in the compressed network
	 * @param compressionMethods	the compression methods to apply
	 */
	public StoichMatrixCompressedMetabolicNetwork writeAll(MetabolicNetwork net, PrintWriter pw, boolean expandReversible, boolean sparse, Set<String> suppressedReactions, CompressionMethod... compressionMethods) {
		super.writeAll(net, pw, expandReversible, sparse);
		LOG.info("original network:            " + MetabolicNetworkUtil.getNetworkSizeString("", net));
		pw.print(getStructureName() + ".cmpinfo='");
		for (int i = 0; i < compressionMethods.length; i++) {
			if (i > 0) pw.print(", ");
			pw.print(compressionMethods[i]);
		}
		pw.println("';");
		if (CompressionMethod.isContainingDuplicateGene(compressionMethods)) {
			net = writeDuplicateGeneCompression(net, pw, sparse, compressionMethods);
			compressionMethods = CompressionMethod.removeDuplicateGeneMethods(compressionMethods);
			LOG.info("duplicate gene free network: " + MetabolicNetworkUtil.getNetworkSizeString("", net));
		}
		else {
			LOG.info("duplicate gene free network: -");
		}
		StoichMatrixCompressedMetabolicNetwork cmpNet = writeNonDuplicateGeneCompression(net, pw, sparse, suppressedReactions, compressionMethods);
		LOG.info("compressed network:          " + MetabolicNetworkUtil.getNetworkSizeString("", cmpNet));
		return cmpNet;
	}
}
