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

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Reaction;

/**
 * Utility class with static methods around {@link MetabolicNetwork}s
 */
public class MetabolicNetworkUtil {
	
	/**
	 * Returns the network size (#metabolites, #reactions, #reversible reactions),
	 * prepending the given prefix.<br/>
	 * If prefix is 'my network has ', the returned string looks like this:
	 * <pre>
	 * my network has 243 metabolites, 345 reactions (104 reversible)
	 * </pre>
	 */
	public static String getNetworkSizeString(String prefix, MetabolicNetwork net) {
		int cntRev = 0;
		for (Reaction reac : net.getReactions()) {
			if (reac.getConstraints().isReversible()) cntRev++;
		}
		return
			(prefix == null ? "" : prefix) + net.getMetabolites().length() + " metabolites, " + 
			net.getReactions().length() + " reactions (" + cntRev + " reversible)";
	}

	/**
	 * Returns the reaction names as a comma separated list, such as
	 * <pre>
	 * reactionA, reactionB, another reaction, lastReaction
	 * </pre>
	 */
	public static String getReactionNamesString(MetabolicNetwork net) {
		final StringBuilder sb = new StringBuilder();
		for (Reaction reac : net.getReactions()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(reac.getName());
		}
		return sb.toString();
	}
	// no instances
	private MetabolicNetworkUtil() {
		super();
	}

}
