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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import ch.javasoft.io.Print;
import ch.javasoft.metabolic.efm.column.Column;
import ch.javasoft.metabolic.efm.column.ColumnHome;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.metabolic.efm.model.DefaultEfmModel.Init;
import ch.javasoft.metabolic.efm.util.MatrixUtil;
import ch.javasoft.smx.iface.ReadableMatrix;
import ch.javasoft.util.ExceptionUtil;

/**
 * Abstract default implementations for {@link ModelPersister}
 */
abstract public class AbstractModelPersister implements ModelPersister {
	
	//inherit javadoc
	public void writeStoich(ReadableMatrix matrix, File file) throws IOException {
		final OutputStream out =  new FileOutputStream(file);
		try {
	        MatrixUtil.writeMatrix(matrix, out);
		}
		finally {
			out.close();
		}
	}
	
	//inherit javadoc
	public <N extends Number, Col extends Column> void writeEfmModel(ColumnHome<N, Col> columnHome, EfmModel model, File file) throws IOException {
		final PrintWriter pw = Print.createWriter(file);
		try {
			pw.println(model.getClass().getName());
			pw.println(model.getStoichRank());
			pw.println(model.getReactionSorting().length);
			for (int i = 0; i < model.getReactionSorting().length; i++) {
				pw.println(model.getReactionSorting()[i]);
			}
			pw.println(model.getMetaboliteSorting().length);
			for (int i = 0; i < model.getMetaboliteSorting().length; i++) {
				pw.println(model.getMetaboliteSorting()[i]);
			}
			pw.println(model.getColumnInspectorModifierFactory().getClass().getName());
		}
		finally {
			pw.close();
		}
	}

	//inherit javadoc
	public ReadableMatrix readStoich(File file) throws IOException {
		final InputStream in = new FileInputStream(file);
		try {
			return MatrixUtil.readMatrix(in);
		}
		finally {
			in.close();
		}
	}
	
