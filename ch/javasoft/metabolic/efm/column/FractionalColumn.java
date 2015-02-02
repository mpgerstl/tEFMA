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
import java.math.BigInteger;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.jbase.EntityMarshaller;
import ch.javasoft.jbase.VariableWidthTable;
import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.math.BigFraction;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.ops.BigFractionOperations;
import ch.javasoft.metabolic.FluxDistribution;
import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.memory.outcore.Cache;
import ch.javasoft.metabolic.efm.model.ColumnInspectorModifier;
import ch.javasoft.metabolic.efm.model.EfmModel;
import ch.javasoft.metabolic.efm.model.IterationStateModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.util.BitSetUtil;
import ch.javasoft.metabolic.impl.FractionNumberFluxDistribution;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerRationalMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.numeric.Zero;

/**
 * A <tt>FractionalColumn</tt> implements the numeric part of the column with 
 * {@link BigFraction large fraction numbers}.
 */
public class FractionalColumn extends AbstractColumn {
	
	private int				mBoolSize;
	private final IBitSet	mBitSet;
	private BigFraction[]	mValues; 

	public FractionalColumn(int boolSize) {
		mBoolSize	= boolSize;
		mBitSet		= BitSetUtil.factory().create(boolSize);
		mValues		= new BigFraction[0];
	}
	protected FractionalColumn(int boolSize, IBitSet bitSet, BigFraction[] values) {
		mBoolSize	= boolSize;
		mBitSet 	= bitSet;
		mValues		= values;
	}
	
	public IBitSet bitValues() {
		return mBitSet;
	}
	
