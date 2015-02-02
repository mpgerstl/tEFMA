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
package ch.javasoft.metabolic.efm.util;

import java.io.File;
import java.io.IOException;

import ch.javasoft.io.Files;

/**
 * The <code>TempDir</code> class abstracts the concept of a personalized
 * temp directory. In principle, a personalized temp directory is a time stamp
 * directory in the base temp directory. This class controls creation, deletion
 * and error handling concerning such a directory.
 */
public class TempDir {
	
	private final File baseDir;
	
	private File personalizedDir;
	
	public TempDir(File baseDir) {
		this.baseDir = baseDir;
	}
	
	/**
	 * Returns the parent temp directory, which was specified when this instance
	 * was constructed.
	 */
	public File getBaseDir() {
		return baseDir;
	}
	
	/**
	 * Returns the personalized temp directory. This is usually a subdirectory
	 * in the {@link #getBaseDir() base temp directory} with a time stamp name.
	 * Note that {@link #mkdirPersonalized()} must be called at least once 
	 * before calling this method, otherwise, an exception is thrown.
	 *
	 * @return	the personalized temp directory, never null
	 * @throws 	IllegalStateException 	if {@link #mkdirPersonalized()} was 
	 * 									never called before
	 */
	public File getPersonalizedDir() {
		if (personalizedDir == null) {
			throw new IllegalStateException("mkdirPersonalized() not called or failed -- most probably, you must use the out-core memory");
		}
		return personalizedDir;
	}
	
	/**
	 * Creates the personalized temp directory. If the parent directory is not 
	 * writable or if the personalized temp directory already exists or cannot 
	 * be created, an exception is thrown.
	 * <p>
	 * A {@link Files#getFileNameStamped() stamped name} is used as directory
	 * name for the personalized temp directory.
	 * 
	 * @throws	IOException if the personalized temp directory already exists, 
	 * 						cannot be created or if the parent directory is not 
	 * 						accessible or writable
     * @throws  SecurityException	if a security manager exists and its 
     * 								{@link SecurityManager#checkWrite(String)} 
     * 								method does not permit the named directory 
     * 								to be created
	 */
	public void mkdirPersonalized() throws IOException {
		mkdirPersonalized(Files.getFileNameStamped(), false);
	}
	
	/**
	 * Creates the personalized temp directory. If the parent directory is not 
	 * writable or if the personalized temp directory already exists or cannot 
	 * be created, an exception is thrown.
	 * 
	 * @param 	personalizedDirName this name is used as directory name for the 
	 * 								personalized directory
	 * @param 	allowExists			if true, using an existing directory is 
	 * 								permitted, otherwise, an exception is thrown 
									if the personalized directory already exists
	 * 
	 * @throws	IOException 		if the personalized temp directory already 
	 * 								exists and allowExist is false, if it does 
	 * 								not exist and cannot be created or if the 
									parent directory is not accessible or 
									writable, or if the personalized directory 
									has already been initialized
     * @throws  SecurityException	if a security manager exists and its 
     * 								{@link SecurityManager#checkWrite(String)}
									method does not permit the named directory 
									to be created
	 */
	public void mkdirPersonalized(String personalizedDirName, boolean allowExists) throws IOException {
		if (personalizedDir == null) {			
			if (baseDir.exists() && baseDir.isDirectory() && baseDir.canRead() && baseDir.canWrite()) {
				final File pd = new File(baseDir, personalizedDirName); 
				if (pd.exists()) {
					if (!allowExists) {
						throw new IOException("personalized temp dir exists: " + pd.getAbsolutePath());
					}
				}
				else {
					if (!pd.mkdir()) {
						throw new IOException("cannot create personalized temp dir: " + pd.getAbsolutePath());
					}
				}
				personalizedDir = pd;
			}
			else {
				throw new IOException("base temp directory not writable: " + baseDir.getAbsolutePath());
			}
		}
		else {
			throw new IOException("personalized dir already initialized");
		}
	}
	
	
	/**
	 * Removes the personalized temp directory and recursively all files and 
	 * subdirectories. If the personalized temp directory does not exists, or if
	 * {@link #mkdirPersonalized()} was never called, the method returns false. 
	 * If it exists and deletion of any concerned file or directory fails, an 
	 * exception is thrown. Otherwise, true is returned indicating deletion of
	 * at least one file.
	 * 
	 * @return  true if any file was deleted as a consequence of calling this
	 * 			method
	 * @throws	IOException	if any file or directory could not be deleted
     * @throws  SecurityException	if a security manager exists and its 
     * 								{@link SecurityManager#checkDelete} method 
     * 								denies delete access to the file
	 */
	public boolean rmdirPersonalized() throws IOException {
		if (personalizedDir != null) {
			if (personalizedDir.exists()) {
				Files.deleteRecursive(personalizedDir);
				if (!personalizedDir.delete()) {
					throw new IOException("could not delete directory: " + personalizedDir.getAbsolutePath());
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * A string with absolute path of base temp directory, and personalized 
	 * directory if {@link #mkdirPersonalized()} has already been called.
	 */
	@Override
	public String toString() {
		return personalizedDir == null ? new File(baseDir, "???").getAbsolutePath() : personalizedDir.getAbsolutePath();
	}
	
}
