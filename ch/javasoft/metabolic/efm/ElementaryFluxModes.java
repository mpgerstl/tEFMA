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
package ch.javasoft.metabolic.efm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.output.CallbackGranularity;
import ch.javasoft.metabolic.efm.output.CountOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.output.NullOutputCallback;
import ch.javasoft.metabolic.efm.output.OptimizerOutputCallback;
import ch.javasoft.metabolic.efm.output.OutputMode;
import ch.javasoft.metabolic.efm.output.TextOutputCallback;
import ch.javasoft.metabolic.efm.output.mat.MatFileOutputCallback;
import ch.javasoft.metabolic.efm.output.text.NumberTextOutputFormatter;

/**
 * The <code>ElementaryFluxModes</code> class encapsulates different implementations
 * to calculate the elementary flux modes for a {@link MetabolicNetwork}.
 * <p>
 * To initialize the implementation for a call within java, one of the 
 * {@link Config#initForJUnitTest(String, ch.javasoft.metabolic.compress.CompressionMethod[], ch.javasoft.metabolic.efm.config.Arithmetic) Config#initForJUnitTest(..)}
 * methods is typically used.
 * <p>
 * Command line invocation} also installs the appropriate implementation and 
 * configure the algorithm according to command line arguments and xml 
 * configuration settings.
 */
public class ElementaryFluxModes {
	
	/**
	 * Interface to implement for EFM computation algorithm
	 */
	public static interface Impl {
		String getImplName();
		void calculateEfms(MetabolicNetwork metaNet, EfmOutputCallback callback);
		Config getConfig();
	}
	private static Impl sImpl;
	
	/**
	 * Set the default implementation. No default implementation is 
	 * automatically set, but command line invocation} installs the appropriate 
	 * implementation. To initialize the implementation for a call within java, 
	 * one of the 
	 * {@link Config#initForJUnitTest(String, ch.javasoft.metabolic.compress.CompressionMethod[], ch.javasoft.metabolic.efm.config.Arithmetic) Config#initForJUnitTest(..)}
	 * methods is typically used.
	 */
	public static void setImpl(Impl impl) {
		sImpl = impl;
	}
	
	public static Impl getImpl() {
		if (sImpl == null) {
			throw new IllegalStateException("efm impl not initialized");
		}
		return sImpl;
	}
	
