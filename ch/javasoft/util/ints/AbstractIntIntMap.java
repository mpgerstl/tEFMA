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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <code>AbstractIntIntMap</code> is the superclass for usual maps from int to 
 * int.
 */
abstract public class AbstractIntIntMap extends AbstractMap<Integer, Integer> implements IntIntMap {
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#get(java.lang.Object)
	 */
	@Override
	public Integer get(Object key) {
		if (key instanceof Integer) {
			try {
				return Integer.valueOf(getInt(((Integer)key).intValue()));
			}
			catch (NoSuchElementException ex) {
				//ignore & return null
				return null;
			}
		}
		return null;
	}
		
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntMap#get(int)
	 */
	public Integer get(int key) {
		try {
			return Integer.valueOf(getInt(key));
		}
		catch (NoSuchElementException ex) {
			//ignore & return null
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		if (key instanceof Integer) {
			return containsKey(((Integer)key).intValue());
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntIntMap#containsKey(int)
	 */
	public boolean containsKey(int key) {
		try {
			getInt(key);
			return true;
		}
		catch (NoSuchElementException ex) {
			return false;
		}		
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		if (value instanceof Integer) {
			return containsValue(((Integer)value).intValue());
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#entrySet()
	 */
	@Override
	public Set<Map.Entry<Integer, Integer>> entrySet() {
		return new AbstractSet<Map.Entry<Integer,Integer>>() {
			private final Set<IntIntMap.IntIntEntry> set = intIntEntrySet();
	        @Override
			public Iterator<Map.Entry<Integer, Integer>> iterator() {
	        	return new Iterator<Entry<Integer,Integer>>() {
		        	private final Iterator<IntIntMap.IntIntEntry> iterator = set.iterator();
					public boolean hasNext() {
						return iterator.hasNext();
					}
					public IntIntMap.IntIntEntry next() {
						return iterator.next();
					}
					public void remove() {
						iterator.remove();
					}
	        	};
	        }
	        @Override
			public boolean contains(Object o) {
	        	return set.contains(o);
	        }
	        @Override
			public int size() {
	        	return set.size();
	        }
	        @Override
	        public boolean isEmpty() {	        	
	        	return set.isEmpty();
	        }
		};
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.util.intcoll.IntMap#intEntrySet()
	 */
	public Set<IntMap.IntEntry<Integer>> intEntrySet() {
		return new AbstractSet<IntMap.IntEntry<Integer>>() {
			private final Set<IntIntMap.IntIntEntry> set = intIntEntrySet();
			@Override
			public Iterator<IntMap.IntEntry<Integer>> iterator() {
	        	return new Iterator<IntMap.IntEntry<Integer>>() {
		        	private final Iterator<IntIntMap.IntIntEntry> iterator = set.iterator();
					public boolean hasNext() {
						return iterator.hasNext();
					}
					public IntIntMap.IntIntEntry next() {
						return iterator.next();
					}
					public void remove() {
						iterator.remove();
					}
	        	};
	        }
	        @Override
			public boolean contains(Object o) {
	        	return set.contains(o);
	        }
	        @Override
			public int size() {
	        	return set.size();
	        }
	        @Override
	        public boolean isEmpty() {	        	
	        	return set.isEmpty();
	        }
		};
	}
	
    public static class SimpleIntIntEntry implements IntIntMap.IntIntEntry {
    	private final int key;
    	private int value;

        public SimpleIntIntEntry(int key, int value) {
        	this.key 	= key;
        	this.value	= value;
        }

        public Integer getKey() {
            return Integer.valueOf(key);
        }
        
        public int getIntKey() {
        	return key;
        }

        public Integer getValue() {
            return Integer.valueOf(value);
        }
        public int getIntValue() {
        	return value;
        }
        
        public Integer setValue(Integer newValue) {
        	return Integer.valueOf(setValue(newValue.intValue()));
        }
        public int setValue(int newValue) {
        	final int old = value;
        	value = newValue;
        	return old;
        }
    
        @Override
		public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
            final Object eKey = e.getKey();
            final Object eVal = e.getValue();
            if (eKey instanceof Integer && eVal instanceof Integer) {
            	return ((Integer)eKey).intValue() == getIntKey() && 
            		((Integer)eVal).intValue() == getIntValue();
            }
            return false;
        }
    
        @Override
		public int hashCode() {
            return key ^ value;
        }
    
        @Override
		public String toString() {
            return getIntKey() + "=" + getValue();
        }

    }
    public static class ImmutableIntIntEntry implements IntIntMap.IntIntEntry {
    	private final int key;
    	private final int value;

        public ImmutableIntIntEntry(int key, int value) {
        	this.key 	= key;
        	this.value	= value;
        }

        public Integer getKey() {
            return Integer.valueOf(key);
        }
        
        public int getIntKey() {
        	return key;
        }

        public Integer getValue() {
            return Integer.valueOf(value);
        }
        public int getIntValue() {
        	return value;
        }
        
        public Integer setValue(Integer newValue) {
        	throw new UnsupportedOperationException("immutable entry");
        }
        public int setValue(int newValue) {
        	throw new UnsupportedOperationException("immutable entry");
        }
    
        @Override
		public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
            final Object eKey = e.getKey();
            final Object eVal = e.getValue();
            if (eKey instanceof Integer && eVal instanceof Integer) {
            	return ((Integer)eKey).intValue() == getIntKey() && 
            		((Integer)eVal).intValue() == getIntValue();
            }
            return false;
        }
    
        @Override
		public int hashCode() {
            return key ^ value;
        }
    
        @Override
		public String toString() {
            return getIntKey() + "=" + getValue();
        }

    }

    /* (non-Javadoc)
     * @see ch.javasoft.util.intcoll.IntMap#put(int, java.lang.Object)
     */
    public Integer put(int key, Integer value) {
    	return put(key, value.intValue());
    }
    /* (non-Javadoc)
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Integer put(Integer key, Integer value) {
    	return put(key.intValue(), value.intValue());
    }
    /* (non-Javadoc)
     * @see ch.javasoft.util.intcoll.IntIntMap#putAll(ch.javasoft.util.intcoll.IntIntMap)
     */
    public void putAll(IntIntMap map) {
    	final IntIterator keyIt = map.keySet().iterator();
    	while (keyIt.hasNext()) {
    		final int key = keyIt.nextInt();
    		put(key, map.getInt(key));
    	}
    }
    /* (non-Javadoc)
     * @see ch.javasoft.util.intcoll.IntMap#putAll(ch.javasoft.util.intcoll.IntMap)
     */
    public void putAll(IntMap<? extends Integer> map) {
    	if (map instanceof IntIntMap) {
    		putAll((IntIntMap)map);
    		return;
    	}
    	final IntIterator keyIt = map.keySet().iterator();
    	while (keyIt.hasNext()) {
    		final int key = keyIt.nextInt();
    		put(key, map.get(key).intValue());
    	}
    }
    /* (non-Javadoc)
     * @see java.util.AbstractMap#putAll(java.util.Map)
     */
    @SuppressWarnings("unchecked")
	@Override
    public void putAll(Map<? extends Integer, ? extends Integer> map) {
    	if (map instanceof IntIntMap) {
    		putAll((IntIntMap)map);
    	}
    	else if (map instanceof IntMap) {
    		putAll((IntMap<Integer>)map);
    	}
    	else {
    		super.putAll(map);
    	}
    }
	/* (non-Javadoc)
	 * @see java.util.AbstractMap#remove(java.lang.Object)
	 */
	@Override
	public Integer remove(Object key) {
		if (key instanceof Integer) {
			return remove(((Integer)key).intValue());
		}
		return null;
	}
	
	@Override
	abstract public IntCollection values();
	@Override
	abstract public IntSet keySet();
}
