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
package ch.javasoft.metabolic.efm.sort;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.ops.Hsl;
import ch.javasoft.smx.ops.HslGateway;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.IntArray;
import ch.javasoft.util.logging.LogPrintWriter;
import ch.javasoft.util.numeric.Zero;

public class SortUtil {
	
	private static File traceSortingFile;
	
	public static final String DEFAULT_SORTER = "MostZerosOrAbsLexMin";
	
	private static final Logger LOG = LogPkg.LOGGER;
	
	public static void setTraceSortingFile(File file) {
		traceSortingFile = file;
	}
	
    public static void sortKernel(ReadableDoubleMatrix kernel, int[] rowMapping, MetabolicNetwork net, Config config) {
        LOG.finest("DEBUG: SortUtil.sortKernel");

    	sortMatrix(kernel, rowMapping, kernel.getColumnCount(), kernel.getRowCount(), false /*reverse sort*/, net, config);

    	if (traceSortingFile != null) {
    		try {
				kernel.writeToMultiline(new FileWriter(traceSortingFile));
			}
			catch (IOException e) {
				throw new RuntimeException("cannot trace row sorting, e=" + e, e);
			}
    	}
    }
    public static void sortStoich(ReadableDoubleMatrix stoich, int startRow, int[] rowMapping, MetabolicNetwork net, Config config) {
    	sortMatrix(stoich, rowMapping, startRow, stoich.getRowCount(), false /*reverse sort*/, net, config);
    	if (traceSortingFile != null) {
    		try {
				stoich.writeToMultiline(new FileWriter(traceSortingFile));
			}
			catch (IOException e) {
				throw new RuntimeException("cannot trace row sorting, e=" + e, e);
			}
    	}
    }
    private static void sortMatrix(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
    	final String rowOrdering = config.getRowOrdering();
    	sortMatrix(kernel, rowMapping, startRow, endRow, reverse, net, config, rowOrdering);
    }
    private static void sortMatrix(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config, String rowOrdering) {
        // System.out.println("DEBUG: SortUtil.sortMatrix: rowOrdering = " + rowOrdering);
    	if (rowOrdering.startsWith("Fixed:")) {
    		//looks like this
    		//Fixed:1:MostZerosOrAbsLexMin:1:3:8:16:19:15:18:17:20:21:25:24:23:26:22:5:2:6:14:12:9:7:13:11:10:4
    		//first number is index base
    		//next part is reference row ordering
    		//number sequence are number of rows to use compared to reference ordering
    		sortMatrixFixed(kernel, rowMapping, startRow, endRow, reverse, rowOrdering.substring("Fixed:".length()), net, config);
    	}
    	else {
        	try {
        		final Class[] signature = new Class[] {ReadableDoubleMatrix.class, int[].class, Integer.TYPE, Integer.TYPE, Boolean.TYPE, MetabolicNetwork.class, Config.class};
        		final Object[] args		= new Object[] {kernel, rowMapping, Integer.valueOf(startRow), Integer.valueOf(endRow), Boolean.valueOf(reverse), net, config};
                        // System.out.println("DEBUG: SortUtil.sortMatrix: class name: " + " sortMatrix" + rowOrdering);
        		Method method = SortUtil.class.getDeclaredMethod("sortMatrix" + rowOrdering, signature);
        		method.invoke(null, args);
        	}
        	catch (Exception ex) {
        		LOG.severe("cannot use row-ordering '" + rowOrdering + "', e=" + ex);
    			ex.printStackTrace(new LogPrintWriter(LOG, Level.SEVERE));
        		LOG.severe("defaulting to row ordering MostZerosOrAbsLexMin.");
        		sortMatrixMostZerosOrAbsLexMin(kernel, rowMapping, startRow, endRow, reverse, net, config);
        	}
    	}

    }

