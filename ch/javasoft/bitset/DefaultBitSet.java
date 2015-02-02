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

import java.util.BitSet;

/**
 * <code>DefaultBitSet</code> is the implementation of {@link IBitSet}
 * backed with Java's own {@link java.util.BitSet}.
 */
public class DefaultBitSet implements IBitSet {
    
	/**
	 * Default static factory for {@link DefaultBitSet} instances.
	 */
    public static final DefaultBitSetFactory FACTORY = new DefaultBitSetFactory();
    
    /**
     * <code>DefaultBitSetFactory</code> is the {@link BitSetFactory} for 
     * {@code DefaultBitSet} instances.
     */
    public static final class DefaultBitSetFactory implements BitSetFactory {
        public DefaultBitSet create() {
            return new DefaultBitSet();
        }
        public DefaultBitSet create(int capacity) {
        	return new DefaultBitSet(capacity);
        }
        public DefaultBitSet create(IBitSet bits) {
        	return new DefaultBitSet(bits);
        }
        public DefaultBitSet create(BitSet bits) {
            return new DefaultBitSet(bits);
        }
        public DefaultBitSet convert(IBitSet bitSet) {
        	return bitSet instanceof DefaultBitSet ? (DefaultBitSet)bitSet : new DefaultBitSet(bitSet);
        }
        public Class<DefaultBitSet> getBitSetClass() {
        	return DefaultBitSet.class;
        }
    };
    
    private BitSet mBitSet;

    /**
     * Creates an empty bit set. All bits are initially false.
     */
    public DefaultBitSet() {
        this(new BitSet());
    }
    /**
     * Creates an empty bit set with the specified initial capacity. All bits 
     * are initially false.
     */
    public DefaultBitSet(int capacity) {
        this(new BitSet(capacity));
    }
    
    /**
     * Constructor using the specified bits to initialize this bit set. The
     * bit set argument is <b>not</b> cloned, meaning that changes to it are 
     * also reflected in {@code this} bit set.
     * 
     * @param bitSet	the bit set used as underlying store for the bits
     */
    public DefaultBitSet(BitSet bitSet) {
        mBitSet = bitSet;
    }
    
    /**
     * Constructor using the specified bits to initialize this bit set. The bits
     * are copied, meaning that changes to the given bit set are <b>not</b> 
     * reflected in {@code this} bit set.
     * 
     * @param bitSet	the bit set used which's bits are used to initialize 
     * 					this bit set
     */
    public DefaultBitSet(IBitSet bitSet) {
        this(bitSet.toBitSet());
    }

    public void set(int bit) {
        mBitSet.set(bit);
    }
    public void set(int bit, boolean value) {
    	mBitSet.set(bit, value);
    }

    public void clear(int bit) {
        mBitSet.clear(bit);
    }
    public void clear() {
        mBitSet.clear();
    }

    public void flip(int bit) {
        mBitSet.flip(bit);
    }

    public boolean get(int bit) {
        return mBitSet.get(bit);
    }

    public boolean isSubSetOf(IBitSet of) {
    	return isSubSetOf(of instanceof DefaultBitSet ? ((DefaultBitSet)of).mBitSet : of.toBitSet());
    }
    public boolean isSubSetOf(DefaultBitSet of) {
    	return isSubSetOf(of.mBitSet);
    }
    public boolean isSubSetOf(BitSet of) {
        if (length() > of.length()) return false;
        final BitSet and = (BitSet)mBitSet.clone();
        and.and(of);
        return and.equals(mBitSet);
        
        //or is this faster? sure with few bits set but large length
        /*
        int start = nextSetBit(0);
        while (start >= 0) {
            if (start != of.nextSetBit(start)) return false;
            start = nextSetBit(start + 1);
        }
        return true;
        */
    }

    public boolean isSuperSetOfIntersection(IBitSet interA, IBitSet interB) {
    	final BitSet bsA = interA instanceof DefaultBitSet ? ((DefaultBitSet)interA).mBitSet : interA.toBitSet();
    	final BitSet bsB = interB instanceof DefaultBitSet ? ((DefaultBitSet)interB).mBitSet : interB.toBitSet();
    	return isSuperSetOfIntersection(bsA, bsB);
    }
    public boolean isSuperSetOfIntersection(BitSet interA, BitSet interB) {
        final BitSet and = (BitSet)interA.clone();
        and.and(interB);
        and.and(mBitSet);
        return and.equals(mBitSet);
    }
    