	public <N extends Number> N getNumeric(ColumnHome<N, ?> columnHome, int row) {
		return columnHome.castNumber(mValues[row]);
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
	
	public int getNumericSignum(Zero zero, int row) {
		return mValues[row].signum();
	}
	public int getHyperplaneSign(EfmModel model, IterationStateModel iteration) {
		return getColumnInspectorModifier(model, BigFraction.class, BigFraction[].class).getHyperplaneSign(columnHome(), model, mBitSet, mBoolSize, mValues, iteration);
	}
	public <Col extends Column> Col convert(ColumnHome<?,Col> columnHome, EfmModel model, IterationStepModel iteration, boolean clone) {
		final ColumnInspectorModifier<BigFraction, BigFraction[]> modifier = getColumnInspectorModifier(model, BigFraction.class, BigFraction[].class);
		final IBitSet	newBin = modifier.convertBinary(columnHome(), model, mBitSet, mBoolSize, mValues, iteration, clone);
		final BigFraction[]		newNum = modifier.convertNumeric(columnHome(), model, mBitSet, mBoolSize, mValues, iteration, clone);

		if (clone) {
			return columnHome.castColumn(new FractionalColumn(iteration.getNextState().getBooleanSize(), newBin, newNum));
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
		return columnHome.castColumn(mergeWith(model, (FractionalColumn)other, iteration));
	}
	public FractionalColumn mergeWith(EfmModel model, FractionalColumn other, IterationStepModel iteration) {
		final ColumnInspectorModifier<BigFraction, BigFraction[]> modifier = getColumnInspectorModifier(model, BigFraction.class, BigFraction[].class);
		final IBitSet newBin = modifier.mergeBinary(columnHome(), model, mBitSet, mBoolSize, mValues, other.mBitSet, other.mBoolSize, other.mValues, iteration);
		final BigFraction[] newNum = modifier.mergeNumeric(columnHome(), model, mBitSet, mBoolSize, mValues, other.mBitSet, other.mBoolSize, other.mValues, iteration);
		return new FractionalColumn(iteration.getNextState().getBooleanSize(), newBin, newNum);
	}

	public void writeTo(DataOutput dataOut) throws IOException {
		columnHome().writeTo(this, dataOut);
	}
	
	@Override
	public FractionalColumn clone() {
		return new FractionalColumn(mBoolSize, mBitSet.clone(), mValues.clone());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof FractionalColumn) {
			final FractionalColumn col = (FractionalColumn)obj;
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
		extends		AbstractHome<BigFraction, FractionalColumn> 
		implements	ColumnHome<BigFraction, FractionalColumn> {}
	
	public static final Home HOME = new Home() {
		public Arithmetic getArithmetic() {
			return Arithmetic.fractional;
		}
		public NumberOperations<BigFraction> getNumberOperations() {
			return BigFractionOperations.instance();
		}
		public FractionalColumn newInstance(int booleanSize, int numericSize) {
            throw new RuntimeException("not implemented");
			//return new FractionalColumn(booleanSize, numericSize);
		}
		public FractionalColumn[] newInstances(ReadableMatrix<BigFraction> matrix, int booleanSize) {
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final FractionalColumn[] res = new FractionalColumn[cols];
			for (int col = 0; col < cols; col++) {
				final BigFraction[] vals = new BigFraction[rows];
				for (int row = 0; row < rows; row++) {
                    vals[row] = matrix.getNumberValueAt(row, col).reduce(); 
				}
                res[col] = new FractionalColumn(booleanSize, BitSetUtil.factory().create(rows), vals);
			}
			return res;
		}
		public FractionalColumn readFrom(DataInput dataIn, int booleanSize, int numericSize) throws IOException {
			final IBitSet bitSet = readBinaryFrom(dataIn, booleanSize);
            final BigFraction[] values = new BigFraction[numericSize];  
			for (int i = 0; i < numericSize; i++) {
				int byteCnt;
				final byte[] numBytes, denBytes;
				
				//read numerator
				byteCnt = dataIn.readInt();
                numBytes = new byte[byteCnt];
				for (int j = 0; j < numBytes.length; j++) {
                    numBytes[j] = dataIn.readByte();
				}
				
				//read denominator
				byteCnt = dataIn.readInt();
                denBytes = new byte[byteCnt];
				for (int j = 0; j < denBytes.length; j++) {
                    denBytes[j] = dataIn.readByte();
				}

				final BigInteger num = new BigInteger(numBytes);
				final BigInteger den = new BigInteger(denBytes);				
				values[i] = new BigFraction(
					numBytes.length <= 8 ? BigInteger.valueOf(num.longValue()) : num, 
					denBytes.length <= 8 ? BigInteger.valueOf(den.longValue()) : den
				);
			}
			return new FractionalColumn(booleanSize, bitSet, values);
		}
		public void writeTo(FractionalColumn column, DataOutput dataOut) throws IOException {
			writeBinaryTo(column, dataOut);
			for (int i = 0; i < column.mValues.length; i++) {
				byte[] bytes;
				
				//write numerator
				bytes = column.mValues[i].getNumerator().toByteArray();
				dataOut.writeInt(bytes.length);
				for (int j = 0; j < bytes.length; j++) {
					dataOut.writeByte(bytes[j]);
				}
				
				//write denominator
				bytes = column.mValues[i].getDenominator().toByteArray();
				dataOut.writeInt(bytes.length);
				for (int j = 0; j < bytes.length; j++) {
					dataOut.writeByte(bytes[j]);
				}
			}
		}
		public EntityMarshaller<FractionalColumn> getEntityMarshaller(final int booleanSize, final int numericSize) throws IOException {
			return new EntityMarshaller<FractionalColumn>() {
                public FractionalColumn readFrom(DataInput in) throws IOException {
                    return HOME.readFrom(in, booleanSize, numericSize);
                }
                public void writeTo(FractionalColumn entity, DataOutput out) throws IOException {
                    HOME.writeTo(entity, out);
                }
            };
		}
        public ConcurrentTable<FractionalColumn> createTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            final int boolByteLen 		= (booleanSize - 1) / 8 + 1;
            final int numericByteLen	= numericSize * (8 + 16);//8 for 2 len values, 16 for two long values (numerator/denominator)
            return new ConcurrentTable<FractionalColumn>(
                VariableWidthTable.create(
                	folder, fileName, boolByteLen + numericByteLen, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.BigFractionMemoryTable.getCacheTableSize(),
                    Cache.BigFractionMemoryTable.getCacheEntrySize()                	
                )
            );
        }
        public ConcurrentTable<FractionalColumn> openTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            return new ConcurrentTable<FractionalColumn>(
                VariableWidthTable.open(
                	folder, fileName, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.BigFractionMemoryTable.getCacheTableSize(),
                    Cache.BigFractionMemoryTable.getCacheEntrySize()                	
                )
            );
        }
		public FluxDistribution createFluxDistribution(MetabolicNetwork net, BigFraction[] values) {
			return new FractionNumberFluxDistribution(net, values);
		}
		public ReadableMatrix<BigFraction> convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling) {
			if (matrix instanceof ReadableBigIntegerRationalMatrix) {
				return ((ReadableBigIntegerRationalMatrix)matrix).toBigIntegerRationalMatrix(false /*enforceNewInstance*/);
			}
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final DefaultBigIntegerRationalMatrix mx = new DefaultBigIntegerRationalMatrix(rows, cols);
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
                                        // cj: b
					// mx.setValueAt(row, col, BigFraction.valueOf(matrix.getNumberValueAt(row, col)));
					mx.setValueAt_BigFraction(row, col, BigFraction.valueOf(matrix.getNumberValueAt(row, col)));
                                        // cj: e
				}
			}
			return mx;
		}
		public ReadableMatrix<BigFraction> castMatrix(ReadableMatrix matrix) {
			if (matrix instanceof ReadableBigIntegerRationalMatrix) {
				return ((ReadableBigIntegerRationalMatrix)matrix).toBigIntegerRationalMatrix(false /*enforceNewInstance*/);
			}
			throw new ClassCastException("not a ReadableBigIntegerRationalMatrix: " + matrix.getClass().getName());
//			final int rows = matrix.getRowCount();
//			final int cols = matrix.getColumnCount();
//			DefaultBigIntegerRationalMatrix mx = new DefaultBigIntegerRationalMatrix(rows, cols);
//			for (int row = 0; row < rows; row++) {
//				for (int col = 0; col < cols; col++) {
//					mx.setValueAt(row, col, BigFraction.valueOf(matrix.getNumberValueAt(row, col)));
//				}
//			}
//			return mx;
		}
		public FractionalColumn castColumn(Column column) {
			return (FractionalColumn)column;
		}
		public BigFraction castNumber(Number number) {
			return (BigFraction)number;
		}
		public int rank(ReadableMatrix matrix, Zero zero) {
			return new Gauss(zero.mZeroPos).rank((ReadableBigIntegerRationalMatrix)matrix);
		}		
	};
    
}
