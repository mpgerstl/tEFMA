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
package ch.javasoft.jbase;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ch.javasoft.jbase.util.AbstractDataInput;
import ch.javasoft.jbase.util.AbstractDataOutput;
import ch.javasoft.jbase.util.UnsupportedOperationException;

/*
 * TIME values
 * 	col 1: insert
 *  col 2: update or ordered read
 *  col 3: read or unordered read
 * 
========

without buffering

testInt (ms) = 718, 2218, 3979
testBigInteger (ms) = 1263, 911, 1050
testBigIntegerCache (ms) = 2332, 2514, 1086, 162
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 872, 3020, 377
testRemoveDeleteFileBigInteger (ms) = 4, 29
testRemoveAllBigInteger (ms) = 98, 2
testSetBigInteger (ms) = 907, 2420, 776
testSetBigInteger (ms) = 860, 1555, 798

testInt (ms) = 587, 615, 607
testBigInteger (ms) = 1059, 959, 945
testBigIntegerCache (ms) = 905, 1468, 2620, 158
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 954, 2974, 362
testRemoveDeleteFileBigInteger (ms) = 4, 1
testRemoveAllBigInteger (ms) = 95, 3
testSetBigInteger (ms) = 853, 2390, 753
testSetBigInteger (ms) = 901, 1609, 770

========
4K

16x256
testInt (ms) = 463, 549, 773
testBigInteger (ms) = 751, 446, 1185
testBigIntegerCache (ms) = 460, 483, 339, 150
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 433, 1843, 154
testRemoveDeleteFileBigInteger (ms) = 4, 1
testRemoveAllBigInteger (ms) = 48, 2
testSetBigInteger (ms) = 392, 6096, 311
testSetBigInteger (ms) = 384, 1955, 365

64x64
testInt (ms) = 504, 591, 742
testBigInteger (ms) = 1321, 616, 1241
testBigIntegerCache (ms) = 1204, 579, 501, 150
testRemoveInt (ms) = 0, 0, 1
testRemoveBigInteger (ms) = 1115, 2340, 237
testRemoveDeleteFileBigInteger (ms) = 4, 1
testRemoveAllBigInteger (ms) = 120, 3
testSetBigInteger (ms) = 1132, 2168, 529
testSetBigInteger (ms) = 1056, 2109, 477

128x32
testInt (ms) = 655, 609, 724
testBigInteger (ms) = 2120, 751, 1227
testBigIntegerCache (ms) = 1857, 811, 674, 237
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 1810, 2882, 336
testRemoveDeleteFileBigInteger (ms) = 3, 1
testRemoveAllBigInteger (ms) = 205, 3
testSetBigInteger (ms) = 1845, 2333, 656
testSetBigInteger (ms) = 1817, 2351, 742

32x128
testInt (ms) = 411, 569, 754
testBigInteger (ms) = 1427, 509, 1173
testBigIntegerCache (ms) = 665, 537, 406, 150
testRemoveInt (ms) = 0, 0, 1
testRemoveBigInteger (ms) = 665, 1963, 180
testRemoveDeleteFileBigInteger (ms) = 3, 1
testRemoveAllBigInteger (ms) = 72, 3
testSetBigInteger (ms) = 601, 2384, 360
testSetBigInteger (ms) = 682, 1955, 425

========

========
16K

256x64
testInt (ms) = 502, 579, 716
testBigInteger (ms) = 1318, 601, 1188
testBigIntegerCache (ms) = 1203, 578, 491, 155
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 1124, 2125, 229
testRemoveDeleteFileBigInteger (ms) = 3, 1
testRemoveAllBigInteger (ms) = 112, 3
testSetBigInteger (ms) = 1085, 1844, 510
testSetBigInteger (ms) = 1023, 2080, 459

64x256
testInt (ms) = 455, 547, 759
testBigInteger (ms) = 734, 455, 1151
testBigIntegerCache (ms) = 424, 432, 344, 157
testRemoveInt (ms) = 0, 0, 1
testRemoveBigInteger (ms) = 432, 1662, 143
testRemoveDeleteFileBigInteger (ms) = 3, 1
testRemoveAllBigInteger (ms) = 48, 2
testSetBigInteger (ms) = 406, 2834, 277
testSetBigInteger (ms) = 387, 1905, 360

========
64K

256x256
testInt (ms) = 439, 561, 702
testBigInteger (ms) = 714, 434, 1127
testBigIntegerCache (ms) = 472, 518, 348, 154
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 434, 1529, 124
testRemoveDeleteFileBigInteger (ms) = 4, 1
testRemoveAllBigInteger (ms) = 46, 3
testSetBigInteger (ms) = 372, 1985, 254
testSetBigInteger (ms) = 376, 1768, 333

64x1024
testInt (ms) = 428, 475, 990
testBigInteger (ms) = 460, 410, 1297
testBigIntegerCache (ms) = 198, 391, 306, 155
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 201, 1538, 95
testRemoveDeleteFileBigInteger (ms) = 5, 1
testRemoveAllBigInteger (ms) = 27, 3
testSetBigInteger (ms) = 244, 2665, 249
testSetBigInteger (ms) = 233, 1998, 272

1024x256
testInt (ms) = 584, 593, 633
testBigInteger (ms) = 1288, 608, 1149
testBigIntegerCache (ms) = 1127, 636, 519, 157
testRemoveInt (ms) = 0, 0, 0
testRemoveBigInteger (ms) = 1105, 1983, 210
testRemoveDeleteFileBigInteger (ms) = 4, 1
testRemoveAllBigInteger (ms) = 112, 2
testSetBigInteger (ms) = 1098, 1699, 470
testSetBigInteger (ms) = 1047, 1946, 421

128x512
testInt (ms) = 353, 565, 725
testBigInteger (ms) = 726, 421, 1098
testBigIntegerCache (ms) = 333, 328, 288, 156
testRemoveInt (ms) = 0, 1, 0
testRemoveBigInteger (ms) = 251, 1429, 105
testRemoveDeleteFileBigInteger (ms) = 4, 1
testRemoveAllBigInteger (ms) = 34, 4
testSetBigInteger (ms) = 304, 2096, 249
testSetBigInteger (ms) = 289, 1809, 250
 */
