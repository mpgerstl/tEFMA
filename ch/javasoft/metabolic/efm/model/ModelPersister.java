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
package ch.javasoft.metabolic.efm.model;

import java.io.File;
import java.io.IOException;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.smx.iface.ReadableMatrix;

/**
 * The <code>ModelPersister</code> stores model data to file(s), and restores 
 * the models from those files afterwards. A typical use case for model 
 * persisters is distributed computation. 
 */
public interface ModelPersister {
	
	/**
	 * Writes the stoichiometric matrix to the specified file.
	 * <p>
	 * The stoich matrix can be restored from this file by calling
	 * {@link #readStoich(File)}.
	 *   
	 * @param matrix		the stoichiometric matrix to persist
	 * @param file			the file to write to
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	void writeStoich(ReadableMatrix matrix, File file) throws IOException;
	
	/**
	 * Restores the stoichiometric matrix instance from file, written by
	 * {@link #writeStoich(ReadableMatrix, File)}
	 * 
	 * @param file	the file containing the stoich matrix
	 * @return	the restored stoich matrix instance
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	ReadableMatrix readStoich(File file) throws IOException;
	
	/**
	 * Writes the content of the specified efm model to the given efm model 
	 * file. Stoichiometric matrix and configuration settings are not stored,
	 * since separate methods exist to store/restore this information. 
	 * 
	 * <p>
	 * The efm model can be restored from this file by calling 
	 * {@link #readEfmModel(Config, ReadableMatrix, File)}. Note that the extra 
	 * information (such as members stored in subclasses of efm model) are not 
	 * stored neither restored by this process.
	 *   
	 * @param <N>			the number type
	 * @param <Col>			the column type
	 * @param columnHome	the column home determining number and column type
	 * @param model			the model to persist
	 * @param file			the efm model file
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	<N extends Number, Col extends Column> void writeEfmModel(ColumnHome<N, Col> columnHome, EfmModel model, File file) throws IOException;
	
	/**
	 * Restores the efm model from previously create file resulting from calling
	 * the {@link #writeEfmModel(ColumnHome, EfmModel, File)} method.
	 * <p>
	 * Note that the extra information (e.g. members stored in subclasses) are 
	 * not restored.
	 *   
	 * @param config		the configuration
	 * @param stoich		the stoich matrix
	 * @param file			the efm model file
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	EfmModel readEfmModel(Config config, ReadableMatrix stoich, File file) throws IOException;
	
	/**
	 * Writes the content of the specified adj enum model to the given file.
	 * <p>
	 * The adj enum model can be restored from those files by calling 
	 * {@link #readAdjEnumModel(ColumnHome, File, MemoryAccessor)}.
	 *   
	 * @param <N>			the number type
	 * @param <Col>			the column type
	 * @param columnHome	the column home determining number and column type
	 * @param model			the model to persist
	 * @param file			the file in which the model information is stored
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	<N extends Number, Col extends Column> void writeAdjEnumModel(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> model, File file) throws IOException;

	/**
	 * Restores the adj enum model from the previously created file. The file is
	 * created by calling the 
	 * {@link #writeAdjEnumModel(ColumnHome, AdjEnumModel, File) writeAdjEnumModel} 
	 * method.
	 * <p>
	 * Note that the extra information (e.g. members stored in subclasses) are 
	 * not restored.
	 *   
	 * @param columnHome		column home determining number and column type
	 * @param file				adj enum file
	 * @param memoryAccessor	access to different memories
	 * @throws IOException	if writing to the files causes an i/o exception
	 */
	<N extends Number, Col extends Column> AdjEnumModel<Col> readAdjEnumModel(ColumnHome<N, Col> columnHome, File file, MemoryAccessor<Col> memoryAccessor) throws IOException;
	
}
