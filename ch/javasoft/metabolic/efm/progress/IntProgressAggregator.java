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
package ch.javasoft.metabolic.efm.progress;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The <code>ProgressAggregator</code> collects progress increments and 
 * aggregates them to larger pieces. The size of the aggregated progress pieces
 * is defined by the {@link ProgressAggregator#getSmallestIncrement() smallest
 * increment} of another progress monitor, the <i>aggregate monitor</i>. If the 
 * aggregated piece size is reached, an update call is generated and forwarded 
 * to the aggregate monitor.
 * <p>
 * Internally, aggregation is performed by summarizing the progress increments
 * with thread safe atomic integer. The integer data type also constrains the
 * {@link #getSmallestIncrement() smallest increment} of this progress 
 * aggregator to values in [0, 30], corresponding to smallest possible progress 
 * increments from one to <code>2<sup>-30</sup></code>.
 */
public class IntProgressAggregator implements ProgressAggregator {
	
	private final int 					smallestIncrement;
	private final AtomicInteger 		progress;
	private final ProgressNotifiable 	aggregateNotifiable;
	private final int					aggregateShift;
	
	/**
	 * Constructor for <code>IntProgressAggregator</code> with smallest possible
	 * increment 30
	 * 
	 * @param aggregateMonitor	an aggregate monitor to which aggregate progress
	 * 							information is forwarded
	 * 
	 * @throws IllegalArgumentException	if the smallest increment of the 
	 * 									specified aggregate monitor is smaller 
	 * 									than 30, i.e. if the exponent of the 
	 * 									smallest increment of the aggregate 
	 * 									monitor is > 30
	 */
	public IntProgressAggregator(ProgressNotifiable aggregateMonitor) {
		this(aggregateMonitor, 30);
	}
	/**
	 * Constructor for <code>IntProgressAggregator</code> with defined smallest 
	 * possible increment
	 * 
	 * @param aggregateMonitor	an aggregate monitor to which aggregate progress
	 * 							information is forwarded
	 * 
	 * @throws IllegalArgumentException	if the smallest increment of the 
	 * 									specified aggregate monitor is smaller 
	 * 									than the smallest increment of this 
	 * 									aggregator, i.e. if the exponent of the 
	 * 									smallest increment of the aggregate 
	 * 									monitor is > smallestIncrement
	 */
	public IntProgressAggregator(ProgressNotifiable aggregateMonitor, int smallestIncrement) {
		if (smallestIncrement < 0 || smallestIncrement > 30) {
			throw new IllegalArgumentException("smallest increment must be in [0, 30], but is " + smallestIncrement);
		}
		if (aggregateMonitor.getSmallestIncrement() >= smallestIncrement) {
			throw new IllegalArgumentException(
				"smallest increment of aggregate most be larger than increment of aggregator, i.e. exponent must be smaller");
		}
		this.smallestIncrement		= smallestIncrement;
		this.progress				= new AtomicInteger(0);
		this.aggregateNotifiable	= aggregateMonitor;
		this.aggregateShift			= smallestIncrement - aggregateMonitor.getSmallestIncrement();	
	}

	public void updateProgress(int e) throws IOException, IllegalArgumentException {
		if (e > smallestIncrement) {
			throw new IllegalArgumentException("progress increment too small, i.e. e > " + smallestIncrement + ": " + e);
		}
		if (e < 0) {
			throw new IllegalArgumentException("negative progress increment: " + e);
		}

		final int add = (1<<(smallestIncrement-e)); 
		int old, upd;
		do {
			old = progress.get();
			upd = old + add;
		}
		while (!progress.compareAndSet(old, upd));
		
		final int aggOld = (old >>> aggregateShift);
		final int aggUpd = (upd >>> aggregateShift);
		if (aggOld != aggUpd) {
			if (aggregateNotifiable instanceof ProgressMonitor) {
				((ProgressMonitor)aggregateNotifiable).notifyProgress(((double)upd) / (1 << smallestIncrement));
			}
			else if (aggregateNotifiable instanceof ProgressAggregator) {
				final ProgressAggregator agg = ((ProgressAggregator)aggregateNotifiable);
				
				//most of the times, this is a one-time loop,
				//but for large increments, it could be more, but at max 30
				int aggE    = aggregateNotifiable.getSmallestIncrement();
				int aggDiff = aggUpd - aggOld;
				while (aggDiff > 0) {
					while ((aggDiff % 2) == 0) {
						aggDiff >>>= 1;
						aggE--;
					}
					aggDiff >>>= 1;
					agg.updateProgress(aggE);
				}
			}
		}
	}

	/**
	 * Returns the smallest increment as specified at construction time. This is
	 * a number in [0, 30], since internally, integers are used to summarize 
	 * progress increments, and <code>2<sup>-30</sup></code> fits into an 
	 * integer.
	 */
	public final int getSmallestIncrement() {
		return smallestIncrement;
	}

	/**
	 * Calls the close method of the aggregate monitor
	 */
	public void close() throws IOException {
		aggregateNotifiable.close();
	}
	
	/**
	 * Returns the current progress, usually a number between 0 (no progress) 
	 * and 1 indicating 100%.
	 */
	public double getProgress() {
		return progress.doubleValue() / (1 << smallestIncrement);
	}

}
