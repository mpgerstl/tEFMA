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
package ch.javasoft.metabolic.efm.column;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.jbase.FixedWidthMarshaller;
import ch.javasoft.jbase.FixedWidthTable;
import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.math.ops.DoubleOperations;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.memory.outcore.Cache;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.metabolic.impl.DefaultFluxDistribution;
import ch.javasoft.smx.iface.ReadableDoubleMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultDoubleMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.numeric.Zero;

/**
 * A <tt>DoubleColumn</tt> implements the numeric part of the column with double
 * values.
 */
public class DoubleColumn extends AbstractColumn {

	private int				mBoolSize;
	private final IBitSet	mBitSet;
	private double[]		mValues;

    public DoubleColumn(int boolSize) {
        this(boolSize, 0);
    }
	public DoubleColumn(int boolSize, int doubleSize) {
		mBoolSize	= boolSize;
		mBitSet		= BitSetUtil.factory().create(boolSize);
		mValues		= new double[doubleSize];
	}
	protected DoubleColumn(int boolSize, IBitSet bitSet, double[] values) {
		mBoolSize	= boolSize;
		mBitSet 	= bitSet;
		mValues		= values;
	}
	
	public IBitSet bitValues() {
		return mBitSet;
	}
	
	public int getNumericSignum(Zero zero, int row) {
		return zero.sgn(mValues[row]);
	}
	public int getHyperplaneSign(EfmModel model, IterationStateModel iteration) {
		return getColumnInspectorModifier(model, Double.class, double[].class).getHyperplaneSign(columnHome(), model, mBitSet, mBoolSize, mValues, iteration);
	}
	public <Col extends Column> Col convert(ColumnHome<?, Col> columnHome, EfmModel model, IterationStepModel iteration, boolean clone) {
		final ColumnInspectorModifier<Double, double[]> modifier = getColumnInspectorModifier(model, Double.class, double[].class);
		final IBitSet	newBin = modifier.convertBinary(columnHome(), model, mBitSet, mBoolSize, mValues, iteration, clone);
		final double[]			newNum = modifier.convertNumeric(columnHome(), model, mBitSet, mBoolSize, mValues, iteration, clone);

		if (clone) {
			return columnHome.castColumn(new DoubleColumn(iteration.getNextState().getBooleanSize(), newBin, newNum));
		}
		else {
			mBoolSize = iteration.getNextState().getBooleanSize();
			if (mBitSet != newBin) {
				mBitSet.clear();
				mBitSet.or(newBin);
			}
			mValues = newNum;
			return columnHome.castColumn(this);
		}
	}
	public <Col extends Column> Col mergeWith(ColumnHome<?,Col> columnHome, EfmModel model, Col other, IterationStepModel iteration) {
		return columnHome.castColumn(mergeWith(model, (DoubleColumn)other, iteration));
	}
	public DoubleColumn mergeWith(EfmModel model, DoubleColumn other, IterationStepModel iteration) {
		final ColumnInspectorModifier<Double, double[]> modifier = getColumnInspectorModifier(model, Double.class, double[].class);
		final IBitSet newBin = modifier.mergeBinary(columnHome(), model, mBitSet, mBoolSize, mValues, other.mBitSet, other.mBoolSize, other.mValues, iteration);
		final double[] newNum = modifier.mergeNumeric(columnHome(), model, mBitSet, mBoolSize, mValues, other.mBitSet, other.mBoolSize, other.mValues, iteration);
		return new DoubleColumn(iteration.getNextState().getBooleanSize(), newBin, newNum);
	}
	
	public int booleanSize() {
		return mBoolSize;
	}
	public int numericSize() {
		return mValues.length;
	}
	public int size() {
		return mBoolSize + mValues.length;
	}
	
	public <N extends Number> N getNumeric(ColumnHome<N, ?> columnHome, int row) {
		return columnHome.castNumber(Double.valueOf(mValues[row]));
	}
	
	
	@Override
	public DoubleColumn clone() {
		return new DoubleColumn(mBoolSize, mBitSet.clone(), mValues.clone());
	}

	public void writeTo(DataOutput dataOut) throws IOException {
		columnHome().writeTo(this, dataOut);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof DoubleColumn) {
			final DoubleColumn col = (DoubleColumn)obj;
			return
				mBoolSize == col.mBoolSize &&
				mBitSet.equals(col.mBitSet) && 
				java.util.Arrays.equals(mValues, col.mValues);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('{');
		for (int ii = 0; ii < mBoolSize; ii++) {
			sb.append(mBitSet.get(ii) ? '1' : '0');
		}
		for (int ii = 0; ii < mValues.length; ii++) {
			if (mBoolSize > 0 || ii > 0) sb.append(", ");
			sb.append(mValues[ii]);
		}
		sb.append('}');
		return sb.toString();
	}
    
