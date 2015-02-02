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

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.jbase.EntityMarshaller;
import ch.javasoft.jbase.VariableWidthTable;
import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.math.NumberOperations;
import ch.javasoft.math.varint.VarInt;
import ch.javasoft.math.varint.VarIntFactory;
import ch.javasoft.math.varint.VarIntNumber;
import ch.javasoft.math.varint.ops.VarIntOperations;
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
import ch.javasoft.smx.ops.Gauss;
import ch.javasoft.util.numeric.Zero;

/**
 * A <tt>VarIntColumn</tt> implements the numeric part of the column 
 * with integers of variable byte length. The values are stored as {@code byte}
 * array according to {@link VarInt#writeTo(java.io.OutputStream)} and
 * {@link VarIntFactory#readFrom(byte[], AtomicInteger)}.
 */
public class VarIntColumn extends AbstractColumn {
	
	private int				mBoolSize;
	private int				mNumeSize;;
	private final IBitSet	mBitSet;
	private byte[]			mNumericRaw; 

	public VarIntColumn(int boolSize) {
		mBoolSize	= boolSize;
		mNumeSize	= 0;
		mBitSet		= BitSetUtil.factory().create(boolSize);
		mNumericRaw	= new byte[0];
	}
	protected VarIntColumn(int boolSize, int numSize, IBitSet bitSet, byte[] numericRaw) {
		mBoolSize	= boolSize;
		mNumeSize	= numSize;
		mBitSet 	= bitSet;
		mNumericRaw	= numericRaw;
	}
	
	public IBitSet bitValues() {
		return mBitSet;
	}
	
	public <N extends Number> N getNumeric(ColumnHome<N, ?> columnHome, int row) {
		final VarIntNumber val = toVarIntNumber(mNumericRaw, row);
		return columnHome.castNumber(val);
	}
	
	private VarIntNumber[] toVarIntNumbers() {
		final AtomicInteger offsetPtr = new AtomicInteger();
		final VarIntNumber[] res = new VarIntNumber[mNumeSize];
		for (int i = 0; i < mNumeSize; i++) {
			res[i] = VarIntFactory.readFrom(mNumericRaw, offsetPtr);
		}
		return res;
	}
	private static VarIntNumber toVarIntNumber(byte[] raw, int row) {
		final AtomicInteger offsetPtr = getOffset(raw, row);
		return VarIntFactory.readFrom(raw, offsetPtr);
	}
	
	private static AtomicInteger getOffset(byte[] raw, int row) {
		final AtomicInteger offsetPtr = new AtomicInteger();
		while (row > 0) {
			VarIntFactory.readFrom(raw, offsetPtr);
			row--;
		}
		return offsetPtr;
	}
	
	private static int toSignum(byte[] raw, int row) {
		return toVarIntNumber(raw, row).signum();
	}
	
