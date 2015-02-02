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
package ch.javasoft.metabolic.efm.dist.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.javasoft.jbase.EntityMarshaller;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.dist.PartIterator;
import ch.javasoft.metabolic.efm.dist.impl.DistServer.Command;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.MemoryAccessor;
import ch.javasoft.metabolic.efm.model.ModelPersister;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;

/**
 * The <code>DistClient</code> is the client side of distributed computation.
 * The client performs the following tasks:
 * <ul>
 * <li>{@link Command#NEXTJOB NEXTJOB}: requests next tree traversing job from
 * 		server, and starts the subtree traversal process</li>
 * <li>{@link Command#APPEND APPEND}: new-born modes from adjacent modes are
 * 		sent to the server</li>
 * <li>{@link Command#PROGRESS PROGRESS}: pre-aggregated progress information
 * 		is sent to the server process</li>
 * </ul>
 * 
 * @see DistServer
 */
public class DistClient<Col extends Column> implements MemoryAccessor<Col>, PartIterator {
	
	private final Socket socket;
	private final DataInputStream dataInput;
	private final DataOutputStream dataOutput;
	private final AdjEnumModel<Col> adjEnumModel;
	private final EntityMarshaller<Col> writeMarshaller;
	private final EntityMarshaller<Col> readMarshaller;
	private final Lock lock = new ReentrantLock();
	
	private final AppendableMemory<Col> memory;
	private final ProgressAggregator	progress;
	
	public DistClient(ColumnHome<?, Col> columnHome, ModelPersister modelPersister, File adjEnumPropsFile, String host, int port) throws UnknownHostException, IOException {
		this.socket 			= new Socket(host, port);
		this.dataInput			= new DataInputStream(socket.getInputStream());
		this.dataOutput			= new DataOutputStream(socket.getOutputStream());
		this.memory				= new Memory();
		this.progress 			= new Progress();
		this.adjEnumModel		= modelPersister.readAdjEnumModel(columnHome, adjEnumPropsFile, this);
		this.writeMarshaller	= columnHome.getEntityMarshaller(adjEnumModel.getNextState().getBooleanSize(), adjEnumModel.getNextState().getNumericSize());
		this.readMarshaller		= columnHome.getEntityMarshaller(adjEnumModel.getCurrentState().getBooleanSize(), adjEnumModel.getCurrentState().getNumericSize());
		LogPkg.LOGGER.finest("CLIENT CONNECTED: " + socket);
	}
	
	/**
	 * Return the model for adjacency enumeration, restored using the model
	 * persister when instantiating this client
	 */
	public AdjEnumModel<Col> getAdjEnumModel() {
		return adjEnumModel;
	}
	
	/**
	 * Return the client side memory for appending only. Appended columns are
	 * sent to the server side.
	 */
	public AppendableMemory<Col> getAppendableMemory() {
		return memory;
	}
	
	/**
	 * Return the client side progress aggregator. Progress notifications are
	 * pre-aggregated and sent to the server.
	 */
	public ProgressAggregator getProgressAggregator() {
		return progress;
	}
	
	private class Memory implements AppendableMemory<Col> { 

		public void appendColumn(Col column) throws IOException {
			lock.lock();
			try {
				LogPkg.LOGGER.finest("CLIENT APPEND: " + column);
				dataOutput.writeByte(DistServer.Command.APPEND.ordinal());
				writeMarshaller.writeTo(column, dataOutput);
			}
			finally {
				lock.unlock();
			}
		}
	
		public void appendColumns(Iterable<? extends Col> columns) throws IOException {
			for (final Col col : columns) appendColumn(col);
		}
	
		public void appendFrom(IndexableMemory<? extends Col> memory) throws IOException {
			appendColumns(memory);
		}
	
		public void flush() throws IOException {
			dataOutput.flush();
		}

		public void close(boolean erase) throws IOException {
			if (erase) throw new IOException("erasing not supported");
			lock.lock();
			try {
				LogPkg.LOGGER.finest("CLOSE CLIENT: " + socket);
				dataInput.close();
				dataOutput.close();
				socket.close();
				progress.close();
				adjEnumModel.closeForThread();
			}
			finally {
				lock.unlock();
			}
		}
	
		public String fileId() throws IOException {
			throw new IOException("not supported");
		}
		
		public SortableMemory<Col> toSortableMemory() throws IOException {
			throw new IOException("not supported");
		}
	
		public int getColumnCount() throws IOException {
			throw new IOException("not supported");
		}
	
		public Iterator<Col> iterator() {
			throw new RuntimeException("not supported");
		}
	}

	private class Progress implements ProgressAggregator {
		public void updateProgress(int e) throws IOException, IllegalArgumentException {
			if (e > getSmallestIncrement()) {
				throw new IllegalArgumentException("progress increment too small, i.e. e > " + getSmallestIncrement() + ": " + e);
			}
			if (e < 0) {
				throw new IllegalArgumentException("negative progress increment: " + e);
			}
			lock.lock();
			try {
				LogPkg.LOGGER.finest("CLIENT PROGRESS: " + socket);
				dataOutput.writeByte(DistServer.Command.PROGRESS.ordinal());
				dataOutput.writeInt(e);
				dataOutput.flush();
			}
			finally {
				lock.unlock();
			}
		}
		/**
		 * Currently always returns 10, corresponding to 2^-10 = 1/1024, i.e.
		 * approx. 1/10 percent.
		 */
		public int getSmallestIncrement() {
			return 10;
		}
		public void close() throws IOException {
			//don't do anything, we close after all jobs
		}
	}
	
	/**
	 * Returns the column, specified by index, from the appropriate memory,
	 * identified by the part identifier.
	 * 
	 * @param	part	identifies the memory type
	 * @param 	index	the index of the desired column
	 */
	public Col getColumn(PartId part, int index) throws IOException {
		lock.lock();
		try {
			LogPkg.LOGGER.finest("CLIENT GET: " + socket);
			dataOutput.writeByte(DistServer.Command.GET.ordinal());
			dataOutput.writeByte(part.ordinal());
			dataOutput.writeInt(index);
			dataOutput.flush();
			return readMarshaller.readFrom(dataInput);
		}
		finally {
			lock.unlock();
		}
	}
	/**
	 * Returns the column count from the appropriate memory, identified by the 
	 * part identifier.
	 * 
	 * @param	part	identifies the memory type
	 */
	public int getColumnCount(PartId part) throws IOException {
		lock.lock();
		try {
			LogPkg.LOGGER.finest("CLIENT GET: " + socket);
			dataOutput.writeByte(DistServer.Command.COUNT.ordinal());
			dataOutput.writeByte(part.ordinal());
			dataOutput.flush();
			return dataInput.readInt();
		}
		finally {
			lock.unlock();
		}
	}
	
	/**
	 * Returns the next part index, or -1 if there is no next part. The client
	 * resources are closed if no next part exists.
	 */
	public int getNextPart() throws IOException {
		lock.lock();
		try {
			LogPkg.LOGGER.finest("CLIENT NEXTJOB: " + socket);
			dataOutput.writeByte(DistServer.Command.NEXTJOB.ordinal());
			dataOutput.flush();
			final int next = dataInput.readInt();
			if (next < 0) {
				close();
			}
			return next;
		}
		finally {
			lock.unlock();
		}
	}
	
	/**
	 * Closes the socket
	 */
	private void close() throws IOException {
		memory.close(false /*erase*/);
		progress.close();
		adjEnumModel.closeForThread();
		socket.close();
	}

}