	public Home columnHome() {
		return HOME;
	}
	
	public static abstract class Home 
		extends		AbstractHome<Double, DoubleColumn> 
		implements	ColumnHome<Double, DoubleColumn> {}
	
	public static final Home HOME = new Home() {
		public Arithmetic getArithmetic() {
			return Arithmetic.double_;
		}
		public DoubleOperations getNumberOperations() {
			return DoubleOperations.instance();
		}
		public DoubleColumn newInstance(int booleanSize, int numericSize) {
			return new DoubleColumn(booleanSize, numericSize);
		}
		public DoubleColumn[] newInstances(ReadableMatrix<Double> matrix, int booleanSize) {
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final DoubleColumn[] res = new DoubleColumn[cols];
			for (int col = 0; col < cols; col++) {
				res[col] = new DoubleColumn(booleanSize, rows);
				final double[] vals = res[col].mValues;
				for (int row = 0; row < rows; row++) {
					vals[row] = matrix.getNumberValueAt(row, col).doubleValue();
				}
			}
			return res;
		}
		public DoubleColumn readFrom(DataInput dataIn, int booleanSize, int numericSize) throws IOException {
			IBitSet bitSet = readBinaryFrom(dataIn, booleanSize);
			double[] dbls	= new double[numericSize];
			for (int i = 0; i < numericSize; i++) {
				dbls[i] = dataIn.readDouble();
			}
			return new DoubleColumn(booleanSize, bitSet, dbls);
		}
		public void writeTo(DoubleColumn column, DataOutput dataOut) throws IOException {
			writeBinaryTo(column, dataOut);
			for (int i = 0; i < column.mValues.length; i++) {
				dataOut.writeDouble(column.mValues[i]);
			}
		}
		public FixedWidthMarshaller<DoubleColumn> getEntityMarshaller(final int booleanSize, final int numericSize) throws IOException {
        	final int byteWidth = 	/*bits as bytes*/ BitSetUtil.byteSize(booleanSize) +
        							/*doubles*/ numericSize * 8;
			return new FixedWidthMarshaller<DoubleColumn>() {
                public int getByteWidth() {
                    return byteWidth; 
                }
                public DoubleColumn readFrom(DataInput in) throws IOException {
                    return HOME.readFrom(in, booleanSize, numericSize);
                }
                public void writeTo(DoubleColumn entity, DataOutput out) throws IOException {
                    HOME.writeTo(entity, out);
                }
            };		
		}
        public ConcurrentTable<DoubleColumn> createTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            return new ConcurrentTable<DoubleColumn>(
                FixedWidthTable.create(
                   	new File(folder, fileName), 
                   	getEntityMarshaller(booleanSize, numericSize),
                    Cache.DoubleMemoryTable.getCacheTableSize(),
                    Cache.DoubleMemoryTable.getCacheEntrySize()                   	
                )
            );
        }
        public ConcurrentTable<DoubleColumn> openTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            return new ConcurrentTable<DoubleColumn>(
                FixedWidthTable.open(
                	new File(folder, fileName), 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.DoubleMemoryTable.getCacheTableSize(),
                    Cache.DoubleMemoryTable.getCacheEntrySize()
            	));
        }
		public DefaultFluxDistribution createFluxDistribution(MetabolicNetwork net, Double[] values) {
			final double[] vals = new double[values.length];
			for (int i = 0; i < vals.length; i++) {
				vals[i] = values[i].doubleValue();
			}
			return createFluxDistribution(net, vals);
		}
		public DefaultFluxDistribution createFluxDistribution(MetabolicNetwork net, double[] values) {
			return new DefaultFluxDistribution(net, values);
		}
		public ReadableMatrix<Double> convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling) {
			if (matrix instanceof ReadableDoubleMatrix) {
				return ((ReadableDoubleMatrix)matrix).toDoubleMatrix(false);
			}
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final DefaultDoubleMatrix mx = new DefaultDoubleMatrix(rows, cols);
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					mx.setValueAt(row, col, matrix.getNumberValueAt(row, col).doubleValue());
				}
			}
			return mx;
		}
		public ReadableMatrix<Double> castMatrix(ReadableMatrix matrix) {
			if (matrix instanceof ReadableDoubleMatrix) {
				return ((ReadableDoubleMatrix)matrix).toDoubleMatrix(false);
			}
			throw new ClassCastException("not a ReadableDoubleMatrix: " + matrix.getClass().getName());
		}
		public DoubleColumn castColumn(Column column) {
			return (DoubleColumn)column;
		}
		public Double castNumber(Number number) {
			return (Double)number;
		}
		public int rank(ReadableMatrix matrix, Zero zero) {
			return new Gauss(zero.mZeroPos).rank((ReadableDoubleMatrix)matrix);
		}
	};
    
}
