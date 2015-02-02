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
package ch.javasoft.smx.ops.mt;

import java.util.logging.Level;

import mt.DenseMatrix;
import mt.DiagMatrix;
import mt.Matrix;
import mt.fact.NotConvergedException;
import mt.fact.SVD;
import mt.fact.SingularvalueComputer;
import mt.ll.Interface;
import ch.javasoft.smx.exception.SingularMatrixException;
import ch.javasoft.smx.iface.DoubleMatrix;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.ops.ext.ExternalOpsImpl;
import ch.javasoft.smx.ops.sscc.SsccOpsImpl;
import ch.javasoft.util.logging.LogPrintWriter;

public class MtOpsImpl extends ExternalOpsImpl {

    public static final int DEFAULT_PRECISION = 10;
    
    private final int		mPrecision;
    private final double	mZero;
    
    public MtOpsImpl() {
        this(DEFAULT_PRECISION);
    }
    
    public MtOpsImpl(int precision) {
    	if (precision < 0) throw new IllegalArgumentException("negative precision: " + precision);
    	mPrecision	= precision;
    	mZero		= Math.pow(10, -mPrecision);
    }

	@Override
	public DoubleMatrix nullspace(ReadableDoubleMatrix src) {
		try {
			return Convert.fromExternal(getNull(getSvd(src, true)));
		}
		catch (RuntimeException ex) {
			LogPkg.LOGGER.info("retrying with sscc library");
			return new SsccOpsImpl().nullspace(src);
		}
		
//		//DEBUG
//		SVD svd			= getSvd(src, true);
//		Matrix mxSrc	= Convert.toExternal(src);
//		Matrix mxU		= getU(svd);
//		Matrix mxVt		= getVt(svd);
//		Matrix mxNull	= getNull(svd);
//		System.out.println("=========================================");
//		src.writeToMultiline(System.out);
//		print(mxSrc);
//		System.out.println("------------------------");
//		int rank	= rank(svd);
//		System.out.println("rank=" + rank + ", nullity=" + nullity(src));
//		print(svd.getU());
//		System.out.println(Arrays.toString(svd.getS()));
//		print(svd.getVt());
//		System.out.println("------------------------");
//		System.out.print("U=");
//		print(mxU);
//		System.out.print("Vt=");
//		print(mxVt);
//		System.out.println("-->");
////		print(eSrc.mult(vTNull, new DenseMatrix(eSrc.numRows(), vTNull.numColumns())));
//		print(multiply(multiply(mxU, new DiagMatrix(svd.getS())), mxVt));
////		System.out.println(algebra().mult(algebra().mult(sing.getU(), sing.getS()), sing.getV()));
//		System.out.println("------------------------");
//		System.out.print("null=");
//		print(mxNull);
//		System.out.println("-->");
//		print(multiply(mxSrc, mxNull));
//		System.out.println("------------------------");
//		
//		return Convert.fromExternal(mxNull);
		
	}

	@Override
	public int nullity(ReadableDoubleMatrix src) {
		return src.getColumnCount() - rank(src);
	}

	@Override
	public int rank(ReadableDoubleMatrix src) {
		return rank(getSvd(src, false));
	}
	
	private int rank(SVD svd) {
		double[] sing = svd.getS();
		int rank = sing.length;
		for (int ii = 0; ii < sing.length; ii++) {
			if (sing[ii] <= mZero) {
				rank = ii;
				break;
			}
		}
		return rank;
	}

	@Override
	public DoubleMatrix invert(ReadableDoubleMatrix src) {
		if (src.getColumnCount() == src.getRowCount()) {
			//A		= U * diag(s) * Vt
			//A'	= (Vt)' * diag(s)' * U' = V * diag(1/s) * Ut
			//    t = transpose
			//    ' = inverse
			SVD	svd		= getSvd(src, true);
			Matrix u 	= getU(svd);
			Matrix vt	= getVt(svd);
			double[] s	= svd.getS();
			for (int ii = 0; ii < s.length; ii++) {
				if (s[ii] < mZero) {
					throw new SingularMatrixException("singular matrix: " + src, ii);
				}
				s[ii] = 1.0d / s[ii];
			}
			Matrix ut	= transpose(u);
			Matrix v	= transpose(vt);
			Matrix inv	= multiply(multiply(v, new DiagMatrix(s)), ut);
			return Convert.fromExternal(inv);
		}
		throw new IllegalArgumentException("not a square matrix: " + src);
	}
	
