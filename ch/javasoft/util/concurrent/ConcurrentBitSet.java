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
package ch.javasoft.util.concurrent;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An <code>ConcurrentBitSet</code> is pretty much the same as a {@link BitSet}, 
 * but operations on the set are performed in a thread-safe manner.
 */
public class ConcurrentBitSet {
	
	private static final int BITS_PER_UNIT = 64;
	
	private final ReadWriteLock			lock = new ReentrantReadWriteLock();
	private volatile AtomicLongArray 	units;

	public ConcurrentBitSet() {
		this(BITS_PER_UNIT);
	}
	public ConcurrentBitSet(int bitCapacity) {
		units = new AtomicLongArray(1 + (bitCapacity - 1) / BITS_PER_UNIT);
	}
    public ConcurrentBitSet(BitSet bitSet) {
        this(bitSet.length());
        for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
            set(bit);
        }
    }
	public ConcurrentBitSet(String bitString) {
		this(bitString.length());
		for (int ii = 0; ii < bitString.length(); ii++) {
			if (bitString.charAt(ii) == '1') set(ii);
		}
	}
	private ConcurrentBitSet(AtomicLongArray array) {
		units = array;
	}
	
	//CONDITION: no lock held
	private void ensureCapacityAndAquireReadLock(int unitLen) {
		while (true) {
			lock.readLock().lock();
			if (units.length() >= unitLen) {
				return;//yes, we keep the read lock
			}
			lock.readLock().unlock();
			lock.writeLock().lock();
			try {
				if (units.length() <= unitLen) {
					final AtomicLongArray newUnits = new AtomicLongArray(unitLen);
					for (int i = 0; i < units.length(); i++) {
						newUnits.set(i, units.get(i));
					}
					units = newUnits;
				}
			}
			finally {
				lock.writeLock().unlock();
			}
		}
	}

	public void set(int bit, boolean value) {
		if (value) set(bit); 
		else clear(bit);
	}
	public void set(int bit) {
		final int unit	= bit / BITS_PER_UNIT;
		final int index	= bit % BITS_PER_UNIT;
		final long mask	= 1L << index;

		ensureCapacityAndAquireReadLock(unit + 1);
		try {
			long old = units.get(unit);
			long upd = old | mask;
			while (!units.compareAndSet(unit, old, upd)) {
				old = units.get(unit);
				upd = old | mask;
			}
		}
		finally {
			lock.readLock().unlock();
		}
	}
    /**
     * Atomically sets the value to the given updated value
     * if the current value {@code ==} the expected value.
     *
     * @param bit the bit index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
	public boolean compareAndSet(int bit, boolean expect, boolean update) {
		final int unit	= bit / BITS_PER_UNIT;
		final int index	= bit % BITS_PER_UNIT;
		final long mask	= 1L << index;

		if (!expect && update) {
			ensureCapacityAndAquireReadLock(unit + 1);
		}
		else {
			lock.readLock().lock();
		}
		try {
			if (unit >= units.length()) {
				if (expect) return false;
				if (!update) return true;
				//should not get here
				throw new RuntimeException("internal error: should have sufficient unit length");
			}
			long old = units.get(unit);
			boolean cur = 0 != (old & mask);
			while (cur == expect && !units.compareAndSet(unit, old, old | mask)) {
				old = units.get(unit);
				cur = 0 != (old & mask);
			}
			return cur == expect;
		}
		finally {
			lock.readLock().unlock();
		}
	}

	public void clear(int bit) {
		int unit	= bit / BITS_PER_UNIT;
		int index	= bit % BITS_PER_UNIT;
		long mask	= 1L << index;
		
		lock.readLock().lock();
		try {
			long old = units.get(unit);
			long upd = old | mask;
			while (!units.compareAndSet(unit, old, upd)) {
				old = units.get(unit);
				upd = old & ~mask;
			}
		}
		finally {
			lock.readLock().unlock();
		}
	}
    
    public void clear() {
		lock.readLock().lock();
		try {
	        for (int i = 0; i < units.length(); i++) {
	            units.set(i, 0);
	        }
		}
		finally {
			lock.readLock().unlock();
		}
    }
    
    public void flip(int bit) {
		final int unit	= bit / BITS_PER_UNIT;
		final int index	= bit % BITS_PER_UNIT;
		final long mask	= 1L << index;

		ensureCapacityAndAquireReadLock(unit + 1);
		try {
			long old = units.get(unit);
			long upd = old ^ (old & mask);
			while (!units.compareAndSet(unit, old, upd)) {
				old = units.get(unit);
				upd = old ^ (old & mask);
			}
		}
		finally {
			lock.readLock().unlock();
		}
    }
	
	public boolean get(int bit) {
		final int unit	= bit / BITS_PER_UNIT;
		final int index	= bit % BITS_PER_UNIT;
		final long mask	= 1L << index;
		
		lock.readLock().lock();
		try {
			if (unit >= units.length()) return false;
			return 0 != (units.get(unit) & mask);
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	public boolean isSubSetOf(ConcurrentBitSet of) {
		if (this == of) return true;
		
		while (true) {
			lock.readLock().lock();
			try {
				if (of.lock.readLock().tryLock()) {
					final int min;
					try {
						min = Math.min(units.length(), of.units.length());
						for (int ii = 0; ii < min; ii++) {
							//classical
							final long myunit = units.get(ii); 
							final long and = myunit & of.units.get(ii);
							if (and != myunit) return false;
							
							//this -> of == not this OR of
				//			if (-1L != (~mUnits[ii] | of.mUnits[ii])) return false;
							
							//not (this -> of) == this AND not of
				//			if (0L != (mUnits[ii] & ~of.mUnits[ii])) return false;
						}		
					}
					finally {
						of.lock.readLock().unlock();
					}
					for (int i = min; i < units.length(); i++) {
						if (units.get(i) != 0L) return false;
					}
					return true;
				}
			}
			finally {
				lock.readLock().unlock();
			}
		}
	}
	
	public void and(ConcurrentBitSet with) {
		if (this == with) return;
		
		while (true) {
			lock.readLock().lock();
			try {
				if (with.lock.readLock().tryLock()) {
					final int len;
					try {
						len = Math.min(units.length(), with.units.length());
						for (int ii = 0; ii < len; ii++) {
							long old = units.get(ii);
							long upd = old & with.units.get(ii);
							while (!units.compareAndSet(ii, old, upd)) {
								old = units.get(ii);
								upd = old & with.units.get(ii);
							}
						}
					}
					finally {
						with.lock.readLock().unlock();
					}
					for (int ii = len; ii < units.length(); ii++) {
						units.set(ii, 0);
					}
					return;
				}
			}
			finally {
				lock.readLock().unlock();
			}
		}
	}
	
    public void or(ConcurrentBitSet with) {
		if (this == with) return;
		
		while (true) {
			final int withLength;
			with.lock.readLock().lock();
			try {
				withLength = with.units.length();
			}
			finally {
				with.lock.readLock().lock();
			}
			//(i) see comment below
			ensureCapacityAndAquireReadLock(withLength);
			try {
				if (with.lock.readLock().tryLock()) {
					try {
						final int len = with.units.length();
			    		if (units.length() >= len) {//else could happen if changed at (i)
				    		for (int i = 0; i < len; i++) {
								long old = units.get(i);
								long upd = old | with.units.get(i);
								while (!units.compareAndSet(i, old, upd)) {
									old = units.get(i);
									upd = old | with.units.get(i);
								}
					    	}
				    		return;
						}
					}
					finally {
						with.lock.readLock().unlock();
					}
				}
			}
			finally {
				lock.readLock().unlock();
			}
		}
    }
    
    /**
     * This set becomes this ^ with
     */
    public void xor(ConcurrentBitSet with) {
		while (true) {
			final int withLength;
			with.lock.readLock().lock();
			try {
				withLength = with.units.length();
			}
			finally {
				with.lock.readLock().lock();
			}
			//(i) see comment below
			ensureCapacityAndAquireReadLock(withLength);
			try {
				if (with.lock.readLock().tryLock()) {
					try {
						final int len = with.units.length();
			    		if (units.length() >= len) {//else could happen if changed at (i)
				    		for (int i = 0; i < len; i++) {
								long old = units.get(i);
								long upd = old ^ with.units.get(i);
								while (!units.compareAndSet(i, old, upd)) {
									old = units.get(i);
									upd = old ^ with.units.get(i);
								}
					    	}
				    		return;
						}
					}
					finally {
						with.lock.readLock().unlock();
					}
				}
			}
			finally {
				lock.readLock().unlock();
			}
		}
    }
    
    /**
     * This set becomes this & not with
     */
    public void andNot(ConcurrentBitSet with) {
    	//with is always true in the large parts, and thus always larger
    	//thus, this is always directing the new length
		while (true) {
			lock.readLock().lock();
			try {
				if (with.lock.readLock().tryLock()) {
					try {
						final int len = Math.min(units.length(), with.units.length());
						for (int ii = 0; ii < len; ii++) {
							long old = units.get(ii);
							long upd = old & ~with.units.get(ii);
							while (!units.compareAndSet(ii, old, upd)) {
								old = units.get(ii);
								upd = old & ~with.units.get(ii);
							}
						}
						return;
					}
					finally {
						with.lock.readLock().unlock();
					}
				}
			}
			finally {
				lock.readLock().unlock();
			}
		}
	}

    public static ConcurrentBitSet getXor(ConcurrentBitSet setA, ConcurrentBitSet setB) {
		while (true) {
			setA.lock.readLock().lock();
			try {
				if (setB.lock.readLock().tryLock()) {
					try {
						final int lenA = setA.units.length();
						final int lenB = setA.units.length();
						final int lMin = Math.min(lenA, lenB);
						final int lMax = Math.max(lenA, lenB);
			    		final AtomicLongArray arr = new AtomicLongArray(lMax);
			    		for (int i = 0; i < lMin; i++) {
							arr.set(i, setA.units.get(i) ^ setB.units.get(i));
				    	}
			    		for (int i = lMin; i < lenA; i++) {
							arr.set(i, setA.units.get(i));
				    	}
			    		for (int i = lMin; i < lenB; i++) {
							arr.set(i, setB.units.get(i));
				    	}
						return new ConcurrentBitSet(arr);
					}
					finally {
						setB.lock.readLock().unlock();
					}
				}
			}
			finally {
				setA.lock.readLock().unlock();
			}
		}
    }
    public static ConcurrentBitSet getOr(ConcurrentBitSet setA, ConcurrentBitSet setB) {
		while (true) {
			setA.lock.readLock().lock();
			try {
				if (setB.lock.readLock().tryLock()) {
					try {
						final int lenA = setA.units.length();
						final int lenB = setA.units.length();
						final int lMin = Math.min(lenA, lenB);
						final int lMax = Math.max(lenA, lenB);
			    		final AtomicLongArray arr = new AtomicLongArray(lMax);
			    		for (int i = 0; i < lMin; i++) {
							arr.set(i, setA.units.get(i) | setB.units.get(i));
				    	}
			    		for (int i = lMin; i < lenA; i++) {
							arr.set(i, setA.units.get(i));
				    	}
			    		for (int i = lMin; i < lenB; i++) {
							arr.set(i, setB.units.get(i));
				    	}
						return new ConcurrentBitSet(arr);
					}
					finally {
						setB.lock.readLock().unlock();
					}
				}
			}
			finally {
				setA.lock.readLock().unlock();
			}
		}
    }

    public static ConcurrentBitSet getAnd(ConcurrentBitSet setA, ConcurrentBitSet setB) {
		while (true) {
			setA.lock.readLock().lock();
			try {
				if (setB.lock.readLock().tryLock()) {
					try {
						final int lenA = setA.units.length();
						final int lenB = setA.units.length();
						final int lMin = Math.min(lenA, lenB);
			    		final AtomicLongArray arr = new AtomicLongArray(lMin);
			    		for (int i = 0; i < lMin; i++) {
							arr.set(i, setA.units.get(i) & setB.units.get(i));
				    	}
						return new ConcurrentBitSet(arr);
					}
					finally {
						setB.lock.readLock().unlock();
					}
				}
			}
			finally {
				setA.lock.readLock().unlock();
			}
		}
	}
    /**
     * Returns setA and not setB
     */
    public static ConcurrentBitSet getAndNot(ConcurrentBitSet setA, ConcurrentBitSet setB) {
		while (true) {
			setA.lock.readLock().lock();
			try {
				if (setB.lock.readLock().tryLock()) {
					try {
						final int lenA = setA.units.length();
						final int lenB = setA.units.length();
						final int lMin = Math.min(lenA, lenB);
			    		final AtomicLongArray arr = new AtomicLongArray(lMin);
			    		for (int i = 0; i < lMin; i++) {
							arr.set(i, setA.units.get(i) & ~setB.units.get(i));
				    	}
						return new ConcurrentBitSet(arr);
					}
					finally {
						setB.lock.readLock().unlock();
					}
				}
			}
			finally {
				setA.lock.readLock().unlock();
			}
		}
	}
	
    public int length() {
		int index;
		long last = 0;
		lock.readLock().lock();
		try {
	    	index = units.length();
	    	do index--;
	    	while (index >= 0 && 0 == (last = units.get(index)));
		}
		finally {
			lock.readLock().unlock();
		}
    	return index < 0 ? 0 : index * BITS_PER_UNIT + bitLenL(last);
	}
    
    private static int bitLenL(long l) {
        int     high    = (int)(l >>> 32);
        int     low     = (int)l;        
        return high == 0 ? bitLenI(low) : 32 + bitLenI(high);        
    }
	
    /**
     * copied from: java.util.BitSet
     * bitLen(val) is the number of bits in val.
     */
    private static int bitLenI(int w) {
        // Binary search - decision tree (5 tests, rarely 6)
        return
         (w < 1<<15 ?
          (w < 1<<7 ?
           (w < 1<<3 ?
            (w < 1<<1 ? (w < 1<<0 ? (w<0 ? 32 : 0) : 1) : (w < 1<<2 ? 2 : 3)) :
            (w < 1<<5 ? (w < 1<<4 ? 4 : 5) : (w < 1<<6 ? 6 : 7))) :
           (w < 1<<11 ?
            (w < 1<<9 ? (w < 1<<8 ? 8 : 9) : (w < 1<<10 ? 10 : 11)) :
            (w < 1<<13 ? (w < 1<<12 ? 12 : 13) : (w < 1<<14 ? 14 : 15)))) :
          (w < 1<<23 ?
           (w < 1<<19 ?
            (w < 1<<17 ? (w < 1<<16 ? 16 : 17) : (w < 1<<18 ? 18 : 19)) :
            (w < 1<<21 ? (w < 1<<20 ? 20 : 21) : (w < 1<<22 ? 22 : 23))) :
           (w < 1<<27 ?
            (w < 1<<25 ? (w < 1<<24 ? 24 : 25) : (w < 1<<26 ? 26 : 27)) :
            (w < 1<<29 ? (w < 1<<28 ? 28 : 29) : (w < 1<<30 ? 30 : 31)))));
    }

    public boolean isEmpty() {
		int index;
		lock.readLock().lock();
		try {
	    	index = units.length();
	    	do index--;
	    	while (index >= 0 && 0 == units.get(index));
		}
		finally {
			lock.readLock().unlock();
		}
    	return index < 0;
	}
	
	@Override
	public ConcurrentBitSet clone() {
		final AtomicLongArray arr;
		lock.readLock().lock();
		try {
			final int len = units.length();
	    	arr = new AtomicLongArray(len);
	    	for (int i = 0; i < len; i++) {
				arr.set(i, units.get(i));
			}
		}
		finally {
			lock.readLock().unlock();
		}
		return new ConcurrentBitSet(arr);
	}
	
	public int cardinality() {
		lock.readLock().lock();
		try {
			int card = 0;
			final int len = units.length();
			for (int i = 0; i < len; i++) {
				card += Long.bitCount(units.get(i));
			}
			return card;
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
    public int cardinality(int fromBit, int toBit) {
        int fromUnit    = fromBit / BITS_PER_UNIT;
        int toUnit      = (toBit + BITS_PER_UNIT - 1) / BITS_PER_UNIT;
        
		lock.readLock().lock();
		try {
	        int unitStart   = Math.max(0, fromUnit);
	        int unitLen     = Math.min(units.length(), toUnit);
	        int card = 0;
	        for (int ii = unitStart; ii < unitLen; ii++) {
	        	long unit = units.get(ii);
	            if (unit != 0) {
	                if (ii == fromUnit) {
	                    int bit = fromBit % BITS_PER_UNIT;
	                    unit &= (0xffffffffffffffffL >>> bit);
	                }
	                if (ii == toUnit - 1) {
	                    int bit = fromBit % BITS_PER_UNIT;
	                    unit &= (0xffffffffffffffffL << (BITS_PER_UNIT - bit));
	                }
	                card += Long.bitCount(unit);
	            }
	        }
	        return card;
		}
		finally {
			lock.readLock().unlock();
		}
    }
	
    @Override
    public int hashCode() {
		lock.readLock().lock();
		try {
	        int code = 0;
			final int len = units.length();
			for (int i = 0; i < len; i++) {
				final long unit = units.get(i);
	            code ^= (0x00000000ffffffffL & unit);
	            code ^= (0x00000000ffffffffL & (unit >>> 32));
	        }
	        return code;
		}
		finally {
			lock.readLock().unlock();
		}
    }
    
    @Override
    public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof ConcurrentBitSet) {
			final ConcurrentBitSet other = (ConcurrentBitSet)obj;
			while (true) {
				lock.readLock().lock();
				try {
					if (other.lock.readLock().tryLock()) {
						try {
							final int len = units.length();
							final int oth = other.units.length();
							final int min = Math.min(len, oth);
							for (int ii = 0; ii < min; ii++) {
								if (units.get(ii) != other.units.get(ii)) return false;
							}		
							for (int i = min; i < len; i++) {
								if (units.get(i) != 0) return false;
							}
							for (int i = min; i < oth; i++) {
								if (other.units.get(i) != 0) return false;
							}
							return true;
						}
						finally {
							other.lock.readLock().unlock();
						}
					}
				}
				finally {
					lock.readLock().unlock();
				}
			}
		}
        return false;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append('{');

		lock.readLock().lock();
		try {
			final int unitLen = units.length();
	        for (int ii = 0; ii < unitLen; ii++) {
	        	final int len = ii == unitLen - 1 ? 1 + (length() - 1) % BITS_PER_UNIT : BITS_PER_UNIT;
	        	final long unit = units.get(ii);
	            if (unit != 0) {
	            	long bit = 1L;
	            	for (int jj = 0; jj < len; jj++, bit <<= 1) {
						if ((unit & bit) != 0) sb.append('1');
						else sb.append('0');
					}
	            }
	            else {
	            	sb.append("0000000000000000000000000000000000000000000000000000000000000000", 0, len);
	            }
	        }
		}
		finally {
			lock.readLock().unlock();
		}
    	sb.append('}');
    	return sb.toString();
    }
    
    public int nextSetBit(int from) {
    	final int fromBit	= from % BITS_PER_UNIT;
    	final int fromUnit	= from / BITS_PER_UNIT;
        
		lock.readLock().lock();
		try {
			final int len = units.length();
	        for (int ii = Math.max(0, fromUnit); ii < len; ii++) {
	        	long unit = units.get(ii);
	            if (unit != 0L) {
	                if (ii == fromUnit) unit &= (0xffffffffffffffffL << fromBit);
	                if (unit != 0L) return ii * BITS_PER_UNIT + Long.numberOfTrailingZeros(unit);
	            }
	        }
	        return -1;        
		}
		finally {
			lock.readLock().unlock();
		}
    }
    
    public int nextClearBit(int from) {
        final int fromBit	= from % BITS_PER_UNIT;
        final int fromUnit	= from / BITS_PER_UNIT;
        
		lock.readLock().lock();
		try {
			final int len = units.length();
	        for (int ii = fromUnit; ii < len; ii++) {
	        	long unit = units.get(ii);
	            if (unit != -1L) {
	                if (ii == fromUnit) unit &= (0xffffffffffffffffL << fromBit);
	                if (unit != 0) return ii * BITS_PER_UNIT + Long.numberOfTrailingZeros(unit);
	            }
	        }
	        return Math.max(from, length());        
		}
		finally {
			lock.readLock().unlock();
		}
    }
    
    public BitSet toBitSet() {
        final BitSet bitSet = new BitSet(length());
        for (int bit = nextSetBit(0); bit >= 0; bit = nextSetBit(bit + 1)) {
            bitSet.set(bit);
        }
        return bitSet;
    }
    
    public void writeTo(OutputStream out) throws IOException {
        final DataOutput dout = out instanceof DataOutput ?
                (DataOutput)out : new DataOutputStream(out);
   		
        lock.readLock().lock();
        try {
        	final int len = units.length();
            dout.writeInt(len);
            for (int i = 0; i < len; i++) {
                dout.writeLong(units.get(i));
            }
        }
		finally {
			lock.readLock().unlock();
		}
    }
    
    public static ConcurrentBitSet readFrom(InputStream in) throws IOException {
        final DataInput din = in instanceof DataInput ? 
                (DataInput)in : new DataInputStream(in);
        
        final int len = din.readInt();
        final AtomicLongArray arr = new AtomicLongArray(len);
        for (int i = 0; i < len; i++) {
        	arr.set(i, din.readLong());
        }
        return new ConcurrentBitSet(arr);
    }
    
    public long[] toLongArray() {
        return toLongArray(null, 0);
    }
    
    public long[] toLongArray(long[] arr, int offset) {
        lock.readLock().lock();
        try {
        	final int len = units.length();
        	if (arr == null || arr.length < len + offset) {
                arr = new long[len + offset];
            }
            for (int i = 0; i < len; i++) {
                arr[i] = units.get(i);
            }
            return arr;
        }
		finally {
			lock.readLock().unlock();
		}
    }
    
    public static void main(String[] args) {
    	System.out.println(new ConcurrentBitSet("00001111").nextClearBit(0));
    	System.out.println(new ConcurrentBitSet("00001111").nextClearBit(1));
    	System.out.println(new ConcurrentBitSet("00001111").nextClearBit(2));
    	System.out.println(new ConcurrentBitSet("00001111").nextClearBit(3));
    	System.out.println(new ConcurrentBitSet("00001111").nextClearBit(4));

    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(0));
    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(1));
    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(2));
    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(3));
    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(4));
    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(7));
    	System.out.println(new ConcurrentBitSet("00001111").nextSetBit(8));
        System.out.println(new ConcurrentBitSet("01010101010101010101"));
        System.out.println(new ConcurrentBitSet("11111111000000001111000000001111"));
        System.out.println(new ConcurrentBitSet("1111111100000000111100000000111111111111000000001111000000001111"));
        System.out.println(ConcurrentBitSet.getAnd(new ConcurrentBitSet("111100001111"), new ConcurrentBitSet("110011110101")));
        System.out.println(ConcurrentBitSet.getAnd(new ConcurrentBitSet("111100001111111100001111111100001111111100001111111100001111111100001111111100001111111100001111111100001111111100001111111100001111111100001111"), new ConcurrentBitSet("110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101110011110101")));
        ConcurrentBitSet set = new ConcurrentBitSet("1111111100000000111100000000111111111111000000001111000000001111");
        set.clear(3);
        System.out.println(set);
        set.clear(0);
        System.out.println(set);
        set = new ConcurrentBitSet("10000000000000000000000000000000000000000000000000000000000000011000000000000000000000000000000000000000000000000000000000000001");
        System.out.println(set);
        set.clear(63);
        System.out.println(set);
        set.clear(64);
        System.out.println(set);
        set.clear(127);
        System.out.println(set);
        set.clear(0);
        System.out.println(set);
        set.set(63);
        System.out.println(set);
        set.set(64);
        System.out.println(set);
        set.set(127);
        System.out.println(set);
        set.set(0);
        System.out.println(set);
    }

}
