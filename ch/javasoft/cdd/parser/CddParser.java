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
package ch.javasoft.cdd.parser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.javasoft.math.NumberMatrixConverter;

/**
 * The {@code CddParser} parses cdd {@code .ine} or {@code .ext} files, that is,
 * polyhedra definition files in <b>ine</b>quality or in <b>ext</b>reme 
 * ray/vertex form. For exact definitions of the file formats, see
 * <pre>ftp://ftp.ifor.math.ethz.ch/pub/fukuda/cdd/cddman/node3.html</pre>
 * 
 * A sample file looks as follows:
 * <pre>
////////////////////////////////////
* file name: ucube.ine
* 3 cube without one "lid"
H-representation
begin
    6      4    integer
  2   -1   0   0
  2    0  -1   0
 -1    1   0   0
 -1    0   1   0
 -1    0   0   1
  4   -1  -1   0
end
incidence
adjacency
input_adjacency
input_incidence
////////////////////////////////////
*file name: ccc4.ext
*Complete cut cone on 4 vertices
*The number of facets is 12.
V-representation
begin
    7    7    integer
 0  1  1  1  0  0  0 
 0  0  1  1  1  1  0 
 0  1  0  1  1  0  1 
 0  1  1  0  0  1  1 
 0  0  0  1  0  1  1 
 0  0  1  0  1  0  1 
 0  1  0  0  1  1  0
end
hull
incidence
adjacency
////////////////////////////////////
 * </pre>
 */
public class CddParser {
	private final Object[] 			matrix;	
	private final CddFileType 		fileType;
	private final CddNumberFormat	numberFormat;
	
	public CddParser(File file) throws FileNotFoundException, IOException {
		this(new FileInputStream(file));
	}
	
	public CddParser(InputStream in) throws IOException {
		this(new InputStreamReader(in));
	}

