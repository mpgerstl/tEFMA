package com.jmatio.types;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class MLSparse extends MLNumericArray<Double>
{
    int nzmax;
    private Set<IndexMN> indexSet;
    private Map<IndexMN, Double> real;  
    private Map<IndexMN, Double> imaginary;  
    
    public MLSparse(String name, int[] dims, int attributes, int nzmax )
    {
        super(name, dims, MLArray.mxSPARSE_CLASS, attributes);
        this.nzmax = nzmax;
        real = new LinkedHashMap<IndexMN, Double>();
        imaginary = new LinkedHashMap<IndexMN, Double>();
        indexSet = new LinkedHashSet<IndexMN>();
    }
    
    /**
     * Gets maximum number of non-zero values
     */
    public int getMaxNZ()
    {
        return nzmax;
    }
    /**
     * Gets row indices
     * 
     * <tt>ir</tt> points to an integer array of length nzmax containing the row indices of
     * the corresponding elements in <tt>pr</tt> and <tt>pi</tt>.
     */
    public int[] getIR()
    {
        int[] ir = new int[nzmax];
        int i = 0;
        for ( IndexMN index : indexSet )
        {
            ir[i++] = index.m;
        }
        return ir;
    }
    /**
     * Gets collumn indices. 
     * 
     * <tt>jc</tt> points to an integer array of length N+1 that contains column index information.
     * For j, in the range <tt>0&lt;=j&lt;=N???1</tt>, <tt>jc[j]</tt> is the index in ir and <tt>pr</tt> (and <tt>pi</tt>
     * if it exists) of the first nonzero entry in the jth column and <tt>jc[j+1]???1</tt> index
     * of the last nonzero entry. As a result, <tt>jc[N]</tt> is also equal to nnz, the number
     * of nonzero entries in the matrix. If nnz is less than nzmax, then more nonzero
     * entries can be inserted in the array without allocating additional storage
     */
    public int[] getJC()
    {
        int[] jc = new int[getN()+1];
        
        //create tmp array of nnz column indices
        int[] tmp = new int[nzmax];
        int i = 0;
        for ( IndexMN index : indexSet )
        {
            tmp[i++] = index.n;
        }
        
        //create jc
        int c = 0;
        for ( int k = 0; k < jc.length - 1; k++ )
        {
            if ( k < tmp.length )
            {
                c = tmp[k];
            }
            jc[k] = c;
        }
        //last one is nzmax
        jc[jc.length-1] = nzmax;
        
        return jc;
    }
    
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.GenericArrayCreator#createArray(int, int)
     */
    public Double[] createArray(int m, int n)
    {
        return null;
    }
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLNumericArray#getReal(int, int)
     */
    @Override
	public Double getReal(int m, int n)
    {
        IndexMN i = new IndexMN(m,n);
        if ( real.containsKey(i) )
        {
            return real.get(i);
        }
        return new Double(0);
    }
    @Override
	public void setReal(Double value, int m, int n)
    {
        IndexMN i = new IndexMN(m,n);
        indexSet.add(i);
        real.put(i, value );
    }
    @Override
	public void setReal(Double value, int index)
    {
        throw new IllegalArgumentException("Can't set Sparse array elements by index. " +
                "Please use setReal(Double value, int m, int n) instead.");
    }
    @Override
	public void setImaginary(Double value, int m, int n)
    {
        IndexMN i = new IndexMN(m,n);
        indexSet.add(i);
        imaginary.put(new IndexMN(m,n), value );
    }
    @Override
	public void setImaginary(Double value, int index)
    {
        throw new IllegalArgumentException("Can't set Sparse array elements by index. " +
        "Please use setImaginary(Double value, int m, int n) instead.");
    }
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLNumericArray#getImaginary(int, int)
     */
    @Override
	public Double getImaginary(int m, int n)
    {
        IndexMN i = new IndexMN(m,n);
        if ( imaginary.containsKey(i) )
        {
            return imaginary.get(i);
        }
        return new Double(0);
    }
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLNumericArray#exportReal()
     */
    @Override
	public Double[] exportReal()
    {
        Double[] ad = new Double[nzmax];
        int i = 0;
        for ( Double d : real.values() )
        {
            ad[i++] = d;
        }
        return ad;
        
    }
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLNumericArray#exportImaginary()
     */
    @Override
	public Double[] exportImaginary()
    {
        Double[] ad = new Double[nzmax];
        int i = 0;
        for ( Double d : imaginary.values() )
        {
            ad[i++] = d;
        }
        return ad;
    }
    /* (non-Javadoc)
     * @see com.paradigmdesigner.matlab.types.MLArray#contentToString()
     */
    @Override
	public String contentToString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name + " = \n");
        
        for ( IndexMN i : indexSet )
        {
            sb.append("\t(");
            sb.append(i.m + "," + i.n);
            sb.append(")");
            sb.append("\t" + getReal(i.m, i.n) );
            if ( isComplex() )
            {
                sb.append("+" + getImaginary(i.m, i.n) );
            }
            sb.append("\n");
            
        }
        
        return sb.toString();
    }
    
    /**
     * Matrix index (m,n)
     * 
     * @author Wojciech Gradkowski <wgradkowski@gmail.com>
     */
    private class IndexMN
    {
        int m;
        int n;
        
        public IndexMN( int m, int n )
        {
            this.m = m;
            this.n = n;
        }
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
		public int hashCode()
        {
            long l = Double.doubleToLongBits(m);
            l ^= Double.doubleToLongBits(n)*31L;
            return (int) l^(int) (l >> 32);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
		public boolean equals(Object o)
        {
            if (o instanceof IndexMN )
            {
                return m == ((IndexMN)o).m && n == ((IndexMN)o).n;
            }
            return super.equals(o);
        }
        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
		public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            sb.append("m=" + m);
            sb.append(", ");
            sb.append("n=" + n);
            sb.append("}");
            return sb.toString();
        }
    }

}