    ///////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////
    private static void resortKernelBecauseOfGeneRules( ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, HashMap hmIterPhaseReacIdx )
    {
       int r, i, c;
       int curResortIdx;

       LOG.finest("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): entered. startRow=" + startRow + " endRow=" + endRow);

       ///////////////////////////////////////////////////////////////
       // print new mapping
       ///////////////////////////////////////////////////////////////
       LOG.finest("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): mapping before resorting:");
       for( i = 0; i < rowMapping.length; i++ )
       {
    	   LOG.finest("DEBUG: mapping " + i + ": " + rowMapping[i] + " before resorting");
       }
       ///////////////////////////////////////////////////////////////

       curResortIdx = startRow;

       for( r = 0; r < endRow; r++ )
       {
          String strR = new Integer(r).toString();
          LOG.finest("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): checking reaction " + r + ": rowMapping: " + rowMapping[r]);
          if( hmIterPhaseReacIdx.containsKey(strR) )
          {
        	  LOG.finest("DEBUG: we found a reaction (" + r + ") that is part of an iteration phase rule");
          }
       }

       for( r = startRow; r < endRow; r++ )
       {

          String strR = new Integer(r).toString();
          LOG.finest("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): checking reaction " + r + ": " + strR);
          if( hmIterPhaseReacIdx.containsKey(strR) )
          {
             // we found a reaction that participates in an iteration phase rule
        	  LOG.finest("DEBUG: we found a reaction (" + r + ") that is part of an iteration phase rule");
             for( i = curResortIdx; i < r; i++ )
             {
                // System.out.println("DEBUG: swapping i=" + i + " with r=" + r);
                kernel.swapRows(i, r);
                IntArray.swap(rowMapping, i, r);
             }
             curResortIdx++;
          }
       }

       ///////////////////////////////////////////////////////////////
       // print new mapping
       ///////////////////////////////////////////////////////////////
       LOG.finest("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): mapping after resorting:");
       for( i = 0; i < rowMapping.length; i++ )
       {
    	   LOG.finest("DEBUG: mapping " + i + ": " + rowMapping[i] + " after resorting");
       }
       ///////////////////////////////////////////////////////////////

       ///////////////////////////////////////////////////////////////
       // print new kernel
       ///////////////////////////////////////////////////////////////
       // System.out.println("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): kernel after resorting:");
       // for( r = 0; r < kernel.getRowCount(); r++ )
       // {
       //    System.out.print(r + ": ");
       //    for( c = 0; c < kernel.getColumnCount(); c++ )
       //    {
       //       System.out.print(" " + kernel.getNumberValueAt(r,c));
       //    }
       //    System.out.println();
       // }
       ///////////////////////////////////////////////////////////////

       // System.out.println("DEBUG: SortUtil.resortKernelBecauseOfGeneRules(): leaving");
    }
    ///////////////////////////////////////////////////////////////////////////////////

    //default method
    private static void sortMatrixFixed(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, String fixedOrdering, MetabolicNetwork net, Config config) {
    	//fixedOrdering is a string like this:
    	//1:ordering:1:3:8:16:19:15:18:17:20:21:25:24:23:26:22:5:2:6:14:12:9:7:13:11:10:4
    	//it refers to the 'normal' ordering, that is, as if it was sorted using
    	//sortMatrixMostZerosOrAbsLexMin
    	//it only refers to the sorted part, not to the identity part
    	
    	final int colon1 		= fixedOrdering.indexOf(':');
    	final int colon2 		= fixedOrdering.indexOf(':', colon1 + 1);
    	final int indexBase		= Integer.parseInt(fixedOrdering.substring(0, colon1));
    	final String refSort	= fixedOrdering.substring(colon1 + 1, colon2);
    	
    	//sort with reference ordering first
    	sortMatrix(kernel, rowMapping, startRow, endRow, reverse, net, config, refSort);
		//now, reorder using the given tour
		String[] fix = fixedOrdering.substring(colon2 + 1).split(":");
		if (endRow - startRow != fix.length) {
			throw new IllegalArgumentException("expected ordering string with " + 
				(endRow - startRow) + " elements, but found " + fix.length);
		}
		
		final int[] fwdMapping = new int[rowMapping.length];
		final int[] bwdMapping = new int[rowMapping.length];
		for (int i = 0; i < rowMapping.length; i++) {
			fwdMapping[i] = i;
			bwdMapping[i] = i;
		}
		
		for (int i = startRow; i < (endRow - 1); i++) {
			int fi 		= startRow + Integer.parseInt(fix[i - startRow]) - indexBase;
			int bfi 	= bwdMapping[fi];
			int mi	 	= fwdMapping[i];
			int mbfi	= fi;//fwdMapping[bfi];
			if (i != bfi) {
				kernel.swapRows(i, bfi);
				IntArray.swap(rowMapping, i, bfi);
				IntArray.swap(fwdMapping, i, bfi);
				IntArray.swap(bwdMapping, mi, mbfi);
			}
		}
    }
    
