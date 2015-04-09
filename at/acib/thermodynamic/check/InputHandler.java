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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import at.acib.thermodynamic.exception.ConcentrationErrorException;

/**
 * read input files
 *
 * @author matthias
 *
 */
public class InputHandler {

    private final static String CONC_FILE_SEPARATOR = ";";
    private final static String CONC_FILE_COMMENT = "#";
    private final static int CONC_IND_ABBREVIATION = 0;
    private final static int CONC_IND_NAME = 1;
    private final static int CONC_IND_STD_MIN = 2;
    private final static int CONC_IND_STD_MAX = 3;

    /**
     * original stoichiometric matrix
     */
    private double[][] m_sMatrix;
    /**
     * sorted single reactions
     */
    private String[] m_reactionList;
    /**
     * sorted compressed reactions
     */
    private String[] m_compressedReactionList;
    /**
     * {@link HashMap} < name of metabolite, instance of {@link Metabolite} >
     */
    private HashMap<String, Metabolite> m_metabolites;
    /**
     * {@link HashMap} < original index of metabolite, name of metabolite >
     */
    private HashMap<Integer, String> m_metaboliteOrder;
    /**
     * {@link HashMap} < original index of single reaction, name of single
     * reaction >
     */
    private HashMap<Integer, String> m_reactionOrder;
    /**
     * {@link HashMap} < original index of compressed reaction, name of
     * compressed reaction >
     */
    private HashMap<Integer, String> m_compressedReactionOrder;
    /**
     * {@link HashSet} < dead end columns in stoichiometric matrix >
     */
    private HashSet<Integer> m_orphanReaction;

    /**
     * Constructor
     *
     * @param sMatrix
     *            original stoichiometric matrix
     * @param metaboliteList
     *            metabolite list according stoichiometric matrix
     * @param reactionList
     *            reaction list according to stoichiometric matrix
     * @param compressedReactionList
     *            compressed reaction list according to compressed
     *            stoichiometric matrix
     * @param concentrationFile
     *            filename of metabolite concentration file
     * @param stdmin
     *            standard minimum concentration of metabolites, if no
     *            concentration is given
     * @param stdmax
     *            standard maximum concentration of metabolites, if no
     *            concentration is given
     * @param gibbsCalculator
     *            instance of {@link GibbsCalculator}
     * @throws ConcentrationErrorException
     */
    public InputHandler(double[][] sMatrix, String[] metaboliteList,
            String[] reactionList, String[] compressedReactionList,
            String concentrationFile, double stdmin, double stdmax,
            String proton, GibbsCalculator gibbsCalculator)
            throws ConcentrationErrorException {
        initialize();
        m_sMatrix = sMatrix;
        m_reactionList = reactionList;
        m_compressedReactionList = compressedReactionList;
        defineOrphans(m_sMatrix);
        defineHashMap(metaboliteList, m_metaboliteOrder);
        defineHashMap(reactionList, m_reactionOrder);
        defineHashMap(compressedReactionList, m_compressedReactionOrder);
        try {
            m_metabolites = arrayList2MetaboliteList(
                    readFile(concentrationFile), gibbsCalculator, stdmin,
                    stdmax, proton);
        } catch (IOException e) {
            System.out.println("Could not read file:\n" + e);
        }
    }

    private void initialize() {
        m_metabolites = new HashMap<String, Metabolite>();
        m_metaboliteOrder = new HashMap<Integer, String>();
        m_reactionOrder = new HashMap<Integer, String>();
        m_compressedReactionOrder = new HashMap<Integer, String>();
        m_orphanReaction = new HashSet<Integer>();
    }

    /**
     * converts an array to an HashMap where index turns to key
     *
     * @param list
     *            String array containing the information
     * @param map
     *            {@link HashMap} to store the information
     */
    private void defineHashMap(String[] list, HashMap<Integer, String> map) {
        for (int i = 0; i < list.length; i++) {
            map.put(i, list[i]);
        }
    }

