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
import ch.javasoft.util.DoubleArray;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.logging.LogFragmenter;

public class OptimizerOutputCallback extends AbstractOutputCallback {
	
	private final int[]		mCostIndices;
	private final double[]	mCostFunction;
	private final EfmOutputCallback mWrapped;
	private final boolean	mCallbackForAllEfms;
	
	private double				mMin, mMax;
	private FluxDistribution	mMinFlux, mMaxFlux;
	
	public OptimizerOutputCallback(EfmOutputCallback wrapped, double[] costFunction, boolean callbackForAllEfms) {
		mWrapped = wrapped;
		IntArray	inds	= new IntArray();
		DoubleArray coeffs	= new DoubleArray();
		for (int i = 0; i < costFunction.length; i++) {
			if (costFunction[i] != 0d) {
				inds.add(i);
				coeffs.add(costFunction[i]);
			}
		}
		mCostIndices	= inds.toArray();
		mCostFunction	= coeffs.toArray();
		mCallbackForAllEfms = callbackForAllEfms;
	}

	@Override
	protected void callbackPre(EfmOutputEvent evt) throws IOException {
		mMin 		= Double.MAX_VALUE;
		mMax		= -Double.MAX_VALUE;
		mMinFlux	= null;
		mMaxFlux	= null;
		mWrapped.callback(evt);
	}
	
	@Override
	protected void callbackEfmOut(EfmOutputEvent evt) throws IOException {
		//FIXME should not only operate on double values
		double[] vals = evt.getEfm().getDoubleRates();
		double cost = 0d;
		for (int i = 0; i < mCostIndices.length; i++) {
			cost += mCostFunction[i] * vals[mCostIndices[i]];
		}
		if (cost < mMin) {
			mMin = cost;
			mMinFlux = evt.getEfm();
		}
		if (cost > mMax) {
			mMax = cost;
			mMaxFlux = evt.getEfm();
		}
		if (mCallbackForAllEfms) mWrapped.callback(evt);
	}

	@Override
	protected void callbackPost(EfmOutputEvent evt) throws IOException {
		logCostFunction();
		if (mMinFlux != null) {
			EfmOutputEvent minEvt = new EfmOutputEvent(evt.getMetabolicNetwork(), mMinFlux, evt.getEfmCount());
			mWrapped.callback(minEvt);
			logMinMax(minEvt, mMin, true);
		}
		if (mMaxFlux != null) {
			EfmOutputEvent maxEvt = new EfmOutputEvent(evt.getMetabolicNetwork(), mMaxFlux, evt.getEfmCount());
			mWrapped.callback(maxEvt);
			logMinMax(maxEvt, mMax, false);
		}
		mWrapped.callback(evt);		
	}
	
	public void logCostFunction() {
		LogFragmenter log = new LogFragmenter(LogPkg.LOGGER);
		log.infoStart("cost function c = ");
		for (int i = 0; i < mCostIndices.length; i++) {
			if (i > 0) log.append( " + ");
			log.append(mCostFunction[i] + " x" + mCostIndices[i]);
		}
		log.end();
	}
	public void logMinMax(EfmOutputEvent evt, double value, boolean isMin) {
		LogPkg.LOGGER.info((isMin ? "minimum" : "maximum") + " value: " + value);
	}

	public CallbackGranularity getGranularity() {
		return CallbackGranularity.DoubleUncompressed;//since we need that
	}
	
	public boolean allowLoggingDuringOutput() {
		return mWrapped.allowLoggingDuringOutput();
	}

	/**
	 * Returns {@code false}
	 * @see EfmOutputCallback#isThreadSafe()
	 */
	public boolean isThreadSafe() {
		return false;
	}
}
