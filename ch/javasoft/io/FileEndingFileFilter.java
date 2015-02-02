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

import javax.swing.filechooser.FileFilter;

/**
 * File filter by ending (e.g. suffix) of the file name. Extends
 * {@link javax.swing.filechooser.FileFilter} and implements {@link java.io.FileFilter}
 * so that it can be used with the {@link javax.swing.JFileChooser} and for
 * simple file filtering, e.g. with {@link java.io.File#listFiles(java.io.FileFilter)}.
 */
public class FileEndingFileFilter extends FileFilter implements java.io.FileFilter {

	private final String	mDesc;
	private final String[]	mEndings;
	private final boolean	mHideSubdirs;
	
	public FileEndingFileFilter(String desc, String ... ending) {
		this(false, desc, ending);
	}
	public FileEndingFileFilter(boolean hideSubdirs, String desc, String ... endings) {
		if (desc == null || endings == null) {
			throw new NullPointerException();
		}
		for (int i = 0; i < endings.length; i++) {
			if (endings[i] == null) throw new NullPointerException();
		}
		mDesc			= desc;
		mEndings		= endings;
		mHideSubdirs	= hideSubdirs;
	}

	@Override
	public boolean accept(File f) {
		if (!mHideSubdirs && f.isDirectory()) return true;
		for (int i = 0; i < mEndings.length; i++) {
			if (f.getName().endsWith(mEndings[i])) return true;			
		}
		return false;
	}

	@Override
	public String getDescription() {
		return mDesc;
	}
	
	public String[] getEndings() {
		return mEndings;
	}
	
	@Override
	public int hashCode() {
		return mEndings.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof FileEndingFileFilter) {
			FileEndingFileFilter filter = (FileEndingFileFilter)obj;
			return mDesc.equals(filter.mDesc) && mEndings.equals(filter.mEndings);
		}
		return false;
	}

}
