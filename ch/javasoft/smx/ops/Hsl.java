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
package ch.javasoft.smx.ops;

/**
 * The <code>Hsl</code> class bridges to the fortran HSL library. 
 * Only some selected functions are supported.
 * <p>
 * See 
 * http://hsl.rl.ac.uk/hsl2007/distrib/packages/hsl_mc66/hsl_mc66.pdf
 * 
 * username: mterzer
 * pwd: as usual, for hsl
 */
public class Hsl {
	
    static {
        System.loadLibrary("hsl-jni");
    }
    
    /**
     * Can be called to check whether the HSL shared library (libhsl.so) can
     * be loaded. If not, an exception is thrown. Otherwise, this function does
     * nothing.
     */
    public static void loadLibrary() {
    	//dummy, but throws an exception if library (see above) cannot be loaded
    }
    
    public static enum Info_mc66 {
    	OK(0, "Subroutine completed successfully."),
    	ERR_MEM_ALLOC(-1, "Memory allocation failed."),
    	ERR_MEM_DEALLOC(-2, "Memory deallocation failed."),
    	ERR_NBLOCKS(-3, "NBLOCKS <= 0 or NBLOCKS > min(M,N)."),
    	ERR_M(-4, "M <= 0"),
    	ERR_N(-5, "N <= 0"),
    	ERR_NZ(-6, "NZ <= 0"),
    	WRN_KBLOCKS(1, "Number KBLOCKS of blocks in the SBBD form is less than the requested number of blocks NBLOCKS."),
    	WRN_IRN(2, "One or more entries in IRN lies outside the range [1,M]. These entries are ignored."),
    	WRN_JCN(3, "One or more entries in JCN lies outside the range [1,N]. These entries are ignored."),
    	WRN_DUP(4, "Duplicated entries were found."),
    	WRN_EMPTY(5, "One or more empty columns were found. Such empty columns are placed at the last diagonal."),    	
    	;
//    	INFO = 0 if the subroutine completed successfully.
//    	INFO = -1 if memory allocation failed.
//    	INFO = -2 if memory deallocation failed.
//    	INFO = -3 if NBLOCKS <= 0 or NBLOCKS > min(M,N).
//    	INFO = -4 if M <= 0.
//    	INFO = -5 if N <= 0.
//    	INFO = -6 if NZ <= 0.
//    	INFO = 1 if the number KBLOCKS of blocks in the SBBD form is less than the requested number of blocks
//    NBLOCKS.	       
//    	INFO = 2 if one or more entries in IRN lies outside the range [1,M]. These entries are ignored.
//    	INFO = 3 if one or more entries in JCN lies outside the range [1,N]. These entries are ignored.
//    	INFO = 4 if duplicated entries were found.
//    	INFO = 5 if one or more empty columns were found. Such empty columns are placed at the last diagonal
    	private final int code;
    	private final String message;
    	private Info_mc66(int code, String message) {
    		this.code		= code;
    		this.message	= message;
    	}
		public int getCode() {
			return code;
		}
		public String getMessage() {
			return message;
		}
		@Override
		public String toString() {
			return name() + " (code=" + code + ", msg=" + message + ")";
		}
		public boolean isOk() {
			return code == 0;
		}
		public boolean isError() {
			return code < 0;
		}
		public boolean isWarning() {
			return code > 0;
		}
		/**
		 * Returns the constant by code, or throws an exception if no such
		 * constant exists
		 */
		public static Info_mc66 valueByCode(int code) {
			for (final Info_mc66 info : values()) {
				if (info.code == code) return info;
			}
			throw new IllegalArgumentException("no info for code " + code);
		}
    }
    /**
     * Output structure to return multiple result values
     *
     */
    public static class Result_mc66 {
    	public Info_mc66 info;
    	public int[] row_order;
    	public int[] rowptr;
    	public int[] column_order;
    	public int[] colptr;
    	public int netcut;
    	public double rowdiff;
    	public int kblocks;
    }
    
    /**
     * @param m				number of matrix rows
     * @param n				number of matrix colums
     * @param irn			non-zero row indices, must have same length as jcn
     * @param jcn			non-zero column indices, must have same length as icn
     * @param nblocks		number of desired blocks
     */
    public static Result_mc66 mc66(int m,int n,int nz,int[] irn,int[] jcn,
    		int nblocks, /*control*/ int seed) {
    	
    	final int[] row_order = new int[m];    	
    	final int[] info_out = new int[1];
    	final int[] rowptr = new int[nblocks + 1];
    	final int[] column_order = new int[n];
    	final int[] colptr = new int[nblocks + 1];
    	final int[] netcut_out = new int[1];
    	final double[] rowdiff_out = new double[1];
    	final int[] kblocks_out = new int[1];
    	                               
    	HSL_mc66_double(m, n, nz, irn, jcn, nblocks, seed, row_order, info_out, rowptr, column_order, colptr, netcut_out, rowdiff_out, kblocks_out);
    	
    	Result_mc66 res = new Result_mc66();
    	res.row_order = row_order;
    	res.rowptr = rowptr;
    	res.column_order = column_order;
    	res.colptr = colptr;
    	res.netcut = netcut_out[0];
    	res.rowdiff = rowdiff_out[0];
    	res.kblocks = kblocks_out[0];
    	res.info = Info_mc66.valueByCode(info_out[0]);
    	
    	return res;
    }