	/**
	 * Calculates EFMs, but circumvents the output process. However, the number
	 * of (uncompressed, filtered) modes is logged
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static void calculateLogNull(MetabolicNetwork metaNet) {
		getImpl().calculateEfms(metaNet, NullOutputCallback.INSTANCE);		
	}
	/**
	 * Calculates EFMs and logs number of uncompressed EFMs
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * 
	 * @return the number of elementary modes (uncompressed)
	 */
	public static long calculateLogCountOnly(MetabolicNetwork metaNet) {
		final CountOutputCallback cb = new CountOutputCallback(true /*uncompress*/);
		getImpl().calculateEfms(metaNet, cb);
		return cb.getEfmCount();
	}
	/**
	 * Calculates EFMs and logs the uncompressed EFMs in binary form 
	 * (output log level INFO). 
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static void calculateLogBinary(MetabolicNetwork metaNet) {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.BinaryUncompressed));		
	}
	/**
	 * Calculates EFMs and logs the uncompressed EFMs in textual form 
	 * (output log level INFO). 
	 * <p>
	 * Opposed to {@link #calculateLogDoubles(MetabolicNetwork)}, this method
	 * converts the internal number format to double values for output.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static void calculateLogDoubles(MetabolicNetwork metaNet) {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.DoubleUncompressed));		
	}
	/**
	 * Calculates EFMs and logs the uncompressed signs of EFMs in textual form 
	 * (output log level INFO). The sign of the flux values are +1/-1/0 for
	 * forward, reverse and zero flux, respectively.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static void calculateLogSigns(MetabolicNetwork metaNet) {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.SignUncompressed));		
	}
	/**
	 * Calculates EFMs and logs the uncompressed EFMs in textual form 
	 * (output log level INFO).
	 * <p>
	 * Opposed to {@link #calculateLogDoubles(MetabolicNetwork)}, this method
	 * does not convert the internal number format to double values for output.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static void calculateLogNumbers(MetabolicNetwork metaNet) {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.DoubleUncompressed, new NumberTextOutputFormatter(false, false)));		
	}
	/**
	 * Calculates EFMs and applies the specified objective function. The result
	 * is written to the log file.
	 * <p>
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static void calculateLogMinMax(MetabolicNetwork metaNet, double[] costFunction, boolean callbackForAllEfms) {		
		getImpl().calculateEfms(metaNet, new OptimizerOutputCallback(new TextOutputCallback(metaNet, OutputMode.DoubleUncompressed), costFunction, callbackForAllEfms));		
	}
	/**
	 * Calculates EFMs and writes one (or multiple) MATLAB .mat files. Multiple
	 * files are written if the output is too large for a single .mat file. 
	 * If the file name is <tt>met.mat</tt>, the written files will be
	 * <tt>mnet0.mat, mnet1.mat, mnet2.mat, ...</tt>.
	 * <p>
	 * The MATLAB file contains an <tt>mnet</tt> structure, with fields for 
	 * metabolite and reaction names, stoichiometric matrix, reaction lower
	 * and upper bounds and an <tt>mnet.efm</tt> field containing the elementary
	 * modes. If multiple files are written, only the first file contains all
	 * fields, subsequent files only contain the <tt>efms</tt> field.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param folder	the folder to write the output to
	 * @param fileName	the file name uses as template for the names of the
	 * 					output files, usually a <tt>.mat</tt> name
	 */
	public static void calculateFileMatlab(MetabolicNetwork metaNet, File folder, String fileName) throws IOException {
		getImpl().calculateEfms(metaNet, new MatFileOutputCallback(metaNet, folder, fileName));		
	}
	/**
	 * Calculates EFMs and writes one (or multiple) MATLAB .mat files. Multiple
	 * files are written if the output is too large for a single .mat file. 
	 * If the file name is <tt>met.mat</tt>, the written files will be
	 * <tt>mnet0.mat, mnet1.mat, mnet2.mat, ...</tt>.
	 * <p>
	 * The MATLAB file contains an <tt>mnet</tt> structure, with fields for 
	 * metabolite and reaction names, stoichiometric matrix, reaction lower
	 * and upper bounds and an <tt>mnet.efm</tt> field containing the elementary
	 * modes. If multiple files are written, only the first file contains all
	 * fields, subsequent files only contain the <tt>efms</tt> field.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param mode		the output mode, e.g. to log only flux sign patterns
	 * @param folder	the folder to write the output to
	 * @param fileName	the file name uses as template for the names of the
	 * 					output files, usually a <tt>.mat</tt> name
	 */
	public static void calculateFileMatlab(MetabolicNetwork metaNet, OutputMode mode, File folder, String fileName) throws IOException {
		getImpl().calculateEfms(metaNet, new MatFileOutputCallback(mode, metaNet, folder, fileName));		
	}
	/**
	 * Calculates EFMs and writes the uncompressed EFMs in binary form to the
	 * specified file.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param file		the output file
	 */
	public static void calculateFileBinary(MetabolicNetwork metaNet, File file) throws IOException {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.BinaryUncompressed, new FileOutputStream(file), true));		
	}
	/**
	 * Calculates EFMs and writes the uncompressed EFMs in binary form to the
	 * specified file.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param mode		the output mode, e.g. to export only flux sign patterns
	 * @param file		the output file
	 */
	public static void calculateFileBinary(MetabolicNetwork metaNet, OutputMode mode, File file) throws IOException {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, mode, new FileOutputStream(file), true));		
	}
	/**
	 * Calculates EFMs and writes the uncompressed EFMs in textual form to the
	 * specified file. The text file is a tab separated file containing double
	 * values. Each mode is printed on a separate line.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param file		the output file
	 */
	public static void calculateFileDoubles(MetabolicNetwork metaNet, File file) throws FileNotFoundException {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.DoubleUncompressed, new FileOutputStream(file), true));		
	}
	/**
	 * Calculates EFMs and writes the uncompressed EFMs in textual form to the
	 * specified file. The text file is a tab separated file containing integer
	 * values +1/0/-1 for forward, reverse and zero flux values, respectively. 
	 * Each mode is printed on a separate line.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param file		the output file
	 */
	public static void calculateFileSigns(MetabolicNetwork metaNet, File file) throws FileNotFoundException {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.SignUncompressed, new FileOutputStream(file), true));		
	}
	/**
	 * Calculates EFMs and writes the uncompressed EFMs in textual form to the
	 * specified file. The text file is a tab separated file containing number
	 * values. Depending on the {@link Arithmetic arithmetic} {@link Config configuration}, 
	 * values might be doubles, fractions or large integer numbers.
	 * Each mode is printed on a separate line.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param file		the output file
	 */
	public static void calculateFileNumbers(MetabolicNetwork metaNet, File file) throws FileNotFoundException {
		getImpl().calculateEfms(metaNet, new TextOutputCallback(metaNet, OutputMode.DoubleUncompressed, new FileOutputStream(file), new NumberTextOutputFormatter(false, false), true));		
	}
	/**
	 * Calculates EFMs and sends callback notification to the specified callback
	 * interface. This is a generic interface, for instance to support other
	 * formats. The callback structure is a memory efficient way especially if
	 * intermediary results are huge and thus have not been kept in memory.
	 * <p>
	 * For a small number of elementary modes, {@link #calculateAndReturnEfms(MetabolicNetwork) calculateAndReturnEfms(..)}
	 * might be more convenient to use.
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 * @param callback	callback interface, receiving notifications for every
	 * 					elementary mode.
	 */
	public static void calculateCallback(MetabolicNetwork metaNet, EfmOutputCallback callback) {
		getImpl().calculateEfms(metaNet, callback);		
	}
	/**
	 * Simple methods to receive an {@link Iterable iterable} (usable in a for 
	 * loop) with the elementary modes. Note that for a large number of modes,
	 * this is not an efficient method, and it is recommended to use
	 * {@link #calculateCallback(MetabolicNetwork, EfmOutputCallback) calculateCallback(..)}
	 * instead. 
	 * 
	 * @param metaNet	the network for which to calculate the EFMs
	 */
	public static Iterable<? extends FluxDistribution> calculateAndReturnEfms(MetabolicNetwork metaNet) {
		final ConcurrentLinkedQueue<FluxDistribution> res = new ConcurrentLinkedQueue<FluxDistribution>();
		EfmOutputCallback cb = new EfmOutputCallback() {
			public CallbackGranularity getGranularity() {
				return CallbackGranularity.DoubleUncompressed;
			}
			public void callback(EfmOutputEvent evt) {
				if (evt.getKind() == EfmOutputEvent.Kind.EFM_OUT) {
					res.add(evt.getEfm());
				}
			}
			public boolean allowLoggingDuringOutput() {
				return true;
			}
			public boolean isThreadSafe() {
				return true;
			}
		};
		getImpl().calculateEfms(metaNet, cb);
		return res;
	}
	
    //no instances
	private ElementaryFluxModes() {
		super();
	}
	
}
