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
package ch.javasoft.smx.ops.sscc;

import ru.sscc.matrix.DenseMatrix;
import ru.sscc.matrix.solve.CrautSolver;
import ru.sscc.matrix.solve.RealCommonSolver;
import ru.sscc.matrix.solve.RealSquareSolver;
import ru.sscc.matrix.solve.RotationSolver;
import ru.sscc.util.CalculatingException;
import ch.javasoft.smx.exception.SingularMatrixException;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.ops.ext.ExternalOpsImpl;

public class SsccOpsImpl extends ExternalOpsImpl {

    @Override
	public DoubleMatrix nullspace(ReadableDoubleMatrix src) {
        DenseMatrix extSrc = Convert.toExternal(src);
        DenseMatrix extNull = factorizeCommon(extSrc).getNullSpace();
        //TODO normalize
        return extNull == null ? new DefaultDoubleMatrix(0, src.getColumnCount()) : Convert.fromExternal(extNull);
    }

    @Override
	public int nullity(ReadableDoubleMatrix src) {
        DenseMatrix extSrc = Convert.toExternal(src);
        return factorizeCommon(extSrc).getNullSpaceRange();
    }
    
    @Override
	public int rank(ReadableDoubleMatrix src) {
        DenseMatrix extSrc = Convert.toExternal(src);
        return factorizeCommon(extSrc).getMatrixRange();
    }
    
    @Override
	public DoubleMatrix invert(ReadableDoubleMatrix src) {
        DenseMatrix extSrc = Convert.toExternal(src);
        DenseMatrix extCln = (DenseMatrix)extSrc.clone();
        DenseMatrix extInv = factorize(extSrc).constructRefinedInverse(extCln, null);
        return Convert.fromExternal(extInv);
    }

    private RealCommonSolver factorizeCommon(DenseMatrix matrix) {
        RealCommonSolver solver = new RotationSolver(matrix);
        try {
            solver.factorize();
        }
        catch (CalculatingException ex) {
            //throw new SingularMatrixException(-1);
        }
        return solver;
    }
    private RealSquareSolver factorize(DenseMatrix matrix) {
        RealSquareSolver solver = new CrautSolver(matrix);
        try {
            solver.factorize();
        }
        catch (CalculatingException ex) {
        	ex.printStackTrace();
            throw new SingularMatrixException(0);
        }
        return solver;
    }
    
    public SsccOpsImpl() {
        super();
    }

}
