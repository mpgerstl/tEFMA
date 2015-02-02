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
package ch.javasoft.metabolic.efm.dist.impl.file;

import java.io.IOException;
import java.util.Properties;

import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.memory.SortableMemory;
import ch.javasoft.metabolic.efm.model.AbstractModelPersister;
import ch.javasoft.metabolic.efm.model.AdjEnumModel;
import ch.javasoft.metabolic.efm.model.IterationStepModel;
import ch.javasoft.metabolic.efm.model.MemoryAccessor;
import ch.javasoft.util.ExceptionUtil;

/**
 * The <tt>FileBasedModelPersister</tt> reads and writes information needed for
 * the instantiation of the different models from/to different files. The 
 * memories are file based, and the memory files are reopened upon creation of 
 * an instance.
 */
public class FileBasedModelPersister extends AbstractModelPersister {

	/**
	 * Adds the memory file information to the properties object after calling
	 * the 
	 * {@link AbstractModelPersister#writeAdjEnumModelToProperties(ColumnHome, AdjEnumModel, Properties) superclass method},
	 * which adds iteration step model properties.
	 * 
	 * @see AbstractModelPersister#writeAdjEnumModel(ColumnHome, AdjEnumModel, java.io.File)
	 * @see AbstractModelPersister#writeAdjEnumModelToProperties(ColumnHome, AdjEnumModel, Properties)
	 */
	@Override
	protected <N extends Number, Col extends Column> void writeAdjEnumModelToProperties(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> model, Properties props) throws IOException {
		super.writeAdjEnumModelToProperties(columnHome, model, props);
		props.put("adj-enum-model-pos-class", model.getMemoryPos().getClass().getName());
		props.put("adj-enum-model-pos-fileid", model.getMemoryPos().fileId());
		props.put("adj-enum-model-neg-class", model.getMemoryNeg().getClass().getName());
		props.put("adj-enum-model-neg-fileid", model.getMemoryNeg().fileId());
		props.put("adj-enum-model-zero-class", model.getMemoryZero().getClass().getName());
		props.put("adj-enum-model-zero-fileid", model.getMemoryZero().fileId());
//		props.put("adj-enum-model-out-fileid", out.fileId());
	}
	
	@Override
	protected <N extends Number, Col extends Column> AdjEnumModel<Col> readAdjEnumModelFromProperties(ColumnHome<N, Col> columnHome, Properties props, IterationStepModel itModel, MemoryAccessor<Col> memoryAccessor) throws IOException {
		final SortableMemory<Col> pos = openMemory(columnHome, itModel, props, "adj-enum-model-pos");
		final SortableMemory<Col> neg = openMemory(columnHome, itModel, props, "adj-enum-model-neg");
		final SortableMemory<Col> zer = openMemory(columnHome, itModel, props, "adj-enum-model-zero");
		return new AdjEnumModel<Col>(itModel, pos, zer, neg, memoryAccessor.getAppendableMemory());
	}
	@SuppressWarnings("unchecked")
	private static <Col extends Column> SortableMemory<Col> openMemory(ColumnHome<?, Col> columnHome, IterationStepModel stepModel, Properties props, String propPrefix) throws IOException {
		final String className = props.getProperty(propPrefix + "-class");
		final String fileId = props.getProperty(propPrefix + "-fileid");
		try {
			final Class<?> clazz = Class.forName(className);
			final Object inst = clazz.getConstructor(String.class).newInstance(fileId);
			return (SortableMemory<Col>)inst;
		}
		catch (Exception ex) {
			throw ExceptionUtil.toIOException(ex);
		}
	}
	
}
