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
import ch.javasoft.smx.iface.ReadableBigIntegerMatrix;
import ch.javasoft.smx.iface.ReadableBigIntegerRationalMatrix;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.smx.impl.DefaultBigIntegerMatrix;
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.Arrays;
import ch.javasoft.util.ByteArray;
import ch.javasoft.util.numeric.BigIntegerUtil;
import ch.javasoft.util.numeric.Zero;

/**
 * A <tt>RawBigIntegerColumn</tt> implements the numeric part of the column with
 * {@link BigInteger large integer numbers}, but stores them encoded as raw
 * bytes in a single byte array. 
 * <p>
 * The first byte of a value in the array indicates the number of bytes for the
 * next big integer. If it is zero, the value is zero. Otherwise, if the first 
 * bit is zero, the next 7 bits encode for the byte length directly. Otherwise, 
 * the first 4 bytes encode for the length, ignoring the negative sign bit in 
 * the resulting integer.    
 */
public class RawBigIntegerColumn extends AbstractColumn {
	
	private int				mBoolSize;
	private int				mNumericSize;
	private final IBitSet	mBitSet;
	private byte[]			mNumericBytes;

	public RawBigIntegerColumn(int boolSize) {
		mBoolSize		= boolSize;
		mBitSet			= BitSetUtil.factory().create(boolSize);
		mNumericBytes	= new byte[0];
	}
	protected RawBigIntegerColumn(int boolSize, int numericSize, IBitSet bitSet, byte[] numericBytes) {
		mBoolSize		= boolSize;
		mNumericSize	= numericSize;
		mBitSet 		= bitSet;
		mNumericBytes	= numericBytes;
	}
	
	public IBitSet bitValues() {
		return mBitSet;
	}
	
	public <N extends Number> N getNumeric(ColumnHome<N, ?> columnHome, int row) {
		return columnHome.castNumber(toBigInteger(mNumericBytes, row));
	}
    
	private static BigInteger toBigInteger(byte[] bytes, int row) {
		final int offset = getOffset(bytes, row);
		final byte first = bytes[offset];
		if (first == 0) { 
			return BigInteger.ZERO;
		}
		if (0 == (0x80 & first)) {
			return new BigInteger(Arrays.copyOfRange(bytes, offset+1, offset+1+first));
		}
		final int len = 0x7fffffff & ((first << 24) | (bytes[offset+1] << 16) | (bytes[offset+2] << 8) | bytes[offset+3]); 
		return new BigInteger(Arrays.copyOfRange(bytes, offset+4, offset+4+len));
	}
	private static int toSignum(byte[] bytes, int row) {
		final int offset = getOffset(bytes, row);
		final byte first = bytes[offset];
		final byte mostsig;
		if (first == 0) {
			return 0;
		}
		else if (0 == (0x80 & first)) {
			mostsig = bytes[offset+1];
		}
		else {
			mostsig = bytes[offset+4];			
		}
		return mostsig == 0 ? 0 : mostsig < 0 ? -1 : 1;
	}
	private static int getOffset(byte[] bytes, int row) {
		int offset = 0;
		while (row > 0) {
			final byte first = bytes[offset];
			if (first == 0) {
				offset++;
			}
			else if (0 == (0x80 & first)) {
				offset++;
				offset+=first;
			}
			else {
				final int len = 0x7fffffff & ((first << 24) | (bytes[offset+1] << 16) | (bytes[offset+2] << 8) | bytes[offset+3]);
				offset += len + 4;
			}
			row--;
		}
		return offset;
	}
	
