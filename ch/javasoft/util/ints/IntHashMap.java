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
package ch.javasoft.util.ints;

/*
 * NOTE:
 * this code has mainly been copied from java.util.HashMap
 */

/*
 * @(#)HashMap.java	1.63 04/02/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

/**
 * Same as {@link java.util.HashMap}, but implementing {@link IntMap}.
 * 
 * <p>Hash table based implementation of the <tt>Map</tt> interface.  This
 * implementation provides all of the optional map operations, and permits
 * <tt>null</tt> values and the <tt>null</tt> key.  (The <tt>IntHashMap</tt>
 * class is roughly equivalent to <tt>Hashtable</tt>, except that it is
 * unsynchronized and permits nulls.)  This class makes no guarantees as to
 * the order of the map; in particular, it does not guarantee that the order
 * will remain constant over time.
 *
 * <p>This implementation provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the hash function
 * disperses the elements properly among the buckets.  Iteration over
 * collection views requires time proportional to the "capacity" of the
 * <tt>IntHashMap</tt> instance (the number of buckets) plus its size (the number
 * of key-value mappings).  Thus, it's very important not to set the initial
 * capacity too high (or the load factor too low) if iteration performance is
 * important.
 *
 * <p>An instance of <tt>IntHashMap</tt> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the capacity is roughly doubled by calling the
 * <tt>rehash</tt> method.
 *
 * <p>As a general rule, the default load factor (.75) offers a good tradeoff
 * between time and space costs.  Higher values decrease the space overhead
 * but increase the lookup cost (reflected in most of the operations of the
 * <tt>IntHashMap</tt> class, including <tt>get</tt> and <tt>put</tt>).  The
 * expected number of entries in the map and its load factor should be taken
 * into account when setting its initial capacity, so as to minimize the
 * number of <tt>rehash</tt> operations.  If the initial capacity is greater
 * than the maximum number of entries divided by the load factor, no
 * <tt>rehash</tt> operations will ever occur.
 *
 * <p>If many mappings are to be stored in a <tt>IntHashMap</tt> instance,
 * creating it with a sufficiently large capacity will allow the mappings to
 * be stored more efficiently than letting it perform automatic rehashing as
 * needed to grow the table.
 *
 * <p><b>Note that this implementation is not synchronized.</b> If multiple
 * threads access this map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or
 * more mappings; merely changing the value associated with a key that an
 * instance already contains is not a structural modification.)  This is
 * typically accomplished by synchronizing on some object that naturally
 * encapsulates the map.  If no such object exists, the map should be
 * "wrapped" using the <tt>Collections.synchronizedMap</tt> method.  This is
 * best done at creation time, to prevent accidental unsynchronized access to
 * the map: <pre> Map m = Collections.synchronizedMap(new IntHashMap(...));
 * </pre>
 *
 * <p>The iterators returned by all of this class's "collection view methods"
 * are <i>fail-fast</i>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator will throw a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis. 
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the 
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author  Doug Lea
 * @author  Josh Bloch
 * @author  Arthur van Hoff
 * @author  Neal Gafter
 * @version 1.63, 02/19/04
 * @see     Object#hashCode()
 * @see     Collection
 * @see	    Map
 * @see	    TreeMap
 * @see	    Hashtable
 * @see	    IntHashMap
 * @since   1.2
 */

public class IntHashMap<V> implements IntMap<V>, Cloneable, Serializable {

	private static final long serialVersionUID = -4555851201703297629L;

	/**
     * The default initial capacity - MUST be a power of two.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    public static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     **/
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    private transient IntEntry[] table;

    /**
     * The number of key-value mappings contained in this identity hash map.
     */
    private transient int size;
  
    /**
     * The next size value at which to resize (capacity * load factor).
     * @serial
     */
    private int threshold;
  
    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    private final float loadFactor;

    /**
     * The number of times this IntHashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the IntHashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the IntHashMap fail-fast.  (See ConcurrentModificationException).
     */
    private transient volatile int modCount;

