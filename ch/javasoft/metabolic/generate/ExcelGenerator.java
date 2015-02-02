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
package ch.javasoft.metabolic.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Metabolite;
import ch.javasoft.metabolic.Reaction;
import ch.javasoft.metabolic.compartment.CompartmentMetabolicNetwork;
import ch.javasoft.metabolic.compartment.CompartmentMetabolite;

public class ExcelGenerator {
	
//	/private static final Charset CHARSET_UTF_16 = Charset.forName("UTF-16");
	
	private final MetabolicNetwork	net;
	private final HSSFWorkbook 		workbook;
	private final HSSFCellStyle 	styleBoldItalic; 
	
	public ExcelGenerator(MetabolicNetwork net) {
		this.net = net;
		workbook = new HSSFWorkbook();
		styleBoldItalic = getCellStyle(HSSFFont.BOLDWEIGHT_BOLD, true);
		initMetabolites();
		initReactions();
	}
	
	
	protected HSSFCellStyle getCellStyle(short boldweight, boolean italic) {
		final HSSFCellStyle style = workbook.createCellStyle();
		final HSSFFont font = workbook.createFont();
		font.setBoldweight(boldweight);
		font.setItalic(italic);
		style.setFont(font);
		return style;
	}
	protected static HSSFCell getCell(HSSFSheet sheet, int row, int col, boolean create) {
		HSSFRow xlsRow = sheet.getRow(row);
		if (xlsRow == null) {
			if (!create) return null;
			xlsRow = sheet.createRow(row);
		}
		HSSFCell xlsCell = xlsRow.getCell((short)col);
		if (xlsCell == null) {
			if (!create) return null;
			xlsCell = xlsRow.createCell((short)col);
		}
		return xlsCell;
	}
	protected static void writeToCell(HSSFSheet sheet, int row, int col, String value) {
		writeToCell(sheet, row, col, value, null);
	}
	protected static void writeToCell(HSSFSheet sheet, int row, int col, String value, HSSFCellStyle style) {
		final HSSFCell xlsCell = getCell(sheet, row, col, true);
		xlsCell.setCellType(HSSFCell.CELL_TYPE_STRING);
//		xlsCell.setEncoding(HSSFCell.ENCODING_UTF_16);
//		xlsCell.setCellValue(new String(value.getBytes(CHARSET_UTF_16), CHARSET_UTF_16));
		xlsCell.setCellValue(new HSSFRichTextString(value));
		if (style != null) {
			xlsCell.setCellStyle(style);
		}
	}
	
	protected void initMetabolites() {
		final HSSFSheet sheet = workbook.createSheet("Metabolites");
		String compartmentName = null;
		boolean hasDescription = false;
		boolean hasCompartment = false;
		boolean hasCompartmentFull = false;
		
		final CompartmentMetabolicNetwork cnet = (net instanceof CompartmentMetabolicNetwork) ?
			(CompartmentMetabolicNetwork)net : null;
			
		//check whether we have compartments/descriptions
		for (final Metabolite meta : net.getMetabolites()) {
			hasDescription |= (meta.getDescription() != null);
			if (meta instanceof CompartmentMetabolite) {
				String cmp = ((CompartmentMetabolite)meta).getCompartment();
				if (compartmentName == null) compartmentName = cmp;
				else hasCompartment = (!compartmentName.equals(cmp));
				if (cnet != null) {
					if (cmp != null) {
						hasCompartmentFull = !cmp.equals(cnet.getCompartmentFullName(cmp));
					}
				}
			}
		}
		
		final int colName = 0;
		final int colDesc = 1;
		final int colComp = hasDescription ? colDesc + 1 : colDesc;
		final int colCFul = hasCompartment ? colComp + 1 : -1;

		//write headers
		writeToCell(sheet, 0, colName, "Name", styleBoldItalic);
		if (hasDescription) writeToCell(sheet, 0, colDesc, "Description", styleBoldItalic);
		if (hasCompartment) writeToCell(sheet, 0, colComp, "Compartment", styleBoldItalic);
		if (hasCompartmentFull) writeToCell(sheet, 0, colCFul, "Compartment Description", styleBoldItalic);

		//write data
		int row = 1;
		for (final Metabolite meta : net.getMetabolites()) {
			writeToCell(sheet, row, colName, meta.getName());
			if (hasDescription) {
				if (meta.getDescription() != null) {
					writeToCell(sheet, row, colDesc, meta.getDescription());
				}
			}
			if (hasCompartment) {
				if (meta instanceof CompartmentMetabolite) {
					final String cmp = ((CompartmentMetabolite)meta).getCompartment();
					final String ful = cnet != null ? cnet.getCompartmentFullName(cmp) : null;
					if (cmp != null) {
						writeToCell(sheet, row, colComp, cmp);						
					}
					if (hasCompartmentFull && ful != null) {
						writeToCell(sheet, row, colCFul, ful);						
					}
				}
			}
			row++;
		}
	}
	protected void initReactions() {
		final HSSFSheet sheet = workbook.createSheet("Reactions");

		boolean hasFullName = false;
		for (final Reaction reac : net.getReactions()) {
			hasFullName |= (!reac.getName().equals(reac.getFullName()));
		}
		
		final int colName = 0;
		final int colFull = 1;
		final int colForm = hasFullName ? colFull + 1 : colFull;
		
		//write headers
		writeToCell(sheet, 0, colName, "Name", styleBoldItalic);
		if (hasFullName) writeToCell(sheet, 0, colFull, "Full Name", styleBoldItalic);
		writeToCell(sheet, 0, colForm, "Formula", styleBoldItalic);
		
		//write data
		int row = 1;
		for (final Reaction reac : net.getReactions()) {
			writeToCell(sheet, row, colName, reac.getName());
			if (hasFullName && reac.getFullName() != null) {
				writeToCell(sheet, row, colFull, reac.getFullName());
			}
			writeToCell(sheet, row, colForm, reac.toString());
			row++;
		}
	}
	public void writeTo(File file) throws IOException {
		writeTo(new FileOutputStream(file));
	}
	public void writeTo(OutputStream out) throws IOException {
		workbook.write(out);
		out.flush();
	}
}