	//inherit javadoc
	public EfmModel readEfmModel(Config config, ReadableMatrix stoich, File file) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(file));
		
		@SuppressWarnings("unused")
		final String className = br.readLine();
		try {
//			final Class[] signature = new Class[] {Column.Home.class, MetabolicNetwork.class, Config.class};
			final Init init = new Init();
			init.stoichiometricMatrixRational = MatrixUtil.convertToBigIntegerRationalMatrix(stoich, config.zero(), false /*enforceNewInstance*/);
			init.stoichRank = Integer.parseInt(br.readLine());
			init.reactionSorting = new int[Integer.parseInt(br.readLine())];
			for (int i = 0; i < init.reactionSorting.length; i++) {
				init.reactionSorting[i] = Integer.parseInt(br.readLine());
			}
			init.metaboliteSorting = new int[Integer.parseInt(br.readLine())];
			for (int i = 0; i < init.metaboliteSorting.length; i++) {
				init.metaboliteSorting[i] = Integer.parseInt(br.readLine());
			}
			init.columnInspectorModifierFactory = (ColumnInspectorModifierFactory)Class.forName(br.readLine()).newInstance();
			init.config = config;
            return new DefaultEfmModel(init);
//            return (EfmModel)Class.forName(className).getConstructor(signature).newInstance(init);
		} 
		catch (InstantiationException e) {
			throw ExceptionUtil.toIOException(e);
		} 
		catch (IllegalAccessException e) {
			throw ExceptionUtil.toIOException(e);
		} 
		catch (ClassNotFoundException e) {
			throw ExceptionUtil.toIOException(e);
		} 
		finally {
			br.close();
		}
	}
	
	/**
	 * Only stores data defined by the {@link IterationStepModel}, information
	 * related to memory is not stored here. Subclasses or more specific 
	 * implementations should store such information.
	 * <p>
	 * This class creates a {@link Properties} object and passes it to
	 * {@link #writeAdjEnumModelToProperties(ColumnHome, AdjEnumModel, Properties)}
	 * before writing the properties to the specified file.
	 * 
	 * @see ModelPersister#writeAdjEnumModel(ColumnHome, AdjEnumModel, File)
	 */
	public <N extends Number, Col extends Column> void writeAdjEnumModel(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> model, File file) throws IOException {
		final Properties props = new Properties();
		writeAdjEnumModelToProperties(columnHome, model, props);
		props.store(new FileOutputStream(file), "adj-enum-model stored by " + getClass().getName());
	}
	
	/**
	 * Only stores data defined by the {@link IterationStepModel}, information
	 * related to memory is not stored here. Subclasses or more specific 
	 * implementations should store such information.
	 * <p>
	 * This method is called from {@link #writeAdjEnumModel(ColumnHome, AdjEnumModel, File)}.
	 * 
	 * @see #writeAdjEnumModel(ColumnHome, AdjEnumModel, File)
	 * @see ModelPersister#writeAdjEnumModel(ColumnHome, AdjEnumModel, File)
	 */
	protected <N extends Number, Col extends Column> void writeAdjEnumModelToProperties(ColumnHome<N, Col> columnHome, AdjEnumModel<Col> model, Properties props) throws IOException {
		final IterationStateModel cur = model.getCurrentState();
		final IterationStateModel nex = model.getNextState();
		props.put("adj-enum-model-iteration-index", String.valueOf(model.getIterationIndex()));
		props.put("adj-enum-model-hyperplane-index", String.valueOf(cur.getHyperplaneIndex()));
		props.put("adj-enum-model-next-hyperplane-index", String.valueOf(nex.getHyperplaneIndex()));
		props.put("adj-enum-model-boolean-size", String.valueOf(cur.getBooleanSize()));
		props.put("adj-enum-model-next-boolean-size", String.valueOf(nex.getBooleanSize()));
		props.put("adj-enum-model-numeric-size", String.valueOf(cur.getNumericSize()));
		props.put("adj-enum-model-next-numeric-size", String.valueOf(nex.getNumericSize()));
	}
	
	public <N extends Number, Col extends Column> AdjEnumModel<Col> readAdjEnumModel(ColumnHome<N, Col> columnHome, File file, MemoryAccessor<Col> memoryAccessor) throws IOException {
		final Properties props = new Properties();
		final FileInputStream in = new FileInputStream(file);
		try {
			props.load(in);//use stream, writer is supported only in jdk6+
		}
		finally {
			in.close();
		}
		final int iterationIndex 		= Integer.parseInt(props.getProperty("adj-enum-model-iteration-index"));
		final int hyperplaneIndex 		= Integer.parseInt(props.getProperty("adj-enum-model-hyperplane-index"));
		final int nextHyperplaneIndex 	= Integer.parseInt(props.getProperty("adj-enum-model-next-hyperplane-index"));
		final int booleanSize 			= Integer.parseInt(props.getProperty("adj-enum-model-boolean-size"));
		final int nextBooleanSize 		= Integer.parseInt(props.getProperty("adj-enum-model-next-boolean-size"));
		final int numericSize 			= Integer.parseInt(props.getProperty("adj-enum-model-numeric-size"));
		final int nextNumericSize 		= Integer.parseInt(props.getProperty("adj-enum-model-next-numeric-size"));
		final IterationStateModel cur = new DefaultIterationStateModel(hyperplaneIndex, booleanSize, numericSize);
		final IterationStateModel nex = new DefaultIterationStateModel(nextHyperplaneIndex, nextBooleanSize, nextNumericSize);
		final IterationStepModel itModel = new DefaultIterationStepModel(iterationIndex, cur, nex);
		return readAdjEnumModelFromProperties(columnHome, props, itModel, memoryAccessor);
	}
	
	abstract protected <N extends Number, Col extends Column> AdjEnumModel<Col> readAdjEnumModelFromProperties(ColumnHome<N, Col> columnHome, Properties properties, IterationStepModel itModel, MemoryAccessor<Col> memoryAccessor) throws IOException;
	
}
