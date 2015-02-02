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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * read thermodynamic data file and define species information
 * 
 * @author matthias
 */
public class SpeciesDataReader {

	/**
	 * {@link HashMap} < name of species, instance of {@link Species} >
	 */
	private HashMap<String, Species> m_species;

	/**
	 * read the thermodynamic data file and stores values in a HashMap
	 * 
	 * @param thermodynamicDataFilename
	 */
	public SpeciesDataReader(String thermodynamicDataFilename) {

		m_species = new HashMap<String, Species>();

		// read species data
		try {
			BufferedReader br = new BufferedReader(new FileReader(thermodynamicDataFilename));
			try {
				String line = br.readLine();
				while (line != null) {
					Species spec = readLine(line);
					if (spec != null) {
						m_species.put(spec.getName(), spec);
					}
					line = br.readLine();
				}
			} finally {
				br.close();
			}
		} catch (IOException e) {
			System.out.println("Could not read file: " + e.toString());
			System.exit(-1);
		}

	}

	/**
	 * convert a inputline to a species
	 * 
	 * @param line
	 *            of the file
	 * @return Species of the inputline
	 */
	private Species readLine(String line) {
		try {
			if (line.contains("=")) {
				String[] splitA = line.split("=");
				String name = splitA[0];
				splitA[1] = splitA[1].replace(" ", "");
				String[] splitB = splitA[1].split("\\)");
				int c = splitB.length;
				double[] dGzero = new double[c];
				int[] zi = new int[c];
				int[] nH = new int[c];
				for (int i = 0; i < c; i++) {
					String[] splitC = splitB[i].split("\\(");
					String[] splitD = splitC[splitC.length - 1].split(",");
					for (int j = 0; j < splitD.length; j++) {
						splitD[j] = splitD[j].replace(" ", "");
					}
					dGzero[i] = Double.parseDouble(splitD[0]);
					zi[i] = Integer.parseInt(splitD[1]);
					nH[i] = Integer.parseInt(splitD[2]);
				}
				return new Species(name, dGzero, zi, nH);
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println("Error in following thermodynamic file line: ");
			System.out.println(line);
			System.exit(-1);
		}
		return null;
	}

	/**
	 * @return {@link HashMap} < name of species, instance of {@link Species} >
	 */
	public HashMap<String, Species> getSpecies() {
		return m_species;
	}

}
