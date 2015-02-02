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
package ch.javasoft.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.javasoft.lang.SystemProperties;

/**
 * Utility class with static methods for file handling, e.g. copying files
 */
public class Files {
	
	/**
	 * Copy the given <tt>src</tt> file to <tt>dst</tt>
	 */
	public static void copyFile(File src, File dst) throws IOException {
	     FileChannel sourceChannel = new FileInputStream(src).getChannel();
	     FileChannel destinationChannel = new FileOutputStream(dst).getChannel();
	     sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
	     // or
	     //  destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	     sourceChannel.close();
	     destinationChannel.close();
	}
	
	/**
	 * Deletes the contents of the given directory recursively, but not the
	 * directory itself
	 * 
	 * @throws  IOException 		if a file could not be deleted
     * @throws  SecurityException	if a security manager exists and its 
     * 								{@link	SecurityManager#checkDelete} method 
     * 								denies delete access to the file
	 */
	public static void deleteRecursive(File dir) throws IOException {
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) deleteRecursive(file);
				if (!file.delete()) {
					throw new IOException("could not delete file: " + file.getAbsolutePath());
				}
			}			
		}
	}
	
	/**
	 * Returns the system temp directory, see 
	 * {@link SystemProperties#JAVA_IO_TMPDIR}
	 */
	public static File getTempDir() {
		return new File(System.getProperty(SystemProperties.JAVA_IO_TMPDIR));
	}
	/**
	 * Returns the specified file with system temp directory as parent.  
	 * See also {@link #getTempDir()} and {@link SystemProperties#JAVA_IO_TMPDIR}
	 */
	public static File getTempFile(String fileName) {
		return new File(getTempDir(), fileName);
	}
	/**
	 * Returns a {@link #getFileNameStamped() stamped file} with system temp 
	 * directory as parent.
	 * See also {@link #getTempDir()} and {@link SystemProperties#JAVA_IO_TMPDIR}
	 */
	public static File getTempFileStamped() {
		return getTempFile(getFileNameStamped());
	}
	
	/**
	 * Date format used for {@link #getFileNameStamped()}, which is
	 * <code>yyyy-MM-dd_hh-mm-ss-SSS</code>
	 */
	private static final Format FORMAT_STAMPED_FILE = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss-SSS");
	/**
	 * Returns a file name created from a time stamp, including date and time up
	 * tp milliseconds: <code>yyyy-MM-dd_hh-mm-ss-SSS</code>
	 */
	public static String getFileNameStamped() {
		return FORMAT_STAMPED_FILE.format(new Date());
	}

	/**
	 * Returns the current user's home directory, see
	 * {@link SystemProperties#USER_HOME}
	 */
	public static File getUserHome() {
		return new File(System.getProperty(SystemProperties.USER_HOME));
	}
	/**
	 * Returns the user's current working directory directory, see 
	 * {@link SystemProperties#USER_DIR}
	 */
	public static File getUserWorkingDir() {
		return new File(System.getProperty(SystemProperties.USER_DIR));
	}
	
	//no instances
	private Files() {
		super();
	}

}
