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
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReadWriteLock;

import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.jbase.concurrent.Stateful;
import ch.javasoft.jbase.util.UnsupportedOperationException;

/**
 * The <code>FixedWidthTable</code> stores entities of known, fixed
 * size. 
 * <p>
 * To store the data, a {@link RandomAccessPersister} is used, usually
 * operating on a {@link RandomAccessFile}. If files are used for storage,
 * a single file is created. Since entities have fixed width. offsets to
 * any entity can be computed easily.
 * <p>
 * Note that fixed width tables are not thread safe. However, a thread safe 
 * table for concurrent use is possible by using this table together with
 * {@link ConcurrentTable}.
 */
public class FixedWidthTable<E> implements Table<E>, Stateful {
    
    private final RandomAccessPersister		raPersister;
    private final FixedWidthMarshaller<E> 	marshaller;
    private final FixedWidthMarshaller<E> 	bufferingMarshaller;
    
    private volatile int 		size;
    private volatile boolean	needsFlush;
    
    /**
     * Constructor for internal use and subclasses only, called from
     * {@link #open(File, FixedWidthMarshaller) open(..)} and
     * {@link #create(File, FixedWidthMarshaller) create(..)} methods.
     * 
     * @param raPersister	the persister to use
     * @param marshaller	the marshaller
     * @param size			the number of table entries
     */
    protected FixedWidthTable(RandomAccessPersister raPersister, final FixedWidthMarshaller<E> marshaller, int size) {
        this.raPersister   = raPersister;
        this.marshaller    = marshaller;
        this.size          = size;
        
        bufferingMarshaller = new FixedWidthMarshaller<E>() {
        	final int byteWidth = marshaller.getByteWidth();
            private final ByteArray buffer = new ByteArray();
        	public int getByteWidth() {
        		return byteWidth;
        	}
        	public E readFrom(DataInput in) throws IOException {
        		buffer.reset();
        		buffer.readBytesFrom(in, byteWidth);
        		return marshaller.readFrom(buffer.getDataInputStream());
        	}
        	public void writeTo(E entity, DataOutput out) throws IOException {
        		buffer.reset();
        		marshaller.writeTo(entity, buffer.getDataOutputStream());
        		buffer.writeTo(out);
        	}
        };
    }
    
