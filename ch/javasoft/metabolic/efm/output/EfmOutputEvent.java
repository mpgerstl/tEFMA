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

import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnFactories;

/**
 * The <tt>EfmOutputEvent</tt> as information carrier for calls to 
 * {@link EfmOutputCallback#callback(EfmOutputEvent)}. Efm output events occur
 * after elementary mode computation, when the system uses the callback method
 * to output efms in a configurable way, being defined by the callback.
 */
public class EfmOutputEvent {

	/**
	 * The event kind, i.e. {@link #PRE} for the initial call to the
	 * {@link EfmOutputCallback#callback(EfmOutputEvent) callback} method 
	 * indicating that efm output starts now, {@link #EFM_OUT} for calls per
	 * efm and {@link #POST} for the final call indicating completion of the
	 * efm output callback process. 
	 */
	public static enum Kind{
		/**
		 * Event kind for the initial call to 
		 * {@link EfmOutputCallback#callback(EfmOutputEvent)} indicating that 
		 * efm output starts now
		 */
		PRE, 
		/**
		 * Event kind for per efm calls to
		 * {@link EfmOutputCallback#callback(EfmOutputEvent)}
		 */
		EFM_OUT, 
		/**
		 * Event kind for the final call to
		 * {@link EfmOutputCallback#callback(EfmOutputEvent)} indicating 
		 * completion of the efm output callback process. 
		 */
		POST
	}
	
	private final Kind				mKind;
	private final MetabolicNetwork	mMetabolicNetwork;
	private final FluxDistribution	mEfm;
	private final long				mEfmCount;//includes cycles

	/**
	 * Generic internal constructor
	 * @param kind	event kind
	 * @param net	metabolic network
	 * @param efm	the efm flux distribution vector
	 */
	protected EfmOutputEvent(Kind kind, MetabolicNetwork net, FluxDistribution efm, long efmCount) {
		if (net == null) {
			throw new NullPointerException("metabolic network cannot be null");
		}
		if ((efm == null) != (kind != Kind.EFM_OUT)) {
			if (kind == Kind.EFM_OUT) {
				throw new NullPointerException("efm cannot be null for event kind " + kind);
			}
			else {
				throw new IllegalArgumentException("efm must be null for event kind " + kind);
			}
		}
		mKind				= kind;
		mMetabolicNetwork	= net;
		mEfm				= efm;
		mEfmCount			= efmCount;
	}
	/**
	 * Constructor for {@link Kind#EFM_OUT} event kinds, with metabolic network
	 * and given efm
	 * @param net		metabolic network
	 * @param efm		the efm flux distribution vector
	 * @param efmCount	the number of efms, before removing futile cycles and
	 * 					without adding dublicate gene multiplications
	 */
	public EfmOutputEvent(MetabolicNetwork net, FluxDistribution efm, long efmCount) {
		this(Kind.EFM_OUT, net, efm, efmCount);
	}
	/**
	 * Constructor for event kinds {@link Kind#PRE} and {@link Kind#POST}, with 
	 * metabolic network
	 * 
	 * @param kind		event kind
	 * @param net		metabolic network
	 * @param efmCount	the number of efms, before removing futile cycles and
	 * 					without adding dublicate gene multiplications
	 */
	public EfmOutputEvent(Kind kind, MetabolicNetwork net, long efmCount) {
		this(kind, net, null, efmCount);
	}
	
	public Kind getKind() {
		return mKind;
	}
	public MetabolicNetwork getMetabolicNetwork() {
		return mMetabolicNetwork;
	}
	
	/**
	 * The elementary flux mode as flux distribution vector, or <tt>null</tt>
	 * if {@link #getKind() event kind} is {@link Kind#PRE} or {@link Kind#POST}
	 * 
	 * @return	The elementary flux mode, or <tt>null</tt> if 
	 * 			{@link #getKind() event kind} is {@link Kind#PRE} or 
	 * 			{@link Kind#POST}.
	 */
	public FluxDistribution getEfm() {
		return mEfm;
	}
	
	public Column getEfmAsColumn() {
		final Column col = ColumnFactories.instance().createBinaryColumn(
				mEfm.getPreferredNumberClass(), mEfm.getSize()); 
		for (int i = 0; i < mEfm.getSize(); i++) {
			if (mEfm.getRateSignum(i) != 0) col.bitValues().set(i);
		}
		return col;
	}

	/**
	 * Returns the number of EFMs as specified at construction time of this
	 * event. This count is usually not considering EFMs resulting from 
	 * duplicate gene EFM multiplications
	 */
	public long getEfmCount() {
		return mEfmCount;
	}
	
}
