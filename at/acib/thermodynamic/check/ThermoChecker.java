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

import at.acib.thermodynamic.*;

import ilog.concert.IloException;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * check modes on thermodynamic feasibility
 *
 * @author matthias
 *
 */
public class ThermoChecker {

    private final static double R = 8.31451;
    public final static String RX_PREFIX = "RX_";
    public final static String RX_REV_PREFIX = "revRX_";

    // private boolean m_objectiveSet;
    private int m_formationStartInd;
    private int m_reactionStartInd;
    private int m_drgConstrStartIndex;
    private int m_dfGConstrSize;
    private int m_finalBooleanSize;
    private double m_rtNeg;
    private double m_temperature;
    private double m_ionStrength;
    private double m_pH;
    private double[] m_lb;
    private double[] m_ub;
    private double[][] m_stoich;
    private CplexHandler m_solver;
    private InputHandler m_iHandler;
    private ArrayList<ReactionBuildHelper> m_reactionBuildHelper;
    private PatternConverter m_patternConverter;

    /**
     * {@link HashMap} < name of metabolite, instance of {@link LpFormation} >
     */
    private HashMap<String, LpFormation> m_lpFormationHash;
    /**
     * {@link HashMap} < name of metabolite, index in lp problem >
     */
    private HashMap<String, Integer> m_activeMetaboliteNameMap;
    /**
     * {@link HashMap} < index in lp problem, name of metabolite >
     */
    private HashMap<Integer, String> m_activeMetaboliteReverseNameMap;
    /**
     * {@link HashMap} < row in stoichiometric matrix, name > of metabolites
     */
    private HashMap<Integer, String> m_metaboliteOrder;
    /**
     * {@link HashMap} < index of compressed reaction, indices of single
     * reactions > of compressed to single reactions
     */
    private HashMap<Integer, HashSet<Integer>> m_activeCompressedReactionMap;
    /**
     * {@link HashMap} < index of single reaction, name of the reaction > of
     * reaction names
     */
    private HashMap<Integer, String> m_singleReactionNameMap;
    /**
     * {@link HashMap} < position in lp problem, row in stoichiometric matrix >
     * of metabolites in lp problem
     */
    private HashMap<Integer, String> m_variableNames;
    /**
     * {@link HashMap} < name of metabolite, instance of {@link Metabolite} > of
     * metabolites
     */
    private HashMap<String, Metabolite> m_metaboliteInfo;
    /**
     * {@link HashMap} < position in lp problem, instance of {@link LpReaction}
     * > of reaction in lp problem
     */
    private HashMap<Integer, LpReaction> m_lpReactions;
    /**
     * {@link HashMap} < name of compartment, instance of
     * {@link GibbsCalculator} > of GibbsCalculator
     */
    private GibbsCalculator m_gibbsC;
    /**
     * sorted array of active metabolite indices (rows in stoichiometric matrix)
     */
    private int[] m_activeMetaboliteKeys;
    /**
     * sorted indices of active single reactions (columns in stoichiometric
     * matrix)
     */
    private int[] m_activeSingleReactionKeys;
    /**
     * {@link HashSet} < index of original reaction >
     */
    private HashSet<Integer> m_activeCompressedSingleReactions;
    /**
     * {@link HashMap} < index in stoichiometric matrx, index of lp problem >
     */
    private HashMap<Integer, Integer> m_activeSingleReactionMap;
    /**
     * {@link HashMap} < position in mode, instance of
     * {@link PredefinedReaction} >
     */
    private HashMap<Integer, PredefinedReaction> m_predefinedReactions;

