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
package ch.javasoft.metabolic.efm.memory.outcore;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import ch.javasoft.jbase.Table;
import ch.javasoft.jbase.util.CachedTableWeakReference;
import ch.javasoft.jbase.util.Tables;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.MappedSortableMemory;
import ch.javasoft.metabolic.efm.memory.MemoryPart;
import ch.javasoft.metabolic.efm.memory.ReadWriteMemory;
import ch.javasoft.metabolic.efm.memory.SortableMemory;

/**
 * The <code>OutOfCoreMemory</code> stores columns out of the core memory. They 
 * are stored in files using appropriate {@link Table} implementations.  
 */
public class OutOfCoreMemory<Col extends Column> implements ReadWriteMemory<Col> {
	
	public final boolean				mSortInCore;
	public final File               	mFile;
	public final int					mBooleanSize;
	public final int					mNumericSize;
	public final int					mIteration;
	
    private final ColumnHome<?, Col>	mColumnHome;
    private final Table<Col>			mTable;
    
    /**
     * Opens the file specified by file id for reading
     */
    public OutOfCoreMemory(String fileId) throws IOException {
        this(new FileId<Col>(fileId));
    }
    /**
     * Opens the file specified by file id for reading
     */
    public OutOfCoreMemory(FileId<Col> fileId) throws IOException {
		if (!fileId.getFile().exists() || !fileId.getFile().canRead()) {
			throw new IOException("cannot read file: " + fileId.getFile().getAbsolutePath());
		}
		mFile         	= fileId.getFile();
        mBooleanSize    = fileId.getBooleanSize();
        mNumericSize    = fileId.getNumericSize();
        mIteration      = fileId.getIteration();
        mColumnHome     = fileId.getColumnHome();
        mSortInCore		= fileId.sortInCore();
        mTable 			= getNestedTable(mColumnHome.openTable(fileId.getFolder(), fileId.getFileName(), mBooleanSize, mNumericSize));
    }
    /**
     * Creates a new non-partitioned memory, stored in one or multiple files.
     */
	public OutOfCoreMemory(File folder, int iteration, int booleanSize, int numericSize, boolean sortInCore, ColumnHome<?, Col> columnHome) throws IOException {
		this(folder, FileName.NORMAL, iteration, booleanSize, numericSize, sortInCore, columnHome);
	}
    /**
     * Creates a new partitioned memory, stored in one or multiple files.
     */
	public OutOfCoreMemory(File folder, MemoryPart part, int iteration, int booleanSize, int numericSize, boolean sortInCore, ColumnHome<?, Col> columnHome) throws IOException {
		this(folder, FileName.getPartFileName(part), iteration, booleanSize, numericSize, sortInCore, columnHome);
	}
	private OutOfCoreMemory(File folder, FileName fileName, int iteration, int booleanSize, int numericSize, boolean sortInCore, ColumnHome<?, Col> columnHome) throws IOException {
		this(folder, fileName.getFileName(iteration), iteration, booleanSize, numericSize, sortInCore, columnHome);
	}
	private OutOfCoreMemory(File folder, String fileName, int iteration, int booleanSize, int numericSize, boolean sortInCore, ColumnHome<?, Col> columnHome) throws IOException {
//		System.out.println(fileName + " / " + iteration + " (" + booleanSize + " / " + numericSize + ")");
		if (!folder.exists()) {
//			if (!folder.mkdirs()) {
//				throw new IOException("mkdirs failed for: " + folder.getAbsolutePath());
//			}
			throw new IOException("no such folder: " + folder.getAbsolutePath());
		}
		mFile         	= new File(folder, fileName);
        mBooleanSize    = booleanSize;
        mNumericSize    = numericSize;
        mIteration      = iteration;
        mColumnHome     = columnHome;
        mSortInCore		= sortInCore;
        mTable 			= getNestedTable(columnHome.createTable(folder, fileName, booleanSize, numericSize));
	}
	private static <C extends Column> Table<C> getNestedTable(Table<C> table) throws IOException {
//		return table;
//		if (true)
//			return Tables.autoflushTable(table);
//		return Tables.synchronizedTable(Tables.autoflushTable(table));
//		return Tables.synchronizedTable(table);
		
//      mTable 			= columnHome.createTable(folder, fileName, booleanSize, numericSize);
//      mTable 			= new CachedTableSoftReference<Col>(columnHome.createTable(folder, fileName, booleanSize, numericSize), 256, true);
//      mTable 			= Tables.readWriteLockTable(columnHome.createTable(folder, fileName, booleanSize, numericSize));
		return new CachedTableWeakReference<C>(table);
//		return new CachedTableSoftReference<Col>(table);
	}
    public static class FileName {
    	public static final FileName NORMAL = new FileName("cols_", "i.tbl");
    	public static FileName getPartFileName(MemoryPart part) {
    		return new FileName("cols_part_" + part.getPartId() + "_", "i.tbl");
    	}
    	private final String prefix;
    	private final String postfix;
    	private FileName(String prefix, String postFix) {
    		this.prefix		= prefix;
    		this.postfix	= postFix;
    	}
    	public String getFileName(int iteration) {
    		return prefix + iteration + postfix;
    	}
    	@SuppressWarnings("unchecked")
		public <N extends Number, Col extends Column> FileId<Col> getFileId(ColumnHome<N, Col> columnHome, File dataFolder, int iteration, int numericSize, int booleanSize, boolean sortInCore) {
    		return new FileId(FileId.toString(columnHome.getArithmetic(), numericSize, booleanSize, iteration, new File(dataFolder, getFileName(iteration)), sortInCore));
    	}
    	public int getIteration(String fileName) {
    		if (fileName.length() <= prefix.length() + postfix.length()) return -1;
    		if (!fileName.startsWith(prefix)) return -1;
    		if (!fileName.endsWith(postfix)) return -1;
    		final String strIt = fileName.substring(prefix.length(), fileName.length() - postfix.length());
    		try {
    			return Integer.parseInt(strIt);
    		}
    		catch (NumberFormatException e) {
    			return -1;
    		}
    	}
    }
    public static class FileId<Col extends Column> {
    	private static enum Parts{
    		Arithmetic,
    		NumericSize,
    		BooleanSize,
    		Iteration,
    		FileName,
    		SortInCore;
    		public String getPart(String[] parts) {
    			return parts[ordinal()];
    		}
    	};
    	final String[] fileIdParts;
    	FileId(String fileId) {
    		fileIdParts = fileId.split(":");
    	}    	
		@SuppressWarnings("unchecked")
		public ColumnHome<?, Col> getColumnHome() {
			return (ColumnHome<?, Col>)Arithmetic.parse(Parts.Arithmetic.getPart(fileIdParts)).getColumnHome();
		}
		public int getNumericSize() {
			return Integer.parseInt(Parts.NumericSize.getPart(fileIdParts));
		}
		public int getBooleanSize() {
			return Integer.parseInt(Parts.BooleanSize.getPart(fileIdParts));
		}
		public int getIteration() {
			return Integer.parseInt(Parts.Iteration.getPart(fileIdParts));
		}
		public File getFile() {
			return new File(Parts.FileName.getPart(fileIdParts));
		}
		public boolean sortInCore() {
			try {
				return Boolean.parseBoolean(Parts.SortInCore.getPart(fileIdParts));
			}
			catch (Exception e) {
				return true;/*default: in-core sorting*/
			}
		}
		public File getFolder() {
			return getFile().getParentFile();
		}
		/** Returns the simple file name without path*/
		public String getFileName() {
			return getFile().getName();
		}

