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
package ch.javasoft.math.array.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import ch.javasoft.math.BigFraction;

/**
 * The <code>MatrixParser</code> parses text files containing matrix data. Each
 * line corresponds to a matrix row, column values are separated by tab or
 * whitespace. Different data types are supported. The parser adopts to the
 * tightest possible type.
 * <p>
 * A sample file looks like this:
 * <pre>
2 -2 1 1 1 -2
1 0 0 0 0 -1
1 -1 -1 2 2 -1
2 1 -2 1 1 -2
1 -1 2 -1 2 -1
2 1 1 -2 1 -2
0 0 0 0 1 0
1 2 -1 -1 2 -1
0 0 1 0 0 0
1 2 2 -1 -1 -1
0 1 0 0 0 0
-1 1 1 1 1 1
1 2 -1 2 -1 -1
0 0 0 1 0 0
2 1 1 1 -2 -2
1 -1 2 2 -1 -1
1 2 -1 -1 -1 2
0 0 0 0 0 1
2 1 1 -2 -2 1
1 -1 2 -1 -1 2
2 1 -2 1 -2 1
1 -1 -1 2 -1 2
1 0 0 0 -1 0
2 -2 1 1 -2 1
2 1 -2 -2 1 1
1 -1 -1 -1 2 2
1 0 0 -1 0 0
2 -2 1 -2 1 1
1 0 -1 0 0 0
2 -2 -2 1 1 1
4 -1 -1 -1 -1 -1
1 -1 0 0 0 0
 * </pre>
 */
public class MatrixParser {
	
	private final MatrixData matrixData;				
	
	public MatrixParser(File file) throws FileNotFoundException, IOException {
		this(new FileInputStream(file));
	}
	
	public MatrixParser(InputStream in) throws IOException {
		this(new InputStreamReader(in));
	}

	public MatrixParser(Reader reader) throws IOException {
		this(reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader));
	}
	public MatrixParser(BufferedReader reader) throws IOException {
		
		int cols = -1;
		
		final Token token = new Token(reader); 
		if (token.eof()) {
			throw token.createIOException("unexpected end of file, expected at least one matrix row");
		}

		String[] parts;
			
		final List<DataType> rowTypes = new ArrayList<DataType>();
		final List<Object> rowData = new ArrayList<Object>();
		DataType matrixType = DataType.Int;			
		boolean needsConversion = false;
		
		while (!token.eof()) {
			parts = token.split();
			if (cols < 0) {
				cols = parts.length;
			}
			else {
				if (cols != parts.length) {
					throw token.createIOException("expected " + cols + " values, but found " + parts.length);
				}
			}
			DataType rowType = matrixType;
			BigFraction[] vals = new BigFraction[parts.length];
			for (int i = 0; i < vals.length; i++) {
				try {
					vals[i] = BigFraction.valueOf(parts[i]);
					final DataType valType = DataType.getTightestFit(vals[i]);
					rowType = DataType.getLoosestType(rowType, valType);						
				}
				catch (Exception e) {
					throw token.createIOException("cannot parse number \"" + parts[i] + "\", e=" + e);
				}
			}
			if (matrixType.isTighterThan(rowType)) {					
				matrixType = rowType;
				needsConversion = !rowTypes.isEmpty();
			}
			rowTypes.add(rowType);
			rowData.add(matrixType.asVector(vals));
			token.readNextLine();
		}
		if (needsConversion) {
			for (int i = 0; i < rowData.size(); i++) {
				final DataType rowType = rowTypes.get(i); 
				if (rowType.isTighterThan(matrixType)) {
					final Object looser = rowType.asLooserVector(matrixType, rowData.get(i));
					rowData.set(i, looser);
				}
				else if (matrixType.isTighterThan(rowType)) {
					throw new RuntimeException("internal error: matrix type tighter than row type");
				}
			}
		}

		matrixData = new DefaultMatrixData(matrixType, rowData);
	}
	
	public MatrixData getData() {
		return matrixData;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("matrix = { ");
		sb.append(matrixData);
		sb.append(" }");
		return sb.toString();
	}
	
	private static class Token {
		final BufferedReader reader;
		Token(BufferedReader reader) throws IOException {
			this.reader = reader;
			readNextLine();
		}
		String line; int lineNo = 0;
		boolean eof() {
			return line == null;
		}
		String readNextLine() throws IOException {
			line = reader.readLine();lineNo++;
			return line;
		}
		String[] split() {
			return line.trim().split("\\s+");
		}
		IOException createIOException(String msg) {
			return new IOException("[" + lineNo + "] " + msg + ": " + (line == null ? "<end of file>" : line));
		}
	}
}