	public CddParser(Reader reader) throws IOException {
		this(reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader));
	}
	public CddParser(BufferedReader reader) throws IOException {
		CddFileType fileType = null;
		String line; int lineNo = 0;
		//skip the lines before the preamble
		while ((line = reader.readLine()) != null && (fileType = CddFileType.parsePreamble(line)) == null) {
			lineNo++;
		}
		if (fileType != null) {
			//skip preamble line
			line = reader.readLine();lineNo++;
		}
		else {
			throw createIOException(CddFileType.Ine.preamble + " or " + CddFileType.Ext.preamble + " expected", lineNo, line);
		}
		if (line != null && Keyword.begin.matches(line)) {
			line = reader.readLine();lineNo++;
		}
		else {
			throw createIOException(Keyword.begin + " expected", lineNo, line);
		}
		int m, dims;
		CddNumberFormat numberFormat;
		//    6      4    integer		
		Matcher matcher = Pattern.compile("\\s*([0-9]+)\\s+([0-9]+)\\s+(\\w+)\\s*").matcher(line);
		if (!matcher.matches()) {
			throw createIOException("preamble syntax error", lineNo, line);
		}
		m		= Integer.parseInt(matcher.group(1));
		dims	= Integer.parseInt(matcher.group(2));
		String sFormat = matcher.group(3);//integer, rational, real
		try {
			numberFormat = CddNumberFormat.valueOf(sFormat);
		}
		catch (Exception ex) {
			throw createIOException("unsupported format in preamble, e=" + ex, lineNo, line);				
		}
		
		this.matrix = numberFormat.newMatrix(m, dims);
		int row = 0;
		int col = 0;
		while ((line = reader.readLine()) != null && !Keyword.end.matches(line)) {
			lineNo++;
			String[] parts = line.trim().split("\\s+");
			for (int i = 0; i < parts.length; i++) {
				try {
					numberFormat.parseAndSet(matrix, row, col, parts[i]);
				}
				catch (Exception ex) {
					throw createIOException("cannot parse number value \"" + parts[i] + "\", e=" + ex, lineNo, line);
				}
				col++;
				if (col == dims) {
					col = 0;
					row++;
				}
			}
		}
		if (!Keyword.end.matches(line)) {
			throw createIOException("unexpected end of file, " + Keyword.end + " keyword expected", lineNo, line);			
		}
		
		this.fileType 		= fileType;
		this.numberFormat 	= numberFormat;
	}
	
	private static IOException createIOException(String msg, int lineNo, String line) {
		return new IOException("[" + lineNo + "] " + msg + ": " + (line == null ? "<end of file>" : line));
	}
	/**
	 * Returns the cdd matrix {@code M} of the polyhedral cone. The cone is 
	 * either defined in hyperplane (ine) or extreme ray/vertex (ext) format:
	 * <ul>
	 * 	<li>ine: <tt>P = { b - Ax &ge; 0} = { Ax &leq; b }, M=[b -A]</tt></li>
	 * 	<li>ext: <tt>P = conv(v) + nonneg(r), M=[ v ; r ], v(*,0)=1, r(*,0)=0</tt></li>
	 * </ul>
	 */
	public Object[] getMatrix() {
		return matrix;
	}
	/**
	 * Casts the matrix into an array of the specified class and returns it. The 
	 * returned matrix is the internally kept matrix instance. Consider using 
	 * {@link #getMatrixConverted(NumberMatrixConverter)} instead.
	 * <p>
	 * The polyhedral cone is either defined in hyperplane (ine) or extreme 
	 * ray/vertex (ext) format. The returned matrix {@code M} defines a cone as
	 * follows:
	 * <ul>
	 * 	<li>ine: <tt>P = { b - Ax &ge; 0} = { Ax &leq; b }, M=[b -A]</tt></li>
	 * 	<li>ext: <tt>P = conv(v) + nonneg(r), M=[ v ; r ], v(*,0)=1, r(*,0)=0</tt></li>
	 * </ul>
	 * 
	 * @param	arrayClass	the class of the array into which the matrix is cast 
	 * @return the matrix {@code M} as described above
	 * @throws ClassCastException	if the matrix is not an array instance of 
	 * 								the specified class
	 *  
	 * @see #getMatrix()
	 * @see #getMatrixConverted(NumberMatrixConverter)
	 */
	public <A> A[] getMatrixCast(Class<A> arrayClass) throws ClassCastException {
		return CddNumberFormat.castMatrix(arrayClass, matrix);
	}
	/**
	 * Converts the matrix using the specified converter and returns it. The 
	 * returned matrix is always a new instance. Consider using 
	 * {@link #getMatrixCast(Class)} instead.
	 * <p>
	 * The polyhedral cone is either defined in hyperplane (ine) or extreme 
	 * ray/vertex (ext) format. The returned matrix {@code M} defines a cone as
	 * follows:
	 * <ul>
	 * 	<li>ine: <tt>P = { b - Ax &ge; 0} = { Ax &leq; b }, M=[b -A]</tt></li>
	 * 	<li>ext: <tt>P = conv(v) + nonneg(r), M=[ v ; r ], v(*,0)=1, r(*,0)=0</tt></li>
	 * </ul>
	 * 
	 * @param	converter	the converter used to create the matrix and to 
	 * 						convert the values into the desired format
	 * @return the matrix {@code M} as described above
	 *  
	 * @see #getMatrix()
	 * @see #getMatrixCast(Class)
	 */
	public <A> A[] getMatrixConverted(NumberMatrixConverter<A> converter) throws ClassCastException {
		final int rows = matrix.length;
		final int cols = numberFormat.getColumnCount(matrix);
		final A[] dst = converter.newMatrix(rows, cols);
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				final Number val = numberFormat.getNumber(matrix, r, c);
				converter.copy(val, dst, r, c);
			}
		}
		return dst;
	}
	public CddFileType getFileType() {
		return fileType;
	}
	public CddNumberFormat getNumberFormat() {
		return numberFormat;
	}
	
	@Override
	public String toString() {
		final int rows = matrix.length;
		final int cols = numberFormat.getColumnCount(matrix);
		final StringBuilder sb = new StringBuilder();
		sb.append("cdd data, P = { ");
		sb.append(fileType).append(":");
		sb.append(rows).append('x').append(cols);
		sb.append(" (").append(numberFormat).append(")");
		sb.append(" }");
		return sb.toString();
	}
	
	private enum Keyword {
		begin, end;
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
	}
	
}
