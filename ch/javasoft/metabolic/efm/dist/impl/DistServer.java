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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import ch.javasoft.jbase.EntityMarshaller;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.memory.AppendableMemory;
import ch.javasoft.metabolic.efm.memory.IndexableMemory;
import ch.javasoft.metabolic.efm.memory.PartId;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.progress.FileProgressWriter;
import ch.javasoft.metabolic.efm.progress.IntProgressAggregator;
import ch.javasoft.metabolic.efm.progress.ProgressAggregator;
import ch.javasoft.metabolic.efm.progress.AbstractStringProgressWriter.Mode;
import ch.javasoft.util.logging.LogPrintWriter;

/**
 * The <code>DistServer</code> is the server side of the distributed 
 * computation. The server handles the following tasks:
 * <ul>
 * <li>{@link Command#NEXTJOB NEXTJOB}: management of the tree traversing jobs,
 *  	the next available job is returned on client request</li>
 * <li>{@link Command#APPEND APPEND}: collecting of new-born modes from clients
 * 		and storage on disk</li>
 * <li>{@link Command#PROGRESS PROGRESS}: gathering progress notifications from 
 * 		clients and writing aggregated progress information to progress file</li>
 * </ul>
 * 
 * @see DistClient
 */
public class DistServer<Col extends Column> {
	
	public static enum Command {
		/**
		 * Command sent to the server process to request the number of columns, 
		 * followed by the ordinal of the {@link PartId} of the desired memory.
		 */
		COUNT,
		/**
		 * Command sent to the server process to request a column, followed by
		 * the ordinal of the {@link PartId} of the desired memory, and the 
		 * index of the desired mode in the memory.
		 */
		GET,
		/**
		 * Command sent to the server process before a new mode, born from two
		 * adjacent modes, is submitted to the server process
		 */
		APPEND, 
		/**
		 * Command sent to the server process if the current subtree has been
		 * traversed, and a new subtree index is requested. The server returns
		 * just the index, or <tt>-1</tt> if no subtrees, that is, no tasks, are 
		 * left.
		 * <p>
		 * If <tt>-1</tt> is returned, the server also decrements the counter 
		 * for active cluster processes since the cluster process is expected to
		 * terminate after receiving <tt>-1</tt>.
		 */
		NEXTJOB, 
		/**
		 * Command sent to the server process notifying a progress increment.
		 * The server collects all increments and updates and outputs the 
		 * overall progress of the traversal process.
		 */
		PROGRESS
	}
	
	private final CountDownLatch activeNodeLatch;
	private final int partCount;
	private final AtomicInteger partIndex = new AtomicInteger();
	private final AtomicReference<IOException> exception = new AtomicReference<IOException>();
	private final ServerSocket socket;
	private final EntityMarshaller<Col> writeMarshaller;
	private final EntityMarshaller<Col> readMarshaller;
	private final AdjEnumModel<Col> adjModel;
	
	private final ProgressAggregator progress;
	
	public DistServer(ColumnHome<?, Col> columnHome, Config config, AdjEnumModel<Col> adjModel, int nodeCount) throws UnknownHostException, IOException {
		this(columnHome, config, adjModel, nodeCount, 0);
	}
	public DistServer(ColumnHome<?, Col> columnHome, Config config, AdjEnumModel<Col> adjModel, final int nodeCount, int port) throws UnknownHostException, IOException {
		this.activeNodeLatch	= new CountDownLatch(nodeCount);
		this.partCount			= config.getDistributedConfig().getPartition();
		this.socket 			= new ServerSocket(port);
		this.writeMarshaller	= columnHome.getEntityMarshaller(adjModel.getNextState().getBooleanSize(), adjModel.getNextState().getNumericSize());
		this.readMarshaller		= columnHome.getEntityMarshaller(adjModel.getCurrentState().getBooleanSize(), adjModel.getCurrentState().getNumericSize());
		this.adjModel			= adjModel;
		if (config.getProgressPartition() <= 0) {
			progress = null;
		}
		else {			
			progress = new IntProgressAggregator(
				new FileProgressWriter(
					new File(
						config.getTempDir().getPersonalizedDir(), 
						"progress-" + adjModel.getIterationIndex() + ".txt"
					),
					Mode.Partition, config.getProgressPartition()
				)
			);
		}		
	}
	
	public int getPartCount() {
		return partCount;
	}
	
