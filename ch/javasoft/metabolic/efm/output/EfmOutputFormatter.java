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

/**
 * The <tt>EfmOutputFormatter</tt> is a generic interface which allows 
 * formatting the efm output.
 * 
 * <p>A callback to {@link EfmOutputCallback#callback(EfmOutputEvent)} typically
 * causes the following calls to this formatter's methods:
 * <ul>
 * <li>{@link #formatHeader(EfmOutputCallback, Object, EfmOutputEvent) formatHeader}: 
 * called once to indicate output start</li>
 * <li>per efm calls to:
 * 	<ul>
 * 		<li>{@link #formatEfmHeader(EfmOutputCallback, Object, EfmOutputEvent, long) formatEfmHeader}: 
 * 			called to indicate that output of an efm starts</li>
 * 		<li>{@link #isEfmValueIterationNeeded(EfmOutputCallback) isEfmValueIterationNeeded}: 
 * 			if <tt>true</tt> is returned, 
 * 			{@link #formatEfmValue(EfmOutputCallback, Object, EfmOutputEvent, long, int, Number) formatEfmValue}
 * 			will be invoked with every single flux value of the efm.
 * 			If <tt>false</tt> is returned, the flux values are not iterated and
 * 			no calls to <tt>formatEfmValue</tt> will be made. It is expected
 * 			that efm flux value output is either not desired or is performed in
 * 			{@link #formatEfmHeader(EfmOutputCallback, Object, EfmOutputEvent, long) formatEfmHeader}
 * 			or
 * 			{@link #formatEfmFooter(EfmOutputCallback, Object, EfmOutputEvent, long) formatEfmFooter}. 			
 * 		</li>
 * 		<li>{@link #formatEfmValue(EfmOutputCallback, Object, EfmOutputEvent, long, int, Number) formatEfmValue}: 
 * 			called to formatted and write a single flux value</li>
 * 		<li>{@link #formatEfmFooter(EfmOutputCallback, Object, EfmOutputEvent, long) formatEfmFooter}: 
 * 			indicating that efm output is complete</li>
 * 	</ul>
 * </li>
 * <li>{@link #formatFooter(EfmOutputCallback, Object, EfmOutputEvent, long) formatFooter}
 * indicating the end of output.</li>
 * </ul>
 * 
 * @param <W>	the output writer
 */
public interface EfmOutputFormatter<W> {
	/**
	 * Called once to indicate that output starts
	 * 
	 * @param cb			The output callback which invoked this method
	 * @param writer		The output writer
	 * @param evt			The output event with access to the metabolic network
	 * @throws IOException	If writing output caused an io exception
	 */
	void formatHeader(EfmOutputCallback cb, W writer, EfmOutputEvent evt) throws IOException;
	/**
	 * Called once per efm to indicate that output of the given efm starts
	 * 
	 * @param cb			The output callback which invoked this method
	 * @param writer		The output writer
	 * @param evt			The output event with access to the metabolic 
	 * 						network and the efm
	 * @param efmIndex		The 0-based index of the efm, i.e. a running counter
	 * @throws IOException	If writing output caused an io exception
	 */
	void formatEfmHeader(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex) throws IOException;
	/**
	 * Called once for every flux value of an efm, indicating that formatting
	 * and writing the given value should now be performed.
	 * 
	 * <p><b>Note</b> that this method is only called if 
	 * {@link #isEfmValueIterationNeeded(EfmOutputCallback)} returns 
	 * <tt>true</tt>
	 * 
	 * @param cb			The output callback which invoked this method
	 * @param writer		The output writer
	 * @param evt			The output event with access to the metabolic 
	 * 						network and the efm
	 * @param efmIndex		The 0-based index of the efm, i.e. a running counter
	 * @param valueIndex	The 0-based value index, i.e. the index of the
	 * 						corresponding reaction in the metabolic network
	 * @param value			the flux value, 0, 1 or -1 for binary output mode,
	 * 						or the actual relative flux value for double output
	 * 						mode.
	 * 						
	 * @throws IOException	If writing output caused an io exception
	 */
	void formatEfmValue(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex, int valueIndex, Number value) throws IOException;
	/**
	 * Called once per efm to indicate that output of the given efm is about to
	 * complete
	 * 
	 * @param cb			The output callback which invoked this method
	 * @param writer		The output writer
	 * @param evt			The output event with access to the metabolic 
	 * 						network and the efm
	 * @param efmIndex		The 0-based index of the efm, i.e. a running counter
	 * @throws IOException	If writing output caused an io exception
	 */
	void formatEfmFooter(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long efmIndex) throws IOException;
	/**
	 * Called once to indicate that output is about to complete.
	 * 
	 * @param cb			The output callback which invoked this method
	 * @param writer		The output writer
	 * @param evt			The output event with access to the metabolic network
	 * @throws IOException	If writing output caused an io exception
	 */
	void formatFooter(EfmOutputCallback cb, W writer, EfmOutputEvent evt, long countEfms) throws IOException;
	
	/**
	 * If <tt>true</tt> is returned, the flux values of each efm are iterated
	 * and {@link #formatEfmValue(EfmOutputCallback, Object, EfmOutputEvent, long, int, Number)}
	 * is invoked for every value.
	 * 
	 * <p>If <tt>false</tt> is returned, the flux values are not iterated and
	 * no calls to <tt>formatEfmValue</tt> will be made. It is expected
	 * that efm flux value output is either not desired or is performed in
	 * {@link #formatEfmHeader(EfmOutputCallback, Object, EfmOutputEvent, long) formatEfmHeader}
	 * or
	 * {@link #formatEfmFooter(EfmOutputCallback, Object, EfmOutputEvent, long) formatEfmFooter}. 	
	 * 
	 * @param cb			The output callback which invoked this method
	 */
	boolean isEfmValueIterationNeeded(EfmOutputCallback cb);
}
