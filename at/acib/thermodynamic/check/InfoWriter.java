/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2015, Matthias P. Gerstl, Vienna, Austria
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


package at.acib.thermodynamic.check;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * write info to outputfiles
 * 
 * @author matthias
 * 
 */
public class InfoWriter {

	private FileWriter m_writer;
	private File m_file;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            for output
	 */
	public InfoWriter(String filename, boolean append) {
		m_file = new File(filename);
		try {
			m_writer = new FileWriter(m_file, append);
		} catch (IOException e) {
			System.out.println("Could not create or write to file " + filename);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * prints the variable assignment of the lp problem
	 * 
	 * @param var
	 *            {@link HashMap} < index , name of variable > of the lp
	 *            variables
	 */
	protected void printVariableInfo(HashMap<Integer, String> var) {
		StringBuilder build = new StringBuilder();
		for (int i = 0; i < var.size(); i++) {
			int j = i + 1;
			build.append("x" + j + "\t" + var.get(i) + "\n");
		}
		write(build);
	}

	protected void printPattern(ArrayList<ArrayList<String>> pattern, int startPosition) {
		StringBuilder build = new StringBuilder();
		for (int i = startPosition; i < pattern.size(); i++) {
			ArrayList<String> pat = pattern.get(i);
			Iterator<String> patIter = pat.iterator();
			while (patIter.hasNext()) {
				String rx = patIter.next();
				rx = rx.replace(ThermoChecker.RX_REV_PREFIX, "-");
				rx = rx.replace(ThermoChecker.RX_PREFIX, "");
				build.append(rx + "  ");
			}
			build.append("\n");
		}
		write(build);
	}

	private void write(StringBuilder build) {
		try {
			m_writer.write(build.toString());
			m_writer.flush();
		} catch (IOException e) {
			System.out.println("Could not write to file " + m_file.getName());
		}
	}

	/**
	 * close the outputfile
	 */
	protected void close() {
		try {
			m_writer.close();
		} catch (IOException e) {
			System.out.println("Could not close file " + m_file.getName());
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