	/**
	 * Returns the index of the next part, or -1 if no part is left
	 */
	public int getNextPart() {
		final int ix = partIndex.getAndIncrement();
		if (ix >= partCount) {
			partIndex.set(partCount);
			return -1;
		}
		return ix;
	}

	public void start() {
		final int nodeCount = (int)activeNodeLatch.getCount();
		new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < nodeCount; i++) {
					try {
						final Socket client = socket.accept();
						handleRequest(client);
					}
					catch (SocketException e) {
						//thrown since socket is closed, ignore
					}
					catch (IOException e) {
						handleException(e);
					}
				}
			}
		}.start();
	}
	public int getPort() {
		return socket.getLocalPort();
	}
	
	private void handleRequest(final Socket client) {
		final Thread thread = new Thread() {
			@Override
			public void run() {
				try {
//					LogPkg.LOGGER.finest("SERVER OPEN: " + client);
					final DataInputStream  dataIn 	= new DataInputStream(client.getInputStream());
					final DataOutputStream dataOut 	= new DataOutputStream(client.getOutputStream());
					boolean stop = false;
					while (!stop && activeNodeLatch.getCount() > 0) {
						final int icommand = dataIn.readByte();
						if (icommand < 0 || icommand >= Command.values().length) {
							throw new IOException("invalid command: " + icommand);
						}
						final Command command = Command.values()[icommand];
						switch (command) {
							case COUNT: 
								{
									final PartId part	= PartId.values()[dataIn.readByte()];
									final IndexableMemory<Col> memory = adjModel.getMemory(part);
									final int count;
									synchronized (memory) {
										count = memory.getColumnCount();
									}
									dataOut.writeInt(count);
									dataOut.flush();
								}
								break;
							case GET: 
								{
									final PartId part	= PartId.values()[dataIn.readByte()];
									final int index 	= dataIn.readInt();
									final IndexableMemory<Col> memory = adjModel.getMemory(part);
									final Col col;
									synchronized (memory) {
										col = memory.getColumn(index);
									}
									readMarshaller.writeTo(col, dataOut);
									dataOut.flush();
//								LogPkg.LOGGER.finest("SERVER GET: " + client);
								}
								break;
							case APPEND:
								{
									final Col col = writeMarshaller.readFrom(dataIn);
									adjModel.getMemoryForNewFromAdj().appendColumn(col);
//									LogPkg.LOGGER.finest("SERVER APPEND: " + client);
								}
								break;
							case NEXTJOB:
								{
	//								LogPkg.LOGGER.finest("SERVER NEXTJOB: " + client);
									final int nextPart = getNextPart();
									dataOut.writeInt(nextPart);
									dataOut.flush();
									if (nextPart < 0) {
										stop = true;
									}
								}
								break;
							case PROGRESS: 
								{
									final int inc = dataIn.readInt();
									progress.updateProgress(inc);
								}
								break;
							default:
								throw new IOException("unsupported command: " + command);
						}
					}
				}
				catch (IOException e) {
					handleException(e);
				}
				finally {
					activeNodeLatch.countDown();
	                try {
						client.close();
					} 
	                catch (IOException e) {
						LogPkg.LOGGER.warning("could not close client sochet, e=" + e);
						e.printStackTrace(new LogPrintWriter(LogPkg.LOGGER, Level.WARNING));
					}
				}
			}
		};
		thread.start();
	}
	private void handleException(IOException e) {
		exception.compareAndSet(null, e);
	}

	/**
	 * Waits until all jobs are completed, that is, until a <i>no job</i> 
	 * response has been sent to every client. Does not close the server.
	 * 
	 * @throws InterruptedException	if waiting is interrupted
	 */
	public void awaitCompletion() throws InterruptedException {
		activeNodeLatch.await();
	}
	/**
	 * Closes the server, and forces abortion if the clients did not complete
	 * yet
	 */
	public AppendableMemory<Col> close() throws IOException {
		while (activeNodeLatch.getCount() > 0) {
			activeNodeLatch.countDown();
		}
        try {
    		socket.close();
		} 
        catch (IOException e) {
			LogPkg.LOGGER.warning("could not close server sochet, e=" + e);
			e.printStackTrace(new LogPrintWriter(LogPkg.LOGGER, Level.WARNING));
		}
		if (progress != null) {
			progress.close();
		}
		if (exception.get() != null) {
			throw exception.get();
		}
		final AppendableMemory<Col> memory = adjModel.getMemoryForNewFromAdj();
		memory.flush();
		return memory;
	}
}