    /**
     * Constructor
     *
     * @param temperature
     *            [K]
     * @param iHandler
     *            instance of {@link InputHandler}
     * @param gibbsC
     *            {@link HashMap} < {@link String} compartment, instance of
     *            {@link GibbsCalculator} >
     * @param lpFile
     *            name of outputfile for lp problem
     * @param lpVariableFile
     *            name of outputfile for lp variables
     */
    public ThermoChecker(double temperature, double ionStrength, double pH,
            InputHandler iHandler, GibbsCalculator gibbsC, String lpFile,
            String lpVariableFile) {
        m_patternConverter = null;
        m_finalBooleanSize = 0;
        m_gibbsC = gibbsC;
        m_temperature = temperature;
        m_ionStrength = ionStrength;
        m_pH = pH;
        m_iHandler = iHandler;
        initialize();
        try {
            defineModel(lpFile, lpVariableFile);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Constructor
     *
     * @param temperature
     *            [K]
     * @param iHandler
     *            instance of {@link InputHandler}
     * @param gibbsC
     *            {@link HashMap} < {@link String} compartment, instance of
     *            {@link GibbsCalculator} >
     * @param lpFile
     *            name of outputfile for lp problem
     * @param lpVariableFile
     *            name of outputfile for lp variables
     * @param patternConverter
     *            instance of {@link PatternConverter}
     */
    public ThermoChecker(double temperature, double ionStrength, double pH,
            InputHandler iHandler, GibbsCalculator gibbsC, String lpFile,
            String lpVariableFile, PatternConverter patternConverter,
            int finalBooleanSize) {
        m_patternConverter = patternConverter;
        m_finalBooleanSize = finalBooleanSize;
        m_gibbsC = gibbsC;
        m_temperature = temperature;
        m_ionStrength = ionStrength;
        m_pH = pH;
        m_iHandler = iHandler;
        initialize();
        try {
            defineModel(lpFile, lpVariableFile);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
        predefineCompressedReactions(finalBooleanSize);
    }

    /**
     * @return new instance of ThermoChecker
     */
    protected ThermoChecker clone() {
        return new ThermoChecker(m_temperature, m_ionStrength, m_pH,
                m_iHandler, m_gibbsC, null, null, m_patternConverter,
                m_finalBooleanSize);
    }

    /**
     * initialize variables and objects
     */
    private void initialize() {
        // m_objectiveSet = false;
        m_rtNeg = -1 * R * m_temperature / 1000;
        m_variableNames = new HashMap<Integer, String>();
        m_lpReactions = new HashMap<Integer, LpReaction>();

        m_metaboliteInfo = m_iHandler.getMetabolites();
        m_metaboliteOrder = m_iHandler.getMetaboliteOrder();
        m_stoich = m_iHandler.getStoichMatrix();
        String[] reactionList = m_iHandler.getReactionList();
        String[] compressedReactionList = m_iHandler
                .getCompressedReactionList();
        ActiveReaction active = new ActiveReaction(m_stoich, m_metaboliteInfo,
                m_metaboliteOrder, m_iHandler.getOrphans(), reactionList,
                compressedReactionList);
        // map
        m_activeCompressedReactionMap = active.getActiveCompressedReactionMap();
        m_singleReactionNameMap = active.getSingleReactionNameMap();
        m_activeSingleReactionMap = active.getActiveReactionMap();
        m_activeMetaboliteNameMap = active.getActiveMetaboliteNameMap();
        m_activeMetaboliteReverseNameMap = active
                .getActiveMetaboliteReverseNameMap();
        // keys
        m_activeCompressedSingleReactions = active
                .getActiveCompressedSingleReactions();
        m_activeMetaboliteKeys = active.getActiveMetaboliteKeys();
        m_activeSingleReactionKeys = active.getActiveReactionKeys();
        m_predefinedReactions = new HashMap<Integer, PredefinedReaction>();
    }

    /**
     * @return list of reactions that are already infeasible at start
     */
    protected ArrayList<String> getInitialInfeasibleReactions() {
        ArrayList<String> infeasible = new ArrayList<String>();
        ArrayList<ArrayList<String>> allInf = new ArrayList<ArrayList<String>>();
        ArrayList<BitSet> infPattern = new ArrayList<BitSet>();
        for (int i = 0; i < m_finalBooleanSize; i++) {
            BitSet mode = new BitSet(m_finalBooleanSize);
            mode.set(i);
            mode.flip(0, m_finalBooleanSize);
            if (!isFeasible(mode, m_finalBooleanSize)) {
                String infString = m_patternConverter.getPatternFromBitSet(
                        mode, m_finalBooleanSize);
                infeasible.add(infString);
                ArrayList<String> conflicts = getConflictReactions();
                allInf.add(conflicts);
                BitSet x = new BitSet(m_finalBooleanSize);
                x.set(i);
                x.flip(0, m_finalBooleanSize);
                infPattern.add(getConflictBitPattern(x, m_finalBooleanSize,
                        conflicts));
            }
        }
        if (infPattern.size() > 0) {
            boolean done = false;
            while (!done) {
                done = StaticPatternContainer.addPattern(allInf);
            }
            done = false;
            while (!done) {
                done = StaticPatternContainer.addBitPattern(infPattern);
            }
        }
        return infeasible;
    }

    /**
     * @param finalBooleanSize
     *            number of compressed reactions
     * @return true if model is feasible at start
     */
    protected boolean isInitialModelFeasible(int finalBooleanSize) {
        for (int i = 0; i < finalBooleanSize; i++) {
            BitSet mode = new BitSet(finalBooleanSize);
            mode.set(i);
            mode.flip(0, finalBooleanSize);
            if (!isFeasible(mode, finalBooleanSize)) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if initial lp problem is feasible by check of each single reaction
     *
     * @return true if model is feasible
     */
    protected boolean isInitialModelFeasible() {
        Iterator<Integer> iter = m_activeCompressedSingleReactions.iterator();
        int max = 0;
        while (iter.hasNext()) {
            int act = iter.next();
            if (act > max) {
                max = act;
            }
        }
        for (int i = 0; i < max; i++) {
            double[] testMode = new double[max];
            for (int j = 0; j < max; j++) {
                testMode[j] = (i == j) ? 1 : 0;
            }
            defineMode(testMode);
            if (!isFeasible()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return list of infeasible reactions
     */
    protected ArrayList<Integer> getInfeasibleReactions() {
        ArrayList<Integer> inf = new ArrayList<Integer>();
        Iterator<Integer> iter = m_activeCompressedSingleReactions.iterator();
        int max = 0;
        while (iter.hasNext()) {
            int act = iter.next();
            if (act > max) {
                max = act;
            }
        }
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < 2; j++) {
                int minus = (j < 1) ? 1 : -1;
                double[] testMode = new double[max];
                for (int k = 0; k < max; k++) {
                    testMode[k] = (i == k) ? 1 * minus : 0;
                }
                defineMode(testMode);
                if (!isFeasible()) {
                    inf.add(i * minus);
                }
            }
        }
        return inf;
    }

    /**
     * @return true if model is feasible
     */
    protected boolean isFeasible() {
        try {
            // if (m_objectiveSet)
            // {
            return m_solver.solve();
            // }
            // return true;
        } catch (IloException e) {
            System.out.println(e);
        }
        return true;
    }

    /**
     * @param mode
     *            as double array
     * @return true if mode is feasible
     */
    protected boolean isFeasible(double[] mode) {
        defineMode(mode);
        return isFeasible();
    }

    /**
     * @param mode
     *            as BitSet, active = 0
     * @param booleanSize
     *            , size of BitSet
     * @return true if mode is feasible
     */
    protected boolean isFeasible(BitSet mode, int booleanSize) {
        defineMode(mode, booleanSize);
        return isFeasible();
    }

    /**
     * create lp model
     *
     * @param stoich
     *            stoichiometric matrix
     * @param metabolites
     *            map of metabolites
     * @param metaboliteOrder
     *            indices and names of metabolites
     * @param lpFile
     *            name of output file for lp problem
     * @param lpVariableFile
     *            name of output file for lp variables
     * @throws IloException
     */
    private void defineModel(String lpFile, String lpVariableFile)
            throws IloException {
        m_solver = new CplexHandler();
        m_dfGConstrSize = 0;
        m_formationStartInd = m_activeMetaboliteKeys.length;
        try {
            defineFormationEnergyConstraintsForReactions();
            defineBounds();
            addFormationEnergyConstraints();
            defineReactionEnergyConstraints();
            predefineCompressedReactions(m_finalBooleanSize);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
        if (lpFile != null) {
            m_solver.exportModel(lpFile);
        }
        if (lpVariableFile != null) {
            InfoWriter iw = new InfoWriter(lpVariableFile, false);
            iw.printVariableInfo(m_variableNames);
            iw.close();
        }
    }

    /**
     * define the bounds of the variables
     *
     * @param stoich
     *            stoichiometric matrix
     * @param metabolites
     *            map of metabolites
     * @param metaboliteOrder
     *            indices and names of metabolites
     * @throws IloException
     */
    private void defineBounds() throws IloException {
        // define size of arrays
        int dfG_bounds = m_lpFormationHash.size();
        int reactionBounds = m_activeSingleReactionKeys.length;
        m_drgConstrStartIndex = dfG_bounds;

        // define start index
        m_reactionStartInd = m_formationStartInd + dfG_bounds;
        int total = m_reactionStartInd + reactionBounds;
        // initialize bounds
        m_lb = new double[total];
        m_ub = new double[total];
        // define boundsm_orphan
        defineConcentrationBounds();
        defineFormationEnergyBounds();
        defineReactionEnergyBounds();
        m_solver.addVariables(m_lb, m_ub);
    }

    /**
     * define bounds of metabolites by their concentration
     *
     * @param metabolites
     *            map of metabolites
     * @param metaboliteOrder
     *            indices and names of metabolites
     */
    private void defineConcentrationBounds() {
        for (int i = 0; i < m_activeMetaboliteKeys.length; i++) {
            int index = m_activeMetaboliteKeys[i];
            Metabolite m = m_metaboliteInfo.get(m_metaboliteOrder.get(index));
            m_lb[i] = Math.log(m.getCmin());
            m_ub[i] = Math.log(m.getCmax());
            m_variableNames.put(i, m.getAbbr());
        }
    }

    /**
     * set bounds of the delta G formation variables to free
     *
     */
    private void defineFormationEnergyBounds() {
        for (int i = m_formationStartInd; i < m_reactionStartInd; i++) {
            m_lb[i] = -Double.MAX_VALUE;
            m_ub[i] = Double.MAX_VALUE;
        }
    }

    /**
     * set bounds of the delta G reaction variables to <= 0
     *
     */
    private void defineReactionEnergyBounds() {
        for (int i = m_reactionStartInd; i < m_lb.length; i++) {
            m_lb[i] = -Double.MAX_VALUE;
            m_ub[i] = ThermodynamicParameters.getDrgUb();
        }
    }

    /**
     * add constraints corresponding to the mode to the lp problem
     *
     * @param mode
     */
    protected void defineMode(double[] mode) {
        try {
            m_solver.removeRows(m_drgConstrStartIndex);
            for (int i = 0; i < mode.length; i++) {
                if (mode[i] != 0) {
                    if (m_activeCompressedReactionMap.containsKey(i)) {
                        HashSet<Integer> indices = m_activeCompressedReactionMap
                                .get(i);
                        Iterator<Integer> iter = indices.iterator();
                        while (iter.hasNext()) {
                            int index = m_activeSingleReactionMap.get(iter
                                    .next());
                            LpReaction rx = m_lpReactions.get(index);
                            int dir = (mode[i] > 0) ? CplexHandler.DIRECTION_FWD
                                    : CplexHandler.DIRECTION_REV;
                            m_solver.addReaction(rx, dir);
                        }
                    }
                }
            }
        } catch (IloException e) {
            System.out.println(e);
        }
    }

    /**
     * @param name
     *            name of reaction
     * @return key of LpReaction
     */
    private int getLpReactionKey(String name) {
        int direction = (name.startsWith(RX_PREFIX)) ? 1 : 2;
        Set<Integer> keys = m_lpReactions.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            int key = iter.next();
            switch (direction) {
            case 1:
                if (m_lpReactions.get(key).getFwdName().equals(name)) {
                    return key;
                }
                break;
            case 2:
                if (m_lpReactions.get(key).getRevName().equals(name)) {
                    return key;
                }
                break;
            }
        }
        return -1;
    }

    /**
     * predefines compressed reactions for linear program
     *
     * @param booleanSize
     *            number of compressed reactions
     */
    private void predefineCompressedReactions(int booleanSize) {
        for (int i = 0; i < booleanSize; i++) {
            String pat = m_patternConverter.getReactionsFromIndex(i);
            String[] rx = pat.contains("::") ? pat.split("::")
                    : new String[] { pat };
            boolean addToList = false;
            PredefinedReaction preRx = new PredefinedReaction();
            for (String x : rx) {
                int key = getLpReactionKey(x);
                if (key > -1) {
                    int dir = (x.startsWith(RX_PREFIX)) ? CplexHandler.DIRECTION_FWD
                            : CplexHandler.DIRECTION_REV;
                    preRx.addReaction(m_lpReactions.get(key), dir);
                    addToList = true;
                }
            }
            if (addToList) {
                m_predefinedReactions.put(i, preRx);
            }
        }
    }

    /**
     * creates constraints for linear problem
     *
     * @param mode
     *            BitSet, active reaction = 0
     * @param booleanSize
     *            number of converted reactions
     */
    protected void defineMode(final BitSet mode, int booleanSize) {
        BitSet t_mode = (BitSet) mode.clone();
        t_mode.flip(0, booleanSize);
        try {
            m_solver.removeRows(m_drgConstrStartIndex);
            int i = -1;
            while ((i = t_mode.nextSetBit(i + 1)) > -1) {
                if (m_predefinedReactions.containsKey(i)) {
                    PredefinedReaction pr = m_predefinedReactions.get(i);
                    ArrayList<LpReaction> rx = pr.getReactions();
                    ArrayList<Integer> dir = pr.getDirections();
                    for (int j = 0; j < rx.size(); j++) {
                        m_solver.addReaction(rx.get(j), dir.get(j));
                    }
                }
            }
        } catch (IloException e) {
            System.out.println(e);
        }
    }

    public BitSet getConflictBitPattern(final BitSet mode, int booleanSize,
            ArrayList<String> conflicts) {
        BitSet t_mode = (BitSet) mode.clone();
        t_mode.flip(0, booleanSize);
        BitSet bitPattern = new BitSet(booleanSize);
        int i = -1;
        while ((i = t_mode.nextSetBit(i + 1)) > -1) {
            if (m_predefinedReactions.containsKey(i)) {
                PredefinedReaction pr = m_predefinedReactions.get(i);
                ArrayList<LpReaction> rx = pr.getReactions();
                for (int j = 0; j < rx.size(); j++) {
                    if (conflicts.contains(rx.get(j).getFwdName())) {
                        bitPattern.set(i);
                    }
                    if (conflicts.contains(rx.get(j).getRevName())) {
                        bitPattern.set(i);
                    }
                }
            }
        }
        return bitPattern;
    }

    /**
     * @return list of conflicting reactions
     */
    protected ArrayList<String> getConflictReactions() {
        try {
            return m_solver.getConflictReactions();
        } catch (IloException e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * @param m
     * @param dfGname
     * @param lpIndex
     * @param innerCompartment
     * @return instance of {@link LpFormation} or null if row already exists in
     *         m_lpFormationHash
     */
    private LpFormation getLpFormation(Metabolite m, String dfGname, int lpIndex) {
        String name = m.getName();
        String abbr = m.getAbbr();
        double dfG = m_gibbsC.getDfG(name);
        int metaboliteIndex = m_activeMetaboliteNameMap.get(abbr);
        int[] cols = new int[] { metaboliteIndex, lpIndex };
        double[] vals = new double[] { m_rtNeg, 1 };
        LpFormation form = new LpFormation(lpIndex, cols, vals, dfG, dfGname);
        return form;
    }

    /**
     * search for needed dfG constraints and defines {@link LpFormation}
     * instances for them
     */
    private void defineFormationEnergyConstraintsForReactions() {
        m_lpFormationHash = new HashMap<String, LpFormation>();
        m_reactionBuildHelper = new ArrayList<ReactionBuildHelper>();
        int reactions = m_activeSingleReactionKeys.length;
        int metabolites = m_activeMetaboliteKeys.length;
        for (int i_rx = 0; i_rx < reactions; i_rx++) {
            int actualReaction = m_activeSingleReactionKeys[i_rx];
            String actualReactionName = m_singleReactionNameMap
                    .get(actualReaction);
            // left equation side
            ArrayList<Double> leftVal = new ArrayList<Double>();
            ArrayList<String> leftMetabols = new ArrayList<String>();
            ArrayList<Metabolite> leftMetab = new ArrayList<Metabolite>();
            ArrayList<String> leftName = new ArrayList<String>();

            // right equation side
            ArrayList<Double> rightVal = new ArrayList<Double>();
            ArrayList<String> rightMetabols = new ArrayList<String>();
            ArrayList<Metabolite> rightMetab = new ArrayList<Metabolite>();
            ArrayList<String> rightName = new ArrayList<String>();

            // set metabolites of actual reaction
            for (int i_metab = 0; i_metab < metabolites; i_metab++) {
                int actualMetabolite = m_activeMetaboliteKeys[i_metab];
                double coeff = m_stoich[actualMetabolite][actualReaction];
                if (coeff != 0) {
                    Metabolite m = m_metaboliteInfo
                            .get(m_activeMetaboliteReverseNameMap.get(i_metab));
                    if (!m.isProton()) {
                        String name = m.getName();
                        if (coeff < 0) {
                            leftVal.add(coeff);
                            leftMetabols.add(name);
                            leftMetab.add(m);
                        } else {
                            rightVal.add(coeff);
                            rightMetabols.add(name);
                            rightMetab.add(m);
                        }
                    }
                }
            }

            int leftSize = leftMetabols.size();
            int rightSize = rightMetabols.size();
            for (int li = 0; li < leftSize; li++) {
                Metabolite m = leftMetab.get(li);
                String dfGname = m.getAbbr();
                leftName.add(li, dfGname);
                if (!m_lpFormationHash.containsKey(dfGname)) {
                    LpFormation form = getLpFormation(m, dfGname,
                            m_formationStartInd + m_dfGConstrSize);
                    if (form.getName().equals(dfGname)) {
                        m_lpFormationHash.put(dfGname, form);
                        m_variableNames.put(m_formationStartInd
                                + m_dfGConstrSize, "DfG_" + dfGname);
                        m_dfGConstrSize++;
                    }
                }
            }
            for (int ri = 0; ri < rightSize; ri++) {
                Metabolite m = rightMetab.get(ri);
                String dfGname = m.getAbbr();
                rightName.add(ri, dfGname);
                if (!m_lpFormationHash.containsKey(dfGname)) {
                    LpFormation form = getLpFormation(m, dfGname,
                            m_formationStartInd + m_dfGConstrSize);
                    if (form.getName().equals(dfGname)) {
                        m_lpFormationHash.put(dfGname, form);
                        m_variableNames.put(m_formationStartInd
                                + m_dfGConstrSize, "DfG_" + dfGname);
                        m_dfGConstrSize++;
                    }
                }
            }
            ReactionBuildHelper rxHelp = new ReactionBuildHelper(
                    actualReactionName, leftVal, leftMetabols, leftMetab,
                    leftName, rightVal, rightMetabols, rightMetab, rightName);
            m_reactionBuildHelper.add(rxHelp);
        }
    }

    /**
     * add constraint for formation energy to linear problem
     */
    private void addFormationEnergyConstraints() {
        Set<String> keySet = m_lpFormationHash.keySet();
        try {
            Iterator<String> iter = keySet.iterator();
            while (iter.hasNext()) {
                m_solver.addFormation(m_lpFormationHash.get(iter.next()));
            }
        } catch (IloException e) {
            System.out.println(e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * predefines formation energy constraints for later addition to linear
     * problem
     *
     * @throws IloException
     * @throws MembranePotentialException
     */
    private void defineReactionEnergyConstraints() throws IloException {
        for (int i = 0; i < m_reactionBuildHelper.size(); i++) {
            LpReaction lpReaction = new LpReaction(m_reactionStartInd + i);
            ReactionBuildHelper r = m_reactionBuildHelper.get(i);
            String actualReactionName = r.getReactionName();
            ArrayList<Double> leftVal = r.getLeftVal();
            ArrayList<Metabolite> leftMetab = r.getLeftMetabolite();
            ArrayList<String> leftName = r.getLeftName();
            ArrayList<Double> rightVal = r.getRightVal();
            ArrayList<Metabolite> rightMetab = r.getRightMetabolite();
            ArrayList<String> rightName = r.getRightName();
            int leftSize = leftMetab.size();
            int rightSize = rightMetab.size();
            int totalSize = leftSize + rightSize;

            // forward reaction
            int[] fwdCols = new int[totalSize];
            double[] fwdVals = new double[totalSize];
            for (int li = 0; li < leftSize; li++) {
                fwdCols[li] = m_lpFormationHash.get(leftName.get(li))
                        .getFormationIndex();
                fwdVals[li] = leftVal.get(li) * -1;
            }
            for (int ri = 0; ri < rightSize; ri++) {
                int ind = leftSize + ri;
                fwdCols[ind] = m_lpFormationHash.get(rightName.get(ri))
                        .getFormationIndex();
                fwdVals[ind] = rightVal.get(ri) * -1;
            }
            lpReaction.setFwdReaction(fwdCols, fwdVals, RX_PREFIX
                    + actualReactionName);

            // reverse reaction
            int[] revCols = new int[totalSize];
            double[] revVals = new double[totalSize];
            for (int fi = 0; fi < leftSize; fi++) {
                revCols[fi] = m_lpFormationHash.get(leftName.get(fi))
                        .getFormationIndex();
                revVals[fi] = leftVal.get(fi);
            }
            for (int ri = 0; ri < rightSize; ri++) {
                int ind = leftSize + ri;
                revCols[ind] = m_lpFormationHash.get(rightName.get(ri))
                        .getFormationIndex();
                revVals[ind] = rightVal.get(ri);
            }
            lpReaction.setRevReaction(revCols, revVals, RX_REV_PREFIX
                    + actualReactionName);

            // add to reaction map
            m_lpReactions.put(i, lpReaction);
            m_variableNames.put(m_reactionStartInd + i, RX_PREFIX
                    + actualReactionName);

            m_solver.addReaction(lpReaction, CplexHandler.DIRECTION_FWD);
            m_solver.addReaction(lpReaction, CplexHandler.DIRECTION_REV);
        }
    }

    /**
     * export the model to output file
     *
     * @param filename
     *            of outputfile
     */
    protected void export(String filename) {
        try {
            m_solver.exportModel(filename);
        } catch (Exception e) {
            System.out.println(this.getClass() + "   Exit with IloException");
        }
    }

    /**
     * print the model
     *
     */
    protected void printModel() {
        try {
            m_solver.print();
        } catch (IloException e) {
            System.out.println(e);
        }
    }
}
