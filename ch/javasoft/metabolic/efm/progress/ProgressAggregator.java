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

/**
 * The <code>ProgressMonitor</code> collects and aggregates progress 
 * information.
 * 
 * @see #updateProgress(int)
 */
public interface ProgressAggregator extends ProgressNotifiable {
	
	/**
	 * Update the overall progress by <code>2<sup>-e</sup></code>. For instance,
	 * a progress increment of 100% would call this method with e = 0, for 50%,
	 * e is 1 and so on.
	 * <p>
	 * When traversing a tree, overall progress starts at the root node with 
	 * e = 0 (i.e. 100%). Visiting the child nodes, each node represents half
	 * of the progress, which means that e is now 1 (or 50% each) and so on. 
	 * With other words, e represents also the recursion depth of a binary tree.
	 * <p>
	 * The overall progress is updated by adding <code>2<sup>-e</sup></code> to
	 * the progress counter, which starts with 0 and ends with 1. Note that for
	 * technical reasons, a {@link #getSmallestIncrement() smallest increment}
	 * is defined. Any number below this threshold cases an illegal argument
	 * exception.
	 * 
	 * @param e A non-negative number indicating a progress increment 
	 * 			of <code>2<sup>-e</sup></code>
	 * 
	 * @throws IllegalArgumentException	
	 * 						if the progress increment is to small to be added to
	 * 						the progress counter, that is, if e is larger than 
	 * 						the value returned by {@link #getSmallestIncrement()}
	 * 						
	 * @throws IOException	if progress monitor outputting causes an 
	 * 						i/o exception
	 */
	void updateProgress(int e) throws IOException, IllegalArgumentException;
	
}
