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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

import ch.javasoft.jbase.concurrent.ConcurrentTable;
import ch.javasoft.jbase.concurrent.Stateful;
import ch.javasoft.jbase.util.UnsupportedOperationException;



/**
 * The <code>VariableWidthTablePaired</code> stores entities of unknown, 
 * variable size. A pair of tables is used to store the rows. If necessary, a 
 * pointer in the first table points to the entry in another table of sufficient
 * row width.
 * <p>
 * Multiple files are used to store the data, each file controlled by a
 * {@link FixedWidthTable}. The primary table contains raw data plus two 
 * additional indices pointing to an entry of the secondary table (one index for
 * the table, one for the row in the table). Multiple secondary tables of 
 * different sizes exist, each storing raw data plus an index back to the row
 * of the primary table (needed for deletions). The widths of the secondary 
 * tables are powers of two. The smallest possible secondary table is used
 * and created if not yet existing.
 * <p>
 * Note that variable width tables are not thread safe. However, a thread safe 
 * table for concurrent use is possible by using this table together with
 * {@link ConcurrentTable}.
 */
public class VariableWidthTable<E> implements Table<E>, Stateful {
    
    private final File                                  folder;
    private final String                                fileName;
    private final EntityMarshaller<E>                   entityMarshaller;  
    private final int									cacheTableSize;
    private final int 									cacheEntrySize;
    private volatile FixedWidthTable<FixedTableRow>     primaryTable;
    
    private final Map<File, FixedWidthTable<FixedTableRow>> secondaryTables;

    private final ByteArray byteBuffer;    		
    
    /**
     * Constructor, only for subclassing, use the static 
     * {@link #create(File, String, int, EntityMarshaller, int, int) create} and 
     * {@link #open(File, String, EntityMarshaller, int, int) open} methods to create 
     * instances.
     * 
     * @param folder            The folder containing the table files
     * @param fileName          The file name of the tables. Indexing is inserted
     *                          in front of the file ending if needed. 
     * @param entityMarshaller  The marshaller transforming entities (rows) into bytes  
     * @param cacheTableSize	Table size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @param cacheEntrySize	Table entry size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     */
    protected VariableWidthTable(File folder, String fileName, EntityMarshaller<E> entityMarshaller, int cacheTableSize, int cacheEntrySize) {
        this.folder             = folder;
        this.fileName           = fileName;
        this.entityMarshaller	= entityMarshaller;
        this.cacheTableSize		= cacheTableSize;
        this.cacheEntrySize		= cacheEntrySize;
        this.secondaryTables    = new ConcurrentHashMap<File, FixedWidthTable<FixedTableRow>>();
        this.byteBuffer			= new ByteArray();
    }

    /**
     * Opens existing variable table files.
     * 
     * @param folder            The folder containing the table files
     * @param fileName          The file name of the tables. Indexing is inserted
     *                          in front of the file ending if needed. 
     * @param entityMarshaller  The marshaller transforming entities (rows) into bytes  
     * @param cacheTableSize	Table size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @param cacheEntrySize	Table entry size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     */
    public static <En> VariableWidthTable<En> open(File folder, String fileName, EntityMarshaller<En> entityMarshaller, int cacheTableSize, int cacheEntrySize) throws IOException {
        final VariableWidthTable<En> tbl = new VariableWidthTable<En>(folder, fileName, entityMarshaller, cacheTableSize, cacheEntrySize);
        tbl.primaryTable = tbl.openPrimaryTable();
        return tbl;
    }
    /**
     * Creates new variable table files, erasing existing files if existent. 
     * <p>
     * The width of the primary table controls the frequency of single/double table 
     * accesses versus gaps in the primary table. If the primary table is wide, the 
     * majority of accesses only concern the primary table, but many rows might only 
     * use a small portion of the fixed row width. If the primary table is small, less 
     * disk space is needed, but secondary table accesses are more likely.
     * 
     * @param folder            The folder containing the table files
     * @param fileName          The file name of the tables. Indexing is inserted
     *                          in front of the file ending if needed. 
     * @param firstTableByteWidth   The byte width of the primary table, without
     *                              index widths for indices to secondary tables
     * @param entityMarshaller  The marshaller transforming entities (rows) into bytes  
     * @param cacheTableSize	Table size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     * @param cacheEntrySize	Table entry size for {@link BufferedRandomAccessPersister},
     * 							or 0 if no cache should be used
     */
    public static <En> VariableWidthTable<En> create(File folder, String fileName, int firstTableByteWidth, EntityMarshaller<En> entityMarshaller, int cacheTableSize, int cacheEntrySize) throws IOException {
        final VariableWidthTable<En> tbl = new VariableWidthTable<En>(folder, fileName, entityMarshaller, cacheTableSize, cacheEntrySize);
        eraseTableFiles(folder, fileName);
        tbl.primaryTable = tbl.createPrimaryTable(firstTableByteWidth);
        return tbl;
    }
    
