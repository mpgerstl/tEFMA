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
package ch.javasoft.jbase.util;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.javasoft.jbase.Table;


/**
 * The <code>CachedTableSoftReference</code> is a table which caches entries of
 * an underlying tables using {@link SoftReference soft references}. Soft
 * references are possibly cleared by the garbage collector on the demand of
 * memory. 
 */
public class CachedTableSoftReference<E> implements Table<E> {

	private final int paletteSize;
	private final boolean cacheOnWrite;
	private final Map<Integer, Palette> cache = new ConcurrentHashMap<Integer, Palette>();
    private final Table<E> baseTable;
    
    @SuppressWarnings("unused")	
    private volatile Palette last = null;//just a hint for the gc that this palette should not be cleared
    
    private class Palette {
    	SoftReference<E[]> data;
    	@SuppressWarnings("unchecked")
		Palette() {
    		this.data = new SoftReference<E[]>((E[])new Object[paletteSize]);
    	}
		E get(int index) {
    		final E[] arr = data.get();
    		if (arr == null) {
    			cache.remove(Integer.valueOf(index / paletteSize));
    			return null;
    		}
   			return arr[index % paletteSize];
    	}
    	@SuppressWarnings("unchecked")
    	void set(int index, E value, boolean force) {
    		E[] arr = data.get();
    		if (arr == null) {
    			if (force) {
    				arr = (E[])new Object[paletteSize];
    				data = new SoftReference<E[]>(arr);
    			}
    			else {
        			cache.remove(Integer.valueOf(index / paletteSize));
        			return;
    			}
    		}
    		arr[index % paletteSize] = value;
    	}
    }
    public CachedTableSoftReference(Table<E> baseTable) {
        this(baseTable, 256, false);
    }
    public CachedTableSoftReference(Table<E> baseTable, int paletteSize, boolean cacheOnWrite) {
        this.baseTable 		= baseTable;
    	this.paletteSize	= paletteSize;
    	this.cacheOnWrite	= cacheOnWrite;
    }
    
	protected Palette getPalette(int index, boolean forceCreate) {
		final Integer key = Integer.valueOf(index / paletteSize);
		Palette pal = cache.get(key);
		if (pal == null && forceCreate) {
			cache.put(key, pal = new Palette());
		}
		return pal == null ? pal : (last = pal);
	}
    protected void cacheEntity(int index, E entity, boolean force) {
		final Palette pal = getPalette(index, force); 
		if (pal != null) {
			pal.set(index, entity, force);
		}
    }
    
    public int add(E entity) throws IOException {
        final int pos = baseTable.add(entity);
    	if (cacheOnWrite) {
    		cacheEntity(pos, entity, true /*force*/);
    	}
    	return pos;
    }
    
    public void removeAll() throws IOException {
    	baseTable.removeAll();
    	clearCache();
    }

    public void close(boolean erase) throws IOException {
        baseTable.close(erase);
    	clearCache();
    }

    public void flush() throws IOException {
        baseTable.flush();
    }

    public E get(int index) throws IOException {
		final Palette pal = getPalette(index, true /*force create*/);
		E entity = pal.get(index);
		if (entity == null) {
			entity = baseTable.get(index);
			pal.set(index, entity, true /*force*/);
		}
		return entity;
    }

    public void remove(int index) throws IOException {
        baseTable.remove(index);
        cacheEntity(index, null, false /*force create*/);
    }

    public void set(int index, E entity) throws IOException {
        baseTable.set(index, entity);
    	cacheEntity(index, entity, cacheOnWrite);
    }
    
    public void swap(int indexA, int indexB) throws IOException {
    	//FIXME still not thread safe!
    	baseTable.swap(indexA, indexB);
    	cacheEntity(indexA, null, false /*force*/);
    	cacheEntity(indexB, null, false /*force*/);
    	
    	//NOT thread safe:
//    	final Palette palA = getPalette(indexA, cacheOnWrite);
//    	final Palette palB = getPalette(indexB, cacheOnWrite);
//    	if (palA != null) {
//			final E entityA = palA.get(indexA);
//    		if (palB != null) {
//    			final E entityB = palB.get(indexB);
//    			palA.set(indexA, entityB, cacheOnWrite);
//    			palB.set(indexB, entityA, cacheOnWrite);
//    		}
//    		else {
//    			palA.set(indexA, null, cacheOnWrite);
//    		}
//    	}
//    	else {
//    		if (palB != null) {
//    			palB.set(indexA, null, cacheOnWrite);
//    		}
//    	}
    }

    public int size() throws IOException {
        return baseTable.size();
    }
    
    public void clearCache() {
        cache.clear();
    }
    
}

