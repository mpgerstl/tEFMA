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
package ch.javasoft.bitset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A <code>LongBitSet</code> stores bits using a byte array.
 */
public class ByteBitSet implements IBitSet {

	/**
	 * Default static factory for {@link ByteBitSet} instances.
	 */
    public static final ByteBitSetFactory FACTORY = new ByteBitSetFactory();
    
    /**
     * <code>ByteBitSetFactory</code> is the {@link BitSetFactory} for 
     * {@code ByteBitSet} instances.
     */
    public static final class ByteBitSetFactory implements BitSetFactory {
        public ByteBitSet create() {
            return new ByteBitSet();
        }
        public ByteBitSet create(int capacity) {
        	return new ByteBitSet(capacity);
        }
        public ByteBitSet create(IBitSet bits) {
            return new ByteBitSet(bits);
        }
        public ByteBitSet convert(IBitSet bitSet) {
        	return bitSet instanceof ByteBitSet ? (ByteBitSet)bitSet : new ByteBitSet(bitSet);
        }
        public ByteBitSet create(BitSet bits) {
            return new ByteBitSet(bits);
        }
        public Class<ByteBitSet> getBitSetClass() {
        	return ByteBitSet.class;
        }
    };

	private static final int BITS_PER_UNIT = Byte.SIZE;
	private byte[] mUnits;