	private Matrix submatrix(Matrix mx, int rStart, int rEnd, int cStart, int cEnd) {
		Matrix res = new DenseMatrix(rEnd - rStart, cEnd - cStart);
		for (int row = 0; row < res.numRows(); row++) {
			for (int col = 0; col < res.numColumns(); col++) {
				res.set(row, col, mx.get(rStart + row, cStart + col));
			}
		}
		return res;
	}
	
	private SVD getSvd(ReadableDoubleMatrix src, boolean vectors) {
		SingularvalueComputer svc = new SingularvalueComputer(src.getRowCount(), src.getColumnCount(), vectors);
		try {
			Matrix mxSrc = Convert.toExternal(src);
			DenseMatrix dmxSrc = mxSrc instanceof DenseMatrix ? (DenseMatrix)mxSrc : new DenseMatrix(mxSrc);
			return svc.factor(dmxSrc);
		}
		catch (NotConvergedException ex) {
			LogPrintWriter log;
			LogPkg.LOGGER.severe("svd not converged, e=" + ex);
			log = new LogPrintWriter(LogPkg.LOGGER, Level.SEVERE);
			ex.printStackTrace(log);
			log.flush();
			log = new LogPrintWriter(LogPkg.LOGGER, Level.INFO);
			src.writeToMultiline(log);
			log.flush();
			throw new RuntimeException("cannot create svd for matrix, e=" + ex, ex);			
		}
	}
	
	@SuppressWarnings("unused")
	private void print(Matrix mx) {		
		System.out.println("{");
		for (int row = 0; row < mx.numRows(); row++) {
			System.out.print(" [");
			for (int col = 0; col < mx.numColumns(); col++) {
				if (col > 0) System.out.print(", ");
				System.out.print(mx.get(row, col));
			}
			System.out.println("]");
		}		
		System.out.println("}");
	}

	private Matrix multiply(Matrix mxA, Matrix mxB) {
		DenseMatrix res = new DenseMatrix(mxA.numRows(), mxB.numColumns());
		return mxA.mult(mxB, res);
	}
	
	private Matrix getU(SVD svd) {
		return submatrix(svd.getU(), 0, svd.getU().numRows(), 0, svd.getS().length);		
	}
	private Matrix getVt(SVD svd) {
		return submatrix(svd.getVt(), 0, svd.getS().length, 0, svd.getVt().numColumns());
	}
	private Matrix getNull(SVD svd) {
		int rank	= rank(svd);
//		Matrix u	= svd.getU();
		Matrix vt	= svd.getVt();
//		if (u.numColumns() > rank) {
//			return transpose(submatrix(u, 0, u.numRows(), rank, u.numColumns()));
//		}
		if (vt.numRows() > rank) {
			return transpose(submatrix(vt, rank, vt.numRows(), 0, vt.numColumns()));
		}
		return new DenseMatrix(rank, 0);
	}
	
	public double[] getSignularValues(ReadableDoubleMatrix mx) {
		SVD svd = getSvd(mx, true);
		double[] singVals			= new double[svd.getVt().numRows()];
		double[] singValsNotZero	= svd.getS();
		System.arraycopy(singValsNotZero, 0, singVals, 0, singValsNotZero.length);
		return singVals;
	}
	
	private Matrix transpose(Matrix mx) {
		return mx.transpose(new DenseMatrix(mx.numColumns(), mx.numRows()));
	}
	
	static {
		boolean any = false;
		if (Interface.blas().getClass().getName().endsWith("JLAPACK_BLASkernel")) {
			String libName = System.mapLibraryName("nni_blas");
			System.out.println("WARNING: native BLAS library '" + libName + "' not found, defaulting to pure java version.");
			any = true;
		}
		if (Interface.lapack().getClass().getName().endsWith("JLAPACK_LAPACKkernel")) {
			String libName = System.mapLibraryName("nni_lapack");
			System.out.println("WARNING: native LAPACK library '" + libName + "' not found, defaulting to pure java version.");
			any = true;
		}
		if (any) {
			System.out.println("java.library.path=" + System.getProperty("java.library.path"));
		}
	}

}
