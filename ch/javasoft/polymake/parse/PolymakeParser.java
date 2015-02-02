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
package ch.javasoft.polymake.parse;

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
import ch.javasoft.math.array.parse.DataType;

/**
 * The <code>PolymakeParser</code> parses polymake .poly files.
 * <p>
 * See
 * <a href="http://www.math.tu-berlin.de/polymake">http://www.math.tu-berlin.de/polymake</a>
 * <p>
 * A sample file looks like this:
 * <pre>
DESCRIPTION

A 5-dimensional cross polytope.

DIM
5

VERTICES
1 1 0 0 0 0
1 0 1 0 0 0
1 0 0 1 0 0
1 0 0 0 1 0
1 0 0 0 0 1 
1 0 1 1 1 1
1 1 0 1 1 1
1 1 1 0 1 1
1 1 1 1 0 1 
1 1 1 1 1 0

FACETS
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

AFFINE_HULL

N_FACETS
32

AMBIENT_DIM
5


_version 1.5.1
_application polytope
 * </pre>
 */
public class PolymakeParser {
	
	private final int		dim;
	private final PolymakeMatrixData[]	polymakeMatrixData;				
	
	public PolymakeParser(File file) throws FileNotFoundException, IOException {
		this(new FileInputStream(file));
	}
	
	public PolymakeParser(InputStream in) throws IOException {
		this(new InputStreamReader(in));
	}

	public PolymakeParser(Reader reader) throws IOException {
		this(reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader));
	}
	public PolymakeParser(BufferedReader reader) throws IOException {
		Keyword key = null;
		
		final List<PolymakeMatrixData> dataList = new ArrayList<PolymakeMatrixData>(2);
		final int[] ptrDim = new int[] {-1};
		
		final Token token = new Token(reader); 
		while (!token.eof() && null == (key = token.asKeyword())) {
			token.readNextLine();
		}
		while (ptrDim[0] < 0 || dataList.isEmpty() || key != null) {
			if (key == null) {
				if (ptrDim[0] < 0) {
					throw token.createIOException("unexpected end of file, keyword " + Keyword.DIM + " expected");
				}
				else {
					throw token.createIOException("unexpected end of file, either of the keywords " + Keyword.VERTICES + " or " + Keyword.FACETS + " expected");
				}
			}
			if (Keyword.DIM.equals(key)) {
				token.readNextLine();
				final int dim = parseDim(token);
				if (ptrDim[0] < 0) {
					ptrDim[0] = dim;
				}
				else {
					if (ptrDim[0] != dim) {
						throw token.createIOException("dimension missmatch, DIM=" + dim + ", data expected=" + (dim + 1) + " but found=" + (ptrDim[0] + 1));
					}
				}
			}
			else {
				token.readNextLine();
				dataList.add(parseData(token, key, ptrDim));
			}
			key = null;
			while (!token.eof() && null == (key = token.asKeyword())) {
				token.readNextLine();
			}
		}
		
		this.dim		= ptrDim[0];
		this.polymakeMatrixData	= dataList.toArray(new PolymakeMatrixData[dataList.size()]);
	}
	
	private int parseDim(Token token) throws IOException {
		try {
			return Integer.parseInt(token.line.trim());
		}
		catch (Exception e) {
			throw token.createIOException("cannot parse dimension \"" + token.line + "\", e=" + e);
		}
	}
	private PolymakeMatrixData parseData(Token token, Keyword key, int[] ptrDim) throws IOException {
		final DefinitionType def = key.getDefinitionType();
		if (token.eof()) {
			throw token.createIOException("unexpected end of file, expected data values for " + key);
		}
		else {
			String[] parts;
			
			final List<DataType> rowTypes = new ArrayList<DataType>();
			final List<Object> rowData = new ArrayList<Object>();
			DataType matrixType = DataType.Int;			
			boolean needsConversion = false;
			
			parts = token.split();
			while (!token.eof() && (ptrDim[0] < 0 || parts.length == ptrDim[0] + 1)) {
				if (ptrDim[0] < 0) {
					ptrDim[0] = parts.length - 1;
				}
				DataType rowType = matrixType;
				BigFraction[] vals = new BigFraction[parts.length];
				for (int i = 0; i < vals.length; i++) {
					try {
						String str = parts[i];
						if (str != null && str.startsWith("+")) {
							str = str.substring(1);
						}
						vals[i] = BigFraction.valueOf(str);
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
				parts = token.split();
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
			return new PolymakeMatrixDataImpl(def, matrixType, rowData);
		}
	}

	public DefinitionType getDefinitionType() {
		if (polymakeMatrixData.length > 1) return DefinitionType.Both;
		return polymakeMatrixData[0].getDefinitionType();
	}
	public int getDim() {
		return dim;
	}
	public PolymakeMatrixData getData(int index) {
		return polymakeMatrixData[index];
	}
	public PolymakeMatrixData getData(DefinitionType type) {
		for (int i = 0; i < polymakeMatrixData.length; i++) {
			if (polymakeMatrixData[i].getDefinitionType().equals(type)) {
				return polymakeMatrixData[i];
			}
		}
		throw new IllegalArgumentException("cannot return data for type " + type);
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("polymake data, P = { dim:");
		sb.append(dim);
		for (int i = 0; i < polymakeMatrixData.length; i++) {
			sb.append(" , ");
			sb.append(polymakeMatrixData[i]);
		}
		sb.append(" }");
		return sb.toString();
	}
	
	private static enum Keyword {
		DIM, POINTS, VERTICES, FACETS;
		/**
		 * Returns true if the line matches this keyword. Whitespace at the end 
		 * of the line is ignored.
		 */
		public boolean matches(String line) {
			if (line == null) return false;
			if (line.startsWith(name())) {
				if (line.substring(name().length()).trim().length() == 0) {
					return true;
				}
			}
			return false;
		}
		/**
		 * Returns the keyword, if one is found in the given line, or null 
		 * otherwise. Whitespace at the end of the line is ignored.
		 */
		public static Keyword parse(String line) {
			for (final Keyword key : values()) {
				if (key.matches(line)) return key;
			}
			return null;
		}
		/**
		 * Returns the definition type corresponding to the keyword, or throws
		 * an exception if there is no matching definition type
		 */
		public DefinitionType getDefinitionType() {
			switch (this) {
				case VERTICES:
					return DefinitionType.Vertices;
				case POINTS:
					return DefinitionType.Vertices;
				case FACETS:
					return DefinitionType.Facets;
				default:
					throw new IllegalStateException("not supported for " + this);
			}
		}
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
		Keyword asKeyword() {
			return Keyword.parse(line);
		}
		String[] split() {
			return line.trim().split("\\s+");
		}
		IOException createIOException(String msg) {
			return new IOException("[" + lineNo + "] " + msg + ": " + (line == null ? "<end of file>" : line));
		}
	}
}