    /**
     * Reads the table byte width from file using the given persister. The
     * byte with is saved at position 0L in the file. 
     */
    public static int readByteWidth(RandomAccessPersister raPersister) throws IOException {
        raPersister.setPosition(0L);
        final DataInput in = raPersister.getInput();
        /*byteWidth   = */return in.readInt();
        /*size        = in.readInt();*/        
    }
    /**
     * Reads the table size, that is, the number of entries, from file using the 
     * given persister. The table size is saved at position 4L in the file. 
     */
    public static int readSize(RandomAccessPersister raPersister) throws IOException {
        raPersister.setPosition(0L);
        final DataInput in = raPersister.getInput();
        /*byteWidth   = */in.readInt();
        /*size        = */ return in.readInt();        
    }
    /**
     * Open the given table for read-only access, using no cache
     * 
     * @param <En>			the entry type
     * @param file			the table file to open
     * @param marshaller	the marshaller
     * @return	the table without cache for reading only
     * 
     * @throws IOException	if an i/o exception occurs
     */
    public static <En> FixedWidthTable<En> open(File file, FixedWidthMarshaller<En> marshaller) throws IOException {
        return open(new RandomAccessFilePersistor(file, new RandomAccessFile(file, "r")), marshaller);
    }
    /**
     * Open the given table for read-only access, using caching
     * 
     * @param <En>				the entry type
     * @param file				the table file to open
     * @param marshaller		the marshaller
     * @param cacheTableSize	Table size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @param cacheEntrySize	Table entry size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @return	the table for reading only
     * 
     * @throws IOException	if an i/o exception occurs
     */
    public static <En> FixedWidthTable<En> open(File file, FixedWidthMarshaller<En> marshaller, int cacheTableSize, int cacheEntrySize) throws IOException {
        return open(
        	new RandomAccessFilePersistor(file, new RandomAccessFile(file, "r")),
//        	new BufferedRandomAccessPersister(
//        		new RandomAccessFilePersistor(file, new RandomAccessFile(file, "r")),
//        		cacheTableSize, cacheEntrySize), 
        	marshaller);
    }
    /**
     * Open the given table for read and write, depending on the specified
     * persister. 
     * 
     * @param <En>			the entry type
     * @param raPersister	the persister for file access
     * @param marshaller	the marshaller
     * @return	the table
     * 
     * @throws IOException	if an i/o exception occurs
     */
    public static <En> FixedWidthTable<En> open(RandomAccessPersister raPersister, FixedWidthMarshaller<En> marshaller) throws IOException {
        final FixedWidthTable<En> tbl = new FixedWidthTable<En>(raPersister, marshaller, 0);
        tbl.readHeader();
        tbl.needsFlush = false;
        return tbl;
    }
    /**
     * Creates the given table for read and write, using no cache 
     * 
     * @param <En>			the entry type
     * @param file			the table file to create
     * @param marshaller	the marshaller
     * @return	the table
     * 
     * @throws IOException	if an i/o exception occurs
     */
    public static <En> FixedWidthTable<En> create(File file, FixedWidthMarshaller<En> marshaller) throws IOException {
        return create(new RandomAccessFilePersistor(file), marshaller);
    }
    /**
     * Creates the given table for read and write, using caching 
     * 
     * @param <En>			the entry type
     * @param file			the table file to create
     * @param marshaller	the marshaller
     * @param cacheTableSize	Table size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @param cacheEntrySize	Table entry size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @return	the table
     * 
     * @throws IOException	if an i/o exception occurs
     */
    public static <En> FixedWidthTable<En> create(File file, FixedWidthMarshaller<En> marshaller, int cacheTableSize, int cacheEntrySize) throws IOException {
        return create(new RandomAccessFilePersistor(file), marshaller);
//        return create(new BufferedRandomAccessPersister(file, cacheTableSize, cacheEntrySize), marshaller);
    }
    /**
     * Creates the given table for read and write using the specified persister. 
     * 
     * @param <En>			the entry type
     * @param raPersister	the persister for file access
     * @param marshaller	the marshaller
     * @return	the table
     * 
     * @throws IOException	if an i/o exception occurs
     */
    public static <En> FixedWidthTable<En> create(RandomAccessPersister raPersister, FixedWidthMarshaller<En> marshaller) throws IOException {
        final FixedWidthTable<En> tbl = new FixedWidthTable<En>(raPersister, marshaller, 0);
        raPersister.setLength(0L);
        tbl.writeHeader();
        tbl.needsFlush = true;
        return tbl;
    }
    
