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
package ch.javasoft.smx.ops.ext;

import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.ops.mt.MtOpsImpl;

public abstract class ExternalOps {
	
	private static ExternalOpsImpl sImpl;
	
	public static void setImpl(ExternalOpsImpl impl) {
		sImpl = impl;
	}
	
	public static ExternalOpsImpl getImpl() {
		if (sImpl == null) {
//			sImpl = new SsccOpsImpl();	//faster, but BUGGY!
			sImpl = new MtOpsImpl();
		}
		return sImpl;
	}

    public static DoubleMatrix nullspace(ReadableDoubleMatrix src) {
    	if (src.getRowCount() == 0) return new DefaultDoubleMatrix(src.getColumnCount(), 0);
    	if (src.getColumnCount() == 0) return new DefaultDoubleMatrix(0, 0);
    	return getImpl().nullspace(src);
    }

    public static int nullity(ReadableDoubleMatrix src) {
        return getImpl().nullity(src);
    }
    
    public static int rank(ReadableDoubleMatrix src) {
        return getImpl().rank(src);
    }
    
    public static DoubleMatrix invert(ReadableDoubleMatrix src) {
        return getImpl().invert(src);
    }

    // no instances
    private ExternalOps() {
        super();
    }

}