    /**
     * Constructs an empty <tt>IntHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity The initial capacity.
     * @param  loadFactor      The load factor.
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive.
     */
    public IntHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) 
            capacity <<= 1;
    
        this.loadFactor = loadFactor;
        threshold = (int)(capacity * loadFactor);
        table = new IntEntry[capacity];
        init();
    }
  
    /**
     * Constructs an empty <tt>IntHashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public IntHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>IntHashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public IntHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table = new IntEntry[DEFAULT_INITIAL_CAPACITY];
        init();
    }

    /**
     * Constructs a new <tt>IntHashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>.  The <tt>IntHashMap</tt> is created with
     * default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param   m the map whose mappings are to be placed in this map.
     * @throws  NullPointerException if the specified map is null.
     */
    public IntHashMap(Map<? extends Integer, ? extends V> m) {
        this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,
                      DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        putAllForCreate(m);
    }

    // internal utilities

    /**
     * Initialization hook for subclasses. This method is called
     * in all constructors and pseudo-constructors (clone, readObject)
     * after IntHashMap has been initialized but before any entries have
     * been inserted.  (In the absence of this method, readObject would
     * require explicit knowledge of subclasses.)
     */
    private void init() {
    }

//    /**
//     * Returns a hash value for the specified object.  In addition to 
//     * the object's own hashCode, this method applies a "supplemental
//     * hash function," which defends against poor quality hash functions.
//     * This is critical because IntHashMap uses power-of two length 
//     * hash tables.<p>
//     *
//     * The shift distances in this function were chosen as the result
//     * of an automated search over the entire four-dimensional search space.
//     */
//    protected int hash(Object x) {
//        int h = x.hashCode();
//
//        h += ~(h << 9);
//        h ^=  (h >>> 14);
//        h +=  (h << 4);
//        h ^=  (h >>> 10);
//        return h;
//    }
//
//    /** 
//     * Check for equality of non-null reference x and possibly-null y. 
//     */
//    protected boolean eq(Object x, Object y) {
//        return x == y || x.equals(y);
//    }

    /**
     * Returns index for key
     */
    protected int indexFor(int key, int length) {
		key += ~(key << 9);
		key ^=  (key >>> 14);
		key +=  (key << 4);
		key ^=  (key >>> 10);
		return key & (length-1);
    }
 
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return size;
    }
  
    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
     * that the map contains no mapping for the key; it is also possible that
     * the map explicitly maps the key to <tt>null</tt>. The
     * <tt>containsKey</tt> method may be used to distinguish these two cases.
     *
     * @param   key the key whose associated value is to be returned.
     * @return  the value to which this map maps the specified key, or
     *          <tt>null</tt> if the map contains no mapping for this key.
     * @see #put(Object, Object)
     */
	public V get(Object key) {
    	if (!(key instanceof Integer)) return null;
        return get(((Integer)key).intValue());
    }
    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
     * that the map contains no mapping for the key; it is also possible that
     * the map explicitly maps the key to <tt>null</tt>. The
     * <tt>containsKey</tt> method may be used to distinguish these two cases.
     *
     * @param   key the key whose associated value is to be returned.
     * @return  the value to which this map maps the specified key, or
     *          <tt>null</tt> if the map contains no mapping for this key.
     * @see #put(Object, Object)
     */
    @SuppressWarnings("unchecked")
	public V get(int key) {
        int i = indexFor(key, table.length);
        IntEntry<V> e = table[i]; 
        while (true) {
            if (e == null)
                return null;
            if (e.key == key) 
                return e.value;
            e = e.next;
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(int key) {
        int i = indexFor(key, table.length);
        IntEntry e = table[i]; 
        while (e != null) {
            if (e.key == key) 
                return true;
            e = e.next;
        }
        return false;
    }
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
    	if (!(key instanceof Integer)) return false;
        return containsKey(((Integer)key).intValue());
    }

    /**
     * Returns the entry associated with the specified key in the
     * IntHashMap.  Returns null if the IntHashMap contains no mapping
     * for this key.
     */
    @SuppressWarnings("unchecked") 
    protected IntEntry<V> getEntry(int key) {
        int i = indexFor(key, table.length);
        IntEntry<V> e = table[i]; 
        while (e != null && !(e.key == key))
            e = e.next;
        return e;
    }
  
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the IntHashMap previously associated
     *	       <tt>null</tt> with the specified key.
     * @throws NullPointerException if the key is null
     */
	public V put(Integer key, V value) {
    	if (key == null) {
    		throw new NullPointerException("null key not allowed");
    	}
        return put(key.intValue(), value);
    }
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the IntHashMap previously associated
     *	       <tt>null</tt> with the specified key.
     * @throws NullPointerException if the key is null
     */
    @SuppressWarnings("unchecked")
    public V put(int key, V value) {
        int i = indexFor(key, table.length);

        for (IntEntry<V> e = table[i]; e != null; e = e.next) {
            if (e.key == key) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(key, value, i);
        return null;
    }

    /**
     * This method is used instead of put by constructors and
     * pseudoconstructors (clone, readObject).  It does not resize the table,
     * check for comodification, etc.  It calls createEntry rather than
     * addEntry.
     */
    @SuppressWarnings("unchecked")
	private void putForCreate(int key, V value) {
        int i = indexFor(key, table.length);

        /**
         * Look for preexisting entry for key.  This will never happen for
         * clone or deserialize.  It will only happen for construction if the
         * input Map is a sorted map whose ordering is inconsistent w/ equals.
         */
        for (IntEntry<V> e = table[i]; e != null; e = e.next) {
            if (e.key == key) {
                e.value = value;
                return;
            }
        }

        createEntry(key, value, i);
    }

	private void putAllForCreate(Map<? extends Integer, ? extends V> m) {
    	if (m instanceof IntMap) {
    		IntMap<? extends V> im = castToIntMap(m);
            for (Iterator<? extends IntMap.IntEntry<? extends V>> i = im.intEntrySet().iterator(); i.hasNext(); ) {
                IntMap.IntEntry<? extends V> e = i.next();
                putForCreate(e.getIntKey(), e.getValue());            	
            }    		
    	}
    	else {
            for (Iterator<? extends Map.Entry<? extends Integer, ? extends V>> i = m.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<? extends Integer, ? extends V> e = i.next();
                putForCreate(e.getKey().intValue(), e.getValue());            	
            }    		
    	}
    }
    
	/**
	 * @throws ClassCastException if the map is not an IntMap
	 */
    @SuppressWarnings("unchecked")
	private static <VV> IntMap<VV> castToIntMap(Map<? extends Integer, VV> map) {
    	return (IntMap<VV>)map;
    }  

    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    protected void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        IntEntry[] newTable = new IntEntry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * loadFactor);
    }

    /** 
     * Transfer all entries from current table to newTable.
     */
    @SuppressWarnings("unchecked") 
    private void transfer(IntEntry[] newTable) {
        IntEntry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            IntEntry<V> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    IntEntry<V> next = e.next;
                    int i = indexFor(e.key, newCapacity);  
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }
    
    /**
     * Copies all of the mappings from the specified map to this map
     * These mappings will replace any mappings that
     * this map had for any of the keys currently in the specified map.
     *
     * @param map mappings to be stored in this map.
     * @throws NullPointerException if the specified map is null.
     */
    public void putAll(IntMap<? extends V> map) {
    	putAll((Map<? extends Integer, ? extends V>)map);
    }

    /**
     * Copies all of the mappings from the specified map to this map
     * These mappings will replace any mappings that
     * this map had for any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map.
     * @throws NullPointerException if the specified map is null.
     */
    public void putAll(Map<? extends Integer, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
            return;

        /*
         * Expand the map if the map if the number of mappings to be added
         * is greater than or equal to threshold.  This is conservative; the
         * obvious condition is (m.size() + size) >= threshold, but this
         * condition could result in a map with twice the appropriate capacity,
         * if the keys to be added overlap with the keys already in this map.
         * By using the conservative calculation, we subject ourself
         * to at most one extra resize.
         */
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
                targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity)
                newCapacity <<= 1;
            if (newCapacity > table.length)
                resize(newCapacity);
        }

        if (m instanceof IntMap) {
        	IntMap<? extends V> im = castToIntMap(m);
            for (Iterator<? extends IntMap.IntEntry<? extends V>> i = im.intEntrySet().iterator(); i.hasNext(); ) {
                IntMap.IntEntry<? extends V> e = i.next();
                put(e.getIntKey(), e.getValue());
            }        	
        }
        else {
            for (Iterator<? extends Map.Entry<? extends Integer, ? extends V>> i = m.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<? extends Integer, ? extends V> e = i.next();
                put(e.getKey().intValue(), e.getValue());
            }        	
        }
    }
  
    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key.
     */
    public V remove(int key) {
        IntEntry<V> e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }
    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key.
     */
    public V remove(Object key) {
    	if (!(key instanceof Integer)) {
    		return null;
    	}
        return remove(((Integer)key).intValue());
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the IntHashMap.  Returns null if the IntHashMap contains no mapping
     * for this key.
     */
    @SuppressWarnings("unchecked") 
    protected IntEntry<V> removeEntryForKey(int key) {
        int i = indexFor(key, table.length);
        IntEntry<V> prev = table[i];
        IntEntry<V> e = prev;

        while (e != null) {
            IntEntry<V> next = e.next;
            if (e.key == key) {
                modCount++;
                size--;
                if (prev == e) 
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }
   
        return e;
    }

    /**
     * Special version of remove for EntrySet.
     */
    @SuppressWarnings("unchecked") 
    protected IntEntry<V> removeMapping(Object o) {
        if (!(o instanceof IntMap.IntEntry))
            return null;

        IntMap.IntEntry<?> entry = (IntMap.IntEntry<?>) o;
        int k = entry.getIntKey();
        int i = indexFor(k, table.length);
        IntEntry<V> prev = table[i];
        IntEntry<V> e = prev;

        while (e != null) {
            IntEntry<V> next = e.next;
            if (e.key == k) {
                modCount++;
                size--;
                if (prev == e) 
                    table[i] = next;
                else
                    prev.next = next;
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }
   
        return e;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        modCount++;
        Entry[] tab = table;
        for (int i = 0; i < tab.length; i++) 
            tab[i] = null;
        size = 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(Object value) {
    	if (value == null) 
            return containsNullValue();

    	IntEntry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (IntEntry e = tab[i] ; e != null ; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    /**
     * Special-case code for containsValue with null argument
     **/
    private boolean containsNullValue() {
    	IntEntry[] tab = table;
        for (int i = 0; i < tab.length ; i++)
            for (IntEntry e = tab[i] ; e != null ; e = e.next)
                if (e.value == null)
                    return true;
        return false;
    }

    /**
     * Returns a shallow copy of this <tt>IntHashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map.
     */
    @Override
	@SuppressWarnings("unchecked")
	public IntHashMap<V> clone() {
        IntHashMap<V> result = null;
        try { 
        	result = (IntHashMap<V>)super.clone();
        } catch (CloneNotSupportedException ex) { 
			//should not happen since we are cloneable
       	throw new RuntimeException(ex);
        }
		result.keySet = null;
		result.values = null;
        result.table = new IntEntry[table.length];
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);

        return result;
    }

    public static class IntEntry<VV> implements IntMap.IntEntry<VV> {
    	protected final int key;
        protected VV value;
        protected IntEntry<VV> next;

        /**
         * Create new entry.
         */
        protected IntEntry(int k, VV v, IntEntry<VV> n) {
            value = v;
            next = n;
            key = k;
        }

        public Integer getKey() {
            return Integer.valueOf(key);
        }
        
        public int getIntKey() {
        	return key;
        }

        public VV getValue() {
            return value;
        }
    
        public VV setValue(VV newValue) {
        	VV oldValue = value;
            value = newValue;
            return oldValue;
        }
    
        @Override
		public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
            final Object eKey = e.getKey();
            final Object eVal = e.getValue();
            if (eKey instanceof Integer && ((Integer)eKey).intValue() == getIntKey()) {
            	return eVal == null ?
            			getValue() == null : eVal.equals(getValue());
            }
            return false;
        }
    
        @Override
		public int hashCode() {
            return key ^ (value==null ? 0 : value.hashCode());
        }
    
        @Override
		public String toString() {
            return getIntKey() + "=" + getValue();
        }

        /**
         * This method is invoked whenever the value in an entry is
         * overwritten by an invocation of put(k,v) for a key k that's already
         * in the IntHashMap.
         */
        protected void recordAccess(IntHashMap<VV> m) {
        }

        /**
         * This method is invoked whenever the entry is
         * removed from the table.
         */
        protected void recordRemoval(IntHashMap<VV> m) {
        }
    }

    /**
     * Add a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this 
     * method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     */
    @SuppressWarnings("unchecked") 
    private void addEntry(int key, V value, int bucketIndex) {
    	IntEntry<V> e = table[bucketIndex];
        table[bucketIndex] = new IntEntry<V>(key, value, e);
        if (size++ >= threshold)
            resize(2 * table.length);
    }

    /**
     * Like addEntry except that this version is used when creating entries
     * as part of Map construction or "pseudo-construction" (cloning,
     * deserialization).  This version needn't worry about resizing the table.
     *
     * Subclass overrides this to alter the behavior of IntHashMap(Map),
     * clone, and readObject.
     */
    @SuppressWarnings("unchecked") 
    private void createEntry(int key, V value, int bucketIndex) {
    	IntEntry<V> e = table[bucketIndex];
        table[bucketIndex] = new IntEntry<V>(key, value, e);
        size++;
    }

    private abstract class HashIterator<E> implements Iterator<E> {
        IntEntry<V> next;	// next entry to return
        int expectedModCount;	// For fast-fail 
        int index;		// current slot 
        IntEntry<V> current;	// current entry

        @SuppressWarnings("unchecked") 
        HashIterator() {
            expectedModCount = modCount;
            IntEntry[] t = table;
            int i = t.length;
            IntEntry<V> n = null;
            if (size != 0) { // advance to first entry
                while (i > 0 && (n = t[--i]) == null) {/*no op*/}
            }
            next = n;
            index = i;
        }

        public boolean hasNext() {
            return next != null;
        }

        @SuppressWarnings("unchecked")
        IntEntry<V> nextEntry() { 
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            IntEntry<V> e = next;
            if (e == null) 
                throw new NoSuchElementException();
                
            IntEntry<V> n = e.next;
            IntEntry[] t = table;
            int i = index;
            while (n == null && i > 0)
                n = t[--i];
            index = i;
            next = n;
            return current = e;
        }

        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            int k = current.key;
            current = null;
            IntHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }

    }

    private class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private class KeyIterator extends HashIterator<Integer> implements IntIterator {
        public Integer next() {
            return Integer.valueOf(nextEntry().key);
        }
        public int nextInt() {
            return nextEntry().key;
        }
    }

    private class IntEntryIterator extends HashIterator<IntMap.IntEntry<V>> {
        public IntMap.IntEntry<V> next() {
            return nextEntry();
        }
    }
    private class EntryIterator extends HashIterator<Map.Entry<Integer, V>> {
        public Map.Entry<Integer, V> next() {
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method
	protected IntIterator newKeyIterator()   {
        return new KeyIterator();
    }
	protected Iterator<V> newValueIterator()   {
        return new ValueIterator();
    }
	protected Iterator<IntMap.IntEntry<V>> newIntEntryIterator()   {
        return new IntEntryIterator();
    }
	protected Iterator<Map.Entry<Integer, V>> newEntryIterator()   {
        return new EntryIterator();
    }


    // Views

    protected transient IntEntrySet entrySet = null;

	protected transient volatile IntSet keySet;

	protected transient volatile  Collection<V> values;

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map.
     */
    public IntSet keySet() {
        IntSet ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private class KeySet extends AbstractSet<Integer> implements IntSet {
        @Override
		public IntIterator iterator() {
            return intIterator();
        }
        @Override
		public int size() {
            return size;
        }
        @Override
		public boolean contains(Object o) {
            return containsKey(o);
        }
		public boolean containsInt(int value) {
			return IntHashMap.this.containsKey(value);
		}
        @Override
		public boolean remove(Object o) {
        	if (o instanceof Integer) {
        		return removeInt(((Integer)o).intValue());
        	}
        	return false;
        }
        public boolean removeInt(int value) {
       		return IntHashMap.this.removeEntryForKey(value) != null;
        }
        @Override
		public void clear() {
            IntHashMap.this.clear();
        }
		public IntIterator intIterator() {
            return newKeyIterator();
		}
		@SuppressWarnings("unused")
		public BitSet toBitSet() {
			BitSet result = new BitSet();
			IntIterator it = intIterator();
			while (it.hasNext()) {
				result.set(it.nextInt());
			}
			return result;
		}		
		public int[] toIntArray() {
			return toIntArray(new int[size]);
		}
		public int[] toIntArray(int[] arr) {
			if (arr.length < size) {
				arr = new int[size];
			}
			IntIterator it = intIterator();
			int index = 0;
			while (it.hasNext()) {
				arr[index++] = it.nextInt();
			}
			return arr;
		}
		public boolean addInt(int value) {
			throw new UnsupportedOperationException();
		}
		public boolean addAll(IntCollection coll) {
			throw new UnsupportedOperationException();
		}
		public boolean addAll(int... values) {
			throw new UnsupportedOperationException();
		}
    }

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    private class Values extends AbstractCollection<V> {
        @Override
		public Iterator<V> iterator() {
            return newValueIterator();
        }
        @Override
		public int size() {
            return size;
        }
        @Override
		public boolean contains(Object o) {
            return containsValue(o);
        }
        @Override
		public void clear() {
            IntHashMap.this.clear();
        }
    }

    /**
     * Returns a collection view of the mappings contained in this map.  Each
     * element in the returned collection is a <tt>Map.Entry</tt>.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the mappings contained in this map.
     * @see java.util.Map.Entry
     */
    public Set<Map.Entry<Integer,V>> entrySet() {
		return internalIntEntrySet().toMapEntrySet();
    }
	public Set<IntMap.IntEntry<V>> intEntrySet() {
		return internalIntEntrySet();
	}
	
	private IntEntrySet internalIntEntrySet() {
		IntEntrySet es = entrySet;
		return (es != null ? es : (entrySet = new IntEntrySet()));
	}

    private class IntEntrySet extends AbstractSet<IntMap.IntEntry<V>> {
    	private transient AbstractSet<Map.Entry<Integer, V>> mapEntrySet;
        @Override
		public Iterator<IntMap.IntEntry<V>> iterator() {
            return newIntEntryIterator();
        }
        @Override
		public boolean contains(Object o) {
            if (!(o instanceof IntMap.IntEntry))
                return false;
            IntMap.IntEntry<?> e = (IntMap.IntEntry<?>) o;
            IntMap.IntEntry<?> candidate = getEntry(e.getIntKey());
            return candidate != null && candidate.equals(e);
        }
        @Override
		public boolean remove(Object o) {
            return removeMapping(o) != null;
        }
        @Override
		public int size() {
            return size;
        }
        @Override
		public void clear() {
            IntHashMap.this.clear();
        }
        public Set<Map.Entry<Integer, V>> toMapEntrySet() {
        	if (mapEntrySet == null) {
        		mapEntrySet = new AbstractSet<Map.Entry<Integer, V>>() {
        	        @Override
					public Iterator<Map.Entry<Integer, V>> iterator() {
        	        	return newEntryIterator();
        	        }
        	        @Override
					public boolean contains(Object o) {
        	        	return IntEntrySet.this.contains(o);
        	        }
        	        @Override
					public boolean remove(Object o) {
        	        	return IntEntrySet.this.remove(o);
        	        }
        	        @Override
					public int size() {
        	        	return IntEntrySet.this.size();
        	        }
        	        @Override
					public void clear() {
        	        	IntEntrySet.this.clear();
        	        }        			
        		};
        	}
        	return mapEntrySet;
        }
    }

    /**
     * Compares the specified object with this map for equality.  Returns
     * <tt>true</tt> if the given object is also a map and the two maps
     * represent the same mappings.  More formally, two maps <tt>t1</tt> and
     * <tt>t2</tt> represent the same mappings if
     * <tt>t1.keySet().equals(t2.keySet())</tt> and for every key <tt>k</tt>
     * in <tt>t1.keySet()</tt>, <tt> (t1.get(k)==null ? t2.get(k)==null :
     * t1.get(k).equals(t2.get(k))) </tt>.  This ensures that the
     * <tt>equals</tt> method works properly across different implementations
     * of the map interface.<p>
     *
     * This implementation first checks if the specified object is this map;
     * if so it returns <tt>true</tt>.  Then, it checks if the specified
     * object is a map whose size is identical to the size of this set; if
     * not, it returns <tt>false</tt>.  If so, it iterates over this map's
     * <tt>entrySet</tt> collection, and checks that the specified map
     * contains each mapping that this map contains.  If the specified map
     * fails to contain such a mapping, <tt>false</tt> is returned.  If the
     * iteration completes, <tt>true</tt> is returned.
     *
     * @param o object to be compared for equality with this map.
     * @return <tt>true</tt> if the specified object is equal to this map.
     */
    @Override
	public boolean equals(Object o) {
    	if (o == this) return true;
    	if (!(o instanceof IntMap)) return false;
    	IntMap<?> t = (IntMap<?>) o;
    	if (t.size() != size()) return false;

        Iterator<IntMap.IntEntry<V>> i = intEntrySet().iterator();
        while (i.hasNext()) {
            IntMap.IntEntry<V> e = i.next();
            int key = e.getIntKey();
            V value = e.getValue();
            if (value == null) {
                if (!(t.get(key)==null && t.containsKey(key)))
                    return false;
            } 
            else {
                if (!value.equals(t.get(key)))
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns the hash code value for this map.  The hash code of a map is
     * defined to be the sum of the hash codes of each entry in the map's
     * <tt>entrySet()</tt> view.  This ensures that <tt>t1.equals(t2)</tt>
     * implies that <tt>t1.hashCode()==t2.hashCode()</tt> for any two maps
     * <tt>t1</tt> and <tt>t2</tt>, as required by the general contract of
     * Object.hashCode.<p>
     *
     * This implementation iterates over <tt>entrySet()</tt>, calling
     * <tt>hashCode</tt> on each element (entry) in the Collection, and adding
     * up the results.
     *
     * @return the hash code value for this map.
     * @see java.util.Map.Entry#hashCode()
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    @Override
	public int hashCode() {
    	int h = 0;
    	Iterator<IntMap.IntEntry<V>> i = intEntrySet().iterator();
    	while (i.hasNext())
    		h += i.next().hashCode();
    	return h;
    }

    /**
     * Returns a string representation of this map.  The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's <tt>entrySet</tt> view's iterator, enclosed in braces
     * (<tt>"{}"</tt>).  Adjacent mappings are separated by the characters
     * <tt>", "</tt> (comma and space).  Each key-value mapping is rendered as
     * the key followed by an equals sign (<tt>"="</tt>) followed by the
     * associated value.  Keys and values are converted to strings as by
     * <tt>String.valueOf(Object)</tt>.<p>
     *
     * This implementation creates an empty string buffer, appends a left
     * brace, and iterates over the map's <tt>entrySet</tt> view, appending
     * the string representation of each <tt>map.entry</tt> in turn.  After
     * appending each entry except the last, the string <tt>", "</tt> is
     * appended.  Finally a right brace is appended.  A string is obtained
     * from the stringbuffer, and returned.
     *
     * @return a String representation of this map.
     */
    @Override
	public String toString() {
    	StringBuilder buf = new StringBuilder();
    	buf.append("{");

    	Iterator<IntMap.IntEntry<V>> i = intEntrySet().iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
        	IntMap.IntEntry<V> e = i.next();
        	int key = e.getIntKey();
            V value = e.getValue();
            buf.append(key);
            buf.append("=");
            if (value == this) buf.append("(this Map)");
            else buf.append(value);
            hasNext = i.hasNext();
            if (hasNext) buf.append(", ");
        }

        buf.append("}");
        return buf.toString();
    }

    /**
     * Save the state of the <tt>IntHashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the IntHashMap (the length of the
     *		   bucket array) is emitted (int), followed  by the
     *		   <i>size</i> of the IntHashMap (the number of key-value
     *		   mappings), followed by the key (Object) and value (Object)
     *		   for each key-value mapping represented by the IntHashMap
     *             The key-value mappings are emitted in the order that they
     *             are returned by <tt>entrySet().iterator()</tt>.
     * 
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
	// Write out the threshold, loadfactor, and any hidden stuff
	s.defaultWriteObject();

	// Write out number of buckets
	s.writeInt(table.length);

	// Write out size (number of Mappings)
	s.writeInt(size);

        // Write out keys and values (alternating)
        for (Iterator<IntMap.IntEntry<V>> i = intEntrySet().iterator(); i.hasNext(); ) {
            IntMap.IntEntry<V> e = i.next();
            s.writeInt(e.getIntKey());
            s.writeObject(e.getValue());
        }
    }


    /**
     * Reconstitute the <tt>IntHashMap</tt> instance from a stream (i.e.,
     * deserialize it).
     */
    @SuppressWarnings("unchecked") 
    private void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
	// Read in the threshold, loadfactor, and any hidden stuff
	s.defaultReadObject();

	// Read in number of buckets and allocate the bucket array;
	int numBuckets = s.readInt();
	table = new IntEntry[numBuckets];

        init();  // Give subclass a chance to do its thing.

	// Read in size (number of Mappings)
	int size = s.readInt();

	// Read the keys and values, and put the mappings in the IntHashMap
	for (int i=0; i<size; i++) {
	    int key = s.readInt();
	    V value = (V) s.readObject();
	    putForCreate(key, value);
	}
    }

}
