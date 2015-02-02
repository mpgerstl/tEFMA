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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Helper class with static creation methods for {@link PrintWriter}s and
 * {@link PrintStream}s from various sources.
 */
public class Print {

	public static final PrintWriter createWriter(Writer writer) {
		return writer instanceof PrintWriter ? (PrintWriter)writer : new PrintWriter(writer);
	}
	public static final PrintWriter createWriter(File file) throws IOException {
		return new PrintWriter(new FileWriter(file));
	}
	public static final PrintWriter createWriter(File folder, String fileName) throws IOException {
		return createWriter(new File(folder, fileName));
	}
	public static final PrintWriter createWriter(OutputStream out) {
		return new PrintWriter(out);
	}
	
	public static final PrintStream createStream(OutputStream out) {
		return out instanceof PrintStream ? (PrintStream)out : new PrintStream(out);
	}
	public static final PrintStream createStream(File file) throws FileNotFoundException {
		return new PrintStream(file);
	}
	public static final PrintStream createStream(File folder, String fileName) throws FileNotFoundException {
		return new PrintStream(new File(folder, fileName));
	}
	public static final PrintStream createStream(Writer writer) {
		return new PrintStream(new WriterOutputStream(writer));
	}

	// no instances
	private Print() {
		super();
	}

}
