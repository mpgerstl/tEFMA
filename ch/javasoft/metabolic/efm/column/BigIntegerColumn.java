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
import ch.javasoft.math.ops.BigIntegerOperations;
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
import ch.javasoft.smx.iface.BigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.numeric.BigIntegerUtil;
import ch.javasoft.util.numeric.Zero;

/**
 * A <tt>BigIntegerColumn</tt> implements the numeric part of the column with 
 * {@link BigInteger large integer numbers}.
 */
public class BigIntegerColumn extends AbstractColumn {
	
	private int				mBoolSize;
	private final IBitSet	mBitSet;
	private BigInteger[]	mValues; 

	public BigIntegerColumn(int boolSize) {
		mBoolSize	= boolSize;
		mBitSet		= BitSetUtil.factory().create(boolSize);
		mValues		= new BigInteger[0];
	}
	protected BigIntegerColumn(int boolSize, IBitSet bitSet, BigInteger[] values) {
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
		return getColumnInspectorModifier(model, BigInteger.class, BigInteger[].class).getHyperplaneSign(columnHome(), model, mBitSet, mBoolSize, mValues, iteration);
	}
	public <Col extends Column> Col convert(ColumnHome<?,Col> columnHome, EfmModel model, IterationStepModel iteration, boolean clone) {
		final ColumnInspectorModifier<BigInteger, BigInteger[]> modifier = getColumnInspectorModifier(model, BigInteger.class, BigInteger[].class);
		final IBitSet	newBin = modifier.convertBinary(columnHome(), model, mBitSet, mBoolSize, mValues, iteration, clone);
		final BigInteger[]		newNum = modifier.convertNumeric(columnHome(), model, mBitSet, mBoolSize, mValues, iteration, clone);

		if (clone) {
			return columnHome.castColumn(new BigIntegerColumn(iteration.getNextState().getBooleanSize(), newBin, newNum));
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
		return columnHome.castColumn(mergeWith(model, (BigIntegerColumn)other, iteration));
	}
	public BigIntegerColumn mergeWith(EfmModel model, BigIntegerColumn other, IterationStepModel iteration) {
		final ColumnInspectorModifier<BigInteger, BigInteger[]> modifier = getColumnInspectorModifier(model, BigInteger.class, BigInteger[].class);
		final IBitSet newBin = modifier.mergeBinary(columnHome(), model, mBitSet, mBoolSize, mValues, other.mBitSet, other.mBoolSize, other.mValues, iteration);
		final BigInteger[] newNum = modifier.mergeNumeric(columnHome(), model, mBitSet, mBoolSize, mValues, other.mBitSet, other.mBoolSize, other.mValues, iteration);
		return new BigIntegerColumn(iteration.getNextState().getBooleanSize(), newBin, newNum);
	}

	public void writeTo(DataOutput dataOut) throws IOException {
		columnHome().writeTo(this, dataOut);
	}
	
	@Override
	public BigIntegerColumn clone() {
		return new BigIntegerColumn(mBoolSize, mBitSet.clone(), mValues.clone());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof BigIntegerColumn) {
			final BigIntegerColumn col = (BigIntegerColumn)obj;
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
		extends		AbstractHome<BigInteger, BigIntegerColumn> 
		implements	ColumnHome<BigInteger, BigIntegerColumn> {
		
		//redefine with BigIntegerMatrix, needed for VarInt stuff
		abstract public BigIntegerMatrix convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling);
	}
	
	public static final Home HOME = new Home() {
		public Arithmetic getArithmetic() {
			return Arithmetic.bigint;
		}
		public NumberOperations<BigInteger> getNumberOperations() {
			return BigIntegerOperations.instance();
		}
		public BigIntegerColumn newInstance(int booleanSize, int numericSize) {
            throw new RuntimeException("not implemented");
			//return new FractionalColumn(booleanSize, numericSize);
		}
		public BigIntegerColumn[] newInstances(ReadableMatrix<BigInteger> matrix, int booleanSize) {
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final BigIntegerColumn[] res = new BigIntegerColumn[cols];
			for (int col = 0; col < cols; col++) {
				final BigInteger[] vals = new BigInteger[rows];				
				for (int row = 0; row < rows; row++) {
                    vals[row] = matrix.getNumberValueAt(row, col); 
				}
                res[col] = new BigIntegerColumn(booleanSize, BitSetUtil.factory().create(rows), vals);
			}
			return res;
		}
		public BigIntegerColumn readFrom(DataInput dataIn, int booleanSize, int numericSize) throws IOException {
			final IBitSet bitSet = readBinaryFrom(dataIn, booleanSize);
            final BigInteger[] values = new BigInteger[numericSize];  
			for (int i = 0; i < numericSize; i++) {
				int byteCnt;
				final byte[] numBytes;
				
				//read numeric
				byteCnt = dataIn.readInt();
                numBytes = new byte[byteCnt];
				for (int j = 0; j < numBytes.length; j++) {
                    numBytes[j] = dataIn.readByte();
				}
				
				final BigInteger num = new BigInteger(numBytes);
				//ensure reuse of instances for small integers 
				values[i] = numBytes.length <= 8 ? BigInteger.valueOf(num.longValue()) : num;
			}
			return new BigIntegerColumn(booleanSize, bitSet, values);
		}
		public void writeTo(BigIntegerColumn column, DataOutput dataOut) throws IOException {
			writeBinaryTo(column, dataOut);
			for (int i = 0; i < column.mValues.length; i++) {
				byte[] bytes;
				
				//write numeric (denominator is always one)
				bytes = column.mValues[i].toByteArray();
				dataOut.writeInt(bytes.length);
				for (int j = 0; j < bytes.length; j++) {
					dataOut.writeByte(bytes[j]);
				}
			}
		}
		public EntityMarshaller<BigIntegerColumn> getEntityMarshaller(final int booleanSize, final int numericSize) throws IOException {
			return new EntityMarshaller<BigIntegerColumn>() {
                public BigIntegerColumn readFrom(DataInput in) throws IOException {
                    return HOME.readFrom(in, booleanSize, numericSize);
                }
                public void writeTo(BigIntegerColumn entity, DataOutput out) throws IOException {
                    HOME.writeTo(entity, out);
                }
            };
		}
        public ConcurrentTable<BigIntegerColumn> createTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            final int boolByteLen 		= BitSetUtil.byteSize(booleanSize);
            final int numericByteLen	= numericSize * (4 + 8);//4 for len value, 8 for one long value
            return new ConcurrentTable<BigIntegerColumn>(
                VariableWidthTable.create(
                	folder, fileName, boolByteLen + numericByteLen, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.BigIntegerMemoryTable.getCacheTableSize(),
                    Cache.BigIntegerMemoryTable.getCacheEntrySize()
                )
            );
        }
        public ConcurrentTable<BigIntegerColumn> openTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            return new ConcurrentTable<BigIntegerColumn>(
                VariableWidthTable.open(
                	folder, fileName, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.BigIntegerMemoryTable.getCacheTableSize(),
                    Cache.BigIntegerMemoryTable.getCacheEntrySize()
                )
            );
        }
		public FluxDistribution createFluxDistribution(MetabolicNetwork net, BigInteger[] values) {
			return new FractionNumberFluxDistribution(net, values);
		}
		@Override
		public BigIntegerMatrix convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling) {
			if (matrix instanceof ReadableBigIntegerMatrix) {
				return ((ReadableBigIntegerMatrix)matrix).toBigIntegerMatrix(false /*enforceNewInstance*/);
			}
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final DefaultBigIntegerMatrix mx = new DefaultBigIntegerMatrix(rows, cols);
			if (allowRowScaling) {
				for (int row = 0; row < rows; row++) {
					BigInteger lcm = BigInteger.ONE;
					for (int col = 0; col < cols; col++) {
						final BigFraction val = BigFraction.valueOf(matrix.getNumberValueAt(row, col));
						lcm = BigIntegerUtil.lcm(lcm, val.getDenominator());
					}
					final BigFraction scale = BigFraction.valueOf(lcm.abs());
					for (int col = 0; col < cols; col++) {
						final BigFraction val = BigFraction.valueOf(matrix.getNumberValueAt(row, col));
						final BigFraction scaled = val.multiply(scale).reduce();
						if (!scaled.isInteger()) {
							throw new RuntimeException("internal error: could not scale to integer: " + scaled);
						}
						mx.setValueAt(row, col, scaled.toBigInteger());
					}
				}
			}
			else if (allowColumnScaling) {
				for (int col = 0; col < cols; col++) {
					BigInteger lcm = BigInteger.ONE;
					for (int row = 0; row < rows; row++) {
						final BigFraction val = BigFraction.valueOf(matrix.getNumberValueAt(row, col));
						lcm = BigIntegerUtil.lcm(lcm, val.getDenominator());
					}
					final BigFraction scale = BigFraction.valueOf(lcm.abs());
					for (int row = 0; row < rows; row++) {
						final BigFraction val = BigFraction.valueOf(matrix.getNumberValueAt(row, col));
						final BigFraction scaled = val.multiply(scale).reduce();
						if (!scaled.isInteger()) {
							throw new RuntimeException("internal error: could not scale to integer: " + scaled);
						}
						mx.setValueAt(row, col, scaled.toBigInteger());
					}
				}
			}
			else {
				throw new IllegalArgumentException("either allowRowScaling or allowColumnScaling must be true");
			}
			return mx;
		}
		public ReadableMatrix<BigInteger> castMatrix(ReadableMatrix matrix) {
			if (matrix instanceof ReadableBigIntegerMatrix) {
				return ((ReadableBigIntegerMatrix)matrix).toBigIntegerMatrix(false /*enforceNewInstance*/);
			}
			throw new ClassCastException("not a ReadableBigIntegerMatrix: " + matrix.getClass().getName());
//			final int rows = matrix.getRowCount();
//			final int cols = matrix.getColumnCount();
//			DefaultBigIntegerRationalMatrix mx = new DefaultBigIntegerRationalMatrix(rows, cols);
//			for (int row = 0; row < rows; row++) {
//				for (int col = 0; col < cols; col++) {
//					mx.setValueAt(row, col, BigInteger.valueOf(matrix.getNumberValueAt(row, col)));
//				}
//			}
//			return mx;
		}
		public BigIntegerColumn castColumn(Column column) {
			return (BigIntegerColumn)column;
		}
		public BigInteger castNumber(Number number) {
			return (BigInteger)number;
		}
		public int rank(ReadableMatrix matrix, Zero zero) {
			return new Gauss(zero.mZeroPos).rank((ReadableBigIntegerRationalMatrix)matrix);
		}		
	};
    
}