    private static void eraseTableFiles(File folder, String fileName) {
    	final String prefix 	= getFileNamePrefix(fileName);
    	final String postfix	= getFileNamePostfix(fileName);
        final File[] files = folder.listFiles(new FilenameFilter() {
        	public boolean accept(File dir, String name) {
        		return name.startsWith(prefix) && name.endsWith(postfix);
        	}
        });
        for (final File file : files) {
        	file.delete();
        }        
    }
    
    private FixedWidthTable<FixedTableRow> openPrimaryTable() throws IOException {
    	return openTableFile(0, -1, false);
    }
    private FixedWidthTable<FixedTableRow> createPrimaryTable(int rawByteWidth) throws IOException {
        final int byteWidth = getTableByteWidth(0, rawByteWidth);
    	return openTableFile(0, byteWidth, true);
    }
    private FixedWidthTable<FixedTableRow> openSecondaryTable(int rawByteWidth, boolean createIfNeeded) throws IOException {
        final int byteWidth = getTableByteWidth(1, rawByteWidth);
        final File file = getTableFile(1, byteWidth);
        FixedWidthTable<FixedTableRow> tbl = secondaryTables.get(file);
        if (tbl == null) {
        	tbl = openTableFile(1, byteWidth, createIfNeeded);
        	secondaryTables.put(file, tbl);
        }
        return tbl;
    }
    private RandomAccessPersister createRandomAccessPersister(File file) throws FileNotFoundException {
    	if (cacheTableSize > 0 && cacheEntrySize > 0) {
    		return new BufferedRandomAccessPersister(file, cacheTableSize, cacheEntrySize);
    	}
    	return new RandomAccessFilePersistor(file);
    }
    protected FixedWidthTable<FixedTableRow> openTableFile(int tableIndex, int byteWidth, boolean createIfNeeded) throws IOException {
        final File file = getTableFile(tableIndex, byteWidth);
        if (file.exists()) {
            final RandomAccessPersister rap = createRandomAccessPersister(file);
            final int width = FixedWidthTable.readByteWidth(rap);
            if (byteWidth >= 0 && width != getTotalByteWidth(tableIndex, byteWidth)) {
            	throw new IOException("table file has unexpected byte width, expected " + getTotalByteWidth(tableIndex, byteWidth) + " but found " + width + " for file " + file.getAbsolutePath());
            }
            final FixedTableRow row = FixedTableRow.getByTotalByteWidth(width, getTableIndexCount(tableIndex));
            return FixedWidthTable.open(rap, row);
        }
        else {
        	if (createIfNeeded) {
                final RandomAccessPersister rap = createRandomAccessPersister(file);
//                final int width = getTableByteWidth(tableIndex, byteWidth);
                final int width = byteWidth;
                final FixedTableRow row = FixedTableRow.getByByteArrayLength(width, getTableIndexCount(tableIndex));
                final FixedWidthTable<FixedTableRow> tbl = FixedWidthTable.create(rap, row);
                tbl.flush();//ensure that the byte width is flushed
                return tbl;
        	}
        	else {
        		throw new IOException("no such table: " + file.getAbsolutePath());
        	}
        }
    }
    