    /**
	  subroutine monet(m,n,nz,irn,jcn,nblocks,control,seed, &
	       row_order,info,rowptr,column_order,colptr,netcut,&
	       rowdiff,kblocks)
	       
	 * See 
	 * http://hsl.rl.ac.uk/hsl2007/distrib/packages/hsl_mc66/hsl_mc66.pdf
	       
3 THE ARGUMENT LISTS
We use square brackets [ ] to indicate OPTIONAL arguments.
3.1 To generate the ordering for a matrix
CALL MC66(M,N,NZ,IRN,JCN,NBLOCKS,CONTROL,SEED,ROW ORDER,&
INFO[,ROWPTR,COLUMN ORDER,COLPTR,NETCUT,ROWDIFF,KBLOCKS])
M is an INTEGER scalar of INTENT (IN). On entry, it must be set by the user to hold the number of rows of A.
Restriction: M >= 1.
N is an INTEGER scalar of INTENT (IN). On entry, it must be set by the user to hold the number of columns of A.
Restriction: N >= 1.
NZ is an INTEGER scalar of INTENT (IN). On entry it must be set by the user to hold the number of entries in A.
Restriction: NZ >= 0.
IRN is an INTEGER array of rank one of size NZ with INTENT (IN). On entry, it must be set by the user to hold the
row indices of the matrix A.
JCN is an INTEGER array of rank one of size NZ with INTENT (IN). On entry, it must be set by the user to hold the
column indices of the matrix A.
NBLOCKS is an INTEGER scalar of INTENT (IN). On entry, it must be set by the user to hold the required number
of blocks in the SBBD form that the sparse matrix is to be reordered into. Restriction: 1 <= NBLOCKS <= min(M,N).
CONTROL is a scalar of type MC66 CONTROL with INTENT (INOUT). Its components control the ordering algorithm, as
explained in Section 2.2.1. The user does not need to set its components unless values other than the default
are required; if the user has reset any to a value ouside its permitted range, the value will be changed (see
Section 2.2.1).
SEED is a scalar of type FA14 SEED with INTENT (INOUT). It is used to hold the seed for the random numbers generated
by HSL FA14 for the calculation. It should not be altered by the user.
ROW ORDER is an INTEGER array of rank one of size M with INTENT (OUT). On exit, ROW ORDER(I) is the original row
index of the I-th row of the reordered matrix in SBBD form.
INFO is an INTEGER scalar of INTENT (OUT) that is used as an error/warning flag. Negative values indicate an error
and positive values a warning. On exit, possible values are:
	INFO = 0 if the subroutine completed successfully.
	INFO = -1 if memory allocation failed.
	INFO = -2 if memory deallocation failed.
	INFO = -3 if NBLOCKS <= 0 or NBLOCKS > min(M,N).
	INFO = -4 if M <= 0.
	INFO = -5 if N <= 0.
	INFO = -6 if NZ <= 0.
	INFO = 1 if the number KBLOCKS of blocks in the SBBD form is less than the requested number of blocks
NBLOCKS.	       
	INFO = 2 if one or more entries in IRN lies outside the range [1,M]. These entries are ignored.
	INFO = 3 if one or more entries in JCN lies outside the range [1,N]. These entries are ignored.
	INFO = 4 if duplicated entries were found.
	INFO = 5 if one or more empty columns were found. Such empty columns are placed at the last diagonal
block of the reordered matrix in SBBD form.
Note that INFO = 1 overwrites other positive values of INFO.
ROWPTR is an OPTIONAL INTEGER array of rank one of size NBLOCKS+1 with INTENT (OUT). On exit, ROWPTR(I)
is the starting row index for the I-th diagonal block in the reordered SBBD matrix (I = 1, : : :, NBLOCKS).
ROWPTR(NBLOCKS+1) = M.
COLUMN ORDER is an OPTIONAL INTEGER array of rank one of size N with INTENT (OUT). On exit, COLUMN ORDER(I)
is the original column index of the I-th column of the reordered matrix in SBBD form.
COLPTR is an OPTIONAL INTEGER array of rank one of size NBLOCKS+1 with INTENT (OUT). On exit, COLPTR(I) gives
the starting column index for the I-th diagonal block in the reordered SBBD matrix (I = 1, : : :, NBLOCKS),
and COLPTR(NBLOCKS+1) holds the starting column index of the border of the reordered matrix in SBBD form.
NETCUT is an OPTIONAL INTEGER scalar of INTENT (OUT). On exit, it holds the net-cut (the number of columns in the
border of the SBBD form).
ROWDIFF is an OPTIONAL REAL (double precision REAL for HSL MC66 DOUBLE) scalar of INTENT (OUT). On exit,
it holds the difference between the maximum block row dimension and the average block row dimension (=
M/NBLOCKS), divided by the average block row dimension, expressed as a percentage. Thus ROWDIFF = 10
denotes a 10% imbalance.
KBLOCKS is an OPTIONAL INTEGER scalar of INTENT (OUT). On exit, it holds the number K of blocks in the SBBD
form. In general, KBLOCKS = NBLOCKS but KBLOCKS may be smaller than NBLOCKS (see INFO = 1).
     */
    private static native void HSL_mc66_double(int m,int n,int nz,int[] irn,int[] jcn,
    		int nblocks, /*control*/ int seed, int[] row_order, int/*ptr*/[] info, 
    		int[] rowptr, int[] column_order, int[] colptr, 
    		int/*ptr*/[] netcut, double/*ptr*/[] rowdiff, int/*ptr*/[] kblocks);
    
}