	private static byte[] fromVarIntNumber(VarIntNumber[] values) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < values.length; i++) {
			try {
				values[i].writeTo(out);
			} 
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return out.toByteArray();
	}

	public int booleanSize() {
		return mBoolSize;
	}
	public int numericSize() {
		return mNumeSize;
	}
	public int size() {
		return mBoolSize + mNumeSize;
	}
	
	public int getNumericSignum(Zero zero, int row) {
		return toSignum(mNumericRaw, row);
	}
	public int getHyperplaneSign(EfmModel model, IterationStateModel iteration) {
		//TODO optimize, conversion to integer neccessary?
		return getColumnInspectorModifier(model, VarIntNumber.class, VarIntNumber[].class).getHyperplaneSign(columnHome(), model, mBitSet, mBoolSize, toVarIntNumbers(), iteration);
	}
	public <Col extends Column> Col convert(ColumnHome<?,Col> columnHome, EfmModel model, IterationStepModel iteration, boolean clone) {
		//TODO optimize, conversion to integer neccessary?
		final VarIntNumber[] biValues = toVarIntNumbers();
		final ColumnInspectorModifier<VarIntNumber, VarIntNumber[]> modifier = getColumnInspectorModifier(model, VarIntNumber.class, VarIntNumber[].class);
		final IBitSet	newBin = modifier.convertBinary(columnHome(), model, mBitSet, mBoolSize, biValues, iteration, clone);
		final VarIntNumber[]		newNum = modifier.convertNumeric(columnHome(), model, mBitSet, mBoolSize, biValues, iteration, clone);

		if (clone) {
			return columnHome.castColumn(new VarIntColumn(iteration.getNextState().getBooleanSize(), newNum.length, newBin, fromVarIntNumber(newNum)));
		}
		else {
			mBoolSize = iteration.getNextState().getBooleanSize();
			if (mBitSet != newBin) {
				mBitSet.clear();
				mBitSet.or(newBin);
			}
			mNumeSize 	= newNum.length;
			mNumericRaw = fromVarIntNumber(newNum);
			return columnHome.castColumn(this);
		}
	}
	public <Col extends Column> Col mergeWith(ColumnHome<?,Col> columnHome, EfmModel model, Col other, IterationStepModel iteration) {
		return columnHome.castColumn(mergeWith(model, (VarIntColumn)other, iteration));
	}
	public VarIntColumn mergeWith(EfmModel model, VarIntColumn other, IterationStepModel iteration) {
		//TODO optimize, conversion to integer neccessary?
		final VarIntNumber[] myValues = toVarIntNumbers();
		final VarIntNumber[] otValues = other.toVarIntNumbers();
		
		final ColumnInspectorModifier<VarIntNumber, VarIntNumber[]> modifier = getColumnInspectorModifier(model, VarIntNumber.class, VarIntNumber[].class);
		final IBitSet newBin = modifier.mergeBinary(columnHome(), model, mBitSet, mBoolSize, myValues, other.mBitSet, other.mBoolSize, otValues, iteration);
		final VarIntNumber[] newNum = modifier.mergeNumeric(columnHome(), model, mBitSet, mBoolSize, myValues, other.mBitSet, other.mBoolSize, otValues, iteration);
		return new VarIntColumn(iteration.getNextState().getBooleanSize(), newNum.length, newBin, fromVarIntNumber(newNum));
	}

	public void writeTo(DataOutput dataOut) throws IOException {
		columnHome().writeTo(this, dataOut);
	}
	
	@Override
	public VarIntColumn clone() {
		return new VarIntColumn(mBoolSize, mNumeSize, mBitSet.clone(), mNumericRaw.clone());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof VarIntColumn) {
			final VarIntColumn col = (VarIntColumn)obj;
			return
				mBoolSize == col.mBoolSize &&
				mBitSet.equals(col.mBitSet) && 
				java.util.Arrays.equals(mNumericRaw, col.mNumericRaw);
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
		for (int ii = 0; ii < mNumeSize; ii++) {
			if (mBoolSize > 0 || ii > 0) sb.append(", ");
			sb.append(toVarIntNumber(mNumericRaw, ii));//not very efficient!
		}
		sb.append('}');
		return sb.toString();
	}
	
	public Home columnHome() {
		return HOME;
	}
	
	public static abstract class Home
		extends		AbstractHome<VarIntNumber, VarIntColumn> 
		implements	ColumnHome<VarIntNumber, VarIntColumn> {}
	
	public static final Home HOME = new Home() {
		public Arithmetic getArithmetic() {
			return Arithmetic.varint;
		}
		public NumberOperations<VarIntNumber> getNumberOperations() {
			return VarIntOperations.instance();
		}
		public VarIntColumn newInstance(int booleanSize, int numericSize) {
            throw new RuntimeException("not implemented");
			//return new FractionalColumn(booleanSize, numericSize);
		}
		public VarIntColumn[] newInstances(ReadableMatrix<VarIntNumber> matrix, int booleanSize) {
			final int rows = matrix.getRowCount();
			final int cols = matrix.getColumnCount();
			final VarIntColumn[] res = new VarIntColumn[cols];
			for (int col = 0; col < cols; col++) {
				final VarIntNumber[] vals = new VarIntNumber[rows];				
				for (int row = 0; row < rows; row++) {
                    vals[row] = matrix.getNumberValueAt(row, col); 
				}
                res[col] = new VarIntColumn(booleanSize, vals.length, BitSetUtil.factory().create(rows), fromVarIntNumber(vals));
			}
			return res;
		}
		public VarIntColumn readFrom(DataInput dataIn, int booleanSize, int numericSize) throws IOException {
			final IBitSet bitSet = readBinaryFrom(dataIn, booleanSize);
			//read numeric
			final int len = dataIn.readInt();
			final byte[] arr = new byte[len];
			dataIn.readFully(arr);
			return new VarIntColumn(booleanSize, numericSize, bitSet, arr);
		}
		public void writeTo(VarIntColumn column, DataOutput dataOut) throws IOException {
			writeBinaryTo(column, dataOut);
			dataOut.writeInt(column.mNumericRaw.length);
			dataOut.write(column.mNumericRaw);
		}
		public EntityMarshaller<VarIntColumn> getEntityMarshaller(final int booleanSize, final int numericSize) throws IOException {
			return new EntityMarshaller<VarIntColumn>() {
                public VarIntColumn readFrom(DataInput in) throws IOException {
                    return HOME.readFrom(in, booleanSize, numericSize);
                }
                public void writeTo(VarIntColumn entity, DataOutput out) throws IOException {
                    HOME.writeTo(entity, out);
                }
            };
		}
        public ConcurrentTable<VarIntColumn> createTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            final int boolByteLen 		= BitSetUtil.byteSize(booleanSize);
            final int numericByteLen	= 4 + numericSize * 8;//4 for array length, 8 for one long value
            return new ConcurrentTable<VarIntColumn>(
                VariableWidthTable.create(
                	folder, fileName, boolByteLen + numericByteLen, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.VarIntMemoryTable.getCacheTableSize(),
                    Cache.VarIntMemoryTable.getCacheEntrySize()
                )
            );
        }
        public ConcurrentTable<VarIntColumn> openTable(File folder, String fileName, final int booleanSize, final int numericSize) throws IOException {
            return new ConcurrentTable<VarIntColumn>(
                VariableWidthTable.open(
                	folder, fileName, 
                	getEntityMarshaller(booleanSize, numericSize),
                    Cache.VarIntMemoryTable.getCacheTableSize(),
                    Cache.VarIntMemoryTable.getCacheEntrySize()
                )
            );
        }
		public FluxDistribution createFluxDistribution(MetabolicNetwork net, VarIntNumber[] values) {
			final BigInteger[] bvalues = new BigInteger[values.length];
			for (int i = 0; i < bvalues.length; i++) {
				bvalues[i] = values[i].toBigInteger();
			}
			return new FractionNumberFluxDistribution(net, bvalues);
		}
		public ReadableMatrix<VarIntNumber> convertMatrix(ReadableMatrix matrix, boolean allowRowScaling, boolean allowColumnScaling) {
			if (matrix instanceof VarIntMatrix) {
				return (VarIntMatrix)matrix;
			}
			return new VarIntMatrix(BigIntegerColumn.HOME.convertMatrix(matrix, allowRowScaling, allowColumnScaling));
		}
		public ReadableMatrix<VarIntNumber> castMatrix(ReadableMatrix matrix) {
			if (matrix instanceof VarIntMatrix) {
				return (VarIntMatrix)matrix;
			}
			throw new ClassCastException("not a VarIntMatrix: " + matrix.getClass().getName());
//			final int rows = matrix.getRowCount();
//			final int cols = matrix.getColumnCount();
//			DefaultVarIntNumberRationalMatrix mx = new DefaultVarIntNumberRationalMatrix(rows, cols);
//			for (int row = 0; row < rows; row++) {
//				for (int col = 0; col < cols; col++) {
//					mx.setValueAt(row, col, VarIntNumber.valueOf(matrix.getNumberValueAt(row, col)));
//				}
//			}
//			return mx;
		}
		public VarIntColumn castColumn(Column column) {
			return (VarIntColumn)column;
		}
		public VarIntNumber castNumber(Number number) {
			return (VarIntNumber)number;
		}
		public int rank(ReadableMatrix matrix, Zero zero) {
			return new Gauss(zero.mZeroPos).rank((ReadableBigIntegerRationalMatrix)matrix);
		}		
	};
	
    
}