    /**
     * Creates an empty bit set with initial capacity for 64 bits. All bits are 
     * initially false.
     */
	public ByteBitSet() {
		this(BITS_PER_UNIT);
	}
    /**
     * Creates an empty bit set with the specified initial capacity. All bits 
     * are initially false.
     */
	public ByteBitSet(int bitCapacity) {
		mUnits = new byte[1 + (bitCapacity - 1) / BITS_PER_UNIT];
	}
    public ByteBitSet(BitSet bitSet) {
        this(bitSet.length());
        for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
            set(bit);
        }
    }
    /**
     * Constructor using the specified bits to initialize this bit set. The bits
     * are copied, meaning that changes to the given bit set are <b>not</b> 
     * reflected in {@code this} bit set.
     * 
     * @param bitSet	the bit set used which's bits are used to initialize 
     * 					this bit set
     */
	public ByteBitSet(IBitSet bitSet) {
		this(bitSet.length());
		for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
			set(bit);
		}
	}
    /**
     * Constructor using the specified string to initialize this bit set. One
     * characters in the string are interpreted as {@code true} bits, meaning 
     * that a '1' in the string at position {@code i} causes that bit {@code i}
     * is set to {@code true}. All other characters are interpreted as false.
     * 
     * @param bitString	the string used which's '1' characters are used to set 
     * 					the one bits of this bit set
     */
	public ByteBitSet(String bitString) {
		this(bitString.length());
		for (int ii = 0; ii < bitString.length(); ii++) {
			if (bitString.charAt(ii) == '1') set(ii);
		}
	}
	
	/**
	 * Constructor for <code>ByteBitSet</code> with units. The units array is
	 * cloned if {@code cloneArray} is true.
	 * 
	 * @param units			the int array with the bits
	 * @param cloneArray	true if {@code units} should be cloned
	 */
	public ByteBitSet(byte[] units, boolean cloneArray) {
		mUnits = cloneArray ? Arrays.copyOf(units, units.length) : units;
	}
	
	private void ensureCapacity(int unitLen) {
		if (mUnits.length < unitLen) {
			final byte[] newUnits = new byte[unitLen];
			System.arraycopy(mUnits, 0, newUnits, 0, mUnits.length);
			mUnits = newUnits;
		}
	}

	public void set(int bit, boolean value) {
		if (value) set(bit); 
		else clear(bit);
	}
	public void set(int bit) {
		int unit	= bit / BITS_PER_UNIT;
		int index	= bit % BITS_PER_UNIT;
		int mask	= 1 << index;
		ensureCapacity(unit + 1);
		mUnits[unit] |= mask;
	}

	public void clear(int bit) {
		int unit	= bit / BITS_PER_UNIT;
		int index	= bit % BITS_PER_UNIT;
		int mask	= 1 << index;
		mUnits[unit] &= ~mask;
	}
    
    public void clear() {
        for (int ii = 0; ii < mUnits.length; ii++) {
            mUnits[ii] = 0;
        }
    }
    
    public void flip(int bit) {
		int unit	= bit / BITS_PER_UNIT;
		int index	= bit % BITS_PER_UNIT;
		int mask	= 1 << index;
		ensureCapacity(unit + 1);
		mUnits[unit] ^= (mUnits[unit] & mask);
    }
	
	public boolean get(int bit) {
		int unit	= bit / BITS_PER_UNIT;
		if (unit >= mUnits.length) return false;
		int index	= bit % BITS_PER_UNIT;
		int mask	= 1 << index;
		return 0 != (mUnits[unit] & mask);
	}
	
	public boolean isSubSetOf(IBitSet of) {
		return isSubSetOf(of instanceof ByteBitSet ? (ByteBitSet)of : new ByteBitSet(of));
	}
	public boolean isSubSetOf(ByteBitSet of) {
    	if (this == of) return true;

    	int min = Math.min(mUnits.length, of.mUnits.length);
		for (int ii = 0; ii < min; ii++) {
			//classical
			int and = mUnits[ii] & of.mUnits[ii];
			if (and != mUnits[ii]) return false;
			
			//this -> of == not this OR of
//			if (-1 != (~mUnits[ii] | of.mUnits[ii])) return false;
			
			//not (this -> of) == this AND not of
//			if (0 != (mUnits[ii] & ~of.mUnits[ii])) return false;
		}		
		for (int i = min; i < mUnits.length; i++) {
			if (mUnits[i] != 0) return false;
		}
		return true;
	}
	
	public boolean isSuperSetOfIntersection(IBitSet interA, IBitSet interB) {
		final ByteBitSet byteA = interA instanceof ByteBitSet ? (ByteBitSet)interA : new ByteBitSet(interA);
		final ByteBitSet byteB = interB instanceof ByteBitSet ? (ByteBitSet)interB : new ByteBitSet(interB);
    	return isSuperSetOfIntersection(byteA, byteB);
	}
	public boolean isSuperSetOfIntersection(ByteBitSet interA, ByteBitSet interB) {
		if (this == interA || this == interB) return true;

		int minInter = Math.min(interA.mUnits.length, interB.mUnits.length);
		int minAll = Math.min(mUnits.length, minInter);
		for (int ii = 0; ii < minAll; ii++) {
			long inter = interA.mUnits[ii] & interB.mUnits[ii];
			if (inter != (inter & mUnits[ii])) return false;
		}		
		for (int i = minAll; i < minInter; i++) {
			if (0L != (interA.mUnits[i] & interB.mUnits[i])) return false;
		}
		return true;
	}

	public void and(IBitSet with) {
    	and(with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
    }
	public void and(ByteBitSet with) {
    	if (this == with) return;

    	int len	= Math.min(mUnits.length, with.mUnits.length);
		for (int ii = 0; ii < len; ii++) {
			mUnits[ii] &= with.mUnits[ii];
		}
		for (int ii = len; ii < mUnits.length; ii++) {
			mUnits[ii] = 0;			
		}
	}
	public ByteBitSet getAnd(IBitSet with) {
    	return getAnd(this, with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
	}
	public ByteBitSet getAnd(ByteBitSet with) {
    	return getAnd(this, with);
	}
	public int getAndCardinality(IBitSet with) {
		return getAndCardinality(this, with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
	}
	public int getAndCardinality(ByteBitSet with) {
		return getAndCardinality(this, with);
	}
	
    public void or(IBitSet with) {
    	or(with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
    }
    public void or(ByteBitSet with) {
    	if (this == with) return;
    	
    	if (with.mUnits.length > mUnits.length) {
    		final byte[] newUnits = new byte[with.mUnits.length];
    		for (int i = 0; i < mUnits.length; i++) {
				newUnits[i] = (byte)(mUnits[i] | with.mUnits[i]);
			}
    		for (int i = mUnits.length; i < with.mUnits.length; i++) {
				newUnits[i] = with.mUnits[i];
			}
    		mUnits = newUnits;
    	}
    	else {
    		int len	= Math.min(mUnits.length, with.mUnits.length);
    		for (int i = 0; i < len; i++) {
				mUnits[i] |= with.mUnits[i];
			}
    	}
    }
    
	public ByteBitSet getOr(IBitSet with) {
    	return getOr(this, with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
	}
	public ByteBitSet getOr(ByteBitSet with) {
    	return getOr(this, with);
	}

	public void xor(IBitSet with) {
    	xor(with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
    }
    public void xor(ByteBitSet with) {
    	int newlen = Math.max(mUnits.length, with.mUnits.length);
    	if (mUnits.length == with.mUnits.length) {
    		while (newlen > 0 && 0 == (mUnits[newlen-1] ^ with.mUnits[newlen-1])) {
    			newlen--;
    		}
    	}
    	if (newlen != mUnits.length) {
    		final byte[] newUnits = new byte[newlen];
    		System.arraycopy(mUnits, 0, newUnits, 0, Math.min(newlen, mUnits.length));
    		mUnits = newUnits;
    	}
    	final int len = Math.min(with.mUnits.length, newlen);
		for (int i = 0; i < len; i++) {
			mUnits[i] ^= with.mUnits[i];
		}
    }

	public ByteBitSet getXor(IBitSet with) {
    	return getXor(this, with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
	}
	public ByteBitSet getXor(ByteBitSet with) {
    	return getXor(this, with);
	}
	public int getXorCardinality(IBitSet with) {
		return getXorCardinality(this, with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
	}
	public int getXorCardinality(ByteBitSet with) {
		return getXorCardinality(this, with);
	}
	
    
    public void andNot(IBitSet with) {
    	andNot(with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
    }
    public void andNot(ByteBitSet with) {
    	//with is always true in the large parts, and thus always larger
    	//thus, this is always directing the new length
    	final int len = Math.min(mUnits.length, with.mUnits.length);
    	for (int i = 0; i < len; i++) {
    		mUnits[i] &= ~with.mUnits[i];
		}
	}

	public ByteBitSet getAndNot(IBitSet with) {
    	return getAndNot(this, with instanceof ByteBitSet ? (ByteBitSet)with : new ByteBitSet(with));
	}
	public ByteBitSet getAndNot(ByteBitSet with) {
    	return getAndNot(this, with);
	}

	public static ByteBitSet getXor(ByteBitSet setA, ByteBitSet setB) {
    	final ByteBitSet res;
    	if (setA.mUnits.length > setB.mUnits.length) {
    		res = setA.clone();
    		res.xor(setB);
    	}
    	else {
    		res = setB.clone();
    		res.xor(setA);
    	}
    	return res;
    }
    /**
     * Return the number of one bits in the xor of the two sets, that is, the
     * number of bits which differ in the two sets
     */
    public static int getXorCardinality(ByteBitSet setA, ByteBitSet setB) {
    	int card = 0;
    	final int minLen = Math.min(setA.mUnits.length, setB.mUnits.length);
    	for (int i = 0; i < minLen; i++) {
			card += Integer.bitCount(setA.mUnits[i] ^ setB.mUnits[i]);
		}
    	for (int i = minLen; i < setA.mUnits.length; i++) {
			card += Integer.bitCount(setA.mUnits[i]);			
		}
    	for (int i = minLen; i < setB.mUnits.length; i++) {
			card += Integer.bitCount(setB.mUnits[i]);			
		}
    	return card;
    }
    public static ByteBitSet getOr(ByteBitSet setA, ByteBitSet setB) {
    	ByteBitSet larger, smaller;
    	if (setA.mUnits.length >= setB.mUnits.length) {
    		larger	= setA;
    		smaller	= setB;
    	}
    	else {
    		larger	= setB;
    		smaller = setA;
    	}
    	final byte[] units = new byte[larger.mUnits.length];
    	for (int i = 0; i < smaller.mUnits.length; i++) {
			units[i] = (byte)(smaller.mUnits[i] | larger.mUnits[i]);
		}
    	return new ByteBitSet(units, false);
    }

    public static ByteBitSet getAnd(ByteBitSet setA, ByteBitSet setB) {
    	ByteBitSet larger, smaller;
    	if (setA.mUnits.length >= setB.mUnits.length) {
    		larger	= setA;
    		smaller	= setB;
    	}
    	else {
    		larger	= setB;
    		smaller = setA;
    	}
    	final byte[] units = new byte[smaller.mUnits.length];
    	for (int i = 0; i < smaller.mUnits.length; i++) {
			units[i] = (byte)(smaller.mUnits[i] & larger.mUnits[i]);
		}
    	return new ByteBitSet(units, false);
	}
    /**
     * Return the number of one bits in the and of the two sets, that is, the
     * number of bits which are common in the two sets
     */
    public static int getAndCardinality(ByteBitSet setA, ByteBitSet setB) {
    	int card = 0;
    	final int minLen = Math.min(setA.mUnits.length, setB.mUnits.length);
    	for (int i = 0; i < minLen; i++) {
			card += Integer.bitCount(setA.mUnits[i] & setB.mUnits[i]);
    	}
    	return card;
    }
    /**
     * Returns setA and not setB
     */
    public static ByteBitSet getAndNot(ByteBitSet setA, ByteBitSet setB) {
    	//set b is always true in the large parts, and thus always larger
    	//thus, set a is always directing the new length
    	final byte[] units = new byte[setA.mUnits.length];
    	for (int i = 0; i < setB.mUnits.length; i++) {
			units[i] = (byte)(setA.mUnits[i] & ~setA.mUnits[i]);
		}
    	for (int i = setB.mUnits.length; i < setA.mUnits.length; i++) {
			units[i] = setA.mUnits[i];
		}
    	return new ByteBitSet(units, false);
	}
	
    public static ByteBitSet getAnd(ByteBitSet... bitSets) {
        // handle special cases 0 - 2 sets
        if (bitSets.length == 0) return new ByteBitSet();
        else if (bitSets.length == 1) return bitSets[0].clone();
        else if (bitSets.length == 2) return getAnd(bitSets[0], bitSets[1]);
        int smallest = 0;
        for (int i = 1; i < bitSets.length; i++) {
			if (bitSets[i].length() < bitSets[smallest].length()) {
				smallest = i;
			}
		}
        final ByteBitSet result = bitSets[smallest].clone();
        for (int i = 0; i < bitSets.length; i++) {
			if (i != smallest) result.and(bitSets[i]);
		}
        return result;
    }

    public int compareTo(IBitSet o) {
    	return compareTo(o instanceof ByteBitSet ? (ByteBitSet)o : new ByteBitSet(o));
    }
    public int compareTo(ByteBitSet o) {
    	final int min = Math.min(mUnits.length, o.mUnits.length);
    	for (int i = 0; i < min; i++) {
			//make unsigned comparison
			final int cmp = Integer.reverse(0x000000ff & mUnits[i]) - Integer.reverse(0x000000ff & o.mUnits[i]);
			if (cmp < 0) return -1;
			if (cmp > 0) return 1;
		}
    	for (int i = min; i < mUnits.length; i++) {
    		if (mUnits[i] != 0) return 1;
    	}
    	for (int i = min; i < o.mUnits.length; i++) {
    		if (mUnits[i] != 0) return -1;
    	}
    	return 0;
    }

    protected int unitLength() {
    	int index = mUnits.length;
    	do index--;
    	while (index >= 0 && mUnits[index] == 0);
    	return index + 1;
	}
    public int length() {
    	int index = mUnits.length;
    	do index--;
    	while (index >= 0 && mUnits[index] == 0);
    	return index < 0 ? 0 : index * BITS_PER_UNIT + Integer.SIZE - Integer.numberOfLeadingZeros(mUnits[index]);
	}

    public boolean isEmpty() {
    	int index = mUnits.length;
    	do index--;
    	while (index >= 0 && mUnits[index] == 0);
    	return index < 0;
	}
	
	@Override
	public ByteBitSet clone() {
		return new ByteBitSet(mUnits.clone(), false);
	}
	
	public int cardinality() {
		int card = 0;
		for (int ii = 0; ii < mUnits.length; ii++) {
			card += Integer.bitCount(mUnits[ii]);
		}
		return card;
	}
	
    public int cardinality(int fromBit, int toBit) {
        int fromUnit    = fromBit / BITS_PER_UNIT;
        int toUnit      = (toBit + BITS_PER_UNIT - 1) / BITS_PER_UNIT;
        int unitStart   = Math.max(0, fromUnit);
        int unitLen     = Math.min(mUnits.length, toUnit);
        int card = 0;
        for (int ii = unitStart; ii < unitLen; ii++) {
            if (mUnits[ii] != 0) {
                byte unit = mUnits[ii];
                if (ii == fromUnit) {
                    int bit = fromBit % BITS_PER_UNIT;
                    unit &= (0xff >>> bit);
                }
                if (ii == toUnit - 1) {
                    int bit = fromBit % BITS_PER_UNIT;
                    unit &= (0xff << (BITS_PER_UNIT - bit));
                }
                card += Integer.bitCount(unit);
            }
        }
        return card;
    }
	
    @Override
    public int hashCode() {
        int code = 0;
        for (int ii = 0; ii < mUnits.length; ii++) {
            code ^= (mUnits[ii] << ((ii & 0x03) << 3));
        }
        return code;
    }
    
    public int hashCodeObj() {
    	return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof ByteBitSet) {
            ByteBitSet bitSet = (ByteBitSet)obj;
            int len = Math.min(mUnits.length, bitSet.mUnits.length);
            for (int ii = 0; ii < len; ii++) {
                if (mUnits[ii] != bitSet.mUnits[ii]) return false;
            }
            for (int ii = len; ii < mUnits.length; ii++) {
				if (mUnits[ii] != 0) return false;
			}
            for (int ii = len; ii < bitSet.mUnits.length; ii++) {
				if (bitSet.mUnits[ii] != 0) return false;
			}
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append('{');
    	int unitLen = unitLength();
        for (int ii = 0; ii < unitLen; ii++) {
        	int len = ii == unitLen - 1 ? 1 + (length() - 1) % BITS_PER_UNIT : BITS_PER_UNIT;
            if (mUnits[ii] != 0) {
            	int bit = 1;
            	for (int jj = 0; jj < len; jj++, bit <<= 1) {
					if ((mUnits[ii] & bit) != 0) sb.append('1');
					else sb.append('0');
				}
            }
            else {
            	sb.append("00000000", 0, len);
            }
        }
    	sb.append('}');
    	return sb.toString();
    }
    
    public int nextSetBit(int from) {
        int fromBit     = from % BITS_PER_UNIT;
        int fromUnit    = from / BITS_PER_UNIT;
        for (int ii = Math.max(0, fromUnit); ii < mUnits.length; ii++) {
            if (mUnits[ii] != 0) {
                int unit = mUnits[ii];
                if (ii == fromUnit) unit &= (0xffffffffffffffffL << fromBit);
                if (unit != 0) return ii * BITS_PER_UNIT + Integer.numberOfTrailingZeros(unit);
            }
        }
        return -1;        
    }
    
    public int nextClearBit(int from) {
        int fromBit     = from % BITS_PER_UNIT;
        int fromUnit    = from / BITS_PER_UNIT;
        for (int ii = fromUnit; ii < mUnits.length; ii++) {
            if (mUnits[ii] != -1) {
                int unit = ~mUnits[ii];
                if (ii == fromUnit) unit &= (0xffffffffffffffffL << fromBit);
                if (unit != 0) return ii * BITS_PER_UNIT + Integer.numberOfTrailingZeros(unit);
            }
        }
        return Math.max(from, length());        
    }
    
    public BitSet toBitSet() {
        BitSet bitSet = new BitSet(length());
        for (int bit = nextSetBit(0); bit >= 0; bit = nextSetBit(bit + 1)) {
            bitSet.set(bit);
        }
        return bitSet;
    }
    
    public void writeTo(OutputStream out) throws IOException {
        final DataOutput dout = out instanceof DataOutput ?
                (DataOutput)out : new DataOutputStream(out);
        dout.writeByte(mUnits.length);
        for (int i = 0; i < mUnits.length; i++) {
            dout.writeByte(mUnits[i]);
        }
    }
    
    public static ByteBitSet readFrom(InputStream in) throws IOException {
        final DataInput din = in instanceof DataInput ? 
                (DataInput)in : new DataInputStream(in);
        final int len = din.readByte();
        final byte[] units = new byte[len];
        for (int i = 0; i < len; i++) {
            units[i] = din.readByte();
        }
        return new ByteBitSet(units, false /*clone*/);
    }
    
    public byte[] compress() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            compress(out);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return out.toByteArray();
    }
    
    public void compress(OutputStream out) throws IOException {
        ZipOutputStream zOut    = new ZipOutputStream(out);
        //zOut.setLevel(Deflater.BEST_SPEED);
        zOut.putNextEntry(new ZipEntry("A"));
        DataOutputStream dOut   = new DataOutputStream(zOut);
        dOut.writeByte(mUnits.length);
        for (int ii = 0; ii < mUnits.length; ii++) {
            dOut.writeByte(mUnits[ii]);
        }        
        dOut.flush();
        zOut.closeEntry();
        zOut.flush();
    }
    
    public static ByteBitSet uncompress(byte[] bytes) {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        try {
            return uncompress(byteIn);            
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    public static ByteBitSet uncompress(InputStream in) throws IOException {
        ZipInputStream zIn  = new ZipInputStream(in);
        zIn.getNextEntry();
        DataInputStream dIn = new DataInputStream(zIn);
        final int unitLen    = dIn.readByte();
        final byte[] units   = new byte[unitLen];
        for (int ii = 0; ii < unitLen; ii++) {
            units[ii] = dIn.readByte();
        }
        return new ByteBitSet(units, false);
    }
    
    public BitSetFactory factory() {
    	return FACTORY;
    }
    
    public int[] toByteArray() {
        return toByteArray(null, 0);
    }
    
    public int[] toByteArray(int[] arr, int offset) {
    	int len = unitLength();
    	if (arr == null || arr.length < len + offset) {
            arr = new int[len + offset];
        }
        System.arraycopy(mUnits, 0, arr, offset, len);   
        return arr;
    }

}