    /**
     * define orphan reactions in the stoichiometric matrix
     *
     * @param stoich
     *            stoichiometric matrix
     * @return integer array of orphan reactions
     */
    private void defineOrphans(double[][] stoich) {
        int metabolites = stoich.length;
        int reactions = stoich[0].length;
        for (int i_rx = 0; i_rx < reactions; i_rx++) {
            boolean prod = false;
            boolean reac = false;
            for (int i_metab = 0; i_metab < metabolites; i_metab++) {
                if (stoich[i_metab][i_rx] > 0) {
                    reac = true;
                    if (prod) {
                        i_metab = metabolites;
                    }
                } else if (stoich[i_metab][i_rx] < 0) {
                    prod = true;
                    if (reac) {
                        i_metab = metabolites;
                    }
                }
            }
            if (!reac || !prod) {
                m_orphanReaction.add(i_rx);
            }
        }
    }

    /**
     * read a file and store each line as element in an ArrayList
     *
     * @param filename
     * @return ArrayList containing the lines of the file
     * @throws IOException
     */
    private ArrayList<String> readFile(String filename) throws IOException {
        ArrayList<String> text = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            text.add(line);
        }
        br.close();
        return text;
    }

    /**
     * @return stoichiometric matrix
     */
    protected double[][] getStoichMatrix() {
        return m_sMatrix;
    }

    /**
     * @return {@link HashSet} < dead end columns in stoichiometric matrix >
     */
    protected HashSet<Integer> getOrphans() {
        return m_orphanReaction;
    }

    /**
     * @return {@link HashMap} < name of metabolite, instance of
     *         {@link Metabolite} >
     */
    protected HashMap<String, Metabolite> getMetabolites() {
        return m_metabolites;
    }

    /**
     * @return {@link HashMap} < original index of metabolite, name of
     *         metabolite >
     */
    protected HashMap<Integer, String> getMetaboliteOrder() {
        return m_metaboliteOrder;
    }

    /**
     * @return sorted single reactions
     */
    protected String[] getReactionList() {
        return m_reactionList;
    }

    /**
     * @return sorted compressed reactions
     */
    protected String[] getCompressedReactionList() {
        return m_compressedReactionList;
    }

    /**
     * read concentration file and create metabolites
     *
     * @param conc
     *            content of concentration file
     * @return {@link HashMap} < abbreviation of metabolite, instance of
     *         {@link Metabolite} >
     */
    private HashMap<String, Metabolite> arrayList2MetaboliteList(
            ArrayList<String> conc, GibbsCalculator gibbsCalculator,
            double stdmin, double stdmax, String proton)
            throws ConcentrationErrorException {
        HashMap<String, Metabolite> m = new HashMap<String, Metabolite>();
        boolean protonFound = false;
        for (int i = 0; i < conc.size(); i++) {
            String line = conc.get(i);
            line = line.trim();
            if (!line.startsWith(CONC_FILE_COMMENT)
                    && line.contains(CONC_FILE_SEPARATOR)) {
                String[] cells = line.split(CONC_FILE_SEPARATOR);
                if (cells.length > 3) {
                    String abbr = cells[CONC_IND_ABBREVIATION];
                    String name = cells[CONC_IND_NAME];
                    double cmin = stdmin;
                    double cmax = stdmax;
                    boolean m_proton = abbr.equals(proton) ? true : false;
                    if (m_proton) {
                        protonFound = true;
                    }
                    if (cells.length > 3) {
                        cmin = Double.valueOf(cells[CONC_IND_STD_MIN]);
                        cmax = Double.valueOf(cells[CONC_IND_STD_MAX]);
                    }
                    Double dfG = gibbsCalculator.getDfG(name);
                    if (dfG != null) {
                        m.put(abbr, new Metabolite(abbr, name, cmin, cmax, dfG,
                                m_proton));
                    } else if (m_proton) {
                        m.put(abbr, new Metabolite(abbr, name, cmin, cmax, 0,
                                m_proton));
                    }
                }
            }
        }
        if (!protonFound) {
            m.put(proton, new Metabolite(proton, proton, 0, 0, 0, true));
        }
        return m;
    }

}
