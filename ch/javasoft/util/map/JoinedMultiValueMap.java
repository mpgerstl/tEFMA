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
package ch.javasoft.util.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * The <code>JoinedMultiValueMap</code> class joins two instances of 
 * {@link MultiValueMap}. The resulting multi value map is immutable. 
 * <p>
 * Joining multi value maps nests the joined maps and does not copy the values. 
 * However, for a large number of accesses, it might be more efficient to 
 * materialize the joined map using {@link #flatten()} beforehand. 
 * 
 * @param <K>	the key type of the one side of the join
 * @param <V>	the value type of the many side of the join
 */
public class JoinedMultiValueMap<K, V> extends AbstractMultiValueMap<K, V> implements Serializable {
	
	private static final long serialVersionUID = 7319560387222766983L;
	
	private final MultiValueMap<K, ?> one;
	private final MultiValueMap<?, V> many;
	
	/**
	 * Cconstructor to join two multi value maps
	 * 
	 * @param one	the one side of the join
	 * @param many	the many side of the join
	 */
	public JoinedMultiValueMap(MultiValueMap<K, ?> one, MultiValueMap<?, V> many) {
		this.one 	= one;
		this.many	= many;
	}
	
	/**
	 * Alternative to the constructor, for convenience only. Joins the two
	 * multi value maps resulting in a new virtual multi value map.
	 * 
	 * @param <K>	the key type of the one side of the join
	 * @param <V>	the value type of the many side of the join
	 * @param one	the one side of the join
	 * @param many	the many side of the join
	 * 
	 * @return	the joined multi value map
	 */
	public static <K, V> JoinedMultiValueMap<K, V> join(MultiValueMap<K, ?> one, MultiValueMap<?, V> many) {
		return new JoinedMultiValueMap<K, V>(one, many);
	}
	/**
	 * Joins several multi value maps resulting in a new virtual multi value map.
	 * The first map defines the key type of the resulting map, the last map the
	 * value type.
	 * 
	 * @param <K>	the key type of the head map of the join
	 * @param <V>	the value type of the tail map of the join
	 * @param first			the head map of the join
	 * @param last			the tail map of the join
	 * @param intermediate	intermediary maps of the join, in join order
	 * 
	 * @return	the joined multi value map
	 */
	public static <K, V> JoinedMultiValueMap<K, V> join(MultiValueMap<K, ?> first, MultiValueMap<?, V> last, MultiValueMap<?, ?> ... intermediate) {
		for (int i = 0; i < intermediate.length; i++) {
			first = join(first, intermediate[i]);
		}
		return join(first, last);		
	}

	/**
	 * Materializes this virtual multi value map into a new instance of
	 * {@link DefaultMultiValueMap}, a mutable map, by copying the content of
	 * this map into the new instance.
	 * 
	 * @return  a {@link DefaultMultiValueMap} instance containing the same 
	 * 			key/collection-of-value pairs as this multi value map
	 */
	public MultiValueMap<K, V> flatten() {
		return new DefaultMultiValueMap<K, V>(this);
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object key) {
		for (final Object vk : one.get(key)) {
			if (many.contains(vk)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#contains(java.lang.Object, java.lang.Object)
	 */
	public boolean contains(Object key, V value) {
		for (final Object vk : one.get(key)) {
			if (many.contains(vk, value)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#count(java.lang.Object)
	 */
	public int count(Object key) {
		int cnt = 0;
		for (final Object vk : one.get(key)) {
			cnt += many.count(vk);
		}
		return cnt;
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#get(java.lang.Object)
	 */
	public Collection<V> get(Object key) {
		Collection<V> coll = null;
		for (final Object vk : one.get(key)) {
			if (coll == null) {
				coll = many.get(vk);
			}
			else {
				coll = createCollection(coll);
				coll.addAll(many.get(vk));
			}
		}
		if (coll == null) {
			coll = createCollection(null);
		}
		return coll;
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#getFirst(java.lang.Object)
	 */
	@Override
	public V getFirst(Object key) {
		for (final Object vk : one.get(key)) {
			final V val = many.getFirst(vk);
			if (val != null) return val;
		}
		return null;
	}

	/**
	 * Returns an unmodifiable set with the keys
	 * 
	 * @see ch.javasoft.util.map.MultiValueMap#keySet()
	 */
	public Set<K> keySet() {
		return Collections.unmodifiableSet(one.keySet());
	}

	/* (non-Javadoc)
	 * @see ch.javasoft.util.map.OneToMany#values()
	 */
	@Override
	public Iterable<V> values() {
		ArrayList<V> vals = new ArrayList<V>();
		for (final K key : one.keySet()) {
			for (final Object vk : one.get(key)) {
				vals.addAll(many.get(vk));
			}
		}
		return vals;
	}
	
	/**
	 * Returns a new collection instance. By default, an {@link ArrayList} with
	 * the given values is created, or an {@link Collections#emptyList() empty 
	 * list} if values is null. 
	 * 
	 * @param values	the values to be added to the created list, or null if
	 * 					an empty list should be returned
	 * @return		the new array list instance, or an 
	 * 				{@link Collections#emptyList() empty list} if values is null
	 */
	protected Collection<V> createCollection(Collection<V> values) {
		if (values == null) {
			return Collections.emptyList();
		}
		return new ArrayList<V>(values);
	}
	
}