    protected void readHeader() throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
        raPersister.setPosition(0L);
        final DataInput in = raPersister.getInput();
        final int byteWidth = in.readInt();
        if (byteWidth != marshaller.getByteWidth()) {
            throw new IOException("file byte width not consistent with marshaller byte width: " +
                    byteWidth + " != " + marshaller.getByteWidth());
        }
        size = in.readInt();
    }
    
    protected void writeHeader() throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
        raPersister.setPosition(0L);
        final DataOutput out = raPersister.getOutput();
        out.writeInt(marshaller.getByteWidth());
        out.writeInt(size);
    }
    
    public int getByteWidth() {
        return marshaller.getByteWidth();
    }
    public FixedWidthMarshaller<E> getEntityMarshaller() {
        return marshaller;
    }

    public int add(E entity) throws IOException {
        final int pos = size;
        size++;
        set(pos, entity);
        needsFlush = true;
        return pos;
    }

    public E get(int index) throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
        raPersister.setPosition(byteOffset(index));
        return bufferingMarshaller.readFrom(raPersister.getInput());
    }

    public void remove(int index) throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
        if (index != size - 1) {
            set(index, get(size - 1));
        }
        size--;
    }

    public void set(int index, E entity) throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
        raPersister.setPosition(byteOffset(index));
        bufferingMarshaller.writeTo(entity, raPersister.getOutput());
        needsFlush = true;
    }

    public void swap(int indexA, int indexB) throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
    	if (indexA == indexB) return;
        raPersister.setPosition(byteOffset(indexA));
        final E entityA = bufferingMarshaller.readFrom(raPersister.getInput());
        raPersister.setPosition(byteOffset(indexB));
        final E entityB = bufferingMarshaller.readFrom(raPersister.getInput());
        raPersister.setPosition(byteOffset(indexB));
        bufferingMarshaller.writeTo(entityA, raPersister.getOutput());
        raPersister.setPosition(byteOffset(indexA));
        bufferingMarshaller.writeTo(entityB, raPersister.getOutput());
        needsFlush = true;
    }
    
    public void removeAll() throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
    	size = 0;
        needsFlush = true;
    }

    public int size() throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
        return size;
    }
    
    public void flush() throws IOException {
    	flush(null);
    }
    private void flush(ReadWriteLock lock) throws IOException {
    	if (size < 0) throw new IOException("table already closed.");
    	if (needsFlush) {
    		if (lock != null) lock.writeLock().lock();
    		try {
		        final long pos = raPersister.getPosition();
		        writeHeader();
		        raPersister.setPosition(pos);
		        raPersister.setLength(byteSizeTotal());
		        raPersister.flush();
		        needsFlush = false;
    		}
    		finally {
    			if (lock != null) lock.writeLock().unlock();
    		}
    	}
    }
    
    public void close(boolean erase) throws IOException {
    	if (size >= 0) {
        	if (!erase) {
		        flush();
        	}
	        raPersister.close(erase);
	        size = -1;
    	}
    }
    
    @SuppressWarnings("unchecked")
    private FixedWidthMarshaller<E> createReadCopyMarshaller(final ReadWriteLock lock) throws IOException {
        return marshaller instanceof Stateful ?
            (FixedWidthMarshaller<E>)((Stateful)marshaller).createReadCopy(lock) : marshaller;
    }
    public FixedWidthTable<E> createReadCopy(final ReadWriteLock lock) throws IOException {
    	final RandomAccessPersister persistor = raPersister.createReadCopy(lock);
    	final FixedWidthMarshaller<E> rmarshaller = createReadCopyMarshaller(lock);
		return new FixedWidthTable<E>(persistor, rmarshaller, size) {
			private void sync() throws IOException {
				if (size < 0) {
					throw new IOException("table already closed.");					
				}
				if (size != FixedWidthTable.this.size) {
					if (FixedWidthTable.this.size < 0) {
						//writable (main) table is closed, so we are closed, too
						close(false);
						throw new IOException("table already closed.");
					}
					FixedWidthTable.this.flush(lock);
					readHeader();
				}    							
			}
			@Override
			public E get(int index) throws IOException {
				sync();
				return super.get(index);
			}
			@Override
			public int size() throws IOException {
				sync();
				return size;
			}
			@Override
			public int add(E entity) throws IOException {
				throw new UnsupportedOperationException("unmodifyable read copy table");    							
			}
			@Override
			public void set(int index, E entity) throws IOException {
				throw new UnsupportedOperationException("unmodifyable read copy table");    							
			}
			@Override
			public void swap(int indexA, int indexB) throws IOException {
				throw new UnsupportedOperationException("unmodifyable read copy table");    							
			}
			@Override
			public void remove(int index) throws IOException {
				throw new UnsupportedOperationException("unmodifyable read copy table");    							
			}
			@Override
			public void removeAll() throws IOException {
				throw new UnsupportedOperationException("unmodifyable read copy table");    							
			}
			@Override
			public void flush() throws IOException {
				//ignore, don't write here!
			}
			@Override
			public void close(boolean erase) throws IOException {
				if (erase) {
					throw new UnsupportedOperationException("unmodifyable read copy table");    							
				}
				super.close(false);//close the ras file
			}
			@Override
			protected void finalize() throws Throwable {
				close(false);
			}
		};
    }
    
    /**
     * Calls {@link #close(boolean)} with false argument, i.e. without erasing
     */
    @Override
    protected void finalize() throws Throwable {
    	close(false);
    }
    
    protected long byteOffset() {
        return 8;
    }
    protected long byteOffset(int index) {
        return byteOffset() + ((long)index) * getByteWidth();
    }
    protected long byteSizeTotal() {
        return byteSizeData() + byteOffset();
    }
    protected long byteSizeData() {
        return ((long)size) * getByteWidth();
    }

}