	private BigInteger[] toBigIntegers() {
		final BigInteger[] res = new BigInteger[mNumericSize];
		int offset = 0;
		for (int i = 0; i < res.length; i++) {
			offset += toBigInteger(res, i, mNumericBytes, offset);
		}
		return res;
	}
	private static int toBigInteger(BigInteger[] result, int resultIndex, byte[] bytes, int offset) {
		final byte first = bytes[offset];
		if (first == 0) {
			result[resultIndex] = BigInteger.ZERO;
			return 1;
		}
		if (0 == (0x80 & first)) {
			result[resultIndex] = new BigInteger(Arrays.copyOfRange(bytes, offset+1, offset+1+first));
			return 1+first;
		}
		final int len = 0x7fffffff & ((first << 24) | (bytes[offset+1] << 16) | (bytes[offset+2] << 8) | bytes[offset+3]); 
		result[resultIndex] = new BigInteger(Arrays.copyOfRange(bytes, offset+4, offset+4+len));
		return 4+len;
	}
	private static byte[] fromBigIntegers(BigInteger[] values) {
		final ByteArray buf = new ByteArray();
		for (int i = 0; i < values.length; i++) {
			if (values[i].signum() == 0) {
				buf.add((byte)0);
			}
			else {
				final byte[] bytes = values[i].toByteArray();
				final int len = bytes.length;
				if (len <= 0x0000007f) {
					buf.add((byte)len);
				}
				else {
					buf.add((byte)(0x00000080 | (len >> 24)));
					buf.add((byte)(len >> 16));
					buf.add((byte)(len >> 8));
					buf.add((byte)len);
				}
				for (int j = 0; j < bytes.length; j++) {
					buf.add(bytes[j]);
				}
			}
		}
		return buf.toArray();
	}
	
	public int booleanSize() {
		return mBoolSize;
	}
	public int numericSize() {
		return mNumericSize;
	}
	public int size() {
		return mBoolSize + mNumericSize;
	}
	
	public int getNumericSignum(Zero zero, int row) {
		return toSignum(mNumericBytes, row);
	}
	public int getHyperplaneSign(EfmModel model, IterationStateModel iteration) {
		return getColumnInspectorModifier(model, BigInteger.class, BigInteger[].class).getHyperplaneSign(columnHome(), model, mBitSet, mBoolSize, toBigIntegers(), iteration);
	}
	public <Col extends Column> Col convert(ColumnHome<?,Col> columnHome, EfmModel model, IterationStepModel iteration, boolean clone) {
		final BigInteger[] bigInts = toBigIntegers();
		final ColumnInspectorModifier<BigInteger, BigInteger[]> modifier = getColumnInspectorModifier(model, BigInteger.class, BigInteger[].class);
		final IBitSet	newBin = modifier.convertBinary(columnHome(), model, mBitSet, mBoolSize, bigInts, iteration, clone);
		final BigInteger[]		newNum = modifier.convertNumeric(columnHome(), model, mBitSet, mBoolSize, bigInts, iteration, clone);

		if (clone) {
			return columnHome.castColumn(new RawBigIntegerColumn(iteration.getNextState().getBooleanSize(), newNum.length, newBin, fromBigIntegers(newNum)));
		}
		else {
			mBoolSize = iteration.getNextState().getBooleanSize();
			if (mBitSet != newBin) {
				mBitSet.clear();
				mBitSet.or(newBin);
			}
			mNumericSize 	= newNum.length;
			mNumericBytes	= fromBigIntegers(newNum);
			return columnHome.castColumn(this);
		}
	}
	public <Col extends Column> Col mergeWith(ColumnHome<?,Col> columnHome, EfmModel model, Col other, IterationStepModel iteration) {
		return columnHome.castColumn(mergeWith(model, (RawBigIntegerColumn)other, iteration));
	}
	public RawBigIntegerColumn mergeWith(EfmModel model, RawBigIntegerColumn other, IterationStepModel iteration) {
		final BigInteger[] myBigInts = toBigIntegers();
		final BigInteger[] otBigInts = other.toBigIntegers();

		final ColumnInspectorModifier<BigInteger, BigInteger[]> modifier = getColumnInspectorModifier(model, BigInteger.class, BigInteger[].class);
		final IBitSet newBin = modifier.mergeBinary(columnHome(), model, mBitSet, mBoolSize, myBigInts, other.mBitSet, other.mBoolSize, otBigInts, iteration);
		final BigInteger[] newNum = modifier.mergeNumeric(columnHome(), model, mBitSet, mBoolSize, myBigInts, other.mBitSet, other.mBoolSize, otBigInts, iteration);
		return new RawBigIntegerColumn(iteration.getNextState().getBooleanSize(), newNum.length, newBin, fromBigIntegers(newNum));
	}

	public void writeTo(DataOutput dataOut) throws IOException {
		columnHome().writeTo(this, dataOut);
	}
	