    public void and(IBitSet with) {
    	and(with instanceof DefaultBitSet ? (DefaultBitSet)with : new DefaultBitSet(with));
    }
    public void and(DefaultBitSet with) {
        mBitSet.and(with.mBitSet);
    }
	public DefaultBitSet getAnd(IBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.mBitSet.and(with instanceof DefaultBitSet ? ((DefaultBitSet)with).mBitSet : with.toBitSet());
    	return clone;
	}
	public DefaultBitSet getAnd(DefaultBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.and(with);
    	return clone;
	}
	public int getAndCardinality(IBitSet with) {
		return getAnd(with).cardinality();
	}
	public int getAndCardinality(DefaultBitSet with) {
		return getAnd(with).cardinality();
	}

    public void or(IBitSet with) {
    	or(with instanceof DefaultBitSet ? (DefaultBitSet)with : new DefaultBitSet(with));
    }
    public void or(DefaultBitSet with) {
        mBitSet.or(with.mBitSet);
    }
	public DefaultBitSet getOr(IBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.mBitSet.or(with instanceof DefaultBitSet ? ((DefaultBitSet)with).mBitSet : with.toBitSet());
    	return clone;
	}
	public DefaultBitSet getOr(DefaultBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.or(with);
    	return clone;
	}

    public void xor(IBitSet with) {
    	xor(with instanceof DefaultBitSet ? (DefaultBitSet)with : new DefaultBitSet(with));
    }
    public void xor(DefaultBitSet with) {
        mBitSet.xor(with.mBitSet);
    }
	public DefaultBitSet getXor(IBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.mBitSet.xor(with instanceof DefaultBitSet ? ((DefaultBitSet)with).mBitSet : with.toBitSet());
    	return clone;
	}
	public DefaultBitSet getXor(DefaultBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.xor(with);
    	return clone;
	}
	public int getXorCardinality(IBitSet with) {
		return getXor(with).cardinality();
	}
	public int getXorCardinality(DefaultBitSet with) {
		return getXor(with).cardinality();
	}

	public void andNot(IBitSet with) {
    	andNot(with instanceof DefaultBitSet ? (DefaultBitSet)with : new DefaultBitSet(with));
    }
    public void andNot(DefaultBitSet with) {
        mBitSet.andNot(with.mBitSet);
    }
	public DefaultBitSet getAndNot(IBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.mBitSet.andNot(with instanceof DefaultBitSet ? ((DefaultBitSet)with).mBitSet : with.toBitSet());
    	return clone;
	}
	public DefaultBitSet getAndNot(DefaultBitSet with) {
		final DefaultBitSet clone = clone();
    	clone.andNot(with);
    	return clone;
	}

    public int length() {
        return mBitSet.length();
    }

    public int cardinality() {
        return mBitSet.cardinality();
    }
    
    public int cardinality(int fromBit, int toBit) {
        return mBitSet.get(fromBit, toBit).cardinality();
    }

    public int compareTo(IBitSet o) {
    	return compareTo(o instanceof DefaultBitSet ? (DefaultBitSet)o : new DefaultBitSet(o));
    }
    public int compareTo(DefaultBitSet o) {
    	throw new RuntimeException("not implemented");
    }

    public int nextSetBit(int from) {
        return mBitSet.nextSetBit(from);
    }

    public int nextClearBit(int from) {
        return mBitSet.nextClearBit(from);
    }
    
    @Override
    public DefaultBitSet clone() {
        return new DefaultBitSet((BitSet)mBitSet.clone());
    }
    
    @Override
    public String toString() {
        return mBitSet.toString();
    }
    
    @Override
    public int hashCode() {
        return mBitSet.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof DefaultBitSet) {
            return mBitSet.equals(((DefaultBitSet)obj).mBitSet);
        }
        return false;
    }
    
    public BitSet toBitSet() {
        return (BitSet)mBitSet.clone();
    }
    
    public BitSetFactory factory() {
    	return FACTORY;
    }
    
}