    protected static String getFileNamePrefix(String fileName) {
        final int lastDot = fileName.lastIndexOf('.');
        return lastDot < 0 ? fileName : fileName.substring(0, lastDot);
    }
    protected static String getFileNamePostfix(String fileName) {
        final int lastDot = fileName.lastIndexOf('.');
        return lastDot < 0 ? "" : fileName.substring(lastDot);
    }
    protected File getTableFile(int tableIndex, int byteWidth) {
    	if (tableIndex == 0) {
    		return new File(folder, fileName); 
    	}
        final int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return new File(folder, fileName + byteWidth);
        }
        return new File(folder, fileName.substring(0, lastDot) + byteWidth + fileName.substring(lastDot));
    }
    
    protected static int getTotalByteWidth(int tableIndex, int byteWidth) {
    	return byteWidth + 4*getTableIndexCount(tableIndex);
    }
    protected static int getTableIndexCount(int tableIndex) {
    	return tableIndex == 0 ? 2 : 1;
    }   
    
    /**
     *
     */
    protected static int getTableByteWidth(int tableIndex, int rawByteWidth) throws IOException {
    	int width;
    	if (tableIndex == 0) {
    		// we want next larger size dividable by 4
    		width = 4 * ((rawByteWidth + 3) / 4); 
    	}
    	else {
    		// we want powers of two
    		width = 4;
        	while (width < rawByteWidth && width > 0) {
                width <<= 1;
            }
    	}
     	if (width < rawByteWidth) {
     		//overflow
     		throw new IOException("table width overflow: " + rawByteWidth);
     	}
        return width;
    }
    
    // primary[0..n-3]: raw data
    // primary[n-2]:    size of secondary table (power of 2), or 0 if data fits 
    //					into primary table (identifies secondary table)
    // primary[n-1]:    index in secondary table, or 0 if data fits into primary
    
    // secondary[0..n-2]:	raw data	
    // secondary[n-1]:		index in primary table	
    
    public int add(E entity) throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");

        byteBuffer.reset();