	@Override
	public RawBigIntegerColumn clone() {
		return new RawBigIntegerColumn(mBoolSize, mNumericSize, mBitSet.clone(), Arrays.copyOf(mNumericBytes, mNumericBytes.length));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof RawBigIntegerColumn) {
			final RawBigIntegerColumn col = (RawBigIntegerColumn)obj;
			return
				mBoolSize == col.mBoolSize &&
				mBitSet.equals(col.mBitSet) && 
				java.util.Arrays.equals(mNumericBytes, col.mNumericBytes);
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
		final BigInteger[] bigInts = toBigIntegers();
		for (int ii = 0; ii < bigInts.length; ii++) {
			if (mBoolSize > 0 || ii > 0) sb.append(", ");
			sb.append(bigInts[ii]);
		}
		sb.append('}');
		return sb.toString();
	}
    
	public Home columnHome() {
		return HOME;
	}
	
	public static abstract class Home
		extends		AbstractHome<BigInteger, RawBigIntegerColumn> 
		implements	ColumnHome<BigInteger, RawBigIntegerColumn> {}
	
	public static final Home HOME = new Home() {
		public Arithmetic getArithmetic() {
			return Arithmetic.rawint;
		}
		public NumberOperations<BigInteger> getNumberOperations() {
			return BigIntegerOperations.instance();
		}
		public RawBigIntegerColumn newInstance(int booleanSize, int numericSize) {
            throw new RuntimeException("not implemented");
			//return new FractionalColumn(booleanSize, numericSize);
		}
		public RawBigIntegerColumn[] newInstances(ReadableMatrix<BigInteger> matrix, int booleanSize) {
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final RawBigIntegerColumn[] res = new RawBigIntegerColumn[cols];
			for (int col = 0; col < cols; col++) {
				final BigInteger[] vals = new BigInteger[rows];				
				for (int row = 0; row < rows; row++) {
                    vals[row] = matrix.getNumberValueAt(row, col); 
				}
                res[col] = new RawBigIntegerColumn(booleanSize, vals.length, BitSetUtil.factory().create(rows), fromBigIntegers(vals));
			}
			return res;
		}
		public RawBigIntegerColumn readFrom(DataInput dataIn, int booleanSize, int numericSize) throws IOException {
			final IBitSet bitSet = readBinaryFrom(dataIn, booleanSize);
			
			//read numeric
			final int byteLen = dataIn.readInt();
			final byte[] bytes = new byte[byteLen];
			dataIn.readFully(bytes);
			
			return new RawBigIntegerColumn(booleanSize, numericSize, bitSet, bytes);
		}
		public void writeTo(RawBigIntegerColumn column, DataOutput dataOut) throws IOException {
			writeBinaryTo(column, dataOut);
			dataOut.writeInt(column.mNumericBytes.length);
			dataOut.write(column.mNumericBytes);
		}
		public EntityMarshaller<RawBigIntegerColumn> getEntityMarshaller(final int booleanSize, final int numericSize) throws IOException {
			return new EntityMarshaller<RawBigIntegerColumn>() {
                public RawBigIntegerColumn readFrom(DataInput in) throws IOException {
                    return HOME.readFrom(in, booleanSize, numericSize);
                }
                public void writeTo(RawBigIntegerColumn entity, DataOutput out) throws IOException {
                    HOME.writeTo(entity, out);
                }
            };
		}
        public ConcurrentTable<RawBigIntegerColumn> createTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            final int boolByteLen 		= BitSetUtil.byteSize(booleanSize);
            final int numericByteLen	= numericSize * (4 + 8);//4 for len value, 8 for one long value
            return new ConcurrentTable<RawBigIntegerColumn>(
                VariableWidthTable.create(
                	folder, fileName, boolByteLen + numericByteLen, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.BigIntegerMemoryTable.getCacheTableSize(),
                    Cache.BigIntegerMemoryTable.getCacheEntrySize()
                )
            );
        }
        public ConcurrentTable<RawBigIntegerColumn> openTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            return new ConcurrentTable<RawBigIntegerColumn>(
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
		public ReadableMatrix<BigInteger> convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling) {
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
		public RawBigIntegerColumn castColumn(Column column) {
			return (RawBigIntegerColumn)column;
		}
		public BigInteger castNumber(Number number) {
			return (BigInteger)number;
		}
		public int rank(ReadableMatrix matrix, Zero zero) {
			return new Gauss(zero.mZeroPos).rank((ReadableBigIntegerRationalMatrix)matrix);
		}		
	};
    
}
