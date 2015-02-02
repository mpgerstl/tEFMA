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
package ch.javasoft.metabolic.parse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.compartment.CompartmentReaction;
import ch.javasoft.metabolic.impl.DefaultMetabolicNetwork;

public class ExcelParser {

	private static final Logger LOG = LogPkg.LOGGER;

	private final File			mFile;
	private final HSSFWorkbook	mWorkbook;
	private final HSSFSheet		mSheet;
	private final String		mSheetName;
	
	public ExcelParser(File file) throws FileNotFoundException, IOException {
		this(file, null);
	}
	
	/**
	 * 
	 * @param file						the excel file to parse
	 * @param sheetIndex				0 based index of the worksheet
	 * @throws FileNotFoundException	if the specified file does not exist
     * @throws IOException			if an i/o exception occurs, for instance 
     * 								caused by file access
	 */
	public ExcelParser(File file, int sheetIndex) throws FileNotFoundException, IOException {
		POIFSFileSystem fs	= new POIFSFileSystem(new FileInputStream(file));
		mWorkbook			= new HSSFWorkbook(fs);
		mSheet				= mWorkbook.getSheetAt(sheetIndex);
		mFile				= file;
		if (mSheet == null) throw new IOException("no sheet " + sheetIndex + " in excel file '" + file.getAbsolutePath() + "'");
		mSheetName			= mWorkbook.getSheetName(sheetIndex);
	}
	protected ExcelParser(File file, String worksheet) throws FileNotFoundException, IOException {
		POIFSFileSystem fs	= new POIFSFileSystem(new FileInputStream(file));
		mWorkbook			= new HSSFWorkbook(fs);
		mSheet				= worksheet == null ? mWorkbook.getSheetAt(0) : mWorkbook.getSheet(worksheet);
		mFile				= file;
		if (mSheet == null) throw new IOException("no sheet " + (worksheet == null ? "0" : "'" + worksheet + "'") + " in excel file '" + file.getAbsolutePath() + "'");
		mSheetName			= worksheet == null ? mWorkbook.getSheetName(0) : worksheet;
	}
	
	/**
	 * Parses the excel file and returns the resulting metabolic network,
	 * skipping <tt>headerRows</tt> rows, using the given columns to extract
	 * information from, column indices being 0-based.
	 * 
	 * @param reactionColumn		0-based column index for reaction formula
	 * @param reactionNameColumn	0-based column index for reaction name
	 * @param headerRows			number of header rows (being ignored)
	 * @throws IOException			if any unexpected exception occurs
	 */
	public MetabolicNetwork parse(int reactionColumn, int reactionNameColumn, int headerRows) throws IOException {
		return parse(reactionColumn, reactionNameColumn, headerRows, null);
	}
	/**
	 * Parses the excel file and returns the resulting metabolic network,
	 * skipping <tt>headerRows</tt> rows, using the given columns to extract
	 * information from, column indices being 0-based. The given pattern is
	 * used to recognize external metabolites; a reversible exchange flux 
	 * reaction is added to each external metabolite.
	 * 
	 * @param reactionColumn		0-based column index for reaction formula
	 * @param reactionNameColumn	0-based column index for reaction name
	 * @param headerRows			number of header rows (being ignored)
	 * @param externalPattern		pattern to recognize external metabolites
	 * @throws IOException			if any unexpected exception occurs
	 */
	public MetabolicNetwork parse(int reactionColumn, int reactionNameColumn, int headerRows, Pattern externalPattern) throws IOException {
		Set<String> reacNames = new HashSet<String>();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(buf));
		HSSFRow row;
		int rowIndex = headerRows;
		try {
			row = mSheet.getRow(rowIndex++);
			while (row != null) {
				HSSFCell nameCell = row.getCell((short)reactionNameColumn);
				HSSFCell formCell = row.getCell((short)reactionColumn);
				if (nameCell != null || formCell != null) {
					if (nameCell == null) {
						throw new IOException("reaction name cell " + (reactionNameColumn + 1) + " is empty");
					}
					if (formCell == null) {
						throw new IOException("reaction formula cell " + (reactionColumn + 1) + " is empty");
					}
					String name = nameCell.toString().trim();
					String form = formCell.toString().trim();
					if (name.length() > 0 && form.length() > 0) {
						if (reacNames.contains(name)) throw new Exception("duplicate reaction name: " + name);
						reacNames.add(name);
						pw.println("\"" + name + "\"\t\"" + name + "\"\t\"" + form + "\"");
						row = mSheet.getRow(rowIndex++);
					}
					else {
						LOG.info("row " + (rowIndex + 1) + " found empty in excel file '" + mFile.getAbsolutePath() + "', stopping here.");
						row = null;
					}					
				}
				else {
					LOG.info("row " + (rowIndex + 1) + " found empty in excel file '" + mFile.getAbsolutePath() + "', stopping here.");
					row = null;
				}				
			}
			pw.flush();
			ByteArrayInputStream in = new ByteArrayInputStream(buf.toByteArray());
			CompartmentReaction[] reacts = externalPattern == null ?
					new PalssonParser().parseReactions(new InputStreamReader(in)) :
					new PalssonParser().parseReactions(new InputStreamReader(in), externalPattern);
			return new DefaultMetabolicNetwork(reacts);			
		}
		catch (Exception ex) {
			IOException ioe = new IOException(ex.getMessage() + " [row " + (rowIndex + 1) + ", file=" + mFile.getAbsolutePath() + ", sheet=" + mSheetName + "]");
			ioe.initCause(ex);
			throw ioe;
		}
	}

}