/**
 * The <tt>BufferedRandomAccessPersister</tt> caches data in a hash table, and
 * delegates the real i/o operations to an underlying delegate
 * {@link RandomAccessPersister}.
 * <p>
 * Note that all cache-access is thread safe. If the caller guarantees that
 * atomic read and write operations do not overlap, the class is thread-safe,
 * even if concurrent read operations are overlapping.
 */
public class BufferedRandomAccessPersister implements RandomAccessPersister {
	
	private final RandomAccessPersister	delegate;
	private final int 					tableSize;
	private final int 					entrySize;
	private final DataInput				dataInput;
	private final DataOutput			dataOutput;//null for read-copy
	private final ReadWriteLock			lock;

	//hash table entry
	private final static class TableEntry {
		public TableEntry(long start, int size) {
			this.start	= start;
			this.buffer	= new byte[size];
			this.length = size;
		}
		public final long 	start;	//start byte position
		public final byte[]	buffer;
		public volatile int length;
		public volatile boolean dirty;
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + start + ".." + (start + length - 1) + "={" +
				(length == 0 ? "" : (buffer[0] + ".." + buffer[length - 1]) + "}]");
		}
	}
	private final AtomicReferenceArray<TableEntry> cache;
	
	private final ThreadLocal<long[]> pos = new ThreadLocal<long[]>() {
		@Override
		protected long[] initialValue() {return new long[]{0};}
	};
	
	/**
	 * Constructor using a {@link RandomAccessFilePersistor} as delegate, and
	 * a cache acording to the specified specifications. 
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>the maximum cache size is <tt>tableSize * entrySize</tt></li>
	 * <li>larger <tt>tableSize</tt> has positive influence on all functions,
	 * 		but especially random read access can profit</li>
	 * <li>larger <tt>entrySize</tt> has positive influence for sequential write 
	 * 		operations, but might have negative affect in combination with
	 * 		small <tt>tableSize</tt> and random modifying operations</li>
	 * </ul>
	 * <p>
	 * <b>Recommended values:</b>
	 * <ul>
	 * <li><tt>tableSize = 64, entrySize = 256</tt>: good setting for general
	 * 		purpose use, maximum cache size is 16K</li>
	 * <li><tt>tableSize = 256, entrySize = 64</tt>: faster read operations, on
	 * 		the price of slower writing, maximum cache size is 16K</li>
	 * </ul>
	 * 
	 * @param file			file to create a {@link RandomAccessFilePersistor}
	 * @param tableSize		number of entries in the hash table
	 * @param entrySize		buffer byte length of a single hash table entry
	 * @throws FileNotFoundException if the specified file is not found
	 */
	public BufferedRandomAccessPersister(File file, int tableSize, int entrySize) throws FileNotFoundException {
		this(new RandomAccessFilePersistor(file), tableSize, entrySize);
	}
	/**
	 * Constructor with delegate persistor and cache specifications. 
	 * <p>
	 * <b>Notes:</b>
	 * <ul>
	 * <li>the maximum cache size is <tt>tableSize * entrySize</tt></li>
	 * <li>larger <tt>tableSize</tt> has positive influence on all functions,
	 * 		but especially random read access can profit</li>
	 * <li>larger <tt>entrySize</tt> has positive influence for sequential write 
	 * 		operations, but might have negative affect in combination with
	 * 		small <tt>tableSize</tt> and random modifying operations</li>
	 * </ul>
	 * <p>
	 * <b>Recommended values:</b>
	 * <ul>
	 * <li><tt>tableSize = 64, entrySize = 256</tt>: good setting for general
	 * 		purpose use, maximum cache size is 16K</li>
	 * <li><tt>tableSize = 256, entrySize = 64</tt>: faster read operations, on
	 * 		the price of slower writing, maximum cache size is 16K</li>
	 * </ul>
	 * 
	 * @param delegate		delegate persistor, does the real i/o operations
	 * @param tableSize		number of entries in the hash table
	 * @param entrySize		buffer byte length of a single hash table entry
	 */
	public BufferedRandomAccessPersister(RandomAccessPersister delegate, int tableSize, int entrySize) {
		this(delegate, tableSize, entrySize, false, new AtomicReferenceArray<TableEntry>(tableSize), new ReentrantReadWriteLock());
	}
	private BufferedRandomAccessPersister(RandomAccessPersister delegate, int tableSize, int entrySize, boolean readCopy, AtomicReferenceArray<TableEntry> cache, ReadWriteLock lock) {
		if (delegate == null) throw new NullPointerException("delegate cannot be null");
		if (tableSize <= 0) throw new IllegalArgumentException("table size must be positive: " + tableSize);
		if (entrySize <= 0) throw new IllegalArgumentException("entry size must be positive: " + entrySize);
		this.delegate		= delegate;
		this.tableSize		= tableSize;
		this.entrySize		= entrySize;
		this.cache			= cache;
		this.dataInput		= new BufferedDataInput();
		this.dataOutput		= readCopy ? null : new BufferedDataOutput();
		this.lock			= lock;
	}

	public DataInput getInput() throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");
		return dataInput;
	}

	public DataOutput getOutput() throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");
		return dataOutput;
	}

	public long getPosition() throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");
		return pos.get()[0];
	}

	public void setPosition(long bytePos) throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");
		pos.get()[0] = bytePos;
	}

	public void setLength(long byteLength) throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");
		lock.writeLock().lock();
		try {
			for (int i = 0; i < cache.length(); i++) {
				final TableEntry e = cache.get(i);
				if (e != null) {
					if (e.start >= byteLength) {
						//just remove entry
						cache.set(i, null);
					}
					else {
						final long newlen = byteLength - e.start;
						if (newlen < e.length) {
							//resize the entry
							e.length = (int)newlen;
							e.dirty  = true;
						}
					}
				}
			}
		}
		finally {
			lock.writeLock().unlock();
		}
		delegate.setLength(byteLength);
	}

	public void flush() throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");
		lock.readLock().lock();
		try {
			for (int i = 0; i < cache.length(); i++) {
				final TableEntry e = cache.get(i);
				if (e != null && e.dirty) {
					flushDirtyEntry(delegate, e);
				}
			}
		}
		finally {
			lock.readLock().unlock();
		}
		delegate.flush();
	}
	
	/**
	 * PRECONDITION: read or write lock held
	 */
	protected void flushDirtyEntry(RandomAccessPersister delegate, TableEntry entry) throws IOException {
		synchronized (delegate) {
			if (entry.dirty) {//could already have been saved by someone else
				delegate.setPosition(entry.start);
				delegate.getOutput().write(entry.buffer, 0, entry.length);
				entry.dirty = false;//could be used by someone else
			}
		}
	}

	public void close(boolean erase) throws IOException {
		if (erase && dataOutput == null) {
			throw new UnsupportedOperationException("unmodifyable read copy");    							
		}
		if (pos.get() != null) {
			pos.set(null);
			lock.readLock().lock();
			try {
				for (int i = 0; i < cache.length(); i++) {
					final TableEntry e;
					if (dataOutput != null) {
						//clear cache if this is not a read copy
						e = cache.getAndSet(i, null);
					}
					else {
						e = cache.get(i);
					}
					if (e != null && e.dirty) {
						flushDirtyEntry(delegate, e);
					}
				}
			}
			finally {
				lock.readLock().unlock();					
			}
			delegate.close(erase);
		}
	}

	public RandomAccessPersister createReadCopy(final ReadWriteLock lock) throws IOException {
		flush();
		final RandomAccessPersister copy = new BufferedRandomAccessPersister(
			delegate.createReadCopy(lock), tableSize, entrySize, true, 
			cache, BufferedRandomAccessPersister.this.lock) {
			@Override
			public DataOutput getOutput() throws IOException {
				throw new UnsupportedOperationException("read only copy, data output not supported");
			}
			@Override
			public void setLength(long byteLength) throws IOException {
				throw new UnsupportedOperationException("read only copy, setting length not supported");
			}
			@Override
			public void flush() throws IOException {
				throw new UnsupportedOperationException("read only copy, flush not supported");
			}
			@Override
			protected void flushDirtyEntry(RandomAccessPersister copyDelegate, TableEntry entry) throws IOException {
				super.flushDirtyEntry(BufferedRandomAccessPersister.this.delegate, entry);
			}
		};
		copy.setPosition(getPosition());
		return copy;
	}
	
	/**
	 * PRECONDITION: read or write lock held
	 */
	private TableEntry loadEntry(long start, int tInd, boolean forWrite) throws IOException {
		//read the desired entry
		final TableEntry e = new TableEntry(start, entrySize);
		delegate.setPosition(start);
		try {
			delegate.getInput().readFully(e.buffer);
		}
		catch (EOFException ex) {
			//end of stream, read byte by byte
			delegate.setPosition(start);
			for (int i = 0; i < entrySize; i++) {
				try {
					e.buffer[i] = delegate.getInput().readByte();
				}
				catch (EOFException ex2) {
					e.length = i;
					break;
				}
			}
		}
		if (e.length > 0 || forWrite) {
			//save the old entry, if necessary
			final TableEntry old = cache.getAndSet(tInd, e);
			if (old != null && old.dirty) {
				flushDirtyEntry(delegate, old);
			}
		}
		return e;
	}
	/**
	 * PRECONDITION: read lock held 
	 */
	private TableEntry getEntryForRead() throws IOException {
		final long index	= pos.get()[0] / entrySize;
		final long start	= index * entrySize;
		final int tInd		= hash(index) % tableSize;
		
		final TableEntry e = cache.get(tInd);
		if (e != null && e.start == start) {
			return e;
		}
		return loadEntry(start, tInd, false);
	}
	/**
	 * PRECONDITION: write lock held 
	 */
	private TableEntry getEntryForWrite() throws IOException {
		final long index	= pos.get()[0] / entrySize;
		final long start	= index * entrySize;
		final int tInd		= hash(index) % tableSize;
		
		//try first from cache
		final TableEntry e = cache.get(tInd);
		if (e != null && e.start == start) {
			return e;
		}
		return loadEntry(start, tInd, true);
	}
	
	private int read(boolean inc) throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");

		final long[] apos = pos.get();
		final int eInd	= (int)(apos[0] % entrySize);

		lock.readLock().lock();
		try {
			final TableEntry e = getEntryForRead();
			if (eInd < e.length) {
				final int v = (0x000000ff & e.buffer[eInd]);
				if (inc) apos[0]++;
				return v;
			}
		}
		finally {
			lock.readLock().unlock();
		}
		//end of stream
		return -1;
	}
	private int read(final byte[] buf, int off, int len) throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");

		final long[] apos = pos.get();
		final int eInd	= (int)(apos[0] % entrySize);
		
		lock.readLock().lock();
		try {
			final TableEntry e = getEntryForRead();
			final int count = Math.min(e.length - eInd, len);
			if (count > 0) {
				System.arraycopy(e.buffer, eInd, buf, off, count);
				apos[0] += count;
				return count;
			}
		}
		finally {
			lock.readLock().unlock();
		}
		
		//end of stream
		return -1;
	}

	private void write(int b) throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");

		final long[] apos = pos.get();
		final int eInd	= (int)(apos[0] % entrySize);
		
		lock.writeLock().lock();
		try {
			final TableEntry e = getEntryForWrite();
			if (eInd < e.buffer.length) {
				e.buffer[eInd] 	= (byte)b;
				e.dirty 		= true;
				e.length		= Math.max(e.length, eInd + 1);
				apos[0]++;
			}
			else {
				//should not happen
				throw new IOException("internal error, write after buffer length: " + eInd + " >= " + e.buffer.length);
			}
		}
		finally {
			lock.writeLock().unlock();
		}
	}
	
	private int write(byte[] buf, int off, int len) throws IOException {
		if (pos.get() == null) throw new IOException("persister already closed");

		final long[] apos = pos.get();
		final int eInd	= (int)(apos[0] % entrySize);
		
		lock.writeLock().lock();
		try {
			final TableEntry e = getEntryForWrite();
			final int count = Math.min(e.buffer.length - eInd, len); 
			if (count > 0) {
				System.arraycopy(buf, off, e.buffer, eInd, count);
				e.dirty 	= true;
				e.length	= Math.max(e.length, eInd + count);
				apos[0] += count;
				return count;
			}
			//should not happen
			throw new IOException("internal error, write after buffer length: " + eInd + " >= " + e.buffer.length);
		}
		finally {
			lock.writeLock().unlock();
		}
	}

	private class BufferedDataInput extends AbstractDataInput {
		@Override
		protected int peek() throws IOException {
			return BufferedRandomAccessPersister.this.read(false);
		}
		@Override
		protected int read() throws IOException {
			return BufferedRandomAccessPersister.this.read(true);
		}
		@Override
		protected int read(byte[] b, int off, int len) throws IOException {
			return BufferedRandomAccessPersister.this.read(b, off, len);
		}
		@Override
		public int skipBytes(int n) throws IOException {
			//TODO this is very inefficient, but ok for now
			int count = 0;
			while (count < n) {
				final int b = read();
				if (b < 0) return count;
				count++;
			}
			return count;
		}
	}
	
	private class BufferedDataOutput extends AbstractDataOutput {
		public void write(int b) throws IOException {
			BufferedRandomAccessPersister.this.write(b);
		}
		public void write(byte[] b, int off, int len) throws IOException {
	        int n = 0;
	    	do {
				int count = BufferedRandomAccessPersister.this.write(b, off + n, len - n);
	    	    if (count < 0)
	    		throw new EOFException();
	    	    n += count;
	    	} while (n < len);
		}
	}
	
    /**
     * Copied from {@link HashMap#hash(int)} 
     * (Java 6, or <code>HashMap#newHash(int)</code> in Java 5), merged with
     * {@link Long#hashCode()}.
     * <p>
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits.
     */
    private static int hash(long value) {    	
    	int h = (int)(value ^ (value >>> 32));
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

}
