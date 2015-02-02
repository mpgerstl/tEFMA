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
package ch.javasoft.lang;

/**
 * Names of predefined system properties as listed in 
 * {@link System#getProperties()}
 */
public interface SystemProperties {
	/** Java Runtime Environment version*/
	public static final String JAVA_VERSION 				= "java.version";
	/** Java Runtime Environment vendor */
	public static final String JAVA_VENDOR 					= "java.vendor";
	/** Java vendor URL */
	public static final String JAVA_VENDOR_URL				= "java.vendor.url";
	/** Java installation directory */
	public static final String JAVA_HOME					= "java.home";
	/** Java Virtual Machine specification version */
	public static final String JAVA_VM_SPECIFICATION_VERSION= "java.vm.specification.version";
	/** Java Virtual Machine specification vendor */
	public static final String JAVA_VM_SPECIFICATION_VENDOR	= "java.vm.specification.vendor";
	/** Java Virtual Machine specification name */
	public static final String JAVA_VM_SPECIFICATION_NAME	= "java.vm.specification.name";
	/** Java Virtual Machine implementation version */
	public static final String JAVA_VM_VERSION				= "java.vm.version";
	/** Java Virtual Machine implementation vendor */
	public static final String JAVA_VM_VENDOR				= "java.vm.vendor";
	/** Java Virtual Machine implementation name */
	public static final String JAVA_VM_NAME					= "java.vm.name";
	/** Java Runtime Environment specification version */
	public static final String JAVA_SPECIFICATION_VERSION	= "java.specification.version";
	/** Java Runtime Environment specification vendor */
	public static final String JAVA_SPECIFICATION_VENDOR	= "java.specification.vendor";
	/** Java Runtime Environment specification name */
	public static final String JAVA_SPECIFICATION_NAME		= "java.specification.name";
	/** Java class format version number */
	public static final String JAVA_CLASS_VERSION			= "java.class.version";
	/** Java class path */
	public static final String JAVA_CLASS_PATH				= "java.class.path";
	/** List of paths to search when loading libraries */
	public static final String JAVA_LIBRARY_PATH			= "java.library.path";
	/** Default temp file path */
	public static final String JAVA_IO_TMPDIR				= "java.io.tmpdir";
	/** Name of JIT compiler to use */
	public static final String JAVA_COMPILER				= "java.compiler";
	/** Path of extension directory or directories */
	public static final String JAVA_EXT_DIRS				= "java.ext.dirs";
	/** Operating system name */
	public static final String OS_NAME						= "os.name";
	/** Operating system architecture */
	public static final String OS_ARCH						= "os.arch";
	/** Operating system version */
	public static final String OS_VERSION					= "os.version";
	/** File separator ("/" on UNIX) */
	public static final String FILE_SEPARATOR				= "file.separator";
	/** Path separator (":" on UNIX) */
	public static final String PATH_SEPARATOR 				= "path.separator";
	/** Line separator ("\n" on UNIX) */
	public static final String LINE_SEPARATOR				= "line.separator";
	/** User's account name */
	public static final String USER_NAME 					= "user.name";
	/** User's home directory */
	public static final String USER_HOME 					= "user.home";
	/** User's current working directory */
	public static final String USER_DIR 					= "user.dir";
}
