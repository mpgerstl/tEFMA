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
package ch.javasoft.metabolic.efm.output.mat;

import java.io.File;
import java.io.IOException;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.efm.output.EfmOutputCallback;
import ch.javasoft.metabolic.efm.output.EfmOutputEvent;
import ch.javasoft.metabolic.efm.output.EfmProcessor;

public class DefaultPartitionedMatFileWriter implements PartitionedMatFileWriter {
	
	private final File 		mFolder;
	private final String	mFileName;
	private final String	mFileEnding;
	private final long		mMaxEfmCountPerPart;
	
	private MatFileWriter	mWriter			= null;
	private long			mLastEfmIndex	= -1;
	
	public DefaultPartitionedMatFileWriter(File folder, String fileName, long maxEfmCountPerPart) {
		this(folder, fileName, ".mat", maxEfmCountPerPart);
	}
	public DefaultPartitionedMatFileWriter(File folder, String fileName, String fileEnding, long maxEfmCountPerPart) {
		mFolder				= folder;
		mFileName			= fileName;
		mFileEnding			= fileEnding;
		mMaxEfmCountPerPart	= maxEfmCountPerPart;
	}

	public long getEfmCountForPart(EfmOutputCallback cb, EfmOutputEvent evt, int partIndex) {
		long efmCount 	= evt.getEfmCount();//;evt.estimateRealEfmCount();
		long efmsSoFar	= partIndex * mMaxEfmCountPerPart;
		long efmsLeft	= efmCount - efmsSoFar;
		return efmsLeft > mMaxEfmCountPerPart ? mMaxEfmCountPerPart : efmsLeft;
	}

	public MatFileWriter getPartMatFileWriter(EfmOutputCallback cb, EfmOutputEvent evt, long efmIndex, MetabolicNetwork originalNetwork, EfmProcessor<MatFileWriter> efmProcessor) throws IOException {
		if (efmIndex != mLastEfmIndex) {
			if (efmIndex % mMaxEfmCountPerPart == 0L) {
				if (mWriter != null) {
					closeCurrentWriter(cb, evt, efmProcessor);
				}
				mWriter = createAndInitWriter(cb, evt, efmIndex, originalNetwork, efmProcessor);
			}
			mLastEfmIndex = efmIndex;
		}
		return mWriter;
	}
	
	public void close(EfmOutputCallback cb, EfmOutputEvent evt, long efmCount, EfmProcessor<MatFileWriter> efmProcessor) throws IOException {
		closeCurrentWriter(cb, evt, efmProcessor);
	}
	
	private MatFileWriter createAndInitWriter(EfmOutputCallback cb, EfmOutputEvent evt, long efmIndex, MetabolicNetwork originalNetwork, EfmProcessor<MatFileWriter> efmProcessor) throws IOException {
		long nextPartEfmInd	= efmIndex + mMaxEfmCountPerPart;
		long totalEfmCount	= evt.getEfmCount();//estimateRealEfmCount();
		int partCount 		= 1 + (int)(Math.max(0, totalEfmCount - 1) / mMaxEfmCountPerPart);
		
		long partEfmCount 	= nextPartEfmInd > totalEfmCount ? totalEfmCount % mMaxEfmCountPerPart : mMaxEfmCountPerPart;  
			
		int partIndex = (int)(efmIndex / mMaxEfmCountPerPart);
		
		final File partFile = getPartFile(partIndex);
		MatFileWriter matWriter = new MatFileWriter(partFile, MatFileOutputCallback.STRUCT_NAME);
		
//		if (partIndex == 0) {
			MatFileOutputFormatter.writeNetworkFootPrint(originalNetwork, matWriter, getFileNameWithoutEnding(), partFile.getName(), partIndex, partCount);
//		}
		// initialize efm cache
		int[] unused = efmProcessor.initialize(cb, matWriter, evt, partEfmCount);
//		if (partIndex == 0) {
			matWriter.write("disabledReactions", unused);
//		}
		return matWriter;
	}
	
	
	private String getFileNameWithoutEnding() {
		final String ending	= mFileEnding.startsWith(".") ? mFileEnding : "." + mFileEnding;
		final String name		= mFileName.endsWith(ending) ? mFileName.substring(0, mFileName.length() - ending.length()) : mFileName;
		return name;
	}
	private File getPartFile(int partIndex) {
		String ending	= mFileEnding.startsWith(".") ? mFileEnding : "." + mFileEnding;
		File file = new File(mFolder, getFileNameWithoutEnding() + '_' + partIndex + ending);
		return file;
	}

	private void closeCurrentWriter(EfmOutputCallback cb, EfmOutputEvent evt, EfmProcessor<MatFileWriter> efmProcessor) throws IOException {
		if (efmProcessor != null) {
			efmProcessor.finalize(cb, mWriter, evt);
		}
		mWriter.close();
	}
	
}