		static String toString(OutOfCoreMemory mem) {
			return toString(mem.mColumnHome.getArithmetic(), mem.mNumericSize, mem.mBooleanSize, mem.mIteration, mem.mFile, mem.mSortInCore);
		}
		static String toString(Arithmetic arith, int numericSize, int booleanSize, int iteration, File file, boolean sortInCore) {
    		final StringBuilder sb = new StringBuilder();
    		sb
				.append(arith)
				.append(':')
				.append(numericSize)
				.append(':')
    			.append(booleanSize)
    			.append(':')
    			.append(iteration)
    			.append(':')
    			.append(file.getAbsolutePath())
    			.append(':')
    			.append(sortInCore)
    			;
    		return sb.toString();
    	}
    }
	public String fileId() throws IOException {
		return FileId.toString(this);
	}
    
    public void appendColumn(Col column) throws IOException {
//    	if (mBooleanSize != column.booleanSize() || mNumericSize != column.numericSize()) {
//    		throw new IOException();
//    	}
        mTable.add(column);
    }
    public void appendColumns(Iterable<? extends Col> columns) throws IOException {
        for (final Col col : columns) {
            appendColumn(col);
        }        
    }
    public void appendFrom(IndexableMemory<? extends Col> memory) throws IOException {
        for (int i = 0; i < memory.getColumnCount(); i++) {
            final Col col = memory.getColumn(i);
            appendColumn(col);
        }
    }
	public void swapColumns(int indexA, int indexB) throws IOException {
		mTable.swap(indexA, indexB);
	}
    public void clear() throws IOException {
        mTable.removeAll();
    }
    public Col getColumn(int index) throws IOException {
        return mTable.get(index);
    }
    public int getColumnCount() throws IOException {
        return mTable.size();
    }
    public Iterator<Col> iterator() {
        return Collections.unmodifiableList(Tables.asList(mTable)).iterator();
    }
    public SortableMemory<Col> toSortableMemory() throws IOException {
//    	final InCoreMemory<Col> mem = new InCoreMemory<Col>();
//    	mem.appendColumns(this);    	
//    	final OutOfCoreMemory<Col> omem = new OutOfCoreMemory<Col>(mFile.getParentFile(), mFile.getName() + "sort", mIteration, mBooleanSize, mNumericSize, mColumnHome);
//    	omem.appendColumns(mem);
//    	return omem;

    	return mSortInCore ? new MappedSortableMemory<Col>(this) : this;
    }
	
    public void flush() throws IOException {
    	mTable.flush();
    }
    public void close(boolean erase) throws IOException {
    	mTable.close(erase);
    }
	@Override
	protected void finalize() throws Throwable {
		close(false);
	}
}