//        final DataOutputStream das = new DataOutputStream(byteBuffer);
        final DataOutputStream das = byteBuffer.getDataOutputStream();
        entityMarshaller.writeTo(entity, das);
        das.flush();
        
        // write to primary table
        FixedTableRow row0 = (FixedTableRow)primaryTable.getEntityMarshaller();
        row0.getBytesFrom(byteBuffer);
        
        final int remainingBytes = byteBuffer.getLength();
        if (remainingBytes != 0) {
        	final FixedWidthTable<FixedTableRow> secondaryTable = openSecondaryTable(
        			remainingBytes, true /*createIfNeeded*/);
            FixedTableRow row1 = (FixedTableRow)secondaryTable.getEntityMarshaller();
            row1.getBytesFrom(byteBuffer);
            row1.setInt(0, primaryTable.size());
            if (!byteBuffer.isEmpty()) {
            	throw new IOException("internal error, buffer not empty after flushing to secondary table"); 
            }
            final int index1 = secondaryTable.size();
            secondaryTable.add(row1);
            row0.setInt(0, row1.getByteArrayLength());
            row0.setInt(1, index1);
//            if (index1 < 10 && secondaryTable.getByteWidth() == 4) {
//                System.out.println("w[" + primaryTable.size() + 
//                        "/" + secondaryTable.size() + "] = " + row0.toString() + row1);
//            }
        }
        else {
        	row0.setInt(0, 0);
        	row0.setInt(1, 0);
        }
        return primaryTable.add(row0);
    }

    public void close(boolean erase) throws IOException {    	
        if (primaryTable != null) {
	        for (final FixedWidthTable<FixedTableRow> tbl : secondaryTables.values()) {
	        	tbl.close(erase);
	        }
	        primaryTable.close(erase);
	        secondaryTables.clear();
	        primaryTable = null;
        }
    }

    public void flush() throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");
        for (final FixedWidthTable<FixedTableRow> tbl : secondaryTables.values()) {
        	tbl.flush();
        }
        primaryTable.flush();
    }
    /**
     * Calls {@link #close(boolean)} with false argument, i.e. without erasing
     */
    @Override
    protected void finalize() throws Throwable {
    	close(false);
    }
    
    @SuppressWarnings("unchecked")
    private EntityMarshaller<E> createReadCopyMarshaller(final ReadWriteLock lock) throws IOException {
        return entityMarshaller instanceof Stateful ?
            (EntityMarshaller<E>)((Stateful)entityMarshaller).createReadCopy(lock) : entityMarshaller;
    }
    public VariableWidthTable<E> createReadCopy(final ReadWriteLock lock) throws IOException {
        final EntityMarshaller<E> marshaller = createReadCopyMarshaller(lock);
    	final VariableWidthTable<E> tbl = new VariableWidthTable<E>(folder, fileName, marshaller, cacheTableSize, cacheEntrySize) {
			@Override
		    protected FixedWidthTable<FixedTableRow> openTableFile(int tableIndex, int byteWidth, boolean createIfNeeded) throws IOException {
				if (createIfNeeded) {
					throw new IOException("internal error: read only table");
				}
				boolean refetchReadLock = false;
				try {
					lock.readLock().unlock();
					refetchReadLock = true;
				}
				catch (IllegalMonitorStateException ex) {
					//ignore					
				}
				lock.writeLock().lock();
				try {
					VariableWidthTable.this.flush();//TODO optimize, do not always do this
					final FixedWidthTable<FixedTableRow> main;				
					if (tableIndex == 0) {
						main = VariableWidthTable.this.primaryTable;
					}
					else {
				        //final int byteWidth = getTableByteWidth(1, rawByteWidth);
						main = VariableWidthTable.this.openSecondaryTable(byteWidth, false /*createIfNeeded*/);
					}
					return main.createReadCopy(lock);
				}
				finally {
					lock.writeLock().unlock();
					if (refetchReadLock) {
						lock.readLock().lock();
					}
				}
			}
			private void sync() throws IOException {
				if (VariableWidthTable.this.primaryTable == null) {
					//main editable table is closed, so we are closed, too
					close(false);
					throw new IOException("table already closed");
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
				return super.size();
			}
			@Override
			public int add(E entity) throws IOException {
				throw new IOException("unmodifyable read copy table");    							
			}
			@Override
			public void set(int index, E entity) throws IOException {
				throw new IOException("unmodifyable read copy table");    							
			}
			@Override
			public void swap(int indexA, int indexB) throws IOException {
				throw new IOException("unmodifyable read copy table");    							
			}
			@Override
			public void remove(int index) throws IOException {
				throw new IOException("unmodifyable read copy table");    							
			}
			@Override
			public void removeAll() throws IOException {
				throw new IOException("unmodifyable read copy table");    							
			}
			@Override
			public void flush() throws IOException {
				//ignore, don't write anything here
			}
			@Override
			public void close(boolean erase) throws IOException {
				if (erase) {
					throw new UnsupportedOperationException("unmodifyable read copy table");    							
				}
				super.close(false);//close the tables
			}
			@Override
			protected void finalize() throws Throwable {
				close(false);
			}
		};
		tbl.primaryTable = tbl.openPrimaryTable();
		return tbl;
    }
    
    public E get(int index) throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");
        
        byteBuffer.reset();
        
        final FixedTableRow row0 = primaryTable.get(index);
        row0.putBytesTo(byteBuffer);
        
        final int tbl1 = row0.getInt(0);
        final int ind1 = row0.getInt(1);
        if (tbl1 != 0) {
        	final FixedWidthTable<FixedTableRow> secondaryTable = openSecondaryTable(
        			tbl1, false /*createIfNeeded*/);
            final FixedTableRow row1 = secondaryTable.get(ind1);
            row1.putBytesTo(byteBuffer);
//            if (ind1 < 10) {
//                System.out.println("r[" + primaryTable.size() + 
//                        "/" + secondaryTable.size() + "] = " + row0.toString() + row1);
//            }
        }
        
        return entityMarshaller.readFrom(byteBuffer.getDataInputStream());
    }

    public void remove(int index) throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");
        
        //just for consistent naming throughout this method
        final FixedWidthTable<FixedTableRow> fwt0 = primaryTable;
        final int ind0 = index;
        
        FixedTableRow row0 = (FixedTableRow)fwt0.getEntityMarshaller();
        row0.setIndexOnlyMode(true);
        try {
        	row0 = fwt0.get(ind0);
        }
        finally {
        	row0.setIndexOnlyMode(false);
        }
        
        final int tbl1 = row0.getInt(0);
        final int ind1 = row0.getInt(1);
        
        //NOTE: the following actions are performed
        //1a) delete entry in primary table, which moves the last entry to the
        //    gap position
        //1b) if the moved entry has a corresponding entry in the secondary 
        //    table, its back pointer has to be updated
        //2a) if the deleted entry had an entry in the secondary table, also
        //    delete it, again moving the last table entry to the gap position
        //2b) the primary entry corresponding to the moved entry has to be
        //    updated, too
        //2c) delete secondary table if it is empty
        
        //1a) delete primary
        fwt0.remove(ind0);
        final int last0 = primaryTable.size();
        
        //1b) update secondary of moved
        if (ind0 != last0) {
            row0.setIndexOnlyMode(true);
            try {
            	row0 = fwt0.get(ind0);
            }
            finally {
            	row0.setIndexOnlyMode(false);
            }
            final int tbl2 = row0.getInt(0);
            final int ind2 = row0.getInt(1);
            
            if (tbl2 != 0) {
                final FixedWidthTable<FixedTableRow> fwt2 = openSecondaryTable(
            			tbl2, false /*createIfNeeded*/);
                final FixedTableRow row2 = (FixedTableRow)fwt2.getEntityMarshaller();
                row2.setIndexOnlyMode(true);
                try {
                    row2.setInt(0, ind0);
                	fwt2.set(ind2, row2);
                }
                finally {
                	row2.setIndexOnlyMode(false);
                }
            }
        }
        
        //2a) delete secondary
        if (tbl1 != 0) {
            final FixedWidthTable<FixedTableRow> fwt1 = openSecondaryTable(
        			tbl1, false /*createIfNeeded*/);
            fwt1.remove(ind1);
            final int last1 = fwt1.size();
            
            //2b) update primary of moved
            if (ind1 != last1) {
                FixedTableRow row1 = (FixedTableRow)fwt1.getEntityMarshaller();
                row1.setIndexOnlyMode(true);
                try {
                	row1 = fwt1.get(ind1);
                }
                finally {
                	row1.setIndexOnlyMode(false);
                }
                final int ind0b = row1.getInt(0);

                row0.setIndexOnlyMode(true);
                try {
	                row0.setInt(0, tbl1);
	                row0.setInt(1, ind1);
	                fwt0.set(ind0b, row0);
                }
                finally {
                	row0.setIndexOnlyMode(false);
                }
            }
            
            //2c) delete if empt
            if (fwt1.size() == 0) {
            	fwt1.close(true);
            	final File file = getTableFile(1, tbl1);
            	file.delete();
            	if (secondaryTables.remove(file) != fwt1) {
            		throw new IOException("internal error, should have deleted file of current table: " + file.getAbsolutePath());
            	}
            }
        }
        
    }
    
    public void removeAll() throws IOException {
    	final int primaryByteWidth = primaryTable.getByteWidth() - 8;
    	close(true);
    	eraseTableFiles(folder, fileName);
    	primaryTable = createPrimaryTable(primaryByteWidth);
    }

    public void set(int index, E entity) throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");

        //TODO do this more efficiently
        add(entity);
        remove(index);
    }
    
    public void swap(int indexA, int indexB) throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");
        if (indexA == indexB) return;
        
        //!!! DOES NOT WORK !!! 
        //since table rows are always same instance: primaryTable.swap(indexA, indexB);

        //we must update the back pointers in the secondary tables
        final int tblA, tblB, indA, indB;
        
        //1) read primary table and get secondary table info
        byteBuffer.reset();
        
        //1a) get entity A and secondary table info for A
        FixedTableRow row = (FixedTableRow)primaryTable.getEntityMarshaller();
        row = primaryTable.get(indexA);
        tblA = row.getInt(0);
        indA = row.getInt(1);
        
        //store entity A in byteBuffer
        row.putBytesTo(byteBuffer);
        
        //1b) get entity B and secondary table info for B
        row = primaryTable.get(indexB);
        tblB = row.getInt(0);
        indB = row.getInt(1);

        //2) swap primary table entries
        
        //2a) write entity B to indexA
        primaryTable.set(indexA, row);

        //2b) write entity A to indexB
        row.getBytesFrom(byteBuffer);
        row.setInt(0, tblA);
        row.setInt(1, indA);
        primaryTable.set(indexB, row);
        
        //3) update back pointers in secondary tables 
        if (tblA != 0) {
        	//3a) update back pointer of secondary A
        	final FixedWidthTable<FixedTableRow> secA = openSecondaryTable(
        			tblA, false /*createIfNeeded*/);
            final FixedTableRow rowA = secA.get(indA);
            rowA.setIndexOnlyMode(true);
            try {
            	rowA.setInt(0, indexB);
            	secA.set(indA, rowA);
            }
            finally {
            	rowA.setIndexOnlyMode(false);
            }
        }
        if (tblB != 0) {
        	//3b) update back pointer of secondary B
        	final FixedWidthTable<FixedTableRow> secB = openSecondaryTable(
        			tblB, false /*createIfNeeded*/);
            final FixedTableRow rowB = secB.get(indB);
            rowB.setIndexOnlyMode(true);
            try {
            	rowB.setInt(0, indexA);
            	secB.set(indB, rowB);
            }
            finally {
            	rowB.setIndexOnlyMode(false);
            }
        }

    }

    public int size() throws IOException {
        if (primaryTable == null) throw new IOException("table already closed");
        return primaryTable.size();
    }

}