	@SuppressWarnings("unused")
	private static void sortMatrixMonet(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
		final int nblocks = 2;
		
		final int cols = kernel.getColumnCount();
		final IntArray irn = new IntArray();
		final IntArray jcn = new IntArray();
		for (int i = startRow; i < endRow; i++) {
			for (int j = 0; j < cols; j++) {
				if (kernel.getSignumAt(i, j) != 0) {
//					irn.addInt(i - startRow + 1);
//					jcn.addInt(j + 1);
					//transpose
					jcn.add(i - startRow + 1);
					irn.add(j + 1);
				}
			}
		}
//		final Hsl.Result_mc66 sort66 = HslGateway.callMc66(endRow - startRow, cols, irn.toIntArray(), jcn.toIntArray(), nblocks);
		//transpose
		final Hsl.Result_mc66 sort66 = HslGateway.callMc66(cols, endRow - startRow, irn.toArray(), jcn.toArray(), nblocks);
		
		//rows
		int[] fwdMapping = new int[rowMapping.length];
		int[] bwdMapping = new int[rowMapping.length];
		for (int i = 0; i < rowMapping.length; i++) {
			fwdMapping[i] = i;
			bwdMapping[i] = i;
		}
		
		for (int i = startRow; i < (endRow - 1); i++) {
//			int fi 		= startRow + sort66.row_order[i - startRow] - 1;
			//transpose
			int fi 		= startRow + sort66.column_order[i - startRow] - 1; 
			int bfi 	= bwdMapping[fi];
			int mi	 	= fwdMapping[i];
			int mbfi	= fi;//fwdMapping[bfi];
			if (i != bfi) {
				kernel.swapRows(i, bfi);
				IntArray.swap(rowMapping, i, bfi);
				IntArray.swap(fwdMapping, i, bfi);
				IntArray.swap(bwdMapping, mi, mbfi);
			}
		}

		//cols
		fwdMapping = new int[cols];
		bwdMapping = new int[cols];
		for (int i = 0; i < cols; i++) {
			fwdMapping[i] = i;
			bwdMapping[i] = i;
		}
		
		for (int i = 0; i < (cols - 1); i++) {
//			int fi 		= sort66.column_order[i] - 1;
			//transpose
			int fi 		= sort66.row_order[i] - 1; 
			int bfi 	= bwdMapping[fi];
			int mi	 	= fwdMapping[i];
			int mbfi	= fi;//fwdMapping[bfi];
			if (i != bfi) {
				kernel.swapColumns(i, bfi);
				IntArray.swap(fwdMapping, i, bfi);
				IntArray.swap(bwdMapping, mi, mbfi);
			}
		}
	}
    public static void main(String[] args) {
    	int startRow 	= 0;
    	int endRow		= 6;
		String[] fix = "1:2:5:3:0:4".split(":");
		if (endRow - startRow != fix.length) {
			throw new IllegalArgumentException("expected ordering string with " + 
				(endRow - startRow) + " elements, but found " + fix.length);
		}
		
		final int[] rowMapping	= new int[] {0,1,2,3,4,5};
		final int[] backMapping = new int[rowMapping.length];
		for (int i = 0; i < rowMapping.length; i++) {
			backMapping[rowMapping[i]] = i;
		}
		
		for (int i = startRow; i < (endRow - 1); i++) {
			int fi 		= Integer.parseInt(fix[i]);
			int bfi 	= backMapping[fi];
			int mi	 	= rowMapping[i];
			int mbfi	= fi;//rowMapping[bfi];
			if (i != bfi) {
				IntArray.swap(rowMapping, i, bfi);
				IntArray.swap(backMapping, mi, mbfi);
			}
		}
    	System.out.println(java.util.Arrays.toString(rowMapping));
    	System.out.println(java.util.Arrays.toString(backMapping));
    }
    //default method
    private static void sortMatrixMostZerosOrAbsLexMin(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
        
        //////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////
        int rows = kernel.getRowCount();

        // System.out.println("DEBUG: SortUtil.sortMatrixMostZerosOrAbsLexMin: cols: " + cols);
        // System.out.println("DEBUG: SortUtil.sortMatrixMostZerosOrAbsLexMin: rows: " + rows);
        // System.out.println("DEBUG: SortUtil.sortMatrixMostZerosOrAbsLexMin: startRow: " + startRow);
        // System.out.println("DEBUG: SortUtil.sortMatrixMostZerosOrAbsLexMin: endRow: " + endRow);

        // System.out.println("DEBUG: SortUtil.sortMatrixMostZerosOrAbsLexMin: before sorting");
        // for( int r = 0; r < rows; r++ )
        // {
        //    System.out.print(r + ": ");
        //    for( int c = 0; c < cols; c++ )
        //    {
        //       System.out.print(" " + kernel.getNumberValueAt(r,c));
        //    }
        //    System.out.println();
        // }
        //////////////////////////////////////////////////////////

//      do not sort the upper square part since we have the identity matrix here
        sortMatrix(
            kernel, rowMapping,
            rowMapping == null ?
                new CascadingSorter(
                    new MostZerosSorter(true, startRow, endRow, 0, cols, new Zero()),
                    new AbsLexMinSorter(true, startRow, endRow, 0, cols)                    
                )
            		:
            	new CascadingSorter(
	        		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	                new MostZerosSorter(true, startRow, endRow, 0, cols, new Zero()),
	                new AbsLexMinSorter(true, startRow, endRow, 0, cols)                    
            	),
            reverse,
            null, false
        );      

        //////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////
        // System.out.println("DEBUG: SortUtil.sortMatrixMostZerosOrAbsLexMin: after sorting");
        // for( int r = 0; r < rows; r++ )
        // {
        //    System.out.print(r + ": ");
        //    for( int c = 0; c < cols; c++ )
        //    {
        //       System.out.print(" " + kernel.getNumberValueAt(r,c));
        //    }
        //    System.out.println();
        // }
        //////////////////////////////////////////////////////////
    }
	@SuppressWarnings("unused")
	private static void sortMatrixRandom(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
//		do not sort the upper square part since we have the identity matrix here
		IntArray arr = new IntArray(endRow - startRow);
		for (int i = startRow; i < endRow; i++) {
			arr.add(i);
		}
		for (int i = startRow; i < endRow; i++) {
			int arrIndex = (int)(Math.random() * arr.length());
			int rowIndex = arr.get(arrIndex);
			if (i != rowIndex) {
				kernel.swapRows(i, rowIndex);
				Arrays.swap(rowMapping, i, rowIndex);
			}
			int lastValue = arr.removeLast();
			if (arrIndex != arr.length()) {
				arr.set(arrIndex, lastValue);
			}			
		}
		//ensure that non-iterated reactions are at the end
        if (rowMapping != null) {
	        sortMatrix(
	            kernel, rowMapping, 
	            new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	            reverse,
	            null, false
	        );
        }
	}
	@SuppressWarnings("unused")
	private static void sortMatrixMostZeros(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
		int cols = kernel.getColumnCount();
//		do not sort the upper square part since we have the identity matrix here
		sortMatrix(
			kernel, rowMapping, 
            new CascadingSorter(
        		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
    			new MostZerosSorter(true, startRow, endRow, 0, cols, new Zero())
            ),
			reverse,
			null, false
		);		
	}
	@SuppressWarnings("unused")
    private static void sortMatrixFewestNegPos(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        Zero zero = new Zero();
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
                new CascadingSorter(
                    new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero)
                ) 
            :
	            new CascadingSorter(
	        		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	                new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero)
	            ),
            reverse,
            null, false
        );      
    }
	@SuppressWarnings("unused")
    private static void sortMatrixAbsLexMin(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
                new CascadingSorter(
                   	new AbsLexMinSorter(true, startRow, endRow, 0, cols)                    
                )
            :
	            new CascadingSorter(
	        		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	               	new AbsLexMinSorter(true, startRow, endRow, 0, cols)                    
	            ),
            reverse,
            null, false
        );      
    }
	@SuppressWarnings("unused")
    private static void sortMatrixLexMin(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
                new CascadingSorter(
                    new LexMinSorter(true, startRow, endRow, 0, cols)                    
                )
            :
	            new CascadingSorter(
	        		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	                new LexMinSorter(true, startRow, endRow, 0, cols)                    
	            ),
            reverse,
            null, false
        );      
    }
	@SuppressWarnings("unused")
    private static void sortMatrixMostZerosOrFewestNegPos(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        Zero zero = new Zero();
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
                new CascadingSorter(
                    new MostZerosSorter(true, startRow, endRow, 0, cols, zero),
                    new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero)                    
                )
            :
	            new CascadingSorter(
	        		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	                new MostZerosSorter(true, startRow, endRow, 0, cols, zero),
	                new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero)                    
	            ),
            reverse,
            null, false
        );      
    }
	@SuppressWarnings("unused")
    private static void sortMatrixMostZerosOrLexMin(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
                new CascadingSorter(
                	new MostZerosSorter(true, startRow, endRow, 0, cols, new Zero()),
                    new LexMinSorter(true, startRow, endRow, 0, cols)                    
                )
            :
	            new CascadingSorter(
	           		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	            	new MostZerosSorter(true, startRow, endRow, 0, cols, new Zero()),
	                new LexMinSorter(true, startRow, endRow, 0, cols)                    
	            ),
            reverse,
            null, false
        );      
    }
	@SuppressWarnings("unused")
    private static void sortMatrixFewestNegPosOrMostZeros(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        Zero zero = new Zero();
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
                new CascadingSorter(
                	new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero),                    
                    new MostZerosSorter(true, startRow, endRow, 0, cols, zero)
                )
            :
	            new CascadingSorter(
	           		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	            	new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero),                    
	                new MostZerosSorter(true, startRow, endRow, 0, cols, zero)
	            ),
            reverse,
            null, false
        );      
    }
	@SuppressWarnings("unused")
    private static void sortMatrixFewestNegPosOrAbsLexMin(ReadableDoubleMatrix kernel, int[] rowMapping, int startRow, int endRow, boolean reverse, MetabolicNetwork net, Config config) {
        int cols = kernel.getColumnCount();
//      do not sort the upper square part since we have the identity matrix here
        Zero zero = new Zero();
        sortMatrix(
            kernel, rowMapping, 
            rowMapping == null ?
		        new CascadingSorter(
	            	new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero),                    
	                new AbsLexMinSorter(true, startRow, endRow, 0, cols)                    
	            )
            :
	            new CascadingSorter(
	          		new SuppressedEnforcedNoSplitSorter(net, config, rowMapping, startRow, endRow),
	            	new FewestNegPosSorter(true, startRow, endRow, 0, cols, zero),                    
	                new AbsLexMinSorter(true, startRow, endRow, 0, cols)                    
	            ),
            reverse,
            null, false
        );      
    }


	private static void sortMatrix(ReadableDoubleMatrix kernel, int[] rowMapping, MatrixSorter rowSorter, boolean reverseRows, MatrixSorter colSorter, boolean reverseCols) {
		LOG.finest("DEBUG: SortUtil.sortMatrix()\n");
		//sort columns
		if (colSorter != null) sortMatrixColumns(kernel, null, colSorter, reverseCols);
		//sort metas
		if (rowSorter != null) sortMatrixRows(kernel, rowMapping, rowSorter, reverseRows);
	}



	public static void sortMatrixRows(ReadableDoubleMatrix mx, int[] mapping, MatrixSorter rowSorter, boolean reverse) {
                ///////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////
                // System.out.println("DEBUG: SortUtil.sortMatrixRows()\n");
                // for( int i = 0; i < mapping.length; i++ )
                // {
                //    System.out.println("DEBUG: mapping " + i + ": " + mapping[i] + " before");
                // }
                ///////////////////////////////////////////////////////////////
		if (rowSorter != null) {
			double rev = reverse ? -1d : 1d;
			if (!rowSorter.compareRows()) throw new IllegalArgumentException("not a row sorter");
			int rows = rowSorter.end(mx);
			//do not sort the upper square part since we have the identity matrix here
			for (int pivotRow = rowSorter.start(mx); pivotRow < rows; pivotRow++) {
				int minRow = pivotRow;
				for (int row = pivotRow + 1; row < rows; row++) {
					if (rev * rowSorter.compare(mx, minRow, row) > 0) {
						minRow = row;
					}
				}
				if (minRow != pivotRow) {
					mx.swapRows(minRow, pivotRow);
					if (mapping != null) {
						int tmp = mapping[minRow];
						mapping[minRow]		= mapping[pivotRow];
						mapping[pivotRow]	= tmp;						
					}
				}
			}			
		}
                ///////////////////////////////////////////////////////////////
                ///////////////////////////////////////////////////////////////
                // System.out.println("DEBUG: SortUtil.sortMatrixRows()\n");
                // for( int i = 0; i < mapping.length; i++ )
                // {
                //    System.out.println("DEBUG: mapping " + i + ": " + mapping[i] + " after");
                // }
                ///////////////////////////////////////////////////////////////
	}



	public static void sortMatrixColumns(ReadableDoubleMatrix mx, int[] mapping, MatrixSorter colSorter, boolean reverse) {
		if (colSorter != null) {
			double rev = reverse ? -1d : 1d;
			if (colSorter.compareRows()) throw new IllegalArgumentException("not a col sorter");
			int cols = colSorter.end(mx);
			for (int pivotCol = colSorter.start(mx); pivotCol < cols; pivotCol++) {
				int minCol = pivotCol;
				for (int col = pivotCol + 1; col < cols; col++) {
					if (rev * colSorter.compare(mx, minCol, col) > 0) {
						minCol = col;
					}
				}
				if (minCol != pivotCol) {
					mx.swapColumns(minCol, pivotCol);
					if (mapping != null) {
						int tmp = mapping[minCol];
						mapping[minCol]		= mapping[pivotCol];
						mapping[pivotCol]	= tmp;						
					}
				}
			}
		}
	}

}
